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
package org.eclipse.stardust.engine.core.persistence.archive;

import javax.jms.*;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;


/**
 * This class sends messages to jms/CarnotExportQueue
 * This is invoked upon complete/abort of a process for the purpose of archiving processes
 * @author jsaayman
 * @version $Revision$
 */
public class ExportQueueSender
{

   private QueueSession queueSession;

   private QueueSender queueSender;

   private Queue queue;

   private ObjectMessage msg;

   public ExportQueueSender() throws PublicException
   {
      try
      {
         if (null == queueSession)
         {
            final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
            QueueConnectionFactory connectionFactory = rtEnv.retrieveQueueConnectionFactory(JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY);
            QueueConnection connection = rtEnv.retrieveQueueConnection(connectionFactory);
            this.queueSession = rtEnv.retrieveQueueSession(connection);
            this.queueSender = rtEnv.retrieveUnidentifiedQueueSender(queueSession);

            this.queue = rtEnv.resolveQueue(JmsProperties.EXPORT_QUEUE_NAME_PROPERTY);
         }

         this.msg = queueSession.createObjectMessage();
      }
      catch (JMSException jmse)
      {
         throw new PublicException(
               BpmRuntimeError.JMS_FAILED_INITIALIZING_JMS_BLOB_BUILDER.raise(), jmse);
      }
   }

   public void sendMessage(ExportResult exportResult) throws PublicException
   {
      try
      {
         msg.setObject(exportResult);
         msg.setStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER, SecurityProperties.getPartition().getId());

         queueSender.send(queue, msg);

         this.msg = null;

         // sender, session and connection will be closed by RT Environment at end of TX
      }
      catch (JMSException jmse)
      {
         throw new PublicException(
               BpmRuntimeError.JMS_FAILED_WRITING_BLOB_TO_JMS.raise(), jmse);
      }
   }

}
