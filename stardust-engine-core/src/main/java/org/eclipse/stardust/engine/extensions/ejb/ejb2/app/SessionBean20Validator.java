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
package org.eclipse.stardust.engine.extensions.ejb.ejb2.app;

import java.util.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;


/**
 * @author ubirkemeyer
 * @version $Revision: 52518 $
 */
public class SessionBean20Validator implements ApplicationValidator
{
   private static final int HOME = 0;
   private static final int REMOTE = 1;

   private final static String[] classAttrNames =
   { PredefinedConstants.HOME_INTERFACE_ATT, PredefinedConstants.REMOTE_INTERFACE_ATT };

   private final static String[] interfaceNames =
   { "Home", "Remote" };

   private final static String[] methodAttrNames =
   { PredefinedConstants.CREATE_METHOD_NAME_ATT, PredefinedConstants.METHOD_NAME_ATT };

   private final static String[] methodNames =
   { "Creation", "Completion" };

   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      ArrayList inconsistencies = new ArrayList();
      check(attributes, inconsistencies, REMOTE);
      check(attributes, inconsistencies, HOME);
      return inconsistencies;
   }

   private void check(Map attributes, ArrayList inconsistencies, int type)
   {
      String clazzName = (String) attributes.get(classAttrNames[type]);
      if (clazzName == null)
      {
         BpmValidationError error = BpmValidationError.JAVA_INTERFACE_NOT_SPECIFIED.raise(interfaceNames[type]);
         inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      else
      {
         try
         {
            Class clazz = Reflect.getClassFromClassName(clazzName);
            String methodName = (String) attributes.get(methodAttrNames[type]);

            if (methodName == null)
            {
               BpmValidationError error = BpmValidationError.JAVA_METHOD_NOT_SPECIFIED.raise(methodNames[type]);
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
                  BpmValidationError error = BpmValidationError.JAVA_COULD_NOT_FIND_METHOD_IN_CLASS.raise(
                        methodName, clazz.getName());
                  inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
               }
            }
         }
         catch (InternalException e)
         {
            BpmValidationError error = BpmValidationError.JAVA_CLASS_NOT_FOUND.raise(clazzName);
            inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
         }
         catch (NoClassDefFoundError e)
         {
            BpmValidationError error = BpmValidationError.JAVA_CLASS_COULD_NOT_BE_LOADED.raise(clazzName);
            inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
         }
      }
   }
}
