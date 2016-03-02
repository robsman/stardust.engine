/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Collections;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ActivityThreadsRecoveryAction implements Action
{
   private long processInstanceOID;

   public ActivityThreadsRecoveryAction(long processInstanceOID)
   {
      this.processInstanceOID = processInstanceOID;
   }

   public Object execute()
   {
      boolean spawned = false;
      ResultIterator tokens = TransitionTokenBean.findForProcessInstance(processInstanceOID);

      while (tokens.hasNext())
      {
         TransitionTokenBean token = (TransitionTokenBean) tokens.next();
         if (token.isConsumed())
         {
            continue;
         }
         if (token.isBound())
         {
            IActivityInstance ai = ActivityInstanceBean.findByOID(token.getTarget());
            IProcessInstance pi = ProcessInstanceBean.findByOID(processInstanceOID);

            final ActivityInstanceState aiState = ai.getState();
            if (aiState == ActivityInstanceState.Interrupted
                || aiState == ActivityInstanceState.Created
                // cover case that AI is suspended but its subPI (if any) is already terminated
                || (isSuspendedSubPiActivity(ai) && hasTermintedSubPi(ai))
                || isAbortingAndReadyForScheduling(ai)
                // can be the case if activity was completed in halted hierarchy.
                || aiState == ActivityInstanceState.Completed)
            {
               if (isSuspendedSubPiActivity(ai))
               {
                  ai.activate();
               }

               ActivityThread.schedule(pi, null, ai, true, null, Collections.EMPTY_MAP,
                     false);
               spawned = true;
            }
         }
         else
         {
            IProcessInstance pi = ProcessInstanceBean.findByOID(processInstanceOID);

            if (token.getTransitionOID() == -1)
            {
               ActivityThread.schedule(pi, pi.getProcessDefinition().getRootActivity(),
                     null, true, null, Collections.EMPTY_MAP, false);
            }
            else
            {
               ActivityThread.schedule(pi, token.getTransition().getToActivity(), null,
                     true, null, Collections.EMPTY_MAP, false);
            }
            spawned = true;
         }
      }
      recoverProcess();

      return spawned ? Boolean.TRUE : Boolean.FALSE;
   }

   private boolean isSuspendedSubPiActivity(IActivityInstance ai)
   {
      return ActivityInstanceState.Suspended.equals(ai.getState())
            && ImplementationType.SubProcess.equals(ai.getActivity()
                  .getImplementationType());
   }

   private boolean hasTermintedSubPi(IActivityInstance ai)
   {
      IProcessInstance subPi = null;
      final ImplementationType implType = ai.getActivity().getImplementationType();
      if (ImplementationType.SubProcess.equals(implType))
      {
         subPi = ProcessInstanceBean.findForStartingActivityInstance(ai.getOID());
      }

      return subPi == null ? false : subPi.isTerminated();
   }

   public String toString()
   {
      return "Recovering process instance: " + processInstanceOID;
   }

   private static boolean isAbortingAndReadyForScheduling(IActivityInstance ai)
   {
      boolean result = false;
      if (ai.isAborting())
      {
         IProcessInstance subPi = null;
         if (ai.getActivity().getImplementationType().isSubProcess())
         {
            subPi = ProcessInstanceBean.findForStartingActivityInstance(ai.getOID());
         }

         if (null == subPi || subPi.isAborted())
         {
            result = true;
         }

      }
      return result;
   }

   private void recoverProcess()
   {
      EventUtils.recoverEvent(ProcessInstanceBean.findByOID(processInstanceOID));

      ResultIterator iterator = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            ActivityInstanceBean.class,
            QueryExtension.where(
                  Predicates.andTerm(
                        Predicates.isEqual(ActivityInstanceBean.FR__PROCESS_INSTANCE, processInstanceOID),
                        Predicates.notEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.COMPLETED),
                        Predicates.notEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.ABORTED),
                        Predicates.notEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.HALTED))));

      while(iterator.hasNext())
      {
         ActivityInstanceBean ai = (ActivityInstanceBean) iterator.next();
         EventUtils.recoverEvent(ai);
      }
   }
}