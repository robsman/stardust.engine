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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.*;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TriggerDaemon implements IDaemon
{
   private static final Logger trace = LogManager.getLogger(TriggerDaemon.class);
   public static final Logger daemonLogger = RuntimeLog.DAEMON;   

   private ITriggerType triggerType;
   private Object evaluator;
   private String type;

   public TriggerDaemon(ITriggerType triggerType, String type)
   {
      this.triggerType = triggerType;
      this.type = type;
      evaluator = Reflect.createInstance(triggerType.getStringAttribute(
                  PredefinedConstants.PULL_TRIGGER_EVALUATOR_ATT));
      if (evaluator instanceof PullTriggerEvaluator)
      {
         trace.warn("The 'PullTriggerEvaluator' was deprecated. Please change class '" +
               evaluator.getClass().getName() +
               "' to implement 'BatchedPullTriggerEvaluator' instead.");
      }
      else if (!(evaluator instanceof BatchedPullTriggerEvaluator))
      {
         trace.error("The '" + evaluator.getClass().getName() + "' doesn't implement " +
               "neither 'BatchedPullTriggerEvaluator', nor 'PullTriggerEvaluator'");
      }
   }

   public ExecutionResult execute(long batchSize)
   {
      IModel model = ModelManagerFactory.getCurrent().findActiveModel();
      if (model == null)
      {
         return IDaemon.WORK_DONE;
      }

      long nTriggers = 0;

      Iterator processes = model.getAllProcessDefinitions();
      while ((nTriggers < batchSize) && processes.hasNext())
      {
         IProcessDefinition processDefinition = (IProcessDefinition) processes.next();

         Iterator processTriggers = processDefinition.getAllTriggers();
         while ((nTriggers < batchSize) && processTriggers.hasNext())
         {
            ITrigger trigger = (ITrigger) processTriggers.next();

            if (!trigger.getType().getId().equals(triggerType.getId()))
            {
               continue;
            }

            try
            {
               Iterator triggerMatches;
               if (evaluator instanceof BatchedPullTriggerEvaluator)
               {
                  triggerMatches = ((BatchedPullTriggerEvaluator) evaluator).getMatches(
                        trigger, batchSize - nTriggers);
               }
               else if (evaluator instanceof PullTriggerEvaluator)
               {
                  triggerMatches = ((PullTriggerEvaluator) evaluator).getMatches(trigger);
               }
               else
               {
                  throw new PublicException("No valid evaluator class provided.");
               }
               while (triggerMatches.hasNext())
               {
                  // to fully support backward compatibility with the old
                  // PullTriggerEvaluator, thr full range of matches has to be handled
                  // (just in case trigger evaluation caused side-effects)
                  ++nTriggers;

                  TriggerMatch match = (TriggerMatch) triggerMatches.next();
                  // @todo (france, ub): there should be are more general
                  // solution for runtime property overwriting
                  //String syncFlag = Parameters.instance().getString(
                  //      Modules.ENGINE + "." + trigger.getId() + "."
                  //      + EngineProperties.THREAD_MODE, "");

                  boolean isSync = trigger.isSynchronous();
                  //if (syncFlag.length() != 0)
                  //{
                  //   isSync = Boolean.getBoolean(syncFlag);
                  //}

                  if (isTransientExecution(processDefinition))
                  {
                     daemonLogger.warn("Trigger Daemon, process trigger '" + trigger.toString() + "' for transient process definition '" + processDefinition.getId() + "' will be ignored.");
                  }
                  else
                  {
                     daemonLogger.info("Trigger Daemon, process trigger '" + trigger.toString() + ", " + match.toString() + "'.");
                     new WorkflowServiceImpl().startProcess(processDefinition.getId(),
                           performParameterMapping(trigger, match.getData()), isSync);
                  }
               }
            }
            catch (PublicException e)
            {
               trace.warn("Failed handling trigger:" + e.getMessage());
               // @todo/hiob (ub) in case of failure, write a db log entry here
            }
         }
      }
      
      return (nTriggers >= batchSize) ? IDaemon.WORK_PENDING : IDaemon.WORK_DONE;
   }

   public String getType()
   {
      return type;
   }

   /**
    * Performs the mapping of trigger parameters to appropriate process data.
    *
    * @param trigger
    * @param parameters The trigger parameters to evaluate.
    * @return The resulting map of (data, value) pairs.
    */
   public static final Map performParameterMapping(ITrigger trigger, Map parameters)
   {
      Map result = CollectionUtils.newMap();
      ModelElementList parameterMappings = trigger.getParameterMappings();
      for (int i = 0, len = parameterMappings.size(); i < len; ++i)
      {
         IParameterMapping mapping = (IParameterMapping) parameterMappings.get(i);
         String parameterId = mapping.getParameterId();
         IData data = mapping.getData();
         if (!StringUtils.isEmpty(parameterId) && data != null)
         {
            Object parameterValue = parameters.get(parameterId);
            String parameterPath = mapping.getParameterPath();
            if (parameterPath != null)
            {
               AccessPoint ap = trigger.findAccessPoint(parameterId);
               ExtendedAccessPathEvaluator evaluator = SpiUtils.createExtendedAccessPathEvaluator(ap, parameterPath);
               AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(null, null);
               parameterValue = evaluator.evaluate(ap, parameterValue, parameterPath, evaluationContext);
            }
            
            String dataPath = mapping.getDataPath();
            if (dataPath != null)
            {
               parameterValue = new DataFragmentValue(dataPath, parameterValue);
            }

            result.put(data.getId(), parameterValue);
         }
      }
      return result;
   }
   
   private boolean isTransientExecution(final IProcessDefinition processDef)
   {
      if ( !ProcessInstanceUtils.isTransientPiSupportEnabled())
      {
         return false;
      }
      
      /* (1) process definition settings */
      final String auditTrailPersistenceStr = (String) processDef.getAttribute(PredefinedConstants.TRANSIENT_PROCESS_AUDIT_TRAIL_PERSISTENCE);
      if (auditTrailPersistenceStr != null)
      {
         final AuditTrailPersistence auditTrailPersistence = AuditTrailPersistence.valueOf(auditTrailPersistenceStr);
         final boolean transientExecution = AuditTrailPersistence.isTransientExecution(auditTrailPersistence);
         if (transientExecution)
         {
            return true;
         }
      }
      
      /* (2) global settings */
      final Parameters params = Parameters.instance();
      final String globalSetting = params.getString(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_OFF);
      if (KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT.equals(globalSetting) || KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED.equals(globalSetting))
      {
         return true;
      }
      
      return false;
   }
}
