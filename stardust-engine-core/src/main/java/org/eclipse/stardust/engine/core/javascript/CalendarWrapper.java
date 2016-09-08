/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.javascript;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.stardust.engine.api.runtime.StartOptions;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkDefinition;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkUtils;
import org.eclipse.stardust.engine.core.benchmark.calendar.CalendarUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;

/**
 * Provides a scripting interface to a Calendar object.
 *
 * @author Florin.Herinean
 */
public class CalendarWrapper extends Calendar
{
   private static final long serialVersionUID = 1L;

   private IProcessInstance pi;
   private Calendar calendar;

   public CalendarWrapper(IProcessInstance pi, Calendar calendar)
   {
      this.pi = pi;
      this.calendar = calendar;
   }

   /*******************************
    *                             *
    * Calendar delegated methods. *
    *                             *
    *******************************/

   public long getTimeInMillis()
   {
      return calendar.getTimeInMillis();
   }

   public void setTimeInMillis(long millis)
   {
      calendar.setTimeInMillis(millis);
   }

   public int get(int field)
   {
      return calendar.get(field);
   }

   public void set(int field, int value)
   {
      calendar.set(field, value);
   }

   public String getDisplayName(int field, int style, Locale locale)
   {
      return calendar.getDisplayName(field, style, locale);
   }

   public Map<String, Integer> getDisplayNames(int field, int style, Locale locale)
   {
      return calendar.getDisplayNames(field, style, locale);
   }

   public boolean equals(Object obj)
   {
      if (obj instanceof CalendarWrapper)
      {
         obj = ((CalendarWrapper) obj).calendar;
      }
      return calendar.equals(obj);
   }

   public int hashCode()
   {
      return calendar.hashCode();
   }

   public boolean before(Object when)
   {
      return calendar.before(when);
   }

   public boolean after(Object when)
   {
      return calendar.after(when);
   }

   public int compareTo(Calendar anotherCalendar)
   {
      return calendar.compareTo(anotherCalendar);
   }

   public void add(int field, int amount)
   {
      calendar.add(field, amount);
   }

   public void roll(int field, boolean up)
   {
      calendar.roll(field, up);
   }

   public void roll(int field, int amount)
   {
      calendar.roll(field, amount);
   }

   public void setTimeZone(TimeZone value)
   {
      calendar.setTimeZone(value);
   }

   public TimeZone getTimeZone()
   {
      return calendar.getTimeZone();
   }

   public void setLenient(boolean lenient)
   {
      calendar.setLenient(lenient);
   }

   public boolean isLenient()
   {
      return calendar.isLenient();
   }

   public void setFirstDayOfWeek(int value)
   {
      calendar.setFirstDayOfWeek(value);
   }

   public int getFirstDayOfWeek()
   {
      return calendar.getFirstDayOfWeek();
   }

   public void setMinimalDaysInFirstWeek(int value)
   {
      calendar.setMinimalDaysInFirstWeek(value);
   }

   public int getMinimalDaysInFirstWeek()
   {
      return calendar.getMinimalDaysInFirstWeek();
   }

   public int getMinimum(int field)
   {
      return calendar.getMinimum(field);
   }

   public int getMaximum(int field)
   {
      return calendar.getMaximum(field);
   }

   public int getGreatestMinimum(int field)
   {
      return calendar.getGreatestMinimum(field);
   }

   public int getLeastMaximum(int field)
   {
      return calendar.getLeastMaximum(field);
   }

   public int getActualMinimum(int field)
   {
      return calendar.getActualMinimum(field);
   }

   public int getActualMaximum(int field)
   {
      return calendar.getActualMaximum(field);
   }

   public Object clone()
   {
      return new CalendarWrapper(pi, (Calendar) calendar.clone());
   }

   public String toString()
   {
      return calendar.toString();
   }

   protected void computeTime()
   {
      throw new UnsupportedOperationException();
   }

   protected void computeFields()
   {
      throw new UnsupportedOperationException();
   }

   /******************************
    *                            *
    * Calendar business methods. *
    *                            *
    ******************************/

   /**
    * Gets the day of week, ranging from 1 (Sunday) to 7 (Saturday).
    *
    * @return the index of the day in the week.
    */
   public int getDay()
   {
      return calendar.get(Calendar.DAY_OF_WEEK);
   }

   /**
    * Gets the day in the month.
    *
    * @return the index of the day in the month,
    * starting with 1 for the first day.
    */
   public int getDate()
   {
      return calendar.get(Calendar.DAY_OF_MONTH);
   }

   /**
    * Gets the month in the year.
    *
    * @return the index of the month in the year,
    * starting with 1 for January.
    */
   public int getMonth()
   {
      return calendar.get(Calendar.MONTH) + 1;
   }

