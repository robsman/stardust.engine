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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A group of order criteria. Can be used to preconfigure specific order criteria for
 * later reuse.
 *
 * @author rsauer
 * @version $Revision$
 */
public final class OrderCriteria implements OrderCriterion
{
   private final List criteria;

   /**
    * Creates an empty group of order criteria.
    */
   public OrderCriteria()
   {
      this.criteria = new LinkedList();
   }

   /**
    * Groups the given order criteria.
    *
    * @param criteria The criteria to be grouped.
    */
   public OrderCriteria(OrderCriterion[] criteria)
   {
      this();

      for (int i = 0; i < criteria.length; i++)
      {
         and(criteria[i]);
      }
   }

   /**
    * Gets the list of order criteria grouped by the callee.
    *
    * @return The unmodifiable list of order criteria.
    */
   public List getCriteria()
   {
      return Collections.unmodifiableList(criteria);
   }

   public Object accept(OrderEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   /**
    * Adds another order criterion to the list of grouped criteria.
    *
    * @param criterion The criterion to be added.
    *
    * @see #and(FilterableAttribute)
    * @see #and(FilterableAttribute, boolean)
    */
   public OrderCriteria and(OrderCriterion criterion)
   {
      // @todo/belgium check scope

      criteria.add(criterion);
      return this;
   }

   /**
    * Orders by ascending values of the given attribute.
    *
    * @param attribute The attribute to order by.
    * @return The order criteria the operation is called on to allow chained calls.
    *
    * @see #and(FilterableAttribute, boolean)
    * @see #and(OrderCriterion)
    */
   public final OrderCriteria and(FilterableAttribute attribute)
   {
      // @todo/belgium check scope

      return and(attribute, true);
   }

   /**
    * Orders by either ascending or descending values of the given attribute.
    *
    * @param attribute The attribute to order by.
    * @param ascending Flag indicating if ordering should be performed by either ascending
    *                  or descending attribute value.
    * @return The order criteria the operation is called on to allow chained calls.
    *
    * @see #and(FilterableAttribute)
    * @see #and(OrderCriterion)
    */
   public final OrderCriteria and(FilterableAttribute attribute, boolean ascending)
   {
      // @todo/belgium check scope

      return and(new AttributeOrder(attribute, ascending));
   }
}
