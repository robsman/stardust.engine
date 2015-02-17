package org.eclipse.stardust.engine.core.persistence.archive;

public class ArchiveManagerFactory
{
   private static final String DEFAULT_FOLDER_FORMAT = "\\yyyy\\MM\\dd\\HH";
   
   public static IArchiveManager getCurrent()
   {
      //these will be bound to audittrail:
      String rootFolder = "c:/temp/";
      String folderFormat = DEFAULT_FOLDER_FORMAT;
      IArchiveManager archiveManager = ZipArchiveManager.getInstance(rootFolder, folderFormat);
      return archiveManager;
   }
}
