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
package org.eclipse.stardust.engine.core.pojo.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.reflect.ResolvedMethod;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class JavaApplicationTypeHelper
{
   /** The name of the access-point resulting from a method's return value. */
   public static final String RETURN_VALUE_ACCESS_POINT_NAME = "returnValue";

   public static Map calculateMethodAccessPoints(Method method,
         String prefix, boolean includeReturnType)
   {
      Map result = CollectionUtils.newMap();

      if (null != method)
      {
         Class[] parameters = method.getParameterTypes();

         for (int i = 0; i < parameters.length; i++)
         {
            String humanName = Reflect.getHumanReadableClassName(parameters[i]);
            String paramName = humanName.toLowerCase().charAt(0) + prefix + (i + 1);
            AccessPoint ap = JavaDataTypeUtils.createIntrinsicAccessPoint(paramName,
                  paramName + " : " + humanName, parameters[i].getName(), Direction.IN,
                  false, JavaAccessPointType.PARAMETER);
            result.put(paramName, ap);
         }

         Class returnType = method.getReturnType();
         if ((Void.TYPE != returnType) && includeReturnType)
         {
            AccessPoint ap = JavaDataTypeUtils.createIntrinsicAccessPoint(RETURN_VALUE_ACCESS_POINT_NAME,
                  "returnValue : " + Reflect.getHumanReadableClassName(returnType), returnType.getName(),
                  Direction.OUT, false, JavaAccessPointType.RETURN_VALUE);
            result.put(RETURN_VALUE_ACCESS_POINT_NAME, ap);
         }
      }
      return result;
   }

   public static Map calculateClassAccessPoints(Class clazz,
         boolean includeGetters, boolean browsableGetters)
   {
      Map result = CollectionUtils.newMap();

      if (null == clazz)
      {
         return result;
      }

      Method[] methods = clazz.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method classMethod = methods[i];
         Class[] parameters = classMethod.getParameterTypes();

         if ((0 == parameters.length) && includeGetters)
         {
            Class rt = classMethod.getReturnType();
            if (Void.TYPE != rt)
            {
               boolean makeBrowsable = browsableGetters
                     && (!rt.isPrimitive() && !rt.isArray());

               String apId = Reflect.encodeMethod(classMethod);

               AccessPoint apGetter = JavaDataTypeUtils.createIntrinsicAccessPoint(
                     apId, Reflect.getSortableMethodName(classMethod),
                     rt.getName(),
                     Direction.OUT, makeBrowsable, JavaAccessPointType.METHOD);
               
               if (apGetter instanceof ModelElement)
               {
                  ((ModelElement) apGetter).setRuntimeAttribute(
                        JavaDataTypeUtils.METHOD_CACHE, Collections.singletonMap(clazz,
                              new ResolvedMethod(classMethod)));
               }
               
               result.put(apId, apGetter);
            }
         }
         else if (1 == parameters.length)
         {
            String apId = Reflect.encodeMethod(classMethod);

            AccessPoint apSetter = JavaDataTypeUtils.createIntrinsicAccessPoint(
                  apId,
                  Reflect.getSortableMethodName(classMethod),
                  parameters[0].getName(),
                  Direction.IN, false, JavaAccessPointType.METHOD);

            if (apSetter instanceof ModelElement)
            {
               ((ModelElement) apSetter).setRuntimeAttribute(
                     JavaDataTypeUtils.METHOD_CACHE, Collections.singletonMap(clazz,
                           new ResolvedMethod(classMethod)));
            }
            
            result.put(apId, apSetter);
         }
      }
      return result;
   }

   public static Map calculateAccessPoints(Class clazz, Method completionMethod,
         boolean includeGetters, boolean browsableGetters)
   {
      Map result = calculateClassAccessPoints(clazz, includeGetters,
            browsableGetters);
      result.putAll(calculateMethodAccessPoints(completionMethod, "Param", true));
      return result;
   }

   public static Map calculateConstructorAccessPoints(Constructor ctor, String prefix)
   {
      Map result = CollectionUtils.newMap();
      if (null != ctor)
      {
         Class[] parameters = ctor.getParameterTypes();

         for (int i = 0; i < parameters.length; i++)
         {
            String humanName = Reflect.getHumanReadableClassName(parameters[i]);
            String paramName = humanName.toLowerCase().charAt(0) + prefix + (i + 1);

            AccessPoint apCtorArg = JavaDataTypeUtils.createIntrinsicAccessPoint(paramName,
                  paramName + " : " + humanName, parameters[i].getName(), Direction.IN,
                  false, JavaAccessPointType.PARAMETER);

            result.put(paramName, apCtorArg);
         }
      }
      return result;
   }
}
