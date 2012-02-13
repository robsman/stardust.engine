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

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ExpectedFailureException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.ServiceException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceInfo;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.JoinSplitType;
import org.eclipse.stardust.engine.api.model.LoopType;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable.SymbolTableFactory;
import org.eclipse.stardust.engine.core.model.beans.TransitionBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger.LoggingBehaviour;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.tokencache.TokenCache;
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
    * Contains the data passed by awakening a HIBERNATED activity instance
    */
   private final Map receiverData;

   private final TokenCache tokenCache;

   private final ProcessCompletionJanitor janitor;
   private Throwable interruption;

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
      else
      {
         ActivityThreadCarrier carrier = new ActivityThreadCarrier();
         carrier.setProcessInstance(processInstance);
         carrier.setActivity(activity);
         carrier.setActivityInstance(activityInstance);
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
      }

      this.tokenCache = new TokenCache(this.processInstance);
      
      this.receiverData = receiverData;
      this.interruption = interruptionState;

      janitor = new ProcessCompletionJanitor(new JanitorCarrier(this.processInstance
            .getOID()), hasParent);
   }
   
   public void run()
   {
      if (isInAbortingPiHierarchy())
      {
         StringBuffer buffer = new StringBuffer();
         
         // TODO: trace the real state: aborted or aborting.
         buffer.append(
               "Scheduled activity thread will not be started because process instance ")
               .append(processInstance).append(" is aborted.");
         trace.info(buffer.toString());
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
   
         if (processInstance.getProcessDefinition().getRootActivity().getId()
               .equals(activity.getId()))
         {
            TransitionTokenBean startToken = tokenCache.lockFreeToken(START_TRANSITION);
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
            long start = System.currentTimeMillis();
            while (!enableVertex())
            {
               count++;
               if (count > RETRIES)
               {
                  if (RETRIES > 0)
                  {
                     trace.warn("No free tokens found in " + (System.currentTimeMillis() - start) + " ms, giving up.");
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
            if (context.isStepMode())
            {
               // wait for join from thread context (i.e. debug controller)
               context.suspendActivityThread(this);
            }
   
            runCurrentActivity();
   
            if ( !(activityInstance.isTerminated()) || activityInstance.isAborting()
                  || isInAbortingPiHierarchy())
            {
               break;
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
   
         LogUtils.traceException(x, false);
   
         AuditTrailLogger.getInstance(LogCode.ENGINE, processInstance).error(
               "Unexpected activity thread state.");
   
         throw new InternalException("Unexpected activity thread state.");
      }
   
      tokenCache.flush();
      janitor.execute();
   
      if (interruption != null)
      {
         throw new PublicException(interruption);
      }
   
   }

   private boolean isInAbortingPiHierarchy()
   {
      boolean result = false;
      final Long piOid = Long.valueOf(processInstance.getOID());

      if (piOid.longValue() == processInstance.getRootProcessInstanceOID())
      {
         return isAbortedStateSafe(processInstance);
      }
      else
      {
         IProcessInstance rootPi = processInstance.getRootProcessInstance();

         if (isAbortedStateSafe(rootPi))
         {
            result = true;
         }
         else
         {
            if(!rootPi.getPersistenceController().isLocked())
            {
               try
               {
                  rootPi.getPersistenceController().reloadAttribute(
                     ProcessInstanceBean.FIELD__PROPERTIES_AVAILABLE);
               }
               catch (PhantomException e) 
               {
                  throw new InternalException(e);
               }
            }
            if (rootPi
                  .isPropertyAvailable(ProcessInstanceBean.PI_PROPERTY_FLAG_PI_ABORTING))
            {
               List abortingOids = new ArrayList();
               for (Iterator iter = rootPi.getAbortingPiOids().iterator(); iter.hasNext();)
               {
                  Attribute attribute = (Attribute) iter.next();
                  abortingOids.add(attribute.getValue());
               }
   
               IProcessInstance currentPi = processInstance;
               while (null != currentPi)
               {
                  if (abortingOids.contains(Long.valueOf(currentPi.getOID())))
                  {
                     result = true;
                     break;
                  }
   
                  // get the parent process instance, if any.
                  IActivityInstance startingActivityInstance = currentPi
                        .getStartingActivityInstance();
                  if (null == startingActivityInstance)
                  {
                     currentPi = null;
                  }
                  else
                  {
                     currentPi = startingActivityInstance.getProcessInstance();
                  }
               }
            }
         }
      }

      return result;
   }
   
   private void createActivityInstance(List<TransitionTokenBean> inTokens)
   {
      if (LoopType.While.equals(activity.getLoopType())
            && !processInstance.validateLoopCondition(activity.getLoopCondition()))
      {
         activityInstance = new PhantomActivityInstance(activity);
      }
      else
      {
         activityInstance = new ActivityInstanceBean(activity, processInstance);

         getCurrentActivityThreadContext().enteringActivity(activityInstance);
      }

      for (int i = 0; i < inTokens.size(); ++i)
      {
         TransitionTokenBean freeToken = inTokens.get(i);
         tokenCache.bindToken(freeToken, this.activityInstance);
         if (JoinSplitType.Xor.equals(activity.getJoinType()))
         {
            break;
         }
      }
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
         int removedTokens = 0;

         List<TransitionTokenBean> boundInTokens = tokenCache.getBoundInTokens(activityInstance, activity);
         for (int i = 0; i < boundInTokens.size(); ++i)
         {
            // TODO (ab) wenn nicht die erste activityInstance (fork, recovery) UND XOR, dann nehme "current" (muss im local cache sein)
            TransitionTokenBean token = boundInTokens.get(i);
            tokenCache.consumeToken(token);
            removedTokens++;

            getCurrentActivityThreadContext().completingTransition(token);
         }

         List<ITransition> enabledTransitions = Collections.emptyList();
         List<ITransition> otherwiseTransitions = Collections.emptyList();

         // find traversable transitions and separate between enabled and otherwise ones
         ModelElementList outTransitions = activity.getOutTransitions();
         SymbolTable symbolTable = SymbolTableFactory.create(activityInstance, activity);
         for (int i = 0; i < outTransitions.size(); ++i)
         {
            ITransition transition = (ITransition) outTransitions.get(i);
            if (transition.isEnabled(symbolTable))
            {
               if ((1 == outTransitions.size())
                     || (JoinSplitType.Xor == activity.getSplitType()))
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
         
         int addedTokens = 0;
         
         final List<ITransition> enabledOutTransitions = enabledTransitions.isEmpty()
               ? otherwiseTransitions
               : enabledTransitions;

         for (int i = 0; i < enabledOutTransitions.size(); ++i)
         {
            ITransition transition = enabledOutTransitions.get(i);
            TransitionTokenBean token = tokenCache.createToken(transition, this.activityInstance);
            addedTokens++;

            getCurrentActivityThreadContext().enteringTransition(token);

            if (JoinSplitType.Xor == activity.getSplitType())
            {
               break;
            }
         }
         
         janitor.incrementCount(addedTokens - removedTokens);

         if (addedTokens == 0)
         {
            activityInstance = null;
         }
         else
         {
            boolean foundSynchronousSuccessor = false;

            List<TransitionTokenBean> freeOutTokens = tokenCache.getFreeOutTokens(activity);
            for (int i = 0; i < freeOutTokens.size(); ++i)
            {
               TransitionTokenBean token = freeOutTokens.get(i);
               ITransition transition = token.getTransition();
               IActivity targetActivity = transition.getToActivity();

               if ( !foundSynchronousSuccessor && !transition.getForkOnTraversal())
               {
                  activity = targetActivity;
                  if (enableVertex())
                  {
                     foundSynchronousSuccessor = true;
                  }
                  else
                  {
                     schedule(processInstance, targetActivity, null,
                           false, null, Collections.EMPTY_MAP, false);
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

   private boolean enableVertex()
   {
      List<TransitionTokenBean> freeTokens = null;

      for (int i = 0; i < activity.getInTransitions().size(); ++i)
      {
         ITransition transition = (ITransition) activity.getInTransitions().get(i);

         // TODO (ab) wenn nicht die erste activityInstance (fork, recovery) UND XOR, dann nehme "current" (muss im local cache sein)
         TransitionTokenBean freeToken = tokenCache.lockFreeToken(transition);
         if (null != freeToken)
         {
            if ((1 == activity.getInTransitions().size())
                  || JoinSplitType.Xor.equals(activity.getJoinType()))
            {
               // found the one sufficient token to proceed
               freeTokens = Collections.singletonList(freeToken);
               break;
            }
            else
            {
               if (null == freeTokens)
               {
                  freeTokens = CollectionUtils.newList();
               }
               freeTokens.add(freeToken);
            }
         }
         else if (JoinSplitType.And.equals(activity.getJoinType()))
         {
            // AND join will not be satisfied as at least one token is missing
            if (null != freeTokens)
            {
               tokenCache.unlockTokens(freeTokens);
            }
            return false;
         }
      }
      if ((null == freeTokens) || freeTokens.isEmpty())
      {
         return false;
      }

      tokenCache.registerPersistenceControllers(freeTokens);
      createActivityInstance(freeTokens);

      return true;
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
            
            activityInstance.complete();
         }
      }
      else if (activityInstance.getState() == ActivityInstanceState.Completed
            || activityInstance.getState() == ActivityInstanceState.Aborted)
      {
         Assert.lineNeverReached();
      }
      else if (activityInstance.getState() == ActivityInstanceState.Application)
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

   /**
    * Tests if the given process instance has a persisted state of ABORTING or ABORTED.
    * After that call the state is recovered.
    * 
    * @param pi the process instance
    * @return true if the persisted state is ABORTING or ABORT
    */
   private static boolean isAbortedStateSafe(IProcessInstance pi)
   {
      ProcessInstanceState stateBackup = pi.getState();
      if(ProcessInstanceState.Aborting.equals(stateBackup) ||
            ProcessInstanceState.Aborted.equals(stateBackup))
      {
         return true;
      }
      try
      {
         ((PersistentBean) pi).reloadAttribute(ProcessInstanceBean.FIELD__STATE);
         return pi.isAborting() || pi.isAborted();
      }
      catch (PhantomException x)
      {
         throw new InternalException(x);
      }
      finally
      {
         if ( !stateBackup.equals(pi.getState()))
         {
            try
            {
               Reflect.getField(ProcessInstanceBean.class,
                     ProcessInstanceBean.FIELD__STATE).setInt(pi, stateBackup.getValue());
            }
            catch (Exception e)
            {
               // should never happen
               throw new InternalException(e);
            }
         }
      }
   }

   private static class PhantomActivityInstance extends ActivityInstanceBean
   {
      PhantomActivityInstance(IActivity activity)
      {
         super();
         setOID(-1);
         this.model = activity.getModel().getModelOID();
         this.activity = ModelManagerFactory.getCurrent().getRuntimeOid(activity);
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
      QualityAssuranceResult result = aiAttributes.getQualityAssuranceResult();
      QualityAssuranceResult.ResultState resultState = result.getQualityAssuranceState();

      if (resultState == QualityAssuranceResult.ResultState.FAILED)
      {
         IActivityInstance oldInstance = activityInstance;
         IActivityInstance newInstance = null;
         
         //decide if new instance should go back to participant (default) or the last user
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
