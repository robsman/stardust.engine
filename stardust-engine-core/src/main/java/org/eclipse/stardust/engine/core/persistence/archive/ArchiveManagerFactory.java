package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class ArchiveManagerFactory
{
   public static final String MODULE_ID_STARDUST_ARCHIVING = "stardust-archiving";

   public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

   public static final String DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB = "100";

   public static final String DEFAULT_ARCHIVE_FOLDER_FORMAT = "\\yyyy\\MM\\dd\\HH";

   public static final String DEFAULT_AUTO_ARCHIVE = "false";
   
   public static final String DEFAULT_KEY_DESCRIPTOR_ONLY = "false";
   
   public static final  String DEFAULT_AUTO_ARCHIVE_DOCUMENTS = DocumentOption.NONE.name();

   public static final String CARNOT_ARCHIVE_WRITER_ROOTFOLDER = "stardust-archiving.export.archive.filesystem.root";

   public static final String CARNOT_ARCHIVE_WRITER_FOLDER_FORMAT = "stardust-archiving.export.archive.filesystem.folderFormat";

   public static final String CARNOT_ARCHIVE_WRITER_DATE_FORMAT = "stardust-archiving.export.archive.filesystem.dateFormat";

   public static final String CARNOT_ARCHIVE_WRITER_ZIP_FILE_SIZE_MB = "stardust-archiving.export.archive.filesystem.zipFileSize";

   public static final String CARNOT_ARCHIVE_WRITER_MANAGER_ID = "stardust-archiving.export.archive.ID";
   
   public static final String CARNOT_ARCHIVE_READER_MANAGER_ID = "stardust-archiving.import.archive.ID";

   public static final String CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE = "stardust-archiving.export.archive.auto";

   public static final String CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE_DOCUMENTS = "stardust-archiving.export.archive.auto.documents";
   
   public static final String CARNOT_ARCHIVE_WRITER_KEY_DESCRIPTOR_ONLY = "stardust-archiving.export.archive.descriptor.keyonly";
   
   public static final String CARNOT_ARCHIVE_READER_ROOTFOLDER = "stardust-archiving.import.archive.filesystem.root";

   public static final String CARNOT_ARCHIVE_READER_FOLDER_FORMAT = "stardust-archiving.import.archive.filesystem.folderFormat";
   
   public static final String CARNOT_ARCHIVE_READER_AUTO_ARCHIVE_DOCUMENTS = "stardust-archiving.import.archive.auto.documents";

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
               IArchiveWriter manager = createArchiveWriter(null);
               if (manager != null)
               {
                  PARTITION_WRITERS.put(partition, manager);
               }
            }
         }
      }
      return PARTITION_WRITERS.get(partition);
   }

   public static IArchiveReader getArchiveReader(Map<String, String> preferences)
   {
      IArchiveReader reader = createArchiveReader(preferences);
      return reader;
   }

   private static IArchiveReader createArchiveReader(Map<String, String> preferences)
   {
      IArchiveReader reader = ExtensionProviderUtils.getFirstExtensionProvider(IArchiveReader.class);
      if (reader != null)
      {
         preferences = aggregateReadPreferences(preferences);
         reader.init(preferences);
      }
      return reader;
   }
   
   private static IArchiveWriter createArchiveWriter(Map<String, String> preferences)
   {
      IArchiveWriter writer = ExtensionProviderUtils.getFirstExtensionProvider(IArchiveWriter.class);
      if (writer != null)
      {
         preferences = aggregateWritePreferences(preferences);
         writer.init(preferences);
      }
      return writer;
   }

   private static Map<String, String> aggregateReadPreferences(Map<String, String> preferences)
   {
      if (preferences == null)
      {
         preferences = new HashMap<String, String>();
      }

      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_READER_ROOTFOLDER, "");
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_READER_FOLDER_FORMAT,
            ArchiveManagerFactory.DEFAULT_ARCHIVE_FOLDER_FORMAT);
      setPreference(preferences, CARNOT_ARCHIVE_READER_AUTO_ARCHIVE_DOCUMENTS, DEFAULT_AUTO_ARCHIVE_DOCUMENTS);
      preferences.put(ArchiveManagerFactory.CARNOT_ARCHIVE_READER_MANAGER_ID, SecurityProperties.getPartition().getId());
      return preferences;
   }
   
   private static Map<String, String> aggregateWritePreferences(Map<String, String> preferences)
   {
      if (preferences == null)
      {
         preferences = new HashMap<String, String>();
      }

      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_ROOTFOLDER, "");
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_FOLDER_FORMAT,
            ArchiveManagerFactory.DEFAULT_ARCHIVE_FOLDER_FORMAT);
      setPreference(preferences, CARNOT_ARCHIVE_WRITER_DATE_FORMAT, DEFAULT_DATE_FORMAT);
      setPreference(preferences, ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_ZIP_FILE_SIZE_MB,
            ArchiveManagerFactory.DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB);
      preferences.put(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_MANAGER_ID, SecurityProperties.getPartition().getId());
      setPreference(preferences, CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE, DEFAULT_AUTO_ARCHIVE);
      setPreference(preferences, CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE_DOCUMENTS, DEFAULT_AUTO_ARCHIVE_DOCUMENTS);
      setPreference(preferences, CARNOT_ARCHIVE_WRITER_KEY_DESCRIPTOR_ONLY, DEFAULT_KEY_DESCRIPTOR_ONLY);
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

   public static String getDateFormat(String partition)
   {
      IArchiveWriter archiveWriter = getArchiveWriter(partition);
      return archiveWriter.getDateFormat();
   }

   public static String getCurrentId(String partition)
   {
      IArchiveWriter archiveWriter = getArchiveWriter(partition);
      return archiveWriter.getArchiveManagerId();
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
      IArchiveWriter archiveWriter = getArchiveWriter(SecurityProperties.getPartition().getId());
      if (archiveWriter == null)
      {
         return false;
      }
      return archiveWriter.isAutoArchive();
   }

   public static DocumentOption getDocumentOption()
   {
      IArchiveWriter archiveWriter = getArchiveWriter(SecurityProperties.getPartition().getId());
      return archiveWriter.getDocumentOption();
   }

   public static boolean isKeyDescriptorsOnly()
   {
      IArchiveWriter archiveWriter = getArchiveWriter(SecurityProperties.getPartition().getId());
      return archiveWriter.isKeyDescriptorsOnly();
   }
}
