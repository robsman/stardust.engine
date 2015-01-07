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
package org.eclipse.stardust.engine.core.cache.hazelcast;

import java.util.Arrays;
import java.util.Map;

import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.spi.cache.Cache;
import org.eclipse.stardust.engine.core.spi.cache.CacheException;
import org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider;



public class HazelcastCacheFactory implements Cache.Factory
{
   private static final Logger trace = LogManager.getLogger(HazelcastCacheFactory.class);
   
   public static final String PRP_HAZELCAST_TX_MODE = "Infinity.Engine.Caching.Hazelcast.TxMode";
   
   private static final HazelcastJcaConnectionFactoryProvider CONNECTION_FACTORY_PROVIDER;
   
   static
   {
      CONNECTION_FACTORY_PROVIDER = ExtensionProviderUtils.getFirstExtensionProvider(HazelcastJcaConnectionFactoryProvider.class, KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER);
      if (CONNECTION_FACTORY_PROVIDER == null)
      {
         throw new IllegalStateException("No Hazelcast JCA connection factory provider could be found.");
      }
   }
   
   @SuppressWarnings("rawtypes")
   public Cache createCache(Map env) throws CacheException
   {
      Parameters params = Parameters.instance();

      try
      {
         String txMode = params.getString(PRP_HAZELCAST_TX_MODE, "rw");
         if ( !Arrays.asList("none", "w", "rw").contains(txMode))
         {
            trace.warn("Unsupported Hazelcast TX mode \"" + txMode + "\". Using \"rw\" instead.");
            txMode = "rw";
         }

         ConnectionFactory hzCf = null;
         if ( !"none".equals(txMode))
         {
            hzCf = CONNECTION_FACTORY_PROVIDER.connectionFactory();
         }
         return new HazelcastCacheAdapter(txMode, hzCf, env);
      }
      catch (Exception e)
      {
         trace.warn("Failed initializing Hazelcast cache.", e);
      }

      return null;
   }
}
