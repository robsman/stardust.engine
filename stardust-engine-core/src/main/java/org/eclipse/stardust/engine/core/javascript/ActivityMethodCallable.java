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

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.query.statistics.evaluation.StatisticsDateUtils;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;


public class ActivityMethodCallable implements Callable
{
   String name;

   Method method;

   Activity activity;

   public ActivityMethodCallable(Activity activity, String name)
   {
      this.name = name;
      this.activity = activity;
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
            result = method.invoke(activity, args);
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
         if (this.name.equalsIgnoreCase("getMeasure"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_MEASURE); 
            result = new String(att);            
         }
         if (this.name.equalsIgnoreCase("getTargetMeasureQuantity"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_TARGET_MEASURE_QUANTITY); 
            result = new Long(att);
         }
         if (this.name.equalsIgnoreCase("getDifficulty"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_DIFFICULTY); 
            result = new Integer(att);
         }
         if (this.name.equalsIgnoreCase("getTargetProcessingTime"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_TARGET_PROCESSING_TIME); 
            Period period = new Period(att);
            Date date = StatisticsDateUtils.periodToDate(period);
            long seconds = date.getTime() / 1000;
            result = new Long(seconds);
         }
         if (this.name.equalsIgnoreCase("getTargetExecutionTime"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_TARGET_EXECUTION_TIME); 
            Period period = new Period(att);
            Date date = StatisticsDateUtils.periodToDate(period);
            long seconds = date.getTime() / 1000;
            result = new Long(seconds);
         }
         if (this.name.equalsIgnoreCase("getTargetIdleTime"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_TARGET_IDLE_TIME); 
            Period period = new Period(att);
            Date date = StatisticsDateUtils.periodToDate(period);
            long seconds = date.getTime() / 1000;
            result = new Long(seconds);
         }
         if (this.name.equalsIgnoreCase("getTargetWaitingTime"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_TARGET_WAITING_TIME); 
            Period period = new Period(att);
            Date date = StatisticsDateUtils.periodToDate(period);
            long seconds = date.getTime() / 1000;
            result = new Long(seconds);
         }
         if (this.name.equalsIgnoreCase("getTargetQueueDepth"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_TARGET_QUEUE_DEPTH); 
            result = new Integer(att);
         }
         if (this.name.equalsIgnoreCase("getTargetCostPerExecution"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_TARGET_COST_PER_EXECUTION); 
            result = new Double(att);
         }
         if (this.name.equalsIgnoreCase("getTargetCostPerSecond"))
         {
            String att = (String) activity.getAttribute(PredefinedConstants.PWH_TARGET_COST_PER_SECOND); 
            result = new Double(att);
         }
         if (this.name.equalsIgnoreCase("getResourcePerformanceCalculation"))
         {
            result = activity.getAttribute(PredefinedConstants.PWH_INCLUDE_TIME); 
         }
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
      Method[] methods = activity.getClass().getMethods();
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
