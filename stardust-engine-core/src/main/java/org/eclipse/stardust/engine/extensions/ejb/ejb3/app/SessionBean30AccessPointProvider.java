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
import org.eclipse.stardust.engine.extensions.ejb.SessionBeanConstants;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SessionBean30AccessPointProvider implements AccessPointProvider
{
   private static final Logger trace = LogManager
         .getLogger(SessionBean30AccessPointProvider.class);

   @SuppressWarnings("unchecked")
   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      String className = (String) context.get(PredefinedConstants.REMOTE_INTERFACE_ATT);
      Class clazz = null;
      try
      {
         clazz = Reflect.getClassFromClassName(className);
      }
      catch (Exception e)
      {
         trace.warn("Couldn't create access points for session bean, class '" + className + "' not found.");
         return Collections.EMPTY_LIST.iterator();
      }
      catch (NoClassDefFoundError e)
      {
         trace.warn("Couldn't create access points for session bean, class '" + className + "' could not be loaded.");
         return Collections.EMPTY_LIST.iterator();
      }

      Map result = JavaApplicationTypeHelper.calculateClassAccessPoints(clazz,
            true, true);

      String methodName = (String) context.get(PredefinedConstants.METHOD_NAME_ATT);
      if (methodName != null && methodName.length() > 0)
      {
         try
         {
            Method method = Reflect.decodeMethod(clazz, methodName);
   
            result.putAll(JavaApplicationTypeHelper.calculateMethodAccessPoints(method,
                  SessionBeanConstants.METHOD_PARAMETER_PREFIX, true));
         }
         catch (Exception e)
         {
            trace.warn("Couldn't decode method '" + methodName
                  + "' on class '" + className + "': " + e.getMessage());
         }
      }

      String createMethodName = (String) context.get(PredefinedConstants.CREATE_METHOD_NAME_ATT);
      if (createMethodName != null && createMethodName.length() > 0)
      {
         try
         {
            Method creationMethod = Reflect.decodeMethod(clazz, createMethodName);
      
            result.putAll(JavaApplicationTypeHelper.calculateMethodAccessPoints(
                  creationMethod,
                  SessionBeanConstants.CREATION_METHOD_PARAMETER_PREFIX, false));
         }
         catch (Exception e)
         {
            trace.warn("Couldn't decode method '" + createMethodName
                  + "' on class '" + className + "': " + e.getMessage());
         }
      }

      return result.values().iterator();
   }
}
