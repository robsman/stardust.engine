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
package org.eclipse.stardust.engine.core.runtime.beans.interceptors;

import java.util.Collections;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.config.PropertyLayerFactory;
import org.eclipse.stardust.common.config.PropertyProvider;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLogUtils;


/**
 * This interceptor creates a property layer and places a reference on itself into 
 * this layer. Subsequent executed code can use this layer to store properties in it.
 * This layer will be destroyed on leaving {@link #invoke(MethodInvocation)}.   
 * 
 * @author sborn
 * @version $Revision$
 */
public class PropertyLayerProviderInterceptor implements MethodInterceptor
{
   private final PropertyProvider propertyProvider;
   private final boolean withPredecessor;
   
   public static final String PROPERTY_LAYER = PropertyLayerProviderInterceptor.class
         .getName() + ".PropertyLayer";
   
   public static final PropertyLayerFactory BPM_RT_ENV_LAYER_FACTORY = new PropertyLayerFactory()
   {
      public PropertyLayer createPropertyLayer(PropertyLayer predecessor)
      {
         return new BpmRuntimeEnvironment(predecessor);
      }
   };
   
   public static final PropertyLayerFactory BPM_RT_ENV_LAYER_FACTORY_NOPREDECESSOR = new PropertyLayerFactory()
   {
      public PropertyLayer createPropertyLayer(PropertyLayer predecessor)
      {
         return new BpmRuntimeEnvironment(null);
      }
   };
   
   private static final ThreadLocal CURRENT = new ThreadLocal();

   public PropertyLayerProviderInterceptor()
   {
      this(null, true);
   }

   public PropertyLayerProviderInterceptor(boolean withPredecessor)
   {
      this(null, withPredecessor);
   }

   public PropertyLayerProviderInterceptor(PropertyProvider propertyProvider)
   {
      this(propertyProvider, true);
   }
   
   private PropertyLayerProviderInterceptor(PropertyProvider propertyProvider,
         boolean withPredecessor)
   {
      this.propertyProvider = propertyProvider;
      this.withPredecessor = withPredecessor;
   }
   
   public static BpmRuntimeEnvironment getCurrent()
   {
      return (BpmRuntimeEnvironment) CURRENT.get();
   }

   public static BpmRuntimeEnvironment setCurrent(BpmRuntimeEnvironment rtEnv)
   {
      final BpmRuntimeEnvironment previous = (BpmRuntimeEnvironment) CURRENT.get();
      
      CURRENT.set(rtEnv);
      
      return previous;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      BpmRuntimeEnvironment propLayer = null;
      try
      {
         propLayer = (BpmRuntimeEnvironment) ParametersFacade.pushLayer(
               invocation.getParameters(),
               withPredecessor ? BPM_RT_ENV_LAYER_FACTORY : BPM_RT_ENV_LAYER_FACTORY_NOPREDECESSOR,
               (propertyProvider != null)
                     ? propertyProvider.getProperties()
                     : Collections.EMPTY_MAP);

         // attach a reference of this layer to its own properties in order to
         // enable following classes to add properties to it.
         propLayer.setProperty(PROPERTY_LAYER, propLayer);
         propLayer.initDetailsFactory();
         final BpmRuntimeEnvironment previous = setCurrent(propLayer);
         try
         {
            Object result = invocation.proceed();
            
            RuntimeLogUtils.logSecurityContext();
            
            return result;
         }
         finally
         {
            setCurrent(previous);
         }
      }
      finally
      {
         if (propLayer != null)
         {
            propLayer.close();
            
            ParametersFacade.popLayer(invocation.getParameters());
         }
      }
   }
}
