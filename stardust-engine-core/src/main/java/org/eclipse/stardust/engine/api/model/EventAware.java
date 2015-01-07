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

/**
 * EventAware represents any instance capable of receiving and handling workflow events.
 */
public interface EventAware
{
   /**
    * Gets all event handlers registered with this instance.
    *
    * @return a List of {@link EventHandler} objects.
    */
   List getAllEventHandlers();

   /**
    * Gets the specified event handler.
    *
    * @param id the id of the event handler.
    *
    * @return the event handler requested.
    */
   EventHandler getEventHandler(String id);
}
