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

import org.eclipse.stardust.common.CollectionUtils;
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
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQueryEvaluator;
import org.eclipse.stardust.engine.api.query.EvaluationContext;
import org.eclipse.stardust.engine.api.query.ProcessQueryPostprocessor;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.persistence.OrTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;
import org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.extensions.events.AbstractThrowEventAction;
import org.eclipse.stardust.engine.extensions.events.escalation.ThrowEscalationEventAction;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.engine.extensions.jms.app.JMSLocation;
import org.eclipse.stardust.engine.extensions.jms.app.MessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.app.MessageType;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerImpl.Match;
import org.eclipse.stardust.engine.extensions.jms.app.spi.MultiMatchCapable;

import com.hazelcast.query.Predicate;

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
         if (message.propertyExists("stardust.bpmn.signal"))
         {
            String signalName = message.getStringProperty("stardust.bpmn.signal");
            //               if (trace.isDebugEnabled())
            {
               trace.info("Accept message " + SendSignalEventAction.SIGNAL_EVENT_TYPE
                     + " for signal name " + signalName + ".");
            }

            OrTerm activityFilter = new OrTerm();
            
            List<IEventHandler> signalEventHandlers = initializeFromModel();
            for (IEventHandler signalEventHandler : signalEventHandlers)
            {
               if (signalEventHandler.getName().equals(signalName)) {
                  IActivity parentActivity = (IActivity) signalEventHandler.getParent();
                  activityFilter.add(
                        andTerm(
                              isEqual(ActivityInstanceBean.FR__ACTIVITY, ModelManagerFactory.getCurrent().getRuntimeOid(parentActivity)), 
                              isEqual(ActivityInstanceBean.FR__MODEL,  parentActivity.getModel().getOID())));
               }
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
      }
      catch (JMSException e)
      {
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

            // TODO handle concurrency exception / trigger retry
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

            /*
             * This doesn't work because the subprocess-activity is in state 'suspended';
             *
            ActivityThread at = new ActivityThread(null, null, activityInstance, null, data, false);
            activityInstance.setPropertyValue(ActivityInstanceBean.BOUNDARY_EVENT_HANDLER_ACTIVATED_PROPERTY_KEY,
                  escalationCode);
            try
            {
               at.run();
            }
            finally
            {
               activityInstance.removeProperty(ActivityInstanceBean.BOUNDARY_EVENT_HANDLER_ACTIVATED_PROPERTY_KEY);
            }*/

         }
      }

      private IEventHandler getMatchingHandler(String signalName, IActivityInstance activityInstance)
      {
         for (IEventHandler handler : activityInstance.getActivity().getEventHandlers())
         {
            try
            {
               if (handler.getAllAttributes().containsKey("stardust.bpmn.signal")) {
                  return handler;
               }
               
               // TODO signalName
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
         // <carnot:Attribute Name= Value="Non-interrupting"/>
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

      List<ITrigger> signalTriggers = newArrayList();
      List<IEventHandler> signalEvents = newArrayList();

      // TODO for activities we have to consider all versions having running processInstances; For triggers we consider only the active/latest model version
      List<IModel> activeModels = ModelManagerFactory.getCurrent().getModels();
      for (IModel model : activeModels) {
         Object cachedSignalTriggers = model.getRuntimeAttribute(CACHED_SIGNAL_TRIGGERS);
         Object cachedSignalEvents = model.getRuntimeAttribute(CACHED_SIGNAL_EVENTS);
         if (null == cachedSignalTriggers || null == cachedSignalEvents) {
            for (IProcessDefinition processDef : model.getProcessDefinitions()) {
               if (null != cachedSignalTriggers) {
                  signalTriggers = (List<ITrigger>) cachedSignalTriggers;
               } else {
                  for (ITrigger trigger : processDef.getTriggers())
                  {
                     if (PredefinedConstants.JMS_TRIGGER.equals(trigger.getType()))
                     {
                        if (null != trigger.getAllAttributes() && trigger.getAllAttributes().containsKey("stardust.bpmn.signal")) {
                           signalTriggers.add(trigger);
                        }
                     }
                  }
               }
               if (null != cachedSignalEvents) {
                  signalEvents = (List<IEventHandler>) cachedSignalEvents;
               } else {
                  for (IActivity activity : processDef.getActivities()) {
                     for (IEventHandler event : activity.getEventHandlers()) {
                        if (null != event.getAllAttributes() && event.getAllAttributes().containsKey("stardust.bpmn.signal")) {
                           signalEvents.add(event);
                        }
                     }
                  }
               }
            }
            model.setRuntimeAttribute(CACHED_SIGNAL_TRIGGERS, signalTriggers);
            model.setRuntimeAttribute(CACHED_SIGNAL_EVENTS, signalEvents);
         }
      }
      
      return signalEvents;
   }

   private Set<Match> findSignalAcceptors(String signal) {
      Set<Match> acceptors = newHashSet();

      acceptors.addAll(findTriggersForSignal(signal));
      acceptors.addAll(findActivitiesForSignal(signal));

      return acceptors;
   }

   private Collection<? extends Match> findActivitiesForSignal(String signal) {
      // TODO create matches for eventHandlers of activities (as found in model definitions) in active models
      return null;
   }

   private Collection<? extends Match> findTriggersForSignal(String signal) {
      // TODO create matches for startTriggers
      return null;
   }

}
