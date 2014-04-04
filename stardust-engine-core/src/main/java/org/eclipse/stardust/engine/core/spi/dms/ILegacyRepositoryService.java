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

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;

/**
 * Contains deprecated methods that still need to be supported until they can be removed.
 * All methods that should be implemented for the SPI contract can be found on {@link IRepositoryService}.
 * 
 * @author Roland.Stamm
 *
 */
public abstract interface ILegacyRepositoryService extends IRepositoryService
{

   /**
    * Gets documents based on the name pattern search.
    *
    * @param namePattern the name pattern to search for.
    * @return list of documents found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   @Deprecated
   List<? extends Document> findDocumentsByName(String namePattern) throws DocumentManagementServiceException;

   /**
    * Gets documents based on the XPath query.
    *
    * @param xpathQuery the XPath query.
    * @return list of documents found.
    * @throws DocumentManagementServiceException on DMS specific errors
    */
   @Deprecated
   List<? extends Document> findDocuments(String xpathQuery) throws DocumentManagementServiceException;

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
   List<? extends Folder> findFoldersByName(String namePattern, int levelOfDetail) throws DocumentManagementServiceException;

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
   List<? extends Folder> findFolders(String xpathQuery, int levelOfDetail) throws DocumentManagementServiceException;

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
       * Locks the document for exclusive access.
       *
       * @param documentId ID or path of the document to be locked
       * @return the locked document
       * @throws DocumentManagementServiceException on DMS specific errors
       */
   //   Document lockDocument(String documentId) throws DocumentManagementServiceException;
   
      /**
       * Unlocks the document previously locked by <code>lockDocument</code>.
       *
       * @param documentId ID or path of the document to be locked
       * @return the unlocked document
       * @throws DocumentManagementServiceException on DMS specific errors
       */
      //Document unlockDocument(String documentId) throws DocumentManagementServiceException;
   
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

}
