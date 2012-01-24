/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.extensions.dms.data.AuditTrailUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderBean;

import com.sungard.infinity.bpm.vfs.IFolder;
import com.sungard.infinity.bpm.vfs.VfsUtils;



/**
 * Utility class for operating {@link DocumentManagementService}
 * 
 * @author rsauer
 * @version $Revision$
 */
public class DmsUtils
{
   private static final String PI_OID_PREFIX = "pi-";
   
   /**
    * Creates a new <code>DocumentInfo</code> object that can be filled with additional
    * information.
    * 
    * @param name
    *           the document name
    * @return the document info object
    */
   public static DocumentInfo createDocumentInfo(String name)
   {
      DmsDocumentBean dmsDocumentBean = new DmsDocumentBean();

      dmsDocumentBean.setName(name);

      return dmsDocumentBean;
   }

   /**
    * Creates a new <code>DocumentInfo</code> object that can be filled with additional
    * information. This method allows assigning the document ID for the case where no DMS
    * backend is used (the caller must ensure the uniqueness of IDs)
    * 
    * @param name
    *           the document name
    * @param id
    *           id of the document
    * @return the document info object
    */
   public static DocumentInfo createDocumentInfo(String name, String id)
   {
      DmsDocumentBean dmsDocumentBean = new DmsDocumentBean();

      dmsDocumentBean.setName(name);
      dmsDocumentBean.vfsResource().put(AuditTrailUtils.RES_ID, id);

      return dmsDocumentBean;
   }

   /**
    * Creates a new <code>FolderInfo</code> object that can be filled with additional
    * information.
    * 
    * @param name
    *           the folder name
    * @return the folder info object
    */
   public static FolderInfo createFolderInfo(String name)
   {
      DmsFolderBean dmsFolderBean = new DmsFolderBean();

      dmsFolderBean.setName(name);

      return dmsFolderBean;
   }

   public static Document getDocument(List documents, String id)
   {
      Document result = null;

      for (int i = 0; i < documents.size(); ++i)
      {
         Document doc = (Document) documents.get(i);
         if (CompareHelper.areEqual(id, doc.getId()))
         {
            result = doc;
            break;
         }
      }

      return result;
   }

   public static void removeDocument(List documents, String id)
   {
      Document doc = getDocument(documents, id);
      if (null != doc)
      {
         documents.remove(doc);
      }
   }

   public static long getProcessInstanceOID(String path)
   {
      path = path.charAt(0) == '/' ? path.substring(1) : path;
      if(path.startsWith(DocumentRepositoryFolderNames.PROCESS_ATTACHMENT_FOLDER))
      {
         String[] segments = path.split("/");
         String pid = segments[5];
         if(!pid.startsWith(PI_OID_PREFIX))
         {
            // seems that the default process attachment path is not used;
            // try to find the OID by iterating the segments
            for (int i = 0; i < segments.length && (i != 5); i++)
            {
               if(segments[i].startsWith(PI_OID_PREFIX))
               {
                  pid = segments[i];
                  break;
               }
            }
         }
         return Long.parseLong(pid.substring(PI_OID_PREFIX.length()));
      }
      return 0l;
   }   
   
   /**
    * Initialize default path based on the scope process instance OID and its start time.
    * Example: "/process-instances/2001/02/03/04/pi-567"
    * 
    * @param scopeProcessInstanceStartTime
    * @param scopeProcessInstanceOID
    * @return path starting with / and ending with a process instance specific folder name
    *         (no trailing slash).
    */
   public static String composeDefaultPath(long scopeProcessInstanceOID,
         Date scopeProcessInstanceStartTime)
   {
      StringBuffer defaultPathSb = new StringBuffer();
      Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      calendar.setTime(scopeProcessInstanceStartTime);

      defaultPathSb.append("/");
      defaultPathSb.append(DocumentRepositoryFolderNames.PROCESS_ATTACHMENT_FOLDER);
      defaultPathSb.append(calendar.get(Calendar.YEAR));
      defaultPathSb.append("/");
      int month = calendar.get(Calendar.MONTH) + 1;
      if (month < 10)
      {
         defaultPathSb.append("0");
      }
      defaultPathSb.append(month);
      defaultPathSb.append("/");

      int day = calendar.get(Calendar.DAY_OF_MONTH);
      if (day < 10)
      {
         defaultPathSb.append("0");
      }
      defaultPathSb.append(day);
      defaultPathSb.append("/");

      int hour = calendar.get(Calendar.HOUR_OF_DAY);
      if (hour < 10)
      {
         defaultPathSb.append("0");
      }
      defaultPathSb.append(hour);
      defaultPathSb.append("/");

      defaultPathSb.append(PI_OID_PREFIX);
      defaultPathSb.append(scopeProcessInstanceOID);

      return defaultPathSb.toString();
   }

