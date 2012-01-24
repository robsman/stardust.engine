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
import java.util.Map;

import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class JavaBeanAccessPathEvaluator implements AccessPathEvaluator, Stateless
{
   private static final Logger trace = LogManager.getLogger(JavaBeanAccessPathEvaluator.class);
   
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
         throw new PublicException("Failed reading bean attribute.",
               e.getTargetException());
      }
   }

   public Object evaluate(Map attributes, Object accessPoint, String inPath,
         Object value)
   {
      try
      {
         return JavaDataTypeUtils.evaluate(inPath, accessPoint, value);
      }
      catch (InvocationTargetException e)
      {
         throw new PublicException("Failed setting bean attribute.",
               e.getTargetException());
      }
   }

   public Object createInitialValue(Map data)
   {
      Object autoInstantiate = data.get(PredefinedConstants.AUTO_INSTANTIATE_ATT);
      if (autoInstantiate instanceof Boolean && ((Boolean) autoInstantiate).booleanValue() ||
            "true".equals(autoInstantiate))
      {
         try
         {
            String className = (String) data.get(PredefinedConstants.CLASS_NAME_ATT);
            return Class.forName(className).newInstance();
         }
         catch (Exception e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug(e);
            }
         }
      }
      return null;
   }

   public Object createDefaultValue(Map attributes)
   {
      String defaultValue = (String) attributes.get(PredefinedConstants
            .DEFAULT_VALUE_ATT);
      if (!StringUtils.isEmpty(defaultValue))
      {
         String className = (String) attributes.get(PredefinedConstants.CLASS_NAME_ATT);
         try
         {
            return Reflect.convertStringToObject(className, defaultValue);
         }
         catch (Exception e)
         {
            throw new PublicException("Can't convert value '" + defaultValue
                  + "' to type '" + className + "'.", e);
         }
      }
      return null;
   }
}
