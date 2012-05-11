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
package org.eclipse.stardust.common.log;

public class DefaultLogger implements Logger
{
   public static final String WARN = "WARN ";
   public static final String INFO = "INFO ";
   public static final String FATAL = "FATAL";
   public static final String ERROR = "ERROR";
   public static final String DEBUG = "DEBUG";

   private String name;
   private boolean debug;

   DefaultLogger(String name)
   {
      this.name = name;
      debug = LogManager.isDebugEnabled(name);
   }

   public void debug(Object o)
   {
      if (debug)
      {
         log(DEBUG, o);
      }
   }

   public void debug(Object o, Throwable throwable)
   {
      if (debug)
      {
         log(DEBUG, o, throwable);
      }
   }

   public void error(Object o)
   {
      log(ERROR, o);
   }

   public void error(Object o, Throwable throwable)
   {
      log(ERROR, o, throwable);
   }

   public void fatal(Object o)
   {
      log(FATAL, o);
   }

   public void fatal(Object o, Throwable throwable)
   {
      log(FATAL, o, throwable);
   }

   public void info(Object o)
   {
      log(INFO, o);
   }

   public void info(Object o, Throwable throwable)
   {
      log(INFO, o, throwable);
   }

   public void warn(Object o)
   {
      log(WARN, o);
   }

   public void warn(Object o, Throwable throwable)
   {
      log(WARN, o, throwable);
   }

   private void log(String level, Object o, Throwable t)
   {
      System.out.println(level + " " + name + ": " + o);
      t.printStackTrace(System.out);
   }

   private void log(String level, Object o)
   {
      System.out.println(level + " " + name + ": " + o);
   }

   public boolean isInfoEnabled()
   {
      return true;
   }

   public boolean isDebugEnabled()
   {
      return debug;
   }
}
