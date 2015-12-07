/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.IndexDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.OracleDbDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.IndexInfo;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.LoggingPreparedStatement;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.NVLFunction;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.UpdateColumnInfo;


/**
 * @author Sebastian Woelk
 * @version $Revision$
 */
public final class DatabaseHelper
{
   public static final long OID_UNDEFINED = -1;


   public enum ColumnNameModificationMode
   {
      UPPER_CASE,
      LOWER_CASE,
      NONE
   }

   public enum AlterMode
   {
      ALL,
      ADDED_COLUMNS_ONLY,
      ADDED_COLUMNS_IGNORED,
   }


   private static final Logger trace = LogManager.getLogger(DatabaseHelper.class);

   /**
    * An error code of 1430 is returned if one try to add a column to a table that already
    * exists.
    */
   public static final int ORACLE_ERROR_COLUMN_EXISTS = 1430;

   /**
    * Oracle reports error code 904 if one try to drop or alter a column that
    * does not exist.
    */
   public static final int ORACLE_ERROR_INVALID_COLUMN = 904;

   /**
    * Oracle reports an error code 942 if one try to use a table name in an sql
    * statement that does not exists.
    */
   public static final int ORACLE_ERROR_TABLE_NOT_EXIST = 942;


   public static ColumnNameModificationMode columnNameModificationMode
      = ColumnNameModificationMode.UPPER_CASE;

   /**
    * <p>Creates a table based on the passed {@link TableInfo} object.</p>
    *
    * <p>The connection to execute the command is obtained from the passed
    * {@link RuntimeItem}.</p>
    *
    * @param item the {@link RuntimeItem} which contains the database
    * connection reference.
    * @param tableInfo the {@link TableInfo} object which represents the table
    * to create.
    */
   public static void createTable(RuntimeItem item, TableInfo tableInfo)
         throws SQLException
   {
      executeDdlStatement(
            item,
            "create table " + getQualifiedName(tableInfo.getTableName()) + "("
            + tableInfo.getTableDefinition() + ")");
   }

   /**
    * Updates multiple columns using batching.
    * That means update are issued in blocks based on
    * the {@code batchSize} and using the {@code oidColumn}
    * as block building criteria. This method also checks if
    * the database supports multi column updates or not based on
    * the {@code item}. The passed {@code tableName} will also be qualified
    * within this method(this means the schema name is appended if necessary)
    *
    * @param item - the runtime item
    * @param tableName - the table on which to update, must contain NO schema name
    * @param oidColumn - the oid column of type Long
    * @param batchSize - the batchsize after each block will be committed
    * @param updateColumnInfos - the columns to update and their value
    * @throws SQLException if any exception occur during updating
    */
   public static void setColumnValuesInBatch(RuntimeItem item,
         String tableName,
         FieldInfo oidColumn,
         int batchSize,
         UpdateColumnInfo...updateColumnInfos) throws SQLException
   {
      String qualifiedTableName = DatabaseHelper.getQualifiedName(tableName);
      long pkMin = getMinOid(item, qualifiedTableName, oidColumn.getName());
      long pkMax = getMaxOid(item, qualifiedTableName, oidColumn.getName());

      // check if the database supports multiple columns in update statement.
      DBDescriptor dbDescriptor = item.getDbDescriptor();
      if(dbDescriptor != null && dbDescriptor.supportsMultiColumnUpdates())
      {
         internalSetColumnValuesInBatch(item, tableName, oidColumn, batchSize, pkMin, pkMax, updateColumnInfos);
      }
      else
      {
         for(UpdateColumnInfo updateColumnInfo: updateColumnInfos)
         {
            internalSetColumnValuesInBatch(item, tableName, oidColumn, batchSize, pkMin, pkMax, updateColumnInfo);
         }
      }
   }

