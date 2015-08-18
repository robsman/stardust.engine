/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.business_calendar.daemon;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentManagementServiceImpl;
import org.eclipse.stardust.engine.core.runtime.scheduling.ScheduledDocumentFinder;
import org.eclipse.stardust.engine.core.runtime.scheduling.SchedulingFactory;
import org.eclipse.stardust.engine.core.runtime.scheduling.SchedulingRecurrence;
import org.eclipse.stardust.engine.core.runtime.scheduling.SchedulingUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ScheduledCalendarFinder extends ScheduledDocumentFinder<ScheduledCalendar>
{
   private static final Logger trace = LogManager.getLogger(ScheduledCalendarFinder.class);

   private static final String PATH_ATT = "path";
   private static final String EXTENSION = ".json";
   private static final String ROOT_PATH = "/business-calendars/processingCalendar";

   private Map<String, List<JsonObject>> eventsMap;
   private Map<String, Document> documentsMap;

   public ScheduledCalendarFinder(Date executionDate)
   {
      this(executionDate, new DocumentManagementServiceImpl());
   }

   public ScheduledCalendarFinder(Date executionDate, DocumentManagementService dms)
   {
      super(dms, null, executionDate, EXTENSION, ROOT_PATH);
      eventsMap = CollectionUtils.newMap();
      documentsMap = CollectionUtils.newMap();
   }

   @Override
   protected ScheduledCalendar createScheduledDocument(JsonObject documentJson,
         QName owner, String documentName, String documentPath, List<JsonObject> events)
   {
      return new ScheduledCalendar(documentJson, owner, documentName, documentPath, events);
   }

   @Override
   public List<Document> scanLocations()
   {
      List<Document> documents = new ArrayList<Document>();
      collectDocuments(documents, getFolder(getFolderName()));
      return documents;
   }

   @Override
   protected boolean acceptEventType(String eventType)
   {
      return "processStart".equals(eventType) || "timeOff".equals(eventType);
   }

   protected boolean isBlocking(JsonObject json)
   {
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
                  return !executionDate.after(endDate);
               }
            }
            else
            {
               if (endDate == null)
               {
                  return !executionDate.before(startDate);
               }
               else
               {
                  return !executionDate.before(startDate) && !executionDate.after(endDate);
               }
            }
            return executionTimeMatches(processSchedule);
         }
      }
      return false;
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

   @Override
   protected JsonObject getDocumentJson(String path)
   {
      Document document = documentsMap.get(path);
      return document == null ? super.getDocumentJson(path) : getDocumentJson(document);
   }

   protected void collectDocuments(List<Document> documents, Folder folder)
   {
      if (folder != null)
      {
         for (Document document : folder.getDocuments())
         {
            if (document.getName().endsWith(EXTENSION))
            {
               documents.add(document);
               documentsMap.put(document.getId(), document);
            }
         }
         for (Folder subfolder : folder.getFolders())
         {
            collectDocuments(documents, getFolder(subfolder.getId()));
         }
      }
   }
}
