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
   @Override
   public <K, V> Map<K, V> clusterSafeMap(final String ignored)
   {
      return new ConcurrentHashMap<K, V>();
   }
   
   @Override
   public Lock clusterSafeLock(final String ignored)
   {
      return new ReentrantLock();
   }
   
   @Override
   public void beforeAccess()
   {
      /* nothing to do */
   }
   
   @Override
   public void afterAccess()
   {
      /* nothing to do */
   }
}
