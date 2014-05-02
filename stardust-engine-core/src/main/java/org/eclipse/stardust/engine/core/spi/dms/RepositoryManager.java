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
package org.eclipse.stardust.engine.core.spi.dms;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProvider.Factory;

/**
 * Manages access and lifecycle for all registered {@link IRepositoryProvider}.
 * <p>
 * Via {@link #getImplicitService()} a {@link IRepositoryService} can be retrieved that
 * chooses the correct {@link IRepositoryInstance} implicitly by the repositoryId that is contained in
 * documentId or folderId of the method call parameters.
 *
 * @author Roland.Stamm
 */
public class RepositoryManager
{
   /**
    * The repositoryId of the system repository.
    * Only one repository can register as the system repository using this repositoryId.
    */
   public static String SYSTEM_REPOSITORY_ID = "System";

   private static Logger trace = LogManager.getLogger(RepositoryManager.class);

   private static RepositoryManager INSTANCE;

   private Map<String, IRepositoryProvider> providers = CollectionUtils.newHashMap();

   private RepositoryInstanceCache instances;

   private String defaultRepositoryId = SYSTEM_REPOSITORY_ID;

   private RepositoryIdMediator repositoryIdMediator;

   public RepositoryManager()
   {
      ServiceLoader<IRepositoryProvider.Factory> loader = ServiceLoader.load(IRepositoryProvider.Factory.class);
      Iterator<Factory> loaderIterator = loader.iterator();
      while (loaderIterator.hasNext())
      {
         IRepositoryProvider.Factory providerFactory = (IRepositoryProvider.Factory) loaderIterator.next();
         IRepositoryProvider provider = providerFactory.getInstance();
         providers.put(provider.getProviderId(), provider);
      }

      instances = new RepositoryInstanceCache();

      repositoryIdMediator = new RepositoryIdMediator(this);

      registerDefaultInstances();

      loadConfigurations();
   }

   public static RepositoryManager getInstance()
   {
      if (INSTANCE == null)
      {
         synchronized (RepositoryManager.class)
         {
            if (INSTANCE == null)
            {
               INSTANCE = new RepositoryManager();
            }
         }
      }
      return INSTANCE;
   }

   private void registerDefaultInstances()
   {
      for (IRepositoryProvider provider : providers.values())
      {
         List<IRepositoryConfiguration> defaultInstanceConfigurations = provider.getDefaultConfigurations();
         for (IRepositoryConfiguration configuration : defaultInstanceConfigurations)
         {
            String repositoryId = (String) configuration.getAttributes().get(
                  IRepositoryConfiguration.REPOSITORY_ID);
            synchronized (instances)
            {
               if ( !instances.containsGlobalKey(repositoryId))
               {
                  IRepositoryInstance instance = provider.createInstance(configuration,
                        SecurityProperties.getPartition().getId());
                  instances.putGlobal(repositoryId, instance);
               }
               else
               {
                  // instance already registered
                  String providerId = (String) configuration.getAttributes().get(
                        IRepositoryConfiguration.PROVIDER_ID);
                  IRepositoryInstance boundInstance = instances.getGlobal(repositoryId);
                  if ( !boundInstance.getProviderId().equals(providerId))
                  {
                     throw new DocumentManagementServiceException(
                           BpmRuntimeError.DMS_REPOSITORY_DEFAULT_LOAD_FAILED.raise(
                                 repositoryId, providerId));
                  }
               }
            }
         }
      }
   }

   private void loadConfigurations()
   {
      List<IRepositoryConfiguration> configurations = RepositoryProviderUtils.getAllConfigurations();

      for (IRepositoryConfiguration configuration : configurations)
      {
         try
         {
            bindRepository(configuration);
         }
         catch (Exception e)
         {
            trace.error("IRepositoryConfiguration could not be loaded.", e);
         }
      }
   }

