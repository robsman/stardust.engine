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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.IEventActionType;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.EventActionValidator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ActionBean extends IdentifiableElementBean
{
   private IEventActionType type = null;

   public ActionBean()
   {
   }

   public ActionBean(String id, String name)
   {
     super(id, name);
   }

   public PluggableType getType()
   {
      return type;
   }

   public void setActionType(IEventActionType type)
   {
      this.type = type;
   }

   public void checkConsistency(List inconsistencies, String name)
   {
      checkId(inconsistencies);
      if (StringUtils.isEmpty(getName()))
      {
         inconsistencies.add(new Inconsistency(name + " does not have a Name.",
               this, Inconsistency.ERROR));
      }
      IEventActionType type = (IEventActionType) getType();
      if (type == null)
      {
         inconsistencies.add(new Inconsistency(name + " does not have a type.",
               this, Inconsistency.ERROR));
      }
      else
      {
         EventActionValidator validator = (EventActionValidator) ValidatorUtils.getValidator(type, this, inconsistencies);
         if (null != validator)
         {
            Collection coll = validator.validate(getAllAttributes());
            for (Iterator i = coll.iterator(); i.hasNext();)
            {
               Inconsistency x = (Inconsistency) i.next();
               inconsistencies.add(new Inconsistency(x.getMessage(), this, x.getSeverity()));
            }
         }
      }
   }
}
