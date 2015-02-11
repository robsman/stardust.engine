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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentManagementServiceImpl;
import org.eclipse.stardust.engine.core.runtime.scheduling.ScheduledDocumentFinder;
import org.eclipse.stardust.engine.core.runtime.scheduling.SchedulingUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ScheduledCalendarFinder extends ScheduledDocumentFinder<ScheduledCalendar>
{
   private Map<String, List<JsonObject>> eventsMap;
   private Map<String, Document> documentsMap;

   public ScheduledCalendarFinder(Date executionDate)
   {
      this(executionDate, new DocumentManagementServiceImpl());
   }

   public ScheduledCalendarFinder(Date executionDate, DocumentManagementService dms)
   {
      super(dms, null, executionDate, ".bpmcal", "/business-calendars");
      eventsMap = CollectionUtils.newMap();
      documentsMap = CollectionUtils.newMap();
   }

   @Override
   protected ScheduledCalendar createScheduledDocument(JsonObject documentJson,
         QName owner, String documentName, List<JsonObject> events)
   {
      return new ScheduledCalendar(documentJson, owner, documentName, events);
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
      return "processStartEvent".equals(eventType) || "timeOff".equals(eventType);
   }

   protected boolean isBlocking(JsonObject json)
   {
      return CompareHelper.areEqual(SchedulingUtils.getAsString(json, "type"), "timeOff");
   }

   @Override
   protected List<JsonObject> getEvents(JsonObject documentJson)
   {
      eventsMap.clear();
      List<JsonObject> events = CollectionUtils.newList();
      collectEvents(events, documentJson);
      return events;
   }

   private void collectEvents(List<JsonObject> events, JsonObject documentJson)
   {
      String uuid = SchedulingUtils.getAsString(documentJson, "uuid");
      if (uuid == null)
      {
         addEvents(events, documentJson.getAsJsonArray("events"));
         addEvents(events, documentJson.getAsJsonArray("recurringEvents"));
      }
      else
      {
         List<JsonObject> vts = eventsMap.get(uuid);
         if (vts == null)
         {
            vts = CollectionUtils.newList();
            addEvents(vts, documentJson.getAsJsonArray("events"));
            addEvents(vts, documentJson.getAsJsonArray("recurringEvents"));
            eventsMap.put(uuid, vts);
         }
         events.addAll(vts);
      }
      JsonArray importedCalendars = documentJson.getAsJsonArray("importedCalendars");
      if (importedCalendars != null)
      {
         for (JsonElement importedCalendar : importedCalendars)
         {
            collectImportedEvents(events, SchedulingUtils.getAsString(importedCalendar.getAsJsonObject(), "uuid"));
         }
      }
   }

   private void collectImportedEvents(List<JsonObject> events, String uuid)
   {
      List<JsonObject> vts = eventsMap.get(uuid);
      if (vts == null)
      {
         JsonObject documentJson = getDocumentJson(uuid);
         collectEvents(events, documentJson);
      }
      else
      {
         //events.addAll(vts);
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
   protected JsonObject getDocumentJson(String id)
   {
      Document document = documentsMap.get(id);
      return document == null ? super.getDocumentJson(id) : getDocumentJson(document);
   }

   protected void collectDocuments(List<Document> documents, Folder folder)
   {
      if (folder != null)
      {
         for (Document document : folder.getDocuments())
         {
            if (document.getName().endsWith(".bpmcal"))
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
