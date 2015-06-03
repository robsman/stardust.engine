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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class CalendarUtils
{

   private static final String KEY_TIME_OFF_CALENDAR_CACHE = CalendarUtils.class.getName()
         + ".TimeoffCalendarCache";

   private static final String BUSINESS_DAY_CACHE = CalendarUtils.class.getName()
         + ".BusinessDayCache";

   private CalendarUtils()
   {
      // Utility class.
   }

   public static boolean isBusinessDay(Date date, String calendarDocumentId)
   {
      TimeOffCalendarFinder timeOffCalendarFinder = getTimeOffCalendar(calendarDocumentId);
      synchronized (timeOffCalendarFinder)
      {

         timeOffCalendarFinder.setExecutionDate(date);
         timeOffCalendarFinder.readAllDefinitions();

         // TODO cache calculated blocked dates.
         return timeOffCalendarFinder.isBlocked();
      }
   }

   private static TimeOffCalendarFinder getTimeOffCalendar(String calendarDocumentId)
   {
      Map<String, TimeOffCalendarFinder> benchmarkCache = getTimeOffCalendarCache(SecurityProperties.getPartition()
            .getId());

      TimeOffCalendarFinder benchmarkDefinition = benchmarkCache.get(calendarDocumentId);

      if (benchmarkDefinition == null)
      {
         benchmarkDefinition = new TimeOffCalendarFinder(calendarDocumentId);
         benchmarkCache.put(calendarDocumentId, benchmarkDefinition);
      }
      return benchmarkDefinition;
   }


   private static Map<String, TimeOffCalendarFinder> getTimeOffCalendarCache(String partitionId)
   {
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
}
