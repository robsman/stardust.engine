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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.util.StringUtils;

public class Offset
{
   private int amount;

   private CalendarUnit unit;

   private Integer hour;

   private Integer minute;

   public Offset(int amount, CalendarUnit unit, String timeOfDay)
   {
      super();
      this.amount = amount;
      this.unit = unit;

      if (!StringUtils.isEmpty(timeOfDay))
      {
         try
         {
            Date date = new SimpleDateFormat("HH:mm").parse(timeOfDay);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            this.minute = calendar.get(Calendar.MINUTE);
            this.hour = calendar.get(Calendar.HOUR_OF_DAY);
         }
         catch (ParseException e)
         {
            // do nothing
         }
      }
   }

   public int getAmount()
   {
      return amount;
   }

   public CalendarUnit getUnit()
   {
      return unit;
   }

   /**
    * @return 24h format hour of day
    */
   public Integer getHour()
   {
      return hour;
   }

   public Integer getMinute()
   {
      return minute;
   }

   public enum CalendarUnit
   {
      DAYS, WEEKS, MONTHS
   }

}
