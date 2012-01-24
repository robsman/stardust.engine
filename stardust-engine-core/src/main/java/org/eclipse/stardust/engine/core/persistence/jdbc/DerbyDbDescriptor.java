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

import org.eclipse.stardust.common.error.InternalException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DerbyDbDescriptor extends IdentityColumnDbDriver
{
   public DerbyDbDescriptor()
   {
   }

   public DBMSKey getDbmsKey()
   {
      return DBMSKey.DERBY;
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

   public boolean useQueryTimeout()
   {
      return true;
   }

   public boolean useAnsiJoins()
   {
      return true;
   }

   public String getIdentityColumnQualifier()
   {
      return "GENERATED ALWAYS AS IDENTITY";
   }

   public String getSelectIdentityStatementString(String schemaName, String tableName)
   {
      return "VALUES IDENTITY_VAL_LOCAL()";
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
}
