package org.eclipse.stardust.engine.extensions.events.signal;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
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
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IDataMapping;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.OrTerm;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.pojo.data.JavaAccessPoint;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.SignalMessageBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.TriggerDaemon;
import org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
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
   public static final String FIRED_SIGNAL_VALIDITY_DURATION = "carnot:engine:signalCode:firedSignalValidityDuration";

   public static final String BPMN_SIGNAL_PROPERTY_KEY = "stardust.bpmn.signal";

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
         if (message.propertyExists(BPMN_SIGNAL_PROPERTY_KEY) && message instanceof MapMessage)
         {
            String signalName = message.getStringProperty(BPMN_SIGNAL_PROPERTY_KEY);
            trace.info("[Activity Instances] Accept message '" + SendSignalEventAction.SIGNAL_EVENT_TYPE
                     + "' for signal name '" + signalName + "'.");

            OrTerm activityFilter = new OrTerm();

            List<IEventHandler> signalEventHandlers = initializeFromModel().getFirst();
            for (IEventHandler signalEventHandler : signalEventHandlers)
            {
               if (signalEventHandler.getId().equals(signalName)) {
                  IActivity activity = (IActivity) signalEventHandler.getParent();
                  activityFilter.add(
                        andTerm(
                              isEqual(ActivityInstanceBean.FR__ACTIVITY, ModelManagerFactory.getCurrent().getRuntimeOid(activity)),
                              isEqual(ActivityInstanceBean.FR__MODEL,  activity.getModel().getModelOID())));
               }
            }
            if (activityFilter.getParts().isEmpty())
            {
               return result.iterator();
            }

            Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
            ResultIterator<ActivityInstanceBean> iterator = session.getIterator(ActivityInstanceBean.class,
                                where(andTerm(isEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.HIBERNATED), activityFilter)));

            List<IActivityInstance> ais = newArrayList();
            while(iterator.hasNext()) {

               IActivityInstance ai = iterator.next();

               if (matchPredicateData(ai, signalName, message))
               {
                  trace.info("Found signal target: " + ai);
                  ais.add(ai);
               }
            }
            iterator.close();

            return ais.iterator();
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
         return new SignalMessageActivityInstanceMatch(this, activityInstance);
      }
      return null;
   }

   @Override
   public Map<String, Object> getData(Message message, StringKey id, final Iterator accessPoints)
   {
      if ( !(message instanceof MapMessage))
      {
         throw new UnsupportedOperationException("Only MapMessages are supported so far.");
      }
      final MapMessage mapMsg = (MapMessage) message;


      Map<String, Object> data = processMessage(new MessageProcessor<Map<String, Object>>()
      {
         @Override
         public Map<String, Object> process() throws JMSException
         {
            Map<String, Object> result = newHashMap();

            while (accessPoints.hasNext())
            {
               AccessPoint ap = (AccessPoint) accessPoints.next();
               result.put(ap.getId(), mapMsg.getObject(ap.getId()));
            }

            return result;
         }
      });

      return data;
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
      /* this one's never called */
      return Collections.emptySet();
   }

   @Override
   public Collection<MessageType> getMessageTypes()
   {
      return Collections.singleton(MessageType.TEXT);
   }

   @Override
   public List<Match> getTriggerMatches(final Message message)
   {
      return processMessage(new MessageProcessor<List<Match>>()
      {
         @Override
         public List<Match> process() throws JMSException
         {
            List<Match> result = CollectionUtils.newArrayList();

            if ( !message.propertyExists(BPMN_SIGNAL_PROPERTY_KEY))
            {
               return result;
            }

            String signalName = message.getStringProperty(BPMN_SIGNAL_PROPERTY_KEY);
            trace.info("[Signal Triggers] Accept message '" + SendSignalEventAction.SIGNAL_EVENT_TYPE + "' for signal name '" + signalName + "'.");

            List<ITrigger> triggers = initializeFromModel().getSecond();
            for (ITrigger trigger : triggers)
            {
               if (trigger.getId().equals(signalName))
               {
                  result.add(new SignalMessageTriggerMatch(SignalMessageAcceptor.this, trigger));
               }
            }

            return result;
         }
      });
   }

   @Override
   public List<Match> getMessageStoreMatches(final Message message)
   {
      return processMessage(new MessageProcessor<List<Match>>()
      {
         @Override
         public List<Match> process() throws JMSException
         {
            if (message.propertyExists(BPMN_SIGNAL_PROPERTY_KEY))
            {
               return Collections.<Match>singletonList(new MessageStoreMatch());
            }
            else
            {
               return Collections.emptyList();
            }
         }
      });
   }

   /* package-private */ boolean matchPredicateData(IActivityInstance ai, String signalName, Message message)
   {
      String eventContextName = PredefinedConstants.EVENT_CONTEXT + signalName;
      ModelElementList<IDataMapping> inDataMappings = ai.getActivity().getInDataMappings();
      Set<IDataMapping> eventMappings = newHashSet();
      Set<AccessPoint> accessPoints = newHashSet();
      for (IDataMapping dm : inDataMappings)
      {
         if (eventContextName.equals(dm.getContext()))
         {
            eventMappings.add(dm);
            accessPoints.add(new JavaAccessPoint(dm.getId(), dm.getName(), Direction.IN));
         }
      }

      Map<String, Object> data = getData(message, null, accessPoints.iterator());
      if (accessPoints.size() > data.size())
      {
         return false;
      }

      for (IDataMapping dm : eventMappings)
      {
         Object aiDataValue = ai.getProcessInstance().getInDataValue(dm.getData(), dm.getDataPath());
         Object msgDataValue = data.get(dm.getId());

         if (aiDataValue == null)
         {
            continue;
         }

         if ( !aiDataValue.equals(msgDataValue))
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Collect triggers and activity-event handlers with a signal declaration.
    */
   private Pair<List<IEventHandler>,List<ITrigger>> initializeFromModel() {
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
                  if (PredefinedConstants.JMS_TRIGGER.equals(trigger.getType().getId()))
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

      return new Pair<List<IEventHandler>,List<ITrigger>>(allSignalEvents, allSignalTriggers);
   }

   private static <T> T processMessage(MessageProcessor<T> processor)
   {
      try
      {
         return processor.process();
      }
      catch (JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e);
      }
   }

   private static interface MessageProcessor<T>
   {
      public T process() throws JMSException;
   }

   /* package-private */ static class SignalMessageActivityInstanceMatch implements Match
   {
      private final MessageAcceptor acceptor;

      private final IActivityInstance activityInstance;

      /* package-private */ SignalMessageActivityInstanceMatch(MessageAcceptor acceptor, IActivityInstance activityInstance)
      {
         this.acceptor = acceptor;
         this.activityInstance = activityInstance;
      }

      public void process(AdministrationServiceImpl session, final Message message)
      {
         String signalName = processMessage(new MessageProcessor<String>()
         {
            @Override
            public String process() throws JMSException
            {
               return message.getStringProperty(BPMN_SIGNAL_PROPERTY_KEY);
            }
         });

         IEventHandler matchingHandler = getMatchingHandler(signalName, activityInstance);
         if (null == matchingHandler)
         {
            trace.warn("No matching escalation handler found for activity instance with oid: "
                  + activityInstance.getOID() + " and escalationCode: " + signalName);
            return;
         }

         trace.info("Catching signal event message: signal name = '" + signalName + "', activity instance OID = '" + activityInstance.getOID() + "'.");
         try
         {
            String eventContextName = PredefinedConstants.EVENT_CONTEXT + signalName;
            Map<String, ?> outData = retrieveOutData(message, eventContextName);

            try
            {
               new WorkflowServiceImpl().activateAndComplete(activityInstance.getOID(), eventContextName, outData, true);
            }
            catch (IllegalStateChangeException e)
            {
               AuditTrailLogger.getInstance(LogCode.EVENT, activityInstance).error("Unable to complete activity.", e);
            }
         } catch (Exception e) {
            trace.error("Failed processing non interrupting escalation.", e);
         }
      }

      private IEventHandler getMatchingHandler(String signalName, IActivityInstance activityInstance)
      {
         for (IEventHandler handler : activityInstance.getActivity().getEventHandlers())
         {
            try
            {
               if (handler.getAllAttributes().containsKey(BPMN_SIGNAL_PROPERTY_KEY))
               {
                  if (handler.getId().equals(signalName))
                  {
                     return handler;
                  }
               }
            }
            catch (Exception e)
            {
               // attribute not available
            }
         }
         return null;
      }

      private Map<String, ?> retrieveOutData(Message message, String eventContextName)
      {
         Map<String, Object> result = CollectionUtils.newHashMap();

         ModelElementList<IDataMapping> outDataMappings = activityInstance.getActivity().getOutDataMappings();
         Set<IDataMapping> eventMappings = newHashSet();
         Set<AccessPoint> accessPoints = newHashSet();
         for (IDataMapping dm : outDataMappings)
         {
            if (eventContextName.equals(dm.getContext()))
            {
               eventMappings.add(dm);
               accessPoints.add(new JavaAccessPoint(dm.getId(), dm.getName(), Direction.OUT));
            }
         }

         Map<String, Object> data = acceptor.getData(message, null, accessPoints.iterator());
         for (IDataMapping dm : eventMappings)
         {
            result.put(dm.getId(), data.get(dm.getId()));
         }

         return result;
      }
   }

   private static class SignalMessageTriggerMatch implements Match
   {
      private final MessageAcceptor acceptor;

      private final ITrigger trigger;

      private SignalMessageTriggerMatch(MessageAcceptor acceptor, ITrigger trigger)
      {
         this.acceptor = acceptor;
         this.trigger = trigger;
      }

      @Override
      public void process(AdministrationServiceImpl session, Message message)
      {
         WorkflowServiceImpl wfs = new WorkflowServiceImpl();

         IProcessDefinition processDef = (IProcessDefinition) trigger.getParent();
         Map<String, ?> msgData = acceptor.getData(message, null, trigger.getAllOutAccessPoints());
         Map<String, ?> data = TriggerDaemon.performParameterMapping(trigger, msgData);

         wfs.startProcess(processDef, data, trigger.isSynchronous());
      }
   }

   private static class MessageStoreMatch implements Match
   {
      @Override
      public void process(AdministrationServiceImpl session, Message message)
      {
         /* create message store entry */
         new SignalMessageBean(SecurityProperties.getPartitionOid(), (MapMessage) message);

         /* create message store index based on currently used predicate data */
         // TODO - bpmn-2-events - create message store index based on currently used predicate data
      }
   }
}
