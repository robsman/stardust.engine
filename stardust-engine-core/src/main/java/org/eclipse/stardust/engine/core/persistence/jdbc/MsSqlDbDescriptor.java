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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;


/**
 * @author rsauer
 * @version $Revision: 52592 $
 */
public class MsSqlDbDescriptor extends IdentityColumnDbDriver
{
   private static final String KEY_ENFORCE_UNICODE = SessionProperties.DS_NAME_AUDIT_TRAIL
         + SessionProperties.DS_ENFORCE_UNICODE_SUFFIX;

   @Override
   public DBMSKey getDbmsKey()
   {
      return DBMSKey.MSSQL;
   }

   public String getSQLType(Class type, long length)
   {
      if (type == Integer.TYPE || type == Integer.class)
      {
         return "INT";
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
         return "FLOAT";
      }
      else if (type == String.class)
      {
         final String unicodePrefix = isUnicodeEnforced() ? "N" : "";

         if (length == Integer.MAX_VALUE)
         {
            // deprecated type? Check if proposed VARCHAR(max)/NVARCHAR(max) syntax is working
            return unicodePrefix + "TEXT";
         }
         else
         {
            if (length == 0)
            {
               length = 300;
            }
            return unicodePrefix + "VARCHAR(" + length + ")";
         }
      }

      throw new InternalException(
            "Illegal type for SQL mapping: '" + type.getName() + "'");
   }

   public boolean useQueryTimeout()
   {
      return true;
   }

   public boolean useAnsiJoins()
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

      StringBuffer buffer = new StringBuffer(100);

      buffer.append("SELECT * FROM ");
      sqlUtils.appendTableRef(buffer, table);
      buffer.append(" WITH(NOWAIT, ROWLOCK, UPDLOCK, READPAST)")
            .append(" WHERE ").append(predicate);

      return buffer.toString();
   }

   public String getIdentityColumnQualifier()
   {
      return "IDENTITY";
   }

   public String getSelectIdentityStatementString(String schemaName, String tableName)
   {
      return "SELECT SCOPE_IDENTITY()";
   }

   public boolean supportsSubselects()
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

   public String getCreateIndexStatement(String schemaName, String tableName, IndexDescriptor indexDescriptor)
   {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("CREATE ");

      if (indexDescriptor.isUnique())
      {
         buffer.append("UNIQUE ");
      }

      buffer.append("INDEX ");
/*
      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
*/
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
      buffer.append(") WITH (ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS = OFF)");

      return buffer.toString();
   }

   @Override
   public boolean isUnicodeEnforced()
   {
      return Parameters.instance().getBoolean(KEY_ENFORCE_UNICODE, false);
   }

}
