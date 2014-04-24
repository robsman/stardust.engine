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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.spi.StructDataMappingUtils;


public class SerializableValidator implements ExtendedDataValidator, Stateless
{

   public boolean isStateless()
   {
      return true;
   }

   public BridgeObject getBridgeObject(AccessPoint point, String path,
         Direction direction, AccessPathEvaluationContext context)
   {
      return new BridgeObjectWithContext(JavaDataTypeUtils.getBridgeObject(point, path),
            context);
   }

   public List validate(Map attributes)
   {
      Vector result = new Vector();
      String beanClassName = (String) attributes.get(PredefinedConstants.CLASS_NAME_ATT);

      if (StringUtils.isEmpty(beanClassName))
      {
         BpmValidationError error = BpmValidationError.JAVA_EMPTY_CLASSNAME.raise();
         result.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      else
      {
         try
         {
            Class clazz = Reflect.getClassFromClassName(beanClassName);
            Object autoInstantiate = attributes.get(PredefinedConstants.AUTO_INSTANTIATE_ATT);
            if (autoInstantiate instanceof Boolean && ((Boolean) autoInstantiate).booleanValue() ||
                  "true".equals(autoInstantiate))
            {
               try
               {
                  clazz.getConstructor(new Class[] {});
               }
               catch (Exception ex)
               {
                  BpmValidationError error = BpmValidationError.JAVA_CLASS_HAS_NO_DEFAULT_CONSTRUCTOR.raise(beanClassName);
                  result.add(new Inconsistency(error, Inconsistency.ERROR));
               }
            }
         }
         catch (Exception e)
         {
            BpmValidationError error = BpmValidationError.JAVA_CANNOT_LOAD_CLASS.raise(
                  beanClassName, e.getMessage());
            result.add(new Inconsistency(error, Inconsistency.WARNING));
         }
      }
      return result;
   }

   private class BridgeObjectWithContext extends BridgeObject
   {
      private AccessPathEvaluationContext context;
      private BridgeObject bridgeObject;

      public BridgeObjectWithContext(BridgeObject bridgeObject,
            AccessPathEvaluationContext context)
      {
         super(null, null);
         this.bridgeObject = bridgeObject;
         this.context = context;
      }

      public Direction getDirection()
      {
         return bridgeObject.getDirection();
      }

      public Class getEndClass()
      {
         return bridgeObject.getEndClass();
      }

      public boolean acceptAssignmentFrom(BridgeObject rhs)
      {
         if (null != context
               && StructDataMappingUtils.isVizRulesApplication(context.getActivity())
               && Map.class.isAssignableFrom(rhs.getEndClass())
               && !getEndClass().isPrimitive()
               && Serializable.class.isAssignableFrom(getEndClass()))
         {
            return true;
         }
         else
         {
            return super.acceptAssignmentFrom(rhs);
         }
      }
   }
}
