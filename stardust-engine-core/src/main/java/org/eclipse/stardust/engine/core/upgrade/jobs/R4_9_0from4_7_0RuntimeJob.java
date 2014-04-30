/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author born
 * @version $Revision: $
 */
public class R4_9_0from4_7_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R4_9_0from4_7_0RuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(4, 9, 0);

   private static final String LOG_ENTRY_TABLE_NAME = "log_entry";
   private static final String LOG_ENTRY_AI_COLUMN = "activityInstance";
   private static final String LOG_ENTRY_PI_COLUMN = "processInstance";

   R4_9_0from4_7_0RuntimeJob()
   {
      super(new DBMSKey[] { DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE });
   }

   public Version getVersion()
   {
      return VERSION;
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DBMSKey targetDBMS = item.getDbDescriptor().getDbmsKey();
      if (DBMSKey.ORACLE.equals(targetDBMS) || DBMSKey.ORACLE9i.equals(targetDBMS))
      {
         try
         {
            DatabaseMetaData databaseMetaData = DatabaseHelper.getDatabaseMetaData(item);
            //list of persistence table for 4_7
            String[] persistenceTables4_7 = {
                  "activity_instance", "activity_inst_log", "act_inst_history",
                  "act_inst_property", "trans_inst", "trans_token", "daemon_log", "data_value",
                  "event_binding", "log_entry", "message_store", "property", "timer_log",
                  "usergroup", "usergroup_property", "workflowuser", "user_property",
                  "wfuser_session", "user_participant", "user_usergroup", "process_instance",
                  "proc_inst_property", "procinst_scope", "procinst_hierarchy",
                  "structured_data_value", "domain", "domain_hierarchy", "wfuser_domain",
                  "wfuser_realm", "workitem", "structured_data", "model", "string_data",
                  "activity", "data", "event_handler", "participant", "partition",
                  "process_definition", "transition", "process_trigger"};

            //qualifying table name is done by the called methods
            for(String tableName: persistenceTables4_7)
            {
               //change every string column from varchar byte to varchar  char
               List<FieldInfo> affectedColumns
                  = DatabaseHelper.getColumns(databaseMetaData, tableName, Types.VARCHAR);
               if(affectedColumns != null && !affectedColumns.isEmpty())
               {
                  final FieldInfo[] fieldInfos
                     = affectedColumns.toArray(new FieldInfo[affectedColumns.size()]);

                  //qualifying table name is done within that call
                  DatabaseHelper.alterTable(item,
                        new AlterTableInfo(tableName)
                        {
                           public FieldInfo[] getModifiedFields()
                           {
                              return (fieldInfos);
                           }

                        }, this);

               }
            }
         }
         catch(SQLException e)
         {
            error("Failed determining the tables and/or string columns to modify.", e);
         }
      }

      DatabaseHelper.alterTable(item, new AlterTableInfo(LOG_ENTRY_TABLE_NAME)
      {
         private final FieldInfo ACTIVITY_INSTANCE = new FieldInfo(LOG_ENTRY_AI_COLUMN,
               Long.TYPE, 0, false);

         private final FieldInfo PROCESS_INSTANCE = new FieldInfo(LOG_ENTRY_PI_COLUMN,
               Long.TYPE, 0, false);

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {//
                  new IndexInfo("log_entry_idx2", false,
                        new FieldInfo[] {ACTIVITY_INSTANCE}),
                  new IndexInfo("log_entry_idx3", false,
                        new FieldInfo[] {PROCESS_INSTANCE})};
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void printMigrateDataInfo()
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
