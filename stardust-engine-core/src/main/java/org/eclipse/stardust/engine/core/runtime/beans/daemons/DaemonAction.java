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
package org.eclipse.stardust.engine.core.runtime.beans.daemons;

import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DaemonDetails;
import org.eclipse.stardust.engine.api.runtime.AcknowledgementState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DaemonExecutionState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Will execute the 'daemon'. Also responsible for handling acknowledge requests.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DaemonAction extends SecurityContextAwareAction
{
   private static final String INVALID_ACTION = "invalid_action";

   public static final Logger daemonLogger = RuntimeLog.DAEMON;

   private static final Logger trace = LogManager.getLogger(DaemonAction.class);

   private final String type;

   private IDaemon daemon;

   private final DaemonCarrier originalCarrier;

   public DaemonAction(DaemonCarrier carrier)
   {
      super(carrier);

      this.type = carrier.getType();
      this.originalCarrier = carrier;
   }

   public Object execute()
   {
      daemon = DaemonFactory.instance().get(type);
      if (daemon == null)
      {
         throw new InternalException("Unknown daemon type '" + type + "'.");
      }

      ForkingServiceFactory factory = Parameters.instance().getObject(
            EngineProperties.FORKING_SERVICE_HOME);
      ForkingService service = null;
      try
      {
         service = factory.get();
         DaemonCarrier carrier = originalCarrier.copy();

         // (fh) check if acknowledgement was requested.
         DaemonLog daemonLog = acknowledge(service, carrier);
         if (daemonLog.getTimeStamp() > 0)
         {
            try
            {
               GetDaemonLogAction getLastExecutionLogAction = GetDaemonLogAction.getLastExecutionLog(carrier);
               DaemonLog lastExecutionLog = (DaemonLog) service.isolate(getLastExecutionLogAction);

               DaemonUtils.getExecutionMonitor().beforeExecute(
                     new DaemonDetails(daemonLog.getType(), carrier.getStartTimeStamp(),
                           lastExecutionLog.getTimeStamp(), true,
                           daemonLog.getAcknowledgementState(),
                           daemonLog.getDaemonExecutionState()));

               long batchSize = Parameters.instance().getLong(
                     type + DaemonProperties.DAEMON_BATCH_SIZE_SUFFIX, Long.MAX_VALUE);
               ExecuteDaemonAction innerAction = new ExecuteDaemonAction(carrier, daemon,
                     batchSize);

               daemonLogger.info("Running daemon '" + type.toString() + "'.");
               while (IDaemon.WORK_PENDING.equals(innerAction.getExecutionStatus()))
               {
                  SecurityContextBoundAction securityContextBoundAction = SecurityContextAwareAction.actionDefinesSecurityContext(innerAction);
                  service.isolate(securityContextBoundAction);
                  // (Florin.Herinean) acknowledge at the end of each loop to increase
                  // responsiveness
                  acknowledge(service, carrier);
               }

               if (IDaemon.WORK_DONE.equals(innerAction.getExecutionStatus()))
               {
                  // (fh) mark successful execution
                  carrier.setStartTimeStamp(TimestampProviderUtils.getTimeStampValue());
                  SetDaemonLogAction setLastExecutionLogAction = SetDaemonLogAction.setLastExecutionLog(
                        carrier, DaemonExecutionState.OK);
                  service.isolate(setLastExecutionLogAction);
               }
               
               getLastExecutionLogAction = GetDaemonLogAction.getLastExecutionLog(carrier);
               lastExecutionLog = (DaemonLog) service.isolate(getLastExecutionLogAction);
               
               DaemonUtils.getExecutionMonitor().afterExecute(
                     new DaemonDetails(daemonLog.getType(), carrier.getStartTimeStamp(),
                           lastExecutionLog.getTimeStamp(), true,
                           daemonLog.getAcknowledgementState(),
                           daemonLog.getDaemonExecutionState()));
            }
            catch (Exception ex)
            {
               AuditTrailLogAction logAction = new AuditTrailLogAction("Execution for '"
                     + type + "' daemon failed.", ex);
               service.isolate(logAction);
            }
         }
      }
      catch (Exception x)
      {
         AuditTrailLogAction logAction = new AuditTrailLogAction("Execution for " + type
               + " daemon failed. It will be stopped now.", x);
         Object result = service.isolate(logAction);
         if (result != null && result.equals(INVALID_ACTION))
         {
            return null;
         }

         DaemonCarrier carrier = originalCarrier.copy();

         service.isolate(new DaemonOperation(DaemonOperation.Type.STOP, carrier));

         carrier.setStartTimeStamp(0);
         SetDaemonLogAction setStartLogAction = SetDaemonLogAction.setStartLog(carrier,
               AcknowledgementState.RespondedFailure);
         service.isolate(setStartLogAction);

         carrier.setStartTimeStamp( -1);
         SetDaemonLogAction setLastExecutionLogAction = SetDaemonLogAction.setLastExecutionLog(
               carrier, DaemonExecutionState.Fatal);
         service.isolate(setLastExecutionLogAction);
      }
      finally
      {
         factory.release(service);
      }
      return null;
   }

   private DaemonLog acknowledge(final ForkingService service, final DaemonCarrier carrier)
         throws Exception
   {
      DaemonLog daemonLog = null;
      DaemonRetry daemonRetry = new DaemonRetry(service);
      try
      {
         while (daemonRetry.hasRetriesLeft())
         {
            try
            {
               Action getStartLogAction = GetDaemonLogAction.getStartLog(carrier);
               daemonLog = (DaemonLog) service.isolate(getStartLogAction);
               if (AcknowledgementState.Requested.equals(daemonLog.getAcknowledgementState()))
               {
                  carrier.setStartTimeStamp( -1);
                  SetDaemonLogAction setStartLogAction = SetDaemonLogAction.setStartLog(
                        carrier, AcknowledgementState.RespondedOK);
                  service.isolate(setStartLogAction);
               }
               break;
            }
            catch (Exception e)
            {
               daemonRetry.handleException(e);
            }
            daemonRetry.delayRetry();
         }
      }
      catch (Exception e)
      {
         daemonRetry.sendErrorMail(e);
         throw e;
      }
      return daemonLog;
   }

   public String toString()
   {
      return "Daemon action: '" + type + "'";
   }

   private class AuditTrailLogAction implements Action
   {
      private Exception x;

      private String message;

      public AuditTrailLogAction(String message, Exception x)
      {
         this.x = x;
         this.message = message;
      }

      public Object execute()
      {
         try
         {
            AuditTrailLogger.getInstance(LogCode.DAEMON).warn(message, x);
         }
         catch (Exception e)
         {
            LogUtils.traceException(e, true);
            return INVALID_ACTION;
         }

         return null;
      }
   }

   private static class ExecuteDaemonAction extends SecurityContextAwareAction
   {
      private static Set<String> locks = CollectionUtils.newSet();

      private final DaemonCarrier carrier;

      private final IDaemon daemon;

      private final long batchSize;

      private IDaemon.ExecutionResult execStatus;

      ExecuteDaemonAction(DaemonCarrier carrier, IDaemon daemon, long batchSize)
      {
         super(carrier);

         this.carrier = carrier;
         this.daemon = daemon;
         this.batchSize = batchSize;

         reset();
      }

      public Object execute()
      {
         boolean memoryLocked = false;
         try
         {
            try
            {
               acquireMemoryLock();
               memoryLocked = true;
               acquireDBLock();
            }
            catch (ConcurrencyException ex)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Lock timed out, cancelling execution: " + ex);
               }
               execStatus = IDaemon.WORK_CANCELLED;
               return null;
            }
            catch (Throwable t)
            {
               trace.warn("Unexpected exception: " + t);
               execStatus = IDaemon.WORK_CANCELLED;
               return null;
            }
            try
            {
               execStatus = daemon.execute(batchSize);
            }
            catch (PublicException e)
            {
               AuditTrailLogger.getInstance(LogCode.DAEMON).warn(
                     "Execution for " + daemon.getType() + " daemon failed: ", e);
            }
            catch (Exception e)
            {
               LogUtils.traceException(e, true);
            }
            return null;
         }
         finally
         {
            if (memoryLocked)
            {
               releaseMemoryLock();
            }
            // (fh) db locks will be released on transaction end (commit/rollback).
         }
      }

      private void releaseMemoryLock()
      {
         synchronized (locks)
         {
            locks.remove(carrier.getType());
         }
      }

      private void acquireMemoryLock()
      {
         synchronized (locks)
         {
            String type = carrier.getType();
            List<String> exclusiveDaemons = Parameters.instance().getStrings(
                  DaemonProperties.DAEMON_EXCLUSIVE_TYPES, ",");
            if (locks.contains(type))
            {
               throw new ConcurrencyException(
                     BpmRuntimeError.BPMRT_DAEMON_ALREADY_RUNNING.raise(type));
            }
            else if (exclusiveDaemons != null && exclusiveDaemons.contains(type))
            {
               for (String exclusiveType : exclusiveDaemons)
               {
                  if (locks.contains(exclusiveType))
                  {
                     throw new ConcurrencyException(
                           BpmRuntimeError.BPMRT_DAEMON_EXCLUSE_TYPE_LOCKED.raise(exclusiveType));
                  }
               }

            }
            locks.add(type);
         }
      }

      private void acquireDBLock()
      {
         Action getLastExecutionLogAction = GetDaemonLogAction.getLastExecutionLog(carrier);
         DaemonLog daemonLog = (DaemonLog) getLastExecutionLogAction.execute();
         daemonLog.lock();
      }

      public IDaemon.ExecutionResult getExecutionStatus()
      {
         return execStatus;
      }

      public String toString()
      {
         return "Daemon execution: '" + daemon.getType() + "'.";
      }

      public void reset()
      {
         execStatus = IDaemon.WORK_PENDING;
      }
   }
}