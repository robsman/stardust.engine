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
package org.eclipse.stardust.engine.core.extensions.conditions.escalation;

import java.util.Map;

import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;

/**
 * @author Stephane Ruffieux
 * @author Simon Nikles
 *
 */
public class EscalationCondition implements EventHandlerInstance
{

   private Map attributes;

   public void bootstrap(Map attributes)
   {
      this.attributes = attributes;
   }

   public boolean accept(Event event)
   {
      return attributes.containsKey("carnot:engine:escalationCode");
   }
}
