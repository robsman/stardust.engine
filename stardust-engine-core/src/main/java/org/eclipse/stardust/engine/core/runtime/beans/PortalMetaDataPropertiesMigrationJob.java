/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.TimeMeasure;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationJobInfo;
import org.eclipse.stardust.engine.extensions.dms.data.DmsMigrationJobInfoBean;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.AnnotationUtils;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotationsImpl;
import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IQueryResult;
import org.eclipse.stardust.vfs.RepositoryOperationFailedException;

public class PortalMetaDataPropertiesMigrationJob
{
   private static final Logger trace = LogManager.getLogger(PortalMetaDataPropertiesMigrationJob.class);

   private static final String JCR_QUERY = "/jcr:root/ipp-repository/partitions//element(*, nt:file)/vfs:metaData/vfs:attributes/vfs:faxEMailMessageInfo";

   private static final String JCR_QUERY_ORDERED = JCR_QUERY
         + " order by @jcr:path ascending";

   public static final RepositoryMigrationJobInfo JOB_INFO = new DmsMigrationJobInfoBean(
   "PortalMetaDataPropertiesMigrationJob",
   "Migrates data the portal stored in the document properties map to document annotations and document description and removes them from the document properties map.",
   0, 1);

   private final IDocumentRepositoryService vfs;

   private boolean finished = false;

   public PortalMetaDataPropertiesMigrationJob(IDocumentRepositoryService vfs,
         String partitionPrefix)
   {
      this.vfs = vfs;
      // not used, migration processes all partitions
      // this.partitionPrefix = partitionPrefix;

   }

   public long doMigration(int batchSize)
   {
      long resourcesDone = 0;
      if (batchSize > 0)
      {
         final TimeMeasure timer = new TimeMeasure().start();
         long limit = batchSize;
         long offset = 0;

         IQueryResult result = null;
         try
         {
            result = vfs.findFiles(JCR_QUERY, limit, offset);
         }
         catch (RepositoryOperationFailedException e)
         {
            throw e;
         }

         List< ? extends IFile> files = result.getResult();

         int resultSize = files.size();
         long time = timer.stop().getDurationInMillis();

         trace.info((result == null ? "0" : resultSize) + " Document(s) found in " + time
               + "ms with query (" + JCR_QUERY + ") limit=" + limit + " offset=" + offset);

         for (IFile iFile : files)
         {
            if (iFile.getProperties().containsKey("faxEMailMessageInfo"))
            {
               String description = (String) iFile.getProperty("description");
               String existingDescription = iFile.getDescription();
               if (isEmpty(existingDescription))
               {
                  iFile.setDescription(description);
               }
               else if ( !isEmpty(description))
               {
                  iFile.setDescription(existingDescription + ";" + description);
               }
               iFile.setProperty("description", null);

               // RevisionComment cannot be set, only via updateFile API. Only needs to be
               // removed from metaData.
               // String versionComments = (String)iFile.getProperty("comments");
               // if (iFile instanceof JcrRepositoryFile)
               // {
               // ((JcrRepositoryFile) iFile).setRevisionComment(versionComments);
               // }
               iFile.setProperty("comments", null);

               DocumentAnnotations docAnnotations = AnnotationUtils.fromMap(iFile.getAnnotations());
               if (docAnnotations == null)
               {
                  docAnnotations = new PrintDocumentAnnotationsImpl();
               }
               if (AnnotationUtils.isPrintDocumentAnnotations(docAnnotations))
               {
                  PrintDocumentAnnotations printDocumentAnnotations = (PrintDocumentAnnotations) docAnnotations;

                  String documentTemplate = (String) iFile.getProperty("DocumentTemplate");

                  printDocumentAnnotations.setTemplateType(documentTemplate);

                  Map<String, Serializable> faxEMailMessageInfo = (Map<String, Serializable>) iFile.getProperty("faxEMailMessageInfo");

                  // is empty in most cases
                  if (faxEMailMessageInfo != null && !faxEMailMessageInfo.isEmpty())
                  {
                     printDocumentAnnotations.setAttachments((String) faxEMailMessageInfo.get("attachments"));
                     printDocumentAnnotations.setBlindCarbonCopyRecipients((String) faxEMailMessageInfo.get("blindCarbonCopyRecipients"));
                     printDocumentAnnotations.setCarbonCopyRecipients((String) faxEMailMessageInfo.get("carbonCopyRecipients"));
                     Boolean emailEnabled = (Boolean) faxEMailMessageInfo.get("mailEnabled");
                     if (emailEnabled != null)
                     {
                        printDocumentAnnotations.setEmailEnabled(emailEnabled);
                     }
                     Boolean faxEnabled = (Boolean) faxEMailMessageInfo.get("faxEnabled");
                     if (faxEnabled != null)
                     {
                        printDocumentAnnotations.setFaxEnabled(faxEnabled);
                     }
                     printDocumentAnnotations.setRecipients((String) faxEMailMessageInfo.get("recipients"));
                     printDocumentAnnotations.setSendDate((Date) faxEMailMessageInfo.get("sendDate"));
                     printDocumentAnnotations.setSender((String) faxEMailMessageInfo.get("sender"));
                     printDocumentAnnotations.setSubject((String) faxEMailMessageInfo.get("subject"));
                     printDocumentAnnotations.setFaxNumber((String) faxEMailMessageInfo.get("faxNumber"));
                  }
                  iFile.setAnnotations(AnnotationUtils.toMap(printDocumentAnnotations));

                  iFile.setProperty("DocumentTemplate", null);
                  iFile.setProperty("faxEMailMessageInfo", null);
               }

               vfs.updateFile(iFile, false, null, null, false);
               resourcesDone++ ;
            }
         } // for

         if (resultSize == 0 || (batchSize > 0 && batchSize > resourcesDone))
         {
            finished = true;
         }
      }

      return resourcesDone;
   }

   public boolean isFinished()
   {
      return finished;
   }

   public long getTotalCount()
   {
      long limit = 0;
      long offset = 0;

      IQueryResult result = null;
      try
      {
         result = vfs.findFiles(JCR_QUERY_ORDERED, limit, offset);
      }
      catch (RepositoryOperationFailedException e)
      {
         throw e;
      }

      return result.getTotalSize();
   }
}

