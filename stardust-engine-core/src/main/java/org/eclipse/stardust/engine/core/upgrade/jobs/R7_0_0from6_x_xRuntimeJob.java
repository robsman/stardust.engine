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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataLoader;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.spi.ISchemaTypeProvider;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataLoader;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.UpdateColumnInfo;

/**
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class R7_0_0from6_x_xRuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private int batchSize = 500;

   private static final Logger trace = LogManager
         .getLogger(R7_0_0from6_x_xRuntimeJob.class);

   private static final String WORK_ITEM_FIELD__ACTIVIYINSTANCE = "activityInstance";

   private static final String ACTIVITY_INSTANCE_TABLE_NAME = "activity_instance";

   private static final String ACTIVITY_INSTANCE_FIELD_OID = "oid";

   private static final String ACTIVITY_INSTANCE_FIELD_CRITICALITY = "criticality";

   private static final String ACTIVITY_INSTANCE_FIELD_PROPERTIES = "propertiesAvailable";

   private static final String ACTIVITY_INSTANCE_FIELD_PI = "processInstance";

   private static final String ACTIVITY_INSTANCE_IDX9 = "activity_inst_idx9";

   private static final String ACTIVITY_INSTANCE_PROP_TABLE_NAME = "act_inst_property";

   private static final String ACTIVITY_INSTANCE_PROP_FIELD_OBJECT_OID = "objectOID";

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

   private static final String PROPERTY_TABLE_NAME = "property";
   private static final String PROPERTY_FIELD_NAME = "name";

   private static final Version VERSION = Version.createFixedVersion(7, 0, 0);

   private static final String AI_LCK_TABLE_NAME = "activity_instance_lck";
   private static final String P_LCK_TABLE_NAME = "partition_lck";
   private static final String P_LCK_FIELD__OID = "oid";

   private RuntimeUpgradeTaskExecutor upgradeTaskExecutor;


   R7_0_0from6_x_xRuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE, DBMSKey.MSSQL8});
      String bs = Parameters.instance().getString(RuntimeUpgrader.UPGRADE_BATCH_SIZE);
      if (bs != null)
      {
         batchSize = Integer.parseInt(bs);
      }

      initUpgradeTasks();
   }

   public Version getVersion()
   {
      return VERSION;
   }

   private void initUpgradeTasks()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R7_0_0from6_x_xRuntimeJob", Parameters.instance()
            .getBoolean(RuntimeUpgrader.UPGRADE_DATA, false));
      initUpgradeSchemaTasks();
      initMigrateDataTasks();
      initFinalizeSchemaTasks();
   }

   private void initUpgradeSchemaTasks()
   {
      final R7_0_0from6_x_xRuntimeJob runtimeJob = this;

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            // Alter ActivityInstance Table
            DatabaseHelper.alterTable(item, new AlterTableInfo(ACTIVITY_INSTANCE_TABLE_NAME)
            {
               private final FieldInfo CRITICALITY = new FieldInfo(
                     ACTIVITY_INSTANCE_FIELD_CRITICALITY, Double.TYPE);

               private final FieldInfo PROPERTIES_AVAILABLE = new FieldInfo(
                     ACTIVITY_INSTANCE_FIELD_PROPERTIES, Integer.TYPE);

               private final FieldInfo PROCESS_INSTANCE = new FieldInfo(
                     ACTIVITY_INSTANCE_FIELD_PI, Long.TYPE, 0, false);

               @Override
               public FieldInfo[] getAddedFields()
               {
                  return new FieldInfo[] {CRITICALITY, PROPERTIES_AVAILABLE};
               }

               @Override
               public IndexInfo[] getAddedIndexes()
               {
                  return new IndexInfo[] {//
                        new IndexInfo(ACTIVITY_INSTANCE_IDX9, false, new FieldInfo[] {
                              CRITICALITY,
                              PROCESS_INSTANCE }) };
               }

            }, runtimeJob);
         }
      });

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         private final FieldInfo CRITICALITY = new FieldInfo(WORK_ITEM_FIELD_CRITICALITY,
               Double.TYPE);

         @Override
         public void execute()
         {
            // Alter WorkItem Table
            DatabaseHelper.alterTable(item, new AlterTableInfo(WORK_ITEM_TABLE_NAME)
            {
               @Override
               public FieldInfo[] getAddedFields()
               {
                  return new FieldInfo[] {CRITICALITY};
               }

            }, runtimeJob);
         }
      });

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
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

            }, runtimeJob);
         }
      });

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
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

            }, runtimeJob);
         }
      });

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
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
               }, runtimeJob);
            }
         }
      });
   }

   private void initMigrateDataTasks()
   {
      // Add default values for new AI columns
      upgradeTaskExecutor.addMigrateDataTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            //create info about the field to update
            final FieldInfo criticalityField = new FieldInfo(
                  ACTIVITY_INSTANCE_FIELD_CRITICALITY, Float.class);
            final FieldInfo propertyAvailableField = new FieldInfo(
                  ACTIVITY_INSTANCE_FIELD_PROPERTIES, Long.class);

            final UpdateColumnInfo updateCriticalityInfo = new UpdateColumnInfo(
                  criticalityField, -1);
            final UpdateColumnInfo updatePropertyAvailableFieldInfo = new UpdateColumnInfo(
                  propertyAvailableField, 0);

            final FieldInfo aiOidColumn = new FieldInfo(ACTIVITY_INSTANCE_FIELD_OID,
                  Long.class, true);

            try
            {
               DatabaseHelper.setColumnValuesInBatch(item, ACTIVITY_INSTANCE_TABLE_NAME,
                     aiOidColumn, batchSize, updateCriticalityInfo,
                     updatePropertyAvailableFieldInfo);
            }
            catch (SQLException e)
            {
               reportExeption(e, "Could not update table: "
                     + ACTIVITY_INSTANCE_TABLE_NAME + ".");
            }
         }
      });

      // Add default values for new WI columns
      upgradeTaskExecutor.addMigrateDataTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            final FieldInfo CRITICALITY = new FieldInfo(WORK_ITEM_FIELD_CRITICALITY,
                  Double.TYPE);
            final FieldInfo workItemPk = new FieldInfo(WORK_ITEM_FIELD__ACTIVIYINSTANCE,
                  Long.TYPE, true);
            final UpdateColumnInfo updateInfo = new UpdateColumnInfo(CRITICALITY, -1);

            try
            {
               DatabaseHelper.setColumnValuesInBatch(item, WORK_ITEM_TABLE_NAME,
                     workItemPk, batchSize, updateInfo);
            }
            catch (SQLException e)
            {
               reportExeption(e, "Could not update new column " + WORK_ITEM_TABLE_NAME
                     + "." + WORK_ITEM_FIELD_CRITICALITY + " to -1.");
            }
         }
      });

      upgradeTaskExecutor.addMigrateDataTask(new UpgradeTask()
      {
         private static final String oldDefinition = "ag.carnot.workflow.runtime.setup_definition";
         private static final String newDefinition = "org.eclipse.stardust.engine.core.runtime.setup_definition";

         @Override
         public void execute()
         {
            // update data cluster setup key
            // @formatter:off
            StringBuffer dataClusterUpdateStatement = new StringBuffer();
            dataClusterUpdateStatement
                  .append(UPDATE).append(DatabaseHelper.getQualifiedName(PROPERTY_TABLE_NAME))
                  .append(SET).append(PROPERTY_FIELD_NAME).append(EQUALS)
                  .append(QUOTE).append(newDefinition).append(QUOTE)
                  .append(WHERE).append(PROPERTY_FIELD_NAME).append(EQUALS)
                  .append(QUOTE).append(oldDefinition).append(QUOTE);
            // @formatter:on

            try
            {
               DatabaseHelper.executeUpdate(item, dataClusterUpdateStatement.toString());
            }
            catch (SQLException e)
            {
               reportExeption(e, "could not update data cluster setup");
            }
         }
      });

      upgradeTaskExecutor.addMigrateDataTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            try
            {
               initActivityInstanceProperties();
            }
            catch (SQLException e)
            {
               reportExeption(e,
                     "Failed init activity instance properties (nested exception).");
            }
         }
      });

      upgradeTaskExecutor.addMigrateDataTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            try
            {
               upgradeDataTypes();
            }
            catch (SQLException e)
            {
               reportExeption(e, "Failed upgrade data types (nested exception).");
            }
         }
      });

      upgradeTaskExecutor.addMigrateDataTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            try
            {
               insertDefaultLinkTypes();
            }
            catch (SQLException sqle)
            {
               reportExeption(sqle,
                     "Failed migrating runtime item tables (nested exception).");
            }
         }
      });
   }

   private void initFinalizeSchemaTasks()
   {
      // no tasks for schema finalization
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeUpgradeSchemaTasks();
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
      info("The new columns 'criticality', 'propertiesAvailable' and index 'activity_inst_idx9' will be created in table 'activity_instance'.");
      info("A new column 'criticality' will be created in table 'workitem'.");
      info("A new table 'procinst_link' with the columns "
            + "'processInstance', 'linkedProcessInstance', 'linkType', 'createTime', 'creatingUser' and 'linkingComment' will be created.");
      info("A new table 'link_type' with the columns 'oid', 'id', 'description', 'partition' and index 'link_type_idx1' will be created.");
      info("Datacluster setup key will be upgraded to 'org.eclipse.stardust.engine.core.runtime.setup_definition' in column 'name' in table 'property'.");
      if (!item.isArchiveAuditTrail() && containsTable(AI_LCK_TABLE_NAME))
      {
         info("A new table 'partition_lck' with column 'oid' and index 'partition_lck_idx' will be created.");
      }
   }

   @Override
   protected void printMigrateDataInfo()
   {
      info("Initializes the field 'propertiesAvailable' in table 'activity_instance'.");
      info("Missing XPaths which are needed to store the revisionComment will be created for Structured Datatypes.");
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
      info("Default link types will be added.");
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeMigrateDataTasks();
      ((org.eclipse.stardust.engine.core.persistence.jdbc.Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL)).flush();
   }

   private void upgradeDataTypes() throws SQLException
   {
      info("Upgrading Datatypes...");
      PreparedStatement selectRowsStmt = null;
      try
      {
         String partitionTableName = DatabaseHelper
               .getQualifiedName(AUDIT_TRAIL_PARTITION_TABLE_NAME);

         StringBuffer selectCmd = new StringBuffer() //
               .append(SELECT).append(AUDIT_TRAIL_PARTITION_FIELD_ID) //
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
                     .getString(AUDIT_TRAIL_PARTITION_FIELD_ID));
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
      Utils.initCarnotEngine(partition, getRtJobEngineProperties());

      Map<Long, AuditTrailDataBean> dataDefRecords = loadModelElementDefinitions(1,
            AuditTrailDataBean.class, AuditTrailDataBean.FR__MODEL);

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
         Utils.flushSession();
         
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
      final String aiAlias = "ai";
      final String aipAlias = "aip";

      Connection connection = item.getConnection();

      // @formatter:off
      StringBuffer selectCmd = new StringBuffer()
            .append(SELECT_DISTINCT).append("ai.").append(ACTIVITY_INSTANCE_FIELD_OID)
            .append(FROM).append(DatabaseHelper.getQualifiedName(ACTIVITY_INSTANCE_TABLE_NAME, aiAlias))
            .append(INNER_JOIN).append(DatabaseHelper.getQualifiedName(ACTIVITY_INSTANCE_PROP_TABLE_NAME, aipAlias))
            .append(ON).append(aipAlias).append(DOT).append(ACTIVITY_INSTANCE_PROP_FIELD_OBJECT_OID)
            .append(EQUALS).append(aiAlias).append(DOT).append(ACTIVITY_INSTANCE_FIELD_OID);

      StringBuffer updateCmd = new StringBuffer()
            .append(UPDATE).append(DatabaseHelper.getQualifiedName(ACTIVITY_INSTANCE_TABLE_NAME))
            .append(SET).append(ACTIVITY_INSTANCE_FIELD_PROPERTIES)
            .append(EQUALS).append("1")
            .append(WHERE).append(ACTIVITY_INSTANCE_FIELD_OID)
            .append(EQUAL_PLACEHOLDER);
      // @formatter:on

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
         insertCmd.append(INSERT_INTO).append(tableName).append(" (");
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_OID).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION).append(") ");
         insertCmd.append("VALUES (").append(nextOid).append(",?,?,?)");
      }
      else if (dbDescriptor.supportsIdentityColumns())
      {
         insertCmd.append(INSERT_INTO).append(tableName).append(" (");
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_ID).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_DESCRIPTION).append(',');
         insertCmd.append(PROCESS_INSTANCE_LINK_TYPE_FIELD_PARTITION).append(") ");
         insertCmd.append("VALUES (?,?,?)");
      }
      else
      {
         insertCmd.append(INSERT_INTO).append(tableName).append(" (");
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
         // @formatter:off
         StringBuffer update = new StringBuffer();
         update.append(UPDATE).append(DatabaseHelper.getQualifiedName("sequence_helper"))
               .append(SET).append("value").append(EQUAL_PLACEHOLDER)
               .append(WHERE).append("name")
               .append(EQUALS).append(QUOTE).append("link_type_seq").append(QUOTE);
         // @formatter:on

         updateStatement = connection.prepareStatement(update.toString());
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
         // @formatter:off
         StringBuffer selectCmd = new StringBuffer()
               .append(SELECT).append(AUDIT_TRAIL_PARTITION_FIELD_OID)
               .append(COMMA).append(AUDIT_TRAIL_PARTITION_FIELD_ID)
               .append(FROM).append(DatabaseHelper.getQualifiedName(AUDIT_TRAIL_PARTITION_TABLE_NAME));
         // @formatter:on

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
      upgradeTaskExecutor.executeFinalizeSchemaTasks();
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

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}