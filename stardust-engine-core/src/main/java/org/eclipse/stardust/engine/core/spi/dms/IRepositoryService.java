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
import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet;



/**
 * The IRepositoryService provides all functionality for document management operations.
 * <p>
 * This includes:
 * <ul>
 * <li>retrieving, adding, updating and deleting documents and folders,</li>
 * <li>versioning documents,</li>
 * <li>retrieving and updating document content.</li>
 * </ul>
 * 
 * Depending on the {@link IRepositoryCapabilities} the {@link IRepositoryProvider}
 * provides only a subset has to be implemented.<br>
 * If a method is not implemented an {@link UnsupportedOperationException} must be thrown.
 * <p>
 * @author rsauer, roland.stamm
 * @version $Revision: 56243 $
 */
public interface IRepositoryService
{

   ///////////////////////////////////////////////////////////////////////////////////////
   // Document retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Gets the document by ID or path.
    * 
    * <p>Implementation is mandatory.
    *
    * @param documentId the ID or path of the document.
    * @return the document or null if no document with such ID (or path) exists.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Document getDocument(String documentId) throws DocumentManagementServiceException;

   /**
    * Gets all versions of the document by document ID (of any of its version).
    *
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isVersioningSupported()} is true.
    *
    * @param documentId the ID (any version) or path of the document.
    * @return list of document versions found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   List<? extends Document> getDocumentVersions(String documentId) throws DocumentManagementServiceException;

   /**
    * Gets multiple documents by ID or path.
    *
    * <p>Implementation is mandatory.
    * 
    * @param documentIds list of document IDs or paths.
    * @return list of documents found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   List<? extends Document> getDocuments(List<String> documentIds) throws DocumentManagementServiceException;

   /**
    * Retrieves the content of the document identified by <code>documentId</code>.
    * <p>
    * Warning: this method should only be used for documents of reasonable size as the
    * full content will be materialized in memory both on the server as well as on the
    * client. It is recommended to us the facilities provided by
    * {@link DmsContentServlet} for memory efficient content access.
    *
    * <p>Implementation is mandatory.
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
    * Retrieves an OutputStream for the document.
    * 
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isStreamingIOSupported()} is true.
    * 
    * @param documentId The ID or path of the document stream should be retrieved for.
    * @param target The target OutputStream
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   public void retrieveDocumentContentStream(String documentId, OutputStream target) throws DocumentManagementServiceException;
   

   ///////////////////////////////////////////////////////////////////////////////////////
   // Folder retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Retrieves a folder and lists its members.
    *
    * <p>Implementation is mandatory.
    *
    * @param folderId The ID or path expression identifying the folder to be retrieved.
    * @return The resolved folder.
    * @throws DocumentManagementServiceException on DMS specific errors
    *
    * @see Folder#LOD_LIST_MEMBERS
    */
   Folder getFolder(String folderId) throws DocumentManagementServiceException;

   /**
    * Retrieves a folder. Level of detail of information returned is controlled by
    * levelOfDetail.
    *
    * <p>Implementation is mandatory.
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
    * <p>Implementation is mandatory.
    *
    * @param folderIds list of IDs or paths.
    * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>, <code>Folder.LOD_LIST_MEMBERS</code>
    * or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
    * @return list of folders found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   List<? extends Folder> getFolders(List<String> folderIds, int levelOfDetail) throws DocumentManagementServiceException;

   

   ///////////////////////////////////////////////////////////////////////////////////////
   // Document manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Creates document in a folder described by the document info. The new document
    * will have no content.
    *
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} is true.
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
    * after retrieving it via {@link #retrieveDocumentContentStream(String, OutputStream)}.
    *
    * <p>
    * Warning: this method should only be used for documents of reasonable size as the
    * full content will be materialized in memory both on the server as well as on the
    * client. It is recommended to us the facilities provided by
    * {@link DmsContentServlet} for memory efficient content access.
    *
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} is true.
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
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} and {@link IRepositoryCapabilities#isVersioningSupported()} is true.
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
    * <p> Implementation is optional if {@link IRepositoryCapabilities#isWriteSupported()} and {@link IRepositoryCapabilities#isVersioningSupported()} is true.
    *
    * @param documentId ID or path of the document.
    * @param documentRevisionId The revisionId of the document version to be removed.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   void removeDocumentVersion(String documentId, String documentRevisionId) throws DocumentManagementServiceException;

   /**
    * Moves the document to the target path.
    *
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} is true.
    *
    * @param documentId The document to be moved.
    * @param targetPath The path to move the document to.
    * @return The moved Document.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Document moveDocument(final String documentId, final String targetPath) throws DocumentManagementServiceException;

   /**
    * Updates document (except document content).
    * 
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} and {@link IRepositoryCapabilities#isVersioningSupported()} is true.
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
    * 
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} and {@link IRepositoryCapabilities#isVersioningSupported()} is true.
    *     
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
    * Allows to set a InputStream for content upload.
    * 
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} and {@link IRepositoryCapabilities#isStreamingIOSupported()} is true.
    * 
    * @param documentId The ID or path of the document stream should be uploaded for.
    * @param source The source InputStream.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   void uploadDocumentContentStream(String documentId, InputStream source, String contentType, String contentEncoding) throws DocumentManagementServiceException;

   /**
    * Removes document.
    *
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} is true.
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
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} is true.
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
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} is true.
    *
    * @param folder folder to be updated.
    * @return the updated folder.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   Folder updateFolder(Folder folder) throws DocumentManagementServiceException;

   /**
    * Removes folder.
    *
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isWriteSupported()} is true.
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
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isAccessControlPolicySupported()} is true.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<Privilege> getPrivileges(String resourceId);

   /**
    * Returns the IAccessControlPolicy objects that currently are in effect on
    * the resource denoted by resourceId (cumulated).
    *
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isAccessControlPolicySupported()} is true.
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
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isAccessControlPolicySupported()} is true.
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
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isAccessControlPolicySupported()} is true.
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
    * <p> Implementation is needed if {@link IRepositoryCapabilities#isAccessControlPolicySupported()} is true.
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
    * <p> Implementation is optional.
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
    * <p> Implementation is optional.
    *
    * @param schemaLocation
    *           the document type's schema location
    * @return XSD schema of this document type
    * @throws ObjectNotFoundException
    *            if the specified schema location cannot be found
    */
   byte[] getSchemaDefinition(String schemaLocation) throws ObjectNotFoundException;

   /**
    * Retrieves all documents satisfying the criteria specified in the provided query.
    *
    * <p> Implementation of {@link DocumentQuery#META_DATA} is needed if {@link IRepositoryCapabilities#isMetaDataSearchSupported()} is true.
    *     Implementation of {@link DocumentQuery#DOC_CONTENT} is needed if {@link IRepositoryCapabilities#isFullTextSearchSupported()} is true.
    *     Implementation of all other options is optional.
    *
    * @param query the document query.
    *
    * @return a List of Document objects.
    */
   Documents findDocuments(DocumentQuery query);

}

