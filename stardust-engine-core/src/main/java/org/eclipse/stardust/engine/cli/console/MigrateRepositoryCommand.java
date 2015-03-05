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

import java.util.Map;

import org.eclipse.stardust.common.TimeMeasure;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationJobInfo;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


public class MigrateRepositoryCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String BATCHSIZE = "batchSize";

   private static final String TIMELIMIT = "timeLimit";

   static
   {
      argTypes.register("-batchSize", "-b", BATCHSIZE,
            "Migrates repository using the defined batch size. (default is 500)", true);
      argTypes.register(
            "-timeLimit",
            "-t",
            TIMELIMIT,
            "Defines a time limit in minutes after which the migration process will be stopped. A value of 0 stands for unlimited. (default is 0)",
            true);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      int batchSize = 500;
      if (options.containsKey(BATCHSIZE))
      {
         batchSize = getIntegerOption(options, BATCHSIZE);
      }
      long timeLimit = 0;
      if (options.containsKey(TIMELIMIT))
      {
         timeLimit = getIntegerOption(options, TIMELIMIT);
      }

      print("Please ensure there is no other write access on the repository to avoid race conditions!");
      if ( !force()
            && !confirm("You are going to migrate the complete repository. Continue?"))
      {
         return -1;
      }
      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
      try
      {
         final TimeMeasure timer = new TimeMeasure();
         DocumentManagementService dms = serviceFactory.getDocumentManagementService();

         RepositoryMigrationReport report = dms.migrateRepository(0, false);
         int currentRepositoryVersion = report.getCurrentRepositoryVersion();
         int targetRepositoryVersion = report.getTargetRepositoryVersion();
         int currentRepositoryStructureVersion = report.getCurrentRepositoryStructureVersion();
         int targetRepositoryStructureVersion = report.getTargetRepositoryStructureVersion();

         if (currentRepositoryVersion >= targetRepositoryVersion
               && currentRepositoryStructureVersion >= targetRepositoryStructureVersion)
         {
            print("No migration required. Current version: " + currentRepositoryVersion
                  + " Target version: " + targetRepositoryVersion
                  + ". Current structure version: " + currentRepositoryStructureVersion
                  + " Target structure version: " + targetRepositoryStructureVersion
                  + ".");
            return 0;
         }

         if (currentRepositoryVersion < targetRepositoryVersion)
         {
            print("Repository requires miration from version: "
                  + currentRepositoryVersion + " to " + targetRepositoryVersion + ".");
         }
         if (currentRepositoryStructureVersion < targetRepositoryStructureVersion)
         {
            print("Repository requires miration from structure version: "
                  + currentRepositoryStructureVersion + " to "
                  + targetRepositoryStructureVersion + ".");
         }

         boolean cancel = false;
         while (batchSize > 0 && !cancel)
         {
            report = dms.migrateRepository(0, true);

            RepositoryMigrationJobInfo currentMigrationJob = report.getCurrentMigrationJob();
            if (currentMigrationJob == null)
            {
               cancel = true;
            }
            else
            {
               long totalCount = report.getTotalCount();

               print("The next migration job is: " + currentMigrationJob.getName()
                     + "( from version " + currentMigrationJob.getFromVersion()
                     + ", to version " + currentMigrationJob.getToVersion() + ").");
               print("In this migration step " + totalCount
                     + " resources need to be processed.");
               print("Starting migration job with a batchSize of " + batchSize + ".");

               boolean stepComplete = false;
               long totalResourcesDone = 0;
               while ( !stepComplete && !cancel)
               {
                  report = dms.migrateRepository(batchSize, false);
                  totalResourcesDone += report.getResourcesDone();
                  print("Resources Processed: " + totalResourcesDone + " of "
                        + totalCount);
                  if (report.getCurrentRepositoryVersion() > currentRepositoryVersion)
                  {
                     stepComplete = true;
                     print("Migration step complete. New repository version is "
                           + report.getCurrentRepositoryVersion() + ".");
                     currentRepositoryVersion = report.getCurrentRepositoryVersion();
                  }
                  else if (report.getCurrentRepositoryStructureVersion() > currentRepositoryStructureVersion)
                  {
                     stepComplete = true;
                     print("Migration step complete. New repository structure version is "
                           + report.getCurrentRepositoryStructureVersion() + ".");
                     currentRepositoryStructureVersion = report.getCurrentRepositoryStructureVersion();
                  }
                  long runtime = timer.stop().getDurationInMillis();
                  if (timeLimit > 0 && runtime > timeLimit * 1000 * 60)
                  {
                     cancel = true;
                     print("Time limit exceded! The migration was stopped after "
                           + runtime / 1000 / 60 + " Minutes.");
                  }
               }
            }
         }
      }
      finally
      {
         serviceFactory.close();
      }

      return 0;
   }

   public String getSummary()
   {
      return "Migrates the document repository to the newest version.";
   }
}
