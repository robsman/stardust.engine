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
import java.util.Map;

import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class PrimitiveAccessPathEvaluator implements AccessPathEvaluator, Stateless
{
   public boolean isStateless()
   {
      return true;
   }

   public Object evaluate(Map attributes, Object accessPoint, String outPath)
   {
      try
      {
         return JavaDataTypeUtils.evaluate(outPath, accessPoint);
      }
      catch (InvocationTargetException e)
      {
         throw new PublicException("Failed reading java value.", e.getTargetException());
      }
   }

   public Object evaluate(Map attributes, Object accessPoint, String inPath, Object value)
   {
      try
      {
         return JavaDataTypeUtils.evaluate(inPath, accessPoint, value);
      }
      catch (InvocationTargetException e)
      {
         throw new PublicException("Failed setting java value.", e.getTargetException());
      }
   }

   public Object createInitialValue(Map attributes)
   {
      Type type = (Type) attributes.get(PredefinedConstants.TYPE_ATT);

      Object defaultValue = createDefaultValue(attributes);
      if (defaultValue == null)
      {
         return newValueInstance(type);
      }
      return defaultValue;
   }

   public Object createDefaultValue(Map attributes)
   {
      String defaultValue = (String) attributes.get(PredefinedConstants
            .DEFAULT_VALUE_ATT);
      if (!StringUtils.isEmpty(defaultValue))
      {
         Type type = (Type) attributes.get(PredefinedConstants.TYPE_ATT);

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
}
