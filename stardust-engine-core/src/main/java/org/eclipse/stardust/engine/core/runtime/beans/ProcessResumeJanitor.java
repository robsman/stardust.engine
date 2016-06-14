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

import static org.eclipse.stardust.engine.api.runtime.ActivityInstanceState.Aborted;
import static org.eclipse.stardust.engine.api.runtime.ActivityInstanceState.Aborting;
import static org.eclipse.stardust.engine.api.runtime.ActivityInstanceState.Completed;
import static org.eclipse.stardust.engine.api.runtime.ActivityInstanceState.Halted;

import java.util.Iterator;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;

public class ProcessResumeJanitor extends ProcessHierarchyStateChangeJanitor
{
   public static final Logger trace = LogManager.getLogger(ProcessResumeJanitor.class);

   public static final String PRP_RETRY_COUNT = "Infinity.Engine.ProcessResume.Failure.RetryCount";
   public static final String PRP_RETRY_PAUSE = "Infinity.Engine.ProcessResume.Failure.RetryPause";

   public static final void schedule(long processInstanceOid)
   {
      if (trace.isDebugEnabled()) trace.debug("Scheduling resume janitor for pi: " + processInstanceOid);
      scheduleJanitor(new Carrier(processInstanceOid), true);
   }

   private ProcessResumeJanitor(Carrier carrier)
   {
      super(carrier);
   }

   @Override
   protected HierarchyStateChangeJanitorCarrier getNewCarrier()
   {
      return new Carrier(processInstanceOid);
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
   protected boolean doRollback()
   {
      return true;
   }

   @Override
   protected void processPi(ProcessInstanceBean pi)
   {
      if (pi.isHalting())
      {
         ensureHalted(pi);
      }
      else if (canResume(pi))
      {
         if (pi.getPersistenceController() != null && !pi.getPersistenceController().isLocked())
         {
            pi.lock();
            try
            {
               pi.reloadAttribute(ProcessInstanceBean.FIELD__STATE);
            }
            catch (PhantomException e)
            {
               throw new InternalException(e);
            }
         }

         if (pi.isHalting())
         {
            ensureHalted(pi);
         }
         else if (!pi.isTerminated())
         {
            /* check if this is needed */
            IProcessInstance rootProcessInstance = pi.getRootProcessInstance();
            rootProcessInstance.lock();
            Object old = rootProcessInstance.getHaltingPiOids();
            rootProcessInstance.removeHaltingPiOid(pi.getOID());
            if (trace.isDebugEnabled()) trace.debug("Removed " + pi
                  + " from  halting " + rootProcessInstance
                  + ", old list: " + old
                  + ", new list: " + rootProcessInstance.getHaltingPiOids());
            /* end check block */

            // sets to active and sends resume event.
            pi.resetInterrupted();
            if (trace.isDebugEnabled()) trace.debug("Resumed " + pi);

            for (Iterator aiIter = ActivityInstanceBean.getAllForProcessInstance(pi); aiIter.hasNext();)
            {
               ActivityInstanceBean activityInstance = (ActivityInstanceBean) aiIter.next();
               if (activityInstance.isHalted())
               {
                  activityInstance.lock();
                  restoreStateFromHistory(activityInstance);
                  if (trace.isDebugEnabled()) trace.debug("Resumed " + activityInstance);
               }
            }

            // Run recovery. This also calls recovery on all events.
            if (trace.isDebugEnabled()) trace.debug("Scheduling recovery for " + pi);
            new ActivityThreadsRecoveryAction(pi.getOID()).execute();

            AuditTrailLogger.getInstance(LogCode.ENGINE, pi).info("Process instance resumed.");
         }
      }
   }

   protected void ensureHalted(ProcessInstanceBean pi)
   {
      try
      {
         Thread.sleep(getRetryPause());
      }
      catch (InterruptedException e)
      {
         throw new InternalException(e);
      }
      // schedules retry
      if (trace.isDebugEnabled()) trace.debug(pi.toString() + " is still halting, rescheduling.");
      ProcessStopJanitorMonitor monitor = ProcessStopJanitorMonitor.getInstance();
      monitor.unregister(pi.getOID());
      schedule(pi.getOID());
   }

   private void restoreStateFromHistory(final ActivityInstanceBean activityInstance)
   {
      ActivityInstanceState targetState = null;
      long userOid = executingUserOid;

      Iterator<ActivityInstanceHistoryBean> historicStates = ActivityInstanceHistoryBean.getAllForActivityInstance(activityInstance, false);
      if (historicStates == null || !historicStates.hasNext())
      {
         trace.warn("Activity instance (oid '" + activityInstance.getOID()
               + "') has no historical states. Resuming to Created state.");
      }
      else
      {
         boolean haltedFound = false;
         while (historicStates.hasNext())
         {
            ActivityInstanceHistoryBean history = (ActivityInstanceHistoryBean) historicStates.next();
            ActivityInstanceState state = history.getState();
            if (haltedFound)
            {
               if (Aborted.equals(state) || Aborting.equals(state) || Completed.equals(state))
               {
                  // terminated states should not be in history before halt.
                  trace.error(state + " not expected.");
                  return;
               }
               else if (Halted.equals(state))
               {
                  // in case of multiple 'halted' entries, continue with next state before halted.
                  continue;
               }
               targetState = state;
               if (history.getUserOid() != 0)
               {
                  userOid = history.getUserOid();
               }
               break;
            }
            else if (ActivityInstanceState.Halted.equals(state))
            {
               // halted state found, continue to state before halted.
               haltedFound = true;
            }
         }
         if (haltedFound && targetState == null)
         {
            trace.warn("Activity instance (oid '" + activityInstance.getOID()
                  + "') has no historical states before it was halted. Resuming to Created state.");
         }
      }
      // if no previous state found resume to created
      activityInstance.setState(targetState == null ? ActivityInstanceState.CREATED : targetState.getValue(), userOid);
   }

   private boolean canResume(ProcessInstanceBean pi)
   {
      // check if all linked inserted processes are terminated.
      ResultIterator<IProcessInstanceLink> links = ProcessInstanceLinkBean
            .findAllForProcessInstance(pi);
      if (links != null)
      {
         while (links.hasNext())
         {
            IProcessInstanceLink link = links.next();
            if (PredefinedProcessInstanceLinkTypes.INSERT.getId()
                  .equals(link.getLinkType().getId())
                  && link.getProcessInstanceOID() == pi.getOID())
            {
               IProcessInstance linkedPi = link.getLinkedProcessInstance();
               if (!linkedPi.isTerminated())
               {
                  trace.debug(
                        "Cannot resume halted process '" + link.getProcessInstanceOID()
                              + "'. The linked inserted processes '"
                              + linkedPi.getOID() + "' is not terminated.");
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

   public static class Carrier extends HierarchyStateChangeJanitorCarrier
   {
      private static final long serialVersionUID = 1L;

      /**
       * Default constructor needed by reflection.
       */
      public Carrier()
      {
      }

      public Carrier(long processInstanceOid)
      {
         super(processInstanceOid, 0, Parameters.instance().getInteger(
               ProcessResumeJanitor.PRP_RETRY_COUNT, 10));
      }

      @Override
      public Action doCreateAction()
      {
         return new ProcessResumeJanitor(this);
      }
   }
}
