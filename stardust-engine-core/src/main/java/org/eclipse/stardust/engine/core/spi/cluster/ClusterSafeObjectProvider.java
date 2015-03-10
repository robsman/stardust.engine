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
package org.eclipse.stardust.engine.core.spi.cluster;

import java.util.Map;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * <p>
 * An SPI allowing to specify which cluster safe object provider to use.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
@SPI(status = Status.Stable, useRestriction = UseRestriction.Internal)
public interface ClusterSafeObjectProvider
{
   /**
    * <p>
    * Returns a cluster safe map for the given map ID. If it does not yet exist, it will be created first.
    * Same map ID means that the same map is returned.
    * </p>
    *
    * @param <K> the type of the map keys
    * @param <V> the type of the map values
    * @param mapId the ID of the map to be returned; must not be <code>null</code>
    * @return the map for the given map ID
    */
   public <K, V> Map<K, V> clusterSafeMap(final String mapId);

   /**
    * <p>
    * Will be called before each and every operation on an object retrieved from the cluster safe object provider.
    * </p>
    */
   public void beforeAccess();

   /**
    * <p>
    * Will be called in case an exception occured during access of an object retrieved from the cluster safe object provider.
    * </p>
    *
    * @param e the exception raised
    */
   public void exception(final Exception e);

   /**
    * <p>
    * Will be called after each and every operation on an object retrieved from the cluster safe object provider.
    * </p>
    */
   public void afterAccess();

   /**
    * Resets the {@link ClusterSafeObjectProvider} to the uninitialized state such that a subsequent call other than
    * {@link #reset()} causes a reinitialization of this {@link ClusterSafeObjectProvider}.
    */
   public void reset();
}
