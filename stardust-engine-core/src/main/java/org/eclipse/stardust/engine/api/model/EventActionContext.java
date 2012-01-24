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
 * @author fherinean
 * @version $Revision$
 */
public class EventActionContext extends StringKey
{
   public static final EventActionContext Bind =
         new EventActionContext("bind", "Bind Action", "BindAction");
   public static final EventActionContext Event =
         new EventActionContext("event", "Event Action", "EventAction");
   public static final EventActionContext Unbind =
         new EventActionContext("unbind", "Unbind Action", "UnbindAction");

   private String prefix;

   private EventActionContext(String id, String defaultName, String prefix)
   {
      super(id, defaultName);
      this.prefix = prefix;
   }

   public static EventActionContext getKey(String id)
   {
      return (EventActionContext) getKey(EventActionContext.class, id);
   }

   public String getPrefix()
   {
      return prefix;
   }

   public int getIntValue()
   {
      int z = 1; // event;
      if (this == Bind)
      {
         z = 0; // bind
      }
      else if (this == Unbind)
      {
         z = 2; // unbind
      }
      return z;
   }
}
