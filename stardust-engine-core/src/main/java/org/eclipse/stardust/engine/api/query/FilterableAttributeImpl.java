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

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.persistence.Operator;


/**
 * Definition of an attribute supporting filter operations.
 *
 * <p>Currently supported predicates are:
 * <ul>
 *    <li>Testing for <code>NULL</code> (see {@link #isNull} or {@link #isNotNull})</li>
 *    <li>Testing for equality (see {@link #isEqual} or {@link #notEqual})</li>
 *    <li>Relations (see {@link #lessThan}, {@link #lessOrEqual}, {@link #greaterThan} or {@link #greaterOrEqual})</li>
 *    <li>Pattern matches (see {@link #like})</li>
 *    <li>Ranges (see {@link #between})</li>
 * </ul>
 * </p>
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see UnaryOperatorFilter
 * @see BinaryOperatorFilter
 * @see TernaryOperatorFilter
 */
public class FilterableAttributeImpl implements FilterableAttribute
{
   private final Class scope;
   private final String attributeName;

   /**
    * Creates a filter matching an attribute having a SQL <code>NULL</code> value.
    *
    * @return The readily configured filter.
    *
    * @see #isNotNull()
    */
   public final UnaryOperatorFilter isNull()
   {
      return new UnaryOperatorFilterImpl(scope, Operator.IS_NULL, attributeName);
   }

   /**
    * Creates a filter matching an attribute not having a SQL <code>NULL</code> value.
    *
    * @return The readily configured filter.
    *
    * @see #isNull()
    */
   public final UnaryOperatorFilter isNotNull()
   {
      return new UnaryOperatorFilterImpl(scope, Operator.IS_NOT_NULL, attributeName);
   }

