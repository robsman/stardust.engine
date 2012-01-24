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
package org.eclipse.stardust.engine.api.query;

import java.sql.ResultSet;
import java.util.Set;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.FetchPredicate;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ActivityInstancePiFetchPredicate implements FetchPredicate
{
   private static final Logger trace = LogManager.getLogger(ActivityInstancePiFetchPredicate.class);

   private static final FieldRef[] REFERNCED_AI_FIELDS =
         new FieldRef[] {ActivityInstanceBean.FR__PROCESS_INSTANCE};
   
   private final Set processOIDs;

   public ActivityInstancePiFetchPredicate(Set processOIDs)
   {
      this.processOIDs = processOIDs;
   }

   public FieldRef[] getReferencedFields()
   {
      return REFERNCED_AI_FIELDS;
   }

   public boolean accept(Object o)
   {
      if (o instanceof ResultSet)
      {
         ResultSet result = (ResultSet) o;

         try
         {
            return processOIDs.contains(new Long(
                  result.getLong(ActivityInstanceBean.FIELD__PROCESS_INSTANCE)));
         }
         catch (Exception e)
         {
            trace.warn("", e);

            return false;
         }
      }
      else if (o instanceof IActivityInstance)
      {
         return processOIDs.contains(new Long(((IActivityInstance) o)
               .getProcessInstanceOID()));
      }
      else
      {
         return false;
      }
   }
}
