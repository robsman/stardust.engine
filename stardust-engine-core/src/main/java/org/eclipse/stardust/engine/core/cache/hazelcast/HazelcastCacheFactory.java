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

import javax.naming.InitialContext;
import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.spi.cache.Cache;
import org.eclipse.stardust.engine.core.spi.cache.CacheException;



public class HazelcastCacheFactory implements Cache.Factory
{
   private static final Logger trace = LogManager.getLogger(HazelcastCacheFactory.class);
   
   public static final String PRP_HAZELCAST_TX_MODE = "Infinity.Engine.Caching.Hazelcast.TxMode";
   
   public static final String PRP_HAZELCAST_CF_JNDI_NAME = "Infinity.Engine.Caching.Hazelcast.ConnectionFactoryJndiName";
   
   public static final String HAZELCAST_CF_DEFAULT_JNDI_NAME = "HazelcastCF";

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
            InitialContext ic = new InitialContext();
            hzCf = (ConnectionFactory) ic.lookup(params.getString(
                  PRP_HAZELCAST_CF_JNDI_NAME, HAZELCAST_CF_DEFAULT_JNDI_NAME));
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
