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
package org.eclipse.stardust.engine.core.cache;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.DepartmentBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserDomainBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserGroupBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.spi.cache.Cache;



/**
 * @author Florin.Herinean
 */
public final class CacheHelper
{
   private static final Logger trace = LogManager.getLogger(CacheHelper.class);

   private static final String COHERENCE = "coherence";

   private static final String EHCACHE = "ehcache";

   private static final String NAME_PROPERTY = "name";

   private static Map<Class, AbstractCache> cacheables = CollectionUtils.newMap();

   private static Cache.Factory factory;

   static
   {
      if (Parameters.instance().getBoolean(KernelTweakingProperties.EXTERNAL_CACHING,
            true))
      {
         try
         {

            factory = getCacheFactory();

            cacheables.put(DepartmentBean.class, DepartmentsCache.instance());
            cacheables.put(UserBean.class, UsersCache.instance());
            cacheables.put(UserRealmBean.class, RealmsCache.instance());
            cacheables.put(UserDomainBean.class, UserDomainsCache.instance());
            cacheables.put(UserGroupBean.class, UserGroupsCache.instance());
            cacheables.put(AuditTrailPartitionBean.class, PartitionsCache.instance());
         }
         catch (Exception ce)
         {
            trace.warn("No caching service is available.");
         }
      }
   }

   public static boolean isCacheable(Class type)
   {
      return isCachingAvailable() && cacheables.containsKey(type);
   }

   public static AbstractCache getCache(Class type)
   {
      return cacheables.get(type);
   }

   static Cache createCache(String id)
   {
      Cache cache = null;
      if (CacheHelper.isCachingAvailable())
      {
         if (CacheHelper.isSharedCache())
         {
            id = "ipp";
         }

         try
         {
            Properties config = CacheHelper.getConfiguration(id);
            cache = createCache(config);
            if (cache != null && trace.isDebugEnabled())
            {
               trace.debug("Using implementation type <" + cache.getClass()
                     + "> for cache <" + id + ">");
            }
         }
         catch (Exception ce)
         {
            trace.warn("Unable to initialize cache <" + id + ">: " + ce.getMessage());
         }

      }
      return cache;
   }

   private static Properties getConfiguration(String id)
   {
      Properties config = new Properties();
      if ( !readProperties(config, "/" + id + "-cache", false))
      {
         trace.info("Using default properties for cache '" + id + "'.");
         String packageName = factory.getClass().getPackage().getName();
         if (packageName.contains(COHERENCE))
         {
            config.put(NAME_PROPERTY, "tx-" + id);
         }
         else if (packageName.contains(EHCACHE))
         {
            readProperties(config, EHCACHE, true);
            config.put(NAME_PROPERTY, "tx-" + id);
         }
      }
      return config;
   }

   private static boolean readProperties(Properties config, String propName,
         boolean warnForNull)
   {
      String name = propName + ".properties";
      InputStream is = CacheHelper.class.getResourceAsStream(name);
      if (is == null)
      {
         if (warnForNull)
         {
            trace.warn("Unable to read cache properties from '" + name + "'.");
         }
      }
      else
      {
         try
         {
            config.load(is);
         }
         catch (Exception e)
         {
            trace.warn("Unable to read properties from '" + name + "'.");
         }
      }
      return is != null;
   }

   private static boolean isSharedCache()
   {
      return Parameters.instance().getBoolean(KernelTweakingProperties.SHARED_CACHING,
            false);
   }

   private static boolean isCachingAvailable()
   {
      return factory != null;
   }

   private static Cache.Factory getCacheFactory() throws Exception
   {
      Cache.Factory factory = null;

      List<Cache.Factory> factories = ExtensionProviderUtils.getExtensionProviders(
            Cache.Factory.class, KernelTweakingProperties.CACHE_FACTORY_OVERRIDE);

      String cacheFactoryOverride = Parameters.instance().getString(
            KernelTweakingProperties.CACHE_FACTORY_OVERRIDE);
      if ( !factories.isEmpty())
      {
         try
         {
            factory = factories.get(0);
         }
         catch (Exception e)
         {
            trace.warn("Failed instantiating cache factory override class "
                  + cacheFactoryOverride + ", falling back to default.", e);
         }
      }

      return factory;
   }

   private static Cache createCache(Map env) throws Exception
   {
      if (factory != null)
      {
         Cache cache = factory.createCache(env);
         return cache;
      }
      return null;
   }

   public static void flushCaches()
   {
      Collection<AbstractCache> caches = cacheables.values();
      for (AbstractCache cache : caches)
      {
         cache.clear();
      }
   }
}
