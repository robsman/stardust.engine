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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;


/**
 * PostgreSQLDbDescriptor describes settings and possibilities afforded when PostgresSQl
 * is to be used as the database.
 * 
 * 
 * @author purang
 * @version $Revision$
 */
public class PostgreSQLDbDescriptor extends SequenceDbDriver
{
   /**
    * Gets the DBMSKey signifying the database whose descriptor this is.
    */
   public DBMSKey getDbmsKey()
   {
      return DBMSKey.POSTGRESQL;
   }

   /**
    * {@inheritDoc}
    */
   public String getSQLType(final Class type, final long length)
   {
      if (type == Integer.TYPE || type == Integer.class)
      {
         return "integer";
      }
      else if (type == Long.TYPE || type == Long.class || type == java.util.Date.class)
      {
         return "bigint";
      }
      else if (type == Float.TYPE || type == Float.class)
      {
         return "numeric(10,2)";
      }
      else if (type == Double.TYPE || type == Double.class)
      {
         return "float8";
      }
      else if (type == String.class)
      {
         if (length == Integer.MAX_VALUE)
         {
            // CLOB support
            return "text";
         }
         else
         {
            return "varchar(" + ((length != 0) ? length : 300) + ")";
         }
      }
      else if (type == java.sql.Date.class)
      {
         return "date";
      }

      throw new InternalException("Illegal type for SQL mapping: '" + type.getName()
            + "'");
   }

   /**
    * {@inheritDoc}
    */
   public String getCreatePKSequenceStatementString(final String schemaName,
         final String pkSequence, final String initialValueExpr)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("CREATE SEQUENCE ");
      if (!StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(pkSequence);
      if (!StringUtils.isEmpty(initialValueExpr))
      {
         buffer.append(" START WITH").append(initialValueExpr);
      }
      return buffer.toString();
   }

   /**
    * {@inheritDoc}
    */
   public String getDropPKSequenceStatementString(String schemaName, String pkSequence)
   {
      StringBuffer buffer = new StringBuffer(100);

      buffer.append("DROP SEQUENCE ");
      if (!StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(pkSequence);

      return buffer.toString();
   }

   /**
    * {@inheritDoc}
    */
   public String getNextValForSeqString(String schemaName, String sequenceName)
   {
      StringBuffer buffer = new StringBuffer(100);

      buffer.append("nextval('");
      if (!StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(sequenceName);
      buffer.append("')");
      return buffer.toString();
   }

   /**
    * {@inheritDoc}
    */
   public String getCreatePKStatement(final String schemaName, final String pkSequence)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("SELECT ");
      buffer.append(getNextValForSeqString(schemaName, pkSequence));
      return buffer.toString();
   }

   public String getCreatePKStatement(final String schemaName, final String pkSequence, int sequenceCount)
   {
      // TODO CRNT-26274 take care of sequenceCount (current implementation only returns one sequence)
      return getCreatePKStatement(schemaName, pkSequence);
   }

   public boolean supportsColumnDeletion()
   {
      return true;
   }

   public boolean isLockRowStatementSQLQuery()
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String getLockRowStatementString(SqlUtils sqlUtils, TypeDescriptor type,
         boolean tryToUseDistinctLockTable, String predicate)
   {
      final ITableDescriptor table = tryToUseDistinctLockTable ? type
            .getLockTableDescriptor() : type;

      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT * FROM ");
      sqlUtils.appendTableRef(buffer, table);
      buffer.append(" WHERE ").append(predicate).append(" FOR UPDATE NOWAIT");

      return buffer.toString();
   }

   public String getLockRowWithTimeoutStatementString(SqlUtils sqlUtils, TypeDescriptor type,
         boolean tryToUseDistinctLockTable, String predicate, int timeout)
   {
      final ITableDescriptor table = tryToUseDistinctLockTable ? type
            .getLockTableDescriptor() : type;

      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT * FROM ");
      sqlUtils.appendTableRef(buffer, table);
      buffer.append(" WHERE ").append(predicate);
      buffer.append(" FOR UPDATE");

      return buffer.toString();
   }

   public boolean useQueryTimeout()
   {
      return false;
   }

   public boolean useAnsiJoins()
   {
      return true;
   }

   public boolean supportsSubselects()
   {
      return true;
   }

   public boolean supportsMultiColumnUpdates()
   {
      return true;
   }

   /**
    * Overrides the base class implementation because PostgresSQL doesn't need schema
    * name to prefix the index name. From PostgresSQL documentation for CREATE INDEX:
    *
    * <pre>
    * The name of the index to be created. No schema name can be included here; the index is always created in the same schema as its parent table.
    * </pre>
    *
    */
   public String getCreateIndexStatement(String schemaName, String tableName,
         IndexDescriptor indexDescriptor)
   {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("CREATE ");

      if (indexDescriptor.isUnique())
      {
         buffer.append("UNIQUE ");
      }

      buffer.append("INDEX ");
      buffer.append(indexDescriptor.getName()).append(" ON ");
      if (!StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(tableName).append("(");

      for (int n = 0; n < indexDescriptor.getColumns().length; ++n)
      {
         if (n > 0)
         {
            buffer.append(", ");
         }

         buffer.append(indexDescriptor.getColumns()[n]);
      }
      buffer.append(")");

      return buffer.toString();
   }

}
