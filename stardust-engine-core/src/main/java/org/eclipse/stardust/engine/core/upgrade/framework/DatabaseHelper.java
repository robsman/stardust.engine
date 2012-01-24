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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.IndexDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.IndexInfo;


/**
 * @author Sebastian Woelk
 * @version $Revision$
 */
public final class DatabaseHelper
{
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

   public static void alterTable(RuntimeItem item, AlterTableInfo tableInfo,
         UpgradeObserver observer)
   {
      String schema = getSchemaName();
      DBDescriptor dbDescriptor = item.getDbDescriptor();
      if ((null != tableInfo.getAddedFields()) && (0 < tableInfo.getAddedFields().length))
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

      // drop obsolete indexes
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
      String schema = getSchemaName();
      StringBuffer buffer = new StringBuffer(400);
      if (StringUtils.isNotEmpty(schema))
      {
         buffer.append(schema + ".");
      }
      buffer.append(name);
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
   
   private static String getColumnName(DBDescriptor dbDescriptor, FieldInfo field)
   {
      String colName = null;
      if(DBMSKey.SYBASE.equals(dbDescriptor.getDbmsKey()))
      {
         colName = field.name;
      }
      else
      {
         colName = field.name.toUpperCase();
      }
      return dbDescriptor.quoteIdentifier(colName);
   }
}
