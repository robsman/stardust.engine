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
package org.eclipse.stardust.engine.core.compatibility.el;

import java.text.MessageFormat;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;


public class ComparisonOperation implements BooleanExpression
{
   public static final int EQUAL = 1;
   public static final int NOT_EQUAL = 2;
   public static final int LESS = 3;
   public static final int LESS_EQUAL = 4;
   public static final int GREATER = 5;
   public static final int GREATER_EQUAL = 6;

   private static final String[] operators = {"=", "!=", "<", "<=", ">", ">="};
   private static final String[] operatorNames = {"EQUAL", "NOT-EQUAL", "LESS",
         "LESS-OR-EQUAL", "GREATER", "GREATER-OR-EQUAL"};

   private ValueExpression lhsValue;
   private ValueExpression rhsValue;
   private int operation;

   protected ComparisonOperation(int operation, ValueExpression lhsValue, ValueExpression rhsValue)
   {
      this.operation = operation;
      this.lhsValue = lhsValue;
      this.rhsValue = rhsValue;
   }

   public Result evaluate(SymbolTable symbolTable) throws EvaluationError
   {
      try
      {
         int result = compare(symbolTable);
         switch (operation)
         {
            case EQUAL:
               return result == 0 ? Result.TRUE : Result.FALSE;
            case NOT_EQUAL:
               return result != 0 ? Result.TRUE : Result.FALSE;
            case LESS:
               return result < 0 ? Result.TRUE : Result.FALSE;
            case LESS_EQUAL:
               return result <= 0 ? Result.TRUE : Result.FALSE;
            case GREATER:
               return result > 0 ? Result.TRUE : Result.FALSE;
            case GREATER_EQUAL:
               return result >= 0 ? Result.TRUE : Result.FALSE;
            default:
               throw new InternalException("Invalid operation: " + operation);
         }
      }
      catch (Exception e)
      {
         throw new EvaluationError(e.getMessage());
      }
   }

   private int compare(SymbolTable symbolTable) throws EvaluationError
   {
      Object lhs = lhsValue.evaluate(symbolTable);
      Object rhs = rhsValue.evaluate(symbolTable);

      int result;
      // Explicitly handle cases different from the CompareHelper#compare(lhs, rhs)
      // contract, delegate the rest.
      if (null == lhs)
      {
         if (null == rhs)
         {
            result = 0;
         }
         else
         {
            // An empty/unknown lhs object is considered smaller than anything else
            result = Integer.MIN_VALUE;
         }
      }
      else if (null == rhs)
      {
         // An empty/unknown rhs object is considered smaller than anything else
         result = Integer.MAX_VALUE;
      }
      else if (lhs instanceof Number)
      {
         if ( !(rhs instanceof Number))
         {
            throw new ClassCastException(
                  MessageFormat.format(
                        "A left-hand-side numeric operand is not comparison compatible with a right-hand-side operand of type {0}.",
                        new Object[] {Reflect.getHumanReadableClassName(rhs.getClass(), true)}));
         }
         
         // implicitly cast any number to Double
         double d1 = ((Number) lhs).doubleValue();
         double d2 = ((Number) rhs).doubleValue();

         if (d1 < d2)
         {
            result = -1;
         }
         else if (d1 > d2)
         {
            result = 1;
         }
         else
         {
            result = 0;
         }
      }
      else if ((lhs instanceof String) && !(rhs instanceof String))
      {
         throw new ClassCastException(
               MessageFormat.format(
                     "A left-hand-side alphanumeric operand is not comparison compatible with a right-hand-side operand of type {0}.",
                     new Object[] {Reflect.getHumanReadableClassName(rhs.getClass(), true)}));
      }
      else if ((rhs instanceof String) && !(lhs instanceof String))
      {
         throw new ClassCastException(
               MessageFormat.format(
                     "A left-hand-side operand of type {1} is not comparison compatible with a right-hand-side alphanumeric operand.",
                     new Object[] {Reflect.getHumanReadableClassName(rhs.getClass(), true)}));
      }
      else
      {
         result = CompareHelper.compare(lhs, rhs);
      }
      return result;
   }

   public String toString()
   {
      return "(" + lhsValue.toString() + " " + operators[operation - 1] + " " + rhsValue.toString() + ")";
   }

   public void debug(Logger ps, String indent)
   {
      lhsValue.debug(ps, indent + "  ");
      ps.debug(indent + operatorNames[operation - 1]);
      rhsValue.debug(ps, indent + "  ");
   }

   public ValueExpression getLhsValue()
   {
      return lhsValue;
   }

   public ValueExpression getRhsValue()
   {
      return rhsValue;
   }

   public int getOperation()
   {
      return operation;
   }
}
