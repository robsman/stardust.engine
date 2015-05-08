package org.eclipse.stardust.engine.extensions.events.signal;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;
import static org.eclipse.stardust.engine.core.persistence.QueryExtension.where;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.persistence.OrTerm;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.engine.extensions.jms.app.JMSLocation;
import org.eclipse.stardust.engine.extensions.jms.app.MessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.app.MessageType;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerImpl.Match;
import org.eclipse.stardust.engine.extensions.jms.app.spi.MultiMatchCapable;

/**
 * @author Simon Nikles
 * @author Stéphane Ruffieux
 * @author rsauer
 *
 */
public class SignalMessageAcceptor implements MessageAcceptor, MultiMatchCapable, Stateless
{
   private static final Logger trace = LogManager.getLogger(SignalMessageAcceptor.class);

   private static final String CACHED_SIGNAL_TRIGGERS = SignalMessageAcceptor.class.getName() + ".SignalJmsTriggers";
   private static final String CACHED_SIGNAL_EVENTS = SignalMessageAcceptor.class.getName() + ".SignalEvents";

   public static final String BPMN_SIGNAL_CODE = "carnot:engine:signalCode";

   private static final String BPMN_SIGNAL_PROPERTY_KEY = "stardust.bpmn.signal";

   @Override
   public boolean isStateless()
   {
      return true;
   }

   @Override
   public boolean findMoreMatches(List<Match> matches)
   {
      // match as many as possible
      return true;
   }

   @Override
   public Iterator<IActivityInstance> getMatchingActivityInstances(Message message)
   {
      List<IActivityInstance> result = newArrayList();
      try
      {
         if (message.propertyExists(BPMN_SIGNAL_PROPERTY_KEY))
         {
            String signalName = message.getStringProperty(BPMN_SIGNAL_PROPERTY_KEY);
            trace.info("Accept message " + SendSignalEventAction.SIGNAL_EVENT_TYPE
                     + " for signal name " + signalName + ".");

            OrTerm activityFilter = new OrTerm();

            List<IEventHandler> signalEventHandlers = initializeFromModel();
            for (IEventHandler signalEventHandler : signalEventHandlers)
            {
               if (signalEventHandler.getName().equals(signalName)) {
                  IActivity parentActivity = (IActivity) signalEventHandler.getParent();
                  activityFilter.add(
                        andTerm(
                              isEqual(ActivityInstanceBean.FR__ACTIVITY, ModelManagerFactory.getCurrent().getRuntimeOid(parentActivity)),
                              isEqual(ActivityInstanceBean.FR__MODEL,  parentActivity.getModel().getModelOID())));
               }
            }
            if (activityFilter.getParts().isEmpty())
            {
               return result.iterator();
            }

            Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
            ResultIterator iterator = session.getIterator(ActivityInstanceBean.class,
                                where(andTerm(isEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.HIBERNATED), activityFilter)));

            List<IActivityInstance> activities = newArrayList();
            while(iterator.hasNext()) {

               IActivityInstance activityInstance = (IActivityInstance) iterator.next();
               trace.info("Found signal target: " + activityInstance);
               activities.add(activityInstance);
            }

            iterator.close();

            return activities.iterator();
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
         return new SignalMessageMatch(this, activityInstance);
      }
      return null;
   }

