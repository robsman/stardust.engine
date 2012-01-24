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
import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.config.extensions.PojoExtensionsUtils.instantiatePojoExtension;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ConfigLog;
import org.eclipse.stardust.common.config.FactoryFinder;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.extensions.spi.ExtensionsResolver;
import org.eclipse.stardust.common.log.Logger;


public class ServiceProviderResolver implements ExtensionsResolver
{
   private static Logger extLog = ConfigLog.EXTENSIONS_LOG;

   public static final String SUFFIX_PROVIDERS = ".Providers";

   public static final String SUFFIX_BLACKLISTED_PROVIDERS = ".BlacklistedProviders";

   private final ConcurrentMap<String, List<? >> resolvedProviders = newConcurrentHashMap();

   public static <T> T resolveFirstServiceProviderFromClasspath(Class<T> providerIntfc)
   {
      return resolveFirstServiceProviderFromClasspath(providerIntfc, null);
   }

   public static <T> T resolveFirstServiceProviderFromClasspath(Class<T> providerIntfc,
         Class<? extends T> fallbackProviderType)
   {
      List<T> providers = resolveServiceProvidersFromClasspath(providerIntfc);

      if ( !isEmpty(providers))
      {
         if (1 < providers.size() && extLog.isDebugEnabled())
         {
            extLog.debug("Ignoring " + (providers.size() - 1)
                  + " additional providers for SPI type " + providerIntfc);
         }

         return providers.get(0);
      }
      else
      {
         if (null != fallbackProviderType)
         {
            if (extLog.isDebugEnabled())
            {
               extLog.debug("Found no service provider for SPI type " + providerIntfc
                     + ", using fallback implementation " + fallbackProviderType);
            }

            return instantiatePojoExtension(fallbackProviderType);
         }
         else
         {
            extLog.info("Found no service provider for SPI type " + providerIntfc);

            return null;
         }
      }
   }

   public static <T> List<T> resolveServiceProvidersFromClasspath(Class<T> providerIntfc)
   {
      Parameters params = Parameters.instance();

      // find explicitly listed providers
      List<String> providerClassNames = CollectionUtils.newList();

      final String providersSpec = params.getString(providerIntfc.getName()
            + SUFFIX_PROVIDERS);
      for (Iterator<String> i = StringUtils.split(providersSpec, ","); i.hasNext();)
      {
         String className = i.next();
         providerClassNames.add(className.trim());
      }

      // find blacklisted providers
      Set<String> blacklistedProviderClassNames = CollectionUtils.newSet();

      final String blacklistedProvidersSpec = params.getString(providerIntfc.getName()
            + SUFFIX_BLACKLISTED_PROVIDERS);
      for (Iterator<String> i = StringUtils.split(blacklistedProvidersSpec, ","); i.hasNext();)
      {
         String className = i.next();
         blacklistedProviderClassNames.add(className.trim());
      }

      List<T> providers;
      try
      {
         if (extLog.isDebugEnabled())
         {
            extLog.debug(MessageFormat.format(
                  "Scanning classpath for service providers for SPI type {0}",
                  providerIntfc));
         }

         providers = FactoryFinder.findFactories(providerIntfc, null, null,
               providerClassNames, blacklistedProviderClassNames);
      }
      catch (FactoryFinder.ConfigurationError ce)
      {
         providers = emptyList();
      }

      if (extLog.isDebugEnabled())
      {
         extLog.debug(MessageFormat.format(
               "Discovered {1,choice,0#no|0<{1}} service {1,choice,0#provider|1<providers} for SPI type {0}: {2}",
               providerIntfc, providers.size(), providers));
      }

      return providers;
   }

   public <T> List<T> resolveExtensionProviders(Class<T> providerIntfc, Map<String, ?> resolutionConfig)
   {
      if ( !resolvedProviders.containsKey(providerIntfc.getName()))
      {
         List<T> providers = resolveServiceProvidersFromClasspath(providerIntfc);

         resolvedProviders.putIfAbsent(providerIntfc.getName(), providers);
      }

      @SuppressWarnings("unchecked")
      List<T> providers = (List<T>) resolvedProviders.get(providerIntfc.getName());

      return providers;
   }
}
