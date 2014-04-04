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
   
   public static Document createDocument(String id, String path, long size, String encoding, Date dateCreated, Date dateLastModified)
   {
      DmsDocumentBean doc = new DmsDocumentBean();
      
      // resource
      doc.setId(id);
      doc.setPath(path);
      doc.setDateCreated(dateCreated);
      doc.setDateLastModified(dateLastModified);
      
      // document content
      doc.setEncoding(encoding);
      doc.setSize(size);
      return doc;
   }
   
   public static Document createDocument(String id, String path, long size, String encoding, Date dateCreated, Date dateLastModified, String revisionId, String revisionName, String revisionComment, List<String> versionLabels)
   {
      DmsDocumentBean doc = new DmsDocumentBean();
      
      // resource
      doc.setId(id);
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
   
   public static Folder createFolder(String id, String path,  Date dateCreated, Date dateLastModified, List<Document> containedDocuments, List<Folder> subFolders, int levelOfDetail)
   {
      DmsFolderBean folder = new DmsFolderBean();
      
      // resource
      folder.setId(id);
      folder.setPath(path);
      folder.setDateCreated(dateCreated);
      folder.setDateLastModified(dateLastModified);
      
      // folder content
      folder.setDocuments(containedDocuments);
      folder.setFolders(subFolders);
      folder.setLevelOfDetail(levelOfDetail);
      
      return folder;
   }

}
