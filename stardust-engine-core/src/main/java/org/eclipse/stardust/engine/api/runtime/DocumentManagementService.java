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

import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;



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
 * @version $Revision$
 */
public interface DocumentManagementService extends Service
{

   ///////////////////////////////////////////////////////////////////////////////////////
   // Document retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Gets the document by ID or path.
    *
    * @param documentId the ID or path of the document.
    * @return the document or null if no document with such ID (or path) exists.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Document getDocument(String documentId) throws DocumentManagementServiceException;

   /**
    * Gets all versions of the document by document ID (of any of its version).
    *
    * @param documentId the ID (any version) or path of the document.
    * @return list of document versions found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   List<Document> getDocumentVersions(String documentId) throws DocumentManagementServiceException;

   /**
    * Gets multiple documents by ID or path.
    *
    * @param documentIds list of document IDs or paths.
    * @return list of documents found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   List<Document> getDocuments(List<String> documentIds) throws DocumentManagementServiceException;

   /**
    * Gets documents based on the name pattern search.
    *
    * @param namePattern the name pattern to search for.
    * @return list of documents found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   @Deprecated
   List<Document> findDocumentsByName(String namePattern) throws DocumentManagementServiceException;

   /**
    * Gets documents based on the XPath query.
    *
    * @param xpathQuery the XPath query.
    * @return list of documents found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   @Deprecated
   List<Document> findDocuments(String xpathQuery) throws DocumentManagementServiceException;

   /**
    * Retrieves all documents satisfying the criteria specified in the provided query.
    *
    * @param query the document query.
    *
    * @return a List of Document objects.
    */
   Documents findDocuments(DocumentQuery query);

   /**
    * Retrieves the content of the document identified by <code>documentId</code>.
    * <p>
    * Warning: this method should only be used for documents of reasonable size as the
    * full content will be materialized in memory both on the server as well as on the
    * client. It is recommended to us the facilities provided by
    * {@link DmsContentServlet} for memory efficient content access.
    *
    * @param documentId The ID or path of the document content should be retrieved for.
    * @return A byte array containing the document content. This byte array will be
    *       encoded according to the document's {@link Document#getEncoding()} attribute.
    * @throws DocumentManagementServiceException on DMS specific errors
    *
    * @see #requestDocumentContentDownload(String)
    */
   byte[] retrieveDocumentContent(String documentId) throws DocumentManagementServiceException;

   /**
    * Initiates document content download via {@link DmsContentServlet}. The
    * returned token should be used as relative URI for the content Servlet and will be
    * valid as long as the session associated with this service is alive.
    *
    * @param documentId The ID or path of the document content should be retrieved for.
    * @return A download token valid for the lifetime of this service's session.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   String requestDocumentContentDownload(String documentId) throws DocumentManagementServiceException;

   ///////////////////////////////////////////////////////////////////////////////////////
   // Folder retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Retrieves a folder and lists its members.
    *
    * @param folderId The ID or path expression identifying the folder to be retrieved.
    * @return The resolved folder if no folder with such ID (or path) exists.
    * @throws DocumentManagementServiceException on DMS specific errors
    *
    * @see Folder#LOD_LIST_MEMBERS
    */
   Folder getFolder(String folderId) throws DocumentManagementServiceException;

   /**
    * Retrieves a folder. Level of detail of information returned is controlled by
    * levelOfDetail.
    *
    * @param folderId ID or path of the folder.
    * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>, <code>Folder.LOD_LIST_MEMBERS</code>
    * or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
    * @return the folder or null if no folder with such ID (or path) exists.
    * @throws DocumentManagementServiceException on DMS specific errors
    *
    * @see Folder#LOD_LIST_MEMBERS
    */
   Folder getFolder(String folderId, int levelOfDetail) throws DocumentManagementServiceException;

