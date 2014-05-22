/*
 * Generated from Revision: 72466 
 */
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.Local;

/**
 * The DocumentManagementService provides all functionality for DMS operations in a
 * CARNOT runtime environment.
 * <p>
 * This includes:
 * <ul>
 * <li>retrieving, adding, updating and deleting documents and folders,</li>
 * <li>versioning documents,</li>
 * <li>retrieving and updating document content.</li>
 * </ul>
 *
 * @author rsauer
 * @version 72466
 */
@Local
public interface DocumentManagementService extends org.eclipse.stardust.engine.api.ejb3.beans.Ejb3Service
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocument(java.lang.String documentId)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         getDocument(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocumentVersions(java.lang.String documentId)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Document>
         getDocumentVersions(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocuments(java.util.List documentIds)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Document>
         getDocuments(
         java.util.List documentIds, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findDocumentsByName(java.lang.String namePattern)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Document>
         findDocumentsByName(
         java.lang.String namePattern,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findDocuments(java.lang.String xpathQuery)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Document>
         findDocuments(
         java.lang.String xpathQuery, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findDocuments(org.eclipse.stardust.engine.api.query.DocumentQuery query)
    */
    public org.eclipse.stardust.engine.api.runtime.Documents
         findDocuments(
         org.eclipse.stardust.engine.api.query.DocumentQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#retrieveDocumentContent(java.lang.String documentId)
    */
    public byte[] retrieveDocumentContent(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#requestDocumentContentDownload(java.lang.String documentId)
    */
    public java.lang.String requestDocumentContentDownload(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolder(java.lang.String folderId)
    */
    public org.eclipse.stardust.engine.api.runtime.Folder
         getFolder(
         java.lang.String folderId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolder(java.lang.String folderId, int levelOfDetail)
    */
    public org.eclipse.stardust.engine.api.runtime.Folder
         getFolder(
         java.lang.String folderId, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolders(java.util.List folderIds, int levelOfDetail)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Folder>
         getFolders(
         java.util.List folderIds, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findFoldersByName(java.lang.String namePattern, int levelOfDetail)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Folder>
         findFoldersByName(
         java.lang.String namePattern, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findFolders(java.lang.String xpathQuery, int levelOfDetail)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Folder>
         findFolders(
         java.lang.String xpathQuery, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createDocument(java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo document)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         createDocument(
         java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo
         document, org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createDocument(java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo document, byte[] content, java.lang.String encoding)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         createDocument(
         java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo
         document, byte[] content, java.lang.String encoding,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#versionDocument(java.lang.String documentId, java.lang.String versionLabel)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         versionDocument(
         java.lang.String documentId, java.lang.String versionLabel,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#versionDocument(java.lang.String documentId, java.lang.String versionComment, java.lang.String versionLabel)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         versionDocument(
         java.lang.String documentId, java.lang.String versionComment, java.lang.String
         versionLabel, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeDocumentVersion(java.lang.String documentId, java.lang.String documentRevisionId)
    */
    public void removeDocumentVersion(
         java.lang.String documentId, java.lang.String documentRevisionId,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#moveDocument(java.lang.String documentId, java.lang.String targetPath)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         moveDocument(
         java.lang.String documentId, java.lang.String targetPath,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(org.eclipse.stardust.engine.api.runtime.Document document, boolean createNewRevision, java.lang.String versionLabel, boolean keepLocked)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, boolean
         createNewRevision, java.lang.String versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(org.eclipse.stardust.engine.api.runtime.Document document, boolean createNewRevision, java.lang.String versionComment, java.lang.String versionLabel, boolean keepLocked)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, boolean
         createNewRevision, java.lang.String versionComment, java.lang.String
         versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(org.eclipse.stardust.engine.api.runtime.Document document, byte[] content, java.lang.String encoding, boolean createNewRevision, java.lang.String versionLabel, boolean keepLocked)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, byte[] content,
         java.lang.String encoding, boolean createNewRevision, java.lang.String
         versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(org.eclipse.stardust.engine.api.runtime.Document document, byte[] content, java.lang.String encoding, boolean createNewRevision, java.lang.String versionComment, java.lang.String versionLabel, boolean keepLocked)
    */
    public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, byte[] content,
         java.lang.String encoding, boolean createNewRevision, java.lang.String
         versionComment, java.lang.String versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#requestDocumentContentUpload(java.lang.String documentId)
    */
    public java.lang.String requestDocumentContentUpload(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeDocument(java.lang.String documentId)
    */
    public void removeDocument(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createFolder(java.lang.String parentFolderId, org.eclipse.stardust.engine.api.runtime.FolderInfo folder)
    */
    public org.eclipse.stardust.engine.api.runtime.Folder
         createFolder(
         java.lang.String parentFolderId,
         org.eclipse.stardust.engine.api.runtime.FolderInfo folder,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateFolder(org.eclipse.stardust.engine.api.runtime.Folder folder)
    */
    public org.eclipse.stardust.engine.api.runtime.Folder
         updateFolder(
         org.eclipse.stardust.engine.api.runtime.Folder folder,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeFolder(java.lang.String folderId, boolean recursive)
    */
    public void removeFolder(
         java.lang.String folderId, boolean recursive,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getPrivileges(java.lang.String resourceId)
    */
    public java.util.Set<org.eclipse.stardust.engine.api.runtime.Privilege>
         getPrivileges(
         java.lang.String resourceId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getEffectivePolicies(java.lang.String resourceId)
    */
    public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getEffectivePolicies(
         java.lang.String resourceId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getPolicies(java.lang.String resourceId)
    */
    public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getPolicies(
         java.lang.String resourceId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getApplicablePolicies(java.lang.String resourceId)
    */
    public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getApplicablePolicies(
         java.lang.String resourceId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#setPolicy(java.lang.String resourceId, org.eclipse.stardust.engine.api.runtime.AccessControlPolicy policy)
    */
    public void setPolicy(
         java.lang.String resourceId,
         org.eclipse.stardust.engine.api.runtime.AccessControlPolicy policy,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#migrateRepository(int batchSize, boolean evaluateTotalCount)
    */
    public org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport
         migrateRepository(
         int batchSize, boolean evaluateTotalCount,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getSchemaDefinition(java.lang.String schemaLocation)
    */
    public byte[] getSchemaDefinition(
         java.lang.String schemaLocation,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#bindRepository(org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration configuration)
    */
    public void
         bindRepository(
         org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration configuration,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#unbindRepository(java.lang.String repositoryId)
    */
    public void unbindRepository(
         java.lang.String repositoryId,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getRepositoryInstanceInfos()
    */
    public
         java.util.List<org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo>
         getRepositoryInstanceInfos(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getRepositoryProviderInfos()
    */
    public
         java.util.List<org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo>
         getRepositoryProviderInfos(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#setDefaultRepository(java.lang.String repositoryId)
    */
    public void setDefaultRepository(
         java.lang.String repositoryId,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDefaultRepository()
    */
    public java.lang.String
         getDefaultRepository(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#migrateRepository(int batchSize, boolean evaluateTotalCount, java.lang.String repositoryId)
    */
    public org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport
         migrateRepository(
         int batchSize, boolean evaluateTotalCount, java.lang.String repositoryId,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         }