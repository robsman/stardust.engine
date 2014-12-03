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
import javax.ejb.*;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.DaemonFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.*;
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
   private static final String FORKING_SERVICE_JNDI_NAME = "java:module/ForkingServiceImpl!"
      + org.eclipse.stardust.engine.core.runtime.ejb.ForkingService.class.getName();

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
      return run(action, getForkingService());
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
                  innerCarrier.getType() + DaemonProperties.DAEMON_PERIODICITY_SUFFIX,
                  DaemonFactory.instance().get(innerCarrier.getType()).getDefaultPeriodicity()) * 1000;
            service.createTimer(periodicity, periodicity, innerCarrier);
            shouldStart = true;
            innerCarrier.setTimeToLive(periodicity);
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
         ForkingService forkingService = getForkingServiceFactory().get();
         forkingService.fork(carrier.copy(), false);

         lastRun = now;
         lastRuns.put(type, lastRun);
      }
   }

   private RemoteSessionForkingServiceFactory getForkingServiceFactory()
   {
      ForkingServiceFactory factory = Parameters.instance().getObject(
            EngineProperties.FORKING_SERVICE_HOME);
      if (factory instanceof RemoteSessionForkingServiceFactory)
      {
         return (RemoteSessionForkingServiceFactory) factory;
      }
      try
      {
         Context context = new InitialContext();
         org.eclipse.stardust.engine.core.runtime.ejb.ForkingService fs = (org.eclipse.stardust.engine.core.runtime.ejb.ForkingService) context.lookup(FORKING_SERVICE_JNDI_NAME);
         return new RemoteSessionForkingServiceFactory(fs);
      }
      catch (Exception ex)
      {
         trace.warn("Unable to retrieve the ForkingService", ex);
         return new RemoteSessionForkingServiceFactory(this);
      }
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
      return getForkingServiceFactory().getService();
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