   /**
    * Gets multiple folders by ID or path.
    *
    * @param folderIds list of IDs or paths.
    * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>, <code>Folder.LOD_LIST_MEMBERS</code>
    * or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
    * @return list of folders found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   List<Folder> getFolders(List<String> folderIds, int levelOfDetail) throws DocumentManagementServiceException;

   /**
    * Gets folders based on the name pattern search.
    *
    * @param namePattern the name pattern to search for.
    * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>, <code>Folder.LOD_LIST_MEMBERS</code>
    * or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
    * @return list of folders found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   @Deprecated
   List<Folder> findFoldersByName(String namePattern, int levelOfDetail) throws DocumentManagementServiceException;

   /**
    * Gets folders based on the name XPath query.
    *
    * @param xpathQuery the XPath query.
    * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>, <code>Folder.LOD_LIST_MEMBERS</code>
    * or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
    * @return list of folders found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   @Deprecated
   List<Folder> findFolders(String xpathQuery, int levelOfDetail) throws DocumentManagementServiceException;

   ///////////////////////////////////////////////////////////////////////////////////////
   // Document manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Creates document in a folder described by the document info. The new document
    * will have no content.
    *
    * @param folderId ID or path of the folder to create the document in. Value "/"
    * designates the top-level folder.
    * @param document an instance of <code>DocumentInfo</code> that describs the document.
    * @return the new document.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Document createDocument(String folderId, DocumentInfo document) throws DocumentManagementServiceException;

   /**
    * Creates document in a folder described by the document info.
    * <p>
    * For the content an encoding can be specified e.g. (UTF-8, UTF-16).
    * The encoding can take any value and can be used to decode the content <code>byte[]</code>
    * after retrieving it via {@link #retrieveDocumentContent(String)}.
    *
    * <p>
    * Warning: this method should only be used for documents of reasonable size as the
    * full content will be materialized in memory both on the server as well as on the
    * client. It is recommended to us the facilities provided by
    * {@link DmsContentServlet} for memory efficient content access.
    *
    * @param folderId ID or path of the folder to create the document in. Value "/"
    * designates the top-level folder
    * @param document an instance of <code>DocumentInfo</code> that describes the document.
    * @param content the content of the new document.
    * @param encoding encoding of the new document content.
    * @return the new document.
    * @throws DocumentManagementServiceException on DMS specific errors
    *
    * @see #requestDocumentContentUpload(String)
    */
   Document createDocument(String folderId, DocumentInfo document, byte[] content,
         String encoding) throws DocumentManagementServiceException;

   /**
    * Creates a new version of the document.
    *
    * @param documentId ID or path of the document to be versioned
    * @param versionLabel label for the new revision. The label must be unique per document.
    * @return document describing the new document version
    * @throws DocumentManagementServiceException on DMS specific errors
    * @deprecated since 7.0 use {@link #versionDocument(String, String, String)}
    */
   @Deprecated
   Document versionDocument(String documentId, String versionLabel) throws DocumentManagementServiceException;

   /**
    * Creates a new version of the document.
    *
    * @param documentId ID or path of the document to be versioned
    * @param versionComment comment for the new revision
    * @param versionLabel label for the new revision. The label must be unique per document.
    * @return document describing the new document version
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Document versionDocument(String documentId, String versionComment, String versionLabel) throws DocumentManagementServiceException;

   /**
    * Removes a version of a document. At least one version has to remain.<br>
    * A call to remove the last remaining version leads to a
    * {@link DocumentManagementServiceException}.<br>
    * If the document exists but the documentRevisionId cannot be resolved to a version of
    * the document a {@link DocumentManagementServiceException} will be thrown.<br>
    * An invalid <code>documentId</code> will lead to a {@link DocumentManagementServiceException}.
    *
    * @param documentId ID or path of the document.
    * @param documentRevisionId The revisionId of the document version to be removed.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   void removeDocumentVersion(String documentId, String documentRevisionId) throws DocumentManagementServiceException;

   /**
    * Moves the document to the target path.
    * <p>
    * If the targetPath or folderId points to a different repository than the source
    * document is located in only the latest version is moved to the targeted repository.
    *
    * @param documentId The document to be moved.
    * @param targetPath The path or folderId to move the document to.
    * @return The moved Document.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Document moveDocument(final String documentId, final String targetPath) throws DocumentManagementServiceException;

   /**
    * Updates document (except document content).
    *
    * @param document document to update.
    * @param createNewRevision if true, new revision of the document will be created
    * @param versionLabel if createNewRevision is true, the new revision will be labeled with this label. The label must be unique per document.
    * @param keepLocked if true, the document will be kept locked after update.
    * @return the updated document
    * @throws DocumentManagementServiceException on DMS specific errors
    * @deprecated since 7.0 use {@link #updateDocument(Document, boolean, String, String, boolean)}
    */
   @Deprecated
   Document updateDocument(Document document, boolean createNewRevision, String versionLabel, boolean keepLocked) throws DocumentManagementServiceException;