   private static void internalSetColumnValuesInBatch(RuntimeItem item,
         String tableName,
         FieldInfo oidColumn,
         int batchSize,
         long pkMin,
         long pkMax,
         UpdateColumnInfo...updateColumnInfos) throws SQLException
   {
      //at least one record was found
      if(pkMin != OID_UNDEFINED)
      {
         StringBuffer updateTableSql = new StringBuffer(500);
         updateTableSql.append("UPDATE ").append(tableName);
         updateTableSql.append(" SET ");

         int columnCount = 0;
         for (UpdateColumnInfo updateColumnInfo : updateColumnInfos)
         {
            if (columnCount > 0)
            {
               updateTableSql.append(",");
            }

            updateTableSql.append(updateColumnInfo.getColumn().getName());
            updateTableSql.append(" = ");
            updateTableSql.append(updateColumnInfo.getValue());
            columnCount++;
         }
         updateTableSql.append(" WHERE ").append(oidColumn.getName());
         updateTableSql.append(" >= ?").append(" AND ");
         updateTableSql.append(oidColumn.getName()).append(" < ? ");

         PreparedStatement updateStatement
            = prepareLoggingStatement(item.getConnection(), updateTableSql.toString());

         long oidCursor = pkMin;
         while(oidCursor <= pkMax)
         {
            //lower border is inclusive
            long lowerOidBorder  = oidCursor;
            //upper border is exclusive
            long upperOidBorder = oidCursor + batchSize;

            updateStatement.setLong(1, lowerOidBorder);
            updateStatement.setLong(2, upperOidBorder);
            updateStatement.execute();

            //commit after each batch
            oidCursor = upperOidBorder;
            item.getConnection().commit();
         }
      }
   }

   private static long getMinOid(RuntimeItem item, String qualifiedTableName, String oidColumn) throws SQLException
   {
      StringBuffer selectPkMinSql = new StringBuffer();
      selectPkMinSql.append("Select MIN(").append(oidColumn);
      selectPkMinSql.append(") from ").append(qualifiedTableName);

      PreparedStatement selectPkMinStatement
         = prepareLoggingStatement(item.getConnection(), selectPkMinSql.toString());
      return getFirstOid(selectPkMinStatement);
   }

   public static long getMaxOid(RuntimeItem item, String qualifiedTableName, String oidColumn) throws SQLException
   {
      StringBuffer selectPkMaxSql = new StringBuffer();
      selectPkMaxSql.append("Select MAX(").append(oidColumn);
      selectPkMaxSql.append(") from ").append(qualifiedTableName);

      PreparedStatement selectPkMinStatement
         = prepareLoggingStatement(item.getConnection(), selectPkMaxSql.toString());
      return getFirstOid(selectPkMinStatement);
   }

   private static long getFirstOid(PreparedStatement ps) throws SQLException
   {
      ResultSet rs = null;
      try {
         rs = ps.executeQuery();
         boolean hasNext = rs.next();
         if(hasNext)
         {
            return rs.getLong(1);
         }
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }

      return OID_UNDEFINED;
   }

   /**
    * <p>Creates a table based on the passed {@link TableInfo} object.</p>
    *
    * <p>The connection to execute the command is obtained from the passed
    * {@link RuntimeItem}.</p>
    *
    * @param item the {@link RuntimeItem} which contains the database
    * connection reference.
    * @param tableInfo the {@link TableInfo} object which represents the table
    * to create.
    */
   public static void createTable(RuntimeItem item, CreateTableInfo tableInfo,
         UpgradeObserver observer)
   {
      String schema = getSchemaName();
      StringBuffer buffer = new StringBuffer(500);
      DBDescriptor dbDescriptor = item.getDbDescriptor();

      buffer.append("CREATE TABLE ");
      if (StringUtils.isNotEmpty(schema))
      {
         buffer.append(schema + ".");
      }
      buffer.append(tableInfo.getTableName()).append(" (");

      String delimiter = "";
      for (int i = 0; i < tableInfo.getFields().length; i++)
      {
         FieldInfo field = tableInfo.getFields()[i];
         buffer.append(delimiter).append(getColumnName(dbDescriptor, field)).append(" ");
         buffer.append(dbDescriptor.getSQLType(field.type, field.size));

         if (field.isPK && dbDescriptor.supportsIdentityColumns()
               && !StringUtils.isEmpty(tableInfo.getSequenceName()))
         {
            buffer.append(" ")
                  .append(dbDescriptor.getIdentityColumnQualifier());
         }

         if(!field.isPK && !dbDescriptor.isColumnNullableByDefault())
         {
            buffer.append(" NULL");
         }

         delimiter = ", ";
      }
      buffer.append(")");

      try
      {
         executeDdlStatement(item, buffer.toString());
      }
      catch (SQLException e)
      {
         // TODO handle DBMS specific error codes
         observer.warn("Couldn't create " + tableInfo.getTableName() + " table.", e);
      }

      if (null != tableInfo.getIndexes())
      {
         // creating indexes
         for (int i = 0; i < tableInfo.getIndexes().length; i++)
         {
            IndexInfo indexInfo = tableInfo.getIndexes()[i];
            try
            {
               createIndex(item, tableInfo, indexInfo);
            }
            catch (SQLException e)
            {
               // TODO handle DBMS specific error codes
               observer.warn("Couldn't create index " + indexInfo.name + " on table "
                     + tableInfo.getTableName() + " table.", e);
            }
         }
      }

      // optionally creating the PK sequence on databases requiring it
      if (dbDescriptor.supportsSequences()
            && !StringUtils.isEmpty(tableInfo.getSequenceName()))
      {
         try
         {
            // TODO schema name
            executeDdlStatement(item, dbDescriptor
                  .getCreatePKSequenceStatementString(schema, tableInfo.getSequenceName()));
         }
         catch (SQLException e)
         {
            // TODO handle DBMS specific error codes
            observer.warn("Couldn't create sequence " + tableInfo.getSequenceName()
                  + " for table " + tableInfo.getTableName() + ".", e);
         }
      }
   }

