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

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.model.parser.info.ModelInfo;
import org.eclipse.stardust.engine.core.model.parser.info.ModelInfoRetriever;
import org.eclipse.stardust.engine.core.model.parser.info.XpdlInfo;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeModelLoader;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeOidRegistry;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.IndexInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.IndexWithTableInfo;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.ModelXmlLoader;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.NVLFunction;
import org.eclipse.stardust.engine.core.upgrade.utils.xml.*;


/**
 * @author born
 * @version $Revision$
 */
public class R4_6_0from4_5_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R4_6_0from4_5_0RuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(4, 6, 0);

   // Property flags
   private static final int NO_PROPERTY_AVAILABLE = 0;
   private static final int ANY_PROPERTY_AVAILABLE = 1;
   private static final int NOTE_PROPERTY_AVAILABLE = 2;

   private static final String NOTE = "NOTE";
   private static final String NOTE_DATA_PATH = "Note";

   // Property tables
   private static final String PI_PROP_TABLE = "proc_inst_property";
   private static final String AI_PROP_TABLE = "act_inst_property";
   private static final String UG_PROP_TABLE = "usergroup_property";
   private static final String U_PROP_TABLE = "user_property";
   private static final String PROP_FIELD_USER = "workflowUser";
   private static final String PROP_FIELD_LAST_MOD = "lastModificationTime";
   private static final String PROP_FIELD_SCOPE = "scope";

   private static final Object PIP_ALIAS = "pip";
   private static final String PIP_FIELD_OID = "oid";
   private static final String PIP_FIELD_OBJECTOID = "objectOID";
   private static final String PIP_FIELD_NAME = "name";
   private static final String PIP_FIELD_TYPE_KEY = "type_key";
   private static final String PIP_FIELD_STRING_VALUE = "string_value";

   private static final String PIP_TMP_INDEX = "pip_tmp_idx";

   // ProcessDefinition table
   private static final String PD_TABLE_NAME = "process_definition";
   private static final String PD_ALIAS = "pd";
   private static final String PD_FIELD_OID = "oid";
   private static final String PD_FIELD_MODEL = "model";

   // ProcessInstance table
   private static final String PI_TABLE_NAME = "process_instance";
   private static final String PI_ALIAS = "pi";
   private static final String PI_FIELD_OID = "oid";
   private static final String PI_FIELD_PROCESS_DEFINITION = "processDefinition";
   private static final String PI_FIELD_PROPERTIES_AVAILABLE = "propertiesAvailable";
   private static final String PI_FIELD_MODEL = "model";

   // UserSession table.
   private static final String US_TABLE_NAME = "wfuser_session";
   private static final String US_FIELD__OID = "oid";
   private static final String US_FIELD__USER = "workflowUser";
   private static final String US_FIELD__START_TIME = "startTime";

   // StructuredData table.
   private static final String SD_TABLE_NAME = "structured_data";
   private static final String SD_BACKUP_TABLE_NAME = "structured_data_backup";
   private static final String SD_FIELD__OID = "oid";
   private static final String SD_FIELD__XPATH = "xpath";
   private static final String SD_FIELD__DATA = "data";
   private static final String SD_FIELD__MODEL = "model";
   private static final String SD_PK_SEQUENCE = "structured_data_seq";

   private static final String SD_VALUE_TABLE_NAME = "structured_data_value";
   private static final String SD_VALUE_FIELD__XPATH = "xpath";

   private static final String DATA_TABLE_NAME = "data";
   private static final String DATA_FIELD__OID = "oid";
   private static final String DATA_FIELD__MODEL = "model";
   private static final String DATA_FIELD__ID = "id";

   private static final String MODEL_TABLE_NAME = "model";
   private static final String MODEL_FIELD__OID = "oid";
   private static final String MODEL_FIELD__ID = "id";
   private static final String MODEL_FIELD__PARTITION = "partition";

   // CLOB data table.
   private static final String CLOB_TABLE_NAME = "clob_data";
   private static final String CLOB_FIELD__OID = "oid";
   private static final String CLOB_FIELD__OWNER_ID = "ownerId";
   private static final String CLOB_FIELD__OWNER_TYPE = "ownerType";
   private static final String CLOB_FIELD__STRINGVAL = "stringValue";
   private static final String CLOB_PK_SEQUENCE = "clob_data_seq";

   // DataValue table
   private static final String DV_TABLE_NAME = "data_value";
   private static final String DV_ALIAS = "dv";
   private static final String DV_FIELD__OID = "oid";
   private static final String DV_FIELD__MODEL = "model";
   private static final String DV_FIELD__DATA = "data";
   private static final String DV_FIELD__PROCESS_INSTANCE = "processInstance";
   private static final String DV_FIELD__TYPE_KEY = "type_key";
   private static final String DV_FIELD__STRING_VALUE = "string_value";

   // LargeStringHolder table
   private static final String LSH_TABLE_NAME = "STRING_DATA";
   private static final String LSH_ALIAS = "lsh";
   private static final String LSH_SEQUENCE = "STRING_DATA_SEQ";
   private static final String LSH_FIELD__OID = "oid";
   private static final String LSH_FIELD__OBJECTID = "objectid";
   private static final String LSH_FIELD__DATA_TYPE = "data_type";
   private static final String LSH_FIELD__DATA = "data";
   private static final String DATA_TABLE = "data";

   private final FieldInfo propAvailableColumn
      = new FieldInfo(PI_FIELD_PROPERTIES_AVAILABLE, Integer.TYPE);
   private NVLFunction<FieldInfo, Integer> nullReplaceFunction = null;

   private int batchSize = 500;

   R4_6_0from4_5_0RuntimeJob()
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

   private List<IndexWithTableInfo> getTemporaryIndexDefinitions()
   {
      DBDescriptor descriptor = item.getDbDescriptor();
      List<IndexWithTableInfo> indexDefinitions
         = new ArrayList<IndexWithTableInfo>();
      if (descriptor instanceof OracleDbDescriptor)
      {
         nullReplaceFunction = new NVLFunction(propAvailableColumn, -8795);
         IndexWithTableInfo nullIndex
            = new IndexWithTableInfo("pi_prop_null_idx99", PI_TABLE_NAME, nullReplaceFunction);
         indexDefinitions.add(nullIndex);
      }

      FieldInfo dvDataField = new FieldInfo(DV_FIELD__DATA, Long.TYPE);
      FieldInfo dvTypeKeyField = new FieldInfo(DV_FIELD__TYPE_KEY, Integer.TYPE);
      FieldInfo dvModelField = new FieldInfo(DV_FIELD__MODEL, Long.TYPE);

      IndexWithTableInfo dataValueIndex
         = new IndexWithTableInfo("data_value_idx99", DV_TABLE_NAME, dvDataField, dvTypeKeyField, dvModelField);
      indexDefinitions.add(dataValueIndex);

      return indexDefinitions;
   }

   private void createTemporaryIndexes(final List<IndexWithTableInfo> indexDefinitions) throws SQLException
   {
      DatabaseMetaData databaseMetaData = DatabaseHelper.getDatabaseMetaData(item);
      for(final IndexWithTableInfo indexWithTableInfo: indexDefinitions)
      {
         if (!DatabaseHelper.hasIndex(databaseMetaData, indexWithTableInfo.getTableName(),
               indexWithTableInfo.getName()))
         {
            DatabaseHelper.alterTable(item, new AlterTableInfo(indexWithTableInfo.getTableName())
            {
               @Override
               public IndexInfo[] getAddedIndexes()
               {
                  IndexInfo[] addedIndexes = new IndexInfo[] {indexWithTableInfo};
                  return addedIndexes;
               }
            }, this);
         }
      }
   }

   private void dropTemporaryIndexes(List<IndexWithTableInfo> indexDefinitions) throws SQLException
   {
      DatabaseMetaData databaseMetaData = DatabaseHelper.getDatabaseMetaData(item);
      for(final IndexWithTableInfo indexWithTableInfo: indexDefinitions)
      {
         if (DatabaseHelper.hasIndex(databaseMetaData, indexWithTableInfo.getTableName(),
               indexWithTableInfo.getName()))
         {
            DatabaseHelper.alterTable(item, new AlterTableInfo(indexWithTableInfo.getTableName())
            {
               @Override
               public IndexInfo[] getDroppedIndexes()
               {
                  IndexInfo[] droppedIndexes = new IndexInfo[] {indexWithTableInfo};
                  return droppedIndexes;
               }
            }, this);
         }
      }
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.alterTable(item, new AlterTableInfo(PI_TABLE_NAME)
      {
         private final FieldInfo FIELD_NOTE_AVAILABLE = new FieldInfo(
               PI_FIELD_PROPERTIES_AVAILABLE, Integer.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {FIELD_NOTE_AVAILABLE};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(PI_PROP_TABLE)
      {
         private final FieldInfo FIELD_LAST_MOD = new FieldInfo(PROP_FIELD_LAST_MOD,
               Long.TYPE);

         private final FieldInfo FIELD_USER = new FieldInfo(PROP_FIELD_USER, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {FIELD_LAST_MOD, FIELD_USER};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(AI_PROP_TABLE)
      {
         private final FieldInfo FIELD_LAST_MOD = new FieldInfo(PROP_FIELD_LAST_MOD,
               Long.TYPE);
         private final FieldInfo FIELD_USER = new FieldInfo(PROP_FIELD_USER, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {FIELD_LAST_MOD, FIELD_USER};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(UG_PROP_TABLE)
      {
         private final FieldInfo FIELD_LAST_MOD = new FieldInfo(PROP_FIELD_LAST_MOD,
               Long.TYPE);
         private final FieldInfo FIELD_USER = new FieldInfo(PROP_FIELD_USER, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {FIELD_LAST_MOD, FIELD_USER};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(U_PROP_TABLE)
      {
         private final FieldInfo FIELD_LAST_MOD = new FieldInfo(PROP_FIELD_LAST_MOD,
               Long.TYPE);
         private final FieldInfo FIELD_SCOPE = new FieldInfo(PROP_FIELD_SCOPE,
               String.class);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {FIELD_LAST_MOD, FIELD_SCOPE};
         }
      }, this);

      final List<IndexInfo> indicesToDrop = new ArrayList<IndexInfo>();
      List<String> indexDropCandidates = new ArrayList<String>();
      // one of the following variants of index names should exist:
      // names longer 18 characters do not work with DB2 8.1.
      // These have been used in original createschema in 4.5.0.
      indexDropCandidates.add("wfuser_session_idx1");
      indexDropCandidates.add("wfuser_session_idx2");
      // These names have been used in upgrade 4.0.0 to 4.5.0
      indexDropCandidates.add("wfuser_session1");
      indexDropCandidates.add("wfuser_session2");
      try
      {
         DatabaseMetaData databaseMetaData = DatabaseHelper.getDatabaseMetaData(item);
         List<IndexInfo> allIndices = DatabaseHelper.getIndices(databaseMetaData, US_TABLE_NAME);
         for(String indexName: indexDropCandidates)
         {
            IndexInfo match = DatabaseHelper.findIndex(databaseMetaData, indexName, allIndices);
            if(match != null)
            {
               indicesToDrop.add(match);
            }
         }
      }
      catch(SQLException e)
      {
         reportExeption(e, "Failed determing which index to drop");
      }

      DatabaseHelper.alterTable(item, new AlterTableInfo(US_TABLE_NAME)
      {
         private final FieldInfo OID = new FieldInfo(US_FIELD__OID, Long.TYPE, 0, true);
         private final FieldInfo USER = new FieldInfo(US_FIELD__USER, Long.TYPE, 0);
         private final FieldInfo START_TIME = new FieldInfo(US_FIELD__START_TIME,
               Long.TYPE, 0);

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {//
                  // new names: conform with createschema and do not exceed 18 characters.
                  new IndexInfo("wfusr_session_idx1", true, new FieldInfo[] {OID}),
                  new IndexInfo("wfusr_session_idx2", false, new FieldInfo[] {
                        USER, START_TIME})};
         }

         public IndexInfo[] getDroppedIndexes()
         {
            return indicesToDrop.toArray(new IndexInfo[indicesToDrop.size()]);
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(PI_PROP_TABLE)
      {
         private final FieldInfo OBJECTOID = new FieldInfo(PIP_FIELD_OBJECTOID,
               Long.TYPE, 0);

         private final FieldInfo NAME = new FieldInfo(PIP_FIELD_NAME, String.class, 0);

         public IndexInfo[] getAddedIndexes()
         {
            // This temporary index is only used for the upgrade job. It will be dropped
            // afterwards.
            return new IndexInfo[] {new IndexInfo(PIP_TMP_INDEX, true, new FieldInfo[] {
                  OBJECTOID, NAME})};
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(SD_BACKUP_TABLE_NAME)
      {
         private static final String INDEX_PREFIX = "struct_data_backup_idx";

         private final FieldInfo OID = new FieldInfo(SD_FIELD__OID, Long.TYPE, 0, true);
         private final FieldInfo DATA = new FieldInfo(SD_FIELD__DATA, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(SD_FIELD__MODEL, Long.TYPE);
         private final FieldInfo XPATH = new FieldInfo(SD_FIELD__XPATH, String.class, 200);

         private final IndexInfo IDX1 = new IndexInfo(INDEX_PREFIX + "1", true,
               new FieldInfo[] {OID});
         private final IndexInfo IDX2 = new IndexInfo(INDEX_PREFIX + "2", false,
               new FieldInfo[] {XPATH});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {OID, DATA, MODEL, XPATH};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {IDX1, IDX2};
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);

      try
      {
         String schema = (String) Parameters.instance().get(
               SessionFactory.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX);
         StringBuffer buffer = new StringBuffer(400);
         buffer.append(INSERT_INTO);
         if (StringUtils.isNotEmpty(schema))
         {
            buffer.append(schema + ".");
         }
         buffer.append(SD_BACKUP_TABLE_NAME);
         buffer.append(" (oid, xpath, data, model) SELECT oid, xpath, data, model FROM ");
         if (StringUtils.isNotEmpty(schema))
         {
            buffer.append(schema + ".");
         }
         buffer.append(SD_TABLE_NAME);
         item.executeDdlStatement(buffer.toString(), false);
         // item.executeDdlStatement("DELETE FROM " SD_TABLE_NAME, false);
      }
      catch (SQLException sqle)
      {
         reportExeption(sqle, "Failed copying " + SD_TABLE_NAME + " table (nested exception).");
      }

      DatabaseHelper.dropTable(item,
            new DropTableInfo(SD_TABLE_NAME, item.isArchiveAuditTrail()
                  ? null
                  : SD_PK_SEQUENCE), this);

      DatabaseHelper.createTable(item, new CreateTableInfo(SD_TABLE_NAME)
      {
         private static final String INDEX_PREFIX = "struct_data_idx";

         private final FieldInfo OID = new FieldInfo(SD_FIELD__OID, Long.TYPE);
         private final FieldInfo DATA = new FieldInfo(SD_FIELD__DATA, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(SD_FIELD__MODEL, Long.TYPE);
         private final FieldInfo XPATH = new FieldInfo(SD_FIELD__XPATH, String.class, 200);

         private final IndexInfo IDX1 = new IndexInfo(INDEX_PREFIX + "1", true,
               new FieldInfo[] {OID, MODEL});
         private final IndexInfo IDX2 = new IndexInfo(INDEX_PREFIX + "2", false,
               new FieldInfo[] {XPATH});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {OID, DATA, MODEL, XPATH};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {IDX1, IDX2};
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(CLOB_TABLE_NAME)
      {
         private static final String INDEX_PREFIX = "clob_dt_i";

         private final FieldInfo OID = new FieldInfo(CLOB_FIELD__OID, Long.TYPE, 0, true);
         private final FieldInfo OWNER_ID = new FieldInfo(CLOB_FIELD__OWNER_ID, Long.TYPE);
         private final FieldInfo OWNER_TYPE = new FieldInfo(CLOB_FIELD__OWNER_TYPE,
               String.class, 32);
         private final FieldInfo STRINGVAL = new FieldInfo(CLOB_FIELD__STRINGVAL,
               String.class, Integer.MAX_VALUE);

         private final IndexInfo IDX1 = new IndexInfo(INDEX_PREFIX + "1", false,
               new FieldInfo[] {OWNER_ID, OWNER_TYPE});
         private final IndexInfo IDX2 = new IndexInfo(INDEX_PREFIX + "2", true,
               new FieldInfo[] {OID});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {OID, OWNER_ID, OWNER_TYPE, STRINGVAL};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {IDX1, IDX2};
         }

         public String getSequenceName()
         {
            return CLOB_PK_SEQUENCE;
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      try
      {
         // perform data migration in well defined TXs
         item.getConnection().setAutoCommit(false);

         //get the definition for temporary indexes to boost performance
         List<IndexWithTableInfo> temporaryIndexDefinitions
            = getTemporaryIndexDefinitions();
         createTemporaryIndexes(temporaryIndexDefinitions);

         info("Setting field process_instance.noteAvailable to default values...");
         updateProcessInstanceTable();
         info("Change storage of process instance notes...");
         updateProcessInstanceNoteStorage();
         info("Updating structured data value table ...");
         updateStructuredDataValueTable();

         dropTemporaryIndexes(temporaryIndexDefinitions);
      }
      catch (SQLException sqle)
      {
         reportExeption(sqle, "Failed migrating runtime item tables (nested exception).");
      }
   }

   private void updateProcessInstanceNoteStorage() throws SQLException
   {
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
               updateProcessInstanceNoteStorage(pendingRows
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
   }

   private List<RTJobCvmDataInfo> getPiNotesData(String modelXml)
   {
      List<RTJobCvmDataInfo> piNotesData = new ArrayList<RTJobCvmDataInfo>();
      try
      {
         ModelInfo modelInfo = ModelInfoRetriever.get(modelXml.getBytes());
         if (modelInfo instanceof XpdlInfo)
         {
            modelXml = XpdlUtils.convertXpdl2Carnot(modelXml);
         }

         RTJobCvmModelParser modelParser = new RTJobCvmModelParser();
         RTJobCvmModelInfo rtModelInfo = modelParser.getIModelInfo(modelXml);
         List<RTJobCvmProcessDefinitionInfo> processDefinitions = rtModelInfo
               .getProcessDefintions();

         // a note must have a datapath with id "Note", an empty accesspath
         // and the corresponding data must be of type String
         for (RTJobCvmProcessDefinitionInfo pd : processDefinitions)
         {
            for (RTJobCvmDataPathInfo dataPathInfo : pd.getDataPathInfos())
            {
               if (dataPathInfo.getId().equals(NOTE_DATA_PATH))
               {
                  String dataId = dataPathInfo.getData();
                  RTJobCvmDataInfo dataInfo = rtModelInfo.findDataById(dataId);
                  if (StringUtils.isEmpty(dataPathInfo.getDataPath())
                        && dataInfo.isStringType())
                  {
                     piNotesData.add(dataInfo);
                  }
                  else
                  {
                     warn(MessageFormat.format(
                           "DataPath with ID 'Note' for process definition {0} found. "
                                 + "But the assigned data is not of type String (current: {1}) "
                                 + "or its access path is not empty (current: {2}). "
                                 + "Its values will be ignored!",
                           new Object[] {
                                 pd.getId(), dataInfo.getType(),
                                 dataPathInfo.getDataPath()}), null);
                  }

               }
            }

         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("error parsing model xml, ", e);
      }

      return piNotesData;
   }


   private List<String> collectDataIds(List<RTJobCvmDataInfo> dataInfos)
   {
      List<String> dataIds = new ArrayList<String>();
      for(RTJobCvmDataInfo dataInfo: dataInfos)
      {
         dataIds.add(dataInfo.getId());
      }
      return dataIds;
   }

   private List<DeployedDataInfo> collectDataOids(long modelOid, List<RTJobCvmDataInfo> dataInfos)
   {
      List<String> dataIds = collectDataIds(dataInfos);
      List<DeployedDataInfo> deployedDataInfo
         = new ArrayList<DeployedDataInfo>();

      if(!dataIds.isEmpty())
      {
         StringBuffer collectDataOidsSql = new StringBuffer();
         collectDataOidsSql.append("Select oid, id from ");
         collectDataOidsSql.append(DatabaseHelper.getQualifiedName(DATA_TABLE));
         collectDataOidsSql.append(" where model = ").append(modelOid);
         collectDataOidsSql.append(" and id ");
         collectDataOidsSql.append(DatabaseHelper.getInClause(dataIds));

         try
         {
            ResultSet rs = DatabaseHelper.executeQuery(item, collectDataOidsSql.toString());
            while(rs.next())
            {
               Long dataOid = rs.getLong(1);
               String dataId = rs.getString(2);

               DeployedDataInfo ddi
                  = new DeployedDataInfo(dataId, dataOid, modelOid);
               deployedDataInfo.add(ddi);
            }
         }
         catch (SQLException e)
         {
            throw new RuntimeException("Exception occured during collecting data oids from auditrail.", e);
         }
      }

      return deployedDataInfo;
   }

   private void updateProcessInstanceNoteStorage(String partition) throws SQLException
   {
      ModelXmlLoader modelXmlLoader = ModelXmlLoader.getInstance(item);
      Set<Long> deployedModelOids = modelXmlLoader.getModelOids();
      for(Long modelOid: deployedModelOids)
      {
         String modelXml = modelXmlLoader.getModelXml(modelOid);
         List<RTJobCvmDataInfo> piNoteData = getPiNotesData(modelXml);
         List<DeployedDataInfo> deployedDataInfo = collectDataOids(modelOid, piNoteData);

         for(DeployedDataInfo ddi: deployedDataInfo)
         {
            updateProcessInstanceNoteStorage(modelOid, ddi.getDataOid());
         }
      }
   }

   private void updateProcessInstanceNoteStorage(long modelOid, long dataOid)
         throws SQLException
   {
      PreparedStatement selectRowsStmt = null;
      PreparedStatement insertPropRowsStmt = null;
      PreparedStatement insertStringDataRowsStmt = null;
      PreparedStatement updateRowsStmt = null;
      try
      {
         String schemaName = (String) Parameters.instance().get(
               SessionFactory.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX);
         Session session = (Session) SessionFactory
               .getSession(SessionProperties.DS_NAME_AUDIT_TRAIL);
         DBDescriptor dbDescriptor = session.getDBDescriptor();

         /*
          * -- type_key = 8 --> value complete in string_value -- type_key = 11 --> value
          * complete in string_data (concatenate!) select dv.STRING_VALUE, dv.TYPE_KEY,
          * pi.oid pi_oid from DATA_VALUE dv inner join PROCESS_INSTANCE pi on
          * (dv.PROCESSINSTANCE = pi.OID) inner join PROCESS_DEFINITION pd on
          * (pi.PROCESSDEFINITION = pd.oid and pi.model = pd.model) where dv.DATA = 7 and
          * dv.model = 601 and pi.PROPERTIESAVAILABLE != 3
          */
         StringBuffer piJoinPredicate = new StringBuffer()
               .append(DV_ALIAS).append(DOT).append(DV_FIELD__PROCESS_INSTANCE)
               .append(EQUALS)
               .append(PI_ALIAS).append(DOT).append(PI_FIELD_OID);

         StringBuffer pdJoinPredicate = new StringBuffer()
               .append(PI_ALIAS).append(DOT).append(PI_FIELD_PROCESS_DEFINITION)
               .append(EQUALS)
               .append(PD_ALIAS).append(DOT).append(PD_FIELD_OID)
               .append(AND)
               .append(PI_ALIAS).append(DOT).append(PI_FIELD_MODEL)
               .append(EQUALS)
               .append(PD_ALIAS).append(DOT).append(PD_FIELD_MODEL);

         String dvTableName = DatabaseHelper.getQualifiedName(DV_TABLE_NAME);
         String piTableName = DatabaseHelper.getQualifiedName(PI_TABLE_NAME);
         String pdTableName = DatabaseHelper.getQualifiedName(PD_TABLE_NAME);

         StringBuffer selectCmd = new StringBuffer()
               .append(SELECT)
                  .append(DV_ALIAS).append(DOT).append(DV_FIELD__OID).append(COMMA)
                  .append(DV_ALIAS).append(DOT).append(DV_FIELD__STRING_VALUE).append(COMMA)
                  .append(DV_ALIAS).append(DOT).append(DV_FIELD__TYPE_KEY).append(COMMA)
                  .append(PI_ALIAS).append(DOT).append(PI_FIELD_OID)
               .append(FROM)
                  .append(dvTableName).append(SPACE).append(DV_ALIAS)
               .append(INNER_JOIN)
                  .append(piTableName).append(SPACE).append(PI_ALIAS).append(onFragment(piJoinPredicate))
               .append(INNER_JOIN)
                  .append(pdTableName).append(SPACE).append(PD_ALIAS).append(onFragment(pdJoinPredicate))
               .append(WHERE)
                  .append(DV_ALIAS).append(DOT).append(DV_FIELD__DATA).append(EQUAL_PLACEHOLDER)
                  .append(AND)
                  .append(DV_ALIAS).append(DOT).append(DV_FIELD__MODEL).append(EQUAL_PLACEHOLDER)
                  .append(AND)
                  .append(PI_ALIAS).append(DOT).append(PI_FIELD_PROPERTIES_AVAILABLE).append(NOT_EQUAL_PLACEHOLDER)
                  .append(AND)
                  .append(DV_ALIAS).append(DOT).append(DV_FIELD__TYPE_KEY).append(" != ").append(-1l);

         String[] insertCols;
         String oidValue;
         if (dbDescriptor.supportsSequences())
         {
            insertCols = new String[] {
                  PIP_FIELD_OID, PIP_FIELD_OBJECTOID, PIP_FIELD_NAME, PIP_FIELD_TYPE_KEY,
                  PIP_FIELD_STRING_VALUE};
            oidValue = dbDescriptor.getNextValForSeqString(schemaName,
                  "proc_inst_property_seq");
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            insertCols = new String[] {
                  PIP_FIELD_OBJECTOID, PIP_FIELD_NAME, PIP_FIELD_TYPE_KEY,
                  PIP_FIELD_STRING_VALUE};
            oidValue = "";
         }
         else
         {
            // TODO: Check for it at the beginning of the upgrade job!
            throw new PublicException(
                  "Database does neither support sequences nor identity columns.");
         }

         String piPropertyTableName = DatabaseHelper.getQualifiedName(PI_PROP_TABLE);
         StringBuffer insertPropCmd = new StringBuffer().append(INSERT_INTO)
               .append(piPropertyTableName).append("(")
               .append(StringUtils.join(Arrays.asList(insertCols).iterator(), COMMA))
               .append(")").append(valuesFragment(oidValue, insertCols.length));

         if (dbDescriptor.supportsSequences())
         {
            insertCols = new String[] {
                  LSH_FIELD__OID, LSH_FIELD__OBJECTID, LSH_FIELD__DATA_TYPE,
                  LSH_FIELD__DATA};
            oidValue = dbDescriptor.getNextValForSeqString(schemaName, LSH_SEQUENCE)
                  + COMMA;
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            insertCols = new String[] {
                  LSH_FIELD__OBJECTID, LSH_FIELD__DATA_TYPE, LSH_FIELD__DATA};
            oidValue = "";
         }
         else
         {
            // TODO: Check for it at the beginning of the upgrade job!
            throw new PublicException(
                  "Database does neither support sequences nor identity columns.");
         }

         String lshTableName = DatabaseHelper.getQualifiedName(LSH_TABLE_NAME);

         StringBuffer insertSubStringDataCmd = new StringBuffer()
               .append(SELECT)
                  .append(PIP_ALIAS).append(DOT).append(PIP_FIELD_OID).append(COMMA)
                  .append(quoted(PI_PROP_TABLE)).append(COMMA)
                  .append(LSH_ALIAS).append(DOT).append(LSH_FIELD__DATA)
               .append(FROM)
                  .append(lshTableName).append(SPACE).append(LSH_ALIAS).append(COMMA)
                  .append(piPropertyTableName).append(SPACE).append(PIP_ALIAS)
               .append(WHERE)
                  .append(LSH_ALIAS).append(DOT).append(LSH_FIELD__OBJECTID).append(EQUAL_PLACEHOLDER)
                  .append(AND)
                  .append(LSH_ALIAS).append(DOT).append(LSH_FIELD__DATA_TYPE).append(EQUALS).append(quoted(DV_TABLE_NAME))
                  .append(AND)
                  .append(PIP_ALIAS).append(DOT).append(PIP_FIELD_OBJECTOID).append(EQUAL_PLACEHOLDER)
                  .append(AND)
                  .append(PIP_ALIAS).append(DOT).append(PIP_FIELD_NAME).append(EQUALS).append(quoted(NOTE))
               .append(ORDER_BY)
                  .append(LSH_ALIAS).append(DOT).append(LSH_FIELD__OID);

         StringBuffer insertStringDataCmd = new StringBuffer()
               .append(INSERT_INTO)
                  .append(lshTableName).append("(")
                  .append(StringUtils.join(Arrays.asList(insertCols).iterator(), COMMA))
                  .append(")")
               .append(SELECT)
                  .append(oidValue).append("sub.*")
               .append(FROM)
                  .append("(").append(insertSubStringDataCmd).append(") sub");

         /*
          * SELECT STRING_DATA_SEQ.NEXTVAL, SUB.* FROM (SELECT PIP.OID,
          * 'proc_inst_property', LSH.DATA FROM STRING_DATA LSH, PROC_INST_PROPERTY PIP
          * WHERE LSH.OBJECTID = 302 AND -- DV_oid LSH.DATA_TYPE = 'data_value' AND
          * PIP.OBJECTOID = 302 AND -- PI_oid PIP.NAME = 'Note' ORDER BY LSH.OID) SUB
          */

         StringBuffer updateCmd = new StringBuffer()
               .append(UPDATE).append(piTableName)
               .append(SET)
                  .append(PI_FIELD_PROPERTIES_AVAILABLE).append(EQUAL_PLACEHOLDER)
               .append(WHERE)
                  .append(PI_FIELD_OID).append(EQUAL_PLACEHOLDER);

         Connection connection = item.getConnection();

         int rowCounter = 0;

         selectRowsStmt = connection.prepareStatement(selectCmd.toString());
         selectRowsStmt.setLong(1, dataOid);
         selectRowsStmt.setLong(2, modelOid);
         selectRowsStmt.setInt(3, ANY_PROPERTY_AVAILABLE | NOTE_PROPERTY_AVAILABLE);

         insertPropRowsStmt = connection.prepareStatement(insertPropCmd.toString());
         insertStringDataRowsStmt = connection.prepareStatement(insertStringDataCmd
               .toString());
         updateRowsStmt = connection.prepareStatement(updateCmd.toString());

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
                     final long dvTypeKey = pendingRows.getLong(3);
                     if (dvTypeKey != 8 && dvTypeKey != 11)
                     {
                        final long dvOid = pendingRows.getLong(1);
                        fatal(MessageFormat.format(
                              "This is no note of type string or big string: DV-OID: {0}, DV-TypeKey: {1}",
                              new Object[] {new Long(dvOid), new Long(dvTypeKey)}), null);
                     }

                     final String dvStringValue = pendingRows.getString(2);
                     final long piOid = pendingRows.getLong(4);

                     insertPropRowsStmt.setLong(1, piOid);
                     insertPropRowsStmt.setString(2, NOTE);
                     insertPropRowsStmt.setLong(3, dvTypeKey);
                     insertPropRowsStmt.setString(4, dvStringValue);
                     insertPropRowsStmt.addBatch();

                     if (dvTypeKey == 11)
                     {
                        final long dvOid = pendingRows.getLong(1);
                        insertStringDataRowsStmt.setLong(1, dvOid);
                        insertStringDataRowsStmt.setLong(2, piOid);
                        insertStringDataRowsStmt.addBatch();
                     }

                     updateRowsStmt.setInt(1, ANY_PROPERTY_AVAILABLE
                           | NOTE_PROPERTY_AVAILABLE);
                     updateRowsStmt.setLong(2, piOid);
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

               if (!eating)
               {
                  break;
               }

               insertPropRowsStmt.executeBatch();
               // inserts into data_string-table have to be done after inserts into
               // PI-property table.
               insertStringDataRowsStmt.executeBatch();
               updateRowsStmt.executeBatch();

               connection.commit();

               info(MessageFormat.format("Committing updates of field {1} on table {0}"
                     + " and inserts into table {2}" + " after {3} rows.", new Object[] {
                     PI_TABLE_NAME, PI_FIELD_PROPERTIES_AVAILABLE, PI_PROP_TABLE,
                     new Integer(rowCounter)}));
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
         QueryUtils.closeStatement(insertPropRowsStmt);
         QueryUtils.closeStatement(insertStringDataRowsStmt);
         QueryUtils.closeStatement(updateRowsStmt);
      }
   }

   private void updateStructuredDataValueTable() throws SQLException
   {
      HashMap registries = new HashMap(); // <partition>,<runtime registry>
      HashMap oids = new HashMap(); // <runtime registry>,(<runtime oid>,<actual oid>)

      PreparedStatement selectRowsStmt = null;
      PreparedStatement insertRowsStmt = null;
      PreparedStatement updateRowsStmt = null;
      try
      {
         String sdBackupTableName = DatabaseHelper.getQualifiedName(SD_BACKUP_TABLE_NAME);
         String dataTableName = DatabaseHelper.getQualifiedName(DATA_TABLE_NAME);
         String modelTableName = DatabaseHelper.getQualifiedName(MODEL_TABLE_NAME);

         StringBuffer selectCmd = new StringBuffer()
               .append("SELECT sd.").append(SD_FIELD__OID)
                  .append(",sd.").append(SD_FIELD__DATA)
                  .append(",sd.").append(SD_FIELD__MODEL)
                  .append(",sd.").append(SD_FIELD__XPATH)
                  .append(",d.").append(DATA_FIELD__ID)
                  .append(",m.").append(MODEL_FIELD__PARTITION)
                  .append(",m.").append(MODEL_FIELD__ID)
               .append(FROM)
                  .append(sdBackupTableName).append(" sd,")
                  .append(dataTableName).append(" d,")
                  .append(modelTableName).append(" m")
               .append(" WHERE sd.").append(SD_FIELD__DATA).append(" = d.").append(DATA_FIELD__OID)
                  .append(AND)
                  .append("sd.").append(SD_FIELD__MODEL).append(" = d.").append(DATA_FIELD__MODEL)
                  .append(AND).append("sd.").append(SD_FIELD__MODEL).append(" = m.").append(MODEL_FIELD__OID);

         String sdTableName = DatabaseHelper.getQualifiedName(SD_TABLE_NAME);
         String sdValueTableName = DatabaseHelper.getQualifiedName(SD_VALUE_TABLE_NAME);

         StringBuffer insertCmd = new StringBuffer()
               .append(INSERT_INTO).append(sdTableName)
                  .append(" (").append(SD_FIELD__OID).append(',')
                  .append(SD_FIELD__DATA).append(',')
                  .append(SD_FIELD__MODEL).append(',')
                  .append(SD_FIELD__XPATH).append(')')
               .append(" VALUES (?,?,?,?)");

         StringBuffer updateCmd = new StringBuffer()
               .append(UPDATE).append(sdValueTableName)
               .append(SET).append(SD_VALUE_FIELD__XPATH).append(EQUAL_PLACEHOLDER)
               .append(WHERE).append(SD_VALUE_FIELD__XPATH).append(EQUAL_PLACEHOLDER);

         Connection connection = item.getConnection();

         selectRowsStmt = connection.prepareStatement(selectCmd.toString());
         insertRowsStmt = connection.prepareStatement(insertCmd.toString());
         updateRowsStmt = connection.prepareStatement(updateCmd.toString());

         ResultSet pendingRows = null;
         try
         {
            pendingRows = selectRowsStmt.executeQuery();
            while (pendingRows.next())
            {
               try
               {
                  Long oid = new Long(pendingRows.getLong(1));
                  long data = pendingRows.getLong(2);
                  long model = pendingRows.getLong(3);
                  String xpath = pendingRows.getString(4);
                  String dataId = pendingRows.getString(5);
                  Short partition = new Short(pendingRows.getShort(6));
                  String modelId = pendingRows.getString(7);

                  RuntimeOidRegistry registry = getRuntimeOidRegistry(registries,
                        partition);
                  String[] fqId = new String[] {modelId, dataId, xpath};
                  Long rtOid = new Long(registry.getRuntimeOid(
                        RuntimeOidRegistry.STRUCTURED_DATA_XPATH, fqId));
                  if (rtOid.longValue() == 0)
                  {
                     // first occurrence of the xpath
                     rtOid = new Long(registry.registerNewRuntimeOid(
                           IRuntimeOidRegistry.STRUCTURED_DATA_XPATH, fqId));
                     setOid(oids, registry, rtOid, oid);
                     insertRowsStmt.setLong(1, oid.longValue());
                     insertRowsStmt.setLong(2, data);
                     insertRowsStmt.setLong(3, model);
                     insertRowsStmt.setString(4, xpath);
                     insertRowsStmt.addBatch();
                     // no sdv update required since the oid is preserved
                  }
                  else
                  {
                     Long newOid = getOid(oids, registry, rtOid);

                     // insert into STRUCTURED_DATA, but take the OID from the existing
                     // entry
                     insertRowsStmt.setLong(1, newOid.longValue());
                     insertRowsStmt.setLong(2, data);
                     insertRowsStmt.setLong(3, model);
                     insertRowsStmt.setString(4, xpath);
                     insertRowsStmt.addBatch();

                     // update references in STRUCTURED_DATA_VALUE to point to the common
                     // OID
                     updateRowsStmt.setLong(1, newOid.longValue());
                     updateRowsStmt.setLong(2, oid.longValue());
                     updateRowsStmt.addBatch();
                  }
               }
               catch (SQLException e)
               {
                  warn(e.getMessage(), null);
               }
            }

            insertRowsStmt.executeBatch();
            updateRowsStmt.executeBatch();
            connection.commit();
         }
         finally
         {
            QueryUtils.closeResultSet(pendingRows);
         }

         info(SD_TABLE_NAME + " table upgraded.");
         info(SD_VALUE_TABLE_NAME + " table upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectRowsStmt);
         QueryUtils.closeStatement(insertRowsStmt);
         QueryUtils.closeStatement(updateRowsStmt);
      }
   }

   private Long getOid(HashMap oids, RuntimeOidRegistry registry, Long rtOid)
   {
      HashMap registeredOids = (HashMap) oids.get(registry);
      return (Long) registeredOids.get(rtOid);
   }

   private void setOid(HashMap oids, RuntimeOidRegistry registry, Long rtOid, Long oid)
   {
      HashMap registeredOids = (HashMap) oids.get(registry);
      if (registeredOids == null)
      {
         registeredOids = new HashMap();
         oids.put(registry, registeredOids);
      }
      registeredOids.put(rtOid, oid);
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

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.dropTable(item, new DropTableInfo(SD_BACKUP_TABLE_NAME, null), this);
      DatabaseHelper.alterTable(item, new AlterTableInfo(PI_PROP_TABLE)
      {
         public IndexInfo[] getDroppedIndexes()
         {
            return new IndexInfo[] {new IndexInfo(PIP_TMP_INDEX, NO_FIELDS)};
         }
      }, this);
   }

   private StringBuffer quoted(String text)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(QUOTE).append(text).append(QUOTE);

      return buffer;
   }

   private StringBuffer existsFragment(StringBuffer subSelect)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(EXISTS).append("(").append(subSelect).append(")");

      return buffer;
   }

   private StringBuffer onFragment(StringBuffer predicate)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(ON).append("(").append(predicate).append(")");

      return buffer;
   }

   private StringBuffer valuesFragment(final String oidValue, final int columnCount)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(VALUES).append("(");

      if (!StringUtils.isEmpty(oidValue))
      {
         buffer.append(oidValue).append(COMMA);
      }

      buffer.append(StringUtils.join(new Iterator()
      {
         private int counter = !StringUtils.isEmpty(oidValue) ? 1 : 0;

         public boolean hasNext()
         {
            return counter < columnCount;
         }

         public Object next()
         {
            ++counter;
            return PLACEHOLDER;
         }

         public void remove()
         {
            throw new UnsupportedOperationException();
         }
      }, COMMA)).append(")");

      return buffer;
   }

   private void updateProcessInstanceTable() throws SQLException
   {
      PreparedStatement selectRowsStmt = null;
      PreparedStatement updateRowsStmt = null;
      try
      {
         String piPropertyTableName = DatabaseHelper.getQualifiedName(PI_PROP_TABLE);
         String piTableName = DatabaseHelper.getQualifiedName(PI_TABLE_NAME);

         StringBuffer subselectPiPropCmd = new StringBuffer()
               .append(SELECT).append(PIP_FIELD_OBJECTOID)
               .append(FROM).append(piPropertyTableName)
               .append(WHERE)
                  .append(PIP_FIELD_OBJECTOID).append(EQUALS).append(PI_TABLE_NAME).append(DOT).append(PI_FIELD_OID);

         // select all pi oids where propertiesAvailable is null
         final FieldInfo piOidColumn
            = new FieldInfo(PI_FIELD_OID, Long.TYPE);
         final String selectOidByNullPropSql = DatabaseHelper.getSelectBasedOnNullCriteriaSql(item,
               PI_TABLE_NAME, propAvailableColumn, nullReplaceFunction, piOidColumn);

         // All PIs which have any properties
         StringBuffer selectPiWithPropCmd = new StringBuffer();
         selectPiWithPropCmd.append(selectOidByNullPropSql);
         selectPiWithPropCmd.append(AND).append(existsFragment(subselectPiPropCmd));

         // All PIs which still containing NULL
         StringBuffer selectPiWithoutPropCmd = new StringBuffer();
         selectPiWithoutPropCmd.append(selectOidByNullPropSql);

         StringBuffer updateCmd = new StringBuffer()
               .append(UPDATE).append(piTableName)
               .append(SET).append(PI_FIELD_PROPERTIES_AVAILABLE).append(EQUAL_PLACEHOLDER)
               .append(WHERE).append(PI_FIELD_OID).append(EQUAL_PLACEHOLDER);

         Connection connection = item.getConnection();
         updateRowsStmt = DatabaseHelper.prepareLoggingStatement(connection, updateCmd.toString());

         selectRowsStmt = DatabaseHelper.prepareLoggingStatement(connection, selectPiWithPropCmd.toString());
         updateProcessInstanceTable(ANY_PROPERTY_AVAILABLE, selectRowsStmt,
               updateRowsStmt, connection);

         selectRowsStmt = DatabaseHelper.prepareLoggingStatement(connection, selectPiWithoutPropCmd.toString());
         updateProcessInstanceTable(NO_PROPERTY_AVAILABLE, selectRowsStmt,
               updateRowsStmt, connection);

         info(PI_TABLE_NAME + " table upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectRowsStmt);
         QueryUtils.closeStatement(updateRowsStmt);
      }
   }

   /**
    * @param ANY_PROPERTY_AVAILABLE
    * @param selectRowsStmt
    * @param updateRowsStmt
    * @param connection
    * @param rowCounter
    * @param pendingRows
    * @throws SQLException
    */
   private int updateProcessInstanceTable(final int propAvailableFlags,
         final PreparedStatement selectRowsStmt, final PreparedStatement updateRowsStmt,
         final Connection connection) throws SQLException
   {
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

                  updateRowsStmt.setInt(1, propAvailableFlags);
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

            if (!eating)
            {
               break;
            }

            updateRowsStmt.executeBatch();
            connection.commit();

            info(MessageFormat.format("Committing updates of field {1} on table {0}"
                  + " after {2} rows.", new Object[] {
                  PI_TABLE_NAME, PI_FIELD_PROPERTIES_AVAILABLE, new Integer(rowCounter)}));
         }
      }
      finally
      {
         QueryUtils.closeResultSet(pendingRows);
      }

      return rowCounter;
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

   private static class DeployedDataInfo
   {
      private String dataId;
      private Long dataOid;
      private Long modelOid;

      public DeployedDataInfo(String dataId, Long dataOid, Long modelOid)
      {
         this.dataId = dataId;
         this.dataOid = dataOid;
         this.modelOid = modelOid;
      }

      public String getDataId()
      {
         return dataId;
      }

      public Long getDataOid()
      {
         return dataOid;
      }

      public Long getModelOid()
      {
         return modelOid;
      }
   }

}