   /**
    * Backup a folder to a zip stream
    * 
    * @param rootFolder
    *           folder to backup
    * @param outputStream
    *           the stream to use for ZipOutputStream
    * @param documentManagementService
    * @param ignoreEmptyFolders
    *           option to not backup empty folders
    * @param partitionId
    *           used for zip comment
    * @return the stream that contains the ZipEntries
    * @throws Exception
    */
   public static ZipOutputStream backupToZipFile(Folder rootFolder,
         OutputStream outputStream, DocumentManagementService documentManagementService,
         boolean ignoreEmptyFolders, String partitionId) throws Exception
   {
      // Create the ZIP file
      ZipOutputStream out = new ZipOutputStream(outputStream);

      out.setComment("Configuration backup for partition '" + partitionId
            + "', created on " + new Date() + ", root folder '" + rootFolder.getPath()
            + "'");

      backupFolder(rootFolder, out, documentManagementService, "", ignoreEmptyFolders);

      // Complete the ZIP file
      out.close();
      return out;
   }

   /**
    * Load a folder from an inputStream
    * 
    * @param rootFolderPath
    *           loaded data is saved to this repository path
    * @param inputStream
    *           the stream is used to create a new ZipInputStream
    * @param documentManagementService
    * @param ignoreEmptyFolders
    *           option to not create empty folders that exist in the zip stream
    * @throws Exception
    */
   public static void loadFromZipFile(String rootFolderPath, InputStream inputStream,
         DocumentManagementService documentManagementService, boolean ignoreEmptyFolders)
         throws Exception
   {
      // normalize rootFolderPathCopy to always end with '/'
      String normalizedRootFolderPath = StringUtils.isEmpty(rootFolderPath)
            ? "/"
            : rootFolderPath;
      if ( !normalizedRootFolderPath.endsWith("/"))
      {
         normalizedRootFolderPath += "/";
      }

      // open the zip file stream
      ZipInputStream stream = new ZipInputStream(inputStream);

      try
      {
         // now iterate through each item in the stream. The get next
         // entry call will return a ZipEntry for each file in the
         // stream
         ZipEntry entry;
         while ((entry = stream.getNextEntry()) != null)
         {
            // take care of Windows paths
            String relativeEntryPath = entry.getName().replace('\\', '/');

            // TODO (ab) how else we can see that this is a folder and not a file
            // (determining this
            // based on the size is dangerous because files also can be empty!
            if ((relativeEntryPath.endsWith("/") || relativeEntryPath.endsWith("\\"))
                  && !ignoreEmptyFolders)
            {
               // this is only an empty folder, create it
               relativeEntryPath = relativeEntryPath.substring(0,
                     relativeEntryPath.length() - 1);
               createFolderIfNotExists(documentManagementService,
                     normalizedRootFolderPath + relativeEntryPath);
            }
            else
            {
               // this is a file, put it as a document
               int pathEndIndex = relativeEntryPath.lastIndexOf('/');
               String folderPath;
               if (pathEndIndex == -1)
               {
                  // file on root path
                  folderPath = normalizedRootFolderPath;
               }
               else
               {
                  // file with relative path
                  folderPath = normalizedRootFolderPath
                        + relativeEntryPath.substring(0, pathEndIndex);
               }

               Folder folder = createFolderIfNotExists(documentManagementService,
                     folderPath);
               // TODO (CRNT-10654) can not use upload servlet here if content size
               // exceeds threshold
               // (carnot.properties "Carnot.Configuration.ContentStreamingThreshold")
               // since the base url of the dms-content servlet is unknown
               String documentName = (pathEndIndex == -1)
                     ? relativeEntryPath
                     : relativeEntryPath.substring(pathEndIndex + 1);
               byte[] documentContent = readEntryData(stream);
               // use default encoding, should not be a problem
               documentManagementService.createDocument(folder.getId(),
                     DmsUtils.createDocumentInfo(documentName, entry), documentContent,
                     null);
            }
         }
      }
      finally
      {
         // we must always close the zip file.
         stream.close();
      }
   }

   public static void cleanupFolder(Folder parentFolder,
         DocumentManagementService documentManagementService)
   {
      List /* <Document> */documents = parentFolder.getDocuments();
      for (int i = 0; i < documents.size(); i++ )
      {
         Document document = (Document) documents.get(i);
         documentManagementService.removeDocument(document.getId());
      }

      List folders = parentFolder.getFolders();
      for (int i = 0; i < folders.size(); i++ )
      {
         Folder folder = (Folder) folders.get(i);
         documentManagementService.removeFolder(folder.getId(), true);
      }
   }

