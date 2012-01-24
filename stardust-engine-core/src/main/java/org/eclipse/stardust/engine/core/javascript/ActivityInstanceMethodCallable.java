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
package org.eclipse.stardust.engine.core.javascript;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;


public class ActivityInstanceMethodCallable implements Callable
{
   String name;

   Method method;

   ActivityInstance activityInstance;

   public ActivityInstanceMethodCallable(ActivityInstance activityInstance, String name)
   {
      this.name = name;
      this.activityInstance = activityInstance;
      this.method = getMethod(name);
   }

   public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
   {
      Object result = null;
      args = unwrapArgs(args);
      if (method != null)
      {
         try
         {
            result = method.invoke(activityInstance, args);
         }
         catch (SecurityException e)
         {
            e.printStackTrace();
         }
         catch (IllegalArgumentException e)
         {
            e.printStackTrace();
         }
         catch (IllegalAccessException e)
         {
            e.printStackTrace();
         }
         catch (InvocationTargetException e)
         {
            e.printStackTrace();
         }
      }
      else
      {
         if (this.name.equalsIgnoreCase("getAge"))
         {
            Long ms = new Date().getTime() - activityInstance.getStartTime().getTime();
            return ms;            
         }
      }
      if (result instanceof Activity)
      {
         result = new ActivityAccessor((Activity) result);
      }
      return result;
   }

   private Object[] unwrapArgs(Object[] args)
   {
      Object[] result = new Object[args.length];
      for (int i = 0; i < args.length; i++)
      {
         if (args[i] instanceof NativeJavaObject)
         {
            result[i] = ((NativeJavaObject) args[i]).unwrap();
         }
         else
         {
            result[i] = args[i];
         }
      }
      return result;
   }

   private Method getMethod(String name)
   {
      Method[] methods = activityInstance.getClass().getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         if (method.getName().equalsIgnoreCase(name))
         {
            return method;
         }
      }
      return null;
   }

}
