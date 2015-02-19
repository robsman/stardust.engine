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
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.persistence.archive.ExportImportSupport;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand.ExportMetaData;
import org.eclipse.stardust.engine.core.persistence.archive.ExportResult;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author jsaayman
 * @version $Revision$
 */
public class ExportCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final int DEFAULT_BATCH_SIZE = 1000;

   private static final int DEFAULT_CONCURRENT_BATCHES = 10;

   private static final String PARTITION = "partition";

   private static final String PROCESSES_BY_OID = "processes";

   private static final String MODELS_BY_OID = "models";

   private static final String PURGE = "purge";

   private static final String FROM_DATE = "fromDate";

   private static final String TO_DATE = "toDate";

   private static final String BATCH_SIZE = "batchSize";

   private static final String CONCURRENT_BATCHES = "concurrentBatches";

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
            "Archives/Deletes specified process instances (comma separated list of\n"
                  + "OIDs).\n"
                  + "Process instances must be terminated (completed or aborted).", true);

      argTypes
            .register(
                  "-" + MODELS_BY_OID,
                  null,
                  MODELS_BY_OID,
                  "Archives/Deletes process instances for the models specified by the models oids provided (comma separated list of\n"
                        + "OIDs).\n"
                        + "Process instances must be terminated (completed or aborted).",
                  true);

      argTypes.register("-" + PURGE, "-p", PURGE,
            "Add this option to purge process instances that are exported", false);

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

      argTypes.addExclusionRule(new String[] {PURGE}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID}, false);
      argTypes.addExclusionRule(new String[] {MODELS_BY_OID}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {MODELS_BY_OID, FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {MODELS_BY_OID, TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {PARTITION}, false);
      argTypes.addExclusionRule(new String[] {FROM_DATE}, false);
      argTypes.addExclusionRule(new String[] {TO_DATE}, false);
      argTypes.addExclusionRule(new String[] {BATCH_SIZE}, false);
      argTypes.addExclusionRule(new String[] {CONCURRENT_BATCHES}, false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      final boolean purge = options.containsKey(PURGE);
      final Date fromDate = getFromDate(options);
      final Date toDate = getToDate(options);
      final List<Long> processOids = getProcessOids(options);
      final List<Integer> modelOids = getModelOids(options);
      final List<String> partitionIds = getPartitions(options);
      final int batchSize = getBatchSize(options);
      final int concurrentBatches = getConcurrentBatches(options);

      for (final String partitionId : partitionIds)
      {
         Date start = new Date();
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(SecurityProperties.PARTITION, partitionId);
         final ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions,
               properties);

         //ProcessTool.createProcesses(serviceFactory);

         ExportMetaData exportMetaData = getExportOids(fromDate, toDate,
               processOids, modelOids, serviceFactory);

         List<ExportMetaData> batches = ExportImportSupport.partition(exportMetaData,
               batchSize);

         final byte[] modelData = exportModel(partitionId, exportMetaData, serviceFactory);

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
                        ExportProcessesCommand.Operation.EXPORT_BATCH, batch);
                  ExportResult result = (ExportResult) serviceFactory
                        .getWorkflowService().execute(command);
                  if (result == null)
                  {
                     print("No Data to export. Export file not created.");
                  }
                  else
                  {
                     result.setModelData(modelData);
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

         print(new Date() + " Export Done for Partition: " + partitionId);
         ExportProcessesCommand command = new ExportProcessesCommand(ExportProcessesCommand.Operation.ARCHIVE, exportResults);
         Boolean success = (Boolean) serviceFactory.getWorkflowService().execute(command);
         if (success)
         {
            print(new Date() + " Archive Done for Partition: " + partitionId);
            if (purge)
            {
               print("Starting Purge for Partition: " + partitionId);
               command = new ExportProcessesCommand(ExportProcessesCommand.Operation.PURGE, exportResults);
               int deleteCount = (Integer) serviceFactory.getWorkflowService().execute(command);
               print(new Date() + " Purge Done for Partition: " + partitionId + " deleted " + deleteCount);
            }
         }
         else
         {
            print("Archive Failed for Partition: " + partitionId);
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

   private int purge(ExecutorService executor, List<ExportResult> exportResults,
         final ServiceFactory serviceFactory)
   {
      List<Future<Integer>> results = new ArrayList<Future<Integer>>();
      for (final ExportResult batch : exportResults)
      {
         Callable<Integer> exportCallable = new Callable<Integer>()
         {
            @Override
            public Integer call() throws Exception
            {
               ExportProcessesCommand command = new ExportProcessesCommand(
                     ExportProcessesCommand.Operation.PURGE, Arrays.asList(batch));
               Integer result = (Integer) serviceFactory
                     .getWorkflowService().execute(command);
               return result;
            }

         };
         results.add(executor.submit(exportCallable));
      }
      int count = 0;
      for (Future<Integer> result : results)
      {
         try
         {
            count += result.get();
         }
         catch (Exception e)
         {
            print("Unexpected Exception during Purge " + e.getMessage());
            e.printStackTrace();
         }
      }
      return count;
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
      else
      {
         processOids = null;
      }
      return processOids;
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

   private ExportMetaData getExportOids(final Date fromDate,
         final Date toDate, final List<Long> processOids, final List<Integer> modelOids,
         final ServiceFactory serviceFactory)
   {
      ExportProcessesCommand command;
      if (processOids != null || modelOids != null)
      {
         command = new ExportProcessesCommand(ExportProcessesCommand.Operation.QUERY,
               modelOids, processOids);
      }
      else if (fromDate != null || toDate != null)
      {
         command = new ExportProcessesCommand(ExportProcessesCommand.Operation.QUERY,
               fromDate, toDate);
      }
      else
      {
         command = new ExportProcessesCommand(ExportProcessesCommand.Operation.QUERY);
      }
      ExportMetaData exportMetaData = (ExportMetaData) serviceFactory
            .getWorkflowService().execute(command);
      return exportMetaData;
   }

   private byte[] exportModel(final String partitionId,
         final ExportMetaData exportMetaData, final ServiceFactory serviceFactory)
   {
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.EXPORT_MODEL, exportMetaData);
      ExportResult exportResult = (ExportResult) serviceFactory.getWorkflowService()
            .execute(command);
      if (exportResult == null)
      {
         print("No Data to export. Export file not created.");
         return null;
      }
      else
      {
         return exportResult.getModelData();
      }
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
