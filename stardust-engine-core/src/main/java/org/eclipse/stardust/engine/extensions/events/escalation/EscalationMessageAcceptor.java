package org.eclipse.stardust.engine.extensions.events.escalation;

import static java.util.Collections.singletonList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.EventHandlerBean;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.extensions.events.AbstractThrowEventAction;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.engine.extensions.jms.app.JMSLocation;
import org.eclipse.stardust.engine.extensions.jms.app.MessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.app.MessageType;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerImpl.Match;

/**
 * @author Simon Nikles
 * @author Stéphane Ruffieux
 * @author rsauer
 *
 */
public class EscalationMessageAcceptor implements MessageAcceptor, Stateless
{
   private static final Logger trace = LogManager.getLogger(EscalationMessageAcceptor.class);

   public static final String BPMN_ESCALATION_CODE = "carnot:engine:escalationCode";

   @Override
   public boolean isStateless()
   {
      return true;
   }

   @Override
   public Iterator<IActivityInstance> getMatchingActivityInstances(Message message)
   {
      List<IActivityInstance> result = newArrayList();
      try
      {
         if (message.propertyExists(DefaultMessageHelper.ACTIVITY_INSTANCE_OID_HEADER)
               && message.propertyExists(AbstractThrowEventAction.THROW_EVENT_TYPE_HEADER))
         {
            if (ThrowEscalationEventAction.THROW_EVENT_TYPE.equals(message
                  .getStringProperty(AbstractThrowEventAction.THROW_EVENT_TYPE_HEADER)))
            {
               long activityInstanceOID = message.getLongProperty(DefaultMessageHelper.ACTIVITY_INSTANCE_OID_HEADER);
               if (trace.isDebugEnabled())
               {
                  trace.debug("Accept message " + ThrowEscalationEventAction.THROW_EVENT_TYPE
                        + " for activity instance with OID " + activityInstanceOID + ".");
               }

               IActivityInstance matchingActivity = ActivityInstanceBean.findByOID(activityInstanceOID);
               if (ImplementationType.SubProcess.equals(matchingActivity.getActivity().getImplementationType()))
               {
                  result = singletonList(matchingActivity);
               }
            }
         }
      }
      catch (ObjectNotFoundException o)
      {
         // TODO - bpmn-2-events - left empty deliberately?
      }
      catch (JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e);
      }
      return result.iterator();
   }

   @Override
   public Match finalizeMatch(IActivityInstance activityInstance)
   {
      if (!activityInstance.isTerminated())
      {
         return new EscalationMessageMatch(this, activityInstance);
      }
      return null;
   }

   @Override
   public Map<String, Object> getData(Message message, StringKey id, Iterator accessPoints)
   {
      try
      {
         Object text = ((TextMessage) message).getText();
         return Collections.singletonMap(BPMN_ESCALATION_CODE, text);
      }
      catch (JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e);
      }
   }

   @Override
   public String getName()
   {
      return "BPMN2.0 Error Message Acceptor";
   }

   @Override
   public boolean hasPredefinedAccessPoints(StringKey id)
   {
      return DefaultMessageHelper.hasPredefinedAccessPoints(id);
   }

   @Override
   public Collection<AccessPoint> getAccessPoints(StringKey messageType)
   {
      List<AccessPoint> intrinsicAccessPoints = null;

      if (messageType.equals(MessageType.TEXT))
      {
         intrinsicAccessPoints = newArrayList();
         AccessPoint ap = JavaDataTypeUtils.createIntrinsicAccessPoint(BPMN_ESCALATION_CODE, BPMN_ESCALATION_CODE,
               String.class.getName(), Direction.OUT, false, null);
         ap.setAttribute(PredefinedConstants.JMS_LOCATION_PROPERTY, JMSLocation.BODY);

         intrinsicAccessPoints.add(ap);
      }
      return intrinsicAccessPoints;
   }

   @Override
   public Collection<MessageType> getMessageTypes()
   {
      return Collections.singleton(MessageType.TEXT);
   }

   @Override
   public List<Match> getTriggerMatches(Message message)
   {
      return Collections.emptyList();
   }

   private class EscalationMessageMatch implements Match
   {
      private final MessageAcceptor acceptor;

      private final IActivityInstance activityInstance;

      private EscalationMessageMatch(MessageAcceptor acceptor, IActivityInstance activityInstance)
      {
         this.acceptor = acceptor;
         this.activityInstance = activityInstance;
      }

      public void process(AdministrationServiceImpl session, Message message)
      {
         IProcessInstance subProcessInstance = ProcessInstanceBean.findForStartingActivityInstance(activityInstance
               .getOID());

         Map<String, Object> data = acceptor.getData(message, null, null);

         String escalationCode = null != data.get(BPMN_ESCALATION_CODE)
               ? data.get(BPMN_ESCALATION_CODE).toString()
               : "";

         IEventHandler matchingHandler = getMatchingHandler(escalationCode, activityInstance);
         if (null == matchingHandler)
         {
            trace.warn("No matching escalation handler found for activity instance with oid: "
                  + activityInstance.getOID() + " and escalationCode: " + escalationCode);
            return;
         }

         if (isInterrupting(matchingHandler))
         {
            trace.info("Abort Process Instance " + subProcessInstance.getOID() + " due to Escalation Message");

            // TODO - bpmn-2-events - handle concurrency exception / trigger retry
            ProcessInstanceUtils.abortProcessInstance(subProcessInstance);

            trace.debug("Activate boundary path for " + "activity instance = " + activityInstance.getOID());

            activityInstance.lock();
            activityInstance.setPropertyValue(ActivityInstanceBean.BOUNDARY_EVENT_HANDLER_ACTIVATED_PROPERTY_KEY,
                  escalationCode);
            activityInstance.activate();
         }
         else
         {
            trace.info("Trigger NonInterrupting escalation flow due to event message; " + "activity instance = " + activityInstance.getOID());
            try
            {
               // TODO - bpmn-2-events - get rid of copy/paste
               /**
                * copied from EventUtils.triggerNonInterruptingExceptionFlow()
                */
               final IActivity currentActivity = activityInstance.getActivity();
               final ITransition exceptionTransition = currentActivity.getExceptionTransition(matchingHandler.getId());
               final IActivity exceptionFlowActivity = exceptionTransition.getToActivity();

               /* create the token to be consumed ... */
               final TransitionTokenBean exceptionTransitionToken = new TransitionTokenBean(activityInstance.getProcessInstance(), exceptionTransition, activityInstance.getOID());
               exceptionTransitionToken.persist();

               /* ... by the activity thread created */
               ActivityThread.schedule(activityInstance.getProcessInstance(), exceptionFlowActivity, null, false, null, null, false);
            } catch (Exception e) {
               // TODO - bpmn-2-events - review exception handling
               trace.error("Failed processing non interrupting escalation.", e);
            }
         }
      }

      private IEventHandler getMatchingHandler(String escalationCode, IActivityInstance activityInstance)
      {
         for (IEventHandler handler : activityInstance.getActivity().getEventHandlers())
         {
            try
            {
               String hdlEscalationCode = handler.getId(); //getStringAttribute(BPMN_ESCALATION_CODE);
               if (null != hdlEscalationCode && hdlEscalationCode.equals(escalationCode))
                  return handler;
            }
            catch (Exception e)
            {
               // attribute not available
            }
         }
         return null;
      }

      private boolean isInterrupting(IEventHandler handler)
      {
         return EventHandlerBean.BOUNDARY_EVENT_TYPE_INTERRUPTING_VALUE.equals(handler.getStringAttribute(EventHandlerBean.BOUNDARY_EVENT_TYPE_KEY));
      }

   }
}
