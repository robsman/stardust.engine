/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.scheduling;

import java.text.ParseException;
import java.util.*;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

import com.google.gson.JsonObject;

public abstract class SchedulingRecurrence
{
   private static final Logger trace = LogManager.getLogger(SchedulingRecurrence.class);

   private String SECONDS = "0";

   private String MINUTES = "0";

   private String HOURS = "12";

   private Date startDate = null;

   private String startTime;

   private Date date;

   public String getSECONDS()
   {
      return SECONDS;
   }

   public void setSECONDS(String sECONDS)
   {
      SECONDS = sECONDS;
   }

   public String getMINUTES()
   {
      return MINUTES;
   }

   public void setMINUTES(String mINUTES)
   {
      MINUTES = mINUTES;
   }

   public String getHOURS()
   {
      return HOURS;
   }

   public void setHOURS(String hOURS)
   {
      HOURS = hOURS;
   }

   public Date getStartDate()
   {
      return startDate;
   }

   public void setStartDate(Date startDate)
   {
      this.startDate = startDate;
   }

   public String getStartTime()
   {
      return startTime;
   }

   public String getXDayOfMonthOrYear(int dayIndex)
   {
      String xDayOfMonthOrYear = "#" + dayIndex;
      if (dayIndex == 5)
      {
         //To handle special case of Last day of every month/year
         xDayOfMonthOrYear = "L";
      }
      return xDayOfMonthOrYear;
   }

   public abstract String generateSchedule(JsonObject json);

   public String processSchedule(JsonObject json)
   {
      Date processSchedule = processSchedule(json, false);
      return processSchedule != null ? SchedulingUtils.getClientDateFormat().format(processSchedule) : null;
   }

   public Date processSchedule(JsonObject json, boolean daemon)
   {
      return processSchedule(json, daemon, 0);
   }

   @SuppressWarnings("deprecation")
   public Date processSchedule(JsonObject json, boolean daemon, int offsetDay)
   {
      JsonObject recurrenceRange = json.get("recurrenceRange").getAsJsonObject();

      String startDateStr = recurrenceRange.get("startDate").getAsString();
      String executionTime = getExecutionTime(json);
      startDate = SchedulingUtils.getParsedDate(startDateStr + ' ' + executionTime
            , SchedulingUtils.getInputDateFormatExt(), SchedulingUtils.getInputDateFormat());

      Calendar instance = Calendar.getInstance();
      instance.setTime(startDate);
      instance.add(Calendar.DAY_OF_YEAR, offsetDay);
      startDate = instance.getTime();

      // Set Current time to compare with Scheduled Execution time.
      // startDate.setHours(0);
      // startDate.setMinutes(0);
      startDate.setSeconds(0);
      if (trace.isDebugEnabled())
      {
         trace.debug("Start Date: " + startDate);
      }

      switch (SchedulingUtils.EndMode.valueOf(recurrenceRange.get("endMode").getAsString()))
      {
      case noEnd:
         return getNoEndNextExecutionDate(getCronExpression(json, daemon), offsetDay);
      case endAfterNOcurrences:
         return getNthExecutionDate(daemon, recurrenceRange, getCronExpression(json, false), offsetDay);
      case endByDate:
         return getByDateNextExecutionDate(recurrenceRange, getCronExpression(json, daemon), offsetDay);
      }
      return null;
   }

   protected String getExecutionTime(JsonObject json)
   {
      String executionTime = json.get("executionTime").getAsString();
      if (executionTime != null && executionTime.length() == 2)
      {
         try
         {
            executionTime = SchedulingUtils.getExecutionTime(Integer.parseInt(executionTime));
         }
         catch (Exception ex)
         {
         }
      }
      return executionTime;
   }

   public List<String> calculateSchedule(JsonObject json, String startDate, String endDate)
   {
      List<Date> scheduleDatesinRange = calculateScheduleDates(json, startDate, endDate);
      List<String> futureExecutionDatesInRange = new ArrayList<String>(scheduleDatesinRange.size());
      for (Date date : scheduleDatesinRange)
      {
         String convertDate = SchedulingUtils.getClientDateFormat().format(date);
         futureExecutionDatesInRange.add(convertDate);
      }
      return futureExecutionDatesInRange;
   }

