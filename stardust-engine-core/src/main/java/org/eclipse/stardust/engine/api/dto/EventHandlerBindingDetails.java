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
package org.eclipse.stardust.engine.api.dto;

import java.util.*;

import org.eclipse.stardust.common.MapUtils;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.EventActionBinding;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.core.runtime.beans.AttributedIdentifiablePersistent;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventHandlerBindingDetails extends EventBindingDetails implements EventHandlerBinding
{
   private static final long serialVersionUID = 7599534989353423055L;
   private Map typeAttributes = new HashMap();
   private List actions;
   private List bindActions;
   private List unbindActions;
   private EventHandler handler;
   private boolean bound;

   public EventHandlerBindingDetails(IEventHandler handler)
   {
      super(handler);

      typeAttributes.putAll(handler.getType().getAllAttributes());
      this.handler = new EventHandlerDetails(handler);
      initUnspecialized(handler);
   }

   public EventHandlerBindingDetails(AttributedIdentifiablePersistent runtimeObject,
         IEventHandler handler)
   {
      super(handler);

      IEventConditionType type = (IEventConditionType) handler.getType();
      typeAttributes.putAll(type.getAllAttributes());
      this.handler = new EventHandlerDetails(handler);

      if (type.getImplementation() == EventType.Pull
            && EventUtils.isBound(runtimeObject, handler))
      {
         bound = true;
         initSpecialized(handler, runtimeObject);
      }
      else
      {
         initUnspecialized(handler);
      }
   }

   public Map getAllTypeAttributes()
   {
      return Collections.unmodifiableMap(typeAttributes);
   }

   public Object getTypeAttribute(String name)
   {
      return typeAttributes.get(name);
   }

   public List getAllEventActions()
   {
      return Collections.unmodifiableList(actions);
   }

   public List getAllBindActions()
   {
      return Collections.unmodifiableList(bindActions);
   }

   public List getAllUnbindActions()
   {
      return Collections.unmodifiableList(unbindActions);
   }

   public EventHandler getHandler()
   {
      return handler;
   }

   public EventActionBinding getEventAction(String id)
   {
      for (Iterator i = actions.iterator(); i.hasNext();)
      {
         EventActionBinding action = (EventActionBinding) i.next();
         if (action.getAction().getId().equals(id))
         {
            return action;
         }
      }
      return null;
   }

   public EventActionBinding getBindAction(String id)
   {
      for (Iterator i = bindActions.iterator(); i.hasNext();)
      {
         EventActionBinding action = (EventActionBinding) i.next();
         if (action.getAction().getId().equals(id))
         {
            return action;
         }
      }
      return null;
   }

   public EventActionBinding getUnbindAction(String id)
   {
      for (Iterator i = unbindActions.iterator(); i.hasNext();)
      {
         EventActionBinding action = (EventActionBinding) i.next();
         if (action.getAction().getId().equals(id))
         {
            return action;
         }
      }
      return null;
   }

   public boolean isBound()
   {
      return bound;
   }

   private void initSpecialized(IEventHandler handler,
         AttributedIdentifiablePersistent runtimeObject)
   {
      injectAttributes(MapUtils.descope(
            runtimeObject.getAllPropertyValues(),
            EventUtils.getHandlerScope(handler)));

      bindActions = new ArrayList();
      for (Iterator i = handler.getAllBindActions(); i.hasNext();)
      {
         IAction action = (IAction) i.next();
         bindActions.add(new EventActionBindingDetails(runtimeObject, action));
      }
      actions = new ArrayList();
      for (Iterator i = handler.getAllEventActions(); i.hasNext();)
      {
         IAction action = (IAction) i.next();
         actions.add(new EventActionBindingDetails(runtimeObject, action));
      }
      unbindActions = new ArrayList();
      for (Iterator i = handler.getAllUnbindActions(); i.hasNext();)
      {
         IAction action = (IAction) i.next();
         unbindActions.add(new EventActionBindingDetails(runtimeObject, action));
      }
   }

   private void initUnspecialized(IEventHandler handler)
   {
      actions = DetailsFactory.createCollection(handler.getAllEventActions(),
            IAction.class, EventActionBindingDetails.class);
      bindActions = DetailsFactory.createCollection(handler.getAllBindActions(),
            IAction.class, EventActionBindingDetails.class);
      unbindActions = DetailsFactory.createCollection(handler.getAllUnbindActions(),
            IAction.class, EventActionBindingDetails.class);
   }
}
