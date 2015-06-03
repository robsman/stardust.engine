/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.benchmark;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.benchmark.calendar.CalendarUtils;

/**
 * Condition evaluator for business days.
 *
 * @author Roland.Stamm
 */
public class BusinessDaysCondition extends CalendarDaysCondition
{
   private static final int MAX_BLOCKED_DAYS = 1500;

   private Logger trace = LogManager.getLogger(BusinessDaysCondition.class);

   protected String calendarDocumentId;

   public BusinessDaysCondition(String calendarDocumentId, Comperator comperator,
         String qualifiedDataId, Offset offset)
   {
      super(comperator, qualifiedDataId, offset);
      this.calendarDocumentId = calendarDocumentId;
   }

   @Override
   protected Date applyOffset(Date date, Offset offset)
   {
      long currentTimeMillis = System.currentTimeMillis();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      // 4,1 years worth of blocked days stop calculation to prevent
      // endless loop for endless calendar events.
      int maxBlockedDaysCounter = MAX_BLOCKED_DAYS;

      if (offset != null)
      {
         int count = 0;
         int amount = offset.getAmount();

         switch (offset.getUnit())
         {
            case MONTHS:
               // TODO exact month?
               amount = amount *30;
               break;

            case WEEKS:
               amount = amount *7;
               break;

            default:
               break;
         }

         if (amount > 0)
         {
            while (count < amount)
            {
               maxBlockedDaysCounter = skipBusinessDay(calendar, maxBlockedDaysCounter, 1);

               calendar.add(Calendar.DAY_OF_YEAR, 1);
               count++;
            }
         }
         else
         {
            while (count > amount)
            {
               maxBlockedDaysCounter = skipBusinessDay(calendar, maxBlockedDaysCounter, -1);

               calendar.add(Calendar.DAY_OF_YEAR, -1);
               count--;
            }
         }
      }
      else
      {
         // The offset is null: only skip business days.
         maxBlockedDaysCounter = skipBusinessDay(calendar, maxBlockedDaysCounter, 1);
      }

      // apply offset time
      if (offset.getHour() != null && offset.getMinute() != null)
      {
         calendar.set(Calendar.HOUR_OF_DAY, offset.getHour());
         calendar.set(Calendar.MINUTE, offset.getMinute());
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
      }

      if (maxBlockedDaysCounter == 0)
      {
         trace.warn("Skipped '" + MAX_BLOCKED_DAYS
               + "' non-business days, ignoring further blocked days calculation!");
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("BusinessDay offset calculation took: "
               + (System.currentTimeMillis() - currentTimeMillis) + " ms");
      }
      return calendar.getTime();
   }

   private int skipBusinessDay(Calendar calendar, int maxBlockedDays, int skipAmount)
   {
      while (isBusinessDay(calendar.getTime()) && maxBlockedDays > 0)
      {
         calendar.add(Calendar.DAY_OF_YEAR, skipAmount);
         maxBlockedDays--;
      }
      return maxBlockedDays;
   }

   private boolean isBusinessDay(Date date)
   {
      return CalendarUtils.isBusinessDay(date, calendarDocumentId);
   }
}
