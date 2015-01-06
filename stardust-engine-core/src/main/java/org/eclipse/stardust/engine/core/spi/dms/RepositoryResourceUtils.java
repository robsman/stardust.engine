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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderBean;

/**
 * Factory that should be used to create {@link Document} and {@link Folder} instances.
 *
 * @author Roland.Stamm
 */
public class RepositoryResourceUtils
{

   private RepositoryResourceUtils()
   {
      // utility class
   }

   /**
    * Use this to create a {@link Document} if no meta data is known at all.<br>
    * For example when it is stored in a key value store and only the key is known.
    * <p>
    * The document name defaults to the specified document id but can be changed via {@link Document#setName(String)}.
    * The document size defaults to 0.<br>
    * The document path defaults to {@link RepositoryConstants#ROOT_FOLDER_PATH}<br>
    * The document revisionId and revisionName default to {@link RepositoryConstants#VERSION_UNVERSIONED}.
    * All other values are <code>null</code>.
    * If more information about the document is available please use {@link #createDocument(String, String, long, String, Date, Date)}.
    * <p>
    * Only parameters that cannot be changed via setters on {@link Document} are available.
    * @param id The unique identifier of the document.
    * @return The new document instance.
    */
   public static Document createDocument(String id)
   {
      return createDocument(id, RepositoryConstants.ROOT_FOLDER_PATH, 0, null, null, null);
   }


   /**
    * This creates a document without version information.
    * <p>
    * The document name defaults to the specified document id but can be changed via {@link Document#setName(String)}.
    * The document revisionId and revisionName default to {@link RepositoryConstants#VERSION_UNVERSIONED}.
    * <p>
    * Only parameters that cannot be changed via setters on {@link Document} are available.
    *
    * @param id The unique identifier of the document.
    * @param path The path of the document. As path separator {@link RepositoryConstants#PATH_SEPARATOR} is expected.
    * @param size The size of the content.
    * @param encoding The encoding of the content.
    * @param dateCreated The date the document was created
    * @param dateLastModified The date the document was modified last.
    * @param revisionId The id of this document's version. It can be the same as the document id.
    * @return The new document instance.
    */
   public static Document createDocument(String id, String path, long size, String encoding, Date dateCreated, Date dateLastModified)
   {
      DmsDocumentBean doc = new DmsDocumentBean();

      // resource
      doc.setId(id);
      doc.setName(id);
      doc.setPath(path);
      doc.setDateCreated(dateCreated);
      doc.setDateLastModified(dateLastModified);

      // document content
      doc.setEncoding(encoding);
      doc.setSize(size);

      // version
      doc.setRevisionId(RepositoryConstants.VERSION_UNVERSIONED);
      doc.setRevisionName(RepositoryConstants.VERSION_UNVERSIONED);
      doc.setVersionLabels(Collections.EMPTY_LIST);
      return doc;
   }

   /**
    * This creates a document with version information.
    * <p>
    * The document name defaults to the specified document id but can be changed via {@link Document#setName(String)}.
    * <p>
    * Only parameters that cannot be changed via setters on {@link Document} are available.
    *
    * @param id The unique identifier of the document.
    * @param path The path of the document. As path separator {@link RepositoryConstants#PATH_SEPARATOR} is expected.
    * @param size The size of the content.
    * @param encoding The encoding of the content.
    * @param dateCreated The date the document was created
    * @param dateLastModified The date the document was modified last.
    * @param revisionId The id of this document's version. It can be the same as the document id.
    * @param revisionName The name of the document's version.
    * @param revisionComment A comment related to the documents version operation.
    * @param versionLabels Labels assigned to the document version.
    * @return The new document instance.
    */
   public static Document createDocument(String id, String path, long size, String encoding, Date dateCreated, Date dateLastModified, String revisionId, String revisionName, String revisionComment, List<String> versionLabels)
   {
      DmsDocumentBean doc = new DmsDocumentBean();

      // resource
      doc.setId(id);
      doc.setName(id);
      doc.setPath(path);
      doc.setDateCreated(dateCreated);
      doc.setDateLastModified(dateLastModified);

      // document content
      doc.setEncoding(encoding);
      doc.setSize(size);

      // version
      doc.setRevisionId(revisionId);
      doc.setRevisionName(revisionName);
      doc.setRevisionComment(revisionComment);
      doc.setVersionLabels(versionLabels);
      return doc;
   }

   /**
    * This creates a folder object.
    * <p>
    * The folder name defaults to the specified folder id but can be changed via {@link Folder#setName(String)}.
    * <p>
    * Only parameters that cannot be changed via setters on {@link Folder} are available.
    *
    * @param id The unique identifier of the folder.
    * @param path The path of the folder. As path separator {@link RepositoryConstants#PATH_SEPARATOR} is expected.
    * @param dateCreated The date the folder was created
    * @param dateLastModified The date the folder was modified last.
    * @param containedDocuments The documents that are contained in the folder. Should be {@link Collections#EMPTY_LIST} if {@link Folder#LOD_NO_MEMBERS} is selected.
    * @param subFolders The folders that are contained in the folder. Should be {@link Collections#EMPTY_LIST} if {@link Folder#LOD_NO_MEMBERS} is selected.
    * @param levelOfDetail The levelOfDetails {@link Folder#LOD_NO_MEMBERS}, {@link Folder#LOD_LIST_MEMBERS}, {@link Folder#LOD_LIST_MEMBERS_OF_MEMBERS}.
    * @return The new folder instance.
    */
   public static Folder createFolder(String id, String path,  Date dateCreated, Date dateLastModified, List<Document> containedDocuments, List<Folder> subFolders, int levelOfDetail)
   {
      DmsFolderBean folder = new DmsFolderBean();

      // resource
      folder.setId(id);
      folder.setName(id);
      folder.setPath(path);
      folder.setDateCreated(dateCreated);
      folder.setDateLastModified(dateLastModified);

      // folder content
      folder.setDocuments(containedDocuments == null ? Collections.EMPTY_LIST: containedDocuments);
      folder.setFolders(subFolders == null ? Collections.EMPTY_LIST: subFolders);
      folder.setLevelOfDetail(levelOfDetail);

      return folder;
   }

}