   @SuppressWarnings("deprecation")
   public List<Date> calculateScheduleDates(JsonObject json, String startDate, String endDate)
   {
      String executionTime = getExecutionTime(json);
      JsonObject recurrenceRange = json.get("recurrenceRange").getAsJsonObject();
      String startDateStr = recurrenceRange.get("startDate").getAsString();
      Date startDate1 = SchedulingUtils.getParsedDate(startDateStr + ' ' + executionTime,
            SchedulingUtils.getInputDateFormatExt(), SchedulingUtils.getInputDateFormat());
      startDate1.setSeconds(0);

      this.startDate = SchedulingUtils.getParsedDate(startDate + ' ' + executionTime,
            SchedulingUtils.getInputDateFormatExt(), SchedulingUtils.getInputDateFormat());
      this.startDate.setSeconds(0);

      if (this.startDate.before(startDate1))
      {
         this.startDate = startDate1;
      }
      if (trace.isDebugEnabled())
      {
         trace.debug("Start Date: " + this.startDate);
      }

      setStartTimeString(false);
      this.startDate.setHours(0);
      this.startDate.setMinutes(0);

      Date endDateObj = SchedulingUtils.getParsedDate(endDate, SchedulingUtils.getClientDateFormat());
      endDateObj.setHours(23);
      endDateObj.setMinutes(59);
      endDateObj.setSeconds(59);

      if (trace.isDebugEnabled())
      {
         trace.debug("End Date: " + endDateObj);
      }

      try
      {
         String cronExpressionInput = generateSchedule(json);
         CronExpression cronExpressionFuture = new CronExpression(cronExpressionInput);
         List<Date> futureExecutionDatesInRange = generateFutureExecutionDatesInRange(
               cronExpressionFuture, this.startDate, endDateObj);

         if (trace.isDebugEnabled())
         {
            trace.debug("Future occurences between Start date: " + this.startDate
               + " and End Date: " + endDateObj + ": "
               + futureExecutionDatesInRange.toString());
         }

         return futureExecutionDatesInRange;
      }
      catch (ParseException e)
      {
         trace.error(e);
         return Collections.emptyList();
      }
   }

   private List<Date> generateFutureExecutionDatesInRange(CronExpression cronExpression,
         Date startDate, Date endDate)
   {
      Date clonedStartDate = new Date(startDate.getTime());

      List<Date> futureExecutionDatesInRange = new ArrayList<Date>();

      Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(clonedStartDate);
      futureExecutionDatesInRange.add(nextValidTimeAfter);
      if (trace.isDebugEnabled())
      {
         trace.debug("Next Execution Date: " + nextValidTimeAfter);
      }
      clonedStartDate = nextValidTimeAfter;
      if (endDate == null)
      {
         return futureExecutionDatesInRange;
      }
      else
      {
         while (true)
         {
            nextValidTimeAfter = cronExpression.getNextValidTimeAfter(clonedStartDate);
            if (nextValidTimeAfter.after(endDate))
            {
               return futureExecutionDatesInRange;
            }
            futureExecutionDatesInRange.add(nextValidTimeAfter);
            clonedStartDate = nextValidTimeAfter;
         }
      }
   }

   protected CronExpression getCronExpression(JsonObject json, boolean daemon)
   {
      setStartTimeString(daemon);
      String cronExpressionInput = generateSchedule(json);
      if (trace.isDebugEnabled())
      {
         trace.debug("CronExpression: " + cronExpressionInput.toString());
      }
      try
      {
         return new CronExpression(cronExpressionInput);
      }
      catch (ParseException e)
      {
         trace.error(e);
      }
      return null;
   }

