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
package org.eclipse.stardust.engine.core.persistence.jms;

import javax.jms.*;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;


/**
 * @author sauer
 * @version $Revision$
 */
public class AuditTrailQueueBlobReader extends AbstractJmsBytesMessageReader
{
   
   private static final Logger trace = LogManager.getLogger(AuditTrailQueueBlobReader.class);

   private Queue queue;

   private QueueSession queueSession;

   private QueueReceiver blobReceiver;

   public BytesMessage nextBlobContainer() throws PublicException
   {
      BytesMessage result = null;
      
      try
      {
         final Message nextMsg = blobReceiver.receiveNoWait();
         if (nextMsg instanceof BytesMessage)
         {
            result = (BytesMessage) nextMsg;
         }
         else
         {
            if (null == nextMsg)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("No more BLOBs are available.");
               }
            }
            else
            {
               trace.warn("Unexpected message of type " + nextMsg.getClass());
            }
         }
      }
      catch (JMSException jmse)
      {
         // TODO: handle exception
         throw new PublicException(
               "Failed reading process BLOB from JMS AuditTrail queue.");
      }
      
      return result;
   }
   
   public void init(Parameters params) throws PublicException
   {
      super.init(params);
      
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      try
      {

         this.queue = (Queue) params.get(JmsProperties.AUDIT_TRAIL_QUEUE_NAME_PROPERTY);

         QueueConnectionFactory connectionFactory = (QueueConnectionFactory) params.get(JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY);
         QueueConnection connection = rtEnv.retrieveQueueConnection(connectionFactory);

         this.queueSession = rtEnv.retrieveQueueSession(connection);

         this.blobReceiver = queueSession.createReceiver(queue);

         // start receiving messages
         connection.start();
      }
      catch (JMSException jmse)
      {
         throw new PublicException("Failed connecting to JMS AuditTrail queue.");
      }
   }

   public void close() throws PublicException
   {
      try
      {
         blobReceiver.close();
      }
      catch (JMSException jmse)
      {
         // ignore
/*
         trace.warn("Failed closing queue receiver.", jmse);
*/
      }
      this.blobReceiver = null;
      
      // JMS resources will be closed by RT environment
      this.queueSession = null;
      this.queue = null;
      
      super.close();
   }

}