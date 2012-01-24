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

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.MapUtils;
import org.eclipse.stardust.engine.api.model.EventAction;
import org.eclipse.stardust.engine.api.model.IAction;
import org.eclipse.stardust.engine.api.model.IEventActionType;
import org.eclipse.stardust.engine.api.runtime.EventActionBinding;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AttributedIdentifiablePersistent;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;


public class EventActionBindingDetails extends EventBindingDetails implements EventActionBinding
{
   private static final long serialVersionUID = -1527423684585883463L;
   private final Map typeAttributes;
   private EventAction action;

   public EventActionBindingDetails(IAction action)
   {
      super(action, null, (null == action.getDescription())
            ? ModelUtils.nullSafeGetName((IEventActionType) action.getType())
            : action.getDescription());

      if (null != action.getType())
      {
         typeAttributes = action.getType().getAllAttributes();
      }
      else
      {
         typeAttributes = Collections.EMPTY_MAP;
      }
      this.action = new EventActionDetails(action);
   }

   public EventActionBindingDetails(AttributedIdentifiablePersistent runtimeObject, 
         IAction action)
   {
      super(action, null, (null == action.getDescription())
            ? ModelUtils.nullSafeGetName((IEventActionType) action.getType())
            : action.getDescription());

      injectAttributes(MapUtils.descope(
            runtimeObject.getAllPropertyValues(),
            EventUtils.getActionScope(action)));

      if (null != action.getType())
      {
         typeAttributes = action.getType().getAllAttributes();
      }
      else
      {
         typeAttributes = Collections.EMPTY_MAP;
      }
      this.action = new EventActionDetails(action);
   }

   public Map getAllTypeAttributes()
   {
      return Collections.unmodifiableMap(typeAttributes);
   }

   public Object getTypeAttribute(String name)
   {
      return typeAttributes.get(name);
   }

   public EventAction getAction()
   {
      return action;
   }
}
