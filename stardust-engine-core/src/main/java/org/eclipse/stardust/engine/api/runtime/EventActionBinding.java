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

import java.util.Map;
import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.EventAction;


/**
 * Client view of the binding state of an event action. It can be used to retrieve or
 * modify the binding state of the event action.
 */
public interface EventActionBinding extends Serializable
{
   /**
    * Gets all the attributes of the event action type.
    *
    * @return an unmodifiable map with the type attributes.
    */
   Map getAllTypeAttributes();

   /**
    * Gets an attribute of the type of the event action.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getTypeAttribute(String name);

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
    * Removes an attribute.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the removed attribute.
    */
   Object removeAttribute(String name);

   /**
    * Gets the associated event action.
    *
    * @return the event action.
    */
   EventAction getAction();

   /**
    * Gets all the attributes of the event action binding.
    *
    * @return a modifiable Map of attributes.
    */
   Map getAllAttributes();

   /**
    * Gets an attribute.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getAttribute(String name);
}
