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
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;


/**
 * Interface to monitor activity instance state changes.
 *
 * @author thomas.wolfram
 */
@SPI(useRestriction = UseRestriction.Public, status = Status.Stable)
public interface IActivityInstanceMonitor
{
   /**
    * Propagate a change of the Activity Instance State.
    *
    * @param activity Activity Instance.
    * @param newState The new Activity Instance State.
    */
   void activityInstanceStateChanged(IActivityInstance activity, int newState);
}
