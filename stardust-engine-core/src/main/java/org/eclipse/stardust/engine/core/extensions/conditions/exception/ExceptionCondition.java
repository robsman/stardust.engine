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
package org.eclipse.stardust.engine.core.extensions.conditions.exception;

import java.util.Map;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ExceptionCondition implements EventHandlerInstance
{
   private static final Logger trace = LogManager.getLogger(ExceptionCondition.class);

   private Map attributes;

   public void bootstrap(Map attributes)
   {
      this.attributes = attributes;
   }

   public boolean accept(Event event)
   {
      boolean accepted = false;

      Class target = null;
      try
      {
         String targetName = (String) attributes
               .get(PredefinedConstants.EXCEPTION_CLASS_ATT);
         target = Reflect.getClassFromClassName(targetName);
      }
      catch (InternalException e)
      {
         trace.warn("Exception class not available.");
      }

      if (null != target)
      {
         Class exceptionClass = event.getAttribute(PredefinedConstants.EXCEPTION_ATT)
               .getClass();
         accepted = ((null != exceptionClass) && target.isAssignableFrom(exceptionClass));
      }
      
      if (accepted)
      {
         // marking exception caught
         event.setIntendedState(ActivityInstanceState.Completed);
      }

      return accepted;
   }
}
