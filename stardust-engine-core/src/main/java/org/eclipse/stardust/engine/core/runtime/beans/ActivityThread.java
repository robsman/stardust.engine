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

import static org.eclipse.stardust.common.CollectionUtils.union;
import static org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils.isSerialExecutionScenario;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.TimeMeasure;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ExpectedFailureException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.ServiceException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceInfo;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable.SymbolTableFactory;
import org.eclipse.stardust.engine.core.model.beans.TransitionBean;
import org.eclipse.stardust.engine.core.model.utils.ExclusionComputer;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusterSafeObjectProviderHolder;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ExecutionPlan;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger.LoggingBehaviour;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.beans.tokencache.TokenCache;
import org.eclipse.stardust.engine.core.runtime.beans.tokencache.TokenCache.TokenLocation;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * A (logical) thread for the execution of a workflow process.
 * Usage is as follows:
 * <p/>
 * <li>instantiate the activity thread with appropriate parameters</li>
 * <li>start/continue the work of the thread synchronously or asynchronously</li>
 * <p/>
 * Each constructor initializes
 * <p/>
 * <li>the process instance on whose behalf the threads is performed</li>
 * <li>the starting activity or - if "reanimated" the current activity instance</li>
 * <li>the user on whose behalf the workflow thread is performed</li>
 * <li>a context where workflow event listeners etc. live (e.g. used for debugging and logging)</li>
 * <p/>
 * A general behavior of the activity thread with an interactive activity as starting
 * activity is, that the activity thread is terminated if terminated if no subsequent
 * <b>interactive</b> activity can be found.
 *
 * @author mgille
 * @version $Revision$
 */
public class ActivityThread implements Runnable
{
   private static final Logger trace = LogManager.getLogger(ActivityThread.class);

   private static final int RETRIES = Parameters.instance().getInteger(
         KernelTweakingProperties.ACTIVITY_THREAD_RETRY_COUNT, 0);
   private static final int PAUSE = Parameters.instance().getInteger(
         KernelTweakingProperties.ACTIVITY_THREAD_RETRY_PAUSE, 100);

   public static final ITransition START_TRANSITION = new TransitionBean("--start--",
         "--start transition--", null, null, null);

   private IActivity activity;
   private final IProcessInstance processInstance;
   private IActivityInstance activityInstance;

   /*
    * Sequential MI activities batch data;
    */
   private int sizeOfMIBatch = 0;
   private int executedActivities = 0;

   /*
    * Contains the data passed by awakening a HIBERNATED activity instance
    */
   private final Map receiverData;

   private final TokenCache tokenCache;

   private final ProcessCompletionJanitor janitor;
   private Throwable interruption;

   private boolean checkForEnabledInclusiveORVertexes;

   private TokenLocation tokenLocation;

   public static void schedule(IProcessInstance processInstance, IActivity activity,
         IActivityInstance activityInstance,
         boolean synchronously,
         Exception interruptionState,
         Map data,
         boolean hasParent)
   {
      if (synchronously)
      {
         ActivityThread thread = new ActivityThread(
               processInstance, activity, activityInstance,
               interruptionState, data, hasParent);

         thread.run();
      }
      else if (isSerialExecutionScenario(processInstance))
      {
         scheduleSerialActivityThread(processInstance, activity, hasParent);
      }
      else
      {
         ActivityThreadCarrier carrier = new ActivityThreadCarrier();
         carrier.setProcessInstanceOID(processInstance != null ? processInstance.getOID() : 0L);
         carrier.setActivityOID(activity != null ? activity.getOID() : 0L);
         carrier.setActivityInstanceOID(activityInstance != null ? activityInstance.getOID() : 0L);
         carrier.setTimeout(interruptionState);

         ForkingServiceFactory factory = (ForkingServiceFactory)
               Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
         ForkingService service = null;
         try
         {
            service = factory.get();
            service.fork(carrier, true);
         }
         finally
         {
            factory.release(service);
         }
      }
   }

   private static void scheduleSerialActivityThread(final IProcessInstance pi, final IActivity activity, final boolean hasParent)
   {
      try
      {
         ClusterSafeObjectProviderHolder.OBJ_PROVIDER.beforeAccess();

         final long rootPiOid = pi.getRootProcessInstanceOID();
         final Map<Long, Queue<SerialActivityThreadData>> map = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(SerialActivityThreadWorkerCarrier.SERIAL_ACTIVITY_THREAD_MAP_ID);
         Queue<SerialActivityThreadData> queue = map.get(rootPiOid);
         if (queue == null)
         {
            queue = new LinkedList<SerialActivityThreadData>();
         }
         final SerialActivityThreadData data = new SerialActivityThreadData(pi.getOID(), activity.getOID());
         queue.add(data);

         /* explicitly override modified queue in cluster safe map               */
         /* since returned value may only be a clone (e.g. in case of Hazelcast) */
         map.put(rootPiOid, queue);
      }
      catch (final Exception e)
      {
         ClusterSafeObjectProviderHolder.OBJ_PROVIDER.exception(e);
         throw new InternalException(e);
      }
      finally
      {
         ClusterSafeObjectProviderHolder.OBJ_PROVIDER.afterAccess();
      }
   }

