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
package org.eclipse.stardust.engine.spring.extensions.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.api.spring.SpringConstants;
import org.eclipse.stardust.engine.core.pojo.app.PlainJavaValidator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SpringBeanValidator extends PlainJavaValidator
{
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      ArrayList inconsistencies = new ArrayList();
      String className = (String) attributes.get(PredefinedConstants.CLASS_NAME_ATT);

      if (className == null)
      {
         BpmValidationError error = BpmValidationError.JAVA_BEAN_TYPE_NOT_SPECIFIED.raise();
         inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
      }
      else
      {
         try
         {
            Class clazz = Class.forName(className);
            String methodName = (String) attributes.get(PredefinedConstants.METHOD_NAME_ATT);

            if (methodName == null)
            {
               BpmValidationError error = BpmValidationError.JAVA_COMPLETION_METHOD_NOT_SPECIFIED.raise();
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

            String beanId = (String) attributes.get(SpringConstants.ATTR_BEAN_ID);

            if (beanId == null)
            {
               BpmValidationError error = BpmValidationError.JAVA_BEAN_ID_NOT_SPECIFIED.raise();
               inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
            }
         }
         catch (ClassNotFoundException e)
         {
            BpmValidationError error = BpmValidationError.JAVA_CLASS_NOT_FOUND.raise(className);
            inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
         }
         catch (NoClassDefFoundError e)
         {
            BpmValidationError error = BpmValidationError.JAVA_CLASS_COULD_NOT_BE_LOADED.raise(className);
            inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
         }
      }
      return inconsistencies;
   }
}
