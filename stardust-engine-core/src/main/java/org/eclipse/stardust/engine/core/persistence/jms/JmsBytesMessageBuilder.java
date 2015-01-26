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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;


/**
 * @author sauer
 * @version $Revision$
 */
public class JmsBytesMessageBuilder implements BlobBuilder
{

   private QueueSession queueSession;

   private QueueSender queueSender;

   private Queue queue;

   private BytesMessage msg;

   public void init(Parameters params) throws PublicException
   {
      try
      {
         if (null == queueSession)
         {
            final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

            QueueConnectionFactory connectionFactory = (QueueConnectionFactory) params.get(JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY);

            QueueConnection connection = rtEnv.retrieveQueueConnection(connectionFactory);
            this.queueSession = rtEnv.retrieveQueueSession(connection);
            this.queueSender = rtEnv.retrieveUnidentifiedQueueSender(queueSession);

            this.queue = rtEnv.getJmsResourceProvider().resolveQueue(JmsProperties.AUDIT_TRAIL_QUEUE_NAME_PROPERTY);
         }

         this.msg = queueSession.createBytesMessage();
      }
      catch (JMSException jmse)
      {
         throw new PublicException(
               BpmRuntimeError.JMS_FAILED_INITIALIZING_JMS_BLOB_BUILDER.raise(), jmse);
      }
   }

   public void persistAndClose() throws PublicException
   {
      try
      {
         msg.writeByte(SECTION_MARKER_EOF);

         queueSender.send(queue, msg);

         this.msg = null;

         // sender, session and connection will be closed by RT Environment and end of TX
      }
      catch (JMSException jmse)
      {
         throw new PublicException(
               BpmRuntimeError.JMS_FAILED_WRITING_BLOB_TO_JMS.raise(), jmse);
      }
   }

   public void startInstancesSection(String tableName, int nInstances)
         throws InternalException
   {
      try
      {
         msg.writeByte(SECTION_MARKER_INSTANCES);
         msg.writeUTF(tableName);
         msg.writeInt(nInstances);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeBoolean(boolean value) throws InternalException
   {
      try
      {
         msg.writeBoolean(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeChar(char value) throws InternalException
   {
      try
      {
         msg.writeChar(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeByte(byte value) throws InternalException
   {
      try
      {
         msg.writeByte(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeShort(short value) throws InternalException
   {
      try
      {
         msg.writeShort(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeInt(int value) throws InternalException
   {
      try
      {
         msg.writeInt(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeLong(long value) throws InternalException
   {
      try
      {
         msg.writeLong(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeFloat(float value) throws InternalException
   {
      try
      {
         msg.writeFloat(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeDouble(double value) throws InternalException
   {
      try
      {
         msg.writeDouble(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

   public void writeString(String value) throws InternalException
   {
      try
      {
         msg.writeUTF(value);
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed writing value to JMS blob.", jmse);
      }
   }

}
