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
      Calendar cal = Calendar.getInstance();

      cal.setTime(time);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);

      return cal.getTime();
   }

   public static Date periodToDate(Period period)
   {
      return periodToDate(period, Calendar.getInstance());
   }

   public static Date periodToDate(Period period, Calendar conversionCalendar)
   {
      conversionCalendar.setTimeInMillis(0l);
      conversionCalendar.add(Calendar.SECOND, period.get(Period.SECONDS));
      conversionCalendar.add(Calendar.MINUTE, period.get(Period.MINUTES));
      conversionCalendar.add(Calendar.HOUR_OF_DAY, period.get(Period.HOURS));
      conversionCalendar.add(Calendar.DAY_OF_YEAR, period.get(Period.DAYS));
      conversionCalendar.add(Calendar.MONTH, period.get(Period.MONTHS));
      conversionCalendar.add(Calendar.YEAR, period.get(Period.YEARS));

      return conversionCalendar.getTime();
   }
   
   private StatisticsDateUtils()
   {
      // utility class
   }
}
