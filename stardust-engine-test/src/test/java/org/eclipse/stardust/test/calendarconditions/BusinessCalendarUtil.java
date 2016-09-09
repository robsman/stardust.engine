/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.test.calendarconditions;

import static org.eclipse.stardust.test.calendarconditions.JsonUtil.*;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.compatibility.extensions.dms.data.DocumentStorageBean;

import com.google.gson.JsonObject;

public class BusinessCalendarUtil
{
   private static final String FOLDER = "/business-calendars";
   private static final String CALENDAR_FOLDER = FOLDER + "/processingCalendar";
   private static final String TIMEOFF_FOLDER = FOLDER + "/timeOffCalendar";

   public static Document createTimeOffCalendar(DocumentManagementService dms, String name, JsonObject content, String owner)
         throws DocumentManagementServiceException, UnsupportedEncodingException
   {
      return createCalendar(dms, TIMEOFF_FOLDER, name, content, owner);
   }

   public static Document createProcessingCalendar(DocumentManagementService dms, String name, JsonObject content, String owner)
         throws DocumentManagementServiceException, UnsupportedEncodingException
   {
      return createCalendar(dms, CALENDAR_FOLDER, name, content, owner);
   }

   private static Document createCalendar(DocumentManagementService dms, String location,
         String name, JsonObject content, String owner)
               throws UnsupportedEncodingException
   {
      Folder folder = DmsUtils.ensureFolderHierarchyExists(location, dms);
      Document document = getDocument(dms, folder, name, owner);
      dms.updateDocument(document,
            content.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);
      return document;
   }

   private static Document getDocument(DocumentManagementService dms, Folder folder, String name, String owner)
   {
      String documentName = folder.getPath() + "/" + name;
      Document calendar = dms.getDocument(documentName);
      if (calendar == null)
      {
         DocumentInfo info = DmsUtils.createDocumentInfo(name);
         calendar = dms.createDocument(folder.getId(), info , null, "UTF-8");
         System.out.println("Created document: " + calendar);
      }
      calendar.setOwner(owner);
      return calendar;
   }

   public static JsonObject timeOffCalendar(String name, Object[] imports, Object... events)
   {
      return json(
         property("pluginId", "timeOffCalendar"),
         property("name", name),
         property("events", array()),
         property("recurringEvents", array(events)),
         property("importedCalendars", array(imports))
      );
   }

   public static JsonObject processingCalendar(String name, Object[] imports, Object... events)
   {
      return json(
         property("pluginId", "processingCalendar"),
         property("name", name),
         property("events", array()),
         property("recurringEvents", array(events)),
         property("importedCalendars", array(imports)),
         property("businessObjectInstance", json())
      );
   }

   public static Object[] importCalendars(Document... documents)
   {
      if (documents == null)
      {
         return null;
      }
      Object[] objects = new Object[documents.length];
      for (int i = 0; i < documents.length; i++)
      {
         objects[i] = importCalendar(documents[i]);
      }
      return objects;
   }

   private static JsonObject importCalendar(Document document)
   {
      return json(
            property("pluginId", "timeOffCalendar"),
            property("path", document.getPath())
      );
   }

   public static JsonObject timeOffEvent(String name, JsonObject schedule)
   {
      return event("timeOff", name, schedule, null);
   }

   public static JsonObject startEvent(String name, String processId, JsonObject schedule)
   {
      return event("processStart", name, schedule, startDetails(processId));
   }

   private static JsonObject startDetails(String processId)
   {
      return json(
            property("relationship", json()),
            property("processDefinitionId", processId),
            property("fallbackDate", "IGNORE")
      );
   }

   private static JsonObject event(String type, String name, JsonObject schedule, JsonObject details)
   {
      ArrayList<Property> properties = new ArrayList<Property>();
      properties.add(property("start", schedule.get("startTimeStamp").getAsLong()));
      properties.add(property("end", schedule.get("endTimeStamp").getAsLong()));
      properties.add(property("title", name));
      properties.add(property("allDay", schedule.get("allDay").getAsBoolean()));
      properties.add(property("description", ""));
      properties.add(property("scheduling", schedule));
      properties.add(property("className", array()));
      if (details != null)
      {
         properties.add(property("eventDetails", details));
      }
      properties.add(property("type", type));
      return json(properties.toArray(new Property[properties.size()]));
   }

