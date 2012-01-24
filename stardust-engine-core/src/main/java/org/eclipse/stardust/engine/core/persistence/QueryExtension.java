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

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;


/**
 * QueryExtension holds a description how to extend a simple query like
 * 'select attribute[,attribute]* from tablename o'.
 *
 * @author sborn
 * @version $Revision$
 */
public class QueryExtension
{
   private static final Column[] NO_SELECTION = new Column[0];

   private boolean distinct;
   private boolean engineDistinct;
   private String schema;
   private String alias;
   private Column[] selection;
   private Joins joins;
   private PredicateTerm predicateTerm;
   private OrderCriteria orderCriteria;
   private List<FieldRef> groupCriteria;
   private Map<String, Serializable> hints;
   private String selectAlias;

   /**
    * Construct a shallow copy of the given query extension template.
    *
    * @param rhs The template query extension.
    */
   public static QueryExtension shallowCopy(QueryExtension rhs)
   {
      return (null == rhs) ? new QueryExtension() : new QueryExtension(rhs, false);
   }

   public static QueryExtension deepCopy(QueryExtension rhs)
   {
      return (null == rhs) ? new QueryExtension() : new QueryExtension(rhs, true);
   }

   /**
    * Factory method which creates an empty QueryExtension and adds a given
    * <code>PredicateTerm</code>.
    *
    * @param predicateTerm The predicate term
    * @return The new query extension
    */
   public static QueryExtension where(PredicateTerm predicateTerm)
   {
      return new QueryExtension().setWhere(predicateTerm);
   }

   /**
    * This method constructs an empty QueryExtension.
    */
   public QueryExtension()
   {
      selection = NO_SELECTION;
      joins = new Joins();
      predicateTerm = new AndTerm();
      orderCriteria = new OrderCriteria();
      groupCriteria = new ArrayList<FieldRef>();
      distinct = false;
      engineDistinct = false;
      hints = null;
      selectAlias = null;
   }

   private QueryExtension(QueryExtension rhs, boolean deep)
   {
      this.distinct = rhs.distinct;
      this.engineDistinct = rhs.engineDistinct;
      this.schema = rhs.schema;
      this.alias = rhs.alias;
      this.selection = rhs.selection;
      this.joins = Joins.shallowCopy(rhs.joins);
      this.predicateTerm = rhs.predicateTerm;
      this.orderCriteria = OrderCriteria.shallowCopy(rhs.orderCriteria);
      this.hints = rhs.hints;
      this.selectAlias = rhs.selectAlias;
      if (deep)
      {
         this.groupCriteria = new ArrayList<FieldRef>();
         this.groupCriteria.addAll(rhs.groupCriteria);
      }
      else
      {
         this.groupCriteria = Collections.unmodifiableList(rhs.groupCriteria);
      }
   }

   /**
    * TODO
    * @param schema
    */
   public QueryExtension setSchema(String schema)
   {
      this.schema = schema;

      return this;
   }

   /**
    * TODO
    * @param alias
    */
   public QueryExtension setAlias(String alias)
   {
      this.alias = alias;

      return this;
   }

   /**
    * Defines an alternate alias only for the SELECT clause.
    * @param selectAlias
    */
   public void setSelectAlias(String selectAlias)
   {
      this.selectAlias = selectAlias;
   }

   /**
    * This methode replaces the current SELECT-attributes by a given new one.
    *
    * Attention: providing a SELECT-Clause explicitly disables the mechanism
    * of building the SELECT-clause by reflection!
    *
    * @param attributes
    * @return <code>this</code> to allow for chained calls
    */
   public QueryExtension setSelection(Column[] fields)
   {
      if (null == fields)
      {
         this.selection = NO_SELECTION;
      }
      else
      {
         this.selection = fields;
      }

      return this;
   }

   /**
    * This method adds a join description to this QueryExtension.
    *
    * @param joinItem The join description
    * @return This QueryExtension
    */
   public QueryExtension addJoin(Join joinItem)
   {
      joins.add(joinItem);

      return this;
   }

   /**
    * This method adds join descriptions to this QueryExtension.
    *
    * @param joinItem The join description
    * @return This QueryExtension
    */
   public QueryExtension addJoins(Joins joins)
   {
      this.joins.add(joins);

      return this;
   }

   /**
    * This methode replaces the current predicate term by a given new one.
    *
    * @param predicateTerm The new predicatye term
    * @return This QueryExtension
    */
   public QueryExtension setWhere(PredicateTerm predicateTerm)
   {
      this.predicateTerm = predicateTerm;

      return this;
   }

   /**
    * This method adds an ascending order criterion by its criterion name.
    *
    * @param criterion Name of the order criterion
    * @return This QueryExtension
    */
   public QueryExtension addOrderBy(FieldRef criterion)
   {
      orderCriteria.add(criterion);

      return this;
   }

   /**
    * This method adds an order criterion by its criterion name and order direction.
    *
    * @param criterion Name of the order criterion
    * @param isAscending true means that the order is ascending otherwise descending
    * @return This QueryExtension
    */
   public QueryExtension addOrderBy(FieldRef criterion, boolean isAscending)
   {
      orderCriteria.add(criterion, isAscending);

      return this;
   }

   /**
    * This method adds an grouping criterion.
    *
    * @param criterion The name of the grouping criterion
    * @return This QueryExtension
    */
   public QueryExtension addGroupBy(FieldRef criterion)
   {
      groupCriteria.add(criterion);

      return this;
   }

   public String getSchema()
   {
      return schema;
   }

   public String getAlias()
   {
      return alias;
   }

   public String getSelectAlias()
   {
      return selectAlias;
   }

   public Column[] getSelection()
   {
      return selection;
   }

   public Joins getJoins()
   {
      return joins;
   }

   public PredicateTerm getPredicateTerm()
   {
      return predicateTerm;
   }

   public OrderCriteria getOrderCriteria()
   {
      return orderCriteria;
   }

   public void setOrderCriteria(OrderCriteria orderCriteria)
   {
      this.orderCriteria = orderCriteria;
   }

   public Collection<FieldRef> getGroupCriteria()
   {
      return Collections.unmodifiableList(groupCriteria);
   }

   /**
    * @return Flag whether database evaluated distinct is necessary.
    */
   public boolean isDistinct()
   {
      return distinct;
   }

   /**
    * @param engineDistinct Flag which signals whether database evaluated distinct
    *                       is necessary.
    */
   public QueryExtension setDistinct(boolean distinct)
   {
      this.distinct = distinct;

      return this;
   }

   /**
    * @return Flag whether engine evaluated distinct is necessary.
    */
   public boolean isEngineDistinct()
   {
      return engineDistinct;
   }

   /**
    * @param engineDistinct Flag which signals whether engine evaluated distinct
    *                       is necessary.
    */
   public void setEngineDistinct(boolean engineDistinct)
   {
      this.engineDistinct = engineDistinct;
   }

   public Map<String, Serializable> getHints()
   {
      if (hints == null)
      {
         hints = CollectionUtils.newHashMap();
      }

      return hints;
   }
}
