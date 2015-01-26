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

import org.eclipse.stardust.common.IntKey;

/**
 * Represents the execution state of a daemon.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DaemonExecutionState extends IntKey
{
   /**
    * Daemon run shows no problems.
    */
   public static final DaemonExecutionState OK = new DaemonExecutionState(0, "OK");

   /**
    * A problem occured during daemon run but daemon could continue to run.
    */
   public static final DaemonExecutionState Warning = new DaemonExecutionState(2, "Warning");

   /**
    * A problem occured during daemon forcing it to stop.
    */
   public static final DaemonExecutionState Fatal = new DaemonExecutionState(4, "Fatal");
   
   private DaemonExecutionState(int id, String defaultName)
   {
      super(id, defaultName);
   }
}
