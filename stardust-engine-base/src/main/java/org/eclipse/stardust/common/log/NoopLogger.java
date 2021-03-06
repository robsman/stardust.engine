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

/**
 * A category which suppresses all log output.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class NoopLogger implements Logger
{
   public NoopLogger()
   {}

   public void debug(Object o)
   {}

   public void debug(Object o, Throwable throwable)
   {}

   public void error(Object o)
   {}

   public void error(Object o, Throwable throwable)
   {}

   public void fatal(Object o)
   {}

   public void fatal(Object o, Throwable throwable)
   {}

   public void info(Object o)
   {}

   public void info(Object o, Throwable throwable)
   {}

   public void warn(Object o)
   {}

   public void warn(Object o, Throwable throwable)
   {}

   public boolean isInfoEnabled()
   {
      return false;
   }

   public boolean isDebugEnabled()
   {
      return false;
   }
}
