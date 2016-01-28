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
package org.eclipse.stardust.engine.core.model.utils;

import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.beans.AccessPointBean;
import org.eclipse.stardust.engine.core.model.beans.DataBean;


/**
 * Abstract base class for all persistent parts of workflow models.
 * <p/>
 * A couple of methods are overwritten in order to allow the instantiation
 * of transient model hierarchies read from XML files.
 */
public abstract class IdentifiableElementBean extends ModelElementBean
      implements IdentifiableElement
{
   private String id;

   private String qualifiedId;

   private String name;

   protected IdentifiableElementBean()
   {
   }

   public IdentifiableElementBean(String id, String name)
   {
      this.id = id;
      this.name = name;
   }

   public String getUniqueId()
   {
      return getClass().getName() + ":" + getId();
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      markModified();
      this.name = name;
   }

   // @todo (france, ub): check uniqueness
   public void setId(String id)
   {
      markModified();
      this.id = id;
      qualifiedId = null;
   }
   
   @Override
   public void setParent(ModelElement parent)
   {
      qualifiedId = null;
      super.setParent(parent);
   }
   
   @Override
   public String getQualifiedId()
   {
      if(null == qualifiedId)
      {
         qualifiedId = ModelUtils.getQualifiedId(getModel(), getId());
      }
      return qualifiedId;
   }

   protected void checkConsistency(List<Inconsistency> inconsistencies)
   {
      checkForVariables(inconsistencies, id, "ID");
      checkForVariables(inconsistencies, name, "Name");

      super.checkConsistency(inconsistencies);
   }

   protected void checkId(List<Inconsistency> inconsistencies)
   {
      if (id == null || id.length() == 0)
      {
         BpmValidationError error = BpmValidationError.VAL_HAS_NO_ID.raise(toString());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
      }
      else if ((this instanceof DataBean || this instanceof AccessPointBean)
            && !StringUtils.isValidIdentifier(getId()))
      {
         BpmValidationError error = BpmValidationError.VAL_HAS_INVALID_ID.raise(toString());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }
   }
}