   public static void alterTableAddColumns(RuntimeItem item, AlterTableInfo tableInfo,
         UpgradeObserver observer)
   {

   }

   public static void alterTable(RuntimeItem item, AlterTableInfo tableInfo,
         UpgradeObserver observer)
   {
      alterTable(item, tableInfo, observer, AlterMode.ALL);
   }

   private static boolean canAddFields(AlterMode alterMode, AlterTableInfo tableInfo)
   {
      FieldInfo[] addedFields = tableInfo.getAddedFields();
      if(alterMode != AlterMode.ADDED_COLUMNS_IGNORED && addedFields != null
            && addedFields.length > 0)
      {
         return true;
      }

      return false;
   }

   public static void alterTable(RuntimeItem item, AlterTableInfo tableInfo,
         UpgradeObserver observer, AlterMode alterMode)
   {
      String schema = getSchemaName();
      DBDescriptor dbDescriptor = item.getDbDescriptor();
      if (canAddFields(alterMode, tableInfo))
      {
         for (int i = 0; i < tableInfo.getAddedFields().length; i++)
         {
            final FieldInfo field = tableInfo.getAddedFields()[i];

            StringBuffer buffer = new StringBuffer(400);

            buffer.append("ALTER TABLE ");
            if (StringUtils.isNotEmpty(schema))
            {
               buffer.append(schema + ".");
            }
            buffer.append(tableInfo.getTableName()).append(" ADD ").append(
                  getColumnName(dbDescriptor, field)).append(" ").append(
                  dbDescriptor.getSQLType(field.type, field.size));

            if(!field.isPK && !dbDescriptor.isColumnNullableByDefault())
            {
               buffer.append(" NULL");
            }

            try
            {
               executeDdlStatement(item, buffer.toString());
            }
            catch (SQLException e)
            {
               // TODO handle DBMS specific error codes
               observer.warn("Couldn't add attribute " + getColumnName(dbDescriptor, field) + " to table "
                     + tableInfo.getTableName() + ".", e);
            }
         }
      }

      if(alterMode == AlterMode.ADDED_COLUMNS_ONLY)
      {
         return;
      }

      if ((null != tableInfo.getModifiedFields()) && (0 < tableInfo.getModifiedFields().length))
      {
         for (int i = 0; i < tableInfo.getModifiedFields().length; i++)
         {
            final FieldInfo field = tableInfo.getModifiedFields()[i];

            StringBuffer buffer = new StringBuffer(400);

            buffer.append("ALTER TABLE ");
            if (StringUtils.isNotEmpty(schema))
            {
               buffer.append(schema + ".");
            }
            buffer.append(tableInfo.getTableName())
                  .append(" MODIFY (")
                  .append(getColumnName(dbDescriptor, field)).append(" ")
                  .append(dbDescriptor.getSQLType(field.type, field.size))
                  .append(")");

            try
            {
               executeDdlStatement(item, buffer.toString());
            }
            catch (SQLException e)
            {
               // TODO handle DBMS specific error codes
               observer.warn("Couldn't modify attribute " + getColumnName(dbDescriptor, field)
                     + " of table " + tableInfo.getTableName() + ".", e);
            }
         }
      }

      // drop obsolete indexes before dropping any columns
      if ((null != tableInfo.getDroppedIndexes())
            && (0 < tableInfo.getDroppedIndexes().length))
      {
         for (int i = 0; i < tableInfo.getDroppedIndexes().length; i++)
         {
            final IndexInfo index = tableInfo.getDroppedIndexes()[i];

            try
            {
               // TODO schema name
               executeDdlStatement(item, dbDescriptor.getDropIndexStatement(
                     schema, tableInfo.getTableName(), index.name));
            }
            catch (SQLException e)
            {
               // TODO handle DBMS specific error codes
               observer.warn("Couldn't drop index " + index.name + " for table "
                     + tableInfo.getTableName() + ".", e);
            }
         }
      }

      boolean performedDmlBeforeIndexCreation = false;
      // alter indexes
      if ((null != tableInfo.getAlteredIndexes())
            && (0 < tableInfo.getAlteredIndexes().length))
      {
         // first step: drop all indexes which have to be altered
         for (int i = 0; i < tableInfo.getAlteredIndexes().length; i++)
         {
            final IndexInfo index = tableInfo.getAlteredIndexes()[i];

            try
            {
               // TODO schema name
               executeDdlStatement(item, dbDescriptor.getDropIndexStatement(
                     schema, tableInfo.getTableName(), index.name));
            }
            catch (SQLException e)
            {
               // TODO handle DBMS specific error codes
               observer.warn("Couldn't drop index " + index.name + " for table "
                     + tableInfo.getTableName() + ".", e);
            }
         }

         // second step: do some necessary DML which for performance reasons has better to
         // be done before the to be altered indexes get recreated.
         try
         {
            tableInfo.executeDmlBeforeIndexCreation(item);
            performedDmlBeforeIndexCreation = true;
         }
         catch (SQLException e)
         {
            // TODO handle DBMS specific error codes
            observer.warn("Couldn't execute pre index creation DML for table "
                  + tableInfo.getTableName() + ".", e);
         }

         // third step: recreate indexes
         for (int i = 0; i < tableInfo.getAlteredIndexes().length; i++)
         {
            final IndexInfo index = tableInfo.getAlteredIndexes()[i];

            try
            {
               // TODO schema name
               createIndex(item, tableInfo, index);
            }
            catch (SQLException e)
            {
               // TODO handle DBMS specific error codes
               observer.warn("Couldn't recreate index " + index.name + " for table "
                     + tableInfo.getTableName() + ".", e);
            }
         }
      }

      if ( !performedDmlBeforeIndexCreation)
      {
         try
         {
            tableInfo.executeDmlBeforeIndexCreation(item);
            performedDmlBeforeIndexCreation = true;
         }
         catch (SQLException e)
         {
            // TODO handle DBMS specific error codes
            observer.warn("Couldn't execute pre index creation DML for table "
                  + tableInfo.getTableName() + ".", e);
         }
      }

      // drop obsolete columns
      if ((null != tableInfo.getDroppedFields())
            && (0 < tableInfo.getDroppedFields().length))
      {
         if (dbDescriptor.supportsColumnDeletion())
         {
            for (int i = 0; i < tableInfo.getDroppedFields().length; i++)
            {
               final FieldInfo field = tableInfo.getDroppedFields()[i];

               StringBuffer buffer = new StringBuffer(400);

               buffer.append("ALTER TABLE ");
               if (StringUtils.isNotEmpty(schema))
               {
                  buffer.append(schema + ".");
               }
               // TODO currently only Oracle 8i/9i syntax
               buffer.append(tableInfo.getTableName()).append(" DROP COLUMN ").append(
                     getColumnName(dbDescriptor, field));

               try
               {
                  executeDdlStatement(item, buffer.toString());
               }
               catch (SQLException e)
               {
                  // TODO handle DBMS specific error codes
                  observer.warn("Couldn't drop attribute " + getColumnName(dbDescriptor, field)
                        + " from table " + tableInfo.getTableName() + ".", e);
               }
            }
         }
         else
         {
            // TODO modify columns to allow NULL values

            // reset obsolete columns to NULL to save space
            StringBuffer buffer = new StringBuffer(400);
            buffer.append("UPDATE ");
            if (StringUtils.isNotEmpty(schema))
            {
               buffer.append(schema + ".");
            }
            buffer.append(tableInfo.getTableName()).append(" SET ");

            String delimiter = "";
            for (int i = 0; i < tableInfo.getDroppedFields().length; i++)
            {
               final FieldInfo field = tableInfo.getDroppedFields()[i];

               buffer.append(delimiter).append(getColumnName(dbDescriptor, field)).append(" = NULL");

               delimiter = ", ";
            }

            try
            {
               executeDdlStatement(item, buffer.toString());
            }
            catch (SQLException e)
            {
               // TODO handle DBMS specific error codes
               observer.warn("Couldn't reset obsolete attributes for table "
                     + tableInfo.getTableName() + " to NULL.", e);
            }
         }
      }

      // create new indexes
      if ((null != tableInfo.getAddedIndexes())
            && (0 < tableInfo.getAddedIndexes().length))
      {
         for (int i = 0; i < tableInfo.getAddedIndexes().length; i++)
         {
            IndexInfo index = tableInfo.getAddedIndexes()[i];
            try
            {
               createIndex(item, tableInfo, index);
            }
            catch (SQLException e)
            {
               // TODO handle DBMS specific error codes
               observer.warn("Couldn't create index " + index.name + " on table "
                     + tableInfo.getTableName() + ".", e);
            }
         }
      }
   }

