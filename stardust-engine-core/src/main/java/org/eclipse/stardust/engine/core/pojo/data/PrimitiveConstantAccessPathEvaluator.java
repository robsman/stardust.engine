/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    barry.grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.engine.core.pojo.data;

import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;

/**
 * @author barry.grotjahn
 * @version $Revision$
 */
public class PrimitiveConstantAccessPathEvaluator implements ExtendedAccessPathEvaluator, Stateless
{
   public boolean isStateless()
   {
      return true;
   }
   
   @Override
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance, String outPath,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      if(StringUtils.isEmpty(outPath))
      {
         return null;
      }

      Object value = null;
      String constantValue = null;
      
      outPath = outPath.trim();
      if(outPath.startsWith("("))
      {
         outPath = outPath.substring(1, outPath.length());
         String[] split = outPath.split("\\)");
         String type = split[0];         
         if(split.length > 1)
         {
            constantValue = split[1];
            constantValue = constantValue.trim();      
         }
         
         try
         {
            return Reflect.convertStringToObject(type, constantValue);
         }
         catch (Exception e)
         {
            throw new PublicException(
                  BpmRuntimeError.POJO_CANNOT_CONVERT_VALUT_TO_TYPE.raise(constantValue,
                        type), e);
         }
      }
      else
      {
         value = constantValue;
      }
      
      return value;
   }

   @Override
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance, String inPath,
         AccessPathEvaluationContext accessPathEvaluationContext, Object value)
   {
      return null;      
   }

   @Override
   public Object createInitialValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return null;      
   }

   @Override
   public Object createDefaultValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return null;      
   }
}