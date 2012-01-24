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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.common.Key;

// @todo (france, ub): remove. Replace usages by a period
/**
 *
 */
public class PeriodicityTypeKey extends Key
{
   public static final int UNKNOWN_VALUE = -1;
   public static final int ONCE_VALUE = 0;
   public static final int MINUTELY_VALUE = 1;
   public static final int HOURLY_VALUE = 2;
   public static final int DAILY_VALUE = 3;
   public static final int MONTHLY_VALUE = 4;
   public static final int YEARLY_VALUE = 5;

   public static final PeriodicityTypeKey UNKNOWN = new PeriodicityTypeKey(UNKNOWN_VALUE);
   public static final PeriodicityTypeKey ONCE = new PeriodicityTypeKey(ONCE_VALUE);
   public static final PeriodicityTypeKey MINUTELY = new PeriodicityTypeKey(MINUTELY_VALUE);
   public static final PeriodicityTypeKey HOURLY = new PeriodicityTypeKey(HOURLY_VALUE);
   public static final PeriodicityTypeKey DAILY = new PeriodicityTypeKey(DAILY_VALUE);
   public static final PeriodicityTypeKey MONTHLY = new PeriodicityTypeKey(MONTHLY_VALUE);
   public static final PeriodicityTypeKey YEARLY = new PeriodicityTypeKey(YEARLY_VALUE);

   static String[] keyList = {"Once", "Minutely", "Hourly",
                              "Daily", "Monthly", "Yearly"/***, "Monthly (15th)",
                               "Monthly (End of the Month)",
                               "Monday", "Tuesday", "Wednesday", "Thursday",
                               "Friday", "Saturday", "Sunday"***/};

   /**
    *
    */
   public static String[] getKeyList()
   {
      return keyList;
   }

   /**
    *
    */
   public PeriodicityTypeKey()
   {
      super();
   }

   /**
    *
    */
   private PeriodicityTypeKey(int value)
   {
      super(value);
   }

   /**
    *
    */
   public String getString()
   {
      if (value < 0)
      {
         return UNKNOWN_STRING;
      }

      return keyList[value];
   }

   public static PeriodicityTypeKey create(int i)
   {
      switch (i)
      {
         case UNKNOWN_VALUE:
            return UNKNOWN;
         case ONCE_VALUE:
            return ONCE;
         case MINUTELY_VALUE:
            return MINUTELY;
         case HOURLY_VALUE:
            return HOURLY;
         case DAILY_VALUE:
            return DAILY;
         case MONTHLY_VALUE:
            return MONTHLY;
         case YEARLY_VALUE:
            return YEARLY;
      }
      return new PeriodicityTypeKey(i);
   }
}

