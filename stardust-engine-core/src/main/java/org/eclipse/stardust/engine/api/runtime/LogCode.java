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
 * Wrapper class for LogEntry codes.
 * It provides human readable names for the log codes.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LogCode extends IntKey
{
   /**
    * Unknown context.
    */
   public static final LogCode UNKNOWN = new LogCode(0, "Unknown");
   /**
    * The LogEntry was created in the Process Warehouse context.
    */
   public static final LogCode PWH = new LogCode(1, "Process Warehouse");
   /**
    * The LogEntry was created in the security context.
    */
   public static final LogCode SECURITY = new LogCode(2, "Security");
   /**
    * The LogEntry was created in the engine context.
    */
   public static final LogCode ENGINE = new LogCode(3, "Engine");
   /**
    * The LogEntry was created during recovery.
    */
   public static final LogCode RECOVERY = new LogCode(4, "Recovery");
   /**
    * The LogEntry was created during daemon execution.
    */
   public static final LogCode DAEMON = new LogCode(5, "Daemon");
   /**
    * The LogEntry was created during event handling.
    */
   public static final LogCode EVENT = new LogCode(6, "Event");
   
   /**
    * The LogEntry was created by an external invocation.
    */
   public static final LogCode EXTERNAL = new LogCode(8, "External");
   
   /**
    * The LogEntry was created by an external administration event.
    */
   public static final LogCode ADMINISTRATION = new LogCode(9, "Administration");

   private LogCode(int code, String defaultName)
   {
      super(code, defaultName);
   }

   /**
    * Factory method to get the LogCode object corresponding to the numerical code.
    *
    * @param code the numeric code of the LogCode.
    *
    * @return the LogCode object.
    */
   public static LogCode getKey(int code)
   {
      LogCode result = (LogCode) getKey(LogCode.class, code);
      return (null != result) ? result : UNKNOWN;
   }
}

