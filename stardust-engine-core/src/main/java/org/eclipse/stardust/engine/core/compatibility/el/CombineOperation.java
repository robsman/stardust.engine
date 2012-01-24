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

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


public class CombineOperation implements BooleanExpression
{
   private static final Logger trace = LogManager.getLogger(CombineOperation.class);

   public static final int AND = 1;
   public static final int OR = 2;
   public static final int NOT = 3;
   public static final int EQUAL = 4;
   public static final int NOT_EQUAL = 5;

   private static final String[] operatorNames = {"AND", "OR", "NOT", "EQUAL", "NOT-EQUAL"};

   private BooleanExpression lhsExpression;
   private BooleanExpression rhsExpression;
   private int operation;

   public CombineOperation(int operation, BooleanExpression lhsExpression, BooleanExpression rhsExpression)
   {
      this.lhsExpression = lhsExpression;
      this.rhsExpression = rhsExpression;
      this.operation = operation;
   }

   public String toString()
   {
      return "(" + lhsExpression.toString() + operatorNames[operation - 1] + rhsExpression.toString() + ")";
   }

   public void debug(Logger ps, String indent)
   {
      lhsExpression.debug(ps, indent + "  ");
      ps.debug(indent + operatorNames[operation - 1]);
      rhsExpression.debug(ps, indent + "  ");
   }

   public Result evaluate(SymbolTable symbolTable) throws EvaluationError
   {
      try
      {
         switch (operation)
         {
            case AND:
               return Result.TRUE.equals(lhsExpression.evaluate(symbolTable))
                   && Result.TRUE.equals(rhsExpression.evaluate(symbolTable))
                    ? Result.TRUE : Result.FALSE;
            case OR:
               return Result.TRUE.equals(lhsExpression.evaluate(symbolTable))
                   || Result.TRUE.equals(rhsExpression.evaluate(symbolTable))
                    ? Result.TRUE : Result.FALSE;
            case NOT:
               return Result.FALSE.equals(lhsExpression.evaluate(symbolTable))
                    ? Result.TRUE : Result.FALSE;
            case EQUAL:
               return lhsExpression.evaluate(symbolTable) == rhsExpression.evaluate(symbolTable)
                    ? Result.TRUE : Result.FALSE;
            case NOT_EQUAL:
               return lhsExpression.evaluate(symbolTable) != rhsExpression.evaluate(symbolTable)
                    ? Result.TRUE : Result.FALSE;
            default:
               throw new InternalException("Invalid operation: " + operation);
         }
      }
      catch (Exception e)
      {
         trace.warn("", e);
         throw new EvaluationError(e.getMessage());
      }
   }

   public BooleanExpression getLhsExpression()
   {
      return lhsExpression;
   }

   public BooleanExpression getRhsExpression()
   {
      return rhsExpression;
   }

   public int getOperation()
   {
      return operation;
   }
}

