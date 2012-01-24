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
package org.eclipse.stardust.engine.core.compatibility.gui;

/**
 * Thread to handle a simple shutdown sequence
 */
public class SimpleShutDownThread extends Thread
{
   private SimpleShutDownListener listener;

   /**
    *
    */
   public SimpleShutDownThread(SimpleShutDownListener listener)
   {
      this.listener = listener;
   }

   /**
    *
    */
   public void run()
   {
      if (listener != null)
      {
         listener.shutDown();
      }
   }

   /**
    *
    */
   public String getType()
   {
      return "Shut down thread for " + listener;
   }

   /**
    *
    */
   static public void registerForShutdown(SimpleShutDownListener listener)
   {
      try
      {
         Runtime.getRuntime().addShutdownHook(new SimpleShutDownThread(listener));
      }
      catch (NoSuchMethodError _ex)
      {
         // hint: the method addShutdownHook is new in JDK 1.3
         //			so if this code runs under JDK 1.2.x a exception may accour
      }
   }
}
