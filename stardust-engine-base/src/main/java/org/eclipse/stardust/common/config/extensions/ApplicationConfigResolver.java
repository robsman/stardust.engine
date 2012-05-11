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
package org.eclipse.stardust.common.config.extensions;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.common.config.ConfigLog.EXTENSIONS_LOG;
import static org.eclipse.stardust.common.config.extensions.PojoExtensionsUtils.buildPojoExtensionsCache;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.extensions.spi.ExtensionsResolver;
import org.eclipse.stardust.common.reflect.Reflect;


public class ApplicationConfigResolver implements ExtensionsResolver
{
   public static final String PRP_CONFIGURATION_PROPERTY = "ConfigurationProperty";

   private final ConcurrentMap<Pair<String, String>, List<? >> resolvedProviders = newConcurrentHashMap();

   public static <T> T getStaticallyConfiguredProvider(Class<T> providerIntfc,
         String configurationProperty)
   {
      List<T> providers = getStaticallyConfiguredProviders(providerIntfc, configurationProperty);

      return !isEmpty(providers) ? providers.get(0) : null;
   }

   public static <T> List<T> getStaticallyConfiguredProviders(Class<T> providerIntfc,
         String configurationProperty)
   {
      Parameters params = Parameters.instance();

      List<Class<? >> providerTypes = newArrayList();
      Object providerConfig = params.get(configurationProperty);
      if (null != providerConfig)
      {
         if (providerConfig instanceof String)
         {
            for (Iterator<String> i = StringUtils.split((String) providerConfig, ","); i.hasNext();)
            {
               String providerTypeName = i.next().trim();
               if ( !isEmpty(providerTypeName))
               {
                  EXTENSIONS_LOG.info("Using configuration value as implementation for SPI type "
                        + providerIntfc + ": " + providerTypeName);

                  Class<? > providerType = Reflect.getClassFromClassName(providerTypeName);
                  providerTypes.add(providerType);
               }
            }
         }
         else
         {
            EXTENSIONS_LOG.warn("Ignoring invalid configuration value for SPI type "
                  + providerIntfc + ": " + providerConfig);

            return emptyList();
         }
      }

      if ( !isEmpty(providerTypes))
      {
         // initialize provider cache
         return buildPojoExtensionsCache(providerIntfc, providerTypes);
      }
      else
      {
         return emptyList();
      }
   }

   public <T> List<T> resolveExtensionProviders(Class<T> providerIntfc, Map<String, ?> resolutionConfig)
   {
      String configurationProperty = (String) resolutionConfig.get(PRP_CONFIGURATION_PROPERTY);
      if (isEmpty(configurationProperty))
      {
         return emptyList();
      }

      Pair<String, String> cacheKey = new Pair<String, String>(providerIntfc.getName(),
            configurationProperty);
      if ( !resolvedProviders.containsKey(cacheKey))
      {
         List<T> providers = getStaticallyConfiguredProviders(providerIntfc, configurationProperty);

         resolvedProviders.putIfAbsent(cacheKey, providers);
      }

      @SuppressWarnings("unchecked")
      List<T> providers = (List<T>) resolvedProviders.get(cacheKey);

      return providers;
   }

}
