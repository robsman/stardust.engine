package org.eclipse.stardust.engine.core.runtime.scheduling;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceYearly extends SchedulingRecurrence
{
   public String generateSchedule(JsonObject json)
   {
      StringBuilder cronExpr = new StringBuilder();
      cronExpr.append(getStartTime());

      JsonObject yearlyRecurrenceOptions = json.get("yearlyRecurrenceOptions").getAsJsonObject();
      int recurrenceYearIntervalCount = yearlyRecurrenceOptions.get("recurEveryYear").getAsInt();

      String yearlyRecurrence = yearlyRecurrenceOptions.get("yearlyRecurrence").getAsString();

      if (yearlyRecurrence.equals("date"))
      {
         cronExpr.append(yearlyRecurrenceOptions.get("onDay").getAsString())
                 .append(' ')
                 .append(yearlyRecurrenceOptions.get("onMonth").getAsString())
                 .append(" ? ");

      }
      else if (yearlyRecurrence.equals("weekday"))
      {
         cronExpr.append("? ")
                 .append(yearlyRecurrenceOptions.get("onTheMonth").getAsString())
                 .append(' ')
                 .append(SchedulingUtils.getDayNameFromIndex(
                       yearlyRecurrenceOptions.get("onTheXDayName").getAsInt()))
                 .append(getXDayOfMonthOrYear(
                       yearlyRecurrenceOptions.get("onTheXDay").getAsInt()))
                 .append(' ');
      }
      cronExpr.append(SchedulingUtils.getYearDateFormat().format(getStartDate()))
              .append('/')
              .append(recurrenceYearIntervalCount);

      return cronExpr.toString();
   }
}
