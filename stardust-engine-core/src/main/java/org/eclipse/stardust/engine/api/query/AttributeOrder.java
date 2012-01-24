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

/**
 * Criterion for ordering elements resulting from a query according to a given attribute,
 * either with ascending or descending values.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see OrderCriteria
 */
public class AttributeOrder extends AbstractSingleOrderCriterion
{
   private static final long serialVersionUID = 2L;
   
   private final FilterableAttribute attribute;

   /**
    * Creates a criterion for ordering by ascending attribute values.
    *
    * @param attribute The attribute to order by.
    *
    * @see #AttributeOrder(FilterableAttribute, boolean)
    */
   public AttributeOrder(FilterableAttribute attribute)
   {
      this(attribute, true);
   }

   /**
    * Creates a criterion for ordering by either ascending or descending attribute values.
    *
    * @param attribute
    * @param ascending
    */
   public AttributeOrder(FilterableAttribute attribute, boolean ascending)
   {
      super(ascending);
      this.attribute = attribute;
   }

   /**
    * Gets the name of the attribute to order by.
    *
    * @return The attribute name.
    * 
    * @deprecated Use {@link #getAttributeName()} instead.
    */
   public String getAttribute()
   {
      return getAttributeName();
   }

   /**
    * Gets the name of the attribute to order by.
    *
    * @return The attribute name.
    */
   public String getAttributeName()
   {
      return attribute.getAttributeName();
   }
   
   /**
    * Gets the attribute to order by.
    *
    * @return The attribute.
    * 
    * TODO: Rename to getAttribute() and change to public when 
    * current deprecated method with this name will be removed. 
    */
   FilterableAttribute getFilterableAttribute()
   {
      return attribute;
   }

   public Object accept(OrderEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
}
