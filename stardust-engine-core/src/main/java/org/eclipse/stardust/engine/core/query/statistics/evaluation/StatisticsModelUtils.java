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
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.query.statistics.api.ParticipantDepartmentPair;
import org.eclipse.stardust.engine.core.query.statistics.utils.AnyActivityVisitor;
import org.eclipse.stardust.engine.core.query.statistics.utils.AnyModelParticipantVisitor;
import org.eclipse.stardust.engine.core.query.statistics.utils.AnyProcessVisitor;
import org.eclipse.stardust.engine.core.query.statistics.utils.IActivityVisitor;
import org.eclipse.stardust.engine.core.query.statistics.utils.IModelParticipantVisitor;
import org.eclipse.stardust.engine.core.query.statistics.utils.IModelVisitor;
import org.eclipse.stardust.engine.core.query.statistics.utils.IProcessVisitor;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;


/**
 * @author rsauer
 * @version $Revision$
 */
public class StatisticsModelUtils
{
   private static final String PARSED_SUFFIX = ":parsed";

   public static final Logger trace = LogManager.getLogger(StatisticsModelUtils.class);

   public static final String PARSED_TARGET_EXECUTION_TIME = PredefinedConstants.PWH_TARGET_EXECUTION_TIME
         + PARSED_SUFFIX;

   public static final String PARSED_TARGET_PROCESSING_TIME = PredefinedConstants.PWH_TARGET_PROCESSING_TIME
         + PARSED_SUFFIX;

   public static final String PARSED_TARGET_COST_PER_EXECUTION = PredefinedConstants.PWH_TARGET_COST_PER_EXECUTION
         + PARSED_SUFFIX;

   public static final String PARSED_ACTUAL_COST_PER_MINUTE = PredefinedConstants.PWH_ACTUAL_COST_PER_MINUTE
         + PARSED_SUFFIX;

   public static void forAnyModel(IModelVisitor visitor)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();

