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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.utils.JavaApplicationTypeHelper;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;


public class JFCAccessPointProvider implements AccessPointProvider
{
   public static final Logger trace =
         LogManager.getLogger(JFCAccessPointProvider.class);

   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      String className = (String) context.get(PredefinedConstants.CLASS_NAME_ATT);
      Class clazz = null;
      try
      {
         clazz = Class.forName(className);
      }
      catch (ClassNotFoundException e)
      {
         // just ignore if JFC class is not present on the server claspath;
         return Collections.emptyList().iterator();
      }
      catch (NoClassDefFoundError e)
      {
         trace.warn("Could not load class: "+className, e);
      }
      catch (NullPointerException e)
      {
         trace.warn("", e);
      }

      Map result = JavaApplicationTypeHelper
            .calculateClassAccessPoints(clazz, true, true);
      String methodName = (String) context.get(PredefinedConstants.METHOD_NAME_ATT);
      try
      {
         Method method = Reflect.decodeMethod(clazz, methodName);
         result.putAll(JavaApplicationTypeHelper.calculateMethodAccessPoints(
               method, JFCConstants.METHOD_PARAMETER_PREFIX, true));
      }
      catch (Exception e)
      {
         trace.warn("", e);
      }
      return result.values().iterator();
   }
}
