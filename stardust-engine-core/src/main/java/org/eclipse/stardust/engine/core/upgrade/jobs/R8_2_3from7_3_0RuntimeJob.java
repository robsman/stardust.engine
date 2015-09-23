/*******************************************************************************
 * Copyright (c) 2011 - 2015 SunGard CSA LLC
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgradeTaskExecutor;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeTask;

public class R8_2_3from7_3_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager
         .getLogger(R8_2_3from7_3_0RuntimeJob.class);

   private RuntimeUpgradeTaskExecutor upgradeTaskExecutor;

   private static final String AUDIT_TRAIL_PARTITION_TABLE_NAME = "partition";

   private static final String AUDIT_TRAIL_PARTITION_FIELD_OID = "oid";

   private static final String AUDIT_TRAIL_PARTITION_FIELD_ID = "id";

   private static final String PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME = "link_type";

   private static final String PROCESS_INSTANCE_LINK_TYPE_PK_SEQUENCE = "link_type_seq";

   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_OID = "oid";

   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_ID = "id";

   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION = "description";

   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION = "partition";

   private static final Version VERSION = Version.createFixedVersion(8, 2, 3);

   R8_2_3from7_3_0RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE, DBMSKey.MSSQL8,
            DBMSKey.MYSQL_SEQ});
      initUpgradeTasks();
   }

   private void initUpgradeTasks()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R8_2_3from7_3_0RuntimeJob",
            Parameters.instance().getBoolean(RuntimeUpgrader.UPGRADE_DATA, false));

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
      });
   }

   private void insertRelatedLinkType() throws SQLException
   {
      Connection connection = item.getConnection();
      String tableName = DatabaseHelper
            .getQualifiedName(PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME);

      if (!linkTypeExists(PredefinedProcessInstanceLinkTypes.RELATED, connection))
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

            PredefinedProcessInstanceLinkTypes type = PredefinedProcessInstanceLinkTypes.RELATED;
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
      // TODO Auto-generated method stub

   }

   @Override
   protected void printMigrateDataInfo()
   {
      info("Default link type RELATED will be added.");
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public boolean isMandatory()
   {
      return false;
   }

   @Override
   public Version getVersion()
   {
      return VERSION;
   }

}
