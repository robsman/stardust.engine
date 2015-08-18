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

import org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider;

import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Transaction;

/**
 * <p>
 * A {@link ClusterSafeObjectProvider} implementaion for testing purposes only
 * (simulates a transaction aware object access).
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revisin: $
 */
public class TestingEnvObjectProvider implements ClusterSafeObjectProvider
{
   private static final HazelcastInstance hzInstance;

   static
   {
      hzInstance = Hazelcast.newHazelcastInstance(new XmlConfigBuilder().build());
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#clusterSafeMap(java.lang.String)
    */
   @Override
   public <K, V> Map<K, V> clusterSafeMap(final String mapId)
   {
      return hzInstance.getMap(mapId);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#beforeAccess()
    */
   @Override
   public void beforeAccess()
   {
      hzInstance.getTransaction().begin();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#exception(java.lang.Exception)
    */
   @Override
   public void exception(final Exception ignored)
   {
      hzInstance.getTransaction().rollback();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#afterAccess()
    */
   @Override
   public void afterAccess()
   {
      if (hzInstance.getTransaction().getStatus() == Transaction.TXN_STATUS_ACTIVE)
      {
         hzInstance.getTransaction().commit();
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider#reset()
    */
   @Override
   public void reset()
   {
      /* nothing to do */
   }
}
