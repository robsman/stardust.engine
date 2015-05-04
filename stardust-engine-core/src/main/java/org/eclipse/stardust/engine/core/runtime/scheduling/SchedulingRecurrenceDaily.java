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
         int interval = Integer.parseInt(dailyRecurrenceOptions.get("daysIntervalCount").getAsString());
         int day = interval < 2 ? 1 : getStartDate().getDate() % interval;
         if (day == 0)
         {
            day += interval;
         }
         cronExpr.append(day)
                 .append('/')
                 .append(interval)
                 .append(" * ? *");
      }
      else if (daysRecurrence.equals("weekdays"))
      {
         cronExpr.append("? * MON-FRI *");
      }

      return cronExpr.toString();
   }

}
