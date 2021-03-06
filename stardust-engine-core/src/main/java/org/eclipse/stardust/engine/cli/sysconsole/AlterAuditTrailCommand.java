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
package org.eclipse.stardust.engine.cli.sysconsole;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.cli.sysconsole.consistency.AuditTrailConsistencyChecker;
import org.eclipse.stardust.engine.cli.sysconsole.consistency.SharedDocumentDataConsistencyCheck;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;


/**
 * @author sborn
 * @version $Revision$
 */
public class AlterAuditTrailCommand extends AuditTrailCommand
{
   private static final Logger trace = LogManager.getLogger(AlterAuditTrailCommand.class);

   private static final String LOCKTABLE_ENABLE = "enableLockTables";
   private static final String LOCKTABLE_VERIFY = "verifyLockTables";
   private static final String LOCKTABLE_DROP = "dropLockTables";

   private static final String SEQ_TABLE_ENABLE = "enableSequenceTable";
   private static final String SEQ_TABLE_VERIFY = "verifySequenceTable";
   private static final String SEQ_TABLE_DROP = "dropSequenceTable";

   private static final String PARTITION_CREATE = "createPartition";
   private static final String PARTITIONS_LIST = "listPartitions";
   private static final String PARTITION_DROP = "dropPartition";

   private static final String DATACLUSTER_UPGRADE = "upgradeDataClusters";
   private static final String DATACLUSTER_ENABLE = "enableDataClusters";
   private static final String DATACLUSTER_VERIFY = "verifyDataClusters";
   private static final String DATACLUSTER_SYNCHRONIZE = "synchronizeDataClusters";
   private static final String DATACLUSTER_DROP = "dropDataClusters";
   private static final String DATACLUSTER_CONFIG_FILE = "configFile";
   private static final String DATACLUSTER_VERBOSE = "verbose";

   private static final String AUDITTRAIL_SKIPDDL = "skipDDL";
   private static final String AUDITTRAIL_SKIPDML = "skipDML";
   private static final String AUDITTRAIL_SQL = "sql";
   private static final String STATEMENT_DELIMITER = "statementDelimiter";
   private static final String AUDITTRAIL_CHECK_CONSISTENCY = "checkConsistency";

   private static final Options argTypes = new Options();

   private PrintStream spoolDevice = null;
   private boolean isListPartitionsOption = false;

