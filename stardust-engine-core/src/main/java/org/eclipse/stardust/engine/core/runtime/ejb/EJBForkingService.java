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
package org.eclipse.stardust.engine.core.runtime.ejb;

import java.lang.reflect.UndeclaredThrowableException;

import javax.jms.*;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.rt.IActionCarrier;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;

/**
 * @author ubirkemeyer
 * @version $Revision: 56255 $
 */
public class EJBForkingService implements ForkingService
{
   private ExecutorService executor;

   public EJBForkingService(ExecutorService service)
   {
      this.executor = service;
   }

   public Object isolate(@SuppressWarnings("rawtypes") Action action) throws PublicException
   {
      try
      {
         return executor.run(action);
      }
      catch (WorkflowException e)
      {
         if (e.getCause() instanceof RuntimeException)
         {
            throw (RuntimeException) e.getCause();
         }
         else
         {
            throw new UndeclaredThrowableException(e.getCause());
         }
      }
   }

   public void fork(@SuppressWarnings("rawtypes") IActionCarrier action, boolean transacted)
   {
      SendAction sender = new SendAction(action);
      if (transacted)
      {
         sender.execute();
      }
      else
      {
         try
         {
            executor.run(sender, executor);
         }
         catch (WorkflowException e)
         {
            if (e.getCause() instanceof RuntimeException)
            {
               throw (RuntimeException) e.getCause();
            }
            else
            {
               throw new UndeclaredThrowableException(e.getCause());
            }
         }
      }
   }

   public void release()
   {
      executor.release();
   }

   private class SendAction implements Action<Object>
   {
      private IActionCarrier<?> action;

      public SendAction(IActionCarrier<?> action)
      {
         this.action = action;
      }

      public Object execute()
      {
         try
         {
            final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

            QueueConnectionFactory factory = executor.getQueueConnectionFactory();
            if (factory == null)
            {
               throw new InternalException("Reference '"
                     + JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY + "' is not set.");
            }

            // sender, session and connection will be closed by RT Environment at end of TX
            final QueueConnection connection = rtEnv.retrieveQueueConnection(factory);
            final QueueSession session = rtEnv.retrieveQueueSession(connection);

            String queueName;
            if (ActionCarrier.SYSTEM_MESSAGE_TYPE_ID == action.getMessageType())
            {
               queueName = JmsProperties.SYSTEM_QUEUE_NAME_PROPERTY;
            }
            else if (ActionCarrier.DAEMON_MESSAGE_TYPE_ID == action.getMessageType())
            {
               queueName = JmsProperties.DAEMON_QUEUE_NAME_PROPERTY;
            }
            else
            {
               throw new PublicException(
                     BpmRuntimeError.EJB_UNKNOWN_CARRIER_MESSAGE_TYPE
                           .raise(action.getMessageType()));
            }

            Queue queue = executor.getQueue(queueName);
            if (queue == null)
            {
               throw new InternalException("Reference '" + queueName + "' is not set.");
            }

            final QueueSender sender = rtEnv.retrieveUnidentifiedQueueSender(session);
            Message message = session.createMapMessage();
            action.fillMessage(message);
            sender.send(queue, message);
         }
         catch (JMSException e)
         {
            throw new InternalException("Failed to send JMS message: " + e.getMessage(), e);
         }
         return null;
      }
   }
}
