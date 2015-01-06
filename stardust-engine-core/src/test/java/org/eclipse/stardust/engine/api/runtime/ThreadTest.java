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

/**
 *
 */
public class ThreadTest extends Thread
{
   /**
    *
    */
   public ThreadTest()
   {
      setDaemon(false);
   }

   /**
    *
    */
   public void run()
   {
      try
      {
         sleep(1000);
      }
      catch (Exception x)
      {
      }

      System.out.println("Done with my stuff.");
   }

   /**
    *
    */
   public static void main(String[] args)
   {
      Thread[] threads = new Thread[10];

      for (int n = 0; n < 10; ++n)
      {
         threads[n] = new ThreadTest();

         threads[n].start();
      }

      System.exit(0);
   }
}
