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

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.MDBInvocationManager;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 */
public class MessageListener
      implements javax.ejb.MessageDrivenBean, javax.jms.MessageListener
{
   private static final Logger trace = LogManager.getLogger(MessageListener.class);

   public static final String MESSAGE_TYPE_TAG = "messageType";

   private MessageDrivenContext context;

   public MessageListener()
   {
   }

   public void setMessageDrivenContext(MessageDrivenContext context)
         throws EJBException
   {
      this.context = context;
   }

   public void ejbCreate()
   {
   }

   public void ejbRemove()
   {
   }

   public void onMessage(Message message)
   {
//      trace.info("Message received: " + message);
      
      final boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK.equalsIgnoreCase(Parameters.instance()
            .getString(JmsProperties.MESSAGE_LISTENER_FAILURE_MODE_PROPERTY,
                  JmsProperties.PROCESSING_FAILURE_MODE_FORGET));
      final int nRetries = Parameters.instance().getInteger(
            JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 20);
      final int tPause = Parameters.instance().getInteger(
            JmsProperties.MESSAGE_LISTENER_RETRY_PAUSE_PROPERTY, 500);

      Action action = (Action) Proxy.newProxyInstance(Action.class.getClassLoader(),
            new Class[] {Action.class}, new MDBInvocationManager(
                  JmsProperties.MDB_NAME_MESSAGE_LISTENER, new MyAction(message),
                  context, nRetries, tPause, rollbackOnError));
      action.execute();
   }

   private class MyAction implements Action
   {
      private Message message;

      public MyAction(Message message)
      {
         this.message = message;
      }

      public Object execute()
      {
         if (message instanceof MapMessage)
         {
            MapMessage mapMessage = (MapMessage) message;

            if (ActionCarrier.extractMessageType(mapMessage) == ActionCarrier
                  .SYSTEM_MESSAGE_TYPE_ID)
            {
               try
               {
                  String transportClassName = mapMessage.getStringProperty(ActionCarrier
                        .TRANSPORT_CLASS_TAG);
                  ActionCarrier transport = (ActionCarrier)
                        Reflect.createInstance(transportClassName);
                  transport.extract(mapMessage);

                  ForkingServiceFactory factory = null;
                  ForkingService service = null;
                  try
                  {
                     factory = (ForkingServiceFactory) Parameters.instance().get(
                           EngineProperties.FORKING_SERVICE_HOME);
                     service = factory.get();
                     service.isolate(transport.createAction());
                  }
                  finally
                  {
                     if (null != factory)
                     {
                        factory.release(service);
                     }
                  }
               }
               catch (JMSException e)
               {
                  trace.warn("Failed handling system message. Recovery run may be required.", e);
               }
            }
            else
            {
               trace.warn("Unknown message type " + ActionCarrier.extractMessageType(mapMessage)
                     + ".");
            }
         }
         else
         {
            trace.warn("JMS Message processed by message daemon is no map message");
         }
         return null;
      }
   }

}