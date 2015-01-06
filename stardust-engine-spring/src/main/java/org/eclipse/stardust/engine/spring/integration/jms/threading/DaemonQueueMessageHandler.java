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
package org.eclipse.stardust.engine.spring.integration.jms.threading;

import java.lang.reflect.Proxy;
import java.util.Arrays;

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
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author sauer
 * @version $Revision: $
 */
public class DaemonQueueMessageHandler extends AbstractMessageHandler
{

   private static final Logger trace = LogManager.getLogger(DaemonQueueMessageHandler.class);
   
   public void onMessage(Message message)
   {
      final boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK.equalsIgnoreCase(Parameters.instance()
            .getString(JmsProperties.DAEMON_LISTENER_FAILURE_MODE_PROPERTY,
                  JmsProperties.PROCESSING_FAILURE_MODE_FORGET));
      final int nRetries = Parameters.instance().getInteger(
            JmsProperties.DAEMON_LISTENER_RETRY_COUNT_PROPERTY, 20);
      final int tPause = Parameters.instance().getInteger(
            JmsProperties.DAEMON_LISTENER_RETRY_PAUSE_PROPERTY, 500);
      
      Action action = (Action) Proxy.newProxyInstance(Action.class.getClassLoader(),
            new Class[] {Action.class}, new MessageHandlingInvocationManager(this,
                  JmsProperties.MDB_NAME_DAEMON_LISTENER,
                  new DaemonQueueMsgDeliveryAction(message), nRetries, tPause,
                  rollbackOnError));

      action.execute();
   }

   private class DaemonQueueMsgDeliveryAction implements Action
   {
      private final Message message;

      public DaemonQueueMsgDeliveryAction(Message message)
      {
         this.message = message;
      }

      public Object execute()
      {
         if (message instanceof MapMessage)
         {
            final MapMessage mapMessage = (MapMessage) message;
            if (ActionCarrier.extractMessageType(mapMessage) == ActionCarrier.DAEMON_MESSAGE_TYPE_ID)
            {
               try
               {
                  final DaemonCarrier carrier = DaemonCarrier.extract(mapMessage);

                  // rsauer: ensure model was bootstrapped before actually running daemon,
                  // as bootstrapping in daemon listener will fail because of a missing
                  // data source
                  ForkingServiceFactory factory = (ForkingServiceFactory) Parameters
                        .instance().get(EngineProperties.FORKING_SERVICE_HOME);
                  ForkingService forkingService = factory.get();
                  try
                  {
                     forkingService.isolate(new Action()
                     {
                        public Object execute()
                        {
                           bootStrapEngine(carrier, mapMessage);
                           ModelManagerFactory.getCurrent().findActiveModel();
                           return null;
                        }
                     });
                  }
                  finally
                  {
                     factory.release(forkingService);
                  }
                  
                  // see CRNT-12082
                  ActionRunner runner = (ActionRunner) Proxy.newProxyInstance(
                        ActionRunner.class.getClassLoader(),
                        new Class[] {ActionRunner.class},
                        new InvocationManager(new ActionRunner()
                        {
                           public Object execute(Action action)
                           {
                              return action.execute();
                           }
                        }, Arrays.asList(new MethodInterceptor[] {
                              new NonInteractiveSecurityContextInterceptor(),
                              new CallingInterceptor()})));
                  runner.execute(carrier.createAction());
               }
               catch (JMSException e)
               {
                  throw new InternalException(e);
               }
            }
            else
            {
               trace.warn("Unknown message type "
                     + ActionCarrier.extractMessageType(mapMessage)
                     + ", message will be lost.");
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
