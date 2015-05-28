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
//   private static final String ROOT_PATH = "/business-calendars/timeOffCalendar";

   private Map<String, List<JsonObject>> eventsMap;

   private boolean isBlocked = false;

   private String calendarDocumentId;

   public TimeOffCalendarFinder(Date executionDate, String calendarDocumentId)
   {
      this(executionDate, new DocumentManagementServiceImpl());
      this.calendarDocumentId = calendarDocumentId;
   }

   private TimeOffCalendarFinder(Date executionDate, DocumentManagementService dms)
   {
      super(dms, null, executionDate, EXTENSION, null);
      eventsMap = CollectionUtils.newMap();
   }

   public boolean isBlocked()
   {
      return isBlocked;
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
      boolean blocking = CompareHelper.areEqual(SchedulingUtils.getAsString(json, "type"), "timeOff");
      if (blocking)
      {
         JsonObject scheduleJson = SchedulingUtils.getAsJsonObject(json, "scheduling");
         SchedulingRecurrence sc = SchedulingFactory.getScheduler(scheduleJson);
         Calendar now = getCalendar(executionDate);
         now.set(Calendar.MINUTE, 0);
         now.set(Calendar.HOUR, 0);
         sc.setDate(now.getTime());
         Date processSchedule = sc.processSchedule(scheduleJson, true);
         if (processSchedule != null)
         {
            Date startDate = getTime(scheduleJson, "startTimeStamp", processSchedule);
            Date endDate = getTime(scheduleJson, "endTimeStamp", processSchedule);
            if (startDate == null)
            {
               if (endDate != null)
               {
                  isBlocking = !executionDate.after(endDate);
               }
            }
            else
            {
               if (endDate == null)
               {
                  isBlocking = !executionDate.before(startDate);
               }
               else
               {
                  isBlocking = !executionDate.before(startDate) && !executionDate.after(endDate);
               }
            }
            isBlocking = executionTimeMatches(processSchedule);
         }
      }

      // mark execution date as blocked.
      this.isBlocked = isBlocking;

      return isBlocking;
   }

   private Date getTime(JsonObject scheduleJson, String name, Date when)
   {
      String value = SchedulingUtils.getAsString(scheduleJson, name);
      if (value != null)
      {
         Date time = new Date(Long.parseLong(value));
         Calendar ref = getCalendar(time);
         Calendar now = getCalendar(when);
         now.set(Calendar.HOUR, ref.get(Calendar.HOUR));
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

   private void collectEvents(List<JsonObject> events, String path, JsonObject documentJson)
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
            collectImportedEvents(events, SchedulingUtils.getAsString(importedCalendar.getAsJsonObject(), PATH_ATT), path);
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
               trace.warn("'" + source + "': could not find imported document '" + path + "'.");
            }
            else
            {
               collectEvents(events, path, documentJson);
            }
         }
      }
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
