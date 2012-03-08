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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.EmbeddedServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;



public class DmsPersistenceManager implements IPreferencesPersistenceManager
{
   private ServiceFactory serviceFactory;

   public DmsPersistenceManager()
   {

   }
   
   @Override
   public Preferences loadPreferences(IUser user, PreferenceScope scope, String moduleId,
         String preferencesId, IPreferencesReader loader)
   {
      String realmId = null;
      String userId = null;
      if (user == null
            && (PreferenceScope.USER.equals(scope) || PreferenceScope.REALM.equals(scope)))
      {
         throw new PublicException(
               "No user specified. PreferenceScope USER and REALM not available.");
      }
      else
      {
         if (user != null)
         {
            realmId = user.getRealm().getId();
            userId = user.getId();
         }
      }

      String fileId = getPreferencesFolderPath(scope, realmId, userId);

      fileId += moduleId + "/" + preferencesId;

      byte[] fileContent = getServiceFactory().getDocumentManagementService()
            .retrieveDocumentContent(fileId);

      Map loadedPreferences = readPreferences(fileContent, loader);

      Preferences preferences = new Preferences(scope, moduleId, preferencesId,
            loadedPreferences);
      setPreferencesOrigin(preferences);
      return preferences;
   }

   public Preferences loadPreferences(PreferenceScope scope, //
         String moduleId, String preferencesId, //
         IPreferencesReader loader)
   {
      final IUser currentUser = SecurityProperties.getUser();
      return loadPreferences(currentUser, scope, moduleId, preferencesId, loader);
      
   }

   private ServiceFactory getServiceFactory()
   {
      if (serviceFactory == null)
      {
         serviceFactory = EmbeddedServiceFactory.CURRENT_TX();
      }

      return serviceFactory;
   }

   private void setPreferencesOrigin(Preferences preferences)
   {

      String partitionId = SecurityProperties.getPartition().getId();
      IUser user = SecurityProperties.getUser();
      if (PreferenceScope.PARTITION.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
      }
      else if (PreferenceScope.REALM.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
         if (user != null)
         {
            preferences.setRealmId(user.getRealm().getId());
         }
      }
      else if (PreferenceScope.USER.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
         if (user != null)
         {
            preferences.setRealmId(user.getRealm().getId());
            preferences.setUserId(user.getId());
         }
      }
   }

   public void updatePreferences(Preferences preferences, IPreferencesWriter writer)
   {
      throw new PublicException(
            BpmRuntimeError.DMS_SECURITY_ERROR_DMS_READONLY_FOR_PREFERENCES.raise());
      // String fileId;
      //
      // final IUser currentUser = SecurityProperties.getUser();
      //
      // String realmId = null;
      // String userId = null;
      // if (currentUser == null
      // && (PreferenceScope.USER.equals(preferences.getScope()) ||
      // PreferenceScope.REALM.equals(preferences.getScope())))
      // {
      // throw new PublicException(
      // "No current user was found. PreferenceScope USER and REALM not available.");
      // }
      // else
      // {
      // realmId = currentUser.getRealm().getId();
      // userId = currentUser.getId();
      // }
      //
      // if (preferences.getRealmId() != null &&
      // !preferences.getRealmId().equals(realmId))
      // {
      // realmId = preferences.getRealmId();
      // }
      // if (preferences.getUserId() != null && !preferences.getUserId().equals(userId))
      // {
      // userId = preferences.getUserId();
      // }
      //
      // fileId = getPreferencesFolderPath(preferences.getScope(), realmId, userId);
      //
      // fileId += preferences.getModuleId() + "/" + preferences.getPreferencesId();
      //
      // Document prefsFile =
      // getServiceFactory().getDocumentManagementService().getDocument(
      // fileId);
      //
      // ByteArrayOutputStream baosPrefsContent = new ByteArrayOutputStream();
      // try
      // {
      // writer.writePreferences(baosPrefsContent, preferences.getModuleId(),
      // preferences.getPreferencesId(), preferences.getPreferences());
      // }
      // catch (IOException ioe)
      // {
      // // TODO
      // return;
      // }
      // finally
      // {
      // try
      // {
      // baosPrefsContent.close();
      // }
      // catch (IOException ioe)
      // {
      // // ignore
      // }
      // }
      //
      // if (null != prefsFile)
      // {
      // getServiceFactory().getDocumentManagementService().updateDocument(prefsFile,
      // baosPrefsContent.toByteArray(), null, false, null, false);
      // }
      // else
      // {
      // DocumentInfo prefsFileInfo =
      // DmsUtils.createDocumentInfo(preferences.getPreferencesId());
      // prefsFileInfo.setContentType("text/xml");
      //
      // String folderId = fileId.substring(0, fileId.lastIndexOf("/"));
      //
      // Folder prefsFolder = DocumentRepositoryUtils.getSubFolder(
      // getServiceFactory().getDocumentManagementService(), folderId);
      //
      // getServiceFactory().getDocumentManagementService().createDocument(
      // prefsFolder.getId(), prefsFileInfo, baosPrefsContent.toByteArray(), null);
      // }
   }

