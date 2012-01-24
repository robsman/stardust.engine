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
import java.util.Timer;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonCarrier;
import org.springframework.scheduling.timer.ScheduledTimerTask;
import org.springframework.scheduling.timer.TimerFactoryBean;


/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class TimerScheduler implements DaemonScheduler
{
   private static final Logger trace = LogManager.getLogger(TimerScheduler.class);

   private TimerFactoryBean factory;

   private static final Map<DaemonCarrier, ScheduledTimerTask> tasks = CollectionUtils.newMap(); 

   public TimerFactoryBean getFactory()
   {
      return factory;
   }

   public void setFactory(TimerFactoryBean factory)
   {
      this.factory = factory;
   }

   public void start(DaemonCarrier carrier, long period, Runnable runnable)
   {
      synchronized (tasks)
      {
         if (!tasks.containsKey(carrier))
         {
            ScheduledTimerTask task = new ScheduledTimerTask(runnable, 0, period, true);
            tasks.put(carrier, task);
            Timer timer = (Timer) factory.getObject();
            timer.scheduleAtFixedRate(task.getTimerTask(), task.getDelay(), task.getPeriod());
            trace.info("Task '" + carrier.getType() + "' was scheduled.");
         }
      }
   }

   public void stop(DaemonCarrier carrier)
   {
      synchronized (tasks)
      {
         ScheduledTimerTask task = tasks.get(carrier);
         if (task != null)
         {
            task.getTimerTask().cancel();
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
