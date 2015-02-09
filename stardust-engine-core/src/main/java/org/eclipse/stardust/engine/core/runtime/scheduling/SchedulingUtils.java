package org.eclipse.stardust.engine.core.runtime.scheduling;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class SchedulingUtils
{
   private static final Logger trace = LogManager.getLogger(SchedulingRecurrence.class);

   public static DateFormat SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss:SSS");
   public static DateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
   public static DateFormat CLIENT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
   public static DateFormat YEAR_DATE_FORMAT = new SimpleDateFormat("yyyy");
   public static DateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm aa");

   public enum RecurrencePattern
   {
      daily, weekly, monthly, yearly
   }

   public enum EndMode
   {
      noEnd, endAfterNOcurrences, endByDate
   }

   public static final String[] DAY_SHORT_NAMES = {
      "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
   };


   public static final String[] WEEK_DAYS = {
      "sundays", "mondays", "tuesdays", "wednesdays", "thursdays", "fridays", "saturdays"
   };

   public static String getDayNameFromIndex(int dayIndex)
   {
      /*
       * For UI, java.util.date 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 =
       * Thursday, 5 = Friday, 6 = Saturday
       *
       * For Quartz, 1-7 or SUN-SAT
       */
      return DAY_SHORT_NAMES[dayIndex];
   }

   private static final String[] TIME_SLOTS = {
      "12:00 AM", "12:30 AM", "01:00 AM", "01:30 AM", "02:00 AM", "02:30 AM",
      "03:00 AM", "03:30 AM", "04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM",
      "06:00 AM", "06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
      "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
      "12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM",
      "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM",
      "06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM",
      "09:00 PM", "09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM"
   };

   /**
    * This function has dummy implementation , more meaningful implementation would be
    * provided by the usage of daemon/engine.
    *
    * @param selectedExecutionTime
    * @return
    */
   public static String getExecutionTime(int slot)
   {
      return TIME_SLOTS[slot - 1];
   }

   public static Date getParsedDate(String startDateStr, DateFormat format)
   {
      try
      {
         return format.parse(startDateStr);
      }
      catch (ParseException e)
      {
         trace.error(e);
         return null;
      }
   }
}
