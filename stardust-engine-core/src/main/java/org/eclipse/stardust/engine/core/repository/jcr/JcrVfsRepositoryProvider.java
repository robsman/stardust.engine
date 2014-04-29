/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.repository.jcr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstance;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProvider;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderManager;

public class JcrVfsRepositoryProvider implements IRepositoryProvider, IRepositoryProvider.Factory
{

   public static final String PROVIDER_ID = "jcr-vfs";

   public IRepositoryProvider getInstance()
   {
      return new JcrVfsRepositoryProvider();
   }

   @Override
   public String getProviderId()
   {
      return PROVIDER_ID;
   }

   @Override
   public List<IRepositoryConfiguration> getDefaultConfigurations()
   {
      List<IRepositoryConfiguration> configurations = CollectionUtils.newArrayList();

      Map<String, Serializable> defaultInstance = CollectionUtils.newHashMap();
      defaultInstance.put(IRepositoryConfiguration.PROVIDER_ID, PROVIDER_ID);
      defaultInstance.put(IRepositoryConfiguration.REPOSITORY_ID, RepositoryProviderManager.SYSTEM_REPOSITORY_ID);
      defaultInstance.put(JcrVfsRepositoryConfiguration.IS_DEFAULT_REPOSITORY, "true");
      defaultInstance.put(JcrVfsRepositoryConfiguration.JNDI_NAME, getDefaultJndiName());
      configurations.add(new JcrVfsRepositoryConfiguration(defaultInstance));

      return configurations;
   }

   private String getDefaultJndiName()
   {
      final Parameters params = Parameters.instance();
      return params.getString("Jcr.ContentRepository", "jcr/ContentRepository");
   }

   @Override
   public IRepositoryInstance createInstance(IRepositoryConfiguration configuration, String partitionId)
   {
      if (configuration.getAttributes().containsKey(JcrVfsRepositoryConfiguration.JNDI_NAME))
      {
         return new JcrVfsRepositoryInstance(configuration, partitionId);
      }
      else
      {
         throw new DocumentManagementServiceException(
               BpmRuntimeError.DMS_REPOSITORY_CONFIGURATION_PARAMETER_IS_NULL.raise(JcrVfsRepositoryConfiguration.JNDI_NAME));
      }
   }

   @Override
   public void destroyInstance(IRepositoryInstance instance)
   {
      instance.close(null);
   }

   @Override
   public IRepositoryProviderInfo getProviderInfo()
   {
      return new JcrVfsRepositoryProviderInfo();
   }

}
