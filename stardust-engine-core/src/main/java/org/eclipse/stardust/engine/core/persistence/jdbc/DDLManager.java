/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.setup.*;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster.DataClusterEnableState;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.DataClusterSynchronizationInfo;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DDLManager
{
   private static final String SEQUENCE_TABLE_NAME = "sequence";
   
   public static final Logger trace = LogManager.getLogger(DDLManager.class);

   private DBDescriptor dbDescriptor;

   public static String getQualifiedName(String schemaName, String objectName)
   {
      String prefix = "";
      if ( !StringUtils.isEmpty(schemaName))
      {
         prefix = schemaName + ".";
      }
      return prefix + objectName;
   }

   private static FieldDescriptor getFieldDescriptor(Field field, Class type)
   {
      Assert.condition(field.getDeclaringClass().isAssignableFrom(type), "Class "
            + field.getDeclaringClass().getName() + " is not assignable from "
            + type.getName());

      // TODO (sb): Improve performance by implementing look up cache
      FieldDescriptor descriptor = null;

      List persistentFields = TypeDescriptor.get(type).getPersistentFields();
      for (Iterator i = persistentFields.iterator(); i.hasNext();)
      {
         FieldDescriptor tempDescriptor = (FieldDescriptor) i.next();
         if (tempDescriptor.getField().equals(field))
         {
            descriptor = tempDescriptor;
            break;
         }
      }

      return descriptor;
   }

   private static boolean isPersistentDataTable(String tableName)
   {
      boolean isPredefined = false;

      for (int n = 0; n < Constants.PERSISTENT_RUNTIME_CLASSES.length; ++n)
      {
         TypeDescriptor typeDescriptor = TypeDescriptor
               .get(Constants.PERSISTENT_RUNTIME_CLASSES[n]);

         if (tableName.equals(typeDescriptor.getTableName()) ||
               tableName.equals(typeDescriptor.getLockTableName()))
         {
            isPredefined = true;
            break;
         }
      }

      if ( !isPredefined)
      {
         for (int n = 0; n < Constants.PERSISTENT_MODELING_CLASSES.length; ++n)
         {
            TypeDescriptor typeDescriptor = TypeDescriptor
                  .get(Constants.PERSISTENT_MODELING_CLASSES[n]);

            if (tableName.equals(typeDescriptor.getTableName())
                  || tableName.equals(typeDescriptor.getLockTableName()))
            {
               isPredefined = true;
               break;
            }
         }
      }

      return isPredefined;
   }

   public static int executeOrSpoolStatement(String statement, Connection connection,
         PrintStream spoolFile) throws SQLException
   {
      int result = 0;
      if (null == spoolFile)
      {
         org.eclipse.stardust.engine.core.persistence.Session auditTrailSession
            = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = null;
         if (auditTrailSession instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
         {
            jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) auditTrailSession;
         }

         Statement stmt = null;
         try
         {
            stmt = connection.createStatement();

            final TimeMeasure timer = new TimeMeasure();
            result = stmt.executeUpdate(statement);
            timer.stop();
            if(jdbcSession != null)
            {
               jdbcSession.monitorSqlExecution(statement, timer);
            }
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
      else
      {
         spoolFile.print(statement);
         spoolFile.println(";");
      }
      return result;
   }


   public DDLManager(DBDescriptor schemaManager)
   {
      this.dbDescriptor = schemaManager;
   }

   public String getCreateTableStatementString(String schemaName,
         TypeDescriptor typeManager)
   {
      return getCreateTableStatementString(schemaName, typeManager, false);
   }

   private String getGrantAllOnTableStatement(String schemaName,
         TypeDescriptor typeManager, String grantTarget)
   {
      StringBuffer buffer = new StringBuffer();

      // TODO: implement it for other db descriptors as well.
      if (DBMSKey.SYBASE.equals(dbDescriptor.getDbmsKey()))
      {
         buffer.append("GRANT ALL ON ");
         buffer.append(dbDescriptor.quoteIdentifier(typeManager.getTableName()));
         buffer.append(" TO ").append(grantTarget);
      }

      return buffer.toString();
   }

   public String getCreateTableStatementString(String schemaName,
         TypeDescriptor typeManager, boolean archive)
   {
      StringBuffer buffer = new StringBuffer();

      String tableName = typeManager.getTableName();
      buffer.append("CREATE TABLE ").append(
            getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(tableName)))
            .append(" (");

      int n;

      List persistentFields = typeManager.getPersistentFields();
      for (n = 0; n < persistentFields.size(); ++n)
      {
         FieldDescriptor descriptor = (FieldDescriptor) persistentFields.get(n);
         descriptor.getField().setAccessible(true);

         if (n > 0)
         {
            buffer.append(", ");
         }

         final String fieldName = descriptor.getField().getName();
         buffer.append(dbDescriptor.quoteIdentifier(fieldName));
         buffer.append(" ");
         buffer.append(dbDescriptor.getSQLType(descriptor.getField().getType(), descriptor.getLength()));

         boolean forceNotNull = false;
         if ((dbDescriptor.supportsIdentityColumns() || DBMSKey.MYSQL_SEQ.equals(dbDescriptor.getDbmsKey()))
             && !archive
             && typeManager.requiresPKCreation()
             && typeManager.isPkField(descriptor.getField())
            )
         {
            buffer.append(" ").append(dbDescriptor.getIdentityColumnQualifier());
            forceNotNull = true;
         }

         if ( !forceNotNull && !dbDescriptor.isColumnNullableByDefault())
         {
            buffer.append(" NULL");
         }
      }

      List links = typeManager.getLinks();
      for (int m = 0; m < links.size(); ++m)
      {
         LinkDescriptor link = (LinkDescriptor) links.get(m);

         if (n > 0 || m > 0)
         {
            buffer.append(", ");
         }

         final String fieldName = link.getField().getName();
         buffer.append(dbDescriptor.quoteIdentifier(fieldName));

         buffer.append(" ");
         buffer.append(dbDescriptor.getSQLType(
               link.getFkField().getType(), link.getFKFieldLength()));

         if ( !dbDescriptor.isColumnNullableByDefault())
         {
            buffer.append(" NULL");
         }
      }

      buffer.append(")");

      final String tableOptions = dbDescriptor.getCreateTableOptions();
      if (!StringUtils.isEmpty(tableOptions))
      {
         buffer.append(" ").append(tableOptions);
      }

      return buffer.toString();
   }

   public String getCreateLockTableStatementString(String schemaName,
         TypeDescriptor typeManager)
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("CREATE TABLE ").append(
            getQualifiedName(schemaName, typeManager.getLockTableName())).append(" (");

      Field[] pkFields = typeManager.getPkFields();

      for (int i = 0; i < pkFields.length; i++ )
      {
         if (0 < i)
         {
            buffer.append(", ");
         }

         buffer.append(pkFields[i].getName()).append(" ")
               .append(dbDescriptor.getSQLType(
                     pkFields[i].getType(),
                     getFieldDescriptor(pkFields[i], typeManager.getType()).getLength()));
      }

      buffer.append(")");

      final String tableOptions = dbDescriptor.getCreateTableOptions();
      if ( !StringUtils.isEmpty(tableOptions))
      {
         buffer.append(" ").append(tableOptions);
      }

      return buffer.toString();
   }

   public String getDropTableStatementString(String schemaName, String tableName)
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("DROP TABLE ").append(
            getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(tableName)));

      return buffer.toString();
   }

   public void createGlobalSequenceIfNecessary(final String schemaName, final Connection connection)
   {
      if (dbDescriptor.supportsSequences())
      {
         final String createGlobalSequenceStmt = dbDescriptor.getCreateGlobalPKSequenceStatementString(schemaName);
         if (createGlobalSequenceStmt == null)
         {
            return;
         }

         Statement stmt = null;
         try
         {
            stmt = connection.createStatement();
            stmt.executeUpdate(createGlobalSequenceStmt);
            trace.debug("Global PK sequence created.");
         }
         catch (final SQLException e)
         {
            final String message = "Error creating global pk sequence. Reason: " + e.getMessage();
            trace.warn(message, e);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
   }

   public void createSequenceStoredProcedureIfNecessary(final String schemaName, final Connection connection)
   {
      if (dbDescriptor.supportsSequences())
      {
         final String createStoredProcedureStmt = dbDescriptor.getCreateSequenceStoredProcedureStatementString(schemaName);
         if (createStoredProcedureStmt == null)
         {
            return;
         }

         Statement stmt = null;
         try
         {
            stmt = connection.createStatement();
            stmt.executeUpdate(createStoredProcedureStmt);
            trace.debug("Sequence stored procedure created.");
         }
         catch (final SQLException e)
         {
            final String message = "Error creating sequence stored procedure. Reason: " + e.getMessage();
            trace.warn(message, e);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
   }

   public void createTableForClass(String schemaName, Class type, Connection connection)
   {
      try
      {
         TypeDescriptor typeManager = TypeDescriptor.get(type);

         if ( !containsTable(schemaName, TypeDescriptor.getTableName(type), connection))
         {
            if (typeManager.hasPkSequence())
            {
               if (dbDescriptor.supportsSequences())
               {

                  Statement stmt = connection.createStatement();
                  try
                  {
                     stmt.executeUpdate(dbDescriptor.getCreatePKSequenceStatementString(
                           schemaName, typeManager.getPkSequence()));
                     trace.debug("PK sequence created.");
                  }
                  catch (SQLException e)
                  {
                     String message = "Error creating pk sequence for table '"
                           + getQualifiedName(schemaName, typeManager.getTableName())
                           + "'. Reason: " + e.getMessage();
                     System.out.println(message);
                     trace.warn(message, e);
                  }
                  finally
                  {
                     QueryUtils.closeStatement(stmt);
                  }
               }
               else if (!dbDescriptor.supportsIdentityColumns())
               {
                  try
                  {
                     Statement stmt = connection.createStatement();

                     // Check for existence of the sequence

                     ResultSet rs = stmt.executeQuery("SELECT value FROM "
                           + getQualifiedName(schemaName, DBDescriptor.SEQUENCE_HELPER_TABLE_NAME)
                           + " WHERE name = '" + typeManager.getPkSequence() + "'");

                     if (!rs.next())
                     {
                        stmt.execute("INSERT INTO "
                              + getQualifiedName(schemaName, DBDescriptor.SEQUENCE_HELPER_TABLE_NAME)
                              + " VALUES ('" + typeManager.getPkSequence() + "', 0)");
                     }

                     rs.close();
                     stmt.close();
                     connection.commit();
                  }
                  catch (SQLException e)
                  {
                     String message = "Error creating pk sequence entry for table '"
                           + getQualifiedName(schemaName, typeManager.getTableName())
                           + "'. Reason: " + e.getMessage();
                     System.out.println(message);
                     trace.warn(message, e);
                  }
                  trace.debug("Sequence created.");
               }
            }

            // Create table

            Statement createTableStmt = connection.createStatement();
            try
            {
               createTableStmt.executeUpdate(getCreateTableStatementString(schemaName,
                     typeManager));
               trace.debug("Table created.");
            }
            catch (SQLException e)
            {
               String message = "Error creating table '"
                     + getQualifiedName(schemaName, typeManager.getTableName())
                     + "'. Reason: " + e.getMessage();
               System.out.println(message);
               trace.warn(message, e);
            }
            finally
            {
               QueryUtils.closeStatement(createTableStmt);
            }

            // Create indexes

            for (int n = 0; n < typeManager.getIndexCount(); ++n)
            {
               Statement stmt = connection.createStatement();
               try
               {
                  stmt.executeUpdate(dbDescriptor.getCreateIndexStatement(schemaName,
                        typeManager.getTableName(), typeManager.getIndexDescriptor(n)));
               }
               catch (SQLException e)
               {
                  String message = "Error creating index for table '"
                        + getQualifiedName(schemaName, typeManager.getTableName())
                        + "'. Reason: " + e.getMessage();
                  System.out.println(message);
                  trace.warn(message, e);
               }
               finally
               {
                  QueryUtils.closeStatement(stmt);
               }
            }

            trace.debug("Indexes created.");
            connection.commit();
         }
         else
         {
            String message = "Table '"
                  + getQualifiedName(schemaName, TypeDescriptor.getTableName(type))
                  + "' already exists";
            System.out.println(message);
            trace.warn(message);
         }
      }
      catch (SQLException x)
      {
         throw new InternalException("While creating type manager for '" +
               type.getName() + "'.", x);
      }
   }

   public void createSequenceTable(String schemaName, Connection connection,
         PrintStream spoolFile) throws SQLException
   {
      if (!containsTable(schemaName, SEQUENCE_TABLE_NAME, connection))
      {
         // create table sequence
         executeOrSpoolStatement(
               dbDescriptor.getCreateGlobalPKSequenceStatementString(schemaName),
               connection, spoolFile);

         // create next_sequence_value_for function
         executeOrSpoolStatement(
               dbDescriptor.getCreateSequenceStoredProcedureStatementString(schemaName),
               connection, spoolFile);
      }
   }

   public void synchronizeSequenceTable(String schemaName, Connection connection,
         PrintStream spoolFile) throws SQLException
   {
      if (containsTable(schemaName, SEQUENCE_TABLE_NAME, connection))
      {
         // synchronize sequence table with the current primary key value
         Collection<Class> persistentClasses = SchemaHelper
               .getPersistentClasses(dbDescriptor);
         for (Class clazz : persistentClasses)
         {
            TypeDescriptor typeManager = TypeDescriptor.get(clazz);
            if (typeManager.hasPkSequence())
            {
               String tableName = schemaName + "." + SEQUENCE_TABLE_NAME;
               StringBuilder selectMaxOidSQLString = new StringBuilder();
               selectMaxOidSQLString
                     .append("(SELECT max(x) FROM (SELECT max(oid) AS x FROM ");
               selectMaxOidSQLString.append(schemaName);
               selectMaxOidSQLString.append(".");
               selectMaxOidSQLString.append(typeManager.getTableName());
               selectMaxOidSQLString.append(" UNION SELECT 0) AS max)");
               StringBuilder selectSequenceTableEntrySQLString = new StringBuilder();
               selectSequenceTableEntrySQLString.append("(SELECT name FROM ");
               selectSequenceTableEntrySQLString.append(tableName);
               selectSequenceTableEntrySQLString.append(" seq WHERE seq.name='");
               selectSequenceTableEntrySQLString.append(typeManager.getPkSequence());
               selectSequenceTableEntrySQLString.append("')");
               Statement existsStmt = null;
               ResultSet resultSet = null;
               try
               {
                  existsStmt = connection.createStatement();
                  existsStmt.execute(selectSequenceTableEntrySQLString.toString());
                  resultSet = existsStmt.getResultSet();
                  if (resultSet.next())
                  {
                     executeOrSpoolStatement(
                           "UPDATE " + tableName + " SET value=" + selectMaxOidSQLString
                                 + "WHERE name='" + typeManager.getPkSequence() + "'",
                           connection, spoolFile);
                  }
                  else
                  {
                     executeOrSpoolStatement("INSERT INTO " + tableName + " VALUES ('"
                           + typeManager.getPkSequence() + "', " + selectMaxOidSQLString
                           + ")", connection, spoolFile);
                  }
               }
               finally
               {
                  QueryUtils.closeStatementAndResultSet(existsStmt, resultSet);
               }

            }
         }
      }
   }

   public void dropSequenceTable(String schemaName, Connection connection,
         PrintStream spoolFile) throws SQLException
   {
      if (containsTable(schemaName, SEQUENCE_TABLE_NAME, connection))
      {
         // drop table sequence
         executeOrSpoolStatement(
               dbDescriptor.getDropGlobalPKSequenceStatementString(schemaName),
               connection, spoolFile);

         // drop next_sequence_value_for function
         executeOrSpoolStatement(
               dbDescriptor.getDropSequenceStoredProcedureStatementString(schemaName),
               connection, spoolFile);
      }
   }

   public void verifySequenceTable(Connection connection, String schemaName)
         throws SQLException
   {
      if (!containsTable(schemaName, SEQUENCE_TABLE_NAME, connection))
      {
         String message = "Table '" + SEQUENCE_TABLE_NAME + "' does not exist.";
         System.out.println(message);
         trace.warn(message);
      }
      else
      {
         boolean inconsistent = false;
         Collection<Class> persistentClasses = SchemaHelper
               .getPersistentClasses(dbDescriptor);
         for (Class clazz : persistentClasses)
         {
            TypeDescriptor typeManager = TypeDescriptor.get(clazz);
            if (typeManager.hasPkSequence())
            {
               Statement verifyStmt = null;
               ResultSet resultSet = null;
               StringBuilder verifySQLString = new StringBuilder();
               verifySQLString.append("SELECT name, value FROM ");
               verifySQLString.append(schemaName);
               verifySQLString.append(".");
               verifySQLString.append(SEQUENCE_TABLE_NAME);
               verifySQLString.append(" seq WHERE seq.name='");
               verifySQLString.append(typeManager.getPkSequence());
               verifySQLString
                     .append("' AND seq.value=(SELECT max(x) FROM (SELECT max(oid) AS x FROM ");
               verifySQLString.append(schemaName);
               verifySQLString.append(".");
               verifySQLString.append(typeManager.getTableName());
               verifySQLString.append(" UNION SELECT 0) AS max)");
               try
               {
                  verifyStmt = connection.createStatement();
                  verifyStmt.execute(verifySQLString.toString());
                  resultSet = verifyStmt.getResultSet();
                  if (!resultSet.next())
                  {
                     inconsistent = true;
                  }
               }
               catch (SQLException e)
               {
                  String message = "Couldn't verify sequence table '"
                        + SEQUENCE_TABLE_NAME + "'." + " Reason: " + e.getMessage();
                  System.out.println(message);
                  trace.warn(message, e);
               }
               finally
               {
                  QueryUtils.closeStatementAndResultSet(verifyStmt, resultSet);
               }
            }
         }
         if (inconsistent)
         {
            String message = "Table " + SEQUENCE_TABLE_NAME
                  + " is not consistent. Synchronization is required.";
            System.out.println(message);
            trace.warn(message);
         }
         else
         {
            String message = "Table " + SEQUENCE_TABLE_NAME + " is consistent.";
            System.out.println(message);
            trace.info(message);
         }
      }
   }

   public void createLockTableForClass(String schemaName, Class type,
         Connection connection, PrintStream spoolFile, String statementDelimiter)
   {
      try
      {
         TypeDescriptor typeManager = TypeDescriptor.get(type);

         if ( !containsTable(schemaName, TypeDescriptor.getLockTableName(type),
               connection))
         {
            // Create table

            try
            {
               executeOrSpoolStatement(getCreateLockTableStatementString(schemaName,
                     typeManager), connection, spoolFile);
               trace.debug("Table created.");
            }
            catch (SQLException e)
            {
               String message = "Error creating lock table '"
                     + getQualifiedName(schemaName, typeManager.getLockTableName())
                     + "'. Reason: " + e.getMessage();
               System.out.println(message);
               trace.warn(message, e);
            }

            // Create index

            try
            {
               Field[] pkFields = typeManager.getPkFields();
               String[] indexFields = new String[pkFields.length];
               for (int i = 0; i < pkFields.length; i++ )
               {
                  indexFields[i] = pkFields[i].getName();
               }
               IndexDescriptor indexDscr = new IndexDescriptor(
                     typeManager.getLockIndexName(), indexFields, true);

               executeOrSpoolStatement(dbDescriptor.getCreateIndexStatement(schemaName,
                     typeManager.getLockTableName(), indexDscr), connection, spoolFile);
            }
            catch (SQLException e)
            {
               String message = "Error creating index for lock table '"
                     + getQualifiedName(schemaName, typeManager.getLockTableName())
                     + "'. Reason: " + e.getMessage();
               System.out.println(message);
               trace.warn(message, e);
            }

            trace.debug("Indexes created.");
            connection.commit();
         }
         else
         {
            String message = "Table '"
                  + getQualifiedName(schemaName, TypeDescriptor.getLockTableName(type))
                  + "' already exists";
            System.out.println(message);
            trace.warn(message);
         }
      }
      catch (SQLException x)
      {
         throw new InternalException("While creating type manager for '" +
               type.getName() + "'.", x);
      }
   }

   public void dropGlobalSequenceIfAny(final String schemaName, final Connection connection)
   {
      if (dbDescriptor.supportsSequences())
      {
         final String dropGlobalSequenceStmt = dbDescriptor.getDropGlobalPKSequenceStatementString(schemaName);
         if (dropGlobalSequenceStmt == null)
         {
            return;
         }

         Statement stmt = null;
         try
         {
            stmt = connection.createStatement();
            stmt.executeUpdate(dropGlobalSequenceStmt);
            trace.debug("Global PK sequence dropped.");
         }
         catch (final SQLException e)
         {
            final String message = "Couldn't drop global pk sequence. Reason: " + e.getMessage();
            trace.warn(message, e);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
   }

   public void dropSequenceStoredProcedureIfAny(final String schemaName, final Connection connection)
   {
      if (dbDescriptor.supportsSequences())
      {
         final String dropSequenceStoredProcedureStmt = dbDescriptor.getDropSequenceStoredProcedureStatementString(schemaName);
         if (dropSequenceStoredProcedureStmt == null)
         {
            return;
         }

         Statement stmt = null;
         try
         {
            stmt = connection.createStatement();
            stmt.executeUpdate(dropSequenceStoredProcedureStmt);
            trace.debug("Sequence stored procedure dropped.");
         }
         catch (final SQLException e)
         {
            final String message = "Couldn't drop sequence stored procedure. Reason: " + e.getMessage();
            trace.warn(message, e);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
   }

   public void dropTableForClass(String schemaName, Class type, Connection connection)
   {
      TypeDescriptor typeManager = TypeDescriptor.get(type);
      Statement dropTableStmt = null;
      try
      {
         dropTableStmt = connection.createStatement();
         dropTableStmt.execute(getDropTableStatementString(schemaName, typeManager
               .getTableName()));
      }
      catch (SQLException x)
      {
         String message = "Couldn't drop table '"
               + getQualifiedName(schemaName, typeManager.getTableName()) + "'."
               + " Reason: " + x.getMessage();
         System.out.println(message);
         trace.warn(message, x);
      }

      finally
      {
         QueryUtils.closeStatement(dropTableStmt);
      }

      if (typeManager.getPkSequence() != null)
      {
         String dropSequenceStmtString = dbDescriptor.getDropPKSequenceStatementString(
               schemaName, typeManager.getPkSequence());
         if (dropSequenceStmtString != null)
         {
            Statement dropSequenceStmt = null;
            try
            {
               dropSequenceStmt = connection.createStatement();
               dropSequenceStmt.executeUpdate(dropSequenceStmtString);
            }
            catch (SQLException x)
            {
               String message = "Couldn't drop sequence for table '"
                     + getQualifiedName(schemaName, typeManager.getTableName()) + "'."
                     + " Reason: " + x.getMessage();
               System.out.println(message);
               trace.warn(message, x);
            }
            finally
            {
               QueryUtils.closeStatement(dropSequenceStmt);
            }
         }
      }
      try
      {
         connection.commit();
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
   }

   public void dropLockTableForClass(String schemaName, Class type,
         Connection connection, PrintStream spoolFile, String statementDelimiter)
   {
      TypeDescriptor typeManager = TypeDescriptor.get(type);
      try
      {
         executeOrSpoolStatement(getDropTableStatementString(schemaName, typeManager
               .getLockTableName()), connection, spoolFile);
      }
      catch (SQLException x)
      {
         String message = "Couldn't drop lock table '"
               + getQualifiedName(schemaName, typeManager.getLockTableName()) + "'."
               + " Reason: " + x.getMessage();
         System.out.println(message);
         trace.warn(message, x);
      }

      try
      {
         connection.commit();
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
   }

   /**
    * Dumps the schema for all database entry (tables, sequences, indexes)
    * for the specified typeMap to a DDL file.
    * @param session TODO
    */
   public void dumpCreateSchemaDDLToFile(File file, Session session, String schemaName,
         Collection classes, String statementDelimiter)
   {
      Assert.isNotNull(file);

      statementDelimiter = getStatementDelimiter(statementDelimiter, dbDescriptor);

      try
      {
         PrintStream ps = new PrintStream(new FileOutputStream(file));

         final String createGlobalPkSequenceStmt = dbDescriptor.getCreateGlobalPKSequenceStatementString(schemaName);
         if (dbDescriptor.supportsSequences() && createGlobalPkSequenceStmt != null)
         {
            ps.print(createGlobalPkSequenceStmt);
            ps.println(statementDelimiter);
            ps.println();
         }

         final String createSequenceStoredProcedureStmt = dbDescriptor.getCreateSequenceStoredProcedureStatementString(schemaName);
         if (dbDescriptor.supportsSequences() && createSequenceStoredProcedureStmt != null)
         {
            final String tmpDelimiter = "//";
            final String delimiterLiteral = "DELIMITER";
            ps.println(delimiterLiteral + " " + tmpDelimiter);
            ps.print(createSequenceStoredProcedureStmt);
            ps.println(tmpDelimiter);
            ps.println(delimiterLiteral + " " + statementDelimiter);
            ps.println();
         }

         for (Iterator i = classes.iterator(); i.hasNext();)
         {
            Class clazz = (Class) i.next();
            TypeDescriptor tm = TypeDescriptor.get(clazz);

            if (dbDescriptor.supportsSequences() && tm.hasPkSequence())
            {
               ps.print(dbDescriptor.getCreatePKSequenceStatementString(schemaName,
                     tm.getPkSequence()));
               ps.println(statementDelimiter);
            }
            ps.print(getCreateTableStatementString(schemaName, tm));
            ps.println(statementDelimiter);

            for (int n = 0; n < tm.getIndexCount(); ++n)
            {
               ps.print(dbDescriptor.getCreateIndexStatement(schemaName,
                     tm.getTableName(), tm.getIndexDescriptor(n)));
               ps.println(statementDelimiter);
            }
            ps.println();

            if (session.isUsingLockTables()
                  && TypeDescriptor.get(clazz).isDistinctLockTableName())
            {
               ps.print(getCreateLockTableStatementString(schemaName, tm));
               ps.println(statementDelimiter);

               Field[] pkFields = tm.getPkFields();
               String[] indexFields = new String[pkFields.length];
               for (int fieldIdx = 0; fieldIdx < pkFields.length; fieldIdx++ )
               {
                  indexFields[fieldIdx] = pkFields[fieldIdx].getName();
               }
               IndexDescriptor indexDscr = new IndexDescriptor(tm.getLockIndexName(),
                     indexFields, true);

               ps.print(dbDescriptor.getCreateIndexStatement(schemaName,
                     tm.getLockTableName(), indexDscr));
               ps.println(statementDelimiter);

               ps.println();
            }
         }

         ps.println();

         if (!dbDescriptor.supportsSequences()
               && !dbDescriptor.supportsIdentityColumns())
         {
            for (Iterator i = classes.iterator(); i.hasNext(); )
            {
               TypeDescriptor td = TypeDescriptor.get((Class) i.next());
               if (td.hasPkSequence())
               {
                  ps.println("INSERT INTO "
                        + getQualifiedName(schemaName,
                              DBDescriptor.SEQUENCE_HELPER_TABLE_NAME) + " VALUES ('"
                        + td.getPkSequence() + "', 0)");
                  ps.println(statementDelimiter);
               }
            }
         }
         ps.println();

         if (dbDescriptor.supportsSequences())
         {
            List columns = new ArrayList();
            columns.add(PropertyPersistor.FIELD__OID);
            columns.add(PropertyPersistor.FIELD__PARTITION);
            columns.add(PropertyPersistor.FIELD__NAME);
            columns.add(PropertyPersistor.FIELD__VALUE);
            columns.add(PropertyPersistor.FIELD__LOCALE);
            columns.add(PropertyPersistor.FIELD__FLAGS);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(PropertyPersistor.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (" + dbDescriptor.getNextValForSeqString(schemaName, "property_seq") +
                  ", -1, 'sysop.password', 'sysop', 'DEFAULT', 0)");
            ps.println(statementDelimiter);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(PropertyPersistor.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (" + dbDescriptor.getNextValForSeqString(schemaName, "property_seq") +
                  ", -1, 'carnot.version', '"
                  + CurrentVersion.getVersionName() + "', 'DEFAULT', 0)");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(AuditTrailPartitionBean.FIELD__OID);
            columns.add(AuditTrailPartitionBean.FIELD__ID);
            columns.add(AuditTrailPartitionBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (" + dbDescriptor.getNextValForSeqString(schemaName, "partition_seq") +
                  ", 'default', NULL)");
            ps.println(statementDelimiter);
            if (session.isUsingLockTables()
                  && TypeDescriptor.get(AuditTrailPartitionBean.class)
                        .isDistinctLockTableName())
            {
               columns = new ArrayList();
               columns.add(AuditTrailPartitionBean.FIELD__OID);
               ps.println(
                     "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.LOCK_TABLE_NAME))
                     + buildColumnsFragment(dbDescriptor, columns)
                     + "SELECT " + "p.oid "
                     + "FROM " +  getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME))  + " p "
                     + "WHERE p.id = 'default'");
               ps.println(statementDelimiter);
            }

            columns = new ArrayList();
            columns.add(UserDomainBean.FIELD__OID);
            columns.add(UserDomainBean.FIELD__ID);
            columns.add(UserDomainBean.FIELD__PARTITION);
            columns.add(UserDomainBean.FIELD__SUPERDOMAIN);
            columns.add(UserDomainBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserDomainBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "SELECT " + dbDescriptor.getNextValForSeqString(schemaName, "domain_seq") + ", 'default', p.oid, NULL, NULL "
                  + "FROM " +  getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)) + " p "
                  + "WHERE p.id = 'default'");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(UserDomainHierarchyBean.FIELD__OID);
            columns.add(UserDomainHierarchyBean.FIELD__SUBDOMAIN);
            columns.add(UserDomainHierarchyBean.FIELD__SUPERDOMAIN);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserDomainHierarchyBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "SELECT " + dbDescriptor.getNextValForSeqString(schemaName, "domain_hierarchy_seq") + ", d.oid, d.oid "
                  + "FROM " +  getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserDomainBean.TABLE_NAME)) + " d "
                  + "WHERE d.id = 'default'");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(UserRealmBean.FIELD__OID);
            columns.add(UserRealmBean.FIELD__ID);
            columns.add(UserRealmBean.FIELD__PARTITION);
            columns.add(UserRealmBean.FIELD__NAME);
            columns.add(UserRealmBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserRealmBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "SELECT " + dbDescriptor.getNextValForSeqString(schemaName, "wfuser_realm_seq") + ", 'carnot', p.oid, 'CARNOT', NULL "
                  + "FROM " +  getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)) + " p "
                  + "WHERE p.id = 'default'");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(UserBean.FIELD__OID);
            columns.add(UserBean.FIELD__ACCOUNT);
            columns.add(UserBean.FIELD__FIRST_NAME);
            columns.add(UserBean.FIELD__LAST_NAME);
            columns.add(UserBean.FIELD__PASSWORD);
            columns.add(UserBean.FIELD__EMAIL);
            columns.add(UserBean.FIELD__VALID_FROM);
            columns.add(UserBean.FIELD__VALID_TO);
            columns.add(UserBean.FIELD__DESCRIPTION);
            columns.add(UserBean.FIELD__FAILED_LOGIN_COUNT);
            columns.add(UserBean.FIELD__LAST_LOGIN_TIME);
            columns.add(UserBean.FIELD__REALM);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "SELECT " + dbDescriptor.getNextValForSeqString(schemaName, "user_seq") + ", 'motu', 'Master', 'Of the Universe', 'motu', NULL, "
                  + TimestampProviderUtils.getTimeStampValue() + ", "
                  + "0, NULL, 0, 0, r.oid "
                  + "FROM " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserRealmBean.TABLE_NAME)) + " r "
                  + "WHERE r.id = 'carnot'");
            ps.println(statementDelimiter);
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            List columns = new ArrayList();
            columns.add(PropertyPersistor.FIELD__PARTITION);
            columns.add(PropertyPersistor.FIELD__NAME);
            columns.add(PropertyPersistor.FIELD__VALUE);
            columns.add(PropertyPersistor.FIELD__LOCALE);
            columns.add(PropertyPersistor.FIELD__FLAGS);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(PropertyPersistor.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (-1, 'sysop.password', 'sysop', 'DEFAULT', 0)");
            ps.println(statementDelimiter);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(PropertyPersistor.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (-1, 'carnot.version', '" + CurrentVersion.getVersionName()
                  + "', 'DEFAULT', 0)");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(AuditTrailPartitionBean.FIELD__ID);
            columns.add(AuditTrailPartitionBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES ('default', NULL)");
            ps.println(statementDelimiter);
            if (session.isUsingLockTables()
                  && TypeDescriptor.get(AuditTrailPartitionBean.class)
                        .isDistinctLockTableName())
            {
               columns = new ArrayList();
               columns.add(AuditTrailPartitionBean.FIELD__OID);
               ps.println(
                     "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.LOCK_TABLE_NAME))
                     + buildColumnsFragment(dbDescriptor, columns)
                     + "SELECT " + "p.oid "
                     + "FROM " +  getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)) + " p "
                     + "WHERE p.id = 'default'");
               ps.println(statementDelimiter);
            }

            columns = new ArrayList();
            columns.add(UserDomainBean.FIELD__ID);
            columns.add(UserDomainBean.FIELD__PARTITION);
            columns.add(UserDomainBean.FIELD__SUPERDOMAIN);
            columns.add(UserDomainBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserDomainBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "SELECT 'default', p.oid, NULL, NULL "
                  + "FROM " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)) + " p "
                  + "WHERE p.id = 'default'");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(UserDomainHierarchyBean.FIELD__SUBDOMAIN);
            columns.add(UserDomainHierarchyBean.FIELD__SUPERDOMAIN);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserDomainHierarchyBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "SELECT d.oid, d.oid "
                  + "FROM " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserDomainBean.TABLE_NAME)) + " d "
                  + "WHERE d.id = 'default'");
            ps.println(statementDelimiter);


            columns = new ArrayList();
            columns.add(UserRealmBean.FIELD__ID);
            columns.add(UserRealmBean.FIELD__PARTITION);
            columns.add(UserRealmBean.FIELD__NAME);
            columns.add(UserRealmBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserRealmBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "SELECT 'carnot', p.oid, 'CARNOT', NULL "
                  + "FROM " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)) + " p "
                  + "WHERE p.id = 'default'");
            ps.println(statementDelimiter);


            columns = new ArrayList();
            columns.add(UserBean.FIELD__ACCOUNT);
            columns.add(UserBean.FIELD__FIRST_NAME);
            columns.add(UserBean.FIELD__LAST_NAME);
            columns.add(UserBean.FIELD__PASSWORD);
            columns.add(UserBean.FIELD__EMAIL);
            columns.add(UserBean.FIELD__VALID_FROM);
            columns.add(UserBean.FIELD__VALID_TO);
            columns.add(UserBean.FIELD__DESCRIPTION);
            columns.add(UserBean.FIELD__FAILED_LOGIN_COUNT);
            columns.add(UserBean.FIELD__LAST_LOGIN_TIME);
            columns.add(UserBean.FIELD__REALM);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "SELECT 'motu', 'Master', 'Of the Universe', 'motu', NULL, "
                  + TimestampProviderUtils.getTimeStampValue() + ", "
                  + "0, NULL, 0, 0, r.oid "
                  + "FROM " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserRealmBean.TABLE_NAME)) + " r "
                  + "WHERE r.id = 'carnot'");
            ps.println(statementDelimiter);
         }
         else
         {
            List columns = new ArrayList();
            columns.add(PropertyPersistor.FIELD__OID);
            columns.add(PropertyPersistor.FIELD__PARTITION);
            columns.add(PropertyPersistor.FIELD__NAME);
            columns.add(PropertyPersistor.FIELD__VALUE);
            columns.add(PropertyPersistor.FIELD__LOCALE);
            columns.add(PropertyPersistor.FIELD__FLAGS);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(PropertyPersistor.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (1, -1, 'sysop.password', 'sysop', 'DEFAULT', 0)");
            ps.println(statementDelimiter);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(PropertyPersistor.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (2, -1, 'carnot.version', '"
                  + CurrentVersion.getVersionName() + "', 'DEFAULT', 0)");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(AuditTrailPartitionBean.FIELD__OID);
            columns.add(AuditTrailPartitionBean.FIELD__ID);
            columns.add(AuditTrailPartitionBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (1, 'default', NULL)");
            ps.println(statementDelimiter);
            if (session.isUsingLockTables()
                  && TypeDescriptor.get(AuditTrailPartitionBean.class)
                        .isDistinctLockTableName())
            {
               columns = new ArrayList();
               columns.add(AuditTrailPartitionBean.FIELD__OID);
               ps.println(
                     "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.LOCK_TABLE_NAME))
                     + buildColumnsFragment(dbDescriptor, columns)
                     + "VALUES (1)");
               ps.println(statementDelimiter);
            }

            columns = new ArrayList();
            columns.add(UserDomainBean.FIELD__OID);
            columns.add(UserDomainBean.FIELD__ID);
            columns.add(UserDomainBean.FIELD__PARTITION);
            columns.add(UserDomainBean.FIELD__SUPERDOMAIN);
            columns.add(UserDomainBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserDomainBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (1, 'default', 1, NULL, NULL)");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(UserDomainHierarchyBean.FIELD__OID);
            columns.add(UserDomainHierarchyBean.FIELD__SUBDOMAIN);
            columns.add(UserDomainHierarchyBean.FIELD__SUPERDOMAIN);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserDomainHierarchyBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (1, 1, 1)");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(UserRealmBean.FIELD__OID);
            columns.add(UserRealmBean.FIELD__ID);
            columns.add(UserRealmBean.FIELD__PARTITION);
            columns.add(UserRealmBean.FIELD__NAME);
            columns.add(UserRealmBean.FIELD__DESCRIPTION);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserRealmBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (1, 'carnot', 1, 'CARNOT', NULL)");
            ps.println(statementDelimiter);

            columns = new ArrayList();
            columns.add(UserBean.FIELD__OID);
            columns.add(UserBean.FIELD__ACCOUNT);
            columns.add(UserBean.FIELD__FIRST_NAME);
            columns.add(UserBean.FIELD__LAST_NAME);
            columns.add(UserBean.FIELD__PASSWORD);
            columns.add(UserBean.FIELD__EMAIL);
            columns.add(UserBean.FIELD__VALID_FROM);
            columns.add(UserBean.FIELD__VALID_TO);
            columns.add(UserBean.FIELD__DESCRIPTION);
            columns.add(UserBean.FIELD__FAILED_LOGIN_COUNT);
            columns.add(UserBean.FIELD__LAST_LOGIN_TIME);
            columns.add(UserBean.FIELD__REALM);
            ps.println(
                  "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(UserBean.TABLE_NAME))
                  + buildColumnsFragment(dbDescriptor, columns)
                  + "VALUES (1, 'motu', 'Master', 'Of the Universe', 'motu', NULL, "
                  + TimestampProviderUtils.getTimeStampValue() + ",0 , NULL, 0, 0, 1)");
            ps.println(statementDelimiter);

            ps.println(
                  "UPDATE " + getQualifiedName(schemaName, "sequence_helper") + " SET value=3 WHERE name='property_seq'");
            ps.println(statementDelimiter);
            ps.println(
                  "UPDATE " + getQualifiedName(schemaName, "sequence_helper") + " SET value=2 WHERE name='partition_seq'");
            ps.println(statementDelimiter);
            ps.println(
                  "UPDATE " + getQualifiedName(schemaName, "sequence_helper") + " SET value=2 WHERE name='domain_seq'");
            ps.println(statementDelimiter);
            ps.println(
                  "UPDATE " + getQualifiedName(schemaName, "sequence_helper") + " SET value=2 WHERE name='domain_hierarchy_seq'");
            ps.println(statementDelimiter);
            ps.println(
                  "UPDATE " + getQualifiedName(schemaName, "sequence_helper") + " SET value=2 WHERE name='wfuser_realm_seq'");
            ps.println(statementDelimiter);
            ps.println(
                  "UPDATE " + getQualifiedName(schemaName, "sequence_helper") + " SET value=2 WHERE name='user_seq'");
            ps.println(statementDelimiter);

         }

         insertPredefinedLinkTypes(schemaName, PredefinedConstants.DEFAULT_PARTITION_ID, statementDelimiter, ps);

         ps.println("COMMIT");
         ps.println(statementDelimiter);
         ps.println();
         ps.close();
      }
      catch (IOException x)
      {
         throw new PublicException(BpmRuntimeError.JDBC_CANNOT_WRITE_TO_FILE.raise(file
               .getName()), x);
      }
   }

   private void insertPredefinedLinkTypes(String schemaName, String partitionId, String statementDelimiter, PrintStream ps)
   {
      int oid = 0;
      for (PredefinedProcessInstanceLinkTypes type : PredefinedProcessInstanceLinkTypes.values())
      {
         oid++;
         ps.println(getInsertLinkTypeStatement(schemaName, type.getId(), type.getDescription(), oid, partitionId));
         ps.println(statementDelimiter);
      }
      if (!dbDescriptor.supportsSequences() && !dbDescriptor.supportsIdentityColumns())
      {
         ps.println(getUpdateLinkTypeSequenceHelperStatement(schemaName));
         ps.println(statementDelimiter);
      }
   }

   private static String getUpdateLinkTypeSequenceHelperStatement(String schemaName)
   {
      return "UPDATE " + getQualifiedName(schemaName, "sequence_helper")
          + " SET value=" + (PredefinedProcessInstanceLinkTypes.values().length + 1)
          + " WHERE name='link_type_seq'";
   }

   private String getInsertLinkTypeStatement(String schemaName, String id, String name, int index, String partitionId)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("INSERT INTO ");
      sb.append(getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(ProcessInstanceLinkTypeBean.TABLE_NAME)));
      List columns = new ArrayList();
      if (dbDescriptor.supportsSequences() || !dbDescriptor.supportsIdentityColumns())
      {
         columns.add(ProcessInstanceLinkTypeBean.FIELD__OID);
      }
      columns.add(ProcessInstanceLinkTypeBean.FIELD__ID);
      columns.add(ProcessInstanceLinkTypeBean.FIELD__DESCRIPTION);
      columns.add(ProcessInstanceLinkTypeBean.FIELD__PARTITION);
      sb.append(buildColumnsFragment(dbDescriptor, columns));
      if (dbDescriptor.supportsSequences() || dbDescriptor.supportsIdentityColumns())
      {
         sb.append("SELECT ");
      }
      else
      {
         sb.append("VALUES (");
      }
      if (dbDescriptor.supportsSequences())
      {
         sb.append(dbDescriptor.getNextValForSeqString(schemaName, "link_type_seq"));
      }
      else if (!dbDescriptor.supportsIdentityColumns())
      {
         sb.append(index);
      }
      if (dbDescriptor.supportsSequences() || !dbDescriptor.supportsIdentityColumns())
      {
         sb.append(", ");
      }
      sb.append('\'');
      sb.append(id);
      sb.append("', '");
      sb.append(name);
      sb.append("', ");
      if (dbDescriptor.supportsSequences() || dbDescriptor.supportsIdentityColumns())
      {
         sb.append("p.oid FROM ");
         sb.append(getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)));
         sb.append(" p WHERE p.id = '" + partitionId + "'");
      }
      else
      {
         sb.append(index);
         sb.append(')');
      }
      return sb.toString();
   }

   public void dumpCreateArchiveSchemaDDLToFile(File file, String schemaName,
         Collection classes)
   {
      try
      {
         PrintStream oStream = new PrintStream(new FileOutputStream(file));

         for (Iterator i = classes.iterator(); i.hasNext();)
         {
            TypeDescriptor typeManager = TypeDescriptor.get((Class) i.next());

            oStream.print(getCreateTableStatementString(schemaName, typeManager, true));
            oStream.println(";");

            String grantTarget = Parameters.instance().getString("CreateArchiveDdlGrantTarget");
            if ( !StringUtils.isEmpty(grantTarget))
            {
               String grantAllOnTableStatement = getGrantAllOnTableStatement(schemaName,
                     typeManager, grantTarget);
               if ( !StringUtils.isEmpty(grantAllOnTableStatement))
               {
                  oStream.print(grantAllOnTableStatement);
                  oStream.println(";");
               }
            }

            for (int n = 0; n < typeManager.getIndexCount(); ++n)
            {
               oStream.print(dbDescriptor.getCreateIndexStatement(schemaName,
                     typeManager.getTableName(), typeManager.getIndexDescriptor(n)));
               oStream.println(";");
            }
            oStream.println();
         }

         List columns = new ArrayList();
         columns.add(PropertyPersistor.FIELD__OID);
         columns.add(PropertyPersistor.FIELD__NAME);
         columns.add(PropertyPersistor.FIELD__VALUE);
         columns.add(PropertyPersistor.FIELD__LOCALE);
         columns.add(PropertyPersistor.FIELD__FLAGS);
         columns.add(PropertyPersistor.FIELD__PARTITION);
         oStream.println(
               "INSERT INTO " + getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(PropertyPersistor.TABLE_NAME))
               + buildColumnsFragment(dbDescriptor, columns)
               + "VALUES (1, '" + Constants.CARNOT_ARCHIVE_AUDITTRAIL + "', 'true', 'DEFAULT', 0, -1);");

         oStream.println();
         oStream.println("COMMIT;");
         oStream.println();
      }
      catch (IOException x)
      {
         throw new PublicException(BpmRuntimeError.JDBC_CANNOT_WRITE_TO_FILE.raise(file
               .getName()), x);
      }
   }

   /**
    * @param session TODO
    *
    */
   public void dumpDropSchemaDDLToFile(File file, Session session, String schemaName,
         Collection classes, String statementDelimiter)
   {
      Assert.isNotNull(file);
      statementDelimiter = getStatementDelimiter(statementDelimiter, dbDescriptor);

      try
      {
         PrintStream outStream = new PrintStream(new FileOutputStream(file));

         final String dropGlobalPkSequenceStmt = dbDescriptor.getDropGlobalPKSequenceStatementString(schemaName);
         if (dbDescriptor.supportsSequences() && dropGlobalPkSequenceStmt != null)
         {
            outStream.print(dropGlobalPkSequenceStmt);
            outStream.println(statementDelimiter);
            outStream.println();
         }

         final String dropSequenceStoredProcedureStmt = dbDescriptor.getDropSequenceStoredProcedureStatementString(schemaName);
         if (dbDescriptor.supportsSequences() && dropSequenceStoredProcedureStmt != null)
         {
            outStream.print(dropSequenceStoredProcedureStmt);
            outStream.println(statementDelimiter);
            outStream.println();
         }

         for (Iterator i = classes.iterator(); i.hasNext();)
         {
            Class type = (Class) i.next();
            TypeDescriptor tm = TypeDescriptor.get(type);

            outStream.print(getDropTableStatementString(schemaName, tm.getTableName()));
            outStream.println(statementDelimiter);

            if (dbDescriptor.supportsSequences())
            {
               String ddlSql = dbDescriptor.getDropPKSequenceStatementString(schemaName,
                     tm.getPkSequence());
               if ( !StringUtils.isEmpty(ddlSql))
               {
                  outStream.print(ddlSql);
                  outStream.println(statementDelimiter);
               }
            }

            outStream.println();

            if (session.isUsingLockTables()
                  && TypeDescriptor.get(type).isDistinctLockTableName())
            {
               outStream.print(getDropTableStatementString(schemaName, tm
                     .getLockTableDescriptor().getTableName()));
               outStream.println(statementDelimiter);

               outStream.println();
            }
         }

         outStream.println();
         outStream.close();
      }
      catch (IOException x)
      {
         throw new PublicException(BpmRuntimeError.JDBC_CANNOT_WRITE_TO_FILE.raise(file
               .getName()), x);
      }
   }

   /**
    * Returns the statement delimiter which shall be for the current statement.
    * If statementDelimiter is null the predefined delimiter will be taken from dbDescriptor.
    *
    * @param statementDelimiter
    * @param dbDescriptor
    * @return
    */
   private static String getStatementDelimiter(String statementDelimiter,
         DBDescriptor dbDescriptor)
   {
      if (StringUtils.isEmpty(statementDelimiter))
      {
         return dbDescriptor.getStatementDelimiter();
      }

      return statementDelimiter;
   }

   public boolean containsTable(String schemaName, String tableName, Connection connection)
         throws SQLException
   {
      if (DBMSKey.DB2_UDB.equals(dbDescriptor.getDbmsKey()))
      {
         String userId = Parameters.instance().getString(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX);

         if (StringUtils.isEmpty(schemaName) && !StringUtils.isEmpty(userId))
         {
            schemaName = userId;
         }
      }

      final String pureSchemaName = !StringUtils.isEmpty(schemaName)
            ? schemaName.toUpperCase()
            : null;
      final String pureTableName = tableName.toUpperCase();

      String catalog = null;
      if (DBMSKey.MYSQL.equals(dbDescriptor.getDbmsKey()) || DBMSKey.MYSQL_SEQ.equals(dbDescriptor.getDbmsKey()))
      {
         // MySQL needs catalog to be set to schema name
         catalog = schemaName;
      }

      try
      {
         DatabaseMetaData metaData = connection.getMetaData();
         ResultSet resultSet = null;
         try
         {
            resultSet = metaData.getTables(catalog, pureSchemaName, pureTableName,
                  new String[]{"TABLE", "SYNONYM", "ALIAS"});

            if (resultSet.next())
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Table " + tableName.toUpperCase() + " found.");
               }
               return true;
            }
            else if (DBMSKey.MYSQL.equals(dbDescriptor.getDbmsKey()) || DBMSKey.MYSQL_SEQ.equals(dbDescriptor.getDbmsKey()))
            {
               // MySQL needs catalog to be set to schema name
               ResultSet rsWithCatalog = null;
               try
               {
                  rsWithCatalog = metaData.getTables(catalog, schemaName, tableName,
                        new String[] {"TABLE", "SYNONYM", "ALIAS"});
                  final boolean foundExactCase = rsWithCatalog.next();

                  if (foundExactCase)
                  {
                     trace.debug("Table " + tableName + " found.");
                  }

                  return foundExactCase;
               }
               finally
               {
                  QueryUtils.closeResultSet(rsWithCatalog);
               }
            }
            else
            {
               return false;
            }
         }
         finally
         {
            QueryUtils.closeResultSet(resultSet);
         }
      }
      catch (SQLException x)
      {
         throw new InternalException(
               "Cannot lookup table " + tableName.toUpperCase() + ".", x);
      }
   }

   public void synchronizeLockTableForClass(Class type, Connection connection,
         String schemaName, PrintStream spoolFile, String statementDelimiter)
   {
      TypeDescriptor typeManager = TypeDescriptor.get(type);

      final String dataTable = getQualifiedName(schemaName, typeManager.getTableName());
      final String lockTable = getQualifiedName(schemaName,
            typeManager.getLockTableName());

      try
      {
         Field[] pkFields = typeManager.getPkFields();

         StringBuffer delBuffer = new StringBuffer(200);
         delBuffer
               .append("DELETE FROM ").append(lockTable)
               .append(" WHERE NOT EXISTS( SELECT 'x' ")
               .append("  FROM ").append(dataTable).append(" DAT ")
               .append(" WHERE ");
         for (int i = 0; i < pkFields.length; i++ )
         {
            if (0 < i)
            {
               delBuffer.append(" AND ");
            }
            delBuffer.append("DAT.").append(pkFields[i].getName()).append(" = ")
                  .append(lockTable).append(".").append(pkFields[i].getName());
         }
         delBuffer.append(" )");

         executeOrSpoolStatement(delBuffer.toString(), connection, spoolFile);

         StringBuffer insBuffer = new StringBuffer(200);
         insBuffer
               .append("INSERT INTO ").append(lockTable).append(" ")
               .append("SELECT ");
         for (int i = 0; i < pkFields.length; i++ )
         {
            if (0 < i)
            {
               insBuffer.append(", ");
            }
            insBuffer.append(pkFields[i].getName());
         }
         insBuffer
               .append("  FROM ").append(dataTable).append(" DAT ")
               .append(" WHERE NOT EXISTS( SELECT 'x' ")
               .append("  FROM ").append(lockTable).append(" LCK ")
               .append(" WHERE ");
         for (int i = 0; i < pkFields.length; i++ )
         {
            if (0 < i)
            {
               insBuffer.append(" AND ");
            }
            insBuffer
                  .append("DAT.").append(pkFields[i].getName())
                  .append(" = LCK.").append(pkFields[i].getName());
         }
         insBuffer.append(" )");

         executeOrSpoolStatement(insBuffer.toString(), connection, spoolFile);
      }
      catch (SQLException x)
      {
         String message = "Couldn't synchronize lock table '"
               + getQualifiedName(schemaName, typeManager.getLockTableName()) + "'."
               + " Reason: " + x.getMessage();
         System.out.println(message);
         trace.warn(message, x);
      }

      try
      {
         connection.commit();
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
   }


   public void verifyLockTableForClass(Class type, Connection connection, String schemaName)
   {
      TypeDescriptor typeManager = TypeDescriptor.get(type);

      final String dataTable = getQualifiedName(schemaName, typeManager.getTableName());
      final String lockTable = getQualifiedName(schemaName,
            typeManager.getLockTableName());

      Statement verifyStmt = null;
      ResultSet resultSet = null;

      try
      {
         if ( !containsTable(schemaName, typeManager.getLockTableName(), connection))
         {
            String message = "Locking table " + lockTable + " does not exist.";

            System.out.println(message);
            trace.warn(message);
         }
         else
         {
            Field[] pkFields = typeManager.getPkFields();

            StringBuffer missingLocksBuffer = new StringBuffer(200);
            missingLocksBuffer
                  .append("SELECT 'x' FROM ").append(lockTable).append(" LCK ")
                  .append(" WHERE NOT EXISTS( SELECT 'x' ")
                  .append("  FROM ").append(dataTable).append(" DAT ")
                  .append(" WHERE ");
            for (int i = 0; i < pkFields.length; i++ )
            {
               if (0 < i)
               {
                  missingLocksBuffer.append(" AND ");
               }
               missingLocksBuffer
                     .append("DAT.").append(pkFields[i].getName())
                     .append(" = LCK.").append(pkFields[i].getName());
            }
            missingLocksBuffer.append(" )");

            verifyStmt = connection.createStatement();
            verifyStmt.execute(missingLocksBuffer.toString());
            resultSet = verifyStmt.getResultSet();

            if (resultSet.next())
            {
               String message = "Locking table " + lockTable + " is not consistent. "
                     + "Missing lock entries.";

               System.out.println(message);
               trace.warn(message);
            }

            StringBuffer danglingLocksBuffer = new StringBuffer(200);
            danglingLocksBuffer
                  .append("SELECT 'x' FROM ").append(dataTable).append(" DAT ")
                  .append(" WHERE NOT EXISTS( SELECT 'x' ")
                  .append("  FROM ").append(lockTable).append(" LCK ")
                  .append(" WHERE ");
            for (int i = 0; i < pkFields.length; i++ )
            {
               if (0 < i)
               {
                  danglingLocksBuffer.append(" AND ");
               }
               danglingLocksBuffer
                     .append("DAT.").append(pkFields[i].getName())
                     .append(" = LCK.").append(pkFields[i].getName());
            }
            danglingLocksBuffer.append(" )");
            verifyStmt.execute(danglingLocksBuffer.toString());
            resultSet = verifyStmt.getResultSet();

            if (resultSet.next())
            {
               String message = "Locking table " + lockTable + " is not consistent. "
                     + "Dangling lock entries.";

               System.out.println(message);
               trace.warn(message);
            }
         }
      }
      catch (SQLException x)
      {
         String message = "Couldn't verify lock table '"
               + lockTable + "'."
               + " Reason: " + x.getMessage();
         System.out.println(message);
         trace.warn(message, x);
      }
      finally
      {
         QueryUtils.closeStatementAndResultSet(verifyStmt, resultSet);
      }
   }


   public void createDataClusterTable(DataCluster dataCluster, Connection connection,
         String schemaName, PrintStream spoolFile, String statementDelimiter)
   {
      if (isPersistentDataTable(dataCluster.getTableName()))
      {
         throw new PublicException(
               BpmRuntimeError.JDBC_DATA_CLUSTER_TABLE_NOT_ALLOWED_BECAUSE_NAME_PREDEFINED_BY_IPP_ENGINE
                     .raise(dataCluster.getTableName()));
      }
      try
      {
         if ( !containsTable(schemaName, dataCluster.getTableName(), connection))
         {
            // Create table

            try
            {
               List columnDescriptors = new ArrayList();
               ColumnDescriptor colDescr = ColumnDescriptor
                     .create(ProcessInstanceBean.class, ProcessInstanceBean.FIELD__OID,
                           dbDescriptor);
               colDescr.setName(dataCluster.getProcessInstanceColumn());
               colDescr.setColumnQualifier(null);
               columnDescriptors.add(colDescr);

               for (DataSlot slot : dataCluster.getAllSlots())
               {
                  colDescr = ColumnDescriptor.create(DataValueBean.class,
                        DataValueBean.FIELD__OID, dbDescriptor);
                  colDescr.setName(slot.getOidColumn());
                  colDescr.setColumnQualifier(null);
                  columnDescriptors.add(colDescr);

                  colDescr = ColumnDescriptor.create(DataValueBean.class,
                        DataValueBean.FIELD__TYPE_KEY, dbDescriptor);
                  colDescr.setName(slot.getTypeColumn());
                  colDescr.setColumnQualifier(null);
                  columnDescriptors.add(colDescr);

                  if ( !StringUtils.isEmpty(slot.getSValueColumn()))
                  {
                     colDescr = ColumnDescriptor.create(DataValueBean.class,
                           DataValueBean.FIELD__STRING_VALUE, dbDescriptor);
                     colDescr.setName(slot.getSValueColumn());
                     colDescr.setColumnQualifier(null);
                     columnDescriptors.add(colDescr);
                  }

                  if ( !StringUtils.isEmpty(slot.getNValueColumn()))
                  {
                     colDescr = ColumnDescriptor.create(DataValueBean.class,
                           DataValueBean.FIELD__NUMBER_VALUE, dbDescriptor);
                     colDescr.setName(slot.getNValueColumn());
                     colDescr.setColumnQualifier(null);
                     columnDescriptors.add(colDescr);
                  }

                  if ( !StringUtils.isEmpty(slot.getDValueColumn()))
                  {
                     colDescr = ColumnDescriptor.create(DataValueBean.class,
                           DataValueBean.FIELD__DOUBLE_VALUE, dbDescriptor);
                     colDescr.setName(slot.getDValueColumn());
                     colDescr.setColumnQualifier(null);
                     columnDescriptors.add(colDescr);
                  }
               }

               String createString = getGenericCreateTableStatementString(schemaName,
                     dataCluster.getTableName(), columnDescriptors);

               executeOrSpoolStatement(createString, connection, spoolFile);
               trace.debug("Table created.");
            }
            catch (SQLException e)
            {
               String message = "Error creating data cluster table '"
                     + getQualifiedName(schemaName, dataCluster.getTableName())
                     + "'. Reason: " + e.getMessage();
               System.out.println(message);
               trace.warn(message, e);
            }

            // Create index

            try
            {
               Set clusterIndexes = dataCluster.getIndexes().entrySet();
               for (Iterator i = clusterIndexes.iterator(); i.hasNext();)
               {
                  DataClusterIndex index = (DataClusterIndex) ((Map.Entry) i.next())
                        .getValue();

                  IndexDescriptor indexDscr = new IndexDescriptor(
                        index.getIndexName(),
                        (String[]) index.getColumnNames().toArray(new String[0]),
                        index.isUnique());

                  String createString = dbDescriptor.getCreateIndexStatement(schemaName,
                        index.getTableName(), indexDscr);
                  executeOrSpoolStatement(createString, connection, spoolFile);
               }
            }
            catch (SQLException e)
            {
               String message = "Error creating index for lock table '"
                     + getQualifiedName(schemaName, dataCluster.getTableName())
                     + "'. Reason: " + e.getMessage();
               System.out.println(message);
               trace.warn(message, e);
            }

            trace.debug("Indexes created.");
            connection.commit();
         }
         else
         {
            String message = "Table '"
                  + getQualifiedName(schemaName, dataCluster.getTableName())
                  + "' already exists";
            System.out.println(message);
            trace.warn(message);
         }
      }
      catch (SQLException x)
      {
         throw new InternalException("While checking existence of table '" +
               dataCluster.getTableName() + "'.", x);
      }
   }

   private String getDataValueField(DataSlotFieldInfo dataSlotField)
   {
      if(dataSlotField.isOidColumn())
      {
         return DataValueBean.FIELD__OID;
      }
      else if(dataSlotField.isTypeColumn())
      {
         return DataValueBean.FIELD__TYPE_KEY;
      }
      else if(dataSlotField.isSValueColumn())
      {
         return DataValueBean.FIELD__STRING_VALUE;
      }
      else if(dataSlotField.isNValueColumn())
      {
         return DataValueBean.FIELD__NUMBER_VALUE;
      }
      else if(dataSlotField.isDValueColumn())
      {
         return DataValueBean.FIELD__DOUBLE_VALUE;
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.JDBC_CANNOT_CREATE_DATA_VALUE_FIELD_FOR_SLOT_COLUMN.raise(
                     dataSlotField.getName(), dataSlotField.getSlotType()));
      }
   }

   private String getColumnValuesSelect(Collection<DataSlotFieldInfo> columns, String dataValuePrefix, String dataValueSelect)
   {
      StringBuilder builder = new StringBuilder();
      builder.append("SELECT ");
      Iterator<DataSlotFieldInfo> i = columns.iterator();
      while(i.hasNext())
      {
         DataSlotFieldInfo tmp = i.next();
         builder.append(dataValuePrefix);
         builder.append(".");
         builder.append(getDataValueField(tmp));
         if(i.hasNext())
         {
            builder.append(", ");
         }
      }

      builder.append(dataValueSelect);
      return builder.toString();
   }

   private String getColumnValueSelect(DataSlotFieldInfo fieldInfo, String dataValuePrefix, String dataValueSelect)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(fieldInfo.getName()).append(" = ");
      builder.append("(");
      builder.append("SELECT ");
      builder.append(dataValuePrefix);
      builder.append(".");
      builder.append(getDataValueField(fieldInfo));
      builder.append(dataValueSelect);
      builder.append(")");
      return builder.toString();

   }

   private static String getStateListValues(DataCluster dataCluster)
   {
      StringBuilder stateListBuilder = new StringBuilder();
      Set<DataClusterEnableState> enableStates = dataCluster.getEnableStates();
      for(DataClusterEnableState enableState:enableStates)
      {
         ProcessInstanceState[] piStates = enableState.getPiStates();
         for(int i=0; i< piStates.length; i++)
         {
            ProcessInstanceState piState = piStates[i];
            int stateValue = piState.getValue();
            stateListBuilder.append(stateValue);
            if(i + 1 < piStates.length)
            {
               stateListBuilder.append(", ");
            }
         }
      }

      return stateListBuilder.toString();
   }

   public void synchronizeDataCluster(boolean performDeleteOrInsert, DataClusterSynchronizationInfo syncInfo, Connection connection,
         String schemaName, PrintStream spoolFile, String statementDelimiter, PrintStream consoleLog)
   {
      int count = 0;
      for(DataCluster dataCluster : syncInfo.getClusters())
      {
         final String processInstanceScopeTable = getQualifiedName(schemaName,
               TypeDescriptor.get(ProcessInstanceScopeBean.class).getTableName());
         final String processInstanceTable = getQualifiedName(schemaName, TypeDescriptor.get(
               ProcessInstanceBean.class).getTableName());
         final String dataValueTable = getQualifiedName(schemaName, TypeDescriptor.get(
               DataValueBean.class).getTableName());
         final String dataTable = getQualifiedName(schemaName, TypeDescriptor.get(
               AuditTrailDataBean.class).getTableName());
         final String modelTable = getQualifiedName(schemaName, TypeDescriptor.get(
               ModelPersistorBean.class).getTableName());
         final String structuredDataTable = getQualifiedName(schemaName, TypeDescriptor.get(
               StructuredDataBean.class).getTableName());
         final String structuredDataValueTable = getQualifiedName(schemaName, TypeDescriptor.get(
               StructuredDataValueBean.class).getTableName());
         final String clusterTable = getQualifiedName(schemaName, dataCluster.getTableName());

         try
         {
            int result = 0;
            if (!containsTable(schemaName, dataCluster.getTableName(), connection))
            {
               createDataClusterTable(dataCluster, connection, schemaName, null, null);
               String message = "Cluster table " + clusterTable
                     + " does not exist: Created table now.";
               printLogMessage(message, consoleLog);
               count++;
            }
            if(performDeleteOrInsert)
            {
               String syncDelSql = MessageFormat.format(
                     "DELETE FROM {0}"
                   + " WHERE NOT EXISTS ("
                   + "  SELECT ''x'' "
                   + "   FROM {2} PIS "
                   + "    INNER JOIN {4} PI ON("
                   + "     PI.{5} = PIS.{3}   "
                   + "      AND PI.{6} IN({7})    "
                   + "    )"
                   + "   WHERE PIS.{3} = {0}.{1}"
                   +"  )",
                   new Object[] {
                         clusterTable,
                         dataCluster.getProcessInstanceColumn(),
                         processInstanceScopeTable,
                         ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE,
                         processInstanceTable,
                         ProcessInstanceBean.FIELD__OID,
                         ProcessInstanceBean.FIELD__STATE,
                         getStateListValues(dataCluster)
                   });
               syncDelSql = syncDelSql.trim();
               result = executeOrSpoolStatement(syncDelSql, connection, spoolFile);
               if (result > 0)
               {
                  String message = "Inconsistent cluster table "
                        + clusterTable
                        + ": deleted cluster entries that referenced non existing process instances.";
                  printLogMessage(message, consoleLog);
                  count++;
               }
                  
               String syncInsSql = MessageFormat.format(
                     "INSERT INTO {0} ({1}) "
                   + "SELECT DISTINCT PIS.{3} "
                   + "  FROM {2} PIS "
                   + "   INNER JOIN {4} PI ON("
                   + "    PI.{5} = PIS.{3}"
                   + "      AND PI.{6} IN({7})"
                   + "   )                    "
                   + " WHERE NOT EXISTS ("
                   + "  SELECT ''x'' "
                   + "    FROM {0} DC "
                   + "   WHERE PIS.{3} = DC.{1}"
                   + " )",
                   new Object[] {
                         clusterTable,
                         dataCluster.getProcessInstanceColumn(),
                         processInstanceScopeTable,
                         ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE,
                         processInstanceTable,
                         ProcessInstanceBean.FIELD__OID,
                         ProcessInstanceBean.FIELD__STATE,
                         getStateListValues(dataCluster)});
               result = executeOrSpoolStatement(syncInsSql, connection, spoolFile);
               if (result > 0)
               {
                  String message = "Inconsistent cluster table "
                        + clusterTable
                        + ": Inserted missing existing process instances into cluster table.";
                  printLogMessage(message, consoleLog);
                  count++;
               }
            }

            // synchronizing slot values
            for (DataSlot dataSlot : syncInfo.getDataSlots(dataCluster))
            {
               String subselectSql;
               String dataValuePrefix;
               if (StringUtils.isEmpty(dataSlot.getAttributeName()))
               {
                  // normal data
                  subselectSql = MessageFormat.format(
                          "  FROM {3} dv, {4} d, {5} m"
                        + " WHERE {0}.{1} = dv." + DataValueBean.FIELD__PROCESS_INSTANCE
                        + "   AND dv." + DataValueBean.FIELD__DATA + " = d." + AuditTrailDataBean.FIELD__OID
                        + "   AND dv." + DataValueBean.FIELD__MODEL + " = d." + AuditTrailDataBean.FIELD__MODEL
                        + "   AND d." + AuditTrailDataBean.FIELD__MODEL + " = m." + ModelPersistorBean.FIELD__OID
                        + "   AND d." + AuditTrailDataBean.FIELD__ID + " = ''{2}''"
                        + "   AND m." + ModelPersistorBean.FIELD__ID + " = ''{6}''"
                        ,
                        new Object[] {
                              clusterTable, // 0
                              dataCluster.getProcessInstanceColumn(), // 1
                              dataSlot.getDataId(), // 2
                              dataValueTable, // 3
                              dataTable, // 4
                              modelTable, // 5
                              dataSlot.getModelId() // 6
                              });
                  dataValuePrefix = "dv";
               }
               else
               {
                  // structured data
                  subselectSql = MessageFormat.format(
                        "  FROM {3} d, {4} sd, {5} sdv, {6} p, {8} m"
                      + " WHERE {0}.{1} = sdv." + StructuredDataValueBean.FIELD__PROCESS_INSTANCE
                      + "   AND sdv." + StructuredDataValueBean.FIELD__XPATH + " = sd." + StructuredDataBean.FIELD__OID
                      + "   AND sd." + DataValueBean.FIELD__DATA + " = d." + AuditTrailDataBean.FIELD__OID
                      + "   AND sd." + DataValueBean.FIELD__MODEL + " = d." + AuditTrailDataBean.FIELD__MODEL
                      + "   AND p." + ProcessInstanceBean.FIELD__OID + " = sdv." + StructuredDataValueBean.FIELD__PROCESS_INSTANCE
                      + "   AND p." + ProcessInstanceBean.FIELD__MODEL + " = d." + DataValueBean.FIELD__MODEL
                      + "   AND sd." + StructuredDataBean.FIELD__XPATH + " = ''{7}'' "
                      + "   AND d." + AuditTrailDataBean.FIELD__ID + " = ''{2}''"
                      + "   AND d."+ DataValueBean.FIELD__MODEL + " = m." + ModelPersistorBean.FIELD__OID
                      + "   AND m." + ModelPersistorBean.FIELD__ID + " = ''{9}''"
                      ,
                      new Object[] {
                            clusterTable,
                            dataCluster.getProcessInstanceColumn(),
                            dataSlot.getDataId(),
                            dataTable, // 3
                            structuredDataTable, // 4
                            structuredDataValueTable, // 5
                            processInstanceTable, // 6
                            dataSlot.getAttributeName(), // 7
                            modelTable, // 8
                            dataSlot.getModelId() // 9
                            });
                  dataValuePrefix = "sdv";
               }

               Collection<DataSlotFieldInfo>
                  dataSlotColumnsToSynch = syncInfo.getDataSlotColumns(dataSlot);
               if(dataSlotColumnsToSynch.size() != 0)
               {
                  StringBuffer buffer = new StringBuffer(1000);
                  buffer.append("UPDATE ").append(clusterTable)
                        .append(" SET ");

                  if (dbDescriptor.supportsMultiColumnUpdates())
                  {
                     buffer.append("(");
                     //which field need to be updated in the cluster table
                     Iterator<DataSlotFieldInfo> i = dataSlotColumnsToSynch.iterator();
                     while(i.hasNext())
                     {
                        DataSlotFieldInfo dataSlotColumn = i.next();
                        buffer.append(dataSlotColumn.getName());
                        if(i.hasNext())
                        {
                           buffer.append(", ");
                        }
                     }
                     buffer.append(")");
                     buffer.append(" = ");
                     buffer.append("(");
                     buffer.append(getColumnValuesSelect(dataSlotColumnsToSynch, dataValuePrefix, subselectSql));
                     buffer.append(")");
                  }
                  else
                  {
                     Iterator<DataSlotFieldInfo> i = dataSlotColumnsToSynch.iterator();
                     while(i.hasNext())
                     {
                        DataSlotFieldInfo dataSlotColumn = i.next();
                        String updateColumnValueStmt = getColumnValueSelect(dataSlotColumn, dataValuePrefix, subselectSql);
                        buffer.append(updateColumnValueStmt);
                        if(i.hasNext())
                        {
                           buffer.append(", ");
                        }
                     }
                  }

                  result = executeOrSpoolStatement(buffer.toString(), connection, spoolFile);
                  if (result > 0)
                  {
                     String message = "Inconsistent cluster table "
                           + clusterTable
                           + ": Fixed cluster entries that contained invalid data values for slot '"
                           + dataSlot.getDataId() + "' attributeName '"
                           + dataSlot.getAttributeName() + "'.";
                     printLogMessage(message, consoleLog);
                     count++;
                  }
               }
            }
         }
         catch (SQLException x)
         {
            String message = "Couldn't synchronize data cluster table '" + clusterTable
                  + "'. Reason: " + x.getMessage();
            System.out.println(message);
            trace.warn(message, x);
            DataClusterHelper.deleteDataClusterSetup();
         }
      }
      if (count != 0)
      {
         StringBuilder message = new StringBuilder();
         message.append("Synchronized data cluster: ");
         message.append(count == 1 ? "There was 1 inconsistency. " : "There were "
               + count + " inconsistencies. ");
         message.append("All inconsistencies have been resolved now.");
         printLogMessage(message.toString(), consoleLog != null ? consoleLog : System.out);
      }
      else
      {
         printLogMessage(
               "Synchronized data cluster. There were no inconsistencies to be resolved.",
               consoleLog != null ? consoleLog : System.out);
      }
   }

   public String getGenericCreateTableStatementString(String schemaName, String tableName,
         List columnDescriptors)
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("CREATE TABLE ").append(
            getQualifiedName(schemaName, tableName)).append(" (");

      List columnList = new ArrayList();
      for (Iterator i = columnDescriptors.iterator(); i.hasNext();)
      {
         ColumnDescriptor column = (ColumnDescriptor) i.next();

         columnList.add(column.getName() + " " + column.getSqlType() + " "
               + column.getColumnQualifier());
      }

      buffer.append(StringUtils.join(columnList.iterator(), ",")).append(")");
      final String tableOptions = dbDescriptor.getCreateTableOptions();
      if ( !StringUtils.isEmpty(tableOptions))
      {
         buffer.append(" ").append(tableOptions);
      }

      return buffer.toString();
   }

   private String replaceSqlFragmentsByCluster(String sqlTemplate,
         DataCluster dataCluster, String schemaName)
   {
      TypeDescriptor typeManager = TypeDescriptor.get(ProcessInstanceBean.class);
      final String processInstanceTable = getQualifiedName(schemaName,
            typeManager.getTableName());
      final String dataValueTable = getQualifiedName(schemaName,
            TypeDescriptor.get(DataValueBean.class).getTableName());
      final String dataTable = getQualifiedName(schemaName,
            TypeDescriptor.get(AuditTrailDataBean.class).getTableName());
      final String clusterTable = getQualifiedName(schemaName,
            dataCluster.getTableName());
      final String structuredDataTable = getQualifiedName(schemaName, TypeDescriptor.get(
            StructuredDataBean.class).getTableName());
      final String structuredDataValueTable = getQualifiedName(schemaName, TypeDescriptor.get(
            StructuredDataValueBean.class).getTableName());
      final String modelTable = getQualifiedName(schemaName, TypeDescriptor.get(
            ModelPersistorBean.class).getTableName());


      String result;

      result = StringUtils.replace(sqlTemplate, "_$MODEL_TAB$_", modelTable);
      result = StringUtils.replace(result, "_$M_OID$_", ModelPersistorBean.FIELD__OID);
      result = StringUtils.replace(result, "_$M_ID$_", ModelPersistorBean.FIELD__ID);
      result = StringUtils.replace(result, "_$CLUSTER_TAB$_", clusterTable);
      result = StringUtils.replace(result, "_$DC_PROCINSTANCE$_", dataCluster
            .getProcessInstanceColumn());
      result = StringUtils.replace(result, "_$PI_TAB$_", processInstanceTable);
      result = StringUtils.replace(result, "_$PI_OID$_", ProcessInstanceBean.FIELD__OID);
      result = StringUtils.replace(result, "_$PI_MODEL$_",
            ProcessInstanceBean.FIELD__MODEL);
      result = StringUtils.replace(result, "_$D_TAB$_", dataTable);
      result = StringUtils.replace(result, "_$D_ID$_", AuditTrailDataBean.FIELD__ID);
      result = StringUtils.replace(result, "_$D_OID$_", AuditTrailDataBean.FIELD__OID);
      result = StringUtils
            .replace(result, "_$D_MODEL$_", AuditTrailDataBean.FIELD__MODEL);
      result = StringUtils.replace(result, "_$DV_TAB$_", dataValueTable);
      result = StringUtils.replace(result, "_$DV_OID$_", DataValueBean.FIELD__OID);
      result = StringUtils.replace(result, "_$DV_NUMBERVALUE$_", DataValueBean.FIELD__NUMBER_VALUE);
      result = StringUtils.replace(result, "_$DV_MODEL$_", DataValueBean.FIELD__MODEL);
      result = StringUtils.replace(result, "_$DV_DATA$_", DataValueBean.FIELD__DATA);
      result = StringUtils.replace(result, "_$DV_PROCINSTANCE$_",
            DataValueBean.FIELD__PROCESS_INSTANCE);
      result = StringUtils.replace(result, "_$DV_TYPE_KEY$_",
            DataValueBean.FIELD__TYPE_KEY);
      result = StringUtils.replace(result, "_$DV_PROCESSINSTANCE$_",
            DataValueBean.FIELD__PROCESS_INSTANCE);
      result = StringUtils.replace(result, "_$SD_TAB$_", structuredDataTable);
      result = StringUtils.replace(result, "_$SD_OID$_", StructuredDataBean.FIELD__OID);
      result = StringUtils.replace(result, "_$SD_XPATH$_", StructuredDataBean.FIELD__XPATH);
      result = StringUtils.replace(result, "_$SDV_TAB$_", structuredDataValueTable);
      result = StringUtils.replace(result, "_$SDV_PROCESSINSTANCE$_", StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
      result = StringUtils.replace(result, "_$SDV_XPATH$_", StructuredDataValueBean.FIELD__XPATH);

      return result;
   }

   private String replaceSqlFragmentsBySlot(String sqlTemplate, DataSlot dataSlot)
   {
      String result;

      result = StringUtils.replace(sqlTemplate, "_$DC_OID$_", dataSlot.getOidColumn());
      result = StringUtils.replace(result, "_$DC_TYPE_KEY$_", dataSlot.getTypeColumn());
      result = StringUtils.replace(result, "_$SLOT_ATTRIBUTE_NAME$_", dataSlot.getAttributeName());
      result = StringUtils.replace(result, "_$SLOT_MODEL_ID_VALUE$_", dataSlot.getModelId());

      if ( !StringUtils.isEmpty(dataSlot.getSValueColumn()))
      {
         result = StringUtils.replace(result, "_$DV_VALUE$_",
               DataValueBean.FIELD__STRING_VALUE);
         result = StringUtils.replace(result, "_$DC_VALUE$_", dataSlot.getSValueColumn());
      }
      else
      {
         result = StringUtils.replace(result, "_$DV_VALUE$_",
               DataValueBean.FIELD__NUMBER_VALUE);
         result = StringUtils.replace(result, "_$DC_VALUE$_", dataSlot.getNValueColumn());
      }

      result = StringUtils.replace(result, "_$SLOT_DATA_ID$_", dataSlot.getDataId());

      return result;
   }
   
   public void verifyClusterTable(DataCluster dataCluster, Connection connection,
         String schemaName, PrintStream consoleLog)
   {
      final String clusterTable = getQualifiedName(schemaName, dataCluster.getTableName());
      int count = 0;

      Statement verifyStmt = null;
      ResultSet resultSet = null;

      try
      {
         if ( !containsTable(schemaName, dataCluster.getTableName(), connection))
         {
            String message = "Cluster table " + clusterTable + " does not exist.";
            printLogMessage(message, consoleLog);
            count++;
         }
         else
         {
            verifyStmt = connection.createStatement();

            String syncStringTemplate =
               "SELECT 'x' FROM _$CLUSTER_TAB$_ dc " +
               "WHERE NOT EXISTS( SELECT 'x' " +
                                 "FROM _$PI_TAB$_ pi " +
                                 "WHERE dc._$DC_PROCINSTANCE$_ = pi._$PI_OID$_ )";

            String syncStmtString = replaceSqlFragmentsByCluster(syncStringTemplate,
                  dataCluster, schemaName);

            verifyStmt.execute(syncStmtString);
            resultSet = verifyStmt.getResultSet();

            if (resultSet.next())
            {
               String message = "Cluster table "
                     + clusterTable
                     + " is not consistent: non existing process instances are referenced by cluster entry.";

               printLogMessage(message, consoleLog);
               count++;
            }

            syncStringTemplate =
               "SELECT 'x' FROM _$PI_TAB$_ pi " +
               "WHERE NOT EXISTS( SELECT 'x' " +
                                 "FROM _$CLUSTER_TAB$_ dc " +
                                 "WHERE pi._$PI_OID$_ = dc._$DC_PROCINSTANCE$_ )";

            syncStmtString = replaceSqlFragmentsByCluster(syncStringTemplate,
                  dataCluster, schemaName);

            verifyStmt.execute(syncStmtString);
            resultSet = verifyStmt.getResultSet();

            if (resultSet.next())
            {
               String message = "Cluster table "
                     + clusterTable
                     + " is not consistent: existing process instances are not referenced by cluster entries.";

               printLogMessage(message, consoleLog);
               count++;
            }

            syncStringTemplate =
               "SELECT 'x' FROM _$CLUSTER_TAB$_ dc, _$PI_TAB$_ pi, _$MODEL_TAB$_ m " +
               "WHERE " +
                  "dc._$DC_PROCINSTANCE$_ = pi._$PI_OID$_ AND " +
                  "dc._$DC_OID$_ IS NOT NULL AND " +
                  "pi._$PI_MODEL$_ = m._$M_OID$_ AND " +
                  "m._$M_ID$_ = '_$SLOT_MODEL_ID_VALUE$_' AND " +
                  "NOT EXISTS( " +
                     "SELECT 'x' " +
                     "FROM _$D_TAB$_ d, _$DV_TAB$_ dv " +
                     "WHERE " +
                        "d._$D_ID$_ = '_$SLOT_DATA_ID$_' AND " +
                        "d._$D_MODEL$_ = pi._$PI_MODEL$_ AND " +
                        "d._$D_OID$_ = dv._$DV_DATA$_ AND " +
                        "d._$D_MODEL$_ = dv._$DV_MODEL$_ AND " +
                        "dv._$DV_PROCINSTANCE$_ = pi._$PI_OID$_ AND " +
                        "dv._$DV_OID$_ = dc._$DC_OID$_ AND " +
                        "dv._$DV_TYPE_KEY$_ = dc._$DC_TYPE_KEY$_ AND " +
                        "(dv._$DV_VALUE$_ = dc._$DC_VALUE$_ OR " +
                           "(dv._$DV_VALUE$_ IS NULL AND dc._$DC_VALUE$_ IS NULL)))";
            syncStringTemplate = replaceSqlFragmentsByCluster(syncStringTemplate,
                  dataCluster, schemaName);

            String syncStringTemplateStruct =
               "SELECT 'x' FROM _$CLUSTER_TAB$_ dc, _$PI_TAB$_ pi, _$MODEL_TAB$_ m " +
               "WHERE " +
                  "dc._$DC_PROCINSTANCE$_ = pi._$PI_OID$_ AND " +
                  "dc._$DC_OID$_ IS NOT NULL AND " +
                  "pi._$PI_MODEL$_ = m._$M_OID$_ AND " +
                  "m._$M_ID$_ = '_$SLOT_MODEL_ID_VALUE$_' AND " +
                  "NOT EXISTS( " +
                     "SELECT 'x' " +
                     "FROM _$D_TAB$_ d, _$DV_TAB$_ dv, _$SD_TAB$_ sd, _$SDV_TAB$_ sdv " +
                     "WHERE " +
                        "d._$D_ID$_ = '_$SLOT_DATA_ID$_' AND " +
                        "d._$D_MODEL$_ = pi._$PI_MODEL$_ AND " +
                        "d._$D_OID$_ = dv._$DV_DATA$_ AND " +
                        "d._$D_MODEL$_ = dv._$DV_MODEL$_ AND " +
                        "dv._$DV_PROCINSTANCE$_ = pi._$PI_OID$_ AND " +
                        "sdv._$SDV_PROCESSINSTANCE$_ = dv._$DV_NUMBERVALUE$_ AND " +
                        "sdv._$SDV_XPATH$_ = sd._$SD_OID$_ AND " +
                        "sd._$SD_XPATH$_ = '_$SLOT_ATTRIBUTE_NAME$_' AND " +
                        "sdv._$DV_OID$_ = dc._$DC_OID$_ AND " +
                        "sdv._$DV_TYPE_KEY$_ = dc._$DC_TYPE_KEY$_ AND " +
                        "(sdv._$DV_VALUE$_ = dc._$DC_VALUE$_ OR " +
                           "(sdv._$DV_VALUE$_ IS NULL AND dc._$DC_VALUE$_ IS NULL)))";
            syncStringTemplateStruct = replaceSqlFragmentsByCluster(syncStringTemplateStruct,
                  dataCluster, schemaName);


            for (DataSlot dataSlot : dataCluster.getAllSlots())
            {
               if (StringUtils.isEmpty(dataSlot.getAttributeName()))
               {
                  syncStmtString = replaceSqlFragmentsBySlot(syncStringTemplate, dataSlot);
               }
               else
               {
                  syncStmtString = replaceSqlFragmentsBySlot(syncStringTemplateStruct, dataSlot);
               }

               verifyStmt.execute(syncStmtString);
               resultSet = verifyStmt.getResultSet();

               if (resultSet.next())
               {
                  String message = "Cluster table "
                        + clusterTable
                        + " is not consistent: referenced data values by cluster entry for slot '"
                        + dataSlot.getQualifiedDataId() + "' attributeName '"
                        + dataSlot.getAttributeName() + "' are not matching.";

                  printLogMessage(message, consoleLog);
                  count++;
               }
            }

            syncStringTemplate =
               "SELECT 'x' FROM _$CLUSTER_TAB$_ dc, _$PI_TAB$_ pi, _$MODEL_TAB$_ m " +
               "WHERE " +
                  "dc._$DC_PROCINSTANCE$_ = pi._$PI_OID$_ AND " +
                  "dc._$DC_OID$_ IS NULL AND " +
                  "pi._$PI_MODEL$_ = m._$M_OID$_ AND " +
                  "m._$M_ID$_ = '_$SLOT_MODEL_ID_VALUE$_' AND " +
                  "(" +
                     "dc._$DC_TYPE_KEY$_ IS NOT NULL OR " +
                     "dc._$DC_VALUE$_ IS NOT NULL" +
                  ")";

            syncStringTemplate = replaceSqlFragmentsByCluster(syncStringTemplate,
                  dataCluster, schemaName);


            for (Iterator i = dataCluster.getAllSlots().iterator(); i.hasNext();)
            {
               DataSlot dataSlot = (DataSlot) i.next();

               syncStmtString = replaceSqlFragmentsBySlot(syncStringTemplate, dataSlot);

               verifyStmt.execute(syncStmtString);
               resultSet = verifyStmt.getResultSet();

               if (resultSet.next())
               {
                  String message = "Cluster table " + clusterTable
                        + " is not consistent: empty cluster entries for slot '"
                        + dataSlot.getQualifiedDataId() + "' attributeName '"
                        + dataSlot.getAttributeName()
                        + "' are not completely set to null.";

                  printLogMessage(message, consoleLog);
                  count++;
               }
            }

            syncStringTemplate =
               "SELECT 'x' FROM _$CLUSTER_TAB$_ dc, _$PI_TAB$_ pi, _$MODEL_TAB$_ m " +
               "WHERE " +
                  "dc._$DC_PROCINSTANCE$_ = pi._$PI_OID$_ AND " +
                  "dc._$DC_OID$_ IS NULL AND " +
                  "dc._$DC_TYPE_KEY$_ IS NULL AND " +
                  "dc._$DC_VALUE$_ IS NULL AND " +
                  "pi._$PI_MODEL$_ = m._$M_OID$_ AND " +
                  "m._$M_ID$_ = '_$SLOT_MODEL_ID_VALUE$_' AND " +
                  "EXISTS( " +
                     "SELECT 'x' " +
                     "FROM _$D_TAB$_ d, _$DV_TAB$_ dv " +
                     "WHERE " +
                        "d._$D_ID$_ = '_$SLOT_DATA_ID$_' AND " +
                        "d._$D_MODEL$_ = pi._$PI_MODEL$_ AND " +
                        "d._$D_OID$_ = dv._$DV_DATA$_ AND " +
                        "d._$D_MODEL$_ = dv._$DV_MODEL$_ AND " +
                        "dv._$DV_PROCINSTANCE$_ = pi._$PI_OID$_)";

            syncStringTemplate = replaceSqlFragmentsByCluster(syncStringTemplate,
                  dataCluster, schemaName);


            for (Iterator i = dataCluster.getAllSlots().iterator(); i.hasNext();)
            {
               DataSlot dataSlot = (DataSlot) i.next();

               syncStmtString = replaceSqlFragmentsBySlot(syncStringTemplate, dataSlot);

               verifyStmt.execute(syncStmtString);
               resultSet = verifyStmt.getResultSet();

               if (resultSet.next())
               {
                  String message = "Cluster table " + clusterTable
                        + " is not consistent: existing data values for slot '"
                        + dataSlot.getDataId() + "' attributeName '"
                        + dataSlot.getAttributeName()
                        + "' are not referenced by cluster entry.";

                  printLogMessage(message, consoleLog);
                  count++;
               }
            }
         }
         if (count != 0)
         {
            StringBuilder message = new StringBuilder();
            message.append("The data cluster is invalid. ");
            message.append(count == 1 ? "There is 1 inconsistency." : "There are "
                  + count + " inconsistencies.");
            printLogMessage(message.toString(), consoleLog != null
                  ? consoleLog
                  : System.out);
         }
         else
         {
            printLogMessage("Verified data cluster. There are no inconsistencies.",
                  consoleLog != null ? consoleLog : System.out);
         }
      }
      catch (SQLException x)
      {
         String message = "Couldn't verify cluster table '"
               + clusterTable + "'."
               + " Reason: " + x.getMessage();
         System.out.println(message);
         trace.warn(message, x);
      }
      finally
      {
         QueryUtils.closeStatementAndResultSet(verifyStmt, resultSet);
      }
      
      try
      {
         connection.commit();
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
   }

   private void printLogMessage(String message, PrintStream consoleLog)
   {
      if (consoleLog != null)
      {
         consoleLog.println(message);
      }
      trace.info(message);
   }

   public void dropClusterTable(DataCluster dataCluster, Connection connection,
         String schemaName, PrintStream spoolFile, String statementDelimiter)
   {
      if (isPersistentDataTable(dataCluster.getTableName()))
      {
         throw new PublicException(
               BpmRuntimeError.JDBC_DATA_CLUSTER_TABLE_NOT_ALLOWED_BECAUSE_NAME_PREDEFINED_BY_IPP_ENGINE
                     .raise(dataCluster.getTableName()));
      }

      try
      {
         String dropString = getDropTableStatementString(schemaName, dataCluster
               .getTableName());
         executeOrSpoolStatement(dropString, connection, spoolFile);
      }
      catch (SQLException x)
      {
         String message = "Couldn't drop cluster table '"
               + getQualifiedName(schemaName, dataCluster.getTableName()) + "'."
               + " Reason: " + x.getMessage();
         System.out.println(message);
         trace.warn(message, x);
      }

      try
      {
         connection.commit();
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
   }

   private boolean isUsingLockTables()
   {
      boolean useLockTablesDefault = dbDescriptor.getUseLockTablesDefault();
      return Parameters.instance().getBoolean(
            SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USE_LOCK_TABLES_SUFFIX,
            useLockTablesDefault);
   }

   public void createAuditTrailPartition(String partitionId, Connection connection,
         String schemaName, PrintStream spoolFile, String statementDelimiter)
   {
      if (!dbDescriptor.supportsSequences() && !dbDescriptor.supportsIdentityColumns())
      {
         throw new PublicException(
               BpmRuntimeError.JDBC_DATABASE_HAS_TO_SUPPORT_SEQUENCES_OR_AUTOMATIC_IDENTITY_COLUMNS
                     .raise());
      }

      // insert partition
      TypeDescriptor typeManager = TypeDescriptor.get(AuditTrailPartitionBean.class);
      String tableName = typeManager.getTableName();
      String partitionTableName = getQualifiedName(schemaName, dbDescriptor
            .quoteIdentifier(tableName));

      try
      {
         String columnPart = "<empty>";
         String valuesPart = "<empty>";

         if (dbDescriptor.supportsSequences())
         {
            List columns = new ArrayList();
            columns.add(AuditTrailPartitionBean.FIELD__OID);
            columns.add(AuditTrailPartitionBean.FIELD__ID);
            columns.add(AuditTrailPartitionBean.FIELD__DESCRIPTION);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = MessageFormat.format("({0}, ''{1}'', {2})",
                  dbDescriptor.getNextValForSeqString(schemaName, typeManager.getPkSequence()),
                  partitionId,
                  "''");
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            List columns = new ArrayList();
            columns.add(AuditTrailPartitionBean.FIELD__ID);
            columns.add(AuditTrailPartitionBean.FIELD__DESCRIPTION);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = "('" + partitionId + "', '')";
         }

         StringBuffer insBuffer = new StringBuffer(200);
         insBuffer//
               .append("INSERT INTO ").append(partitionTableName).append(columnPart)//
               .append(" VALUES").append(valuesPart);

         executeOrSpoolStatement(insBuffer.toString(), connection, spoolFile);

         // insert entry into partition's lock table if required
         if (isUsingLockTables() && typeManager.isDistinctLockTableName())
         {
            String lockTableName = typeManager.getLockTableName();
            lockTableName = getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(lockTableName));

            List columns = new ArrayList();
            columns.add(AuditTrailPartitionBean.FIELD__OID);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = MessageFormat.format("{0}",
                  dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__OID));

            insBuffer = new StringBuffer(200);
            insBuffer//
                  .append("INSERT INTO ").append(lockTableName).append(columnPart)//
                  .append(" SELECT ").append(valuesPart)
                  .append(" FROM ").append(partitionTableName)
                  .append(" WHERE ")
                  .append(dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__ID))
                  .append(" = '").append(partitionId).append("'");

            executeOrSpoolStatement(insBuffer.toString(), connection, spoolFile);
         }

         // insert partitions default domain
         typeManager = TypeDescriptor.get(UserDomainBean.class);
         tableName = typeManager.getTableName();
         String domainTableName = getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(tableName));

         if (dbDescriptor.supportsSequences())
         {
            List columns = new ArrayList();
            columns.add(UserDomainBean.FIELD__OID);
            columns.add(UserDomainBean.FIELD__ID);
            columns.add(UserDomainBean.FIELD__DESCRIPTION);
            columns.add(UserDomainBean.FIELD__PARTITION);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = MessageFormat.format("{0}, ''{1}'', {2}, {3}",
                  dbDescriptor.getNextValForSeqString(schemaName, typeManager.getPkSequence()),
                  partitionId,
                  "''",
                  dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__OID));
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            List columns = new ArrayList();
            columns.add(UserDomainBean.FIELD__ID);
            columns.add(UserDomainBean.FIELD__DESCRIPTION);
            columns.add(UserDomainBean.FIELD__PARTITION);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = MessageFormat.format("''{0}'', ''{1}'', {2}",
                  partitionId,
                  "",
                  dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__OID));
         }

         insBuffer = new StringBuffer(200);
         insBuffer//
               .append("INSERT INTO ").append(domainTableName).append(columnPart)//
               .append(" SELECT ").append(valuesPart)
               .append(" FROM ").append(partitionTableName)
               .append(" WHERE ")
               .append(dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__ID))
               .append(" = '").append(partitionId).append("'");

         executeOrSpoolStatement(insBuffer.toString(), connection, spoolFile);

         // insert partitions default domain hierarchy
         typeManager = TypeDescriptor.get(UserDomainHierarchyBean.class);
         tableName = typeManager.getTableName();
         String domainHierTableName = getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(tableName));

         if (dbDescriptor.supportsSequences())
         {
            List columns = new ArrayList();
            columns.add(UserDomainHierarchyBean.FIELD__OID);
            columns.add(UserDomainHierarchyBean.FIELD__SUPERDOMAIN);
            columns.add(UserDomainHierarchyBean.FIELD__SUBDOMAIN);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = MessageFormat.format("{0}, D.{1}, D.{2}", new Object[] {
                  dbDescriptor.getNextValForSeqString(schemaName, typeManager
                        .getPkSequence()),
                  dbDescriptor.quoteIdentifier(UserDomainBean.FIELD__OID),
                  dbDescriptor.quoteIdentifier(UserDomainBean.FIELD__OID) });
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            List columns = new ArrayList();
            columns.add(UserDomainHierarchyBean.FIELD__SUPERDOMAIN);
            columns.add(UserDomainHierarchyBean.FIELD__SUBDOMAIN);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = MessageFormat.format("D.{0}, D.{1}", new Object[] {
                  dbDescriptor.quoteIdentifier(UserDomainBean.FIELD__OID),
                  dbDescriptor.quoteIdentifier(UserDomainBean.FIELD__OID) });
         }

         insBuffer = new StringBuffer(200);
         insBuffer //
               .append("INSERT INTO ").append(domainHierTableName).append(columnPart) //
               .append(" SELECT ").append(valuesPart)
               .append(" FROM ").append(domainTableName).append(" D ,").append(partitionTableName).append(" P")
               .append(" WHERE P.").append(dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__ID))
               .append(" = '").append(partitionId).append("'")
               .append("  AND D.").append(dbDescriptor.quoteIdentifier(UserDomainBean.FIELD__PARTITION))
               .append(" = P.").append(dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__OID));

         executeOrSpoolStatement(insBuffer.toString(), connection, spoolFile);

         // insert partitions default realm
         typeManager = TypeDescriptor.get(UserRealmBean.class);
         tableName = typeManager.getTableName();
         String realmTableName = getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(tableName));

         if (dbDescriptor.supportsSequences())
         {
            List columns = new ArrayList();
            columns.add(UserRealmBean.FIELD__OID);
            columns.add(UserRealmBean.FIELD__ID);
            columns.add(UserRealmBean.FIELD__NAME);
            columns.add(UserRealmBean.FIELD__DESCRIPTION);
            columns.add(UserRealmBean.FIELD__PARTITION);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = MessageFormat.format("{0}, ''{1}'', ''{2}'', ''{3}'', {4}",
                  dbDescriptor.getNextValForSeqString(schemaName, typeManager.getPkSequence()),
                  PredefinedConstants.DEFAULT_REALM_ID,
                  PredefinedConstants.DEFAULT_REALM_NAME,
                  "",
                  dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__OID));
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            List columns = new ArrayList();
            columns.add(UserRealmBean.FIELD__ID);
            columns.add(UserRealmBean.FIELD__NAME);
            columns.add(UserRealmBean.FIELD__DESCRIPTION);
            columns.add(UserRealmBean.FIELD__PARTITION);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            valuesPart = MessageFormat.format("''{0}'', ''{1}'', ''{2}'', {3}",
                  PredefinedConstants.DEFAULT_REALM_ID,
                  PredefinedConstants.DEFAULT_REALM_NAME,
                  "",
                  dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__OID));
         }

         insBuffer = new StringBuffer(200);
         insBuffer//
               .append("INSERT INTO ").append(realmTableName).append(columnPart)//
               .append(" SELECT ").append(valuesPart)
               .append(" FROM ").append(partitionTableName)
               .append(" WHERE ")
               .append(dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__ID)).append(" = '").append(partitionId).append("'");

         executeOrSpoolStatement(insBuffer.toString(), connection, spoolFile);

         // insert default realms motu user
         typeManager = TypeDescriptor.get(UserBean.class);
         tableName = typeManager.getTableName();
         String userTableName = getQualifiedName(schemaName, dbDescriptor.quoteIdentifier(tableName));

         if (dbDescriptor.supportsSequences())
         {
            List columns = new ArrayList();
            columns.add(UserBean.FIELD__OID);
            columns.add(UserBean.FIELD__ACCOUNT);
            columns.add(UserBean.FIELD__FIRST_NAME);
            columns.add(UserBean.FIELD__LAST_NAME);
            columns.add(UserBean.FIELD__PASSWORD);
            columns.add(UserBean.FIELD__EMAIL);
            columns.add(UserBean.FIELD__VALID_FROM);
            columns.add(UserBean.FIELD__VALID_TO);
            columns.add(UserBean.FIELD__DESCRIPTION);
            columns.add(UserBean.FIELD__FAILED_LOGIN_COUNT);
            columns.add(UserBean.FIELD__LAST_LOGIN_TIME);
            columns.add(UserBean.FIELD__REALM);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            // more than 10 arguments will result in "IllegalArgumentException: argument number too large"
            // on jdk 1.3. Therefore the creation of valuesPart is splitted.
            valuesPart = MessageFormat.format("{0}, ''{1}'', ''{2}'', ''{3}'', ''{4}'', ''{5}''", new Object[] {
                  dbDescriptor.getNextValForSeqString(schemaName, typeManager
                        .getPkSequence()),
                  PredefinedConstants.MOTU,
                  PredefinedConstants.MOTU_FIRST_NAME,
                  PredefinedConstants.MOTU_LAST_NAME,
                  PredefinedConstants.MOTU,
                  "" });
            valuesPart = valuesPart + MessageFormat.format(", {0}, {1}, ''{2}'', {3}, {4}, R.{5}", new Object[] {
                  new Long(0),
                  new Long(0),
                  "",
                  new Long(0),
                  new Long(0),
                  dbDescriptor.quoteIdentifier(UserRealmBean.FIELD__OID) });
         }
         else if (dbDescriptor.supportsIdentityColumns())
         {
            List columns = new ArrayList();
            columns.add(UserBean.FIELD__ACCOUNT);
            columns.add(UserBean.FIELD__FIRST_NAME);
            columns.add(UserBean.FIELD__LAST_NAME);
            columns.add(UserBean.FIELD__PASSWORD);
            columns.add(UserBean.FIELD__EMAIL);
            columns.add(UserBean.FIELD__VALID_FROM);
            columns.add(UserBean.FIELD__VALID_TO);
            columns.add(UserBean.FIELD__DESCRIPTION);
            columns.add(UserBean.FIELD__FAILED_LOGIN_COUNT);
            columns.add(UserBean.FIELD__LAST_LOGIN_TIME);
            columns.add(UserBean.FIELD__REALM);

            columnPart = buildColumnsFragment(dbDescriptor, columns);
            // more than 10 arguments will result in "IllegalArgumentException: argument number too large"
            // on jdk 1.3. Therefore the creation of valuesPart is splitted.
            valuesPart = MessageFormat.format("''{0}'', ''{1}'', ''{2}'', ''{3}'', ''{4}''", new Object[] {
                  PredefinedConstants.MOTU,
                  PredefinedConstants.MOTU_FIRST_NAME,
                  PredefinedConstants.MOTU_LAST_NAME,
                  PredefinedConstants.MOTU,
                  "" });
            valuesPart = valuesPart + MessageFormat.format(", {0}, {1}, ''{2}'', {3}, {4}, R.{5}", new Object[] {
                  new Long(0),
                  new Long(0),
                  "",
                  new Long(0),
                  new Long(0),
                  dbDescriptor.quoteIdentifier(UserRealmBean.FIELD__OID) });
         }

         insBuffer = new StringBuffer(200);
         insBuffer//
               .append("INSERT INTO ").append(userTableName).append(columnPart)//
               .append(" SELECT ").append(valuesPart)
               .append(" FROM ").append(realmTableName).append(" R ,").append(partitionTableName).append(" P")
               .append(" WHERE P.").append(dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.FIELD__ID))
               .append(" = '").append(partitionId).append("'")
               .append("  AND R.").append(dbDescriptor.quoteIdentifier(UserRealmBean.FIELD__PARTITION))
               .append(" = P.").append(AuditTrailPartitionBean.FIELD__OID);

         executeOrSpoolStatement(insBuffer.toString(), connection, spoolFile);

         // insert predefined link type
         int oid = 0;
         for (PredefinedProcessInstanceLinkTypes type : PredefinedProcessInstanceLinkTypes.values())
         {
            oid++;
            String insertStmt = getInsertLinkTypeStatement(schemaName, type.getId(), type.getDescription(), oid, partitionId);
            executeOrSpoolStatement(insertStmt, connection, spoolFile);
         }
         if (!dbDescriptor.supportsSequences() && !dbDescriptor.supportsIdentityColumns())
         {
            String updateStmt = getUpdateLinkTypeSequenceHelperStatement(schemaName);
            executeOrSpoolStatement(updateStmt, connection, spoolFile);
         }
      }
      catch (SQLException x)
      {
         try
         {
            connection.rollback();
         }
         catch (SQLException e)
         {
            throw new InternalException(e);
         }

         String message = MessageFormat.format(
               "Could not create partition ''{0}''. Reason: {1}.", new Object[] {
                     partitionId, x.getMessage() });
         trace.warn(message, x);
      }

      try
      {
         connection.commit();
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
   }

   public void listAuditTrailPartitions(Connection connection, String schemaName)
   {
      for (Iterator iter = AuditTrailPartitionBean.findAll(); iter.hasNext();)
      {
         System.out.println(MessageFormat.format("{0}", new Object[] { iter.next() }));
      }
   }

   /**
    * This method will concatenate all column names provided in a collection delimited by
    * a comma. Each column name will be quoted if necessary.
    *
    * @param dbDescriptor DbDescriptor used to quote column names if necessary.
    * @param columns Collection of column names.
    * @return The concatenated and quoted column names.
    */
   private static String buildColumnsFragment(final DBDescriptor dbDescriptor,
         Collection columns)
   {
      String result;
      Iterator transIter = new TransformingIterator(columns.iterator(), new Functor()
      {
         public Object execute(Object source)
         {
            return dbDescriptor.quoteIdentifier((String) source);
         }
      });

      result = " (" + StringUtils.join(transIter, ",") + ") ";

      return result;
   }

   /**
    * This class packages everything which describes a column for table creation in SQL.
    *
    * @author sborn
    * @version $Revision$
    */
   private static class ColumnDescriptor
   {
      private String name;
      private String sqlType;
      private String columnQualifier;

      /**
       * This factory method creates a {@link ColumnDescriptor} instance.
       *
       * @param type Class for which the column descriptor shall be created.
       * @param fieldName Name of a field within the given class.
       * @param dbDescriptor A database descriptor determining the database the column
       *        descriptor shall work for.
       * @return A {@link ColumnDescriptor} instance.
       */
      public static ColumnDescriptor create(Class type, String fieldName,
            DBDescriptor dbDescriptor)
      {
         TypeDescriptor typeManager = TypeDescriptor.get(type);
         int index = typeManager.getColumnIndex(fieldName);
         FieldDescriptor descriptor = typeManager.getPersistentField(index);
         Field field = descriptor.getField();
         field.setAccessible(true);

         String sqlType = dbDescriptor
               .getSQLType(field.getType(), descriptor.getLength());
         String columnQualifier = "";
         if (dbDescriptor.supportsIdentityColumns() &&
               typeManager.requiresPKCreation() && typeManager.isPkField(field))
         {
            columnQualifier = dbDescriptor.getIdentityColumnQualifier();
         }

         return new ColumnDescriptor(fieldName, sqlType, columnQualifier);
      }

      public ColumnDescriptor(String name, String sqlType, String columnQualifier)
      {
         this.name = name;
         this.sqlType = sqlType;
         this.columnQualifier = columnQualifier;
      }

      public void setColumnQualifier(String columnQualifier)
      {
         this.columnQualifier = StringUtils.isEmpty(columnQualifier) ? ""
               : columnQualifier;
      }

      public String getColumnQualifier()
      {
         return columnQualifier;
      }

      public void setName(String name)
      {
         this.name = StringUtils.isEmpty(name) ? "" : name;
      }

      public String getName()
      {
         return name;
      }

      public void setSqlType(String sqlType)
      {
         this.sqlType = StringUtils.isEmpty(sqlType) ? "" : sqlType;
      }

      public String getSqlType()
      {
         return sqlType;
      }
   }

}
