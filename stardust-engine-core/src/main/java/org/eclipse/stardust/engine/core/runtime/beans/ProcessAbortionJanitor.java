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

import java.util.Iterator;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 *
 * @author sborn
 * @version $Revision: $
 */
public class ProcessAbortionJanitor extends ProcessHierarchyStateChangeJanitor
{
   public static final Logger trace = LogManager.getLogger(ProcessAbortionJanitor.class);

   public static final String PRP_RETRY_COUNT = "Infinity.Engine.ProcessAbortion.Failure.RetryCount";
   public static final String PRP_RETRY_PAUSE = "Infinity.Engine.ProcessAbortion.Failure.RetryPause";

   public ProcessAbortionJanitor(AbortionJanitorCarrier carrier)
   {
      super(carrier);
   }

   @Override
   protected HierarchyStateChangeJanitorCarrier getNewCarrier()
   {
      return new AbortionJanitorCarrier(processInstanceOid, triesLeft);
   }

   @Override
   protected boolean preventFinalState()
   {
      return Parameters.instance().getBoolean(
            KernelTweakingProperties.PREVENT_ABORTING_TO_ABORTED_STATE_CHANGE, false);
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

         pi.setTerminationTime(TimestampProviderUtils.getTimeStamp());
         pi.setState(ProcessInstanceState.ABORTED);
         pi.addAbortingUserOid(executingUserOid);

         for (Iterator aiIter = ActivityInstanceBean.getAllForProcessInstance(pi); aiIter
               .hasNext();)
         {
            final ActivityInstanceBean activityInstance = (ActivityInstanceBean) aiIter
                  .next();

            if (!activityInstance.isTerminated())
            {
               activityInstance.lock();

               activityInstance.setState(ActivityInstanceState.ABORTED, executingUserOid);
               activityInstance.removeFromWorklists();
               EventUtils.detachAll(activityInstance);
            }
         }

         EventUtils.detachAll(pi);
         ProcessInstanceUtils.cleanupProcessInstance(pi);

         AuditTrailLogger.getInstance(LogCode.ENGINE, pi).info(
               "Process instance aborted.");
      }
   }

   @Override
   protected void postProcessPi(ProcessInstanceBean pi)
   {
      removePiFromAbortingList(pi);
      stopStartingActivityInstance(pi);
   }

   private void stopStartingActivityInstance(IProcessInstance pi)
   {
      IActivityInstance startingActivityInstance = pi.getStartingActivityInstance();
      if (startingActivityInstance != null)
      {
         ActivityInstanceUtils.scheduleNewActivityThread(startingActivityInstance);
      }
      else
      {
         // if it's a spawned sub process, try to complete it's parent
         ProcessCompletionJanitor.resumeParentOfSpawnedSubprocess(pi, false);
      }
   }

   private void removePiFromAbortingList(ProcessInstanceBean pi)
   {
      IProcessInstance rootPi = pi.getRootProcessInstance();
      rootPi.lock();
      rootPi.removeAbortingPiOid(processInstanceOid);
   }

   public String toString()
   {
      return "Process abortion janitor, pi = " + processInstanceOid;
   }

}
