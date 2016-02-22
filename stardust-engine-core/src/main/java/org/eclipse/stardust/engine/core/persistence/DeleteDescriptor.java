/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


public class DeleteDescriptor extends TableDescriptor implements FieldRefResolver
{
   private boolean affectingLockTable;
   private TypeDescriptor tdType;
   private String tableAlias;
   private QueryExtension qe;

   public static DeleteDescriptor from(Class type)
   {
      return from(null, type);
   }

   public static DeleteDescriptor from(String schema, Class type)
   {
      DeleteDescriptor result = new DeleteDescriptor(schema);

      result.tdType = TypeDescriptor.get(type);

      return result;
   }

   public static DeleteDescriptor from(String schema, Class type, String tableAlias)
   {
      DeleteDescriptor result = new DeleteDescriptor(schema);

      result.tdType = TypeDescriptor.get(type);
      result.tableAlias = tableAlias;

      return result;
   }

   public static DeleteDescriptor fromLockTable(Class type)
   {
      return fromLockTable(null, type);
   }

   public static DeleteDescriptor fromLockTable(String schema, Class type)
   {
      DeleteDescriptor result = new DeleteDescriptor(schema);

      result.tdType = TypeDescriptor.get(type);
      result.affectingLockTable = true;

      return result;
   }

   private DeleteDescriptor(String schemaName)
   {
      super(schemaName);
      this.qe = new QueryExtension();
   }

   public FieldRef resolveFieldRef(FieldRef field)
   {
      // TODO cache resolvable tables
      Map resolvableTables = new HashMap();
      Set unresolvableTables = new HashSet();

      // don't use any alias on the table to delete from
      resolvableTables.put(getTableName(), this);

      for (Iterator i = qe.getJoins().iterator(); i.hasNext();)
      {
         Join join = (Join) i.next();

         ITableDescriptor conflictingTable = (ITableDescriptor) resolvableTables.get(join.getTableName());
         if (null == conflictingTable)
         {
            resolvableTables.put(join.getTableName(), join);
         }
         else
         {
            unresolvableTables.add(join.getTableName());
         }
      }

      resolvableTables.keySet().removeAll(unresolvableTables);

      FieldRef result;

      ITableDescriptor resolvedTable = (ITableDescriptor) resolvableTables.get(field.getType()
            .getTableName());
      if (null != resolvedTable)
      {
         result = resolvedTable.fieldRef(field.fieldName, field.isIgnorePreparedStatements());
      }
      else
      {
         if ( !unresolvableTables.contains(field.getType().getTableName()))
         {
            throw new InternalException(MessageFormat.format(
                  "Table {0} for field {1} can not be resolved.",
                        field.getType().getTableName(), field.fieldName));
         }
         result = field;
      }

      return result;
   }

   public String getTableName()
   {
      return isAffectingLockTable() ? tdType.getLockTableName() : tdType.getTableName();
   }

   public String getTableAlias()
   {
      return tableAlias;
   }

   public Join innerJoin(Class rhsType)
   {
      return innerJoin(getSchemaName(), rhsType, null);
   }

   public Join innerJoin(String schema, Class rhsType)
   {
      return innerJoin(schema, rhsType, null);
   }

   public Join innerJoin(Class rhsType, String rhsAlias)
   {
      return innerJoin(null, rhsType, rhsAlias);
   }

   public Join innerJoin(String schema, Class rhsType, String rhsAlias)
   {
      Join join = new Join(schema, rhsType, rhsAlias);
      join.setRequired(true);

      qe.addJoin(join);

      return join;
   }

   /**
    * Convenience method which creates an empty QueryExtension and adds a given
    * <code>PredicateTerm</code>.
    *
    * @param predicateTerm The predicate term
    * @return The new query extension
    */
   public DeleteDescriptor where(PredicateTerm predicateTerm)
   {
      qe.setWhere(predicateTerm);

      return this;
   }

   public Class getType()
   {
      return tdType.getType();
   }

   public boolean isAffectingLockTable()
   {
      return affectingLockTable;
   }

   public QueryExtension getQueryExtension()
   {
      return qe;
   }

   public PredicateTerm getPredicateTerm()
   {
      return qe.getPredicateTerm();
   }
}
