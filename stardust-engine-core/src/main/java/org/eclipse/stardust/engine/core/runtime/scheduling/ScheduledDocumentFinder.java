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
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class ScheduledDocumentFinder<T extends ScheduledDocument>
{
   public static final String REALMS_DIR = "/realms";
   public static final String USER_DIR = "/users";
   public static final String DESIGNS_SUBFOLDER = "/designs";

   private Date startingDate;
   private Date executionDate;
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

   protected abstract T createScheduledDocument(JsonObject documentJson, QName valueOf, String reportName);

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

   public List<T> readAllDefinitions()
   {
      List<T> scheduledDocuments = CollectionUtils.newList();
      for (Document document : scanLocations())
      {
         if (document.getName().endsWith(extension))
         {
            boolean matches = false;

            String owner = document.getOwner();
            String reportName = document.getName();
            reportName = reportName.substring(0, reportName.length() - extension.length());

            String content = new String(dmService.retrieveDocumentContent(document.getId()));
            JsonObject documentJson = new JsonParser().parse(content).getAsJsonObject();
            JsonArray eventsArray = documentJson.getAsJsonArray("events");

            if (eventsArray != null)
            {
               for (int n = 0; n < eventsArray.size(); ++n)
               {
                  JsonObject schedulingJson = eventsArray.get(n).getAsJsonObject();
                  if (schedulingJson != null)
                  {
                     if (schedulingJson.get("active") != null
                           && schedulingJson.get("active").getAsBoolean())
                     {
                        SchedulingRecurrence sc = SchedulingFactory.getScheduler(schedulingJson);
                        Date processSchedule = sc.processSchedule(schedulingJson, true);

                        if (processSchedule != null && executionTimeMatches(processSchedule))
                        {
                           matches = true;
                           break;
                        }
                     }
                  }
               }

               if (matches)
               {
                  scheduledDocuments.add(createScheduledDocument(documentJson, QName.valueOf(owner), reportName));
               }
            }
         }
      }
      return scheduledDocuments;
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
         return targetCalendar.after(scheduleCalendar);
      }

      Calendar startingCalendar = getCalendar(startingDate);
      return scheduleCalendar.after(startingCalendar) && targetCalendar.after(scheduleCalendar);
   }

   protected Calendar getCalendar(Date date)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      return calendar;
   }
}