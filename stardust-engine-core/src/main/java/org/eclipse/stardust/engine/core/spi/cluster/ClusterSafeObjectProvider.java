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
 * TODO javadoc
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
@SPI(status = Status.Stable, useRestriction = UseRestriction.Internal)
public interface ClusterSafeObjectProvider
{
   public <K, V> Map<K, V> clusterSafeMap(final String mapId);
   
   public void beforeAccess();
   
   public void exception(final Exception e);
   
   public void afterAccess();
}
