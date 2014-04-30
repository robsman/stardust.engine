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

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.LogType;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


// @todo (france, ub): make this an appender

/**
 * Wrapper to write a LogEntry to the audit trail.
 */
public class AuditTrailLogger implements Logger
{
   public static final Logger trace = LogManager.getLogger(AuditTrailLogger.class);

   private long user;
   private Object context;
   private LogCode code;
   private LoggingBehaviour loggingBehaviour = LoggingBehaviour.SAME_TRANSACTION;

   public static enum LoggingBehaviour {
      SAME_TRANSACTION,
      SEPARATE_TRANSACTION_SYNCHRONOUS
   }

   public static AuditTrailLogger getInstance(LogCode code, Object context, LoggingBehaviour loggingBehaviour)
   {
      return new AuditTrailLogger(code, context, loggingBehaviour);
   }

   public static AuditTrailLogger getInstance(LogCode code, Object context)
   {
      final Parameters params = Parameters.instance();
      ITransactionStatus txStatus = TransactionUtils.getCurrentTxStatus(params);
      final LoggingBehaviour loggingBehaviour;

      if(txStatus.isRollbackOnly())
      {
         loggingBehaviour = LoggingBehaviour.SEPARATE_TRANSACTION_SYNCHRONOUS;
   }
      else
      {
         loggingBehaviour = LoggingBehaviour.SAME_TRANSACTION;
      }

      return getInstance(code, context, loggingBehaviour);
   }

   public static AuditTrailLogger getInstance(LogCode code)
   {
      return getInstance(code, null);
   }

   private AuditTrailLogger(LogCode code, Object context, LoggingBehaviour loggingBehaviour)
   {
      this.context = context;
      this.user = SecurityProperties.getUserOID();
      this.code = code;
      this.loggingBehaviour = loggingBehaviour;
   }

   private AuditTrailLogger(LogCode code, Object context)
   {
      this.context = context;
      this.user = SecurityProperties.getUserOID();
      this.code = code;
   }

   public void debug(Object o)
   {
      log(LogType.Debug, o, null);
   }

   public void debug(Object o, Throwable throwable)
   {
      log(LogType.Debug, o, throwable);
   }

   public void error(Object o)
   {
      log(LogType.Error, o, null);
   }

   public void error(Object o, Throwable throwable)
   {
      log(LogType.Error, o, throwable);
   }

   public void fatal(Object o)
   {
      log(LogType.Fatal, o, null);
   }

   public void fatal(Object o, Throwable throwable)
   {
      log(LogType.Fatal, o, throwable);
   }

   public void info(Object o)
   {
      log(LogType.Info, o, null);
   }

   public void info(Object o, Throwable throwable)
   {
      log(LogType.Info, o, throwable);
   }

   public void warn(Object o)
   {
      log(LogType.Warn, o, null);
   }

   public void warn(Object o, Throwable throwable)
   {
      log(LogType.Warn, o, throwable);
   }

   public boolean isInfoEnabled()
   {
      return true;
   }

   public boolean isDebugEnabled()
   {
      return false;
   }

   private void log(LogType severity, Object o, Throwable throwable)
   {
      String message = (o == null ? "" : o.toString());
      message = (throwable == null ? message : message + ": " + throwable.getMessage());
      long activityInstance =
            (context instanceof IActivityInstance ? ((IActivityInstance) context).getOID() : 0);
      long processInstance =
            (context instanceof IProcessInstance ? ((IProcessInstance) context).getOID() : 0);
      if ( !Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         boolean isTransientExecutionScenario = isTransientExecutionScenario(context);
         if ( !isTransientExecutionScenario)
         {
            short partitionOid = SecurityProperties.getPartitionOid();
            logToAuditTrailDataBase(severity, message, processInstance, activityInstance, partitionOid);
         }
      }
      message = message + " (" + SecurityProperties.getUser() + ")";
      if (throwable != null)
      {
         switch (severity.getValue())
         {
            case LogType.DEBUG:
               trace.debug(message, throwable);
               break;
            case LogType.INFO:
               trace.info(message, throwable);
               break;
            case LogType.WARN:
               trace.warn(message, throwable);
               break;
            case LogType.ERROR:
               trace.error(message, throwable);
               break;
            case LogType.FATAL:
               trace.fatal(message, throwable);
               break;
            default:
               trace.warn(message, throwable);
               break;
         }
      }
      else
      {
         switch (severity.getValue())
         {
            case LogType.DEBUG:
               trace.debug(message);
               break;
            case LogType.INFO:
               trace.info(message);
               break;
            case LogType.WARN:
               trace.warn(message);
               break;
            case LogType.ERROR:
               trace.error(message);
               break;
            case LogType.FATAL:
               trace.fatal(message);
               break;
            default:
               trace.warn(message);
               break;
         }
      }
   }

   private boolean isTransientExecutionScenario(final Object context)
   {
      if (context instanceof IActivityInstance)
      {
         return ActivityInstanceUtils.isTransientExecutionScenario((IActivityInstance) context);
      }
      if (context instanceof IProcessInstance)
      {
         return ProcessInstanceUtils.isTransientExecutionScenario((IProcessInstance) context);
      }

      return false;
   }

   private void logToAuditTrailDataBase(final LogType severity,
                                        final String message,
                                        final long processInstance,
                                        final long activityInstance,
                                        final short partitionOid)
   {
      switch(loggingBehaviour)
      {
         case SAME_TRANSACTION:
            new LogEntryBean(severity.getValue(), code.getValue(), message, processInstance,
                  activityInstance, user, partitionOid);
            break;
         case SEPARATE_TRANSACTION_SYNCHRONOUS:
            final Parameters params = Parameters.instance();
            ForkingServiceFactory fsf = (ForkingServiceFactory) params.get(EngineProperties.FORKING_SERVICE_HOME);
            ForkingService fs = fsf.get();
            try
            {
               fs.isolate(new Action<Object>()
               {
                  public Object execute()
                  {
                     new LogEntryBean(severity.getValue(), code.getValue(), message, processInstance,
                           activityInstance, user, partitionOid);
                     return null;
                  }
               });
            }
            finally
            {
               fsf.release(fs);
            }
            break;
         default:
            break;
      }
   }
}
