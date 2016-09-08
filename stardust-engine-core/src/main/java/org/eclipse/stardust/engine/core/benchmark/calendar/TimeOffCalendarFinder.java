/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.benchmark.calendar;

import java.util.*;

import javax.xml.namespace.QName;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentManagementServiceImpl;
import org.eclipse.stardust.engine.core.runtime.scheduling.*;

public class TimeOffCalendarFinder extends ScheduledDocumentFinder<ScheduledDocument>
{
   private static final Logger trace = LogManager.getLogger(TimeOffCalendarFinder.class);

   private static final String PATH_ATT = "path";

   private static final String EXTENSION = ".json";

   // private static final String ROOT_PATH = "/business-calendars/timeOffCalendar";

   private Map<String, List<JsonObject>> eventsMap;

   private boolean isBlocked = false;

   private String calendarDocumentId;

   private Map<String, JsonObject> calendarJsonByPath;

   private Map<Pair<Integer, Integer>, Boolean> isDayOfYearBlocked;

   public TimeOffCalendarFinder(String calendarDocumentId)
   {
      this(new DocumentManagementServiceImpl());
      this.calendarDocumentId = calendarDocumentId;
   }

   private TimeOffCalendarFinder(DocumentManagementService dms)
   {
      super(dms, null, null, EXTENSION, null);
      eventsMap = CollectionUtils.newMap();
      calendarJsonByPath = CollectionUtils.newMap();
      isDayOfYearBlocked = CollectionUtils.newMap();
   }

   public void clearCache()
   {
      calendarJsonByPath.clear();
      isDayOfYearBlocked.clear();
   }

   public synchronized Boolean isBlocked(Date date)
   {
      Pair<Integer, Integer> key = getKey(date);
      Boolean dayBlocked = isDayOfYearBlocked.get(key);

      if (dayBlocked == null)
      {
         this.executionDate = date;
         this.readAllDefinitions();
         if (this.calendarJsonByPath.isEmpty())
         {
            return null;
         }
         dayBlocked = this.isBlocked;
         isDayOfYearBlocked.put(key, dayBlocked);
      }

      return dayBlocked;

   }

