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

import java.lang.reflect.Field;
import java.util.Iterator;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class IdentityColumnDbDriver extends DBDescriptor
{
   public final boolean supportsIdentityColumns()
   {
      return true;
   }

   public final boolean supportsSequences()
   {
      return false;
   }

   public final String getCreatePKSequenceStatementString(String schemaName,
         String pkSequence, String initialValueExpr)
   {
      return null;
   }

   public final String getDropPKSequenceStatementString(String schemaName, String pkSequence)
   {
      return null;
   }

   public final String getCreatePKStatement(String schemaName, String pkSequence)
   {
      return null;
   }

   public String getCreatePKStatement(final String schemaName, final String pkSequence, int sequenceCount)
   {
      return null;
   }
   
   public final String getNextValForSeqString(String schemaName, String sequenceName)
   {
      return null;
   }
   
   protected static String getLockRowByUpdateStatementString(SqlUtils sqlUtils,
         TypeDescriptor type, boolean tryToUseDistinctLockTable, String predicate)
   {
      final ITableDescriptor table = tryToUseDistinctLockTable ? type
            .getLockTableDescriptor() : type;

      Field lockField = null;

      if (tryToUseDistinctLockTable && type.isDistinctLockTableName())
      {
         // the pk fields in data table and lock table have to be the same 
         lockField = type.getPkFields()[0];
      }
      else
      {
         for (Iterator i = type.getPersistentFields().iterator(); i.hasNext();)
         {
            Field field = ((FieldDescriptor) i.next()).getField();
            if ( !type.isPkField(field))
            {
               // search non-PK fields, trying to find first numeric one for efficiency
               // reasons
               lockField = field;
               if (Reflect.isAssignable(Number.class, field.getType()))
               {
                  break;
               }
            }
         }
      }

      if (null == lockField)
      {
         throw new InternalException("Unable to lock instance of " + type.getType()
               + " as no updatable field could be found.");
      }

      StringBuffer buffer = new StringBuffer(100);

      buffer.append("UPDATE ");
      sqlUtils.appendTableRef(buffer, table, false);
      buffer.append(" SET ");
      sqlUtils.appendFieldRef(buffer, table.fieldRef(lockField.getName()), false);
      buffer.append("=");
      sqlUtils.appendFieldRef(buffer, table.fieldRef(lockField.getName()), false);
      buffer.append(" WHERE ");

      if ( !StringUtils.isEmpty(table.getTableAlias())
            && predicate.startsWith(table.getTableAlias()))
      {
         buffer.append(table.getTableName());
         final int indexOfDot = predicate.indexOf(".");
         buffer.append(predicate.substring(indexOfDot));
      }
      else
      {
         buffer.append(predicate);
      }

      return buffer.toString();
   }

}
