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

import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.cache.CacheAdapterBase;
import org.eclipse.stardust.engine.runtime.utils.HazelcastUtils;

import com.hazelcast.core.IMap;



public class HazelcastCacheAdapter extends CacheAdapterBase<IMap<Object, Object>>
{
   private static final Logger trace = LogManager.getLogger(HazelcastCacheAdapter.class);

   public static final String KEY_CURRENT_CONNECTION = HazelcastCacheAdapter.class.getName() + ".CurrentConnection";

   public static final String PRP_HAZELCAST_GLOBAL_CACHE_NAME = "Infinity.Engine.Caching.Hazelcast.GlobalCacheName";

   private final String txMode;

   private final ConnectionFactory hzCf;

   @SuppressWarnings("rawtypes")
   public HazelcastCacheAdapter(String txMode, ConnectionFactory hzCf, Map config)
   {
      this.txMode = txMode;
      this.hzCf = hzCf;

      Parameters params = Parameters.instance();
      String cacheName = params.getString(PRP_HAZELCAST_GLOBAL_CACHE_NAME, "ipp-2nd-level-cache");
      if (StringUtils.isEmpty(cacheName))
      {
         cacheName = (String) config.get("name");
      }

      this.delegate = HazelcastUtils.getHazelcastInstance().getMap(cacheName);

      if (trace.isDebugEnabled())
      {
         if (trace.isInfoEnabled())
         {
            trace.info("Successfully bound Hazelcast cache \"" + delegate.getName()
                  + "\" for \"" + config.get("name") + "\".");
         }
      }
   }

   @Override
   protected void beforeCacheAccess(boolean read)
   {
      if ("rw".equals(txMode) || ("w".equals(txMode) && !read))
      {
         final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         try
         {
            rtEnv.retrieveJcaConnection(hzCf);
         }
         catch (final ResourceException e)
         {
            throw new PublicException(
                  BpmRuntimeError.HZLC_FAILES_ENLISTING_HAZLECAST_CACHE_IN_CURRENT_TRANSACTION
                        .raise(), e);
         }
      }
   }
}
