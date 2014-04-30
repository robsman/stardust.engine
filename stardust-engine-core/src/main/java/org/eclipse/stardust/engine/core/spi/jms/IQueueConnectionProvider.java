package org.eclipse.stardust.engine.core.spi.jms;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 *
 * @author Thomas.Wolfram
 *
 */

@SPI(useRestriction = UseRestriction.Public, status = Status.Stable)
public interface IQueueConnectionProvider
{
   /**
    *
    * @param factory {@link QueueConnectionFactory}
    * @return {@link QueueConnection}
    * @throws JMSException
    */
   QueueConnection createQueueConnection(QueueConnectionFactory factory)
         throws JMSException;
}
