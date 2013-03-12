/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.sysconsole;

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.cli.sysconsole.patch.FixRuntimeOidCommand;
import org.eclipse.stardust.engine.cli.sysconsole.utils.SysconsoleCommandExecuter;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.model.beans.IConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.NullConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author ubirkemeyer, rsauer
 * @version $Revision$
 */
public class ArchiveCommand extends AuditTrailCommand
{
   private static final Options argTypes = new Options();

   private static final String PARTITION = "partition";

   private static final String MODEL = "model";

   private static final String PROCESSES_BY_OID = "processes";

   private static final String DEAD_MODELS = "deadModels";

   private static final String DEAD_PROCESSES = "deadProcesses";

   private static final String DEAD_DATA = "deadData";

   private static final String LOG_ENTRIES = "logEntries";

   private static final String USER_SESSIONS = "userSessions";

   private static final String NO_BACKUP = "noBackup";

   private static final String SCHEMA_NAME = "schemaName";

   private static final String TIMESTAMP = "timestamp";

   private static final String INTERVAL = "interval";

   private static final String BATCH_SIZE = "batchSize";
   
   private static final String DISCLAIMER = 
           "PLEASE NOTE: this archive sysconsole command with model deletion should only\n"
         + "             be performed in maintenance windows without workflow, otherwise\n"
         + "             this might lead to inconsistency in the audit trail.";

   // 1 min is the minimum commit interval
   private static long MIN_COMMIT_INTERVAL = 1000 * 60;
   // 1 day is the default commit interval if not specified
   private static long DEFAULT_COMMIT_INTERVAL = 1000 * 60 * 60 * 24;

