/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc.transientpi;

import java.util.Map;

import javax.resource.ResourceException;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider;
import org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.engine.runtime.utils.HazelcastUtils;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Transaction;

/**
 * <p>
 * A <i>Hazelcast</i> implementation of {@link ClusterSafeObjectProvider} which is
 * transaction aware, i.e. it enlists in the running <i>JTA</i> transaction so that
 * operations on the objects returned are transactional.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class ClusteredEnvHazelcastObjectProvider implements ClusterSafeObjectProvider
{
   private static volatile ClusteredEnvHazelcastObjectProviderHolder holder;

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#clusterSafeMap(java.lang.String)
    */
   @Override
   public <K, V> Map<K, V> clusterSafeMap(final String mapId)
   {
      if (mapId == null)
      {
         throw new NullPointerException("Map ID for Hazelcast map must not be null.");
      }

      return holder().hzInstance.getMap(mapId);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#beforeAccess()
    */
   @Override
   public void beforeAccess()
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      try
      {
         /* Hazelcast can only cope with one transaction per thread */
         if (holder().hzInstance.getTransaction().getStatus() != Transaction.TXN_STATUS_ACTIVE)
         {
            /* enlists Hazelcast objects in the current tx */
            rtEnv.retrieveJcaConnection(holder().hzConnectionFactoryProvider.connectionFactory());
         }
      }
      catch (final ResourceException e)
      {
         throw new PublicException(
               BpmRuntimeError.HZLC_FAILES_ENLISTING_HAZLECAST_OBJECTS_IN_CURRENT_TRANSACTION
                     .raise(), e);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#exception(java.lang.Exception)
    */
   @Override
   public void exception(final Exception ignored)
   {
      /* nothing to do */
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#afterAccess()
    */
   @Override
   public void afterAccess()
   {
      /* nothing to do */
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#reset()
    */
   @Override
   public void reset()
   {
      synchronized (ClusteredEnvHazelcastObjectProvider.class)
      {
         if (holder != null)
         {
            holder.hzConnectionFactoryProvider.reset();
         }

         holder = null;
      }
   }

   private ClusteredEnvHazelcastObjectProviderHolder holder()
   {
      if (holder == null)
      {
         synchronized (ClusteredEnvHazelcastObjectProvider.class)
         {
            if (holder == null)
            {
               holder = new ClusteredEnvHazelcastObjectProviderHolder();
            }
         }
      }
      return holder;
   }

   private static final class ClusteredEnvHazelcastObjectProviderHolder
   {
      private final HazelcastJcaConnectionFactoryProvider hzConnectionFactoryProvider;

      private final HazelcastInstance hzInstance;

      public ClusteredEnvHazelcastObjectProviderHolder()
      {
         hzConnectionFactoryProvider = ExtensionProviderUtils.getFirstExtensionProvider(HazelcastJcaConnectionFactoryProvider.class, KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER);
         if (hzConnectionFactoryProvider == null)
         {
            throw new IllegalStateException("No Hazelcast JCA connection factory provider could be found.");
         }
         /* bootstraps the Hazelcast Resource Adapter */
         hzConnectionFactoryProvider.connectionFactory();

         hzInstance = HazelcastUtils.getHazelcastInstance();
      }
   }
}
