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

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.spi.monitoring.IRuntimeEnvironmentMonitor;


/**
 * @author sauer
 * @version $Revision: $
 */
public class RuntimeEnvironmentMonitorMediator implements IRuntimeEnvironmentMonitor
{
   private static final Logger trace = LogManager.getLogger(RuntimeEnvironmentMonitorMediator.class);

   private final List<IRuntimeEnvironmentMonitor> monitors;
   
   public RuntimeEnvironmentMonitorMediator(List<IRuntimeEnvironmentMonitor> monitors)
   {
      this.monitors = monitors;
   }

   public void partitionCreated(IAuditTrailPartition partition)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IRuntimeEnvironmentMonitor monitor = monitors.get(i);
         try
         {
            monitor.partitionCreated(partition);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting runtime environment monitor event.", e);
         }
      }
   }

   public void partitionDropped(IAuditTrailPartition partition)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IRuntimeEnvironmentMonitor monitor = monitors.get(i);
         try
         {
            monitor.partitionDropped(partition);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting runtime environment monitor event.", e);
         }
      }
   }

}
