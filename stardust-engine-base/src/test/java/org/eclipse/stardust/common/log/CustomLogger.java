/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common.log;

import org.eclipse.stardust.common.log.Logger;

/**
 * Logs {@link System#out} only on {@link #info(Object)}.
 *
 * @author Roland.Stamm
 */
public class CustomLogger implements Logger
{

   public CustomLogger(String name)
   {
   }

   @Override
   public void debug(Object o)
   {
   }

   @Override
   public void debug(Object o, Throwable throwable)
   {
   }

   @Override
   public void error(Object o)
   {
   }

   @Override
   public void error(Object o, Throwable throwable)
   {
   }

   @Override
   public void fatal(Object o)
   {
   }

   @Override
   public void fatal(Object o, Throwable throwable)
   {
   }

   @Override
   public void info(Object o)
   {
      System.out.println("** CUSTOM LOGGER TEST ** " + o.toString());
   }

   @Override
   public void info(Object o, Throwable throwable)
   {
   }

   @Override
   public void warn(Object o)
   {
   }

   @Override
   public void warn(Object o, Throwable throwable)
   {
   }

   @Override
   public boolean isInfoEnabled()
   {
      return false;
   }

   @Override
   public boolean isDebugEnabled()
   {
      return false;
   }

}
