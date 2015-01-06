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
package org.eclipse.stardust.engine.api.ejb2.beans;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.NonInteractiveSecurityContextInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.MDBInvocationManager;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DaemonListener implements javax.ejb.MessageDrivenBean,
      javax.jms.MessageListener
{
   public static final Logger trace = LogManager.getLogger(DaemonListener.class);

   private MessageDrivenContext context;

   public void ejbCreate()
   {
   }

   public void setMessageDrivenContext(MessageDrivenContext context)
         throws EJBException
   {
      this.context = context;
   }

   public void ejbRemove()
   {
   }

   public void onMessage(Message message)
   {
      String failureMode = Parameters.instance().getString(
            JmsProperties.DAEMON_LISTENER_FAILURE_MODE_PROPERTY, JmsProperties.PROCESSING_FAILURE_MODE_FORGET);
      final boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK.equalsIgnoreCase(failureMode);
      final int nRetries = Parameters.instance().getInteger(
            JmsProperties.DAEMON_LISTENER_RETRY_COUNT_PROPERTY, 20);
      final int tPause = Parameters.instance().getInteger(
            JmsProperties.DAEMON_LISTENER_RETRY_PAUSE_PROPERTY, 500);
      
      Action action = (Action) Proxy.newProxyInstance(Action.class.getClassLoader(),
            new Class[] {Action.class}, new MDBInvocationManager(
                  JmsProperties.MDB_NAME_DAEMON_LISTENER, new MyAction(message), context,
                  nRetries, tPause, rollbackOnError));
      action.execute();
   }

   private class MyAction implements Action
   {
      private Message message;

      public MyAction(Message message)
      {
         this.message = message;
      }
      
      public String toString()
      {
    	 return "Daemon Action";
      }

      public Object execute()
      {
         if (message instanceof MapMessage)
         {
            MapMessage mapMessage = (MapMessage) message;
            if (ActionCarrier.extractMessageType(mapMessage) == ActionCarrier
                  .DAEMON_MESSAGE_TYPE_ID)
            {
               trace.info("Start Daemon message received.");

               // rsauer: ensure model was bootstrapped before actually running daemon,
               // as bootstrapping in daemon listener will fail because of a missing
               // data source
               ForkingServiceFactory factory = (ForkingServiceFactory)
                     Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
               ForkingService forkingService = factory.get();
               try
               {
                  forkingService.isolate(new Action()
                  {
                     public Object execute()
                     {
                        ModelManagerFactory.getCurrent().findActiveModel();
                        return null;
                     }
                  });
               }
               finally
               {
                  factory.release(forkingService);
               }

               try
               {
                  DaemonCarrier carrier = DaemonCarrier.extract(mapMessage);
                  // (fh) we need an ActionRunner for the interceptor to kick in
                  ActionRunner runner = (ActionRunner) Proxy.newProxyInstance(
                        ActionRunner.class.getClassLoader(),
                        new Class[] {ActionRunner.class},
                        new InvocationManager(new ActionRunner()
                        {
                           public Object execute(Action action)
                           {
                              return action.execute();
                           }
                        },
                        Arrays.asList(new MethodInterceptor[] {
                              new NonInteractiveSecurityContextInterceptor(),
                              new CallingInterceptor()})));
                  runner.execute(carrier.createAction());
               }
               catch (JMSException e)
               {
                  throw new InternalException(e);
               }
               finally
               {
                  trace.info("Daemon action ended.");
               }
            }
            else
            {
               trace.warn("Unknown message type " + ActionCarrier.extractMessageType(
                     mapMessage) + ", message will be lost.");
            }
         }
         else
         {
            trace.warn("JMS Message processed by message daemon is no map message, message "
                  + "will be lost.");
         }
         return null;
      }
   }
}
