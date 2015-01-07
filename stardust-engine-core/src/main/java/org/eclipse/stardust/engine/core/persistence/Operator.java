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

import org.eclipse.stardust.common.StringKey;

/**
 * Enum class listing all currently supported comparison operators.
 *
 * @author rsauer
 * @version $Revision$
 */
public abstract class Operator extends StringKey
{
   private static final long serialVersionUID = 8798359914713197157L;

   /**
    * Resolves to true if the operand does not have a value.
    */
   public static final Unary IS_NULL = new Unary("IS NULL");
   /**
    * Resolves to true if the operand does have a value.
    */
   public static final Unary IS_NOT_NULL = new Unary("IS NOT NULL");

   /**
    * Resolves to true if the operand is equal the given value.
    */
   public static final Binary IS_EQUAL = new Binary("=");
   /**
    * Resolves to true if the operand is not equal the given value.
    */
   public static final Binary NOT_EQUAL = new Binary("<>");
   /**
    * Resolves to true if the operand is less than the given value.
    */
   public static final Binary LESS_THAN = new Binary("<");
   /**
    * Resolves to true if the operand less than or equal the given value.
    */
   public static final Binary LESS_OR_EQUAL = new Binary("<=");
   /**
    * Resolves to true if the operand is greater than the given value.
    */
   public static final Binary GREATER_THAN = new Binary(">");
   /**
    * Resolves to true if the operand is greater than or equal the given value.
    */
   public static final Binary GREATER_OR_EQUAL = new Binary(">=");
   /**
    * Resolves to true if the value of the operand is matched by the given pattern.
    * Pattern syntax may be dependent on context, i.e. regular expressions or SQL LIKE
    * patterns.
    */
   public static final Binary LIKE = new Binary("LIKE");
   /**
    * Resolves to true if the operand is equal one of the values in the given list.
    */
   public static final Binary IN = new Binary("IN");
   /**
    * Resolves to true if the operand is not equal any of the values in the given list.
    */
   public static final Binary NOT_IN = new Binary("NOT IN");
   /**
    * Resolves to true if the operand does not match any of the values in the given list.
    */
   public static final Binary NOT_ANY_OF = new Binary("NOT ANY OF");

   /**
    * Resolves to true if the operand greater than or equal the first given value and less
    * than or equal the second given value.
    */
   public static final Ternary BETWEEN = new Ternary("BETWEEN", "AND");

   private Operator(String id)
   {
      super(id, id);
   }

   /**
    * Indicates if the operator is unary, i.e. supports no comparison operands.
    *
    * @return <code>true</code> if the operator is unary, else <code>false</code>.
    */
   public abstract boolean isUnary();

   /**
    * Indicates if the operator is binary, i.e. supports one comparison operand.
    *
    * @return <code>true</code> if the operator is binary, else <code>false</code>.
    */
   public abstract boolean isBinary();

   /**
    * Indicates if the operator is ternary, i.e. supports two comparison operands.
    *
    * @return <code>true</code> if the operator is ternary, else <code>false</code>.
    */
   public abstract boolean isTernary();

   /**
    * Enum class listing all currently supported unary comparison operators.
    *
    * @author rsauer
    * @version $Revision$
    */
   public static final class Unary extends Operator
   {
      private Unary(String id)
      {
         super(id);
      }

      public boolean isUnary()
      {
         return true;
      }

      public boolean isBinary()
      {
         return false;
      }

      public boolean isTernary()
      {
         return false;
      }
   }

   /**
    * Enum class listing all currently supported binary comparison operators.
    *
    * @author rsauer
    * @version $Revision$
    */
   public static final class Binary extends Operator
   {
      private static final long serialVersionUID = -5223941647191028400L;

      private Binary(String id)
      {
         super(id);
      }

      public boolean isUnary()
      {
         return false;
      }

      public boolean isBinary()
      {
         return true;
      }

      public boolean isTernary()
      {
         return false;
      }
   }

   /**
    * Enum class listing all currently supported ternary comparison operators.
    *
    * @author rsauer
    * @version $Revision$
    */
   public static final class Ternary extends Operator
   {
      /**
       *
       */
      private static final long serialVersionUID = -7083917706180387538L;
      private final String secondOperator;

      private Ternary(String firstOperator, String secondOperator)
      {
         super(firstOperator);

         this.secondOperator = secondOperator;
      }

      public String getSecondOperator()
      {
         return secondOperator;
      }

      public boolean isUnary()
      {
         return false;
      }

      public boolean isBinary()
      {
         return false;
      }

      public boolean isTernary()
      {
         return true;
      }
   }
}