      for (Iterator i = modelManager.getAllModels(); i.hasNext();)
      {
         IModel model = (IModel) i.next();
         if(!visitor.visitModel(model))
         {
            break;
         }
      }
   }

   public static void forAnyModelParticipant(final IModelParticipantVisitor visitor)
   {
      forAnyModel(new AnyModelParticipantVisitor()
      {
         public boolean visitParticipant(IModelParticipant participant)
         {
            return visitor.visitParticipant(participant);
         }
      });
   }

   public static void forAnyProcess(final IProcessVisitor visitor)
   {
      forAnyModel(new AnyProcessVisitor()
      {
         public boolean visitProcess(IProcessDefinition process)
         {
            return visitor.visitProcess(process);
         }
      });
   }

   public static void forAnyActivity(final IActivityVisitor visitor)
   {
      forAnyProcess(new AnyActivityVisitor()
      {
         public boolean visitActivity(IActivity activity)
         {
            return visitor.visitActivity(activity);
         }
      });
   }

   public static Set<ParticipantDepartmentOidPair> modelParticipantIdsToRtOids(
         final Set<ParticipantDepartmentPair> mpIds)
   {
      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      final Map<String, Set<ParticipantDepartmentPair>> lookup = CollectionUtils.newMap();

      if(null != mpIds)
      {
         for(ParticipantDepartmentPair pdPair : mpIds)
         {
            Set<ParticipantDepartmentPair> lookupSet = lookup.get(pdPair.getParticipantId());
            if(lookupSet == null)
            {
               lookupSet = CollectionUtils.newSet();
               lookup.put(pdPair.getParticipantId(), lookupSet);
            }
            lookupSet.add(pdPair);
         }
      }

      final Map<String, Set<ParticipantDepartmentOidPair>> resultTable = CollectionUtils.newMap();

      forAnyModelParticipant(new IModelParticipantVisitor()
      {
         public boolean visitParticipant(IModelParticipant participant)
         {
            String qPId = ModelUtils.getQualifiedId(participant);
            Set<ParticipantDepartmentPair> pdPairs = lookup.get(qPId);
            if (pdPairs != null)
            {
               if ( !resultTable.containsKey(qPId))
               {
                  Set<ParticipantDepartmentOidPair> oidPairs = CollectionUtils.newSet();
                  long runtimeOid = modelManager.getRuntimeOid(participant);

                  for(ParticipantDepartmentPair pdPair : pdPairs)
                  {
                     oidPairs.add(new ParticipantDepartmentOidPair(runtimeOid,
                           pdPair.getDepartmentOid()));
                  }

                  resultTable.put(qPId, oidPairs);
               }
            }
            return true;
         }
      });

      final Set<ParticipantDepartmentOidPair> result = CollectionUtils.newSet();
      for(Set<ParticipantDepartmentOidPair> participantPairs : resultTable.values())
      {
         result.addAll(participantPairs);
      }
      return result;
   }

   public static IModelParticipant findModelParticipant(final long rtOid)
   {
      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      final IModelParticipant result[] = new IModelParticipant[1];

      forAnyModel(new IModelVisitor()
      {
         public boolean visitModel(IModel model)
         {
            if (null == result[0])
            {
               IModelParticipant participant = modelManager.findModelParticipant(
                     model.getModelOID(), rtOid);
               if (null != participant)
               {
                  result[0] = participant;
                  return false;
               }
            }
            return true;
         }
      });

      return result[0];
   }

   public static String findProcessId(final long rtOid)
   {
      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      final IProcessDefinition[] result = new IProcessDefinition[1];

      forAnyModel(new IModelVisitor()
      {
         public boolean visitModel(IModel model)
         {
            if (null == result[0])
            {
               IProcessDefinition process = modelManager.findProcessDefinition(
                     model.getModelOID(), rtOid);
               if (null != process)
               {
                  result[0] = process;
                  return false;
               }
            }
            return true;
         }
      });

      return (null != result[0]) ? ModelUtils.getQualifiedId(result[0]) : null;
   }

   public static Map<String, Long> processIdsToRtOids(final Set<String> processIds)
   {
      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      final Map<String, Long> result = CollectionUtils.newMap();

      forAnyProcess(new IProcessVisitor()
      {
         public boolean visitProcess(IProcessDefinition process)
         {
            if ((null == processIds) || processIds.contains(ModelUtils.getQualifiedId(process)))
            {
               if ( !result.keySet().contains(ModelUtils.getQualifiedId(process)))
               {
                  result.put(ModelUtils.getQualifiedId(process), //
                        modelManager.getRuntimeOid(process));
               }
            }
            return true;
         }
      });

      return result;
   }

   public static String findActivityId(final long rtOid)
   {
      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      final IActivity[] result = new IActivity[1];

      forAnyModel(new IModelVisitor()
      {
         public boolean visitModel(IModel model)
         {
            if (null == result[0])
            {
               IActivity activity = modelManager.findActivity(model.getModelOID(), rtOid);
               if (null != activity)
               {
                  result[0] = activity;
                  return false;
               }
            }
            return true;
         }
      });

      return (null != result[0]) ? result[0].getId() : null;
   }

   public static Set<Long> rtOidsForProcessIds(Set<String> processIds)
   {
      return new HashSet<Long>(processIdsToRtOids(processIds).values());
   }

   public static Set<ParticipantDepartmentOidPair> rtOidsForModelParticipantIds(
         Set<ParticipantDepartmentPair> mpIds)
   {
      return new HashSet<ParticipantDepartmentOidPair>(modelParticipantIdsToRtOids(mpIds));
   }

   public static float getCostPerMinute(IParticipant performer)
   {
      Object costValue = null;
      if (performer instanceof IModelParticipant)
      {
         costValue = doGetCostParameter((IModelParticipant) performer,
               PredefinedConstants.PWH_ACTUAL_COST_PER_MINUTE,
               PARSED_ACTUAL_COST_PER_MINUTE);
      }
      else if (performer instanceof IUserGroup)
      {
         costValue = ((IUserGroup) performer).getPropertyValue(PredefinedConstants.PWH_ACTUAL_COST_PER_MINUTE);
      }

      float costPerMinute = 0.0f;
      if (costValue instanceof Number)
      {
         costPerMinute = ((Number) costValue).floatValue();
      }
      else if (costValue instanceof String)
      {
         try
         {
            costPerMinute = NumberFormat.getInstance()
                  .parse((String) costValue)
                  .floatValue();
         }
         catch (ParseException pe)
         {
            trace.warn("Failed parsing cost per minute for participant " + performer, pe);
         }
      }

      return costPerMinute;
   }

   public static Period getTargetExecutionTime(ModelElement modelElement)
   {
      return doGetDurationParameter(modelElement,
            PredefinedConstants.PWH_TARGET_EXECUTION_TIME, PARSED_TARGET_EXECUTION_TIME);
   }

   public static Period getTargetProcessingTime(ModelElement modelElement)
   {
      return doGetDurationParameter(modelElement,
            PredefinedConstants.PWH_TARGET_PROCESSING_TIME, PARSED_TARGET_PROCESSING_TIME);
   }

   public static Number getTargetCostPerExecution(ModelElement modelElement)
   {
      return doGetCostParameter(modelElement,
            PredefinedConstants.PWH_TARGET_COST_PER_EXECUTION,
            PARSED_TARGET_COST_PER_EXECUTION);
   }

   private static Period doGetDurationParameter(ModelElement modelElement,
         String paramName, String parsedParamName)
   {
      Period duration = null;

      if (null != modelElement)
      {
         Object durationValue = modelElement.getAttribute(paramName);

         if (durationValue instanceof Period)
         {
            duration = (Period) durationValue;
         }
         else
         {
            duration = (Period) modelElement.getRuntimeAttribute(parsedParamName);
            if ((null == duration) && (null != durationValue))
            {
               Calendar calBase = Calendar.getInstance();
               calBase.setTimeInMillis(0l);

               Calendar cal = Calendar.getInstance();
               cal.setTimeInMillis(0l);
               durationValue = durationValue instanceof Number ? durationValue.toString() : durationValue;
               if (durationValue instanceof String)
               {
                  try
                  {
                     if(((String)durationValue).indexOf(":") > -1)
                     {
                        duration = new Period((String)durationValue);
                     }
                     else
                     {
                        cal.add(Calendar.MINUTE, NumberFormat.getInstance().parse(
                           (String) durationValue).intValue());
                        duration = new Period((short) (cal.get(Calendar.YEAR) - calBase.get(Calendar.YEAR)),
                              (short) (cal.get(Calendar.MONTH) - calBase.get(Calendar.MONTH)),
                              (short) (cal.get(Calendar.DAY_OF_MONTH) - calBase.get(Calendar.DAY_OF_MONTH)),
                              (short) (cal.get(Calendar.HOUR_OF_DAY) - calBase.get(Calendar.HOUR_OF_DAY)),
                              (short) (cal.get(Calendar.MINUTE) - calBase.get(Calendar.MINUTE)),
                              (short) (cal.get(Calendar.SECOND - calBase.get(Calendar.SECOND))));
                     }
                  }
                  catch (ParseException pe)
                  {
                     trace.warn("Failed parsing target execution time for model element "
                           + modelElement, pe);
                  }
               }

               if (null != duration
                     && (modelElement instanceof IProcessDefinition || modelElement instanceof IActivity))
               {
                  if(duration.get(Period.YEARS) == 0 &
                        duration.get(Period.MONTHS) == 0 &
                        duration.get(Period.DAYS) == 0 &
                        duration.get(Period.HOURS) == 0 &
                        duration.get(Period.MINUTES) == 0 &
                        duration.get(Period.SECONDS) == 0)
                  {
                     duration = null;
                  }
               }

               modelElement.setRuntimeAttribute(parsedParamName, duration);
            }
         }
      }

      return duration;
   }

   private static Number doGetCostParameter(ModelElement modelElement,
         String paramName, String parsedParamName)
   {
      Number result = null;

      if (null != modelElement)
      {
         Object costValue = modelElement.getAttribute(paramName);

         if (costValue instanceof Number)
         {
            result = (Number) costValue;
         }
         else
         {
            result = (Number) modelElement.getRuntimeAttribute(parsedParamName);

            if ((null == result) && (null != costValue))
            {
               if (costValue instanceof String)
               {
                  try
                  {
                     result = NumberFormat.getInstance().parse((String) costValue);
                  }
                  catch (ParseException pe)
                  {
                     trace.warn("Failed parsing cost parameter " + paramName
                           + " for participant " + modelElement, pe);
                  }
               }

               modelElement.setRuntimeAttribute(parsedParamName, result);
            }
         }
      }

      return result;
   }

   private StatisticsModelUtils()
   {
      // utility class
   }
}