   @Override
   public Map<String, Object> getData(Message message, StringKey id, Iterator accessPoints)
   {
      try
      {
         Object text = ((TextMessage) message).getText();
         return Collections.singletonMap(BPMN_SIGNAL_CODE, text);
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
         AccessPoint ap = JavaDataTypeUtils.createIntrinsicAccessPoint(BPMN_SIGNAL_CODE, BPMN_SIGNAL_CODE,
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

   private static class SignalMessageMatch implements Match
   {
      private final MessageAcceptor acceptor;

      private final IActivityInstance activityInstance;

      private SignalMessageMatch(MessageAcceptor acceptor, IActivityInstance activityInstance)
      {
         this.acceptor = acceptor;
         this.activityInstance = activityInstance;
      }

      public void process(AdministrationServiceImpl session, Message message)
      {
         IProcessInstance subProcessInstance = ProcessInstanceBean.findForStartingActivityInstance(activityInstance
               .getOID());

         Map<String, Object> data = acceptor.getData(message, null, null);

         String escalationCode = null != data.get(BPMN_SIGNAL_CODE)
               ? data.get(BPMN_SIGNAL_CODE).toString()
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
                * copied from CompleteActivityEventAction.execute()
                */
               try
               {
                  new WorkflowServiceImpl().activateAndComplete(activityInstance.getOID(), null,
                        Collections.EMPTY_MAP, true);
               }
               catch (IllegalStateChangeException e)
               {
                  AuditTrailLogger.getInstance(LogCode.EVENT, activityInstance).error(
                        "Unable to complete activity.", e);
               }
            } catch (Exception e) {
               trace.error("Failed processing non interrupting escalation.", e);
            }
         }
      }

      private IEventHandler getMatchingHandler(String signalName, IActivityInstance activityInstance)
      {
         for (IEventHandler handler : activityInstance.getActivity().getEventHandlers())
         {
            try
            {
               if (handler.getAllAttributes().containsKey(BPMN_SIGNAL_PROPERTY_KEY)) {
                  return handler;
               }

               // TODO - bpmn-2-events - signalName
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
         return "Interrupting".equals(handler.getStringAttribute("carnot:engine:event:boundaryEventType"));
      }
   }

   /**
    * Collect triggers and activity-event handlers with a signal declaration.
    */
   private List<IEventHandler> initializeFromModel() {
      if (trace.isDebugEnabled())
      {
         trace.debug("Bootstrapping signal acceptors");
      }

      List<ITrigger> allSignalTriggers = newArrayList();
      List<IEventHandler> allSignalEvents = newArrayList();

      // TODO - bpmn-2-events - for activities we have to consider all versions having running processInstances; For triggers we consider only the active/latest model version
      List<IModel> activeModels = ModelManagerFactory.getCurrent().getModels();
      for (IModel model : activeModels) {
         Object cachedSignalTriggers = model.getRuntimeAttribute(CACHED_SIGNAL_TRIGGERS);
         Object cachedSignalEvents = model.getRuntimeAttribute(CACHED_SIGNAL_EVENTS);

         if (null == cachedSignalTriggers) {
            List<ITrigger> signalTriggersPerModel = newArrayList();
            for (IProcessDefinition processDef : model.getProcessDefinitions()) {
               for (ITrigger trigger : processDef.getTriggers())
               {
                  if (PredefinedConstants.JMS_TRIGGER.equals(trigger.getType()))
                  {
                     if (null != trigger.getAllAttributes() && trigger.getAllAttributes().containsKey(BPMN_SIGNAL_PROPERTY_KEY)) {
                        signalTriggersPerModel.add(trigger);
                     }
                  }
               }
            }
            model.setRuntimeAttribute(CACHED_SIGNAL_TRIGGERS, signalTriggersPerModel);
            allSignalTriggers.addAll((List<ITrigger>) signalTriggersPerModel);
         } else {
            allSignalTriggers.addAll((List<ITrigger>) cachedSignalTriggers);
         }

         if (null == cachedSignalEvents) {
            List<IEventHandler> signalEventsPerModel = newArrayList();
            for (IProcessDefinition processDef : model.getProcessDefinitions()) {
               for (IActivity activity : processDef.getActivities()) {
                  for (IEventHandler event : activity.getEventHandlers()) {
                     if (null != event.getAllAttributes() && event.getAllAttributes().containsKey(BPMN_SIGNAL_PROPERTY_KEY)) {
                        signalEventsPerModel.add(event);
                     }
                  }
               }
            }
            model.setRuntimeAttribute(CACHED_SIGNAL_EVENTS, signalEventsPerModel);
            allSignalEvents.addAll((List<IEventHandler>) signalEventsPerModel);
         } else {
            allSignalEvents.addAll((List<IEventHandler>) cachedSignalEvents);
         }
      }

      return allSignalEvents;
   }

   private Set<Match> findSignalAcceptors(String signal) {
      Set<Match> acceptors = newHashSet();

      acceptors.addAll(findTriggersForSignal(signal));
      acceptors.addAll(findActivitiesForSignal(signal));

      return acceptors;
   }

   private Collection<? extends Match> findActivitiesForSignal(String signal) {
      // TODO - bpmn-2-events - create matches for eventHandlers of activities (as found in model definitions) in active models
      return null;
   }

   private Collection<? extends Match> findTriggersForSignal(String signal) {
      // TODO - bpmn-2-events - create matches for startTriggers
      return null;
   }

}
