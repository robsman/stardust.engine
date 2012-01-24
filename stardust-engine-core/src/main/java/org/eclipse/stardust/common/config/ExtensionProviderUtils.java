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

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.config.ConfigLog.EXTENSIONS_LOG;
import static org.eclipse.stardust.common.config.extensions.ServiceProviderResolver.resolveFirstServiceProviderFromClasspath;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.config.extensions.ExtensibleExtensionsManager;
import org.eclipse.stardust.common.config.extensions.ExtensionsManager;


/**
 * @author robert.sauer
 * @version $Revision$
 */
public class ExtensionProviderUtils
{
   private static final String SUFFIX_RESOLVED_PROVIDERS_PER_CLASSLOADER = ".ResolvedExtensionProvidersPerClassLoader";

   public static <T> List<T> getExtensionProviders(Class<T> providerIntfc)
   {
      return getExtensionProviders(providerIntfc, null);
   }

   public static <T> List<T> getExtensionProviders(Class<T> providerIntfc,
         String configurationProperty)
   {
      ExtensionsManager extensionsManager = getExtensionsManager();

      return extensionsManager.getExtensionProviders(providerIntfc, configurationProperty);
   }

   public static <T> T getFirstExtensionProvider(Class<T> providerIntfc)
   {
      return getFirstExtensionProvider(providerIntfc, null);
   }

   public static <T> T getFirstExtensionProvider(Class<T> providerIntfc,
         String configurationProperty)
   {
      ExtensionsManager extensionsManager = getExtensionsManager();

      return extensionsManager.getFirstExtensionProvider(providerIntfc,
            configurationProperty);
   }

   public static <T> ExtensionsManager getExtensionsManager()
   {
      GlobalParameters globals = GlobalParameters.globals();

      ConcurrentMap<ClassLoader, ExtensionsManager> classLoaderMap = getExtensionsManagerCache(globals);

      ClassLoader classLoader = getClassLoader(ExtensionProviderUtils.class);

      ExtensionsManager extensionsManager = classLoaderMap.get(classLoader);
      if (null == extensionsManager)
      {
         ExtensionsManager globalExtensionsManager = resolveFirstServiceProviderFromClasspath(
               ExtensionsManager.class, ExtensibleExtensionsManager.class);

         EXTENSIONS_LOG.info("Using extensions manager: " + globalExtensionsManager);

         classLoaderMap.putIfAbsent(classLoader, globalExtensionsManager);
         extensionsManager = classLoaderMap.get(classLoader);
      }
      return extensionsManager;
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static ConcurrentMap<ClassLoader, ExtensionsManager> getExtensionsManagerCache(GlobalParameters globals)
   {
      ConcurrentMap<ClassLoader, ExtensionsManager> classLoaderMap = (ConcurrentMap) globals.get(SUFFIX_RESOLVED_PROVIDERS_PER_CLASSLOADER);
      if (null == classLoaderMap)
      {
         classLoaderMap = (ConcurrentMap) globals.getOrInitialize(
               SUFFIX_RESOLVED_PROVIDERS_PER_CLASSLOADER, newConcurrentHashMap());
      }
      return classLoaderMap;
   }

   private static ClassLoader getClassLoader(Class<? > providerType)
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (classLoader == null)
      {
         classLoader = providerType.getClassLoader();
      }
      return classLoader;
   }

   private ExtensionProviderUtils()
   {
      // utility class
   }

}
