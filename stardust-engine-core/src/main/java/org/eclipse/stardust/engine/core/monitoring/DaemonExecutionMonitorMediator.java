/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.monitoring;

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.core.spi.monitoring.IDaemonExecutionMonitor;

/**
 * 
 * @author thomas.wolfram
 *
 */
public class DaemonExecutionMonitorMediator implements IDaemonExecutionMonitor
{

   private static final String FAILED_BROADCASTING_DAEMON_EXECUTION_MONITOR_EVENT = "Failed broadcasting daemon execution monitor event.";

   private static final Logger trace = LogManager.getLogger(DaemonExecutionMonitorMediator.class);

   private final List<IDaemonExecutionMonitor> monitors;

   public DaemonExecutionMonitorMediator(List<IDaemonExecutionMonitor> monitors)
   {
      this.monitors = monitors;
   }

   @Override
   public void beforeExecute(Daemon daemon)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IDaemonExecutionMonitor daemonMonitor = monitors.get(i);
         try
         {
            daemonMonitor.beforeExecute(daemon);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_DAEMON_EXECUTION_MONITOR_EVENT, e);
         }
      }
   }

   @Override
   public void afterExecute(Daemon daemon)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IDaemonExecutionMonitor daemonMonitor = monitors.get(i);
         try
         {
            daemonMonitor.afterExecute(daemon);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_DAEMON_EXECUTION_MONITOR_EVENT, e);
         }
      }

   }

}
