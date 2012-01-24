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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Arrays;

// @todo (france, ub): handle as primitive type on all occasions:
// - largestringholder
// - reflect
// - primitive data type
// - etc
// - xml converter

/**
 * The <code>Period</code> class is used to handle time periods. A period consists of a
 * group of 6 numbers, each specifying an amount of units, i.e. 6 months and 3 hours.
 * The <code>Period</code> also provides convenient methods to perform operations on
 * Calendars.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Period implements Serializable
{
   static final long serialVersionUID = -6754672417151666040L;
   
   /** Identifier for the number of years field. */
   public static final int YEARS = 0;
   /** Identifier for the number of months field. */
   public static final int MONTHS = 1;
   /** Identifier for the number of days field. */
   public static final int DAYS = 2;
   /** Identifier for the number of hours field. */
   public static final int HOURS = 3;
   /** Identifier for the number of minutes field. */
   public static final int MINUTES = 4;
   /** Identifier for the number of seconds field. */
   public static final int SECONDS = 5;

   private static final DecimalFormat format = new DecimalFormat("000000:");
   private short[] periods;

   /**
    * Creates a new Period from a string representation. The format should contain in
    * order: the years, months, days, hours, minutes and seconds. As field separator is
    * used the colon (':').
    * <p><i><b>Example: </b>"</i><code>0:6:0:3:0:0</code><i>" represents a period of
    * 6 months and 3 hours.</i></p>
    *
    * @param raw the String representation of the period.
    */
   public Period(String raw)
   {
      periods = new short[6];
      if (StringUtils.isEmpty(raw))
      {
         return;
      }
      StringTokenizer tkr = new StringTokenizer(raw, ":");
      for (int i = 0; i < periods.length; i++)
      {
         periods[i] = Short.parseShort(tkr.nextToken());
      }
   }

   /**
    * Creates a new Period from the given values.
    *
    * @param years   the number of years contained in the period.
    * @param months  the number of months contained in the period.
    * @param days    the number of days contained in the period.
    * @param hours   the number of hours contained in the period.
    * @param minutes the number of minutes contained in the period.
    * @param seconds the number of seconds contained in the period.
    */
   public Period(short years, short months, short days, short hours,
                 short minutes, short seconds)
   {
      periods = new short[] {years, months, days, hours, minutes, seconds};
   }

   /**
    * Returns a fixed size String representation of the period, where each field has 6
    * digits allocated.
    *
    * @return the string representation of the period.
    */
   public String toString()
   {
      StringBuffer result = new StringBuffer();
      FieldPosition position = new FieldPosition(0);
      for (int i = 0; i < periods.length; i++)
      {
         format.format(periods[i], result, position);
      }
      return result.substring(0, result.length() - 1);
   }

   /**
    * Adds this period to the provided calendar.
    *
    * @param source the reference time.
    *
    * @return the time obtained by adding this period to the reference time.
    */
   public Calendar add(Calendar source)
   {
      // @todo (france, ub): also support day_of_week?!
      Calendar result = (Calendar) source.clone();

      result.add(Calendar.YEAR, periods[0]);
      result.add(Calendar.MONTH, periods[1]);
      result.add(Calendar.DAY_OF_MONTH, periods[2]);
      result.add(Calendar.HOUR_OF_DAY, periods[3]);
      result.add(Calendar.MINUTE, periods[4]);
      result.add(Calendar.SECOND, periods[5]);

      return result;
   }

   /**
    * Subtracts this period from the provided calendar.
    *
    * @param source the reference time.
    *
    * @return the time obtained by subtracting this period from the reference time.
    */
   public Calendar subtract(Calendar source)
   {
      // @todo (france, ub): revise
      // @todo (france, ub): also support day_of_week?!
      Calendar result = (Calendar) source.clone();
      result.add(Calendar.YEAR, -periods[0]);
      result.add(Calendar.MONTH, -periods[1]);
      result.add(Calendar.DAY_OF_MONTH, -periods[2]);
      result.add(Calendar.HOUR, -periods[3]);
      result.add(Calendar.MINUTE, -periods[4]);
      result.add(Calendar.SECOND, -periods[5]);
      return result;
   }

   /**
    * Gets the value of a specific field of the period.
    *
    * @param field one of the identifier constants.
    *
    * @return the value of the field.
    */
   public short get(int field)
   {
      return periods[field];
   }

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (!(o instanceof Period)) return false;
      return Arrays.equals(periods, ((Period) o).periods);
   }

   public int hashCode()
   {
      int result = 0;
      for (int i = 0; i < periods.length; i++)
      {
         result = 29 * result + periods[i];
      }
      return result;
   }
}
