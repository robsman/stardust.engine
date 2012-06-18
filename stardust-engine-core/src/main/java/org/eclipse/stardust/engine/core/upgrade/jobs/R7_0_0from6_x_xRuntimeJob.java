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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.DataTypeDetails;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.runtime.IModelPersistor;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.cli.sysconsole.Utils;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceProperty;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ModelPersistorBean;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeModelLoader;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeOidRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeOidUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataLoader;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.spi.ISchemaTypeProvider;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataLoader;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.CreateTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeItem;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;

/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class R7_0_0from6_x_xRuntimeJob extends DbmsAwareRuntimeUpgradeJob
{

   private static final Logger trace = LogManager
         .getLogger(R7_0_0from6_x_xRuntimeJob.class);

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

   // Constants for SQL building
   private static final String SELECT = "SELECT ";

   private static final String FROM = " FROM ";

   private static final Version VERSION = new Version(7, 0, 0);

   private static final String AI_LCK_TABLE_NAME = "activity_instance_lck";   
   private static final String P_LCK_TABLE_NAME = "partition_lck";      
   private static final String P_LCK_FIELD__OID = "oid";
   
   
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
      // Alter ActivityInstance Table
      DatabaseHelper.alterTable(item, new AlterTableInfo(ACTIVITY_INSTANCE_TABLE_NAME)
      {
         private final FieldInfo CRITICALITY = new FieldInfo(
               ACTIVITY_INSTANCE_FIELD_CRITICALITY, Double.TYPE);

         private final FieldInfo PROPERTIES_AVAILABLE = new FieldInfo(
               ACTIVITY_INSTANCE_FIELD_PROPERTIES, Integer.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {CRITICALITY, PROPERTIES_AVAILABLE};
         }

      }, this);

      try
      {
         setColumnDefaultValue(item, ACTIVITY_INSTANCE_TABLE_NAME,
               ACTIVITY_INSTANCE_FIELD_CRITICALITY, -1);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + ACTIVITY_INSTANCE_TABLE_NAME
               + "." + ACTIVITY_INSTANCE_FIELD_CRITICALITY + " to -1.");
      }

      try
      {
         setColumnDefaultValue(item, ACTIVITY_INSTANCE_TABLE_NAME,
               ACTIVITY_INSTANCE_FIELD_PROPERTIES, 0);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + ACTIVITY_INSTANCE_TABLE_NAME
               + "." + ACTIVITY_INSTANCE_FIELD_PROPERTIES + " to 0.");
      }

      // Alter WorkItem Table
      DatabaseHelper.alterTable(item, new AlterTableInfo(WORK_ITEM_TABLE_NAME)
      {
         private final FieldInfo CRITICALITY = new FieldInfo(WORK_ITEM_FIELD_CRITICALITY,
               Double.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {CRITICALITY};
         }

      }, this);

      try
      {
         setColumnDefaultValue(item, WORK_ITEM_TABLE_NAME, WORK_ITEM_FIELD_CRITICALITY,
               -1);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + WORK_ITEM_TABLE_NAME + "."
               + WORK_ITEM_FIELD_CRITICALITY + " to -1.");
      }

      // Create ProcessInstanceLink Table
      DatabaseHelper.createTable(item, new CreateTableInfo(
            PROCESS_INSTANCE_LINK_TABLE_NAME)
      {
         private final FieldInfo processInstance = new FieldInfo(
               PROCESS_INSTANCE_LINK_FIELD_PROCESS_INSTANCE, Long.TYPE, 0, true);

         private final FieldInfo linkedProcessInstance = new FieldInfo(
               PROCESS_INSTANCE_LINK_FIELD_LINKED_PROCESS_INSTANCE, Long.TYPE, 0, true);

         private final FieldInfo linkType = new FieldInfo(
               PROCESS_INSTANCE_LINK_FIELD_LINK_TYPE, Long.TYPE, 0, true);

         private final FieldInfo createTime = new FieldInfo(
               PROCESS_INSTANCE_LINK_FIELD_CREATE_TIME, Long.TYPE, 0, false);

         private final FieldInfo creatingUser = new FieldInfo(
               PROCESS_INSTANCE_LINK_FIELD_CREATING_USER, Long.TYPE, 0, false);

         private final FieldInfo linkingComment = new FieldInfo(
               PROCESS_INSTANCE_LINK_FIELD_LINKING_COMMENT, String.class, 255, false);

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {
                  processInstance, linkedProcessInstance, linkType, createTime,
                  creatingUser, linkingComment};
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
      DatabaseHelper.createTable(item, new CreateTableInfo(
            PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME)
      {
         private final FieldInfo oid = new FieldInfo(
               PROCESS_INSTANCE_LINK_TYPE_FIELD_OID, Long.TYPE, 0, true);

         private final FieldInfo id = new FieldInfo(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID,
               String.class, 50, false);

         private final FieldInfo description = new FieldInfo(
               PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION, String.class, 255, false);

         private final FieldInfo partition = new FieldInfo(
               PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION, Long.TYPE, 0, false);

         private final IndexInfo IDX1 = new IndexInfo(PROCESS_INSTANCE_LINK_TYPE_IDX1,
               true, new FieldInfo[] {oid});

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

      // update datacluster setup key
      StringBuffer dataClusterUpdateStatement = new StringBuffer();
      dataClusterUpdateStatement.append("UPDATE property");
      dataClusterUpdateStatement.append(" SET name=");
      dataClusterUpdateStatement.append("'");
      dataClusterUpdateStatement
            .append("org.eclipse.stardust.engine.core.runtime.setup_definition");
      dataClusterUpdateStatement.append("'");
      dataClusterUpdateStatement.append(" where name=");
      dataClusterUpdateStatement.append("'");
      dataClusterUpdateStatement.append("ag.carnot.workflow.runtime.setup_definition");
      dataClusterUpdateStatement.append("'");

      try
      {
         DatabaseHelper.executeUpdate(item, dataClusterUpdateStatement.toString());
      }
      catch (SQLException e)
      {
         reportExeption(e, "could not update data cluster setup");
      }
      
      
      // Lock table will only be created if any other lock table already exists, e.g. the one for AIBean
      if (!item.isArchiveAuditTrail() && containsTable(AI_LCK_TABLE_NAME))      
      {
         DatabaseHelper.createTable(item, new CreateTableInfo(P_LCK_TABLE_NAME)
         {
            private static final String INDEX_NAME = "partition_lck_idx";
   
            private final FieldInfo OID = new FieldInfo(P_LCK_FIELD__OID, Long.TYPE, 0, true);
            
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
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      try
      {
         initActivityInstanceProperties();
         upgradeDataTypes();
      }
      catch (SQLException e)
      {
      }
   }

   private void upgradeDataTypes() throws SQLException
   {
      info("Upgrading Datatypes...");
      PreparedStatement selectRowsStmt = null;
      try
      {
         String partitionTableName = DatabaseHelper
               .getQualifiedName(AuditTrailPartitionBean.TABLE_NAME);

         StringBuffer selectCmd = new StringBuffer() //
               .append(SELECT).append(AuditTrailPartitionBean.FIELD__ID) //
               .append(FROM).append(partitionTableName);

         Connection connection = item.getConnection();

         selectRowsStmt = connection.prepareStatement(selectCmd.toString());

         ResultSet pendingRows = null;
         try
         {
            pendingRows = selectRowsStmt.executeQuery();
            while (pendingRows.next())
            {
               upgradeDataTypesByPartition(pendingRows
                     .getString(AuditTrailPartitionBean.FIELD__ID));
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
      info("Upgrading Datatypes...done.");
   }

   //The following section is copied from RuntimeModelLoader. This is for fixing CRNT-24871.
   //To make sure that the data deploy mechanism is suitable for a 6.0 / 7.0 upgrade step this
   //mechanism had to be extracted from other code sections to here. This should prevent code changes
   //in the original model loader to break this upgrade step.
   
   private void upgradeDataTypesByPartition(String partition)
   {
      Map props = CollectionUtils.newHashMap();
      props.put("jdbc/" + SessionProperties.DS_NAME_AUDIT_TRAIL
            + SessionProperties.DS_DATA_SOURCE_SUFFIX,
            new ConnectionWrapper(item.getConnection()));
      props.put(Constants.FORCE_IMMEDIATE_INSERT_ON_SESSION, Boolean.TRUE);
      
      Utils.initCarnotEngine(partition, props);
      
      Map<Long, AuditTrailDataBean> dataDefRecords = loadModelElementDefinitions(1,
            AuditTrailDataBean.class, AuditTrailDataBean.FR__MODEL);

      Session driver = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);


      Short partitionOid = (Short) Parameters.instance().get(
            SecurityProperties.CURRENT_PARTITION_OID);
      


      HashMap registries = new HashMap(); // <partition>,<runtime registry>
      RuntimeOidRegistry rtOidRegistry = null;
      info("Partition with OID: " + partitionOid);
      for (Iterator modelIter = ModelPersistorBean.findAll(partitionOid.shortValue()); modelIter
            .hasNext();)
      {
         IModelPersistor currentModel = (IModelPersistor) modelIter.next();
         IModel model = ModelManagerFactory.getCurrent().findModel(
               currentModel.getModelOID());
         rtOidRegistry = getRuntimeOidRegistry(registries, partitionOid);
         for (Iterator allData = model.getAllData(); allData.hasNext();)
         {
            IData data = (IData) allData.next();
            if (model != data.getModel())
            {
               continue;
            }

            AuditTrailDataBean auditTrailData = null;
            long rtOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.DATA,
                  RuntimeOidUtils.getFqId(data));
            if (0 != rtOid)
            {
               auditTrailData = (AuditTrailDataBean) dataDefRecords.get(Long
                     .valueOf(rtOid));
            }

            DataTypeDetails dataTypeDetails = (DataTypeDetails) DetailsFactory.create(
                  data.getType(), IDataType.class, DataTypeDetails.class);
            DataLoader dataTypeLoader = (DataLoader) Reflect
                  .createInstance(dataTypeDetails.getDataTypeLoaderClass());
            if (dataTypeLoader instanceof StructuredDataLoader)
            {
               deployData(rtOidRegistry, data, rtOid, model.getModelOID(), model);
            }
         }
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

      PreparedStatement selectRowsStmt = connection
            .prepareStatement(selectCmd.toString());
      PreparedStatement updateRowsStmt = connection
            .prepareStatement(updateCmd.toString());

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

   private void setColumnDefaultValue(RuntimeItem item, String tableName,
         String columnName, Object defaultValue) throws SQLException
   {
      tableName = DatabaseHelper.getQualifiedName(tableName);

      StringBuffer buffer = new StringBuffer(500);
      buffer.append("UPDATE ").append(tableName);
      buffer.append(" SET ").append(columnName).append(" = ").append(defaultValue);

      // Execute DML instead of DDL, but this DML is part of the DDL so it has to be
      // handled the same way.
      item.executeDdlStatement(buffer.toString(), false);
   }

   private void insertDefaultLinkTypes() throws SQLException
   {
      Connection connection = item.getConnection();
      String tableName = DatabaseHelper
            .getQualifiedName(PROCESS_INSTANCE_LINK_TYPE_TABLE_NAME);

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
         String update = "UPDATE " + DatabaseHelper.getQualifiedName("sequence_helper")
               + " SET value=?" + " WHERE name='link_type_seq'";
         updateStatement = connection.prepareStatement(update);
         updateStatement.setLong(1,
               PredefinedProcessInstanceLinkTypes.values().length + 1);
      }

      Iterator<Pair<Long, String>> partitions = fetchListOfPartitionInfo().iterator();
      long oid = 0;
      while (partitions.hasNext())
      {
         Pair<Long, String> partitionInfo = partitions.next();
         long partitionOid = partitionInfo.getFirst();

         trace.debug("Adding default link types to partition '"
               + partitionInfo.getSecond() + "'...");
         insertStatement.setLong(3, partitionOid);

         for (PredefinedProcessInstanceLinkTypes type : PredefinedProcessInstanceLinkTypes
               .values())
         {
            insertStatement.setString(1, type.getId());
            insertStatement.setString(2, type.getDescription());
            if (hasSequenceHelper)
            {
               oid++;
               insertStatement.setLong(4, oid);
            }
            insertStatement.execute();
            trace.debug("Added '" + type + "' link type.");
         }
      }
      insertStatement.close();
      if (hasSequenceHelper)
      {
         updateStatement.setLong(1, oid + 1);
         updateStatement.execute();
         updateStatement.close();
      }
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

   private static class ConnectionWrapper implements DataSource
   {
      Connection connection;

      private ConnectionWrapper(Connection connection)
      {
         this.connection = connection;
      }

      public Connection getConnection() throws SQLException
      {
         return connection;
      }

      public Connection getConnection(String username, String password)
            throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      public int getLoginTimeout() throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      public PrintWriter getLogWriter() throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      public void setLoginTimeout(int seconds) throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      public void setLogWriter(PrintWriter out) throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean isWrapperFor(Class< ? > iface) throws SQLException
      {
         // TODO Auto-generated method stub
         return false;
      }

      @Override
      public <T> T unwrap(Class<T> iface) throws SQLException
      {
         // TODO Auto-generated method stub
         return null;
      }
   }

   private static <T extends IdentifiablePersistent> Map<Long, T> loadModelElementDefinitions(
         int modelOid, Class<T> type, FieldRef frModel)
   {
      Map<Long, T> result = CollectionUtils.newHashMap();

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         for (Iterator i = session.getIterator(type, //
               QueryExtension.where(Predicates.isEqual(frModel, modelOid))); i.hasNext();)
         {
            IdentifiablePersistent element = (IdentifiablePersistent) i.next();
            result.put(Long.valueOf(element.getOID()), (T) element);
         }
      }

      return result;
   }

   private RuntimeOidRegistry getRuntimeOidRegistry(HashMap registries, Short partition)
   {
      RuntimeOidRegistry registry = (RuntimeOidRegistry) registries.get(partition);
      if (registry == null)
      {
         registry = new RuntimeOidRegistry(partition.shortValue());
         RuntimeModelLoader runtimeModelLoader = new RuntimeModelLoader(
               partition.shortValue());
         runtimeModelLoader.loadRuntimeOidRegistry(registry);

         registries.put(partition, registry);
      }
      return registry;
   }

   
   //The following section is copied from StructuredDataLoader. This is for fixing CRNT-24871.
   //To make sure that the data deploy mechanism is suitable for a 6.0 / 7.0 upgrade step this
   //mechanism had to be extracted from other code sections to here.

   public void deployData(IRuntimeOidRegistry rtOidRegistry, IData data, long dataRtOid,
         long modelOID, RootElement model)
   {
      try
      {
         // create xpath mapping
         Set /* <TypedXPath> */xPaths = this.findAllXPaths(data, model);

         if (null != xPaths)
         {
            Map<Long, StructuredDataBean> structDataDefRecords = loadXPathDefinitions(
                  modelOID, dataRtOid);

            Map /* <Long,TypedXPath> */allXPaths = CollectionUtils.newMap();
            for (Iterator i = xPaths.iterator(); i.hasNext();)
            {
               TypedXPath xPath = (TypedXPath) i.next();

               // dataId and xPath must uniquely map to an xPathRtOid
               long xPathRtOid = rtOidRegistry.getRuntimeOid(
                     IRuntimeOidRegistry.STRUCTURED_DATA_XPATH,
                     RuntimeOidUtils.getFqId(data, xPath.getXPath()));

               StructuredDataBean xPathBean = null;
               if (xPathRtOid == 0)
               {
                  // there is no StructuredDataBean this xPathRtOid and modelOID, new one
                  // must be created
                  xPathRtOid = rtOidRegistry.registerNewRuntimeOid(
                        IRuntimeOidRegistry.STRUCTURED_DATA_XPATH,
                        RuntimeOidUtils.getFqId(data, xPath.getXPath()));

                  xPathBean = new StructuredDataBean(xPathRtOid, dataRtOid, modelOID,
                        xPath.getXPath());
               }
               else
               {
                  // try to find StructuredDataBean for this rtOid and modelOID
                  xPathBean = (StructuredDataBean) structDataDefRecords.get(Long
                        .valueOf(xPathRtOid));
                  if (xPathBean == null)
                  {
                     // if, for some reason, the StructuredDataBean for exising xPathRtOid
                     // was not found
                     // for the current modelOID, create one
                     xPathBean = new StructuredDataBean(xPathRtOid, dataRtOid, modelOID,
                           xPath.getXPath());
                  }
               }

               allXPaths.put(new Long(xPathBean.getOID()), xPath);
            }
         }
      }
      catch (Exception e)
      {
         throw new InternalException(
               "Could not create XPath mapping using schema for data '" + data.getId()
                     + "'", e);
      }
   }

   private Set<TypedXPath> findAllXPaths(IData data, RootElement model) throws Exception
   {
      Set<TypedXPath> result = null;

      String declaredTypeId = (String) data
            .getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
      if (null != declaredTypeId)
      {
         return StructuredTypeRtUtils.getAllXPaths((IModel) model, declaredTypeId);
      }
      else
      {
         IReference ref = data.getExternalReference();
         if (ref != null)
         {
            return StructuredTypeRtUtils.getAllXPaths(ref);
         }
         for (Iterator i = ExtensionProviderUtils.getExtensionProviders(
               ISchemaTypeProvider.Factory.class).iterator(); i.hasNext();)
         {
            ISchemaTypeProvider.Factory stpFactory = (ISchemaTypeProvider.Factory) i
                  .next();

            ISchemaTypeProvider provider = stpFactory.getSchemaTypeProvider(data
                  .getType().getId());
            if (null != provider)
            {
               result = provider.getSchemaType(data);

               if (null != result)
               {
                  return result;
               }
            }
         }
         throw new InternalException("Could not find predefined XPaths for data type '"
               + data.getType()
               + "'. Check if schema providers are configured correctly.");
      }
   }

   private static Map<Long, StructuredDataBean> loadXPathDefinitions(long modelOid,
         long dataRtOid)
   {
      Map<Long, StructuredDataBean> result = CollectionUtils.newHashMap();

      for (Iterator i = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .getIterator(
                  StructuredDataBean.class, //
                  QueryExtension.where(Predicates.andTerm(
                        //
                        Predicates.isEqual(StructuredDataBean.FR__DATA, dataRtOid),
                        Predicates.isEqual(StructuredDataBean.FR__MODEL, modelOid)))); i
            .hasNext();)
      {
         StructuredDataBean sdd = (StructuredDataBean) i.next();
         result.put(new Long(sdd.getOID()), sdd);
      }

      return result;
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