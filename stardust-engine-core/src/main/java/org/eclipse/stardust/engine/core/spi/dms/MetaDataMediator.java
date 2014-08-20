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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
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

public class MetaDataMediator implements ILegacyRepositoryService
{

   private IRepositoryService service;

   private RepositoryManager repositoryManager;

   public MetaDataMediator(IRepositoryService service, RepositoryManager repositoryManager)
   {
      this.service = service;
      this.repositoryManager = repositoryManager;

   }

   private void addMetaDataFromAuditTrail(String documentId, Document document)
   {
      if (document != null)
      {
         Document auditTrailDocument = RepositoryAuditTrailUtils.retrieveDocument(documentId);
         if (auditTrailDocument != null)
         {
            document.setProperties(auditTrailDocument.getProperties());
         }
      }
   }

   private void addMetaDataFromAuditTrail(String folderId, Folder folder)
   {
      Folder auditTrailFolder = RepositoryAuditTrailUtils.retrieveFolder(folderId);
      if (auditTrailFolder != null)
      {
         folder.setProperties(auditTrailFolder.getProperties());
      }
   }

   private void storeMetaDataToAuditTrail(Document document)
   {
      RepositoryAuditTrailUtils.storeDocument(document);
   }

   private void storeMetaDataToAuditTrail(Folder folder)
   {
      RepositoryAuditTrailUtils.storeFolder(folder);
   }

   private boolean isMetaDataSupported(String repositoryId)
   {
      IRepositoryInstance instance = repositoryManager.getInstance(repositoryId);
      boolean metaDataSupported = instance.getRepositoryInstanceInfo()
            .isMetaDataWriteSupported();
      return metaDataSupported;
   }

   @Override
   public Document getDocument(String documentId)
         throws DocumentManagementServiceException
   {
      Document document = service.getDocument(documentId);

      String repositoryId = RepositoryIdUtils.extractRepositoryId(documentId);
      if ( !isMetaDataSupported(repositoryId))
      {
         addMetaDataFromAuditTrail(documentId, document);
      }

      return document;
   }

   @Override
   public List< ? extends Document> getDocumentVersions(String documentId)
         throws DocumentManagementServiceException
   {
      List< ? extends Document> documentVersions = service.getDocumentVersions(documentId);

      String repositoryId = RepositoryIdUtils.extractRepositoryId(documentId);
      if ( !isMetaDataSupported(repositoryId))
      {
         for (Document document : documentVersions)
         {
            addMetaDataFromAuditTrail(document.getRevisionId(), document);
         }
      }

      return documentVersions;
   }

   @Override
   public List< ? extends Document> getDocuments(List<String> documentIds)
         throws DocumentManagementServiceException
   {
      List< ? extends Document> documents = service.getDocuments(documentIds);

      List<String> repositoryIds = RepositoryIdUtils.extractRepositoryId(documentIds);
      if (repositoryIds != null && !repositoryIds.isEmpty())
      {
         if ( !isMetaDataSupported(repositoryIds.get(0)))
         {
            for (Document document : documents)
            {
               addMetaDataFromAuditTrail(document.getId(), document);
            }
         }
      }

      return documents;
   }

   @Override
   public byte[] retrieveDocumentContent(String documentId)
         throws DocumentManagementServiceException
   {
      return service.retrieveDocumentContent(documentId);
   }

   @Override
   public void retrieveDocumentContentStream(String documentId, OutputStream target)
         throws DocumentManagementServiceException
   {
      service.retrieveDocumentContentStream(documentId, target);
   }

   @Override
   public Folder getFolder(String folderId) throws DocumentManagementServiceException
   {
      Folder folder = service.getFolder(folderId);

      String repositoryId = RepositoryIdUtils.extractRepositoryId(folderId);
      if ( !isMetaDataSupported(repositoryId))
      {
         addMetaDataFromAuditTrail(folderId, folder);
      }

      return folder;
   }

   @Override
   public Folder getFolder(String folderId, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      Folder folder = service.getFolder(folderId, levelOfDetail);

      String repositoryId = RepositoryIdUtils.extractRepositoryId(folderId);
      if ( !isMetaDataSupported(repositoryId))
      {
         addMetaDataFromAuditTrail(folderId, folder);
      }

      return folder;
   }

   @Override
   public List< ? extends Folder> getFolders(List<String> folderIds, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      List< ? extends Folder> folders = service.getFolders(folderIds, levelOfDetail);

      List<String> repositoryIds = RepositoryIdUtils.extractRepositoryId(folderIds);
      if (repositoryIds != null && !repositoryIds.isEmpty())
      {
         if ( !isMetaDataSupported(repositoryIds.get(0)))
         {
            for (Folder folder : folders)
            {
               addMetaDataFromAuditTrail(folder.getId(), folder);
            }
         }
      }

      return folders;
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document)
         throws DocumentManagementServiceException
   {
      Document createdDocument = service.createDocument(folderId, document);

      if ( !isMetaDataSupported(createdDocument.getRepositoryId()))
      {
         storeMetaDataToAuditTrail(createdDocument);
      }

      return createdDocument;
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document, byte[] content,
         String encoding) throws DocumentManagementServiceException
   {
      Document createdDocument = service.createDocument(folderId, document, content, encoding);

      if ( !isMetaDataSupported(createdDocument.getRepositoryId()))
      {
         storeMetaDataToAuditTrail(createdDocument);
      }

      return createdDocument;
   }

