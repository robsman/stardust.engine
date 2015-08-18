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
package org.eclipse.stardust.engine.spring.integration.jms.threading;

import javax.jms.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.rt.IActionCarrier;
import org.eclipse.stardust.common.rt.IMessageWithTtl;
import org.eclipse.stardust.engine.api.spring.AbstractSpringForkingServiceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.spi.jms.IJmsResourceProvider;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;



/**
 * @author sauer
 * @version $Revision: $
 */
public class JmsForkingService extends AbstractSpringForkingServiceBean
{
   
   public void fork(final IActionCarrier action, boolean transacted)
   {
      if ( !transacted)
      {
         // send message from autonomous TX
         TransactionTemplate txTempl = new TransactionTemplate(getTransactionManager());
         txTempl.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
         txTempl.execute(new TransactionCallback()
         {
            public Object doInTransaction(TransactionStatus status)
            {
               fork(action, true);

               return null;
            }
         });
      }
      else
      {
         IJmsResourceProvider jmsResourceProvider = getJmsResourceProvider();
         
         JmsTemplate jmsTemplate = new JmsTemplate();

         jmsTemplate.setConnectionFactory(jmsResourceProvider.resolveQueueConnectionFactory(JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY));

         jmsTemplate.setSessionTransacted(transacted);
         
         Queue targetQueue;
         switch (action.getMessageType())
         {
         case ActionCarrier.SYSTEM_MESSAGE_TYPE_ID:
            targetQueue = jmsResourceProvider.resolveQueue(JmsProperties.SYSTEM_QUEUE_NAME_PROPERTY);
            break;
         case ActionCarrier.DAEMON_MESSAGE_TYPE_ID:
            targetQueue = jmsResourceProvider.resolveQueue(JmsProperties.DAEMON_QUEUE_NAME_PROPERTY);
            break;
         case ActionCarrier.RESPONSE_HANDLER_MESSAGE_TYPE_ID:
            targetQueue = jmsResourceProvider.resolveQueue(JmsProperties.APPLICATION_QUEUE_NAME_PROPERTY);
            break;
         default:
            throw new InternalException("Unsupported message type: " + action.getMessageType());
         }
         
         // TODO verify availability of target queue
         
         if (action instanceof IMessageWithTtl)
         {
            jmsTemplate.setExplicitQosEnabled(true);
            jmsTemplate.setTimeToLive(((IMessageWithTtl) action).getTimeToLive());
         }
         
         jmsTemplate.send(targetQueue, new MessageCreator()
         {
            public Message createMessage(Session session) throws JMSException
            {
               MapMessage msg = session.createMapMessage();
               action.fillMessage(msg);
               
               return msg;
            }
         });
      }
   }

}
