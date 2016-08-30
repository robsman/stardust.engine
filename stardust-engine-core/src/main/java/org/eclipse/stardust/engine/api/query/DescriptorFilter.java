/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antje.Fuhrmann (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.EvaluationOption;
import org.eclipse.stardust.engine.core.persistence.IEvaluationOptionProvider;
import org.eclipse.stardust.engine.core.persistence.Operator;
import org.eclipse.stardust.engine.core.persistence.Operator.Binary;
import org.eclipse.stardust.engine.core.persistence.Operator.Ternary;

public class DescriptorFilter implements FilterCriterion, IEvaluationOptionProvider
{
   private static final long serialVersionUID = -5459208191987978472L;

   public static final int MODE_ALL_FROM_SCOPE = 1;

   public static final int MODE_SUBPROCESSES = 2;

   public static final int MODE_ALL_FROM_HIERARCHY = 3;

   private final Operator operator;

   private final String descriptorID;

   private final Serializable operand;

   private final int filterMode;

   private Map options;

   private final boolean caseDescriptor;

   private DescriptorFilter(String descriptorID, Ternary operator, Serializable value1,
         Serializable value2, int filterMode)
   {
      this.operator = operator;
      this.filterMode = filterMode;
      this.operand = new Pair(value1, value2);
      this.descriptorID = descriptorID;
      this.caseDescriptor = false;
   }

   private DescriptorFilter(String descriptorID, Binary operator, Serializable value,
         int filterMode)
   {
      this.operator = operator;
      this.filterMode = filterMode;
      this.operand = value;
      this.descriptorID = descriptorID;
      this.caseDescriptor = false;
   }

   private DescriptorFilter(String descriptorID, Binary operator, boolean caseDescriptor, Serializable value,
         int filterMode)
   {
      this.operator = operator;
      this.filterMode = filterMode;
      this.operand = value;
      this.descriptorID = descriptorID;
      this.caseDescriptor = caseDescriptor;
   }

   /**
    * Creates a filter matching workflow data being both greater than or equal the given
    * <code>lowerBound</code> and less than or equal the given <code>upperBound</code>.
    * <p />
    * The meaning of being less than or greater than is specific to the type of the
    * workflow data, i.e. arithmetic or lexical order.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param lowerBound
    *           The lower bound of the value range to match with.
    * @param upperBound
    *           The upper bound of the value range to match with.
    * @return The readily configured descriptor filter.
    * @throws PublicException
    *            If <code>lowerBound</code> and <code>upperBound</code> are not instances
    *            of exactly the same Java class.
    */
   public static DescriptorFilter between(String descriptorID, Serializable lowerBound,
         Serializable upperBound)
   {
      if (!lowerBound.getClass().equals(upperBound.getClass()))
      {
         throw new PublicException(
               BpmRuntimeError.QUERY_TYPES_OF_LOWER_AND_UPPER_BOUND_ARE_INHOMOGENEOUS
                     .raise(lowerBound.getClass().getName(), upperBound.getClass()
                           .getName()));
      }
      return new DescriptorFilter(descriptorID, Operator.BETWEEN, lowerBound, upperBound,
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being greater than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The value to match with.
    * @return The readily configured descriptor filter.
    */
   public static DescriptorFilter greaterOrEqual(String descriptorID, Serializable value)
   {
      return new DescriptorFilter(descriptorID, Operator.GREATER_OR_EQUAL, value,
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being greater than the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The value to match with.
    * @return The readily configured descriptor filter.
    */
   public static DescriptorFilter greaterThan(String descriptorID, Serializable value)
   {
      return new DescriptorFilter(descriptorID, Operator.GREATER_THAN, value,
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being equal one of the given
    * <code>values</code>.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param values
    *           The list of values to match with.
    * @return The readily configured descriptor filter.
    * @throws PublicException
    *            If not all elements of <code>values</code> are instances of exactly the
    *            same Java class.
    */
   public static DescriptorFilter in(String descriptorID, Collection values)
   {
      checkCollectionValues(values, Operator.IN);
      return new DescriptorFilter(descriptorID, Operator.IN, new ArrayList(values),
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being equal with the given
    * <code>value</code>.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The string value to match with.
    * @param caseSensitive
    *           Indicates if the search should be case sensitive or not.
    * @return The readily configured descriptor filter.
    */
   public static DescriptorFilter isEqual(String descriptorID, String value,
         boolean caseSensitive)
   {
      DescriptorFilter descriptorFilter = new DescriptorFilter(descriptorID,
            Operator.IS_EQUAL, value, MODE_ALL_FROM_SCOPE);
      descriptorFilter.setOption(EvaluationOption.CASE_SENSITIVE,
            Boolean.valueOf(caseSensitive));
      return descriptorFilter;
   }

   /**
    * Creates a filter matching workflow data being equal with the given
    * <code>value</code>.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The value to match with.
    * @return The readily configured descriptor filter.
    */
   public static DescriptorFilter isEqual(String descriptorID, Serializable value)
   {
      return new DescriptorFilter(descriptorID, Operator.IS_EQUAL, value,
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being less than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The value to match with.
    * @return The readily configured descriptor filter.
    */
   public static DescriptorFilter lessOrEqual(String descriptorID, Serializable value)
   {
      return new DescriptorFilter(descriptorID, Operator.LESS_OR_EQUAL, value,
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being less than the given <code>value</code>
    * .
    * <p />
    * The meaning of being less than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The value to match with.
    * @return The readily configured descriptor filter.
    */
   public static DescriptorFilter lessThan(String descriptorID, Serializable value)
   {
      return new DescriptorFilter(descriptorID, Operator.LESS_THAN, value,
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching text workflow data according to the pattern given by
    * <code>value</code>.
    * <p />
    * The pattern language syntax is that of SQL <code>LIKE</code> patterns. For details
    * please check the documentation of your database backend.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The value to match with.
    * @return The readily configured data filter.
    */
   public static DescriptorFilter like(String descriptorID, String value)
   {
      return new DescriptorFilter(descriptorID, Operator.LIKE, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching text workflow data according to the pattern given by
    * <code>value</code>.
    * <p />
    * The pattern language syntax is that of SQL <code>LIKE</code> patterns. For details
    * please check the documentation of your database backend.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The value to match with.
    * @param caseSensitive
    *           Indicates if the search should be case sensitive or not.
    * @return The readily configured data filter.
    */
   public static DescriptorFilter like(String descriptorID, String value,
         boolean caseSensitive)
   {
      DescriptorFilter descriptorFilter = new DescriptorFilter(descriptorID,
            Operator.LIKE, value, MODE_ALL_FROM_SCOPE);
      descriptorFilter.setOption(EvaluationOption.CASE_SENSITIVE,
            Boolean.valueOf(caseSensitive));
      return descriptorFilter;
   }

   /**
    * Creates a filter matching workflow data being not equal with the given
    * <code>value</code>.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param value
    *           The value to match with.
    * @return The readily configured data filter.
    */
   public static DescriptorFilter notEqual(String descriptorID, Serializable value)
   {
      return new DescriptorFilter(descriptorID, Operator.NOT_EQUAL, value,
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being not equal one of the given
    * <code>values</code>.
    *
    * @param descriptorID
    *           The ID of the descriptor to search for.
    * @param values
    *           The list of values to not match with.
    * @return The readily configured data filter.
    * @throws PublicException
    *            If not all elements of <code>values</code> are instances of exactly the
    *            same Java class.
    */
   public static DescriptorFilter notIn(String descriptorID, Collection values)
   {
      checkCollectionValues(values, Operator.NOT_IN);

      return new DescriptorFilter(descriptorID, Operator.NOT_IN, new ArrayList(values),
            MODE_ALL_FROM_SCOPE);
   }
   
   /**
    * Creates a filter matching case descriptor being equal with the given value.
    *
    * @param id
    *           descriptor id.
    * @param value
    *           the required descriptor value.
    * @return The configured descriptor filter.
    */
   public static DescriptorFilter equalsCaseDescriptor(String descriptorID,
         Serializable value)
   {
      return new DescriptorFilter(descriptorID, Operator.IS_EQUAL, true, value,
            MODE_ALL_FROM_SCOPE);
   }
   
   /**
    * Creates a filter matching case descriptor using the like function with the given
    * value.
    *
    * @param id
    *           descriptor id.
    * @param value
    *           the required descriptor value.
    * @return The configured descriptor filter.
    */
   public static DescriptorFilter likeCaseDescriptor(String descriptorID,
         Serializable value)
   {
      return new DescriptorFilter(descriptorID, Operator.LIKE, true, value,
            MODE_ALL_FROM_SCOPE);
   }

   public String getDescriptorID()
   {
      return descriptorID;
   }

   public int getFilterMode()
   {
      return filterMode;
   }

   public Serializable getOperand()
   {
      return operand;
   }

   public Operator getOperator()
   {
      return operator;
   }

   public Serializable getOption(EvaluationOption option)
   {
      return (null != options) ? (Serializable) options.get(option) : null;
   }

   protected Serializable setOption(EvaluationOption option, Serializable value)
   {
      if (options == null)
      {
         options = CollectionUtils.newMap();
      }

      return (Serializable) options.put(option, value);
   }
   
   public boolean isCaseDescriptor()
   {
      return caseDescriptor;
   }

   protected static void checkCollectionValues(Collection values, Operator operator)
   {
      if (values.isEmpty())
      {
         throw new PublicException(
               BpmRuntimeError.QUERY_DATA_FILTER_EMPTY_VALUE_LIST_FOR_XXX_OPERATOR
                     .raise(operator));
      }

      Set typeSet = new HashSet(values.size());
      for (Iterator i = values.iterator(); i.hasNext();)
      {
         typeSet.add(i.next().getClass());

         if (typeSet.size() > 1)
         {
            throw new PublicException(
                  BpmRuntimeError.QUERY_DATA_FILTER_VALUE_TYPES_ARE_INHOMOGENEOUS
                        .raise(typeSet));
         }
      }
   }

   @Override
   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
}
