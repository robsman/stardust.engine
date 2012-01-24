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
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceProperty;
import org.eclipse.stardust.engine.core.upgrade.framework.*;


/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class R7_0_0from6_x_xRuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R7_0_0from6_x_xRuntimeJob.class);

   private static final String ACTIVITY_INSTANCE_TABLE_NAME = "activity_instance";
   private static final String ACTIVITY_INSTANCE_FIELD_CRITICALITY = "criticality";
   private static final String ACTIVITY_INSTANCE_FIELD_PROPERTIES = "propertiesAvailable";

   private static final String AUDIT_TRAIL_PARTITION_TABLE_NAME = "partition";
   private static final String AUDIT_TRAIL_PARTITION_FIELD_OID = "oid";
   private static final String AUDIT_TRAIL_PARTITION_FIELD_ID = "id";

   private static final String PROCESS_INSTANCE_LINK_TABLE_NAME = "procinst_link";
   private static final String PROCESS_INSTANCE_LINK_FIELD_PROCESS_INSTANCE = "processInstance";
   private static final String PROCESS_INSTANCE_LINK_FIELD_LINKED_PROCESS_INSTANCE = "linkedProcessInstance";
   private static final String PROCESS_INSTANCE_LINK_FIELD_LINK_TYPE = "linkType";
   private static final String PROCESS_INSTANCE_LINK_FIELD_CREATE_TIME = "createTime";
   private static final String PROCESS_INSTANCE_LINK_FIELD_CREATING_USER = "creatingUser";
   private static final String PROCESS_INSTANCE_LINK_FIELD_LINKING_COMMENT = "linkingComment";

   private static final String PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME = "link_type";
   private static final String PROCESS_INSTANCE_LINK_TYPE_PK_SEQUENCE = "link_type_seq";
   private static final String PROCESS_INSTANCE_LINK_TYPE_IDX1 = "link_type_idx1";
   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_OID = "oid";
   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_ID = "id";
   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION = "description";
   private static final String PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION = "partition";

   private static final String WORK_ITEM_TABLE_NAME = "workitem";
   private static final String WORK_ITEM_FIELD_CRITICALITY = "criticality";

   private static final Version VERSION = new Version(7, 0, 0);

   R7_0_0from6_x_xRuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE, DBMSKey.MSSQL8});
   }

   public Version getVersion()
   {
      return VERSION;
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      //Alter ActivityInstance Table
      DatabaseHelper.alterTable(item, new AlterTableInfo(ACTIVITY_INSTANCE_TABLE_NAME)
      {
         private final FieldInfo CRITICALITY = new FieldInfo(ACTIVITY_INSTANCE_FIELD_CRITICALITY, Double.TYPE);
         private final FieldInfo PROPERTIES_AVAILABLE = new FieldInfo(ACTIVITY_INSTANCE_FIELD_PROPERTIES, Integer.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {CRITICALITY, PROPERTIES_AVAILABLE};
         }

      }, this);

      try
      {
         setColumnDefaultValue(item, ACTIVITY_INSTANCE_TABLE_NAME, ACTIVITY_INSTANCE_FIELD_CRITICALITY, -1);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + ACTIVITY_INSTANCE_TABLE_NAME + "."
               + ACTIVITY_INSTANCE_FIELD_CRITICALITY + " to -1.");
      }

      try
      {
         setColumnDefaultValue(item, ACTIVITY_INSTANCE_TABLE_NAME, ACTIVITY_INSTANCE_FIELD_PROPERTIES, 0);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + ACTIVITY_INSTANCE_TABLE_NAME + "."
               + ACTIVITY_INSTANCE_FIELD_PROPERTIES + " to 0.");
      }
      
      //Alter WorkItem Table
      DatabaseHelper.alterTable(item, new AlterTableInfo(WORK_ITEM_TABLE_NAME)
      {
         private final FieldInfo CRITICALITY = new FieldInfo(WORK_ITEM_FIELD_CRITICALITY, Double.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {CRITICALITY};
         }

      }, this);

      try
      {
         setColumnDefaultValue(item, WORK_ITEM_TABLE_NAME, WORK_ITEM_FIELD_CRITICALITY, -1);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + WORK_ITEM_TABLE_NAME + "."
               + WORK_ITEM_FIELD_CRITICALITY + " to -1.");
      }

      // Create ProcessInstanceLink Table
      DatabaseHelper.createTable(item, new CreateTableInfo(PROCESS_INSTANCE_LINK_TABLE_NAME)
      {
         private final FieldInfo processInstance = new FieldInfo(PROCESS_INSTANCE_LINK_FIELD_PROCESS_INSTANCE, Long.TYPE, 0, true);
         private final FieldInfo linkedProcessInstance = new FieldInfo(PROCESS_INSTANCE_LINK_FIELD_LINKED_PROCESS_INSTANCE, Long.TYPE, 0, true);
         private final FieldInfo linkType = new FieldInfo(PROCESS_INSTANCE_LINK_FIELD_LINK_TYPE, Long.TYPE, 0, true);

         private final FieldInfo createTime = new FieldInfo(PROCESS_INSTANCE_LINK_FIELD_CREATE_TIME, Long.TYPE, 0, false);
         private final FieldInfo creatingUser = new FieldInfo(PROCESS_INSTANCE_LINK_FIELD_CREATING_USER, Long.TYPE, 0, false);
         private final FieldInfo linkingComment = new FieldInfo(PROCESS_INSTANCE_LINK_FIELD_LINKING_COMMENT, String.class, 255, false);

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {processInstance, linkedProcessInstance, linkType, createTime, creatingUser, linkingComment};
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return null;
         }

         @Override
         public String getSequenceName()
         {
            return null;
         }

      }, this);

      // Create ProcessInstanceLinkType Table
      DatabaseHelper.createTable(item, new CreateTableInfo(PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME)
      {
         private final FieldInfo oid = new FieldInfo(PROCESS_INSTANCE_LINK_TYPE_FIELD_OID, Long.TYPE, 0, true);
         
         private final FieldInfo id = new FieldInfo(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID, String.class, 50, false);
         private final FieldInfo description = new FieldInfo(PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION, String.class, 255, false);
         private final FieldInfo partition = new FieldInfo(PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION, Long.TYPE, 0, false);

         private final IndexInfo IDX1 = new IndexInfo(PROCESS_INSTANCE_LINK_TYPE_IDX1, true, new FieldInfo[] {oid});

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {oid, id, description, partition};
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {IDX1};
         }

         @Override
         public String getSequenceName()
         {
            return PROCESS_INSTANCE_LINK_TYPE_PK_SEQUENCE;
         }

      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {      
      try
      {
         initActivityInstanceProperties();
      }
      catch (SQLException e)
      {
      }      
   }
   
   private void initActivityInstanceProperties() throws SQLException
   {
      Connection connection = item.getConnection();
   
      StringBuffer selectCmd = new StringBuffer() //
         .append("SELECT DISTINCT ai.").append(ActivityInstanceBean.FIELD__OID) //
         .append(" FROM ").append(ActivityInstanceBean.TABLE_NAME) //
         .append(" ai ") //
         .append(" INNER JOIN ").append(ActivityInstanceProperty.TABLE_NAME) //
         .append(" aip ON aip.").append(ActivityInstanceProperty.FIELD__OBJECT_OID) //
         .append(" = ai.").append(ActivityInstanceBean.FIELD__OID); //

      StringBuffer updateCmd = new StringBuffer() //
         .append("UPDATE ").append(ActivityInstanceBean.TABLE_NAME) //
         .append(" SET ").append(ActivityInstanceBean.FIELD__PROPERTIES_AVAILABLE) //
         .append(" = 1 ") //
         .append(" WHERE ").append(ActivityInstanceBean.FIELD__OID) //
         .append(" = ?"); //
      
      PreparedStatement selectRowsStmt = connection.prepareStatement(selectCmd.toString());
      PreparedStatement updateRowsStmt = connection.prepareStatement(updateCmd.toString());
      
      ResultSet pendingRows = null;
      try
      {
         pendingRows = selectRowsStmt.executeQuery();
         while (pendingRows.next())
         {
            Long oid = pendingRows.getLong(1);

            updateRowsStmt.setLong(1, oid);
            updateRowsStmt.addBatch();
         }
         
         updateRowsStmt.executeBatch();         
         connection.commit();         
      }
      finally
      {
         QueryUtils.closeResultSet(pendingRows);
         QueryUtils.closeStatement(selectRowsStmt);
         QueryUtils.closeStatement(updateRowsStmt);
      }
   }

   private void setColumnDefaultValue(RuntimeItem item, String tableName, String columnName, Object defaultValue) throws SQLException
   {
      tableName = DatabaseHelper.getQualifiedName(tableName);

      StringBuffer buffer = new StringBuffer(500);
      buffer.append("UPDATE ").append(tableName);
      buffer.append(" SET ").append(columnName).append(" = ").append(defaultValue);
      
      // Execute DML instead of DDL, but this DML is part of the DDL so it has to be handled the same way. 
      item.executeDdlStatement(buffer.toString(), false);
   }

   private void insertDefaultLinkTypes() throws SQLException
   {
      Connection connection = item.getConnection();
      String tableName = DatabaseHelper.getQualifiedName(PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME);
      
      //Populate ProcessInstanceLinkType table
      StringBuffer insertCmd = new StringBuffer();
      if (item.getDbDescriptor().supportsSequences())
      {
         String nextOid = item.getDbDescriptor().getNextValForSeqString(null, PROCESS_INSTANCE_LINK_TYPE_PK_SEQUENCE);
         insertCmd.append("INSERT INTO ").append(tableName).append(" (");
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_OID).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION).append(") ");
         insertCmd.append("VALUES (").append(nextOid).append(",?,?,?)");
      }
      else
      {
         insertCmd.append("INSERT INTO ").append(tableName).append(" (");
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION).append(") ");
         insertCmd.append("VALUES (?,?,?)");
      }
      PreparedStatement insertStatement = connection.prepareStatement(insertCmd.toString());
      
      Iterator<Pair<Long, String>> partitions = fetchListOfPartitionInfo().iterator();
      while (partitions.hasNext())
      {
         Pair<Long, String> partitionInfo = partitions.next();
         long partitionOid = partitionInfo.getFirst();

         trace.debug("Adding default link types to partition '" + partitionInfo.getSecond() + "'...");
         
         insertStatement.setString(1, "switch");
         insertStatement.setString(2, "Peer Process Instance");
         insertStatement.setLong(3, partitionOid);
         insertStatement.execute();
         trace.debug("Added 'switch' link type");

         insertStatement.setString(1, "join");
         insertStatement.setString(2, "Join Process Instance");
         insertStatement.setLong(3, partitionOid);
         insertStatement.execute();
         trace.debug("Added 'join' link type");
      }
      insertStatement.close();
   }

   private Set<Pair<Long, String>> fetchListOfPartitionInfo() throws SQLException
   {
      Set<Pair<Long, String>> partitionInfo = CollectionUtils.newSet();
      PreparedStatement selectRowsStmt = null;
      try
      {
         StringBuffer selectCmd = new StringBuffer() //
               .append("SELECT ").append(AUDIT_TRAIL_PARTITION_FIELD_OID).append(", ").append(AUDIT_TRAIL_PARTITION_FIELD_ID) //
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

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      try
      {
         insertDefaultLinkTypes();
      }
      catch (SQLException sqle)
      {
         reportExeption(sqle, "Failed migrating runtime item tables (nested exception).");
      }
   }

   private void reportExeption(SQLException sqle, String message)
   {
      SQLException ne = sqle;
      do
      {
         trace.error(message, ne);
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
