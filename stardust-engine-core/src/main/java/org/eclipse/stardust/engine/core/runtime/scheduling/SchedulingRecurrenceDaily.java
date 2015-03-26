package org.eclipse.stardust.engine.core.runtime.scheduling;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceDaily extends SchedulingRecurrence
{
   @SuppressWarnings("deprecation")
   public String generateSchedule(JsonObject json)
   {
      StringBuilder cronExpr = new StringBuilder();
      cronExpr.append(getStartTime());

      JsonObject dailyRecurrenceOptions = json.get("dailyRecurrenceOptions").getAsJsonObject();
      String daysRecurrence = dailyRecurrenceOptions.get("daysRecurrence").getAsString();

      if (daysRecurrence.equals("interval"))
      {
         cronExpr.append(getStartDate().getDate())
                 .append('/')
                 .append(dailyRecurrenceOptions.get("daysIntervalCount").getAsString())
                 .append(" * ? *");
      }
      else if (daysRecurrence.equals("weekdays"))
      {
         cronExpr.append("? * MON-FRI *");
      }

      return cronExpr.toString();
   }

}
