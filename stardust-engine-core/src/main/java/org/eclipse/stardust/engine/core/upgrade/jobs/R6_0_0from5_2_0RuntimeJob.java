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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.model.beans.NullConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.preferences.XmlPreferenceWriter;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ModelPersistorBean;
import org.eclipse.stardust.engine.core.runtime.beans.PropertyPersistor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.Permissions;
import org.eclipse.stardust.engine.core.upgrade.framework.*;


/**
 * @author roland.stamm
 */
public class R6_0_0from5_2_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R6_0_0from5_2_0RuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(6, 0, 0);

   private static final String RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION = "ag.carnot.workflow.runtime.setup_definition";

   private static final String PI_TABLE_NAME = "process_instance";
   private static final String PI_FIELD__DEPLOYMENT = "deployment";
   private static final String PI_FIELD__MODEL = "model";

   private static final String P_TABLE_NAME = "preferences";

   private static final String P_FIELD__OWNER_ID = "ownerId";

   private static final String P_FIELD__OWNER_TYPE = "ownerType";

   private static final String P_FIELD__MODULE_ID = "moduleId";

   private static final String P_FIELD__PREFERENCES_ID = "preferencesId";

   private static final String P_FIELD__PARTITION = "partition";

   private static final String P_FIELD__STRING_VALUE = "stringValue";

   private static final String P_IDX1 = "preferences_idx1";

   private static final String PREFERENCES_MODULE_ID_PERMISSIONS = "permissions";

   private static final String PREFERENCES_PARTITION_SCOPE_TYPE = "PARTITION";

   private static final String MS_TABLE_NAME = "message_store";
   private static final String MS_SEQ_NAME = "message_store_seq";

   // Model Table
   private static final String M_TABLE_NAME = "model";
   private static final String M_FIELD__DEPLOYMENT_STAMP = "deploymentStamp";
   private static final String M_FIELD__OID = "oid";

   // Model Ref Table

   private static final String MODEL_REF_TABLE_NAME = "model_ref";

   private static final String MODEL_REF_FIELD__CODE = "code";

   private static final String MODEL_REF_FIELD__MODEL_OID = "modelOid";

   private static final String MODEL_REF_FIELD__ID = "id";

   private static final String MODEL_REF_FIELD__REF_OID = "refOid";

   private static final String MODEL_REF_FIELD__DEPLOYMENT = "deployment";

   private static final String MODEL_REF_IDX1 = "model_ref_idx1";

   private static final String MODEL_REF_IDX2 = "model_ref_idx2";

   //Model Dep Table

   private static final String MODEL_DEP_TABLE_NAME = "model_dep";

   private static final String MD_SEQ_NAME = "model_dep_seq";

   private static final String MODEL_DEP_FIELD__OID = "oid";

   private static final String MODEL_DEP_FIELD__DEPLOYER = "deployer";

   private static final String MODEL_DEP_FIELD__DEPLOYMENT_TIME = "deploymentTime";

   private static final String MODEL_DEP_FIELD__VALID_FROM = "validFrom";

   private static final String MODEL_DEP_FIELD__DEPLOYMENT_COMMENT = "deploymentComment";

   private static final String MODEL_DEP_IDX1 = "model_dep_idx1";

   private static final String MODEL_DEP_IDX2 = "model_dep_idx2";

   private static final String MODEL_DEP_IDX3 = "model_dep_idx3";

   private static final String MD_LOCK_TABLE_NAME = "model_dep_lck";

   private static final String MD_LOCK_INDEX_NAME = "model_dep_lck_idx";

   //User Participant Table

   private static final String UP_TABLE_NAME = "user_participant";

   private static final String UP_FIELD__MODEL = "model";

   private static final String UP_FIELD__DEPARTMENT = "department";

   private static final String UP_FIELD__PARTICIPANT = "participant";

   private static final String UP_INDEX_IDX2 = "user_particip_idx2";

   // partition table
   private static final String AUDIT_TRAIL_PARTITION_TABLE_NAME = "partition";
   private static final String AUDIT_TRAIL_PARTITION_FIELD_OID = "oid";
   private static final String AUDIT_TRAIL_PARTITION_FIELD_ID = "id";


   R6_0_0from5_2_0RuntimeJob()
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
      DatabaseHelper.createTable(item, new CreateTableInfo(P_TABLE_NAME)
      {
         private final FieldInfo OWNER_ID = new FieldInfo(P_FIELD__OWNER_ID, Long.TYPE,
               0, true);

         private final FieldInfo OWNER_TYPE = new FieldInfo(P_FIELD__OWNER_TYPE,
               String.class, 32, true);

         private final FieldInfo MODULE_ID = new FieldInfo(P_FIELD__MODULE_ID,
               String.class, 255, true);

         private final FieldInfo PREFERENCES_ID = new FieldInfo(P_FIELD__PREFERENCES_ID,
               String.class, 255, true);

         private final FieldInfo PARTITION = new FieldInfo(P_FIELD__PARTITION, Long.TYPE,
               0, false);

         private final FieldInfo STRING_VALUE = new FieldInfo(P_FIELD__STRING_VALUE,
               String.class, Integer.MAX_VALUE);

         private final IndexInfo IDX1 = new IndexInfo(P_IDX1, true, new FieldInfo[] {
               OWNER_ID, OWNER_TYPE, MODULE_ID, PREFERENCES_ID, PARTITION});

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {
                  OWNER_ID, OWNER_TYPE, MODULE_ID, PREFERENCES_ID, PARTITION,
                  STRING_VALUE};
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {IDX1};
         }

         @Override
         public String getSequenceName()
         {
            return null;
         }

      }, this);

      // Persistent type MessagePersistor was never used and will be dropped now.
      DatabaseHelper.dropTable(item, new DropTableInfo(MS_TABLE_NAME, //
            item.isArchiveAuditTrail() ? null : MS_SEQ_NAME), this);

      // Create Model_Ref Table
      DatabaseHelper.createTable(item, new CreateTableInfo(MODEL_REF_TABLE_NAME)
      {
         private final FieldInfo CODE = new FieldInfo(MODEL_REF_FIELD__CODE, Integer.TYPE,
               0, true);

         private final FieldInfo MODEL_OID = new FieldInfo(MODEL_REF_FIELD__MODEL_OID, Long.TYPE,
               0, true);

         private final FieldInfo ID = new FieldInfo(MODEL_REF_FIELD__ID,
               String.class, 255, true);

         private final FieldInfo REF_OID = new FieldInfo(MODEL_REF_FIELD__REF_OID, Long.TYPE,
               0, true);

         private final FieldInfo DEPLOYMENT = new FieldInfo(MODEL_REF_FIELD__DEPLOYMENT, Long.TYPE,
               0, true);

         private final IndexInfo IDX1 = new IndexInfo(MODEL_REF_IDX1, true, new FieldInfo[] {
               CODE, MODEL_OID, ID, DEPLOYMENT});
         private final IndexInfo IDX2 = new IndexInfo(MODEL_REF_IDX2, true, new FieldInfo[] {
               CODE, MODEL_OID, REF_OID, DEPLOYMENT});

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {
                  CODE, MODEL_OID, ID, REF_OID, DEPLOYMENT,
            };
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {IDX1, IDX2};
         }

         @Override
         public String getSequenceName()
         {
            return null;
         }

      }, this);

      // Create Model_Dep Table
      DatabaseHelper.createTable(item, new CreateTableInfo(MODEL_DEP_TABLE_NAME)
      {
         private final FieldInfo OID = new FieldInfo(MODEL_DEP_FIELD__OID, Long.TYPE,
               0, true);

         private final FieldInfo DEPLOYER = new FieldInfo(MODEL_DEP_FIELD__DEPLOYER,
               Long.TYPE, 0, false);

         private final FieldInfo DEPLOYMENT_TIME = new FieldInfo(MODEL_DEP_FIELD__DEPLOYMENT_TIME, Long.TYPE,
               0, false);

         private final FieldInfo VALID_FROM = new FieldInfo(MODEL_DEP_FIELD__VALID_FROM, Long.TYPE,
               0, false);

         private final FieldInfo DEPLOYMENT_COMMENT = new FieldInfo(MODEL_DEP_FIELD__DEPLOYMENT_COMMENT,
               String.class, 255, false);

         private final IndexInfo IDX1 = new IndexInfo(MODEL_DEP_IDX1, true, new FieldInfo[] {OID});
         private final IndexInfo IDX2 = new IndexInfo(MODEL_DEP_IDX2, false, new FieldInfo[] {DEPLOYMENT_TIME});
         private final IndexInfo IDX3 = new IndexInfo(MODEL_DEP_IDX3, false, new FieldInfo[] {DEPLOYER});

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {
                  OID, DEPLOYER, DEPLOYMENT_TIME, VALID_FROM, DEPLOYMENT_COMMENT,
            };
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {IDX1, IDX2, IDX3};
         }

         @Override
         public String getSequenceName()
         {
            return MD_SEQ_NAME;
         }

      }, this);

      // Create MODEL_DEP_LCK table

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (session.getDBDescriptor().getUseLockTablesDefault())
      {
         DatabaseHelper.createTable(item, new CreateTableInfo(
               MD_LOCK_TABLE_NAME)
         {
            private final FieldInfo OID = new FieldInfo(MODEL_DEP_FIELD__OID, Long.TYPE,
                  0, true);

            private final IndexInfo IDX1 = new IndexInfo(
                  MD_LOCK_INDEX_NAME, true, new FieldInfo[] {OID});

            @Override
            public FieldInfo[] getFields()
            {
               return new FieldInfo[] {OID};
            }

            @Override
            public IndexInfo[] getIndexes()
            {
               return new IndexInfo[] {IDX1};
            }

            @Override
            public String getSequenceName()
            {
               return null;
            }

         }, this);
      }

      // Alter Process Instance Table

      DatabaseHelper.alterTable(item, new AlterTableInfo(PI_TABLE_NAME)
      {
         private final FieldInfo DEPLOYMENT = new FieldInfo(PI_FIELD__DEPLOYMENT,
               Long.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { DEPLOYMENT };
         }

      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      try
      {
         migrateDeployedModels(recover);
         consolidateGrants();
         migratePermissions(recover);
         migrateDataClusterDefinition();
      }
      catch (SQLException sqle)
      {
         reportExeption(sqle, "Failed migrating runtime item tables (nested exception).");
      }
   }

   private void consolidateGrants() throws SQLException
   {
      // Keep only entries from the active models.
      List<Long> activeModels = CollectionUtils.newList();
      for (Pair<Long, String> info : fetchListOfPartitionInfo())
      {
         String partitionId = info.getSecond();
         Utils.initCarnotEngine(partitionId, getRtJobEngineProperties());
         IModel model = ModelManagerFactory.getCurrent().findActiveModel();
         if (model == null)
         {
            model = ModelManagerFactory.getCurrent().findLastDeployedModel();
         }
         if (model != null)
         {
            activeModels.add((long) model.getModelOID());
         }
         Utils.flushSession();
      }

      // No need to do anything if no models are deployed.
      if (!activeModels.isEmpty())
      {
         // delete grants for all but the active models
         DatabaseHelper.executeUpdate(item, getDeleteGrantsSql(activeModels));

         // as columns cannot be dropped just set value to null
         String tableName = DatabaseHelper.getQualifiedName(UP_TABLE_NAME);
         StringBuffer buffer = new StringBuffer(500);
         buffer.append(UPDATE).append(tableName);
         buffer.append(SET).append(UP_FIELD__MODEL).append(EQUALS).append(NULL);

         DatabaseHelper.executeUpdate(item, buffer.toString());
      }
   }

   private static String getDeleteGrantsSql(List<Long> activeModels)
   {
      StringBuilder sql = new StringBuilder()
         .append(DELETE_FROM).append(DatabaseHelper.getQualifiedName(UP_TABLE_NAME))
         .append(WHERE).append(UP_FIELD__MODEL).append(" NOT IN (");
      for (int i = 0, l = activeModels.size(); i < l; i++)
      {
         if (i > 0)
         {
            sql.append(',');
         }
         sql.append(activeModels.get(i));
      }
      sql.append(')');
      return sql.toString();
   }

   private void migrateDeployedModels(boolean recover) throws SQLException
   {
      Connection connection = item.getConnection();

      //Populate MODEL_DEP
      StringBuffer selectCmd = new StringBuffer();
      selectCmd.append(SELECT);
      selectCmd.append(M_FIELD__DEPLOYMENT_STAMP).append(COMMA);
      selectCmd.append(MODEL_DEP_FIELD__VALID_FROM).append(COMMA);
      selectCmd.append(MODEL_DEP_FIELD__DEPLOYMENT_COMMENT);
      selectCmd.append(FROM).append(DatabaseHelper.getQualifiedName(M_TABLE_NAME));

      ResultSet rs = DatabaseHelper.executeQuery(item, selectCmd.toString());

      if (recover)
      {
         StringBuffer deleteCmd = new StringBuffer();
         deleteCmd.append(DELETE_FROM).append(DatabaseHelper.getQualifiedName(MODEL_DEP_TABLE_NAME));
         DatabaseHelper.executeUpdate(item, deleteCmd.toString());
      }

      StringBuffer insertCmd = new StringBuffer();
      if (item.getDbDescriptor().supportsSequences())
      {
         String nextOid = item.getDbDescriptor()
               .getNextValForSeqString(DatabaseHelper.getSchemaName(), MD_SEQ_NAME);
         insertCmd.append(INSERT_INTO).append(DatabaseHelper.getQualifiedName(MODEL_DEP_TABLE_NAME)).append(" (");
         insertCmd.append(MODEL_DEP_FIELD__OID).append(COMMA);
         insertCmd.append(MODEL_DEP_FIELD__DEPLOYER).append(COMMA);
         insertCmd.append(MODEL_DEP_FIELD__DEPLOYMENT_TIME).append(COMMA);
         insertCmd.append(MODEL_DEP_FIELD__VALID_FROM).append(COMMA);
         insertCmd.append(MODEL_DEP_FIELD__DEPLOYMENT_COMMENT).append(") ");
         insertCmd.append("VALUES (").append(nextOid).append(",?,?,?,?)");
      }
      else
      {
         insertCmd.append(INSERT_INTO).append(DatabaseHelper.getQualifiedName(MODEL_DEP_TABLE_NAME)).append(" (");
         insertCmd.append(MODEL_DEP_FIELD__DEPLOYER).append(COMMA);
         insertCmd.append(MODEL_DEP_FIELD__DEPLOYMENT_TIME).append(COMMA);
         insertCmd.append(MODEL_DEP_FIELD__VALID_FROM).append(COMMA);
         insertCmd.append(MODEL_DEP_FIELD__DEPLOYMENT_COMMENT).append(") ");
         insertCmd.append("VALUES (?,?,?,?)");
      }

      PreparedStatement insertStatement
         = DatabaseHelper.prepareLoggingStatement(connection, insertCmd.toString());
      while (rs.next())
      {
         insertStatement.setLong(1, 0);
         insertStatement.setLong(2, rs.getLong(1));
         insertStatement.setLong(3, rs.getLong(2));
         insertStatement.setString(4, rs.getString(3));
         insertStatement.execute();
      }
      insertStatement.close();
      rs.close();

      //Populate MODEL_REF
      selectCmd = new StringBuffer();
      selectCmd.append(SELECT);
      selectCmd.append(M_FIELD__OID).append(COMMA);
      selectCmd.append(MODEL_REF_FIELD__ID).append(COMMA);
      selectCmd.append(M_FIELD__OID).append(COMMA);
      selectCmd.append(M_FIELD__OID);
      selectCmd.append(FROM).append(DatabaseHelper.getQualifiedName(M_TABLE_NAME));

      rs = DatabaseHelper.executeQuery(item, selectCmd.toString());

      if (recover)
      {
         StringBuffer deleteCmd = new StringBuffer();
         deleteCmd.append(DELETE_FROM).append(DatabaseHelper.getQualifiedName(MODEL_REF_TABLE_NAME));
         DatabaseHelper.executeUpdate(item, deleteCmd.toString());
      }

      insertCmd = new StringBuffer();
      insertCmd.append(INSERT_INTO).append(DatabaseHelper.getQualifiedName(MODEL_REF_TABLE_NAME)).append(" (");
      insertCmd.append(MODEL_REF_FIELD__CODE).append(COMMA);
      insertCmd.append(MODEL_REF_FIELD__MODEL_OID).append(COMMA);
      insertCmd.append(MODEL_REF_FIELD__ID).append(COMMA);
      insertCmd.append(MODEL_REF_FIELD__REF_OID).append(COMMA);
      insertCmd.append(MODEL_REF_FIELD__DEPLOYMENT).append(")");
      insertCmd.append("VALUES (?,?,?,?,?)");
      insertStatement = DatabaseHelper.prepareLoggingStatement(connection, insertCmd.toString());

      while (rs.next())
      {
         insertStatement.setInt(1, 0);
         insertStatement.setInt(2, rs.getInt(1));
         insertStatement.setString(3, rs.getString(2));
         insertStatement.setString(4, rs.getString(3));
         insertStatement.setString(5, rs.getString(4));
         insertStatement.execute();
      }

      insertStatement.close();
      rs.close();

      //Populate field 'deployment' in Process Instance table
      selectCmd = new StringBuffer();
      selectCmd.append(SELECT);
      selectCmd.append(MODEL_REF_FIELD__DEPLOYMENT).append(COMMA);
      selectCmd.append(MODEL_REF_FIELD__MODEL_OID);
      selectCmd.append(FROM).append(DatabaseHelper.getQualifiedName(MODEL_REF_TABLE_NAME));

      rs = DatabaseHelper.executeQuery(item, selectCmd.toString());

      StringBuffer updateCmd = new StringBuffer();
      updateCmd.append(UPDATE).append(DatabaseHelper.getQualifiedName(PI_TABLE_NAME));
      updateCmd.append(SET).append(PI_FIELD__DEPLOYMENT).append(EQUAL_PLACEHOLDER);
      updateCmd.append(WHERE).append(PI_FIELD__MODEL).append(EQUAL_PLACEHOLDER);
      PreparedStatement p2 = DatabaseHelper.prepareLoggingStatement(connection, updateCmd.toString());
      while (rs.next())
      {
         p2.setLong(1, rs.getLong(1));
         p2.setLong(2, rs.getLong(2));
         p2.execute();
      }
      p2.close();
      rs.close();
   }

   private void migrateDataClusterDefinition() throws SQLException
   {
      Set<Pair<Long, String>> partitionInfo = CollectionUtils.newSet();
      partitionInfo = fetchListOfPartitionInfo();

      if ( !partitionInfo.isEmpty())
      {
         // any partition is OK for that migration step - get the first one
         Pair<Long, String> info = partitionInfo.iterator().next();
         Utils.initCarnotEngine(info.getSecond(), getRtJobEngineProperties());

         PropertyPersistor prop = PropertyPersistor
               .findByName(RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION);

         if (null != prop)
         {
            String xml = LargeStringHolder.getLargeString(prop.getOID(),
                  PropertyPersistor.class);

            // get model id
            Iterator modelIter = ModelPersistorBean.findAll(info.getFirst().shortValue());
            if (modelIter.hasNext())
            {
               ModelPersistorBean model = (ModelPersistorBean) modelIter.next();
               model.setConfVarProvider(new NullConfigurationVariablesProvider());
               String modelId = model.getId();

               // update xml with model id
               String toReplace = "<data-slot(?!s)"; // (?!s) will prevent matching for <data-slots (see s at the end)
               String replaceBy = "<data-slot modelId=\"" + modelId + "\"";
               String updatedXml = xml.replaceAll(toReplace, replaceBy);

               // replace orig xml with updated xml (now containing model information)
               LargeStringHolder.setLargeString(prop.getOID(), PropertyPersistor.class,
                     updatedXml);

               item.commit();
            }
         }
      }
   }

   private void migratePermissions(boolean recover) throws SQLException
   {
      Set<Pair<Long, String>> partitionInfo = CollectionUtils.newSet();
      partitionInfo = fetchListOfPartitionInfo();

      for (Pair<Long, String> info : partitionInfo)
      {
         migratePermissions(info.getSecond(), recover);
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
         selectRowsStmt = DatabaseHelper.prepareLoggingStatement(connection, selectCmd.toString());

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

   private void migratePermissions(String partitionId, boolean recover)
   {
      // init engine
      Utils.initCarnotEngine(partitionId, getRtJobEngineProperties());

      IModel model = ModelManagerFactory.getCurrent().findActiveModel();

      if (model == null)
      {
         model = ModelManagerFactory.getCurrent().findLastDeployedModel();
      }
      if (model != null)
      {
         try
         {
            Map<String, List<String>> allPermissions = CollectionUtils.newSortedMap();
            // Model Permissions
            Map<String, String> modelPermissions = getPermissionsFromAttributes(model.getAllAttributes());
            addModelPermissions(allPermissions, modelPermissions);

            if ( !allPermissions.isEmpty())
            {
               if (recover)
               {
                  StringBuffer deleteCmd = new StringBuffer();
                  deleteCmd.append(DELETE_FROM).append(DatabaseHelper.getQualifiedName(P_TABLE_NAME));
                  DatabaseHelper.executeUpdate(item, deleteCmd.toString());
               }

               String preferencesId = "global";
               insertPermissions(preferencesId, allPermissions);

               item.commit();
            }
         }
         catch (SQLException e)
         {
            reportExeption(e, "INSERT of model permissions to preferences table failed.");
         }
      }
   }

   private static Map<String, String> getPermissionsFromAttributes(
         Map<String, Object> modelAttributes)
   {
      Map<String, String> modelPermissions = CollectionUtils.newHashMap();

      for (java.util.Map.Entry entry : modelAttributes.entrySet())
      {
         if (entry.getKey() instanceof String
               && ((String) entry.getKey()).startsWith(Permissions.PREFIX))
         {
            modelPermissions.put(
                  ((String) entry.getKey()).substring(Permissions.PREFIX.length()),
                  (String) entry.getValue());
         }
      }
      return modelPermissions;
   }

   private void addModelPermissions(Map<String, List<String>> allPermissions,
         Map<String, String> modelPermissions) throws SQLException
   {
      for (Entry<String, String> entry : modelPermissions.entrySet())
      {
         String key = withoutScope(entry.getKey());
         String value = entry.getValue();

         if (key.endsWith("]"))
         {
            key = key.substring(0, key.lastIndexOf('['));
            if (allPermissions.get(key) != null)
            {
               List<String> values = allPermissions.get(key);
               values.add(value);
               allPermissions.put(key, values);
            }
            else
            {
               List values = new LinkedList<String>();
               values.add(value);
               allPermissions.put(key, values);
            }

         }
         else
         {
            List values = new LinkedList<String>();
            values.add(value);
            allPermissions.put(key, values);
         }
      }

      // filter default permissions
      List<String> toRemove = new LinkedList<String>();
      for (Map.Entry<String, List<String>> entry : allPermissions.entrySet())
      {
         String key = entry.getKey();
         List<String> values = entry.getValue();
         if (values != null && values.size() == 1
               && isModelDefaultPermission(key, values.get(0)))
         {
            toRemove.add(key);
         }
      }
      for (String key : toRemove)
      {
         allPermissions.remove(key);
      }

   }

   private boolean isModelDefaultPermission(String key, String value)
   {
      if (("controlProcessEngine".equals(key) && "Administrator".equals(value))
            || ("deployProcessModel".equals(key) && "Administrator".equals(value))
            || ("forceSuspend".equals(key) && "Administrator".equals(value))
            || ("manageDaemons".equals(key) && "Administrator".equals(value))
            || ("modifyAuditTrail".equals(key) && "Administrator".equals(value))
            || ("modifyDepartments".equals(key) && "Administrator".equals(value))
            || ("modifyUserData".equals(key) && "Administrator".equals(value))
            || ("manageAuthorization".equals(key) && "Administrator".equals(value))
            || ("readAuditTrailStatistics".equals(key) && "Administrator".equals(value))
            || ("readDepartments".equals(key) && "__carnot_internal_all_permissions__".equals(value))
            || ("readModelData".equals(key) && "__carnot_internal_all_permissions__".equals(value))
            || ("readUserData".equals(key) && "__carnot_internal_all_permissions__".equals(value))
            || ("resetUserPassword".equals(key) && "__carnot_internal_all_permissions__".equals(value))
            || ("runRecovery".equals(key) && "Administrator".equals(value))
      //
      )
      {
         return true;
      }
      return false;
   }

   private String withoutScope(String key)
   {
      return key.substring(key.indexOf('.') + 1);
   }

   private void insertPermissions(String preferencesId,
         Map<String, List<String>> allPermissions) throws SQLException
   {
      String[] insertCols = new String[] {
            P_FIELD__OWNER_ID, P_FIELD__OWNER_TYPE, P_FIELD__MODULE_ID,
            P_FIELD__PREFERENCES_ID, P_FIELD__PARTITION, P_FIELD__STRING_VALUE};

      StringBuffer insertPrefCmd = new StringBuffer()
            .append(INSERT_INTO).append(DatabaseHelper.getQualifiedName(P_TABLE_NAME))
            .append("(")
            .append(StringUtils.join(Arrays.asList(insertCols).iterator(), COMMA))
            .append(")")
            .append(valuesFragment(insertCols.length));

      Connection connection = item.getConnection();
      PreparedStatement prefStmt
         = DatabaseHelper.prepareLoggingStatement(connection, insertPrefCmd.toString());

      short partitionOid = SecurityProperties.getPartitionOid();
      String moduleId = PREFERENCES_MODULE_ID_PERMISSIONS;
      prefStmt.setLong(1, partitionOid);
      prefStmt.setString(2, PREFERENCES_PARTITION_SCOPE_TYPE);
      prefStmt.setString(3, moduleId);
      prefStmt.setString(4, preferencesId);
      prefStmt.setLong(5, partitionOid);
      prefStmt.setString(6, new String(writePreferencesContent(moduleId, preferencesId,
            allPermissions)));
      prefStmt.addBatch();

      try
      {
         prefStmt.executeBatch();
      }
      finally
      {
         prefStmt.close();
      }
   }

   private byte[] writePreferencesContent(String moduleId, String preferencesId,
         Map<String, List<String>> preferencesMap)
   {
      ByteArrayOutputStream baosPrefsContent = new ByteArrayOutputStream();

      byte[] content;
      try
      {
         XmlPreferenceWriter prefsWriter = new XmlPreferenceWriter();
         prefsWriter.writePreferences(baosPrefsContent, moduleId, preferencesId,
               preferencesMap);

         content = baosPrefsContent.toByteArray();
      }
      catch (IOException ioe)
      {
         // TODO
         content = null;
         throw new PublicException(ioe);
      }
      finally
      {
         try
         {
            baosPrefsContent.close();
         }
         catch (IOException ioe)
         {
            // ignore
         }
      }
      return content;
   }

   private StringBuffer valuesFragment(final int columnCount)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(" VALUES ").append("(");

      buffer.append(StringUtils.join(new Iterator()
      {
         private int counter = 0;

         public boolean hasNext()
         {
            return counter < columnCount;
         }

         public Object next()
         {
            ++counter;
            return "?";
         }

         public void remove()
         {
            throw new UnsupportedOperationException();
         }
      }, COMMA)).append(")");

      return buffer;
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      // Drop old index and create new one without model column.
      DatabaseHelper.alterTable(item, new AlterTableInfo(UP_TABLE_NAME)
      {
         private final FieldInfo PARTICIPANT = new FieldInfo(
               UP_FIELD__PARTICIPANT, Long.TYPE);

         private final FieldInfo DEPARTMENT = new FieldInfo(
               UP_FIELD__DEPARTMENT, Long.TYPE);

         private final IndexInfo IDX2 = new IndexInfo(UP_INDEX_IDX2, false, new FieldInfo[] {
               PARTICIPANT, DEPARTMENT});

         @Override
         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {IDX2};
         }
      }, this);
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
      info("A new table 'preferences' with the columns "
            + "'ownerId', 'ownerType', 'moduleId', 'preferencesId', 'partition', 'stringValue' "
            + "and index 'preferences_idx1' will be created.");
      info("The table 'message_store' will be dropped.");
      info("A new table 'model_ref' with the columns "
            + "'code', 'modelOid', 'id', 'refOid', 'deployment' "
            + "and indexes 'model_ref_idx1' and 'model_ref_idx2' will be created.");
      info("A new table 'model_dep' with the columns "
            + "'oid', 'deployer', 'deploymentTime', 'validFrom', 'deploymentComment' "
            + "and indexes 'model_dep_idx1', 'model_dep_idx2' and 'model_dep_idx3' will be created.");
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (session.getDBDescriptor().getUseLockTablesDefault())
      {
         info("A new table 'model_dep_lck' with the column 'oid' and index 'model_dep_lck_idx' will be created.");
      }
      info("A new column 'deployment' will be created in table 'process_instance'.");
   }

   @Override
   protected void printMigrateDataInfo()
   {
      info("Table 'model_ref' will be populated.");
      info("Table 'model_dep' will be populated.");
      info("Field 'deployment' in table 'process_instance' will be populated.");
      info("Index 'user_particip_idx2' in table 'user_participant' will be modified.");
      info("Permissions will be inserted into table 'preferences'.");
      info("Model Id will be added to xml data cluster definition.");
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {

   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
