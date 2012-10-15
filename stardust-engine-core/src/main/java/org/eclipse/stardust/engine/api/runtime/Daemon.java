/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Date;

/**
 * The <code>Daemon</code> class represents a snapshot of a Carnot workflow daemon.
 * <p>Carnot workflow engine contains two tipes of daemons: the event daemon and the
 * trigger daemons.<br>
 * Daemons can be started, stopped and queried using the {@link AdministrationService}.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Daemon extends Serializable
{
   /**
    * Returns the type of the daemon. Predefined values are:
    * <ul>
    * <li><code>mail.trigger</code> for the mail trigger daemon
    * <li><code>timer.trigger</code> for the timer trigger daemon
    * <li><code>event.daemon</code> for the event daemon
    * <li><code>system.daemon</code> for the notification daemon.</li>
    * <li><code>criticality.daemon</code> for the criticality daemon.</li>
    * </ul>
    *
    * @return the type of the daemon
    */
   String getType();

   /**
    * Returns the time of the last daemon start.
    *
    * @return the start time
    */
   Date getStartTime();

   /**
    * Returns the time of the last daemon execution.
    *
    * @return the last execution time
    */
   Date getLastExecutionTime();

   /**
    * Returns whether the daemon is currently up and running.
    *
    * @return true, if the daemon is running, otherwise false
    *       This flag is determined heuristically based on start time,
    *       last execution time, daemon periodicity, current time and acknowledgement state.
    */
   boolean isRunning();

   /**
    * Returns the acknowledgement state of the daemon query.
    * @return acknowledgement state or null if the daemon operation was done without
    *         acknowledgement
    */
   AcknowledgementState getAcknowledgementState();

   /**
    * Returns the execution state of the daemon.
    * @return execution state
    */
   DaemonExecutionState getDaemonExecutionState();
}
