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

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.core.persistence.Operator;


/**
 * Ternary filter operator to be applied directly to attributes of items queried for.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see UnaryOperatorFilter
 * @see BinaryOperatorFilter
 * @see ActivityInstanceQuery
 * @see ProcessInstanceQuery
 * @see LogEntryQuery
 * @see UserQuery
 */
final class TernaryOperatorFilterImpl extends ScopedFilterImpl
      implements TernaryOperatorFilter
{
   private final Operator.Ternary operator;
   private final String attribute;
   private final Pair value;

   public TernaryOperatorFilterImpl(Class scope, Operator.Ternary operator,
         String attribute, String firstValue, String secondValue)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
      this.value = new Pair(firstValue, secondValue);
   }

   public TernaryOperatorFilterImpl(Class scope, Operator.Ternary operator, String attribute,
         long firstValue, long secondValue)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
      this.value = new Pair(new Long(firstValue), new Long(secondValue));
   }

   public TernaryOperatorFilterImpl(Class scope, Operator.Ternary operator, String attribute,
         double firstValue, double secondValue)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
      this.value = new Pair(new Double(firstValue), new Double(secondValue));
   }

   protected TernaryOperatorFilterImpl(Class scope, Operator.Ternary operator,
         String attribute, Pair value)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
      this.value = value;
   }

   public Operator.Ternary getOperator()
   {
      return operator;
   }

   public String getAttribute()
   {
      return attribute;
   }
   
   public Pair getValue()
   {
      return value;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
}
