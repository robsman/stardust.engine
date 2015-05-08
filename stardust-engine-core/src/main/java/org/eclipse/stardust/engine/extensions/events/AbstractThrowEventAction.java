package org.eclipse.stardust.engine.extensions.events;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;

public abstract class AbstractThrowEventAction implements EventActionInstance
{
   static final Logger trace = LogManager.getLogger(AbstractThrowEventAction.class);

   public final static String THROW_EVENT_TYPE_HEADER = "throwEventType";

   protected String eventCode;

   @Override
   public Event execute(Event event) throws UnrecoverableExecutionException
   {
      // TODO - bpmn-2-events - handle sending signals as well

      BpmRuntimeEnvironment bpmrt = PropertyLayerProviderInterceptor.getCurrent();
      Queue queue = bpmrt.getJmsResourceProvider().resolveQueue(JmsProperties.APPLICATION_QUEUE_NAME_PROPERTY);
      QueueConnectionFactory connectionFactory = bpmrt.getJmsResourceProvider().resolveQueueConnectionFactory(
            JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY);
      boolean fromActivity = true;
      try
      {
         IProcessInstance processInstance = null;

         if (Event.ACTIVITY_INSTANCE == event.getType())
         {
            // event holder activity
            ActivityInstanceBean activityInstanceBean = ActivityInstanceBean.findByOID(event.getObjectOID());
            // failing process
            processInstance = activityInstanceBean.getProcessInstance();
         }
         else if (Event.PROCESS_INSTANCE == event.getType())
         {
            fromActivity = false;
            processInstance = ProcessInstanceBean.findByOID(event.getObjectOID());
         }

         IActivityInstance startingActivityInstance = processInstance.getStartingActivityInstance();
         IActivityInstance escalationCatchingActivityInstance = findCatchingEvent(startingActivityInstance);

         if (null != escalationCatchingActivityInstance)
         {
            QueueConnection queueConnection = bpmrt.retrieveQueueConnection(connectionFactory);
            QueueSession session = bpmrt.retrieveQueueSession(queueConnection);
            final QueueSender sender = bpmrt.retrieveUnidentifiedQueueSender(session);
            TextMessage message = session.createTextMessage();
            message.setText(this.eventCode);
            message.setLongProperty(DefaultMessageHelper.ACTIVITY_INSTANCE_OID_HEADER,
                  escalationCatchingActivityInstance.getOID());
            message.setStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER, SecurityProperties.getPartition()
                  .getId());
            message.setStringProperty(THROW_EVENT_TYPE_HEADER, getThrowEventType());
          trace.info("Send "+getThrowEventType()+" Message (code: " + eventCode + ") from throwing "+(fromActivity ? "Activity" : "Process")+" ("+event.getObjectOID()+") to catching Activity "
                  + escalationCatchingActivityInstance.getOID());
            sender.send(queue, message);
         }
         else
         {
            trace.warn("No Catching Activity Instance found for event ("+getThrowEventType()+ ": "+eventCode+") fired in Process Instance with OID " + processInstance.getOID());
         }

      }
      catch (JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new UnrecoverableExecutionException("Unable to send event message.", e);
      }

      return event;
   }

   /**
    * @param startingActivityInstance
    *           starting activity of the subprocess throwing the error message
    * @return hierarchically nearest subprocess-activity (potentially
    *         <code>startingActivityInstance</code> itself) having a matching error catch
    *         event or null, if no catch has been found.
    */
   protected IActivityInstance findCatchingEvent(IActivityInstance startingActivityInstance)
   {
      IActivityInstance activityInstance = startingActivityInstance;
      while(null != activityInstance && !hasMatchingCatchEvent(activityInstance))
      {
         try
         {
            activityInstance = activityInstance.getProcessInstance().getStartingActivityInstance();
         }
         catch(Exception e)
         {
            trace.warn("No starting Activity Instance found for activity with oid " + activityInstance.getOID());
         }
      }
      return activityInstance;
   }

   protected boolean hasMatchingCatchEvent(IActivityInstance activityInstance)
   {
      ModelElementList<IEventHandler> eventHandlers = activityInstance.getActivity().getEventHandlers();
      for (IEventHandler handler : eventHandlers)
      {
         if (handler.getType().getId().equals(getConditionType()))
         {
            return true;
         }
      }

      return false;
   }

   protected abstract String getThrowEventType();

   protected abstract String getConditionType();
}