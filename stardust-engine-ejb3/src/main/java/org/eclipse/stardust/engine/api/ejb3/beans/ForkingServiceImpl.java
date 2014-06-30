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
package org.eclipse.stardust.engine.api.ejb3.beans;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.sql.DataSource;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonHandler;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonOperation;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonOperationExecutor;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.ExecutorService;
import org.eclipse.stardust.engine.core.runtime.ejb.RemoteSessionForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * @author ubirkemeyer
 * @version $Revision: 60993 $
 */

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ForkingServiceImpl implements org.eclipse.stardust.engine.core.runtime.ejb.ForkingService,
      TimedObject, DaemonHandler
{
   private static final Logger trace = LogManager.getLogger(ForkingServiceImpl.class);

   private static Map<String, Long> lastRuns = CollectionUtils.newMap();

   @Resource
   protected SessionContext sessionContext;

   @Resource(mappedName = "jdbc/AuditTrail.DataSource")
   protected DataSource dataSource;

   @Resource(mappedName = "jms/CarnotXAConnectionFactory")
   private QueueConnectionFactory queueConnectionFactory;

   @Resource(mappedName = "jms/CarnotSystemQueue")
   private Queue messageQueue;

   @Resource(mappedName = "jms/CarnotDaemonQueue")
   private Queue daemonQueue;

   @Resource(mappedName = "jcr/ContentRepository")
   protected Object repository;

   private ExecuteActionInvocationManager manager;

   private ActionRunner service;

   private ActionRunner serviceInstance;

   public ForkingServiceImpl()
   {
      serviceInstance = new ForkingServiceActionRunner();
   }

   public void ejbTimeout(Timer timer)
   {
      DaemonCarrier carrier = (DaemonCarrier) timer.getInfo();
      try
      {
         runDaemon(carrier);
      }
      catch (Exception ex)
      {
         trace.error("Unable to trigger execution of daemon '" + carrier.getType() + "'.");
      }
   }

   public Object run(Action<?> action) throws WorkflowException
   {
      return run(action, getExecutorService());
   }

   public Object run(Action<?> action, ExecutorService proxyService) throws WorkflowException
   {
      manager = new ExecuteActionInvocationManager(sessionContext, serviceInstance, proxyService);
      service = (ActionRunner) Proxy.newProxyInstance(
            ActionRunner.class.getClassLoader(), new Class[] {ActionRunner.class},
            manager);
      try
      {
         if (action instanceof DaemonOperation)
         {
            action = new DaemonOperationExecutor((DaemonOperation) action, this);
         }
         return service.execute(action);
      }
      catch (PublicException e)
      {
         throw new WorkflowException(e);
      }
   }

   private Timer getTimer(TimerService service, DaemonCarrier carrier)
   {
      @SuppressWarnings("unchecked")
      Collection<Timer> timers = service.getTimers();
      for (Timer timer : timers)
      {
         if (carrier.equals(timer.getInfo()))
         {
            return timer;
         }
      }
      return null;
   }

   public void startTimer(DaemonCarrier carrier)
   {
      DaemonCarrier innerCarrier = carrier.copy();
      TimerService timerService = sessionContext.getTimerService();
      boolean shouldStart = false;
      synchronized (timerService)
      {
         if (getTimer(timerService, innerCarrier) == null)
         {
            TimerService service = sessionContext.getTimerService();
            long periodicity = Parameters.instance().getLong(
                  innerCarrier.getType() + DaemonProperties.DAEMON_PERIODICITY_SUFFIX, 5) * 1000;
            service.createTimer(periodicity, periodicity, innerCarrier);
            shouldStart = true;
            trace.info("Timer '" + innerCarrier.getType() + "' started.");
         }
      }
      if (shouldStart)
      {
         runDaemon(innerCarrier);
      }
   }

   public void stopTimer(DaemonCarrier carrier)
   {
      TimerService timerService = sessionContext.getTimerService();
      synchronized (timerService)
      {
         Timer timer = getTimer(timerService, carrier);
         if (timer != null)
         {
            timer.cancel();
            trace.info("Timer '" + carrier.getType() + "' was stopped.");
         }
      }
   }

   public boolean checkTimer(DaemonCarrier carrier)
   {
      TimerService timerService = sessionContext.getTimerService();
      synchronized (timerService)
      {
         return getTimer(timerService, carrier) != null;
      }
   }

   public void runDaemon(DaemonCarrier carrier)
   {
      // prevent firing of batch events if the system time is changed on a running engine.
      String type = carrier.getType();
      Long lastRun = lastRuns.get(type);
      long now = System.currentTimeMillis();
      if (lastRun == null || now - lastRun > 100 || now < lastRun)
      {
         ForkingService forkingService = getLocalForkingService();
         forkingService.fork(carrier.copy(), false);

         lastRun = now;
         lastRuns.put(type, lastRun);
      }
   }

   private ForkingService getLocalForkingService()
   {
      ForkingServiceFactory factory = Parameters.instance().getObject(
            EngineProperties.FORKING_SERVICE_HOME);
      if (factory == null)
      {
         // TODO: change to client view
/*         try
         {
            throw new RuntimeException("Stack");
         }
         catch (Exception ex)
         {
            trace.warn("\"this\" used as executor service", ex);
         }*/
         factory = new RemoteSessionForkingServiceFactory(this);
      }
      return factory.get();
   }

   private ExecutorService getExecutorService()
   {
      ForkingServiceFactory factory = Parameters.instance().getObject(
            EngineProperties.FORKING_SERVICE_HOME);
      if (factory == null)
      {
         // TODO: change to client view
/*         try
         {
            throw new RuntimeException("Stack");
         }
         catch (Exception ex)
         {
            trace.warn("\"this\" used as executor service", ex);
         }*/
         factory = new RemoteSessionForkingServiceFactory(this);
      }
      return ((RemoteSessionForkingServiceFactory) factory).getService();
   }

   public DataSource getDataSource()
   {
      return dataSource;
   }

   public QueueConnectionFactory getQueueConnectionFactory()
   {
      return queueConnectionFactory;
   }

   public Queue getQueue(String name)
   {
      if (name.equals(JmsProperties.DAEMON_QUEUE_NAME_PROPERTY))
      {
         return daemonQueue;
      }
      else if (name.equals(JmsProperties.SYSTEM_QUEUE_NAME_PROPERTY))
      {
         return messageQueue;
      }
      else
      {
         return null;
      }
   }

   @Override
   public LoggedInUser login(String userId, String password, @SuppressWarnings("rawtypes") Map properties)
   {
      return null;
   }

   @Override
   public ExecutorService getForkingService()
   {
      return null;
   }

   @Override
   public void remove()
   {
   }

   @Override
   public void logout()
   {
   }

   @Override
   public Object getRepository()
   {
      return this.repository;
   }

   @Override
   public void release()
   {
      // (fh) nothing to do
   }
}