   private Pair<Integer, Integer> getKey(Date date)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);

      return new Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR));
   }

   @Override
   protected ScheduledDocument createScheduledDocument(JsonObject documentJson,
         QName owner, String documentName, String documentPath, List<JsonObject> events)
   {
      // No document to schedule, just time off blocking needs to be evaluated.
      return null;
   }

   @Override
   public List<Document> scanLocations()
   {
      List<Document> documents = new ArrayList<Document>();
      Document calendarDocument = getDocumentManagementService().getDocument(
            calendarDocumentId);
      if (calendarDocument != null)
      {
         documents.add(calendarDocument);
      }
      return documents;
   }

   @Override
   protected boolean acceptEventType(String eventType)
   {
      return "timeOff".equals(eventType);
   }

   protected boolean isBlocking(JsonObject json)
   {
      boolean isBlocking = false;
      boolean blocking = CompareHelper.areEqual(
            SchedulingUtils.getAsString(json, "type"), "timeOff");
      boolean allDay = isAllDay(json);
      if (blocking && allDay)
      {
         JsonObject scheduleJson = SchedulingUtils.getAsJsonObject(json, "scheduling");
         SchedulingRecurrence sc = SchedulingFactory.getScheduler(scheduleJson);

         Calendar now = getCalendar(executionDate);
         now.set(Calendar.MINUTE, 0);
         now.set(Calendar.HOUR_OF_DAY, 0);
         sc.setDate(now.getTime());

         Date timeOffSchedule = sc.processSchedule(scheduleJson, true/*, -1*/);
         if (timeOffSchedule == null && sc instanceof SchedulingRecurrenceNone)
         {
            // use today
            timeOffSchedule = new Date();
         }
         if (timeOffSchedule != null)
         {
            Date startDate = getTime(scheduleJson, "startTimeStamp", timeOffSchedule);
            Date endDate = getTime(scheduleJson, "endTimeStamp", timeOffSchedule);
            if (startDate == null)
            {
               isBlocking = endDate == null || !executionDate.after(endDate);
            }
            else
            {
               if (endDate == null)
               {
                  isBlocking = !executionDate.before(startDate);
               }
               else
               {
                  isBlocking = !executionDate.before(startDate)
                        && !executionDate.after(endDate);
               }
            }
         }
      }

      // mark execution date as blocked.
      this.isBlocked = isBlocking;

      return isBlocking;
   }

   private boolean isAllDay(JsonObject json)
   {
      boolean allDay = json.get("allDay").getAsBoolean();

      Calendar start = getCalendar(new Date (json.get("start").getAsLong()));
      Calendar end = getCalendar(new Date (json.get("end").getAsLong()));

      if (!allDay && start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)
          && start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
          && start.get(Calendar.HOUR_OF_DAY) == 0
          && start.get(Calendar.MINUTE) == 0
          && end.get(Calendar.HOUR_OF_DAY) == 23
          && end.get(Calendar.MINUTE) == 59
          )
      {
         allDay = true;
      }
      return allDay;
   }

   private Date getTime(JsonObject scheduleJson, String name, Date when)
   {
      String value = SchedulingUtils.getAsString(scheduleJson, name);
      if (value != null)
      {
         Date time = new Date(Long.parseLong(value));
         Calendar ref = getCalendar(time);
         Calendar now = getCalendar(when);
         now.set(Calendar.HOUR_OF_DAY, ref.get(Calendar.HOUR_OF_DAY));
         now.set(Calendar.MINUTE, ref.get(Calendar.MINUTE));
         return now.getTime();
      }
      return null;
   }

   @Override
   protected List<JsonObject> getEvents(String path, JsonObject documentJson)
   {
      eventsMap.clear();
      List<JsonObject> events = CollectionUtils.newList();
      collectEvents(events, path, documentJson);
      return events;
   }

   private void collectEvents(List<JsonObject> events, String path,
         JsonObject documentJson)
   {
      List<JsonObject> vts = eventsMap.get(path);
      if (vts == null)
      {
         vts = CollectionUtils.newList();
         addEvents(vts, documentJson.getAsJsonArray("events"));
         addEvents(vts, documentJson.getAsJsonArray("recurringEvents"));
         eventsMap.put(path, vts);
      }
      events.addAll(vts);

      JsonArray importedCalendars = documentJson.getAsJsonArray("importedCalendars");
      if (importedCalendars != null)
      {
         for (JsonElement importedCalendar : importedCalendars)
         {
            collectImportedEvents(events, SchedulingUtils.getAsString(
                  importedCalendar.getAsJsonObject(), PATH_ATT), path);
         }
      }
   }

   private void collectImportedEvents(List<JsonObject> events, String path, String source)
   {
      if (path != null)
      {
         List<JsonObject> vts = eventsMap.get(path);
         if (vts == null)
         {
            JsonObject documentJson = getDocumentJson(path);
            if (documentJson == null)
            {
               trace.warn("'" + source + "': could not find imported document '" + path
                     + "'.");
            }
            else
            {
               collectEvents(events, path, documentJson);
            }
         }
      }
   }

   @Override
   protected JsonObject getDocumentJson(String path)
   {
      JsonObject jsonObject = this.calendarJsonByPath.get(path);
      if (jsonObject == null)
      {
         jsonObject = super.getDocumentJson(path);
         if (jsonObject != null)
         {
            this.calendarJsonByPath.put(path, jsonObject);
         }
      }
      return jsonObject;
   }

   @Override
   protected JsonObject getDocumentJson(Document document)
   {
      JsonObject jsonObject = this.calendarJsonByPath.get(document.getPath());
      if (jsonObject == null)
      {
         jsonObject = super.getDocumentJson(document);
         if (jsonObject != null)
         {
            this.calendarJsonByPath.put(document.getPath(), jsonObject);
         }
      }
      return jsonObject;
   }

   private void addEvents(List<JsonObject> events, JsonArray eventsArray)
   {
      if (eventsArray != null)
      {
         for (JsonElement event : eventsArray)
         {
            events.add(event.getAsJsonObject());
         }
      }
   }
}
