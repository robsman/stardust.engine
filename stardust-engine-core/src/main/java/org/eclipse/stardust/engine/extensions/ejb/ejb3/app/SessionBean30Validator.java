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
package org.eclipse.stardust.engine.extensions.ejb.ejb3.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;


/**
 * 
 * @author herinean
 * @version $Revision$
 */
public class SessionBean30Validator implements ApplicationValidator
{
   private final static String[] methodAttrNames = {
      PredefinedConstants.CREATE_METHOD_NAME_ATT,
      PredefinedConstants.METHOD_NAME_ATT
   };

   @SuppressWarnings("unchecked")
   public List<Inconsistency> validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      ArrayList<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      check(attributes, inconsistencies);
      return inconsistencies;
   }

   private void check(Map<String, String> attributes, ArrayList<Inconsistency> inconsistencies)
   {
      String clazzName = attributes.get(PredefinedConstants.REMOTE_INTERFACE_ATT);
      if (clazzName == null)
      {
         inconsistencies.add(new Inconsistency("Business interface not specified.", Inconsistency.WARNING));
      }
      else
      {
         try
         {
            Class<?> clazz = Reflect.getClassFromClassName(clazzName);
            
            for (int i = 0; i < methodAttrNames.length; i++)
            {
               String methodName = attributes.get(methodAttrNames[i]);
   
               if (methodName != null && methodName.length() > 0)
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
         }
         catch (InternalException e)
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
