package org.eclipse.stardust.engine.core.runtime.scheduling;

import com.google.gson.JsonObject;

public class SchedulingFactory
{
   public static SchedulingRecurrence getScheduler(JsonObject json)
   {
      try
      {
         switch (SchedulingUtils.RecurrencePattern.valueOf(
               json.get("recurrenceInterval").getAsString()))
         {
         case daily: return new SchedulingRecurrenceDaily();
         case weekly: return new SchedulingRecurrenceWeekly();
         case monthly: return new SchedulingRecurrenceMonthly();
         case yearly: return new SchedulingRecurrenceYearly();
         default /*"none"*/: return new SchedulingRecurrenceNone();
         }
      }
      catch (Exception ex)
      {
         return null;
      }
   }
}
