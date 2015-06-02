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
package org.eclipse.stardust.engine.core.benchmark;

public class Offset
{
   private int amount;

   private CalendarUnit unit;

   public Offset(int amount, CalendarUnit unit)
   {
      super();
      this.amount = amount;
      this.unit = unit;
   }

   public int getAmount()
   {
      return amount;
   }

   public CalendarUnit getUnit()
   {
      return unit;
   }

   public enum CalendarUnit
   {
      DAYS, WEEKS, MONTHS
   }

}