   /**
    * <p>Drops a table based on the passed {@link TableInfo} object.</p>
    *
    * <p>The connection to execute the command is obtained from the passed
    * {@link RuntimeItem}.</p>
    *
    * @param item the {@link RuntimeItem} which contains the database
    * connection reference.
    * @param tableInfo the {@link TableInfo} object which represents the table
    * to create.
    */
   public static void dropTable(RuntimeItem item, DropTableInfo tableInfo,
         UpgradeObserver observer)
   {
      String schema = getSchemaName();
      // optionally drop sequence on databases supporting it
      if (item.getDbDescriptor().supportsSequences()
            && !StringUtils.isEmpty(tableInfo.getSequenceName()))
      {
         try
         {
            // TODO schema name
            executeDdlStatement(item, item.getDbDescriptor()
                  .getDropPKSequenceStatementString(schema, tableInfo.getSequenceName()));
         }
         catch (SQLException e)
         {
            // TODO handle DBMS specific error codes
            observer.warn("Couldn't drop sequence " + tableInfo.getSequenceName()
                  + " for table " + tableInfo.getTableName() + ".", e);
         }
      }

      try
      {
         StringBuffer buffer = new StringBuffer(400);
         buffer.append("DROP TABLE ");
         if (StringUtils.isNotEmpty(schema))
         {
            buffer.append(schema + ".");
         }
         buffer.append(tableInfo.getTableName());
         executeDdlStatement(item, buffer.toString());
      }
      catch (SQLException e)
      {
         // TODO handle DBMS specific error codes
         observer.warn("Couldn't drop table " + tableInfo.getTableName() + " table.", e);
      }
   }

