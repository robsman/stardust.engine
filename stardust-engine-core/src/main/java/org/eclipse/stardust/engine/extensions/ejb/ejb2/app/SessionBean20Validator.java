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
         inconsistencies.add(new Inconsistency(interfaceNames[type] +
               " interface not specified.", Inconsistency.WARNING));
      }
      else
      {
         try
         {
            Class clazz = Class.forName(clazzName);
            String methodName = (String) attributes.get(methodAttrNames[type]);

            if (methodName == null)
            {
               inconsistencies.add(new Inconsistency(methodNames[type] +
                     " method not specified.", Inconsistency.WARNING));
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
         }
         catch (ClassNotFoundException e)
         {
            inconsistencies.add(new Inconsistency("Class '" + clazzName
                  + "' not found.", Inconsistency.WARNING));
         }
         catch (NoClassDefFoundError e)
         {
            inconsistencies.add(new Inconsistency("Class '" + clazzName
                  + "' could not be loaded.", Inconsistency.WARNING));
         }
      }
   }
}
