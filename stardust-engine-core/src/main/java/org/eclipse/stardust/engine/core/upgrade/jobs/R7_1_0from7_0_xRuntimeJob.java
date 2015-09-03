/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.DerbyDbDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.OracleDbDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.UpdateColumnInfo;

public class R7_1_0from7_0_xRuntimeJob extends DbmsAwareRuntimeUpgradeJob
{

   private int batchSize = 500;

   // Structured Data Table
   private static final String SD_TABLE = "structured_data";
   private static final String SD_ALIAS = "sd";
   private static final String SD_OID = "oid";
   private static final String SD_DATA = "data";
   private static final String SD_MODEL = "model";

   // Structured Data Value Table
   private static final String SDV_TABLE = "structured_data_value";
   private static final String SDV_OID = "oid";
   private static final String SDV_STRING_VALUE = "string_value";
   private static final String SDV_DOUBLE_VALUE = "double_value";

   // tmp table
   private static final String TMP_XPATH_DEC_TABLE = "tmp_xpath_dec";
   private static final String TMP_XPATH_DEC_OID = "oid";
   private static final String TMP_XPATH_DEC_MODEL = "model";

   // Data Value Table
   private static final String DV_TABLE = "data_value";
   private static final String DV_STRING_VALUE = "string_value";
   private static final String DV_DOUBLE_VALUE = "double_value";

   // Model Table
   private static final String M_TABLE = "model";

   // Process Instance Table
   private static final String PI_TABLE = "process_instance";

   private static final String IGNORE_MISSING_XPATH
   = "Infinty.RTUpgrade.7_1_0.IgnoreMissingXPath";

   private static final Logger trace = LogManager
         .getLogger(R7_1_0from7_0_xRuntimeJob.class);

   private RuntimeUpgradeTaskExecutor upgradeTaskExecutor;

   private UpgradeObserver observer;

   private static final Version VERSION = Version.createFixedVersion(7, 1, 0);

