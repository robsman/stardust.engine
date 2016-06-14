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

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;

public class ProcessHaltJanitor extends ProcessHierarchyStateChangeJanitor
{
   public static final Logger trace = LogManager.getLogger(ProcessHaltJanitor.class);

   public static final String PRP_RETRY_COUNT = "Infinity.Engine.ProcessHalt.Failure.RetryCount";
   public static final String PRP_RETRY_PAUSE = "Infinity.Engine.ProcessHalt.Failure.RetryPause";

   public static void schedule(long processInstanceOid, long haltingUserOid)
   {
      if (trace.isDebugEnabled()) trace.debug("Scheduling halt janitor for pi: " + processInstanceOid);
      scheduleJanitor(new Carrier(processInstanceOid, haltingUserOid), true);
   }

   public static void scheduleSeparate(long processInstanceOid, long haltingUserOid)
   {
      if (trace.isDebugEnabled()) trace.debug("Scheduling in separate transaction halt janitor for pi: " + processInstanceOid);
      scheduleJanitor(new Carrier(processInstanceOid, haltingUserOid), false, true);
   }

   private ProcessHaltJanitor(Carrier carrier)
   {
      super(carrier);
   }

   @Override
   protected HierarchyStateChangeJanitorCarrier getNewCarrier()
   {
      return new Carrier(processInstanceOid, triesLeft);
   }

   @Override
   protected boolean preventFinalState()
   {
      // does not prevent transition from Halting to Halted.
      return false;
   }

   @Override
   protected boolean doRollback()
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
      pi.lock();
      try
      {
         pi.reloadAttribute(ProcessInstanceBean.FIELD__STATE);
         if (pi.isHalting() || pi.isHalted())
         {
            if (trace.isDebugEnabled()) trace.debug("Processing " + pi + " in " + pi.getState() + " state!");
            boolean hasActiveActivities = false;
            for (Iterator itr = ActivityInstanceBean.getAllForProcessInstance(pi); itr.hasNext();)
            {
               ActivityInstanceBean ai = (ActivityInstanceBean) itr.next();
               ai.lock();
               try
               {
                  ai.reloadAttribute(ProcessInstanceBean.FIELD__STATE);
                  if (ActivityInstanceUtils.isHaltable(ai))
                  {
                     ai.setState(ActivityInstanceState.HALTED, executingUserOid);
                     if (trace.isDebugEnabled()) trace.debug("Halted " + ai);
                  }
                  else
                  {
                     if (ActivityInstanceUtils.isActiveState(ai))
                     {
                        if (trace.isDebugEnabled()) trace.debug("Found active " + ai);
                        hasActiveActivities = true;
                     }
                  }
               }
               catch (PhantomException e)
               {
                  // ignore
               }
            }

            if (hasActiveActivities)
            {
               try
               {
                  Thread.sleep(getRetryPause());
               }
               catch (InterruptedException e)
               {
                  throw new InternalException(e);
               }
               if (trace.isDebugEnabled()) trace.debug(pi.toString() + " is still active, rescheduling.");
               ProcessStopJanitorMonitor monitor = ProcessStopJanitorMonitor.getInstance();
               monitor.unregister(pi.getOID());
               schedule(pi.getOID(), executingUserOid);
            }
            else if (pi.isHalting())
            {
               pi.setState(ProcessInstanceState.HALTED);
               pi.addHaltingUserOid(executingUserOid);
               AuditTrailLogger.getInstance(LogCode.ENGINE, pi).info("Process instance halted.");
               if (trace.isDebugEnabled()) trace.debug("Halted " + pi);
            }
         }
         else
         {
            if (trace.isDebugEnabled()) trace.debug("Skipping " + pi + " in " + pi.getState() + " state!");
         }
      }
      catch (PhantomException ex)
      {
         // ignore
      }
   }

   @Override
   protected void postProcessPi(ProcessInstanceBean pi)
   {
   }

   public String toString()
   {
      return "Process halt janitor, pi = " + processInstanceOid;
   }

   /**
    * @author Roland.Stamm
    */
   public static class Carrier extends HierarchyStateChangeJanitorCarrier
   {
      private static final long serialVersionUID = 1L;

      /**
       * Default constructor, needed for creating instances via reflection.
       */
      public Carrier()
      {
      }

      public Carrier(long processInstanceOid, long haltingUserOid)
      {
         super(processInstanceOid, haltingUserOid,
               Parameters.instance().getInteger(ProcessHaltJanitor.PRP_RETRY_COUNT, 10));
      }

      @Override
      public Action doCreateAction()
      {
         return new ProcessHaltJanitor(this);
      }
   }
}
