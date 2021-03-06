/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.scheduling;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class ScheduledDocumentFinder<T extends ScheduledDocument>
{
   private static final Logger trace = LogManager.getLogger(ScheduledDocumentFinder.class);

   public static final String REALMS_DIR = "/realms";
   public static final String USER_DIR = "/users";
   public static final String DESIGNS_SUBFOLDER = "/designs";

   protected Date startingDate;
   protected Date executionDate;
   private DocumentManagementService dmService;

   private String extension;
   private String folderName;

   public ScheduledDocumentFinder(DocumentManagementService dmService, Date startingDate,
         Date executionDate, String extension, String folderName)
   {
      this.dmService = dmService;
      this.startingDate = startingDate;
      this.executionDate = executionDate;
      this.extension = extension;
      this.folderName = folderName;
   }

   protected abstract T createScheduledDocument(JsonObject documentJson, QName owner, String documentName,
         String documentPath, List<JsonObject> events);

   protected abstract List<JsonObject> getEvents(String path, JsonObject documentJson);

   protected String getExtension()
   {
      return extension;
   }

   protected String getFolderName()
   {
      return folderName;
   }

   protected Date getExecutionDate()
   {
      return executionDate;
   }

   protected DocumentManagementService getDocumentManagementService()
   {
      return dmService;
   }

   protected Folder getFolder(String folderName)
   {
      return dmService.getFolder(folderName);
   }

   protected boolean acceptEventType(String eventType)
   {
      return true;
   }

   public List<T> readAllDefinitions()
   {
      List<T> scheduledDocuments = CollectionUtils.newList();
      for (Document document : scanLocations())
      {
         if (document.getName().endsWith(extension))
         {
            boolean matches = false;

            String owner = document.getOwner();
            String documentName = document.getName();
            documentName = documentName.substring(0, documentName.length() - extension.length());

            JsonObject documentJson = getDocumentJson(document);

            List<JsonObject> matchingEvents = CollectionUtils.newList();
            List<JsonObject> events = getEvents(document.getPath(), documentJson);
            for (JsonObject event : events)
            {
               JsonObject scheduleJson = SchedulingUtils.getAsJsonObject(event, "scheduling");
               if (scheduleJson != null && isActive(scheduleJson) && acceptEventType(SchedulingUtils.getAsString(event, "type")))
               {
                  if (isBlocking(event))
                  {
                     matches = false;
                     break;
                  }

                  SchedulingRecurrence sc = SchedulingFactory.getScheduler(scheduleJson);
                  if (sc == null)
                  {
                     trace.warn("Invalid schedule in document '" + document.getPath() + "'.");
                  }
                  else
                  {
                     sc.setDate(executionDate);
                     Date processSchedule = sc.processSchedule(scheduleJson, true);

                     if (processSchedule != null && executionTimeMatches(processSchedule))
                     {
                        matches = true;
                        matchingEvents.add(event);
                     }
                  }
               }
            }

            if (matches)
            {
               T createdScheduledDocument = createScheduledDocument(documentJson,
                     owner == null ? new QName("") : QName.valueOf(owner),
                     documentName, document.getPath(), matchingEvents);
               if (createdScheduledDocument != null)
               {
               scheduledDocuments.add(createdScheduledDocument);
               }
            }
         }
      }
      return scheduledDocuments;
   }

   protected JsonObject getDocumentJson(String id)
   {
      Document document = getDocumentManagementService().getDocument(id);
      return document == null ? null : getDocumentJson(document);
   }

   protected JsonObject getDocumentJson(Document document)
   {
      String content;
      try
      {
         content = new String(dmService.retrieveDocumentContent(document.getId()), "UTF-8");
      }
      catch (Exception e)
      {
         try
         {
            content = new String(dmService.retrieveDocumentContent(document.getId()));
         }
         catch (Exception ex)
         {
            trace.warn("Could not read imported document '" + document.getPath() + "'.", ex);
            return new JsonObject();
         }
      }
      return new JsonParser().parse(content).getAsJsonObject();
   }

   protected boolean isBlocking(JsonObject json)
   {
      return false;
   }

   protected boolean isActive(JsonObject eventJson)
   {
      JsonElement active = eventJson.get("active");
      return active == null || active.getAsBoolean();
   }

   public List<Document> scanLocations()
   {
      List<Document> documents = new ArrayList<Document>();

      Folder folder = dmService.getFolder(folderName + DESIGNS_SUBFOLDER);
      if (folder != null)
      {
         documents.addAll(folder.getDocuments());
      }

      folder = dmService.getFolder(REALMS_DIR);
      if (folder != null)
      {
         for (Folder realmsFolder : folder.getFolders())
         {
            String realmPath = realmsFolder.getPath();
            Folder usersFolder = dmService.getFolder(realmPath + USER_DIR);
            if (usersFolder != null)
            {
               for (Folder userFolder : usersFolder.getFolders())
               {
                  String userPath = userFolder.getPath();
                  Folder subFolder = dmService.getFolder(userPath
                        + "/documents" + folderName + DESIGNS_SUBFOLDER);
                  if (subFolder != null)
                  {
                     documents.addAll(subFolder.getDocuments());
                  }
               }
            }
         }
      }

      folder = dmService.getFolder(folderName);
      if (folder != null)
      {
         Set<String> excludedPaths = getExcludedPaths();
         String designsFolderPath = folderName + DESIGNS_SUBFOLDER;

         for (Folder participantFolder : folder.getFolders())
         {
            String participantPath = participantFolder.getPath();
            if (!excludedPaths.contains(participantPath)
                  && !designsFolderPath.equals(participantPath))
            {
               Folder subFolder = dmService.getFolder(participantPath + DESIGNS_SUBFOLDER);
               if (subFolder != null)
               {
                  documents.addAll(subFolder.getDocuments());
               }
            }
         }
      }

      return documents;
   }

   protected Set<String> getExcludedPaths()
   {
      return Collections.emptySet();
   }

   protected boolean executionTimeMatches(Date processSchedule)
   {
      Calendar targetCalendar = getCalendar(executionDate);
      Calendar scheduleCalendar = getCalendar(processSchedule);
      if (startingDate == null)
      {
         return targetCalendar.getTimeInMillis() >= scheduleCalendar.getTimeInMillis();
      }
      Calendar startingCalendar = getCalendar(startingDate);
      return targetCalendar.getTimeInMillis() >= scheduleCalendar.getTimeInMillis()
            && scheduleCalendar.getTimeInMillis() > startingCalendar.getTimeInMillis();
   }

   protected Calendar getCalendar(Date date)
   {
      Calendar calendar = TimestampProviderUtils.getCalendar(date);
      calendar.setLenient(true);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      return calendar;
   }
}