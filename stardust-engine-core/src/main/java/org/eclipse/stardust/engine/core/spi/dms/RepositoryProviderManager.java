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
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProvider.Factory;

public class RepositoryProviderManager
{
   
   private static String INSTANCE_CACHE_KEY = IRepositoryInstance.class.getName()
         + ".cache";

   private static RepositoryProviderManager INSTANCE;
   
   private Map<String, IRepositoryProvider> providers = CollectionUtils.newHashMap();
   
   private ConcurrentHashMap<String, IRepositoryInstance> instances;
   
   private String defaultRepositoryId = "default";

   public RepositoryProviderManager()
   {
      ServiceLoader<IRepositoryProvider.Factory> loader = ServiceLoader.load(IRepositoryProvider.Factory.class);
      Iterator<Factory> loaderIterator = loader.iterator();
      while (loaderIterator.hasNext())
      {
         IRepositoryProvider.Factory providerFactory = (IRepositoryProvider.Factory) loaderIterator.next();
         IRepositoryProvider provider = providerFactory.getInstance();
         providers.put(provider.getProviderId(), provider); 
      }
      
      initializeInstanceCache();
      
      registerDefaultInstances();
   }
   
   public static RepositoryProviderManager getInstance()
   {
      if (INSTANCE == null)
      {
         synchronized (RepositoryProviderManager.class)
         {
            if (INSTANCE == null)
            {
               INSTANCE = new RepositoryProviderManager();
            }
         }
      }
      return INSTANCE;
   }

   private void initializeInstanceCache()
   {
      final GlobalParameters globals = GlobalParameters.globals();
      Map instanceCache = (Map) globals.get(INSTANCE_CACHE_KEY);
      if (instanceCache == null)
      {
         synchronized (INSTANCE_CACHE_KEY)
         {
            instanceCache = (Map) globals.get(INSTANCE_CACHE_KEY);
            if (instanceCache == null)
            {
               instanceCache = new ConcurrentHashMap<String, IRepositoryInstance>();
               globals.set(INSTANCE_CACHE_KEY, instanceCache);
            }
         }
      }
      this.instances = (ConcurrentHashMap<String, IRepositoryInstance>) instanceCache;
   }
   
   private void registerDefaultInstances()
   {
      for (IRepositoryProvider provider : providers.values())
      {
         List<IRepositoryConfiguration> defaultInstanceConfigurations = provider.getDefaultConfigurations();
         for (IRepositoryConfiguration configuration : defaultInstanceConfigurations)
         {
            String repositoryId = (String) configuration.getAttributes().get(IRepositoryConfiguration.REPOSITORY_ID);
            if ( !instances.containsKey(repositoryId))
            {
               bindRepository(configuration);
            }
            else
            {
               // instance already registered
               String providerId = (String) configuration.getAttributes().get(IRepositoryConfiguration.PROVIDER_ID);
               IRepositoryInstance boundInstance = instances.get(repositoryId);
               if ( !boundInstance.getProviderId().equals(providerId))
               {
                  throw new PublicException("Loading default providers failed: The repositoryId '"
                        + repositoryId + "' is already bound by provider '" + providerId
                        + "'.");
               }
            }
         }
      }
   }

   public String getDefaultRepositoryId()
   {
      return defaultRepositoryId;
   }

   public void bindRepository(IRepositoryConfiguration configuration)
   {
      String providerId = (String) configuration.getAttributes().get(IRepositoryConfiguration.PROVIDER_ID);
      String repositoryId = (String) configuration.getAttributes().get(IRepositoryConfiguration.REPOSITORY_ID);
      IRepositoryProvider provider = providers.get(providerId);
      if (provider == null)
      {
         throw new PublicException("The repository provider '"+ providerId +"' was not found.");
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
            throw new PublicException("The repositoryId '" + repositoryId
                  + "' is already bound. ");
         }
      }
   }
   
   public void unbindRepository(String repositoryId)
   {
      if (repositoryId == null || defaultRepositoryId.equals(repositoryId))
      {
         throw new PublicException("Unbinding the default repository is not allowed!");
      }
      
      IRepositoryInstance instance = instances.get(repositoryId);
      if (instance == null)
      {
         throw new PublicException("This repositoryId '" + repositoryId
               + "' is not bound: ");
      }
      IRepositoryProvider provider = providers.get(instance.getProviderId());
      provider.destroyInstance(instance);
      instances.remove(repositoryId);
   }

   public IRepositoryService getImplicitInstance()
   {
      return new RepositoryIdMediator(this);
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
         throw new PublicException("No instance was found for '" + repositoryId + "'.");
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
