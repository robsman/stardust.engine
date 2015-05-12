package org.eclipse.stardust.engine.cli.console;

import java.util.*;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public abstract class BaseExportImportCommand extends ConsoleCommand
{

   protected static final Options argTypes = new Options();
   private static final int DEFAULT_CONCURRENT_BATCHES = 10;
   protected static final String PARTITION = "partition";
   protected static final String PROCESSES_BY_OID = "processOids";
   protected static final String FROM_DATE = "fromDate";
   protected static final String TO_DATE = "toDate";
   protected static final String CONCURRENT_BATCHES = "concurrentBatches";
   protected static final String DESCRIPTORS = "descriptors";
   protected static final String DATE_DESCRIPTORS = "dateDescriptors";
   protected static final String MODEL_IDS = "models";
   protected static final String PROCESS_DEFINITION_IDS = "processes";
   protected static final String PREFERENCES = "preferences";
   

   public BaseExportImportCommand()
   {
      super();
   }

   public Options getOptions()
   {
      return argTypes;
   }

   protected int getConcurrentBatches(Map options)
   {
      Long concurrent = Options.getLongValue(options, CONCURRENT_BATCHES);
      if (concurrent == null || concurrent < 1)
      {
         return DEFAULT_CONCURRENT_BATCHES;
      }
      return concurrent.intValue();
   }

   protected HashMap<String, Object> getDescriptors(Map options)
   {
      // evaluate partition, fall back to default partition, if configured
      String descr = (String) options.get(DESCRIPTORS);
      String dateDescr = (String) options.get(DATE_DESCRIPTORS);
      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      List<String> descriptorValues = new ArrayList<String>();
      List<String> dateDescriptorValues = new ArrayList<String>();
      splitListString(descr, descriptorValues);
      splitListString(dateDescr, dateDescriptorValues);
      listToMapString(descriptorValues, descriptors);
      listToMapDate(dateDescriptorValues, descriptors);
      if (descriptors.isEmpty())
      {
         return null;
      }
      return descriptors;
   }

   private void listToMapString(List<String> descriptorValues, HashMap<String, Object> descriptors)
   {
      for (String value : descriptorValues)
      {
         String[] nameValue = value.split("=", 2);
         descriptors.put(nameValue[0], nameValue[1]);
      }
   }

   private void listToMapDate(List<String> descriptorValues, HashMap<String, Object> descriptors)
   {
      for (String value : descriptorValues)
      {
         String[] nameValue = value.split("=", 2);
         Date date = Options.getDateValue(nameValue[1]);
         if (date != null)
         {
            descriptors.put(nameValue[0], date);
         }
         else
         {
            throw new PublicException(
                  BpmRuntimeError.CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_DATEDESCRIPTOR
                        .raise(value));
         }
      }
   }

   protected Date getFromDate(Map options)
   {
      Date fromDate = Options.getDateValue(options, FROM_DATE);
      if ((null == fromDate) && options.containsKey(FROM_DATE))
      {
         throw new PublicException(
               BpmRuntimeError.CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_FROMDATE
                     .raise(options.get(FROM_DATE)));
      }
      return fromDate;
   }

   protected Date getToDate(Map options)
   {
      Date toDate = Options.getDateValue(options, TO_DATE);
      if ((null == toDate) && options.containsKey(TO_DATE))
      {
         throw new PublicException(
               BpmRuntimeError.CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_TODATE
                     .raise(options.get(TO_DATE)));
      }
      return toDate;
   }

   protected List<Long> getProcessOids(Map options)
   {
      List<Long> processOids;
      if (options.containsKey(PROCESSES_BY_OID))
      {
         String processInstanceIds = (String) options.get(PROCESSES_BY_OID);
         
         if (processInstanceIds.contains(","))
         {
            processOids = new ArrayList<Long>();
            splitListLong(processInstanceIds, processOids);
         } 
         else if (processInstanceIds.contains("-"))
         {
            processOids = new ArrayList<Long>();
            String[] split = processInstanceIds.split("-", 2);
            if (split.length == 2 && StringUtils.isNotEmpty(split[0]) && StringUtils.isNotEmpty(split[1]))
            {
               long min = new Long(split[0]);
               long max = new Long(split[1]);
               for (long i = min; i <= max; i++)
               {
                  processOids.add(i);
               }
            }
            else
            {
               throw new PublicException(
                  BpmRuntimeError.CLI_UNSUPPORTED_FORMAT_FOR_OPTION_PROCESSINSTANCEOID
                  .raise(processInstanceIds));
            }
         }
         else
         {
            throw new PublicException(
            BpmRuntimeError.CLI_UNSUPPORTED_FORMAT_FOR_OPTION_PROCESSINSTANCEOID.raise(options
                  .get(processInstanceIds)));
         }
      }
      else
      {
         processOids = null;
      }
      return processOids;
   }

   protected List<String> getProcessDefinitionIds(Map options)
   {
      String value = (String) options.get(PROCESS_DEFINITION_IDS);
      List<String> ids = new ArrayList<String>();
      splitListString(value, ids);
      return ids;
   }
   
   protected List<String> getModelIds(Map options)
   {
      String value = (String) options.get(MODEL_IDS);
      List<String> ids = new ArrayList<String>();
      splitListString(value, ids);
      return ids;
   }
   
   protected List<String> getPartitions(Map options)
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

}