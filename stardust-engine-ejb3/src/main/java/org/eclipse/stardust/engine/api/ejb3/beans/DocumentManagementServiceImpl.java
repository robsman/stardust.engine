/*
 * Generated from Revision: 56243 
 */
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

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
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DocumentManagementServiceImpl extends org.eclipse.stardust.engine.api.ejb3.beans.AbstractEjb3ServiceBean implements DocumentManagementService, RemoteDocumentManagementService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocument(java.lang.String documentId)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         getDocument(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getDocument(documentId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocumentVersions(java.lang.String documentId)
    */
   public java.util.List getDocumentVersions(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getDocumentVersions(documentId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getDocuments(java.util.List documentIds)
    */
   public java.util.List getDocuments(
         java.util.List documentIds, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getDocuments(documentIds);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findDocumentsByName(java.lang.String namePattern)
    */
   public java.util.List findDocumentsByName(
         java.lang.String namePattern,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).findDocumentsByName(namePattern);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findDocuments(java.lang.String xpathQuery)
    */
   public java.util.List findDocuments(
         java.lang.String xpathQuery, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).findDocuments(xpathQuery);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#retrieveDocumentContent(java.lang.String documentId)
    */
   public byte[] retrieveDocumentContent(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).retrieveDocumentContent(documentId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#requestDocumentContentDownload(java.lang.String documentId)
    */
   public java.lang.String requestDocumentContentDownload(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).requestDocumentContentDownload(documentId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolder(java.lang.String folderId)
    */
   public org.eclipse.stardust.engine.api.runtime.Folder
         getFolder(
         java.lang.String folderId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getFolder(folderId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolder(java.lang.String folderId, int levelOfDetail)
    */
   public org.eclipse.stardust.engine.api.runtime.Folder
         getFolder(
         java.lang.String folderId, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getFolder(folderId, levelOfDetail);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getFolders(java.util.List folderIds, int levelOfDetail)
    */
   public java.util.List getFolders(
         java.util.List folderIds, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getFolders(folderIds, levelOfDetail);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findFoldersByName(java.lang.String namePattern, int levelOfDetail)
    */
   public java.util.List findFoldersByName(
         java.lang.String namePattern, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).findFoldersByName(namePattern, levelOfDetail);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#findFolders(java.lang.String xpathQuery, int levelOfDetail)
    */
   public java.util.List findFolders(
         java.lang.String xpathQuery, int levelOfDetail,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).findFolders(xpathQuery, levelOfDetail);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createDocument(java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo document)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         createDocument(
         java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo
         document, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).createDocument(folderId, document);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createDocument(java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo document, byte[] content, java.lang.String encoding)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         createDocument(
         java.lang.String folderId, org.eclipse.stardust.engine.api.runtime.DocumentInfo
         document, byte[] content, java.lang.String encoding,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).createDocument(folderId, document, content, encoding);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#versionDocument(java.lang.String documentId, java.lang.String versionLabel)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         versionDocument(
         java.lang.String documentId, java.lang.String versionLabel,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).versionDocument(documentId, versionLabel);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#versionDocument(java.lang.String documentId, java.lang.String versionComment, java.lang.String versionLabel)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         versionDocument(
         java.lang.String documentId, java.lang.String versionComment, java.lang.String
         versionLabel, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).versionDocument(documentId, versionComment, versionLabel);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeDocumentVersion(java.lang.String documentId, java.lang.String documentRevisionId)
    */
   public void removeDocumentVersion(
         java.lang.String documentId, java.lang.String documentRevisionId,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).removeDocumentVersion(documentId, documentRevisionId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#moveDocument(java.lang.String documentId, java.lang.String targetPath)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         moveDocument(
         java.lang.String documentId, java.lang.String targetPath,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).moveDocument(documentId, targetPath);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(org.eclipse.stardust.engine.api.runtime.Document document, boolean createNewRevision, java.lang.String versionLabel, boolean keepLocked)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, boolean
         createNewRevision, java.lang.String versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).updateDocument(document, createNewRevision, versionLabel, keepLocked);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(org.eclipse.stardust.engine.api.runtime.Document document, boolean createNewRevision, java.lang.String versionComment, java.lang.String versionLabel, boolean keepLocked)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, boolean
         createNewRevision, java.lang.String versionComment, java.lang.String
         versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).updateDocument(
            document, createNewRevision, versionComment, versionLabel, keepLocked);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(org.eclipse.stardust.engine.api.runtime.Document document, byte[] content, java.lang.String encoding, boolean createNewRevision, java.lang.String versionLabel, boolean keepLocked)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, byte[] content,
         java.lang.String encoding, boolean createNewRevision, java.lang.String
         versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).updateDocument(
            document, content, encoding, createNewRevision, versionLabel, keepLocked);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateDocument(org.eclipse.stardust.engine.api.runtime.Document document, byte[] content, java.lang.String encoding, boolean createNewRevision, java.lang.String versionComment, java.lang.String versionLabel, boolean keepLocked)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         updateDocument(
         org.eclipse.stardust.engine.api.runtime.Document document, byte[] content,
         java.lang.String encoding, boolean createNewRevision, java.lang.String
         versionComment, java.lang.String versionLabel, boolean keepLocked,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).updateDocument(
            document, content, encoding, createNewRevision, versionComment, versionLabel,
            keepLocked);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#requestDocumentContentUpload(java.lang.String documentId)
    */
   public java.lang.String requestDocumentContentUpload(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).requestDocumentContentUpload(documentId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeDocument(java.lang.String documentId)
    */
   public void removeDocument(
         java.lang.String documentId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).removeDocument(documentId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#createFolder(java.lang.String parentFolderId, org.eclipse.stardust.engine.api.runtime.FolderInfo folder)
    */
   public org.eclipse.stardust.engine.api.runtime.Folder
         createFolder(
         java.lang.String parentFolderId,
         org.eclipse.stardust.engine.api.runtime.FolderInfo folder,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).createFolder(parentFolderId, folder);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#updateFolder(org.eclipse.stardust.engine.api.runtime.Folder folder)
    */
   public org.eclipse.stardust.engine.api.runtime.Folder
         updateFolder(
         org.eclipse.stardust.engine.api.runtime.Folder folder,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).updateFolder(folder);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#removeFolder(java.lang.String folderId, boolean recursive)
    */
   public void removeFolder(
         java.lang.String folderId, boolean recursive,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).removeFolder(folderId, recursive);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getPrivileges(java.lang.String resourceId)
    */
   public java.util.Set<org.eclipse.stardust.engine.api.runtime.Privilege>
         getPrivileges(
         java.lang.String resourceId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getPrivileges(resourceId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getEffectivePolicies(java.lang.String resourceId)
    */
   public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getEffectivePolicies(
         java.lang.String resourceId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getEffectivePolicies(resourceId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getPolicies(java.lang.String resourceId)
    */
   public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getPolicies(
         java.lang.String resourceId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getPolicies(resourceId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getApplicablePolicies(java.lang.String resourceId)
    */
   public
         java.util.Set<org.eclipse.stardust.engine.api.runtime.AccessControlPolicy>
         getApplicablePolicies(
         java.lang.String resourceId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getApplicablePolicies(resourceId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#setPolicy(java.lang.String resourceId, org.eclipse.stardust.engine.api.runtime.AccessControlPolicy policy)
    */
   public void setPolicy(
         java.lang.String resourceId,
         org.eclipse.stardust.engine.api.runtime.AccessControlPolicy policy,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).setPolicy(resourceId, policy);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#migrateRepository(int batchSize, boolean evaluateTotalCount)
    */
   public org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport
         migrateRepository(
         int batchSize, boolean evaluateTotalCount,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).migrateRepository(batchSize, evaluateTotalCount);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.DocumentManagementService#getSchemaDefinition(java.lang.String schemaLocation)
    */
   public byte[] getSchemaDefinition(
         java.lang.String schemaLocation,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return
            ((org.eclipse.stardust.engine.api.runtime.DocumentManagementService)
            service).getSchemaDefinition(schemaLocation);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

	public DocumentManagementServiceImpl()
	{
      this.serviceType=org.eclipse.stardust.engine.api.runtime.DocumentManagementService.class;
      this.serviceTypeImpl=org.eclipse.stardust.engine.core.runtime.beans.DocumentManagementServiceImpl.class;
	}
}