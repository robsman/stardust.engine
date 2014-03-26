/*
 * Generated from  Revision: 56243 
 */
package org.eclipse.stardust.engine.api.ejb2.tunneling;

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
 * @version 56243
 */
public interface TunnelingRemoteDocumentManagementService extends javax.ejb.EJBObject, org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingRemoteService
{

    /**
     * Gets the document by ID or path.
     *
     * @param documentId the ID or path of the document.
     *
     * @return the document or null if no document with such ID (or path) exists.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocument(
     *     java.lang.String documentId)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         getDocument(
         java.lang.String documentId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Gets all versions of the document by document ID (of any of its version).
     *
     * @param documentId the ID (any version) or path of the document.
     *
     * @return list of document versions found.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocumentVersions(
     *     java.lang.String documentId)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Document>
         getDocumentVersions(
         java.lang.String documentId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Gets multiple documents by ID or path.
     *
     * @param documentIds list of document IDs or paths.
     *
     * @return list of documents found.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocuments(
     *     java.util.List documentIds)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Document>
         getDocuments(
         java.util.List documentIds,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Gets documents based on the name pattern search.
     *
     * @param namePattern the name pattern to search for.
     *
     * @return list of documents found.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findDocumentsByName(
     *     java.lang.String namePattern)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Document>
         findDocumentsByName(
         java.lang.String namePattern,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Gets documents based on the XPath query.
     *
     * @param xpathQuery the XPath query.
     *
     * @return list of documents found.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findDocuments(
     *     java.lang.String xpathQuery)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Document>
         findDocuments(
         java.lang.String xpathQuery,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves all documents satisfying the criteria specified in the provided query.
     *
     * @param query the document query.
     *
     * @return a List of Document objects.
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findDocuments(
     *     org.eclipse.stardust.engine.api.query.DocumentQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.Documents
         findDocuments(
         org.eclipse.stardust.engine.api.query.DocumentQuery query,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves the content of the document identified by <code>documentId</code>.
     * <p>
     * Warning: this method should only be used for documents of reasonable size as the
     * full content will be materialized in memory both on the server as well as on the
     * client. It is recommended to us the facilities provided by
     * {@link DmsContentServlet} for memory efficient content access.
     *
     * @param documentId The ID or path of the document content should be retrieved for.
     *
     * @return A byte array containing the document content. This byte array will be
     *           encoded according to the document's {@link Document#getEncoding()} attribute.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #requestDocumentContentDownload(String)
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#retrieveDocumentContent(
     *     java.lang.String documentId)
     */
    public byte[] retrieveDocumentContent(
         java.lang.String documentId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Initiates document content download via {@link DmsContentServlet}. The
     * returned token should be used as relative URI for the content Servlet and will be
     * valid as long as the session associated with this service is alive.
     *
     * @param documentId The ID or path of the document content should be retrieved for.
     *
     * @return A download token valid for the lifetime of this service's session.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#requestDocumentContentDownload(
     *     java.lang.String documentId)
     */
    public java.lang.String requestDocumentContentDownload(
         java.lang.String documentId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves a folder and lists its members.
     *
     * @param folderId The ID or path expression identifying the folder to be retrieved.
     *
     * @return The resolved folder.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.Folder#LOD_LIST_MEMBERS
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolder(
     *     java.lang.String folderId)
     */
    public org.eclipse.stardust.engine.api.runtime.Folder
         getFolder(
         java.lang.String folderId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves a folder. Level of detail of information returned is controlled by
     * levelOfDetail.
     *
     * @param folderId ID or path of the folder.
     * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>,
     *     <code>Folder.LOD_LIST_MEMBERS</code>
     *     or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
     *
     * @return the folder or null if no folder with such ID (or path) exists.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.Folder#LOD_LIST_MEMBERS
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolder(
     *     java.lang.String folderId, int levelOfDetail)
     */
    public org.eclipse.stardust.engine.api.runtime.Folder
         getFolder(
         java.lang.String folderId, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Gets multiple folders by ID or path.
     *
     * @param folderIds list of IDs or paths.
     * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>,
     *     <code>Folder.LOD_LIST_MEMBERS</code>
     *     or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
     *
     * @return list of folders found.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolders(
     *     java.util.List folderIds, int levelOfDetail)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Folder>
         getFolders(
         java.util.List folderIds, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Gets folders based on the name pattern search.
     *
     * @param namePattern the name pattern to search for.
     * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>,
     *     <code>Folder.LOD_LIST_MEMBERS</code>
     *     or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
     *
     * @return list of folders found.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findFoldersByName(
     *     java.lang.String namePattern, int levelOfDetail)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Folder>
         findFoldersByName(
         java.lang.String namePattern, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Gets folders based on the name XPath query.
     *
     * @param xpathQuery the XPath query.
     * @param levelOfDetail one of <code>Folder.LOD_NO_MEMBERS</code>,
     *     <code>Folder.LOD_LIST_MEMBERS</code>
     *     or <code>Folder.LOD_LIST_MEMBERS_OF_MEMBERS</code>.
     *
     * @return list of folders found.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findFolders(
     *     java.lang.String xpathQuery, int levelOfDetail)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Folder>
         findFolders(
         java.lang.String xpathQuery, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Creates document in a folder described by the document info. The new document
     * will have no content.
     *
     * @param folderId ID or path of the folder to create the document in. Value "/"
     *     designates the top-level folder.
     * @param document an instance of <code>DocumentInfo</code> that describs the document.
     *
     * @return the new document.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createDocument(
     *     java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo
     *     document)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         createDocument(
         java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo
         document, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Creates document in a folder described by the document info.
     * <p>
     * For the content an encoding can be specified e.g. (UTF-8, UTF-16).
     * The encoding can take any value and can be used to decode the content
     * <code>byte[]</code>
     * after retrieving it via {@link #retrieveDocumentContent(String)}.
     * 
     * <p>
     * Warning: this method should only be used for documents of reasonable size as the
     * full content will be materialized in memory both on the server as well as on the
     * client. It is recommended to us the facilities provided by
     * {@link DmsContentServlet} for memory efficient content access.
     *
     * @param folderId ID or path of the folder to create the document in. Value "/"
     *     designates the top-level folder
     * @param document an instance of <code>DocumentInfo</code> that describes the document.
     * @param content the content of the new document.
     * @param encoding encoding of the new document content.
     *
     * @return the new document.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #requestDocumentContentUpload(String)
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createDocument(
     *     java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo
     *     document, byte[] content, java.lang.String encoding)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         createDocument(
         java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo
         document, byte[] content, java.lang.String encoding,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Creates a new version of the document.
     *
     * @param documentId ID or path of the document to be versioned
     * @param versionLabel label for the new revision. The label must be unique per document.
     *
     * @return document describing the new document version
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     *
     * @deprecated since 7.0 use {@link #versionDocument(String, String, String)}
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#versionDocument(
     *     java.lang.String documentId, java.lang.String versionLabel)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         versionDocument(
         java.lang.String documentId, java.lang.String versionLabel,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Creates a new version of the document.
     *
     * @param documentId ID or path of the document to be versioned
     * @param versionComment comment for the new revision
     * @param versionLabel label for the new revision. The label must be unique per document.
     *
     * @return document describing the new document version
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#versionDocument(
     *     java.lang.String documentId, java.lang.String versionComment, java.lang.String
     *     versionLabel)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         versionDocument(
         java.lang.String documentId, java.lang.String versionComment, java.lang.String
         versionLabel, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Removes a version of a document. At least one version has to remain.<br>
     * A call to remove the last remaining version leads to a
     * {@link DocumentManagementServiceException}.<br>
     * If the document exists but the documentRevisionId cannot be resolved to a version of
     * the document a {@link DocumentManagementServiceException} will be thrown.<br>
     * An invalid <code>documentId</code> will lead to a {@link
     * DocumentManagementServiceException}.
     *
     * @param documentId ID or path of the document.
     * @param documentRevisionId The revisionId of the document version to be removed.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeDocumentVersion(
     *     java.lang.String documentId, java.lang.String documentRevisionId)
     */
    public void removeDocumentVersion(
         java.lang.String documentId, java.lang.String documentRevisionId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Moves the document to the target path.
     *
     * @param documentId The document to be moved.
     * @param targetPath The path to move the document to.
     *
     * @return The moved Document.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#moveDocument(
     *     java.lang.String documentId, java.lang.String targetPath)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         moveDocument(
         java.lang.String documentId, java.lang.String targetPath,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Updates document (except document content).
     *
     * @param document document to update.
     * @param createNewRevision if true, new revision of the document will be created
     * @param versionLabel if createNewRevision is true, the new revision will be labeled with this
     *     label. The label must be unique per document.
     * @param keepLocked if true, the document will be kept locked after update.
     *
     * @return the updated document
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     *
     * @deprecated since 7.0 use {@link #updateDocument(Document, boolean, String, String, boolean)}
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(
     *     org.eclipse.stardust.engine.api.runtime.Document document, boolean createNewRevision,
     *     java.lang.String versionLabel, boolean keepLocked)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, boolean
         createNewRevision, java.lang.String versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Updates document (except document content).
     *
     * @param document document to update.
     * @param createNewRevision if true, new revision of the document will be created
     * @param versionComment can be specified to comment the version operation.
     * @param versionLabel if createNewRevision is true, the new revision will be labeled with this
     *     label. The label must be unique per document.
     * @param keepLocked if true, the document will be kept locked after update.
     *
     * @return the updated document
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(
     *     org.eclipse.stardust.engine.api.runtime.Document document, boolean createNewRevision,
     *     java.lang.String versionComment, java.lang.String versionLabel, boolean keepLocked)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, boolean
         createNewRevision, java.lang.String versionComment, java.lang.String
         versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Updates document.
     * 
     * <p>
     * Warning: this method should only be used for documents of reasonable size as the
     * full content will be materialized in memory both on the server as well as on the
     * client. It is recommended to us the facilities provided by
     * {@link DmsContentServlet} for memory efficient content access.
     *
     * @param document document to update.
     * @param content new document content.
     * @param encoding encoding of the new document content.
     * @param createNewRevision if true, new revision of the document will be created
     * @param versionLabel if createNewRevision is true, the new revision will be labeled with this
     *     label. The label must be unique per document.
     * @param keepLocked if true, the document will be kept locked after update.
     *
     * @return the updated document
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #requestDocumentContentUpload(String)
     *
     * @deprecated since 7.0 use {@link #updateDocument(
     *     Document, byte[], String, boolean, String, String, boolean)}
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(
     *     org.eclipse.stardust.engine.api.runtime.Document document, byte[] content,
     *     java.lang.String encoding, boolean createNewRevision, java.lang.String versionLabel,
     *     boolean keepLocked)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, byte[] content,
         java.lang.String encoding, boolean createNewRevision, java.lang.String
         versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Updates document.
     * 
     * <p>
     * Warning: this method should only be used for documents of reasonable size as the
     * full content will be materialized in memory both on the server as well as on the
     * client. It is recommended to us the facilities provided by
     * {@link DmsContentServlet} for memory efficient content access.
     *
     * @param document document to update.
     * @param content new document content.
     * @param encoding encoding of the new document content.
     * @param createNewRevision if true, new revision of the document will be created
     * @param versionComment can be specified to comment the version operation.
     * @param versionLabel if createNewRevision is true, the new revision will be labeled with this
     *     label. The label must be unique per document.
     * @param keepLocked if true, the document will be kept locked after update.
     *
     * @return the updated document
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #requestDocumentContentUpload(String)
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(
     *     org.eclipse.stardust.engine.api.runtime.Document document, byte[] content,
     *     java.lang.String encoding, boolean createNewRevision, java.lang.String versionComment,
     *     java.lang.String versionLabel, boolean keepLocked)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, byte[] content,
         java.lang.String encoding, boolean createNewRevision, java.lang.String
         versionComment, java.lang.String versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Initiates document content upload via {@link DmsContentServlet}. The
     * returned token should be used as relative URI for the content Servlet and will be
     * valid as long as the session associated with this service is alive.
     *
     * @param documentId The ID/path of the document content should be retrieved for.
     *
     * @return An upload token valid for the lifetime of this service's session.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#requestDocumentContentUpload(
     *     java.lang.String documentId)
     */
    public java.lang.String requestDocumentContentUpload(
         java.lang.String documentId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Removes document.
     *
     * @param documentId ID or path of the document to remove.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeDocument(
     *     java.lang.String documentId)
     */
    public void removeDocument(
         java.lang.String documentId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Creates document in a folder described by the document info. The new document
     * will have no content.
     *
     * @param parentFolderId ID or path of the folder to create the folder in. Value "/"
     *     designates the top-level folder.
     * @param folder an instance of <code>FolderInfo</code> that describs the folder.
     *
     * @return the new folder.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createFolder(
     *     java.lang.String parentFolderId, org.eclipse.stardust.engine.api.runtime.FolderInfo
     *     folder)
     */
    public org.eclipse.stardust.engine.api.runtime.Folder
         createFolder(
         java.lang.String parentFolderId,
         org.eclipse.stardust.engine.api.runtime.FolderInfo folder,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Updates folder.
     *
     * @param folder folder to be updated.
     *
     * @return the updated folder.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateFolder(
     *     org.eclipse.stardust.engine.api.runtime.Folder folder)
     */
    public org.eclipse.stardust.engine.api.runtime.Folder
         updateFolder(
         org.eclipse.stardust.engine.api.runtime.Folder folder,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Removes folder.
     *
     * @param folderId ID or path of the folder to remove.
     * @param recursive if true, documents and subfolders will be removed also
     *     (applies to all the children). If false, only the folder itself will be removed.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException on DMS
     *     specific errors
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeFolder(
     *     java.lang.String folderId, boolean recursive)
     */
    public void removeFolder(
         java.lang.String folderId, boolean recursive,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Returns the privileges the session has for the resource denoted by
     * resourceId, which must exist.
     *
     * @param resourceId absolute path or ID of a file or folder
     *
     * @return 
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getPrivileges(
     *     java.lang.String resourceId)
     */
    public java.util.Set<org.eclipse.stardust.engine.api.runtime.Privilege>
         getPrivileges(
         java.lang.String resourceId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Returns the IAccessControlPolicy objects that currently are in effect on
     * the resource denoted by resourceId (cumulated).
     * 
     * Returned objects can not be modified, they represent a read-only view of
     * effective policies.
     *
     * @param resourceId absolute path or ID of a file or folder
     *
     * @return 
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getEffectivePolicies(
     *     java.lang.String resourceId)
     */
    public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getEffectivePolicies(
         java.lang.String resourceId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Returns the IAccessControlPolicy objects that are currently set for
     * the resource denoted by resourceId.
     * 
     * Returned objects can be changed, changes take effect after calling
     * setPolicy()
     *
     * @param resourceId absolute path or ID of a file or folder
     *
     * @return 
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getPolicies(
     *     java.lang.String resourceId)
     */
    public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getPolicies(
         java.lang.String resourceId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Returns the IAccessControlPolicy objects that can be set for
     * the resource denoted by resourceId.
     * 
     * Returned objects can be changed, and used as arguments to
     * setPolicy() in order to add a new policy.
     *
     * @param resourceId absolute path or ID of a file or folder
     *
     * @return 
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getApplicablePolicies(
     *     java.lang.String resourceId)
     */
    public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getApplicablePolicies(
         java.lang.String resourceId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
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
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#setPolicy(
     *     java.lang.String resourceId,
     *     org.eclipse.stardust.engine.api.runtime.AccessControlPolicy policy)
     */
    public void setPolicy(
         java.lang.String resourceId,
         org.eclipse.stardust.engine.api.runtime.AccessControlPolicy policy,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Migrates resources in the repository. This migration process works sequential from
     * the current version to the next higher version. The count of resources migrated in
     * one execution is limited by the parameter batchSize.<br> Subsequent calls will migrate
     * further resources if there are resources for migration available. After all
     * resources for the migration from one version to the next are processed subsequent
     * calls will start the migration to the next higher repository structure version.<br>
     * The
     * migration is complete when the two current version (
     * vfs internal and engine structure) reach the respective target version.
     * <p>
     * <b>Important Note</b>
     * Please ensure there is no other write access on the repository to avoid race
     * conditions!
     * Since there is no locking mechanism this should be run in read-only maintenance
     * window!
     * <p>
     * The MigrationReport returned by each call contains information about: Total resources
     * that need migration to the next version,
     * resources already migrated, current version, next version and target version of the
     * repository structure.
     * <p>There are two different versions, internal versions are migrated first.
     * <br>The vfs internal version called <code>repositoryVersion</code>.
     * {@link RepositoryMigrationReport#getTargetRepositoryVersion()}
     * <br>The engine's structure version called <code>repositoryStructureVersion</code>.
     * {@link RepositoryMigrationReport#getTargetRepositoryStructureVersion()}
     *
     * @param batchSize
     *               count of resources to be migrated in this call. A value of 0 will return a
     *               MigrationReport without migrating.
     * @param evaluateTotalCount
     *     if set to <code>true</code> the total count of resources that need processing in
     *     essing in this
     *     migration step is evaluated. Setting this parameter to <code>false</code>
     *     se</code> saves
     *               performance.
     *
     * @return a report containing information about the migration batch execution.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException if there
     *     are problems in repository access or the user is not an administrator.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException} will be
     *     wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#migrateRepository(
     *     int batchSize, boolean evaluateTotalCount)
     */
    public org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport
         migrateRepository(
         int batchSize, boolean evaluateTotalCount,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves the XSD schema for the specified schema location from the Document
     * Repository serialized into a byte[].
     *
     * @param schemaLocation
     *               the document type's schema location
     *
     * @return XSD schema of this document type
     *
     * @throws ObjectNotFoundException
     *                if the specified schema location cannot be found
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getSchemaDefinition(
     *     java.lang.String schemaLocation)
     */
    public byte[] getSchemaDefinition(
         java.lang.String schemaLocation,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#bindRepository(
     *     org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration configuration)
     */
    public void
         bindRepository(
         org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration configuration,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#unbindRepository(
     *     java.lang.String repositoryId)
     */
    public void unbindRepository(
         java.lang.String repositoryId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getRepositoryInstanceInfos(
     *     )
     */
    public
         java.util.List<org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo>
         getRepositoryInstanceInfos(
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getRepositoryProviderInfos(
     *     )
     */
    public
         java.util.List<org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo>
         getRepositoryProviderInfos(
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getSchemaDefinition(
     *     java.lang.String schemaLocation, java.lang.String repositoryId)
     */
    public byte[] getSchemaDefinition(
         java.lang.String schemaLocation, java.lang.String repositoryId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#migrateRepository(
     *     int batchSize, boolean evaluateTotalCount, java.lang.String repositoryId)
     */
    public org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport
         migrateRepository(
         int batchSize, boolean evaluateTotalCount, java.lang.String repositoryId,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException,
         java.rmi.RemoteException;
         }