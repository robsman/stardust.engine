package org.eclipse.stardust.engine.core.persistence.archive;

import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;

public class ArchiveManagerFactory
{
   private static final String DEFAULT_ARCHIVE_MANAGER = "FILESYSTEM";
   
   public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm";

   public static final int DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB = 100;

   public static final String DEFAULT_ARCHIVE_FOLDER_FORMAT = "\\yyyy\\MM\\dd\\HH";
   
   public static final boolean DEFAULT_AUTO_ARCHIVE = false;
   
   public static final String CARNOT_ARCHIVE_MANAGER = "stardust-archiving.export.archive.type";

   public static final String CARNOT_ARCHIVE_ROOTFOLDER = "stardust-archiving.export.archive.filesystem.root";

   public static final String CARNOT_ARCHIVE_FOLDER_FORMAT = "stardust-archiving.export.archive.filesystem.folderFormat";

   public static final String CARNOT_ARCHIVE_DATE_FORMAT = "stardust-archiving.export.archive.filesystem.dateFormat";
   
   public static final String ARCHIVE_ZIP_FILE_SIZE_MB = "stardust-archiving.export.archive.filesystem.zipFileSize";

   public static final String CARNOT_ARCHIVE_MANAGER_CUSTOM = "Archive.Manager.Type.Class";
   
   public static final String CARNOT_ARCHIVE_MANAGER_ID = "stardust-archiving.export.archive.ID";

   public static final String CARNOT_AUTO_ARCHIVE = "stardust-archiving.export.archive.auto";
   
   public static final String CARNOT_AUTO_ARCHIVE_DOCUMENTS = "stardust-archiving.export.archive.auto.documents";


   public static IArchiveManager getArchiveManagerFactory(Map<String, String> preferences)
   {
      String archiveManagerType = getPreferenceValue(preferences, CARNOT_ARCHIVE_MANAGER,
            DEFAULT_ARCHIVE_MANAGER);

      ArchiveManagerType type = ArchiveManagerType.valueOf(archiveManagerType);

      IArchiveManager archiveManager;
      switch (type)
      {
         case FILESYSTEM: archiveManager = getZipArchiveManager(preferences);
            break;
         case CUSTOM: archiveManager = getCustomArchiveManager(preferences);
            break;
         default:
            throw new IllegalArgumentException("Unknown ArchiveManager");
      }
      return archiveManager;
   }
   
   public static String getPreferenceValue(Map<String, String> preferences, String key, String defaultValue)
   {
      String value;
      if (preferences == null || !preferences.containsKey(key))
      {
         value = Parameters.instance().getString(key, defaultValue);
      }
      else
      {
         value = preferences.get(key);
      }
      
      return value;
   }
   

   public static int getPreferenceValueInt(Map<String, String> preferences, String key, int defaultValue)
   {
      int value;
      if (preferences == null || !preferences.containsKey(key))
      {
         value = Parameters.instance().getInteger(key, defaultValue);
      }
      else
      {
         value = new Integer(preferences.get(key));
      }
      
      return value;
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

   private static IArchiveManager getCustomArchiveManager(Map<String, String> preferences)
   {
      String custom = getPreferenceValue(preferences, CARNOT_ARCHIVE_MANAGER_CUSTOM,"");
      if (StringUtils.isEmpty(custom.trim()))
      {
         throw new IllegalArgumentException(CARNOT_ARCHIVE_MANAGER_CUSTOM + " must be provided for Custom archive type");
      }
      try
      {
         Class type = Class.forName(custom);
         Method method = type.getMethod("getInstance", Map.class);
         IArchiveManager instance = (IArchiveManager) method.invoke(null, preferences);
         return instance;
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException(custom + " class not found");
      }
      catch (ClassCastException e)
      {
         throw new IllegalArgumentException(custom + " is not an IArchiveManager." + e.getMessage());
      }
      catch (NoSuchMethodException e)
      {
         throw new IllegalArgumentException(custom + " is not an IArchiveManager." + e.getMessage());
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(custom + " class could not be instantiated " + e.getMessage());
      }
   }

   private static IArchiveManager getZipArchiveManager(Map<String, String> preferences)
   {
      IArchiveManager archiveManager = ZipArchiveManager.getInstance(preferences);
      return archiveManager;
   }

   public enum ArchiveManagerType
   {
      FILESYSTEM,
      CUSTOM;
   }
}
