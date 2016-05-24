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
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;

public class ProcessHaltJanitor extends ProcessHierarchyStateChangeJanitor
{
   public static final Logger trace = LogManager.getLogger(ProcessHaltJanitor.class);

   public static final String PRP_RETRY_COUNT = "Infinity.Engine.ProcessHalt.Failure.RetryCount";
   public static final String PRP_RETRY_PAUSE = "Infinity.Engine.ProcessHalt.Failure.RetryPause";

   public static void schedule(long processInstanceOid, long haltingUserOid)
   {
      scheduleJanitor(new Carrier(processInstanceOid, haltingUserOid), false);
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
   protected long getRetryPause()
   {
      return Parameters.instance().getLong(PRP_RETRY_PAUSE, 500);
   }

   @Override
   protected void processPi(ProcessInstanceBean pi)
   {
      if (trace.isDebugEnabled()) trace.debug("Processing " + pi);

      if (pi.isHalting())
      {
         // TODO: (fh) should we lock at the beginning ?
         boolean hasActiveActivities = false;
         for (Iterator itr = ActivityInstanceBean.getAllForProcessInstance(pi); itr.hasNext();)
         {
            ActivityInstanceBean ai = (ActivityInstanceBean) itr.next();
            if (ActivityInstanceUtils.isHaltable(ai))
            {
               ai.lock();
               ai.setState(ActivityInstanceState.HALTED, executingUserOid);
               if (trace.isDebugEnabled()) trace.debug("Halted " + ai);
            }
            else
            {
               if (ActivityInstanceUtils.isActiveState(ai))
               {
                  hasActiveActivities = true;
               }
            }
         }

         if (hasActiveActivities)
         {
            if (trace.isDebugEnabled()) trace.debug(pi.toString() + " is still active, rescheduling.");
            schedule(pi.getOID(), executingUserOid);
         }
         else
         {
            pi.lock();
            pi.setState(ProcessInstanceState.HALTED);
            pi.addHaltingUserOid(executingUserOid);
            AuditTrailLogger.getInstance(LogCode.ENGINE, pi).info("Process instance halted.");
            if (trace.isDebugEnabled()) trace.debug("Halted " + pi);
         }
      }
      else
      {
         if (trace.isDebugEnabled()) trace.debug(pi.toString() + " is in " + pi.getState() + " state!");
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
