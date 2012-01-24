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

import java.util.Collections;

import org.eclipse.stardust.common.Action;
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
            if (ai.getState() == ActivityInstanceState.Interrupted
                || ai.getState() == ActivityInstanceState.Created
                || isAbortingAndReadyForScheduling(ai))
            {
               ActivityThread.schedule(pi, null, ai, true, null,
                     Collections.EMPTY_MAP, false);
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
                        Predicates.notEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.ABORTED))));
      
      while(iterator.hasNext())
      {
         ActivityInstanceBean ai = (ActivityInstanceBean) iterator.next();
         EventUtils.recoverEvent(ai);
      }      
   }
}