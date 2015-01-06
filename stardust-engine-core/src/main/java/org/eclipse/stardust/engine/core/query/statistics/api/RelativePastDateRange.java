/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Calendar;
import java.util.Date;

public class RelativePastDateRange implements DateRange
{
   private static final long serialVersionUID = -6869385351019340999L;

   private Duration offset;

   private CalendarUnit offsetType;

   private Duration range;

   private CalendarUnit rangeType;

   public RelativePastDateRange(Duration offset, CalendarUnit offsetType, Duration range,
         CalendarUnit rangeType)
   {
      super();
      this.offset = offset;
      this.offsetType = offsetType;
      this.range = range;
      this.rangeType = rangeType;
   }

   @Override
   public Date getIntervalBegin()
   {
      Date now = new Date();
      Calendar cal = Calendar.getInstance();
      cal.setTime(now);

      // Subtract offset and snap to calendar unit type
      snapToBeginOfCalendarUnit(cal, offset, offsetType, true);

      return cal.getTime();
   }

   @Override
   public Date getIntervalEnd()
   {
      Date now = new Date();
      Calendar cal = Calendar.getInstance();
      cal.setTime(now);

      // Subtract offset and snap to calendar unit type
      snapToBeginOfCalendarUnit(cal, offset, offsetType, true);

      // Subtract range and snap to calendar unit type
      snapToBeginOfCalendarUnit(cal, range, rangeType, false);

      return cal.getTime();
   }

   private void snapToBeginOfCalendarUnit(Calendar cal, Duration duration,
         CalendarUnit type, boolean substract)
   {
      switch (type)
      {
      case DAY:
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);

         cal.add(Calendar.DAY_OF_YEAR,
               substract ? -Math.abs(duration.getDays()) : Math.abs(duration.getDays()));
         break;
      case WEEK:
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);

         cal.setFirstDayOfWeek(Calendar.MONDAY);
         cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

         cal.add(Calendar.WEEK_OF_YEAR,
               substract ? -Math.abs(duration.getWeeks()) : Math.abs(duration.getWeeks()));
         break;
      case MONTH:
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);

         cal.setFirstDayOfWeek(Calendar.MONDAY);
         cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

         cal.set(Calendar.DAY_OF_MONTH, 1);

         cal.add(
               Calendar.MONTH,
               substract ? -Math.abs(duration.getMonths()) : Math.abs(duration.getMonths()));
         break;
      case YEAR:
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);

         cal.setFirstDayOfWeek(Calendar.MONDAY);
         cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

         cal.set(Calendar.DAY_OF_YEAR, 1);

         cal.add(Calendar.YEAR,
               substract ? -Math.abs(duration.getYears()) : Math.abs(duration.getYears()));
         break;
      default:
         break;
      }
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((offset == null) ? 0 : offset.hashCode());
      result = prime * result + ((offsetType == null) ? 0 : offsetType.hashCode());
      result = prime * result + ((range == null) ? 0 : range.hashCode());
      result = prime * result + ((rangeType == null) ? 0 : rangeType.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      RelativePastDateRange other = (RelativePastDateRange) obj;
      if (offset == null)
      {
         if (other.offset != null)
            return false;
      }
      else if ( !offset.equals(other.offset))
         return false;
      if (offsetType != other.offsetType)
         return false;
      if (range == null)
      {
         if (other.range != null)
            return false;
      }
      else if ( !range.equals(other.range))
         return false;
      if (rangeType != other.rangeType)
         return false;
      return true;
   }

}
