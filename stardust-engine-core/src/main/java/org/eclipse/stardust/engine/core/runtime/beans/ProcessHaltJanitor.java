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
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;

public class ProcessHaltJanitor extends ProcessHierarchyStateChangeJanitor
{
   public static final Logger trace = LogManager.getLogger(ProcessHaltJanitor.class);

   public static final String PRP_RETRY_COUNT = "Infinity.Engine.ProcessHalt.Failure.RetryCount";
   public static final String PRP_RETRY_PAUSE = "Infinity.Engine.ProcessHalt.Failure.RetryPause";

   public ProcessHaltJanitor(HaltJanitorCarrier carrier)
   {
      super(carrier);
   }

   @Override
   protected HierarchyStateChangeJanitorCarrier getNewCarrier()
   {
      return new HaltJanitorCarrier(processInstanceOid, triesLeft);
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
      if (!pi.isTerminated() && !pi.isHalted())
      {
         pi.lock();

         pi.setState(ProcessInstanceState.HALTED);
         pi.addHaltingUserOid(executingUserOid);

         for (Iterator aiIter = ActivityInstanceBean.getAllForProcessInstance(pi); aiIter
               .hasNext();)
         {
            final ActivityInstanceBean activityInstance = (ActivityInstanceBean) aiIter
                  .next();

            if (ActivityInstanceUtils.isHaltable(activityInstance))
            {
               activityInstance.lock();

               activityInstance.setState(ActivityInstanceState.HALTED, executingUserOid);

               // Do not remove from worklist.
               // activityInstance.removeFromWorklists();

               // Do not remove events.
               // EventUtils.detachAll(activityInstance);
            }
         }
         // Do not remove events.
         // EventUtils.detachAll(pi);

         AuditTrailLogger.getInstance(LogCode.ENGINE, pi).info(
               "Process instance halted.");
      }
   }

   @Override
   protected void postProcessPi(ProcessInstanceBean pi)
   {
      removePiFromHaltingList(pi);
   }

   private void removePiFromHaltingList(ProcessInstanceBean pi)
   {
      IProcessInstance rootPi = pi.getRootProcessInstance();
      rootPi.lock();
      rootPi.removeHaltingPiOid(processInstanceOid);
   }

   public String toString()
   {
      return "Process halt janitor, pi = " + processInstanceOid;
   }

}
