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
import java.util.TimerTask;

import javax.annotation.PreDestroy;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonCarrier;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class DefaultScheduler implements DaemonScheduler
{
   private static final Logger trace = LogManager.getLogger(DefaultScheduler.class);

   private static final Map<DaemonCarrier, Timer> timers = CollectionUtils.newMap();
   
   private volatile boolean stopScheduler = false;
   
   public void start(DaemonCarrier carrier, long period, final Runnable runnable)
   {
      synchronized (timers)
      {
         if (!timers.containsKey(carrier) && !stopScheduler)
         {
            Timer timer = new Timer(carrier.getType(), true);
            TimerTask task = new TimerTask()
            {
               private Long lastRun;
               
               @Override
               public void run()
               {
                  // prevent firing of batch events if the system time is changed on a running engine.
                  long now = TimestampProviderUtils.getTimeStampValue();
                  if (!stopScheduler &&
                        (lastRun == null || now - lastRun > 100 || now < lastRun))
                  {
                     runnable.run();
                     lastRun = now;
                  }
               }
            };
            timer.scheduleAtFixedRate(task, 0, period);
            timers.put(carrier, timer);
            trace.info("Timer '" + carrier.getType() + "' started.");
         }
      }
   }

   public void stop(DaemonCarrier carrier)
   {
      synchronized (timers)
      {
         Timer timer = timers.get(carrier);
         if (timer != null)
         {
            timer.cancel();
            timers.remove(carrier);
            trace.info("Timer '" + carrier.getType() + "' was stopped.");
         }
      }
   }

   public boolean isScheduled(DaemonCarrier carrier)
   {
      synchronized (timers)
      {
         return timers.containsKey(carrier);
      }
   }
   
   @PreDestroy
   public void shutdownScheduler()
   {
      stopScheduler = true;
      synchronized (timers)
      {
         trace.info("Timers will be stopped now because scheduler is shutting down.");
         for(DaemonCarrier carrier : timers.keySet())
         {
            stop(carrier);
         }
         assert timers.isEmpty();
      }
   }
}
