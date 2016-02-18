/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;


public class DataUtils
{

   /**
    * Returns all dataIds used in process.
    * <p>
    * Works for data used in: FormalParameters, DataPaths, DataMappings,
    * TransitionConditions, Triggers, EventHandler, EventActions, ConditionalPerformers, DepartmentRuntimeBinding.
    *
    * @param model
    * @param unqualifiedProcessId
    * @return
    */
   public static Set<String> getDataForProcess(String unqualifiedProcessId, IModel model)
   {
      IProcessDefinition pd = model.findProcessDefinition(unqualifiedProcessId);

      if (pd != null)
      {
         return getDataUsedInProcess(pd, model);
      }
      else
      {
         return Collections.EMPTY_SET;
      }
   }

   public static String getUnqualifiedProcessId(String processId)
   {
      if (processId != null)
      {
         return QName.valueOf(processId).getLocalPart();
      }

      return processId;
   }

   private static Set<String> getDataUsedInProcess(IProcessDefinition pd, IModel model)
   {
      Set<String> dataIdsUsed = new HashSet<String>();
      
      // collect all data ids
      Set<String> allDataIds = new HashSet<String>();
      for (IData iData : model.getData())
      {
         allDataIds.add(iData.getId());
      }

      // FormalParameters
      for (IFormalParameter iFormalParameter : pd.getFormalParameters())
      {
         dataIdsUsed.add(iFormalParameter.getData().getId());
      }

      // DataPaths
      for (IDataPath iDataPath : pd.getDataPaths())
      {
         IData data = iDataPath.getData();
         if (data != null)
         {
            dataIdsUsed.add(iDataPath.getData().getId());
         }         
      }

      for (IActivity iActivity : pd.getActivities())
      {
         // DataMappings
         for (IDataMapping iDataMapping : iActivity.getDataMappings())
         {
            dataIdsUsed.add(iDataMapping.getData().getId());
         }

         // Event Actions
         ModelElementList<IEventHandler> eventHandlers = iActivity.getEventHandlers();
         for (IEventHandler iEventHandler : eventHandlers)
         {
            Boolean timerDataUsed = (Boolean) iEventHandler.getAttribute(PredefinedConstants.TIMER_CONDITION_USE_DATA_ATT);
            if (Boolean.TRUE.equals(timerDataUsed))
            {
               String timerDataId = (String) iEventHandler.getAttribute(PredefinedConstants.TIMER_CONDITION_DATA_ATT);
               if (!StringUtils.isEmpty(timerDataId))
               {
                  dataIdsUsed.add(timerDataId);
               }
            }

            Iterator<IEventAction> allEventActions = iEventHandler.getAllEventActions();
            while (allEventActions.hasNext())
            {
               IAction iEventAction = (IAction) allEventActions.next();
               addUsedDataId(iEventAction, dataIdsUsed);
            }
            
            Iterator<IEventAction> allBindActions = iEventHandler.getAllBindActions();
            while (allBindActions.hasNext())
            {
               IAction iBindEventAction = (IAction) allBindActions.next();
               addUsedDataId(iBindEventAction, dataIdsUsed);

            }
            
            Iterator<IEventAction> allUnbindActions = iEventHandler.getAllUnbindActions();
            while (allUnbindActions.hasNext())
            {
               IAction iEventAction = (IAction) allUnbindActions.next();
               addUsedDataId(iEventAction, dataIdsUsed);
            }
         }
      }

      // Transitions
      for (ITransition iTransition : pd.getTransitions())
      {
         for (String dataId : allDataIds)
         {
            String condition = iTransition.getCondition();
            // (fh) this is incorrect, for example
            // if dataId is "e" and condition is "true" it does not mean
            // that the data "e" is used by this transition condition.
            if (condition != null && condition.contains(dataId))
            {
               dataIdsUsed.add(dataId);
            }
         }
      }

      // Triggers
      for (ITrigger iTrigger : pd.getTriggers())
      {
         if (PredefinedConstants.SCAN_TRIGGER.equals(iTrigger.getType().getId()))
         {
            Iterator<IAccessPoint> allAccessPoints = iTrigger.getAllAccessPoints();
            while (allAccessPoints.hasNext())
            {
               IAccessPoint accessPoint = allAccessPoints.next();
               if (allDataIds.contains(accessPoint.getId()))
               {
                  dataIdsUsed.add(accessPoint.getId());
               }
            }
         }
         
         ModelElementList<IParameterMapping> allParameterMappings = iTrigger.getParameterMappings();
         for (IParameterMapping parameterMapping : allParameterMappings)
         {
            IData data = parameterMapping.getData();
            if (data != null)
            {
               dataIdsUsed.add(data.getId());
            }
         }
      }

      // ConditionalPerformers
      ModelElementList<IModelParticipant> participants = model.getParticipants();
      for (IModelParticipant iModelParticipant : participants)
      {
         if (iModelParticipant instanceof IConditionalPerformer)
         {
            IConditionalPerformer iConditionalPerformer = (IConditionalPerformer) iModelParticipant;
            IData data2 = iConditionalPerformer.getData();
            if (data2 != null)
            {
               if (allDataIds.contains(data2.getId()))
               {
                  if (isParticipantUsedInProcess(iConditionalPerformer, pd))
                  {
                     dataIdsUsed.add(data2.getId());
                  }
               }
            }
         }
         else if (iModelParticipant instanceof IOrganization)
         {
            // Department runtime binding
            IOrganization org = (IOrganization) iModelParticipant;
            Boolean bound = (Boolean) org.getAttribute(PredefinedConstants.BINDING_ATT);
            if (bound != null && bound)
            {
               Object attribute = org.getAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);
               if (attribute != null)
               {
                  String dataId = (String) attribute;
                  if (allDataIds.contains(dataId))
                  {
                     if (isParticipantUsedInProcess(org, pd))
                     {
                        dataIdsUsed.add(dataId);
                     }

                     Iterator allParticipants = org.getAllParticipants();
                     while (allParticipants.hasNext())
                     {
                        IModelParticipant participant = (IModelParticipant) allParticipants.next();
                        if (isParticipantUsedInProcess(participant, pd))
                        {
                           dataIdsUsed.add(dataId);
                        }
                     }
                  }
               }
            }
         }
      }

      return dataIdsUsed;
   }

   private static void addUsedDataId(IAction iEventAction,
         Set<String> dataIdsUsed)
   {

      String mailBodyDataId = (String) iEventAction.getAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT);
      if (!StringUtils.isEmpty(mailBodyDataId))
      {
         dataIdsUsed.add(mailBodyDataId);
      }
      String setDataDataId = (String) iEventAction.getAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT);
      if (!StringUtils.isEmpty(setDataDataId))
      {
         dataIdsUsed.add(setDataDataId);
      }
      String excludedPerformerDataId = (String) iEventAction.getAttribute(PredefinedConstants.EXCLUDED_PERFORMER_DATA);
      if (!StringUtils.isEmpty(excludedPerformerDataId))
      {
         dataIdsUsed.add(excludedPerformerDataId);
      }
   }

   /**
    * Limited to activity performer.
    *
    * @param participant
    * @param pd
    * @return
    */
   private static boolean isParticipantUsedInProcess(IModelParticipant participant,
         IProcessDefinition pd)
   {
      boolean include = false;

      ModelElementList<IActivity> activities = pd.getActivities();
      for (IActivity iActivity : activities)
      {
         IModelParticipant performer = iActivity.getPerformer();
         if (performer != null && performer.getId().equals(participant.getId()))
         {
            include = true;
            break;
         }
      }

      return include;
   }

}
