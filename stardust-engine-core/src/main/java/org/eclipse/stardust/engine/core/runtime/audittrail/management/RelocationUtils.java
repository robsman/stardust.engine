/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;

public final class RelocationUtils
{
   private RelocationUtils() {}
   
   public static List<TransitionTarget> getRelocateTargets(long activityInstanceOid, TransitionOptions options, ScanDirection direction)
   {
      IActivityInstance ai = ActivityInstanceBean.findByOID(activityInstanceOid);
      IActivity activity = ai.getActivity();
      
      if (!activity.getBooleanAttribute(PredefinedConstants.ACTIVITY_IS_RELOCATE_SOURCE_ATT))
      {
         // activity is not a relocation source
         return Collections.emptyList();
      }
      
      Stack<TransitionStep> steps = new Stack();
      List<TransitionTarget> targets = CollectionUtils.newList();
      Set<TransitionTarget> visited = CollectionUtils.newSet();
      switch (direction)
      {
      case FORWARD:
         addActivities(visited, targets, ai, options == null ? TransitionOptions.DEFAULT : options, true, steps);
         break;
      case BACKWARD:
         addActivities(visited, targets, ai, options == null ? TransitionOptions.DEFAULT : options, false, steps);
         break;
      default:
         addActivities(visited, targets, ai, options == null ? TransitionOptions.DEFAULT : options, true, steps);
         addActivities(visited, targets, ai, options == null ? TransitionOptions.DEFAULT : options, false, steps);
      }
      return targets;
   }

   private static void addActivities(Set<TransitionTarget> visited, List<TransitionTarget> targets, IActivityInstance ai, TransitionOptions options, boolean forward, Stack<TransitionStep> steps)
   {
      if (ai != null)
      {
         steps.push(TransitionTargetFactory.createTransitionStep(ai));
         // add activities from current process definition
         addActivities(visited, targets, ai.getActivity(), options, forward, steps);
         // step up into the calling process - starting activity cannot be a relocation target
         if (options.isTransitionOutOfSubprocessesAllowed())
         {
            addActivities(visited, targets, ai.getProcessInstance().getStartingActivityInstance(), options, forward, steps);
         }
         steps.pop();
      }
   }

   private static void addActivities(Set<TransitionTarget> visited, List<TransitionTarget> targets, IActivity activity, TransitionOptions options, boolean forward, Stack<TransitionStep> steps)
   {
      ModelElementList<ITransition> transitions = forward ? activity.getOutTransitions() : activity.getInTransitions();
      JoinSplitType jsType = forward ? activity.getSplitType() : activity.getJoinType();
      if (JoinSplitType.And == jsType && transitions.size() > 1)
      {
         Stack<IActivity> startActivities = new Stack<IActivity>();
         startActivities.push(activity);
         IActivity target = consume(startActivities, asList(transitions), forward, options.areLoopsAllowed());
         if (target != null)
         {
            addActivity(visited, targets, target, options, forward, steps);
         }
      }
      else
      {
         for (ITransition transition : transitions)
         {
            IActivity target = forward ? transition.getToActivity() : transition.getFromActivity();
            jsType = forward ? target.getJoinType() : target.getSplitType();
            if (JoinSplitType.And != jsType)
            {
               addActivity(visited, targets, target, options, forward, steps);
            }
         }
      }
   }

   private static LinkedList<ITransition> asList(ModelElementList<ITransition> transitions)
   {
      LinkedList<ITransition> unconsumed = CollectionUtils.newLinkedList();
      for (ITransition transition : transitions)
      {
         unconsumed.add(transition);
      }
      return unconsumed;
   }

