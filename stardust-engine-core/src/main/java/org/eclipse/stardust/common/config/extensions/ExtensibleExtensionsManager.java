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
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.common.CollectionUtils.copyList;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.config.ConfigLog;
import org.eclipse.stardust.common.config.extensions.spi.ExtensionsResolver;
import org.eclipse.stardust.common.log.Logger;


public class ExtensibleExtensionsManager implements ExtensionsManager
{
   private static Logger extLog = ConfigLog.EXTENSIONS_LOG;

   private final List<ExtensionsResolver> extensionResolvers;

   public ExtensibleExtensionsManager()
   {
      this.extensionResolvers = newArrayList();

      extLog.info("Initializing static extensions resolver");
      extensionResolvers.add(new ApplicationConfigResolver());

      List<ExtensionsResolver> nonDefaultResolvers = ServiceProviderResolver.resolveServiceProvidersFromClasspath(ExtensionsResolver.class);
      for (ExtensionsResolver resolver : nonDefaultResolvers)
      {
         if ( !(resolver instanceof ApplicationConfigResolver)
               && !(resolver instanceof ServiceProviderResolver))
         {
            extLog.info("Initializing non-default extensions resolver: " + resolver);
            extensionResolvers.add(resolver);
         }
      }

      extLog.info("Initializing Service Provider extensions resolver");
      extensionResolvers.add(new ServiceProviderResolver());
   }

   public <T> T getFirstExtensionProvider(Class<T> providerIntfc,
         String configurationProperty)
   {
      List<T> providers = getExtensionProviders(providerIntfc, configurationProperty, true);

      return !isEmpty(providers) ? providers.get(0) : null;
   }

   public <T> List<T> getExtensionProviders(Class<T> providerIntfc,
         String configurationProperty)
   {
      return getExtensionProviders(providerIntfc, configurationProperty, false);
   }

   private <T> List<T> getExtensionProviders(Class<T> providerIntfc,
         String configurationProperty, boolean findFirst)
   {
      boolean resultIsModifiable = false;
      List<T> result = emptyList();

      Map<String, ? > resolutionConfig = singletonMap(
            ApplicationConfigResolver.PRP_CONFIGURATION_PROPERTY, configurationProperty);

      for (ExtensionsResolver resolver : extensionResolvers)
      {
         List<T> providers = resolver.resolveExtensionProviders(providerIntfc, resolutionConfig);
         if (isEmpty(providers))
         {
            // no contributions from this resolver, try next
            continue;
         }
         else if (findFirst)
         {
            // return only first provider
            return singletonList(providers.get(0));
         }

         if (result.isEmpty())
         {
            result = providers;
         }
         else
         {
            if ( !resultIsModifiable)
            {
               // make sure the target list is actually modifiable
               result = copyList(result);
               resultIsModifiable = true;
            }

            result.addAll(providers);
         }
      }

      return result;
   }
}
