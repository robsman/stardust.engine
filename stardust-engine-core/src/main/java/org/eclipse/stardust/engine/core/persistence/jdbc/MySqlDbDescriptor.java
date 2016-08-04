/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;


/**
 * @author rsauer
 * @version $Revision$
 */
public class MySqlDbDescriptor extends IdentityColumnDbDriver
{
   private static final String[] keywords = {
      // keyword since version 5.0
      "CONDITION",
      // Keyword since 5.6
      "PARTITION"
   };
   private static final Set<String> keywordSet = new HashSet<String>(Arrays.asList(keywords));

   public DBMSKey getDbmsKey()
   {
      return DBMSKey.MYSQL;
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

      buffer.append("INDEX ").append(indexDescriptor.getName()).append(" ON ");
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

   public String getDropIndexStatement(String schemaName, String tableName, String indexName)
   {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("DROP INDEX ").append(indexName).append(" ON ");
      if (!StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(quoteIdentifier(tableName));

      return buffer.toString();
   }

   public boolean supportsColumnDeletion()
   {
      return true;
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
         return "FLOAT";
      }
      else if (type == Double.TYPE || type == Double.class)
      {
         return "DOUBLE";
      }
      else if (type == String.class)
      {
         if (length == 0)
         {
            length = 255;
         }
         if (length <= 255)
         {
            return "VARCHAR(" + length + ")";
         }
         else if (length == Integer.MAX_VALUE)
         {
            // "CLOB" support: MEDIUMTEXT can contain up to 16,777,215 chars
            return "MEDIUMTEXT";
         }
         else
         {
            return "TEXT";
         }
      }

      throw new InternalException("Illegal type for SQL mapping: '" + type.getName()
            + "'");
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
      buffer.append(" WHERE ").append(predicate).append(" FOR UPDATE");

      return buffer.toString();
   }

   public String getCreateTableOptions()
   {
      return "ENGINE=InnoDB";
   }

   public String getIdentityColumnQualifier()
   {
      return "AUTO_INCREMENT PRIMARY KEY";
   }

   public String getSelectIdentityStatementString(String schemaName, String tableName)
   {
      return "SELECT LAST_INSERT_ID()";
   }

   public boolean supportsSubselects()
   {
      return false;
   }

   public String quoteIdentifier(String identifier)
   {
      if (identifier != null && keywordSet.contains(identifier.toUpperCase()))
      {
         return "`" + identifier + "`";
      }
      else
      {
         return identifier;
      }
   }
}