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
package org.eclipse.stardust.common;

/** */
public class MeasureTime
{
   String purpose;
   long start;
   long end;

   /** */
   public MeasureTime()
   {
      this.purpose = "unknown";
      start = System.currentTimeMillis();
      end = start;
   }

   /** */
   public MeasureTime(String purpose)
   {
      this.purpose = purpose;
      start = System.currentTimeMillis();
      end = start;
   }

   /** */
   public void start()
   {
      start("unknown");
   }

   /** */
   public void start(String purpose)
   {
      this.purpose = purpose;
      start = System.currentTimeMillis();
      end = start;
   }

   /** */
   public void end()
   {
      end = System.currentTimeMillis();
   }

   /** */
   public String toString()
   {
      long total = end - start;
      long minutes = total / 60000;
      long seconds = (total % 60000) / 1000;
      long milliseconds = (total % 1000);

      if (purpose == null || purpose.length() == 0)
         return (minutes + ":" + seconds + ":" + milliseconds);
      else
         return ("Time needed for \"" + purpose + "\": " + minutes + ":" + seconds + ":" + milliseconds);
   }

}