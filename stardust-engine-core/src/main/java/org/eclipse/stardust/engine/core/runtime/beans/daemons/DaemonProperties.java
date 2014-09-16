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
package org.eclipse.stardust.engine.core.runtime.beans.daemons;

public interface DaemonProperties
{
   static final String DAEMON_BATCH_SIZE_SUFFIX = ".BatchSize";

   static final String DAEMON_PERIODICITY_SUFFIX = ".Periodicity";

   static final String DAEMON_RETRY_NUMBER = "Stardust.Engine.Daemon.Aknowledge.Retry";

   static final String DAEMON_RETRY_DELAY = "Stardust.Engine.Daemon.Aknowledge.Delay";
}
