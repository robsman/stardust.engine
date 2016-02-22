/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.util;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Encapsulates the time to wait until a timeout occurs.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class WaitTimeout
{
   private final long time;
   private final TimeUnit unit;
   
   /**
    * <p>
    * Initializes a newly created wait timeout with the
    * given parameters.
    * </p>
    * 
    * @param time the time to set
    * @param unit the time unit to set
    */
   public WaitTimeout(final long time, final TimeUnit unit)
   {
      if (unit == null)
      {
         throw new NullPointerException("Time unit must not be null.");
      }
      
      this.time = time;
      this.unit = unit;
   }
   
   /**
    * @return the set time
    */
   public long time()
   {
      return time;
   }
   
   /**
    * @return the set time unit
    */
   public TimeUnit unit()
   {
      return unit;
   }
   
   public String toString()
   {
      return time + " " + unit.toString().toLowerCase();
   }
}