   public static String getSchemaName()
   {
      return (String) Parameters.instance().get(
            SessionFactory.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX);
   }

   public static String getUserName()
   {
      return (String) Parameters.instance().get(
            SessionFactory.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX);
   }

   /**
    * <p>Drops a table based on the passed {@link TableInfo} object.</p>
    *
    * <p>The connection to execute the command is obtained from the passed
    * {@link RuntimeItem}.</p>
    *
    * @param item the {@link RuntimeItem} which contains the database
    * connection reference.
    * @param tableInfo the {@link TableInfo} object which represents the table
    * to create.
    */
   public static void dropTable(RuntimeItem item, TableInfo tableInfo)
         throws SQLException
   {
      String schema = getSchemaName();
      StringBuffer buffer = new StringBuffer(400);
      buffer.append("drop table ");
      if (StringUtils.isNotEmpty(schema))
      {
         buffer.append(schema + ".");
      }
      buffer.append(tableInfo.getTableName());
      buffer.append(" cascade constraints");
      executeDdlStatement(item, buffer.toString());

   }

   /**
    * <p>Creates a sequence which is related to the table which is represented
    * by the passed {@link TableInfo} object.</p>
    *
    * <p>The connection to execute the command is obtained from the passed
    * {@link RuntimeItem}.</p>
    *
    * @param item the {@link RuntimeItem} which contains the database
    * connection reference.
    * @param tableInfo the {@link TableInfo} object which represents the table
    * to create.
    */
   public static void createSequence(RuntimeItem item, TableInfo tableInfo)
         throws SQLException
   {
      executeDdlStatement(
            item, "create sequence " + getQualifiedName(tableInfo.getSequenceName()));
   }

