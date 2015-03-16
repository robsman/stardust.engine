package org.eclipse.stardust.engine.core.runtime.scheduling;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceNone extends SchedulingRecurrence
{
   @SuppressWarnings("deprecation")
   public String generateSchedule(JsonObject json)
   {
      StringBuilder cronExpr = new StringBuilder();
      cronExpr.append(getStartTime())
              .append(getStartDate().getDate())
              .append(' ')
              .append(getStartDate().getMonth() + 1)
              .append(" ? ")
              .append(getStartDate().getYear() + 1900);
      return cronExpr.toString();
   }

}
