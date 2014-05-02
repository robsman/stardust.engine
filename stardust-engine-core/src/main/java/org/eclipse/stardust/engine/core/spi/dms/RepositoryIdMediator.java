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
import static org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderUtils.getUserContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.RepositoryPolicy;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;

public class RepositoryIdMediator implements ILegacyRepositoryService
{
   private RepositoryProviderManager manager;
   private FederatedSearchHandler federatedSearchHandler;
   private ResourceSyncHandler syncHandler;

   public RepositoryIdMediator(RepositoryProviderManager manager)
   {
      this.manager = manager;
      this.federatedSearchHandler = new FederatedSearchHandler(manager);
      this.syncHandler = new ResourceSyncHandler();
   }

   @Override
   public Document getDocument(String documentId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      Document document = instance.getService(getUserContext()).getDocument(stripRepositoryId(documentId));
      return addRepositoryId(document, instance.getRepositoryId());
   }

   @Override
   public List< ? extends Document> getDocumentVersions(String documentId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      List< ? extends Document> documents = instance.getService(getUserContext()).getDocumentVersions(stripRepositoryId(documentId));
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
         List< ? extends Document> documents = instance.getService(getUserContext()).getDocuments(stripRepositoryId(documentIds));
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
      IRepositoryService service = instance.getService(getUserContext());
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
      IRepositoryService service = instance.getService(getUserContext());
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
      return instance.getService(getUserContext()).retrieveDocumentContent(stripRepositoryId(documentId));
   }

