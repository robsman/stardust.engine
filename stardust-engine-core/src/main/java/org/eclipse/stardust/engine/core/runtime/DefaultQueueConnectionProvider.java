package org.eclipse.stardust.engine.core.runtime;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import org.eclipse.stardust.engine.core.spi.jms.IQueueConnectionProvider;

public class DefaultQueueConnectionProvider implements IQueueConnectionProvider
{

   @Override
   public QueueConnection createQueueConnection(QueueConnectionFactory factory) throws JMSException
   {
         return factory.createQueueConnection();

   }

}
