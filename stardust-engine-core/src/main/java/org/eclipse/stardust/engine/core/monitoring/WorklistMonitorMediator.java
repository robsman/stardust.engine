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

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IWorklistMonitor;


public class WorklistMonitorMediator implements IWorklistMonitor
{
   private static final Logger trace = LogManager.getLogger(WorklistMonitorMediator.class);

   private final List<IWorklistMonitor> monitors;
   
   public WorklistMonitorMediator(List<IWorklistMonitor> monitors)
   {
      this.monitors = monitors;
   }
   
   public boolean hasMonitors()
   {
      return !isEmpty(monitors);
   }

   public void addedToWorklist(IParticipant participant,
         IActivityInstance activityInstance)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IWorklistMonitor monitor = monitors.get(i);
         try
         {
            monitor.addedToWorklist(participant, activityInstance);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting worklist monitor event.", e);
         }
      }
   }

   public void removedFromWorklist(IParticipant participant,
         IActivityInstance activityInstance)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IWorklistMonitor monitor = monitors.get(i);
         try
         {
            monitor.removedFromWorklist(participant, activityInstance);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting worklist monitor event.", e);
         }
      }
   }

}
