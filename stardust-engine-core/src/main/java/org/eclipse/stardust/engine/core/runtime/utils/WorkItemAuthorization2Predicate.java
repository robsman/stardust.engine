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
package org.eclipse.stardust.engine.core.runtime.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.UserPK;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;

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

   public boolean addPrefetchDataHints(Query query)
   {
      boolean returnValue = super.addPrefetchDataHints(query);
      FilterAndTerm queryFilter = query.getFilter();            
      getExcludeUserFilter(queryFilter);
      
      return returnValue;
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
               long activityInstanceOid = rs.getLong(WorkItemBean.FIELD__ACTIVITY_INSTANCE);               
               long activityRtOid = rs.getLong(WorkItemBean.FIELD__ACTIVITY);
               long modelOid = rs.getLong(WorkItemBean.FIELD__MODEL);
               int performerKind = rs.getInt(WorkItemBean.FIELD__PERFORMER_KIND);
               long performer = rs.getLong(WorkItemBean.FIELD__PERFORMER);
               
               long scopeProcessInstanceOid = 0;
               try
               {
                  scopeProcessInstanceOid = rs.getLong(WorkItemBean.FIELD__SCOPE_PROCESS_INSTANCE);
               }
               catch (SQLException x)
               {
                  // leave it to 0 if column cannot be found
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
               
               if(isExcludedUser(activityInstanceOid, modelOid))
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
      
   public boolean isExcludedUser(long activityRtOid, long modelOid)
   {
      IUser currentUser = SecurityProperties.getUser();      
      long currentPerformer = currentUser.getOID();
      
      ActivityInstanceBean bean = ActivityInstanceBean.findByOID(activityRtOid);
      IActivity activity = bean.getActivity();
      
      long processInstanceOID = bean.getProcessInstanceOID();      
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processInstanceOID);
      IProcessDefinition processDefinition = processInstance.getProcessDefinition();
            
      if (activity.hasEventHandlers(
            PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION))
      {      
         for (int k = 0; k < activity.getEventHandlers().size(); ++k)
         {
            IEventHandler handler = (IEventHandler) activity.getEventHandlers().get(k);
            if (((IEventConditionType) handler.getType()).getImplementation() != EventType.Pull)
            {
               for (Iterator l = handler.getAllEventActions(); l.hasNext();)
               {
                  IEventAction action = (IEventAction) l.next();
                  PluggableType type = action.getType();
                  String instanceName = type.getStringAttribute(PredefinedConstants.ACTION_CLASS_ATT);
                  String excludeUserAction = PredefinedConstants.EXCLUDE_USER_ACTION_CLASS;
                  Class classFromClassName = Reflect.getClassFromClassName(excludeUserAction, false);
                  if(classFromClassName != null)
                  {
                     excludeUserAction = classFromClassName.getName();
                  }
                  
                  if(instanceName.equals(excludeUserAction))
                  {
                     Map<String, Object> attributes = action.getAllAttributes();
                     String dataId = (String) attributes.get(PredefinedConstants.EXCLUDED_PERFORMER_DATA);
                     String dataPath = (String) attributes
                           .get(PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH);
                     IData data = ModelUtils.getData(processDefinition, dataId);
                     IDataValue dataValue = processInstance.getDataValue(data);
                     
                     Object value = dataValue.getValue();
                     if(!StringUtils.isEmpty(dataPath))
                     {
                        ExtendedAccessPathEvaluator evaluator = SpiUtils
                        .createExtendedAccessPathEvaluator(data.getType());
                        AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(
                        processInstance, null, null, null);
                        value = evaluator.evaluate(data, dataValue.getValue(), dataPath, evaluationContext);                        
                     }
                     
                     Long longValue = null;
                     if(value instanceof Long)
                     {
                        longValue = (Long) value;
                     }
                     else if(value instanceof UserPK)
                     {
                        try
                        {
                           longValue = Long.parseLong(value.toString());
                        }
                        catch (NumberFormatException e)
                        {
                        }                        
                     }
                                          
                     if(longValue != null && currentPerformer == longValue)
                     {
                        return true;
                     }                        
                  }
               }
            }
         }
      }
      
      return false;   
   }      
}