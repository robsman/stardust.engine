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
import java.util.concurrent.locks.Lock;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider;

import com.hazelcast.core.Hazelcast;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public abstract class ClusteredEnvHazelcastObjectProvider implements ClusterSafeObjectProvider
{
   @Override
   public <K, V> Map<K, V> clusterSafeMap(final String mapId)
   {
      return Hazelcast.getMap(mapId);
   }
   
   @Override
   public Lock clusterSafeLock(final String lockId)
   {
      return Hazelcast.getLock(lockId);
   }
   
   @Override
   public void beforeAccess()
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      final ConnectionFactory connectionFactory = connectionFactory();
      
      try
      {
         /* enlists Hazelcast objects in the current tx */
         rtEnv.retrieveJcaConnection(connectionFactory);
      }
      catch (final ResourceException e)
      {
         throw new PublicException("Failed enlisting Hazelcast objects in the current transaction.", e);
      }
   }
   
   @Override
   public void afterAccess()
   {
      /* nothing to do */
   }

   protected abstract ConnectionFactory connectionFactory();
}
