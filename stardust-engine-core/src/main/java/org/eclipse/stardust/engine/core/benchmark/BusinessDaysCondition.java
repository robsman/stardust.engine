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
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      // 4,1 years
      int maxBlockedDays = 1500;

      if (offset != null)
      {
         int count = 0;
         int amount = offset.getAmount();
         if (amount > 0)
         {
            while (count < amount)
            {
               while (isBlockedDay(calendar.getTime()) && maxBlockedDays > 0)
               {
                  calendar.add(Calendar.DAY_OF_YEAR, 1);
                  maxBlockedDays--;
               }
               switch (offset.getUnit())
               {
                  case DAYS:
                     calendar.add(Calendar.DAY_OF_YEAR, 1);
                     break;
                  case WEEKS:
                     calendar.add(Calendar.WEEK_OF_YEAR, 1);
                     break;
                  case MONTHS:
                     calendar.add(Calendar.MONTH, 1);
                     break;
               }
               count++;
            }
         }
         else
         {
            while (count > amount)
            {
               while (isBlockedDay(calendar.getTime()) && maxBlockedDays > 0)
               {
                  calendar.add(Calendar.DAY_OF_YEAR, -1);
                  maxBlockedDays--;
               }
               switch (offset.getUnit())
               {
                  case DAYS:
                     calendar.add(Calendar.DAY_OF_YEAR, -1);
                     break;
                  case WEEKS:
                     calendar.add(Calendar.WEEK_OF_YEAR, -1);
                     break;
                  case MONTHS:
                     calendar.add(Calendar.MONTH, -1);
                     break;
               }
               count--;
            }
         }
      }
      else
      {
         while (isBlockedDay(calendar.getTime()) && maxBlockedDays > 0)
         {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            maxBlockedDays--;
         }
      }

      if (maxBlockedDays == 0)
      {
         trace.warn("Advanced blocked days is greater than '" + maxBlockedDays
               + "', skipped blocked days calculation!");
      }

      return calendar.getTime();
   }

   private boolean isBlockedDay(Date date)
   {
      return CalendarUtils.isBlocked(date, calendarDocumentId);
   }
}
