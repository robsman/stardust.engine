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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.spi.extensions.model.DataType;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;


public class ValidatorUtils
{
   public static Object getValidator(PluggableType type, ModelElement modelElement)
   {
      return getValidator(type, modelElement, null);
   }

   public static Object getValidator(PluggableType type, ModelElement modelElement,
         List inconsistencies)
   {
      return getValidator(type, modelElement, inconsistencies, true);
   }

   public static Object getValidator(PluggableType type, ModelElement modelElement,
      List inconsistencies, boolean mandatory)
   {
      Object validator = null;
      String validatorClassName = type
            .getStringAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT);
      if ( !StringUtils.isEmpty(validatorClassName))
      {
         try
         {
            if (type instanceof DataType)
            {
               validator = SpiUtils.createExtendedDataValidator(validatorClassName);
            }
            else
            {
               final Class clazz = Reflect.getClassFromClassName(validatorClassName,
                     mandatory);
               if (null != clazz)
               {
                  validator = Reflect.createInstance(clazz, null, null);
               }
            }
         }
         catch (InternalException e)
         {
            Throwable t = e.getCause();
            if (t instanceof ClassNotFoundException || t instanceof NoClassDefFoundError)
            {
               if (mandatory)
               {
                  BpmValidationError error = BpmValidationError.VAL_CANNOT_RETRIEVE_CLASS_FOR_VALIDATION.raise(validatorClassName);
                  inconsistencies.add(new Inconsistency(error, modelElement,
                        Inconsistency.WARNING));
               }
            }
            else
            {
               throw e;
            }
         }
      }

      return validator;
   }

   private ValidatorUtils()
   {
      // Utility class
   }

}
