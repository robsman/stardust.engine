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
import org.eclipse.stardust.engine.core.runtime.beans.IWorkItem;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;


/**
 * @author stephan.born
 * @version $Revision: $
 */
public class WorkItemPiFetchPredicate implements FetchPredicate
{
   private static final Logger trace = LogManager
         .getLogger(WorkItemPiFetchPredicate.class);

   private static final FieldRef[] REFERNCED_WI_FIELDS = new FieldRef[] { WorkItemBean.FR__PROCESS_INSTANCE };

   private final Set processOIDs;

   public WorkItemPiFetchPredicate(Set processOIDs)
   {
      this.processOIDs = processOIDs;
   }

   public FieldRef[] getReferencedFields()
   {
      return REFERNCED_WI_FIELDS;
   }

   public boolean accept(Object o)
   {
      if (o instanceof ResultSet)
      {
         ResultSet result = (ResultSet) o;

         try
         {
            return processOIDs.contains(new Long(result
                  .getLong(WorkItemBean.FIELD__PROCESS_INSTANCE)));
         }
         catch (Exception e)
         {
            trace.warn("", e);

            return false;
         }
      }
      else if (o instanceof IWorkItem)
      {
         return processOIDs.contains(new Long(((IWorkItem) o).getProcessInstanceOID()));
      }
      else
      {
         return false;
      }
   }
}
