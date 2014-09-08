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
package org.eclipse.stardust.engine.core.runtime;

import java.io.Serializable;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;


/**
 * @author rsauer
 * @version $Revision$
 */
public class TxRollbackPolicy implements Serializable
{
   protected static Mode fromConfig(String modeName)
   {
      if (KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_NEVER.equalsIgnoreCase(modeName))
      {
         return Mode.Never;
      }
      else if (KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_LENIENT.equalsIgnoreCase(modeName))
      {
         return Mode.Lenient;
      }
      else if (KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_ALWAYS.equalsIgnoreCase(modeName))
      {
         return Mode.Always;
      }
      else
      {
         RuntimeLog.CONFIGURATION.warn("Unsupported TX rollback mode: " + modeName
               + " (falling back to "
               + KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_ALWAYS + ").");

         return Mode.Always;
      }
   }

   public static enum Mode {
      Always, Lenient, Never
   };

   private static final long serialVersionUID = 1L;

   private final String serviceName;

   public TxRollbackPolicy(String serviceName)
   {
      this.serviceName = serviceName;
   }

   protected Mode determineRollbackOnErrorMode(MethodInvocation invocation)
   {
      Parameters params = invocation.getParameters();

      return fromConfig(params.getString(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR,
            KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_ALWAYS));
   }

   public boolean mustRollback(MethodInvocation invocation, Throwable Exception)
   {
      boolean forceRollback;

      switch (determineRollbackOnErrorMode(invocation))
      {
         case Never:
            forceRollback = false;
            break;

         case Lenient:
            forceRollback = mustRollbackIfLenient(invocation);
            break;

         case Always:
            forceRollback = true;
            break;

         default:
            forceRollback = true;
      }

      return forceRollback;
   }

   private boolean mustRollbackIfLenient(MethodInvocation invocation)
   {
      boolean forceRollback;

      if (AdministrationService.class.getName().equals(serviceName))
      {
         if ("getDaemon".equals(invocation.getMethod().getName())
               || "getAllDaemons".equals(invocation.getMethod().getName())
               || "startDaemon".equals(invocation.getMethod().getName())
               || "stopDaemon".equals(invocation.getMethod().getName())
               || "getAuditTrailHealthReport".equals(invocation.getMethod().getName())
               || "flushCaches".equals(invocation.getMethod().getName())
               || "getUser".equals(invocation.getMethod().getName()))
         {
            forceRollback = false;
         }
         else
         {
            forceRollback = true;
         }
      }
      else if (WorkflowService.class.getName().equals(serviceName))
      {
         if ("complete".equals(invocation.getMethod().getName())
               || "activateAndComplete".equals(invocation.getMethod().getName())
               || "startProcess".equals(invocation.getMethod().getName())
               || "abortProcessInstance".equals(invocation.getMethod().getName())
               || "abortActivityInstance".equals(invocation.getMethod().getName()))
         {
            forceRollback = true;
         }
         else
         {
            forceRollback = false;
         }
      }
      else
      {
         forceRollback = false;
      }

      return forceRollback;
   }
}
