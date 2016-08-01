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
package org.eclipse.stardust.engine.core.benchmark.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

public class CalendarUtils
{
   private static Logger trace = LogManager.getLogger(CalendarUtils.class);

   private static final String KEY_TIME_OFF_CALENDAR_CACHE = CalendarUtils.class
         .getName() + ".TimeoffCalendarCache";

   private static final String KEY_TIME_OFF_CALENDAR_PURGE_DATE = CalendarUtils.class
         .getName() + ".TimeoffCalendarPurgeDate";

   private CalendarUtils()
   {
      // Utility class.
   }

   public static boolean isBlockedBusinessDay(Date date, String calendarDocumentId)
   {
      TimeOffCalendarFinder timeOffCalendarFinder = getTimeOffCalendar(calendarDocumentId);

      Boolean blocked = timeOffCalendarFinder.isBlocked(date);

      if (blocked == null)
      {
         trace.warn("Blocked business day calculation failed. TimeOffCalendar not found: '"+ calendarDocumentId +"'. "
               + "Using workdays mo-fr for calculation.");
         timeOffCalendarFinder.clearCache();
         blocked = !isWorkday(date);
      }

      return blocked;
   }

   public static boolean isWorkday(Date date)
   {
      Calendar calendar = Calendar.getInstance();

      calendar.setTime(date);

      int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
      return !(dayOfWeek == 0 || dayOfWeek == 6);
   }

   private static TimeOffCalendarFinder getTimeOffCalendar(String calendarDocumentId)
   {
      Map<String, TimeOffCalendarFinder> benchmarkCache = getTimeOffCalendarCache();

      TimeOffCalendarFinder benchmarkDefinition = benchmarkCache.get(calendarDocumentId);

      if (benchmarkDefinition == null)
      {
         benchmarkDefinition = new TimeOffCalendarFinder(calendarDocumentId);
         benchmarkCache.put(calendarDocumentId, benchmarkDefinition);
      }
      return benchmarkDefinition;
   }

   private synchronized static Map<String, TimeOffCalendarFinder> getTimeOffCalendarCache()
   {
      final String partitionId = SecurityProperties.getPartition().getId();
      final GlobalParameters globals = GlobalParameters.globals();


      ConcurrentHashMap<String, Map> timeOffCalendarPartitionCache = (ConcurrentHashMap<String, Map>) globals
            .get(KEY_TIME_OFF_CALENDAR_CACHE);
      if (null == timeOffCalendarPartitionCache)
      {
         globals.getOrInitialize(KEY_TIME_OFF_CALENDAR_CACHE, new ValueProvider()
         {

            public Object getValue()
            {
               return new ConcurrentHashMap<String, Map>();
            }

         });
         timeOffCalendarPartitionCache = (ConcurrentHashMap<String, Map>) globals
               .get(KEY_TIME_OFF_CALENDAR_CACHE);
      }

      // set next purge date.
      Date purgeDate = (Date) globals.get(KEY_TIME_OFF_CALENDAR_PURGE_DATE);
      if (purgeDate == null)
      {
         purgeDate = (Date) globals.getOrInitialize(KEY_TIME_OFF_CALENDAR_PURGE_DATE,
               getNextPurgeDate());
      }

      if (purgeDate.before(TimestampProviderUtils.getTimeStamp()))
      {
         // purge cache for partition and allow next purge date on next call.
         globals.set(KEY_TIME_OFF_CALENDAR_PURGE_DATE, null);
         timeOffCalendarPartitionCache.remove(partitionId);
      }

      Map timeOffCalendarCache = timeOffCalendarPartitionCache.get(partitionId);
      if (null == timeOffCalendarCache)
      {
         timeOffCalendarPartitionCache.put(partitionId,
               new ConcurrentHashMap<String, TimeOffCalendarFinder>());
         timeOffCalendarCache = (Map<String, TimeOffCalendarFinder>) timeOffCalendarPartitionCache
               .get(partitionId);
      }

      return timeOffCalendarCache;
   }

   private static Date getNextPurgeDate()
   {
      Calendar calendar = TimestampProviderUtils.getCalendar();
      calendar.add(Calendar.DAY_OF_YEAR, 1);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      return calendar.getTime();
   }
}
