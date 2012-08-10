package org.eclipse.stardust.engine.core.spi.cluster;

import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * @author Nicolas.Werlein
 * @version $Revision: $
 * 
 * TODO (nw) change status as soon as it's stable
 */
@SPI(status = Status.Experimental, useRestriction = UseRestriction.Internal)
public interface ClusterSafeObjectProvider
{
   public <K, V> Map<K, V> clusterSafeMap(final String mapId);
   
   public Lock clusterSafeLock(final String lockId);
   
   public void beforeAccess();
   
   public void afterAccess();
}
