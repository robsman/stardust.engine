/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.console;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.persistence.archive.ArchiveFilter;
import org.eclipse.stardust.engine.core.persistence.archive.IArchive;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author jsaayman
 * @version $Revision$
 */
public class ImportCommand extends BaseExportImportCommand
{
   static
   {
      argTypes.register("-" + CONCURRENT_BATCHES, null, CONCURRENT_BATCHES,
            "Defines how many batches can be imported concurrently", true);

      argTypes
            .register(
                  "-" + PARTITION,
                  null,
                  PARTITION,
                  "Optionally specifies the partition(s) to be imported into.\n"
                        + "Accepts as argument a single partition ID or a comma separated list of\npartition IDs.\n"
                        + "If this parameter is not used, the import command of sysconsole has\nan effect only on the default partition.",
                  true);

      argTypes.register("-" + PROCESSES_BY_OID, null, PROCESSES_BY_OID,
            "Imports specified process instances (comma separated list of\n" + "OIDs or a range eg: 1-1000).",
            true);

      argTypes.register("-" + PROCESS_DEFINITION_IDS, "-procDef", PROCESS_DEFINITION_IDS,
            "Imports process instances for specified list of process definition IDs(comma separated list of\n"
                  + "IDs).", true);

      argTypes.register("-" + MODEL_IDS, "-model", MODEL_IDS,
            "Imports process instances for specified list of model IDs(comma separated list of\n"
                  + "IDs).", true);

      argTypes
            .register(
                  "-" + FROM_DATE,
                  "-fd",
                  FROM_DATE,
                  "Restricts import to process instances started after the\n"
                        + "given date (always inclusive).\n"
                        + "The specified date must conforms to ISO date patterns\n"
                        + "(i.e. \"2005-12-31\", \"2005-12-31 23:59\" or \"2005-12-31T23:59:59:999\"),\n"
                        + "or \""
                        + DateUtils.getNoninteractiveDateFormat().toPattern()
                        + "\" for backward compatibility."
                        + "If fromDate is not provided and toDate is provided fromDate defaults to 1 January 1970",
                  true);

      argTypes
            .register(
                  "-" + TO_DATE,
                  "-td",
                  TO_DATE,
                  "Restricts import to process instances terminated before the\n"
                        + "given date (always inclusive).\n"
                        + "The specified date must conforms to ISO date patterns\n"
                        + "(i.e. \"2005-12-31\", \"2005-12-31 23:59\" or \"2005-12-31T23:59:59:999\"),\n"
                        + "or \""
                        + DateUtils.getNoninteractiveDateFormat().toPattern()
                        + "\" for backward compatibility."
                        + "If toDate is not provided and fromDate is provided toDate defaults to now.",
                  true);
      

      argTypes
            .register(
                  "-" + DATE_DESCRIPTORS,
                  "-ddscr",
                  DATE_DESCRIPTORS,
                  "Restricts any operation to process instances that has the specified descriptor values. Use this option to specify descriptors that have date values.\n"
                        + "The specified date must conforms to ISO date patterns\n"
                        + "(i.e. \"2005-12-31\", \"2005-12-31 23:59\" or \"2005-12-31T23:59:59:999\"),\n"
                        + "or \""
                        + DateUtils.getNoninteractiveDateFormat().toPattern()
                        + "\" for backward compatibility.",
                  true);
      
      argTypes
      .register(
            "-" + DESCRIPTORS,
            "-dscr",
            DESCRIPTORS,
            "Restricts any operation to process instances that has the descriptor values. Use this option to specify all non-date descriptors.",
            true);

      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, MODEL_IDS}, false);
   }

   public int run(Map options)
   {
      final Date fromDate = getFromDate(options);
      final Date toDate = getToDate(options);
      final List<Long> processOids = getProcessOids(options);
      List<String> partitionIds = getPartitions(options);
      final int concurrentBatches = getConcurrentBatches(options);
      final HashMap<String, Object> descriptors = getDescriptors(options);
      final Collection<String> processDefinitionIds = getProcessDefinitionIds(options);
      final Collection<String> modelIds = getModelIds(options); 
      for (final String partitionId : partitionIds)
      {
         Date start = new Date();
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(SecurityProperties.PARTITION, partitionId);
         final ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions,
               properties);
         final WorkflowService workflowService = serviceFactory.getWorkflowService();

         final List<IArchive> archives = findArchives(serviceFactory, processDefinitionIds, modelIds, fromDate, toDate, processOids, descriptors);
         print("Starting Import for partition " + partitionId + ". Found " + archives.size()
               + " archives to import.");
         
         if (CollectionUtils.isNotEmpty(archives))
         {
            ExecutorService executor = Executors.newFixedThreadPool(concurrentBatches);
            List<Future<Integer>> results = new ArrayList<Future<Integer>>();
            for (IArchive archive : archives)
            {
               final IArchive currentArchive = archive;
               Callable<Integer> exportCallable = new Callable<Integer>()
               {
                  @Override
                  public Integer call() throws Exception
                  {
                     ImportMetaData importMetaData = null;
                     if (CollectionUtils.isNotEmpty(archives))
                     {
                        importMetaData = validateImport(currentArchive, workflowService,descriptors);
                     }
                     int count = 0;
                     if (StringUtils.isEmpty(importMetaData.getErrorMessage()))
                     {
                        count = importFile(processDefinitionIds, modelIds, fromDate, toDate, processOids,descriptors, partitionId,
                        importMetaData, serviceFactory, currentArchive);
                     }
                     return count;
                  }

               };
               results.add(executor.submit(exportCallable));
            }

            print("Import Submitted");
            int count = 0;
            for (Future<Integer> result : results)
            {
               try
               {
                  count += result.get();
               }
               catch (Exception e)
               {
                  print("Unexpected Exception during Import " + e.getMessage());
                  e.printStackTrace();
               }
            }
            Date end = new Date();
            long millis = end.getTime() - start.getTime();
            String duration = String.format(
                  "%d min, %d sec",
                  TimeUnit.MILLISECONDS.toMinutes(millis),
                  TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                              .toMinutes(millis)));
            print("Import Complete for partition " + partitionId
                  + ". Imported a total of " + count
                  + " process instances into partition " + partitionId + ". Time taken: "
                  + duration);
         }
      }
      print("Import of all partitions complete");
      return 0;
   }

   private ImportMetaData validateImport(IArchive archive, WorkflowService workflowService,
         HashMap<String, Object> descriptors)
   {
      ImportMetaData importMetaData;
      ArchiveFilter filter = new ArchiveFilter(null, null, null, null, null, null, null);
      ImportProcessesCommand importCommand = new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, archive, filter, null);
      importMetaData = (ImportMetaData) workflowService.execute(importCommand);
      if (StringUtils.isEmpty(importMetaData.getErrorMessage()))
      {
         print("Model validated, proceding with import for " + archive.getArchiveKey());
      }
      else
      {
         print("Model validation failed. " + importMetaData.getErrorMessage() + ". Import can not be done for archive: "
               + archive.getArchiveKey());
      }
      return importMetaData;
   }

   private int importFile(final Collection<String> processDefinitionIds,
         final Collection<String> modelIds, Date fromDate, Date toDate, List<Long> processOids, HashMap<String, Object> descriptors,
         String partitionId, ImportMetaData importMetaData,
         ServiceFactory serviceFactory, IArchive archive)
   {
      int count = 0;

      if (archive != null)
      {
         ArchiveFilter filter = new ArchiveFilter(modelIds, processDefinitionIds,processOids, null, fromDate, toDate, descriptors);
         ImportProcessesCommand command = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
                  archive, filter, importMetaData);
         count = (Integer) serviceFactory.getWorkflowService().execute(command);

         print("Imported " + count + " process instances into partition " + partitionId
               + " from archive: " + archive.getArchiveKey());
      }
      return count;
   }

   private List<IArchive> findArchives(final ServiceFactory serviceFactory, final Collection<String> processDefinitionIds,
         final Collection<String> modelIds, final Date fromDate, final Date toDate, final List<Long> processOids,
         HashMap<String, Object> descriptors)
   {
      ArchiveFilter filter = new ArchiveFilter(modelIds, processDefinitionIds, processOids, null, fromDate, toDate, descriptors);
      ImportProcessesCommand command = new ImportProcessesCommand(filter);
      List<IArchive> archives = (List<IArchive>) serviceFactory.getWorkflowService().execute(command);
      return archives;
   }

   public void printCommand(Map options)
   {
      print("Imports process instances:\n");
   }

   public String getSummary()
   {
      return "Imports process instances.";
   }
}