   static
   {
      argTypes.register("-" + LOCKTABLE_ENABLE, "-elt", LOCKTABLE_ENABLE,
            "Creates missing proxy locking tables and synchronizes table content.", false);
      argTypes.register("-" + LOCKTABLE_VERIFY, "-vlt", LOCKTABLE_VERIFY,
            "Verifies existence of proxy locking tables and their consistency.", false);
      argTypes.register("-" + LOCKTABLE_DROP, "-dlt", LOCKTABLE_DROP,
            "Drops any existing proxy locking tables.", false);

      argTypes.register("-" + SEQ_TABLE_ENABLE, "-est", SEQ_TABLE_ENABLE,
            "Creates 'sequence' table, 'next_sequence_value_for' function and synchronizes table content.", false);
      argTypes.register("-" + SEQ_TABLE_VERIFY, "-vst", SEQ_TABLE_VERIFY,
            "Verifies existence of 'sequence' table and their consistency.", false);
      argTypes.register("-" + SEQ_TABLE_DROP, "-dst", SEQ_TABLE_DROP,
            "Drops existing 'sequence' table tables.", false);

      argTypes.register("-" + PARTITION_CREATE, "-cp", PARTITION_CREATE,
            "Creates a new partition with the given ID, if no partition having this ID currently exists.", true);
      argTypes.register("-" + PARTITION_DROP, "-dp", PARTITION_DROP,
            "Deletes the partition identified by the given ID and any contained data from the AuditTrail.", true);
      argTypes.register("-" + PARTITIONS_LIST, "-lp", PARTITIONS_LIST,
            "Lists all existing partitions.", false);

      argTypes.register("-" + DATACLUSTER_ENABLE, "-edc", DATACLUSTER_ENABLE,
            "Creates missing data cluster tables and synchronizes table content.", false);

      argTypes.register("-" + DATACLUSTER_UPGRADE, "-udc", DATACLUSTER_UPGRADE,
            "Upgrades data cluster tables and synchronizes table content.", false);

      argTypes.register("-" + DATACLUSTER_VERIFY, "-vdc", DATACLUSTER_VERIFY,
            "Verifies existence of data cluster tables and their consistency.", false);
      argTypes.register("-" + DATACLUSTER_VERBOSE, "-v", DATACLUSTER_VERBOSE,
            "Prints all identified inconsistencies of data cluster tables to the console.", false);
      argTypes.register("-" + DATACLUSTER_SYNCHRONIZE, "-sdc", DATACLUSTER_SYNCHRONIZE,
            "Resolves all identified inconsistencies of data cluster tables.", false);
      argTypes.register("-" + DATACLUSTER_DROP, "-ddc", DATACLUSTER_DROP,
            "Drops any existing data cluster tables.", false);
      argTypes.register("-" + DATACLUSTER_CONFIG_FILE, null, DATACLUSTER_CONFIG_FILE,
            "Specifies the name of the config file which shall be deployed to audit trail.", true);
      argTypes.register("-" + AUDITTRAIL_SKIPDDL, null, AUDITTRAIL_SKIPDDL,
            "Skips the execution of schema changing commands like 'create' or 'drop'.", false);
      argTypes.register("-" + AUDITTRAIL_SKIPDML, null, AUDITTRAIL_SKIPDML,
            "Skips the execution of data changing commands like 'insert' or 'update'.", false);
      argTypes.register("-" + AUDITTRAIL_SQL, null, AUDITTRAIL_SQL,
            "Spools SQL statements to file instead of executing them on audit trail.", true);
      argTypes.register("-" + STATEMENT_DELIMITER, "-sd", STATEMENT_DELIMITER,
            "Specifies the delimiter applied after each SQL statement.", true);
      argTypes.register("-" + AUDITTRAIL_CHECK_CONSISTENCY, "-cco", AUDITTRAIL_CHECK_CONSISTENCY,
            "Checks wether any problem instances exists in audit trail.", false);

      argTypes.addExclusionRule(//
            new String[] { LOCKTABLE_ENABLE, LOCKTABLE_VERIFY, LOCKTABLE_DROP,//
                  DATACLUSTER_ENABLE, DATACLUSTER_VERIFY, DATACLUSTER_DROP, DATACLUSTER_UPGRADE,//
                  DATACLUSTER_SYNCHRONIZE, PARTITION_CREATE, PARTITION_DROP, PARTITIONS_LIST,//
                  AUDITTRAIL_CHECK_CONSISTENCY, SEQ_TABLE_ENABLE, SEQ_TABLE_VERIFY, SEQ_TABLE_DROP}, true);
      argTypes.addExclusionRule(//
            new String[] { LOCKTABLE_ENABLE, LOCKTABLE_VERIFY, LOCKTABLE_DROP,//
                  DATACLUSTER_VERIFY, DATACLUSTER_DROP,//
                  PARTITION_CREATE, PARTITION_DROP, PARTITIONS_LIST,//
                  DATACLUSTER_CONFIG_FILE }, false);
      argTypes.addExclusionRule(//
            new String[] { LOCKTABLE_VERIFY, DATACLUSTER_VERIFY, AUDITTRAIL_SQL }, false);
   }

