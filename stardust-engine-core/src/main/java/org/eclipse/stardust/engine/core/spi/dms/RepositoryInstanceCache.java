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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class RepositoryInstanceCache
{

   private static String INSTANCE_CACHE_KEY = IRepositoryInstance.class.getName()
         + ".cache";

   private ConcurrentHashMap<Key, IRepositoryInstance> instances;

   public RepositoryInstanceCache()
   {
      initializeInstanceCache();
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
      this.instances = (ConcurrentHashMap<Key, IRepositoryInstance>) instanceCache;
   }

   private String getPartitionId()
   {
      return SecurityProperties.getPartition().getId();
   }

   public boolean containsKey(String repositoryId)
   {
      return instances.containsKey(new Pair<String, String>(null, repositoryId))
            || instances.containsKey(new Pair<String, String>(getPartitionId(),
                  repositoryId));
   }

   public void put(String repositoryId, IRepositoryInstance instance)
   {
      instances.put(new Key(getPartitionId(), repositoryId), instance);
   }

   public IRepositoryInstance get(String repositoryId)
   {
      // try to get partition local instance
      IRepositoryInstance instance = instances.get(new Key(getPartitionId(), repositoryId));
      if (instance == null)
      {
         // fallback to global instance
         instance = instances.get(new Key(null, repositoryId));
      }
      return instance;
   }

   public void remove(String repositoryId)
   {
      instances.remove(new Key(getPartitionId(), repositoryId));
   }

   public Iterable<IRepositoryInstance> values()
   {
      return Collections.unmodifiableCollection(instances.values());
   }

   public boolean containsGlobalKey(String repositoryId)
   {
      return instances.containsKey(new Key(null, repositoryId));
   }

   public IRepositoryInstance getGlobal(String repositoryId)
   {
      return instances.get(new Key(null, repositoryId));
   }

   public void putGlobal(String repositoryId, IRepositoryInstance instance)
   {
     instances.put(new Key(null, repositoryId), instance);
   }
   
   public void removeGlobal(String repositoryId)
   {
      instances.remove(new Key(null, repositoryId));
   }

   private class Key extends Pair<String, String>
   {
      private static final long serialVersionUID = 1L;

      public Key(String partitionId, String repositoryId)
      {
         super(partitionId, repositoryId);
      }
   }

}
