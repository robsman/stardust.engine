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
   public static final String REALMS_DIR = "/realms"; // realm id + /users/ + user id
   public static final String USER_DIR = "/users";
   public static final String DESIGNS_SUBFOLDER = "/designs";

   private Date executionDate;
   private DocumentManagementService documentManagementService;

   private String extension;
   private String folderName;

   public ScheduledDocumentFinder(DocumentManagementService dmsService, Date executionDate,
         String extension, String folderName)
   {
      documentManagementService = dmsService;
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

   protected Folder getFolder(String folderName)
   {
      return documentManagementService.getFolder(folderName);
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

            String content = new String(
                  documentManagementService.retrieveDocumentContent(document.getId()));
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

      Folder folder = documentManagementService
            .getFolder(folderName + DESIGNS_SUBFOLDER);
      if (folder != null)
      {
         documents.addAll(folder.getDocuments());
      }

      folder = documentManagementService.getFolder(REALMS_DIR);
      List<Folder> realmsFolders = folder != null ? folder.getFolders() : null;
      if (realmsFolders != null)
      {
         for (Folder realmsFolder : realmsFolders)
         {
            String realmPath = realmsFolder.getPath();
            Folder usersFolder = documentManagementService.getFolder(realmPath + USER_DIR);
            List<Folder> usersFolders = usersFolder != null
                  ? usersFolder.getFolders()
                  : null;
            if (usersFolders != null)
            {
               for (Folder userFolder : usersFolders)
               {
                  String userPath = userFolder.getPath();
                  Folder subFolder = documentManagementService.getFolder(userPath
                        + "/documents" + folderName + DESIGNS_SUBFOLDER);
                  if (subFolder != null)
                  {
                     documents.addAll(subFolder.getDocuments());
                  }
               }
            }
         }
      }

      folder = documentManagementService.getFolder(folderName);
      //String saveFolderPath = folderName + SchedulingConstants.SAVE_SUBFOLDER;
      Set<String> excludedPaths = getExcludedPaths();
      String designsFolderPath = folderName + DESIGNS_SUBFOLDER;

      List<Folder> participantFolders = folder != null ? folder.getFolders() : null;
      if (participantFolders != null)
      {
         for (Folder participantFolder : participantFolders)
         {
            String participantPath = participantFolder.getPath();
            if (!excludedPaths.contains(participantPath)
                  && !designsFolderPath.equals(participantPath))
            {
               Folder subFolder = documentManagementService.getFolder(participantPath + DESIGNS_SUBFOLDER);
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

   private boolean executionTimeMatches(Date processSchedule)
   {
      Calendar current = Calendar.getInstance();
      current.setTime(executionDate);

      Calendar scheduled = Calendar.getInstance();
      scheduled.setTime(processSchedule);

      // minute precision
      return current.get(Calendar.YEAR)        == scheduled.get(Calendar.YEAR)
          && current.get(Calendar.DAY_OF_YEAR) == scheduled.get(Calendar.DAY_OF_YEAR)
          && current.get(Calendar.HOUR_OF_DAY) == scheduled.get(Calendar.HOUR_OF_DAY)
          && current.get(Calendar.MINUTE)      == scheduled.get(Calendar.MINUTE);
   }
}