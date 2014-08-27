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

import java.io.Serializable;

/**
 * A duration in years, months, weeks, days, hours, minutes, seconds as a plain holder
 * object. Values are integer and are not calculated in any way. e.g. setting 500 days 1
 * week is possible and other values are not calculated based on the set value.
 *
 * @author Roland.Stamm
 *
 */
public class Duration implements Serializable
{

   private static final long serialVersionUID = -5010595429107861506L;

   public static final Duration ZERO = Duration.days(0);

   private int years;

   private int months;

   private int weeks;

   private int days;

   private int hours;

   private int minutes;

   private int seconds;

   public Duration(int years, int months, int weeks, int days, int hours, int minutes,
         int seconds)
   {
      super();
      this.years = years;
      this.months = months;
      this.weeks = weeks;
      this.days = days;
      this.hours = hours;
      this.minutes = minutes;
      this.seconds = seconds;
   }

   public int getYears()
   {
      return years;
   }

   public void setYears(int years)
   {
      this.years = years;
   }

   public int getMonths()
   {
      return months;
   }

   public void setMonths(int months)
   {
      this.months = months;
   }

   public int getDays()
   {
      return days;
   }

   public int getWeeks()
   {
      return weeks;
   }

   public void setWeeks(int weeks)
   {
      this.weeks = weeks;
   }

   public void setDays(int days)
   {
      this.days = days;
   }

   public int getHours()
   {
      return hours;
   }

   public void setHours(int hours)
   {
      this.hours = hours;
   }

   public int getMinutes()
   {
      return minutes;
   }

   public void setMinutes(int minutes)
   {
      this.minutes = minutes;
   }

   public int getSeconds()
   {
      return seconds;
   }

   public void setSeconds(int seconds)
   {
      this.seconds = seconds;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + days;
      result = prime * result + hours;
      result = prime * result + minutes;
      result = prime * result + months;
      result = prime * result + seconds;
      result = prime * result + weeks;
      result = prime * result + years;
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
      Duration other = (Duration) obj;
      if (days != other.days)
         return false;
      if (hours != other.hours)
         return false;
      if (minutes != other.minutes)
         return false;
      if (months != other.months)
         return false;
      if (seconds != other.seconds)
         return false;
      if (weeks != other.weeks)
         return false;
      if (years != other.years)
         return false;
      return true;
   }

   public static Duration years(int years)
   {
      return new Duration(years, 0, 0, 0, 0, 0, 0);
   }

   public static Duration months(int months)
   {
      return new Duration(0, months, 0, 0, 0, 0, 0);
   }

   public static Duration weeks(int weeks)
   {
      return new Duration(0, 0, weeks, 0, 0, 0, 0);
   }

   public static Duration days(int days)
   {
      return new Duration(0, 0, 0, days, 0, 0, 0);
   }

   public static Duration hours(int hours)
   {
      return new Duration(0, 0, 0, 0, hours, 0, 0);
   }

   public static Duration minutes(int minutes)
   {
      return new Duration(0, 0, 0, 0, 0, minutes, 0);
   }

   public static Duration seconds(int seconds)
   {
      return new Duration(0, 0, 0, 0, 0, 0, seconds);
   }

}
