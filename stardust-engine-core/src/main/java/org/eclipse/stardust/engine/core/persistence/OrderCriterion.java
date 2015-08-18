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

/**
 * OrderCriterion is a single element for building a <code>OrderCriteria</code> which
 * can be used to order the result of a query.
 *  
 * @author sborn
 * @version $Revision$
 */
public class OrderCriterion
{
   private FieldRef fieldRef;
   private boolean isAscending;
   
   /**
    * Constructs an OrderCriterion for a given field reference and the order direction.
    * 
    * @param fielRef Field reference
    * @param isAscending true means that the ordering is ascending otherwise descending
    */
   public OrderCriterion(FieldRef fielRef, boolean isAscending)
   {
      this.fieldRef = fielRef;
      this.isAscending = isAscending;
   }
   
   /**
    * Constructs an ascending OrderCriterion for a given field reference.
    * 
    * @param fieldRef Field reference
    */
   public OrderCriterion(FieldRef fieldRef)
   {
      this(fieldRef, true);
   }

   public FieldRef getFieldRef()
   {
      return fieldRef;
   }

   public boolean isAscending()
   {
      return isAscending;
   }
}