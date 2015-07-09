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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;

public class R9_0_0from7_3_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{

   private static final Logger trace = LogManager.getLogger(R9_0_0from7_3_0RuntimeJob.class);

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

   protected R9_0_0from7_3_0RuntimeJob()
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
      });
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
   }

   @Override
   protected void printMigrateDataInfo()
   {
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
   }

}
