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
package org.eclipse.stardust.engine.api.spring;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.IActionCarrier;
import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.spring.threading.IJobManager;
import org.eclipse.stardust.engine.spring.threading.Job;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class QueuedSpringForkingService extends AbstractSpringForkingServiceBean
{
   private static final Logger trace = LogManager.getLogger(QueuedSpringForkingService.class);

   private IJobManager jobManager;

   public IJobManager getJobManager()
   {
      return jobManager;
   }

   public void setJobManager(IJobManager jobManager)
   {
      this.jobManager = jobManager;
   }

   public void fork(IActionCarrier order, boolean transacted)
   {
      ForkingServiceFactory serviceFactory = Parameters.instance().getObject(EngineProperties.FORKING_SERVICE_HOME);
      if (serviceFactory == null)
      {
         serviceFactory = new ForkingServiceSpringBeanFactory(J2eeContainerType.POJO, getBeanFactory());
      }

      ForkedActionRunner runnable = new ForkedActionRunner(order.createAction(),
            serviceFactory);

      if (transacted)
      {
         TxAwareJobScheduler deferredActionsManager = new TxAwareJobScheduler();
         deferredActionsManager.scheduleRunner(runnable);

         TransactionSynchronizationManager.registerSynchronization(deferredActionsManager);
      }
      else
      {
         // immediately acknowledge TX, so forked thread starts in parallel to pending TX
         getJobManager().scheduleJob(new Job(runnable));
      }
   }

   private class ForkedActionRunner implements Runnable
   {
      private final Action action;

      private final ForkingServiceFactory serviceFactory;

      public ForkedActionRunner(Action action, ForkingServiceFactory serviceFactory)
      {
         this.action = action;
         this.serviceFactory = serviceFactory;
      }

      public void run()
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Performing forked action " + action + " after commit.");
         }

         // place retry policy around action invocation
         ActionRunner actionInvoker = (ActionRunner) Proxy.newProxyInstance(
               ActionRunner.class.getClassLoader(), new Class[]
               {
                  ActionRunner.class
               },//
               new ForkedActionInvocationManager(//
                     new ForkedActionInvoker(serviceFactory)));

         try
         {
            actionInvoker.execute(action);
         }
         catch (Throwable e)
         {
            // No way to handle it besides logging

            trace.warn("Oops .. execptionally terminating managed runnable.", e);
         }
      }
      
      @Override
      public String toString()
      {
         final StringBuilder sb = new StringBuilder();
         
         sb.append("ForkedActionRunner {");
         sb.append("action = ").append(action);
         sb.append("}");
         
         return sb.toString();
      }
   }

   private class TxAwareJobScheduler extends TransactionSynchronizationAdapter
   {
      private List/* <ForkedActionRunner> */scheduledRunners = new ArrayList(5);

      public void scheduleRunner(ForkedActionRunner runner)
      {
         scheduledRunners.add(runner);
      }

      public void afterCompletion(int status)
      {
         for (Iterator i = scheduledRunners.iterator(); i.hasNext();)
         {
            ForkedActionRunner runner = (ForkedActionRunner) i.next();

            try
            {
               if (TransactionSynchronization.STATUS_COMMITTED == status)
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Scheduling forked action " + runner.action
                           + " after commit.");
                  }

                  getJobManager().scheduleJob(new Job(runner));
               }
               else
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Forked action " + runner.action + " was rolled back.");
                  }
               }
            }
            catch (Throwable t)
            {
               trace.warn("Failed scheduling forked action " + runner.action + ".", t);
            }
         }
      }
   }
}
