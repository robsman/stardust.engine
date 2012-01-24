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
package org.eclipse.stardust.engine.extensions.ejb.ejb2.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.ejb.EJBObject;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.PublicException;
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
 * ApplicationInstance implementation for the SessionBeanApplicationInstance
 * 
 * @author jmahmood, ubirkemeyer
 * @version $Revision: 52518 $
 */
public class SessionBean20ApplicationInstance implements SynchronousApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(SessionBean20ApplicationInstance.class);

   private List accessPointValues = new ArrayList();
   private List outDataMappingOrder = new ArrayList();

   private Application application;
   private Class remoteInterfaceClass;
   private Class homeInterfaceClass;
   private String jndiPath;
   private Object sessionBean;
   private boolean local;

   private Object lastReturnValue;

   public void bootstrap(ActivityInstance activityInstance)
   {
      application = activityInstance.getActivity().getApplication();

      jndiPath = (String) application.getAttribute(PredefinedConstants.JNDI_PATH_ATT);
      remoteInterfaceClass = Reflect.getClassFromClassName(
            (String) application.getAttribute(PredefinedConstants.REMOTE_INTERFACE_ATT));

      String homeInterfaceClassName = (String) application.getAttribute(PredefinedConstants.HOME_INTERFACE_ATT);

      homeInterfaceClassName = StringUtils.isEmpty(homeInterfaceClassName) ?
            remoteInterfaceClass.getName() + "Home" : homeInterfaceClassName;
      homeInterfaceClass = Reflect.getClassFromClassName(homeInterfaceClassName);

      Boolean loc = (Boolean) application.getAttribute(PredefinedConstants.IS_LOCAL_ATT);
      local = (loc != null ? loc.booleanValue() : false);
      
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
      if (sessionBean instanceof EJBObject)
      {
         try
         {
            ((EJBObject) sessionBean).remove();
         }
         catch (Exception e)
         {
            trace.warn("", e);
         }
      }
      sessionBean = null;
   }

   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      // reset from possible previous execution
      try
      {

         lastReturnValue = null;
   
         doCreateSessionBean();
   
         doSetInAccessPointValues();
   
         Method method = Reflect.decodeMethod(remoteInterfaceClass,
               (String) application.getAttribute(PredefinedConstants.METHOD_NAME_ATT));
   
         Object[] parameters = new Object[method.getParameterTypes().length];
   
         Class[] parameterTypes = method.getParameterTypes();
         for (int i = 0; i < parameterTypes.length; i++)
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
                     + paramName + "' for session bean method '"
                     + Reflect.encodeMethod(method) + "'.");
            }
         }
   
         try
         {
            lastReturnValue = method.invoke(sessionBean, parameters);
         }
         catch (InvocationTargetException e)
         {
            trace.warn("Failed to invoke session-bean method '" + Reflect
                  .encodeMethod(method) + "'.", e.getTargetException());
            throw e;
         }
         catch (Exception e)
         {
            trace.warn("Failed to invoke session-bean method '" + Reflect
                  .encodeMethod(method) + "'.", e);
            throw new InvocationTargetException(e, "Failed to invoke session bean method "
                  + Reflect.encodeMethod(method) + ".");
         }
   
         return doGetOutAccessPointValues(outDataTypes);
      }
      catch (InvocationTargetException ite) 
      {
         throw ite;
      }
      catch (Exception e) 
      {
         throw new InvocationTargetException(e);
      }
   }

   /**
    * Instantiates the actual session-bean.
    */
   private void doCreateSessionBean()
   {
      String createMethodName = (String) application.getAttribute(
            PredefinedConstants.CREATE_METHOD_NAME_ATT);

      createMethodName = (createMethodName == null || createMethodName.length() == 0) ?
            "create()" : createMethodName;

      Method createMethod = Reflect.decodeMethod(homeInterfaceClass, createMethodName);

      Object[] createParameters = new Object[createMethod.getParameterTypes().length];

      Class[] createParameterTypes = createMethod.getParameterTypes();
      for (int i = 0; i < createParameterTypes.length; i++)
      {
         String humanName = Reflect.getHumanReadableClassName(createParameterTypes[i]);
         String paramName = humanName.toLowerCase().charAt(0)
               + SessionBeanConstants.CREATION_METHOD_PARAMETER_PREFIX + (i + 1);
         Pair param = findAccessPointValue(paramName);
         createParameters[i] = (null != param) ? param.getSecond() : null;
      }

      Object home = EJBUtils.getHomeObject(jndiPath, homeInterfaceClass, local);

      Assert.isNotNull(home, "The home object reference is null.");

      try
      {
         sessionBean = createMethod.invoke(home, createParameters);
      }
      catch (InvocationTargetException x)
      {
         throw new PublicException("Cannot create session bean.", x.getTargetException());
      }
      catch (Exception x)
      {
         throw new PublicException("Cannot create session bean.", x);
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
         Assert.isNotNull(accessPoint, "Access point '" + name + "' does not exist, " + remoteInterfaceClass);
         Object characteristics = accessPoint.getAttribute(PredefinedConstants.FLAVOR_ATT);
         if (JavaAccessPointType.METHOD.equals(characteristics))
         {
            try
            {
               JavaDataTypeUtils.evaluate(name, sessionBean, value);
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
            trace.warn("Failed to invoke getter '" + name + "'.",
                  e.getTargetException());
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
         throw new InternalException("Unknown characteristics '" +
               characteristics + "' for access point '" + name + "'.");
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
