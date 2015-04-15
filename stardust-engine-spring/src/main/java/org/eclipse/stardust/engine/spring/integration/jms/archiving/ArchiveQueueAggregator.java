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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jms.*;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.spring.SpringConstants;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;


/**
 * Listens to for messages on jms/CarnotExportQueue, groups and aggregates them by partition and sends them to jms/CarnotArchiveQueue
 * @author jsaayman
 * @version $Revision: $
 */
public class ArchiveQueueAggregator implements ApplicationContextAware
{

   private static final Logger trace = LogManager.getLogger(ArchiveQueueAggregator.class);

   private ApplicationContext applicationContext;

   private PlatformTransactionManager transactionManager;
   // configured with default value that can be overriden by spring
   private long receiveTimeout = 3000;
   // configured with default value that can be overriden by spring
   private long maxMessages = 1000;

   public void aggregate() 
   {
      QueueConnectionFactory connectionFactory = (QueueConnectionFactory) applicationContext
            .getBean(SpringConstants.BEAN_ID_JMS_CONNECTION_FACTORY);
      Queue exportQ = (Queue) applicationContext.getBean(SpringConstants.BEAN_ID_JMS_EXPORT_QUEUE);
      Queue archiveQ = (Queue) applicationContext.getBean(SpringConstants.BEAN_ID_JMS_ARCHIVE_QUEUE);
      doAggregate(connectionFactory, exportQ, archiveQ);
   }
   
   public void doAggregate(QueueConnectionFactory connectionFactory, Queue exportQ, Queue archiveQ) 
   {
      QueueConnection connection = null;
      QueueSession session = null;
      MessageConsumer consumer = null;
      QueueSender sender = null;
      boolean success = true;
      TransactionStatus status = transactionManager
            .getTransaction(new DefaultTransactionDefinition());
      try
      {
         connection = connectionFactory.createQueueConnection();
         session = connection.createQueueSession(false,
               javax.jms.Session.CLIENT_ACKNOWLEDGE);
         consumer = session.createConsumer(exportQ);
         sender = session.createSender(archiveQ);

         Message message = null;
         Map<String, ArrayList<Object>> partitionObjects = new HashMap<String, ArrayList<Object>>();
         int messageCount = 0;
         do
         {
            message = consumer.receive(receiveTimeout);
            if (message != null && !(message instanceof ObjectMessage))
            {
               throw new UnsupportedOperationException(
                     "Can only read from Object message.");
            }
            if (message != null)
            {
               ObjectMessage objMessage = (ObjectMessage) message;
               String partition = message.getStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER);
               ArrayList<Object> exports = partitionObjects.get(partition);
               if (exports == null)
               {
                  exports = new ArrayList<Object>();
                  partitionObjects.put(partition, exports);
               }
               exports.add(objMessage.getObject());
               message.acknowledge();
               messageCount++;
            }
         }
         while (message != null && messageCount < maxMessages);
         if (trace.isDebugEnabled())
         {
            trace.debug("ArchiveQueueAggregator processed " + messageCount + " messages");
         }
         System.out.println("ArchiveQueueAggregator processed " + messageCount + " messages");

         if (CollectionUtils.isNotEmpty(partitionObjects.keySet()))
         {
            for (String partition : partitionObjects.keySet())
            {
               ObjectMessage archiveMessage = session.createObjectMessage();
               archiveMessage.setStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER, partition);
               archiveMessage.setObject(partitionObjects.get(partition));
               sender.send(archiveQ, archiveMessage);
               if (trace.isDebugEnabled())
               {
                  trace.debug("ArchiveQueueAggregator sent " + 
                        partitionObjects.get(partition).size() + 
                        " messages to partition " + partition);
               }
               System.out.println("ArchiveQueueAggregator sent " + 
                     partitionObjects.get(partition).size() + 
                     " messages to partition " + partition);

            }
            success = true;
         }
         else
         {
            success = true;
         }
      }
      catch (JMSException e)
      {
         trace.error("Failed aggregating archive messages", e);
         success = false;
      }
      finally
      {
         if (success)
         {
            transactionManager.commit(status);
         }
         else
         {
            transactionManager.rollback(status);
         }
         if (consumer != null)
         {
            try
            {
               consumer.close();
            }
            catch (JMSException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Ignoring error during JMS Consumer close: "
                        + e.getMessage(), e);
               }
            }
         }
         if (sender != null)
         {
            try
            {
               sender.close();
            }
            catch (JMSException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Ignoring error during JMS sender close: "
                        + e.getMessage(), e);
               }
            }
         }
         if (session != null)
         {
            try
            {
               session.close();
            }
            catch (JMSException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Ignoring error during JMS session close: "
                        + e.getMessage(), e);
               }
            }
         }
         if (connection != null)
         {
            try
            {
               connection.close();
            }
            catch (JMSException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Ignoring error during JMS connection close: "
                        + e.getMessage(), e);
               }
            }
         }
      }
   }

   @Override
   public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException
   {
      this.applicationContext = applicationContext;
   }

   public void setTransactionManager(PlatformTransactionManager transactionManager)
   {
      this.transactionManager = transactionManager;
   }

   public void setReceiveTimeout(long receiveTimeout)
   {
      this.receiveTimeout = receiveTimeout;
   }

   public void setMaxMessages(long maxMessages)
   {
      this.maxMessages = maxMessages;
   }
   
}