   // @todo (france, ub): remove interruptionState from the contract as soon as it is
   // appropriate
   public ActivityThread(
         IProcessInstance processInstance,
         IActivity activity,
         IActivityInstance activityInstance,
         Exception interruptionState,
         Map receiverData,
         boolean hasParent)
   {
      this.activityInstance = activityInstance;
      if (activityInstance == null)
      {
         if (null == activity)
         {
            throw new ExpectedFailureException(
                  BpmRuntimeError.BPMRT_START_ACTIVITY_THREAD_MISSING_ACTIVITY.raise());
         }
         if (null == processInstance)
         {
            throw new ExpectedFailureException(
                  BpmRuntimeError.BPMRT_START_ACTIVITY_THREAD_MISSING_PI.raise());
         }

         this.activity = activity;
         this.processInstance = processInstance;
      }
      else
      {
         this.processInstance = activityInstance.getProcessInstance();
         this.activity = activityInstance.getActivity();
         if (isMultiInstance())
         {
            ((ActivityInstanceBean) activityInstance).setIndex(TransitionTokenBean.getMultiInstanceIndex(activityInstance.getOID()));
         }
      }

      sizeOfMIBatch = fetchMIBatchSize();

      this.tokenCache = new TokenCache(this.processInstance);

      this.receiverData = receiverData;
      this.interruption = interruptionState;

      janitor = new ProcessCompletionJanitor(new JanitorCarrier(this.processInstance
            .getOID()), hasParent);
   }

   public void run()
   {
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      boolean secureContext = runtimeEnvironment.isSecureContext();
      runtimeEnvironment.setSecureContext(false);
      try
      {
         runInternal();
      }
      finally
      {
         runtimeEnvironment.setSecureContext(secureContext);
      }
   }

   public void runInternal()
   {
      if (isInAbortingPiHierarchy())
      {
         Long oid = (Long) processInstance
               .getPropertyValue(ProcessInstanceBean.ABORTING_USER_OID);
         if (oid == null)
         {
            oid = Long.valueOf(0);
         }
         // TODO: trace the real state: aborted or aborting.
         BpmRuntimeError error;
         if (activityInstance == null)
         {
            error = BpmRuntimeError.BPMRT_CANNOT_RUN_A_INVALID_PI_STATE.raise(activity,
                  processInstance.getOID());
         }
         else
         {
            error = BpmRuntimeError.BPMRT_CANNOT_RUN_AI_INVALID_PI_STATE
                  .raise(activityInstance.getOID(), processInstance.getOID());
         }
         // TODO: (fh) shouldn't be in a separate transaction ?
         ProcessAbortionJanitor.scheduleJanitor(
               new AbortionJanitorCarrier(this.processInstance.getOID(), oid));
         throw new IllegalOperationException(error);
      }

      // TODO: (fh) verify this
      if (isInHaltingPiHierarchyAndHaltable())
      {
         Long oid = (Long) processInstance
               .getPropertyValue(ProcessInstanceBean.HALTING_USER_OID);
         if (oid == null)
         {
            oid = Long.valueOf(0);
         }
         // TODO: trace the real state: halted or halting.
         BpmRuntimeError error;
         if (activityInstance == null)
         {
            error = BpmRuntimeError.BPMRT_CANNOT_RUN_A_INVALID_PI_STATE.raise(activity,
                  processInstance.getOID());
         }
         else
         {
            error = BpmRuntimeError.BPMRT_CANNOT_RUN_AI_INVALID_PI_STATE
                  .raise(activityInstance.getOID(), processInstance.getOID());
         }
         ProcessHaltJanitor.schedule(processInstance.getOID(), oid);
         //throw new IllegalOperationException(error);
         return;
      }

      if (trace.isDebugEnabled())
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("Started activity thread: ").append(processInstance);
         if (null != activityInstance)
         {
            buffer.append(", ").append(activityInstance);
         }
         else if (null != activity)
         {
            buffer.append(", ").append(activity);
         }
         trace.debug(buffer.toString());
      }

