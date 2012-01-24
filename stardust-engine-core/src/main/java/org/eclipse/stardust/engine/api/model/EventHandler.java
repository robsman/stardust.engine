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
package org.eclipse.stardust.engine.api.model;

import java.util.List;
import java.util.Map;

/**
 * A client side view of a workflow event handler.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface EventHandler extends ModelElement
{
   /**
    * Gets the runtime OID of the model element.
    * <p>
    * Contrary to the element OID, runtime element OIDs are guaranteed to be stable over
    * model versions for model elements of same type and identical fully qualified IDs.
    * </p>
    * 
    * <p>
    * The fully qualified ID of a model element consists of the concatenation of the fully
    * qualified element ID of its parent element, if existent, and the element ID.
    * </p>
    * 
    * @return the runtime model element OID
    * 
    * @see ModelElement#getElementOID()
    */
   long getRuntimeElementOID();

   /**
    * Gets all the attributes of the event handler type.
    *
    * @return an unmodifiable Map containing the type attributes.
    */
   Map getAllTypeAttributes();

   /**
    * Gets a specific type attribute.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getTypeAttribute(String name);

   /**
    * Gets all the event actions registered for this event handler.
    *
    * @return a List of {@link EventAction} objects.
    */
   List getAllEventActions();

   /**
    * Gets the specified event action.
    *
    * @param id the id of the event action.
    *
    * @return the corresponding event action.
    */
   EventAction getEventAction(String id);

   /**
    * Gets all the bind actions registered for this event handler.
    *
    * @return a List of {@link EventAction} objects.
    */
   List getAllBindActions();

   /**
    * Gets the specified bind action.
    *
    * @param id the id of the bind action.
    *
    * @return the corresponding bind action.
    */
   EventAction getBindAction(String id);

   /**
    * Gets all the unbind actions registered for this event handler.
    *
    * @return a List of {@link EventAction} objects.
    */
   List getAllUnbindActions();

   /**
    * Gets the specified unbind action.
    *
    * @param id the id of the unbind action.
    *
    * @return the corresponding unbind action.
    */
   EventAction getUnbindAction(String id);
}
