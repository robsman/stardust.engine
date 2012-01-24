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
package org.eclipse.stardust.engine.core.extensions.conditions.expression;

import java.util.Map;

import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ExpressionCondition implements EventHandlerInstance
{
   public void bootstrap(Map attributes)
   {
      // @todo implement
   }

   public boolean accept(Event event)
   {
      // @todo implement
      // @todo (france, ub): check the expression condition
      return false;
   }
}
