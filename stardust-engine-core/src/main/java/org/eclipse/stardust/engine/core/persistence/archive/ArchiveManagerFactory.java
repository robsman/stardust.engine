package org.eclipse.stardust.engine.core.persistence.archive;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;

public class ArchiveManagerFactory
{
   private static final String DEFAULT_ARCHIVE_MANAGER = "ZIP";
   
   private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm";

   public static final int DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB = 100;

   private static final String DEFAULT_ARCHIVE_FOLDER_FORMAT = "\\yyyy\\MM\\dd\\HH";

   public static final String CARNOT_ARCHIVE_MANAGER = "Archive.Manager.Type";

   private static final String CARNOT_ARCHIVE_ROOTFOLDER = "Archive.Manager.RootFolder";

   private static final String CARNOT_ARCHIVE_FOLDER_FORMAT = "Archive.Manager.FolderFormat";

   private static final String CARNOT_ARCHIVE_DATE_FORMAT = "Archive.Manager.DateFormat";
   
   private static final String ARCHIVE_ZIP_FILE_SIZE_MB = "Archive.Manager.ZipFile.SizeInMB";

   public static final String CARNOT_ARCHIVE_MANAGER_CUSTOM = "Archive.Manager.Type.Class";
   
   public static final String CARNOT_ARCHIVE_MANAGER_ID = "Archive.Manager.ID";

   public static final String CARNOT_AUTO_ARCHIVE = "Archive.Auto";

   private static final boolean DEFAULT_AUTO_ARCHIVE = false;

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
         case CUSTOM: archiveManager = getCustomArchiveManager();
            break;
         default:
            throw new IllegalArgumentException("Unknown ArchiveManager");
      }
      return archiveManager;
   }
   
   public static String getCurrentId() 
   {
      String id = Parameters.instance().getString(CARNOT_ARCHIVE_MANAGER_ID,"");
      if (StringUtils.isEmpty(id.trim()))
      {
         throw new IllegalArgumentException(CARNOT_ARCHIVE_MANAGER_ID + " must be provided for an Archive");
      }
      return id;
   }
   
   /**
    * Returns archive date format, used in export. 
    * Note that archives being imported may have a different date format to be obtained from the archive's ExportIndex.
    * @return
    */
   public static String getDateFormat() 
   {
      return Parameters.instance().getString(CARNOT_ARCHIVE_DATE_FORMAT,DEFAULT_DATE_FORMAT);
   }
   
   /**
    * Returns archive date format, used in export. 
    * Note that archives being imported may have a different date format to be obtained from the archive's ExportIndex.
    * @return
    */
   public static boolean autoArchive() 
   {
      return Parameters.instance().getBoolean(CARNOT_AUTO_ARCHIVE, DEFAULT_AUTO_ARCHIVE);
   }

   private static IArchiveManager getCustomArchiveManager()
   {
      String custom = Parameters.instance().getString(CARNOT_ARCHIVE_MANAGER_CUSTOM,"");
      if (StringUtils.isEmpty(custom.trim()))
      {
         throw new IllegalArgumentException(CARNOT_ARCHIVE_MANAGER_CUSTOM + " must be provided for Custom archive type");
      }
      try
      {
         Class type = Class.forName(custom);
         Object instance = type.newInstance();
         if (instance instanceof IArchiveManager)
         {
            return (IArchiveManager) instance;
         }
         else
         {
            throw new IllegalArgumentException(custom + " is not an IArchiveManager");
         }
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException(custom + " class not found");
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(custom + " class could not be instantiated " + e.getMessage());
      }
   }

   private static IArchiveManager getZipArchiveManager()
   {
      String rootFolder = Parameters.instance().getString(CARNOT_ARCHIVE_ROOTFOLDER, "");
      if (StringUtils.isEmpty(rootFolder.trim()))
      {
         throw new IllegalArgumentException(CARNOT_ARCHIVE_ROOTFOLDER + " must be provided for ZIP archive type");
      }
      String id = getCurrentId();
      String folderFormat = Parameters.instance().getString(CARNOT_ARCHIVE_FOLDER_FORMAT,
            DEFAULT_ARCHIVE_FOLDER_FORMAT);
      int zipFileSize = Parameters.instance().getInteger(ARCHIVE_ZIP_FILE_SIZE_MB, DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB);
      IArchiveManager archiveManager = ZipArchiveManager.getInstance(id, rootFolder,
            folderFormat, zipFileSize);
      return archiveManager;
   }

   public enum ArchiveManagerType
   {
      ZIP,
      CUSTOM;
   }
}
