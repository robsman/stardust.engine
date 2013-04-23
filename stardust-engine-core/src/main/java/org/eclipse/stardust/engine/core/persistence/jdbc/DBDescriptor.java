/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.util.Collections;
import java.util.Date;
import java.util.TreeSet;
//import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * Manages a single schema for Java/SQL mapping.
 * <p>
 * <b>All proprietary information about DBMS driver implementation specifics is
 * accumulated here.</b>
 * 
 * Also acts as a factory for creating DBDescriptors for different flavours of RDBMSes.
 * 
 * @author sborn
 * @version $Revision$
 */
public abstract class DBDescriptor
{
   private static final Pair INT_VALUE_RANGE = new Pair(Integer.MIN_VALUE, Integer.MAX_VALUE);
   private static final Pair LONG_VALUE_RANGE = new Pair(Long.MIN_VALUE, Long.MAX_VALUE);

   // Float.MIN_VALUE is NOT negative, use negative MAX_VALUE as lower border
   private static final Pair FLOAT_VALUE_RANGE = new Pair(-Float.MAX_VALUE, Float.MAX_VALUE);
   private static final Pair DOUBLE_VALUE_RANGE = new Pair( -1.0e+125, 1.0e+125);
   private static final Pair EPSILON_DOUBLE_VALUE_RANGE = new Pair(-2.225E-307, 2.225E-307);

   private static final String DBDESCRIPTOR_PREFIX = "org.eclipse.stardust.engine.core.persistence.jdbc.dbdescriptor.";

   public static final String SEQUENCE_HELPER_TABLE_NAME = "sequence_helper";
   
   private static final String KEY_AUDIT_TRAIL_DB_DESCRIPTOR = DBDESCRIPTOR_PREFIX
         + SessionProperties.DS_NAME_AUDIT_TRAIL;

   /**
    * Creates a DBDescriptor identified by the given (parameter) name.
    * 
    * Example is the AuditTrail.Type found in the carnot.properties file.
    * 
    * It lazily caches the descriptors so as to reduce the overhead of finding a suitable
    * descriptor.
    * 
    * It relies on &lt;name&gt;.DBDescriptor parameter to get hold of a custom
    * implementation of <code>DBDescriptor</code>.
    * 
    * If it doesn't find such a class then it relies on supported RDBMS implementations to
    * find the requested descriptor.
    * 
    * For creating supported descriptors it relies on
    * {@link DBDescriptor#create(String, String)}.
    * 
    * @param name
    *           key/identifier for the instance of the DBDescriptor
    * @return DBDescriptor corresponding to the given name
    * @throws InternalException
    *            if the DBDescriptor couldn't be located
    */
   public static DBDescriptor create(final String name)
   {
      final GlobalParameters globals = GlobalParameters.globals();

      final String keyCachedDbDescriptor = SessionProperties.DS_NAME_AUDIT_TRAIL.equals(name)
            ? KEY_AUDIT_TRAIL_DB_DESCRIPTOR
            : DBDESCRIPTOR_PREFIX + name;
      
      DBDescriptor result = (DBDescriptor) globals.get(keyCachedDbDescriptor);
      if (null == result)
      {
         result = (DBDescriptor) globals.initializeIfAbsent(keyCachedDbDescriptor,
               new ValueProvider()
               {
                  public Object getValue()
                  {
                     final Parameters params = Parameters.instance();
                     
                     DBDescriptor descriptor;

                     String descriptorClass = params.getString(name + ".DBDescriptor");

                     if (null != descriptorClass)
                     {
                        try
                        {
                           descriptor = (DBDescriptor) Reflect.getClassFromClassName(descriptorClass)
                                 .newInstance();
                        }
                        catch (Exception e)
                        {
                           throw new InternalException("Couldn't lookup DB descriptor: "
                                 + descriptorClass, e);
                        }
                     }
                     else
                     {
                        String type = params.getString(name + ".Type",
                              DBMSKey.ORACLE.getId());
                        descriptor = create(name, type);
                     }

                     return descriptor;
                  }
               });
      }

      return result;
   }

