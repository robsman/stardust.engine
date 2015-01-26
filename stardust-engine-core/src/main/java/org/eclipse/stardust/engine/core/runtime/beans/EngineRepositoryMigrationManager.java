package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.extensions.dms.data.DmsMigrationReportBean;
import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IFileInfo;
import org.eclipse.stardust.vfs.IMigrationReport;
import org.eclipse.stardust.vfs.impl.FileInfo;

public class EngineRepositoryMigrationManager
{
   private static final Logger trace = LogManager.getLogger(EngineRepositoryMigrationManager.class);

   public static final String ENGINE_CONFIG_DOCUMENT_NAME = "engine-configuration.properties";

   public static final String CONFIG_FILE_CONTENT_TYPE = "text/plain";

   public static final String ENGINE_REPOSITORY_STRUCTURE_PROPERTY = "org.eclipse.stardust.engine.repository.structure.version";

   public static final int ENGINE_REPOSITORY_STRUCTURE_TARGET_VERSION = 1;

   public static RepositoryMigrationReport handleEngineMigration(int batchSize, boolean evaluateTotalCount,
         IMigrationReport migrationReport, IDocumentRepositoryService vfs,
         String partitionPrefix)
   {
      RepositoryMigrationReport report = null;
      int structureVersion = 0;
      if (migrationReport.getCurrentRepositoryVersion() == migrationReport.getTargetRepositoryVersion())
      {
         structureVersion = getRepositoryStructureVersion(vfs, partitionPrefix);

         if (structureVersion == 0)
         {
            report = migrate(0, 1, batchSize, evaluateTotalCount, vfs, partitionPrefix, migrationReport);

            if (report != null && report.getCurrentRepositoryStructureVersion() == report.getTargetRepositoryStructureVersion())
            {
               setRepositoryStructureVersion(
                     report.getCurrentRepositoryStructureVersion(), vfs, partitionPrefix);
            }
         }
      }
      if (report == null)
      {
         // no migration done
         report = new DmsMigrationReportBean(migrationReport, structureVersion,
               ENGINE_REPOSITORY_STRUCTURE_TARGET_VERSION, null , null, null);
      }
      return report;
   }

   private static RepositoryMigrationReport migrate(int sourceVersion, int targetVersion, int batchSize, boolean evaluateTotalCount, IDocumentRepositoryService vfs, String partitionPrefix, IMigrationReport migrationReport)
   {
      long resourcesDone = 0;
      long totalCount = -1;
      if (sourceVersion == 0 && targetVersion == 1)
      {
         int versionAfterMigrationRun = sourceVersion;
         PortalMetaDataPropertiesMigrationJob portalMigrationJob = new PortalMetaDataPropertiesMigrationJob(vfs, partitionPrefix);

         if (evaluateTotalCount)
         {
            totalCount = portalMigrationJob.getTotalCount();
         }
         resourcesDone = portalMigrationJob.doMigration(batchSize);
         if (portalMigrationJob.isFinished())
         {
            versionAfterMigrationRun = targetVersion;
         }
         return new DmsMigrationReportBean(migrationReport, versionAfterMigrationRun, ENGINE_REPOSITORY_STRUCTURE_TARGET_VERSION, totalCount, resourcesDone, PortalMetaDataPropertiesMigrationJob.JOB_INFO);
      }
      // no fitting migration job
      return null;
   }

   private static int getRepositoryStructureVersion(IDocumentRepositoryService vfs, String partitionPrefix)
   {
      int version = -1;
      final String configFolder = partitionPrefix + "/"
            + DocumentRepositoryFolderNames.CONFIGURATION_FOLDER;

      IFile file = vfs.getFile(configFolder + "/" + ENGINE_CONFIG_DOCUMENT_NAME);

      if (file == null)
      {
         // No version exists
         version = 0;
      }
      else
      {
         Properties configProperties = propertyFromBytes(vfs.retrieveFileContent(file));
         version = Integer.parseInt((String) configProperties.get(ENGINE_REPOSITORY_STRUCTURE_PROPERTY));
      }
      return version;
   }

   private static void setRepositoryStructureVersion(int newVersion, IDocumentRepositoryService vfs, String partitionPrefix)
   {
      final String configFolder = partitionPrefix + "/"
      + DocumentRepositoryFolderNames.CONFIGURATION_FOLDER;
      DmsVfsConversionUtils.ensureFolderHierarchyExists(vfs, configFolder);

      IFileInfo configFile = new FileInfo(ENGINE_CONFIG_DOCUMENT_NAME);
      configFile.setContentType(CONFIG_FILE_CONTENT_TYPE);

      Properties configProperties = new Properties();

      configProperties.put(ENGINE_REPOSITORY_STRUCTURE_PROPERTY, Integer.valueOf(newVersion).toString());

      vfs.createFile(configFolder, configFile, propertyToBytes(configProperties), null);
   }

   private static Properties propertyFromBytes(byte[] retrieveFileContent)
   {
      Properties properties = new Properties();

      try
      {
         properties.load(new ByteArrayInputStream(retrieveFileContent));
      }
      catch (IOException e)
      {
         log(e);
      }

      return properties;
   }

   private static byte[] propertyToBytes(Properties configProperties)
   {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try
      {
         configProperties.store(os, null);
      }
      catch (IOException e)
      {
         log(e);
      }
      return os.toByteArray();
   }

   private static void log(Exception e)
   {
     trace.error(e);
   }

}
