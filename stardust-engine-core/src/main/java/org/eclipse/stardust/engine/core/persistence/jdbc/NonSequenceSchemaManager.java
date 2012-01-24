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

import java.util.Iterator;

import org.eclipse.stardust.common.OneElementIterator;
import org.eclipse.stardust.common.StringUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class NonSequenceSchemaManager extends DBDescriptor
{
   public DBMSKey getDbmsKey()
   {
      return DBMSKey.GENERIC_JDBC;
   }

   public boolean supportsSequences()
   {
      return false;
   }

   public boolean useAnsiJoins()
   {
      return true;
   }

   public String getDropPKSequenceStatementString(String schemaName, String pkSequence)
   {
      StringBuffer buffer = new StringBuffer(200);
      buffer.append("DELETE FROM ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(SEQUENCE_HELPER_TABLE_NAME)
            .append(" WHERE NAME = '").append(pkSequence).append("'");
      return buffer.toString();
   }

   public boolean isLockRowStatementSQLQuery()
   {
      return false;
   }

   public String getLockRowStatementString(String schemaName, TypeDescriptor type,
         String predicate)
   {
      StringBuffer buffer = new StringBuffer(200);

      buffer.append("UPDATE ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }
      buffer.append(type.getTableName())
            .append(" SET oid=oid")
            .append(" WHERE ").append(predicate);

      return buffer.toString();
   }

   public Iterator getPersistentTypes()
   {
      return new OneElementIterator(PseudoSequenceHelperBean.class);
   }

   public static class PseudoSequenceHelperBean extends PersistentBean
   {
      public static final String TABLE_NAME = SEQUENCE_HELPER_TABLE_NAME;

      private static final int name_COLUMN_LENGTH = 32;
      private String name;
      private long value;
   }

}
