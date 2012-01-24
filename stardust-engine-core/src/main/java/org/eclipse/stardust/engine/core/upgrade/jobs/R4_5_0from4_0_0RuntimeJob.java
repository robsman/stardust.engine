/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.DDLManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceLogBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.utils.PerformerUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.CreateTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author fherinean
 * @version $Revision$
 */
public class R4_5_0from4_0_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R4_5_0from4_0_0RuntimeJob.class);
   
   private static final Version VERSION = new Version(4, 5, 0);
   
   private static final int[] aiLogStateMapping = {
      ActivityInstanceState.CREATED,
      ActivityInstanceState.APPLICATION,
      ActivityInstanceState.SUSPENDED,
      ActivityInstanceState.COMPLETED,
      ActivityInstanceState.INTERRUPTED,
      ActivityInstanceState.ABORTED};

   private static final String SDV_TABLE_NAME = "structured_data_value";
   private static final String SDV_FIELD__OID = "oid";
   private static final String SDV_FIELD__PROCESS_INSTANCE = "processInstance";
   private static final String SDV_FIELD__PARENT = "parent";
   private static final String SDV_FIELD__ENTRY_KEY = "entryKey";
   private static final String SDV_FIELD__XPATH = "xpath";
   private static final String SDV_FIELD__TYPE_KEY = "type_key";
   private static final String SDV_FIELD__STRING_VALUE = "string_value";
   private static final String SDV_FIELD__NUMBER_VALUE = "number_value";
   private static final String SDV_PK_SEQUENCE = "structured_data_value_seq";

   private static final String AI_LCK_TABLE_NAME = "activity_instance_lck";
   private static final String SDV_LCK_TABLE_NAME = "structured_data_value_lck";
   private static final String SDV_LCK_FIELD__OID = "oid";
   
   private static final String SD_TABLE_NAME = "structured_data";
   private static final String SD_FIELD__OID = "oid";
   private static final String SD_FIELD__DATA = "data";
   private static final String SD_FIELD__MODEL = "model";
   private static final String SD_FIELD__XPATH = "xpath";
   private static final String SD_PK_SEQUENCE = "structured_data_seq";

   private static final String AIH_TABLE_NAME = "act_inst_history";
   private static final String AIH_FIELD__PROCESS_INSTANCE = "processInstance";
   private static final String AIH_FIELD__ACTIVITY_INSTANCE = "activityInstance";
   private static final String AIH_FIELD__STATE = "state";
   private static final String AIH_FIELD__FROM = "fromTimestamp";
   private static final String AIH_FIELD__UNTIL = "untilTimestamp";
   private static final String AIH_FIELD__DOMAIN = "domain";
   private static final String AIH_FIELD__PERFORMER_KIND = "performerKind";
   private static final String AIH_FIELD__PERFORMER = "performer";
   private static final String AIH_FIELD__ON_BEHALF_OF_KIND = "onBehalfOfKind";
   private static final String AIH_FIELD__ON_BEHALF_OF = "onBehalfOf";
   private static final String AIH_FIELD__USER = "workflowUser";
   
   // UserSession table.
   private static final String US_TABLE_NAME = "wfuser_session";
   private static final String US_FIELD__OID = "oid";
   private static final String US_FIELD__USER = "workflowUser";
   private static final String US_FIELD__CLIENT_ID = "clientId";
   private static final String US_FIELD__START_TIME = "startTime";
   private static final String US_FIELD__LAST_MODIFICATION_TIME = "lastModificationTime";
   private static final String US_FIELD__EXPIRATION_TIME = "expirationTime";
   private static final String US_PK_SEQUENCE = US_TABLE_NAME + "_seq";

   private int batchSize = 500;

   R4_5_0from4_0_0RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY});

      String bs = Parameters.instance().getString(RuntimeUpgrader.UPGRADE_BATCH_SIZE);
      if (bs != null)
      {
         batchSize = Integer.parseInt(bs);
      }
   }

   public Version getVersion()
   {
      return VERSION;
   }
   
   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.alterTable(item, new AlterTableInfo(ProcessInstanceBean.TABLE_NAME)
      {
         private final FieldInfo FIELD_PRIORITY = new FieldInfo(
               ProcessInstanceBean.FIELD__PRIORITY, Integer.TYPE);
         
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{FIELD_PRIORITY};
         }
      }, this);
      
      DatabaseHelper.createTable(item, new CreateTableInfo(SDV_TABLE_NAME)
      {
         private static final String INDEX_PREFIX = "struct_dv_index";

         private final FieldInfo OID = new FieldInfo(SDV_FIELD__OID, Long.TYPE, 0, true);
         private final FieldInfo ROOT_OID = new FieldInfo(SDV_FIELD__PROCESS_INSTANCE, Long.TYPE);
         private final FieldInfo PARENT_OID = new FieldInfo(SDV_FIELD__PARENT, Long.TYPE);
         private final FieldInfo ENTRY_KEY = new FieldInfo(SDV_FIELD__ENTRY_KEY, String.class, 50);
         private final FieldInfo XPATH_OID = new FieldInfo(SDV_FIELD__XPATH, Long.TYPE);
         private final FieldInfo TYPE_KEY = new FieldInfo(SDV_FIELD__TYPE_KEY, Integer.TYPE);
         private final FieldInfo STRING_VALUE = new FieldInfo(SDV_FIELD__STRING_VALUE, String.class, 128);
         private final FieldInfo NUMBER_VALUE = new FieldInfo(SDV_FIELD__NUMBER_VALUE, Long.TYPE);
         
         private final IndexInfo IDX1 = new IndexInfo(INDEX_PREFIX + "1", true,
               new FieldInfo[] { OID });
         private final IndexInfo IDX2 = new IndexInfo(INDEX_PREFIX + "2", false,
               new FieldInfo[] { PARENT_OID });
         private final IndexInfo IDX3 = new IndexInfo(INDEX_PREFIX + "3", false,
               new FieldInfo[] { XPATH_OID });
         private final IndexInfo IDX4 = new IndexInfo(INDEX_PREFIX + "4", false,
               new FieldInfo[] { TYPE_KEY });
         private final IndexInfo IDX5 = new IndexInfo(INDEX_PREFIX + "5", false,
               new FieldInfo[] { NUMBER_VALUE });
         private final IndexInfo IDX6 = new IndexInfo(INDEX_PREFIX + "6", false,
               new FieldInfo[] { STRING_VALUE });
         private final IndexInfo IDX7 = new IndexInfo(INDEX_PREFIX + "7", false,
               new FieldInfo[] { ROOT_OID });

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] { OID, ROOT_OID, PARENT_OID, ENTRY_KEY, XPATH_OID,
                  TYPE_KEY, STRING_VALUE, NUMBER_VALUE };
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] { IDX1, IDX2, IDX3, IDX4, IDX5, IDX6, IDX7 };
         }

         public String getSequenceName()
         {
            return item.isArchiveAuditTrail() ? null : SDV_PK_SEQUENCE;
         }
      }, this);

      // Lock table will only be created if any other lock table already exists, e.g. the one for AIBean
      if (!item.isArchiveAuditTrail() && containsTable(AI_LCK_TABLE_NAME))      
      {
         DatabaseHelper.createTable(item, new CreateTableInfo(SDV_LCK_TABLE_NAME)
         {
            private static final String INDEX_NAME = "struct_dv_lck_idx";
   
            private final FieldInfo OID = new FieldInfo(SDV_LCK_FIELD__OID, Long.TYPE, 0, true);
            
            private final IndexInfo IDX = new IndexInfo(INDEX_NAME, true,
                  new FieldInfo[] { OID });
   
            public FieldInfo[] getFields()
            {
               return new FieldInfo[] { OID };
            }
   
            public IndexInfo[] getIndexes()
            {
               return new IndexInfo[] { IDX };
            }
   
            public String getSequenceName()
            {
               return null;
            }
         }, this);
      }

      DatabaseHelper.createTable(item, new CreateTableInfo(SD_TABLE_NAME)
      {
         private static final String INDEX_PREFIX = "struct_data_idx";

         private final FieldInfo OID = new FieldInfo(SD_FIELD__OID, Long.TYPE, 0, true);
         private final FieldInfo DATA = new FieldInfo(SD_FIELD__DATA, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(SD_FIELD__MODEL, Long.TYPE);
         private final FieldInfo XPATH = new FieldInfo(SD_FIELD__XPATH, String.class, 200);
         
         private final IndexInfo IDX1 = new IndexInfo(INDEX_PREFIX + "1", true,
               new FieldInfo[] { OID });
         private final IndexInfo IDX2 = new IndexInfo(INDEX_PREFIX + "2", false,
               new FieldInfo[] { XPATH });

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] { OID, DATA, MODEL, XPATH };
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] { IDX1, IDX2 };
         }

         public String getSequenceName()
         {
            return item.isArchiveAuditTrail() ? null : SD_PK_SEQUENCE;
         }
      }, this);
      
      // ActivityInstanceHistory table
      DatabaseHelper.createTable(item, new CreateTableInfo(AIH_TABLE_NAME)
      {
         private final FieldInfo PROCESS_INSTANCE = new FieldInfo(
               AIH_FIELD__PROCESS_INSTANCE, Long.TYPE);
         private final FieldInfo ACTIVITY_INSTANCE = new FieldInfo(
               AIH_FIELD__ACTIVITY_INSTANCE, Long.TYPE);
         private final FieldInfo STATE = new FieldInfo(
               AIH_FIELD__STATE, Integer.TYPE);
         private final FieldInfo FROM = new FieldInfo(
               AIH_FIELD__FROM, Long.TYPE);
         private final FieldInfo UNTIL = new FieldInfo(
               AIH_FIELD__UNTIL, Long.TYPE);
         private final FieldInfo DOMAIN = new FieldInfo(
               AIH_FIELD__DOMAIN, Long.TYPE);
         private final FieldInfo PERFORMER_KIND = new FieldInfo(
               AIH_FIELD__PERFORMER_KIND, Integer.TYPE);
         private final FieldInfo PERFORMER = new FieldInfo(
               AIH_FIELD__PERFORMER, Long.TYPE);
         private final FieldInfo ON_BEHALF_OF_KIND = new FieldInfo(
               AIH_FIELD__ON_BEHALF_OF_KIND, Integer.TYPE);
         private final FieldInfo ON_BEHALF_OF = new FieldInfo(
               AIH_FIELD__ON_BEHALF_OF, Long.TYPE);
         private final FieldInfo USER = new FieldInfo(
               AIH_FIELD__USER, Long.TYPE);

         private final IndexInfo IDX1 = new IndexInfo("act_inst_hist_idx1", true,
               new FieldInfo[] {ACTIVITY_INSTANCE, FROM});
         private final IndexInfo IDX2 = new IndexInfo("act_inst_hist_idx2", false,
               new FieldInfo[] {PROCESS_INSTANCE});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {PROCESS_INSTANCE, ACTIVITY_INSTANCE, STATE, FROM, UNTIL,
                  DOMAIN, PERFORMER_KIND, PERFORMER, ON_BEHALF_OF_KIND, ON_BEHALF_OF, USER};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] { IDX1, IDX2 };
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);
      
      // UserSession table
      DatabaseHelper.createTable(item, new CreateTableInfo(US_TABLE_NAME)
      {
         private static final String INDEX_PREFIX = US_TABLE_NAME;

         private final FieldInfo OID = new FieldInfo(US_FIELD__OID, Long.TYPE, 0, true);
         private final FieldInfo USER = new FieldInfo(US_FIELD__USER, Long.TYPE, 0);
         private final FieldInfo CLIENT_ID = new FieldInfo(US_FIELD__CLIENT_ID,
               String.class, 0);
         private final FieldInfo START_TIME = new FieldInfo(US_FIELD__START_TIME,
               Long.TYPE, 0);
         private final FieldInfo LAST_MODIFICATION_TIME = new FieldInfo(
               US_FIELD__LAST_MODIFICATION_TIME, Long.TYPE, 0);
         private final FieldInfo FIELD__EXPIRATION_TIME = new FieldInfo(
               US_FIELD__EXPIRATION_TIME, Long.TYPE, 0);
         
         private final IndexInfo IDX1 = new IndexInfo(INDEX_PREFIX + "1", true,
               new FieldInfo[] { OID });
         private final IndexInfo IDX2 = new IndexInfo(INDEX_PREFIX + "2", false,
               new FieldInfo[] { USER, START_TIME });
         
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] { OID, USER, CLIENT_ID, START_TIME,
                  LAST_MODIFICATION_TIME, FIELD__EXPIRATION_TIME };
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] { IDX1, IDX2 };
         }

         public String getSequenceName()
         {
            return item.isArchiveAuditTrail() ? null : US_PK_SEQUENCE;
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      try
      {
         // perform data migration in well defined TXs
         item.getConnection().setAutoCommit(false);
         
         info("Setting priorities ...");
         
         updateProcessInstanceTable();
         
         updateActivityInstanceHistoryTable();
         
      }
      catch (SQLException sqle)
      {
         SQLException ne = sqle;
         do
         {
            trace.error("Failed migrating runtime item tables (nested exception).", ne);
         }
         while (null != (ne = ne.getNextException()));

         try
         {
            item.rollback();
         }
         catch (SQLException e1)
         {
            warn("Failed rolling back transaction.", e1);
         }
         error("Failed migrating runtime item tables.", sqle);
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   private void updateProcessInstanceTable() throws SQLException
   {
      final int NORMAL_PRIORITY = 0;

      final String TABLE_NAME_PROC_INST = ProcessInstanceBean.TABLE_NAME;
      final String FIELD_NAME_PRIORITY = ProcessInstanceBean.FIELD__PRIORITY;
      final String FIELD_NAME_OID = ProcessInstanceBean.FIELD__OID;
      
      PreparedStatement selectRowsStmt = null;
      PreparedStatement updateRowsStmt = null;
      try
      {
         String procInstTableName = DatabaseHelper.getQualifiedName(TABLE_NAME_PROC_INST);
         
         StringBuffer selectCmd = new StringBuffer()
               .append("SELECT ").append(FIELD_NAME_OID)
               .append(" FROM ").append(procInstTableName)
               .append(" WHERE ").append(FIELD_NAME_PRIORITY).append(" IS NULL");

         StringBuffer updateCmd = new StringBuffer()
               .append("UPDATE ").append(procInstTableName)
               .append(" SET ").append(FIELD_NAME_PRIORITY).append(" = ?")
               .append(" WHERE ").append(FIELD_NAME_OID).append(" = ?");

         Connection connection = item.getConnection();

         selectRowsStmt = connection.prepareStatement(selectCmd.toString());
         updateRowsStmt = connection.prepareStatement(updateCmd.toString());

         int rowCounter = 0;

         ResultSet pendingRows = null;
         try
         {
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;

               pendingRows = selectRowsStmt.executeQuery();
               while (pendingRows.next())
               {
                  eating = true;
                  try
                  {
                     final long rowOid = pendingRows.getLong(1);

                     updateRowsStmt.setInt(1, NORMAL_PRIORITY);
                     updateRowsStmt.setLong(2, rowOid);
                     updateRowsStmt.addBatch();

                     ++rowCounter;
                  }
                  catch (SQLException e)
                  {
                     warn(e.getMessage(), null);
                  }

                  if (batchSize <= (rowCounter - rowCounterBackup))
                  {
                     break;
                  }
               }

               if ( !eating)
               {
                  break;
               }

               updateRowsStmt.executeBatch();
               connection.commit();

               info(MessageFormat.format("Committing updates of field {1} on table {0}"
                     + " after {2} rows.", new Object[] {
                     TABLE_NAME_PROC_INST, FIELD_NAME_PRIORITY, new Integer(rowCounter)}));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(pendingRows);
         }

         info(TABLE_NAME_PROC_INST + " table upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectRowsStmt);
         QueryUtils.closeStatement(updateRowsStmt);
      }
   }

   private void updateActivityInstanceHistoryTable() throws SQLException
   {
      PreparedStatement selectLastActivityInstanceStmt = null;
      PreparedStatement firstSelectActivityInstanceStmt = null;
      PreparedStatement selectActivityInstanceStmt = null;
      PreparedStatement selectActivityInstanceLogsStmt = null;
      PreparedStatement insertStmt = null;

      try
      {         
         String activityInstanceTableName 
            = DatabaseHelper.getQualifiedName(ActivityInstanceBean.TABLE_NAME);
         String activityInstanceHistoryTableName 
            = DatabaseHelper.getQualifiedName(ActivityInstanceHistoryBean.TABLE_NAME);
         String activityInstanceLogTableName 
            = DatabaseHelper.getQualifiedName(ActivityInstanceLogBean.TABLE_NAME);
      
         StringBuffer selectLastActivityInstanceCmd = new StringBuffer()
               .append("SELECT MAX(").append(ActivityInstanceHistoryBean.FIELD__ACTIVITY_INSTANCE).append(")")
               .append(" FROM ").append(activityInstanceHistoryTableName);

         StringBuffer firstSelectActivityInstanceCmd = new StringBuffer()
               .append("SELECT ").append(ActivityInstanceBean.FIELD__OID).append(", ")
                                 .append(ActivityInstanceBean.FIELD__PROCESS_INSTANCE)
               .append(" FROM ").append(activityInstanceTableName)
               .append(" ORDER BY ").append(ActivityInstanceBean.FIELD__OID);

         StringBuffer selectActivityInstanceCmd = new StringBuffer()
               .append("SELECT ").append(ActivityInstanceBean.FIELD__OID).append(", ")
                                 .append(ActivityInstanceBean.FIELD__PROCESS_INSTANCE)
               .append(" FROM ").append(activityInstanceTableName)
               .append(" WHERE ").append(ActivityInstanceBean.FIELD__OID).append(" > ?")
               .append(" ORDER BY ").append(ActivityInstanceBean.FIELD__OID);

         StringBuffer selectActivityInstanceLogsCmd = new StringBuffer()
               .append("SELECT ").append(ActivityInstanceLogBean.FIELD__STAMP).append(", ")
                                 .append(ActivityInstanceLogBean.FIELD__TYPE).append(", ")
                                 .append(ActivityInstanceLogBean.FIELD__WORKFLOW_USER)
               .append(" FROM ").append(activityInstanceLogTableName)
               .append(" WHERE ").append(ActivityInstanceLogBean.FIELD__ACTIVITY_INSTANCE).append(" = ?")
               .append(" ORDER BY ").append(ActivityInstanceLogBean.FIELD__STAMP);

         StringBuffer insertCmd = new StringBuffer()
               .append("INSERT INTO ").append(activityInstanceHistoryTableName )
               .append(" (")
                     .append(ActivityInstanceHistoryBean.FIELD__PROCESS_INSTANCE).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__ACTIVITY_INSTANCE).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__STATE).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__FROM).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__UNTIL).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__DOMAIN).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__PERFORMER_KIND).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__PERFORMER).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__ON_BEHALF_OF_KIND).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__ON_BEHALF_OF).append(", ")
                     .append(ActivityInstanceHistoryBean.FIELD__USER + ")")
               .append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

         Connection connection = item.getConnection();

         selectLastActivityInstanceStmt = connection.prepareStatement(
               selectLastActivityInstanceCmd.toString());
         firstSelectActivityInstanceStmt = connection.prepareStatement(
               firstSelectActivityInstanceCmd.toString());
         selectActivityInstanceStmt = connection.prepareStatement(
               selectActivityInstanceCmd.toString());
         selectActivityInstanceLogsStmt = connection.prepareStatement(
               selectActivityInstanceLogsCmd.toString());
         insertStmt = connection.prepareStatement(
               insertCmd.toString());

         int rowCounter = 0;

         ResultSet pendingRows = null;
         try
         {
            boolean firstSelect = true;
            long activityInstanceOid = 0;
            ResultSet maxActivityInstanceOidResultSet = null;
            try
            {
               maxActivityInstanceOidResultSet = selectLastActivityInstanceStmt.executeQuery();
               if (maxActivityInstanceOidResultSet.next())
               {
                  activityInstanceOid = maxActivityInstanceOidResultSet.getLong(1);
                  firstSelect = false;
               }
            }
            finally
            {
               QueryUtils.closeResultSet(maxActivityInstanceOidResultSet);
            }
            
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;
               
               if (firstSelect)
               {
                  pendingRows = firstSelectActivityInstanceStmt.executeQuery();
                  firstSelect = false;
               }
               else
               {
                  selectActivityInstanceStmt.setLong(1, activityInstanceOid);
                  pendingRows = selectActivityInstanceStmt.executeQuery();
               }
               while (pendingRows.next())
               {
                  eating = true;
                  try
                  {
                     activityInstanceOid = pendingRows.getLong(1);
                     long processInstanceOid = pendingRows.getLong(2);

                     ResultSet rs = null;
                     try
                     {
                        selectActivityInstanceLogsStmt.setLong(1, activityInstanceOid);
                        rs = selectActivityInstanceLogsStmt.executeQuery();
                        
                        boolean firstRecord = true;
                        long from = 0;
                        int state = 0;
                        long user = 0;
                        while (rs.next())
                        {
                           long stamp = rs.getLong(1);
                           int type = aiLogStateMapping[rs.getInt(2)];
                           long workflowUser = rs.getLong(3);
                           if (type == ActivityInstanceState.CREATED)
                           {
                              continue;
                           }
                           if (firstRecord)
                           {
                              firstRecord = false;
                           }
                           else
                           {
                              long untilTS = stamp;
                              insertStmt.setLong(1, processInstanceOid);
                              insertStmt.setLong(2, activityInstanceOid);
                              insertStmt.setInt(3, state);
                              insertStmt.setLong(4, from);
                              insertStmt.setLong(5, untilTS);
                              insertStmt.setLong(6, 0); // domain is for the moment 0
                              PerformerUtils.EncodedPerformer encodedPerformer = 
                                 PerformerUtils.encodeParticipant(null);
                              insertStmt.setInt(7, encodedPerformer.kind.getValue());
                              insertStmt.setLong(8, encodedPerformer.oid);
                              PerformerUtils.EncodedPerformer encodedOnBehalfOf =
                                 PerformerUtils.encodeParticipant(null);
                              insertStmt.setInt(9, encodedOnBehalfOf.kind.getValue());
                              insertStmt.setLong(10, encodedOnBehalfOf.oid);
                              insertStmt.setLong(11, user);
                              insertStmt.addBatch();
                           }
                           from = stamp;
                           state = type;
                           user = workflowUser;
                        }
                     }
                     finally
                     {
                        QueryUtils.closeResultSet(rs);
                     }

                     ++rowCounter;
                  }
                  catch (SQLException e)
                  {
                     warn(e.getMessage(), null);
                  }

                  if (batchSize <= (rowCounter - rowCounterBackup))
                  {
                     break;
                  }
               }

               if ( !eating)
               {
                  break;
               }

               insertStmt.executeBatch();
               connection.commit();

               info(MessageFormat.format("Committing inserts on table {0}"
                     + " after {1} activity instances.", new Object[] {
                     ActivityInstanceHistoryBean.TABLE_NAME, new Integer(rowCounter)}));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(pendingRows);
         }

         info(ActivityInstanceHistoryBean.TABLE_NAME + " table upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectLastActivityInstanceStmt);
         QueryUtils.closeStatement(firstSelectActivityInstanceStmt);
         QueryUtils.closeStatement(selectActivityInstanceStmt);
         QueryUtils.closeStatement(selectActivityInstanceLogsStmt);
         QueryUtils.closeStatement(insertStmt);
      }
   }

   private boolean containsTable(String tableName)
   {
      boolean result = false;
      DDLManager ddlManager = new DDLManager(item.getDbDescriptor());
      try 
      {
         result = ddlManager.containsTable(DatabaseHelper.getSchemaName(),
               tableName, item.getConnection());
      } 
      catch (SQLException e) 
      {
         error("", e);
      }
      
      return result;
   }
}
