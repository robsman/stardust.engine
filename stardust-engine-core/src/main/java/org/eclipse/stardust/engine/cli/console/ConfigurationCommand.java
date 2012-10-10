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
package org.eclipse.stardust.engine.cli.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;


/**
 * Allows backup and loading of partition configuration as a ZIP file.
 */
public class ConfigurationCommand extends ConsoleCommand
{

   private static final Logger trace = LogManager.getLogger(ConfigurationCommand.class);

   private static final Options argTypes = new Options();

   private static final String SOURCEFILE = "sourceFile";

   private static final String TARGETFILE = "targetFile";

   private static final String BACKUP = "backup";

   private static final String LOAD = "load";

   private static final String SCOPE = "scope";

   private static final String IGNORE_EMPTY_FOLDERS = "ignoreEmptyFolders";

   static
   {
      argTypes.register("-" + TARGETFILE, "-t", TARGETFILE,
            "Path to the target ZIP file which will contain the configuration.", true);
      argTypes.register(
            "-" + BACKUP,
            "-b",
            BACKUP,
            "Extracts all files stored in the configuration area of the partition.\n"
                  + "Configuration files will be dumped in a .ZIP file stored at "
                  + TARGETFILE
                  + ".\n"
                  + "The extracted file structure represents the structure of the tree view for the\n"
                  + "Resource Management View in the Administration Portal.\n", false);
      argTypes.register("-" + SOURCEFILE, "-s", SOURCEFILE,
            "The ZIP file containing the configuration.", true);
      argTypes.register(
            "-" + LOAD,
            "-l",
            LOAD,
            "Uploads all configuration files from the ZIP archive given with "
                  + SOURCEFILE
                  + " to the\n"
                  + "configuration area of the partition\n"
                  + "It is assumed that this structure reflects the structure of a result of the "
                  + BACKUP
                  + " operation.\n"
                  + "This command will replace the entire configuration; all files of the old configuration will be removed.\n"
                  + "Configuration files will be dumped in a .ZIP file stored at "
                  + TARGETFILE + ".\n", false);
      argTypes.register(
            "-" + SCOPE,
            null,
            SCOPE,
            "Changes the scope for the configuration operation. The default scope is partition.",
            true);
      argTypes.register("-" + IGNORE_EMPTY_FOLDERS, "-i", IGNORE_EMPTY_FOLDERS,
            "Ignore empty folders (default is to extract and import empty folders).",
            false);

      argTypes.addExclusionRule(new String[] {BACKUP, LOAD}, true);
      argTypes.addExclusionRule(new String[] {SOURCEFILE, TARGETFILE}, true);
      argTypes.addExclusionRule(new String[] {BACKUP, SOURCEFILE}, false);
      argTypes.addExclusionRule(new String[] {TARGETFILE, LOAD}, false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      boolean ignoreEmptyFolders = false;
      if ((Boolean) options.get(IGNORE_EMPTY_FOLDERS) != null)
      {
         ignoreEmptyFolders = ((Boolean) options.get(IGNORE_EMPTY_FOLDERS)).booleanValue();
      }

      if (options.containsKey(SCOPE))
      {
         print("The scope for this operation is always partition. Specifying other scopes via a parameter is not implemented.");
         return 1;
      }

      // returns 0 on success, -1 on errors when no changes are done to the data
      // and 1 on errors if some changes may have been done to the data
      if (isSet(BACKUP, options))
      {
         boolean force = isSet(ConsoleCommand.GLOBAL_OPTION_FORCE, this.globalOptions);
         String targetFileString = (String) options.get(TARGETFILE);
         try
         {
            return doBackup(targetFileString, force, ignoreEmptyFolders);
         }
         catch (ApplicationException e)
         {
            printError("\nERROR: could not backup configuration: ", e);
            return 1;
         }
         catch (Exception e)
         {
            printError("\nERROR: could not backup configuration: ", e);
            return 1;
         }
      }
      else
      {
         // load
         String sourceFileString = (String) options.get(SOURCEFILE);
         try
         {
            return doLoad(sourceFileString, ignoreEmptyFolders);
         }
         catch (ApplicationException e)
         {
            printError("\nERROR: could not load configuration: ", e);
            return 1;
         }
         catch (Exception e)
         {
            printError("\nERROR: could not load configuration: ", e);
            return 1;
         }
      }
   }

   private boolean isSet(String booleanOption, Map options)
   {
      Boolean value = (Boolean) options.get(booleanOption);
      if (value == null)
      {
         return false;
      }
      else
      {
         return ((Boolean) value).booleanValue();
      }
   }

   private void printError(String message, ApplicationException e)
   {
      if (e.getError() != null)
      {
         print("\nERROR: could not backup: " + e.getError());
         
         trace.fatal(message, e);
      }
   }

   private void printError(String message, Exception e)
   {
      print("\nERROR: could not backup: " + e.getClass().getName() + ":" + e.getMessage());
      for (int i = 0; i < e.getStackTrace().length; i++ )
      {
         print(e.getStackTrace()[i].getClassName()+e.getStackTrace()[i].getMethodName()+e.getStackTrace()[i].getLineNumber());
      }
      trace.fatal(message, e);
   }

   private int doBackup(String targetFileString, boolean force, boolean ignoreEmptyFolders)
         throws Exception
   {
      File targetFile = new File(targetFileString);
      if (targetFile.exists() && !force)
      {
         print("\nERROR: '" + targetFileString
               + "' exists, specify -force to overwrite it.");
         return -1;
      }
      if (targetFile.exists() && !targetFile.isFile())
      {
         print("\nERROR: can not write to target file '" + targetFileString
               + "', it is not a regular file.");
         return -1;
      }
      if (targetFile.exists() && !targetFile.canWrite())
      {
         print("\nERROR: can not write to target file '" + targetFileString
               + "', check permissions.");
         return -1;
      }

      print("\nBackup configuration to '" + targetFile.getCanonicalPath() + "'.");
      if (ignoreEmptyFolders)
      {
         print("Empty folders will be ignored.");
      }
      else
      {
         print("Empty folders will be exported.");
      }

      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
      DocumentManagementService documentManagementService = serviceFactory.getDocumentManagementService();
      String partition = serviceFactory.getUserService().getUser().getPartitionId();
      print("\nReading configuration of partition '" + partition + "'.");

      /* rootFolder set to artifacts folder for consistency with portal configuration export functionality
       * see Ticket CRNT-26589 for details
       */
      String rootFolderId = "/artifacts/";
      Folder rootFolder = documentManagementService.getFolder(rootFolderId,
            Folder.LOD_LIST_MEMBERS);

      if (rootFolder == null)
      {
         print("\nERROR: partition configuration folder does not exist in the repository, no backup file can be created.");
         return -1;
      }

      FileOutputStream outputStream = null;
      try
      {
         outputStream = new FileOutputStream(targetFile);
         DmsUtils.backupToZipFile(rootFolder, outputStream, documentManagementService,
               ignoreEmptyFolders, partition);
      }
      catch (Exception e)
      {
         // cleanup possibly incomplete file on problems
         targetFile.delete();
         throw e;
      }
      finally
      {
         outputStream.close();
      }

      print("\nBackup completed successfully.");

      return 0;
   }

   private int doLoad(String sourceFileString, boolean ignoreEmptyFolders)
         throws Exception
   {
      File sourceFile = new File(sourceFileString);
      if ( !sourceFile.exists())
      {
         print("\nERROR: '" + sourceFileString
               + "' does not exist, specify -force to overwrite it.");
         return -1;
      }
      if ( !sourceFile.canRead())
      {
         print("\nERROR: can not read from target file '" + sourceFileString
               + "', check permissions.");
         return -1;
      }

      print("\nLoad configuration from '" + sourceFile.getCanonicalPath() + "'.");
      if (ignoreEmptyFolders)
      {
         print("Empty folders will be ignored.");
      }
      else
      {
         print("Empty folders will be created.");
      }

      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
      DocumentManagementService documentManagementService = serviceFactory.getDocumentManagementService();
      String partition = serviceFactory.getUserService().getUser().getPartitionId();
      print("\nLoading the configuration for partition '" + partition + "' from '"
            + sourceFile.getCanonicalPath() + "'.");

      // String partitionFolderPath = DocumentRepositoryFolderNames.REPOSITORY_ROOT_FOLDER
      // + DocumentRepositoryFolderNames.PARTITIONS_FOLDER + partition;
      String rootFolderPath = "/artifacts/";
      Folder partitionFolder = documentManagementService.getFolder(rootFolderPath,
            Folder.LOD_LIST_MEMBERS);

      if (partitionFolder != null
            && (partitionFolder.getDocumentCount() > 0 || partitionFolder.getFolderCount() > 0))
      {
         print("\nPartition '"
               + partition
               + "' already contains configuration. The load operation will REPLACE the existing configuration.");
         if ( !force() && !confirm("Do you want to continue?: "))
         {
            return -1;
         }
         // completely clean old configuration for this partition
         DmsUtils.cleanupFolder(partitionFolder, documentManagementService);
      }

      FileInputStream inputStream = new FileInputStream(sourceFile);
      DmsUtils.loadFromZipFile(rootFolderPath, inputStream, documentManagementService,
            ignoreEmptyFolders);
      inputStream.close();
      
      print("\nLoad completed successfully.");

      return 0;
   }

   public String getSummary()
   {
      return "Allows backup and loading of partition configuration as a ZIP file.";
   }

}