   private static byte[] readEntryData(ZipInputStream stream) throws Exception
   {
      // create a buffer to improve performance
      byte[] buffer = new byte[2048];

      // Once we get the entry from the stream, the stream is
      // positioned read to read the raw data, and we keep
      // reading until read returns 0 or less.
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try
      {
         int len = 0;
         while ((len = stream.read(buffer)) > 0)
         {
            output.write(buffer, 0, len);
         }
         return output.toByteArray();
      }
      finally
      {
         // must always close the output file
         if (output != null)
         {
            output.close();
         }
      }
   }

   private static Folder createFolderIfNotExists(
         DocumentManagementService documentManagementService, String folderPath)
   {
      Folder folder = documentManagementService.getFolder(folderPath,
            IFolder.LOD_NO_MEMBERS);

      if (null == folder)
      {
         // folder does not exist yet, create it
         String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
         String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

         if (StringUtils.isEmpty(parentPath))
         {
            // top-level reached
            return documentManagementService.createFolder("/",
                  DmsUtils.createFolderInfo(childName));
         }
         else
         {
            Folder parentFolder = createFolderIfNotExists(documentManagementService,
                  parentPath);
            return documentManagementService.createFolder(parentFolder.getId(),
                  DmsUtils.createFolderInfo(childName));
         }
      }
      else
      {
         return folder;
      }
   }

   private static void backupFolder(Folder parentFolder, ZipOutputStream out,
         DocumentManagementService documentManagementService, String subfolder,
         boolean ignoreEmptyFolders) throws Exception
   {
      List /* <Document> */documents = parentFolder.getDocuments();
      for (int i = 0; i < documents.size(); i++ )
      {
         Document document = (Document) documents.get(i);
         // Add ZIP entry to output stream.
         String entryName = (subfolder + document.getName()).replace('/',
               File.separatorChar);
         ZipEntry zipEntry = new ZipEntry(entryName);
         zipEntry.setSize(document.getSize());
         zipEntry.setTime(document.getDateLastModified().getTime());
         zipEntry.setComment(document.getContentType());
         out.putNextEntry(zipEntry);

         // TODO (CRNT-10654) can not use download servlet here if content size exceeds
         // threshold
         // (carnot.properties "Carnot.Configuration.ContentStreamingThreshold")
         // since the base url of the dms-content servlet is unknown
         byte[] documentContent = documentManagementService.retrieveDocumentContent(document.getId());
         out.write(documentContent);

         // Complete the entry
         out.closeEntry();
      }

      List folders = parentFolder.getFolders();
      for (int i = 0; i < folders.size(); i++ )
      {
         Folder folder = (Folder) folders.get(i);
         // re-get the folder with the subfolders
         folder = documentManagementService.getFolder(folder.getId(),
               Folder.LOD_LIST_MEMBERS);
         // recurse
         backupFolder(folder, out, documentManagementService, subfolder
               + folder.getName() + File.separator, ignoreEmptyFolders);
      }

      if (documents.size() == 0 && folders.size() == 0 && !ignoreEmptyFolders)
      {
         // create an entry for an empty folder
         String entryName = subfolder.replace('/', File.separatorChar);
         out.putNextEntry(new ZipEntry(entryName));
         out.closeEntry();
      }

   }

   private static DocumentInfo createDocumentInfo(String documentName, ZipEntry entry)
   {
      DmsDocumentBean dmsDocumentBean = new DmsDocumentBean();

      dmsDocumentBean.setName(documentName);
      // dmsDocumentBean.vfsResource().put(AuditTrailUtils.RES_DATE_LAST_MODIFIED,
      // new Date(entry.getTime()));
      dmsDocumentBean.setContentType(entry.getComment());

      return dmsDocumentBean;
   }

   public static Folder ensureFolderHierarchyExists(String folderPath,
         DocumentManagementService dms)
   {
      // recursion: find/create a processinstance-specific folder (hierarchical)

      if (StringUtils.isEmpty(folderPath))
      {
         // special handling for root folder, because vfs.getFolder("", or
         // vfs.getFolder("/", returns null
         // anyway, we assume, that the root folder always exist, so there is no need to
         // check/retrieve it
         return null;
      }
      else
      {
         Folder folder = dms.getFolder(folderPath, IFolder.LOD_NO_MEMBERS);

         if (null == folder)
         {
            // folder does not exist yet, create it
            String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
            String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);
            Folder parentFolder = ensureFolderHierarchyExists(parentPath, dms);
            if (null == parentFolder)
            {
               return dms.createFolder(VfsUtils.REPOSITORY_ROOT,
                     createFolderInfo(childName));
            }
            else
            {
               return dms.createFolder(parentFolder.getId(), createFolderInfo(childName));
            }
         }
         else
         {
            return folder;
         }
      }
   }

}
