/*******************************************************************************
 * Copyright (c) 2012, 2013 SunGard CSA LLC and others.
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
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.DerbyDbDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.OracleDbDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.upgrade.framework.*;

public class R7_1_0from7_0_xRuntimeJob extends DbmsAwareRuntimeUpgradeJob
{

   // Structured Data Value Table
   private static final String SDV_TABLE = "structured_data_value";
   private static final String SDV_OID = "oid";
   private static final String SDV_TYPE_KEY = "type_key";
   private static final String SDV_PROCESS_INSTANCE = "processInstance";
   private static final String SDV_XPATH = "xpath";
   private static final String SDV_STRING_VALUE = "string_value";
   private static final String SDV_DOUBLE_VALUE = "double_value";

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

   private void initUpgradeSchemaTasks(RuntimeItem item)
   {
      upgradeTaskExecutor
            .addUpgradeSchemaTask(addDoubleValueColumnToDataValueUpgradeTask(DV_TABLE,
                  DV_DOUBLE_VALUE));
      upgradeTaskExecutor
            .addUpgradeSchemaTask(addDoubleValueColumnToDataValueUpgradeTask(SDV_TABLE,
                  SDV_DOUBLE_VALUE));
   }

   private UpgradeTask addDoubleValueColumnToDataValueUpgradeTask(final String tableName,
         final String fieldName)
   {
      return new UpgradeTask()
      {
         @Override
         public void execute()
         {
            DatabaseHelper.alterTable(item, new AlterTableInfo(tableName)
            {
               private final FieldInfo DOUBLE_VALUE = new FieldInfo(fieldName,
                     Double.TYPE);

               @Override
               public FieldInfo[] getAddedFields()
               {
                  return new FieldInfo[] {DOUBLE_VALUE};
               }
            }, observer);

            try
            {
               String initStmnt = "UPDATE " + DatabaseHelper.getQualifiedName(tableName)
                     + " SET " + fieldName + "=0.0";
               item.executeDdlStatement(initStmnt, false);
            }
            catch (SQLException e)
            {
               reportExeption(e, "Could not initialize new column " + tableName + "."
                     + fieldName + ".");
            }
         }
      };
   }

   private void initFinalizeSchemaTasks(RuntimeItem item)
   {}

   private void initMigrateDataTasks(RuntimeItem item)
   {
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
            throw new PublicException("preparation of string to value migration failed", e1);
         }
      }

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
      };
   }

   private Double getDecimalValue(IData theData, long xpathOid, String value)
   {
      try
      {
         boolean tryParseDouble = false;
         if (theData != null)
         {
            IXPathMap xPathMap = DataXPathMap.getXPathMap(theData);
            TypedXPath typedXPath = xPathMap.getXPath(xpathOid);
            String xsdTypeName = typedXPath.getXsdTypeName();
            tryParseDouble = "decimal".equals(xsdTypeName);
         }
         else
         {
            tryParseDouble = true;
         }

         if (tryParseDouble)
         {
            try
            {
               return Double.parseDouble(value);
            }
            catch (NumberFormatException x)
            {
            }
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
            errorMsg.append(" - ignoring record.");
            trace.warn(errorMsg.toString());
         }
         else
         {
            throw new PublicException(errorMsg.toString(), e);
         }
      }

      return null;
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
   {}

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

   private void upgradeDoubleValuesByPartition(final PartitionInfo partitionInfo)
   {
      runUpdateDataValueStmnt(DV_TABLE,
            DV_DOUBLE_VALUE, DV_STRING_VALUE,
            partitionInfo);
      runUpdateDataValueStmnt(SDV_TABLE,
            SDV_DOUBLE_VALUE,
            SDV_STRING_VALUE, partitionInfo);

      Utils.initCarnotEngine(partitionInfo.getId(), getRtJobEngineProperties());

      StringBuilder structOidStmnt = new StringBuilder();
      structOidStmnt
            .append(SELECT)
               .append("sdv.").append(SDV_OID).append(COMMA)
               .append(SDV_XPATH).append(COMMA)
               .append(SDV_PROCESS_INSTANCE).append(COMMA)
               .append(SDV_STRING_VALUE)
            .append(FROM)
               .append(DatabaseHelper.getQualifiedName(SDV_TABLE, "sdv"))
            .append(INNER_JOIN).append(DatabaseHelper.getQualifiedName(PI_TABLE, "pi"))
               .append(ON).append("(sdv.processInstance = pi.oid)")
            .append(INNER_JOIN).append(DatabaseHelper.getQualifiedName(M_TABLE, "m"))
               .append(ON).append("(m.oid = pi.model)")
            .append(WHERE)
               .append(SDV_TYPE_KEY).append(EQUALS).append(BigData.STRING)
               .append(AND).append("m.partition").append(EQUALS).append(partitionInfo.getOid());
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      Map<Long, Double> structValMap = CollectionUtils.newMap();
      try
      {
         ResultSet resultSet = DatabaseHelper.executeQuery(item,
               structOidStmnt.toString());
         while (resultSet.next())
         {
            long structValOid = resultSet.getLong(1);
            long xpathOid = resultSet.getLong(2);
            long piOid = resultSet.getLong(3);
            String value = resultSet.getString(4);
            long modelOid = ProcessInstanceBean.findByOID(piOid)
                  .getProcessDefinition().getModel().getModelOID();

            IData theData = modelManager.findDataForStructuredData(modelOid, xpathOid);
            Double decimalValue = getDecimalValue(theData, xpathOid, value);
            if(decimalValue != null)
            {
               structValMap.put(structValOid, decimalValue);
            }
         }

         PreparedStatement updateStmnt = item.getConnection().prepareStatement(
               createUpdateDecStructValStmnt());
         for (Long structValOid : structValMap.keySet())
         {
            updateStmnt.setDouble(1, structValMap.get(structValOid));
            updateStmnt.setLong(2, structValOid);
            updateStmnt.addBatch();
         }

         updateStmnt.executeBatch();
         updateStmnt.close();
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update double value.");
      }
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
