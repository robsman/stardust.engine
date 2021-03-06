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

import static org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderUtils.checkWriteInArchiveMode;
import static org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderUtils.getUserContext;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.dms.ILegacyRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryManager;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

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

   private RepositoryManager getProvider()
   {
      return RepositoryManager.getInstance();
   }

   private void checkAdministratorRole()
   {
      PropertyLayer layer = ParametersFacade.pushLayer(Collections.singletonMap(
            UserDetailsLevel.PRP_USER_DETAILS_LEVEL, UserDetailsLevel.Core));
      try
      {
         User userDetails = (User) DetailsFactory.create(SecurityProperties.getUser(),
               IUser.class, UserDetails.class);
         if (!userDetails.isAdministrator())
         {
            throw new DocumentManagementServiceException(
                  BpmRuntimeError.DMS_SECURITY_ERROR_ADMIN_REQUIRED.raise());
         }
      }
      finally
      {
         if (layer != null)
         {
            ParametersFacade.popLayer();
            layer = null;
         }
      }
   }

   @Override
   public Document getDocument(String documentId)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitService().getDocument(documentId);
   }

   @Override
   public List getDocumentVersions(String documentId)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitService().getDocumentVersions(documentId);
   }

   @Override
   public List getDocuments(List documentIds) throws DocumentManagementServiceException
   {
      return getProvider().getImplicitService().getDocuments(documentIds);
   }

   @Override
   @Deprecated
   public List findDocumentsByName(String namePattern)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitService();
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
      IRepositoryService dms = getProvider().getImplicitService();
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
      return getProvider().getImplicitService().retrieveDocumentContent(documentId);
   }

   @Override
   public String requestDocumentContentDownload(String documentId)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitService();
      Document document = dms.getDocument(documentId);

      if (null != document)
      {
         return DmsContentServlet.encodeDmsServletToken(documentId,
               DmsContentServlet.OP_DOWNLOAD, SecurityProperties.getUserOID(),
               TimestampProviderUtils.getTimeStampValue());
      }
      else
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(documentId));
      }
   }

   @Override
   public Folder getFolder(String folderId) throws DocumentManagementServiceException
   {
      return getProvider().getImplicitService().getFolder(folderId);
   }

   @Override
   public Folder getFolder(String folderId, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitService().getFolder(folderId, levelOfDetail);
   }

   @Override
   public List getFolders(List folderIds, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      return getProvider().getImplicitService().getFolders(folderIds, levelOfDetail);
   }

   @Override
   @Deprecated
   public List findFoldersByName(String namePattern, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      IRepositoryService dms = getProvider().getImplicitService();
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
      IRepositoryService dms = getProvider().getImplicitService();
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
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().createDocument(folderId, document);
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document, byte[] content,
         String encoding) throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().createDocument(folderId, document, content, encoding);
   }

   @Deprecated
   @Override
   public Document versionDocument(String documentId, String versionLabel)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      IRepositoryService dms = getProvider().getImplicitService();
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
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().versionDocument(documentId, versionComment, versionLabel);
   }

   @Override
   public void removeDocumentVersion(String documentId, String documentRevisionId)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      getProvider().getImplicitService().removeDocumentVersion(documentId, documentRevisionId);
   }

   @Override
   public Document moveDocument(String documentId, String targetPath)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().moveDocument(documentId, targetPath);
   }

   @Deprecated
   @Override
   public Document updateDocument(Document document, boolean createNewRevision,
         String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      IRepositoryService dms = getProvider().getImplicitService();
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
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().updateDocument(document, createNewRevision, versionComment, versionLabel, keepLocked);
   }

   @Deprecated
   @Override
   public Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      IRepositoryService dms = getProvider().getImplicitService();
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
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().updateDocument(document, content, encoding, createNewRevision, versionComment, versionLabel, keepLocked);
   }

   @Override
   public String requestDocumentContentUpload(String documentId)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      IRepositoryService dms = getProvider().getImplicitService();
      Document document = dms.getDocument(documentId);

      if (null != document)
      {
         return DmsContentServlet.encodeDmsServletToken(documentId,
               DmsContentServlet.OP_UPLOAD, SecurityProperties.getUserOID(),
               TimestampProviderUtils.getTimeStampValue());
      }
      else
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(documentId));
      }
   }

   @Override
   public void removeDocument(String documentId)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      getProvider().getImplicitService().removeDocument(documentId);
   }

   @Override
   public Folder createFolder(String parentFolderId, FolderInfo folder)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().createFolder(parentFolderId, folder);
   }

   @Override
   public Folder updateFolder(Folder folder) throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().updateFolder(folder);
   }

   @Override
   public void removeFolder(String folderId, boolean recursive)
         throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      getProvider().getImplicitService().removeFolder(folderId, recursive);
   }

   @Override
   public Set<Privilege> getPrivileges(String resourceId)
   {
      return getProvider().getImplicitService().getPrivileges(resourceId);
   }

   @Override
   public Set<AccessControlPolicy> getEffectivePolicies(String resourceId)
   {
      return getProvider().getImplicitService().getEffectivePolicies(resourceId);
   }

   @Override
   public Set<AccessControlPolicy> getPolicies(String resourceId)
   {
      return getProvider().getImplicitService().getPolicies(resourceId);
   }

   @Override
   public Set<AccessControlPolicy> getApplicablePolicies(String resourceId)
   {
      return getProvider().getImplicitService().getApplicablePolicies(resourceId);
   }

   @Override
   public void setPolicy(String resourceId, AccessControlPolicy policy)
   {
      checkWriteInArchiveMode();
      getProvider().getImplicitService().setPolicy(resourceId, policy);
   }

   @Override
   public RepositoryMigrationReport migrateRepository(int batchSize,
         boolean evaluateTotalCount) throws DocumentManagementServiceException
   {
      checkWriteInArchiveMode();
      return getProvider().getImplicitService().migrateRepository(batchSize, evaluateTotalCount);
   }

   @Override
   public byte[] getSchemaDefinition(String schemaLocation)
         throws ObjectNotFoundException
   {
      return getProvider().getExplicitInstance(RepositoryManager.SYSTEM_REPOSITORY_ID)
            .getService(getUserContext())
            .getSchemaDefinition(schemaLocation);
   }

   // ************************************************
   // *                                              *
   // *    Explicit Federated Repository Methods     *
   // *                                              *
   // ************************************************

   @Override
   public Documents findDocuments(DocumentQuery query)
   {
      return getProvider().getImplicitService().findDocuments(query);
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
      checkAdministratorRole();
      getProvider().bindRepository(configuration);
   }

   @Override
   public void unbindRepository(String repositoryId)
   {
      checkAdministratorRole();
      getProvider().unbindRepository(repositoryId);
   }

   @Override
   public String getDefaultRepository()
   {
      return getProvider().getDefaultRepository();
   }

   @Override
   public void setDefaultRepository(String repositoryId)
   {
      checkAdministratorRole();
      getProvider().setDefaultRepository(repositoryId);
   }

   @Override
   public RepositoryMigrationReport migrateRepository(int batchSize,
         boolean evaluateTotalCount, String repositoryId)
         throws DocumentManagementServiceException
   {
      return getProvider().getExplicitInstance(repositoryId)
            .getService(getUserContext())
            .migrateRepository(batchSize, evaluateTotalCount);
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