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

/**
 * The <code>Folder</code> interface represents an existing JCR folder.
 *
 * @author rsauer
 * @version $Revision$
 */
public interface Folder extends FolderInfo, Resource
{
   // keep in sync with jcr-vfs's IFolder

   /**
    * If level of detail is set to <code>LOD_NO_MEMBERS</code>,
    * the <code>Folder</code> object contains neither its document
    * nor subfolder information
    */
   int LOD_NO_MEMBERS = 0;

   /**
    * If level of detail is set to <code>LOD_LIST_MEMBERS</code>,
    * the <code>Folder</code> object contains both its document and subfolder
    * without their document and subfolder information
    */
   int LOD_LIST_MEMBERS = 1;

   /**
    * If level of detail is set to <code>LOD_LIST_MEMBERS_OF_MEMBERS</code>,
    * the <code>Folder</code> object contains both its document and subfolder
    * with their document and subfolder information
    */
   int LOD_LIST_MEMBERS_OF_MEMBERS = 2;

   /**
    * Gets the level of detail of information contained in the <code>Folder</code>
    * object.
    *
    * @return one of <code>LOD_NO_MEMBERS</code>, <code>LOD_LIST_MEMBERS</code>
    * or <code>LOD_LIST_MEMBERS_OF_MEMBERS</code>.
    */
   int getLevelOfDetail();

   /**
    * Gets the number of documents contained in this folder. The information returned
    * may be different depending on the level of detail.
    *
    * @return the number of documents contained in this folder.
    */
   int getDocumentCount();

   /**
    * Gets the documents contained in this folder. The information returned
    * may be different depending on the level of detail.
    *
    * Use {@link DocumentManagementService#createDocument(String, DocumentInfo)} to add documents
    * to this folder.
    *
    * @return the documents contained in this folder. This list is unmodifiable.
    */
   List<Document> getDocuments();

   /**
    * Gets the number of subfolders contained in this folder. The information returned
    * may be different depending on the level of detail.
    *
    * @return the number of subfolders contained in this folder.
    */
   int getFolderCount();

   /**
    * Gets the subfolders contained in this folder. The information returned
    * may be different depending on the level of detail.
    *
    * Use {@link DocumentManagementService#createFolder(String, FolderInfo)} to add subfolders
    * to this folder.
    *
    * @return the subfolders contained in this folder. This list is unmodifiable.
    */
   List<Folder> getFolders();

}
