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
 * Unary filter operator to be applied directly to attributes of items queried for.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see BinaryOperatorFilter
 * @see TernaryOperatorFilter
 * @see ActivityInstanceQuery
 * @see ProcessInstanceQuery
 * @see LogEntryQuery
 * @see UserQuery
 */
final class UnaryOperatorFilterImpl extends ScopedFilterImpl
      implements UnaryOperatorFilter
{
   private final Operator.Unary operator;
   private final String attribute;

   protected UnaryOperatorFilterImpl(Class scope, Operator.Unary operator, String attribute)
   {
      super(scope);

      this.operator = operator;
      this.attribute = attribute;
   }

   public Operator.Unary getOperator()
   {
      return operator;
   }

   public String getAttribute()
   {
      return attribute;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
}
