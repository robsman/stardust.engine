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
import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.IActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SimpleSpringForkingService extends AbstractSpringForkingServiceBean
{
   private static final Logger trace = LogManager.getLogger(SimpleSpringForkingService.class);

   private DeferredActionsManager deferredActionsManager;

   public void fork(IActionCarrier order, boolean transacted)
   {
      final ForkingServiceFactory serviceFactory = (ForkingServiceFactory) Parameters.instance()
            .get(EngineProperties.FORKING_SERVICE_HOME);

      ForkedActionRunner runnable = new ForkedActionRunner(order.createAction(),
            serviceFactory);

      // TODO consider thread pooling
      Thread forkedThread = new Thread(runnable);
      // start thread immediately to reduce likelyhood of errors after commit
      forkedThread.start();

      if (transacted)
      {
         if (null == deferredActionsManager)
         {
            this.deferredActionsManager = new DeferredActionsManager();
            TransactionSynchronizationManager.registerSynchronization(deferredActionsManager);
         }
         deferredActionsManager.scheduleAction(runnable, forkedThread);
      }
      else
      {
         // immediately accknowledge TX, so forked thread starts in parallel to pending TX
         runnable.setTxStatus(TransactionSynchronization.STATUS_COMMITTED);
      }
   }

   public class ForkedActionRunner implements Runnable
   {
      private final Action action;

      private final ForkingServiceFactory serviceFactory;

      private boolean wasTxSynchronized;

      private int txStatus;

      public ForkedActionRunner(Action action, ForkingServiceFactory serviceFactory)
      {
         this.wasTxSynchronized = false;

         this.action = action;
         this.serviceFactory = serviceFactory;
      }

      public synchronized void setTxStatus(int status)
      {
         Assert.condition( !wasTxSynchronized,
               "TX outcome must be synchronized only once.");

         this.txStatus = status;
         this.wasTxSynchronized = true;

         // notifying myself (see run method)
         notify();
      }

      public void run()
      {
         // wait for explicit go from parent thread
         synchronized (this)
         {
            while ( !wasTxSynchronized)
            {
               try
               {
                  wait();
               }
               catch (InterruptedException e)
               {
                  // ignore, as condition will be retested
               }
            }
         }

         if (TransactionSynchronization.STATUS_COMMITTED == txStatus)
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
            finally
            {
               
            }
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Forked action " + action + " was rolled back.");
            }
         }
      }
   }

   private class DeferredActionsManager extends TransactionSynchronizationAdapter
   {
      private List/* <Pair<ForkedActionRunner, Thread>> */deferredThreads = new ArrayList(5);

      public void scheduleAction(ForkedActionRunner runner, Thread thread)
      {
         deferredThreads.add(new Pair(runner, thread));
      }

      public void afterCompletion(int status)
      {
         for (Iterator i = deferredThreads.iterator(); i.hasNext();)
         {
            Pair forkedRunner = (Pair) i.next();

            ForkedActionRunner runner = (ForkedActionRunner) forkedRunner.getFirst();
            runner.setTxStatus(status);
         }
      }
   }
}
