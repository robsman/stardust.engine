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
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;

/**
 * Interface to monitor process execution.
 *
 * @author sauer
 * @version $Revision: $
 */
@SPI(useRestriction = UseRestriction.Public, status = Status.Stable)
public interface IProcessExecutionMonitor
{
   /**
    * Propagate that the Process has been started.
    *
    * @param process Process Instance.
    */
   void processStarted(IProcessInstance process);

   /**
    * Propagate that the Process has been completed.
    *
    * @param process Process Instance.
    */
   void processCompleted(IProcessInstance process);

   /**
    * Propagate that the Process has been aborted.
    *
    * @param process Process Instance.
    */
   void processAborted(IProcessInstance process);

   /**
    * Propagate that the Process has been interrupted.
    *
    * @param process Process Instance.
    */
   void processInterrupted(IProcessInstance process);

   /**
    * Propagate that the Process has been halted.
    *
    * @param process Process Instance.
    */
   void processHalted(IProcessInstance process);

   /**
    * Propagate that the Process has been resumed from Interrupted or Halted.
    *
    * @param process Process Instance.
    */
   void processResumed(IProcessInstance process);

}
