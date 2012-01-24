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
package org.eclipse.stardust.common.config;

import java.util.Date;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;


public class TimestampProviderUtils
{

   private static final String PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE = TimestampProvider.class.getName()+".CachedInstance";
   
   public static long getTimeStampValue()
   {
      TimestampProvider provider = getProvider();

      // try to avoid creation of new Date instance if possible (unfortunately the
      // original contract was designed with Date instances)
      return (RealtimeTimestampProvider.INSTANCE == provider)
            ? ((RealtimeTimestampProvider) provider).getTimestampValue()
            : provider.getTimestamp().getTime();
   }
   
   public static Date getTimeStamp()
   {
      return getProvider().getTimestamp();
   }
   
   public static TimestampProvider getProvider()
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      
      TimestampProvider result = rtEnv.getTimestampProvider();

      if (null == result)
      {
         final GlobalParameters globals = GlobalParameters.globals();
         
         TimestampProvider cachedProvider = (TimestampProvider) globals.get(PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE);
         
         if (null != cachedProvider)
         {
            result = cachedProvider;
         }
         else
         {
            result = (TimestampProvider) globals.getOrInitialize(
                  PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE, new ValueProvider()
                  {
                     public Object getValue()
                     {
                        Object configuredTimestampProvider = globals.get(KernelTweakingProperties.RUNTIME_TIMESTAMP_PROVIDER);

                        if (configuredTimestampProvider instanceof TimestampProvider)
                        {
                           return configuredTimestampProvider;
                        }
                        else if (configuredTimestampProvider instanceof String)
                        {
                           return Reflect.createInstance((String) configuredTimestampProvider);
                        }
                        else
                        {
                           if (null != configuredTimestampProvider)
                           {
                              RuntimeLog.CONFIGURATION.warn("Ignoring unsupported value for configuration option "
                                    + KernelTweakingProperties.RUNTIME_TIMESTAMP_PROVIDER
                                    + " = " + configuredTimestampProvider);
                           }

                           // use default provider
                           return RealtimeTimestampProvider.INSTANCE;
                        }
                     }
                  });
         }
         
         if (null != result)
         {
            rtEnv.setTimestampProvider(result);
         }
      }

      return result;
   }
}
