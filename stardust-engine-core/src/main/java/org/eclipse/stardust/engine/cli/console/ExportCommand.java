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

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand.ExportMetaData;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author jsaayman
 * @version $Revision$
 */
public class ExportCommand extends BaseExportImportCommand
{
   private static final Options argTypes = new Options();

   private static final int DEFAULT_BATCH_SIZE = 1000;

   private static final String MODELS_BY_OID = "modelOids";

   private static final String DUMP = "dump";

   private static final String BATCH_SIZE = "batchSize";

   static
   {
      argTypes.register("-" + BATCH_SIZE, null, BATCH_SIZE,
            "Defines the number of process instances to be exported per batch", true);

      argTypes.register("-" + CONCURRENT_BATCHES, null, CONCURRENT_BATCHES,
            "Defines how many batches can export concurrently", true);

      argTypes
            .register(
                  "-" + PARTITION,
                  null,
                  PARTITION,
                  "Optionally specifies the partition(s) to be archived.\n"
                        + "Accepts as argument a single partition ID or a comma separated list of\npartition IDs.\n"
                        + "If this parameter is not used, the export command of sysconsole has\nan effect only on the default partition.",
                  true);

      argTypes.register("-" + PROCESSES_BY_OID, null, PROCESSES_BY_OID,
            "Archives/Dumps specified process instances (comma separated list of\n"
                  + "OIDs).\n"
                  + "Process instances must be terminated (completed or aborted).", true);

      argTypes.register("-" + PROCESS_DEFINITION_IDS, "-procDef", PROCESS_DEFINITION_IDS,
            "Archives/Dumps process instances for specified list of process definition IDs(comma separated list of\n"
                  + "IDs).\n"
                  + "Process instances must be terminated (completed or aborted).", true);

      argTypes.register("-" + MODEL_IDS, "-model", MODEL_IDS,
            "Archives/Dumps process instances for specified list of model IDs(comma separated list of\n"
                  + "IDs).\n"
                  + "Process instances must be terminated (completed or aborted).", true);
    

      argTypes.register("-" + PROCESS_MIN_OID, null, PROCESS_MIN_OID,
            "Archives/Dumps all processes with OID great or equal to this number and less or equal to processMax.", true);


      argTypes.register("-" + PROCESS_MAX_OID, null, PROCESS_MAX_OID,
            "Archives/Dumps all processes with OID great or equal to processMin and less or equal to this number.", true);

      argTypes
            .register(
                  "-" + MODELS_BY_OID,
                  null,
                  MODELS_BY_OID,
                  "Archives/Dumps process instances for the models specified by the models oids provided (comma separated list of\n"
                        + "OIDs).\n"
                        + "Process instances must be terminated (completed or aborted).",
                  true);

      argTypes.register("-" + DUMP, "-d", DUMP,
            "Backs up processes without generating a unique id, or deleting them ", false);
           
      argTypes
            .register(
                  "-" + FROM_DATE,
                  "-fd",
                  FROM_DATE,
                  "Restricts any operation to process instances started after the\n"
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
                  "Restricts any operation to process instances terminated before the\n"
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


      argTypes.addExclusionRule(new String[] {DESCRIPTORS}, false);
      argTypes.addExclusionRule(new String[] {DATE_DESCRIPTORS}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_DEFINITION_IDS}, false);
      argTypes.addExclusionRule(new String[] {MODELS_BY_OID}, false);
      argTypes.addExclusionRule(new String[] {MODEL_IDS}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MIN_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MIN_OID, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MAX_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MAX_OID, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {MODELS_BY_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {MODELS_BY_OID, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {MODEL_IDS, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {MODEL_IDS, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {PARTITION}, false);
      argTypes.addExclusionRule(new String[] {FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {BATCH_SIZE}, false);
      argTypes.addExclusionRule(new String[] {CONCURRENT_BATCHES}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MIN_OID, PROCESSES_BY_OID}, false);
      argTypes.addExclusionRule(new String[] {PROCESS_MAX_OID, PROCESSES_BY_OID}, false);
      argTypes.addInclusionRule(PROCESS_MIN_OID, PROCESS_MAX_OID);
      argTypes.addInclusionRule(PROCESS_MAX_OID, PROCESS_MIN_OID);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      final boolean dumpData = options.containsKey(DUMP);
      final Date fromDate = getFromDate(options);
      final Date toDate = getToDate(options);
      final List<Long> processOids = getProcessOids(options);
      final List<Integer> modelOids = getModelOids(options);
      final List<String> partitionIds = getPartitions(options);
      final int batchSize = getBatchSize(options);
      final int concurrentBatches = getConcurrentBatches(options);
      final HashMap<String, Object> descriptors = getDescriptors(options);

      for (final String partitionId : partitionIds)
      {
         Date start = new Date();
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(SecurityProperties.PARTITION, partitionId);
         final ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions,
               properties);

//         ProcessTool.createProcesses(serviceFactory);
//         if (1 == 1)
//         {
//            return 0;
//         }

         ExportMetaData exportMetaData = getExportOids(fromDate, toDate,
               processOids, modelOids, serviceFactory, descriptors, dumpData);

         print("Found " + exportMetaData.getAllProcessesForExport(dumpData).size() + " processes to export");
         List<ExportMetaData> batches = ExportImportSupport.partition(exportMetaData,
               batchSize);
         print("Found " + batches.size() + " batches to export");
         final ExportModel exportModel;
         if (exportMetaData.getAllProcessesForExport(dumpData).size() > 0)
         {
            exportModel = exportModel(partitionId, exportMetaData, serviceFactory, dumpData);
         }
         else
         {
            exportModel = null;
         }
         ExecutorService executor = Executors.newFixedThreadPool(concurrentBatches);
         List<Future<ExportResult>> results = new ArrayList<Future<ExportResult>>();
         for (final ExportMetaData batch : batches)
         {
            Callable<ExportResult> exportCallable = new Callable<ExportResult>()
            {
               @Override
               public ExportResult call() throws Exception
               {
                  ExportProcessesCommand command = new ExportProcessesCommand(
                        ExportProcessesCommand.Operation.EXPORT_BATCH, batch, dumpData);
                  ExportResult result = (ExportResult) serviceFactory
                        .getWorkflowService().execute(command);
                  if (result == null)
                  {
                     print("No Data to export. Export file not created.");
                  }
                  return result;
               }

            };
            results.add(executor.submit(exportCallable));
         }

         print(new Date() + " Export Submitted");
         List<ExportResult> exportResults = new ArrayList<ExportResult>();

         for (Future<ExportResult> result : results)
         {
            try
            {
               ExportResult exportResult = result.get();
               exportResults.add(exportResult);
            }
            catch (Exception e)
            {
               print("Unexpected Exception during Export " + e.getMessage());
               e.printStackTrace();
            }
         }
         String operation = dumpData ? "Dump" : "Archive";
         print(new Date() + " Export Done for Partition: " + partitionId);
         ExportResult mergedResult = ExportImportSupport.merge(exportResults, exportModel);
         if (mergedResult != null)
         {
            int archiveCount = 0;
            for (Date date : mergedResult.getDates())
            {
               ExportIndex exportIndex = mergedResult.getExportIndex(date);
               archiveCount += exportIndex.getOidsToUuids().size();
            }
            if (dumpData)
            {
               print(new Date() + " Processes to " + operation + ": " + archiveCount);
            }
            else
            {
               print(new Date() + " Processes to " + operation + ": " + archiveCount + "; Processes to delete: " +  mergedResult.getPurgeProcessIds().size());
            }
         }
         else
         {
            if (dumpData)
            {
               print(new Date() + " Processes to " + operation + ": 0");
            }
            else
            {
               print(new Date() + " Processes to " + operation + ": 0; Processes to delete: 0");
            }
         }
         ExportProcessesCommand command = new ExportProcessesCommand(ExportProcessesCommand.Operation.ARCHIVE, mergedResult, dumpData);
         Boolean success = (Boolean) serviceFactory.getWorkflowService().execute(command);
         if (success)
         {
            print(new Date() + " " + operation + " Done for Partition: " + partitionId);
         }
         else
         {
            print(operation + " Failed for Partition: " + partitionId);
         }

         Date end = new Date();
         long millis = end.getTime() - start.getTime();
         String duration = String.format("%d min, %d sec", TimeUnit.MILLISECONDS
               .toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis)
               - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
         print("Export Complete for Partition: " + partitionId + ". Time taken: "
               + duration);
      }

      return 0;
   }
    
   private int getBatchSize(Map options)
   {
      Long batchSize = Options.getLongValue(options, BATCH_SIZE);
      if (batchSize == null || batchSize < 1)
      {
         batchSize = Parameters.instance().getLong(
               KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE, DEFAULT_BATCH_SIZE);
      }

      return batchSize.intValue();
   }

   private List<Integer> getModelOids(Map options)
   {
      List<Integer> modelOids;
      if (options.containsKey(MODELS_BY_OID))
      {
         String modelIds = (String) options.get(MODELS_BY_OID);
         modelOids = new ArrayList<Integer>();
         splitListInteger(modelIds, modelOids);
      }
      else
      {
         modelOids = null;
      }
      return modelOids;
   }
   
   private ExportMetaData getExportOids(final Date fromDate,
         final Date toDate, final List<Long> processOids, final List<Integer> modelOids,
         final ServiceFactory serviceFactory, HashMap<String, Object> descriptors, boolean dumpData)
   {
      ArchiveFilter filter = new ArchiveFilter(processOids, modelOids, fromDate, toDate, descriptors);
      ExportProcessesCommand command = new ExportProcessesCommand(ExportProcessesCommand.Operation.QUERY, filter, dumpData);
      ExportMetaData exportMetaData = (ExportMetaData) serviceFactory
            .getWorkflowService().execute(command);
      return exportMetaData;
   }

   private ExportModel exportModel(final String partitionId,
         final ExportMetaData exportMetaData, final ServiceFactory serviceFactory, boolean dump)
   {
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.EXPORT_MODEL, exportMetaData, dump);
      ExportResult exportResult = (ExportResult) serviceFactory.getWorkflowService()
            .execute(command);
      if (exportResult == null)
      {
         print("No Data to export. Export file not created.");
         return null;
      }
      else
      {
         print("Model exported");
         return exportResult.getExportModel();
      }
   }

   private void splitListInteger(String partitionSpec, List<Integer> partitionIds)
   {
      for (Iterator i = StringUtils.split(partitionSpec, ","); i.hasNext();)
      {
         String id = (String) i.next();
         id = getListValue(id);
         partitionIds.add(new Integer(id));
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
      print("Export and/or purging process instances:\n");
   }

   public String getSummary()
   {
      return "Exports and/or purges process instances.";
   }
}
