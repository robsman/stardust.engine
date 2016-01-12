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

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DaemonDetails;
import org.eclipse.stardust.engine.api.runtime.AcknowledgementState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.monitoring.DaemonExecutionMonitorMediator;
import org.eclipse.stardust.engine.core.persistence.jdbc.extension.ISessionLifecycleExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.extension.SessionLifecycleExtensionMediator;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.DaemonFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IDaemon;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.spi.monitoring.IDaemonExecutionMonitor;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;



/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DaemonUtils
{
   public static final Logger trace = LogManager.getLogger(DaemonUtils.class);
   public static final Logger daemonLogger = RuntimeLog.DAEMON;   
   
   private static final String KEY_SESSION_DAEMON_EXECUTION_MONITOR_MEDIATOR = DaemonExecutionMonitorMediator.class.getName()
         + ".RuntimeEnvironmentMonitorMediator";
   

   public static Daemon startDaemon(String daemonId, boolean acknowledge)
   {
      checkDaemonName(daemonId, true);

      ForkingServiceFactory factory = Parameters.instance().getObject(EngineProperties.FORKING_SERVICE_HOME);
      ForkingService service = null;
      try
      {
         service = factory.get();
         
         trace.info("Starting daemon '" + daemonId + "'.");
         daemonLogger.info("Starting daemon '" + daemonId + "'.");
         
         DaemonCarrier carrier = new DaemonCarrier(daemonId);
         carrier.setStartTimeStamp(TimestampProviderUtils.getTimeStampValue());

         SetDaemonLogAction setStartLogAction = SetDaemonLogAction.setStartLog(
               carrier, AcknowledgementState.Requested);
         service.isolate(setStartLogAction);

         DaemonOperation operation = new DaemonOperation(DaemonOperation.Type.START, carrier);
         service.isolate(operation);
         
         Daemon result = get(service, daemonId, acknowledge, null);
         if (result.isRunning())
         {
            // rsauer: write log entry after all nested TXs to reduce deadlock probability
            AuditTrailLogger logger = AuditTrailLogger.getInstance(LogCode.DAEMON);
            logger.info("Daemon '" + daemonId + "' started at "
                  + DateUtils.getInteractiveDateFormat().format(result.getStartTime()) + ".");
         }
         return result;
      }
      finally
      {
         factory.release(service);
      }
   }

   public static Daemon stopDaemon(String daemonId, boolean acknowledge)
   {
      checkDaemonName(daemonId, false);

      ForkingServiceFactory factory = Parameters.instance().getObject(EngineProperties.FORKING_SERVICE_HOME);
      ForkingService service = null;
      try
      {
         service = factory.get();
         
         trace.info("Stopping daemon '" + daemonId + "'.");
         daemonLogger.info("Stopping daemon '" + daemonId + "'.");
         
         DaemonCarrier carrier = new DaemonCarrier(daemonId);

         GetDaemonLogAction getStartLogAction = GetDaemonLogAction.getStartLog(carrier);
         DaemonLog startLog = (DaemonLog) service.isolate(getStartLogAction);

         AcknowledgementState ack = null;
         if (startLog.getTimeStamp() > 0)
         {
            carrier.setStartTimeStamp(0);
            SetDaemonLogAction setStartLogAction = SetDaemonLogAction.setStartLog(
                  carrier, acknowledge ? AcknowledgementState.Requested : startLog.getAcknowledgementState());
            service.isolate(setStartLogAction);
            if (acknowledge)
            {
               ack = acknowledge(service, daemonId);
            }
         }

         DaemonOperation operation = new DaemonOperation(DaemonOperation.Type.STOP, carrier);
         service.isolate(operation);

         return get(service, daemonId, acknowledge, ack);
      }
      finally
      {
         factory.release(service);
      }
   }

   public static Daemon getDaemon(String type, boolean acknowledge)
   {
      checkDaemonName(type, false);

      ForkingServiceFactory factory = Parameters.instance().getObject(EngineProperties.FORKING_SERVICE_HOME);
      ForkingService service = null;
      try
      {
         service = factory.get();

         return get(service, type, acknowledge, null);
      }
      finally
      {
         factory.release(service);
      }
   }

   public static IDaemonExecutionMonitor getExecutionMonitor()
   {
      GlobalParameters globals = GlobalParameters.globals();

      IDaemonExecutionMonitor mediator = (IDaemonExecutionMonitor) globals.get(KEY_SESSION_DAEMON_EXECUTION_MONITOR_MEDIATOR);

      if (null == mediator)
      {
         mediator = new DaemonExecutionMonitorMediator(
               ExtensionProviderUtils.getExtensionProviders(IDaemonExecutionMonitor.class));
         globals.set(KEY_SESSION_DAEMON_EXECUTION_MONITOR_MEDIATOR, mediator);
      }

      return mediator;
   }

   private static Daemon get(ForkingService service, String type, boolean acknowledge, AcknowledgementState ack)
   {
      DaemonCarrier carrier = new DaemonCarrier(type);
      
      GetDaemonLogAction getStartLogAction = GetDaemonLogAction.getStartLog(carrier);
      DaemonLog startLog = (DaemonLog) service.isolate(getStartLogAction);
      
      if (startLog.getTimeStamp() > 0)
      {
         if (acknowledge)
         {
            carrier.setStartTimeStamp(-1);
            
            SetDaemonLogAction setStartLogAction = SetDaemonLogAction.setStartLog(carrier,
                  AcknowledgementState.Requested);
            service.isolate(setStartLogAction);

            ack = acknowledge(service, type);
         }
      }
      else
      {
         if (acknowledge && ack == null)
         {
            ack = AcknowledgementState.RespondedOK;
         }
      }

      GetDaemonLogAction getLastExecutionLogAction = GetDaemonLogAction.getLastExecutionLog(carrier);
      DaemonLog lastExecutionLog = (DaemonLog) service.isolate(getLastExecutionLogAction);

      DaemonOperation operation = new DaemonOperation(DaemonOperation.Type.CHECK, carrier);
      Boolean b_running = (Boolean) service.isolate(operation);
      boolean running = b_running == null ? false : b_running.booleanValue();

      return new DaemonDetails(type, startLog.getTimeStamp(),
            lastExecutionLog.getTimeStamp(), running,
            ack, lastExecutionLog.getDaemonExecutionState());
   }

   private static AcknowledgementState acknowledge(ForkingService service, String type)
   {
      int retries = Parameters.instance().getInteger(type + ".AckRetries", 10);
      int wait = Parameters.instance().getInteger(type + ".AckWait", 2);
      for (int i = 0; i < retries; i++)
      {
         DaemonCarrier carrier = new DaemonCarrier(type);
         
         GetDaemonLogAction getStartLogAction = GetDaemonLogAction.getStartLog(carrier);
         DaemonLog startLog = (DaemonLog) service.isolate(getStartLogAction);
         
         AcknowledgementState ack = startLog.getAcknowledgementState();
         if (ack != AcknowledgementState.Requested)
         {
            return ack;
         }
         try
         {
            Thread.sleep(wait * 1000);
         }
         catch (InterruptedException x)
         {
         }
      }
      return AcknowledgementState.Requested;
   }

   private static void checkDaemonName(String type, boolean onlyActiveModel)
   {
      if (!onlyActiveModel)
      {
         DaemonLog log = DaemonLog.find(type, DaemonLog.LAST_EXECUTION,
               SecurityProperties.getPartitionOid());
         if (log != null)
         {
            return;
         }
      }
      IDaemon daemon = DaemonFactory.instance().get(type);
      if (daemon == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.BPMRT_UNKNOWN_DAEMON.raise(type));
      }
   }
}