   public List<Preferences> getAllPreferences(ParsedPreferenceQuery evaluatedQuery,
         IPreferencesReader reader)
   {
      List<Preferences> preferences = CollectionUtils.newArrayList();

      String partitionId = SecurityProperties.getPartition().getId();

      PreferenceScope scope = evaluatedQuery.getScope() == null
            ? PreferenceScope.PARTITION
            : evaluatedQuery.getScope();
      String moduleIdWildcard = evaluatedQuery.getModuleId();
      String preferencesIdWildcard = evaluatedQuery.getPreferencesId();
      String realmIdWildcard = evaluatedQuery.getRealmId();
      String userIdWildcard = evaluatedQuery.getUserId();

      DocumentManagementService dms = getServiceFactory().getDocumentManagementService();
      if (PreferenceScope.USER.equals(scope))
      {
         List<String> realmIds = lookupRealmIds(dms, partitionId, realmIdWildcard);

         for (String realmId : realmIds)
         {
            List<String> usersIds = lookupUserIds(dms, partitionId, realmId,
                  userIdWildcard);

            for (String userId : usersIds)
            {
               String folderPath = getPreferencesFolderPath(scope, realmId, userId);

               fetchPreferences(folderPath, scope, moduleIdWildcard,
                     preferencesIdWildcard, reader, preferences, partitionId, realmId,
                     userId);
            }
         }

      }
      else if (PreferenceScope.REALM.equals(scope))
      {
         List<String> realmIds = lookupRealmIds(dms, partitionId, realmIdWildcard);

         for (String realmId : realmIds)
         {
            String folderPath = getPreferencesFolderPath(scope, realmId, null);

            fetchPreferences(folderPath, scope, moduleIdWildcard, preferencesIdWildcard,
                  reader, preferences, partitionId, realmId, null);
         }

      }
      else if (PreferenceScope.PARTITION.equals(scope))
      {
         String folderPath = getPreferencesFolderPath(scope, null, null);

         fetchPreferences(folderPath, scope, moduleIdWildcard, preferencesIdWildcard,
               reader, preferences, partitionId, null, null);
      }
      else
      {
         // DEFAULT scope not available here
      }

      return preferences;

   }

   public static List<String> lookupRealmIds(DocumentManagementService dms,
         String partitionId, String realmIdWildcard)
   {
      List<String> realmIds = CollectionUtils.newLinkedList();

      String xpathQuery = "/jcr:root"
            + DocumentRepositoryFolderNames.getRepositoryRootFolder() //
            + IPreferencesPersistenceManager.PARTITIONS_FOLDER + partitionId + "/" //
            + IPreferencesPersistenceManager.REALMS_FOLDER + "element(*, nt:folder)";

      List<Folder> folders = dms.findFolders(xpathQuery, Folder.LOD_NO_MEMBERS);

      for (Folder folder : folders)
      {
         if (PreferenceStoreUtils.matchesWildcard(realmIdWildcard, folder.getName()))
         {
            realmIds.add(folder.getName());
         }
      }

      return realmIds;
   }

