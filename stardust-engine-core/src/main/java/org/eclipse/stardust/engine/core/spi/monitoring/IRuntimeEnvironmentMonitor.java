/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.monitoring;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;

/**
 * Interface to monitor on runtime environment events.
 *
 * @author sauer
 */
@SPI(useRestriction = UseRestriction.Public, status = Status.Stable)
public interface IRuntimeEnvironmentMonitor
{

   /**
    * Propagate creation of a new audittrail partition.
    *
    * @param partition The new partition.
    */
   void partitionCreated(IAuditTrailPartition partition);

   /**
    * Propagate deletion of an audittrail partition.
    *
    * @param partition The deleted partition.
    */
   void partitionDropped(IAuditTrailPartition partition);

}
