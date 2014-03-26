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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderManager;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.ILegacyRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;

/**
 * @author rsauer, roland.stamm
 * @version $Revision$
 */
public class DocumentManagementServiceImpl
      implements Serializable, DocumentManagementService
{
   static final long serialVersionUID = 1L;
   
   public DocumentManagementServiceImpl()
   {
   }

   private RepositoryProviderManager getProvider()
   {
      return RepositoryProviderManager.getInstance();  
   }

   @Override
   public Document getDocument(String documentId)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().getDocument(documentId);
   }

   @Override
   public List getDocumentVersions(String documentId)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().getDocumentVersions(documentId);
   }

   @Override
   public List getDocuments(List documentIds) throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().getDocuments(documentIds);
   }

   @Override
   @Deprecated
   public List findDocumentsByName(String namePattern)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).findDocumentsByName(namePattern);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   @Deprecated
   public List findDocuments(String xpathQuery) throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).findDocuments(xpathQuery);
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
      return getProvider().getImplicitInstance().retrieveDocumentContent(documentId);
   }

   @Override
   @Deprecated
   public String requestDocumentContentDownload(String documentId)
         throws DocumentManagementServiceException
   {
      // TODO move token encode here, change DMSContentServlet to use stream API in spi.dms.IDocumentManagementService
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).requestDocumentContentDownload(documentId);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public Folder getFolder(String folderId) throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().getFolder(folderId);
   }

   @Override
   public Folder getFolder(String folderId, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().getFolder(folderId, levelOfDetail);
   }

   @Override
   public List getFolders(List folderIds, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().getFolders(folderIds, levelOfDetail);
   }

   @Override
   @Deprecated
   public List findFoldersByName(String namePattern, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).findFoldersByName(namePattern, levelOfDetail);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   @Deprecated
   public List findFolders(String xpathQuery, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).findFolders(xpathQuery, levelOfDetail);
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
      return getProvider().getImplicitInstance().createDocument(folderId, document);
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document, byte[] content,
         String encoding) throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().createDocument(folderId, document, content, encoding);
   }

   @Deprecated
   @Override
   public Document versionDocument(String documentId, String versionLabel)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).versionDocument(documentId, versionLabel);
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
      return getProvider().getImplicitInstance().versionDocument(documentId, versionComment, versionLabel);
   }

   @Override
   public void removeDocumentVersion(String documentId, String documentRevisionId)
         throws DocumentManagementServiceException
   {
      getProvider().getImplicitInstance().removeDocumentVersion(documentId, documentRevisionId);
   }

   @Override
   public Document moveDocument(String documentId, String targetPath)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().moveDocument(documentId, targetPath);
   }

   @Deprecated
   @Override
   public Document updateDocument(Document document, boolean createNewRevision,
         String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).updateDocument(document, createNewRevision, versionLabel, keepLocked);
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
      return getProvider().getImplicitInstance().updateDocument(document, createNewRevision, versionComment, versionLabel, keepLocked);
   }

   @Deprecated
   @Override
   public Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).updateDocument(document, content, encoding, createNewRevision, versionLabel, keepLocked);
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
      return getProvider().getImplicitInstance().updateDocument(document, content, encoding, createNewRevision, versionComment, versionLabel, keepLocked);
   }

   @Override
   @Deprecated
   public String requestDocumentContentUpload(String documentId)
         throws DocumentManagementServiceException
   {
      // TODO move token encode here, change DMSContentServlet to use stream API in spi.dms.IDocumentManagementService
      IRepositoryService dms = getProvider().getImplicitInstance();
      if (dms instanceof ILegacyRepositoryService)
      {
         return ((ILegacyRepositoryService) dms).requestDocumentContentUpload(documentId);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public void removeDocument(String documentId)
         throws DocumentManagementServiceException
   {
      getProvider().getImplicitInstance().removeDocument(documentId);
   }

   @Override
   public Folder createFolder(String parentFolderId, FolderInfo folder)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().createFolder(parentFolderId, folder);
   }

   @Override
   public Folder updateFolder(Folder folder) throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().updateFolder(folder);
   }

   @Override
   public void removeFolder(String folderId, boolean recursive)
         throws DocumentManagementServiceException
   {
      getProvider().getImplicitInstance().removeFolder(folderId, recursive);
   }

   @Override
   public Set<Privilege> getPrivileges(String resourceId)
   {
      return getProvider().getImplicitInstance().getPrivileges(resourceId);
   }

   @Override
   public Set<AccessControlPolicy> getEffectivePolicies(String resourceId)
   {
      return getProvider().getImplicitInstance().getEffectivePolicies(resourceId);
   }

   @Override
   public Set<AccessControlPolicy> getPolicies(String resourceId)
   {
      return getProvider().getImplicitInstance().getPolicies(resourceId);
   }
   
   @Override
   public Set<AccessControlPolicy> getApplicablePolicies(String resourceId)
   {
      return getProvider().getImplicitInstance().getApplicablePolicies(resourceId);
   }

   @Override
   public void setPolicy(String resourceId, AccessControlPolicy policy)
   {
      getProvider().getImplicitInstance().setPolicy(resourceId, policy);
   }

   @Override
   public RepositoryMigrationReport migrateRepository(int batchSize,
         boolean evaluateTotalCount) throws DocumentManagementServiceException
   {
      return getProvider().getImplicitInstance().migrateRepository(batchSize, evaluateTotalCount);
   }

   @Override
   public byte[] getSchemaDefinition(String schemaLocation)
         throws ObjectNotFoundException
   {
      return getProvider().getImplicitInstance().getSchemaDefinition(schemaLocation);
   }
   
   // ************************************************
   // *                                              *
   // *    Explicit Federated Repository Methods     *
   // *                                              *
   // ************************************************
   
   @Override
   public Documents findDocuments(DocumentQuery query)
   {
      return getProvider().getImplicitInstance().findDocuments(query);
   }

   @Override
   public List<IRepositoryProviderInfo> getRepositoryProviderInfos()
   {
      return getProvider().getAllProviderInfos();
   }

   @Override
   public List<IRepositoryInstanceInfo> getRepositoryInstanceInfos()
   {
      return getProvider().getAllInstanceInfos();
   }

   @Override
   public void bindRepository(IRepositoryConfiguration configuration)
   {
      getProvider().bindRepository(configuration);
   }

   @Override
   public void unbindRepository(String repositoryId)
   {
      getProvider().unbindRepository(repositoryId);
   }
   
   @Override
   public RepositoryMigrationReport migrateRepository(int batchSize,
         boolean evaluateTotalCount, String repositoryId) throws DocumentManagementServiceException
   {
      return getProvider().getInstance(repositoryId).migrateRepository(batchSize, evaluateTotalCount);
   }
   
   @Override
   public byte[] getSchemaDefinition(String schemaLocation, String repositoryId)
         throws ObjectNotFoundException
   {
      return getProvider().getInstance(repositoryId).getSchemaDefinition(schemaLocation);
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Document retrieval.
   // /////////////////////////////////////////////////////////////////////////////////////

   // /////////////////////////////////////////////////////////////////////////////////////
   // Document manipulation.
   // /////////////////////////////////////////////////////////////////////////////////////

   // /////////////////////////////////////////////////////////////////////////////////////
   // Folder manipulation.
   // /////////////////////////////////////////////////////////////////////////////////////

   // /////////////////////////////////////////////////////////////////////////////////////
   // Security.
   // /////////////////////////////////////////////////////////////////////////////////////

   // /////////////////////////////////////////////////////////////////////////////////////
   // Utility methods.
   // /////////////////////////////////////////////////////////////////////////////////////

}