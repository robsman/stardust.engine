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
package org.eclipse.stardust.engine.core.pojo.app;

import java.util.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class PlainJavaValidator implements ApplicationValidator
{
   /**
    * Checks if the application has a valid class, method and constructor.
    * 
    * @param attributes
    *           The application context attributes.
    * @param typeAttributes
    *           The application type attributes.
    * @param accessPoints
    * @return A list with all found {@link org.eclipse.stardust.engine.api.model.Inconsistency}
    *         instances.
    */
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      ArrayList inconsistencies = new ArrayList();
      String className = (String) attributes.get(PredefinedConstants.CLASS_NAME_ATT);

      if (className == null)
      {
         inconsistencies.add(new Inconsistency("Java class not specified.",
               Inconsistency.WARNING));
      }
      else
      {
         try
         {
            Class clazz = Reflect.getClassFromClassName(className);
            String methodName = (String) attributes
                  .get(PredefinedConstants.METHOD_NAME_ATT);

            if (methodName == null)
            {
               inconsistencies.add(new Inconsistency("Completion method not specified.",
                     Inconsistency.WARNING));
            }
            else
            {
               try
               {
                  Reflect.decodeMethod(clazz, methodName);
               }
               catch (InternalException e)
               {
                  inconsistencies.add(new Inconsistency("Couldn't find method '"
                        + methodName + "' in class '" + clazz.getName() + "'.",
                        Inconsistency.WARNING));
               }
            }

            String constructorName = (String) attributes
                  .get(PredefinedConstants.CONSTRUCTOR_NAME_ATT);

            if (constructorName == null)
            {
               inconsistencies.add(new Inconsistency("Constructor not specified.",
                     Inconsistency.WARNING));
            }
            else
            {
               try
               {
                  Reflect.decodeConstructor(clazz, constructorName);
               }
               catch (InternalException e)
               {
                  inconsistencies.add(new Inconsistency("Couldn't find constructor '"
                        + constructorName + "' in class '" + clazz.getName() + "'.",
                        Inconsistency.WARNING));
               }
            }
         }
         catch (InternalException e)
         {
            inconsistencies.add(new Inconsistency("Class '" + className + "' not found.",
                  Inconsistency.WARNING));
         }
         catch (NoClassDefFoundError e)
         {
            inconsistencies.add(new Inconsistency("Class '" + className
                  + "' could not be loaded.", Inconsistency.WARNING));
         }
      }
      return inconsistencies;
   }
}
