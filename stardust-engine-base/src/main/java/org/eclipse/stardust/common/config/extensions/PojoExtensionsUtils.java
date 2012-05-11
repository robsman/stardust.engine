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

import static org.eclipse.stardust.common.config.ConfigLog.EXTENSIONS_LOG;

import java.util.List;

import org.eclipse.stardust.common.annotations.Stateless;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;


public class PojoExtensionsUtils
{
   private static final Logger trace = LogManager.getLogger(PojoExtensionsUtils.class);

   public static <T, E extends T> T instantiatePojoExtension(Class<E> extensionType)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("About to instantiate SPI provider " + extensionType);
      }

      T instance = extensionType.cast(Reflect.createInstance(extensionType.getName()));

      if (trace.isDebugEnabled())
      {
         trace.debug("Instantiated SPI provider " + instance);
      }

      return instance;
   }

   public static <T> PojoExtensionsCache<T> buildPojoExtensionsCache(
         Class<T> providerIntfc, List<Class<? >> providerTypes)
   {
      PojoExtensionsCache<T> providers = new PojoExtensionsCache<T>(providerTypes.size());
      for (Class<? > potentialProviderType : providerTypes)
      {
         if ( !providerIntfc.isAssignableFrom(potentialProviderType))
         {
            EXTENSIONS_LOG.warn("Ignoring incompatible implementation for SPI type "
                  + providerIntfc + ": " + potentialProviderType);
            continue;
         }

         @SuppressWarnings("unchecked")
         Class<? extends T> providerType = (Class<? extends T>) potentialProviderType;

         if (providerType.isAnnotationPresent(Stateless.class))
         {
            if (EXTENSIONS_LOG.isDebugEnabled())
            {
               EXTENSIONS_LOG.debug("Registered stateless provider for SPI type "
                     + providerIntfc + ": " + providerType);
            }

            providers.addExtensionProvider(providerType, true);
         }
         else
         {
            if (EXTENSIONS_LOG.isDebugEnabled())
            {
               EXTENSIONS_LOG.debug("Registered stateful provider for SPI type "
                     + providerIntfc + ": " + providerType);
            }

            providers.addExtensionProvider(providerType, false);
         }
      }
      return providers;
   }

}
