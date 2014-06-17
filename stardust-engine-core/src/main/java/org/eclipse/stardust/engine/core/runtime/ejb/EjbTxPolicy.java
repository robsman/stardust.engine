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
package org.eclipse.stardust.engine.core.runtime.ejb;

import java.io.Serializable;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author rsauer
 * @version $Revision$
 */
public class EjbTxPolicy implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final String serviceName;

   public EjbTxPolicy(String serviceName)
   {
      this.serviceName = serviceName;
   }

   public boolean mustRollback(MethodInvocation invocation, Throwable Exception)
   {
      final String rollbackOnError = Parameters.instance().getString(
            KernelTweakingProperties.EJB_ROLLBACK_ON_ERROR,
            KernelTweakingProperties.EJB_ROLLBACK_ON_ERROR_ALWAYS);

      boolean forceRollback;

      if (KernelTweakingProperties.EJB_ROLLBACK_ON_ERROR_NEVER.equals(rollbackOnError))
      {
         forceRollback = false;
      }
      else if (KernelTweakingProperties.EJB_ROLLBACK_ON_ERROR_LENIENT.equals(rollbackOnError))
      {
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
      }
      else
      {
         forceRollback = true;
      }

      return forceRollback;
   }
}