   @Override
   public Document versionDocument(String documentId, String versionComment,
         String versionLabel) throws DocumentManagementServiceException
   {
      Document versionedDocument = service.versionDocument(documentId, versionComment, versionLabel);

      if ( !isMetaDataSupported(versionedDocument.getRepositoryId()))
      {
         storeMetaDataToAuditTrail(versionedDocument);
      }

      return versionedDocument;
   }

   @Override
   public void removeDocumentVersion(String documentId, String documentRevisionId)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      service.removeDocumentVersion(documentId, documentRevisionId);
   }

   @Override
   public Document moveDocument(String documentId, String targetPath)
         throws DocumentManagementServiceException
   {
      return service.moveDocument(documentId, targetPath);
   }

   @Override
   public Document updateDocument(Document document, boolean createNewRevision,
         String versionComment, String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return service.updateDocument(document, createNewRevision, versionComment,
            versionLabel, keepLocked);
   }

   @Override
   public Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionComment, String versionLabel,
         boolean keepLocked) throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return service.updateDocument(document, content, encoding, createNewRevision,
            versionComment, versionLabel, keepLocked);
   }

   @Override
   public void uploadDocumentContentStream(String documentId, InputStream source,
         String contentType, String contentEncoding)
         throws DocumentManagementServiceException
   {
      service.uploadDocumentContentStream(documentId, source, contentType,
            contentEncoding);
   }

   @Override
   public void removeDocument(String documentId)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      service.removeDocument(documentId);
   }

   @Override
   public Folder createFolder(String parentFolderId, FolderInfo folder)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return service.createFolder(parentFolderId, folder);
   }

   @Override
   public Folder updateFolder(Folder folder) throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return service.updateFolder(folder);
   }

   @Override
   public void removeFolder(String folderId, boolean recursive)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      // TODO recursive leaves dead audittrail entries?
      service.removeFolder(folderId, recursive);
   }

   @Override
   public Set<Privilege> getPrivileges(String resourceId)
   {
      return service.getPrivileges(resourceId);
   }

   @Override
   public Set<AccessControlPolicy> getEffectivePolicies(String resourceId)
   {
      return service.getEffectivePolicies(resourceId);
   }

   @Override
   public Set<AccessControlPolicy> getPolicies(String resourceId)
   {
      return service.getPolicies(resourceId);
   }

   @Override
   public Set<AccessControlPolicy> getApplicablePolicies(String resourceId)
   {
      return service.getApplicablePolicies(resourceId);
   }

   @Override
   public void setPolicy(String resourceId, AccessControlPolicy policy)
   {
      service.setPolicy(resourceId, policy);
   }

   @Override
   public RepositoryMigrationReport migrateRepository(int batchSize,
         boolean evaluateTotalCount) throws DocumentManagementServiceException
   {
      return service.migrateRepository(batchSize, evaluateTotalCount);
   }

   @Override
   public byte[] getSchemaDefinition(String schemaLocation)
         throws ObjectNotFoundException
   {
      return service.getSchemaDefinition(schemaLocation);
   }

   @Override
   public Documents findDocuments(DocumentQuery query)
   {
      Documents documents = service.findDocuments(query);

      if (documents != null)
      {
         for (Document document : documents)
         {
            if ( !isMetaDataSupported(document.getRepositoryId()))
            {
               addMetaDataFromAuditTrail(document.getId(), document);
            }
         }
      }

      return documents;
   }

   @Override
   public List< ? extends Document> findDocumentsByName(String namePattern)
         throws DocumentManagementServiceException
   {
      if (service instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) service).findDocumentsByName(namePattern);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public List< ? extends Document> findDocuments(String xpathQuery)
         throws DocumentManagementServiceException
   {
      if (service instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) service).findDocuments(xpathQuery);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public List< ? extends Folder> findFoldersByName(String namePattern, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      if (service instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) service).findFoldersByName(namePattern, levelOfDetail);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public List< ? extends Folder> findFolders(String xpathQuery, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      if (service instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) service).findFolders(xpathQuery, levelOfDetail);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public Document versionDocument(String documentId, String versionLabel)
         throws DocumentManagementServiceException
   {
      if (service instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) service).versionDocument(documentId, versionLabel);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public Document updateDocument(Document document, boolean createNewRevision,
         String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      if (service instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) service).updateDocument(document, createNewRevision, versionLabel, keepLocked);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      if (service instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) service).updateDocument(document, content, encoding, createNewRevision, versionLabel, keepLocked);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

}
