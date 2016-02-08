/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Iterator;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;

public class ProcessResumeJanitor extends ProcessHierarchyStateChangeJanitor
{
   public static final Logger trace = LogManager.getLogger(ProcessResumeJanitor.class);

   public static final String PRP_RETRY_COUNT = "Infinity.Engine.ProcessResume.Failure.RetryCount";
   public static final String PRP_RETRY_PAUSE = "Infinity.Engine.ProcessResume.Failure.RetryPause";

   public ProcessResumeJanitor(ResumeJanitorCarrier carrier)
   {
      super(carrier);
   }

   @Override
   protected HierarchyStateChangeJanitorCarrier getNewCarrier()
   {
      return new ResumeJanitorCarrier(processInstanceOid);
   }

   @Override
   protected boolean preventFinalState()
   {
      return false;
   }

   @Override
   protected long getRetryPause()
   {
      return Parameters.instance().getLong(PRP_RETRY_PAUSE, 500);
   }

   @Override
   protected void processPi(ProcessInstanceBean pi)
   {
      if (!pi.isTerminated())
      {
         pi.lock();

         if (canResume(pi))
         {
            IProcessInstance rootProcessInstance = pi.getRootProcessInstance();
            rootProcessInstance.lock();
            rootProcessInstance.removeHaltingPiOid(pi.getOID());

            // sets to active and sends resume event.
            pi.resetInterrupted();

            for (Iterator aiIter = ActivityInstanceBean
                  .getAllForProcessInstance(pi); aiIter.hasNext();)
            {
               final ActivityInstanceBean activityInstance = (ActivityInstanceBean) aiIter
                     .next();

               if (!activityInstance.isTerminated())
               {
                  activityInstance.lock();

                  restoreStateFromHistory(activityInstance);

                  // Run recovery. This also calls recovery on all events.
                  ActivityThreadsRecoveryAction activityThreadsRecoveryAction = new ActivityThreadsRecoveryAction(
                        pi.getOID());
                  activityThreadsRecoveryAction.execute();
               }
            }
            AuditTrailLogger.getInstance(LogCode.ENGINE, pi)
                  .info("Process instance resumed.");
         }
      }
   }

   private void restoreStateFromHistory(final ActivityInstanceBean activityInstance)
   {
      Iterator<ActivityInstanceHistoryBean> historicStates2 = ActivityInstanceHistoryBean
            .getAllForActivityInstance(activityInstance, false);
      boolean haltedFound = false;

      if (historicStates2 == null || !historicStates2.hasNext())
      {
         trace.warn("Activity instance " + activityInstance.getOID()
               + " has no historical states. Resuming to Created state.");
         activityInstance.setState(ActivityInstanceState.CREATED, executingUserOid);
      }

      while (historicStates2.hasNext())
      {
         ActivityInstanceHistoryBean activityInstanceHistoryBean = (ActivityInstanceHistoryBean) historicStates2
               .next();

         ActivityInstanceState state = activityInstanceHistoryBean.getState();
         if (haltedFound)
         {
            if (ActivityInstanceState.Interrupted.equals(state))
            {
               // return to interrupted.
               activityInstance.setState(ActivityInstanceState.INTERRUPTED,
                     executingUserOid);
            }
            else if (ActivityInstanceState.Hibernated.equals(state))
            {
               // return to hibernated.
               activityInstance.setState(ActivityInstanceState.HIBERNATED,
                     executingUserOid);
            }
            else if (ActivityInstanceState.Suspended.equals(state))
            {
               // return to suspended.
               activityInstance.setState(ActivityInstanceState.SUSPENDED,
                     executingUserOid);
            }
            else if (ActivityInstanceState.Created.equals(state))
            {
               // Recovery will continue the ai.
               activityInstance.setState(ActivityInstanceState.CREATED,
                     executingUserOid);
            }
            else if (ActivityInstanceState.Halted.equals(state))
            {
               // in case of multiple 'halted' entries, continue with next state before
               // halted.
               continue;
            }
            else
            {
               // terminated states should not be in history before halt.
               trace.error(state + " not expected.");
               continue;
            }

            // done, state before halted is restored.
            break;
         }
         else if (ActivityInstanceState.Halted.equals(state))
         {
            // halted state found, continue to state before halted.
            haltedFound = true;
         }
      } // end while
   }

   private boolean canResume(ProcessInstanceBean pi)
   {
      // check if all linked inserted processes are terminated.
      ResultIterator<IProcessInstanceLink> links = ProcessInstanceLinkBean.findAllForProcessInstance(pi);
      if (links !=null)
      {
         while (links.hasNext())
         {
            IProcessInstanceLink link = links.next();
            if (PredefinedProcessInstanceLinkTypes.INSERT.getId().equals(
                  link.getLinkType().getId()) && link.getProcessInstanceOID() == pi.getOID())
            {
               IProcessInstance linkedPi = link.getLinkedProcessInstance();
               if (!linkedPi.isTerminated())
               {
                  trace.info(
                        "Cannot resume halted process '" + link.getProcessInstanceOID()
                              + "'. The linked inserted processes '" + linkedPi.getOID()
                              + "' is not terminated.");
                  return false;
               }
            }
         }
      }
      return true;
   }

   @Override
   protected void postProcessPi(ProcessInstanceBean pi)
   {
   }

   public String toString()
   {
      return "Process resume janitor, pi = " + processInstanceOid;
   }

}
