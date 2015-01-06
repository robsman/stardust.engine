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
package org.eclipse.stardust.engine.spring.threading;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;


public class FiFoJobManager implements IJobManager, ApplicationListener, InitializingBean
{
   private static final Logger trace = LogManager.getLogger(FiFoJobManager.class);

   private static final Job SHUTDOWN_REQUEST = new Job(null);

   private int maxParallelJobs = 10;
   
   private boolean useShutdownHook = false;
   
   private Thread dispatcherThread;

   private final JobDispatcher dispatcher;

   private List scheduledJobs = new LinkedList();

   private List activeJobs = new LinkedList();
   
   private Thread shutdownHook;
   
   private String threadSuffix = "Thread";

   private final AtomicLong threadSeqNumber = new AtomicLong();

   private long nextThreadID()
   {
      return threadSeqNumber.incrementAndGet();
   }
   
   public FiFoJobManager()
   {
      this.dispatcher = new JobDispatcher();
   }

   public void onApplicationEvent(ApplicationEvent event)
   {
      if (event instanceof ContextClosedEvent)
      {
         trace.info("Application context was closed, scheduling shutdown ...");
         shutdown();
      }
   }

   public void afterPropertiesSet() throws Exception
   {
      try
      {
         this.dispatcherThread = new Thread(dispatcher, getThreadSuffix() + nextThreadID());
         dispatcherThread.setDaemon(true);
         trace.warn("This configuration is not supported for productive use and a XA enabled messaging configuration should be used instead.");
         trace.info("Starting dispatcher thread for " + getThreadSuffix() + "...");
         dispatcherThread.start();
      }
      catch (Throwable t)
      {
         throw new PublicException("Failed initilizing FiFo-Job Manager.", t);
      }
      
      if (useShutdownHook)
      {
         this.shutdownHook = new Thread()
         {
            public void run()
            {
               trace.info("JVM is being terminated, scheduling shutdown ...");

               // preventing deregistration of shutdown hook
               shutdownHook = null;

               shutdown();
            }
         };
         Runtime.getRuntime().addShutdownHook(shutdownHook);
      }
   }

   public int getMaxParallelJobs()
   {
      return maxParallelJobs;
   }

   public void setMaxParallelJobs(int maxParallelJobs)
   {
      this.maxParallelJobs = maxParallelJobs;
   }

   public void setUseShutdownHook(boolean useShutdownHook)
   {
      this.useShutdownHook = useShutdownHook;
   }

   public void scheduleJob(Job job)
   {
      synchronized (dispatcher)
      {
         if ( !scheduledJobs.contains(SHUTDOWN_REQUEST) && !scheduledJobs.contains(job))
         {
            if ((null == dispatcherThread) || !dispatcherThread.isAlive())
            {
               this.dispatcherThread = new Thread(dispatcher, getThreadSuffix() + nextThreadID());
               trace.info("Starting new dispatcher thread for " + getThreadSuffix()
                     + "...");
               dispatcherThread.start();
            }
            
            scheduledJobs.add(job);
            if (trace.isInfoEnabled())
            {
               trace.info("Enqueued job " + job + ". Scheduled queue length is now "
                     + scheduledJobs.size());
            }
            dispatcher.notify();
         }
      }
   }
   
   public void shutdown()
   {
      scheduleJob(SHUTDOWN_REQUEST);
   }

   private void notifyJobCompleted(Job job, Throwable error)
   {
      synchronized (dispatcher)
      {
         synchronized (activeJobs)
         {
            if (activeJobs.contains(job))
            {
               job.done = true;
               
               if (null != error)
               {
                  job.error = error;
               }
               
               activeJobs.remove(job);
               if (trace.isDebugEnabled())
               {
                  trace.debug("Finished job " + job
                        + ". Active jobs queue length is now " + activeJobs.size());
               }
            }
         }

         dispatcher.notify();
      }
   }

   private class JobDispatcher implements Runnable
   {
      public void run()
      {
         trace.info("Dispatcher thread was started for "+getThreadSuffix()+"...");

         Job nextJob = null;
         do
         {
            synchronized (this)
            {
               while (scheduledJobs.isEmpty() || (getMaxParallelJobs() <= activeJobs.size()))
               {
                  try
                  {
                     this.wait();
                  }
                  catch (InterruptedException e)
                  {
                     // ignore
                  }
               }
   
               nextJob = (Job) scheduledJobs.remove(0);
               if (trace.isDebugEnabled())
               {
                  trace.debug("Dequened next job " + nextJob
                        + ". Scheduled queue length is now " + scheduledJobs.size());
               }
            }
   
            if (SHUTDOWN_REQUEST != nextJob)
            {
               try
               {
                  Thread jobThread = new Thread(new JobRunner(nextJob), getThreadSuffix() + nextThreadID());
                  synchronized (activeJobs)
                  {
                     activeJobs.add(nextJob);
                     if (trace.isInfoEnabled())
                     {
                        trace.info("Adding job " + nextJob
                              + ". Active jobs queue length is now " + activeJobs.size());
                     }
                  }
                  jobThread.start();
               }
               catch (Throwable t)
               {
                  notifyJobCompleted(nextJob, t);
               }
               finally
               {
                  nextJob = null;
               }
            }
         }
         while (SHUTDOWN_REQUEST != nextJob);
         
         trace.info("Dispatcher thread is shutting down ...");
         
         if (null != shutdownHook)
         {
            try
            {
               Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            catch (IllegalStateException e)
            {
               // ignore
            }
         }
      }
   }

   private class JobRunner implements Runnable
   {
      private final Job job;

      public JobRunner(Job job)
      {
         this.job = job;
      }

      public void run()
      {
         try
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Executing job " + job);
            }
            job.runnable.run();

            notifyJobCompleted(job, null);
         }
         catch (Throwable t)
         {
            notifyJobCompleted(job, t);
         }

         // terminate thread
      }
   }

   public String getThreadSuffix()
   {
      return threadSuffix;
   }

   public void setThreadSuffix(String threadSuffix)
   {
      this.threadSuffix = threadSuffix;
   }

}
