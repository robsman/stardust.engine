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
import java.io.IOException;
import java.util.*;

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
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author jsaayman
 * @version $Revision$
 */
public class ExportCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String PARTITION = "partition";

   private static final String PROCESSES_BY_OID = "processes";

   private static final String MODELS_BY_OID = "models";

   private static final String PURGE = "purge";

   private static final String FROM_DATE = "fromDate";

   private static final String TO_DATE = "toDate";

   static
   {
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
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      boolean purge = options.containsKey(PURGE);
      Date fromDate = Options.getDateValue(options, FROM_DATE);
      Date toDate = Options.getDateValue(options, TO_DATE);
      if ((null == fromDate) && options.containsKey(FROM_DATE))
      {
         throw new PublicException(
               BpmRuntimeError.CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_TIMESTAMP
                     .raise(options.get(FROM_DATE)));
      }
      if ((null == toDate) && options.containsKey(TO_DATE))
      {
         throw new PublicException(
               BpmRuntimeError.CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_TIMESTAMP
                     .raise(options.get(TO_DATE)));
      }
      List<Long> processOids;
      List<Integer> modelOids;
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
      // evaluate partition, fall back to default partition, if configured
      String partitionSpec = (String) options.get(PARTITION);
      if (StringUtils.isEmpty(partitionSpec))
      {
         partitionSpec = ParametersFacade.instance().getString(
               SecurityProperties.DEFAULT_PARTITION,
               PredefinedConstants.DEFAULT_PARTITION_ID);
      }
      List partitionIds = new ArrayList();
      splitListString(partitionSpec, partitionIds);
      if (partitionIds.isEmpty())
      {
         throw new PublicException(
               BpmRuntimeError.CLI_NO_AUDITTRAIL_PARTITION_SPECIFIED.raise());
      }
      for (Iterator partitionItr = partitionIds.iterator(); partitionItr.hasNext();)
      {
         String partitionId = (String) partitionItr.next();

         ExportProcessesCommand command;
         if (processOids != null)
         {
            command = new ExportProcessesCommand(modelOids, processOids, purge);
         }
         else if (fromDate != null || toDate != null)
         {
            command = new ExportProcessesCommand(fromDate, toDate, purge);
         }
         else
         {
            command = new ExportProcessesCommand(purge);
         }
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(SecurityProperties.PARTITION, partitionId);
         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions,
               properties);
         byte[] data = (byte[]) serviceFactory.getWorkflowService().execute(command);

         if (data == null)
         {
            print("No Data to export. Export file not created.");
         }
         else
         {
            try
            {
               File f = new File(System.getProperty("java.io.tmpdir") + "/export_"
                     + partitionId + ".dat");
               FileUtils.writeByteArrayToFile(f, data, false);
               print("Export saved to: " + f.getAbsolutePath());
            }
            catch (IOException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }

      return 0;
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
