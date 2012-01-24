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
 * Wrapper class for the log entry types.
 * It provides human readable names for the log type codes.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LogType extends IntKey
{
   /**
    * The LogEntry contains unknown information.
    */
   public static final int UNKNOWN = 0;
   /**
    * The LogEntry contains debug information.
    */
   public static final int DEBUG = 1;
   /**
    * The LogEntry contains information.
    */
   public static final int INFO = 3;
   /**
    * The LogEntry contains a warning.
    */
   public static final int WARN = 5;
   /**
    * The LogEntry contains an error.
    */
   public static final int ERROR = 7;
   /**
    * The LogEntry contains a fatal error.
    */
   public static final int FATAL = 9;

   public static final LogType Unknwon = new LogType(UNKNOWN, "Unknown");
   public static final LogType Debug = new LogType(DEBUG, "Debug");
   public static final LogType Info = new LogType(INFO, "Info");
   public static final LogType Warn = new LogType(WARN, "Warn");
   public static final LogType Error = new LogType(ERROR, "Error");
   public static final LogType Fatal = new LogType(FATAL, "Fatal");

   private LogType(int id, String defaultName)
   {
      super(id, defaultName);
   }

   /**
    * Factory method to get the LogType object corresponding to the numerical code.
    *
    * @param code the numeric code of the LogType.
    *
    * @return the LogType object.
    */
   public static LogType getKey(int code)
   {
      LogType result = (LogType) getKey(LogType.class, code);
      return (null != result) ? result : Unknwon;
   }
}
