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
package org.eclipse.stardust.engine.core.monitoring;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.core.spi.monitoring.IActivityInstanceMonitor;
import org.eclipse.stardust.engine.core.spi.monitoring.IPartitionMonitor;
import org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor;
import org.eclipse.stardust.engine.core.spi.monitoring.IRuntimeEnvironmentMonitor;
import org.eclipse.stardust.engine.core.spi.monitoring.IWorklistMonitor;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListener;


/**
 * @author sauer
 * @version $Revision: $
 */
public class MonitoringUtils
{

   private static final String KEY_RT_ENV_MONITOR_MEDIATOR = MonitoringUtils.class.getName()
         + ".RuntimeEnvironmentMonitorMediator";

   private static final String KEY_PARTITON_MONITOR_MEDIATOR = MonitoringUtils.class.getName()
         + ".PartitionMonitorMediator";

   private static final String KEY_PROCESS_EXECUTION_MONITOR_MEDIATOR = MonitoringUtils.class.getName()
         + ".ProcessExecutionMonitorMediator";

   private static final String KEY_WORKLIST_MONITOR_MEDIATOR = MonitoringUtils.class.getName()
         + ".WorklistMonitorMediator";

   private static final String KEY_ACTIVITY_INSTANCE_MONITOR_MEDIATOR = MonitoringUtils.class.getName()
         + ".ActivityInstanceMonitorMediator";



   public static IRuntimeEnvironmentMonitor runtimeEnvironmentMonitors()
   {
      GlobalParameters globals = GlobalParameters.globals();

      IRuntimeEnvironmentMonitor mediator = (IRuntimeEnvironmentMonitor) globals.get(KEY_RT_ENV_MONITOR_MEDIATOR);

      if (null == mediator)
      {
         mediator = new RuntimeEnvironmentMonitorMediator(
               ExtensionProviderUtils.getExtensionProviders(IRuntimeEnvironmentMonitor.class));
         globals.set(KEY_RT_ENV_MONITOR_MEDIATOR, mediator);
      }

      return mediator;
   }

   public static IPartitionMonitor partitionMonitors()
   {
      GlobalParameters globals = GlobalParameters.globals();

      IPartitionMonitor mediator = (IPartitionMonitor) globals.get(KEY_PARTITON_MONITOR_MEDIATOR);

      if (null == mediator)
      {
         mediator = new PartitionMonitorMediator(
               ExtensionProviderUtils.getExtensionProviders(IPartitionMonitor.class));
         globals.set(KEY_PARTITON_MONITOR_MEDIATOR, mediator);
      }

      return mediator;
   }

   public static IProcessExecutionMonitor processExecutionMonitors()
   {
      GlobalParameters globals = GlobalParameters.globals();

      IProcessExecutionMonitor mediator = (IProcessExecutionMonitor) globals.get(KEY_PROCESS_EXECUTION_MONITOR_MEDIATOR);

      if (null == mediator)
      {
         mediator = new ProcessExecutionMonitorMediator(
               ExtensionProviderUtils.getExtensionProviders(IProcessExecutionMonitor.class));
         globals.set(KEY_PROCESS_EXECUTION_MONITOR_MEDIATOR, mediator);
      }

      return mediator;
   }

   public static boolean hasWorklistMonitors()
   {
      IWorklistMonitor mediator = worklistMonitors();

      return (null != mediator) && ((WorklistMonitorMediator) mediator).hasMonitors();
   }

   public static IWorklistMonitor worklistMonitors()
   {
      GlobalParameters globals = GlobalParameters.globals();

      WorklistMonitorMediator mediator = (WorklistMonitorMediator) globals.get(KEY_WORKLIST_MONITOR_MEDIATOR);
      if (null == mediator)
      {
         mediator = new WorklistMonitorMediator(
               ExtensionProviderUtils.getExtensionProviders(IWorklistMonitor.class));
         globals.set(KEY_WORKLIST_MONITOR_MEDIATOR, mediator);
      }

      return mediator;
   }

   public static IActivityInstanceMonitor activityInstanceMonitors()
   {
      GlobalParameters globals = GlobalParameters.globals();

      IActivityInstanceMonitor mediator = (IActivityInstanceMonitor) globals.get(KEY_ACTIVITY_INSTANCE_MONITOR_MEDIATOR);
      if (null == mediator)
      {
         mediator = new ActivityInstanceMonitorMediator(
               ExtensionProviderUtils.getExtensionProviders(IActivityInstanceMonitor.class));
         globals.set(KEY_ACTIVITY_INSTANCE_MONITOR_MEDIATOR, mediator);
      }

      return mediator;
   }


}
