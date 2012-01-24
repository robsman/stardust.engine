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
package org.eclipse.stardust.engine.core.extensions.conditions.statechange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.stardust.common.IntKey;
import org.eclipse.stardust.engine.api.model.EventHandlerOwner;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.EventConditionValidator;


public class StatechangeConditionValidator implements EventConditionValidator
{
   public Collection validate(EventHandlerOwner context, Map attributes)
   {
      IntKey sourceState = (IntKey) attributes.get(PredefinedConstants.SOURCE_STATE_ATT);
      IntKey targetState = (IntKey) attributes.get(PredefinedConstants.TARGET_STATE_ATT);
      ArrayList list = new ArrayList();
      if (sourceState != null && sourceState == targetState)
      {
         list.add(new Inconsistency("Target State is the same with Source State.",
               Inconsistency.WARNING));
      }
      return list;
   }
}