   static
   {
      argTypes.register("-" + PARTITION, Options.NO_SHORTNAME, PARTITION, "Optionally specifies the partition(s) to be archived.\n"
            + "Accepts as argument a single partition ID or a comma separated list of\npartition IDs.\n"
            + "If this parameter is not used, the archiving command of sysconsole has\nan effect only on the default partition.", true);

      argTypes.register("-" + MODEL, "-v", MODEL,
            "Deletes audit trail for the model version with " + "the specified OID.\n"
                  + DISCLAIMER, true);

      argTypes.register("-" + PROCESSES_BY_OID, Options.NO_SHORTNAME, PROCESSES_BY_OID,
            "Archives/Deletes the specified process instances (comma separated list of\n"
            + "OIDs).\n"
            + "Process instances must be terminated (completed or aborted).", true);

      argTypes.register("-" + DEAD_MODELS, "-m", DEAD_MODELS, "Deletes audit trail for "
            + "all dead models (models not having nonterminated\n"
            + "process instances).\n" + DISCLAIMER, false);

      argTypes.register("-" + DEAD_PROCESSES, "-p", DEAD_PROCESSES, "Deletes terminated "
            + "process instances.", false);

      argTypes.register("-" + DEAD_DATA, "-d", DEAD_DATA, "Deletes data values for terminated process instances.\n"
            + "Accepts as argument a single data ID or a comma separated list of data\nIDs.",
            true);

      argTypes.register("-" + LOG_ENTRIES, "-l", LOG_ENTRIES, "Deletes log entries.", false);

      argTypes.register("-" + USER_SESSIONS, "-u", USER_SESSIONS, "Archives/Deletes user sessions.", false);

      argTypes.register("-" + NO_BACKUP, "-n", NO_BACKUP, "Only deletes and doesn't archive data.", false);

      argTypes.register("-" + SCHEMA_NAME, "-s", SCHEMA_NAME, "Specifies the schema containing the backup tables.", true);

      argTypes.register("-" + TIMESTAMP, "-t", TIMESTAMP, "Restricts any operation to either process instances terminated before the\n"
            + "given date or log records created before the given date (always\n"
            + "inclusive).\n"
            + "The specified date must conforms to ISO date patterns\n"
            + "(i.e. \"2005-12-31\", \"2005-12-31 23:59\" or \"2005-12-31T23:59:59:999\"),\n"
            + "or \"" + DateUtils.getNoninteractiveDateFormat().toPattern() + "\" for backward compatibility."
            , true);

      argTypes.register("-" + INTERVAL, "-i", INTERVAL,
            "The search interval in format nn{d{ays}|h{ours}|m{inutes}}. If this option\n"
            +"is missing, a default interval of 1 day will be used.", true);

      argTypes.register("-" + BATCH_SIZE, "-b", BATCH_SIZE,
            "Performs any archive/delete operation in controlled batches (i.e.\n"
            + "transactions). If this option is missing, a default batch size of\n"
            + "1000 will be used.", true);
      
      argTypes.addExclusionRule(new String[] {
         MODEL, PROCESSES_BY_OID, DEAD_MODELS, DEAD_PROCESSES, DEAD_DATA, LOG_ENTRIES, USER_SESSIONS}, true);

      argTypes.addExclusionRule(new String[] {MODEL, PARTITION}, false);
      argTypes.addExclusionRule(new String[] {MODEL, TIMESTAMP}, false);
      argTypes.addExclusionRule(new String[] {DEAD_MODELS, TIMESTAMP}, false);

      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, TIMESTAMP}, false);
      argTypes.addExclusionRule(new String[] {PROCESSES_BY_OID, INTERVAL}, false);

      argTypes.addExclusionRule(new String[] {DEAD_DATA, TIMESTAMP}, false);
   }

   public Options getOptions()
   {
      return argTypes;
   }
   
   public int doRun(Map options)
   {
      try
      {
         Map locals = new HashMap();
         locals.put(IConfigurationVariablesProvider.CONFIGURATION_VAR_PROVIDER,
               new NullConfigurationVariablesProvider());
         ParametersFacade.pushLayer(locals);

         return internalDoRun(options);
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   private int internalDoRun(Map options)
   {
      Date before = Options.getDateValue(options, TIMESTAMP);
      if ((null == before) && options.containsKey(TIMESTAMP))
      {
         throw new PublicException(MessageFormat.format(
               "Unsupported date format for option -timestamp: ''{0}''.",
               new Object[] {options.get(TIMESTAMP)}));
      }
      long interval = getIntervalOption(options, INTERVAL);
      Long batchSizeOption = Options.getLongValue(options, BATCH_SIZE);
      long txBatchSize = (null != batchSizeOption) ? batchSizeOption.longValue() : 1000;
      boolean noBackup = options.containsKey(NO_BACKUP);
      if (!noBackup && !options.containsKey(SCHEMA_NAME))
      {
         throw new PublicException("No archive audittrail schema specified.");
      }
      String archiveSchema = (String) options.get(SCHEMA_NAME);

      SchemaHelper.verifySysopPassword(
            (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL),
            (String) globalOptions.get("password"));
      
      // evaluate partition, fall back to default partition, if configured
      String partitionSpec = (String) options.get(PARTITION);
      if (StringUtils.isEmpty(partitionSpec))
      {
         partitionSpec = ParametersFacade.instance().getString(
               SecurityProperties.DEFAULT_PARTITION,
               PredefinedConstants.DEFAULT_PARTITION_ID);
      }
      List partitionIds = new ArrayList();
      for (Iterator i = StringUtils.split(partitionSpec, ","); i.hasNext(); )
      {
         String id = (String) i.next();
         if ((2 < id.length())
               && ((id.startsWith("\"") && id.endsWith("\"")
                     || (id.startsWith("'") && id.endsWith("'")))))
         {
            id = id.substring(1, id.length() - 2);
         }
         partitionIds.add(id);
      }
      if (partitionIds.isEmpty())
      {
         throw new PublicException("No audittrail partition specified.");
      }
      for (Iterator partitionItr = partitionIds.iterator(); partitionItr.hasNext();)
      {
         String partitionId = (String) partitionItr.next();
         
         setConnectionOptions();
         Utils.initCarnotEngine(partitionId);
         
         Archiver archiver = new Archiver( !noBackup, archiveSchema, txBatchSize,
               globalOptions.containsKey("force"), partitionId);
         
         if (options.containsKey(MODEL))
         {
            archiver.archiveDeadModel(getIntegerOption(options, MODEL), interval);
         }
         else if (options.containsKey(DEAD_MODELS))
         {
            archiver.archiveDeadModels(interval);
         }
         else if (options.containsKey(PROCESSES_BY_OID))
         {
            archiver.archiveDeadProcesses(Options.getLongValues(options, PROCESSES_BY_OID));
         }
         else if (options.containsKey(DEAD_DATA))
         {
            List dataIds = new ArrayList();
            for (Iterator i = StringUtils.split((String) options.get(DEAD_DATA), ","); i.hasNext(); )
            {
               String id = (String) i.next();
               if ((2 < id.length())
                     && ((id.startsWith("\"") && id.endsWith("\"")
                           || (id.startsWith("'") && id.endsWith("'")))))
               {
                  id = id.substring(1, id.length() - 2);
               }
               dataIds.add(id);
            }
            archiver.archiveDeadData(
                  (String[]) dataIds.toArray(StringUtils.EMPTY_STRING_ARRAY), before,
                  interval);
         }
         else if (options.containsKey(DEAD_PROCESSES))
         {
            archiver.archiveDeadProcesses(before, interval);
         }
         else if (options.containsKey(LOG_ENTRIES))
         {
            archiver.archiveLogEntries(before, interval);
         }
         else if (options.containsKey(USER_SESSIONS))
         {
            if (noBackup
                  && !force()
                  && !confirm("Do you really want to delete user sessions without archiving them? (Y/N): "))
            {
               return -1;
            }
            
            archiver.archiveUserSessions(before, interval);
         }
      }
      
      //fix runtime oids which could be corrupted by the previous archive run
//      String fixCommandName = FixRuntimeOidCommand.COMMAND_NAME;
//      String[] args = { "-password", "sysop", "-force", "-dbschema", 
//            archiveSchema, fixCommandName, FixRuntimeOidCommand.NO_LOG_ARG};
//      SysconsoleCommandExecuter.main(args);
      
      return 0;
   }
   
   private long getIntervalOption(Map options, String name)
   {
      String[] types = {"days", "hours", "minutes"};
      long[] multiplicators = {24 * 60, 60, 1};
      long result = 0;
      if (options.containsKey(name))
      {
         try
         {
            boolean found = false;
            String value = ((String) options.get(name)).toLowerCase();
            for (int i = 0; i < types.length; i++)
            {
               if (value.endsWith(types[i]))
               {
                  value = value.substring(0, value.length() - types[i].length());
               }
               else if (value.endsWith(types[i].substring(0, 1)))
               {
                  value = value.substring(0, value.length() - 1);
               }
               else
               {
                  continue;
               }
               result = multiplicators[i] * Long.parseLong(value) * 60 * 1000;
               found = true;
               break;
            }
            if (!found)
            {
               throw new NumberFormatException("type unknown");
            }
         }
         catch (NumberFormatException e)
         {
            throw new PublicException(
                  "Interval value '" + (String) options.get(name)
                  + "' for option '" + name + "' is not in correct format. Format has to be "
                  + "nn{d{ays}|h{ours}|m{inutes}}");
         }
      }
      else
      {
         result = DEFAULT_COMMIT_INTERVAL;
      }
      return result < MIN_COMMIT_INTERVAL ? MIN_COMMIT_INTERVAL : result;
   }

   public void printCommand(Map options)
   {
      print("Cleaning or archiving (parts of) the audit trail DB:\n");
   }

   public String getSummary()
   {
      return "Deletes or archives the audit trail or parts of it.";
   }
}