   /**
    * Creates a DBDescriptor identified by the given (parameter) name and database type.
    * 
    * @param name
    *           key/identifier for the instance of the DBDescriptor
    * @param databaseType
    *           case insensitive database type string; see {@link DBMSKey#getId()}
    * @return
    * @throws InternalException
    *            if the DBDescriptor couldn't be located
    */
   public static DBDescriptor create(String name, String databaseType)
   {
      DBDescriptor descriptor;

      if (DBMSKey.ORACLE9i.getId().equalsIgnoreCase(databaseType))
      {
         descriptor =  new Oracle9iDbDescriptor();
      }
      else if (DBMSKey.ORACLE.getId().equalsIgnoreCase(databaseType))
      {
         descriptor =  new OracleDbDescriptor();
      }
      else if (DBMSKey.DB2_UDB.getId().equalsIgnoreCase(databaseType))
      {
         descriptor = new DB2DbDescriptor();
      }
      else if (DBMSKey.DERBY.getId().equalsIgnoreCase(databaseType))
      {
         descriptor = new DerbyDbDescriptor();
      }
      else if (DBMSKey.MSSQL8.getId().equalsIgnoreCase(databaseType))
      {
         descriptor = new MsSql8DbDescriptor();
      }
      else if (DBMSKey.MYSQL.getId().equalsIgnoreCase(databaseType))
      {
         descriptor = new MySqlDbDescriptor();
      }
      else if (DBMSKey.MYSQL_SEQ.getId().equalsIgnoreCase(databaseType))
      {
         descriptor = new MySqlSeqDbDescriptor();
      }
      else if (DBMSKey.POSTGRESQL.getId().equalsIgnoreCase(databaseType))
      {
         descriptor = new PostgreSQLDbDescriptor();
      }
      else if (DBMSKey.SYBASE.getId().equalsIgnoreCase(databaseType))
      {
         descriptor = new SybaseDbDescriptor();
      }
      else
      {
         throw new InternalException("Unsupported database type: " + databaseType);
      }

      return descriptor;
   }

   protected DBDescriptor()
   {
   }

   public abstract DBMSKey getDbmsKey();
   
