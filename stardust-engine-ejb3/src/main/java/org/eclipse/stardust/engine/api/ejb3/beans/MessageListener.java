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

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.ejb.ForkingService;

@MessageDriven(activationConfig = {
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/queue/CarnotSystemQueue") })
public class MessageListener extends AbstractEjb3MessageListener
{
   private static final Logger trace = LogManager.getLogger(MessageListener.class);

   public MessageListener()
   {
      super(Kind.MessageListener);
   }

   @Override
   protected MDAction createAction(Message message, ForkingService forkingService)
   {
      return new MDAction(message, forkingService)
      {
         @SuppressWarnings("deprecation")
         public Object execute()
         {
            if (message instanceof MapMessage)
            {
               int messageType = ActionCarrier.extractMessageType(message);
               if (messageType == ActionCarrier.SYSTEM_MESSAGE_TYPE_ID)
               {
                  try
                  {
                     String transportClassName = message.getStringProperty(ActionCarrier.TRANSPORT_CLASS_TAG);
                     ActionCarrier<?> transport = (ActionCarrier<?>) Reflect.createInstance(transportClassName);
                     transport.extract(message);
                     try
                     {
                        forkingService.run(transport.createAction(), forkingService);
                     }
                     catch (WorkflowException e)
                     {
                        throw e.getRootCause();
                     }
                  }
                  catch (JMSException e)
                  {
                     trace.warn("Failed handling system message. Recovery run may be required.", e);
                  }
               }
               else
               {
                  trace.warn("Unknown message type " + messageType + ".");
               }
            }
            else
            {
               trace.warn("JMS Message processed by message daemon is no map message");
            }
            return null;
         }
      };
   }
}