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

public class Functions
{
   public static BoundFunction constantExpression(String expression)
   {
      return constantExpression(expression, expression);
   }
   
   public static BoundFunction constantExpression(String expression, String selector)
   {
      BoundFunction boundFunction = new BoundFunction(new Function.Constant(expression));
      boundFunction.setSelector(selector);
      return boundFunction;
   }
   
   public static BoundFunction rowCount()
   {
      return new BoundFunction(Function.ROW_COUNT);
   }
   
   public static BoundFunction count(FieldRef field)
   {
      return new BoundFunction(Function.COUNT, field);
   }
   
   public static BoundFunction countDistinct(FieldRef field)
   {
      return new BoundFunction(Function.COUNT_DISTINCT, new FieldRef[] {field});
   }
   
   public static BoundFunction countDistinct(FieldRef[] fields)
   {
      return new BoundFunction(Function.COUNT_DISTINCT, fields);
   }
   
   public static BoundFunction min(FieldRef field)
   {
      return new BoundFunction(Function.MIN, field);
   }
   
   public static BoundFunction max(FieldRef field)
   {
      return new BoundFunction(Function.MAX, field);
   }
   
   public static UnaryPredicateFunction strUpper(FieldRef field)
   {
      return new UnaryPredicateFunction(Function.STR_UPPER, field);
   }
   
   public static UnaryPredicateFunction strLower(FieldRef field)
   {
      return new UnaryPredicateFunction(Function.STR_LOWER, field);
   }
   
   private Functions()
   {
      // utility class
   }
   
   public static class BoundFunction implements Column
   {
      private final Function function;
      private final FieldRef[] operands;
      private String selector;
      
      private BoundFunction(Function.ParamLess function)
      {
         this(function, new FieldRef[0]);
      }

      private BoundFunction(Function.Unary function, FieldRef operand)
      {
         this(function, new FieldRef[] {operand});
      }

      private BoundFunction(Function.Nary function, FieldRef[] operands)
      {
         this((Function) function, operands);
      }

      private BoundFunction(Function function, FieldRef[] operands)
      {      
         this.function = function;
         this.operands = operands;
      }

      public Function getFunction()
      {
         return function;
      }

      public FieldRef[] getOperands()
      {
         return operands;
      }
      
      public void setSelector(String selector)
      {
         this.selector = selector;
      }
      
      public String getSelector()
      {
         return selector;
      }
   }

   public static class UnaryPredicateFunction extends FieldRef implements IUnaryFunction
   {
      private final Function function;
      private final FieldRef operand;
      
      private UnaryPredicateFunction(Function.Unary function, FieldRef operand)
      {
         super(operand.getType(), operand.fieldName);
         
         this.function = function;
         this.operand = operand;
      }

      public String getFunctionName()
      {
         return function.getId();
      }

      public Function getFunction()
      {
         return function;
      }

      public FieldRef getOperand()
      {
         return operand;
      }
      
      public String toString()
      {
         return function.getId() + "(" + super.toString() + ")";
      }
   }
}
