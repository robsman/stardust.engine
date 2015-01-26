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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.pojo.utils.JavaAccessPointType;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class JFCApplicationInstance implements InteractiveApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(
         JFCApplicationInstance.class);

   private Map accessPointValues = new HashMap();

   private Map applicationProperties;
   private ActivityInstance activityInstance;

   private Class clazz;
   private JPanel panel;

   public JFCApplicationInstance(ActivityInstance activityInstance)
   {
      applicationProperties = activityInstance.getActivity().
            getApplicationContext(PredefinedConstants.JFC_CONTEXT).getAllAttributes();
      this.activityInstance = activityInstance;

      String className = (String) applicationProperties.get(
            PredefinedConstants.CLASS_NAME_ATT);
      clazz = Reflect.getClassFromClassName(className);
      panel = (JPanel) Reflect.createInstance(className);
   }

   public void setInAccessPointValue(String name, Object value)
   {
      AccessPoint ap = activityInstance.getActivity().
            getApplicationContext(PredefinedConstants.JFC_CONTEXT).getAccessPoint(name);
      Object characteristics = ap == null ? null :
            ap.getAttribute(PredefinedConstants.FLAVOR_ATT);

      if (JavaAccessPointType.PARAMETER.equals(characteristics))
      {
         accessPointValues.put(name, value);
      }
      else if (JavaAccessPointType.METHOD.equals(characteristics))
      {

         Method method = Reflect.decodeMethod(clazz, name);
         try
         {
            method.invoke(panel, new Object[]{value});
         }
         catch (Exception e)
         {
            trace.warn("", e);
            throw new InternalException(e);
         }
      }
      else
      {
         throw new InternalException("Unknown characteristics '" +
               characteristics + "' for access point '" + name + "'.");
      }
   }

   public Object getOutAccessPointValue(String name)
   {
      AccessPoint ap = activityInstance.getActivity().
            getApplicationContext(PredefinedConstants.JFC_CONTEXT).getAccessPoint(name);
      Object characteristics = ap == null ? null :
            ap.getAttribute(PredefinedConstants.FLAVOR_ATT);

      // hint: no return value here because the getOutAccessPointValue method is only
      // for processing in data mappings.
      if (JavaAccessPointType.METHOD.equals(characteristics))
      {
         Method method = Reflect.decodeMethod(clazz, name);
         try
         {
            return method.invoke(panel, new Object[]{});
         }
         catch (Exception e)
         {
            trace.warn("", e);
            throw new InternalException(e);
         }
      }
      else
      {
         throw new InternalException("Unknown characteristics '" +
               characteristics + "' for access point '" + name + "'.");
      }
   }

   public Map invoke(Iterator outDataTypes)
   {
      Method method = Reflect.decodeMethod(clazz, (String) applicationProperties.get(
            PredefinedConstants.METHOD_NAME_ATT));

      Object[] parameters = new Object[method.getParameterTypes().length];

      Class[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++)
      {
         String humanName = Reflect.getHumanReadableClassName(parameterTypes[i]);
         String paramName = humanName.toLowerCase().charAt(0) + "Param" + (i + 1);
         parameters[i] = accessPointValues.get(paramName);
      }

      Object returnValue = null;

      try
      {
         returnValue = method.invoke(panel, parameters);
      }
      catch (Exception e)
      {
         throw new InternalException("Failed to invoke completion method " +
               Reflect.encodeMethod(method) + ".", e);
      }

      // assembly of desired OUT access points to return
      Map result = new HashMap();

      while (outDataTypes.hasNext())
      {
         String name = ((AccessPoint) outDataTypes.next()).getId();
         if (name.equals("returnValue"))
         {
            result.put(name, returnValue);
         }
         else
         {
            // a getter method
            try
            {
               Method getMethod = Reflect.decodeMethod(clazz, name);
               result.put(name, getMethod.invoke(panel, new Object[]{}));
            }
            catch (Exception e)
            {
               throw new PublicException(e);
            }
         }
      }
      return result;
   }

   public JPanel getPanel()
   {
      return panel;
   }

   public void cleanup()
   {
   }

}
