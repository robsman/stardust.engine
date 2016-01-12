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
package org.eclipse.stardust.engine.spring.integration.jms.archiving;

import java.lang.reflect.Proxy;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.archive.ArchiveQueueHandlerCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.spring.integration.jms.threading.AbstractMessageHandler;
import org.eclipse.stardust.engine.spring.integration.jms.threading.MessageHandlingInvocationManager;

/**
 * Listens to for messages on jms/CarnotArchiveQueue and sends them to ExportProcessesCommand for archiving
 * @author jsaayman
 * @version $Revision: $
 */
public class ArchiveMessageHandler extends AbstractMessageHandler
{

   private static final Logger trace = LogManager.getLogger(ArchiveMessageHandler.class);

   public void onMessage(Message message)
   {
      if (message instanceof ObjectMessage)
      {
         final boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK
               .equalsIgnoreCase(Parameters.instance().getString(
                     JmsProperties.RESPONSE_HANDLER_FAILURE_MODE_PROPERTY,
                     JmsProperties.PROCESSING_FAILURE_MODE_FORGET));
         final int nRetries = Parameters.instance().getInteger(
               JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 20);
         final int tPause = Parameters.instance().getInteger(
               JmsProperties.RESPONSE_HANDLER_RETRY_PAUSE_PROPERTY, 500);

         Action action = (Action) Proxy.newProxyInstance(Action.class.getClassLoader(),
               new Class[] {Action.class}, new MessageHandlingInvocationManager(this,
                     JmsProperties.MDB_NAME_MESSAGE_LISTENER,
                     new ArchiveQueueMessageDeliveryAction((ObjectMessage)message), nRetries, tPause, rollbackOnError));
         action.execute();
      }
      else
      {
         trace.warn("JMS Message processed by message ArchiveMessageHandler is not an Object message");
      }

   }

   private class ArchiveQueueMessageDeliveryAction implements Action
   {
      private final ObjectMessage message;

      public ArchiveQueueMessageDeliveryAction(ObjectMessage message)
      {
         this.message = message;
      }

      public Object execute()
      {
         boolean success;
         ForkingService service = null;
         try
         {
            ArchiveQueueHandlerCarrier carrier = new ArchiveQueueHandlerCarrier(message);
            carrier.extract(message);

            ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance()
                  .get(EngineProperties.FORKING_SERVICE_HOME);
            service = factory.get();
            service.isolate(carrier.createAction());
            success = true;
         }
         catch (JMSException e)
         {
            throw new InternalException(e);
         }
         finally
         {
            // @todo (france, ub): ForkingServiceLocator.release(service);
         }
         return success;
      }
   }

}
