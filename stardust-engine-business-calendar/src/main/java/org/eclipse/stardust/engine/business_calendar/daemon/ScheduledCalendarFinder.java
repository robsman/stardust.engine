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

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentManagementServiceImpl;
import org.eclipse.stardust.engine.core.runtime.scheduling.ScheduledDocumentFinder;

import com.google.gson.JsonObject;

public class ScheduledCalendarFinder extends ScheduledDocumentFinder<ScheduledCalendar>
{
   public ScheduledCalendarFinder(Date executionDate)
   {
      super(new DocumentManagementServiceImpl(), null, executionDate, ".bpmcal", "/business-calendars");
   }

   @Override
   protected ScheduledCalendar createScheduledDocument(JsonObject documentJson,
         QName owner, String documentName)
   {
      return new ScheduledCalendar(documentJson, owner, documentName);
   }

   @Override
   public List<Document> scanLocations()
   {
      List<Document> documents = new ArrayList<Document>();
      collectDocuments(documents, getFolder(getFolderName()));
      return documents;
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
            }
         }
         for (Folder subfolder : folder.getFolders())
         {
            collectDocuments(documents, getFolder(subfolder.getId()));
         }
      }
   }
}
