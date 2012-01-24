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

import org.eclipse.stardust.common.StringKey;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventType extends StringKey
{
   public static final EventType Pull =
         new EventType("pull", "pull");
   public static final EventType Push =
         new EventType("push", "push");
   public static final EventType Engine =
         new EventType("engine", "engine");

   private EventType(String id, String defaultName)
   {
      super(id, defaultName);
   }

   public static EventType getKey(String id)
   {
      return (EventType) getKey(EventType.class, id);
   }
}