   private static IActivity consume(Stack<IActivity> startActivities, LinkedList<ITransition> unconsumed,
         boolean forward, boolean supportsLoops)
   {
      Set<ITransition> visited = CollectionUtils.newSet();

      while (!unconsumed.isEmpty())
      {
         ITransition transition = unconsumed.element();
         IActivity target = forward ? transition.getToActivity() : transition.getFromActivity();
         
         if (startActivities.contains(target))
         {
            // unsupported loop
            break;
         }

         JoinSplitType inJsType = forward ? target.getJoinType() : target.getSplitType();
         if (JoinSplitType.And == inJsType)
         {
            List<ITransition> pending = CollectionUtils.newList();
            ModelElementList<ITransition> transitions = forward ? target.getInTransitions() : target.getOutTransitions();
            for (ITransition incoming : transitions)
            {
               if (unconsumed.remove(incoming))
               {
                  pending.add(incoming);
               }
            }
            if (pending.size() == transitions.size()) // all incoming transitions consumed
            {
               if (unconsumed.isEmpty())
               {
                  return target;
               }
            }
            else
            {
               if (!unconsumed.isEmpty()) // unable to consume all transitions, but there are more branches, put them all back to the end
               {
                  unconsumed.addAll(pending);
               }
               continue;
            }
         }
         else
         {
            unconsumed.remove(transition);
         }

         ModelElementList<ITransition> transitions = forward ? target.getOutTransitions() : target.getInTransitions();
         if (transitions.isEmpty())
         {
            return null;
         }
         
         JoinSplitType outJsType = forward ? target.getSplitType() : target.getJoinType();
         while (target != null && JoinSplitType.And == outJsType && transitions.size() > 1)
         {
            startActivities.push(target);
            target = consume(startActivities, asList(transitions), forward, supportsLoops);
            startActivities.pop();
            if (target == null)
            {
               return null;
            }
            transitions = forward ? target.getOutTransitions() : target.getInTransitions();
            outJsType = forward ? target.getSplitType() : target.getJoinType();
         }

         for (ITransition out : transitions)
         {
            if (visited.contains(out)) // loop
            {
               if (!supportsLoops)
               {
                  return null;
               }
            }
            else
            {
               visited.add(out);
               unconsumed.add(out);
            }
         }
      }
      return null;
   }

   private static void addActivity(Set<TransitionTarget> visited, List<TransitionTarget> targets, IActivity target, TransitionOptions options,
         boolean forward, Stack<TransitionStep> steps)
   {
      TransitionTarget candidate = TransitionTargetFactory.createTransitionTarget(target, steps, forward);
      if (!visited.add(candidate))
      {
         // target already visited, stop processing
         return;
      }
      if (target.getBooleanAttribute(PredefinedConstants.ACTIVITY_IS_RELOCATE_TARGET_ATT))
      {
         // found a relocation target, check filters
         String processIdPattern = options.getProcessIdPattern();
         if (processIdPattern == null || target.getProcessDefinition().getId().matches(processIdPattern))
         {
            String activityIdPattern = options.getActivityIdPattern();
            if (activityIdPattern == null || target.getId().matches(activityIdPattern))
            {
               targets.add(candidate);
            }
         }
      }

      if (options.isTransitionIntoSubprocessesAllowed()
            && target.getImplementationType() == ImplementationType.SubProcess 
            && target.getSubProcessMode() != SubProcessModeKey.ASYNC_SEPARATE)
      {
         IProcessDefinition process = target.getImplementationProcessDefinition();
         if (process != null)
         {
            steps.push(TransitionTargetFactory.createTransitionStep(target));
            addActivities(visited, targets, process, options, forward, steps);
            steps.pop();
         }
      }

      addActivities(visited, targets, target, options, forward, steps);
   }

   private static void addActivities(Set<TransitionTarget> visited, List<TransitionTarget> targets, IProcessDefinition process, TransitionOptions options,
         boolean forward, Stack<TransitionStep> steps)
   {
      if (forward)
      {
         addActivity(visited, targets, process.getRootActivity(), options, forward, steps);
      }
      else
      {
         for (IActivity activity : getEndActivities(process))
         {
            addActivity(visited, targets, activity, options, forward, steps);
         }
      }
   }

   private static List<IActivity> getEndActivities(IProcessDefinition process)
   {
      List<IActivity> activities = CollectionUtils.newList();
      for (IActivity activity : process.getActivities())
      {
         if (activity.getOutTransitions().isEmpty())
         {
            activities.add(activity);
         }
      }
      return activities;
   }

   public static void performTransition(ActivityInstanceBean activityInstance, TransitionTarget transitionTarget,
         boolean complete)
   {
      ExecutionPlan plan = new ExecutionPlan(transitionTarget);
      plan.assertNoOtherActiveActivities();
      
      ModelManager mm = ModelManagerFactory.getCurrent();
      IActivity target = mm.findActivity(transitionTarget.getModelOid(), transitionTarget.getActivityRuntimeOid());
      if (target == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_ACTIVITY_IN_MODEL.raise(
               transitionTarget.getActivityRuntimeOid(), transitionTarget.getModelOid()));
      }
      
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      ExecutionPlan oldPlan = rtEnv.getExecutionPlan();
      try
      {
         rtEnv.setExecutionPlan(plan);
         if (complete)
         {
            ActivityInstanceUtils.complete(activityInstance, null, null, true);
         }
         else
         {
            long rootOid = plan.getRootActivityInstanceOid();
            if (rootOid != activityInstance.getOID())
            {
               activityInstance = ActivityInstanceUtils.lock(rootOid);
            }
            ActivityInstanceUtils.abortActivityInstance(activityInstance);
         }
      }
      finally
      {
         rtEnv.setExecutionPlan(oldPlan);
      }
   }
}
