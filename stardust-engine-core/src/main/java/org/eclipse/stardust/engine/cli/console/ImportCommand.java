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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.io.FileUtils;

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
         final File[] files = getFiles(partitionId);
         print("Starting Import for partition " + partitionId + ". Found " + files.length
               + " to import.");
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(SecurityProperties.PARTITION, partitionId);
         final ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions,
               properties);
         WorkflowService workflowService = serviceFactory.getWorkflowService();
         ImportMetaData importMetaData = null;
         if (files != null && files.length > 0)
         {
            importMetaData = validateImport(files[0], workflowService);
         }
         if (importMetaData != null)
         {
            ExecutorService executor = Executors.newFixedThreadPool(concurrentBatches);
            List<Future<Integer>> results = new ArrayList<Future<Integer>>();
            final ImportMetaData metaData = importMetaData;
            for (int i = 1; i < files.length; i++)
            {
               final int current = i;
               Callable<Integer> exportCallable = new Callable<Integer>()
               {
                  @Override
                  public Integer call() throws Exception
                  {
                     int count = importFile(fromDate, toDate, processOids, partitionId,
                           metaData, serviceFactory, files[current]);
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

   private ImportMetaData validateImport(File file, WorkflowService workflowService)
   {
      ImportMetaData importMetaData;
      byte[] data;
      try
      {
         data = FileUtils.readFileToByteArray(file);
      }
      catch (IOException e)
      {
         data = null;
         print("Failed to read export model file: " + file.getName());
      }

      ImportProcessesCommand importCommand = new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, data, null);
      importMetaData = (ImportMetaData) workflowService.execute(importCommand);
      if (importMetaData != null)
      {
         print("Model validated");
      }
      else
      {
         print("Model validation failed. No import can be done. Model file: "
               + file.getName());
      }
      return importMetaData;
   }

   private int importFile(Date fromDate, Date toDate, List<Long> processOids,
         String partitionId, ImportMetaData importMetaData,
         ServiceFactory serviceFactory, File file)
   {
      int count = 0;
      byte[] data;
      try
      {
         data = FileUtils.readFileToByteArray(file);
      }
      catch (IOException e)
      {
         data = null;
         print("Failed to read export file: " + file.getName());
      }

      if (data != null)
      {
         ImportProcessesCommand command;
         if (processOids != null)
         {
            command = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
                  data, processOids, importMetaData);
         }
         else if (fromDate != null || toDate != null)
         {
            command = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
                  data, fromDate, toDate, importMetaData);
         }
         else
         {
            command = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
                  data, importMetaData);
         }
         count = (Integer) serviceFactory.getWorkflowService().execute(command);

         print("Imported " + count + " process instances into partition " + partitionId
               + " from file: " + file.getName());
      }
      return count;
   }

   private File[] getFiles(final String partitionId)
   {
      File[] files;

      File directory = new File(System.getProperty("java.io.tmpdir"));

      FilenameFilter filter = new FilenameFilter()
      {

         @Override
         public boolean accept(File dir, String name)
         {
            if (name.lastIndexOf('.') > 0)
            {
               // get last index for '.' char
               int lastIndex = name.lastIndexOf('.');

               // get extension
               String ext = name.substring(lastIndex);
               String fileName = name.substring(0, lastIndex - 1);

               // match path name extension
               if (ext.equals(".dat") && fileName.startsWith("export_" + partitionId))
               {
                  return true;
               }
            }
            return false;
         }
      };

      if (directory.isDirectory())
      {
         File[] allfiles = directory.listFiles(filter);
         files = new File[allfiles.length];
         for (int i = 0; i < allfiles.length; i++)
         {
            String name = allfiles[i].getName();
            int lastIndex = name.lastIndexOf('.');
            String fileName = name.substring(0, lastIndex);
            String[] parts = fileName.split("_");
            int exportIndex;
            if ("model".equals(parts[2]))
            {
               exportIndex = 0;
            }
            else
            {
               exportIndex = Integer.valueOf(parts[2]);
            }
            if (exportIndex >= files.length)
            {
               throw new IllegalStateException("Not All Files From export are present");
            }
            else
            {
               files[exportIndex] = allfiles[i];
            }
         }
         for (int i = 0; i < files.length; i++)
         {
            if (files[i] == null)
            {
               throw new IllegalStateException(
                     "Not All Files From export are present, in expected sequence");
            }
            print("Export file " + files[i].getName() + " found to import");
         }
      }
      else
      {
         files = new File[] {};
         ;
      }
      return files;
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
