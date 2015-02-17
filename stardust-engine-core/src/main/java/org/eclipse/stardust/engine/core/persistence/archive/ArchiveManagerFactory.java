package org.eclipse.stardust.engine.core.persistence.archive;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;

public class ArchiveManagerFactory
{
   private static final String DEFAULT_ARCHIVE_MANAGER = "ZIP";

   private static final String DEFAULT_ARCHIVE_FOLDER_FORMAT = "\\yyyy\\MM\\dd\\HH";

   private static final String CARNOT_ARCHIVE_MANAGER = "Archive.Manager.Type";

   private static final String CARNOT_ARCHIVE_ROOTFOLDER = "Archive.Manager.RootFolder";

   private static final String CARNOT_ARCHIVE_FOLDER_FORMAT = "Archive.Manager.FolderFormat";

   public static IArchiveManager getCurrent()
   {
      String archiveManagerType = Parameters.instance().getString(CARNOT_ARCHIVE_MANAGER,
            DEFAULT_ARCHIVE_MANAGER);

      ArchiveManagerType type = ArchiveManagerType.valueOf(archiveManagerType);

      IArchiveManager archiveManager;
      switch (type)
      {
         case ZIP: archiveManager = getZipArchiveManager();
            break;
         default:
            throw new IllegalArgumentException("Unknow ArchiveManager");
      }
      return archiveManager;
   }

   private static IArchiveManager getZipArchiveManager()
   {
      String rootFolder = Parameters.instance().getString(CARNOT_ARCHIVE_ROOTFOLDER, "");
      if (StringUtils.isEmpty(rootFolder.trim()))
      {
         throw new IllegalArgumentException(CARNOT_ARCHIVE_ROOTFOLDER + " must be provided for ZIP archive type");
      }
      
      String folderFormat = Parameters.instance().getString(CARNOT_ARCHIVE_FOLDER_FORMAT,
            DEFAULT_ARCHIVE_FOLDER_FORMAT);
      IArchiveManager archiveManager = ZipArchiveManager.getInstance(rootFolder,
            folderFormat);
      return archiveManager;
   }

   public enum ArchiveManagerType
   {
      ZIP;
   }
}
