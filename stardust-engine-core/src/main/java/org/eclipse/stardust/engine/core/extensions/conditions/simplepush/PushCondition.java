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
package org.eclipse.stardust.engine.core.extensions.conditions.simplepush;

import java.util.Map;

import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;


// @todo (france, ub): flesh out
/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class PushCondition implements EventHandlerInstance
{
   public void bootstrap(Map actionAttributes)
   {
      // @todo implement
   }

   public boolean accept(Event event)
   {
      // @todo implement
      return false;
   }
}
