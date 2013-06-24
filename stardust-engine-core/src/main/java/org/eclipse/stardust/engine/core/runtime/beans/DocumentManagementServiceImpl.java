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

import static org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.fromVfs;
import static org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.fromVfsDocumentList;
import static org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.fromVfsFolderList;
import static org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.fromVfsPolicies;
import static org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.fromVfsPrivileges;
import static org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.hasValidPartitionPrefix;
import static org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.isSecurityEnabled;
import static org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.toVfs;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Modules;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils;
import org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils.AccessMode;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;
import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet;
import org.eclipse.stardust.engine.core.extensions.ExtensionService;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.spi.dms.IDmsResourceSyncListener;
import org.eclipse.stardust.engine.core.thirdparty.encoding.ISO9075;
import org.eclipse.stardust.engine.extensions.dms.data.AuditTrailUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.IPrivilegeAdapter;
import org.eclipse.stardust.engine.extensions.dms.data.VirtualFolderKey;
import org.eclipse.stardust.vfs.IAccessControlEntry;
import org.eclipse.stardust.vfs.IAccessControlEntry.EntryType;
import org.eclipse.stardust.vfs.IAccessControlPolicy;
import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IFolder;
import org.eclipse.stardust.vfs.IMigrationReport;
import org.eclipse.stardust.vfs.IPrivilege;
import org.eclipse.stardust.vfs.MetaDataLocation;
import org.eclipse.stardust.vfs.RepositoryOperationFailedException;
import org.eclipse.stardust.vfs.VfsUtils;
import org.eclipse.stardust.vfs.impl.jcr.AuthorizableOrganizationDetails;
import org.eclipse.stardust.vfs.impl.jcr.JcrDocumentRepositoryService;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsAccessControlEntry;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsAccessControlPolicy;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsPrincipal;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;
import org.eclipse.stardust.vfs.jcr.spring.JcrSpringSessionFactory;


/**
 * @author rsauer
 * @version $Revision$
 */
