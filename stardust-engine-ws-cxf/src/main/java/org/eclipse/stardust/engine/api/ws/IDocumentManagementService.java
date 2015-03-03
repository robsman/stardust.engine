package org.eclipse.stardust.engine.api.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * The DocumentManagementService provides all functionality for DMS operations in a
 *  	  CARNOT runtime environment.
 *  	  This includes:
 * retrieving, adding, updating and deleting documents and folders,
 * versioning documents, retrieving and updating document content.
 *    	  
 *
 * This class was generated by Apache CXF 2.6.1
 * 2015-02-24T10:40:56.756+01:00
 * Generated source version: 2.6.1
 * 
 */
@WebService(targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "IDocumentManagementService")
@XmlSeeAlso({ObjectFactory.class})
public interface IDocumentManagementService {

    /**
     * Binds a new repository instance. The repository id supplied via the RepositoryConfiguration can be
     *  freely chosen but has to differ from currently bound repository ids. The provider id supplied via RepositoryConfiguration has to
     *  match a registered repository provider. A template RepositoryConfiguration can be retrieved from
     *  RepositoryProviderInfo. This template contains keys and template values that are required for the configuration of an new repository instance.
     * 
     */
    @RequestWrapper(localName = "bindRepository", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.BindRepository")
    @WebMethod(action = "bindRepository")
    @ResponseWrapper(localName = "bindRepositoryResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.BindRepositoryResponse")
    public void bindRepository(
        @WebParam(name = "repositoryConfiguration", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.RepositoryConfigurationXto repositoryConfiguration
    ) throws BpmFault;

    /**
     * Gets all versions of the document by document ID (of any of its version).
     * 
     */
    @WebResult(name = "documents", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDocumentVersions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentVersions")
    @WebMethod(action = "getDocumentVersions")
    @ResponseWrapper(localName = "getDocumentVersionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentVersionsResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentsXto getDocumentVersions(
        @WebParam(name = "documentId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String documentId,
        @WebParam(name = "metaDataType", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.xml.namespace.QName metaDataType
    ) throws BpmFault;

    /**
     * Creates a new version of the document.
     * 
     */
    @WebResult(name = "document", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "versionDocument", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.VersionDocument")
    @WebMethod(action = "versionDocument")
    @ResponseWrapper(localName = "versionDocumentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.VersionDocumentResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentXto versionDocument(
        @WebParam(name = "documentId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String documentId,
        @WebParam(name = "versionComment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String versionComment,
        @WebParam(name = "versionLabel", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String versionLabel
    ) throws BpmFault;

    /**
     * Gets the document by ID or path.
     * 
     */
    @WebResult(name = "document", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDocument", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocument")
    @WebMethod(action = "getDocument")
    @ResponseWrapper(localName = "getDocumentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentXto getDocument(
        @WebParam(name = "documentId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String documentId,
        @WebParam(name = "metaDataType", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.xml.namespace.QName metaDataType
    ) throws BpmFault;

    /**
     * Creates document in a folder described by the document info.
     * 
     */
    @WebResult(name = "document", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "createDocument", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateDocument")
    @WebMethod(action = "createDocument")
    @ResponseWrapper(localName = "createDocumentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateDocumentResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentXto createDocument(
        @WebParam(name = "folderId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String folderId,
        @WebParam(name = "createMissingFolders", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        boolean createMissingFolders,
        @WebParam(name = "documentInfo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DocumentInfoXto documentInfo,
        @WebParam(name = "content", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.activation.DataHandler content,
        @WebParam(name = "versionInfo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DocumentVersionInfoXto versionInfo
    ) throws BpmFault;

    /**
     * Provides information about all bound repositories.
     * 
     */
    @WebResult(name = "repositoryInstanceInfos", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getRepositoryInstanceInfos", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetRepositoryInstanceInfos")
    @WebMethod(action = "getRepositoryInstanceInfos")
    @ResponseWrapper(localName = "getRepositoryInstanceInfosResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetRepositoryInstanceInfosResponse")
    public org.eclipse.stardust.engine.api.ws.RepositoryInstanceInfosXto getRepositoryInstanceInfos() throws BpmFault;

    /**
     * Updates folder.
     * 
     */
    @WebResult(name = "folder", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "updateFolder", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.UpdateFolder")
    @WebMethod(action = "updateFolder")
    @ResponseWrapper(localName = "updateFolderResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.UpdateFolderResponse")
    public org.eclipse.stardust.engine.api.ws.FolderXto updateFolder(
        @WebParam(name = "updateFolder", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.FolderXto updateFolder
    ) throws BpmFault;

    /**
     * Gets multiple documents by ID or path.
     * 
     */
    @WebResult(name = "documents", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDocuments", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocuments")
    @WebMethod(action = "getDocuments")
    @ResponseWrapper(localName = "getDocumentsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentsResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentsXto getDocuments(
        @WebParam(name = "documentIds", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.GetDocuments.DocumentIdsXto documentIds,
        @WebParam(name = "metaDataType", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.xml.namespace.QName metaDataType
    ) throws BpmFault;

    /**
     * Gets Documents based on the name pattern or XPath Query search.
     * 
     */
    @WebResult(name = "documents", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findDocuments", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindDocuments")
    @WebMethod(action = "findDocuments")
    @ResponseWrapper(localName = "findDocumentsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindDocumentsResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentsXto findDocuments(
        @WebParam(name = "documentQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DocumentQueryXto documentQuery
    ) throws BpmFault;

    /**
     * Creates a folder described by the folderInfo in the folder specified by parentFolderId.
     * 
     */
    @WebResult(name = "folder", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "createFolder", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateFolder")
    @WebMethod(action = "createFolder")
    @ResponseWrapper(localName = "createFolderResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateFolderResponse")
    public org.eclipse.stardust.engine.api.ws.FolderXto createFolder(
        @WebParam(name = "parentFolderId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String parentFolderId,
        @WebParam(name = "folderInfo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.FolderInfoXto folderInfo
    ) throws BpmFault;

    /**
     * Binds the policy to the resource denoted by resourceId (overwrites the old version of the policy) If the policy does not contain any Access Control Entry then this policy is removed from the resource.
     * 
     */
    @RequestWrapper(localName = "setPolicy", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetPolicy")
    @WebMethod(action = "setPolicy")
    @ResponseWrapper(localName = "setPolicyResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetPolicyResponse")
    public void setPolicy(
        @WebParam(name = "resourceId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String resourceId,
        @WebParam(name = "accessControlPolicy", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.AccessControlPolicyXto accessControlPolicy
    ) throws BpmFault;

    /**
     * Initiates document content upload via DmsContentServlet.
     * 
     */
    @WebResult(name = "uploadToken", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "requestDocumentContentUpload", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RequestDocumentContentUpload")
    @WebMethod(action = "requestDocumentContentUpload")
    @ResponseWrapper(localName = "requestDocumentContentUploadResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RequestDocumentContentUploadResponse")
    public java.lang.String requestDocumentContentUpload(
        @WebParam(name = "documentId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String documentId
    ) throws BpmFault;

    /**
     * Allows to set a bound repository as default repository.
     * Path based access targets the default repository.
     * Id based access targets the repository contained in the id. An id provided by the
     * system is always prefixed with a repository id. If the id is not provided by the
     * system and is not prefixed with a repository id the default repository is targeted.
     * 
     */
    @RequestWrapper(localName = "setDefaultRepository", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetDefaultRepository")
    @WebMethod(action = "setDefaultRepository")
    @ResponseWrapper(localName = "setDefaultRepositoryResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetDefaultRepositoryResponse")
    public void setDefaultRepository(
        @WebParam(name = "repositoryId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String repositoryId
    ) throws BpmFault;

    /**
     * Gets the schema definition referenced by a document type schema location.
     * 
     */
    @WebResult(name = "schmema", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDocumentTypeSchema", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentTypeSchema")
    @WebMethod(action = "getDocumentTypeSchema")
    @ResponseWrapper(localName = "getDocumentTypeSchemaResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentTypeSchemaResponse")
    public org.eclipse.stardust.engine.api.ws.XmlValueXto getDocumentTypeSchema(
        @WebParam(name = "schemaLocation", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String schemaLocation
    ) throws BpmFault;

    /**
     * Gets a list of document types defined in all alive models. Can be limited to a specified modelId.
     * 
     */
    @WebResult(name = "documentTypeResults", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDocumentTypes", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentTypes")
    @WebMethod(action = "getDocumentTypes")
    @ResponseWrapper(localName = "getDocumentTypesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentTypesResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentTypeResultsXto getDocumentTypes(
        @WebParam(name = "modelId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String modelId
    ) throws BpmFault;

    /**
     * Creates multiple documents in folders described by the corresponding document info.
     * 
     */
    @WebResult(name = "documents", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "createDocuments", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateDocuments")
    @WebMethod(action = "createDocuments")
    @ResponseWrapper(localName = "createDocumentsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateDocumentsResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentsXto createDocuments(
        @WebParam(name = "inputDocuments", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.InputDocumentsXto inputDocuments,
        @WebParam(name = "createMissingFolders", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean createMissingFolders
    ) throws BpmFault;

    /**
     * Gets multiple folders by ID or path.
     * 
     */
    @WebResult(name = "folders", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getFolders", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetFolders")
    @WebMethod(action = "getFolders")
    @ResponseWrapper(localName = "getFoldersResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetFoldersResponse")
    public org.eclipse.stardust.engine.api.ws.FoldersXto getFolders(
        @WebParam(name = "folderIds", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.GetFolders.FolderIdsXto folderIds,
        @WebParam(name = "folderLevelOfDetail", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.FolderLevelOfDetailXto folderLevelOfDetail,
        @WebParam(name = "documentMetaDataType", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.xml.namespace.QName documentMetaDataType,
        @WebParam(name = "folderMetaDataType", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.xml.namespace.QName folderMetaDataType
    ) throws BpmFault;

    /**
     * Gets folders based on the name pattern or XPath query.
     * 
     */
    @WebResult(name = "folders", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findFolders", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindFolders")
    @WebMethod(action = "findFolders")
    @ResponseWrapper(localName = "findFoldersResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindFoldersResponse")
    public org.eclipse.stardust.engine.api.ws.FoldersXto findFolders(
        @WebParam(name = "folderQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.FolderQueryXto folderQuery
    ) throws BpmFault;

    /**
     * Removes folder.
     * 
     */
    @RequestWrapper(localName = "removeFolder", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RemoveFolder")
    @WebMethod(action = "removeFolder")
    @ResponseWrapper(localName = "removeFolderResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RemoveFolderResponse")
    public void removeFolder(
        @WebParam(name = "folderId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String folderId,
        @WebParam(name = "recursive", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean recursive
    ) throws BpmFault;

    /**
     * Updates document.
     * 
     */
    @WebResult(name = "document", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "updateDocument", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.UpdateDocument")
    @WebMethod(action = "updateDocument")
    @ResponseWrapper(localName = "updateDocumentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.UpdateDocumentResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentXto updateDocument(
        @WebParam(name = "documentId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String documentId,
        @WebParam(name = "documentInfo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DocumentInfoXto documentInfo,
        @WebParam(name = "content", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.activation.DataHandler content,
        @WebParam(name = "versionInfo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DocumentVersionInfoXto versionInfo
    ) throws BpmFault;

    /**
     * Migrates resources in the repository. This migration process works sequential from the current version to the next higher version. The count of resources migrated in one execution is limited by the parameter batchSize.
     * Subsequent calls will migrate further resources if there are resources for migration available. After all resources for the migration from one version to the next are processed subsequent calls will start the migration to the next higher repository structure version.
     * The migration is complete if the current version of the repository reaches the target version defined by the repository.
     * Important Note Please ensure there is no other write access on the repository to avoid race conditions! Since there is no locking mechanism this should be run in read-only maintenance window!
     * The MigrationReport returned by each call contains information about: Total resources that need migration to the next version, resources already migrated, current version, next version and target version of the repository structure.
     * 
     */
    @WebResult(name = "repositoryMigrationReport", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "migrateRepository", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.MigrateRepository")
    @WebMethod(action = "migrateRepository")
    @ResponseWrapper(localName = "migrateRepositoryResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.MigrateRepositoryResponse")
    public org.eclipse.stardust.engine.api.ws.RepositoryMigrationReportXto migrateRepository(
        @WebParam(name = "batchSize", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        int batchSize,
        @WebParam(name = "evaluateTotalCount", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        boolean evaluateTotalCount,
        @WebParam(name = "repositoryId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String repositoryId
    ) throws BpmFault;

    /**
     * Retrieves a folder.
     * 
     */
    @WebResult(name = "folder", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getFolder", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetFolder")
    @WebMethod(action = "getFolder")
    @ResponseWrapper(localName = "getFolderResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetFolderResponse")
    public org.eclipse.stardust.engine.api.ws.FolderXto getFolder(
        @WebParam(name = "folderId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String folderId,
        @WebParam(name = "folderLevelOfDetail", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.FolderLevelOfDetailXto folderLevelOfDetail,
        @WebParam(name = "documentMetaDataType", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.xml.namespace.QName documentMetaDataType,
        @WebParam(name = "folderMetaDataType", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.xml.namespace.QName folderMetaDataType
    ) throws BpmFault;

    /**
     * Initiates document content download via DmsContentServlet.
     * 
     */
    @WebResult(name = "downloadToken", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "requestDocumentContentDownload", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RequestDocumentContentDownload")
    @WebMethod(action = "requestDocumentContentDownload")
    @ResponseWrapper(localName = "requestDocumentContentDownloadResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RequestDocumentContentDownloadResponse")
    public java.lang.String requestDocumentContentDownload(
        @WebParam(name = "documentId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String documentId
    ) throws BpmFault;

    /**
     * Provides information about all available repository providers.
     * 
     */
    @WebResult(name = "repositoryProviderInfos", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getRepositoryProviderInfos", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetRepositoryProviderInfos")
    @WebMethod(action = "getRepositoryProviderInfos")
    @ResponseWrapper(localName = "getRepositoryProviderInfosResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetRepositoryProviderInfosResponse")
    public org.eclipse.stardust.engine.api.ws.RepositoryProviderInfosXto getRepositoryProviderInfos() throws BpmFault;

    /**
     * Returns the Access Control Policies for the resource denoted by resourceId by the specified scope.
     * All: Policies that are currently set.
     * Applicable: Policy template which has to be used if no policy is set. (Empty if policies were set once, use 'All' scope to retrieve Policies in this case.)
     * Effective: Effective policies, including implicit Policies from e.g. Administrator or Everyone group.
     * 
     */
    @WebResult(name = "accessControlPolicies", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getPolicies", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPolicies")
    @WebMethod(action = "getPolicies")
    @ResponseWrapper(localName = "getPoliciesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPoliciesResponse")
    public org.eclipse.stardust.engine.api.ws.AccessControlPoliciesXto getPolicies(
        @WebParam(name = "resourceId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String resourceId,
        @WebParam(name = "policyScope", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.PolicyScopeXto policyScope
    ) throws BpmFault;

    /**
     * Returns the privileges the session has for the resource denoted by resourceId, which must exist.
     * 
     */
    @WebResult(name = "privileges", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getPrivileges", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPrivileges")
    @WebMethod(action = "getPrivileges")
    @ResponseWrapper(localName = "getPrivilegesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPrivilegesResponse")
    public org.eclipse.stardust.engine.api.ws.PrivilegesXto getPrivileges(
        @WebParam(name = "resourceId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String resourceId
    ) throws BpmFault;

    /**
     * Allows to identify the currently set default repository.
     * 
     */
    @WebResult(name = "repositoryId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDefaultRepository", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDefaultRepository")
    @WebMethod(action = "getDefaultRepository")
    @ResponseWrapper(localName = "getDefaultRepositoryResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDefaultRepositoryResponse")
    public java.lang.String getDefaultRepository() throws BpmFault;

    /**
     * Unbinds a previously bound repository.
     * 
     */
    @RequestWrapper(localName = "unbindRepository", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.UnbindRepository")
    @WebMethod(action = "unbindRepository")
    @ResponseWrapper(localName = "unbindRepositoryResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.UnbindRepositoryResponse")
    public void unbindRepository(
        @WebParam(name = "repositoryId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String repositoryId
    ) throws BpmFault;

    /**
     * Retrieves the content of the document identified by documentId.
     * 
     */
    @WebResult(name = "content", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDocumentContent", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentContent")
    @WebMethod(action = "getDocumentContent")
    @ResponseWrapper(localName = "getDocumentContentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDocumentContentResponse")
    public javax.activation.DataHandler getDocumentContent(
        @WebParam(name = "documentId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String documentId
    ) throws BpmFault;

    /**
     * Removes document.
     * 
     */
    @RequestWrapper(localName = "removeDocument", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RemoveDocument")
    @WebMethod(action = "removeDocument")
    @ResponseWrapper(localName = "removeDocumentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RemoveDocumentResponse")
    public void removeDocument(
        @WebParam(name = "documentId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String documentId
    ) throws BpmFault;
}