   public void bindRepository(IRepositoryConfiguration configuration)
   {
      String providerId = (String) configuration.getAttributes().get(IRepositoryConfiguration.PROVIDER_ID);
      String repositoryId = (String) configuration.getAttributes().get(IRepositoryConfiguration.REPOSITORY_ID);
      if (StringUtils.isEmpty(providerId))
      {
         throw new DocumentManagementServiceException(
               BpmRuntimeError.DMS_REPOSITORY_CONFIGURATION_PARAMETER_IS_NULL.raise(IRepositoryConfiguration.PROVIDER_ID));
      }
      if (StringUtils.isEmpty(repositoryId))
      {
         throw new DocumentManagementServiceException(
               BpmRuntimeError.DMS_REPOSITORY_CONFIGURATION_PARAMETER_IS_NULL.raise(IRepositoryConfiguration.REPOSITORY_ID));
      }

      IRepositoryProvider provider = providers.get(providerId);
      if (provider == null)
      {
         throw new DocumentManagementServiceException(BpmRuntimeError.DMS_REPOSITORY_PROVIDER_NOT_FOUND.raise(providerId));
      }

      synchronized (instances)
      {
         if ( !instances.containsKey(repositoryId))
         {
            IRepositoryInstance instance = provider.createInstance(configuration, SecurityProperties.getPartition().getId());
            instances.put(repositoryId, instance);
         }
         else
         {
            throw new DocumentManagementServiceException(BpmRuntimeError.DMS_REPOSITORY_INSTANCE_ALREADY_BOUND.raise(repositoryId));
         }
         RepositoryProviderUtils.saveConfiguration(configuration);
      }
   }

   public void unbindRepository(String repositoryId)
   {
      if (repositoryId == null)
      {
         throw new IllegalArgumentException("null");
      }
      if (defaultRepositoryId.equals(repositoryId))
      {
         throw new DocumentManagementServiceException(BpmRuntimeError.DMS_REPOSITORY_DEFAULT_UNBIND.raise());
      }
      if (SYSTEM_REPOSITORY_ID.equals(repositoryId))
      {
         throw new DocumentManagementServiceException(BpmRuntimeError.DMS_REPOSITORY_SYSTEM_UNBIND.raise());
      }

      IRepositoryInstance instance = instances.get(repositoryId);
      if (instance != null)
      {

         IRepositoryProvider provider = providers.get(instance.getProviderId());
         provider.destroyInstance(instance);
         instances.remove(repositoryId);
      }
      RepositoryProviderUtils.removeConfiguration(repositoryId);
   }

   public String getDefaultRepository()
   {
      return defaultRepositoryId;
   }

   public void setDefaultRepository(String repositoryId)
   {
      if (repositoryId != null && !instances.containsKey(repositoryId))
      {
         throw new DocumentManagementServiceException(BpmRuntimeError.DMS_REPOSITORY_INSTANCE_NOT_FOUND.raise(repositoryId));
      }

      this.defaultRepositoryId = repositoryId == null
            ? SYSTEM_REPOSITORY_ID
            : repositoryId;
   }

   public IRepositoryService getImplicitService()
   {
      return repositoryIdMediator;
   }

   public IRepositoryInstance getInstance(String repositoryId)
   {
      String repoId = repositoryId == null ? defaultRepositoryId : repositoryId;

      IRepositoryInstance instance = instances.get(repoId);
      if (instance != null)
      {
         return instance;
      }
      else
      {
         throw new DocumentManagementServiceException(BpmRuntimeError.DMS_REPOSITORY_INSTANCE_NOT_FOUND.raise(repositoryId));
      }
   }

   public List<IRepositoryInstanceInfo> getAllInstanceInfos()
   {
      List<IRepositoryInstanceInfo> repositoryInfos = CollectionUtils.newArrayList();

      for (IRepositoryInstance instance : instances.values())
      {
         repositoryInfos.add(instance.getRepositoryInstanceInfo());
      }
      return repositoryInfos;
   }

   public List<IRepositoryProviderInfo> getAllProviderInfos()
   {
      List<IRepositoryProviderInfo> repositoryInfos = CollectionUtils.newArrayList();
      for (IRepositoryProvider provider : providers.values())
      {
         repositoryInfos.add(provider.getProviderInfo());
      }
      return repositoryInfos;
   }

}
