package org.eclipse.stardust.engine.extensions.events.signal;

import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;

public class SendSignalEventAction implements EventActionInstance
{
   public static final String SIGNAL_EVENT_TYPE = "signalEvent";

   static final Logger trace = LogManager.getLogger(SendSignalEventAction.class);

   private String signalCode;
   private String partitionId;

   @Override
   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      Object code = actionAttributes.get(SignalMessageAcceptor.BPMN_SIGNAL_CODE);
      this.signalCode = null != code ? code.toString() : "";

      IAuditTrailPartition partition = SecurityProperties.getPartition();
      this.partitionId = (partition != null) ? partition.getId() : PredefinedConstants.DEFAULT_PARTITION_ID;
   }

   @Override
   public Event execute(Event event) throws UnrecoverableExecutionException
   {
      BpmRuntimeEnvironment bpmrt = PropertyLayerProviderInterceptor.getCurrent();
      Queue queue = bpmrt.getJmsResourceProvider().resolveQueue(JmsProperties.APPLICATION_QUEUE_NAME_PROPERTY);
      QueueConnectionFactory connectionFactory = bpmrt.getJmsResourceProvider().resolveQueueConnectionFactory(JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY);

      try
      {
         QueueConnection queueConnection = bpmrt.retrieveQueueConnection(connectionFactory);
         QueueSession session = bpmrt.retrieveQueueSession(queueConnection);
         final QueueSender sender = bpmrt.retrieveUnidentifiedQueueSender(session);

         TextMessage message = session.createTextMessage();
         message.setStringProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_KEY, signalCode);
         message.setStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER, partitionId);

         sender.send(queue, message);
      }
      catch (JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new UnrecoverableExecutionException("Unable to send event message.", e);
      }

      return null;
   }
}