public class DocumentManagementServiceImpl
      implements Serializable, DocumentManagementService
{
   static final long serialVersionUID = 1L;

   public static final String PROPERTY_CREATE_DMS_RESOURCE_IN_NESTED_TX = "Stardust.DocumentManagement.Resource.Create.inNestedTx";

   private static final Logger trace = LogManager.getLogger(DocumentManagementServiceImpl.class);

   private transient VirtualFolderHandler virtualFolderHandler;

   private transient IDocumentRepositoryService adminVfs;
   
   private transient List<IDmsResourceSyncListener> resourceSyncListeners;

   public DocumentManagementServiceImpl()
   {
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Document retrieval.
   // /////////////////////////////////////////////////////////////////////////////////////

   public Document getDocument(final String docId)
         throws DocumentManagementServiceException
   {
      return (Document) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String docIdWithPrefix = decodeResourceName(docId);

            IFile file = vfs.getFile(docIdWithPrefix);

            if (file == null
                  || hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                        AccessMode.Read))
            {
               return fromVfs(file, getPartitionPrefix());
            }
            else
            {
               return null;
            }
         }
      });
   }

   public List/* <Document> */getDocumentVersions(final String docId)
         throws DocumentManagementServiceException
   {
      return (List/* <Document> */) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String docIdWithPrefix = decodeResourceName(docId);

            IFile file = vfs.getFile(docIdWithPrefix);
            if (file == null
                  || hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                        AccessMode.Read))
            {
               return fromVfsDocumentList(vfs.getFileVersions(docIdWithPrefix),
                     getPartitionPrefix());
            }
            else
            {
               throw new DocumentManagementServiceException(
                     BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(docId));
            }
         }
      });
   }

   public List/* <Document> */getDocuments(final List/* <String> */docIds)
         throws DocumentManagementServiceException
   {
      return (List/* <Document> */) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            List<String> prefixedDocIds = addPrefixes(docIds);

            return fromVfsDocumentList(vfs.getFiles(prefixedDocIds), getPartitionPrefix());
         }
      });
   }

   public List/* <Document> */findDocumentsByName(final String namePattern)
         throws DocumentManagementServiceException
   {
      final String expression = createXPathQueryByName(namePattern, getPartitionPrefix());

      return (List/* <Document> */) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            return fromVfsDocumentList(vfs.findFiles(expression), getPartitionPrefix());
         }
      });
   }

   private String createXPathQueryByName(String namePattern, String prefixPath)
   {
      MetaDataLocation metaDataLocation = (MetaDataLocation) adaptVfsCall(new IVfsOperationCallback()
      {
         public MetaDataLocation withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            return vfs.getMetaDataLocation();
         }
      });

      String vfsPrefix = "vfs:";
      String vfsMetaData = vfsPrefix + VfsUtils.VFS_META_DATA;
      String prpVfsName = vfsPrefix + VfsUtils.VFS_NAME;
      if (prefixPath == null)
      {
         prefixPath = "";
   }

      String xpathQuery;
      if (MetaDataLocation.LOCAL.equals(metaDataLocation))
      {
         xpathQuery = "/jcr:root" + ISO9075.encodePath(prefixPath) + "//*[jcr:like("
               + vfsMetaData + "/" + prpVfsName + ", '" + namePattern + "')]";
      }
      else
      {
         xpathQuery = "/jcr:root/" + vfsMetaData + "/*[jcr:like(" + prpVfsName + ", '"
               + namePattern + "')]";
      }
      return xpathQuery;
   }

   public List/* <Document> */findDocuments(final String expression)
         throws DocumentManagementServiceException
   {
      return (List/* <Document> */) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            return fromVfsDocumentList(vfs.findFiles(expression), getPartitionPrefix());
         }
      });
   }

   public byte[] retrieveDocumentContent(final String docId)
         throws DocumentManagementServiceException
   {
      return (byte[]) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String docIdWithPrefix = decodeResourceName(docId);

            IFile file = vfs.getFile(docIdWithPrefix);

            if (file == null
                  || hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                        AccessMode.Read))
            {
               return vfs.retrieveFileContent(docIdWithPrefix);
            }
            else
            {
               return null;
            }
         }
      });
   }

   public String requestDocumentContentDownload(final String docId)
         throws DocumentManagementServiceException
   {
      return (String) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String docIdWithPrefix = decodeResourceName(docId);

            IFile file = vfs.getFile(docIdWithPrefix);

            if (null != file)
            {
               if (hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                     AccessMode.Read))
               {
                  // TODO encode sessionId/fileId
                  return DmsContentServlet.encodeDmsServletToken(docIdWithPrefix,
                        DmsContentServlet.OP_DOWNLOAD, SecurityProperties.getUserOID(),
                        System.currentTimeMillis());
               }
               else
               {
                  throw new ObjectNotFoundException(
                        BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(docId));
               }
            }
            else
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(docId));
            }
         }
      });
   }

   public Folder getFolder(final String folderId)
         throws DocumentManagementServiceException
   {
      return (Folder) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {

            String folderIdWithPrefix = decodeResourceName(folderId);

            IFolder folder = vfs.getFolder(folderIdWithPrefix);

            if (folder == null)
            {
               return substituteVirtualFolder(folderIdWithPrefix);
            }
            else if (hasValidPartitionPrefix(folder.getPath(), getPartitionPrefix(),
                  AccessMode.Read))
            {
               return fromVfs(folder, getPartitionPrefix());
            }
            return null;
         }
      });
   }

   public Folder getFolder(final String folderId, final int levelOfDetail)
         throws DocumentManagementServiceException
   {
      return (Folder) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String folderIdWithPrefix = decodeResourceName(folderId);

            IFolder folder = vfs.getFolder(folderIdWithPrefix, levelOfDetail);

            if (folder == null)
            {
               return substituteVirtualFolder(folderIdWithPrefix);
            }
            else if (hasValidPartitionPrefix(folder.getPath(), getPartitionPrefix(),
                  AccessMode.Read))
            {
               return fromVfs(folder, getPartitionPrefix());
            }
            return null;
         }
      });
   }

   public List/* <Folder> */getFolders(final List/* <String> */folderIds,
         final int levelOfDetail) throws DocumentManagementServiceException
   {
      return (List/* <Folder> */) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            List<String> prefixedFolderIds = addPrefixes(folderIds);

            return fromVfsFolderList(vfs.getFolders(prefixedFolderIds, levelOfDetail),
                  getPartitionPrefix());
         }
      });
   }

   public List/* <Folder> */findFoldersByName(final String namePattern,
         final int levelOfDetail) throws DocumentManagementServiceException
   {
      final String expression = createXPathQueryByName(namePattern, getPartitionPrefix());

      return (List/* <Folder> */) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            return fromVfsFolderList(vfs.findFolders(expression, levelOfDetail),
                  getPartitionPrefix());
         }
      });
   }

   public List/* <Folder> */findFolders(final String expression, final int levelOfDetail)
         throws DocumentManagementServiceException
   {
      return (List/* <Folder> */) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            return fromVfsFolderList(vfs.findFolders(expression, levelOfDetail),
                  getPartitionPrefix());
         }
      });
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Document manipulation.
   // /////////////////////////////////////////////////////////////////////////////////////

   public Document createDocument(final String folderId, final DocumentInfo document)
         throws DocumentManagementServiceException
   {
      return (Document) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            return createDocument(folderId, document, null, null);
         }
      });
   }

   public Document createDocument(final String folderId, final DocumentInfo document,
         final byte[] content, final String encoding)
         throws DocumentManagementServiceException
   {      
      return (Document) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(final IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            final String folderIdWithPrefix = decodeResourceName(folderId);

            IFolder folder = vfs.getFolder(folderIdWithPrefix, IFolder.LOD_NO_MEMBERS);

            if (folder == null)
            {
               if (ensureVirtualFolderExists(folderIdWithPrefix))
               {
                  checkValidDocumentType(document);

                  if (!isCreateInNextedTxEnabled())
                  {
                     return fromVfs(vfs.createFile(folderIdWithPrefix, toVfs(document),
                           content, encoding), getPartitionPrefix());
                  }
                  else
                  {
                     return getVirtualFolderHandler().runIsolateAction(
                           new Action<Document>()
                           {
                              public Document execute()
                              {
                                 return fromVfs(vfs.createFile(folderIdWithPrefix,
                                       toVfs(document), content, encoding),
                                       getPartitionPrefix());
                              }
                           });
                  }
               }
               else
               {
                  throw new DocumentManagementServiceException(
                        BpmRuntimeError.DMS_UNKNOWN_FOLDER_ID.raise(folderId));
               }
            }
            else if (hasValidPartitionPrefix(folder.getPath(), getPartitionPrefix(),
                  AccessMode.Write))
            {
               checkValidDocumentType(document);

               return fromVfs(vfs.createFile(folderIdWithPrefix, toVfs(document),
                     content, encoding), getPartitionPrefix());
            }
            else
            {
               throw new DocumentManagementServiceException(
                     BpmRuntimeError.DMS_UNKNOWN_FOLDER_ID.raise(folderId));
            }

         }
      });
   }

   public Document versionDocument(final String docId, final String versionLabel)
         throws DocumentManagementServiceException
   {
      return versionDocument(docId, null, versionLabel);
   }

   public Document versionDocument(final String docId, final String versionComment, final String versionLabel)
         throws DocumentManagementServiceException
   {      
      return (Document) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String documentIdWithPrefix = decodeResourceName(docId);

            IFile file = vfs.getFile(documentIdWithPrefix);

            if (file == null
                  || hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                        AccessMode.Write))
            {
               return fromVfs(vfs.createFileVersion(documentIdWithPrefix, versionComment, versionLabel,
                     false), getPartitionPrefix());
            }
            else
            {
               return null;
            }
         }
      });
   }

   public Document moveDocument(final String documentId, final String targetPath)
         throws DocumentManagementServiceException
   {      
      return (Document) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String documentIdWithPrefix = decodeResourceName(documentId);
            String pathWithPrefix = decodeResourceName(targetPath);



            IFile file = vfs.getFile(documentIdWithPrefix);

            if (file != null)
            {
               if ( !pathWithPrefix.endsWith("/"))
               {
                  pathWithPrefix += "/";
               }
               pathWithPrefix += file.getName();
            }

            if (file == null
                  || hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                        AccessMode.Write))
            {
               return fromVfs(vfs.moveFile(documentIdWithPrefix, pathWithPrefix, null),
                     getPartitionPrefix());
            }
            else
            {
               return null;
            }
         }
      });
   }

   public void removeDocumentVersion(final String documentId,
         final String documentRevisionId) throws DocumentManagementServiceException
   {
      adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String documentIdWithPrefix = decodeResourceName(documentId);

            IFile file = vfs.getFile(documentIdWithPrefix);

            if (file == null)
            {
               throw new DocumentManagementServiceException(
                     BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(documentId));
            }
            else if (hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                  AccessMode.Write))
            {
               try
               {
                  vfs.removeFileVersion(documentIdWithPrefix, documentRevisionId);
               }
               catch (RepositoryOperationFailedException e)
               {
                  if (e.getCause() instanceof ItemNotFoundException)
                  {
                     throw new DocumentManagementServiceException(
                           BpmRuntimeError.DMS_UNKNOWN_FILE_VERSION_ID.raise());
                  }
                  else if (e.getCause() instanceof ReferentialIntegrityException)
                  {
                     throw new DocumentManagementServiceException(
                           BpmRuntimeError.DMS_CANNOT_REMOVE_ROOT_FILE_VERSION.raise());
                  }
                  else
                  {
                     throw e;
                  }
               }
            }
            return null;
         }
      });
   }

   public Document lockDocument(final String docId)
         throws DocumentManagementServiceException
   {
      return (Document) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String documentIdWithPrefix = decodeResourceName(docId);

            IFile file = vfs.getFile(documentIdWithPrefix);

            if (file == null
                  || hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                        AccessMode.Write))
            {
               return fromVfs(vfs.lockFile(documentIdWithPrefix), getPartitionPrefix());
            }
            else
            {
               return null;
            }
         }
      });
   }

   public Document unlockDocument(final String docId)
         throws DocumentManagementServiceException
   {
      return (Document) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String documentIdWithPrefix = decodeResourceName(docId);

            IFile file = vfs.getFile(documentIdWithPrefix);

            if (file == null
                  || hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                        AccessMode.Write))
            {
               return fromVfs(vfs.unlockFile(documentIdWithPrefix), getPartitionPrefix());
            }
            else
            {
               return null;
            }

         }
      });
   }

   public Document updateDocument(final Document document,
         final boolean createNewRevision, final String versionLabel,
         final boolean keepLocked) throws DocumentManagementServiceException
   {
      return updateDocument(document, createNewRevision, null, versionLabel, keepLocked);
   }

   public Document updateDocument(final Document document,
         final boolean createNewRevision, final String versionComment, final String versionLabel,
         final boolean keepLocked) throws DocumentManagementServiceException
   {
      return (Document) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            checkValidDocumentType(document);

            IFile oldDoc = vfs.getFile(document.getId());

            if (oldDoc == null
                  || hasValidPartitionPrefix(oldDoc.getPath(), getPartitionPrefix(),
                        AccessMode.Write))
            {
               IFile doc = toVfs(document, getPartitionPrefix());
               IFile vfsFile = vfs.updateFile(doc, createNewRevision, versionComment, versionLabel, keepLocked);
              
               Document updatedDocument = fromVfs(vfsFile, getPartitionPrefix());
               Document oldDocument = fromVfs(oldDoc, getPartitionPrefix());
               notifyDocumentUpdated(oldDocument, updatedDocument);
               
               return updatedDocument;
            }
            else
            {
               throw new DocumentManagementServiceException(
                     BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(document.getId()));
            }
         }
      });
   }

   public Document updateDocument(final Document document, final byte[] content,
         final String encoding, final boolean createNewRevision,
         final String versionLabel, final boolean keepLocked)
         throws DocumentManagementServiceException
   {
      return updateDocument(document, content, encoding, createNewRevision, null, versionLabel, keepLocked);
   }

   public Document updateDocument(final Document document, final byte[] content,
         final String encoding, final boolean createNewRevision,
         final String versionComment, final String versionLabel, final boolean keepLocked)
         throws DocumentManagementServiceException
   {
      return (Document) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            checkValidDocumentType(document);

            IFile oldDoc = vfs.getFile(document.getId());

            if (oldDoc == null
                  || hasValidPartitionPrefix(oldDoc.getPath(), getPartitionPrefix(),
                        AccessMode.Write))
            {
               IFile doc = toVfs(document, getPartitionPrefix());
               IFile vfsFile = vfs.updateFile(doc, content, encoding, createNewRevision,
                     versionComment, versionLabel, keepLocked);
               
               Document updatedDocument = fromVfs(vfsFile, getPartitionPrefix());
               Document oldDocument = fromVfs(oldDoc, getPartitionPrefix());
               notifyDocumentUpdated(oldDocument, updatedDocument);
               
               return updatedDocument;
            }
            else
            {
               throw new DocumentManagementServiceException(
                     BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(document.getId()));
            }
         }
      });
   }

   public String requestDocumentContentUpload(final String docId)
         throws DocumentManagementServiceException
   {
      return (String) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String documentIdWithPrefix = decodeResourceName(docId);
            IFile file = vfs.getFile(documentIdWithPrefix);

            if (null != file)
            {
               // TODO encode sessionId/fileId

               if (hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                     AccessMode.Write))
               {
                  return DmsContentServlet.encodeDmsServletToken(documentIdWithPrefix,
                        DmsContentServlet.OP_UPLOAD, SecurityProperties.getUserOID(),
                        System.currentTimeMillis());
               }
               else
               {
                  throw new ObjectNotFoundException(
                        BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(docId));
               }
            }
            else
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(documentIdWithPrefix));
            }
         }
      });
   }

   public void removeDocument(final String docId)
         throws DocumentManagementServiceException
   {
      adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String docIdWithPrefix = decodeResourceName(docId);
            IFile file = vfs.getFile(docIdWithPrefix);

            if (file == null
                  || hasValidPartitionPrefix(file.getPath(), getPartitionPrefix(),
                        AccessMode.Write))
            {
               vfs.removeFile(docIdWithPrefix);
               
               if (file != null)
               {
                  Document oldDocument = fromVfs(file, getPartitionPrefix());
                  notifyDocumentUpdated(oldDocument, null);
               }
            }
            return null;
         }
      });
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Folder manipulation.
   // /////////////////////////////////////////////////////////////////////////////////////

   public Folder createFolder(final String parentFolderId, final FolderInfo folder)
         throws DocumentManagementServiceException
   {
      return (Folder) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(final IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            final String parentFolderIdWithPrefix = decodeResourceName(parentFolderId);

            IFolder parentFolder = vfs.getFolder(parentFolderIdWithPrefix,
                  IFolder.LOD_NO_MEMBERS);

            if (parentFolder == null)
            {
               // folder does not exist, maybe it is a virtual folder
               ensureVirtualFolderExists(parentFolderIdWithPrefix);

               if (!isCreateInNextedTxEnabled())
               {
                  return fromVfs(
                        vfs.createFolder(parentFolderIdWithPrefix, toVfs(folder)),
                        getPartitionPrefix());

               }
               else
               {
                  return getVirtualFolderHandler().runIsolateAction(new Action<Folder>()
                  {
                     public Folder execute()
                     {
                        return fromVfs(
                              vfs.createFolder(parentFolderIdWithPrefix, toVfs(folder)),
                              getPartitionPrefix());
                     }
                  });
               }
            }
            else if (hasValidPartitionPrefix(parentFolder.getPath(),
                  getPartitionPrefix(), AccessMode.Write))
            {
               // parent folder exists, but maybe the to be created folder is virtual too
               Folder substituteVirtualFolder = substituteVirtualFolder(parentFolder.getPath()
                     + "/" + folder.getName());

               if (substituteVirtualFolder != null)
               {
                  return substituteVirtualFolder;
               }
               else
               {
                  return fromVfs(
                        vfs.createFolder(parentFolderIdWithPrefix, toVfs(folder)),
                        getPartitionPrefix());
               }
            }
            else
            {
               return null;
            }
         }
      });
   }

   public Folder updateFolder(final Folder folder)
         throws DocumentManagementServiceException
   {
      return (Folder) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            IFolder oldFolder = vfs.getFolder(folder.getId());
            if (hasValidPartitionPrefix(oldFolder.getPath(), getPartitionPrefix(),
                  AccessMode.Write))
            {
               IFolder vfsFolder = toVfs(folder, getPartitionPrefix());
               return fromVfs(vfs.updateFolder(vfsFolder), getPartitionPrefix());
            }
            else
            {
               throw new DocumentManagementServiceException(
                     BpmRuntimeError.DMS_UNKNOWN_FOLDER_ID.raise(folder.getId()));
            }
         }
      });
   }

   public void removeFolder(final String folderId, final boolean recursively)
         throws DocumentManagementServiceException
   {
      adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String folderIdWithPrefix = decodeResourceName(folderId);
            IFolder targetFolder = vfs.getFolder(folderIdWithPrefix);

            if (targetFolder == null
                  || hasValidPartitionPrefix(targetFolder.getPath(),
                        getPartitionPrefix(), AccessMode.Write))
            {
               vfs.removeFolder(folderIdWithPrefix, recursively);
            }
            return null;
         }
      });
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Security.
   // /////////////////////////////////////////////////////////////////////////////////////

   public Set<Privilege> getPrivileges(final String resourceId)
         throws DocumentManagementServiceException
   {
      return (Set<Privilege>) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            String decodedResourceId = decodeResourceName(resourceId);
            String virtualFolderPath;

            if (isVirtualFolderPath(decodedResourceId)
                  && vfs.getFolder(decodedResourceId, Folder.LOD_NO_MEMBERS) == null)
            {
               virtualFolderPath = getParentOfVirtualFolder(decodedResourceId);

               Set<IPrivilege> parentNodePrivileges = vfs.getPrivileges(virtualFolderPath);

               addVirtualFolderPrivileges(parentNodePrivileges, decodedResourceId);

               return fromVfsPrivileges(parentNodePrivileges);
            }
            else
            {
               return fromVfsPrivileges(vfs.getPrivileges(decodedResourceId));
            }
         }
      });
   }

   public Set<AccessControlPolicy> getEffectivePolicies(final String resourceId)
         throws DocumentManagementServiceException
   {
      return (Set<AccessControlPolicy>) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            Set<IAccessControlPolicy> ret;
            String decodedResourceId = decodeResourceName(resourceId);
            if (isVirtualFolderPath(decodedResourceId)
                  && vfs.getFolder(decodedResourceId, Folder.LOD_NO_MEMBERS) == null)
            {
               IAccessControlPolicy virtualFolderPolicy = getVirtualFolderPolicy(decodedResourceId);
               if (hasPrivilege(DmsPrivilege.READ_ACL_PRIVILEGE,
                     getParentOfVirtualFolder(decodedResourceId), virtualFolderPolicy,
                     vfs))
               {
                  ret = CollectionUtils.newSet();
                  ret.add(createVirtualEffectivePolicy());

                  if (virtualFolderPolicy != null)
                  {
                     ret.add(virtualFolderPolicy);
                  }
               }
               else
               {
                  throw new DocumentManagementServiceException(
                        BpmRuntimeError.DMS_GENERIC_ERROR.raise(),
                        new AccessDeniedException("Access denied at " + decodedResourceId));
               }
            }
            else
            {
               ret = vfs.getEffectivePolicies(decodedResourceId);
            }
            return fromVfsPolicies(ret);
         }
      });
   }

   public Set<AccessControlPolicy> getPolicies(final String resourceId)
         throws DocumentManagementServiceException
   {
      return (Set<AccessControlPolicy>) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            Set<IAccessControlPolicy> ret;
            String decodedResourceId = decodeResourceName(resourceId);
            if (isVirtualFolderPath(decodedResourceId)
                  && vfs.getFolder(decodedResourceId, Folder.LOD_NO_MEMBERS) == null)
            {
               IAccessControlPolicy virtualFolderPolicy = getVirtualFolderPolicy(decodedResourceId);
               if (hasPrivilege(DmsPrivilege.READ_ACL_PRIVILEGE,
                     getParentOfVirtualFolder(decodedResourceId), virtualFolderPolicy,
                     vfs))
               {
                  if (virtualFolderPolicy != null)
                  {
                     ret = Collections.singleton(virtualFolderPolicy);
                  }
                  else
                  {
                     ret = CollectionUtils.newSet();
                  }
               }
               else
               {
                  throw new DocumentManagementServiceException(
                        BpmRuntimeError.DMS_GENERIC_ERROR.raise(),
                        new AccessDeniedException("Access denied at " + decodedResourceId));
               }
            }
            else
            {
               ret = vfs.getPolicies(decodedResourceId);
            }
            return fromVfsPolicies(ret);
         }
      });
   }

   public Set<AccessControlPolicy> getApplicablePolicies(final String resourceId)
         throws DocumentManagementServiceException
   {
      return (Set<AccessControlPolicy>) adaptVfsCall(new IVfsOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
           Set<IAccessControlPolicy> ret;
            String decodedResourceId = decodeResourceName(resourceId);
            if (isVirtualFolderPath(decodedResourceId)
                  && vfs.getFolder(decodedResourceId, Folder.LOD_NO_MEMBERS) == null)
            {
               IAccessControlPolicy virtualFolderPolicy = getVirtualFolderPolicy(decodedResourceId);
               if (hasPrivilege(DmsPrivilege.READ_ACL_PRIVILEGE,
                     getParentOfVirtualFolder(decodedResourceId), virtualFolderPolicy,
                     vfs))
               {
                  if (virtualFolderPolicy != null)
                  {
                     ret = CollectionUtils.newSet();
                  }
                  else
                  {
                     ret = Collections.singleton(createVirtualApplicablePolicy());
                  }
               }
               else
               {
                  throw new DocumentManagementServiceException(
                        BpmRuntimeError.DMS_GENERIC_ERROR.raise(),
                        new AccessDeniedException("Access denied at " + decodedResourceId));
               }
            }
            else
            {
               ret = vfs.getApplicablePolicies(decodedResourceId);
            }
            return fromVfsPolicies(ret);
         }

      });
   }

   public void setPolicy(final String resourceId, final AccessControlPolicy policy)
         throws DocumentManagementServiceException
   {
      adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(final IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            final String decodedResourceId = decodeResourceName(resourceId);
            if (isVirtualFolderPath(decodedResourceId)
                  && vfs.getFolder(decodedResourceId, Folder.LOD_NO_MEMBERS) == null)
            {
               ensureVirtualFolderExists(decodedResourceId);

               if (!isCreateInNextedTxEnabled())
               {
                  vfs.setPolicy(decodedResourceId, toVfs(policy));
               }
               else
               {
                  getVirtualFolderHandler().runIsolateAction(new Action<Object>()
                  {
                     public Folder execute()
                     {
                        vfs.setPolicy(decodedResourceId, toVfs(policy));
                        return null;
                     }
                  });
               }
            }
            else
            {
               vfs.setPolicy(decodedResourceId, toVfs(policy));
            }
            return null;
         }
      });
   }

   public RepositoryMigrationReport migrateRepository(final int batchSize,
         final boolean evaluateTotalCount)
   {
      IUser user = SecurityProperties.getUser();
      if (user == null || !user.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE))
      {
         throw new DocumentManagementServiceException(
               BpmRuntimeError.DMS_SECURITY_ERROR_ADMIN_REQUIRED.raise());
      }

      return (RepositoryMigrationReport) adaptVfsCall(new IVfsWriteOperationCallback()
      {
         public Object withVfs(IDocumentRepositoryService vfs)
               throws RepositoryOperationFailedException
         {
            IMigrationReport migrationReport = vfs.migrateRepository(batchSize,
                  evaluateTotalCount);

            RepositoryMigrationReport migrationReportDetails = handleEngineMigration(batchSize, evaluateTotalCount, migrationReport, vfs);

            return migrationReportDetails;
         }

      });
   }

   public byte[] getSchemaDefinition(String schemaLocation)
   throws ObjectNotFoundException
   {
      String documentPath = DocumentTypeUtils.getXsdDocumentPath(schemaLocation);

      byte[] xsdSchemaBytes = retrieveDocumentContent(documentPath);

      if (xsdSchemaBytes != null)
      {
         return xsdSchemaBytes;
      }
      else
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.DMS_UNKNOWN_FILE_ID.raise(documentPath));
      }
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Utility methods.
   // /////////////////////////////////////////////////////////////////////////////////////

   private Object adaptVfsCall(IVfsOperationCallback vfsOperation)
   {
      
      if (vfsOperation instanceof IVfsWriteOperationCallback)
      {
         // prevent write in archive mode
         if (Parameters.instance().getBoolean(
               Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
         {
            throw new DocumentManagementServiceException(
                  BpmRuntimeError.DMS_SECURITY_ERROR_WRITE_IN_ARCHIVE_MODE.raise());
         }
      }
      
      try
      {
         ExtensionService.initializeModuleExtensions(Modules.DMS);

         // TODO support multiple repositories
         IDocumentRepositoryService vfs = getVfs();

         return vfsOperation.withVfs(vfs);
      }
      catch (PublicException e)
      {
         // rethrow already wrapped exceptions
         throw e;
      }
      catch (RepositoryOperationFailedException e)
      {
         Throwable tr = e.getCause();
         // TODO error codes should be also available in jcr-vfs
         BpmRuntimeError error = BpmRuntimeError.DMS_GENERIC_ERROR.raise();
         if (tr instanceof ItemExistsException)
         {
            error = BpmRuntimeError.DMS_ITEM_EXISTS.raise();
         }
         // special case for jackrabbit, thrown if Document.name contains '*' or other unsupported characters
         else if (tr instanceof RepositoryException && tr.getMessage().startsWith("Failed to resolve path"))
         {
            error = BpmRuntimeError.DMS_FAILED_PATH_RESOLVE.raise(tr.getCause().getMessage());
         }
         else if (tr instanceof PathNotFoundException)
         {
            error = BpmRuntimeError.DMS_FAILED_PATH_RESOLVE.raise(tr.getMessage());
         }

         throw new DocumentManagementServiceException(error, e);
      }
      catch (Exception e)
      {
         // special case for jackrabbit, thrown if Document.name contains '/'
         if (e instanceof IllegalArgumentException
               && e.getMessage().startsWith("relPath is not a relative path"))
         {
            BpmRuntimeError error = BpmRuntimeError.DMS_FAILED_PATH_RESOLVE.raise(e.getMessage());
            throw new DocumentManagementServiceException(error, e);
         }

         throw new DocumentManagementServiceException(
               BpmRuntimeError.DMS_GENERIC_ERROR.raise(), e);
      }
   }

   public interface IVfsOperationCallback
   {
      Object withVfs(IDocumentRepositoryService vfs)
            throws RepositoryOperationFailedException;
   }
   
   /**
    * Should be used to identify write operations.
    */
   public interface IVfsWriteOperationCallback extends IVfsOperationCallback
   {
      Object withVfs(IDocumentRepositoryService vfs)
      throws RepositoryOperationFailedException;
   }

   protected static String getPartitionPrefix()
   {
      return DocumentRepositoryFolderNames.REPOSITORY_ROOT_FOLDER
            + DocumentRepositoryFolderNames.PARTITIONS_FOLDER
            + SecurityProperties.getPartition().getId();
   }

   protected RepositoryMigrationReport handleEngineMigration(int batchSize, boolean evaluateTotalCount, IMigrationReport migrationReport,
         IDocumentRepositoryService vfs)
   {
      return EngineRepositoryMigrationManager.handleEngineMigration(batchSize, evaluateTotalCount, migrationReport, vfs, getPartitionPrefix());
   }
   
   protected boolean isCreateInNextedTxEnabled()
   {
      return Parameters.instance().getBoolean(
            PROPERTY_CREATE_DMS_RESOURCE_IN_NESTED_TX, false);
   }

   private List<String> addPrefixes(List<String> list)
   {
      List<String> ret = new ArrayList<String>();

      for (String string : list)
      {
         ret.add(decodeResourceName(string));
      }

      return ret;
   }

   private IDocumentRepositoryService getVfs()
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      IDocumentRepositoryService vfs = rtEnv.getDocumentRepositoryService();
      if (vfs == null)
      {
         throw new DocumentManagementServiceException(
               BpmRuntimeError.DMS_FILE_STORE_UNAVAILABLE.raise());
      }
      return vfs;
   }

   private VirtualFolderHandler getVirtualFolderHandler()
   {
      if (virtualFolderHandler == null)
      {
         virtualFolderHandler = new VirtualFolderHandler();
      }
      return virtualFolderHandler;
   }
   
   private List<IDmsResourceSyncListener> getResourceSyncListeners()
   {
      if (resourceSyncListeners == null)
      {
         resourceSyncListeners = CollectionUtils.newArrayList();
   
         List factories = ExtensionProviderUtils.getExtensionProviders(IDmsResourceSyncListener.Factory.class);
         for (int i = 0; i < factories.size(); ++i)
         {
            IDmsResourceSyncListener.Factory factory = (IDmsResourceSyncListener.Factory) factories.get(i);
   
            IDmsResourceSyncListener resourceSyncListener = factory.getListener();
            if (null != resourceSyncListener)
            {
               resourceSyncListeners.add(resourceSyncListener);
            }
         }
      }
      return resourceSyncListeners;
   }

   private void notifyDocumentUpdated(Document oldDocument, Document newDocument)
   {
      List<IDmsResourceSyncListener> listeners = getResourceSyncListeners();
      if (listeners != null)
      {
         for (IDmsResourceSyncListener iResourceSyncListener : listeners)
         {
            iResourceSyncListener.documentChanged(oldDocument, newDocument);
         }
      }
   }
   
   private void notifyFolderUpdated(Folder oldFolder, Folder newFolder)
   {
      List<IDmsResourceSyncListener> listeners = getResourceSyncListeners();
      if (listeners != null)
      {
         for (IDmsResourceSyncListener iResourceSyncListener : listeners)
         {
            iResourceSyncListener.folderChanged(oldFolder, newFolder);
         }
      }
   }
   
   private IDocumentRepositoryService getAdminVfs()
   {
      if (adminVfs == null)
      {
         adminVfs = new AdminJcrDocumentRepositoryService();
      }
      return adminVfs;
   }

   private Session createJcrAdminSession() throws RepositoryException
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      IDocumentRepositoryService documentRepositoryService = rtEnv.getDocumentRepositoryService();

      Session session = null;
      if (documentRepositoryService != null)
      {
         if (documentRepositoryService instanceof JcrDocumentRepositoryService)
         {
            ISessionFactory sf = ((JcrDocumentRepositoryService) documentRepositoryService).getSessionFactory();
            if (sf instanceof JcrSpringSessionFactory)
            {
               Repository repository = ((JcrSpringSessionFactory) sf).getRepository();
               session = repository.login(createAdminCredentials());
            }
            else if (sf instanceof EjbDocumentRepositoryService)
            {
               Repository repository = ((EjbDocumentRepositoryService) sf).getRepository();
               session = repository.login(createAdminCredentials());
            }
         }
      }
      else
      {
         trace.warn("IDocumentRepositoryService not initialized.");
      }
      if (session == null)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Creation of javax.jcr.Session via createSimpleCredentialsJcrSession failed.");
         }
      }
      return session;
   }

   private Credentials createAdminCredentials()
   {
      // JCR user name does not matter.
      // Administration privileges are granted by the predefined group "administrators"
      String jcrUser = "admin";

      SimpleCredentials credentials = new SimpleCredentials(jcrUser,
            "pw-not-implemented".toCharArray());

      // add administrators group
      // org.apache.jackrabbit.core.security.SecurityConstants.ADMINISTRATORS_NAME
      String adminGroup = "administrators";

      credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT,
            Collections.singleton(new AuthorizableOrganizationDetails(adminGroup)));

      return credentials;
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Delegate methods.
   // /////////////////////////////////////////////////////////////////////////////////////

   private String getParentOfVirtualFolder(String resourceId)
   {
      return getVirtualFolderHandler().getParentOfVirtualFolder(resourceId);
   }

   private void addVirtualFolderPrivileges(Set<IPrivilege> parentNodePrivileges,
         String virtualFolderPath)
   {
      getVirtualFolderHandler().addVirtualFolderPrivileges(parentNodePrivileges,
            virtualFolderPath);
   }

   protected String decodeResourceName(String string)
   {
      return getVirtualFolderHandler().decodeResourceName(string);
   }

   private boolean ensureVirtualFolderExists(String folderPath)
         throws DocumentManagementServiceException
   {
      return getVirtualFolderHandler().ensureVirtualFolderExists(folderPath);
   }

   private Folder substituteVirtualFolder(String folderPath)
   {
      return getVirtualFolderHandler().substituteVirtualFolder(folderPath);
   }

   private boolean isVirtualFolderPath(String folderPath)
   {
      return getVirtualFolderHandler().isVirtualFolderPath(folderPath);
   }

   private IAccessControlPolicy createVirtualEffectivePolicy()
   {
      return getVirtualFolderHandler().createVirtualEffectivePolicy();
   }

   private IAccessControlPolicy createVirtualApplicablePolicy()
   {
      return getVirtualFolderHandler().createVirtualApplicablePolicy();
   }

   private IAccessControlPolicy getVirtualFolderPolicy(String folderPath)
   {
      return getVirtualFolderHandler().getVirtualFolderPolicy(folderPath);
   }

   private boolean hasPrivilege(DmsPrivilege privilege, String virtualFolderPath,
         IAccessControlPolicy virtualFolderPolicy, IDocumentRepositoryService vfs)
   {
      return getVirtualFolderHandler().hasPrivilege(privilege, virtualFolderPath,
            virtualFolderPolicy, vfs);
   }

   private void checkValidDocumentType(final DocumentInfo document)
   {
      if ( !DocumentTypeUtils.isValidForDeployment(document))
      {
         String docTypeId = "";
         DocumentType documentType = document.getDocumentType();
         if (documentType != null)
         {
            docTypeId = documentType.getDocumentTypeId();
         }

         throw new DocumentManagementServiceException(
               BpmRuntimeError.DMS_DOCUMENT_TYPE_INVALID.raise(docTypeId));
      }
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Private classes.
   // /////////////////////////////////////////////////////////////////////////////////////

   private class AdminJcrDocumentRepositoryService extends JcrDocumentRepositoryService
   {

      public AdminJcrDocumentRepositoryService()
      {
         setSessionFactory(new AdminSessionFactory());
      }
   }

   private class AdminSessionFactory implements ISessionFactory
   {
      private Session session;

      public Session getSession() throws RepositoryException
      {
         if (session == null)
         {
            session = createJcrAdminSession();
         }
         return session;

      }

      public void releaseSession(Session session)
      {
         try
         {
            session.save();
         }
         catch (RepositoryException e)
         {
            trace.warn("RepositoryException on createJcrAdminSession:", e);
            throw new PublicException(e);
         }

         session.logout();
         session = null;
         this.session = null;
      }

   }

   private class VirtualFolderHandler
   {
      private static final String EVERYONE_PRINCIPAL = "everyone";

      private static final String ADMIN_PRINCIPAL = "administrators";

      private Map<VirtualFolderKey, String> virtualFolderPaths;

      private Map<String, IAccessControlPolicy> virtualFolderPermissions;

      public VirtualFolderHandler()
      {
      }

      public IAccessControlPolicy createVirtualApplicablePolicy()
      {
         Set<IAccessControlEntry> aces = CollectionUtils.newSet();
         return new JcrVfsAccessControlPolicy(aces,true);
      }

      public IAccessControlPolicy createVirtualEffectivePolicy()
      {
         Set<IAccessControlEntry> aces = CollectionUtils.newSet();

         IAccessControlEntry everyoneAce = new JcrVfsAccessControlEntry(
               new JcrVfsPrincipal(EVERYONE_PRINCIPAL),
               Collections.<IPrivilege> singleton(new IPrivilegeAdapter(
                     DmsPrivilege.READ_PRIVILEGE)), EntryType.ALLOW);
         aces.add(everyoneAce);

         IAccessControlEntry adminAce = new JcrVfsAccessControlEntry(new JcrVfsPrincipal(
               ADMIN_PRINCIPAL),
               Collections.<IPrivilege> singleton(new IPrivilegeAdapter(
                     DmsPrivilege.ALL_PRIVILEGES)), EntryType.ALLOW);
         aces.add(adminAce);
         return new JcrVfsAccessControlPolicy(aces, false);
      }

      private void updatePathsAndPermissions()
      {
         virtualFolderPaths = initVirtualFolderPaths();
         virtualFolderPermissions = initVirtualFolderPermissions();
      }

      private Map<VirtualFolderKey, String> initVirtualFolderPaths()
      {
         Map<VirtualFolderKey, String> allowedVirtualFolders = CollectionUtils.newHashMap();

         String realmId = SecurityProperties.getUserRealm().getId();
         String userId = SecurityProperties.getUser().getId();

         // TODO externalize to e.g. DocumentRepositoryFolderNames
         final String documentsFolder = "/" + DocumentRepositoryFolderNames.DOCUMENTS_FOLDER;

         // '/ipp-repository/partitions/<partitionId>'
         String partitionFolder = getPartitionPrefix();
         allowedVirtualFolders.put(VirtualFolderKey.PARTITION_FOLDER, partitionFolder);

         // '/ipp-repository/partitions/<partitionId>/preferences'
         String partitionPreferences = partitionFolder + "/"
               + DocumentRepositoryFolderNames.PREFS_FOLDER.replace("/", "");
         allowedVirtualFolders.put(VirtualFolderKey.PARTITION_PREFRERENCES_FOLDER,
               partitionPreferences);

         // '/ipp-repository/partitions/<partitionId>/process-instances'
         String partitionProcessInstancesFolder = partitionFolder + "/"
               + DocumentRepositoryFolderNames.PROCESS_ATTACHMENT_FOLDER.replace("/", "");
         allowedVirtualFolders.put(VirtualFolderKey.PARTITION_PROCESS_INSTANCES_FOLDER,
               partitionProcessInstancesFolder);

         // '/ipp-repository/partitions/<partitionId>/documents'
         String partitionDocumentsFolder = partitionFolder + documentsFolder;
         allowedVirtualFolders.put(VirtualFolderKey.PARTITION_DOCUMENTS_FOLDER,
               partitionDocumentsFolder);

         // '/ipp-repository/partitions/<partitionId>/realms/<realmId>/users/<userId>/documents'
         String userFolder = partitionFolder + //
               "/" + DocumentRepositoryFolderNames.REALMS_FOLDER + realmId + //
               "/" + DocumentRepositoryFolderNames.USERS_FOLDER + userId;
         allowedVirtualFolders.put(VirtualFolderKey.USER_FOLDER, userFolder);

         return allowedVirtualFolders;
      }

      private Map<String, IAccessControlPolicy> initVirtualFolderPermissions()
      {
         Map<String, IAccessControlPolicy> ret = null;
         if (virtualFolderPaths != null)
         {
            Map<String, IAccessControlPolicy> virtualFolderPermissions = CollectionUtils.newHashMap();

            String userFolderPath = virtualFolderPaths.get(VirtualFolderKey.USER_FOLDER);
            if (userFolderPath != null)
            {
               virtualFolderPermissions.put(userFolderPath, createSingleEntryPolicy(
                     EVERYONE_PRINCIPAL, DmsPrivilege.ALL_PRIVILEGES, EntryType.ALLOW));
            }

            String partitionProcessInstancesFolder = virtualFolderPaths.get(VirtualFolderKey.PARTITION_PROCESS_INSTANCES_FOLDER);
            if (partitionProcessInstancesFolder != null)
            {
               virtualFolderPermissions.put(partitionProcessInstancesFolder,
                     createSingleEntryPolicy(EVERYONE_PRINCIPAL,
                           DmsPrivilege.ALL_PRIVILEGES, EntryType.ALLOW));
            }
            ret = virtualFolderPermissions;
         }
         return ret;
      }

      public boolean isVirtualFolderPath(String folderPath)
      {
         updatePathsAndPermissions();

         for (String path : virtualFolderPaths.values())
         {
            if (path.equals(folderPath))
            {
               return true;
            }
         }
         return false;
      }

      private IAccessControlPolicy createSingleEntryPolicy(String principalName,
            Privilege privilege, EntryType type)
      {
         Principal prin = new JcrVfsPrincipal(principalName);
         Set<IPrivilege> priv = Collections.<IPrivilege> singleton(new IPrivilegeAdapter(
               privilege));
         Set<IAccessControlEntry> aces = Collections.<IAccessControlEntry> singleton(new JcrVfsAccessControlEntry(
               prin, priv, type));
         IAccessControlPolicy acp = new JcrVfsAccessControlPolicy(aces, true);

         return acp;
      }

      public String decodeResourceName(String string)
      {
         updatePathsAndPermissions();

         // only add prefix if its a Path
         if (string != null)
         {
            // remove tailing "/"
            if (string.length() > 1 && string.endsWith("/"))
            {
               string = string.substring(0, string.length() - 1);
            }

            final String partitionPrefix = getPartitionPrefix();
            if (string.startsWith("/"))
            {
               if ( !string.startsWith(partitionPrefix))
               {
                  if (string.length() == 1)
                  {
                     return partitionPrefix;
                  }
                  return partitionPrefix.concat(string);
               }
            }
            for (Map.Entry<VirtualFolderKey, String> entry : virtualFolderPaths.entrySet())
            {
               if (string.startsWith(entry.getKey().getName()))
               {
                  string = string.replace(entry.getKey().getName(), entry.getValue());
                  break;
               }
            }
         }
         return string;
      }

      public boolean ensureVirtualFolderExists(String folderPath)
            throws DocumentManagementServiceException
      {
         boolean isVirtualFolderPath = false;
         updatePathsAndPermissions();

         for (String allowedVirtualFolder : virtualFolderPaths.values())
         {
            if (allowedVirtualFolder.equals(folderPath))
            {
               isVirtualFolderPath = true;
               synchronized (VirtualFolderHandler.class)
               {
                  // only use adminSession for jcr security enabled environment
                  final String innerFolderPath = folderPath;
                  if (isSecurityEnabled(getVfs(), innerFolderPath))
                  {
                     runIsolateAction(new Action<Object>()
                     {
                        public Object execute()
                        {

                           IDocumentRepositoryService adminVfs = getAdminVfs();

                           DmsVfsConversionUtils.ensureFolderHierarchyExists(adminVfs, innerFolderPath);

                           ensureFolderPermissions(adminVfs, innerFolderPath);
                           return null;
                        }
                     });
                  }
                  else
                  {
                     adaptVfsCall(new IVfsOperationCallback()
                     {
                        public Object withVfs(IDocumentRepositoryService vfs)
                              throws RepositoryOperationFailedException
                        {
                           DmsVfsConversionUtils.ensureFolderHierarchyExists(vfs, innerFolderPath);
                           return null;
                        }
                     });
                  }
               }
            }
         }
         return isVirtualFolderPath;
      }

      private <T extends Object> T runIsolateAction(Action<T> action)
      {
         ForkingServiceFactory factory = null;
         ForkingService service = null;
         try
         {
            factory = (ForkingServiceFactory) Parameters.instance().get(
                  EngineProperties.FORKING_SERVICE_HOME);
            service = factory.get();
            return (T) service.isolate(action);
         }
         finally
         {
            if (null != factory)
            {
               factory.release(service);
            }
         }
      }

      public String getParentOfVirtualFolder(final String folderPath)
      {
         updatePathsAndPermissions();

         String ret = null;
         for (String allowedVirtualFolder : virtualFolderPaths.values())
         {
            if (allowedVirtualFolder.equals(folderPath))
            {
               ret = (String) adaptVfsCall(new IVfsOperationCallback()
               {
                  public Object withVfs(IDocumentRepositoryService vfs)
                        throws RepositoryOperationFailedException
                  {
                     String parent = folderPath;
                     boolean found = false;
                     while ( !found)
                     {
                        int lastSlash = parent.lastIndexOf("/");

                        parent = parent.substring(0, lastSlash);

                        if (StringUtils.isEmpty(parent))
                        {
                           parent = VfsUtils.REPOSITORY_ROOT;
                           found = true;
                        }
                        else if (null != vfs.getFolder(parent, Folder.LOD_NO_MEMBERS))
                        {
                           found = true;
                        }
                     }
                     return parent;
                  }
               });

            }
         }
         return ret;

      }


      public void addVirtualFolderPrivileges(Set<IPrivilege> parentNodePrivileges,
            String virtualFolderPath)
      {
         updatePathsAndPermissions();

         IAccessControlPolicy accessControlPolicy = virtualFolderPermissions.get(virtualFolderPath);
         if (accessControlPolicy != null)
         {
            // Privilege entry exists for path, now add it to the privilege set.
            IAccessControlEntry ace = accessControlPolicy.getAccessControlEntries()
                  .iterator()
                  .next();

            // TODO privilege evaluation. (userId, participantIds against principal name)
            // only everyone principal supported right now.
            if (EVERYONE_PRINCIPAL.equals(ace.getPrincipal().getName()))
            {
               IPrivilege allPrivilege = new IPrivilegeAdapter(
                     DmsPrivilege.ALL_PRIVILEGES);
               if (ace.getPrivileges().contains(allPrivilege))
               {
                  parentNodePrivileges.clear();
                  parentNodePrivileges.add(allPrivilege);
               }
               else
               {
                  parentNodePrivileges.addAll(ace.getPrivileges());
               }
            }
            else
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Setting virtual folder internal privileges is only supported for the EveryonePrincipal.");
               }
            }
         }
      }

      public boolean hasPrivilege(DmsPrivilege privilege, String virtualFolderPath,
            IAccessControlPolicy virtualFolderPolicy, IDocumentRepositoryService vfs)
      {
         if (virtualFolderPolicy != null)
         {
            for (IAccessControlEntry ace : virtualFolderPolicy.getAccessControlEntries())
            {
               // TODO privilege evaluation. (userId, participantIds against principal)
               // only everyone principal supported right now.
               if (EVERYONE_PRINCIPAL.equals(ace.getPrincipal().getName()))
               {
                  Set<IPrivilege> privileges = ace.getPrivileges();
                  if (privileges.contains(new IPrivilegeAdapter(
                        DmsPrivilege.ALL_PRIVILEGES))
                        || privileges.contains(new IPrivilegeAdapter(privilege)))
                  {
                     return true;
                  }
               }
            }
         }

         Set<IPrivilege> privileges = vfs.getPrivileges(virtualFolderPath);
         if (privileges.contains(new IPrivilegeAdapter(DmsPrivilege.ALL_PRIVILEGES))
               || privileges.contains(new IPrivilegeAdapter(privilege)))
         {
            return true;
         }

         return false;
      }

      public Folder substituteVirtualFolder(String folderPath)
      {
         Folder ret = null;
         String partitionPrefix = getPartitionPrefix();

         updatePathsAndPermissions();

         // virtual root folder
         if (folderPath.equals("/") || folderPath.equals(partitionPrefix + "/"))
         {
            ret = createVirtualFolder(folderPath,
                  VirtualFolderKey.PARTITION_FOLDER.getName());
         }
         for (Map.Entry<VirtualFolderKey, String> entry : virtualFolderPaths.entrySet())
         {
            if (entry.getValue().equals(folderPath))
            {
               ret = createVirtualFolder(folderPath, entry.getKey().getName());
            }
         }
         return ret;
      }

      private Folder createVirtualFolder(String folderId, String virtualId)
      {
         Map legoObject = CollectionUtils.newHashMap();

         legoObject.put(AuditTrailUtils.RES_ID, virtualId);
         legoObject.put(AuditTrailUtils.RES_PATH, removePrefixPath(folderId,
               getPartitionPrefix()));

         return new DmsFolderBean(legoObject);
      }

      private String removePrefixPath(String path, String prefixPath)
      {

         if ( !StringUtils.isEmpty(prefixPath))
         {
            if (prefixPath.equals(path))
            {
               path = "/";
            }
            else if (path.startsWith(prefixPath))
            {
               path = path.substring(prefixPath.length());
            }
         }
         return path;
      }

      public IAccessControlPolicy getVirtualFolderPolicy(String folderPath)
      {
         updatePathsAndPermissions();

         if (virtualFolderPermissions != null)
         {
            return virtualFolderPermissions.get(folderPath);
         }
         return null;
      }

      private void ensureFolderPermissions(IDocumentRepositoryService adminVfs,
            String folderPath)
      {
         if (virtualFolderPermissions != null)
         {
            IAccessControlPolicy acpToApply = virtualFolderPermissions.get(folderPath);

            if (acpToApply != null)
            {
               ensureFolderPermission(folderPath, acpToApply, adminVfs);
            }

         }
      }

      private void ensureFolderPermission(String folderPath, IAccessControlPolicy policy,
            IDocumentRepositoryService vfs)
      {

         Set<IAccessControlPolicy> applicablePolicies = vfs.getApplicablePolicies(folderPath);
         Set<IAccessControlPolicy> policies = vfs.getPolicies(folderPath);

         if (applicablePolicies.isEmpty() && policies.isEmpty())
         {
            // jcr security is disabled, do nothing
         }
         else if ( !applicablePolicies.isEmpty())
         {
            IAccessControlPolicy acp = applicablePolicies.iterator().next();

            for (IAccessControlEntry ace : policy.getAccessControlEntries())
            {
               acp.addAccessControlEntry(ace.getPrincipal(), ace.getPrivileges(), ace.getType());
            }
            vfs.setPolicy(folderPath, acp);

         }
         else if ( !policies.isEmpty())
         {
            // do nothing if a policy is already set.

            // for (IAccessControlPolicy accessControlPolicy : policies)
            // {
            // Set<IAccessControlEntry> aces =
            // accessControlPolicy.getAccessControlEntries();
            // for (IAccessControlEntry accessControlEntry : aces)
            // {
            // if (principalName.equals(accessControlEntry.getPrincipal().getName()))
            // {
            //
            // }
            // }
            // }
         }
      }
   }

}