   public String getCreateIndexStatement(String schemaName, String tableName, IndexDescriptor indexDescriptor)
   {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("CREATE ");

      if (indexDescriptor.isUnique())
      {
         buffer.append("UNIQUE ");
      }

      buffer.append("INDEX ");
      if (!StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(indexDescriptor.getName()).append(" ON ");
      if (!StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(quoteIdentifier(tableName)).append("(");

      for (int n = 0; n < indexDescriptor.getColumns().length; ++n)
      {
         if (n > 0)
         {
            buffer.append(", ");
         }

         buffer.append(quoteIdentifier(indexDescriptor.getColumns()[n]));
      }
      buffer.append(")");

      return buffer.toString();
   }
   
   public String getDropIndexStatement(String schema, String tableName, String indexName)
   {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("DROP INDEX ");
      if (!StringUtils.isEmpty(schema))
      {
         buffer.append(schema).append(".");
      }
      buffer.append(indexName);
      
      return buffer.toString();
   }

   public abstract boolean supportsIdentityColumns();
   
   public abstract String getIdentityColumnQualifier();
   
   public abstract String getSelectIdentityStatementString(String schemaName,
         String tableName);
   
   public abstract boolean supportsSequences();

   public String getCreatePKSequenceStatementString(String schemaName, String pkSequence)
   {
      return getCreatePKSequenceStatementString(schemaName, pkSequence, null);
   }

   /**
    * Gets the SQL statement for creating a sequence that starts with an initial value.
    * 
    * @param schemaName name of the schema in which the sequence has to exist
    * @param pkSequence the sequence name
    * @param initialValueExpr initial value for the sequence
    * @return SQL statement for creating a sequence
    */
   public abstract String getCreatePKSequenceStatementString(String schemaName,
         String pkSequence, String initialValueExpr);
   /**
    * Gets the SQL statement for dropping a sequence in a database schema.
    * 
    * @param schemaName name of the schema in which the sequence exists
    * @param pkSequence the sequence name
    * @return SQL statement for dropping a sequence
    */
   public abstract String getDropPKSequenceStatementString(String schemaName, String pkSequence);

   /**
    * Gets the SQL statement for accessing the next value in a sequence.
    * 
    * <p>
    * <b>For example</b>: While using <u>PostgreSQL</u>
    * 
    * <pre>
    * SELECT nextval('schemaxyz.serial');
    * </pre>
    * 
    * calling this method <code>getCreatePKStatement("schemaxyz", "serial")</code> will
    * return <code>SELECT nextval('schemaxyz.serial')</code> which can then be executed
    * to get the next value in the sequence.
    * </p>
    * 
    * @param schemaName
    *           schema to which the sequence belongs
    * @param sequenceName
    *           name of the sequence
    * @param sequenceCount 
    *           number of sequences to be retrieved in one select
    * @return SQL statement for getting the next value of a Sequence
    */
   public abstract String getCreatePKStatement(String schemaName, String pkSequence, int sequenceCount);
   
   public abstract String getCreatePKStatement(String schemaName, String pkSequence);

   /**
    * Gets the fragment for accessing the next value in a sequence so that it can be used
    * as part of another SQL statement.
    * 
    * <p>
    * <b>For example</b>: While using <u>PostgreSQL</u> the intention is to execute an
    * SQL statement like,
    * 
    * <pre>
    * INSERT INTO distributors VALUES (nextval('schemaxyz.serial'), 'nothing');
    * </pre>
    * 
    * calling this method <code>getNextValForSeqString("schemaxyz", "serial")</code>
    * will return <code>nextval('schemaxyz.serial')</code> which can then be
    * incorporated into the intended SQL statement.
    * </p>
    * 
    * @param schemaName
    *           schema to which the sequence belongs
    * @param sequenceName
    *           name of the sequence
    * @return SQL fragment for getting the next value of a Sequence
    */
   public abstract String getNextValForSeqString(String schemaName, String sequenceName);
   
   public String getCreateSequenceStoredProcedureStatementString(String schemaName)
   {
      return null;
   }
   
   public String getDropSequenceStoredProcedureStatementString(String schemaName)
   {
      return null;
   }
   
   public String getCreateGlobalPKSequenceStatementString(String schemaName)
   {
      return null;
   }
   
   public String getDropGlobalPKSequenceStatementString(String schemaName)
   {
      return null;
   }
   
   public boolean supportsColumnDeletion()
   {
      return false;
   }
   
   /**
    * Maps Java field types to valid SQL column types.
    */
   public abstract String getSQLType(Class type, long length);
   
   public <E> Pair<E, E> getNumericSQLTypeEpsilonBorders(Class<E> type)
   {
      return EPSILON_DOUBLE_VALUE_RANGE;
   }

   //public static <E> TreeSet<E> copySet(TreeSet<? extends E> rhs)
   public <E> Pair< E, E> getNumericSQLTypeValueBorders(
         Class<E> type)
   {
      if (type == Integer.TYPE || type == Integer.class)
      {
         return INT_VALUE_RANGE;
      }
      else if (type == Long.TYPE || type == Long.class || type == Date.class)
      {
         return LONG_VALUE_RANGE;
      }
      else if (type == Float.TYPE || type == Float.class)
      {
         return FLOAT_VALUE_RANGE;
      }
      else if (type == Double.TYPE || type == Double.class)
      {
         return DOUBLE_VALUE_RANGE;
      }

      throw new InternalException("Illegal type for SQL mapping: '" + type.getName()
            + "'");
   }

   public abstract boolean useQueryTimeout();

   /**
    * Does the DB support ANSI SQL/92 join syntax?
    * 
    * @return true if the DB does support them.
    */
   public abstract boolean useAnsiJoins();

   public abstract boolean isLockRowStatementSQLQuery();

   /**
    * Returns a valid SQL statement for locking rows in the table which is mapped to the
    * given type.
    * @param sqlUtils TODO
    * @param type
    *        Description of the type for which database rows shall be locked.
    * @param tryToUseDistinctLockTable
    *        A boolean which mandates whether an available distinct lock table shall be
    *        used for the locking statement.
    * @param predicate
    * 
    * @return The lock statement.
    */
   public abstract String getLockRowStatementString(SqlUtils sqlUtils,
         TypeDescriptor type, boolean tryToUseDistinctLockTable, String predicate);

   public Iterator getPersistentTypes()
   {
      return Collections.EMPTY_LIST.iterator();
   }
   
   public String getCreateTableOptions()
   {
      return null;
   }

   public abstract boolean supportsSubselects();
   
   /**
    * Determines whether update statements like 
    * <br>
    * <code>
    * UPDATE table_name <br>
    * SET (col1, col2, col3) = (...) <br>
    * </code>
    * are possible or whether each column col1, etc. has to be updated by its own
    * SET-clause.
    * 
    * @return true if (col1, col2, col3) is possible.
    */
   public boolean supportsMultiColumnUpdates()
   {
      return false;
   }

   /**
    * Returns a quoted version of the given identifier (tablename, columnname, ...) if
    * it is necessary.
    * 
    * @param identifier
    * @return If necessary the quoted identifier.
    */
   public String quoteIdentifier(String identifier)
   {
      return identifier;
   }
   
   /**
    * Determines whether columns containing string do trim trailing blanks,
    * e.g. if you write "abc   " to a string column it will be read as "abc".
    * 
    * @return true if trailing blanks are trimmed.
    */
   public boolean isTrimmingTrailingBlanks()
   {
      return false;
   }
   
   /**
    * Determines whether columns are nullable by default on create table.
    * 
    * @return true if trailing blanks are trimmed.
    */
   public boolean isColumnNullableByDefault()
   {
      return true;
   }
   
   /**
    * Defines the default value for property {@link SessionProperties#DS_USE_LOCK_TABLES_SUFFIX}. 
    * This value can be overwritten by explicitly setting the property. 
    * 
    * @return true if extra lock table shall be used, otherwise false.
    */
   public boolean getUseLockTablesDefault()
   {
      return false;
   }
   
   /**
    * SQL statement delimiter to be used when SQL statements are not executed directly by
    * JDBC but written to some file or other storage for later execution. This is not necessarily 
    * a single character but could be a string containing any whitespace characters.
    *  
    * @return the statement delimiter.
    */
   public String getStatementDelimiter()
   {
      return ";";
   }
}
