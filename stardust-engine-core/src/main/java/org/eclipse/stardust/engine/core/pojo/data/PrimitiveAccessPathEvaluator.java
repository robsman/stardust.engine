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
package org.eclipse.stardust.engine.core.pojo.data;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class PrimitiveAccessPathEvaluator implements ExtendedAccessPathEvaluator, Stateless
{
   public boolean isStateless()
   {
      return true;
   }

   @SuppressWarnings("deprecation")
   private Object newValueInstance(Type type)
   {
      if (type.equals(Type.Boolean))
      {
         return Boolean.FALSE;
      }
      else if (type.equals(Type.String))
      {
         return "";
      }
      else if (type.equals(Type.Char))
      {
         return new Character((char) 0);
      }
      else if (type.equals(Type.Byte))
      {
         return new Byte((byte) 0);
      }
      else if (type.equals(Type.Short))
      {
         return new Short((short) 0);
      }
      else if (type.equals(Type.Integer))
      {
         return new Integer(0);
      }
      else if (type.equals(Type.Long))
      {
         return new Long(0);
      }
      else if (type.equals(Type.Float))
      {
         return new Float(0.0);
      }
      else if (type.equals(Type.Double))
      {
         return new Double(0.0);
      }
      else if (type.equals(Type.Calendar))
      {
         return Calendar.getInstance();
      }
      else if (type.equals(Type.Timestamp))
      {
         return new Date();
      }
      else if (type.equals(Type.Money))
      {
         return new Money(0);
      }
      return null;
   }

   @Override
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance, String outPath,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      try
      {
         if (accessPointInstance != null && JavaDataTypeUtils.isJavaEnumeration(accessPointDefinition))
         {
            if (accessPathEvaluationContext == null || accessPathEvaluationContext.getTargetAccessPointDefinition() instanceof JavaAccessPoint)
            {
               Class enumClass = JavaDataTypeUtils.getReferenceClass(accessPointDefinition, true);
               if (enumClass != null && enumClass.isEnum())
               {
                  if (accessPointInstance instanceof Number)
                  {
                     accessPointInstance = enumClass.getEnumConstants()[((Number) accessPointInstance).intValue()];
                  }
                  else
                  {
                     accessPointInstance = Enum.valueOf(enumClass, accessPointInstance.toString());
                  }
               }
            }
         }
         return JavaDataTypeUtils.evaluate(outPath, accessPointInstance);
      }
      catch (InvocationTargetException e)
      {
         throw new PublicException("Failed reading java value.", e.getTargetException());
      }
   }

   @Override
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance, String inPath,
         AccessPathEvaluationContext accessPathEvaluationContext, Object value)
   {
      try
      {
         if (accessPointInstance != null && JavaDataTypeUtils.isJavaEnumeration(accessPointDefinition))
         {
            if (value != null && value.getClass().isEnum())
            {
               if (accessPathEvaluationContext == null || accessPathEvaluationContext.getTargetAccessPointDefinition() instanceof JavaAccessPoint)
               {
                  Class enumClass = JavaDataTypeUtils.getReferenceClass(accessPointDefinition, true);
                  if (enumClass != null && enumClass.isEnum() && enumClass.isInstance(value))
                  {
                     value = ((Enum) value).name();
                  }
               }
            }
         }
         return JavaDataTypeUtils.evaluate(inPath, accessPointInstance, value);
      }
      catch (InvocationTargetException e)
      {
         throw new PublicException("Failed setting java value.", e.getTargetException());
      }
   }

   @Override
   public Object createInitialValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      Object defaultValue = createDefaultValue(accessPointDefinition, accessPathEvaluationContext);
      return defaultValue == null
            ? newValueInstance((Type) accessPointDefinition.getAttribute(PredefinedConstants.TYPE_ATT))
            : defaultValue;
   }

   @Override
   public Object createDefaultValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      String defaultValue = accessPointDefinition.getStringAttribute(PredefinedConstants.DEFAULT_VALUE_ATT);
      if (!StringUtils.isEmpty(defaultValue))
      {
         Type type = (Type) accessPointDefinition.getAttribute(PredefinedConstants.TYPE_ATT);
         if (Type.Enumeration == type)
         {
            Class refClass = JavaDataTypeUtils.getReferenceClass(accessPointDefinition, true);
            if (refClass.isEnum())
            {
               Object[] values = refClass.getEnumConstants();
               for (Object o : values)
               {
                  if (o instanceof Enum && defaultValue.equals(((Enum) o).name()))
                  {
                     return ((Enum) o).name();
                  }
               }
            }
            else
            {
               return defaultValue;
            }
         }
         try
         {
            return Reflect.convertStringToObject(type.getId(), defaultValue);
         }
         catch (Exception e)
         {
            throw new PublicException("Can't convert value '" + defaultValue
                  + "' to type '" + type + "'.", e);
         }
      }
      return null;
   }
}
