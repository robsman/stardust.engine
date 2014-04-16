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

import java.lang.reflect.UndeclaredThrowableException;

import javax.ejb.EJB;
import javax.ejb.RemoveException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.IActionCarrier;
import org.eclipse.stardust.engine.api.ejb3.WorkflowException;
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
   private static final Logger trace = LogManager.getLogger(EJBForkingService.class);
   

   private final org.eclipse.stardust.engine.api.ejb3.ForkingService impl;

   public EJBForkingService(org.eclipse.stardust.engine.api.ejb3.ForkingService service)
   {
         this.impl = service;
   }

   public Object isolate(Action action) throws PublicException
   {
      try
      {
         return impl.run(action);
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

   public void fork(IActionCarrier action, boolean transacted)
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
            impl.run(sender, impl);
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
   
   

   private class SendAction implements Action
   {
      private IActionCarrier action;

      public SendAction(IActionCarrier action)
      {
         this.action = action;
      }

      public Object execute()
      {
         try
         {
            final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor
                  .getCurrent();

            QueueConnectionFactory factory = impl.getQueueConnectionFactory();
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
               throw new PublicException("Unknown carrier message type: "
                     + action.getMessageType());
            }

            Queue queue = impl.getQueue(queueName);
            if (queue == null)
            {
               throw new InternalException("Reference '" + queueName + "' is not set.");
            }

            final QueueSender sender = rtEnv.retrieveUnidentifiedQueueSender(session);

            Message message = session.createMapMessage();

            action.fillMessage(message);

            sender.send(queue, message);
            
//            session.commit();
//            
//            session.close();
         }
         catch (JMSException e)
         {
            throw new InternalException("Failed to send JMS message: " + e.getMessage(), e);
         }
         return null;
      }
   }
}


