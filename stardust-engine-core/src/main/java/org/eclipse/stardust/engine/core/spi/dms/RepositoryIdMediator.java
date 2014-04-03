/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import static org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils.addRepositoryId;
import static org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils.extractRepositoryId;
import static org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils.stripRepositoryId;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class RepositoryIdMediator implements ILegacyRepositoryService
{
   private RepositoryProviderManager manager;

   public RepositoryIdMediator(RepositoryProviderManager manager)
   {
      this.manager = manager;
   }

   @Override
   public Document getDocument(String documentId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      Document document = instance.getService(getUser()).getDocument(stripRepositoryId(documentId));
      return addRepositoryId(document, instance.getRepositoryId());
   }

   @Override
   public List< ? extends Document> getDocumentVersions(String documentId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      List< ? extends Document> documents = instance.getService(getUser()).getDocumentVersions(stripRepositoryId(documentId));
      return addRepositoryId(documents, instance.getRepositoryId());
   }

   @Override
   public List< ? extends Document> getDocuments(List<String> documentIds)
         throws DocumentManagementServiceException
   {
      // TODO federated getDocuments
      List<String> extractedRepositoryIds = extractRepositoryId(documentIds);
      if (extractedRepositoryIds != null && !extractedRepositoryIds.isEmpty())
      {
         IRepositoryInstance instance = manager.getInstance(extractedRepositoryIds.get(0));
         List< ? extends Document> documents = instance.getService(getUser()).getDocuments(stripRepositoryId(documentIds));
         return addRepositoryId(documents, instance.getRepositoryId());
      }
      return null;
   }

   @Deprecated
   public List< ? extends Document> findDocumentsByName(String namePattern)
         throws DocumentManagementServiceException
   {
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();

      IRepositoryInstance instance = manager.getInstance(repositoryId);
      IRepositoryService service = instance.getService(getUser());
      if (service instanceof ILegacyRepositoryService)
      {
         List< ? extends Document> documents = ((ILegacyRepositoryService) service).findDocumentsByName(namePattern);
         return addRepositoryId(documents, instance.getRepositoryId());
      }
      else
      {
         throw new UnsupportedOperationException();

      }
   }

   @Deprecated
   public List< ? extends Document> findDocuments(String xpathQuery)
         throws DocumentManagementServiceException
   {
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();

      IRepositoryInstance instance = manager.getInstance(repositoryId);
      IRepositoryService service = instance.getService(getUser());
      if (service instanceof ILegacyRepositoryService)
      {
         List< ? extends Document> documents = ((ILegacyRepositoryService) service).findDocuments(xpathQuery);
         return addRepositoryId(documents, instance.getRepositoryId());
      }
      else
      {
         throw new UnsupportedOperationException();

      }
   }

   @Override
   public byte[] retrieveDocumentContent(String documentId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      return instance.getService(getUser()).retrieveDocumentContent(stripRepositoryId(documentId));
   }

   @Override
   public void retrieveDocumentContentStream(String documentId, OutputStream target)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      instance.getService(getUser()).retrieveDocumentContentStream(stripRepositoryId(documentId), target);
   }

   @Override
   public Folder getFolder(String folderId) throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      Folder folder = instance.getService(getUser()).getFolder(stripRepositoryId(folderId));
      return addRepositoryId(folder, instance.getRepositoryId());
   }

   @Override
   public Folder getFolder(String folderId, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      Folder folder = instance.getService(getUser()).getFolder(stripRepositoryId(folderId), levelOfDetail);
      return addRepositoryId(folder, instance.getRepositoryId());
   }

   @Override
   public List< ? extends Folder> getFolders(List<String> folderIds, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      // TODO federated getFolders
      List<String> extractedRepositoryIds = extractRepositoryId(folderIds);
      if (extractedRepositoryIds != null && !extractedRepositoryIds.isEmpty())
      {
         IRepositoryInstance instance = manager.getInstance(extractedRepositoryIds.get(0));
         List< ? extends Folder> folders = instance.getService(getUser()).getFolders(
               stripRepositoryId(folderIds), levelOfDetail);
         return addRepositoryId(folders, instance.getRepositoryId());
      }
      return null;
   }

   @Deprecated
   public List< ? extends Folder> findFoldersByName(String namePattern, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();

      IRepositoryInstance instance = manager.getInstance(repositoryId);
      IRepositoryService service = instance.getService(getUser());
      if (service instanceof ILegacyRepositoryService)
      {
         List< ? extends Folder> folders = ((ILegacyRepositoryService) service).findFoldersByName(
               namePattern, levelOfDetail);
         return addRepositoryId(folders, instance.getRepositoryId());
      }
      else
      {
         throw new UnsupportedOperationException();

      }
   }

   @Deprecated
   public List< ? extends Folder> findFolders(String xpathQuery, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();
      IRepositoryInstance instance = manager.getInstance(repositoryId);
      IRepositoryService service = instance.getService(getUser());
      if (service instanceof ILegacyRepositoryService)
      {
         List< ? extends Folder> folders = ((ILegacyRepositoryService) service).findFolders(
               xpathQuery, levelOfDetail);
         return addRepositoryId(folders, instance.getRepositoryId());
      }
      else
      {
         throw new UnsupportedOperationException();

      }
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      Document createdDocument = instance.getService(getUser()).createDocument(stripRepositoryId(folderId),
            document);
      return addRepositoryId(createdDocument, instance.getRepositoryId());
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document, byte[] content,
         String encoding) throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      Document createdDocument = instance.getService(getUser()).createDocument(stripRepositoryId(folderId),
            document, content, encoding);
      return addRepositoryId(createdDocument, instance.getRepositoryId());
   }

   @Deprecated
   public Document versionDocument(String documentId, String versionLabel)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      IRepositoryService service = instance.getService(getUser());
      if (service instanceof ILegacyRepositoryService)
      {
         Document versionedDocument = ((ILegacyRepositoryService) service).versionDocument(
               stripRepositoryId(documentId), versionLabel);
         return addRepositoryId(versionedDocument, instance.getRepositoryId());
      }
      else
      {
         throw new UnsupportedOperationException();

      }
   }

   @Override
   public Document versionDocument(String documentId, String versionComment,
         String versionLabel) throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      Document versionedDocument = instance.getService(getUser()).versionDocument(
            stripRepositoryId(documentId), versionComment, versionLabel);
      return addRepositoryId(versionedDocument, instance.getRepositoryId());
   }

   @Override
   public void removeDocumentVersion(String documentId, String documentRevisionId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      instance.getService(getUser()).removeDocumentVersion(stripRepositoryId(documentId),
            stripRepositoryId(documentRevisionId));
   }

   @Override
   public Document moveDocument(String documentId, String targetPath)
         throws DocumentManagementServiceException
   {
      // TODO cross repository move?
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      Document movedDocument = instance.getService(getUser()).moveDocument(stripRepositoryId(documentId),
            stripRepositoryId(targetPath));
      return addRepositoryId(movedDocument, instance.getRepositoryId());
   }

   @Deprecated
   public Document updateDocument(Document document, boolean createNewRevision,
         String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(document));
      IRepositoryService service = instance.getService(getUser());
      if (service instanceof ILegacyRepositoryService)
      {
         Document updatedDocument = ((ILegacyRepositoryService) service).updateDocument(
               stripRepositoryId(document), createNewRevision, versionLabel, keepLocked);
         return addRepositoryId(updatedDocument, instance.getRepositoryId());
      }
      else
      {
         throw new UnsupportedOperationException();

      }
   }

   @Override
   public Document updateDocument(Document document, boolean createNewRevision,
         String versionComment, String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(document));
      Document updatedDocument = instance.getService(getUser()).updateDocument(stripRepositoryId(document),
            createNewRevision, versionComment, versionLabel, keepLocked);
      return addRepositoryId(updatedDocument, instance.getRepositoryId());
   }

   @Deprecated
   public Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(document));
      IRepositoryService service = instance.getService(getUser());
      if (service instanceof ILegacyRepositoryService)
      {
         Document updatedDocument = ((ILegacyRepositoryService) service).updateDocument(
               stripRepositoryId(document), content, encoding, createNewRevision,
               versionLabel, keepLocked);
         return addRepositoryId(updatedDocument, instance.getRepositoryId());
      }
      else
      {
         throw new UnsupportedOperationException();

      }
   }

   @Override
   public Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionComment, String versionLabel,
         boolean keepLocked) throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(document));
      Document updatedDocument = instance.getService(getUser()).updateDocument(stripRepositoryId(document),
            content, encoding, createNewRevision, versionComment, versionLabel,
            keepLocked);
      return addRepositoryId(updatedDocument, instance.getRepositoryId());
   }

   @Override
   public void uploadDocumentContentStream(String documentId, InputStream source, String contentType, String contentEncoding)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      instance.getService(getUser()).uploadDocumentContentStream(stripRepositoryId(documentId), source, contentType, contentEncoding);
   }

   @Override
   public void removeDocument(String documentId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      instance.getService(getUser()).removeDocument(stripRepositoryId(documentId));
   }

   @Override
   public Folder createFolder(String parentFolderId, FolderInfo folder)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(parentFolderId));
      Folder createdFolder = instance.getService(getUser()).createFolder(stripRepositoryId(parentFolderId),
            folder);
      return addRepositoryId(createdFolder, instance.getRepositoryId());
   }

   @Override
   public Folder updateFolder(Folder folder) throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folder));
      Folder updatedFolder = instance.getService(getUser()).updateFolder(stripRepositoryId(folder));
      return addRepositoryId(updatedFolder, instance.getRepositoryId());
   }

   @Override
   public void removeFolder(String folderId, boolean recursive)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      instance.getService(getUser()).removeFolder(stripRepositoryId(folderId), recursive);
   }

   @Override
   public Set<Privilege> getPrivileges(String resourceId)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      return instance.getService(getUser()).getPrivileges(stripRepositoryId(resourceId));
   }

   @Override
   public Set<AccessControlPolicy> getEffectivePolicies(String resourceId)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      return instance.getService(getUser()).getEffectivePolicies(stripRepositoryId(resourceId));
   }

   @Override
   public Set<AccessControlPolicy> getPolicies(String resourceId)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      return instance.getService(getUser()).getPolicies(stripRepositoryId(resourceId));
   }

   @Override
   public Set<AccessControlPolicy> getApplicablePolicies(String resourceId)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      return instance.getService(getUser()).getApplicablePolicies(stripRepositoryId(resourceId));
   }

   @Override
   public void setPolicy(String resourceId, AccessControlPolicy policy)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      instance.getService(getUser()).setPolicy(stripRepositoryId(resourceId), policy);
   }

   @Override
   public RepositoryMigrationReport migrateRepository(int batchSize,
         boolean evaluateTotalCount) throws DocumentManagementServiceException
   {
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();
      
      IRepositoryInstance instance = manager.getInstance(repositoryId);
      
      return instance.getService(getUser()).migrateRepository(batchSize, evaluateTotalCount);
   }

   @Override
   public byte[] getSchemaDefinition(String schemaLocation)
         throws ObjectNotFoundException
   {
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();
      
      IRepositoryInstance instance = manager.getInstance(repositoryId);
      
      return instance.getService(getUser()).getSchemaDefinition(schemaLocation);
   }
   
   @Override
   public Documents findDocuments(DocumentQuery query)
   {
      // RepositoryPolicy repositoryPolicy = query.getPolicy(RepositoryPolicy.class);
      // if (repositoryPolicy != null)
      // {
      // TODO Federated Search
      // } else
      
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();
      
      IRepositoryInstance defaultRepositoryInstance = manager.getInstance(repositoryId);

      return addRepositoryId(defaultRepositoryInstance.getService(getUser()).findDocuments(query), repositoryId);

   }
   
   private User getUser()
   {
      return (User) DetailsFactory.create(SecurityProperties.getUser(),
            IUser.class, UserDetails.class);
   }

}
