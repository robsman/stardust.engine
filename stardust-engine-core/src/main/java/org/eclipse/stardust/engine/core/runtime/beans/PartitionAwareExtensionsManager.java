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
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.config.extensions.ExtensibleExtensionsManager;
import org.eclipse.stardust.common.config.extensions.ExtensionsManager;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


public class PartitionAwareExtensionsManager implements ExtensionsManager
{
   private final ExtensionsManager globalExtensionsManager;

   private final ConcurrentMap<String, ExtensionsManager> perTenantExtensionsManagers = newConcurrentHashMap();

   public PartitionAwareExtensionsManager()
   {
      this.globalExtensionsManager = new ExtensibleExtensionsManager();
   }

   public String getPartitionId()
   {
      IAuditTrailPartition partition = SecurityProperties.getPartition();

      return (null != partition) ? partition.getId() : null;
   }

   public <T> T getFirstExtensionProvider(Class<T> providerIntfc,
         String configurationProperty)
   {
      ExtensionsManager currentExtensionManager = getCurrentExtensionManager();

      return currentExtensionManager.getFirstExtensionProvider(providerIntfc,
            configurationProperty);
   }

   public <T> List<T> getExtensionProviders(Class<T> providerIntfc, String configurationProperty)
   {
      ExtensionsManager currentExtensionManager = getCurrentExtensionManager();

      return currentExtensionManager.getExtensionProviders(providerIntfc,
            configurationProperty);
   }

   private ExtensionsManager getCurrentExtensionManager()
   {
      String tenantId = getPartitionId();
      if ( !isEmpty(tenantId))
      {
         ExtensionsManager localExtensionsManager = perTenantExtensionsManagers.get(tenantId);
         if (null == localExtensionsManager)
         {
            perTenantExtensionsManagers.putIfAbsent(tenantId,
                  new ExtensibleExtensionsManager());
            localExtensionsManager = perTenantExtensionsManagers.get(tenantId);
         }

         return localExtensionsManager;
      }
      else
      {
         return globalExtensionsManager;
      }
   }
}
