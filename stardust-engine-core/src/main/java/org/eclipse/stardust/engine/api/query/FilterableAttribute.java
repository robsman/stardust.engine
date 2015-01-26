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

import java.io.Serializable;

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
 * <p>
 * IMPORTANT: This interface is <em>not</em> intended to be implemented.
 * </p>
 * 
 * @author rsauer
 * @version $Revision$
 *
 * @see UnaryOperatorFilter
 * @see BinaryOperatorFilter
 * @see TernaryOperatorFilter
 */
public interface FilterableAttribute extends Serializable
{

   /**
    * Creates a filter matching an attribute having a SQL <code>NULL</code> value.
    *
    * @return The readily configured filter.
    *
    * @see #isNotNull()
    */
   public UnaryOperatorFilter isNull();

   /**
    * Creates a filter matching an attribute not having a SQL <code>NULL</code> value.
    *
    * @return The readily configured filter.
    *
    * @see #isNull()
    */
   public UnaryOperatorFilter isNotNull();

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
   public BinaryOperatorFilter isEqual(String value);

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
   public BinaryOperatorFilter isEqual(long value);

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
   public BinaryOperatorFilter isEqual(double value);

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
   public BinaryOperatorFilter notEqual(String value);

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
   public BinaryOperatorFilter notEqual(long value);

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
   public BinaryOperatorFilter notEqual(double value);

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
   public BinaryOperatorFilter lessThan(String value);

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
   public BinaryOperatorFilter lessThan(long value);

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
   public BinaryOperatorFilter lessThan(double value);

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
   public BinaryOperatorFilter lessOrEqual(String value);

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
   public BinaryOperatorFilter lessOrEqual(long value);

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
   public BinaryOperatorFilter lessOrEqual(double value);

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
   public BinaryOperatorFilter greaterThan(String value);

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
   public BinaryOperatorFilter greaterThan(long value);

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
   public BinaryOperatorFilter greaterThan(double value);

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
   public BinaryOperatorFilter greaterOrEqual(String value);

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
   public BinaryOperatorFilter greaterOrEqual(long value);

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
   public BinaryOperatorFilter greaterOrEqual(double value);

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
   public BinaryOperatorFilter like(String value);

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
   public TernaryOperatorFilter between(String lowerBound, String upperBound);

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
   public TernaryOperatorFilter between(long lowerBound, long upperBound);

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
   public TernaryOperatorFilter between(double lowerBound, double upperBound);

   /**
    * Returns the name of the attribute to apply this filter to.
    *
    * @return The attribute name.
    */
   public String getAttributeName();

}
