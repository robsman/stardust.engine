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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.constants.PlainJavaConstants;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.app.PlainJavaAccessPointProvider;
import org.eclipse.stardust.engine.core.pojo.utils.JavaApplicationTypeHelper;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SpringBeanAccessPointProvider extends PlainJavaAccessPointProvider
{
   private static final Logger trace = LogManager.getLogger(SpringBeanAccessPointProvider.class);

   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      String className = (String) context.get(PredefinedConstants.CLASS_NAME_ATT);
      Class clazz = null;
      try
      {
         clazz = Reflect.getClassFromClassName(className);
      }
      catch (Exception e)
      {
         trace.warn("Couldn't create access points for java type, class '" + className
               + "' not found.");
         return Collections.EMPTY_LIST.iterator();
      }
      catch (NoClassDefFoundError e)
      {
         trace.warn("Couldn't create access points for java type, class '" + className
               + "' could not not be loaded.");
      }

      Map result = JavaApplicationTypeHelper.calculateClassAccessPoints(clazz, true, true);

      String methodName = (String) context.get(PredefinedConstants.METHOD_NAME_ATT);
      try
      {
         Method method = Reflect.decodeMethod(clazz, methodName);

         result.putAll(JavaApplicationTypeHelper.calculateMethodAccessPoints(method,
               PlainJavaConstants.METHOD_PARAMETER_PREFIX, true));
      }
      catch (Exception e)
      {
         trace.warn("", e);
      }

      return result.values().iterator();
   }
}
