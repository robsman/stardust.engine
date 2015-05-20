package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class ArchiveManagerFactory
{
   public static final String MODULE_ID_STARDUST_ARCHIVING = "stardust-archiving";

   private static final String DEFAULT_ARCHIVE_MANAGER = "FILESYSTEM";

   public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm";

   public static final String DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB = "100";

   public static final String DEFAULT_ARCHIVE_FOLDER_FORMAT = "\\yyyy\\MM\\dd\\HH";

   public static final String DEFAULT_AUTO_ARCHIVE = "false";


   public static final String CARNOT_ARCHIVE_ROOTFOLDER = "stardust-archiving.archive.filesystem.root";

   public static final String CARNOT_ARCHIVE_FOLDER_FORMAT = "stardust-archiving.archive.filesystem.folderFormat";

   public static final String CARNOT_ARCHIVE_DATE_FORMAT = "stardust-archiving.archive.filesystem.dateFormat";

   public static final String CARNOT_ARCHIVE_ZIP_FILE_SIZE_MB = "stardust-archiving.archive.filesystem.zipFileSize";

   public static final String CARNOT_ARCHIVE_MANAGER_ID = "stardust-archiving.archive.ID";

   public static final String CARNOT_AUTO_ARCHIVE = "stardust-archiving.archive.auto";

   public static final String CARNOT_AUTO_ARCHIVE_DOCUMENTS = "stardust-archiving.archive.auto.documents";
   
   public static final String CARNOT_ARCHIVE_MANAGER_TYPE = "stardust-archiving.archive.type";
   
   public static final String CARNOT_ARCHIVE_MANAGER_CUSTOM = "Archive.Manager.Type.Class";

   private static volatile ConcurrentHashMap<String, IArchiveManager> PARTITION_MANAGERS = CollectionUtils
         .newConcurrentHashMap();

   public static void resetArchiveManagers()
   {
      PARTITION_MANAGERS.clear();
   }

   public static IArchiveManager getArchiveManager()
   {
      return getArchiveManager(SecurityProperties.getPartition().getId());
   }
   
   public static void removeArchiveManager(Map<String, String> preferences)
   {
      String uuid = preferences.get(CARNOT_ARCHIVE_MANAGER_ID);
      if (StringUtils.isEmpty(uuid))
      {
         throw new IllegalArgumentException(
               "When providing custom/temporary preferences for an ArchiveManager a uuid must be provided.");
      }
      if (PARTITION_MANAGERS.containsKey(uuid))
      {
         synchronized (ArchiveManagerFactory.class)
         {
            if (PARTITION_MANAGERS.containsKey(uuid))
            {
               PARTITION_MANAGERS.remove(uuid);
            }
         }
      }
   }

   public static IArchiveManager getArchiveManager(String partition)
   {
      if (!PARTITION_MANAGERS.containsKey(partition))
      {
         synchronized (ArchiveManagerFactory.class)
         {
            if (!PARTITION_MANAGERS.containsKey(partition))
            {
               Map<String, String> preferences = aggregatePreferences(null);
               getCurrentId(preferences);
               IArchiveManager manager = createArchiveManager(preferences);
               PARTITION_MANAGERS.put(partition, manager);
            }
         }
      }
      return PARTITION_MANAGERS.get(partition);
   }

   /**
    * Constructor used when importing from a temporary archive manager
    * 
    * @param preferences
    * @return
    */
   public static IArchiveManager getArchiveManager(Map<String, String> preferences)
   {
      if (preferences == null)
      {
         return getArchiveManager();
      }
      else
      {
         String uuid = preferences.get(CARNOT_ARCHIVE_MANAGER_ID);
         if (StringUtils.isEmpty(uuid))
         {
            throw new IllegalArgumentException(
                  "When providing custom/temporary preferences for an ArchiveManager a uuid must be provided.");
         }

         if (!PARTITION_MANAGERS.containsKey(uuid))
         {
            synchronized (ArchiveManagerFactory.class)
            {
               if (!PARTITION_MANAGERS.containsKey(uuid))
               {
                  preferences = aggregatePreferences(preferences);
                  IArchiveManager manager = createArchiveManager(preferences);
                  PARTITION_MANAGERS.put(uuid, manager);
               }
            }
         }
         return PARTITION_MANAGERS.get(uuid);
      }
   }

   private static IArchiveManager createArchiveManager(Map<String, String> preferences)
   {
      String archiveManagerType = preferences.get(CARNOT_ARCHIVE_MANAGER_TYPE);

      ArchiveManagerType type = ArchiveManagerType.valueOf(archiveManagerType.toUpperCase());

      IArchiveManager archiveManager;
      switch (type)
      {
         case FILESYSTEM:
            archiveManager = getZipArchiveManager(preferences);
            break;
         case CUSTOM:
            archiveManager = getCustomArchiveManager(preferences);
            break;
         default:
            throw new IllegalArgumentException("Unknown ArchiveManager");
      }
      return archiveManager;
   }

   private static Map<String, String> aggregatePreferences(Map<String, String> preferences)
   {
      if (preferences == null)
      {
         preferences = new HashMap<String, String>();
      }

      setPreference(preferences, CARNOT_ARCHIVE_MANAGER_TYPE, DEFAULT_ARCHIVE_MANAGER);
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_ROOTFOLDER, "");
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_FOLDER_FORMAT,
            ArchiveManagerFactory.DEFAULT_ARCHIVE_FOLDER_FORMAT);
      setPreference(preferences, CARNOT_ARCHIVE_DATE_FORMAT, DEFAULT_DATE_FORMAT);
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_ZIP_FILE_SIZE_MB,
            ArchiveManagerFactory.DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB);
      setPreference(preferences, CARNOT_ARCHIVE_MANAGER_CUSTOM, "");
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_ID, null);
      setPreference(preferences, CARNOT_AUTO_ARCHIVE, DEFAULT_AUTO_ARCHIVE);
      setPreference(preferences, CARNOT_AUTO_ARCHIVE_DOCUMENTS, DEFAULT_AUTO_ARCHIVE);
      return preferences;
   }

   private static void setPreference(Map<String, String> preferences, String preference,
         String defaultValue)
   {
      if (!preferences.containsKey(preference))
      {
         preferences.put(preference, getPreference(preference, defaultValue));
      }
   }

   private static String getCurrentId(Map<String, String> preferences)
   {
      String partition = SecurityProperties.getPartition().getId();
      String uuid = preferences.get(CARNOT_ARCHIVE_MANAGER_ID);
      if (StringUtils.isEmpty(uuid))
      {
         uuid = partition;
         saveArchiveManagerId(uuid);
      }
      preferences.put(CARNOT_ARCHIVE_MANAGER_ID, uuid);
      return uuid;
   }

   private static String getPreference(String preferenceName, String defaultValue)
   {
      IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();

      Preferences preferences = preferenceStore.getPreferences(PreferenceScope.PARTITION,
            MODULE_ID_STARDUST_ARCHIVING, preferenceName);
      if (preferences.getPreferences().containsKey(preferenceName))
      {
         return (String) preferences.getPreferences().get(preferenceName);
      }
      else
      {
         return defaultValue;
      }
   }

   private static void saveArchiveManagerId(String archiveManagerId)
   {
      IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();

      preferenceStore.savePreferences(
            new Preferences(PreferenceScope.PARTITION, MODULE_ID_STARDUST_ARCHIVING,
                  CARNOT_ARCHIVE_MANAGER_ID, Collections.singletonMap(
                        CARNOT_ARCHIVE_MANAGER_ID, (Serializable) archiveManagerId)),
            false);
   }

   private static IArchiveManager getCustomArchiveManager(Map<String, String> preferences)
   {
      String custom = preferences.get(CARNOT_ARCHIVE_MANAGER_CUSTOM);
      if (StringUtils.isEmpty(custom.trim()))
      {
         throw new IllegalArgumentException(CARNOT_ARCHIVE_MANAGER_CUSTOM
               + " must be provided for Custom archive type");
      }
      try
      {
         Class type = Class.forName(custom);
         Constructor method = type.getConstructor(Map.class);
         IArchiveManager instance = (IArchiveManager) method.newInstance(preferences);
         return instance;
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException(custom + " class not found");
      }
      catch (ClassCastException e)
      {
         throw new IllegalArgumentException(custom + " is not an IArchiveManager."
               + e.getMessage());
      }
      catch (NoSuchMethodException e)
      {
         throw new IllegalArgumentException(custom + " is not an IArchiveManager."
               + e.getMessage());
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(custom + " class could not be instantiated "
               + e.getMessage());
      }
   }

   private static IArchiveManager getZipArchiveManager(Map<String, String> preferences)
   {
      IArchiveManager archiveManager = new ZipArchiveManager(preferences);
      return archiveManager;
   }

   public enum ArchiveManagerType
   {
      FILESYSTEM, CUSTOM;
   }

   public static String getDateFormat(String partition)
   {
      return getArchiveManager(partition).getDateFormat();
   }

   public static String getCurrentId(String partition)
   {
      return getArchiveManager(partition).getArchiveManagerId();
   }

   public static String getDateFormat()
   {
      return getDateFormat(SecurityProperties.getPartition().getId());
   }

   public static String getCurrentId()
   {
      return getCurrentId(SecurityProperties.getPartition().getId());
   }

   public static boolean autoArchive()
   {
      return getArchiveManager(SecurityProperties.getPartition().getId()).isAutoArchive();
   }
}
