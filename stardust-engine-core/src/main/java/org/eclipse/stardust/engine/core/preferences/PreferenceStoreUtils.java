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
package org.eclipse.stardust.engine.core.preferences;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;


public class PreferenceStoreUtils
{

   public static ZipOutputStream backupToZipFile(OutputStream outputStream,
         List<Preferences> preferencesList, ServiceFactory serviceFactory)
         throws Exception
   {
      // Create the ZIP file
      if (preferencesList != null && !preferencesList.isEmpty())
      {
         ZipOutputStream out = new ZipOutputStream(outputStream);

         String partitionId = serviceFactory.getAdministrationService()
               .getUser()
               .getPartitionId();

         out.setComment("Preferences backup for partition '" + partitionId
               + "', created on " + new Date());

         for (Preferences preferences : preferencesList)
         {

            XmlPreferenceWriter writer = new XmlPreferenceWriter();

            ByteArrayOutputStream osStream = new ByteArrayOutputStream();
            writer.writePreferences(osStream, preferences.getModuleId(),
                  preferences.getPreferencesId(), preferences.getPreferences());
            byte[] documentContent = osStream.toByteArray();

            // Add ZIP entry to output stream.
            String entryName = (PreferencePathBuilder.getPreferencesModuleIdPath(preferences).substring(1)).replace(
                  '/', File.separatorChar)
                  + preferences.getPreferencesId();
            ZipEntry zipEntry = new ZipEntry(entryName);
            zipEntry.setSize(documentContent.length);
            zipEntry.setTime(System.currentTimeMillis());
            zipEntry.setComment("text/xml");
            out.putNextEntry(zipEntry);

            out.write(documentContent);

            // Complete the entry
            out.closeEntry();

         }

         // Complete the ZIP file
         out.close();
      }
      return null;
   }

   public static void loadFromZipFile(InputStream inputStream,
         ServiceFactory serviceFactory) throws Exception
   {
      // open the zip file stream
      ZipInputStream stream = new ZipInputStream(inputStream);
      List<Preferences> preferencesList = CollectionUtils.newLinkedList();
      AdministrationService as = serviceFactory.getAdministrationService();

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

            // if ((relativeEntryPath.endsWith("/")))
            // {
            // this is only an empty folder, does not happen for preferences

            // relativeEntryPath = relativeEntryPath.substring(0,
            // relativeEntryPath.length() - 1);
            // createFolderIfNotExists(documentManagementService,
            // normalizedRootFolderPath + relativeEntryPath);
            // }
            // else
            // {
            // this is a file, put it as a document
            int pathEndIndex = relativeEntryPath.lastIndexOf('/');
            String folderPath;
            String documentName;
            if (pathEndIndex == -1)
            {
               // file on root path
               folderPath = null;
               documentName = relativeEntryPath;
            }
            else
            {
               // file with relative path
               folderPath = relativeEntryPath.substring(0, pathEndIndex);
               documentName = relativeEntryPath.substring(pathEndIndex + 1);
            }
            if ( !StringUtils.isEmpty(documentName))
            {
               byte[] documentContent = readEntryData(stream);

               Map preferenceMap = decodePreferencesXml(documentContent);

               PreferencePathParser eval = new PreferencePathParser(folderPath);
               PreferenceScope scope = eval.getScope();
               String moduleId = eval.getModuleId();
               String preferencesId = documentName;

               Preferences preferences = new Preferences(scope, moduleId, preferencesId,
                     preferenceMap);
               preferences.setPartitionId(as.getUser().getPartitionId());
               preferences.setRealmId(eval.getRealmId());
               preferences.setUserId(eval.getUserId());

               preferencesList.add(preferences);
            }
         }
      }
      finally
      {
         // we must always close the zip file.
         stream.close();
      }

      as.savePreferences(preferencesList);
   }

   private static Map decodePreferencesXml(byte[] documentContent)
   {
      XmlPreferenceReader reader = new XmlPreferenceReader();

      ByteArrayInputStream inputStream = new ByteArrayInputStream(documentContent);

      Map prefMap;
      try
      {
         prefMap = reader.readPreferences(inputStream);
      }
      catch (IOException e)
      {
         throw new PublicException(e);
      }
      finally
      {
         try
         {
            inputStream.close();
         }
         catch (IOException e)
         {
            throw new PublicException(e);
         }
      }
      return prefMap;
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

   public static void cleanupAllPreferencesFromDms(long loginUserOID,
         boolean keepLoginUser, ServiceFactory sf)
   {
      final User user = sf.getUserService().getUser(loginUserOID);

      DocumentManagementService dms = sf.getDocumentManagementService();

      if ( !keepLoginUser)
      {
         String partitionPath = "/" + IPreferencesPersistenceManager.PREFS_FOLDER;
         dms.removeFolder(partitionPath, true);
      }

      String partitionId = user.getPartitionId();
      String userRealmId = user.getRealm().getId();

      List<String> realmIds = DmsPersistenceManager.lookupRealmIds(dms, partitionId, "*");

      for (String realmId : realmIds)
      {

         if ( !keepLoginUser || !realmId.equals(userRealmId))
         {
            String realmfolderPath = PreferencePathBuilder.getPreferencesFolderPath(
                  PreferenceScope.REALM, realmId, null);
            dms.removeFolder(realmfolderPath, true);
         }

         List<String> usersIds = DmsPersistenceManager.lookupUserIds(dms, partitionId,
               realmId, "*");

         for (String userId : usersIds)
         {
            if ( !keepLoginUser
                  || !(userId.equals(user.getId()) && realmId.equals(userRealmId)))
            {
               String userfolderPath = PreferencePathBuilder.getPreferencesFolderPath(
                     PreferenceScope.USER, realmId, userId);
               dms.removeFolder(userfolderPath, true);
            }

         }
      }

   }
   
   public static boolean matchesWildcard(String wildcardString, String name)
   {
      if (wildcardString == null)
      {
         // null value is used if no wildcards are allowed.
         return false;
      }
      return name.matches(wildcardString.replace("*", ".*"));
   }

   public static boolean isWildcardString(String wildcardString)
   {
      if (wildcardString == null)
      {
         return false;
      }
      
      if (wildcardString.contains("*"))
      {
         return true;
      }

      return false;
   }

}
