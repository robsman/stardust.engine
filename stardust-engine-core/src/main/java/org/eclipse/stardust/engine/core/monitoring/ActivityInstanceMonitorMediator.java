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
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IActivityInstanceMonitor;


/**
 * 
 * @author thomas.wolfram
 * 
 */
public class ActivityInstanceMonitorMediator implements IActivityInstanceMonitor
{

   private static final Logger trace = LogManager.getLogger(ProcessExecutionMonitorMediator.class);

   private final List<IActivityInstanceMonitor> monitors;

   public ActivityInstanceMonitorMediator(List<IActivityInstanceMonitor> monitors)
   {
      this.monitors = monitors;
   }

   public void activityInstanceStateChanged(IActivityInstance activity, int newState)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IActivityInstanceMonitor monitor = monitors.get(i);
         try
         {
            monitor.activityInstanceStateChanged(activity, newState);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting activity instance monitor event.", e);
         }
      }
   }

}
