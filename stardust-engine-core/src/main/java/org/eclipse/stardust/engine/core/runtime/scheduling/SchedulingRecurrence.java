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
      return processSchedule != null ? SchedulingUtils.CLIENT_DATE_FORMAT.format(processSchedule) : null;
   }

   @SuppressWarnings("deprecation")
   public Date processSchedule(JsonObject json, boolean daemon)
   {
      JsonObject recurrenceRange = json.get("recurrenceRange").getAsJsonObject();

      String startDateStr = recurrenceRange.get("startDate").getAsString();
      String executionTime = getExecutionTime(json);
      startDate = SchedulingUtils.getParsedDate(startDateStr + ' ' + executionTime, SchedulingUtils.INPUT_DATE_FORMAT);

      // Set Current time to compare with Scheduled Execution time.
      // startDate.setHours(0);
      // startDate.setMinutes(0);
      startDate.setSeconds(0);
      trace.info("Start Date: " + startDate);

      switch (SchedulingUtils.EndMode.valueOf(recurrenceRange.get("endMode").getAsString()))
      {
      case noEnd:
         return getNoEndNextExecutionDate(getCronExpression(json, daemon));
      case endAfterNOcurrences:
         return getNthExecutionDate(daemon, recurrenceRange, getCronExpression(json, false));
      case endByDate:
         return getByDateNextExecutionDate(recurrenceRange, getCronExpression(json, daemon));
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
         String convertDate = SchedulingUtils.CLIENT_DATE_FORMAT.format(date);
         futureExecutionDatesInRange.add(convertDate);
      }
      return futureExecutionDatesInRange;
   }

   @SuppressWarnings("deprecation")
   public List<Date> calculateScheduleDates(JsonObject json, String startDate, String endDate)
   {
      String executionTime = getExecutionTime(json);
      this.startDate = SchedulingUtils.getParsedDate(startDate + ' ' + executionTime, SchedulingUtils.INPUT_DATE_FORMAT);

      setStartTimeString(false);

      // Set Current time to compare with Scheduled Execution time.
      this.startDate.setHours(0);
      this.startDate.setMinutes(0);
      this.startDate.setSeconds(0);

      trace.info("Start Date: " + this.startDate);

      Date endDateObj = SchedulingUtils.getParsedDate(endDate,
            SchedulingUtils.CLIENT_DATE_FORMAT);
      endDateObj.setHours(23);
      endDateObj.setMinutes(59);
      endDateObj.setSeconds(59);

      trace.info("End Date: " + endDateObj);

      try
      {
         String cronExpressionInput = generateSchedule(json);
         CronExpression cronExpressionFuture = new CronExpression(cronExpressionInput);
         List<Date> futureExecutionDatesInRange = generateFutureExecutionDatesInRange(
               cronExpressionFuture, this.startDate, endDateObj);

         trace.info("Future occurences between Start date: " + this.startDate
               + " and End Date: " + endDateObj + ": "
               + futureExecutionDatesInRange.toString());

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
      trace.info("Next Execution Date: " + nextValidTimeAfter);
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
      trace.info("CronExpression: " + cronExpressionInput.toString());
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
   protected Date getByDateNextExecutionDate(JsonObject recurrenceRange, CronExpression cronExpression)
   {
      Date currentDate = getTimeStamp();

      String endDateStr = recurrenceRange.get("endDate").getAsString();
      Date endDate = SchedulingUtils.getParsedDate(endDateStr, SchedulingUtils.CLIENT_DATE_FORMAT);
      endDate.setHours(23);
      endDate.setMinutes(59);
      endDate.setSeconds(59);

      trace.info("End Date: " + endDate);
      if (endDate != null)
      {
         if (startDate.after(endDate))
         {
            trace.info("Invalid Dates: Start Date is after End Date");
         }
         else if (startDate.before(currentDate) && endDate.before(currentDate))
         {
            // Start Date and End Date are less than current date.
            trace.info("Start Date and End Date are less than current date");
         }
         else if (!currentDate.before(startDate) && !currentDate.after(endDate))
         {
            // Current Running Scenario
            return getNextExecutionDate(cronExpression, currentDate, endDate);
         }
         else if (startDate.after(currentDate))
         {
            // Future Date Scenario
            return getNextExecutionDate(cronExpression, startDate, endDate);
         }
      }
      return null;
   }

   protected Date getNthExecutionDate(boolean daemon, JsonObject recurrenceRange, CronExpression cronExpression)
   {
      Date currentDate = getTimeStamp();

      // stop after n occurrences
      int occurences = recurrenceRange.get("occurences").getAsInt();

      if (occurences <= 0)
      {
         return null;
      }

      // Generate n Future Execution Dates
      List<Date> nFutureExecutionDates = generateNFutureExecutionDates(cronExpression, startDate, occurences);
      trace.info("N Future occurences: " + nFutureExecutionDates.toString());

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
      trace.info("All Occurences are finished");
      return null;
   }

   protected Date getNoEndNextExecutionDate(CronExpression cronExpression)
   {
      Date currentDate = getTimeStamp();

      trace.info("No End Date is selected");
      return getNextExecutionDate(cronExpression, startDate.after(currentDate) ? startDate : currentDate, null);
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
      trace.info("Next Execution Date: " + nextValidTimeAfter);
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