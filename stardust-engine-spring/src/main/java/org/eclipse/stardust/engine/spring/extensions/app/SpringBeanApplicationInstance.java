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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.spring.SpringConstants;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.utils.JavaAccessPointType;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.springframework.context.ApplicationContext;


/**
 * ApplicationInstance implementation for Spring Beans
 * 
 * @author rsauer
 * @version $Revision$
 */
public class SpringBeanApplicationInstance implements SynchronousApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(SpringBeanApplicationInstance.class);

   private ActivityInstance activityInstance;
   
   private List accessPointValues = new ArrayList();

   private List outDataMappingOrder = new ArrayList();

   private Application application;

   private Class theType;

   private Object theObject;

   private Object lastReturnValue;

   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }

   public void bootstrap(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
      
      application = activityInstance.getActivity().getApplication();

      theType = Reflect.getClassFromClassName(
            (String) application.getAttribute(PredefinedConstants.CLASS_NAME_ATT));
      
      for (Iterator i = activityInstance.getActivity()
            .getApplicationContext(PredefinedConstants.APPLICATION_CONTEXT)
            .getAllOutDataMappings()
            .iterator(); i.hasNext();)
      {
         DataMapping mapping = (DataMapping) i.next();
         outDataMappingOrder.add(mapping.getApplicationAccessPoint().getId());
      }
   }

   public void setInAccessPointValue(String name, Object value)
   {
      Pair param = findAccessPointValue(name);
      if (null != param)
      {
         accessPointValues.remove(param);
      }
      accessPointValues.add(new Pair(name, value));
   }

   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      // reset from possible previous execution
      try
      {
         lastReturnValue = null;
   
         createObject();
   
         doSetInAccessPointValues();
   
         Method method = Reflect.decodeMethod(theType,
               (String) application.getAttribute(PredefinedConstants.METHOD_NAME_ATT));
   
         Object[] parameters = new Object[method.getParameterTypes().length];
   
         Class[] parameterTypes = method.getParameterTypes();
         for (int i = 0; i < parameterTypes.length; i++ )
         {
            String humanName = Reflect.getHumanReadableClassName(parameterTypes[i]);
            String paramName = humanName.toLowerCase().charAt(0) + "Param" + (i + 1);
            Pair param = findAccessPointValue(paramName);
            try
            {
               parameters[i] = Reflect.castValue((null != param) ? param.getSecond() : null,
                     parameterTypes[i]);
            }
            catch (InvalidValueException e)
            {
               throw new InvocationTargetException(e, "Failed to apply IN parameter '"
                     + paramName + "' for java method '" + Reflect.encodeMethod(method)
                     + "'.");
            }
         }
   
         try
         {
            lastReturnValue = method.invoke(theObject, parameters);
         }
         catch (InvocationTargetException e)
         {
            trace.warn("Failed to invoke Spring Bean method '" + Reflect.encodeMethod(method)
                  + "'.", e.getTargetException());
            throw e;
         }
         catch (Exception e)
         {
            trace.warn("Failed to invoke Spring Bean method '"
                  + Reflect.encodeMethod(method) + "'.", e);
            throw new InvocationTargetException(e, "Failed to invoke Spring Bean method "
                  + Reflect.encodeMethod(method) + ".");
         }
   
         return doGetOutAccessPointValues(outDataTypes);
      }
      catch (InvocationTargetException e) 
      {
         throw e;
      }
      catch (Exception e) 
      {
         throw new InvocationTargetException(e);
      }
   }

   public Object getOutAccessPointValue(String name)
   {
      try
      {
         return doGetOutAccessPointValue(name, false);
      }
      catch (InvocationTargetException e)
      {
         throw new InternalException(e.getMessage(), e.getTargetException());
      }
   }

   public void cleanup()
   {
      this.activityInstance = null;
   }

   /**
    * Obtain bean instance from spring app context
    */
   private void createObject()
   {
      ApplicationContext appContext = (ApplicationContext) Parameters.instance().get(
            SpringConstants.PRP_APPLICATION_CONTEXT);
      
      if (null == appContext)
      {
         // if no thread local application context was bound, we are not in a pure Spring
         // environment (i.e. EJB deployment calling Spring services), so use global
         // Spring context
         appContext = SpringUtils.getApplicationContext();
         
         if (null == appContext)
         {
            throw new PublicException("No Spring application context available.");
         }
      }

      String beanId = (String) application.getAttribute(SpringConstants.ATTR_BEAN_ID);

      try
      {

         theObject = appContext.getBean(beanId);
      }
      catch (Exception x)
      {
         throw new PublicException("Cannot retrieve bean.", x);
      }
   }

   private void doSetInAccessPointValues() throws InvocationTargetException
   {
      for (Iterator i = accessPointValues.iterator(); i.hasNext();)
      {
         Pair entry = (Pair) i.next();
         String name = (String) entry.getFirst();
         Object value = entry.getSecond();
         AccessPoint accessPoint = application.getAccessPoint(name);
         Assert.isNotNull(accessPoint, "Access point '" + name + "' does not exist, "
               + theType);
         Object characteristics = accessPoint.getAttribute(PredefinedConstants.FLAVOR_ATT);
         if (JavaAccessPointType.METHOD.equals(characteristics))
         {
            try
            {
               JavaDataTypeUtils.evaluate(name, theObject, value);
            }
            catch (InvocationTargetException e)
            {
               trace.warn("Failed to invoke setter '" + name + "'.",
                     e.getTargetException());
               throw e;
            }
            catch (Exception e)
            {
               trace.warn("", e);
               throw new InvocationTargetException(e, "Failed setting session-bean in "
                     + "access-point '" + name + "'.");
            }
         }
      }
   }

   private Map doGetOutAccessPointValues(Set outDataTypes)
         throws InvocationTargetException
   {
      Map result = new HashMap();

      for (Iterator i = outDataMappingOrder.iterator(); i.hasNext();)
      {
         String name = (String) i.next();
         if (outDataTypes.contains(name))
         {
            result.put(name, doGetOutAccessPointValue(name, true));
         }
      }
      return result;
   }

   private Object doGetOutAccessPointValue(String name, boolean allowReturnValue)
         throws InvocationTargetException
   {
      Object characteristics = application.getAccessPoint(name).getAttribute(
            PredefinedConstants.FLAVOR_ATT);

      if (allowReturnValue && JavaAccessPointType.RETURN_VALUE.equals(characteristics))
      {
         return lastReturnValue;
      }
      else if (JavaAccessPointType.METHOD.equals(characteristics))
      {
         try
         {
            return JavaDataTypeUtils.evaluate(name, theObject);
         }
         catch (InvocationTargetException e)
         {
            trace.warn("Failed to invoke getter '" + name + "'.", e.getTargetException());
            throw e;
         }
         catch (Exception e)
         {
            trace.warn("", e);
            throw new InvocationTargetException(e, "Failed retrieving session-bean out "
                  + "access-point '" + name + "'.");
         }
      }
      else
      {
         throw new InternalException("Unknown characteristics '" + characteristics
               + "' for access point '" + name + "'.");
      }
   }

   private Pair findAccessPointValue(String name)
   {
      Pair result = null;
      for (Iterator i = accessPointValues.iterator(); i.hasNext();)
      {
         Pair entry = (Pair) i.next();
         if (name.equals(entry.getFirst()))
         {
            result = entry;
            break;
         }
      }
      return result;
   }
}
