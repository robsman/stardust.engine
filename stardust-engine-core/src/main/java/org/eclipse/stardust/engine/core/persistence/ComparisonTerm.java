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

import org.eclipse.stardust.common.Pair;

/**
 * ComparisonTerm holds a comparison description between an attribute and an
 * value expression by an <code>Operator</code>.
 *
 * @author sborn
 * @version $Revision$
 */
public class ComparisonTerm implements PredicateTerm
{
   private final FieldRef lhsField;
   private final Operator operator;
   private final Object valueExpr;

   private String tag;

   /**
    * Constructs an <code>ComparisonTerm</code> which describes an comparison
    * of an alias-qualified attribute by an unary operator.
    *
    * @param lhsField The field to be compared
    * @param operator An unary operator
    */
   public ComparisonTerm(FieldRef lhsField, Operator.Unary operator)
   {
      this.lhsField = lhsField;
      this.operator = operator;
      this.valueExpr = null;
   }

   /**
    * Constructs an <code>ComparisonTerm</code> which describes an comparison
    * of an alias-qualified attribute by a binary operator with a value expression.
    *
    * @param lhsField The field to be compared
    * @param operator An binary operator
    * @param valueExpr The value expression
    */
   public ComparisonTerm(FieldRef lhsField, Operator.Binary operator,
         Object valueExpr)
   {
      this.lhsField = lhsField;
      this.operator = operator;
      this.valueExpr = valueExpr;
   }

   /**
    * Constructs an <code>ComparisonTerm</code> which describes an comparison
    * of an alias-qualified attribute by a ternary operator with a pair of value expressions.
    *
    * @param lhsField The field to be compared
    * @param operator An ternary operator
    * @param valueExpr Two value expressions hold by a <code>Pair</code>
    */
   public ComparisonTerm(FieldRef lhsField, Operator.Ternary operator, Pair valueExpr)
   {
      this.lhsField = lhsField;
      this.operator = operator;
      this.valueExpr = valueExpr;
   }

   public FieldRef getLhsField()
   {
      return lhsField;
   }

   public Operator getOperator()
   {
      return operator;
   }

   public Object getValueExpr()
   {
      return valueExpr;
   }

   @Override
   public void setTag(String tag)
   {
      this.tag = tag;
   }

   @Override
   public String getTag()
   {
      return this.tag;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(50);

      if (null != lhsField.getType().getTableAlias())
      {
         buffer.append(lhsField.getType().getTableAlias()).append(".");
      }

      buffer.append(lhsField.fieldName).append(" ").append(operator).append(" ");

      if (valueExpr instanceof String)
      {
         buffer.append("'").append(valueExpr).append("'");
      }
      else
      {
         buffer.append(valueExpr);
      }

      return buffer.toString();
   }
}