   /**
    * <p>Drops the sequence which belongs to passed {@link TableInfo}
    * object.</p>
    *
    * <p>The connection to execute the command is obtained from the passed
    * {@link RuntimeItem}.</p>
    *
    * @param item the {@link RuntimeItem} which contains the database
    * connection reference.
    * @param tableInfo the {@link TableInfo} object which represents the table
    * to create.

    */
   public static void dropSequence(RuntimeItem item, TableInfo tableInfo)
         throws SQLException
   {
      executeDdlStatement(
            item, "drop sequence " + getQualifiedName(tableInfo.getSequenceName()));

   }

   /**
    *
    */
   public static void createSynonym(
         RuntimeItem item, SynonymTableInfo synonymTableInfo)
         throws SQLException
   {
      executeDdlStatement(
            item, "create synonym " + synonymTableInfo.getSynonymName()
            + " for " + synonymTableInfo.getTableName());

   }

   /**
    *
    */
   public static void dropSynonym(
         RuntimeItem item, SynonymTableInfo synonymTableInfo)
         throws SQLException
   {
      executeDdlStatement(
            item, "drop synonym " + synonymTableInfo.getSynonymName());
   }

   /**
    *
    */
   public static void executeDdlStatement(RuntimeItem item, String sqlCommand)
         throws SQLException
   {
      trace.debug("executing SQL DDL command: '" + sqlCommand + "'");

      item.executeDdlStatement(sqlCommand, false);
   }

   /**
    * Like {@link #executeDdlStatement(RuntimeItem, String)} but fault tolerant.
    */
   public static void tryExecuteDdlStatement(RuntimeItem item, String sqlCommand)
   {
      try
      {
         executeDdlStatement(item, sqlCommand);
      }
      catch (SQLException e)
      {
         System.out.println("Error executing '" + sqlCommand
               + "', message: " + e.getMessage());
         trace.warn("Error executing '" + sqlCommand + "'.", e);
      }
   }

   public static void executeUpdate(RuntimeItem item, String sqlCommand)
         throws SQLException
   {
      Statement statement = null;

      try
      {
         trace.debug("executing SQL command: '" + sqlCommand + "'");

         statement = item.getConnection().createStatement();
         statement.executeUpdate(sqlCommand);
      }
      finally
      {
         QueryUtils.closeStatement(statement);
      }
   }

   public static String getQualifiedName(String name)
   {
      return getQualifiedName(name, null);
   }

   public static String getQualifiedName(String name, String alias)
   {
      String schema = getSchemaName();
      StringBuffer buffer = new StringBuffer(400);
      if (StringUtils.isNotEmpty(schema))
      {
         buffer.append(schema + ".");
      }
      buffer.append(name);

      if(StringUtils.isNotEmpty(alias))
      {
         buffer.append(" ").append(alias);
      }
      return buffer.toString();
   }

   public static String getQualifiedColName(String tabAlias, String colName)
   {
      StringBuffer buffer = new StringBuffer(400);
      if (StringUtils.isNotEmpty(tabAlias))
      {
         buffer.append(tabAlias + ".");
      }
      buffer.append(colName);

      return buffer.toString();
   }

   /**
    *
    */
   public static ResultSet executeQuery(RuntimeItem item, String sqlQuery)
         throws SQLException
   {
      Statement statement = null;
      ResultSet resultSet = null;

      try
      {
         trace.debug("executing SQL query: '" + sqlQuery + "'");

         statement = item.getConnection().createStatement();
         resultSet = statement.executeQuery(sqlQuery);
         return resultSet;
      }
      catch (SQLException se)
      {
         trace.error("", se);
         QueryUtils.closeStatementAndResultSet(statement, resultSet);

         throw se;
      }

   }

   private static void createIndex(RuntimeItem item, AbstractTableInfo table,
         IndexInfo index) throws SQLException
   {
      String schema = getSchemaName();
      DBDescriptor dbDescriptor = item.getDbDescriptor();
      // TODO consolidate API to remove need to copy field names
      String[] fieldNames = new String[index.fields.length];
      for (int i = 0; i < index.fields.length; i++)
      {
         fieldNames[i] = getColumnName(dbDescriptor, index.fields[i]);
      }

      IndexDescriptor idxDesc = new IndexDescriptor(index.name, fieldNames, index.unique);
      // TODO schema name
      executeDdlStatement(item, dbDescriptor.getCreateIndexStatement(schema,
            table.getTableName(), idxDesc));
   }