   public static List<String> lookupUserIds(DocumentManagementService dms,
         String partitionId, String realmId, String userIdWildcard)
   {
      List<String> userIds = CollectionUtils.newLinkedList();

      String xpathQuery = "/jcr:root"
            + DocumentRepositoryFolderNames.getRepositoryRootFolder() //
            + IPreferencesPersistenceManager.PARTITIONS_FOLDER + partitionId + "/" //
            + IPreferencesPersistenceManager.REALMS_FOLDER + realmId + "/" //
            + IPreferencesPersistenceManager.USERS_FOLDER + "element(*, nt:folder)";

      List<Folder> folders = dms.findFolders(xpathQuery, Folder.LOD_NO_MEMBERS);

      for (Folder folder : folders)
      {
         if (PreferenceStoreUtils.matchesWildcard(userIdWildcard, folder.getName()))
         {
            userIds.add(folder.getName());
         }
      }

      return userIds;
   }

   // private List<IUserRealm> evaluateRealms(String realmIdWildcard, short partitionOid)
   // {
   // List<IUserRealm> realmIds = CollectionUtils.newLinkedList();
   //
   // Iterator iter = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
   // UserRealmBean.class, QueryExtension.where(//
   // Predicates.andTerm(//
   // Predicates.isEqual(UserRealmBean.FR__PARTITION, partitionOid),//
   // Predicates.isLike(UserRealmBean.FR__ID, realmIdWildcard))));
   //
   // if (iter != null)
   // {
   // while (iter.hasNext())
   // {
   // realmIds.add((IUserRealm) iter.next());
   // }
   // }
   //
   // return realmIds;
   // }

   private void fetchPreferences(String preferencesFolderPath, PreferenceScope scope,
         String moduleIdWildcard, String preferencesIdWildcard,
         IPreferencesReader reader, List<Preferences> preferencesList,
         String partitionId, String realmId, String userId)
   {
      List<Folder> includeModuleFolders = new LinkedList<Folder>();
      // TODO handle moduleIdWildcard
      if (PreferenceStoreUtils.isWildcardString(moduleIdWildcard))
      {
         final Folder preferencesFolder = getServiceFactory().getDocumentManagementService()
               .getFolder(preferencesFolderPath, Folder.LOD_LIST_MEMBERS_OF_MEMBERS);

         if (preferencesFolder != null)
         {
            List<Folder> moduleFolders = preferencesFolder.getFolders();
            for (Folder moduleFolder : moduleFolders)
            {
               if (PreferenceStoreUtils.matchesWildcard(moduleIdWildcard,
                     moduleFolder.getName()))
               {
                  includeModuleFolders.add(moduleFolder);
               }
            }
         }
      }
      else
      {
         final Folder folder = getServiceFactory().getDocumentManagementService()
               .getFolder(preferencesFolderPath += moduleIdWildcard + "/");
         if (folder != null)
         {
            includeModuleFolders.add(folder);
         }
      }

      for (Folder folder : includeModuleFolders)
      {
         List<Document> docs = folder.getDocuments();
         for (Document doc : docs)
         {
            if (PreferenceStoreUtils.matchesWildcard(preferencesIdWildcard, doc.getName()))
            {
               byte[] fileContent = getServiceFactory().getDocumentManagementService()
                     .retrieveDocumentContent(doc.getId());

               Map loadedPreferences = readPreferences(fileContent, reader);

               Preferences preferences = new Preferences(scope, folder.getName(),
                     doc.getName(), loadedPreferences);
               preferences.setPartitionId(partitionId);
               preferences.setRealmId(realmId);
               preferences.setUserId(userId);
               preferencesList.add(preferences);
            }
         }
      }
   }

   private Map readPreferences(byte[] fileContent, IPreferencesReader loader)
   {
      Map loadedPreferences = null;
      if ((null != fileContent) && (0 < fileContent.length))
      {
         InputStream isFileContent = new ByteArrayInputStream(fileContent);
         try
         {
            loadedPreferences = loader.readPreferences(isFileContent);
         }
         catch (IOException ioe)
         {
            // TODO
         }
         finally
         {
            try
            {
               isFileContent.close();
            }
            catch (IOException ioe)
            {
               // ignore
            }
         }
      }

      if (loadedPreferences == null)
      {
         loadedPreferences = CollectionUtils.newHashMap();
      }

      return loadedPreferences;
   }

   private String getPreferencesFolderPath(PreferenceScope scope, String realmId,
         String userId)
   {
      return PreferencePathBuilder.getPreferencesFolderPath(scope, realmId, userId);
   }
}
