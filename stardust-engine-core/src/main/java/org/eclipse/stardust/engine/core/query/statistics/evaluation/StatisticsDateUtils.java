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
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public class StatisticsDateUtils
{
   public static final long MILLISECONDS_PER_SECOND = 1000;
   
   public static final long MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND;

   public static Date getBeginOfDay(Date time)
   {
      Calendar cal = TimestampProviderUtils.getCalendar(time);

      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);

      return cal.getTime();
   }

   public static Date periodToDate(Period period)
   {
      // TimestampProviderUtils isn't needed here because the calendar object will be
      // reseted in periodToDate(Period, Calendar)
      return periodToDate(period, Calendar.getInstance());
   }

   public static Date periodToDate(Period period, Calendar conversionCalendar)
   {
      long relStartTime = 1000l * (
            period.get(Period.SECONDS) +
            (period.get(Period.MINUTES) * 60) +        // minutes in sec
            (period.get(Period.HOURS) * 60 * 60) +     // hours in sec
            (period.get(Period.DAYS) * 24 * 60 * 60));  // days in sec
      
      conversionCalendar.setTimeInMillis(relStartTime);
      
      // months are not standardized so we cannot convert these in millisecs
      conversionCalendar.add(Calendar.MONTH, period.get(Period.MONTHS));
      conversionCalendar.add(Calendar.YEAR, period.get(Period.YEARS));

      return conversionCalendar.getTime();
   }
   
   private StatisticsDateUtils()
   {
      // utility class
   }
}
