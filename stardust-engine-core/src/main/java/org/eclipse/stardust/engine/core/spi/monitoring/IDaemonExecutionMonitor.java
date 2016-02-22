/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
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
import org.eclipse.stardust.engine.api.runtime.Daemon;

/**
 * Interface to monitor on daemon execution
 * 
 * @author thomas.wolfram
 *
 */

@SPI(useRestriction = UseRestriction.Internal, status = Status.Experimental)
public interface IDaemonExecutionMonitor
{

   void beforeExecute(Daemon daemon);
   
   void afterExecute(Daemon daemon);
   
}
