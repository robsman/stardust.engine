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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This utility class provides convenience methods for date / time related
 * conversions and checks.
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DateUtils
{
   static private final String DEFAULT_HOUR_SIGN = " h";
   static private final char LEADING_NULL_SIGN = '0';
   static private final char DEFAULT_DURATION_SEPARATOR = ':';
   
   private static final String YEARS_REGEXP = "[1-9]{1}[0-9]{3}";
   private static final String MONTHS_REGEXP = "([0]{0,1}[1-9]|10|11|12)";
   private static final String DAYS_REGEXP = "(([1-2]{1,1}[0-9])|([0-2]{0,1}[1-9])|([3][0-1]))";
   private static final String ISO_TIME_REGEXP = "(([2][0-3])|([0-1]{0,1}[0-9]{0,1})):([0-5]{0,1}[0-9]{0,1})(:[0-5]{0,1}[0-9]{0,1}(:[0-9]{1,3}){0,1}){0,1}";
   private static final String ISO_DATE_REGEXP = "^(" + YEARS_REGEXP + "\\-"
         + MONTHS_REGEXP + "\\-" + DAYS_REGEXP + "(([\\s]|[T])" + ISO_TIME_REGEXP
         + "){0,1}){0,1}$";
   private static final String NON_INTERACTIVE_DATE_REGEXP = "^(" + YEARS_REGEXP + "\\/"
         + MONTHS_REGEXP + "\\/" + DAYS_REGEXP + "([\\s]" + ISO_TIME_REGEXP
         + "){0,1}){0,1}$";
   private static final Pattern ISO_DATE_PATTERN = Pattern.compile(ISO_DATE_REGEXP);
   private static final Pattern NON_INTERACTIVE_DATE_PATTERN = Pattern.compile(NON_INTERACTIVE_DATE_REGEXP);

   /**
    * Factory which creates a SimpleDateFormat instance which can be used to
    * format Dates to strings with pattern "yyyy/MM/dd hh:mm:ss:SSS".
    * 
    * @return the SimpleDateFormat instance
    * 
    * @see SimpleDateFormat
    */
   public static SimpleDateFormat getNoninteractiveDateFormat() 
   {
      return new SimpleDateFormat("yyyy/MM/dd hh:mm:ss:SSS");
   }

   /**
    * Factory which creates a SimpleDateFormat instance which can be used to
    * format Dates to strings with pattern "yyyy/MM/dd hh:mm:ss".
    * 
    * @return the SimpleDateFormat instance
    * 
    * @see SimpleDateFormat
    */
   public static SimpleDateFormat getInteractiveDateFormat() 
   {
      return new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
   }

   /**
    * Returns number of milliseconds since January 1, 1970, 00:00:00 GMT
    * represented by given Date object. If Date object is null the default value
    * will be returned.
    * 
    * @param date the Date object.
    * @param def the default value used if Date object is null.
    * @return number of milliseconds since January 1, 1970, 00:00:00 GMT.
    * 
    * @see Date#getTime()
    */
   public static long getTimestamp(Date date, long def)
   {
      return date == null ? def : date.getTime();
   }

   /**
    * Returns a String representation for given Date object. The String is a
    * concatenation of date formatter and time formatter separated by single
    * space, both using default style for default locale.
    * 
    * @param date
    *           the Date object.
    * @return String representation for Date object using default style for
    *         default locale. For null Date object an empty String will be
    *         returned.
    * 
    * @see DateFormat#getDateInstance()
    * @see DateFormat#getTimeInstance()
    */
   public static String formatDateTime(Date date)
   {
      return date == null ? "" : DateFormat.getDateInstance().format(date) + " "
            + DateFormat.getTimeInstance().format(date);
   }

   /**
    * Returns a String representation for given Date object using date formatter
    * with default style for default locale.
    * 
    * @param date
    *           the Date object.
    * @return String representation of date component for Date object using default style for
    *         default locale. For null Date object an empty String will be
    *         returned.
    * 
    * @see DateFormat#getDateInstance()
    */
   public static String formatDate(Date date)
   {
      return date == null ? "" : DateFormat.getDateInstance().format(date);
   }

   /**
    * Returns a String representation for given Date object using time formatter
    * with default style for default locale.
    * 
    * @param date
    *           the Date object.
    * @return String representation of time component for Date object using default style for
    *         default locale. For null Date object an empty String will be
    *         returned.
    * 
    * @see DateFormat#getTimeInstance()
    */
   public static String formatTime(Date date)
   {
      return date == null ? "" : DateFormat.getTimeInstance().format(date);
   }

   /**
    * Returns the given duration in hours as formatted string.</br>
    * Format is: hours:minutes:seconds h</br>
    * Example  : 1:05:20 h
    * 
    * @param durationInHours the duration in hours.
    * @return String representation of duration.
    */
   public static String formatDurationAsString(double durationInHours)
   {
      StringBuffer _durationString = new StringBuffer(15);

      Double _durationInSec = new Double(durationInHours * 3600);

      int _rest = _durationInSec.intValue();

      // append hours
      _durationString.append(_rest / 3600);
      _rest = _rest % 3600;

      // append minutes
      _durationString.append(DEFAULT_DURATION_SEPARATOR);
      if ((_rest / 60) < 10)
      {
         _durationString.append(LEADING_NULL_SIGN);
      }
      _durationString.append(_rest / 60);
      _rest = _rest % 60;

      // append seconds
      _durationString.append(DEFAULT_DURATION_SEPARATOR);
      if (_rest < 10)
      {
         _durationString.append(LEADING_NULL_SIGN);
      }
      _durationString.append(_rest);

      // append hour sign
      _durationString.append(DEFAULT_HOUR_SIGN);

      return _durationString.toString();
   }
   
   /**
    * Checks if the input date conforms to an ISO date pattern (e.g. yyyy-MM-dd
    * HH:mm:ss:SSS).
    * 
    * @param date
    *           String representation of date.
    * @return <code>true</code> if the date conforms to an ISO date pattern,
    *         <code>false</code> if it does not conform
    */
   public static boolean isValidISODateFormat(String date)
   {
      Matcher dateMatcher = ISO_DATE_PATTERN.matcher(date);
      return date != null ? dateMatcher.find() : false;
   }

   /**
    * Checks if the input date conforms to the non-interactive date format
    * yyyy/MM/dd hh:mm:ss:SSS.
    * 
    * @param date
    *           String representation of date.
    * @return <code>true</code> if the date conforms to this pattern,
    *         <code>false</code> if it does not conform
    */
   public static boolean isValidNonInteractiveFormat(String date)
   {
      Matcher dateMatcher = NON_INTERACTIVE_DATE_PATTERN.matcher(date);
      return date != null ? dateMatcher.find() : false;
   }
}