   @Override
   public void retrieveDocumentContentStream(String documentId, OutputStream target)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      instance.getService(getUserContext()).retrieveDocumentContentStream(stripRepositoryId(documentId), target);
   }

   @Override
   public Folder getFolder(String folderId) throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      Folder folder = instance.getService(getUserContext()).getFolder(stripRepositoryId(folderId));
      return addRepositoryId(folder, instance.getRepositoryId());
   }

   @Override
   public Folder getFolder(String folderId, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      Folder folder = instance.getService(getUserContext()).getFolder(stripRepositoryId(folderId), levelOfDetail);
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
         List< ? extends Folder> folders = instance.getService(getUserContext()).getFolders(
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
      IRepositoryService service = instance.getService(getUserContext());
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
      IRepositoryService service = instance.getService(getUserContext());
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
      Document createdDocument = instance.getService(getUserContext()).createDocument(stripRepositoryId(folderId),
            document);
      return addRepositoryId(createdDocument, instance.getRepositoryId());
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document, byte[] content,
         String encoding) throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      Document createdDocument = instance.getService(getUserContext()).createDocument(stripRepositoryId(folderId),
            document, content, encoding);
      return addRepositoryId(createdDocument, instance.getRepositoryId());
   }

   @Deprecated
   public Document versionDocument(String documentId, String versionLabel)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      IRepositoryService service = instance.getService(getUserContext());
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
      Document versionedDocument = instance.getService(getUserContext()).versionDocument(
            stripRepositoryId(documentId), versionComment, versionLabel);
      return addRepositoryId(versionedDocument, instance.getRepositoryId());
   }

   @Override
   public void removeDocumentVersion(String documentId, String documentRevisionId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      instance.getService(getUserContext()).removeDocumentVersion(stripRepositoryId(documentId),
            stripRepositoryId(documentRevisionId));
   }

   @Override
   public Document moveDocument(String documentId, String targetPath)
         throws DocumentManagementServiceException
   {
      String sourceRepositoryId = extractRepositoryId(documentId);
      String targetRepositoryId = extractRepositoryId(targetPath);
      IRepositoryInstance sourceInstance = manager.getInstance(sourceRepositoryId);
      final IRepositoryService sourceService = sourceInstance.getService(getUserContext());
      final String strippedSourceDocumentId = stripRepositoryId(documentId);
      String strippedTargetPath = stripRepositoryId(targetPath);
      if (sourceRepositoryId != null && sourceRepositoryId.equals(targetRepositoryId)
            || sourceRepositoryId == null && targetRepositoryId == null)
      {
         // Same repository move.
         Document movedDocument = sourceService.moveDocument(strippedSourceDocumentId,
               strippedTargetPath);
         return addRepositoryId(movedDocument, sourceInstance.getRepositoryId());
      }
      else
      {
         // Cross repository move.
         IRepositoryInstance targetInstance = manager.getInstance(targetRepositoryId);
         IRepositoryService targetService = targetInstance.getService(getUserContext());

         // Copy head revision meta data and content.
         Document sourceDocument = sourceService.getDocument(strippedSourceDocumentId);
         byte[] content = sourceService.retrieveDocumentContent(strippedSourceDocumentId);
         Document targetDocument = targetService.createDocument(strippedTargetPath,
               sourceDocument, content, sourceDocument.getEncoding());

         // Verify content size.
         Document movedDocument = targetService.getDocument(targetDocument.getId());
         if (targetDocument.getSize() == movedDocument.getSize())
         {
            // Delete source document if file size matches.
            sourceService.removeDocument(strippedSourceDocumentId);
         }
         else
         {
            throw new DocumentManagementServiceException(
                  BpmRuntimeError.DMS_FAILED_UPDATING_CONTENT_FOR_DOCUMENT.raise(documentId));
         }

         return addRepositoryId(movedDocument, targetRepositoryId);
      }
   }

   @Deprecated
   public Document updateDocument(Document document, boolean createNewRevision,
         String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(document));
      IRepositoryService service = instance.getService(getUserContext());
      if (service instanceof ILegacyRepositoryService)
      {
         Document oldDocument = this.getDocument(document.getId());
         try
         {
            Document updatedDocument = ((ILegacyRepositoryService) service).updateDocument(
                  stripRepositoryId(document), createNewRevision, versionLabel,
                  keepLocked);

            Document prefixedUpdatedDocument = addRepositoryId(updatedDocument,
                  instance.getRepositoryId());
            syncHandler.notifyDocumentUpdated(oldDocument, prefixedUpdatedDocument);
            return prefixedUpdatedDocument;
         }
         finally
         {
            // restore document state
            addRepositoryId(document, instance.getRepositoryId());
         }
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
      Document oldDocument = this.getDocument(document.getId());
      try
      {
         Document updatedDocument = instance.getService(getUserContext()).updateDocument(
               stripRepositoryId(document), createNewRevision, versionComment,
               versionLabel, keepLocked);

         Document prefixedUpdatedDocument = addRepositoryId(updatedDocument,
               instance.getRepositoryId());
         syncHandler.notifyDocumentUpdated(oldDocument, prefixedUpdatedDocument);
         return prefixedUpdatedDocument;
      }
      finally
      {
         // restore document state
         addRepositoryId(document, instance.getRepositoryId());
      }
   }

   @Deprecated
   public Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(document));
      IRepositoryService service = instance.getService(getUserContext());
      if (service instanceof ILegacyRepositoryService)
      {
         Document oldDocument = this.getDocument(document.getId());
         try
         {
            Document updatedDocument = ((ILegacyRepositoryService) service).updateDocument(
                  stripRepositoryId(document), content, encoding, createNewRevision,
                  versionLabel, keepLocked);

            Document prefixedUpdatedDocument = addRepositoryId(updatedDocument,
                  instance.getRepositoryId());
            syncHandler.notifyDocumentUpdated(oldDocument, prefixedUpdatedDocument);
            return prefixedUpdatedDocument;
         }
         finally
         {
            // restore document state
            addRepositoryId(document, instance.getRepositoryId());
         }
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
      Document oldDocument = this.getDocument(document.getId());
      try
      {
         Document updatedDocument = instance.getService(getUserContext()).updateDocument(
               stripRepositoryId(document), content, encoding, createNewRevision,
               versionComment, versionLabel, keepLocked);

         Document prefixedUpdatedDocument = addRepositoryId(updatedDocument,
               instance.getRepositoryId());
         syncHandler.notifyDocumentUpdated(oldDocument, prefixedUpdatedDocument);
         return prefixedUpdatedDocument;
      }
      finally
      {
         // restore document state
         addRepositoryId(document, instance.getRepositoryId());
      }
   }

   @Override
   public void uploadDocumentContentStream(String documentId, InputStream source, String contentType, String contentEncoding)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      instance.getService(getUserContext()).uploadDocumentContentStream(stripRepositoryId(documentId), source, contentType, contentEncoding);
   }

   @Override
   public void removeDocument(String documentId)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(documentId));
      Document oldDocument = this.getDocument(documentId);
      instance.getService(getUserContext()).removeDocument(stripRepositoryId(documentId));
      syncHandler.notifyDocumentUpdated(oldDocument, null);
   }

   @Override
   public Folder createFolder(String parentFolderId, FolderInfo folder)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(parentFolderId));
      Folder createdFolder = instance.getService(getUserContext()).createFolder(stripRepositoryId(parentFolderId),
            folder);
      return addRepositoryId(createdFolder, instance.getRepositoryId());
   }

   @Override
   public Folder updateFolder(Folder folder) throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folder));
      Folder oldFolder = this.getFolder(folder.getId());
      try
      {
         Folder updatedFolder = instance.getService(getUserContext()).updateFolder(
               stripRepositoryId(folder));
         Folder prefixedUpdatedFolder = addRepositoryId(updatedFolder,
               instance.getRepositoryId());
         syncHandler.notifyFolderUpdated(oldFolder, prefixedUpdatedFolder);
         return prefixedUpdatedFolder;
      }
      finally
      {
         // restore folder state
         addRepositoryId(folder, instance.getRepositoryId());
      }
   }

   @Override
   public void removeFolder(String folderId, boolean recursive)
         throws DocumentManagementServiceException
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(folderId));
      Folder oldFolder = this.getFolder(folderId);
      instance.getService(getUserContext()).removeFolder(stripRepositoryId(folderId), recursive);
      syncHandler.notifyFolderUpdated(oldFolder, null);
   }

   @Override
   public Set<Privilege> getPrivileges(String resourceId)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      return instance.getService(getUserContext()).getPrivileges(stripRepositoryId(resourceId));
   }

   @Override
   public Set<AccessControlPolicy> getEffectivePolicies(String resourceId)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      return instance.getService(getUserContext()).getEffectivePolicies(stripRepositoryId(resourceId));
   }

   @Override
   public Set<AccessControlPolicy> getPolicies(String resourceId)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      return instance.getService(getUserContext()).getPolicies(stripRepositoryId(resourceId));
   }

   @Override
   public Set<AccessControlPolicy> getApplicablePolicies(String resourceId)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      return instance.getService(getUserContext()).getApplicablePolicies(stripRepositoryId(resourceId));
   }

   @Override
   public void setPolicy(String resourceId, AccessControlPolicy policy)
   {
      IRepositoryInstance instance = manager.getInstance(extractRepositoryId(resourceId));
      instance.getService(getUserContext()).setPolicy(stripRepositoryId(resourceId), policy);
   }

   @Override
   public RepositoryMigrationReport migrateRepository(int batchSize,
         boolean evaluateTotalCount) throws DocumentManagementServiceException
   {
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();

      IRepositoryInstance instance = manager.getInstance(repositoryId);

      return instance.getService(getUserContext()).migrateRepository(batchSize, evaluateTotalCount);
   }

   @Override
   public byte[] getSchemaDefinition(String schemaLocation)
         throws ObjectNotFoundException
   {
      // fallback to default
      String repositoryId = RepositoryProviderManager.getInstance()
            .getDefaultRepository();

      IRepositoryInstance instance = manager.getInstance(repositoryId);

      return instance.getService(getUserContext()).getSchemaDefinition(schemaLocation);
   }

   @Override
   public Documents findDocuments(DocumentQuery query)
   {
      RepositoryPolicy repositoryPolicy = (RepositoryPolicy) query.getPolicy(RepositoryPolicy.class);
      if (repositoryPolicy != null)
      {
         return federatedSearchHandler.findDocuments(query);
      }
      else
      {
         // fallback to default
         String repositoryId = RepositoryProviderManager.getInstance()
               .getDefaultRepository();

         IRepositoryInstance defaultRepositoryInstance = manager.getInstance(repositoryId);

         return addRepositoryId(defaultRepositoryInstance.getService(getUserContext())
               .findDocuments(query), repositoryId);
      }
   }

}
