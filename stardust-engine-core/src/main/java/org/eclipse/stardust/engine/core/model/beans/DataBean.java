/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailDataBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.StructuredDataValidator;


/**
 * @author mgille
 * @version $Revision$
 */
public class DataBean extends IdentifiableElementBean implements IData
{
   private IDataType type = null;

   private IReference externalReference = null;

   DataBean()
   {
   }

   public DataBean(String id, IDataType type, String name, String description,
         boolean predefined, Map attributes)
   {
      super(id, name);
      setDescription(description);
      this.type = type;
      setPredefined(predefined);
      setAllAttributes(attributes);
      if (attributes != null && !attributes.containsKey(PredefinedConstants.BROWSABLE_ATT))
      {
         setAttribute(PredefinedConstants.BROWSABLE_ATT, Boolean.TRUE);
      }
   }

   public String toString()
   {
      return "Data: " + getName();
   }

   public PluggableType getType()
   {
      return type;
   }

   public void setDataType(IDataType type)
   {
      markModified();
      this.type = type;
      removeAllAttributes();
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the data.
    */
   public void checkConsistency(List inconsistencies)
   {
      super.checkConsistency(inconsistencies);
      checkId(inconsistencies);

      // check for unique Id
      IData d = ((IModel) getModel()).findData(getId());
      if (d != null && d != this)
      {
         BpmValidationError error = BpmValidationError.DATA_DUPLICATE_ID_FOR_DATA.raise(getName());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
      }

      if (null != getId())
      {
         // check id to fit in maximum length
         if (getId().length() > AuditTrailDataBean.getMaxIdLength())
         {
            BpmValidationError error = BpmValidationError.DATA_ID_FOR_DATA_EXCEEDS_MAXIMUM_LENGTH_OF_CHARACTERS.raise(
                  getName(), AuditTrailDataBean.getMaxIdLength());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }
      }

      // use the validator, if present, to check the attributes validity
      ExtendedDataValidator validator = (ExtendedDataValidator) ValidatorUtils.getValidator(getType(), this, inconsistencies);
      if (null != validator)
      {
         Collection problems = validator.validate(getAllAttributes());
         for (Iterator i = problems.iterator(); i.hasNext();)
         {
            Inconsistency inc = (Inconsistency) i.next();
            if (inc.getError() != null)
            {
               inconsistencies.add(new Inconsistency(inc.getError(), this,
                     inc.getSeverity()));
            }
            inconsistencies.add(new Inconsistency(inc.getMessage(), this,
                  inc.getSeverity()));
         }
         
         if(validator instanceof StructuredDataValidator)
         {
            problems = ((StructuredDataValidator) validator).validate(this);
            for (Iterator i = problems.iterator(); i.hasNext();)
            {
               Inconsistency inc = (Inconsistency) i.next();
               if (inc.getError() != null)
               {
                  inconsistencies.add(new Inconsistency(inc.getError(), this,
                        inc.getSeverity()));
               }
               inconsistencies.add(new Inconsistency(inc.getMessage(), this,
                     inc.getSeverity()));
            }
         }
      }
   }

   public Direction getDirection()
   {
      // @todo (france, ub):
      // (fh) isn't a data object a bidirectional accesspoint ?
      return Direction.IN_OUT;
   }

   public IReference getExternalReference()
   {
      return externalReference;
   }

   public void setExternalReference(IReference externalReference)
   {
      this.externalReference = externalReference;
   }
}
