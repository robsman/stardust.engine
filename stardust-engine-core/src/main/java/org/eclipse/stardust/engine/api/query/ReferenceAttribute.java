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
import org.eclipse.stardust.engine.core.persistence.Operator.Binary;
import org.eclipse.stardust.engine.core.persistence.Operator.Ternary;
import org.eclipse.stardust.engine.core.persistence.Operator.Unary;


/**
 * This class allows to create a filterable attribute which references an attribute
 * of another class. This other class is joined automatically.
 * 
 * @see UserQuery#REALM_ID
 * @see ActivityInstanceQuery#PROCESS_INSTANCE_PRIORITY
 * @see WorklistQuery#PROCESS_INSTANCE_PRIORITY
 * 
 * @author sborn
 *
 */
public final class ReferenceAttribute extends AttributeJoinDescriptor
      implements FilterableAttribute
{
   private static final long serialVersionUID = 1L;
   
   private FilterableAttribute attribute;

   public static UnaryOperatorFilter createUnaryOperatorFilter(
         UnaryOperatorFilter filter, IAttributeJoinDescriptor joinDescriptor)
   {
      return new ReferenceUnaryOperatorFilter(filter, joinDescriptor);
   }

   public static BinaryOperatorFilter createBinaryOperatorFilter(
         BinaryOperatorFilter filter, IAttributeJoinDescriptor joinDescriptor)
   {
      return new ReferenceBinaryOperatorFilter(filter, joinDescriptor);
   }

   public static TernaryOperatorFilter createTernaryOperatorFilter(
         TernaryOperatorFilter filter, IAttributeJoinDescriptor joinDescriptor)
   {
      return new ReferenceTernaryOperatorFilter(filter, joinDescriptor);
   }

   public ReferenceAttribute(FilterableAttribute attribute, Class joinRhsType,
         String lhsField, String rhsField, String joinAttributeName)
   {
      super(joinRhsType, lhsField, rhsField, joinAttributeName);
      this.attribute = attribute;
   }

   public TernaryOperatorFilter between(double lowerBound, double upperBound)
   {
      return new ReferenceTernaryOperatorFilter(attribute.between(lowerBound,
            upperBound), this);
   }

   public TernaryOperatorFilter between(long lowerBound, long upperBound)
   {
      return new ReferenceTernaryOperatorFilter(attribute.between(lowerBound,
            upperBound), this);
   }

   public TernaryOperatorFilter between(String lowerBound, String upperBound)
   {
      return new ReferenceTernaryOperatorFilter(attribute.between(lowerBound,
            upperBound), this);
   }

   public String getAttributeName()
   {
      return attribute.getAttributeName();
   }

   public BinaryOperatorFilter greaterOrEqual(double value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.greaterOrEqual(value), this);
   }

   public BinaryOperatorFilter greaterOrEqual(long value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.greaterOrEqual(value), this);
   }

   public BinaryOperatorFilter greaterOrEqual(String value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.greaterOrEqual(value), this);
   }

   public BinaryOperatorFilter greaterThan(double value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.greaterThan(value), this);
   }

   public BinaryOperatorFilter greaterThan(long value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.greaterThan(value), this);
   }

   public BinaryOperatorFilter greaterThan(String value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.greaterThan(value), this);
   }

   public BinaryOperatorFilter isEqual(double value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.isEqual(value), this);
   }

   public BinaryOperatorFilter isEqual(long value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.isEqual(value), this);
   }

   public BinaryOperatorFilter isEqual(String value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.isEqual(value), this);
   }

   public UnaryOperatorFilter isNotNull()
   {
      return new ReferenceUnaryOperatorFilter(attribute.isNotNull(), this);
   }

   public UnaryOperatorFilter isNull()
   {
      return new ReferenceUnaryOperatorFilter(attribute.isNull(), this);
   }

   public BinaryOperatorFilter lessOrEqual(double value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.lessOrEqual(value), this);
   }

   public BinaryOperatorFilter lessOrEqual(long value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.lessOrEqual(value), this);
   }

   public BinaryOperatorFilter lessOrEqual(String value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.lessOrEqual(value), this);
   }

   public BinaryOperatorFilter lessThan(double value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.lessThan(value), this);
   }

   public BinaryOperatorFilter lessThan(long value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.lessThan(value), this);
   }

   public BinaryOperatorFilter lessThan(String value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.lessThan(value), this);
   }

   public BinaryOperatorFilter like(String value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.like(value), this);
   }

   public BinaryOperatorFilter notEqual(double value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.notEqual(value), this);
   }

   public BinaryOperatorFilter notEqual(long value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.notEqual(value), this);
   }

   public BinaryOperatorFilter notEqual(String value)
   {
      return new ReferenceBinaryOperatorFilter(attribute.notEqual(value), this);
   }
   
   public int hashCode()
   {
      return attribute.hashCode();
   }

   public boolean equals(Object obj)
   {
      return attribute.equals(obj);
   }

   public String toString()
   {
      return attribute.toString();
   }

   private static final class ReferenceUnaryOperatorFilter extends
         AttributeJoinDescriptor implements UnaryOperatorFilter
   {
      private UnaryOperatorFilter filter;

      public ReferenceUnaryOperatorFilter(UnaryOperatorFilter filter,
            IAttributeJoinDescriptor joinDesc)
      {
         super(joinDesc.getJoinRhsType(), (Pair[]) joinDesc.getJoinFields().toArray(
               new Pair[] {}), joinDesc.getJoinAttributeName());
         this.filter = filter;
      }

      public Object accept(FilterEvaluationVisitor visitor, Object context)
      {
         return visitor.visit(this, context);
      }

      public String getAttribute()
      {
         return filter.getAttribute();
      }

      public Unary getOperator()
      {
         return filter.getOperator();
      }

      public Class getScope()
      {
         return filter.getScope();
      }
   }

   private static final class ReferenceBinaryOperatorFilter extends
         AttributeJoinDescriptor implements BinaryOperatorFilter
   {
      private BinaryOperatorFilter filter;

      public ReferenceBinaryOperatorFilter(BinaryOperatorFilter filter,
            IAttributeJoinDescriptor joinDesc)
      {
         super(joinDesc.getJoinRhsType(), (Pair[]) joinDesc.getJoinFields().toArray(
               new Pair[] {}), joinDesc.getJoinAttributeName());
         this.filter = filter;
      }

      public Object accept(FilterEvaluationVisitor visitor, Object context)
      {
         return visitor.visit(this, context);
      }

      public String getAttribute()
      {
         return filter.getAttribute();
      }

      public Binary getOperator()
      {
         return filter.getOperator();
      }

      public Object getValue()
      {
         return filter.getValue();
      }

      public Class getScope()
      {
         return filter.getScope();
      }
   }

   private static final class ReferenceTernaryOperatorFilter extends
         AttributeJoinDescriptor implements TernaryOperatorFilter
   {
      private TernaryOperatorFilter filter;

      public ReferenceTernaryOperatorFilter(TernaryOperatorFilter filter,
            IAttributeJoinDescriptor joinDesc)
      {
         super(joinDesc.getJoinRhsType(), (Pair[]) joinDesc.getJoinFields().toArray(
               new Pair[] {}), joinDesc.getJoinAttributeName());
         this.filter = filter;
      }

      public Object accept(FilterEvaluationVisitor visitor, Object context)
      {
         return visitor.visit(this, context);
      }

      public String getAttribute()
      {
         return filter.getAttribute();
      }

      public Ternary getOperator()
      {
         return filter.getOperator();
      }

      public Pair getValue()
      {
         return filter.getValue();
      }

      public Class getScope()
      {
         return filter.getScope();
      }
   }
}