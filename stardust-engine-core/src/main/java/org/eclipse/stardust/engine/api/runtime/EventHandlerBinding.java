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
package org.eclipse.stardust.engine.api.runtime;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.EventHandler;


/**
 * Client view of the binding state of an event handler. It can be used to retrieve or
 * modify the binding state of the event handler.
 * <p>The binding state can be retrieved and modified only for event handlers which has
 * been defined as bindable in the model.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface EventHandlerBinding extends Serializable
{
   /**
    * Removes an attribute.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the removed attribute.
    */
   Object removeAttribute(String name);

   /**
    * Sets the value of an attribute.
    *
    * @param name the name of the attribute.
    * @param value the new value of the attribute.
    *
    * @return the previous value of the attribute, or null if the attribute didn't exist.
    */
   Object setAttribute(String name, Object value);

   /**
    * Gets all the attributes of the event handler binding.
    *
    * @return a modifiable Map of attributes.
    */
   Map getAllAttributes();

   /**
    * Gets all the attributes of the event handler type.
    *
    * @return an unmodifiable Map of attributes.
    */
   Map getAllTypeAttributes();

   /**
    * Gets an attribute of the event handler type.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getTypeAttribute(String name);

   /**
    * Gets all event bindings corresponding to the event actions defined for this event
    * handler. These are the actions that are executed when the event is handled.
    *
    * @return a List of {@link EventActionBinding}.
    */
   List getAllEventActions();

   /**
    * Gets all event bindings corresponding to the bind actions defined for this event
    * handler. These are the actions that are executed when the event handler is
    * bound (activated).
    *
    * @return a List of {@link EventActionBinding}.
    */
   List getAllBindActions();

   /**
    * Gets all event bindings corresponding to the unbind actions defined for this event
    * handler. These are the actions performed when the event handler is unbound
    * (deactivated).
    *
    * @return a List of {@link EventActionBinding}.
    */
   List getAllUnbindActions();

   /**
    * Gets the event handler associated with this binding object.
    *
    * @return the associated event handler.
    */
   EventHandler getHandler();

   /**
    * Gets an attribute.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getAttribute(String name);

   /**
    * Gets the event binding corresponding to the specified event action.
    *
    * @param id the name of the event action.
    *
    * @return the corresponding event action binding.
    */
   EventActionBinding getEventAction(String id);

   /**
    * Gets the event binding corresponding to the specified bind action.
    *
    * @param id the name of the bind action.
    *
    * @return the corresponding event binding.
    */
   EventActionBinding getBindAction(String id);

   /**
    * Gets the event binding corresponding to the specified unbind action.
    *
    * @param id the name of the unbind action.
    *
    * @return the corresponding event binding.
    */
   EventActionBinding getUnbindAction(String id);

   /**
    * Gets whether the event handler is bound (activated) or not.
    *
    * @return true if the event handler is bound.
    */
   boolean isBound();
}
