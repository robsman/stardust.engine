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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;


/**
 * @author rsauer
 * @version $Revision$
 */
public class DmsFolderBean extends DmsResourceBean implements Folder, Serializable, FolderInfo // ,
                                                                     // org.eclipse.stardust.engine.core.compatibility.extensions.dms.Folder
{

   private static final long serialVersionUID = 1L;
   
   private int levelOfDetail = Folder.LOD_LIST_MEMBERS;
   
   public DmsFolderBean(Map legoFolder)
   {
      super(legoFolder);
   }
   
   public DmsFolderBean()
   {
      super(CollectionUtils.newHashMap());
   }

   public int getLevelOfDetail()
   {
      return levelOfDetail;
   }

   public void setLevelOfDetail(int levelOfDetail)
   {
      this.levelOfDetail = levelOfDetail;
   }

   public int getDocumentCount()
   {
      return (null != vfsResource().get(AuditTrailUtils.FOLDER_DOCUMENT_COUNT))
            ? ((Integer) vfsResource().get(AuditTrailUtils.FOLDER_DOCUMENT_COUNT)).intValue()
            : 0;
   }
   
   public void setDocumentCount(int documentCount)
   {
      vfsResource().put(AuditTrailUtils.FOLDER_DOCUMENT_COUNT, documentCount); 
   }

   public int getFolderCount()
   {
      return (null != vfsResource().get(AuditTrailUtils.FOLDER_FOLDER_COUNT))
            ? ((Integer) vfsResource().get(AuditTrailUtils.FOLDER_FOLDER_COUNT)).intValue()
            : 0;
   }

   public void setFolderCount(int folderCount)
   {
      vfsResource().put(AuditTrailUtils.FOLDER_FOLDER_COUNT, folderCount);
   }

   public List/*<Folder>*/ getFolders()
   {
      List /* <Map> */ auditTrailSubFolders = (List)vfsResource().get(AuditTrailUtils.FOLDER_SUB_FOLDERS);
      if (auditTrailSubFolders == null || auditTrailSubFolders.isEmpty())
      {
         return Collections.EMPTY_LIST;
      }
      List /* <IDmsFolder> */ folders = CollectionUtils.newArrayList();
      for (Iterator f = auditTrailSubFolders.iterator(); f.hasNext(); )
      {
         folders.add(new DmsFolderBean((Map) f.next()));
      }
      return Collections.unmodifiableList(folders);
   }

   public void setFolders(List<Folder> folders)
   {
      List /* <Map> */auditTrailSubFolders = null;
      if (folders != null)
      {
         auditTrailSubFolders = new ArrayList();
         for (Folder folder : folders)
         {
            if (folder instanceof DmsFolderBean)
            {
               auditTrailSubFolders.add(((DmsFolderBean) folder).vfsResource());
            }
         }
         vfsResource().put(AuditTrailUtils.FOLDER_FOLDER_COUNT, auditTrailSubFolders.size());
      }
      vfsResource().put(AuditTrailUtils.FOLDER_SUB_FOLDERS, auditTrailSubFolders);
   }

   public List/*<Document>*/ getDocuments()
   {
      List /* <Map> */ auditTrailDocuments = (List)vfsResource().get(AuditTrailUtils.FOLDER_DOCUMENTS);
      if (auditTrailDocuments == null || auditTrailDocuments.isEmpty())
      {
         return Collections.EMPTY_LIST;
      }
      List /* <IDmsFolder> */ documents = CollectionUtils.newArrayList();
      for (Iterator f = auditTrailDocuments.iterator(); f.hasNext(); )
      {
         documents.add(new DmsDocumentBean((Map) f.next()));
      }

      return Collections.unmodifiableList(documents);
   }

   public void setDocuments(List<Document> documents)
   {
      List /* <Map> */auditTrailDocuments = null;
      if (documents != null)
      {
         auditTrailDocuments = new ArrayList();
         for (Document document : documents)
         {
            if (document instanceof DmsDocumentBean)
            {
               auditTrailDocuments.add(((DmsDocumentBean) document).vfsResource());
            }
         }
         vfsResource().put(AuditTrailUtils.FOLDER_DOCUMENT_COUNT, auditTrailDocuments.size());
      }
      vfsResource().put(AuditTrailUtils.FOLDER_DOCUMENTS, auditTrailDocuments);
   }

}
