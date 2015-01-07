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
 * @author born
 * @version $Revision$
 */
public class SybaseDbDescriptor extends IdentityColumnDbDriver
{
   private static final String PARTITION_IDENTIFIER = "PARTITION";

   public DBMSKey getDbmsKey()
   {
      return DBMSKey.SYBASE;
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
         return "DOUBLE PRECISION";
      }
      else if (type == String.class)
      {
         if (length == Integer.MAX_VALUE)
         {
            return "TEXT";
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

   public boolean useQueryTimeout()
   {
      return true;
   }

   public boolean useAnsiJoins()
   {
      return true;
   }

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
      
      buffer.append("DROP INDEX ")
            .append(quoteIdentifier(tableName)).append(".")
            .append(quoteIdentifier(indexName));
      
      return buffer.toString();
   }
   
   public String quoteIdentifier(String identifier)
   {
      if (PARTITION_IDENTIFIER.equalsIgnoreCase(identifier))
      {
         return "\"" + identifier + "\"";
      }
      else
      {
         return identifier;
      }
   }
   
   public boolean isLockRowStatementSQLQuery()
   {
      return false;
   }
   
   public String getLockRowStatementString(SqlUtils sqlUtils, TypeDescriptor type,
         boolean tryToUseDistinctLockTable, String predicate)
   {
      return getLockRowByUpdateStatementString(sqlUtils, type, tryToUseDistinctLockTable,
            predicate);
   }
   
   public String getIdentityColumnQualifier()
   {
      return "IDENTITY";
   }

   public String getSelectIdentityStatementString(String schemaName, String tableName)
   {
      StringBuffer buffer = new StringBuffer(100);

      buffer.append("SELECT @@IDENTITY");
      
      return buffer.toString();
   }
   
   public boolean isTrimmingTrailingBlanks()
   {
      return true;
   }
   
   public boolean isColumnNullableByDefault()
   {
      return false;
   }
   
   public String getCreateTableOptions()
   {
      return "lock datarows";
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
   
   public String getStatementDelimiter()
   {
      return "\nGO";
   }

}
