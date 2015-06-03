package org.eclipse.stardust.engine.core.persistence.archive;

import java.lang.reflect.Constructor;
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

   public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

   public static final String DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB = "100";

   public static final String DEFAULT_ARCHIVE_FOLDER_FORMAT = "\\yyyy\\MM\\dd\\HH";

   public static final String DEFAULT_AUTO_ARCHIVE = "false";

   public static final String CARNOT_ARCHIVE_WRITER_ROOTFOLDER = "stardust-archiving.export.archive.filesystem.root";

   public static final String CARNOT_ARCHIVE_WRITER_FOLDER_FORMAT = "stardust-archiving.export.archive.filesystem.folderFormat";

   public static final String CARNOT_ARCHIVE_WRITER_DATE_FORMAT = "stardust-archiving.export.archive.filesystem.dateFormat";

   public static final String CARNOT_ARCHIVE_WRITER_ZIP_FILE_SIZE_MB = "stardust-archiving.export.archive.filesystem.zipFileSize";

   public static final String CARNOT_ARCHIVE_WRITER_MANAGER_ID = "stardust-archiving.export.archive.ID";
   
   public static final String CARNOT_ARCHIVE_READER_MANAGER_ID = "stardust-archiving.import.archive.ID";

   public static final String CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE = "stardust-archiving.export.archive.auto";

   public static final String CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE_DOCUMENTS = "stardust-archiving.export.archive.auto.documents";
   
   public static final String CARNOT_ARCHIVE_WRITER_MANAGER_TYPE = "stardust-archiving.export.archive.type";
   
   public static final String CARNOT_ARCHIVE_WRITER_CUSTOM = "Archive.Manager.Writer.Type.Class";
   
   public static final String CARNOT_ARCHIVE_READER_CUSTOM = "Archive.Manager.Reader.Type.Class";
   
   public static final String CARNOT_ARCHIVE_READER_MANAGER_TYPE = "stardust-archiving.import.archive.type";
   
   public static final String CARNOT_ARCHIVE_READER_ROOTFOLDER = "stardust-archiving.import.archive.filesystem.root";

   public static final String CARNOT_ARCHIVE_READER_FOLDER_FORMAT = "stardust-archiving.import.archive.filesystem.folderFormat";

   private static volatile ConcurrentHashMap<String, IArchiveWriter> PARTITION_WRITERS = CollectionUtils
         .newConcurrentHashMap();

   public static void resetArchiveManagers()
   {
      PARTITION_WRITERS.clear();
   }

   public static IArchiveWriter getArchiveWriter()
   {
      return getArchiveWriter(SecurityProperties.getPartition().getId());
   }
   
   public static IArchiveWriter getArchiveWriter(String partition)
   {
      if (!PARTITION_WRITERS.containsKey(partition))
      {
         synchronized (ArchiveManagerFactory.class)
         {
            if (!PARTITION_WRITERS.containsKey(partition))
            {
               Map<String, String> preferences = aggregateWritePreferences(null);
               getCurrentId(preferences);
               IArchiveWriter manager = createArchiveWriter(preferences);
               PARTITION_WRITERS.put(partition, manager);
            }
         }
      }
      return PARTITION_WRITERS.get(partition);
   }

   public static IArchiveReader getArchiveReader(Map<String, String> preferences)
   {
      preferences = aggregateReadPreferences(preferences);
      IArchiveReader reader = createArchiveReader(preferences);
      return reader;
   }
   
   private static IArchiveReader createArchiveReader(Map<String, String> preferences)
   {
      String archiveManagerType = preferences.get(CARNOT_ARCHIVE_READER_MANAGER_TYPE);

      ArchiveManagerType type = ArchiveManagerType.valueOf(archiveManagerType.toUpperCase());

      IArchiveReader reader;
      switch (type)
      {
         case FILESYSTEM:
            reader = new ZipArchiveReader(preferences);;
            break;
         case CUSTOM:
            reader = getCustomArchiveReader(preferences);
            break;
         default:
            throw new IllegalArgumentException("Unknown ArchiveReader");
      }
      return reader;
   }
   
   private static IArchiveWriter createArchiveWriter(Map<String, String> preferences)
   {
      String archiveManagerType = preferences.get(CARNOT_ARCHIVE_WRITER_MANAGER_TYPE);

      ArchiveManagerType type = ArchiveManagerType.valueOf(archiveManagerType.toUpperCase());

      IArchiveWriter archiveManager;
      switch (type)
      {
         case FILESYSTEM:
            archiveManager = new ZipArchiveWriter(preferences);
            break;
         case CUSTOM:
            archiveManager = getCustomArchiveWriter(preferences);
            break;
         default:
            throw new IllegalArgumentException("Unknown ArchiveManager");
      }
      return archiveManager;
   }

   private static Map<String, String> aggregateReadPreferences(Map<String, String> preferences)
   {
      if (preferences == null)
      {
         preferences = new HashMap<String, String>();
      }

      setPreference(preferences, CARNOT_ARCHIVE_READER_MANAGER_TYPE, DEFAULT_ARCHIVE_MANAGER);
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_READER_ROOTFOLDER, "");
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_READER_FOLDER_FORMAT,
            ArchiveManagerFactory.DEFAULT_ARCHIVE_FOLDER_FORMAT);
      setPreference(preferences, CARNOT_ARCHIVE_READER_CUSTOM, "");
      preferences.put(ArchiveManagerFactory.CARNOT_ARCHIVE_READER_MANAGER_ID, SecurityProperties.getPartition().getId());
      return preferences;
   }
   
   private static Map<String, String> aggregateWritePreferences(Map<String, String> preferences)
   {
      if (preferences == null)
      {
         preferences = new HashMap<String, String>();
      }

      setPreference(preferences, CARNOT_ARCHIVE_WRITER_MANAGER_TYPE, DEFAULT_ARCHIVE_MANAGER);
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_ROOTFOLDER, "");
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_FOLDER_FORMAT,
            ArchiveManagerFactory.DEFAULT_ARCHIVE_FOLDER_FORMAT);
      setPreference(preferences, CARNOT_ARCHIVE_WRITER_DATE_FORMAT, DEFAULT_DATE_FORMAT);
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_ZIP_FILE_SIZE_MB,
            ArchiveManagerFactory.DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB);
      setPreference(preferences, CARNOT_ARCHIVE_WRITER_CUSTOM, "");
      preferences.put(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_MANAGER_ID, SecurityProperties.getPartition().getId());
      setPreference(preferences, CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE, DEFAULT_AUTO_ARCHIVE);
      setPreference(preferences, CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE_DOCUMENTS, DEFAULT_AUTO_ARCHIVE);
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
      return partition;
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

   private static IArchiveWriter getCustomArchiveWriter(Map<String, String> preferences)
   {
      String custom = preferences.get(CARNOT_ARCHIVE_WRITER_CUSTOM);
      if (StringUtils.isEmpty(custom.trim()))
      {
         throw new IllegalArgumentException(CARNOT_ARCHIVE_WRITER_CUSTOM
               + " must be provided for Custom archive writer");
      }
      try
      {
         Class type = Class.forName(custom);
         Constructor method = type.getConstructor(Map.class);
         IArchiveWriter instance = (IArchiveWriter) method.newInstance(preferences);
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
   
   private static IArchiveReader getCustomArchiveReader(Map<String, String> preferences)
   {
      String custom = preferences.get(CARNOT_ARCHIVE_READER_CUSTOM);
      if (StringUtils.isEmpty(custom.trim()))
      {
         throw new IllegalArgumentException(CARNOT_ARCHIVE_READER_CUSTOM
               + " must be provided for Custom archive reader type");
      }
      try
      {
         Class type = Class.forName(custom);
         Constructor method = type.getConstructor(Map.class);
         IArchiveReader instance = (IArchiveReader) method.newInstance(preferences);
         return instance;
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException(custom + " class not found");
      }
      catch (ClassCastException e)
      {
         throw new IllegalArgumentException(custom + " is not an IArchiveReader."
               + e.getMessage());
      }
      catch (NoSuchMethodException e)
      {
         throw new IllegalArgumentException(custom + " is not an IArchiveReader."
               + e.getMessage());
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(custom + " class could not be instantiated "
               + e.getMessage());
      }
   }

   public enum ArchiveManagerType
   {
      FILESYSTEM, CUSTOM;
   }

   public static String getDateFormat(String partition)
   {
      return getArchiveWriter(partition).getDateFormat();
   }

   public static String getCurrentId(String partition)
   {
      return getArchiveWriter(partition).getArchiveManagerId();
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
      return getArchiveWriter(SecurityProperties.getPartition().getId()).isAutoArchive();
   }
}
