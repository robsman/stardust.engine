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
import java.util.List;

import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

public class CalendarCondition extends DateCondition
{
   protected String calendarDocumentId;

   @Override
   public boolean evaluate(ActivityInstanceBean ai)
   {
      boolean result = super.evaluate(ai);

      result &= !isBlockedDay();

      return result;
   }

   @Override
   public boolean evaluate(ProcessInstanceBean pi)
   {
      boolean result = super.evaluate(pi);

      result &= !isBlockedDay();

      return result;
   }

   private boolean isBlockedDay()
   {
      Calendar currentTime = Calendar.getInstance();
      currentTime.setTime(new Date());

      Calendar blockedTime = Calendar.getInstance();
      for (Date date : getBlockedDays())
      {
         blockedTime.setTime(date);

         boolean sameDay = currentTime.get(Calendar.DAY_OF_YEAR) == blockedTime
               .get(Calendar.DAY_OF_YEAR)
               && currentTime.get(Calendar.YEAR) == blockedTime.get(Calendar.YEAR);

         if (sameDay)
         {
            return true;
         }
      }

      return false;
   }

   private List<Date> getBlockedDays()
   {
      // TODO cache for a time.
      List<Date> blockedDays = CalendarUtils.getBlockedDays(calendarDocumentId);

      return blockedDays;

   }
}
