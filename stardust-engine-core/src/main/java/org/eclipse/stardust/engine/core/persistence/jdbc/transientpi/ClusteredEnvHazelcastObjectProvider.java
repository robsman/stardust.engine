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
import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider;
import org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Transaction;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ClusteredEnvHazelcastObjectProvider implements ClusterSafeObjectProvider
{
   private static final HazelcastJcaConnectionFactoryProvider CONNECTION_FACTORY_PROVIDER;
   
   static
   {
      CONNECTION_FACTORY_PROVIDER = ExtensionProviderUtils.getFirstExtensionProvider(HazelcastJcaConnectionFactoryProvider.class, KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER);
      if (CONNECTION_FACTORY_PROVIDER == null)
      {
         throw new IllegalStateException("No Hazelcast JCA connection factory provider could be found.");
      }
   }
   
   @Override
   public <K, V> Map<K, V> clusterSafeMap(final String mapId)
   {
      if (mapId == null)
      {
         throw new NullPointerException("Map ID for Hazelcast map must not be null.");
      }
      
      return Hazelcast.getMap(mapId);
   }

   @Override
   public void beforeAccess()
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      final ConnectionFactory connectionFactory = CONNECTION_FACTORY_PROVIDER.connectionFactory();
      
      try
      {
         /* Hazelcast can only cope with one transaction per thread */
         if (Hazelcast.getTransaction().getStatus() != Transaction.TXN_STATUS_ACTIVE)
         {
            /* enlists Hazelcast objects in the current tx */
            rtEnv.retrieveJcaConnection(connectionFactory);
         }
      }
      catch (final ResourceException e)
      {
         throw new PublicException("Failed enlisting Hazelcast objects in the current transaction.", e);
      }
   }
   
   @Override
   public void exception(final Exception ignored)
   {
      /* nothing to do */
   }
   
   @Override
   public void afterAccess()
   {
      /* nothing to do */
   }
}
