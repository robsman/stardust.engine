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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Transaction;

/**
 * <p>
 * For testing purposes only.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revisin: $
 */
public class TestingEnvObjectProvider implements ClusterSafeObjectProvider
{
   @Override
   public <K, V> Map<K, V> clusterSafeMap(final String mapId)
   {
      return Hazelcast.getMap(mapId);
   }
   
   @Override
   public void beforeAccess()
   {
      Hazelcast.getTransaction().begin();
   }
   
   @Override
   public void exception(final Exception ignored)
   {
      Hazelcast.getTransaction().rollback();
   }
   
   @Override
   public void afterAccess()
   {
      if (Hazelcast.getTransaction().getStatus() == Transaction.TXN_STATUS_ACTIVE)
      {
         Hazelcast.getTransaction().commit();
      }
   }
}
