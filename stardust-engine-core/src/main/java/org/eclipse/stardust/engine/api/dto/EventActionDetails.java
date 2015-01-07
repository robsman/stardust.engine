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

import org.eclipse.stardust.engine.api.model.EventAction;
import org.eclipse.stardust.engine.api.model.IAction;


public class EventActionDetails extends ModelElementDetails implements EventAction
{
   private static final long serialVersionUID = -7757684167324274802L;
   private final Map typeAttributes;

   public EventActionDetails(IAction action)
   {
      super(action);

      if (null != action.getType())
      {
         typeAttributes = action.getType().getAllAttributes();
      }
      else
      {
         typeAttributes = Collections.EMPTY_MAP;
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
}
