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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.EventHandlerOwner;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.EventConditionValidator;


public class ExceptionConditionValidator implements EventConditionValidator
{
   public Collection validate(EventHandlerOwner context, Map attributes)
   {
      ArrayList list = new ArrayList();
      try
      {
         Class o = Reflect.getClassFromClassName((String) attributes.get(
               PredefinedConstants.EXCEPTION_CLASS_ATT));
         if (!Exception.class.isAssignableFrom(o))
         {
            list.add(new Inconsistency(o.getName() + " is not an Exception",
                  Inconsistency.WARNING));
         }
      }
      catch (Exception ex)
      {
         list.add(new Inconsistency(ex.getMessage(), Inconsistency.WARNING));
      }
      catch (NoClassDefFoundError e) 
      {
         list.add(new Inconsistency(e.getMessage(), Inconsistency.WARNING));
      }
      
      return list;
   }
}
