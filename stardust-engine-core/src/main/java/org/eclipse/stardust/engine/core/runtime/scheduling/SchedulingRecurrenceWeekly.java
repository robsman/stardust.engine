package org.eclipse.stardust.engine.core.runtime.scheduling;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceWeekly extends SchedulingRecurrence
{
   public String generateSchedule(JsonObject json)
   {
      JsonObject weeklyRecurrenceOptions = json.get("weeklyRecurrenceOptions").getAsJsonObject();
      StringBuilder builder = new StringBuilder()
            .append(getStartTime())
            .append("? * ");
      int count = 0;
      for (int i = 1; i <= SchedulingUtils.WEEK_DAYS.length; i++)
      {
         int x = i % SchedulingUtils.WEEK_DAYS.length;
         if (weeklyRecurrenceOptions.get(SchedulingUtils.WEEK_DAYS[x]).getAsBoolean())
         {
            if (count > 0)
            {
               builder.append(',');
            }
            builder.append(SchedulingUtils.DAY_SHORT_NAMES[x]);
            count++;
         }
      }
      builder.append(" *");
      return builder.toString();
   }
}
