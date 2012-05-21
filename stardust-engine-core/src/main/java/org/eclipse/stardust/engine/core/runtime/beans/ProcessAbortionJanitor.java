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

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.monitoring.MonitoringUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 *
 * @author sborn
 * @version $Revision: $
 */
public class ProcessAbortionJanitor extends SecurityContextAwareAction
{
   public static final Logger trace = LogManager.getLogger(ProcessAbortionJanitor.class);

   protected static final String PRP_RETRY_COUNT = "Infinity.Engine.ProcessAbortion.Failure.RetryCount";
   protected static final String PRP_RETRY_PAUSE = "Infinity.Engine.ProcessAbortion.Failure.RetryPause";

   private long processInstanceOid;
   private int triesLeft;
   private ProcessInstanceLocking piLock = new ProcessInstanceLocking();

   public ProcessAbortionJanitor(AbortionJanitorCarrier carrier)
   {
      super(carrier);
      this.processInstanceOid = carrier.getProcessInstanceOid();
      this.triesLeft = carrier.getTriesLeft();
   }

   public Object execute()
   {
      if (Parameters.instance().getBoolean(
            KernelTweakingProperties.PREVENT_ABORTING_TO_ABORTED_STATE_CHANGE, false))
      {
         return Boolean.TRUE;
      }

      triesLeft -= 1;

      boolean performed = false;
      ProcessInstanceBean pi = ProcessInstanceBean.findByOID(processInstanceOid);

      if ( !pi.isTerminated())
      {
         try
         {
            List<IProcessInstance> pis = piLock.lockAllTransitions(pi);

            abortAllProcessInstances(pis);
            removePiFromAbortingList(pi);
            abortStartingActivityInstance(pi);

            performed = true;
         }
         catch (ConcurrencyException e)
         {
            BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
            if (triesLeft > 0 && rtEnv.getExecutionPlan() == null)
            {
               trace.info(MessageFormat.format(
                     "Cannot run abortion janitor for {0} due to a locking conflict, scheduling a new one. Tries left: {1}.",
                     new Object[] { pi, new Integer(triesLeft) }));

               try
               {
                  Thread.sleep(Parameters.instance().getLong(PRP_RETRY_PAUSE, 500));
               }
               catch (InterruptedException x)
               {
               }
               scheduleJanitor(new AbortionJanitorCarrier(processInstanceOid, triesLeft));
            }
            else
            {
               trace.warn(MessageFormat.format("Could not run abortion janitor for {0}.",
                     new Object[] { pi }));
            }
         }
      }

      return performed ? Boolean.TRUE : Boolean.FALSE;
   }

   public String toString()
   {
      return "Process abortion janitor, pi = " + processInstanceOid;
   }



   private void abortAllProcessInstances(List<IProcessInstance> pis)
   {
      for (Iterator piIter = pis.iterator(); piIter.hasNext();)
      {
         ProcessInstanceBean pi = (ProcessInstanceBean) piIter.next();

         abort(pi);
      }
   }

   private void abort(ProcessInstanceBean pi)
   {
      if ( !pi.isTerminated())
      {
         pi.lock();

         pi.setTerminationTime(TimestampProviderUtils.getTimeStamp());
         pi.setState(ProcessInstanceState.ABORTED);

         for (Iterator aiIter = ActivityInstanceBean.getAllForProcessInstance(pi); aiIter
               .hasNext();)
         {
            final ActivityInstanceBean activityInstance = (ActivityInstanceBean) aiIter
                  .next();

            if ( !activityInstance.isTerminated())
            {
               activityInstance.lock();

               activityInstance.setState(ActivityInstanceState.ABORTED);
               activityInstance.removeFromWorklists();
               EventUtils.detachAll(activityInstance);
            }
         }

         EventUtils.detachAll(pi);
         MonitoringUtils.processExecutionMonitors().processAborted(pi);

         AuditTrailLogger.getInstance(LogCode.ENGINE, pi).info(
               "Process instance aborted.");
      }
   }

   private void abortStartingActivityInstance(IProcessInstance pi)
   {
      IActivityInstance startingActivityInstance = pi.getStartingActivityInstance();
      if (startingActivityInstance != null)
      {
         ActivityInstanceUtils.scheduleNewActivityThread(startingActivityInstance);
      }
   }

   private void removePiFromAbortingList(ProcessInstanceBean pi)
   {
      IProcessInstance rootPi = pi.getRootProcessInstance();
      rootPi.lock();
      rootPi.removeAbortingPiOid(processInstanceOid);
   }

   public static void scheduleJanitor(ActionCarrier carrier)
   {
      ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance().get(
            EngineProperties.FORKING_SERVICE_HOME);
      ForkingService service = null;
      try
      {
         service = factory.get();
         service.fork(carrier, true);
      }
      finally
      {
         factory.release(service);
      }
   }
}
