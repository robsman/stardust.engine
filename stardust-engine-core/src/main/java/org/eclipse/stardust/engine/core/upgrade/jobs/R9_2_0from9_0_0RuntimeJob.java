/*******************************************************************************
 * Copyright (c) 2011 - 2015 SunGard CSA LLC
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.upgrade.framework.*;

public class R9_2_0from9_0_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager
         .getLogger(R9_2_0from9_0_0RuntimeJob.class);

   private RuntimeUpgradeTaskExecutor upgradeTaskExecutor;

   private static final String AI_LCK_TABLE_NAME = "activity_instance_lck";

   private static final String AUDIT_TRAIL_PARTITION_TABLE_NAME = "partition";

   private static final String AUDIT_TRAIL_PARTITION_FIELD_OID = "oid";

   private static final String AUDIT_TRAIL_PARTITION_FIELD_ID = "id";

   private static final String PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME = "link_type";

   private static final String PROCESS_INSTANCE_LINK_TYPE_PK_SEQUENCE = "link_type_seq";

   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_OID = "oid";

   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_ID = "id";

   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION = "description";

   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION = "partition";

   static final Version VERSION = Version.createFixedVersion(9, 2, 0);

   private static final String DATA_VALUE_HISTORY_TABLE_NAME = "data_value_history";

   private static final String DATA_VALUE_HISTORY_FIELD_OID = "oid";

   private static final String DATA_VALUE_HISTORY_FIELD_MODEL = "model";

   private static final String DATA_VALUE_HISTORY_FIELD_DATA = "data";

   private static final String DATA_VALUE_HISTORY_FIELD_PROCESS_INSTANCE = "processInstance";

   private static final String DATA_VALUE_HISTORY_FIELD_TYPE_KEY = "type_key";

   private static final String DATA_VALUE_HISTORY_FIELD_STRING_VALUE = "string_value";

   private static final String DATA_VALUE_HISTORY_FIELD_NUMBER_VALUE = "number_value";

   private static final String DATA_VALUE_HISTORY_FIELD_DOUBLE_VALUE = "double_value";

   private static final String DATA_VALUE_HISTORY_FIELD_MOD_TIMESTAMP = "mod_timestamp";

   private static final String DATA_VALUE_HISTORY_FIELD_MOD_USER = "mod_user";

   private static final String DATA_VALUE_HISTORY_FIELD_MOD_ACTIVITY_INSTANCE = "mod_activity_instance";

   private static final String DATA_VALUE_HISTORY_SEQ = "data_value_history_seq";

   private static final String DATA_VALUE_HISTORY_LCK_TABLE_NAME = "data_value_history_lck";

   private static final String DATA_VALUE_HISTORY_LCK_IDX = "data_value_history_lck_idx";

   private static final String selectBusinessDateStatement =
         "SELECT dv.oid,dv.number_value "
         + "FROM data_value dv "
         + "INNER JOIN data dd "
           + "ON dv.data = dd.oid AND dv.model = dd.model "
         + "WHERE dd.id = 'BUSINESS_DATE'";

   private static final String updateBusinessDateStatement =
         "UPDATE data_value dv "
         + "SET dv.number_value = ? "
         + "WHERE dv.oid = ?";

   private R9_2_0from9_0_0RuntimeJob runtimeJob;

   private int batchSize;

   @SuppressWarnings("deprecation")
   R9_2_0from9_0_0RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE, DBMSKey.MSSQL8,
            DBMSKey.MYSQL_SEQ});
      runtimeJob = this;
      initUpgradeTasks();
      String bs = Parameters.instance().getString(RuntimeUpgrader.UPGRADE_BATCH_SIZE);
      if (bs != null)
      {
         batchSize = Integer.parseInt(bs);
      }
   }

   private void initUpgradeTasks()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R9_2_0from9_0_0RuntimeJob",
            Parameters.instance().getBoolean(RuntimeUpgrader.UPGRADE_DATA, true));

      upgradeTaskExecutor.addMigrateDataTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            try
            {
               insertRelatedLinkType();
            }
            catch (SQLException sqle)
            {
               reportExeption(sqle,
                     "Failed migrating runtime item tables (nested exception).");
            }
         }

         @Override
         public void printInfo()
         {
         }
      });

      upgradeTaskExecutor.addMigrateDataTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            try
            {
               migrateBusinessDates();
            }
            catch (SQLException sqle)
            {
               reportExeption(sqle,
                     "Failed migrating runtime item tables (nested exception).");
            }
         }

         @Override
         public void printInfo()
         {
         }
      });

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            insertDataValueHistoryTable();
         }

         @Override
         public void printInfo()
         {
            info("A new table 'data_value_history' with the columns 'oid', 'model', "
                  + "'data', 'string_value', 'number_value', 'double_value', "
                  + "'mod_timestamp', 'mod_user', 'mod_activity_instance', "
                  + "'processInstance' and 'type_key' will be created.");
         }

      });

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            // Lock table will only be created if any other lock table already exists,
            // e.g. the
            // one for AIBean
            if (!item.isArchiveAuditTrail() && containsTable(AI_LCK_TABLE_NAME))
            {
               insertDataValueHistoryLckTable();
            }
         }

         @Override
         public void printInfo()
         {// Lock table will only be created if any other lock table already exists, e.g.
          // the
          // one for AIBean
            if (!item.isArchiveAuditTrail() && containsTable(AI_LCK_TABLE_NAME))
            {
               info("A new table 'data_value_history_lck' with column 'oid' and "
                     + "index 'data_value_history_lck_idx'  will be created.");
            }
         }

      });
   }

   private void migrateBusinessDates() throws SQLException
   {
      try
      {
         PreparedStatement updateStmnt = item.getConnection().prepareStatement(updateBusinessDateStatement);

         ResultSet resultSet = null;
         try
         {
            resultSet = DatabaseHelper.executeQuery(item, selectBusinessDateStatement);

            Calendar cal = Calendar.getInstance();
            int batchCounter = 0;
            while (resultSet.next())
            {
               long oid = resultSet.getLong(1);
               long value = resultSet.getLong(2);

               if (!resultSet.wasNull())
               {
                  cal.setTimeInMillis(value);
                  long newValue = DateUtils.businessDateToTimestamp(cal);

                  updateStmnt.setLong(1, newValue);
                  updateStmnt.setLong(2, oid);

                  updateStmnt.addBatch();
                  ++batchCounter;

                  if (batchCounter >= batchSize)
                  {
                     batchCounter = 0;
                     updateStmnt.executeBatch();
                  }

               }
            }

            // As it might be expensive to check for resultSet.isLast()
            // the batch is being executed last time once the loop has been left
            if (batchCounter != 0)
            {
               updateStmnt.executeBatch();
            }
         }
         finally
         {
            resultSet.close();
            updateStmnt.close();
         }
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update long value.");
      }
   }

   private void insertRelatedLinkType() throws SQLException
   {
      Connection connection = item.getConnection();
      String tableName = DatabaseHelper
            .getQualifiedName(PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME);

      if (!linkTypeExists(PredefinedProcessInstanceLinkTypes.INSERT, connection))
      {
         // Populate ProcessInstanceLinkType table
         StringBuffer insertCmd = new StringBuffer();
         DBDescriptor dbDescriptor = item.getDbDescriptor();
         if (dbDescriptor.supportsSequences())
         {
            String nextOid = dbDescriptor.getNextValForSeqString(
                  DatabaseHelper.getSchemaName(), PROCESS_INSTANCE_LINK_TYPE_PK_SEQUENCE);
            insertCmd.append("INSERT INTO ").append(tableName).append(" (");
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_OID).append(',');
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID).append(',');
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION).append(',');
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION).append(") ");
            insertCmd.append("VALUES (").append(nextOid).append(",?,?,?)");
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            insertCmd.append("INSERT INTO ").append(tableName).append(" (");
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID).append(',');
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION).append(',');
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION).append(") ");
            insertCmd.append("VALUES (?,?,?)");
         }
         else
         {
            insertCmd.append("INSERT INTO ").append(tableName).append(" (");
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID).append(',');
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION).append(',');
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION).append(',');
            insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_OID).append(") ");
            insertCmd.append("VALUES (?,?,?,?)");
         }
         PreparedStatement insertStatement = connection.prepareStatement(insertCmd
               .toString());

         PreparedStatement updateStatement = null;
         boolean hasSequenceHelper = !dbDescriptor.supportsSequences()
               && !dbDescriptor.supportsIdentityColumns();
         if (hasSequenceHelper)
         {
            String update = "UPDATE "
                  + DatabaseHelper.getQualifiedName("sequence_helper") + " SET value=?"
                  + " WHERE name='link_type_seq'";
            updateStatement = connection.prepareStatement(update);
            updateStatement.setLong(1,
                  PredefinedProcessInstanceLinkTypes.values().length + 1);
         }

         Iterator<Pair<Long, String>> partitions = fetchListOfPartitionInfo().iterator();
         long relatedOid = 5;
         while (partitions.hasNext())
         {
            Pair<Long, String> partitionInfo = partitions.next();
            long partitionOid = partitionInfo.getFirst();

            trace.debug("Adding default link type to partition '"
                  + partitionInfo.getSecond() + "'...");
            insertStatement.setLong(3, partitionOid);

            PredefinedProcessInstanceLinkTypes type = PredefinedProcessInstanceLinkTypes.INSERT;
            insertStatement.setString(1, type.getId());
            insertStatement.setString(2, type.getDescription());
            if (hasSequenceHelper)
            {
               insertStatement.setLong(4, relatedOid);
            }
            insertStatement.execute();
            trace.debug("Added '" + type + "' link type.");
         }
         insertStatement.close();
         if (hasSequenceHelper)
         {
            updateStatement.setLong(1, relatedOid + 1);
            updateStatement.execute();
            updateStatement.close();
         }
      }
   }

   private boolean linkTypeExists(PredefinedProcessInstanceLinkTypes predefinedLinkType,
         Connection connection) throws SQLException
   {
      StringBuffer selectCmd = new StringBuffer();
      selectCmd.append("SELECT oid FROM ").append(PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME);
      selectCmd.append(" WHERE ").append(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID)
            .append(" = ?");

      PreparedStatement stmt = connection.prepareStatement(selectCmd.toString());
      stmt.setString(1, predefinedLinkType.getId());

      ResultSet resultSet = stmt.executeQuery();

      long oid = 0;
      if (resultSet.next())
      {
         oid = resultSet.getLong(PROCESS_INSTANCE_LINK_TYPE_FIELD_OID);
      }
      resultSet.close();
      stmt.close();

      return oid > 0 ? true : false;
   }

   private Set<Pair<Long, String>> fetchListOfPartitionInfo() throws SQLException
   {
      Set<Pair<Long, String>> partitionInfo = CollectionUtils.newSet();
      PreparedStatement selectRowsStmt = null;
      try
      {
         StringBuffer selectCmd = new StringBuffer()
               //
               .append("SELECT ").append(AUDIT_TRAIL_PARTITION_FIELD_OID).append(", ")
               .append(AUDIT_TRAIL_PARTITION_FIELD_ID) //
               .append(" FROM ").append(AUDIT_TRAIL_PARTITION_TABLE_NAME);

         Connection connection = item.getConnection();

         selectRowsStmt = connection.prepareStatement(selectCmd.toString());

         ResultSet pendingRows = null;
         try
         {
            pendingRows = selectRowsStmt.executeQuery();
            while (pendingRows.next())
            {
               Long oid = pendingRows.getLong(AUDIT_TRAIL_PARTITION_FIELD_OID);
               String id = pendingRows.getString(AUDIT_TRAIL_PARTITION_FIELD_ID);
               partitionInfo.add(new Pair(oid, id));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(pendingRows);
         }
      }
      finally
      {
         QueryUtils.closeStatement(selectRowsStmt);
      }

      return partitionInfo;
   }

   private void insertDataValueHistoryTable()
   {
      DatabaseHelper.createTable(item, new CreateTableInfo(
           DATA_VALUE_HISTORY_TABLE_NAME)
      {
         private final FieldInfo oid = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_OID, Long.TYPE, 0, true);

         private final FieldInfo model = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_MODEL, Long.TYPE, 0, false);

         private final FieldInfo data = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_DATA, Long.TYPE, 0, false);

         private final FieldInfo string_value = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_STRING_VALUE, String.class, 255, false);

         private final FieldInfo number_value = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_NUMBER_VALUE, Long.TYPE, 0, false);

         private final FieldInfo double_value = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_DOUBLE_VALUE, Double.TYPE, 0, false);

         private final FieldInfo mod_timestamp = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_MOD_TIMESTAMP, Long.TYPE, 0, false);

         private final FieldInfo mod_user = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_MOD_USER, Long.TYPE, 0, false);

         private final FieldInfo mod_activity_instance = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_MOD_ACTIVITY_INSTANCE, Long.TYPE, 0, false);

         private final FieldInfo type_key = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_TYPE_KEY, Integer.TYPE, 0, false);

         private final FieldInfo processInstance = new FieldInfo(
               DATA_VALUE_HISTORY_FIELD_PROCESS_INSTANCE, Long.TYPE, 0, false);

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {
                  oid, model, data, string_value, number_value, double_value,
                  mod_timestamp, mod_user, mod_activity_instance, type_key,
                  processInstance};
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return null;
         }

         @Override
         public String getSequenceName()
         {
            return DATA_VALUE_HISTORY_SEQ;
         }

      }, runtimeJob);

   }

   protected void insertDataValueHistoryLckTable()
   {
      DatabaseHelper.createTable(item, new CreateTableInfo(DATA_VALUE_HISTORY_LCK_TABLE_NAME)
      {
         private final FieldInfo oid = new FieldInfo(DATA_VALUE_HISTORY_FIELD_OID,
               Long.TYPE, 0, true);

         private final IndexInfo idx = new IndexInfo(DATA_VALUE_HISTORY_LCK_IDX, true,
               new FieldInfo[] {oid});

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {oid};
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {idx};
         }

         @Override
         public String getSequenceName()
         {
            return null;
         }

      }, runtimeJob);
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
   }

   @Override
   protected void migrateData(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeMigrateDataTasks();
      ((org.eclipse.stardust.engine.core.persistence.jdbc.Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL)).flush();
   }

   @Override
   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeFinalizeSchemaTasks();
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
     upgradeTaskExecutor.printUpgradeSchemaInfo();
   }

   @Override
   protected void printMigrateDataInfo()
   {
      info("Default link type INSERT will be added.");
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Version getVersion()
   {
      return VERSION;
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
