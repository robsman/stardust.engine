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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.utils.ejb.EJBUtils;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.utils.JavaAccessPointType;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.ejb.SessionBeanConstants;


/**
 * 
 * @author herinean
 * @version $Revision$
 */
public class SessionBean30ApplicationInstance implements SynchronousApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(SessionBean30ApplicationInstance.class);

   private List<Pair> accessPointValues = new ArrayList<Pair>();
   private List<String> outDataMappingOrder = new ArrayList<String>();

   private Application application;
   private Class<?> remoteInterfaceClass;
   private String jndiPath;
   private Object sessionBean;

   private Object lastReturnValue;

   @SuppressWarnings("unchecked")
   public void bootstrap(ActivityInstance activityInstance)
   {
      application = activityInstance.getActivity().getApplication();

      jndiPath = (String) application.getAttribute(PredefinedConstants.JNDI_PATH_ATT);
      remoteInterfaceClass = Reflect.getClassFromClassName(
            (String) application.getAttribute(PredefinedConstants.REMOTE_INTERFACE_ATT));

      for (Iterator<DataMapping> i = activityInstance.getActivity()
            .getApplicationContext(PredefinedConstants.APPLICATION_CONTEXT)
            .getAllOutDataMappings()
            .iterator(); i.hasNext();)
      {
         DataMapping mapping = i.next();
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

   public Object getOutAccessPointValue(String name)
   {
      // hint: no returnValue access-point here allowed because the getOutAccessPointValue
      // method is only for processing in data mappings.

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
      sessionBean = null;
   }

   @SuppressWarnings("unchecked")
   public Map<String, ?> invoke(Set outDataTypes) throws InvocationTargetException
   {
      try
      {
         doCreateSessionBean();
   
         String creationMethodName = (String) application.getAttribute(PredefinedConstants.CREATE_METHOD_NAME_ATT);
         executeMethod(creationMethodName, SessionBeanConstants.CREATION_METHOD_PARAMETER_PREFIX);
   
         doSetInAccessPointValues();
         
         lastReturnValue = null;
   
         String completionMethodName = (String) application.getAttribute(PredefinedConstants.METHOD_NAME_ATT);
         executeMethod(completionMethodName, SessionBeanConstants.METHOD_PARAMETER_PREFIX);
   
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

   private void executeMethod(String methodName, String parameterPrefix) throws InvocationTargetException
   {
      if (methodName == null || methodName.length() == 0)
      {
         return;
      }
      
      Method method = Reflect.decodeMethod(remoteInterfaceClass, methodName);

      Object[] parameters = new Object[method.getParameterTypes().length];

      Class<?>[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++)
      {
         String humanName = Reflect.getHumanReadableClassName(parameterTypes[i]);
         String paramName = humanName.toLowerCase().charAt(0) + parameterPrefix + (i + 1);
         Pair param = findAccessPointValue(paramName);
         try
         {
            parameters[i] = Reflect.castValue((null != param) ? param.getSecond() : null,
                  parameterTypes[i]);
         }
         catch (InvalidValueException e)
         {
            String msg = MessageFormat.format(
                  "Failed to apply IN parameter '{0}' for session bean method '{1}'.",
                  paramName, Reflect.encodeMethod(method));
            throw new InvocationTargetException(e, msg);
         }
      }

      try
      {
         lastReturnValue = method.invoke(sessionBean, parameters);
      }
      catch (Exception e)
      {
         String msg = MessageFormat.format(
               "Failed to invoke session bean method {0}.", Reflect.encodeMethod(method));
         trace.warn(msg, e);
         throw e instanceof InvocationTargetException
               ? (InvocationTargetException) e : new InvocationTargetException(e, msg);
      }
   }

   /**
    * Instantiates the actual session-bean.
    */
   private void doCreateSessionBean()
   {
      sessionBean = EJBUtils.getJndiObject(jndiPath, remoteInterfaceClass);
   }

   private void doSetInAccessPointValues() throws InvocationTargetException
   {
      for (Iterator<Pair> i = accessPointValues.iterator(); i.hasNext();)
      {
         Pair entry = i.next();
         String name = (String) entry.getFirst();
         Object value = entry.getSecond();
         AccessPoint accessPoint = application.getAccessPoint(name);
         if (accessPoint == null)
         {
            Assert.condition(false, MessageFormat.format(
                  "Access point '{0}' does not exist in class '{1}'", name, remoteInterfaceClass));
         }
         Object characteristics = accessPoint.getAttribute(PredefinedConstants.FLAVOR_ATT);
         if (JavaAccessPointType.METHOD.equals(characteristics))
         {
            try
            {
               JavaDataTypeUtils.evaluate(name, sessionBean, value);
            }
            catch (InvocationTargetException e)
            {
               trace.warn(MessageFormat.format(
                     "Failed to invoke setter '{0}'.", name),
                     e.getTargetException());
               throw e;
            }
            catch (Exception e)
            {
               trace.warn("", e);
               throw new InvocationTargetException(e, MessageFormat.format(
                     "Failed setting session-bean in access-point '{0}'.", name));
            }
         }
      }
   }

   private Map<String, Object> doGetOutAccessPointValues(Set<String> outDataTypes)
         throws InvocationTargetException
   {
      Map<String, Object> result = new HashMap<String, Object>();

      for (Iterator<String> i = outDataMappingOrder.iterator(); i.hasNext();)
      {
         String name = i.next();
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
      Object characteristics = application.getAccessPoint(name).getAttribute(PredefinedConstants.FLAVOR_ATT);

      if (allowReturnValue && JavaAccessPointType.RETURN_VALUE.equals(characteristics))
      {
         return lastReturnValue;
      }
      else if (JavaAccessPointType.METHOD.equals(characteristics))
      {
         try
         {
            return JavaDataTypeUtils.evaluate(name, sessionBean);
         }
         catch (InvocationTargetException e)
         {
            trace.warn(MessageFormat.format(
                  "Failed to invoke getter '{0}'.", name),
                  e.getTargetException());
            throw e;
         }
         catch (Exception e)
         {
            trace.warn("", e);
            throw new InvocationTargetException(e, MessageFormat.format(
                  "Failed retrieving session-bean out access-point '{0}'.", name));
         }
      }
      else
      {
         throw new InternalException(MessageFormat.format(
               "Unknown characteristics '{0}' for access point '{1}'.", characteristics, name));
      }
   }

   private Pair findAccessPointValue(String name)
   {
      Pair result = null;
      for (Iterator<Pair> i = accessPointValues.iterator(); i.hasNext();)
      {
         Pair entry = i.next();
         if (name.equals(entry.getFirst()))
         {
            result = entry;
            break;
         }
      }
      return result;
   }
}