   /**
    * Updates document (except document content).
    *
    * @param document document to update.
    * @param createNewRevision if true, new revision of the document will be created
    * @param versionComment can be specified to comment the version operation.
    * @param versionLabel if createNewRevision is true, the new revision will be labeled with this label. The label must be unique per document.
    * @param keepLocked if true, the document will be kept locked after update.
    * @return the updated document
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Document updateDocument(Document document, boolean createNewRevision, String versionComment, String versionLabel, boolean keepLocked) throws DocumentManagementServiceException;


   /**
    * Updates document.
    *
    * <p>
    * Warning: this method should only be used for documents of reasonable size as the
    * full content will be materialized in memory both on the server as well as on the
    * client. It is recommended to us the facilities provided by
    * {@link DmsContentServlet} for memory efficient content access.

    * @param document document to update.
    * @param content new document content.
    * @param encoding encoding of the new document content.
    * @param createNewRevision if true, new revision of the document will be created
    * @param versionLabel if createNewRevision is true, the new revision will be labeled with this label. The label must be unique per document.
    * @param keepLocked if true, the document will be kept locked after update.
    * @return the updated document
    * @throws DocumentManagementServiceException on DMS specific errors
    *
    * @see #requestDocumentContentUpload(String)
    * @deprecated since 7.0 use {@link #updateDocument(Document, byte[], String, boolean, String, String, boolean)}
    */
   @Deprecated
   Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionLabel, boolean keepLocked) throws DocumentManagementServiceException;

   /**
    * Updates document.
    *
    * <p>
    * Warning: this method should only be used for documents of reasonable size as the
    * full content will be materialized in memory both on the server as well as on the
    * client. It is recommended to us the facilities provided by
    * {@link DmsContentServlet} for memory efficient content access.

    * @param document document to update.
    * @param content new document content.
    * @param encoding encoding of the new document content.
    * @param createNewRevision if true, new revision of the document will be created
    * @param versionComment can be specified to comment the version operation.
    * @param versionLabel if createNewRevision is true, the new revision will be labeled with this label. The label must be unique per document.
    * @param keepLocked if true, the document will be kept locked after update.
    * @return the updated document
    * @throws DocumentManagementServiceException on DMS specific errors
    *
    * @see #requestDocumentContentUpload(String)
    */
   Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionComment, String versionLabel, boolean keepLocked) throws DocumentManagementServiceException;


   /**
    * Initiates document content upload via {@link DmsContentServlet}. The
    * returned token should be used as relative URI for the content Servlet and will be
    * valid as long as the session associated with this service is alive.
    *
    * @param documentId The ID/path of the document content should be retrieved for.
    * @return An upload token valid for the lifetime of this service's session.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   String requestDocumentContentUpload(String documentId) throws DocumentManagementServiceException;

   /**
    * Removes document.
    *
    * @param documentId ID or path of the document to remove.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   void removeDocument(String documentId) throws DocumentManagementServiceException;

   ///////////////////////////////////////////////////////////////////////////////////////
   // Folder manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Creates document in a folder described by the document info. The new document
    * will have no content.
    *
    * @param parentFolderId ID or path of the folder to create the folder in. Value "/"
    * designates the top-level folder.
    * @param folder an instance of <code>FolderInfo</code> that describs the folder.
    * @return the new folder.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Folder createFolder(String parentFolderId, FolderInfo folder) throws DocumentManagementServiceException;

   /**
    * Updates folder.
    *
    * @param folder folder to be updated.
    * @return the updated folder.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Folder updateFolder(Folder folder) throws DocumentManagementServiceException;

   /**
    * Removes folder.
    *
    * @param folderId ID or path of the folder to remove.
    * @param recursive if true, documents and subfolders will be removed also
    * (applies to all the children). If false, only the folder itself will be removed.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   void removeFolder(String folderId, boolean recursive) throws DocumentManagementServiceException;

   ///////////////////////////////////////////////////////////////////////////////////////
   // Security.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Returns the privileges the session has for the resource denoted by
    * resourceId, which must exist.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<Privilege> getPrivileges(String resourceId);

   /**
    * Returns the IAccessControlPolicy objects that currently are in effect on
    * the resource denoted by resourceId (cumulated).
    *
    * Returned objects can not be modified, they represent a read-only view of
    * effective policies.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<AccessControlPolicy> getEffectivePolicies(String resourceId);

   /**
    * Returns the IAccessControlPolicy objects that are currently set for
    * the resource denoted by resourceId.
    *
    * Returned objects can be changed, changes take effect after calling
    * setPolicy()
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<AccessControlPolicy> getPolicies(String resourceId);

   /**
    * Returns the IAccessControlPolicy objects that can be set for
    * the resource denoted by resourceId.
    *
    * Returned objects can be changed, and used as arguments to
    * setPolicy() in order to add a new policy.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<AccessControlPolicy> getApplicablePolicies(String resourceId);

   /**
    * Binds the policy to the resource denoted by resourceId (overwrites the old
    * version of the policy)
    *
    * If the policy does not contain any IAccessControlEntry then this policy is
    * removed from the resource.
    *
    * If the policy was obtained using getApplicablePolicies(), the policy will
    * be added, if it was obtained using getPolicies(), the policy will replace
    * its old version.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @param policy
    */
   void setPolicy(String resourceId, AccessControlPolicy policy);

   /**
    * Migrates resources in the repository. This migration process works sequential from
    * the current version to the next higher version. The count of resources migrated in
    * one execution is limited by the parameter batchSize.<br> Subsequent calls will migrate
    * further resources if there are resources for migration available. After all
    * resources for the migration from one version to the next are processed subsequent
    * calls will start the migration to the next higher repository structure version.<br> The
    * migration is complete when the two current version (vfs internal and engine structure) reach the respective target version.
    * <p>
    * <b>Important Note</b>
    * Please ensure there is no other write access on the repository to avoid race conditions!
    * Since there is no locking mechanism this should be run in read-only maintenance window!
    * <p>
    * The MigrationReport returned by each call contains information about: Total resources that need migration to the next version,
    * resources already migrated, current version, next version and target version of the repository structure.
    * <p>There are two different versions, internal versions are migrated first.
    * <br>The vfs internal version called <code>repositoryVersion</code>.
    * {@link RepositoryMigrationReport#getTargetRepositoryVersion()}
    * <br>The engine's structure version called <code>repositoryStructureVersion</code>.
    * {@link RepositoryMigrationReport#getTargetRepositoryStructureVersion()}
    *
    * @param batchSize
    *           count of resources to be migrated in this call. A value of 0 will return a
    *           MigrationReport without migrating.
    * @param evaluateTotalCount
    *           if set to <code>true</code> the total count of resources that need processing in this
    *           migration step is evaluated. Setting this parameter to <code>false</code> saves
    *           performance.
    * @return a report containing information about the migration batch execution.
    * @throws DocumentManagementServiceException if there are problems in repository access or the user is not an administrator.
    */
   RepositoryMigrationReport migrateRepository(int batchSize, boolean evaluateTotalCount) throws DocumentManagementServiceException;

   /**
    * Retrieves the XSD schema for the specified schema location from the Document
    * Repository serialized into a byte[].
    *
    * @param schemaLocation
    *           the document type's schema location
    * @return XSD schema of this document type
    * @throws ObjectNotFoundException
    *            if the specified schema location cannot be found
    */
   byte[] getSchemaDefinition(String schemaLocation) throws ObjectNotFoundException;

   ///////////////////////////////////////////////////////////////////////////////////////
   // Repository Management.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Binds a new repository instance.
    * <p>
    * The repository id supplied via {@link IRepositoryConfiguration#REPOSITORY_ID} can be
    * freely chosen but has to differ from currently bound repository ids. <br>
    * The provider id supplied via {@link IRepositoryConfiguration#PROVIDER_ID} has to
    * match a registered repository provider.
    * <p>
    * A template {@link IRepositoryConfiguration} can be retrieved from
    * {@link IRepositoryProviderInfo#getConfigurationTemplate()}.<br>
    * This template contains keys and template values that are required for the
    * configuration of an new repository instance.
    *
    * @param configuration
    *           The configuration for the repository to bind.
    */
   void bindRepository(IRepositoryConfiguration configuration);

   /**
    * Unbinds a previously bound repository.
    *
    * @param repositoryId The id of the repository instance to unbind.
    */
   void unbindRepository(String repositoryId);

   /**
    * Provides information about all bound repositories.
    *
    * @return Repository instance information.
    */
   List<IRepositoryInstanceInfo> getRepositoryInstanceInfos();

   /**
    * Provides information about all available repository providers.
    * <p>
    * The {@link IRepositoryProviderInfo#getProviderId()} is used to select a repository
    * provider when binding a new repository instance with
    * {@link #bindRepository(IRepositoryConfiguration)}.
    *
    * @return Repository provider information.
    */
   List<IRepositoryProviderInfo> getRepositoryProviderInfos();

   /**
    * Allows to set a bound repository as default repository.
    * <p>
    * Path based access targets the default repository. <br>
    * Id based access targets the repository contained in the id. An id provided by the
    * system is always prefixed with a repository id. If the id is not provided by the
    * system and is not prefixed with a repository id the default repository is targeted.
    *
    * @param repositoryId
    *           The id of the repository instance.
    */
   void setDefaultRepository(String repositoryId);

   /**
    * Allows to identify the currently set default repository.
    *
    * @return The id of the currently set default repository.
    */
   String getDefaultRepository();

   ///////////////////////////////////////////////////////////////////////////////////////
   // Repository specific methods.
   ///////////////////////////////////////////////////////////////////////////////////////

   RepositoryMigrationReport migrateRepository(int batchSize, boolean evaluateTotalCount, String repositoryId)
         throws DocumentManagementServiceException;

}
