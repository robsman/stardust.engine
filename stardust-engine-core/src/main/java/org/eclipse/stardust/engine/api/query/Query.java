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
package org.eclipse.stardust.engine.api.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic functionality for creating query containers for complex queries.
 *
 * <p>A query consists of the following three optional parts:
 * <ul>
 *    <li>Filter criteria to qualify/reduce the set of data returned</li>
 *    <li>Order conditions to (partially) specify the order of the data returned.</li>
 *    <li>Evaluation policies to select strategies for evaluating the query.</li>
 * </ul>
 * </p>
 *
 * @author rsauer
 * @version $Revision$
 */
public abstract class Query implements Serializable
{
   private FilterAndTerm filter;

   private final OrderCriteria order;
   private final Map policies;

   public Query(FilterVerifier filterVerifyer)
   {
      this.filter = new FilterAndTerm(filterVerifyer);
      this.order = new OrderCriteria();
      this.policies = new HashMap();
   }

   protected Query(Query rhs, FilterCopier copier)
   {
      this.filter = (FilterAndTerm) copier.visit(rhs.getFilter(), null);

      this.order = OrderCopier.copy(rhs.order);
      this.policies = new HashMap(rhs.policies);
   }

   /**
    * Gets the top-level filter term.
    * 
    * @return The top-level AND filtert term.
    * 
    * @see #where(FilterCriterion)
    */
   public final FilterAndTerm getFilter()
   {
      return filter;
   }

   /**
    * Adds the given filter criterion to the top-level filter term.
    * 
    * @param filter The filter criterion to be added
    * @return The top-level AND filer-term, thus allowing for chained calls.
    * 
    * @throws UnsupportedFilterException if the filter criterion to be added is not valid
    *       for this specific query
    * 
    * @see #getFilter()
    */
   public final FilterAndTerm where(FilterCriterion filter)
         throws UnsupportedFilterException
   {
      return getFilter().and(filter);
   }

   /**
    * Adds a new order criterion.
    *
    * @param criterion The order criterion to add.
    *
    * @return The callee's order criteria, thus allowing chained calls.
    *
    * @see #orderBy(FilterableAttribute, boolean)
    * @see #orderBy(FilterableAttribute)
    */
   public final OrderCriteria orderBy(OrderCriterion criterion)
   {
      return order.and(criterion);
   }

   /**
    * Orders the resulting elements by ascending values of the given attribute.
    *
    * @param attribute The attribute to order by.
    *
    * @return The callee's order criteria, thus allowing chained calls.
    *
    * @see #orderBy(FilterableAttribute, boolean)
    * @see #orderBy(OrderCriterion)
    */
   public final OrderCriteria orderBy(FilterableAttribute attribute)
   {
      return order.and(attribute);
   }

   /**
    * Orders the resulting elements by either ascending or descending values of the given
    * attribute.
    *
    * @param attribute The attribute to order by.
    * @param ascending Flag indicating if ordering should be performed by either ascending
    *                  or descending attribute value.
    *
    * @return The callee's order criteria, thus allowing chained calls.
    *
    * @see #orderBy(FilterableAttribute)
    * @see #orderBy(OrderCriterion)
    */
   public final OrderCriteria orderBy(FilterableAttribute attribute, boolean ascending)
   {
      return order.and(attribute, ascending);
   }
   
   /**
    * Gets the top-level order criteria.
    * 
    * @return The top-level order criteria.
    * 
    * @see #orderBy(FilterableAttribute)
    * @see #orderBy(OrderCriterion)
    * @see #orderBy(FilterableAttribute, boolean)
    */
   public final OrderCriteria getOrderCriteria()
   {
      return order;
   }

   /**
    * Extracts any set policy of the given kind.
    *
    * @param policyClass The kind of policy to extract.
    *
    * @return The policy, or <code>null</code> if no such policy exists for the query.
    *
    * @see #setPolicy
    * @see #removePolicy
    */
   public final EvaluationPolicy getPolicy(Class policyClass)
   {
      return (EvaluationPolicy) policies.get(policyClass);
   }

   /**
    * Sets the given policy.
    *
    * <p>There always exists at most one policy per policy class. Thus an existing policy
    * of the same class is overwritten.</p>
    *
    * @param policy The policy to be set.
    *
    * @see #getPolicy
    * @see #removePolicy
    */
   public final void setPolicy(EvaluationPolicy policy)
   {
      if (null != policy)
      {
         policies.put(policy.getClass(), policy);
      }
   }

   /**
    * Removes any set policy of the given kind.
    *
    * @param policyClass The kind of policy to be removed from the query.
    *
    * @see #getPolicy
    * @see #setPolicy
    */
   public final void removePolicy(Class policyClass)
   {
      policies.remove(policyClass);
   }

   /**
    * Template method used for query evaluation. The most simple implementation just
    * delegates to <code>getFilter().accept(visitor, context)</code>.
    *
    * <p>Can be overwritten to manipulate the tree of filter criteria before evaluation.
    * </p>
    *
    * @param visitor The visitor used for filter evaluation.
    */
   Object evaluateFilter(FilterEvaluationVisitor visitor, Object context)
   {
      return getFilter().accept(visitor, context);
   }

   /**
    * Template method used for query evaluation. The most simple implementation just
    * delegates to <code>getOrder().accept(visitor, context)</code>.
    *
    * <p>Can be overwritten to manipulate the tree of order criteria before evaluation.
    * </p>
    *
    * @param visitor The visitor used for order evaluation.
    */
   Object evaluateOrder(OrderEvaluationVisitor visitor, Object context)
   {
      return order.accept(visitor, context);
   }
}
