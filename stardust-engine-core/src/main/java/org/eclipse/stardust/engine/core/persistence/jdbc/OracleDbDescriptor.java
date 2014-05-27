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

import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class OracleDbDescriptor extends SequenceDbDriver
{
   public DBMSKey getDbmsKey()
   {
      return DBMSKey.ORACLE;
   }

   public String getSQLType(Class type, long length)
   {
      if (type == Integer.TYPE || type == Integer.class)
      {
         return "INTEGER";
      }
      else if (type == Long.TYPE || type == Long.class || type == Date.class)
      {
         return "NUMBER";
      }
      else if (type == Float.TYPE || type == Float.class)
      {
         return "NUMBER(10,2)";
      }
      else if (type == Double.TYPE || type == Double.class)
      {
         return "DOUBLE PRECISION";
      }
      else if (type == String.class)
      {
         if (length == Integer.MAX_VALUE)
         {
            return "CLOB";
         }
         else
         {
            if (length == 0)
            {
               length = 300;
            }
            return "VARCHAR2(" + length + " CHAR)";
         }
      }
      else if (type == java.sql.Date.class)
      {
         return "DATE";
      }

      throw new InternalException("Illegal type for SQL mapping: '" + type.getName()
            + "'");
   }

   public String getNextValForSeqString(String schemaName, String sequenceName)
   {
      StringBuffer buffer = new StringBuffer(100);

      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
   }
      buffer.append(sequenceName).append(".nextVal");

      return buffer.toString();
   }

   public boolean supportsColumnDeletion()
   {
      return true;
   }

   public boolean isLockRowStatementSQLQuery()
   {
      return true;
   }

   public String getLockRowStatementString(SqlUtils sqlUtils,
         TypeDescriptor type, boolean tryToUseDistinctLockTable, String predicate)
   {
      final ITableDescriptor table = tryToUseDistinctLockTable
            ? type.getLockTableDescriptor()
            : type;

      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT * FROM ");
      sqlUtils.appendTableRef(buffer, table);
      buffer.append(" WHERE ").append(predicate)
            .append(" FOR UPDATE NOWAIT");

      return buffer.toString();
   }

   public String getLockRowWithTimeoutStatementString(SqlUtils sqlUtils,
         TypeDescriptor type, boolean tryToUseDistinctLockTable, String predicate, int timeout)
   {
      final ITableDescriptor table = tryToUseDistinctLockTable
            ? type.getLockTableDescriptor()
            : type;

      StringBuffer buffer = new StringBuffer(200);

      buffer.append("SELECT * FROM ");
      sqlUtils.appendTableRef(buffer, table);
      buffer.append(" WHERE ").append(predicate)
            .append(" FOR UPDATE WAIT ").append(timeout);

      return buffer.toString();
   }

   public String getCreatePKStatement(String schemaName, String pkSequence)
   {
      return getCreatePKStatement(schemaName, pkSequence, 1);
   }

   public String getCreatePKStatement(String schemaName, String pkSequence, int sequenceCount)
   {
      // uses SQL "union" to retrieve multiple sequence numbers

      StringBuffer buffer = new StringBuffer(sequenceCount*50);

      buffer.append("SELECT ");

      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(pkSequence).append(".nextVal");

      if (1 < sequenceCount)
      {
         buffer.append(" FROM (");
         for (int i = 0; i < sequenceCount; i++ )
         {
            buffer.append("SELECT ").append(i).append(" FROM DUAL");
            if (i < sequenceCount - 1)
            {
               buffer.append(" UNION ALL ");
            }
         }

         buffer.append(")");
      }
      else
      {
         buffer.append(" FROM DUAL");
      }

      return buffer.toString();
   }

   public String getCreatePKSequenceStatementString(String schemaName,
         String pkSequence, String initialValueExpr)
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append("CREATE SEQUENCE ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
   }
      buffer.append(pkSequence);
      if (!StringUtils.isEmpty(initialValueExpr))
      {
         buffer.append(" START WITH ").append(initialValueExpr);
      }
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

   public String getDropPKSequenceStatementString(String schemaName, String pkSequence)
   {
      StringBuffer buffer = new StringBuffer(100);

      buffer.append("DROP SEQUENCE ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
   }
      buffer.append(pkSequence);

      return buffer.toString();
   }

   public boolean supportsSubselects()
   {
      return true;
   }

   public boolean supportsMultiColumnUpdates()
   {
      return true;
   }

   /*public void throwIfUniqueConstraintViolated(SQLException exception)
         throws UniqueConstraintViolatedException
   {
      if (1 == exception.getErrorCode())
      {
         throw new UniqueConstraintViolatedException("Unique constraint violated.", exception);
      }
   }
   */
}
