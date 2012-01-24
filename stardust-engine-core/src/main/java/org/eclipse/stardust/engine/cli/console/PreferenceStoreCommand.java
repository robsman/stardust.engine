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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStoreUtils;
import org.eclipse.stardust.engine.core.preferences.Preferences;



/**
 * Allows backup and loading of preferences as a ZIP file.
 */
public class PreferenceStoreCommand extends ConsoleCommand
{

   private static final Logger trace = LogManager.getLogger(PreferenceStoreCommand.class);

   private static final Options argTypes = new Options();

   private static final String SOURCEFILE = "sourceFile";

   private static final String TARGETFILE = "targetFile";

   private static final String BACKUP = "backup";

   private static final String LOAD = "load";

   private static final String LIMIT_SCOPE = "limitScope";

   private static final String LIMIT_MODULEID = "limitModuleId";

   static
   {
      argTypes.register("-" + TARGETFILE, "-t", TARGETFILE,
            "Path to the target ZIP file which will contain the preferences.", true);
      argTypes.register("-" + BACKUP, "-b", BACKUP,
            "Extracts all preferences stored in the PreferenceStore.\n"
                  + "Preferences will be dumped in a .ZIP file stored at " + TARGETFILE
                  + ".\n", false);
      argTypes.register("-" + SOURCEFILE, "-s", SOURCEFILE,
            "The ZIP file containing the preferences.", true);
      argTypes.register(
            "-" + LOAD,
            "-l",
            LOAD,
            "Uploads all preferences from the ZIP archive given with "
                  + SOURCEFILE
                  + " to the\n"
                  + "PreferenceStore\n"
                  + "It is assumed that this structure reflects the structure of a result of the "
                  + BACKUP
                  + " operation.\n"
                  + "This command will replace existing preferences if they are contained in the source ZIP file.\n",
            false);
      argTypes.register(
            "-" + LIMIT_SCOPE,
            null,
            LIMIT_SCOPE,
            "Limits operation to the specified scope. The default is all scopes. Available scopes are PARTITION, REALM or USER.",
            true);
      argTypes.register(
            "-" + LIMIT_MODULEID,
            null,
            LIMIT_MODULEID,
            "Limits operation to the specified moduleId. The default is preferences having any moduleId. Any moduleId can be specified. Optionally the '%' wildcard can be used.",
            true);

      argTypes.addExclusionRule(new String[] {BACKUP, LOAD}, true);
      argTypes.addExclusionRule(new String[] {SOURCEFILE, TARGETFILE}, true);
      argTypes.addExclusionRule(new String[] {BACKUP, SOURCEFILE}, false);
      argTypes.addExclusionRule(new String[] {TARGETFILE, LOAD}, false);
      argTypes.addExclusionRule(new String[] {LIMIT_SCOPE, LOAD}, false);
      argTypes.addExclusionRule(new String[] {LIMIT_MODULEID, LOAD}, false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      // returns 0 on success, -1 on errors when no changes are done to the data
      // and 1 on errors if some changes may have been done to the data
      if (isSet(BACKUP, options))
      {
         boolean force = isSet(ConsoleCommand.GLOBAL_OPTION_FORCE, this.globalOptions);
         String targetFileString = (String) options.get(TARGETFILE);
         try
         {
            return doBackup(targetFileString, force, (String) options.get(LIMIT_SCOPE),
                  (String) options.get(LIMIT_MODULEID));
         }
         catch (ApplicationException e)
         {
            printError("\nERROR: could not backup preferences: ", e);
            return 1;
         }
         catch (Exception e)
         {
            printError("\nERROR: could not backup preferences: ", e);
            return 1;
         }
      }
      else
      {
         // load
         if (options.containsKey(LIMIT_SCOPE))
         {
            print("The scope for the "
                  + LOAD
                  + " operation cannot be defined. All preference entries in the ZIP file will be written to the PreferenceStore.");
            return -1;
         }
         String sourceFileString = (String) options.get(SOURCEFILE);
         try
         {
            return doLoad(sourceFileString);
         }
         catch (ApplicationException e)
         {
            printError("\nERROR: could not load preferences: ", e);
            return 1;
         }
         catch (Exception e)
         {
            printError("\nERROR: could not load preferences: ", e);
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
         print(message + e.getError());

         trace.fatal(message, e);
      }
   }

   private void printError(String message, Exception e)
   {
      print(message + e.getClass().getName() + ":" + e.getMessage());
      for (int i = 0; i < e.getStackTrace().length; i++ )
      {
         print(e.getStackTrace()[i].getClassName() + e.getStackTrace()[i].getMethodName()
               + e.getStackTrace()[i].getLineNumber());
      }
      trace.fatal(message, e);
   }

   private int doBackup(String targetFileString, boolean force, String limitScope,
         String limitModuleId) throws Exception
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

      print("\nBackup preferences to '" + targetFile.getCanonicalPath() + "'.");

      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);

      String partition = serviceFactory.getUserService().getUser().getPartitionId();
      print("\nReading preferences of partition '" + partition + "'.");

      QueryService queryService = serviceFactory.getQueryService();
      List<Preferences> preferencesList = new LinkedList<Preferences>();

      String moduleId = "*";
      if (!StringUtils.isEmpty(limitModuleId))
      {
         moduleId = limitModuleId;
      }

      if (limitScope == null || PreferenceScope.PARTITION.name().equals(limitScope.toUpperCase()))
      {
         preferencesList.addAll(queryService.getAllPreferences(PreferenceQuery.findPreferences(
               PreferenceScope.PARTITION, moduleId, "*")));
      }
      if (limitScope == null || PreferenceScope.REALM.name().equals(limitScope.toUpperCase()))
      {
         preferencesList.addAll(queryService.getAllPreferences(PreferenceQuery.findPreferences(
               PreferenceScope.REALM, moduleId, "*")));
      }
      if (limitScope == null || PreferenceScope.USER.name().equals(limitScope.toUpperCase()))
      {
         preferencesList.addAll(queryService.getAllPreferences(PreferenceQuery.findPreferences(
               PreferenceScope.USER, moduleId, "*")));
      }

      if ( !preferencesList.isEmpty())
      {
         FileOutputStream outputStream = null;
         try
         {
            outputStream = new FileOutputStream(targetFile);

            PreferenceStoreUtils.backupToZipFile(outputStream, preferencesList,
                  serviceFactory);
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
      }
      else
      {
         print("\nFound no matching preferences to backup.");
      }

      return 0;
   }

   private int doLoad(String sourceFileString) throws Exception
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

      print("\nLoad preferences from '" + sourceFile.getCanonicalPath() + "'.");

      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
      String partition = serviceFactory.getUserService().getUser().getPartitionId();
      print("\nLoading the preferences for partition '" + partition + "' from '"
            + sourceFile.getCanonicalPath() + "'.");

      print("\nThe load operation will REPLACE preferences that are already existing; preferences not contained in the ZIP file will stay in the PreferenceStore");
      if ( !force() && !confirm("Do you want to continue?: "))
      {
         return -1;
      }

      FileInputStream inputStream = new FileInputStream(sourceFile);
      PreferenceStoreUtils.loadFromZipFile(inputStream, serviceFactory);
      inputStream.close();

      print("\nLoad completed successfully.");

      return 0;
   }

   public String getSummary()
   {
      return "Allows backup and loading of preferences as a ZIP file.";
   }

}
