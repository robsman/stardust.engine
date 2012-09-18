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
public class DB2DbDescriptor extends SequenceDbDriver
{
   public DB2DbDescriptor()
   {
   }

   public DBMSKey getDbmsKey()
   {
      return DBMSKey.DB2_UDB;
   }

   public String getSQLType(Class type, long length)
   {
      if (type == Integer.TYPE || type == Integer.class)
      {
         return "INTEGER";
      }
      else if (type == Long.TYPE || type == Long.class || type == Date.class)
      {
         return "BIGINT";
      }
      else if (type == Float.TYPE || type == Float.class)
      {
         return "REAL";
      }
      else if (type == Double.TYPE || type == Double.class)
      {
         return "DOUBLE";
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
            return "VARCHAR(" + length + ")";
         }
      }

      throw new InternalException(
            "Illegal type for SQL mapping: '" + type.getName() + "'");
   }

   public String getDropPKSequenceStatementString(String schemaName, String pkSequence)
   {
      StringBuffer buffer = new StringBuffer(100);

      buffer.append("DROP SEQUENCE ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(pkSequence).append(" RESTRICT");

      return buffer.toString();
   }


   public String getNextValForSeqString(String schemaName, String sequenceName)
   {
         StringBuffer buffer = new StringBuffer(100);

         buffer.append("NEXTVAL FOR ");
         if ( !StringUtils.isEmpty(schemaName))
         {
            buffer.append(schemaName).append(".");
         }
         buffer.append(sequenceName);

         return buffer.toString();
   }

   public boolean isLockRowStatementSQLQuery()
   {
      return false;
   }

   public String getLockRowStatementString(SqlUtils sqlUtils,
         TypeDescriptor type, boolean tryToUseDistinctLockTable, String predicate)
   {
      final ITableDescriptor table = tryToUseDistinctLockTable
            ? type.getLockTableDescriptor()
            : type;

      StringBuffer buffer = new StringBuffer(100);

      buffer.append("UPDATE ");
      sqlUtils.appendTableRef(buffer, table);
      buffer.append(" SET ");
      sqlUtils.appendFieldRef(buffer,
            table.fieldRef(IdentifiablePersistentBean.FIELD__OID));
      buffer.append("=");
      sqlUtils.appendFieldRef(buffer,
            table.fieldRef(IdentifiablePersistentBean.FIELD__OID));
      buffer.append(" WHERE ").append(predicate);

      return buffer.toString();
   }

   public boolean useQueryTimeout()
   {
      return true;
   }

   public boolean useAnsiJoins()
   {
      return true;
   }

   public String getCreatePKStatement(String schemaName, String pkSequence)
   {
      StringBuffer buffer = new StringBuffer(100);

      buffer.append("VALUES nextVal FOR ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(pkSequence);

      return buffer.toString();
   }

   public String getCreatePKStatement(final String schemaName, final String pkSequence, int sequenceCount)
   {
      // TODO CRNT-26273 take care of sequenceCount (current implementation only returns one sequence)
      return getCreatePKStatement(schemaName, pkSequence);
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

   public boolean supportsSubselects()
   {
      return true;
   }
   
   public boolean supportsMultiColumnUpdates()
   {
      return true;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getUseLockTablesDefault()
    */
   public boolean getUseLockTablesDefault()
   {
      return true;
   }
}