   protected R7_1_0from7_0_xRuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE, DBMSKey.MSSQL8,
            DBMSKey.MYSQL_SEQ});
      observer = this;
      String bs = Parameters.instance().getString(RuntimeUpgrader.UPGRADE_BATCH_SIZE);
      if (bs != null)
      {
         batchSize = Integer.parseInt(bs);
      }
   }

   @Override
   public UpgradableItem run(UpgradableItem item, boolean recover)
         throws UpgradeException
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R7_1_0from7_0_xRuntimeJob",
            Parameters.instance().getBoolean(RuntimeUpgrader.UPGRADE_DATA, false));
      initUpgradeTasks((RuntimeItem) item);
      return super.run(item, recover);
   }

   private void initUpgradeTasks(RuntimeItem item)
   {
      initUpgradeSchemaTasks(item);
      initMigrateDataTasks(item);
      initFinalizeSchemaTasks(item);
   }

   private void initUpgradeSchemaTasks(final RuntimeItem item)
   {
      //oid & double value are both the same for table data_value and
      //structured_data_value
      FieldInfo oidColumn = new FieldInfo(SDV_OID, Long.TYPE);
      FieldInfo doubleValueColumn = new FieldInfo(SDV_DOUBLE_VALUE, Double.TYPE);

      upgradeTaskExecutor
            .addUpgradeSchemaTask(getDoubleValueUpgradeTask(DV_TABLE, oidColumn, doubleValueColumn));
      upgradeTaskExecutor
            .addUpgradeSchemaTask(getDoubleValueUpgradeTask(SDV_TABLE, oidColumn, doubleValueColumn));

      upgradeTaskExecutor.addUpgradeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            createTmpTable();
         }

         @Override
         public void printInfo()
         {
            // TODO Auto-generated method stub
         }
      });
   }

   private void createTmpTable()
   {
      DatabaseHelper.createTable(item, new CreateTableInfo(TMP_XPATH_DEC_TABLE)
      {
         private final FieldInfo OID = new FieldInfo(TMP_XPATH_DEC_OID, Long.TYPE, 0, false);
         private final FieldInfo MODEL = new FieldInfo(TMP_XPATH_DEC_MODEL, Long.TYPE, 0, false);

         private final IndexInfo IDX1 = new IndexInfo("tmp_idx1", true, new FieldInfo[] {OID, MODEL});

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {OID, MODEL};
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

   private UpgradeTask getDoubleValueUpgradeTask(final String tableName,
         final FieldInfo oidColumn, final FieldInfo doubleValueColumn)
   {
      return new UpgradeTask()
      {
         @Override
         public void execute()
         {
            DatabaseHelper.alterTable(item, new AlterTableInfo(tableName)
            {
               @Override
               public FieldInfo[] getAddedFields()
               {
                  return new FieldInfo[] { doubleValueColumn };
               }
            }, observer);
         }

         @Override
         public void printInfo()
         {
            // TODO Auto-generated method stub
         }
      };
   }

   private void initFinalizeSchemaTasks(final RuntimeItem item)
   {
      upgradeTaskExecutor.addFinalizeSchemaTask(new UpgradeTask()
      {
         @Override
         public void execute()
         {
            DatabaseHelper.dropTable(item, new DropTableInfo(TMP_XPATH_DEC_TABLE, null),
                  R7_1_0from7_0_xRuntimeJob.this);

         }

         @Override
         public void printInfo()
         {
            // TODO Auto-generated method stub
         }
      });
   }

   private void initMigrateDataTasks(RuntimeItem item)
   {
      //oid & double value are both the same for table data_value and
      //structured_data_value
      final FieldInfo oidColumn = new FieldInfo(SDV_OID, Long.TYPE);
      final FieldInfo doubleValueColumn = new FieldInfo(SDV_DOUBLE_VALUE, Double.TYPE);

      upgradeTaskExecutor.addMigrateDataTask(initStringValueToDoubleValueTask(DV_TABLE,
            oidColumn, doubleValueColumn));
      upgradeTaskExecutor.addMigrateDataTask(initStringValueToDoubleValueTask(SDV_TABLE,
            oidColumn, doubleValueColumn));

      upgradeTaskExecutor.addMigrateDataTask(migrateStringValueToDoubleValueTask());
   }

   private void prepareMigrateStringValueToDoubleValue()
   {
      DBDescriptor dbDescriptor = item.getDbDescriptor();
      if(dbDescriptor instanceof OracleDbDescriptor)
      {
         //set the decimal delimiter for number conversions
         String setDelemiterStatement =
            "ALTER SESSION SET NLS_NUMERIC_CHARACTERS='.,'";
         try
         {
            DatabaseHelper.executeQuery(item, setDelemiterStatement);
         }
         catch (SQLException e1)
         {
            throw new PublicException(
                  BpmRuntimeError.JDBC_PREPARATION_OF_STRING_TO_VALUE_MIGRATION_FAILED
                        .raise(),
                  e1);
         }
      }

   }

   private UpgradeTask initStringValueToDoubleValueTask(final String tableName,
         final FieldInfo oidColumn, final FieldInfo doubleValueColumn)
   {
      return new UpgradeTask()
      {
         @Override
         public void execute()
         {

            final UpdateColumnInfo updateColumnInfo = new UpdateColumnInfo(
                  doubleValueColumn, "0.0");

            try
            {
               //check javadoc for further information on this method
               DatabaseHelper.setColumnValuesInBatch(item, tableName, oidColumn,
                     batchSize, updateColumnInfo);
            }
            catch (SQLException e)
            {
               reportExeption(e, "Could not initialize new column " + tableName + "."
                     + doubleValueColumn.getName() + ".");
            }
         }

         @Override
         public void printInfo()
         {
            // TODO Auto-generated method stub
            
         }
      };
   }

   private UpgradeTask migrateStringValueToDoubleValueTask()
   {
      return new UpgradeTask()
      {
         @Override
         public void execute()
         {
            prepareMigrateStringValueToDoubleValue();

            Set<PartitionInfo> partitions = getPartitionsFromDb();
            for (PartitionInfo partitionInfo : partitions)
            {
               upgradeDoubleValuesByPartition(partitionInfo);
            }
         }

         @Override
         public void printInfo()
         {
            // TODO Auto-generated method stub
         }
      };
   }

   private boolean isDecimalCandidate(IData theData, long xpathOid)
   {
      boolean tryParseDouble = true;

      try
      {
         if (theData != null)
         {
            IXPathMap xPathMap = DataXPathMap.getXPathMap(theData);
            TypedXPath typedXPath = xPathMap.getXPath(xpathOid);
            String xsdTypeName = typedXPath.getXsdTypeName();
            tryParseDouble = "decimal".equals(xsdTypeName);
         }
      }
      catch (Exception e)
      {
         boolean ignoreXsdErrors = Parameters.instance().getBoolean(IGNORE_MISSING_XPATH,
               false);

         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("Could not analyse structured data: ");
         errorMsg.append(theData.getId());
         errorMsg.append(" for xpath oid ");
         errorMsg.append(xpathOid);

         if (ignoreXsdErrors)
         {
            tryParseDouble = false;

            errorMsg.append(" - ignoring record.");
            trace.warn(errorMsg.toString());
         }
         else
         {
            throw new PublicException(
                  BpmRuntimeError.SDT_COULD_NOT_ANALYSE_STRUCTURED_DATA_FOR_XPATH_OID.raise(
                        theData.getId(), xpathOid), e);
         }
      }

      return tryParseDouble;
   }


   private String createInsertTmpXpathDecimalStmnt()
   {
      StringBuilder insertSql = new StringBuilder();
      insertSql.append(INSERT_INTO)
            .append(DatabaseHelper.getQualifiedName(TMP_XPATH_DEC_TABLE))
            .append(BRACKET_OPEN)
               .append(TMP_XPATH_DEC_OID).append(COMMA).append(TMP_XPATH_DEC_MODEL)
            .append(BRACKET_CLOSE)
            .append(VALUES)
            .append(BRACKET_OPEN)
               .append(PLACEHOLDER).append(COMMA).append(PLACEHOLDER)
            .append(BRACKET_CLOSE);
      return insertSql.toString();
   }

   private String createUpdateDecStructValStmnt()
   {
      StringBuilder updateSql = new StringBuilder();
      updateSql.append(UPDATE)
            .append(DatabaseHelper.getQualifiedName(SDV_TABLE))
            .append(SET).append(SDV_DOUBLE_VALUE).append(EQUAL_PLACEHOLDER)
            .append(WHERE)
            .append(SDV_OID).append(EQUAL_PLACEHOLDER);
      return updateSql.toString();
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
   }

   @Override
   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      upgradeTaskExecutor.executeFinalizeSchemaTasks();
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
      info("A new column 'double_value' will be created in table 'data_value'.");
      info("A new column 'double_value' will be created in table 'structured_data_value'.");
   }

   @Override
   protected void printMigrateDataInfo()
   {
      info("Values stored in column 'string_data' in table 'data_value' will be added to "
            + "new column 'double_value' if 'type_key' is 'BigData.FLOAT' or 'BigData.DOUBLE'");
      info("Values stored in column 'string_data' in table 'structured_data_value' will be added to "
            + "new column 'double_value' if 'type_key' is 'BigData.FLOAT' or 'BigData.DOUBLE'");
      info("Values stored in column 'string_data' in table 'structured_data_value' will be added to "
            + "new column 'double_value' if 'xsd type name' is 'decimal'");
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {}

   @Override
   public Version getVersion()
   {
      return VERSION;
   }

   private String getStringToDoubleValueStmt(String doubleFieldName, String stringFieldName)
   {
      DBDescriptor dbDescriptor = item.getDbDescriptor();
      StringBuilder builder = new StringBuilder();
      builder.append(doubleFieldName);
      builder.append(EQUALS);
      //derby needs special handling when converting from varchar to double
      //convert to decimal explicit and the to double implicit
      if(dbDescriptor instanceof DerbyDbDescriptor)
      {
         builder.append("CAST(");
         builder.append(stringFieldName);
         builder.append(" AS DECIMAL)");
      }
      else
      {
         builder.append(stringFieldName);
      }

      return builder.toString();
   }

   private void runUpdateDataValueStmnt(String tableName, String doubleFieldName,
         String stringFieldName, final PartitionInfo partitionInfo)
   {
      StringBuilder updateDataValueStmt = new StringBuilder();
      updateDataValueStmt
            .append(UPDATE)
               .append(DatabaseHelper.getQualifiedName(tableName))
            .append(SET)
               .append(getStringToDoubleValueStmt(doubleFieldName, stringFieldName))
            .append(WHERE)
               .append("(type_key").append(EQUALS).append(BigData.DOUBLE)
               .append(OR)
               .append("type_key").append(EQUALS).append(BigData.FLOAT).append(")")
            .append(AND).append(EXISTS)
            .append("(SELECT 0")
            .append(FROM).append(DatabaseHelper.getQualifiedName(PI_TABLE, "pi"))
            .append(INNER_JOIN).append(DatabaseHelper.getQualifiedName(M_TABLE, "m"))
               .append(ON).append("(m.oid = pi.model)")
            .append(WHERE)
               .append("pi.oid").append(EQUALS).append(DatabaseHelper.getQualifiedName(tableName)).append(DOT).append("processInstance")
               .append(AND).append("m.partition").append(EQUALS).append(partitionInfo.getOid()).append(")");
      try
      {
         item.executeDdlStatement(updateDataValueStmt.toString(), false);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + tableName + "."
               + doubleFieldName + ".");
      }
   }

   /*
    * Copy of code from class DmlManager which handles correct settings for double values.
    */
   private static void setSqlValue(PreparedStatement statement, int index,
         DBDescriptor dbDescriptor, Double value) throws SQLException
   {
      double doubleValue = value.doubleValue();

      if (doubleValue == Unknown.DOUBLE)
      {
         statement.setNull(index, java.sql.Types.DOUBLE);
      }
      else
      {
         Pair<Double, Double> valueBorders = dbDescriptor
               .getNumericSQLTypeValueBorders(Double.class);
         if (doubleValue < valueBorders.getFirst())
         {
            doubleValue = valueBorders.getFirst();
         }
         else if (doubleValue > valueBorders.getSecond())
         {
            doubleValue = valueBorders.getSecond();
         }
         else
         {
            Pair<Double, Double> valueEpsilonBorders = dbDescriptor
                  .getNumericSQLTypeEpsilonBorders(Double.class);
            if (doubleValue > valueEpsilonBorders.getFirst() && doubleValue < 0)
            {
               doubleValue = 0.0;
            }
            else if (doubleValue < valueEpsilonBorders.getSecond() && doubleValue > 0)
            {
               doubleValue = 0.0;
            }
         }
         statement.setDouble(index, doubleValue);
      }
   }

   private void upgradeDoubleValuesByPartition(final PartitionInfo partitionInfo)
   {
      runUpdateDataValueStmnt(DV_TABLE,
            DV_DOUBLE_VALUE, DV_STRING_VALUE,
            partitionInfo);
      runUpdateDataValueStmnt(SDV_TABLE,
            SDV_DOUBLE_VALUE,
            SDV_STRING_VALUE, partitionInfo);

      Utils.initCarnotEngine(partitionInfo.getId(), getRtJobEngineProperties());

      try
      {
         prepareTmpTableForPartition(partitionInfo);

         PreparedStatement updateStmnt = item.getConnection().prepareStatement(
               createUpdateDecStructValStmnt());
         StringBuilder structOidStmnt = createFetchSdvDecimalCandidates();

         ResultSet resultSet = null;
         try
         {
            resultSet = DatabaseHelper.executeQuery(item,
                  structOidStmnt.toString());

            int batchCounter = 0;
            while (resultSet.next())
            {
               long structValOid = resultSet.getLong(1);
               String value = resultSet.getString(2);

               Double decimalValue = Unknown.DOUBLE;
               if (value != null)
               {
                  try
                  {
                     decimalValue = Double.parseDouble(value);
                  }
                  catch (NumberFormatException x)
                  {
                     trace.warn(
                           MessageFormat
                                 .format(
                                       "Value {0} for SDV with oid {1} cannot be converted. Will be ignored.",
                                       new Object[] { value, structValOid }), x);
                  }
               }

               setSqlValue(updateStmnt, 1, item.getDbDescriptor(), decimalValue);
               updateStmnt.setLong(2, structValOid);
               updateStmnt.addBatch();
               ++batchCounter;

               if (batchCounter >= batchSize)
               {
                  batchCounter = 0;
                  updateStmnt.executeBatch();
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
         reportExeption(e, "Could not update double value.");
      }
   }

   private void prepareTmpTableForPartition(final PartitionInfo partitionInfo)
         throws SQLException
   {
      // First clean-up this table as it might be filled with data
      // from another partition or previously failed run
      StringBuffer deleteCmd = new StringBuffer();
      deleteCmd.append(DELETE_FROM).append(DatabaseHelper.getQualifiedName(TMP_XPATH_DEC_TABLE));
      DatabaseHelper.executeUpdate(item, deleteCmd.toString());

      // add xpath oids to tmp table for all xpaths which are decimal
      // these will be used later in order to reduce result set of next query used for updates
      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      final String sdStmnt = createSelectSdForPartition(partitionInfo);
      ResultSet resultSet = DatabaseHelper.executeQuery(item, sdStmnt);

      PreparedStatement insertStmnt = item.getConnection().prepareStatement(
            createInsertTmpXpathDecimalStmnt());
      try
      {
         while (resultSet.next())
         {
            long xpathOid = resultSet.getLong(1);
            long data = resultSet.getLong(2);
            long modelOid = resultSet.getLong(3);

            IData theData = modelManager.findData(modelOid, data);
            if (isDecimalCandidate(theData, xpathOid))
            {
               insertStmnt.setLong(1, xpathOid);
               insertStmnt.setLong(2, modelOid);
               insertStmnt.addBatch();
            }
         }

         insertStmnt.executeBatch();

         // is this wise / necessary?
         item.commit();
      }
      finally
      {
         insertStmnt.close();
         resultSet.close();
      }
   }

   private String createSelectSdForPartition(final PartitionInfo partitionInfo)
   {
      StringBuilder sdStmnt = new StringBuilder();

      sdStmnt
         .append(SELECT)
         .append(DatabaseHelper.getQualifiedColName(SD_ALIAS, SD_OID)).append(COMMA)
         .append(DatabaseHelper.getQualifiedColName(SD_ALIAS, SD_DATA)).append(COMMA)
         .append(DatabaseHelper.getQualifiedColName(SD_ALIAS, SD_MODEL))
         .append(FROM).append(DatabaseHelper.getQualifiedName(SD_TABLE, SD_ALIAS))
         .append(INNER_JOIN).append(DatabaseHelper.getQualifiedName(M_TABLE, "m"))
         // TODO: use constants.
         .append(ON).append("(m.oid = sd.model)")
         .append(WHERE).append("m.partition").append(EQUALS).append(partitionInfo.getOid());

      return sdStmnt.toString();
   }

   private StringBuilder createFetchSdvDecimalCandidates()
   {
      StringBuilder structOidStmnt = new StringBuilder();
      structOidStmnt
            .append(SELECT)
               .append(DatabaseHelper.getQualifiedColName("sdv", SDV_OID)).append(COMMA)
               .append(SDV_STRING_VALUE)
            .append(FROM)
               .append(DatabaseHelper.getQualifiedName(SDV_TABLE, "sdv"))
            .append(INNER_JOIN).append(DatabaseHelper.getQualifiedName(PI_TABLE, "pi"))
               .append(ON).append("(sdv.processInstance = pi.oid)")
            .append(INNER_JOIN).append(DatabaseHelper.getQualifiedName(TMP_XPATH_DEC_TABLE, "tmp"))
               // TODO: use constants
               .append(ON).append("(tmp.oid = sdv.xpath AND tmp.model = pi.model)");

      return structOidStmnt;
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
