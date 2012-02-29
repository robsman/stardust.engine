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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;


import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IFolder;

/**
 * IFolder-view of a Folder
 */
public class FolderToIFolderAdapter extends ResourceToIResourceAdapter implements IFolder
{

   private final Folder folder;

   private String prefixPath;

   public FolderToIFolderAdapter(Folder folder, String prefixPath)
   {
      super(folder, prefixPath);

      this.folder = folder;
      this.prefixPath = prefixPath;
   }

   public IFile findFile(String name)
   {
      for (Iterator i = folder.getDocuments().iterator(); i.hasNext();)
      {
         Document document = (Document) i.next();
         if (document.getName().equals(name))
         {
            return new DocumentToIFileAdapter(document, prefixPath);
         }
      }
      return null;
   }

   public IFolder findFolder(String name)
   {
      for (Iterator i = folder.getFolders().iterator(); i.hasNext();)
      {
         Folder folder = (Folder) i.next();
         if (folder.getName().equals(name))
         {
            return new FolderToIFolderAdapter(folder, prefixPath);
         }
      }
      return null;
   }

   public IFile getFile(int index)
   {
      return new DocumentToIFileAdapter((Document) folder.getDocuments().get(index),
            prefixPath);
   }

   public IFile getFile(String id)
   {
      for (Iterator i = folder.getDocuments().iterator(); i.hasNext();)
      {
         Document document = (Document) i.next();
         if (document.getId().equals(id))
         {
            return new DocumentToIFileAdapter(document, prefixPath);
         }
      }
      return null;
   }

   public int getFileCount()
   {
      return folder.getDocumentCount();
   }

   public List/* < ? extends IFile> */getFiles()
   {
      List files = CollectionUtils.newList();
      for (Iterator i = folder.getDocuments().iterator(); i.hasNext();)
      {
         Document document = (Document) i.next();
         files.add(new DocumentToIFileAdapter(document, prefixPath));
      }
      return Collections.unmodifiableList(files);
   }

   public IFolder getFolder(int index)
   {
      return new FolderToIFolderAdapter((Folder) folder.getFolders().get(index),
            prefixPath);
   }

   public IFolder getFolder(String id)
   {
      for (Iterator i = folder.getFolders().iterator(); i.hasNext();)
      {
         Folder folder = (Folder) i.next();
         if (folder.getId().equals(id))
         {
            return new FolderToIFolderAdapter(folder, prefixPath);
         }
      }
      return null;
   }

   public int getFolderCount()
   {
      return this.folder.getFolderCount();
   }

   public List/* < ? extends IFolder> */getFolders()
   {
      List folders = CollectionUtils.newList();
      for (Iterator i = folder.getFolders().iterator(); i.hasNext();)
      {
         Folder folder = (Folder) i.next();
         folders.add(new FolderToIFolderAdapter(folder, prefixPath));
      }
      return Collections.unmodifiableList(folders);
   }

   public int getLevelOfDetail()
   {
      return folder.getLevelOfDetail();
   }

}
