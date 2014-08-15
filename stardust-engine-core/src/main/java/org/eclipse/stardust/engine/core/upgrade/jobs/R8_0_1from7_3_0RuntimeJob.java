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
package org.eclipse.stardust.engine.core.upgrade.jobs;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgradeTaskExecutor;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeTask;

/**
 *
 * @author Roland.Stamm
 * @version $Revision: $
 */
public class R8_0_1from7_3_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{

   private static final Logger trace = LogManager.getLogger(R8_0_1from7_3_0RuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(8, 0, 1);

   protected static final String CLOB_TABLE_NAME = "clob_data";

   private static final String CLOB_FIELD_STRING_KEY = "stringKey";

   private static final String CLOB_FIELD_OWNER_TYPE = "ownerType";

   private static final String CLOB_IDX3 = "clob_dt_i3";

   private RuntimeUpgradeTaskExecutor upgradeTaskExecutor;

   protected R8_0_1from7_3_0RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE, DBMSKey.MSSQL8});

      initUpgradeTasks();
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }

   public Version getVersion()
   {
      return VERSION;
   }

   private void initUpgradeTasks()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R8_0_1from7_3_0RuntimeJob",
            Parameters.instance().getBoolean(RuntimeUpgrader.UPGRADE_DATA, false));
      initUpgradeSchemaTasks();
      // initMigrateDataTasks();
      // initFinalizeSchemaTasks();
   }

   private void initUpgradeSchemaTasks()
   {
      final R8_0_1from7_3_0RuntimeJob runtimeJob = this;

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            DatabaseHelper.alterTable(item, new AlterTableInfo(
                  CLOB_TABLE_NAME)
            {
               private final FieldInfo STRING_KEY = new FieldInfo(
                     CLOB_FIELD_STRING_KEY, String.class, 255, false);

               private final FieldInfo OWNER_TYPE = new FieldInfo(
                     CLOB_FIELD_OWNER_TYPE, String.class);

               @Override
               public FieldInfo[] getAddedFields()
               {
                  return new FieldInfo[] {STRING_KEY};
               }

               @Override
               public IndexInfo[] getAddedIndexes()
               {
                  return new IndexInfo[] {//
                  new IndexInfo(CLOB_IDX3, false, new FieldInfo[] {
                        OWNER_TYPE, STRING_KEY})};
               }

            }, runtimeJob);
         }
      });
   }

   @Override
   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeUpgradeSchemaTasks();
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
      info("The new column 'stringKey' and index 'clob_dt_i3' will be created in table 'clob_data'.");
   }

   @Override
   protected void migrateData(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeMigrateDataTasks();
   }

   @Override
   protected void printMigrateDataInfo()
   {

   }

   @Override
   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeFinalizeSchemaTasks();
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {

   }
}