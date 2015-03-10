package org.eclipse.stardust.engine.core.runtime.scheduling;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceMonthly extends SchedulingRecurrence
{
   @SuppressWarnings("deprecation")
   public String generateSchedule(JsonObject json)
   {
      JsonObject monthlyRecurrenceOptions = json.get("monthlyRecurrenceOptions").getAsJsonObject();
      String monthsRecurrence = monthlyRecurrenceOptions.get("monthsRecurrence").getAsString();

      int startMonth = getStartDate().getMonth() + 1;

      StringBuilder cronExpr = new StringBuilder();
      cronExpr.append(getStartTime());
      if (monthsRecurrence.equals("day"))
      {
         cronExpr.append(monthlyRecurrenceOptions.get("dayNumber"))
                 .append(' ')
                 .append(startMonth)
                 .append('/')
                 .append(monthlyRecurrenceOptions.get("month"))
                 .append(" ? *");
      }
      else if (monthsRecurrence.equals("weekday"))
      {
         cronExpr.append("? ")
                 .append(startMonth)
                 .append('/')
                 .append(monthlyRecurrenceOptions.get("monthIndex"))
                 .append(' ')
                 .append(SchedulingUtils.getDayNameFromIndex(monthlyRecurrenceOptions.get("day").getAsInt()))
                 .append(getXDayOfMonthOrYear(monthlyRecurrenceOptions.get("dayIndex").getAsInt()))
                 .append(" *");
      }

      return cronExpr.toString();
   }
}
