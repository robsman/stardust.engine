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

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;


/**
 * @author sauer
 * @version $Revision: $
 */
public class SystemQueueMessageHandler extends AbstractMessageHandler
{

   private static final Logger trace = LogManager.getLogger(SystemQueueMessageHandler.class);
   
   public void onMessage(Message message)
   {
      final boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK.equalsIgnoreCase(Parameters.instance()
            .getString(JmsProperties.MESSAGE_LISTENER_FAILURE_MODE_PROPERTY,
                  JmsProperties.PROCESSING_FAILURE_MODE_FORGET));
      final int nRetries = Parameters.instance().getInteger(
            JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 20);
      final int tPause = Parameters.instance().getInteger(
            JmsProperties.MESSAGE_LISTENER_RETRY_PAUSE_PROPERTY, 500);

      Action action = (Action) Proxy.newProxyInstance(Action.class.getClassLoader(),
            new Class[] {Action.class}, new MessageHandlingInvocationManager(this,
                  JmsProperties.MDB_NAME_MESSAGE_LISTENER,
                  new SystemQueueMessageDeliveryAction(message), nRetries, tPause,
                  rollbackOnError));

      action.execute();
   }

   private class SystemQueueMessageDeliveryAction implements Action
   {
      private final Message message;

      public SystemQueueMessageDeliveryAction(Message message)
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

                  getForkingService().isolate(transport.createAction());
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
