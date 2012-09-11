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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider;

/**
 * <p>
 * For testing purposes only.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revisin: $
 */
public class NonClusteredEnvObjectProvider implements ClusterSafeObjectProvider
{
   private static final Lock LOCK = new ReentrantLock();
   
   @Override
   public <K, V> Map<K, V> clusterSafeMap(final String ignored)
   {
      return new ConcurrentHashMap<K, V>();
   }
   
   @Override
   public void beforeAccess()
   {
      LOCK.lock();
   }
   
   @Override
   public void afterAccess()
   {
      LOCK.unlock();
   }
}
