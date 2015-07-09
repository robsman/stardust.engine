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

import org.eclipse.stardust.common.error.InternalException;
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
   private static final int MAX_BLOCKED_DAYS = 20000;

   private static Logger trace = LogManager.getLogger(BusinessDaysCondition.class);

   protected String calendarDocumentId;

   /*
   public BusinessDaysCondition(String calendarDocumentId, Comperator comperator,
         String qualifiedDataId, String dataPath, Offset offset)
   {
      super(comperator, qualifiedDataId, dataPath, offset);
      this.calendarDocumentId = calendarDocumentId;
   }
   */
   
   public BusinessDaysCondition(String calendarDocumentid, ConditionParameter lhsParameter, Comperator comperator,
         ConditionParameter rhsParameter, Offset offset)
   {
      super(lhsParameter, comperator, rhsParameter, offset);
      this.calendarDocumentId = calendarDocumentid;
   }


   @Override
   protected Date applyOffset(Date date, Offset offset)
   {
      long currentTimeMillis = System.currentTimeMillis();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      // 54.79 years worth of blocked days stop calculation to prevent
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
               amount = amount * 30;
               break;

            case WEEKS:
               amount = amount * 7;
               break;

            default:
               break;
         }

         if (amount == 0)
         {
            // only skip blocked business days forward.
            maxBlockedDaysCounter = skipBlockedBusinessDays(calendar,
                  maxBlockedDaysCounter, 1);
         }
         else if (amount > 0)
         {
            while (count < amount)
            {
               maxBlockedDaysCounter = skipBlockedBusinessDays(calendar,
                     maxBlockedDaysCounter, 1);

               calendar.add(Calendar.DAY_OF_YEAR, 1);
               count++;
            }
         }
         else
         {
            while (count > amount)
            {
               // skip blocked business days backward.
               maxBlockedDaysCounter = skipBlockedBusinessDays(calendar,
                     maxBlockedDaysCounter, -1);

               calendar.add(Calendar.DAY_OF_YEAR, -1);
               count--;
            }
         }

         // apply offset time
         if (offset.getHour() != null && offset.getMinute() != null)
         {
            calendar.set(Calendar.HOUR_OF_DAY, offset.getHour());
            calendar.set(Calendar.MINUTE, offset.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
         }
      }
      else
      {
         // The offset is null: only skip business days.
         maxBlockedDaysCounter = skipBlockedBusinessDays(calendar, maxBlockedDaysCounter,
               1);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("BusinessDay offset calculation took: "
               + (System.currentTimeMillis() - currentTimeMillis) + " ms");
      }

      if (maxBlockedDaysCounter == 0)
      {
         String message = "Calculation failed: Skipped " + MAX_BLOCKED_DAYS
               + " non-business days. Endless schedule in calendar?";
         throw new InternalException(message);
      }
      return calendar.getTime();
   }

   private int skipBlockedBusinessDays(Calendar calendar, int maxBlockedDays,
         int skipAmount)
   {
      while (CalendarUtils.isBlockedBusinessDay(calendar.getTime(), calendarDocumentId)
            && maxBlockedDays > 0)
      {
         calendar.add(Calendar.DAY_OF_YEAR, skipAmount);
         maxBlockedDays--;
      }
      return maxBlockedDays;
   }
}
