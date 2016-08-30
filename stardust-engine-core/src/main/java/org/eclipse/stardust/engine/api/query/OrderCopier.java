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

import java.util.Iterator;

/**
 * @author rsauer
 * @version $Revision$
 */
public final class OrderCopier implements OrderEvaluationVisitor
{
   public static final OrderCriteria copy(OrderCriteria criteria)
   {
      OrderCopier copier = new OrderCopier();
      return (OrderCriteria) copier.visit(criteria, null);
   }

   private OrderCopier()
   {
   }

   public Object visit(OrderCriteria order, Object context)
   {
      OrderCriteria result = new OrderCriteria();

      for (Iterator itr = order.getCriteria().iterator(); itr.hasNext();)
      {
         OrderCriterion criterion = (OrderCriterion) itr.next();
         result.and((OrderCriterion) criterion.accept(this, context));
      }

      return result;
   }

   public Object visit(AttributeOrder order, Object context)
   {
      return order;
   }

   public Object visit(DataOrder order, Object context)
   {
      return order;
   }
   
   public Object visit(DescriptorOrder order, Object context)
   {
      return order;
   }
   
   public Object visit(CustomOrderCriterion order, Object context)
   {
      return order;
   }

   public Object visit(RootProcessDefinitionDescriptor rootProcessDefinitionDescriptor, Object context)
   {
      return null;
   }
}