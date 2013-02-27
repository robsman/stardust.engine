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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;
import org.eclipse.stardust.engine.core.upgrade.framework.*;

public class R7_1_0from7_0_xRuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
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
            .addUpgradeSchemaTask(addDoubleValueColumnToDataValueUpgradeTask(
                  DataValueBean.TABLE_NAME, DataValueBean.FIELD__DOUBLE_VALUE));
      upgradeTaskExecutor
            .addUpgradeSchemaTask(addDoubleValueColumnToDataValueUpgradeTask(
                  StructuredDataValueBean.TABLE_NAME,
                  StructuredDataValueBean.FIELD__DOUBLE_VALUE));
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

   private UpgradeTask migrateStringValueToDoubleValueTask()
   {
      return new UpgradeTask()
      {
         @Override
         public void execute()
         {
            runUpdateDataValueStmnt(DataValueBean.TABLE_NAME,
                  DataValueBean.FIELD__DOUBLE_VALUE, DataValueBean.FIELD__STRING_VALUE);
            runUpdateDataValueStmnt(StructuredDataValueBean.TABLE_NAME,
                  StructuredDataValueBean.FIELD__DOUBLE_VALUE,
                  StructuredDataValueBean.FIELD__STRING_VALUE);

            String partition = ParametersFacade.instance().getString(
                  SecurityProperties.DEFAULT_PARTITION,
                  PredefinedConstants.DEFAULT_PARTITION_ID);
            Utils.initCarnotEngine(partition);

            StringBuilder structOidStmnt = new StringBuilder();
            structOidStmnt
                  .append("SELECT ")
                  .append(StructuredDataValueBean.FIELD__OID)
                  .append(", ")
                  .append(StructuredDataValueBean.FIELD__XPATH)
                  .append(", ")
                  .append(StructuredDataValueBean.FIELD__PROCESS_INSTANCE)
                  .append(", ")
                  .append(StructuredDataValueBean.FIELD__STRING_VALUE)
                  .append(" FROM ")
                  .append(
                        DatabaseHelper
                              .getQualifiedName(StructuredDataValueBean.TABLE_NAME))
                  .append(" WHERE ").append(StructuredDataValueBean.FIELD__TYPE_KEY)
                  .append("=").append(BigData.STRING);
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
                  IData theData = modelManager.findDataForStructuredData(modelOid,
                        xpathOid);
                  IXPathMap xPathMap = DataXPathMap.getXPathMap(theData);
                  TypedXPath typedXPath = xPathMap.getXPath(xpathOid);
                  String xsdTypeName = typedXPath.getXsdTypeName();
                  boolean tryParseDouble = "decimal".equals(xsdTypeName);
                  if (tryParseDouble)
                  {
                     try
                     {
                        // As xsd:decimal is stored as string it is tried to convert to
                        // double
                        structValMap.put(structValOid, Double.parseDouble(value));
                     }
                     catch (NumberFormatException x)
                     {
                        trace.debug("Cannot parse as a double: " + value, x);
                        structValMap.put(structValOid, 0.0);
                     }
                  }
               }

               Statement updateStmnt = item.getConnection().createStatement();
               for (Long structValOid : structValMap.keySet())
               {
                  updateStmnt.addBatch(createUpdateDecStructValStmnt(structValOid,
                        structValMap.get(structValOid)));
               }
               updateStmnt.executeBatch();
               updateStmnt.close();
            }
            catch (SQLException e)
            {
               reportExeption(e, "Could not update double value.");
            }
         }
      };
   }

   protected String createUpdateDecStructValStmnt(Long structValOid, Double value)
   {
      StringBuilder updateSql = new StringBuilder();
      updateSql.append("UPDATE ")
            .append(DatabaseHelper.getQualifiedName(StructuredDataValueBean.TABLE_NAME))
            .append(" SET ").append(StructuredDataValueBean.FIELD__DOUBLE_VALUE)
            .append(" = ").append(value).append(" WHERE ")
            .append(StructuredDataValueBean.FIELD__OID).append(" = ")
            .append(structValOid);
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

   private void runUpdateDataValueStmnt(String tableName, String doubleFieldName,
         String stringFieldName)
   {
      StringBuilder updateDataValueStmt = new StringBuilder();
      updateDataValueStmt.append("UPDATE ")
            .append(DatabaseHelper.getQualifiedName(tableName)).append(" SET ")
            .append(doubleFieldName).append("=").append(stringFieldName)
            .append(" WHERE type_key=").append(BigData.DOUBLE).append(" OR type_key=")
            .append(BigData.FLOAT);
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

}
