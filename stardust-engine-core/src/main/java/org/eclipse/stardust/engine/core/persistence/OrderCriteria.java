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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * OrderCriteria can hold an arbitrary number of <code>OrderCriterion</code> and
 * is used to sort the result of a query.
 * 
 * @author sborn
 * @version $Revision$
 */
public class OrderCriteria implements Iterable<OrderCriterion>
{
   private List<OrderCriterion> criteria;
   
   public static OrderCriteria shallowCopy(OrderCriteria rhs)
   {
      return new OrderCriteria(rhs);
   }
   
   /**
    * Constructs a OrderCriteria with a single <code>OrderCriterion</code>.
    * 
    * @param criterion Name of the criterion
    * @param isAscending true means that the ordering is ascending otherwise decsending
    */
   public OrderCriteria(FieldRef criterion, boolean isAscending)
   {
      criteria = new ArrayList<OrderCriterion>();

      if (null != criterion)
      {
         add(criterion, isAscending);
      }
   }

   /**
    * Constructs a OrderCriteria with a single ascending <code>OrderCriterion</code>.
    * 
    * @param criterion Name of the criterion
    */
   public OrderCriteria(FieldRef criterion)
   {
      this(criterion, true);
   }

   /**
    * Constructs an empty OrderCriteria.
    */
   public OrderCriteria()
   {
      this((FieldRef) null);
   }
   
   protected OrderCriteria(OrderCriteria rhs)
   {
      this.criteria = Collections.unmodifiableList(rhs.criteria);
   }

   /**
    * Adds a <code>OrderCriterion</code> by its criterion name an sorting direction.
    * 
    * @param criterion Name of the criterion
    * @param isAscending true means that the ordering is ascending otherwise decsending
    * @return This OrderCriteria
    */
   public OrderCriteria add(FieldRef criterion, boolean isAscending)
   {
      criteria.add(new OrderCriterion(criterion, isAscending));
      
      return this;
   }

   /**
    * Adds an ascending <code>OrderCriterion</code> by its criterion name.
    * 
    * @param criterion Name of the criterion
    * @return This OrderCriteria
    */
   public OrderCriteria add(FieldRef criterion)
   {
      criteria.add(new OrderCriterion(criterion, true));
      
      return this;
   }

   /**
    * Adds an <code>OrderCriterion</code>.
    * 
    * @param criterion The criterion
    * @return This OrderCriteria
    */
   public OrderCriteria add(OrderCriterion criterion)
   {
      if (null != criterion)
      {
         criteria.add(criterion);
      }
      
      return this;
   }

   /**
    * Adds an <code>OrderCriteria</code>.
    * 
    * @param criteria The criteria
    * @return This OrderCriteria
    */
   public OrderCriteria add(OrderCriteria criteria)
   {
      if (null != criteria)
      {
         for (Iterator<OrderCriterion> i = criteria.iterator(); i.hasNext();)
         {
            OrderCriterion criterion = i.next();
            this.criteria.add(criterion);
         }
      }
      
      return this;
   }

   /**
    * Returns an Iterator for all <code>OrderCriterion</code>s.
    * 
    * @return The iterator
    */
   public Iterator<OrderCriterion> iterator()
   {
      return criteria.iterator();
   }
   
   /**
    * Returns <code>true</code> if the criteria container contains no elements.
    * @return <code>true</code> if the criteria container contains no elements.
    */
   public boolean isEmpty()
   {
      return criteria.isEmpty();
   }
}
