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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.persistence.Operator;


// TODO: documentation
/**
 * 
 * 
 * @author born
 * @version $Revision$
 */
public class SubProcessDataFilter extends AbstractDataFilter
{
   private SubProcessDataFilter(String dataID, Operator.Binary operator, Serializable value,
         int filterMode)
   {
      super(dataID, null, operator, value, filterMode);
   }

   private SubProcessDataFilter(String dataID, Operator.Ternary operator,
         Serializable value1, Serializable value2, int filterMode)
   {
      super(dataID, null, operator, value1, value2, filterMode);
   }
   
   /**
    * Creates a filter matching workflow data being equal with the given
    * <code>value</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static SubProcessDataFilter isEqual(String dataID, Serializable value)
   {
      return new SubProcessDataFilter(dataID, Operator.IS_EQUAL, value, MODE_SUBPROCESSES);
   }

   /**
    * Creates a filter matching workflow data being not equal with the given
    * <code>value</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static SubProcessDataFilter notEqual(String dataID, Serializable value)
   {
      return new SubProcessDataFilter(dataID, Operator.NOT_EQUAL, value, MODE_SUBPROCESSES);
   }

   /**
    * Creates a filter matching workflow data being less than the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static SubProcessDataFilter lessThan(String dataID, Serializable value)
   {
      return new SubProcessDataFilter(dataID, Operator.LESS_THAN, value, MODE_SUBPROCESSES);
   }

   /**
    * Creates a filter matching workflow data being less than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static SubProcessDataFilter lessOrEqual(String dataID, Serializable value)
   {
      return new SubProcessDataFilter(dataID, Operator.LESS_OR_EQUAL, value, MODE_SUBPROCESSES);
   }

   /**
    * Creates a filter matching workflow data being greater than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static SubProcessDataFilter greaterOrEqual(String dataID, Serializable value)
   {
      return new SubProcessDataFilter(dataID, Operator.GREATER_OR_EQUAL, value, MODE_SUBPROCESSES);
   }

   /**
    * Creates a filter matching workflow data being greater than the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static SubProcessDataFilter greaterThan(String dataID, Serializable value)
   {
      return new SubProcessDataFilter(dataID, Operator.GREATER_THAN, value, MODE_SUBPROCESSES);
   }

   /**
    * Creates a filter matching text workflow data according to the pattern given by
    * <code>value</code>.
    * <p />
    * The pattern language syntax is that of SQL <code>LIKE</code> patterns. For details
    * please check the documentation of your database backend.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static SubProcessDataFilter like(String dataID, String value)
   {
      return new SubProcessDataFilter(dataID, Operator.LIKE, value, MODE_SUBPROCESSES);
   }

   /**
    * Creates a filter matching workflow data being equal one of the given
    * <code>values</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param values The list of values to match with.
    * @return The readily configured data filter.
    * @throws PublicException If not all elements of <code>values</code> are instances of
    *                         exactly the same Java class.
    */
   public static SubProcessDataFilter in(String dataID, Collection values)
   {
      if (values.isEmpty())
      {
         throw new PublicException("Empty value list for IN operator");
      }

      Set typeSet = new HashSet(values.size());
      for (Iterator i = values.iterator(); i.hasNext();)
      {
         typeSet.add(i.next().getClass());

         if (typeSet.size() > 1)
         {
            throw new PublicException("Value types are inhomogeneous: " + typeSet);
         }
      }

      return new SubProcessDataFilter(dataID, Operator.IN, new ArrayList(values),
            MODE_SUBPROCESSES);
   }

   /**
    * Creates a filter matching workflow data being both greater than or equal the given
    * <code>lowerBound</code> and less than or equal the given <code>upperBound</code>.
    * <p />
    * The meaning of being less than or greater than is specific to the type of the
    * workflow data, i.e. arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param lowerBound The lower bound of the value range to match with.
    * @param upperBound The upper bound of the  value range to match with.
    * @return The readily configured data filter.
    * @throws PublicException If <code>lowerBound</code> and <code>upperBound</code> are
    *                         not instances of exactly the same Java class.
    */
   public static SubProcessDataFilter between(String dataID, Serializable lowerBound,
         Serializable upperBound)
   {
      if (!lowerBound.getClass().equals(upperBound.getClass()))
      {
         throw new PublicException("Types of lower and upper bound are inhomogeneous: ["
               + "class " + lowerBound.getClass().getName()
               + ", class " + upperBound.getClass().getName() + "]");
      }

      return new SubProcessDataFilter(dataID, Operator.BETWEEN, lowerBound, upperBound,
            MODE_SUBPROCESSES);
   }

}