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
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.persistence.archive.IArchive;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author jsaayman
 * @version $Revision$
 */
public class ImportCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final int DEFAULT_CONCURRENT_BATCHES = 10;

   private static final String PARTITION = "partition";

   private static final String PROCESSES_BY_OID = "processes";

   private static final String PROCESS_MIN_OID = "processMin";
   
   private static final String PROCESS_MAX_OID = "processMax";

   private static final String FROM_DATE = "fromDate";

   private static final String TO_DATE = "toDate";

   private static final String CONCURRENT_BATCHES = "concurrentBatches";

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
            "Imports specified process instances (comma separated list of\n" + "OIDs).",
            true);

      argTypes.register("-" + PROCESS_MIN_OID, null, PROCESS_MIN_OID,
            "Imports all processes with OID great or equal to this number and less or equal to processMax.", true);


      argTypes.register("-" + PROCESS_MAX_OID, null, PROCESS_MAX_OID,
            "Imports all processes with OID great or equal to processMin and less or equal to this number.", true);

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

      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {PARTITION}, false);
      argTypes.addExclusionRule(new String[] {FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {CONCURRENT_BATCHES}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MIN_OID, PROCESSES_BY_OID}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MAX_OID, PROCESSES_BY_OID}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MIN_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MIN_OID, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MAX_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MAX_OID, TO_DATE}, false);
      argTypes.addInclusionRule(PROCESS_MIN_OID, PROCESS_MAX_OID);
      argTypes.addInclusionRule(PROCESS_MAX_OID, PROCESS_MIN_OID);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      final Date fromDate = getFromDate(options);
      final Date toDate = getToDate(options);
      final List<Long> processOids = getProcessOids(options);
      List<String> partitionIds = getPartitions(options);
      final int concurrentBatches = getConcurrentBatches(options);
      for (final String partitionId : partitionIds)
      {
         Date start = new Date();
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(SecurityProperties.PARTITION, partitionId);
         final ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions,
               properties);
         final WorkflowService workflowService = serviceFactory.getWorkflowService();

         final List<IArchive> archives = findArchives(serviceFactory, fromDate, toDate, processOids);
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
                        importMetaData = validateImport(currentArchive, workflowService);
                     }
                     int count = importFile(fromDate, toDate, processOids, partitionId,
                           importMetaData, serviceFactory, currentArchive);
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

   private int getConcurrentBatches(Map options)
   {
      Long concurrent = Options.getLongValue(options, CONCURRENT_BATCHES);
      if (concurrent == null || concurrent < 1)
      {
         return DEFAULT_CONCURRENT_BATCHES;
      }
      return concurrent.intValue();
   }

   private Date getFromDate(Map options)
   {
      Date fromDate = Options.getDateValue(options, FROM_DATE);
      if ((null == fromDate) && options.containsKey(FROM_DATE))
      {
         throw new PublicException(
               BpmRuntimeError.CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_TIMESTAMP
                     .raise(options.get(FROM_DATE)));
      }
      return fromDate;
   }

   private Date getToDate(Map options)
   {
      Date toDate = Options.getDateValue(options, TO_DATE);
      if ((null == toDate) && options.containsKey(TO_DATE))
      {
         throw new PublicException(
               BpmRuntimeError.CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_TIMESTAMP
                     .raise(options.get(TO_DATE)));
      }
      return toDate;
   }

   private List<Long> getProcessOids(Map options)
   {
      List<Long> processOids;
      if (options.containsKey(PROCESSES_BY_OID))
      {
         String processInstanceIds = (String) options.get(PROCESSES_BY_OID);
         processOids = new ArrayList<Long>();
         splitListLong(processInstanceIds, processOids);
      }
      else if (options.containsKey(PROCESS_MIN_OID) && options.containsKey(PROCESS_MAX_OID))
      {
         processOids = new ArrayList<Long>();
         long min  = Options.getLongValue(options, PROCESS_MIN_OID);
         long max = Options.getLongValue(options, PROCESS_MAX_OID);
         for (long i = min; i <= max; i++)
         {
            processOids.add(i);
         }
      }
      else
      {
         processOids = null;
      }
      return processOids;
   }

   private List<String> getPartitions(Map options)
   {
      // evaluate partition, fall back to default partition, if configured
      String partitionSpec = (String) options.get(PARTITION);
      if (StringUtils.isEmpty(partitionSpec))
      {
         partitionSpec = ParametersFacade.instance().getString(
               SecurityProperties.DEFAULT_PARTITION,
               PredefinedConstants.DEFAULT_PARTITION_ID);
      }
      List<String> partitionIds = new ArrayList<String>();
      splitListString(partitionSpec, partitionIds);
      if (partitionIds.isEmpty())
      {
         throw new PublicException(
               BpmRuntimeError.CLI_NO_AUDITTRAIL_PARTITION_SPECIFIED.raise());
      }
      return partitionIds;
   }
   
   private ImportMetaData validateImport(IArchive archive, WorkflowService workflowService)
   {
      ImportMetaData importMetaData;

      ImportProcessesCommand importCommand = new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, archive, null);
      importMetaData = (ImportMetaData) workflowService.execute(importCommand);
      if (importMetaData != null)
      {
         print("Model validated");
      }
      else
      {
         print("Model validation failed. No import can be done. Model file: "
               + archive.getArchiveKey());
      }
      return importMetaData;
   }

   private int importFile(Date fromDate, Date toDate, List<Long> processOids,
         String partitionId, ImportMetaData importMetaData,
         ServiceFactory serviceFactory, IArchive archive)
   {
      int count = 0;

      if (archive != null)
      {
         ImportProcessesCommand command;
         if (processOids != null)
         {
            command = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
                  archive, processOids, importMetaData);
         }
         else if (fromDate != null || toDate != null)
         {
            command = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
                  archive, fromDate, toDate, importMetaData);
         }
         else
         {
            command = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
                  archive, importMetaData);
         }
         count = (Integer) serviceFactory.getWorkflowService().execute(command);

         print("Imported " + count + " process instances into partition " + partitionId
               + " from archive: " + archive.getArchiveKey());
      }
      return count;
   }

   private List<IArchive> findArchives(final ServiceFactory serviceFactory, final Date fromDate, final Date toDate, final List<Long> processOids)
   {
      ImportProcessesCommand command;
      if (processOids != null)
      {
         command = new ImportProcessesCommand(processOids);
      }
      else if (fromDate != null || toDate != null)
      {
         command = new ImportProcessesCommand(fromDate, toDate);
      }
      else
      {
         command = new ImportProcessesCommand();
      }
      List<IArchive> archives = (List<IArchive>) serviceFactory.getWorkflowService().execute(command);
      return archives;
   }

   private void splitListString(String partitionSpec, List<String> partitionIds)
   {
      for (Iterator i = StringUtils.split(partitionSpec, ","); i.hasNext();)
      {
         String id = (String) i.next();
         id = getListValue(id);
         partitionIds.add(id);
      }
   }

   private void splitListLong(String partitionSpec, List<Long> partitionIds)
   {
      for (Iterator i = StringUtils.split(partitionSpec, ","); i.hasNext();)
      {
         String id = (String) i.next();
         id = getListValue(id);
         partitionIds.add(new Long(id));
      }
   }

   private String getListValue(String id)
   {
      if ((2 < id.length())
            && ((id.startsWith("\"") && id.endsWith("\"") || (id.startsWith("'") && id
                  .endsWith("'")))))
      {
         id = id.substring(1, id.length() - 2);
      }
      return id;
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