   public static String getInClause(Collection<?> values)
   {
      StringBuffer inClause = new StringBuffer();
      inClause.append("IN(");

      int count = 0;
      for(Object value: values)
      {
         if(count > 0)
         {
            inClause.append(",");
         }

         inClause.append(getSqlValue(value));
         count++;
      }

      inClause.append(")");
      return inClause.toString();

   }

   public static LoggingPreparedStatement prepareLoggingStatement(Connection connection, String sql) throws SQLException
   {
      PreparedStatement ps = connection.prepareStatement(sql);
      return new LoggingPreparedStatement(ps, sql);
   }

   protected static String getColumnName(DBDescriptor dbDescriptor, FieldInfo field)
   {
      if(columnNameModificationMode == ColumnNameModificationMode.UPPER_CASE)
      {
         String colName = null;
         if(DBMSKey.SYBASE.equals(dbDescriptor.getDbmsKey()))
         {
            colName = field.getName();
         }
         else
         {
            colName = field.getName().toUpperCase();
         }
         return dbDescriptor.quoteIdentifier(colName);
      }
      else if(columnNameModificationMode == ColumnNameModificationMode.LOWER_CASE)
      {
         String colName = field.getName().toLowerCase();
         return dbDescriptor.quoteIdentifier(colName);
      }
      else if(columnNameModificationMode == ColumnNameModificationMode.NONE)
      {
         return dbDescriptor.quoteIdentifier(field.getName());
      }

      throw new PublicException(
            BpmRuntimeError.JDBC_UNKNOWN_COLUMN_MODIFICATION_TYPE
                  .raise(columnNameModificationMode));
   }

   public static boolean hasTable(DatabaseMetaData databaseMetaData, String tableName) throws SQLException
   {
      String metaDataSchema = getMetaDataSchema(databaseMetaData);
      tableName = getIdentifierAsStoredInDb(databaseMetaData, tableName);

      String[] types = {"TABLE"};
      ResultSet rs = null;
      try {
         rs = databaseMetaData.getTables(null, metaDataSchema, tableName, types);
         if(rs.next())
         {
            return true;
         }

         return false;
      }
      finally
      {
         if(rs != null)
         {
            rs.close();
         }
      }
   }

   public static List<FieldInfo> getColumns(DatabaseMetaData databaseMetaData, String tableName, Integer columType) throws SQLException
   {
      List<FieldInfo> columns = new ArrayList<FieldInfo>();
      String metaDataSchema = getMetaDataSchema(databaseMetaData);
      tableName = getIdentifierAsStoredInDb(databaseMetaData, tableName);

      ResultSet rs = null;
      try {
         rs = databaseMetaData.getColumns(null, metaDataSchema, tableName, null);
         while(rs.next())
         {
            int rsType = rs.getInt(5);
            boolean match = true;
            if(columType != null)
            {
               if(!columType.equals(rsType))
               {
                  match = false;
               }
            }

            if(match)
            {
               Class javaType = DmlManager.mapSqlTypeToJavaTpe(rsType);
               String columnName = rs.getString(4);
               int columnSize = rs.getInt(7);

               FieldInfo fieldInfo
                  = new FieldInfo(columnName, javaType, columnSize);
               columns.add(fieldInfo);
            }
         }
      }
      finally
      {
         if(rs != null)
         {
            rs.close();
         }
      }

      return columns;
   }

   public static List<String> getColumns(DatabaseMetaData databaseMetaData, String tableName) throws SQLException
   {
      List<String> columns = new ArrayList<String>();
      String metaDataSchema = getMetaDataSchema(databaseMetaData);
      tableName = getIdentifierAsStoredInDb(databaseMetaData, tableName);

      ResultSet rs = null;
      try {
         rs = databaseMetaData.getColumns(null, metaDataSchema, tableName, null);
         while(rs.next())
         {
            String column = rs.getString(4);
            columns.add(column);
         }
      }
      finally
      {
         if(rs != null)
         {
            rs.close();
         }
      }

      return columns;
   }

   public static boolean hasIndex(DatabaseMetaData databaseMetaData, String tableName, String indexName) throws SQLException
   {
      List<IndexInfo> allIndices = getIndices(databaseMetaData, tableName);
      IndexInfo match = findIndex(databaseMetaData, indexName, allIndices);
      if(match != null)
      {
         return true;
      }

      return false;
   }