   /**
    * Creates a filter matching an attribute being equal with the given
    * <code>value</code>.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #isEqual(long)
    * @see #isEqual(double)
    */
   public final BinaryOperatorFilter isEqual(String value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.IS_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being equal with the given
    * <code>value</code>.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #isEqual(String)
    * @see #isEqual(double)
    */
   public final BinaryOperatorFilter isEqual(long value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.IS_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being equal with the given
    * <code>value</code>.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #isEqual(String)
    * @see #isEqual(long)
    */
   public final BinaryOperatorFilter isEqual(double value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.IS_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being not equal with the given
    * <code>value</code>.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #notEqual(long)
    * @see #notEqual(double)
    */
   public final BinaryOperatorFilter notEqual(String value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.NOT_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being not equal with the given
    * <code>value</code>.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #notEqual(String)
    * @see #notEqual(double)
    */
   public final BinaryOperatorFilter notEqual(long value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.NOT_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being equal with the given
    * <code>value</code>.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #notEqual(String)
    * @see #notEqual(long)
    */
   public final BinaryOperatorFilter notEqual(double value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.NOT_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being less than the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #lessThan(long)
    * @see #lessThan(double)
    */
   public final BinaryOperatorFilter lessThan(String value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.LESS_THAN, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being less than the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #lessThan(String)
    * @see #lessThan(double)
    */
   public final BinaryOperatorFilter lessThan(long value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.LESS_THAN, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being less than the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #lessThan(String)
    * @see #lessThan(long)
    */
   public final BinaryOperatorFilter lessThan(double value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.LESS_THAN, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being less than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #lessOrEqual(long)
    * @see #lessOrEqual(double)
    */
   public final BinaryOperatorFilter lessOrEqual(String value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.LESS_OR_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being less than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #lessOrEqual(String)
    * @see #lessOrEqual(double)
    */
   public final BinaryOperatorFilter lessOrEqual(long value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.LESS_OR_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being less than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #lessOrEqual(String)
    * @see #lessOrEqual(long)
    */
   public final BinaryOperatorFilter lessOrEqual(double value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.LESS_OR_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being greater than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #greaterOrEqual(long)
    * @see #greaterOrEqual(double)
    */
   public final BinaryOperatorFilter greaterThan(String value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.GREATER_THAN, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being greater than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #greaterOrEqual(String)
    * @see #greaterOrEqual(double)
    */
   public final BinaryOperatorFilter greaterThan(long value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.GREATER_THAN, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being greater than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #greaterOrEqual(String)
    * @see #greaterOrEqual(long)
    */
   public final BinaryOperatorFilter greaterThan(double value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.GREATER_THAN, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being greater than the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #greaterThan(long)
    * @see #greaterThan(double)
    */
   public final BinaryOperatorFilter greaterOrEqual(String value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.GREATER_OR_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being greater than the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #greaterThan(String)
    * @see #greaterThan(double)
    */
   public final BinaryOperatorFilter greaterOrEqual(long value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.GREATER_OR_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being greater than the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the attribute, i.e.
    * arithmetic or lexical order.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    *
    * @see #greaterThan(String)
    * @see #greaterThan(long)
    */
   public final BinaryOperatorFilter greaterOrEqual(double value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.GREATER_OR_EQUAL, attributeName, value);
   }

   /**
    * Creates a filter matching a text attribute according to the pattern given by
    * <code>value</code>.
    * <p />
    * The pattern language syntax is that of SQL <code>LIKE</code> patterns. For details
    * please check the documentation of your database backend.
    *
    * @param value The value to match with.
    * @return The readily configured filter.
    */
   public final BinaryOperatorFilter like(String value)
   {
      return new BinaryOperatorFilterImpl(scope, Operator.LIKE, attributeName, value);
   }

   /**
    * Creates a filter matching an attribute being both greater than or equal the given
    * <code>lowerBound</code> and less than or equal the given <code>upperBound</code>.
    * <p />
    * The meaning of being less than or greater than is specific to the type of the
    * attribute, i.e. arithmetic or lexical order.
    *
    * @param lowerBound The lowerBound bound of the value range to match with.
    * @param upperBound The upperBound bound of the  value range to match with.
    * @return The readily configured filter.
    *
    * @see #between(long, long)
    * @see #between(double, double)
    */
   public final TernaryOperatorFilter between(String lowerBound, String upperBound)
   {
      return new TernaryOperatorFilterImpl(scope, Operator.BETWEEN, attributeName,
            lowerBound, upperBound);
   }

   /**
    * Creates a filter matching an attribute being both greater than or equal the given
    * <code>lowerBound</code> and less than or equal the given <code>upperBound</code>.
    * <p />
    * The meaning of being less than or greater than is specific to the type of the
    * attribute, i.e. arithmetic or lexical order.
    *
    * @param lowerBound The lowerBound bound of the value range to match with.
    * @param upperBound The upperBound bound of the  value range to match with.
    * @return The readily configured filter.
    *
    * @see #between(String, String)
    * @see #between(double, double)
    */
   public final TernaryOperatorFilter between(long lowerBound, long upperBound)
   {
      return new TernaryOperatorFilterImpl(scope, Operator.BETWEEN, attributeName,
            lowerBound, upperBound);
   }

   /**
    * Creates a filter matching an attribute being both greater than or equal the given
    * <code>lowerBound</code> and less than or equal the given <code>upperBound</code>.
    * <p />
    * The meaning of being less than or greater than is specific to the type of the
    * attribute, i.e. arithmetic or lexical order.
    *
    * @param lowerBound The lowerBound bound of the value range to match with.
    * @param upperBound The upperBound bound of the  value range to match with.
    * @return The readily configured filter.
    *
    * @see #between(String, String)
    * @see #between(long, long)
    */
   public final TernaryOperatorFilter between(double lowerBound, double upperBound)
   {
      return new TernaryOperatorFilterImpl(scope, Operator.BETWEEN, attributeName,
            lowerBound, upperBound);
   }

   public FilterableAttributeImpl(Class scope, String name)
   {
      this.scope = scope;
      this.attributeName = name;
   }

   /**
    * Returns the name of the attribute to apply this filter to.
    *
    * @return The attribute name.
    */
   public String getAttributeName()
   {
      return attributeName;
   }

   public String toString()
   {
      return Reflect.getHumanReadableClassName(scope) + "::" + attributeName;
   }
}
