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
package org.eclipse.stardust.engine.core.extensions.interactive.contexts.jfc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationContextValidator;


public class JFCValidator implements ApplicationContextValidator
{
   public List validate(Map attributes, Iterator accessPoints)
   {
      List inconsistencies = CollectionUtils.newList();
      String className = (String) attributes.get(PredefinedConstants.CLASS_NAME_ATT);

      if (className == null)
      {
         BpmValidationError error = BpmValidationError.APP_UNSPECIFIED_CLASS_FOR_JFC_APPLICATION.raise();
         inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      else
      {
         try
         {
            Class clazz = Reflect.getClassFromClassName(className);
            String methodName = (String) attributes.get(PredefinedConstants.METHOD_NAME_ATT);

            if (methodName == null)
            {
               BpmValidationError error = BpmValidationError.APP_UNSPECIFIED_COMPLETION_METHOD_FOR_JFC_APPLICATION.raise();
               inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
            }
            else
            {
               try
               {
                  Reflect.decodeMethod(clazz, methodName);
               }
               catch (InternalException e)
               {
                  BpmValidationError error = BpmValidationError.APP_COMPLETION_METHOD_NOT_FOUND.raise(methodName);
                  inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
               }
            }
         }
         catch (InternalException e)
         {
            // inconsistencies.add(new Inconsistency("Couldn't find class '" + className
            //      + "' for JFC application.", Inconsistency.WARNING));

            // JFC classes are not required to be on the server classpath.
         }
         catch (NoClassDefFoundError e)
         {
            //inconsistencies.add(new Inconsistency("Couldn't find class definition for "
            //      + "JFC application.", Inconsistency.WARNING));

            // JFC classes are not required to be on the server classpath.
         }
      }
      return inconsistencies;
   }
}