   public static IndexInfo findIndex(DatabaseMetaData databaseMetaData, String indexName,
         List<IndexInfo> indices) throws SQLException
   {
      indexName = getIdentifierAsStoredInDb(databaseMetaData, indexName);
      for (IndexInfo index : indices)
      {
         String tmpIndexName = getIdentifierAsStoredInDb(databaseMetaData, index.getName());
         if (indexName.equals(tmpIndexName))
         {
            return index;
         }
      }

      return null;
   }

   public static List<IndexInfo> getIndices(DatabaseMetaData databaseMetaData,
         String tableName) throws SQLException
   {
      List<IndexInfo> nonUniqueIndices = getIndices(databaseMetaData, tableName, false);
      List<IndexInfo> uniqueIndices = getIndices(databaseMetaData, tableName, true);

      List<IndexInfo> allIndices = new ArrayList<IndexInfo>();
      allIndices.addAll(nonUniqueIndices);
      allIndices.addAll(uniqueIndices);
      return allIndices;
   }

   public static List<IndexInfo> getIndices(DatabaseMetaData databaseMetaData,
         String tableName, boolean unique) throws SQLException
   {
      List<IndexInfo> indices = new ArrayList<IndexInfo>();
      String metaDataSchema = getMetaDataSchema(databaseMetaData);
      tableName = getIdentifierAsStoredInDb(databaseMetaData, tableName);

      ResultSet rs = null;
      try {
         rs = databaseMetaData.getIndexInfo(null, metaDataSchema, tableName, unique, true);
         while(rs.next())
         {
            String indexName = rs.getString(6);
            IndexInfo indexInfo = new IndexInfo(indexName, unique, new FieldInfo[0]);
            indices.add(indexInfo);
         }
      }
      finally
      {
         if(rs != null)
         {
            rs.close();
         }
      }

      return indices;
   }

   private static String getMetaDataSchema(DatabaseMetaData databaseMetaData) throws SQLException
   {
      String schemaIdentifier = getSchemaName();
      if(StringUtils.isNotEmpty(schemaIdentifier))
      {
         return getIdentifierAsStoredInDb(databaseMetaData, schemaIdentifier);
      }

      return null;
   }

   public static DatabaseMetaData getDatabaseMetaData(RuntimeItem item) throws SQLException
   {
      return item.getConnection().getMetaData();
   }

   //return an identifier in the format as its stored in the databse
   //this is used when working with DatabaseMetaData
   public static String getIdentifierAsStoredInDb(DatabaseMetaData databaseMetaData, String identifier) throws SQLException
   {
      if(StringUtils.isNotEmpty(identifier))
      {
         if(databaseMetaData.storesLowerCaseIdentifiers())
         {
            identifier = identifier.toLowerCase();
         }
         else if(databaseMetaData.storesUpperCaseIdentifiers())
         {
            identifier = identifier.toUpperCase();
         }
      }

      return identifier;
   }

   public static String getSqlValue(Object value)
   {
      String quotedValue = null;
      if(value != null)
      {
         if(value instanceof String)
         {
            quotedValue = "'"+value.toString()+"'";
         }
         else if(value instanceof FieldInfo)
         {
            quotedValue = ((FieldInfo) value).getName();
         }
         else
         {
            quotedValue = value.toString();
         }
      }

      return quotedValue;
   }

   public static String getSelectBasedOnNullCriteriaSql(RuntimeItem item,
         String tableName,
         FieldInfo nullCriteriaColumn,
         NVLFunction nullReplaceFunction,
         FieldInfo...selectColumns) throws SQLException
   {
      DBDescriptor descriptor = item.getDbDescriptor();

      tableName = getQualifiedName(tableName);
      StringBuffer buffer = new StringBuffer();
      buffer.append("SELECT ");

      int columnCount = 0;
      for(FieldInfo selectColumn: selectColumns)
      {
         if(columnCount > 0)
         {
            buffer.append(", ");
         }
         buffer.append(selectColumn.getName());
      }
      buffer.append(" FROM ").append(tableName);
      buffer.append(" WHERE ");

      if(descriptor instanceof OracleDbDescriptor
            && nullReplaceFunction != null)
      {
         buffer.append(nullReplaceFunction);
         buffer.append(" = ");
         buffer.append(nullReplaceFunction.getReplaceValue());
      }
      else
      {
         buffer.append(nullCriteriaColumn.getName());
         buffer.append(" IS NULL");
      }

      return buffer.toString();
   }
}