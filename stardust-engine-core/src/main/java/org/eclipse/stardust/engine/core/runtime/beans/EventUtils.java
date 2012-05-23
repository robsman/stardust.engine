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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.api.dto.EventBindingDetails;
import org.eclipse.stardust.engine.api.model.EventHandlerOwner;
import org.eclipse.stardust.engine.api.model.EventType;
import org.eclipse.stardust.engine.api.model.IAction;
import org.eclipse.stardust.engine.api.model.IBindAction;
import org.eclipse.stardust.engine.api.model.IEventAction;
import org.eclipse.stardust.engine.api.model.IEventActionType;
import org.eclipse.stardust.engine.api.model.IEventConditionType;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IUnbindAction;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.EventActionBinding;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.TimeStampBinder;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.runtime.utils.PropertyUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventBinder;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventUtils
{
   private static final Logger trace = LogManager.getLogger(EventUtils.class);

   private static final String EVENT_FAILURE_COUNTER = "event.failure.counter";
   private static final String FAILURE_SCOPE = "failure.";
   private static final String ACTION_SCOPE = "action.";
   private static final String HANDLER_SCOPE = "handler.";
   private static int RETRY_FAILURE_MAX = Parameters.instance().getInteger("event.retry.failure", 5);
   public static int DEACTIVE_TYPE = 1024;
   
   public static String getHandlerScope(IEventHandler handler)
   {
      return HANDLER_SCOPE + handler.getElementOID() + ".";
   }

   public static String getActionScope(IAction action)
   {
      return ACTION_SCOPE + action.getElementOID() + ".";
   }

   public static String getFailureScope(EventBindingBean binding)
   {
      return FAILURE_SCOPE + binding.getOID() + ".";
   }   

   public static AttributedIdentifiablePersistent getEventSourceInstance(Event event)
   {
      if (event.getType() == Event.ACTIVITY_INSTANCE)
      {
         return ActivityInstanceBean.findByOID(event.getObjectOID());
      }
      else if (event.getType() == Event.PROCESS_INSTANCE)
      {
         return ProcessInstanceBean.findByOID(event.getObjectOID());
      }
      throw new PublicException("Unknown event type " + event.getType());
   }

   public static EventHandlerOwner getEventSourceDefinition(
         AttributedIdentifiablePersistent eventSourceInstance)
   {
      if (eventSourceInstance instanceof IActivityInstance)
      {
         return ((IActivityInstance) eventSourceInstance).getActivity();
      }
      else if (eventSourceInstance instanceof IProcessInstance)
      {
         return ((IProcessInstance) eventSourceInstance).getProcessDefinition();
      }
      else
      {
         return null;
      }
   }

   public static IProcessInstance getProcessInstance(Event event)
   {
      if (event.getType() == Event.ACTIVITY_INSTANCE)
      {
         IActivityInstance ai = ActivityInstanceBean.findByOID(event.getObjectOID());
         return ai.getProcessInstance();
      }
      else if (event.getType() == Event.PROCESS_INSTANCE)
      {
         return ProcessInstanceBean.findByOID(event.getObjectOID());
      }
      throw new PublicException("Unknown event type " + event.getType());
   }

   public static Event processAutomaticEvent(EventHandlerOwner owner,
         String conditionTypeId, Event event)
   {
      DependentObjectsCache cache = ModelManagerFactory.getCurrent().getDependentCache();

      final AttributedIdentifiablePersistent context = getEventSourceInstance(event);

      for (int i = 0; i < owner.getEventHandlers().size(); ++i)
      {
         IEventHandler handler = (IEventHandler) owner.getEventHandlers().get(i);
         
         if ( !conditionTypeId.equals(handler.getType().getId()))
         {
            continue;
         }
         
         event.setHandlerOID(handler.getOID());

         final EventHandlerInstance condition = cache.getHandlerInstance(handler);
         if (null == condition)
         {
            trace.warn("No handler instance for '" + handler + "', model = "
                  + handler.getModel().getModelOID() + ", oid = " + handler.getOID() + " found.");
            return event;
         }

         if (condition.accept(event))
         {
            if (handler.isLogHandler())
            {
               AuditTrailLogger.getInstance(LogCode.EVENT, context).info(
                     "Processing event " + event + " for handler " + handler);
            }
            for (Iterator j = handler.getAllEventActions(); j.hasNext();)
            {
               IEventAction action = (IEventAction) j.next();

               final EventActionInstance instance = cache.getActionInstance(action);
               if (null == instance)
               {
                  trace.warn("No action instance for '" + action + "', model = "
                        + action.getModel().getModelOID() + ", oid = " + action.getOID()
                        + " found.");
                  break;
               }

//               final Map actionAttributes;
/* todo (rsauer) how about the binding here? If binding attributes are used, don't forget
 * merging between dynamic and static attributes
               if (null != binding)
               {
                  EventActionBinding actionAspect = binding.getBindAction(action.getId());
                  actionAttributes = (null != actionAspect)
                        ? actionAspect.getAllAttributes()
                        : Collections.EMPTY_MAP;
               }
               else
*/               
//               {
//                  actionAttributes = action.getAllAttributes();
//               }
//               instance.bootstrap(actionAttributes, handler.getAllAccessPoints());
               
               try
               {
                  event = instance.execute(event);
               }
               catch (UnrecoverableExecutionException e)
               {
                  final String message = "Failed executing event action '" + action
                        + "' for handler '" + handler + "': " + e.getMessage();

                  AuditTrailLogger.getInstance(LogCode.ENGINE, context).error(message, e);
               }
               // @todo (france, ub): how to handle other exceptions?
            }

            if (handler.isConsumeOnMatch())
            {
               break;
            }
         }
      }
      return event;
   }

   public static void processPullEvent(Event event)
   {
      final AttributedIdentifiablePersistent context = getEventSourceInstance(event);
      
      EventHandlerOwner owner = getEventSourceDefinition(context);
      IEventHandler handler = ModelManagerFactory.getCurrent().findEventHandler(
            owner.getModel().getModelOID(), event.getHandlerOID());
      if (null != handler)
      {
         if (null == context)
         {
            trace.warn("No handler for event " + event);
            return;
         }
         IProcessInstance processInstance = getProcessInstance(event);
         if (processInstance.isAborted())
         {
            detachAll(context); // (fh) Normally should not happen            
            AuditTrailLogger.getInstance(LogCode.EVENT, getEventSourceInstance(event))
                  .warn("Skipping event handling for aborted " + processInstance);
            return;
         }

         EventHandlerInstance handlerInstance = createHandlerInstance(handler);
         final Map handlerAttributes = MapUtils.merge(handler.getAllAttributes(),
               MapUtils.descope(context.getAllPropertyValues(),
                     getHandlerScope(handler)));
         handlerInstance.bootstrap(handlerAttributes);

         if (handlerInstance.accept(event))
         {
            if (handler.isLogHandler())
            {
               AuditTrailLogger.getInstance(LogCode.EVENT, context).info(
                     "Processing event " + event + "for handler " + handler);
            }
            for (Iterator l = handler.getAllEventActions(); l.hasNext();)
            {
               IEventAction action = (IEventAction) l.next();
               EventActionInstance instance = createActionInstance(action);
               final Map actionAttributes = MapUtils.merge(action.getAllAttributes(),
                     MapUtils.descope(context.getAllPropertyValues(),
                           getActionScope(action)));
               instance.bootstrap(actionAttributes, handler.getAllAccessPoints());
               try
               {
                  event = instance.execute(event);
               }
               catch (UnrecoverableExecutionException e)
               {
                  final String message = "Failed executing pull-event action '" + action
                        + "' for handler '" + handler + "': " + e.getMessage();

                  AuditTrailLogger.getInstance(LogCode.ENGINE, context).error(message, e);
               }
               // @todo (france, ub): how to handle other exceptions?                              
               catch (RuntimeException ex)
               {        
            	   
                  checkForDeactivation(context, handler, action);
                  throw ex;
               }
            }
            //clear any event error count in case of sucessfull execution
            clearEventErrorCounter(context, handler);
            unbind(context, handler, null);
         }
      }
      else
      {
         trace.warn("No handler found for oid " + event.getHandlerOID() + " in " + owner);
      }
   }
   
   private static void clearEventErrorCounter(AttributedIdentifiablePersistent context, final IEventHandler handler) {
      int objectType = getEventSourceType(context);
      final EventBindingBean binding = EventBindingBean.find(objectType,
            context.getOID(), handler, SecurityProperties.getPartitionOid());
      if (binding != null)
      {
         String key = getFailureScope(binding) + EVENT_FAILURE_COUNTER;
         Parameters.instance().set(key, null);
      }   
   }

   private static void checkForDeactivation(
	         final AttributedIdentifiablePersistent context, final IEventHandler handler,
	         final IEventAction action)
   {
      ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance().get(
            EngineProperties.FORKING_SERVICE_HOME);
      final IJobManager jobManager = factory.getJobManager();
      // perform all selects/modifications in a separate transaction because
      // setRollbackOnly() can already be called by the ActivityThread
      jobManager.performSynchronousJob(new Procedure()
      {
         protected void invoke()
         {
            int objectType = getEventSourceType(context);
            final EventBindingBean binding = EventBindingBean.find(objectType,
                  context.getOID(), handler, SecurityProperties.getPartitionOid());

            if (binding != null)
            {
               String key = getFailureScope(binding) + EVENT_FAILURE_COUNTER;
               Integer countValue = Parameters.instance().getInteger(key, 0);               
               countValue++;
               if (countValue >= RETRY_FAILURE_MAX)
               {
                  deactivate(context, handler);
                  Parameters.instance().set(key, null);
               }
               else 
               {
                  Parameters.instance().setInteger(key, countValue);
               }
            }
         }
      });
   }

   public static void bind(AttributedIdentifiablePersistent runtimeObject,
         IEventHandler handler, EventHandlerBinding bindParams)
   {
      final int objectType = getEventSourceType(runtimeObject);

      final EventBinder binder = getBinder(handler);
      if (null == binder)
      {
         trace.warn("Unable to retrieve event binder for handler " + handler.getOID()
               + ", skipping event binding.");
         return;
      }
      
      binder.bind(objectType, runtimeObject.getOID(), handler, MapUtils.merge(
            handler.getAllAttributes(),
            (null != bindParams) ? bindParams.getAllAttributes() : null));

      if (null != bindParams)
      {
         // persist dynamic properties, possibly overwriting static ones from model
         final Map handlerAttributes = (bindParams instanceof EventBindingDetails)
               ? ((EventBindingDetails) bindParams).getAllDynamicAttributes()
               : bindParams.getAllAttributes();
         runtimeObject.addPropertyValues(MapUtils.scope(handlerAttributes,
               getHandlerScope(handler)));

         for (Iterator i = handler.getAllEventActions(); i.hasNext();)
         {
            IEventAction action = (IEventAction) i.next();
            EventActionBinding actionBinding = bindParams.getEventAction(action.getId());
            final Map actionAttributes = (actionBinding instanceof EventBindingDetails)
                  ? ((EventBindingDetails) actionBinding).getAllDynamicAttributes()
                  : actionBinding.getAllAttributes();
   
            runtimeObject.addPropertyValues(MapUtils.scope(actionAttributes,
                  getActionScope(action)));
         }
      }

      if (handler.hasBindActions())
      {
         performBindActions(objectType, runtimeObject, handler, bindParams);
      }
   }

   public static void countFailures(final AttributedIdentifiablePersistent context,
         final EventBindingBean binding)
   {
      int cnt_ = 0;
      // check counter and increase  
      // context.lock();
      Integer counter = (Integer) context.getPropertyValue(getFailureScope(binding) + EVENT_FAILURE_COUNTER);
      if(counter != null)
      {
         cnt_ = counter.intValue();
      }
      cnt_++;
      context.setPropertyValue(getFailureScope(binding) + EVENT_FAILURE_COUNTER, Integer.valueOf(cnt_));
   }
      
   public static void deactivate(AttributedIdentifiablePersistent runtimeObject,
         IEventHandler handler)
   {
      final int objectType = getEventSourceType(runtimeObject);

      final EventBinder binder = getBinder(handler);
      if (null != binder)
      {
         binder.deactivate(objectType, runtimeObject.getOID(), handler);
      }
      else
      {
         trace.warn("Unable to retrieve event binder for handler " + handler.getOID()
               + ", skipping event deactivation.");
      }
   }   
   
   public static void unbind(AttributedIdentifiablePersistent runtimeObject,
         IEventHandler handler, EventHandlerBinding bindParams)
   {
      final int objectType = getEventSourceType(runtimeObject);

      final EventBinder binder = getBinder(handler);
      if (null != binder)
      {         
         if (handler.hasUnbindActions())
         {
            // @todo (france, ub): any unbind actions here?
            performUnbindActions(objectType, runtimeObject, handler, bindParams);
         }

         for (Iterator i = handler.getAllEventActions(); i.hasNext();)
         {
            IEventAction action = (IEventAction) i.next();
            PropertyUtils.removePropertyWithPrefix(runtimeObject, getActionScope(action));
         }

         PropertyUtils.removePropertyWithPrefix(runtimeObject, getHandlerScope(handler));

         binder.unbind(objectType, runtimeObject.getOID(), handler);
      }
      else
      {
         trace.warn("Unable to retrieve event binder for handler " + handler.getOID()
               + ", skipping event unbinding.");
      }
   }

   private static EventBinder getBinder(IEventHandler handler)
   {
      String binderClass = handler.getType().getStringAttribute(
            PredefinedConstants.CONDITION_BINDER_CLASS_ATT);
      if (StringUtils.isEmpty(binderClass))
      {
         binderClass = PredefinedConstants.DEFAULT_EVENT_BINDER_CLASS;
      }

      try
      {
         return (EventBinder) Reflect.getInstance(binderClass);
      }
      catch (Exception e)
      {
         throw new InternalException("Instance class '" + binderClass
               + "' for event binder of handler with oid " + handler.getElementOID()
               + ", model oid " + handler.getModel().getModelOID()
               + " cannot be created. Reason: ", e);
      }
   }

   public static EventActionInstance createActionInstance(IAction action)
   {
      IEventActionType type = (IEventActionType) action.getType();
      if (null == type)
      {
         throw new InternalException("Type for event action with oid "
               + action.getElementOID() + ", model oid "
               + action.getModel().getModelOID() + " is null");
      }

      String instanceName = type.getStringAttribute(PredefinedConstants.ACTION_CLASS_ATT);
      if (trace.isDebugEnabled())
      {
         trace.debug("name of event action instance '" + instanceName + "'");
      }

      try
      {
         return (EventActionInstance) Reflect.createInstance(instanceName);
      }
      catch (Exception e)
      {
         throw new InternalException("Instance class '" + instanceName
               + "' for event action with oid " + action.getElementOID() + ", model oid "
               + action.getModel().getModelOID() + " cannot be created. ", e);
      }
   }

   public static EventHandlerInstance createHandlerInstance(IEventHandler handler)
   {
      IEventConditionType type = (IEventConditionType) handler.getType();
      if (null == type)
      {
         throw new InternalException("Type for event handler with oid "
               + handler.getElementOID() + ", model oid "
               + handler.getModel().getModelOID() + " is null");
      }
      String instanceName = type
            .getStringAttribute(PredefinedConstants.CONDITION_CONDITION_CLASS_ATT);
      try
      {
         return (EventHandlerInstance) Reflect.createInstance(instanceName);
      }
      catch (Exception e)
      {
         throw new InternalException("Handler instance '" + instanceName
               + "' for event handler with oid " + handler.getElementOID()
               + ", model oid " + handler.getModel().getModelOID()
               + " cannot be created. Reason: ", e);
      }
   }

   public static boolean isBound(AttributedIdentifiablePersistent runtimeObject,
         IEventHandler handler)
   {
      return null != EventBindingBean.find(getEventSourceType(runtimeObject),
            runtimeObject.getOID(), handler, SecurityProperties.getPartitionOid());
   }

   public static Object getAccessPointValue(AccessPoint accessPoint, Event event,
         Map handlerAttributes)
   {
      if (accessPoint.getBooleanAttribute(PredefinedConstants.EVENT_ACCESS_POINT))
      {
         return event.getAttribute(accessPoint.getId());
      }
      else
      {
         return handlerAttributes.get(accessPoint.getId());
      }
   }

   public static void detachAll(AttributedIdentifiablePersistent runtimeObject)
   {
      final int objectType = getEventSourceType(runtimeObject);

      boolean hasEvents = false;
      
      final EventHandlerOwner owner = getEventSourceDefinition(runtimeObject);
      for (int i = 0; i < owner.getEventHandlers().size(); ++i)
      {
         IEventHandler handler = (IEventHandler) owner.getEventHandlers().get(i);

         // TODO (ellipsis) can the condition for cleaning property scopes defined even tighter?
         hasEvents = true;
         
         if (((IEventConditionType)handler.getType()).getImplementation() != EventType.Engine)
         {
            getBinder(handler).unbind(objectType, runtimeObject.getOID(), handler);
            getBinder(handler).unbind(objectType + DEACTIVE_TYPE, runtimeObject.getOID(), handler);
         }
      }
      
      if (hasEvents)
      {
         PropertyUtils.removePropertyWithPrefix(runtimeObject, HANDLER_SCOPE);
         PropertyUtils.removePropertyWithPrefix(runtimeObject, ACTION_SCOPE);         
         PropertyUtils.removePropertyWithPrefix(runtimeObject, FAILURE_SCOPE);         
      }
   }

   private static int getEventSourceType(IdentifiablePersistent runtimeObject)
   {
      int objectType = 0;
      if (runtimeObject instanceof IActivityInstance)
      {
         objectType = Event.ACTIVITY_INSTANCE;
      }
      else if (runtimeObject instanceof IProcessInstance)
      {
         objectType = Event.PROCESS_INSTANCE;
      }
      else
      {
         Assert.lineNeverReached();
      }
      return objectType;
   }

   private static void performBindActions(int objectType, IdentifiablePersistent source,
         IEventHandler handler, EventHandlerBinding binding)
   {
      Event event = new Event(objectType, source.getOID(), handler.getOID(),
            Event.ENGINE_EVENT);
      // dynamically injecting targetTimestamp attribute
      if (PredefinedConstants.TIMER_CONDITION.equals(handler.getType().getId()))
      {
         event.setAttribute(PredefinedConstants.TARGET_TIMESTAMP_ATT, new Long(
               TimeStampBinder.findTargetTimestamp(objectType, source.getOID(),
                     handler)));
      }

      for (Iterator i = handler.getAllBindActions(); i.hasNext();)
      {
         IBindAction action = (IBindAction) i.next();
         final Map actionAttributes;
         if (null != binding)
         {
            EventActionBinding actionAspect = binding.getBindAction(action.getId());
            actionAttributes = (null != actionAspect)
                  ? actionAspect.getAllAttributes()
                  : Collections.EMPTY_MAP;
         }
         else
         {
            actionAttributes = action.getAllAttributes();
         }

         final EventActionInstance instance = createActionInstance(action);
         if (null != instance)
         {
            instance.bootstrap(actionAttributes, handler.getAllAccessPoints());

            try
            {
               event = instance.execute(event);
            }
            catch (UnrecoverableExecutionException e)
            {
               final String message = "Failed executing event bind-action '" + action
                     + "' for handler '" + handler + "': " + e.getMessage();

               AuditTrailLogger.getInstance(LogCode.ENGINE, source).error(message,
                     e);
            }
         }
         else
         {
            trace.warn("Unable to retrieve event bind action instance for handler "
                  + handler.getOID() + ", skipping action execution.");
         }
      }
   }

   private static void performUnbindActions(int objectType,
         IdentifiablePersistent source, IEventHandler handler, EventHandlerBinding binding)
   {
      Event event = new Event(objectType, source.getOID(), handler.getOID(),
            Event.ENGINE_EVENT);
      // dynamically injecting targetTimestamp attribute
      if (PredefinedConstants.TIMER_CONDITION.equals(handler.getType().getId()))
      {
         event.setAttribute(PredefinedConstants.TARGET_TIMESTAMP_ATT, new Long(
               TimeStampBinder.findTargetTimestamp(objectType, source.getOID(),
                     handler)));
      }

      for (Iterator i = handler.getAllUnbindActions(); i.hasNext();)
      {
         IUnbindAction action = (IUnbindAction) i.next();
         final Map actionAttributes;
         if (null != binding)
         {
            EventActionBinding actionAspect = binding.getBindAction(action.getId());
            actionAttributes = (null != actionAspect)
                  ? actionAspect.getAllAttributes()
                  : Collections.EMPTY_MAP;
         }
         else
         {
            actionAttributes = action.getAllAttributes();
         }

         final EventActionInstance instance = createActionInstance(action);
         if (null != instance)
         {
            instance.bootstrap(actionAttributes, handler.getAllAccessPoints());

            try
            {
               event = instance.execute(event);
            }
            catch (UnrecoverableExecutionException e)
            {
               final String message = "Failed executing event unbind-action '" + action
                     + "' for handler '" + handler + "': " + e.getMessage();

               AuditTrailLogger.getInstance(LogCode.ENGINE, source).error(message,
                     e);
            }
         }
         else
         {
            trace.warn("Unable to retrieve event unbind action instance for handler "
                  + handler.getOID() + ", skipping action execution.");
         }
      }
   }
   
   public static void recoverEvent(AttributedIdentifiablePersistent runtimeObject)
   {
      PropertyUtils.removePropertyWithPrefix(runtimeObject, FAILURE_SCOPE);         

      ResultIterator iterator = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            EventBindingBean.class, // 
            QueryExtension.where(Predicates.andTerm( //
                  Predicates.isEqual(EventBindingBean.FR__OBJECT_OID, runtimeObject.getOID()),//
                  Predicates.greaterThan(EventBindingBean.FR__TYPE, EventUtils.DEACTIVE_TYPE),//
                  Predicates.isEqual(EventBindingBean.FR__PARTITION, SecurityProperties.getPartitionOid()))));
                  
      while(iterator.hasNext())
      {
         EventBindingBean eventBinding = (EventBindingBean) iterator.next();
         int type = eventBinding.getType();
         eventBinding.setType(type - DEACTIVE_TYPE);         
      }
   }
   
   public static long countDeactiveEventBindings()
   {
      // get all from event binding where type > DEACTIVE_TYPE
      ResultIterator iterator = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            EventBindingBean.class, // 
            QueryExtension.where(Predicates.greaterThan(EventBindingBean.FR__TYPE, EventUtils.DEACTIVE_TYPE)));
         
      Set processes = new HashSet();
      while(iterator.hasNext())
      {
         EventBindingBean eventBinding = (EventBindingBean) iterator.next();
         int type = eventBinding.getType() - EventUtils.DEACTIVE_TYPE;
         long objectOID = eventBinding.getObjectOID();
         
         // if activity, get process
         if(type == 1)
         {
            ActivityInstanceBean activityInstanceBean = ActivityInstanceBean.findByOID(objectOID);
            objectOID = activityInstanceBean.getProcessInstance().getOID();                  
         }
         processes.add(objectOID);
      }
      return processes.size();
   }
}