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
 * The CARNOT standard logger interface. It is modeled after the log4j <code>Logger</code>
 * class, but serves here as a wrapper around different logging implementations.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Logger
{
   void debug(Object o);

   void debug(Object o, Throwable throwable);

   void error(Object o);

   void error(Object o, Throwable throwable);

   void fatal(Object o);

   void fatal(Object o, Throwable throwable);

   void info(Object o);

   void info(Object o, Throwable throwable);

   void warn(Object o);

   void warn(Object o, Throwable throwable);

   boolean isInfoEnabled();

   boolean isDebugEnabled();
}
