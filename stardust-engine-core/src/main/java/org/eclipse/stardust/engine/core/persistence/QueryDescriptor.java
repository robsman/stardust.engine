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
package org.eclipse.stardust.engine.core.persistence;

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


public class QueryDescriptor extends TableDescriptor
      implements ITypeDescriptor, FieldRefResolver
{
   private TypeDescriptor tdType;
   
   private String alias;
   
   private final QueryExtension qe;
   
   public static QueryDescriptor shallowCopy(QueryDescriptor rhs)
   {
      QueryDescriptor result = new QueryDescriptor(rhs.getSchemaName(), QueryExtension.shallowCopy(rhs.qe));
      
      result.tdType = rhs.tdType;
      result.alias = rhs.alias;
      
      return result;
   }
   
   public static QueryDescriptor from(Class type)
   {
      return from(null, type, (String) null);
   }
   
   public static QueryDescriptor from(Class type, String alias)
   {
      return from(null, type, alias);
   }

   public static QueryDescriptor from(String schema, Class type)
   {
      return from(schema, type, (String) null);
   }
   
   /**
    * @deprecated
    */
   public static QueryDescriptor from(Class type, QueryExtension qe)
   {
      return from(null, type, qe);
   }
   
   /**
    * @deprecated
    */
   public static QueryDescriptor from(String schema, Class type, QueryExtension qe)
   {
      QueryDescriptor result = new QueryDescriptor(schema, QueryExtension.shallowCopy(qe));

      result.setType(type);

      return result;
   }
   
   public static QueryDescriptor from(String schema, Class type, String alias)
   {
      QueryDescriptor result = from(schema, type, (QueryExtension) null);

      result.setAlias(alias);

      return result;
   }
   
   public QueryDescriptor()
   {
      this(null, new QueryExtension());
   }
   
   protected QueryDescriptor(QueryExtension qe)
   {
      this(null, qe);
   }
   
   protected QueryDescriptor(String schemaName, QueryExtension qe)
   {
      super(schemaName);
      this.qe = qe;
   }
   
   public FieldRef resolveFieldRef(FieldRef field)
   {
      if (field.getType() == this)
      {
         return field;
      }

      // TODO cache resolvable tables
      Map resolvableTables = new HashMap();
      Set unresolvableTables = new HashSet();
      
      resolvableTables.put(tdType.getTableName(), this);

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
                  "Table {0} for field {1} can not be resolved.", new Object[] {
                        field.getType().getTableName(), field.fieldName}));
         }
         result = field;
      }

      return (field instanceof IUnaryFunction) ? new ResolvedUnaryFunction(result,
            (IUnaryFunction) field) : result;
   }

   public String getTableName()
   {
      return tdType.getTableName();
   }

   public String getTableAlias()
   {
      return StringUtils.isEmpty(alias) ? tdType.getTableAlias() : alias;
   }

   public String getDecryptKey()
   {
      return tdType.getDecryptKey();
   }

   public String getEncryptKey()
   {
      return tdType.getEncryptKey();
   }

   public List getLinks()
   {
      return tdType.getLinks();
   }

   public List getPersistentFields()
   {
      return tdType.getPersistentFields();
   }

   public List getPersistentVectors()
   {
      return tdType.getPersistentVectors();
   }

   public QueryDescriptor select(String attr)
   {
      return select(tdType.fieldRef(attr));
   }
   
   public QueryDescriptor selectDistinct(String attr)
   {
      QueryDescriptor query = select(tdType.fieldRef(attr));
      query.getQueryExtension().setDistinct(true);

      return query;
   }
   
   public QueryDescriptor select(Column column)
   {
      qe.setSelection(new Column[] {column});
      
      return this;
   }
   
   public QueryDescriptor select(Column col1, Column col2)
   {
      qe.setSelection(new Column[] {col1, col2});
      
      return this;
   }
   
   public QueryDescriptor select(Column col1, Column col2, Column col3)
   {
      qe.setSelection(new Column[] {col1, col2, col3});
      
      return this;
   }
   
   public QueryDescriptor select(Column col1, Column col2, Column col3, Column col4)
   {
      qe.setSelection(new Column[] {col1, col2, col3, col4});
      
      return this;
   }
   
   public QueryDescriptor select(Column col1, Column col2, Column col3, Column col4, Column col5)
   {
      qe.setSelection(new Column[] {col1, col2, col3, col4, col5});
      
      return this;
   }
   
   public QueryDescriptor select(Column col1, Column col2, Column col3, Column col4, Column col5, Column col6)
   {
      qe.setSelection(new Column[] {col1, col2, col3, col4, col5, col6});
      
      return this;
   }
   
   public QueryDescriptor select(String attr1, String attr2)
   {
      qe.setSelection(new FieldRef[] {tdType.fieldRef(attr1), tdType.fieldRef(attr2)});
      
      return this;
   }
   
   public QueryDescriptor select(String attr1, String attr2, String attr3)
   {
      qe.setSelection(new FieldRef[] {
            tdType.fieldRef(attr1), tdType.fieldRef(attr2), tdType.fieldRef(attr3)});
      
      return this;
   }
   
   public QueryDescriptor select(String[] attributes)
   {
      FieldRef[] fields = new FieldRef[attributes.length];
      for (int i = 0; i < attributes.length; i++ )
      {
         fields[i] = new FieldRef(tdType, attributes[i]);
      }
      qe.setSelection(fields);
      
      return this;
   }
   
   public QueryDescriptor select(Column[] columns)
   {
      qe.setSelection(columns);
      
      return this;
   }
   
   public Join innerJoin(Class rhsType)
   {
      return innerJoin(null, rhsType, null);
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
   
   public Join leftOuterJoin(Class rhsType)
   {
      return leftOuterJoin(null, rhsType, null);
   }
   
   public Join leftOuterJoin(String schema, Class rhsType)
   {
      return leftOuterJoin(schema, rhsType, null);
   }
   
   public Join leftOuterJoin(Class rhsType, String rhsAlias)
   {
      return leftOuterJoin(null, rhsType, rhsAlias);
   }
   
   public Join leftOuterJoin(String schema, Class rhsType, String rhsAlias)
   {
      Join join = new Join(schema, rhsType, rhsAlias);
      join.setRequired(false);

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
   public QueryDescriptor where(PredicateTerm predicateTerm)
   {
      setPredicateTerm(predicateTerm);
      
      return this;
   }
   
   public QueryDescriptor groupBy(FieldRef[] columns)
   {
      for (FieldRef column : columns)
      {
         getQueryExtension().addGroupBy(column);
      }
      
      return this;
   }

   public QueryDescriptor groupBy(FieldRef column)
   {
      getQueryExtension().addGroupBy(column);
      
      return this;
   }

   public QueryDescriptor groupBy(FieldRef col1, FieldRef col2)
   {
      groupBy(col1);
      
      getQueryExtension().addGroupBy(col2);
      
      return this;
   }

   public QueryDescriptor groupBy(FieldRef col1, FieldRef col2, FieldRef col3)
   {
      groupBy(col1, col2);
      
      getQueryExtension().addGroupBy(col3);
      
      return this;
   }

   public QueryDescriptor groupBy(FieldRef col1, FieldRef col2, FieldRef col3, FieldRef col4)
   {
      groupBy(col1, col2, col3);
      
      getQueryExtension().addGroupBy(col4);
      
      return this;
   }

   public QueryDescriptor groupBy(FieldRef col1, FieldRef col2, FieldRef col3, FieldRef col4, FieldRef col5)
   {
      groupBy(col1, col2, col3, col4);
      
      getQueryExtension().addGroupBy(col5);
      
      return this;
   }

   public QueryDescriptor orderBy(FieldRef column)
   {
      getQueryExtension().addOrderBy(column);
      
      return this;
   }

   public QueryDescriptor orderBy(FieldRef col1, FieldRef col2)
   {
      orderBy(col1);
      
      getQueryExtension().addOrderBy(col2);
      
      return this;
   }

   public QueryDescriptor orderBy(FieldRef col1, FieldRef col2, FieldRef col3)
   {
      orderBy(col1, col2);
      
      getQueryExtension().addOrderBy(col3);
      
      return this;
   }

   public QueryDescriptor orderBy(FieldRef col1, FieldRef col2, FieldRef col3,
         FieldRef col4)
   {
      orderBy(col1, col2, col3);
      
      getQueryExtension().addOrderBy(col4);
      
      return this;
   }

   public Class getType()
   {
      return tdType.getType();
   }

   public void setType(Class type)
   {
      this.tdType = TypeDescriptor.get(type);
   }

   public String getAlias()
   {
      return alias;
   }

   public void setAlias(String alias)
   {
      this.alias = alias;
   }

   public QueryExtension getQueryExtension()
   {
      return qe;
   }

   public PredicateTerm getPredicateTerm()
   {
      return qe.getPredicateTerm();
   }
   
   /**
    * This methode replaces the current predicate term by a given new one.
    * 
    * @param predicateTerm The new predicatye term
    */
   public void setPredicateTerm(PredicateTerm predicateTerm)
   {
      qe.setWhere(predicateTerm);
   }

   private static class ResolvedUnaryFunction extends FieldRef implements IUnaryFunction
   {
      private final IUnaryFunction function;
      
      public ResolvedUnaryFunction(FieldRef field, IUnaryFunction function)
      {
         super(field.getType(), field.fieldName);
         
         this.function = function;
      }

      public String getFunctionName()
      {
         return function.getFunctionName();
      }
      
   }
}