   public static JsonObject weeklySchedule(Calendar executionTime, boolean allDay, Calendar start, Calendar end,
         Integer... days)
   {
      List<?> list = Arrays.asList(days);
      JsonObject options = json(
            property("recurrenceWeekCount", 1),
            property("mondays", list.contains(Calendar.MONDAY)),
            property("tuesdays", list.contains(Calendar.TUESDAY)),
            property("wednesdays", list.contains(Calendar.WEDNESDAY)),
            property("thursdays", list.contains(Calendar.THURSDAY)),
            property("fridays", list.contains(Calendar.FRIDAY)),
            property("saturdays", list.contains(Calendar.SATURDAY)),
            property("sundays", list.contains(Calendar.SUNDAY))
      );
      return schedule("weekly", executionTime, allDay, start, end,
         range(start, end, 1, "noEnd"), options);
   }

   public static JsonObject dailySchedule(Calendar executionTime, boolean allDay, Calendar start, Calendar end)
   {
      JsonObject options = json(
            property("daysRecurrence", "interval"),
            property("daysIntervalCount", 1)
      );
      return schedule("daily", executionTime, allDay, start, end,
         range(start, end, 1, "noEnd"), options);
   }

   private static JsonObject range(Calendar start, Calendar end,
         int occurences, String mode)
   {
      return json(
            property("startDate", getDate(start)),
            property("endDate", getDate(end)),
            property("occurences", occurences),
            property("endMode", mode)
      );
   }

   private static JsonObject schedule(String interval, Calendar executionTime, boolean allDay, Calendar start, Calendar end,
         JsonObject range, JsonObject options)
   {
      return json(
            property("recurrenceInterval", interval),
            property("executionTime", getExecutionTime(executionTime)),
            property("startTimeStamp", start.getTimeInMillis()),
            property("endTimeStamp", end.getTimeInMillis()),
            property("allDay", allDay),
            property("recurringEvent", true),
            property("active", true),
            property("recurrenceRange", range),
            property(interval + "RecurrenceOptions", options)
      );
   }

   private static String getExecutionTime(Calendar date)
   {
      return MessageFormat.format("{0,number,00}:{1,number,00} {2} {3}",
            date.get(Calendar.HOUR),
            date.get(Calendar.MINUTE),
            date.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM",
            date.getTimeZone().getID());
   }

   private static String getDate(Calendar date)
   {
      return MessageFormat.format("{0,number,0000}-{1,number,00}-{2,number,00}",
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH));
   }

   public static void setEndOfDay(Calendar date)
   {
      setStartOfDay(date);
      date.add(Calendar.DAY_OF_YEAR, 1);
      date.add(Calendar.MILLISECOND, -1);
   }

   public static void setStartOfDay(Calendar date)
   {
      date.set(Calendar.HOUR_OF_DAY, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.SECOND, 0);
      date.set(Calendar.MILLISECOND, 0);
   }

   public static Document mockDocument(String location, String name)
   {
      final String path = location + "/" + name;
      return new DocumentStorageBean()
      {
         private static final long serialVersionUID = 1L;

         public String getPath()
         {
            return path;
         }
      };
   }

   public static void main(String[] args)
   {
      Calendar start = Calendar.getInstance();
      start.set(2016, Calendar.SEPTEMBER, 1);
      setStartOfDay(start);

      Calendar end = Calendar.getInstance();
      end.set(2016, Calendar.SEPTEMBER, 30);
      setEndOfDay(end);

      Calendar exec = Calendar.getInstance();
      exec.set(2016, Calendar.SEPTEMBER, 12, 9, 15, 0);

      JsonObject timeOffJson = timeOffCalendar("Florin's Timeoff", null,
         timeOffEvent("Weekend", weeklySchedule(exec, true, start, end, Calendar.SATURDAY, Calendar.SUNDAY))
      );
      System.out.println(toPrettyString(timeOffJson));

      Document importedTimeOff = mockDocument(TIMEOFF_FOLDER, "timeOffCalendar.json");
      JsonObject processingJson = processingCalendar("General Process Start", importCalendars(importedTimeOff),
         startEvent("Dailies", "{ServiceConsumer}CommonIntegrationProcess",
            dailySchedule(exec, true, start, end))
      );
      System.out.println(toPrettyString(processingJson));
   }
}