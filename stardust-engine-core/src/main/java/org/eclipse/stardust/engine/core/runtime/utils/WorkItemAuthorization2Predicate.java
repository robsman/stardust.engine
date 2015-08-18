/*******************************************************************************
 * Copyright (c) 2011. 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.DataPrefetchHint;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.runtime.beans.IWorkItem;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemAdapter;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;

/**
 * Predicate class which is used to restrict access to work items based on its activity
 * declarative security permission.
 *
 * @author stephan.born
 * @version $Revision: 5162 $
 */
public class WorkItemAuthorization2Predicate extends AbstractAuthorization2Predicate
{
   private static final Logger trace = LogManager.getLogger(WorkItemAuthorization2Predicate.class);

   @Override
   protected boolean queryRequiresExcludeUserHandling(Query query)
   {
      // ExcludeUser handling always required for Worklistworklist queries.
      return true;
   }

   private static final FieldRef[] LOCAL_STRINGS = {
      WorkItemBean.FR__ACTIVITY,
      WorkItemBean.FR__MODEL,
      WorkItemBean.FR__PERFORMER_KIND,
      WorkItemBean.FR__PERFORMER,
      WorkItemBean.FR__SCOPE_PROCESS_INSTANCE,
      WorkItemBean.FR__DEPARTMENT
   };

   public WorkItemAuthorization2Predicate(AuthorizationContext context)
   {
      super(context);
   }

   public FieldRef[] getLocalFields()
   {
      return LOCAL_STRINGS;
   }

   public boolean accept(Object o)
   {
      boolean result = true;
      if (delegate != null)
      {
         result = delegate.accept(o);
      }
      if (result && super.accept(o))
      {
         if (o instanceof ResultSet)
         {
            ResultSet rs = (ResultSet) o;
            try
            {
               long activityRtOid = rs.getLong(WorkItemBean.FIELD__ACTIVITY);
               long modelOid = rs.getLong(WorkItemBean.FIELD__MODEL);
               int performerKind = rs.getInt(WorkItemBean.FIELD__PERFORMER_KIND);
               long performer = rs.getLong(WorkItemBean.FIELD__PERFORMER);

               Map<String, Long> dataValueOids = CollectionUtils.newMap();
               long scopeProcessInstanceOid = 0;
               try
               {
                  scopeProcessInstanceOid = rs
                        .getLong(WorkItemBean.FIELD__SCOPE_PROCESS_INSTANCE);
                  for (String dataId : dataPrefetchHintFilter.keySet())
                  {
                     DataPrefetchHint dataPrefetchHint = dataPrefetchHintFilter
                           .get(dataId);
                     int columnIdx = dataPrefetchHint.getPrefetchNumberValueColumnIdx();
                     long dataValueOid = rs.getLong(columnIdx);
                     dataValueOids.put(dataId, dataValueOid);
                  }
               }
               catch (SQLException x)
               {
                  // leave it to 0 if column cannot be found
                  trace.warn("", x);
               }

               long departmentOid = rs.getLong(WorkItemBean.FIELD__DEPARTMENT);

               long currentUserPerformer;
               long currentPerformer;
               switch (performerKind)
               {
                  case PerformerType.USER:
                     currentUserPerformer = performer;
                     currentPerformer = 0;
                     break;

                  case PerformerType.MODEL_PARTICIPANT:
                     currentUserPerformer = 0;
                     currentPerformer = performer;
                     break;

                  case PerformerType.USER_GROUP:
                     currentUserPerformer = 0;
                     currentPerformer = -performer;
                     break;

                  default:
                     trace.warn("Unknown perfomer type will not be accepted: " + performerKind);

                     return false;
               }

               if (isExcludedUser(activityRtOid, scopeProcessInstanceOid, modelOid,
                     dataValueOids))
               {
                  return false;
               }

               context.setActivityDataWithScopePi(scopeProcessInstanceOid, activityRtOid, modelOid, currentPerformer,
                     currentUserPerformer, departmentOid);
               return Authorization2.hasPermission(context);
            }
            catch (SQLException e)
            {
               trace.warn("", e);
               return false;
            }
         }
         else if (o instanceof IWorkItem)
         {
            IWorkItem wi = (IWorkItem) o;
            context.setActivityInstance(new WorkItemAdapter(wi));
            return Authorization2.hasPermission(context);
         }
      }
      return result;
   }
}