      final ActivityThreadContext context = getCurrentActivityThreadContext();
      if (activityInstance == null)
      {
         ITransition transition = null;
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         ExecutionPlan plan = rtEnv.getExecutionPlan();
         if (plan != null && !plan.isTerminated())
         {
            if (!plan.hasStartActivity() || plan.hasMoreSteps())
            {
               transition = START_TRANSITION;
            }
            else if (plan.hasNextActivity())
            {
               transition = plan.getTransition();
               TransitionTokenBean token = plan.getToken();
               tokenCache.registerToken(transition, token);
            }
            if (activity == plan.getTargetActivity())
            {
               plan.terminate();
            }
         }
         else if (processInstance.getProcessDefinition().getRootActivity().getId().equals(activity.getId()))
         {
            transition = START_TRANSITION;
         }
         if (transition != null)
         {
            TransitionTokenBean startToken = tokenCache.lockFreeToken(transition);
            if (startToken == null)
            {
               return;
            }

            List<TransitionTokenBean> wrappedStartToken = Collections.singletonList(startToken);
            tokenCache.registerPersistenceControllers(wrappedStartToken);
            createActivityInstance(wrappedStartToken);
         }
         else
         {
            int count = 0;
            final TimeMeasure timer = new TimeMeasure();

            while (!enableVertex())
            {
               count++;
               if (count > RETRIES)
               {
                  if (RETRIES > 0)
                  {
                     trace.warn("No free tokens found for Process Instance <"
                           + processInstance.getOID()
                           + "> in "
                           + timer.stop().getDurationInMillis()
                           + " ms, giving up.");
                  }
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Ended thread, no free tokens.");
                  }
                  return;
               }

               trace.warn("Retrying " + count + " time.");
               try
               {
                  Thread.sleep(PAUSE);
               }
               catch (InterruptedException e1)
               {
               }
            }
         }
      }
      else
      {
         // checks?
      }

      // @todo laokoon (ub): replace by an explicit parameter whether to throw
      // exceptions
      try
      {
         while (activityInstance != null)
         {
            if (!activityInstance.isTerminated())
            {
               if (context.isStepMode())
               {
                  // wait for join from thread context (i.e. debug controller)
                  context.suspendActivityThread(this);
               }

               runCurrentActivity();
               executedActivities++;

               if (!activityInstance.isTerminated() || isInAbortingPiHierarchy())
               {
                  if (trace.isDebugEnabled()) trace.debug("Activity thread stopped for " + activityInstance);
                  break;
               }
               else if (ProcessInstanceUtils.isInHaltingPiHierarchy(processInstance))
               {
                  if (ActivityInstanceUtils.isHaltable(activityInstance))
                  {
                     ((ActivityInstanceBean) activityInstance).setState(ActivityInstanceState.HALTED);
                     ProcessHaltJanitor.schedule(processInstance.getOID(),
                           activityInstance.getActivity().isInteractive() ? SecurityProperties.getUserOID() : 0);
                  }
                  if (trace.isDebugEnabled()) trace.debug("Activity thread stopped for " + activityInstance);
                  break;
               }
            }
            determineNextActivityInstance();
         }
      }
      catch (ServiceException se)
      {
         // let service exceptions bubble up
         throw se;
      }
      catch (PublicException e)
      {
         throw new PublicException(BpmRuntimeError.BPMRT_ROLLING_BACK_ACTIVITY_THREAD.raise(),
               "Rolling back activity thread.", e);
      }
      catch (Throwable x)
      {
         trace.error("activityInstance = " + activityInstance);
         trace.error("activity = " + activity);
         trace.error("processInstance = " + processInstance);

         x.printStackTrace();
         LogUtils.traceException(x, false);

         // do not try to log to audit trail if TX is marked for rollback
         if (!TransactionUtils.isCurrentTxRollbackOnly())
         {
            AuditTrailLogger.getInstance(LogCode.ENGINE, processInstance).error(
                  "Unexpected activity thread state.");
         }

         throw new InternalException("Unexpected activity thread state.");
      }

      if (checkForEnabledInclusiveORVertexes)
      {
         startEnabledOrGateways();
      }

      tokenCache.flush();
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      ExecutionPlan plan = rtEnv.getExecutionPlan();
      janitor.execute(plan != null, tokenCache.getTokenChange());

      if (interruption != null)
      {
         throw new PublicException(interruption);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Ended thread, last executed was " + activity);
      }
   }

   public IProcessInstance processInstance()
   {
      return processInstance;
   }

   public IActivity activity()
   {
      return activity;
   }

   public IActivityInstance activityInstance()
   {
      return activityInstance;
   }

   private boolean isInAbortingPiHierarchy()
   {
      return ProcessInstanceUtils.isInAbortingPiHierarchy(this.processInstance);
   }

   private boolean isInHaltingPiHierarchyAndHaltable()
   {
      return ActivityInstanceUtils.isHaltable(activityInstance)
            && ProcessInstanceUtils.isInHaltingPiHierarchy(this.processInstance);
   }

   private void createActivityInstance(List<TransitionTokenBean> inTokens)
   {
      boolean multiInstance = isMultiInstance();
      int count = multiInstance ? getMultiInstanceCount() : 1;
      sizeOfMIBatch = fetchMIBatchSize();

      if (count > 0)
      {
         if (LoopType.While.equals(activity.getLoopType())
               && !processInstance.validateLoopCondition(activity.getLoopCondition()))
         {
            activityInstance = new PhantomActivityInstance(activity, processInstance);
         }
         else
         {
            activityInstance = new ActivityInstanceBean(activity, processInstance);
         }
      }

      for (int i = 0; i < inTokens.size(); ++i)
      {
         TransitionTokenBean freeToken = inTokens.get(i);
         if (count > 0)
         {
            tokenCache.bindToken(freeToken, activityInstance);
         }
         if (multiInstance)
         {
            tokenCache.consumeToken(freeToken);
         }
         if (activity.getJoinType() == JoinSplitType.Xor)
         {
            break;
         }
      }

      if (activityInstance != null)
      {
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         ExecutionPlan plan = rtEnv.getExecutionPlan();
         if (plan != null && plan.isTerminated() && plan.getTargetActivityInstance() == null
               && activityInstance.getActivity() == plan.getTargetActivity())
         {
            plan.setTargetActivityInstance(activityInstance);
         }
      }

      if (multiInstance)
      {
         executedActivities = 0;
         if (count > 0)
         {
            createLoopOutputData();
         }
         boolean parallel = !isSequential();
         for (int i = 0; i < count; i++)
         {
            createMultiInstance(i == 0 ? activityInstance
                  : parallel ? new ActivityInstanceBean(activity, processInstance) : null, i);
         }
         if (parallel)
         {
            activityInstance = null;
         }
      }

      if (activityInstance != null)
      {
         getCurrentActivityThreadContext().enteringActivity(activityInstance);
      }
   }

   private int fetchMIBatchSize()
   {
      Object att = activity.getAttribute(PredefinedConstants.ACTIVITY_MI_BATCH_SIZE_ATT);
      if (att == null)
      {
         return 0;
      }
      if (att instanceof Integer)
      {
         return (Integer) att;
      }
      String val = att.toString().trim();
      if (val.isEmpty())
      {
         activity.removeAttribute(PredefinedConstants.ACTIVITY_MI_BATCH_SIZE_ATT);
         return 0;
      }
      try
      {
         int size = Integer.parseInt(val);
         activity.setAttribute(PredefinedConstants.ACTIVITY_MI_BATCH_SIZE_ATT, size);
         return size;
      }
      catch (NumberFormatException ex)
      {
         activity.removeAttribute(PredefinedConstants.ACTIVITY_MI_BATCH_SIZE_ATT);
         return 0;
      }
   }

   private void createMultiInstance(IActivityInstance target, int index)
   {
      TransitionTokenBean token = tokenCache.createMultiInstanceToken(activityInstance, index);
      bindTokenAndScheduleActivity(token, target);
   }

   private boolean bindTokenAndScheduleActivity(TransitionTokenBean token,
         IActivityInstance target)
   {
      tokenCache.bindToken(token, target);
      if (target != null)
      {
         ((ActivityInstanceBean) target).setIndex(token.getMultiInstanceIndex());
         getCurrentActivityThreadContext().enteringActivity(target);
         if (shouldSchedule())
         {
            schedule(processInstance, null, target, false, null, Collections.EMPTY_MAP, false);
            return true;
         }
      }
      return false;
   }

   private boolean shouldSchedule()
   {
      return !isSequential() || sizeOfMIBatch > 0 && executedActivities == sizeOfMIBatch;
   }

   private void determineNextActivityInstance()
   {
      if (activity.isQualityAssuranceEnabled())
      {
         boolean workflowChanged = false;
         QualityAssuranceState qualityAssuranceState = activityInstance
               .getQualityAssuranceState();
         if (qualityAssuranceState == QualityAssuranceState.NO_QUALITY_ASSURANCE
               || qualityAssuranceState == QualityAssuranceState.IS_REVISED)
         {
            workflowChanged = handleQualityAssuranceEnabledInstance(qualityAssuranceState);
         }
         else if (qualityAssuranceState == QualityAssuranceState.IS_QUALITY_ASSURANCE)
         {
            workflowChanged = handleQualityAssuranceInstance();
         }

         if (workflowChanged)
         {
            return;
         }
      }

      if (isValidLoopCondition())
      {
         // perform old-style loop
         IActivityInstance oldBinding = activityInstance;
         activityInstance = new ActivityInstanceBean(activity, processInstance);
         tokenCache.updateInBindings(oldBinding, activityInstance, activity);

         getCurrentActivityThreadContext().enteringActivity(activityInstance);
      }
      else
      {
         // traverse outgoing transitions
         List<TransitionTokenBean> boundInTokens = tokenCache.getBoundInTokens(activityInstance, activity);
         if (boundInTokens.isEmpty())
         {
            if (trace.isDebugEnabled()) trace.debug("No tokens found!");
            activityInstance = null;
            return;
         }
         for (TransitionTokenBean boundToken : boundInTokens)
         {
            if (isMultiInstance())
            {
               // (fh) lock this token and any other token we can get
               TransitionTokenBean token = tokenCache.lockSourceAndOtherToken(boundToken);
               if (boundToken.isConsumed())
               {
                  if (trace.isDebugEnabled()) trace.debug("!!! Bound token already consumed: " + boundToken);
                  activityInstance = null;
                  return;
               }
               if (token != boundToken)
               {
                  // (fh) this thread dies here
                  if (token == null)
                  {
                     if (trace.isDebugEnabled()) trace.debug("No other token locked, another thread scheduled for " + activityInstance);
                     schedule(processInstance, null, activityInstance, false, null, Collections.EMPTY_MAP, false);
                     activityInstance = null;
                     return;
                  }
                  else
                  {
                     if (token.isConsumed())
                     {
                        if (trace.isDebugEnabled()) trace.debug("!!! Other token already consumed: " + token);
                     }
                     tokenCache.consumeToken(boundToken);
                     getCurrentActivityThreadContext().completingTransition(boundToken);
                     if (isSequential())
                     {
                        activityInstance = new ActivityInstanceBean(activity, processInstance);
                        if (bindTokenAndScheduleActivity(token, activityInstance))
                        {
                           activityInstance = null;
                        }
                     }
                     else
                     {
                        if (trace.isDebugEnabled()) trace.debug("Thread stops here for " + activityInstance + " because other unconsumed token found " + token);
                        activityInstance = null;
                     }
                     return;
                  }
               }
            }
            tokenCache.consumeToken(boundToken);
            getCurrentActivityThreadContext().completingTransition(boundToken);
         }

         List<ITransition> enabledTransitions = Collections.emptyList();
         List<ITransition> otherwiseTransitions = Collections.emptyList();
         ITransition exceptionTransition = null;

         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         ExecutionPlan plan = rtEnv.getExecutionPlan();
         JoinSplitType splitType = activity.getSplitType();
         if (plan != null && !plan.isTerminated())
         {
            if (plan.hasNextActivity())
            {
               if (plan.isStart() || plan.getToken() == null && !plan.hasMoreSteps())
               {
                  enabledTransitions = Collections.singletonList(plan.getTransition());
               }
               else
               {
                  TransitionTokenBean token = plan.getToken();
                  if (token == null && plan.hasStartActivity())
                  {
                     ITransition transition = plan.getTransition();
                     token = tokenCache.createToken(transition , plan.getStartActivityInstance());
                     plan.setToken(token);
                     getCurrentActivityThreadContext().enteringTransition(token);
                  }
               }
            }
            else if (plan.isStepUpwards())
            {
               // (fh) we must consume the tokens to force completion
               ResultIterator<TransitionTokenBean> tokens = TransitionTokenBean.findUnconsumedForProcessInstance(
                     processInstance.getOID());
               while (tokens.hasNext())
               {
                  TransitionTokenBean token = tokens.next();
                  if (!token.isBound())
                  {
                     tokenCache.consumeToken(token);
                  }
               }
            }
         }
         else
         {
            if (activity.hasExceptionTransitions())
            {
               final String eventHandlerId = (String) activityInstance.getPropertyValue(ActivityInstanceBean.BOUNDARY_EVENT_HANDLER_ACTIVATED_PROPERTY_KEY);
               exceptionTransition = eventHandlerId != null ? activity.getExceptionTransition(eventHandlerId) : null;
            }
            if (exceptionTransition == null)
            {
               // find traversable transitions and separate between enabled and otherwise ones
               ModelElementList outTransitions = activity.getOutTransitions();
               SymbolTable symbolTable = SymbolTableFactory.create(activityInstance, activity);
               for (int i = 0; i < outTransitions.size(); ++i)
               {
                  ITransition transition = (ITransition) outTransitions.get(i);
                  if (transition.isEnabled(symbolTable))
                  {
                     if (outTransitions.size() == 1 || JoinSplitType.Xor == splitType)
                     {
                        enabledTransitions = Collections.singletonList(transition);
                        break;
                     }
                     else
                     {
                        if (enabledTransitions.isEmpty())
                        {
                           enabledTransitions = CollectionUtils.newList(outTransitions.size());
                        }
                        enabledTransitions.add(transition);
                     }
                  }
                  else if (transition.isOtherwiseEnabled(symbolTable))
                  {
                     if (1 == outTransitions.size())
                     {
                        otherwiseTransitions = Collections.singletonList(transition);
                     }
                     else
                     {
                        if (otherwiseTransitions.isEmpty())
                        {
                           otherwiseTransitions = CollectionUtils.newList(outTransitions.size());
                        }
                        otherwiseTransitions.add(transition);
                     }
                  }
               }
            }
         }

         final List<ITransition> enabledOutTransitions;
         if (!enabledTransitions.isEmpty())
         {
            enabledOutTransitions = exceptionTransition != null
                  ? union(Collections.singletonList(exceptionTransition), enabledTransitions, false)
                  : enabledTransitions;
         }
         else if (!otherwiseTransitions.isEmpty())
         {
            enabledOutTransitions = exceptionTransition != null
                  ? union(Collections.singletonList(exceptionTransition), otherwiseTransitions, false)
                  : otherwiseTransitions;
         }
         else if (exceptionTransition != null)
         {
            enabledOutTransitions = Collections.singletonList(exceptionTransition);
         }
         else
         {
            enabledOutTransitions = Collections.emptyList();
         }

         for (int i = 0; i < enabledOutTransitions.size(); ++i)
         {
            ITransition transition = enabledOutTransitions.get(i);
            TransitionTokenBean token = tokenCache.createToken(transition, this.activityInstance);
            if (plan != null && !plan.isTerminated() && transition == plan.getTransition())
            {
               plan.setToken(token);
            }
            if (trace.isDebugEnabled())
            {
               trace.debug("Created " + token);
            }

            getCurrentActivityThreadContext().enteringTransition(token);

            if (JoinSplitType.Xor == splitType && exceptionTransition == null)
            {
               break;
            }
         }

         if (exceptionTransition != null
               || JoinSplitType.Xor == splitType
               || enabledOutTransitions.isEmpty() && !activity.getOutTransitions().isEmpty())
         {
            checkForEnabledInclusiveORVertexes  = true;
         }

         if (enabledOutTransitions.isEmpty() && (plan == null || !plan.hasMoreSteps()))
         {
            activityInstance = null;
         }
         else
         {
            List<TransitionTokenBean> freeOutTokens = null;
            if (plan != null && plan.hasNextActivity())
            {
               IActivity step = plan.getCurrentStep();
               if (step != null)
               {
                  activity = step;
                  createActivityInstance(Collections.<TransitionTokenBean>emptyList());
                  return;
               }
               freeOutTokens = Collections.singletonList(tokenCache.lockFreeToken(plan.getTransition()));
               plan.terminate();
            }
            else
            {
               freeOutTokens = tokenCache.getFreeOutTokens(enabledOutTransitions);
            }

            boolean foundSynchronousSuccessor = false;

            for (int i = 0; i < freeOutTokens.size(); ++i)
            {
               TransitionTokenBean token = freeOutTokens.get(i);
               ITransition transition = plan != null && token == plan.getToken()
                     ? plan.getTransition() : token.getTransition();
               IActivity targetActivity = transition.getToActivity();

               if (!foundSynchronousSuccessor && !transition.getForkOnTraversal())
               {
                  activity = targetActivity;
                  if (enableVertex())
                  {
                     foundSynchronousSuccessor = true;
                  }
                  else
                  {
                     if (isSerialExecutionScenario(processInstance) && activity.getJoinType() == JoinSplitType.And)
                     {
                        /* do not schedule a new activity thread for every single incoming */
                        /* thread - the serial execution ensures that the last thread is   */
                        /* able to pass the join gateway (no contention).                  */
                        continue;
                     }
                     /* do not schedule a new activity thread if the blocking token resides */
                     /* in the local cache.                                                 */
                     if (tokenLocation != TokenLocation.local)
                     {
                        schedule(processInstance, targetActivity, null,
                              false, null, Collections.EMPTY_MAP, false);
                     }
                  }
               }
               else
               {
                  schedule(processInstance, targetActivity, null,
                        false, null, Collections.EMPTY_MAP, false);
               }
            }

            if (!foundSynchronousSuccessor)
            {
               activityInstance = null;
            }
         }
      }
   }

   private Set<ITransition> getExclusionList(ITransition transition)
   {
      Set<ITransition> excluded = transition.getRuntimeAttribute("INCLUSIVE_OR_EXCLUSION_SET");
      if (excluded == null)
      {
         synchronized(transition)
         {
            ExclusionComputer<IActivity, ITransition> computer = new ExclusionComputer<IActivity, ITransition>()
            {
               protected IActivity getFrom(ITransition transition) {return transition.getFromActivity();}
               protected IActivity getTo(ITransition transition) {return transition.getToActivity();}
               protected Iterable<ITransition> getIn(IActivity activity) {return activity.getInTransitions();}
               protected boolean isInclusiveJoin(IActivity activity) {return activity.getJoinType() == JoinSplitType.And
                     || activity.getJoinType() == JoinSplitType.Or;}
            };
            excluded = Collections.unmodifiableSet(computer.getExclusionSet(transition));
            if (trace.isDebugEnabled())
            {
               trace.debug(transition + " exclusion set: " + excluded);
            }
            transition.setRuntimeAttribute("INCLUSIVE_OR_EXCLUSION_SET", excluded);
         }
      }
      return excluded;
   }

   private void startEnabledOrGateways()
   {
      IActivity lastActivity = activity;
      ModelElementList<IActivity> activities = processInstance.getProcessDefinition().getActivities();
      for (IActivity activity : activities)
      {
         /* do not check the gateway if it was the last activity because it was already checked */
         if (activity.getJoinType() == JoinSplitType.Or && activity != lastActivity)
         {
            activityInstance = null;
            this.activity = activity;
            if (enableVertex())
            {
               schedule(processInstance, null, activityInstance,
                     false, null, Collections.EMPTY_MAP, false);
            }
         }
      }
      activity = lastActivity;
   }

   private boolean isMultiInstance()
   {
      return activity.getLoopCharacteristics() instanceof IMultiInstanceLoopCharacteristics;
   }

   private boolean isSequential()
   {
      return ((IMultiInstanceLoopCharacteristics) activity.getLoopCharacteristics()).isSequential();
   }

   private int getMultiInstanceCount()
   {
      IMultiInstanceLoopCharacteristics loop = (IMultiInstanceLoopCharacteristics) activity.getLoopCharacteristics();
      String inputParameterId = loop.getInputParameterId();
      if (inputParameterId != null)
      {
         String context = inputParameterId.substring(0, inputParameterId.indexOf(':'));
         inputParameterId = inputParameterId.substring(context.length() + 1);
         ModelElementList<IDataMapping> mappings = activity.getInDataMappings();
         for (IDataMapping mapping : mappings)
         {
            if (context.equals(mapping.getContext()) && inputParameterId.equals(mapping.getActivityAccessPointId()))
            {
               Object inputValue = processInstance.getInDataValue(mapping.getData(), mapping.getDataPath());
               if (inputValue != null)
               {
                  if (inputValue instanceof List)
                  {
                     return ((List) inputValue).size();
                  }
                  if (inputValue.getClass().isArray())
                  {
                     return Array.getLength(inputValue);
                  }
                  // TODO (fh) support other types ?
               }
               break;
            }
         }
      }
      return 0;
   }

   private void createLoopOutputData()
   {
      IMultiInstanceLoopCharacteristics loop = (IMultiInstanceLoopCharacteristics) activity.getLoopCharacteristics();
      String outputParameterId = loop.getOutputParameterId();
      if (outputParameterId != null)
      {
         String context = outputParameterId.substring(0, outputParameterId.indexOf(':'));
         outputParameterId = outputParameterId.substring(context.length() + 1);
         ModelElementList<IDataMapping> mappings = activity.getOutDataMappings();
         for (IDataMapping mapping : mappings)
         {
            if (context.equals(mapping.getContext()) && outputParameterId.equals(mapping.getActivityAccessPointId()))
            {
               processInstance.getInDataValue(mapping.getData(), mapping.getDataPath());
               break;
            }
         }
      }
   }

   private boolean enableVertex()
   {
      tokenLocation = null;
      JoinSplitType joinType = activity.getJoinType();
      List<TransitionTokenBean> freeTokens =
            joinType == JoinSplitType.And ? enableAnd() :
            joinType == JoinSplitType.Or ? enableOr() :
            /* default */ enableXor();

      // need at least a token to continue
      if (freeTokens == null || freeTokens.isEmpty())
      {
         return false;
      }

      tokenCache.registerPersistenceControllers(freeTokens);
      createActivityInstance(freeTokens);

      return true;
   }

   private List<TransitionTokenBean> enableXor()
   {
      ModelElementList<ITransition> inTransitions = activity.getInTransitions();
      for (ITransition transition : inTransitions)
      {
         TransitionTokenBean freeToken = tokenCache.lockFreeToken(transition);
         if (freeToken != null)
         {
            return Collections.singletonList(freeToken);
         }
      }
      return null;
   }

   private List<TransitionTokenBean> enableAnd()
   {
      List<TransitionTokenBean> freeTokens = null;
      ModelElementList<ITransition> inTransitions = activity.getInTransitions();
      for (ITransition transition : inTransitions)
      {
         TransitionTokenBean freeToken = tokenCache.lockFreeToken(transition);
         if (freeToken != null)
         {
            if (freeTokens == null)
            {
               freeTokens = CollectionUtils.newList();
            }
            freeTokens.add(freeToken);
         }
         else
         {
            // AND join will not be satisfied as at least one token is missing
            if (freeTokens != null)
            {
               tokenCache.unlockTokens(freeTokens);
            }
            return null;
         }
      }
      return freeTokens;
   }

   private List<TransitionTokenBean> enableOr()
   {
      Set<ITransition> excluded = null;

      List<TransitionTokenBean> freeTokens = null;
      ModelElementList<ITransition> inTransitions = activity.getInTransitions();
      for (ITransition transition : inTransitions)
      {
         TransitionTokenBean freeToken = tokenCache.lockFreeToken(transition);
         if (freeToken != null)
         {
            if (freeTokens == null)
            {
               freeTokens = CollectionUtils.newList();
            }
            freeTokens.add(freeToken);
            if (excluded == null)
            {
               excluded = CollectionUtils.newSet(getExclusionList(transition));
            }
            else
            {
               excluded.retainAll(getExclusionList(transition));
            }
         }
      }
      if (trace.isDebugEnabled())
      {
         trace.debug(activity + " exclusion set: " + excluded);
      }

      // OR join will not be satisfied as at least one token is missing
      if (freeTokens != null)
      {
         if (excluded.isEmpty() || !hasUnconsumedTokens(excluded))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Activating " + activity + " with " + freeTokens);
            }
            return freeTokens;
         }

         tokenCache.unlockTokens(freeTokens);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Could not activate " + activity + ".");
      }
      return null;
   }

   private boolean hasUnconsumedTokens(Set<ITransition> excluded)
   {
      tokenLocation = tokenCache.hasUnconsumedTokens(excluded);
      return tokenLocation != null;
   }

   private boolean isValidLoopCondition()
   {
      return (LoopType.Repeat.equals(activity.getLoopType())
            && !processInstance.validateLoopCondition(activity.getLoopCondition()))
            || (LoopType.While.equals(activity.getLoopType())
            && processInstance.validateLoopCondition(activity.getLoopCondition()));
   }

   /**
    * Performs the current activity instance of a process instance.
    * <p/>
    * The activity instance leaves this method in one of the following states:
    * <ul>
    * <li>Completed</li>
    * <li>Suspended<li>
    * <li>Hibernated<li>
    * </ul>
    */
   private void runCurrentActivity()
   {
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      runtimeEnvironment.setCurrentActivityInstance(activityInstance);

      if (activityInstance.getState() == ActivityInstanceState.Hibernated
            || activityInstance.getState() == ActivityInstanceState.Suspended)
      {
         Assert.lineNeverReached();
      }
      else if (activityInstance.getState() == ActivityInstanceState.Created
            || activityInstance.getState() == ActivityInstanceState.Interrupted)
      {
         try
         {
            if (activityInstance instanceof ActivityInstanceBean)
            {
               ((ActivityInstanceBean) activityInstance).lockAndCheck();
            }
            else
            {
               activityInstance.lock();
            }
            activityInstance.start();
         }
         catch (NonInteractiveApplicationException x)
         {
            // hint: exception is already logged.

            final Parameters params = Parameters.instance();

            Object appExceptionPropagation = params.getString(
                  KernelTweakingProperties.APPLICATION_EXCEPTION_PROPAGATION,
                  KernelTweakingProperties.APPLICATION_EXCEPTION_PROPAGATION_NEVER);

            ITransactionStatus txStatus = TransactionUtils.getCurrentTxStatus(params);

            if (KernelTweakingProperties.APPLICATION_EXCEPTION_PROPAGATION_ALWAYS.equals(appExceptionPropagation)
                  || (KernelTweakingProperties.APPLICATION_EXCEPTION_PROPAGATION_ON_ROLLBACK.equals(appExceptionPropagation)
                        && txStatus.isRollbackOnly()))
            {
               if ( !txStatus.isRollbackOnly())
               {
                  // force rollback if this was not already done otherwise (i.e. if
                  // "propagate always" is set)
                  txStatus.setRollbackOnly();
               }

               final String activityId = activity ==  null ? "" : activity.getId();
               final ServiceException serviceException = new ServiceException(
                     BpmRuntimeError.BPMRT_ROLLED_BACK_ACTIVITY_THREAD_AT_ACTIVITY.raise(
                           activityId, x.getCause().getClass().getName()),
                     x.getCause());

               AuditTrailLogger auditTrailLogger = AuditTrailLogger.getInstance(LogCode.ENGINE,
                     activityInstance.getProcessInstance(), LoggingBehaviour.SEPARATE_TRANSACTION_SYNCHRONOUS);
               auditTrailLogger.warn(serviceException.getMessage());

               // propagate exception
               throw serviceException;
            }
            else
            {
               String errorLogMessage = MessageFormat.format(
                     "Activity thread interrupted at ''{0}'', reason: {1}: {2}",
                     new Object[] {
                           activity, x.getCause().getClass().getName(),
                           x.getCause().getMessage()});

               if(!txStatus.isRollbackOnly())
               {
                  processInstance.interrupt();

                  activityInstance.interrupt();

                  AuditTrailLogger auditTrailLogger = AuditTrailLogger.getInstance(
                        LogCode.ENGINE, activityInstance,
                        LoggingBehaviour.SAME_TRANSACTION);
                  auditTrailLogger.warn(errorLogMessage);
               }
               else
               {
                  AuditTrailLogger auditTrailLogger = AuditTrailLogger.getInstance(
                        LogCode.ENGINE, activityInstance,
                        LoggingBehaviour.SEPARATE_TRANSACTION_SYNCHRONOUS);
                  auditTrailLogger.warn(errorLogMessage);

                  throw x;
               }

               return;
            }
         }

         // watch out for implicit state changes caused by event actions
         if (!activityInstance.isTerminated())
         {
            if (activityInstance.getState() == ActivityInstanceState.Suspended)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Stop for interactive application.");
               }
               return;
            }
            else if (activityInstance.getState() == ActivityInstanceState.Hibernated)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Leaving " + activityInstance + " in hibernated state.");
               }
               return;
            }
            else if(ActivityInstanceState.Aborting == activityInstance.getState())
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Leaving " + activityInstance + " in aborting state.");
               }
               return;
            }
            else if(ActivityInstanceState.Halted.equals(activityInstance.getState()))
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Leaving " + activityInstance + " in halted state.");
               }
               return;
            }

            activityInstance.complete();
         }
      }
      else if (activityInstance.getState() == ActivityInstanceState.Completed
            || activityInstance.getState() == ActivityInstanceState.Aborted)
      {
         Assert.lineNeverReached();
      }
      else if (activityInstance.getState() == ActivityInstanceState.Application
            || activityInstance.getState() == ActivityInstanceState.Halted)
      {
         activityInstance.complete();
         activityInstance.accept(receiverData);

         if (activity.isInteractive())
         {
            interruption = null;
         }
      }
      else if (activityInstance.getState() == ActivityInstanceState.Aborting)
      {
         if (activityInstance instanceof ActivityInstanceBean)
         {
            boolean preventAbortion = Parameters.instance().getBoolean(
                  KernelTweakingProperties.PREVENT_ABORTING_TO_ABORTED_STATE_CHANGE,
                  false);
            if ( !preventAbortion)
            {
               IProcessInstance subPi = null;
               if (activityInstance.getActivity().getImplementationType().isSubProcess())
               {
                  subPi = ProcessInstanceBean
                        .findForStartingActivityInstance(activityInstance.getOID());
               }
               if (null == subPi || ProcessInstanceState.Aborted == subPi.getState())
               {
                  // AI can only be aborted if it is no SubProcess AI or
                  // its sub process is already aborted itself.
                  ActivityInstanceBean aiBean = (ActivityInstanceBean) activityInstance;
                  aiBean.setState(ActivityInstanceState.ABORTED);
               }
            }
         }
         else
         {
            Assert.lineNeverReached();
         }
      }
   }

   static ActivityThreadContext getCurrentActivityThreadContext()
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      ActivityThreadContext threadContext = rtEnv.getActivityThreadContext();
      if (threadContext == null)
      {
         // fall back to old behavior
         threadContext = (ActivityThreadContext) Parameters.instance().get(
               EngineProperties.ACTIVITY_THREAD_CONTEXT);
      }
      return threadContext;
   }

   // TODO: refactor, should not extend ActivityInstanceBean but directly implement IActivityInstance
   private static class PhantomActivityInstance extends ActivityInstanceBean
   {
      private static final long serialVersionUID = 1L;

      PhantomActivityInstance(IActivity activity, IProcessInstance processInstance)
      {
         super();
         setOID(-1);
         this.model = activity.getModel().getModelOID();
         this.activity = ModelManagerFactory.getCurrent().getRuntimeOid(activity);
         this.processInstance = (ProcessInstanceBean) processInstance;
      }

      public void start()
      {
         setState(ActivityInstanceState.APPLICATION);
      }

      public void complete()
      {
         setState(ActivityInstanceState.COMPLETED);
      }
   }

   private boolean handleQualityAssuranceInstance()
   {
      boolean isWorkflowModified = false;
      ActivityInstanceAttributes aiAttributes = QualityAssuranceUtils
            .getActivityInstanceAttributes(activityInstance);

      if(aiAttributes != null)
      {
         QualityAssuranceResult result = aiAttributes.getQualityAssuranceResult();
         QualityAssuranceResult.ResultState resultState = result.getQualityAssuranceState();

         if (resultState == QualityAssuranceResult.ResultState.FAILED)
         {
            IActivityInstance oldInstance = activityInstance;
            IActivityInstance newInstance = null;

            //decide if new instance should go back to the last user who performed on it
            //or to the modeled participant
            if(result.isAssignFailedInstanceToLastPerformer())
            {
               //we need to know which user worked before this qa instance
               QualityAssuranceInfo qaInfo = QualityAssuranceUtils.getQualityAssuranceInfo(oldInstance);
               long monitoredUserOID = qaInfo.getMonitoredInstance().getPerformedByOID();
               IUser monitoredUser = UserBean.findByOid(monitoredUserOID);

               newInstance = new ActivityInstanceBean(activity, processInstance, monitoredUser);
            }
            else
            {
               newInstance = new ActivityInstanceBean(activity, processInstance);
            }

            newInstance.setQualityAssuranceState(QualityAssuranceState.IS_REVISED);
            //set a backward reference to the failed qa instance
            newInstance.setPropertyValue(QualityAssuranceInfo.FAILED_QUALITY_CONTROL_INSTANCE_OID,
                  oldInstance.getOID());

            activityInstance = newInstance;
            tokenCache.updateInBindings(oldInstance, newInstance, activity);
            getCurrentActivityThreadContext().enteringActivity(newInstance);

            isWorkflowModified = true;
         }
      }

      return isWorkflowModified;
   }

   private boolean handleQualityAssuranceEnabledInstance(QualityAssuranceState qualityAssuranceState)
   {
      boolean isWorkflowModified = false;
      if(QualityAssuranceUtils.shouldQualityAssuranceBePerformed(activityInstance))
      {
         IActivityInstance oldInstance = activityInstance;
         IActivityInstance newInstance = new ActivityInstanceBean(activity,
               processInstance);

         newInstance.setQualityAssuranceState(QualityAssuranceState.IS_QUALITY_ASSURANCE);
         // set a backward reference to the monitored instance
         newInstance.setPropertyValue(QualityAssuranceInfo.MONITORED_INSTANCE_OID,
               oldInstance.getOID());

         //only the first instance in the qc chain is marked as triggered
         if(qualityAssuranceState != QualityAssuranceState.IS_REVISED)
         {
            oldInstance.setQualityAssuranceState(QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED);
         }
         // propagate the failed qc instance
         else
         {
            Long failedQcInstanceOid
               = (Long) oldInstance.getPropertyValue(QualityAssuranceInfo.FAILED_QUALITY_CONTROL_INSTANCE_OID);
            newInstance.setPropertyValue(QualityAssuranceInfo.FAILED_QUALITY_CONTROL_INSTANCE_OID,
                  failedQcInstanceOid);
         }

         activityInstance = newInstance;
         tokenCache.updateInBindings(oldInstance, newInstance, activity);
         getCurrentActivityThreadContext().enteringActivity(newInstance);

         isWorkflowModified = true;
      }


      return isWorkflowModified;
   }
}
