/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.SQLException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.DDLManager;
import org.eclipse.stardust.engine.core.upgrade.framework.*;

public class R9_0_0from8_2_3RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R9_0_0from8_2_3RuntimeJob.class);

   private static final String PI_TABLE_NAME = "process_instance";

   private static final String AI_TABLE_NAME = "activity_instance";

   private static final String WI_TABLE_NAME = "workitem";

   private static final String PI_FIELD_BENCHMARK_VALUE = "benchmarkValue";

   private static final String PI_FIELD_BENCHMARK = "benchmark";

   private static final String AI_FIELD_BENCHMARK_VALUE = "benchmarkValue";

   private static final String WI_FIELD_BENCHMARK_VALUE = "benchmarkValue";

   private static final String RUNTIME_ARTIFACT_TABLE_NAME = "runtime_artifact";

   private static final String RUNTIME_ARTIFACT_PK_SEQUENCE = "runtime_artifact_seq";

   private static final String RUNTIME_ARTIFACT_FIELD_OID = "oid";

   private static final String RUNTIME_ARTIFACT_FIELD_ARTIFACT_TYPE_ID = "artifactTypeId";

   private static final String RUNTIME_ARTIFACT_FIELD_ARTIFACT_ID = "artifactId";

   private static final String RUNTIME_ARTIFACT_FIELD_ARTIFACT_NAME = "artifactName";

   private static final String RUNTIME_ARTIFACT_FIELD_REFERENCE_ID = "referenceId";

   private static final String RUNTIME_ARTIFACT_FIELD_VALID_FROM = "validFrom";

   private static final String RUNTIME_ARTIFACT_FIELD_PARTITION = "partition";

   private static final String RUNTIME_ARTIFACT_IDX1 = "runtime_artifact_idx1";

   private static final String RUNTIME_ARTIFACT_IDX2 = "runtime_artifact_idx2";
   
   static final Version VERSION = Version.createFixedVersion(9, 0, 0);

   private RuntimeUpgradeTaskExecutor upgradeTaskExecutor;

   private UpgradeObserver observer;

   protected R9_0_0from8_2_3RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE, DBMSKey.MSSQL8,
            DBMSKey.MYSQL_SEQ});
      observer = this;
      initUpgradeTasks();
   }

   private void initUpgradeTasks()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R9_0_0from7_3_0RuntimeJob",
            Parameters.instance().getBoolean(RuntimeUpgrader.UPGRADE_DATA, false));

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            DatabaseHelper.createTable(item, new CreateTableInfo(
                  RUNTIME_ARTIFACT_TABLE_NAME)
            {
               private final FieldInfo oid = new FieldInfo(RUNTIME_ARTIFACT_FIELD_OID,
                     Long.TYPE, 0, true);

               private final FieldInfo artifactTypeId = new FieldInfo(
                     RUNTIME_ARTIFACT_FIELD_ARTIFACT_TYPE_ID, String.class, 255, false);

               private final FieldInfo artifactId = new FieldInfo(
                     RUNTIME_ARTIFACT_FIELD_ARTIFACT_ID, String.class, 255, false);

               private final FieldInfo artifactName = new FieldInfo(
                     RUNTIME_ARTIFACT_FIELD_ARTIFACT_NAME, String.class, 255, false);

               private final FieldInfo referenceId = new FieldInfo(
                     RUNTIME_ARTIFACT_FIELD_REFERENCE_ID, String.class, 255, false);

               private final FieldInfo validFrom = new FieldInfo(
                     RUNTIME_ARTIFACT_FIELD_VALID_FROM, Long.TYPE, 0, false);

               private final FieldInfo partition = new FieldInfo(
                     RUNTIME_ARTIFACT_FIELD_PARTITION, Long.TYPE, 0, false);

               private final IndexInfo IDX1 = new IndexInfo(RUNTIME_ARTIFACT_IDX1, true,
                     new FieldInfo[] {oid});

               private final IndexInfo IDX2 = new IndexInfo(RUNTIME_ARTIFACT_IDX2, false,
                     new FieldInfo[] {artifactTypeId, artifactId, validFrom, partition});

               @Override
               public FieldInfo[] getFields()
               {
                  return new FieldInfo[] {
                        oid, artifactTypeId, artifactId, artifactName, referenceId,
                        validFrom, partition};
               }

               @Override
               public IndexInfo[] getIndexes()
               {
                  return new IndexInfo[] {IDX1, IDX2};
               }

               @Override
               public String getSequenceName()
               {
                  return RUNTIME_ARTIFACT_PK_SEQUENCE;
               }
            }, observer);
         }

         @Override
         public void printInfo()
         {
         }
      });
      
      upgradeTaskExecutor.addUpgradeSchemaTask(new SignalMsgTableUpgradeTask());
      upgradeTaskExecutor.addUpgradeSchemaTask(new SignalMsgLookupTableUpgradeTask());
      upgradeTaskExecutor.addUpgradeSchemaTask(new DaemonLogLckTableUpgradeTask());
   }

   @Override
   public Version getVersion()
   {
      return VERSION;
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }

   @Override
   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeUpgradeSchemaTasks();

      DatabaseHelper.alterTable(item, new AlterTableInfo(PI_TABLE_NAME)
      {
         private final FieldInfo BENCHMARK = new FieldInfo(PI_FIELD_BENCHMARK, Long.TYPE);

         private final FieldInfo BENCHMARK_VALUE = new FieldInfo(
               PI_FIELD_BENCHMARK_VALUE, Integer.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {BENCHMARK, BENCHMARK_VALUE};
         }

      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(AI_TABLE_NAME)
      {

         private final FieldInfo BENCHMARK_VALUE = new FieldInfo(
               AI_FIELD_BENCHMARK_VALUE, Integer.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { BENCHMARK_VALUE };
         }

      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(WI_TABLE_NAME)
      {

         private final FieldInfo BENCHMARK_VALUE = new FieldInfo(
               WI_FIELD_BENCHMARK_VALUE, Integer.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { BENCHMARK_VALUE };
         }

      }, this);
   }

   @Override
   protected void migrateData(boolean recover) throws UpgradeException
   {
   }

   @Override
   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
      upgradeTaskExecutor.printUpgradeSchemaInfo();
   }

   @Override
   protected void printMigrateDataInfo()
   {
      upgradeTaskExecutor.printMigrateDataInfo();
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
      upgradeTaskExecutor.printFinalizeSchemaInfo();
   }
   
   public class SignalMsgTableUpgradeTask implements UpgradeTask
   {
      private static final String SIGNAL_MESSAGE_TABLE_NAME = "signal_message";

      private static final String SIGNAL_MESSAGE_FIELD_OID = "oid";

      private static final String SIGNAL_MESSAGE_FIELD_PARTITION_OID = "partitionOid";

      private static final String SIGNAL_MESSAGE_FIELD_SIGNAL_NAME = "signalName";

      private static final String SIGNAL_MESSAGE_FIELD_MESSAGE_CONTENT = "messageContent";

      private static final String SIGNAL_MESSAGE_FIELD_TIMESTAMP = "timestamp";

      private static final String SIGNAL_MESSAGE_IDX1 = "signal_message_idx1";
      
      private static final String SIGNAL_MESSAGE_PK_SEQUENCE = "signal_message_seq";

      @Override
      public void execute()
      {
         DatabaseHelper.createTable(item, new CreateTableInfo(SIGNAL_MESSAGE_TABLE_NAME)
         {
            private final FieldInfo oid = new FieldInfo(SIGNAL_MESSAGE_FIELD_OID,
                  Long.TYPE, 0, true);

            private final FieldInfo partitionOid = new FieldInfo(
                  SIGNAL_MESSAGE_FIELD_PARTITION_OID, Long.TYPE, 0, false);

            private final FieldInfo signalName = new FieldInfo(
                  SIGNAL_MESSAGE_FIELD_SIGNAL_NAME, String.class, 255, false);

            private final FieldInfo messageContent = new FieldInfo(
                  SIGNAL_MESSAGE_FIELD_MESSAGE_CONTENT, String.class, 255, false);

            private final FieldInfo timestamp = new FieldInfo(
                  SIGNAL_MESSAGE_FIELD_TIMESTAMP, Long.TYPE, 0, false);

            private final IndexInfo IDX1 = new IndexInfo(SIGNAL_MESSAGE_IDX1, true,
                  new FieldInfo[] {oid});

            @Override
            public String getSequenceName()
            {
               return SIGNAL_MESSAGE_PK_SEQUENCE;
            }

            @Override
            public FieldInfo[] getFields()
            {
               return new FieldInfo[] {
                     oid, partitionOid, signalName, messageContent, timestamp};
            }

            @Override
            public IndexInfo[] getIndexes()
            {
               return new IndexInfo[] {IDX1};
            }

         }, observer);
      }

      public void printInfo()
      {
         info("A new table '" + SIGNAL_MESSAGE_TABLE_NAME + "' with the columns " + "'"
               + SIGNAL_MESSAGE_FIELD_OID + "', '" + SIGNAL_MESSAGE_FIELD_PARTITION_OID
               + "', '" + SIGNAL_MESSAGE_FIELD_SIGNAL_NAME + "', '"
               + SIGNAL_MESSAGE_FIELD_MESSAGE_CONTENT + "', '"
               + SIGNAL_MESSAGE_FIELD_TIMESTAMP + "' " + "and index '"
               + SIGNAL_MESSAGE_IDX1 + "' will be created.");
      }

   }

   public class SignalMsgLookupTableUpgradeTask implements UpgradeTask
   {
      private static final String SIGNAL_MESSAGE_LOOKUP_TABLE_NAME = "signal_message_lookup";

      private static final String SIGNAL_MESSAGE_LOOKUP_FIELD_PARTITION_OID = "partitionOid";

      private static final String SIGNAL_MESSAGE_LOOKUP_FIELD_SIGNAL_NAME = "signalDataHash";

      private static final String SIGNAL_MESSAGE_LOOKUP_FIELD_SIGNAL_MESSAGE_OID = "signalMessageOid";

      private static final String SIGNAL_MESSAGE_LOOKUP_IDX1 = "signal_message_lookup_idx1";

      @Override
      public void execute()
      {
         DatabaseHelper.createTable(item, new CreateTableInfo(
               SIGNAL_MESSAGE_LOOKUP_TABLE_NAME)
         {
            private final FieldInfo partitionOid = new FieldInfo(
                  SIGNAL_MESSAGE_LOOKUP_FIELD_PARTITION_OID, Long.TYPE, 0, false);

            private final FieldInfo signalDataHash = new FieldInfo(
                  SIGNAL_MESSAGE_LOOKUP_FIELD_SIGNAL_NAME, String.class, 255, false);

            private final FieldInfo signalMessageOid = new FieldInfo(
                  SIGNAL_MESSAGE_LOOKUP_FIELD_SIGNAL_MESSAGE_OID, Long.TYPE, 0, false);

            private final IndexInfo IDX1 = new IndexInfo(SIGNAL_MESSAGE_LOOKUP_IDX1,
                  false, new FieldInfo[] {partitionOid, signalDataHash});

            @Override
            public String getSequenceName()
            {
               return null;
            }

            @Override
            public FieldInfo[] getFields()
            {
               return new FieldInfo[] {partitionOid, signalDataHash, signalMessageOid};
            }

            @Override
            public IndexInfo[] getIndexes()
            {
               return new IndexInfo[] {IDX1};
            }

         }, observer);
      }

      @Override
      public void printInfo()
      {
         info("A new table '" + SIGNAL_MESSAGE_LOOKUP_TABLE_NAME + "' with the columns "
               + "'" + SIGNAL_MESSAGE_LOOKUP_FIELD_PARTITION_OID + "', '"
               + SIGNAL_MESSAGE_LOOKUP_FIELD_SIGNAL_NAME + "', '"
               + SIGNAL_MESSAGE_LOOKUP_FIELD_SIGNAL_MESSAGE_OID + " and index '"
               + SIGNAL_MESSAGE_LOOKUP_IDX1 + "' will be created.");
      }

   }
   
   public class DaemonLogLckTableUpgradeTask implements UpgradeTask
   {
      private static final String DAEMON_LOG_LCK_TABLE_NAME = "daemon_log_lck";
      private static final String DAEMON_LOG_LCK_FIELD_OID = "oid";
      private static final String AI_LCK_TABLE_NAME = "activity_instance_lck";
      
      @Override
      public void execute()
      {
         // Lock table will only be created if any other lock table already exists, e.g.
         // the one for AIBean
         if (!item.isArchiveAuditTrail() && containsTable(AI_LCK_TABLE_NAME))
         {
            DatabaseHelper.createTable(item, new CreateTableInfo(
                  DAEMON_LOG_LCK_TABLE_NAME)
            {
               private final FieldInfo oid = new FieldInfo(DAEMON_LOG_LCK_FIELD_OID,
                     Long.TYPE, 0, false);

               @Override
               public String getSequenceName()
               {
                  return null;
               }

               @Override
               public FieldInfo[] getFields()
               {
                  return new FieldInfo[] {oid};
               }

            }, observer);
         }
      }

      @Override
      public void printInfo()
      {
         // Lock table will only be created if any other lock table already exists, e.g.
         // the one for AIBean
         if (!item.isArchiveAuditTrail() && containsTable(AI_LCK_TABLE_NAME))
         {
            info("A new table '" + DAEMON_LOG_LCK_TABLE_NAME + "' with the columns "
                  + "'" + DAEMON_LOG_LCK_FIELD_OID + "' will be created.");
         }
      }
   }

   private boolean containsTable(String tableName)
   {
      boolean result = false;
      DDLManager ddlManager = new DDLManager(item.getDbDescriptor());
      try
      {
         result = ddlManager.containsTable(DatabaseHelper.getSchemaName(), tableName,
               item.getConnection());
      }
      catch (SQLException e)
      {
         error("", e);
      }

      return result;
   }

}