   /**
    * Gets the year.
    *
    * @return the year.
    */
   public int getYear()
   {
      return calendar.get(Calendar.YEAR);
   }

   /**
    * Gets the week in the year.
    *
    * @return the index of the week, starting with 1.
    */
   public int getWeek()
   {
      return calendar.get(Calendar.WEEK_OF_YEAR);
   }

   /**
    * Compute the business day in the month represented by this calendar.
    * First business day has the index 1.
    *
    * @return the index of the business day or -1 if it is not a business day.
    */
   public int getBusinessDate()
   {
      if (!isBusinessDay())
      {
         return -1;
      }
      Calendar c = (Calendar) calendar.clone();
      int businessDate = 1;
      int min = c.getActualMinimum(DAY_OF_MONTH);
      for (int i = min; i < getDate(); i++)
      {
         c.set(DAY_OF_MONTH, i);
         if (isBusinessDay(c))
         {
            businessDate++;
         }
      }
      return businessDate;
   }

   /**
    * Gets if the date represented by this calendar is a valid business date.
    *
    * @return true if it is a valid business date, false otherwise
    */
   public boolean isBusinessDay()
   {
      return isBusinessDay(calendar);
   }

   /**
    * Compute the next business day.
    *
    * @return the index of the next business day or -1 if there are
    * no more business days until the end of the month.
    */
   public int getNextBusinessDate()
   {
      Calendar c = (Calendar) calendar.clone();
      int current = getDate();
      int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
      for (int i = current + 1; i <= max; i++)
      {
         c.set(DAY_OF_MONTH, i);
         if (isBusinessDay(c))
         {
            return i;
         }
      }
      return -1;
   }

   /**
    * Compute the previous business day.
    *
    * @return the index of the previous business day or -1 if there are no previous business days from the beginning of the month.
    */
   public int getPreviousBusinessDate()
   {
      Calendar c = (Calendar) calendar.clone();
      int current = getDate();
      int min = c.getActualMinimum(Calendar.DAY_OF_MONTH);
      for (int i = current - 1; i >= min; i--)
      {
         c.set(DAY_OF_MONTH, i);
         if (isBusinessDay(c))
         {
            return i;
         }
      }
      return -1;
   }

   /**
    * Computes if the day represented by this calendar is the first business day in the month
    *
    * @return true if it is the first business day of the month, false otherwise.
    */
   public boolean isFirstBusinessDay()
   {
      return getBusinessDate() == 1;
   }

   public int getFirstBusinessDate()
   {
      Calendar c = (Calendar) calendar.clone();
      int min = c.getActualMinimum(Calendar.DAY_OF_MONTH);
      int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
      for (int i = min; i <= max; i++)
      {
         c.set(DAY_OF_MONTH, i);
         if (isBusinessDay(c))
         {
            return i;
         }
      }
      return -1;
   }

   /**
    * Computes if the day represented by this calendar is the last business day in the month
    *
    * @return true if it is the last business day of the month, false otherwise.
    */
   public boolean isLastBusinessDay()
   {
      if (!isBusinessDay())
      {
         return false;
      }
      Calendar c = (Calendar) calendar.clone();
      int current = getDate();
      int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
      for (int i = current + 1; i <= max; i++)
      {
         c.set(DAY_OF_MONTH, i);
         if (isBusinessDay(c))
         {
            return false;
         }
      }
      return true;
   }

   public int getLastBusinessDate()
   {
      Calendar c = (Calendar) calendar.clone();
      int min = c.getActualMinimum(Calendar.DAY_OF_MONTH);
      int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
      for (int i = max; i >= min; i--)
      {
         c.set(DAY_OF_MONTH, i);
         if (isBusinessDay(c))
         {
            return i;
         }
      }
      return -1;
   }

   /**
    * Computes if the date represented by the given calendar is a valid business day.
    *
    * @param c the calendar
    * @return true if it is a valid business day.
    */
   protected boolean isBusinessDay(Calendar c)
   {
      String documentId = null;

      Serializable startingCalendar = pi.getPropertyValue(StartOptions.BUSINESS_CALENDAR);
      if (startingCalendar != null)
      {
         documentId = startingCalendar.toString();
      }
      else
      {
         if (BenchmarkUtils.isBenchmarkedPI(pi))
         {
            BenchmarkDefinition benchmark = BenchmarkUtils.getBenchmarkDefinition(pi.getBenchmark());
            documentId = benchmark.getBusinessCalendarId();
         }
      }

      if (documentId != null)
      {
         return !CalendarUtils.isBlockedBusinessDay(c.getTime(), documentId);
      }
      return true;
   }
}
