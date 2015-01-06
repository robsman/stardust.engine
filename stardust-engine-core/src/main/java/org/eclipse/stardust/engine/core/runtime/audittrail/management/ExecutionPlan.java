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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.api.runtime.TransitionInfo;
import org.eclipse.stardust.engine.api.runtime.TransitionStep;
import org.eclipse.stardust.engine.api.runtime.TransitionTarget;
import org.eclipse.stardust.engine.core.model.utils.ModelElementListAdapter;
import org.eclipse.stardust.engine.core.runtime.beans.*;

public class ExecutionPlan
{
   private static final Class[] ACTIVITY_INTERFACES = {IActivity.class};
   private static final Class[] TRANSITION_INTERFACES = {ITransition.class};
   
   private TransitionTarget transitionTarget;
   private int current = 0;

   private ITransition transition;

   private TransitionTokenBean token;
   private boolean terminated;

   public ExecutionPlan(TransitionTarget transitionTarget)
   {
      this.transitionTarget = transitionTarget;
   }

   public long getRootActivityInstanceOid()
   {
      long rootOid = transitionTarget.getActivityInstanceOid();
      List<TransitionStep> steps = transitionTarget.getTransitionSteps();
      for (TransitionStep step : steps)
      {
         if (step.isUpwards())
         {
            current++;
            rootOid = step.getActivityInstanceOid();
         }
         else
         {
            break;
         }
      }
      return rootOid;
   }

   public void checkNextStep(IActivityInstance activityInstance)
   {
      if (!terminated)
      {
         List<TransitionStep> steps = transitionTarget.getTransitionSteps();
         if (current < steps.size())
         {
            TransitionStep step = steps.get(current);
            if (step.isUpwards() && activityInstance != null && activityInstance.getOID() == step.getActivityInstanceOid())
            {
               current++;
               return;
            }
         }
         throw new InternalException("Execution plan failed. Unexpected activity instance: " + activityInstance);
      }
   }

   public boolean hasNextActivity()
   {
      if (terminated)
      {
         return false;
      }
      List<TransitionStep> steps = transitionTarget.getTransitionSteps();
      return current == steps.size() || current < steps.size() && !steps.get(current).isUpwards();
   }

   public IActivity getCurrentStep()
   {
      List<TransitionStep> steps = transitionTarget.getTransitionSteps();
      if (current < steps.size())
      {
         TransitionStep step = steps.get(current);
         if (step.isUpwards())
         {
            throw new InternalException("Execution plan failed.");
         }
         ModelManager mm = ModelManagerFactory.getCurrent();
         return mm.findActivity(step.getModelOid(), step.getActivityRuntimeOid());
      }
      return null;
   }

   public boolean nextStep()
   {
      List<TransitionStep> steps = transitionTarget.getTransitionSteps();
      if (current < steps.size())
      {
         current++;
      }
      return current < steps.size();
   }

   public ITransition getTransition()
   {
      if (this.transition == null)
      {
         IActivityInstance ai = ActivityInstanceBean.findByOID(transitionTarget.getActivityInstanceOid());
         final IActivity from = ai.getActivity();
         final IActivity to = getTargetActivity();
         IProcessDefinition pd = to.getProcessDefinition();
         final ITransition transition = pd.findTransition(PredefinedConstants.RELOCATION_TRANSITION_ID);
         InvocationHandler activityHandler = new InvocationHandler()
         {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
               if (method.getDeclaringClass().equals(IActivity.class) && (args == null || args.length == 0))
               {
                  if ("getInTransitions".equals(method.getName()))
                  {
                     return new ModelElementListAdapter<ITransition>(Collections.singletonList(ExecutionPlan.this.transition));
                  }
               }
               return method.invoke(to, args);
            }
         };
         final IActivity fixedTo = (IActivity) Proxy.newProxyInstance(
               Thread.currentThread().getContextClassLoader(),
               ACTIVITY_INTERFACES, activityHandler);
         InvocationHandler transitionHandler = new InvocationHandler()
         {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
               if (method.getDeclaringClass().equals(ITransition.class) && (args == null || args.length == 0))
               {
                  if ("getFromActivity".equals(method.getName()) || "getFirst".equals(method.getName()))
                  {
                     return from;
                  }
                  if ("getToActivity".equals(method.getName()) || "getSecond".equals(method.getName()))
                  {
                     return fixedTo;
                  }
               }
               return method.invoke(transition, args);
            }
         };
         this.transition = (ITransition) Proxy.newProxyInstance(
               Thread.currentThread().getContextClassLoader(),
               TRANSITION_INTERFACES, transitionHandler );
      }
      return this.transition;
   }

   public boolean hasMoreSteps()
   {
      return current < transitionTarget.getTransitionSteps().size();
   }

   public boolean hasMoreSteps2()
   {
      return current + 1 < transitionTarget.getTransitionSteps().size();
   }

   public IActivity getTargetActivity()
   {
      ModelManager mm = ModelManagerFactory.getCurrent();
      return mm.findActivity(transitionTarget.getModelOid(), transitionTarget.getActivityRuntimeOid());
   }

   public boolean isStart()
   {
      return current == 0;
   }

   public void setToken(TransitionTokenBean token)
   {
      this.token = token;
   }

   public TransitionTokenBean getToken()
   {
      return token;
   }

   public void assertNoOtherActiveActivities()
   {
      TransitionInfo previous = transitionTarget;
      List<TransitionStep> steps = transitionTarget.getTransitionSteps();
      for (TransitionStep step : steps)
      {
         if (!step.isUpwards())
         {
            break;
         }
         IActivityInstance ai = ActivityInstanceBean.findByOID(previous.getActivityInstanceOid());
         assertNoOtherActiveActivities(ai);
         previous = step;
      }
   }

   private static void assertNoOtherActiveActivities(IActivityInstance ai)
   {
      IProcessInstance pi = ai.getProcessInstance();
      Iterator<IActivityInstance> iterator = ActivityInstanceBean.getAllForProcessInstance(pi);
      while (iterator.hasNext())
      {
         IActivityInstance nextAi = iterator.next();
         if (ai != nextAi)
         {
            
            ErrorCase errorCase = null; // TODO: (fh) - process has more than 1 active activity instance. 
            throw new IllegalOperationException(errorCase);
         }
      }
   }

   public boolean isStepUpwards()
   {
      List<TransitionStep> steps = transitionTarget.getTransitionSteps();
      if (current < steps.size())
      {
         TransitionStep step = steps.get(current);
         if (step.isUpwards())
         {
            return true;
         }
      }
      return false;
   }

   public boolean hasStartActivity()
   {
      return transitionTarget.getActivityInstanceOid() >= 0;
   }

   public void terminate()
   {
      terminated  = true;
   }

   public boolean isTerminated()
   {
      return terminated;
   }

   public IActivityInstance getStartActivityInstance()
   {
      return ActivityInstanceBean.findByOID(transitionTarget.getActivityInstanceOid());
   }
}
