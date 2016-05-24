/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

public abstract class ProcessHierarchyStateChangeJanitor extends SecurityContextAwareAction
{
   public static final Logger trace = LogManager.getLogger(ProcessHierarchyStateChangeJanitor.class);

   protected long processInstanceOid;
   protected long executingUserOid;
   protected int triesLeft;

   private ProcessInstanceLocking piLock = new ProcessInstanceLocking();

   protected abstract HierarchyStateChangeJanitorCarrier getNewCarrier();

   protected abstract boolean preventFinalState();

   protected abstract long getRetryPause();

   protected abstract void processPi(ProcessInstanceBean pi);

   protected abstract void postProcessPi(ProcessInstanceBean pi);

   public ProcessHierarchyStateChangeJanitor(HierarchyStateChangeJanitorCarrier carrier)
   {
      super(carrier);
      this.processInstanceOid = carrier.getProcessInstanceOid();
      this.executingUserOid = carrier.getUserOid();
      this.triesLeft = carrier.getTriesLeft();
   }

   public static void scheduleJanitor(HierarchyStateChangeJanitorCarrier carrier)
   {
      scheduleJanitor(carrier, true);
   }

   public static void scheduleJanitor(HierarchyStateChangeJanitorCarrier carrier, boolean transacted)
   {
      ProcessStopJanitorMonitor monitor = ProcessStopJanitorMonitor.getInstance();
      // only one abortion thread allowed per process instance
      if (monitor.register(carrier.getProcessInstanceOid()))
      {
         ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance()
               .get(EngineProperties.FORKING_SERVICE_HOME);
         ForkingService service = null;
         try
         {
            service = factory.get();
            service.fork(carrier, transacted);
         }
         finally
         {
            factory.release(service);
         }
      }

   }

   public Object execute()
   {
      triesLeft -= 1;
      boolean performed = false;
      ProcessInstanceBean pi = null;
      Exception exception = null;

      try
      {
         if (preventFinalState())
         {
            triesLeft = 0;
            performed = true;
            return Boolean.TRUE;
         }

         pi = ProcessInstanceBean.findByOID(processInstanceOid);
         if ( !pi.isTerminated())
         {
            Collection<IProcessInstance> pis = piLock.lockAllTransitions(pi);

            processAllProcessInstances(pis);
            postProcessPi(pi);
         }

         performed = true;
      }
      catch (ConcurrencyException e)
      {
         trace.warn(MessageFormat.format("Cannot run " + this.getClass().getSimpleName()
               + " for {0} due to a locking conflict", new Object[] {pi}));
         exception = e;
      }
      // catch exception and dont let the MultipleTryInterceptor retry
      catch (Exception e)
      {
         trace.warn("Cannot run " + this.getClass().getSimpleName()
               + " due to a unexpected exception", e);
         exception = e;
      }
      finally
      {
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

         ProcessStopJanitorMonitor monitor = ProcessStopJanitorMonitor.getInstance();
         monitor.unregister(processInstanceOid);

         // if the exception can be handled and tries are left, a new janitor is scheduled
         if (canHandleExceptionOnStop(exception) && triesLeft > 0
               && rtEnv.getExecutionPlan() == null)
         {
            trace.info(MessageFormat.format("Rescheduling " + this.getClass().getSimpleName()
                  + " for {0} Tries left: {1}.",
                  new Object[] {pi, new Integer(triesLeft)}));

            try
            {
               Thread.sleep(getRetryPause());
            }
            catch (InterruptedException x)
            {
            }
            scheduleJanitor(getNewCarrier());
         }
      }

      return performed ? Boolean.TRUE : Boolean.FALSE;
   }

   private boolean canHandleExceptionOnStop(Exception exception)
   {
      return exception instanceof ConcurrencyException;
   }

   private void processAllProcessInstances(Collection<IProcessInstance> pis)
   {
      for (Iterator piIter = pis.iterator(); piIter.hasNext();)
      {
         ProcessInstanceBean pi = (ProcessInstanceBean) piIter.next();

         processPi(pi);
      }
   }

   private static class ProcessStopJanitorMonitor
   {
      private static ProcessStopJanitorMonitor instance = null;

      private HashMap<Long, Boolean> repository = new HashMap<Long, Boolean>();

      public synchronized static ProcessStopJanitorMonitor getInstance()
      {
         if (instance == null)
         {
            instance = new ProcessStopJanitorMonitor();
         }
         return instance;
      }

      public synchronized boolean register(long processInstanceOid)
      {
         if (repository.containsKey(processInstanceOid))
         {
            return false;
         }
         repository.put(processInstanceOid, true);
         return true;
      }

      public synchronized void unregister(long processInstanceOid)
      {
         repository.remove(processInstanceOid);
      }
   }
}