   @SuppressWarnings("deprecation")
   protected Date getByDateNextExecutionDate(JsonObject recurrenceRange, CronExpression cronExpression, int offsetDay)
   {
      Date currentDate = getTimeStamp();

      Calendar instance = Calendar.getInstance();
      instance.setTime(currentDate);
      instance.add(Calendar.DAY_OF_YEAR, offsetDay);
      currentDate = instance.getTime();

      String endDateStr = recurrenceRange.get("endDate").getAsString();
      Date endDate = SchedulingUtils.getParsedDate(endDateStr, SchedulingUtils.getClientDateFormat());
      endDate.setHours(23);
      endDate.setMinutes(59);
      endDate.setSeconds(59);

      if (trace.isDebugEnabled())
      {
         trace.debug("End Date: " + endDate);
      }
      if (endDate != null)
      {
         if (startDate.after(endDate))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Invalid Dates: Start Date is after End Date");
            }
         }
         else if (startDate.before(currentDate) && endDate.before(currentDate))
         {
            // Start Date and End Date are less than current date.
            if (trace.isDebugEnabled())
            {
               trace.debug("Start Date and End Date are less than current date");
            }
         }
         else if (!currentDate.before(startDate) && !currentDate.after(endDate))
         {
            // Current Running Scenario
            return getNextExecutionDate(cronExpression, currentDate, endDate);
         }
         else if (startDate.after(currentDate))
         {
            // we have to do this because of wrong result if it is the same day (startDate after currentDate on same day)
            Date compareDate = (Date) startDate.clone();
            compareDate.setHours(currentDate.getHours());
            compareDate.setMinutes(currentDate.getMinutes());

            // from here next execution date
            Date startDateClone = (Date) startDate.clone();
            startDateClone.setHours(0);
            startDateClone.setMinutes(0);
            startDateClone.setSeconds(0);
            // Future Date Scenario
            return getNextExecutionDate(cronExpression, compareDate.after(currentDate) ? startDateClone : currentDate, endDate);
         }
      }
      return null;
   }

   protected Date getNthExecutionDate(boolean daemon, JsonObject recurrenceRange, CronExpression cronExpression, int offsetDay)
   {
      Date currentDate = getTimeStamp();

      Calendar instance = Calendar.getInstance();
      instance.setTime(currentDate);
      instance.add(Calendar.DAY_OF_YEAR, offsetDay);
      currentDate = instance.getTime();

      // stop after n occurrences
      int occurences = recurrenceRange.get("occurences").getAsInt();

      if (occurences <= 0)
      {
         return null;
      }

      // Generate n Future Execution Dates
      List<Date> nFutureExecutionDates = generateNFutureExecutionDates(cronExpression, startDate, occurences);
      if (trace.isDebugEnabled())
      {
         trace.debug("N Future occurences: " + nFutureExecutionDates.toString());
      }

      Date lastDate = nFutureExecutionDates.get(nFutureExecutionDates.size() - 1);
      if (!lastDate.before(currentDate))
      {
         for (Date date : nFutureExecutionDates)
         {
            if (!currentDate.after(date))
            {
               return date;
            }
         }
      }
      if (trace.isDebugEnabled())
      {
         trace.debug("All Occurences are finished");
      }
      return null;
   }

   @SuppressWarnings("deprecation")
   protected Date getNoEndNextExecutionDate(CronExpression cronExpression, int offsetDay)
   {
      Date currentDate = getTimeStamp();

      Calendar instance = Calendar.getInstance();
      instance.setTime(currentDate);
      instance.add(Calendar.DAY_OF_YEAR, offsetDay);
      currentDate = instance.getTime();

      if (trace.isDebugEnabled())
      {
         trace.debug("No End Date is selected");
      }
      // we have to do this because of wrong result if it is the same day (startDate after currentDate on same day)
      Date compareDate = (Date) startDate.clone();
      compareDate.setHours(currentDate.getHours());
      compareDate.setMinutes(currentDate.getMinutes());

      // from here next execution date
      Date startDateClone = (Date) startDate.clone();
      startDateClone.setHours(0);
      startDateClone.setMinutes(0);
      startDateClone.setSeconds(0);
      return getNextExecutionDate(cronExpression, compareDate.after(currentDate) ? startDateClone : currentDate, null);
   }

   /**
    * Retrieves the current time stamp minute aligned.
    *
    * @return a date object corresponding to the start time stamp of the current minute.
    */
   protected Date getTimeStamp()
   {
      if (date == null)
      {
         date = TimestampProviderUtils.getTimeStamp();
      }
      Calendar calendar = TimestampProviderUtils.getCalendar(date);
      calendar.setLenient(true);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      return calendar.getTime();
   }

   @SuppressWarnings("deprecation")
   private void setStartTimeString(boolean daemon)
   {
      // TODO: (fh) check consequences, but it should be:
      // startTime = startDate.getSeconds() + ' ' + startDate.getMinutes() + ' ' + startDate.getHours() + ' ';

      String cronSeconds = "0/1";
      if (!daemon)
      {
         try
         {
            cronSeconds = Integer.toString(startDate.getSeconds());
         }
         catch (Exception e)
         {
            trace.error(e);
         }
      }
      startTime = cronSeconds + ' ' + startDate.getMinutes() + ' ' + startDate.getHours() + ' ';
   }

   private Date getNextExecutionDate(CronExpression cronExpression, Date startDate, Date endDate)
   {
      Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(startDate);
      if (trace.isDebugEnabled())
      {
         trace.debug("Next Execution Date: " + nextValidTimeAfter);
      }
      if (endDate == null)
      {
         return nextValidTimeAfter;
      }
      else
      {
         return nextValidTimeAfter.before(endDate) ? nextValidTimeAfter : null;
      }
   }

   private List<Date> generateNFutureExecutionDates(CronExpression cronExpression,
         Date startDate, int count)
   {
      List<Date> nFutureExecutionDates = new ArrayList<Date>(count);
      startDate = new Date(startDate.getTime() - 1000);
      for (int i = 0; i < count; i++)
      {
         startDate = cronExpression.getNextValidTimeAfter(startDate);
         nFutureExecutionDates.add(startDate);
      }
      return nFutureExecutionDates;
   }

   public void setDate(Date date)
   {
      this.date = date;
   }
}