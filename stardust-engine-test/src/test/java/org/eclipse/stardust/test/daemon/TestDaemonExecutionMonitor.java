package org.eclipse.stardust.test.daemon;

import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.core.spi.monitoring.IDaemonExecutionMonitor;

public class TestDaemonExecutionMonitor implements IDaemonExecutionMonitor
{

   @Override
   public void beforeExecute(Daemon daemon)
   {

      TestDaemonExecutionMonitorLog.getInstance().addLogEntry("beforeExecute",
            "Callback BEFORE Daemon <" + daemon.getType() + "> has been executed");

   }

   @Override
   public void afterExecute(Daemon daemon)
   {
      TestDaemonExecutionMonitorLog.getInstance().addLogEntry("afterExecute",
            "Callback AFTER Daemon <" + daemon.getType() + "> has been executed");

   }

}
