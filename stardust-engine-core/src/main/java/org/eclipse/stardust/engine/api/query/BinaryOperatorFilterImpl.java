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

import org.eclipse.stardust.engine.core.persistence.Operator;

/**
 * Binary filter operator to be applied directly to attributes of items queried for.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see UnaryOperatorFilter
 * @see TernaryOperatorFilter
 * @see ActivityInstanceQuery
 * @see ProcessInstanceQuery
 * @see LogEntryQuery
 * @see UserQuery
 */
final class BinaryOperatorFilterImpl extends ScopedFilterImpl
      implements BinaryOperatorFilter
{
   private final Operator.Binary operator;
   private final String attribute;
   private final Object value;

   protected BinaryOperatorFilterImpl(Class scope, Operator.Binary operator, String attribute, String value)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
      this.value = value;
   }

   protected BinaryOperatorFilterImpl(Class scope, Operator.Binary operator, String attribute, long value)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
      this.value = new Long(value);
   }

   protected BinaryOperatorFilterImpl(Class scope, Operator.Binary operator, String attribute, double value)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
      this.value = new Double(value);
   }

   protected BinaryOperatorFilterImpl(Class scope, Operator.Binary operator,
         String attribute, Object value)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
      this.value = value;
   }

   public Operator.Binary getOperator()
   {
      return operator;
   }

   public String getAttribute()
   {
      return attribute;
   }

   public Object getValue()
   {
      return value;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   @Override
   public String toString()
   {
      return attribute + ' ' + operator + ' ' + value;
   }
}