   private boolean doRunLockingTableOptions(Map options)
   {
      final String password = (String) globalOptions.get("password");
      final String statementDelimiter = (String) options.get(STATEMENT_DELIMITER);

      boolean optionHandled = false;
      if (options.containsKey(LOCKTABLE_ENABLE))
      {
         optionHandled = true;
         print("Creating missing proxy locking tables and synchronizing their table content for Infinity schema.");

         boolean skipDdl = options.containsKey(AUDITTRAIL_SKIPDDL);
         boolean skipDml = options.containsKey(AUDITTRAIL_SKIPDML);

         try
         {
            SchemaHelper.alterAuditTrailCreateLockingTables(password, skipDdl, skipDml,
                  spoolDevice, statementDelimiter);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                  .getMessage()));
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(e
                        .getMessage()));
         }

         print("Proxy locking tables created and synchronized.");
      }
      else if (options.containsKey(LOCKTABLE_VERIFY))
      {
         optionHandled = true;
         print("Verifying existence of proxy locking tables and their consistency.");

         try
         {
            SchemaHelper.alterAuditTrailVerifyLockingTables(password);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                  .getMessage()));
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(e
                        .getMessage()));
         }

         print("Verification of proxy locking tables and their consistency done.");
      }
      else if (options.containsKey(LOCKTABLE_DROP))
      {
         optionHandled = true;
         print("Dropping any proxy locking tables from Infinity schema.");
         SchemaHelper.alterAuditTrailDropLockingTables(password, spoolDevice,
               statementDelimiter);
         print("Proxy locking tables dropped.");
      }

      return optionHandled;
   }

   private boolean doRunDataClusterOptions(Map options)
   {
      final String password = (String) globalOptions.get("password");
      final String statementDelimiter = (String) options.get(STATEMENT_DELIMITER);
      boolean performUpgrade = options.containsKey(DATACLUSTER_UPGRADE);

      boolean optionHandled = false;
      if (options.containsKey(DATACLUSTER_ENABLE) || performUpgrade)
      {
         optionHandled = true;
         if(performUpgrade)
         {
            print("Performing an upgrade");
         }
         print("Creating missing data cluster tables and synchronizing their table content for Infinity schema.");

         String configFileName = (String) options.get(DATACLUSTER_CONFIG_FILE);
         boolean skipDdl = options.containsKey(AUDITTRAIL_SKIPDDL);
         boolean skipDml = options.containsKey(AUDITTRAIL_SKIPDML);

         try
         {
            SchemaHelper.alterAuditTrailDataClusterTables(password, configFileName,
                  performUpgrade, skipDdl, skipDml, spoolDevice, statementDelimiter);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                  .getMessage()));
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(e
                        .getMessage()));
         }

         print("Data cluster tables created and synchronized.");
      }
      else if (options.containsKey(DATACLUSTER_VERIFY))
      {
         optionHandled = true;
         print("Verifying existence of data cluster tables and their consistency.");

         try
         {
            SchemaHelper.alterAuditTrailVerifyDataClusterTables(password,
                  options.containsKey(DATACLUSTER_VERBOSE) ? System.out : null);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                  .getMessage()));
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(e
                        .getMessage()));
         }

         print("Verification of data cluster tables and their consistency done.");
      }
      else if (options.containsKey(DATACLUSTER_SYNCHRONIZE))
      {
         optionHandled = true;
         print("Resolves all identified inconsistencies of data cluster tables.");

         try
         {
            SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(password,
                  options.containsKey(DATACLUSTER_VERBOSE) ? System.out : null,
                  spoolDevice, statementDelimiter);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                  .getMessage()));
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(e
                        .getMessage()));
         }

         print("Verification of data cluster tables and their consistency done.");
      }
      else if (options.containsKey(DATACLUSTER_DROP))
      {
         optionHandled = true;
         print("Dropping any data cluster tables from CARNOT schema.");

         SchemaHelper.alterAuditTrailDropDataClusterTables(password, spoolDevice,
               statementDelimiter);
         print("Data cluster tables dropped.");
      }

      return optionHandled;
   }

   private boolean doRunPartitionOptions(Map options)
   {
      final String password = (String) globalOptions.get("password");
      final String statementDelimiter = (String) options.get(STATEMENT_DELIMITER);


      boolean optionHandled = false;
      if (options.containsKey(PARTITION_CREATE))
      {
         optionHandled = true;
         print("Creating a new partition in AuditTrail.");

         String partitionId = (String) options.get(PARTITION_CREATE);

         try
         {
            SchemaHelper.alterAuditTrailCreatePartition(password, partitionId,
                  spoolDevice, statementDelimiter);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                  .getMessage()));
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(e
                        .getMessage()));
         }

         print("Creation of partition in AuditTrail done.");
      }
      else if (options.containsKey(PARTITION_DROP))
      {
         optionHandled = true;
         print("Deletes the partition and any contained data from the AuditTrail.");

         String partitionId = (String) options.get(PARTITION_DROP);

         Utils.initCarnotEngine(partitionId, getSysconsoleDBProperties());
         SchemaHelper.alterAuditTrailDropPartition(partitionId, password);

         print("Deletion of partition and contained data from AuditTrail done.");
      }
      else if (options.containsKey(PARTITIONS_LIST))
      {
         optionHandled = true;
         print("Lists all existing partitions.");
         try
         {
            SchemaHelper.alterAuditTrailListPartitions(password);
            print("List completed.");
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                  .getMessage()));
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(e
                        .getMessage()));
         }
      }

      return optionHandled;
   }

   private Map getSysconsoleDBProperties()
   {
      Map properties = CollectionUtils.newHashMap();

      if (globalOptions.containsKey("dbschema"))
      {
         properties.put(
               SessionFactory.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               globalOptions.get("dbschema"));
      }
      if (globalOptions.containsKey("dbuser"))
      {
         properties.put(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX,
               globalOptions.get("dbuser"));
      }
      if (globalOptions.containsKey("dbpassword"))
      {
         properties.put(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_PASSWORD_SUFFIX,
               globalOptions.get("dbpassword"));
      }
      if (globalOptions.containsKey("dburl"))
      {
         properties.put(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_URL_SUFFIX,
               globalOptions.get("dburl"));
      }
      if (globalOptions.containsKey("dbdriver"))
      {
         properties.put(
               SessionProperties.DS_NAME_AUDIT_TRAIL
                     + SessionProperties.DS_DRIVER_CLASS_SUFFIX,
               globalOptions.get("dbdriver"));
      }
      if (globalOptions.containsKey("dbtype"))
      {
         properties.put(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_TYPE_SUFFIX,
               globalOptions.get("dbtype"));
      }
      return properties;
   }

   private boolean doRunCheckConsistencyOptions(Map options)
   {
      boolean optionHandled = false;
      if (options.containsKey(AUDITTRAIL_CHECK_CONSISTENCY))
      {
         optionHandled = true;
         print("Checks wether any problem instances exists in audit trail.");
         AuditTrailConsistencyChecker consistencyChecker = new AuditTrailConsistencyChecker(
               options);
         consistencyChecker.addConsistencyCheck(new SharedDocumentDataConsistencyCheck());
         consistencyChecker.run();
         if (Boolean
               .parseBoolean(SchemaHelper
                     .getAuditTrailProperty(KernelTweakingProperties.INFINITY_DMS_SHARED_DATA_EXIST)))
         {
            print("The audit trail contains data of type \"Document\" and \"Document Set\" that are shared "
                  + "between super- and subprocess although they should not. This will may result in "
                  + "undesired effects at runtime and will slow down archiving operations.");
         }
         else
         {
            print("The audit trail does not contain any problem instances.");
         }
         print("Consistency check done.");
      }
      return optionHandled;
   }

   private boolean doRunSequenceTableOptions(Map options)
   {
      boolean optionHandled = false;
      String dbType = Parameters.instance().getString(
            SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_TYPE_SUFFIX);
      if (DBMSKey.MYSQL_SEQ.getId().equalsIgnoreCase(dbType))
      {
         final String password = (String) globalOptions.get("password");
         optionHandled = true;
         if (options.containsKey(SEQ_TABLE_ENABLE))
         {
            boolean skipDdl = options.containsKey(AUDITTRAIL_SKIPDDL);
            boolean skipDml = options.containsKey(AUDITTRAIL_SKIPDML);
            if (!skipDdl)
            {
               print("Creating 'sequence' table and 'next_sequence_value_for' function.");
            }
            if (!skipDml)
            {
               print("Synchronizing 'sequence' table content.");
            }
            try
            {
               SchemaHelper.alterAuditTrailCreateSequenceTable(password, skipDdl,
                     skipDml, spoolDevice);
            }
            catch (SQLException e)
            {
               trace.warn("", e);
               throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                     .getMessage()));
            }
            if (!skipDdl)
            {
               print("'sequence' table and 'next_sequence_value_for' function created.");
            }
            if (!skipDml)
            {
               print("'sequence' table synchronized.");
            }
         }
         else if (options.containsKey(SEQ_TABLE_VERIFY))
         {
            optionHandled = true;
            print("Verifying existence of 'sequence' table and its consistency.");
            try
            {
               SchemaHelper.alterAuditTrailVerifySequenceTable(password);
            }
            catch (SQLException e)
            {
               trace.warn("", e);
               throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                     .getMessage()));
            }
            print("Verification of 'sequence' table and its consistency done.");
         }
         else if (options.containsKey(SEQ_TABLE_DROP))
         {
            optionHandled = true;
            print("Dropping 'sequence' table from Infinity schema.");
            try
            {
               SchemaHelper.alterAuditTrailDropSequenceTable(password, spoolDevice);
            }
            catch (SQLException e)
            {
               trace.warn("", e);
               throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
                     .getMessage()));
            }
            print("'sequence' table dropped.");
         }
      }
      else
      {
         print("Invalid audittrail type. 'AuditTrail.Type in carnot.properties' "
               + "or global option 'dbtype' has to be set to 'MYSQL_SEQ'.");
      }
      return optionHandled;
   }

   public int doRun(Map options)
   {
      if (globalOptions.containsKey("dbtype"))
      {
         Parameters.instance().set(SessionFactory.AUDIT_TRAIL + ".Type",
               globalOptions.get("dbtype"));
      }

      if (options.containsKey(AUDITTRAIL_SQL))
      {
         String fileName = (String) options.get(AUDITTRAIL_SQL);
         if ( !StringUtils.isEmpty(fileName))
         {
            File ddlFile = new File(fileName);

            try
            {
               spoolDevice = new PrintStream(new FileOutputStream(ddlFile));
            }
            catch(FileNotFoundException x)
            {
               trace.warn("", x);
               throw new PublicException(
                     BpmRuntimeError.CLI_COULD_NOT_INITIALIZE_DDL_SPOOL_FILE.raise(x
                           .getMessage()));
            }
         }
      }

      if ( !doRunLockingTableOptions(options) && !doRunDataClusterOptions(options)
            && !doRunPartitionOptions(options) && !doRunCheckConsistencyOptions(options)
            && !doRunSequenceTableOptions(options))
      {
         print("Unknown option for command auditTrail.");
      }

      return 0;
   }

   public void printCommand(Map options)
   {
      if (options.containsKey(LOCKTABLE_VERIFY)
            || options.containsKey(DATACLUSTER_VERIFY))
      {
         print("Verifies existing Infinity schema:\n");
      }
      else if (options.containsKey(PARTITIONS_LIST))
      {
         print("Lists elements from existing Infinity schema:\n");
      }
      else if (options.containsKey(PARTITION_CREATE))
      {
         print("Creates new partition in Infinity schema:\n");
      }
      else if (options.containsKey(PARTITION_DROP))
      {
         print("Drops existing partition in Infinity schema:\n");
      }
      else
      {
         print("Alters existing Infinity schema:\n");
      }
   }

   public void preprocessOptions(Map options)
   {
      if (options.containsKey(PARTITIONS_LIST))
      {
         isListPartitionsOption = true;
      }

      super.preprocessOptions(options);
   }

   public boolean force()
   {
      if (isListPartitionsOption)
      {
         return true;
      }

      return super.force();
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public String getSummary()
   {
      return "Allows altering of the existing Infinity schema.";
   }

}
