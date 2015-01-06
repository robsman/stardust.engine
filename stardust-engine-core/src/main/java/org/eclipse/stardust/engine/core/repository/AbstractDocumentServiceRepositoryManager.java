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
package org.eclipse.stardust.engine.core.repository;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.stardust.common.LRUCache;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;


/**
 * @author sauer
 * @version $Revision: $
 */
public abstract class AbstractDocumentServiceRepositoryManager
      implements IRepositoryManager
{

   private LRUCache folderCache;
   private LRUCache contentProviderCache;

   protected abstract DocumentManagementService getDocumentService();

   protected abstract String getPartitionId();

   public AbstractDocumentServiceRepositoryManager ()
   {
      final long cacheTTL = Parameters.instance().getInteger(
            KernelTweakingProperties.CONFIGURATION_CACHE_TTL, 60000);
      final int maxCacheSize = Parameters.instance().getInteger(
            KernelTweakingProperties.CONFIGURATION_MAXIMUM_CACHE_ITEMS, 100);
      this.folderCache = new LRUCache(cacheTTL, maxCacheSize, true);
      this.contentProviderCache = new LRUCache(cacheTTL, maxCacheSize, true);
   }

   public IRepositoryContentProvider getContentProvider(RepositorySpaceKey space)
   {
      // TODO setup root folder

      Folder contentFolder = getContentFolder(space);

      if (contentFolder == null)
      {
         return NullContentProvider.INSTANCE;
      }
      else
      {
         IRepositoryContentProvider contentProvider = (IRepositoryContentProvider) this.contentProviderCache.get(contentFolder.getId());
         if (contentProvider == null)
         {
            contentProvider = new DocumentServiceContentProvider(this, contentFolder);
            this.contentProviderCache.put(contentFolder.getId(), contentProvider);
         }
         return contentProvider;
      }
   }

   public Folder getContentFolder(RepositorySpaceKey spaceKey)
   {
      return getContentFolder(spaceKey, false);
   }

   public Folder getContentFolder(RepositorySpaceKey spaceKey, boolean create)
   {
      StringBuffer folderPath = new StringBuffer(1024);
      folderPath.append(DocumentRepositoryFolderNames.getRepositoryRootFolder());

      folderPath.append(DocumentRepositoryFolderNames.PARTITIONS_FOLDER)
            .append(getPartitionId())
            .append("/")
            .append(DocumentRepositoryFolderNames.ARTIFACTS_FOLDER);

      if (RepositorySpaceKey.SKINS == spaceKey)
      {
         folderPath.append(DocumentRepositoryFolderNames.SKINS_FOLDER);
      }
      else if (RepositorySpaceKey.CONTENT == spaceKey)
      {
         folderPath.append(DocumentRepositoryFolderNames.CONTENT_FOLDER);
      }
      else if (RepositorySpaceKey.BUNDLES == spaceKey)
      {
         folderPath.append(DocumentRepositoryFolderNames.BUNDLES_FOLDER);
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.DMS_INVALID_REPOSITORY_SPACE.raise(spaceKey));
      }

      String folderPathString = folderPath.toString();
      if (folderPathString.endsWith("/"))
      {
         folderPath.deleteCharAt(folderPath.length() - 1);
         folderPathString = folderPath.toString();
      }

      Folder contentFolder = (Folder) this.folderCache.get(folderPathString);
      if (contentFolder == null)
      {
         contentFolder = getDocumentService().getFolder(folderPathString);
         this.folderCache.put(folderPathString, contentFolder);
      }

      if ((null == contentFolder) && create)
      {
         contentFolder = DocumentRepositoryUtils.getSubFolder(getDocumentService(),
               folderPathString);
         this.folderCache.put(folderPathString, contentFolder);
      }

      return contentFolder;
   }

   public void resetCaches()
   {
      this.contentProviderCache.clear();
      this.folderCache.clear();
   }

   private static class NullContentProvider implements IRepositoryContentProvider
   {

      static final IRepositoryContentProvider INSTANCE = new NullContentProvider();

      public InputStream getContentStream(String fileId)
      {
         return null;
      }

      public URL getContentUrl(String fileId)
      {
         return null;
      }

   }
}
