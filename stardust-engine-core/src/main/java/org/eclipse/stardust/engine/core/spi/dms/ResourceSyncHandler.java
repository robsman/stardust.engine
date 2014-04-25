package org.eclipse.stardust.engine.core.spi.dms;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;

public class ResourceSyncHandler
{
   private transient List<IDmsResourceSyncListener> resourceSyncListeners;

   public void notifyDocumentUpdated(Document oldDocument, Document newDocument)
   {
      if (oldDocument != null)
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
   }

   public void notifyFolderUpdated(Folder oldFolder, Folder newFolder)
   {
      if (oldFolder != null)
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

}
