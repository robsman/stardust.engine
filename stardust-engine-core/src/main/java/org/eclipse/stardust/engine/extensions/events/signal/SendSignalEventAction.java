package org.eclipse.stardust.engine.extensions.events.signal;

import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;

/**
 * @author Simon Nikles
 * @author Stéphane Ruffieux
 * @author rsauer
 *
 */
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
         QueueSender sender = bpmrt.retrieveUnidentifiedQueueSender(session);

         MapMessage message = session.createMapMessage();
         message.setStringProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_KEY, signalCode);
         message.setStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER, partitionId);

         prepareMessage(event, message);

         sender.send(queue, message);
      }
      catch (JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new UnrecoverableExecutionException("Unable to send event message.", e);
      }

      return null;
   }

   protected void prepareMessage(Event event, MapMessage message) throws JMSException
   {
      if (event.getObjectOID() == Event.OID_UNDEFINED)
      {
         message.setIntProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_EMITTER_TYPE, event.getEmitterType());

         int modelOid = (Integer) event.getAttribute("modelOid");
         message.setIntProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_MODEL_OID, modelOid);

         long runtimeOid = (Long) event.getAttribute("runtimeOid");
         message.setLongProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_RUNTIME_OID, runtimeOid);

         String id = (String) event.getAttribute("id");
         Object dataValue = event.getAttribute("dataValue");
         message.setObject(id, dataValue);
      }
      else
      {
         ActivityInstanceBean ai = ActivityInstanceBean.findByOID(event.getObjectOID());
         String eventHandlerId = determineActiveEventHandlerId(ai.getActivity(), event.getHandlerModelElementOID());
         ModelElementList<IDataMapping> inDataMappings = ai.getActivity().getInDataMappings();
         for (IDataMapping mapping : inDataMappings)
         {
            if ( !mapping.getContext().equals(PredefinedConstants.EVENT_CONTEXT + eventHandlerId))
            {
               continue;
            }

            String id = mapping.getId();
            IData data = mapping.getData();
            String dataPath = mapping.getDataPath();
            Object dataValue = ai.getProcessInstance().getInDataValue(data, dataPath);
            message.setObject(id, dataValue);
         }
      }
   }

   private String determineActiveEventHandlerId(IActivity ai, long handlerOid)
   {
      for (IEventHandler e : ai.getEventHandlers())
      {
         if (e.getOID() == handlerOid)
         {
            return e.getId();
         }
      }

      throw new IllegalStateException("Active Event Handler with OID '" + handlerOid + "' could not be determined:");
   }
}
