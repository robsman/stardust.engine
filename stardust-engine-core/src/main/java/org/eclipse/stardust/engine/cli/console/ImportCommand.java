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
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author jsaayman
 * @version $Revision$
 */
public class ImportCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String PARTITION = "partition";

   private static final String PROCESSES_BY_OID = "processes";

   private static final String FROM_DATE = "fromDate";

   private static final String TO_DATE = "toDate";

   static
   {
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
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
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
         byte[] data;
         try
         {
            File f = new File(System.getProperty("java.io.tmpdir") + "/export_"
                  + partitionId + ".dat");
            data = FileUtils.readFileToByteArray(f);

         }
         catch (IOException e)
         {
            data = null;
            print("No export file export_" + partitionId + ".dat found to import");
         }

         if (data != null)
         {
            ImportProcessesCommand command;
            if (processOids != null)
            {
               command = new ImportProcessesCommand(data, processOids);
            }
            else if (fromDate != null || toDate != null)
            {
               command = new ImportProcessesCommand(data, fromDate, toDate);
            }
            else
            {
               command = new ImportProcessesCommand(data);
            }

            Map<String, String> properties = new HashMap<String, String>();
            properties.put(SecurityProperties.PARTITION, partitionId);
            ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions,
                  properties);
            int count = (Integer) serviceFactory.getWorkflowService().execute(command);

            print("Imported " + count + " process instances into partition "
                  + partitionId);
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
