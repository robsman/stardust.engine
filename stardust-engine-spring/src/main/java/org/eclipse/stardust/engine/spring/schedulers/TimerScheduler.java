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
package org.eclipse.stardust.engine.spring.schedulers;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonCarrier;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;

/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class TimerScheduler implements DaemonScheduler
{
   private static final Logger trace = LogManager.getLogger(TimerScheduler.class);

   private ScheduledExecutorService service;

   private static final Map<DaemonCarrier, ScheduledFuture> tasks = CollectionUtils.newMap(); 

   public ScheduledExecutorService getService()
   {
      return service;
   }

   public void setService(ScheduledExecutorService service)
   {
      this.service = service;
   }   

   public void start(DaemonCarrier carrier, long period, Runnable runnable)
   {
      synchronized (tasks)
      {
         if (!tasks.containsKey(carrier))
         {
            ScheduledExecutorTask task = new ScheduledExecutorTask(runnable, 0, period, true);
            ScheduledFuture< ? > scheduleAtFixedRate = service.scheduleAtFixedRate(task.getRunnable(), task.getDelay(), task.getPeriod(), task.getTimeUnit());
            tasks.put(carrier, scheduleAtFixedRate);
            trace.info("Task '" + carrier.getType() + "' was scheduled.");
         }
      }
   }

   public void stop(DaemonCarrier carrier)
   {
      synchronized (tasks)
      {
         ScheduledFuture scheduledFuture = tasks.get(carrier);
         if (scheduledFuture != null)
         {
            scheduledFuture.cancel(true);
            tasks.remove(carrier);
            trace.info("Task '" + carrier.getType() + "' was cancelled.");
         }
      }
   }

   public boolean isScheduled(DaemonCarrier carrier)
   {
      synchronized (tasks)
      {
         return tasks.containsKey(carrier);
      }
   }
}