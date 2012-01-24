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
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.persistence.EvaluationOption;
import org.eclipse.stardust.engine.core.persistence.Operator;


public class DataFilter extends AbstractDataFilter
{
   private static final long serialVersionUID = -489237070443309695L;

   private DataFilter(String dataID, String attributeName, Operator.Binary operator, Serializable value,
         int filterMode)
   {
      super(dataID, attributeName, operator, value, filterMode);
   }

   private DataFilter(String dataID, String attributeName, Operator.Ternary operator,
         Serializable value1, Serializable value2, int filterMode)
   {
      super(dataID, attributeName, operator, value1, value2, filterMode);
   }

   /**
    * Creates a filter matching workflow data being equal with the given
    * <code>value</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter isEqual(String dataID, Serializable value)
   {
      return new DataFilter(dataID, null, Operator.IS_EQUAL, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being equal with the given
    * <code>value</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The string value to match with.
    * @param caseSensitive Indicates if the search should be case sensitive or not.
    * @return The readily configured data filter.
    */
   public static DataFilter isEqual(String dataID, String value, boolean caseSensitive)
   {
      DataFilter filter = new DataFilter(dataID, null, Operator.IS_EQUAL, value, MODE_ALL_FROM_SCOPE);

      filter.setOption(EvaluationOption.CASE_SENSITIVE, Boolean.valueOf(caseSensitive));

      return filter;
   }

   /**
    * Creates a filter matching workflow data being equal with the given
    * <code>value</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter isEqual(String dataID, String attributeName, Serializable value)
   {
      return new DataFilter(dataID, attributeName, Operator.IS_EQUAL, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being equal with the given
    * <code>value</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The string value to match with.
    * @param caseSensitive Indicates if the search should be case sensitive or not.
    * @return The readily configured data filter.
    */
   public static DataFilter isEqual(String dataID, String attributeName,
         String value, boolean caseSensitive)
   {
      DataFilter filter = new DataFilter(dataID, attributeName, Operator.IS_EQUAL, value, MODE_ALL_FROM_SCOPE);

      filter.setOption(EvaluationOption.CASE_SENSITIVE, Boolean.valueOf(caseSensitive));

      return filter;
   }

   /**
    * Creates a filter matching workflow data being not equal with the given
    * <code>value</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter notEqual(String dataID, Serializable value)
   {
      return new DataFilter(dataID, null, Operator.NOT_EQUAL, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being not equal with the given
    * <code>value</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter notEqual(String dataID, String attributeName, Serializable value)
   {
      return new DataFilter(dataID, attributeName, Operator.NOT_EQUAL, value, MODE_ALL_FROM_SCOPE);
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
   public static DataFilter lessThan(String dataID, Serializable value)
   {
      return new DataFilter(dataID, null, Operator.LESS_THAN, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being less than the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter lessThan(String dataID, String attributeName, Serializable value)
   {
      return new DataFilter(dataID, attributeName, Operator.LESS_THAN, value, MODE_ALL_FROM_SCOPE);
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
   public static DataFilter lessOrEqual(String dataID, Serializable value)
   {
      return new DataFilter(dataID, null, Operator.LESS_OR_EQUAL, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being less than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being less than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter lessOrEqual(String dataID, String attributeName, Serializable value)
   {
      return new DataFilter(dataID, attributeName, Operator.LESS_OR_EQUAL, value, MODE_ALL_FROM_SCOPE);
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
   public static DataFilter greaterOrEqual(String dataID, Serializable value)
   {
      return new DataFilter(dataID, null, Operator.GREATER_OR_EQUAL, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being greater than or equal the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter greaterOrEqual(String dataID, String attributeName, Serializable value)
   {
      return new DataFilter(dataID, attributeName, Operator.GREATER_OR_EQUAL, value, MODE_ALL_FROM_SCOPE);
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
   public static DataFilter greaterThan(String dataID, Serializable value)
   {
      return new DataFilter(dataID, null, Operator.GREATER_THAN, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching workflow data being greater than the given
    * <code>value</code>.
    * <p />
    * The meaning of being greater than is specific to the type of the workflow data, i.e.
    * arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter greaterThan(String dataID, String attributeName, Serializable value)
   {
      return new DataFilter(dataID, attributeName, Operator.GREATER_THAN, value, MODE_ALL_FROM_SCOPE);
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
   public static DataFilter like(String dataID, String value)
   {
      return new DataFilter(dataID, null, Operator.LIKE, value, MODE_ALL_FROM_SCOPE);
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
    * @param caseSensitive Indicates if the search should be case sensitive or not.
    * @return The readily configured data filter.
    */
   public static DataFilter like(String dataID, String value, boolean caseSensitive)
   {
      DataFilter filter = new DataFilter(dataID, null, Operator.LIKE, value, MODE_ALL_FROM_SCOPE);

      filter.setOption(EvaluationOption.CASE_SENSITIVE, Boolean.valueOf(caseSensitive));

      return filter;
   }

   /**
    * Creates a filter matching text workflow data according to the pattern given by
    * <code>value</code>.
    * <p />
    * The pattern language syntax is that of SQL <code>LIKE</code> patterns. For details
    * please check the documentation of your database backend.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The value to match with.
    * @return The readily configured data filter.
    */
   public static DataFilter like(String dataID, String attributeName, String value)
   {
      return new DataFilter(dataID, attributeName, Operator.LIKE, value, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching text workflow data according to the pattern given by
    * <code>value</code>.
    * <p />
    * The pattern language syntax is that of SQL <code>LIKE</code> patterns. For details
    * please check the documentation of your database backend.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param value The value to match with.
    * @param caseSensitive Indicates if the search should be case sensitive or not.
    * @return The readily configured data filter.
    */
   public static DataFilter like(String dataID, String attributeName, String value,
         boolean caseSensitive)
   {
      DataFilter filter = new DataFilter(dataID, attributeName, Operator.LIKE, value, MODE_ALL_FROM_SCOPE);

      filter.setOption(EvaluationOption.CASE_SENSITIVE, Boolean.valueOf(caseSensitive));

      return filter;
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
   public static DataFilter in(String dataID, Collection values)
   {
      return in(dataID, null, values);
   }

   /**
    * Creates a filter matching workflow data being equal one of the given
    * <code>values</code>.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param values The list of values to match with.
    * @return The readily configured data filter.
    * @throws PublicException If not all elements of <code>values</code> are instances of
    *                         exactly the same Java class.
    */
   public static DataFilter in(String dataID, String attributeName, Collection values)
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

      return new DataFilter(dataID, attributeName, Operator.IN, new ArrayList(values),
            MODE_ALL_FROM_SCOPE);
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
   public static DataFilter between(String dataID, Serializable lowerBound,
         Serializable upperBound)
   {
      return between(dataID, null, lowerBound, upperBound);
   }

   /**
    * Creates a filter matching workflow data being both greater than or equal the given
    * <code>lowerBound</code> and less than or equal the given <code>upperBound</code>.
    * <p />
    * The meaning of being less than or greater than is specific to the type of the
    * workflow data, i.e. arithmetic or lexical order.
    *
    * @param dataID The ID of the workflow data to be matched against.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param lowerBound The lower bound of the value range to match with.
    * @param upperBound The upper bound of the  value range to match with.
    * @return The readily configured data filter.
    * @throws PublicException If <code>lowerBound</code> and <code>upperBound</code> are
    *                         not instances of exactly the same Java class.
    */
   public static DataFilter between(String dataID, String attributeName, Serializable lowerBound,
         Serializable upperBound)
   {
      if (!lowerBound.getClass().equals(upperBound.getClass()))
      {
         throw new PublicException("Types of lower and upper bound are inhomogeneous: ["
               + "class " + lowerBound.getClass().getName()
               + ", class " + upperBound.getClass().getName() + "]");
      }

      return new DataFilter(dataID, attributeName, Operator.BETWEEN, lowerBound, upperBound,
            MODE_ALL_FROM_SCOPE);
   }

   /**
    * Creates a filter matching case descriptor being equal with the given value.
    *
    * @param id descriptor id.
    * @param value the required descriptor value.
    * @return The configured data filter.
    */
   public static DataFilter equalsCaseDescriptor(String id, Object value)
   {
      if (PredefinedConstants.CASE_NAME_ELEMENT.equals(id))
      {
         return isEqual(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
               PredefinedConstants.CASE_NAME_ELEMENT, value.toString());
      }
      else if (PredefinedConstants.CASE_DESCRIPTION_ELEMENT.equals(id))
      {
         return isEqual(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
               PredefinedConstants.CASE_DESCRIPTION_ELEMENT, value.toString());
      }
      else
      {
         return isEqual(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
               PredefinedConstants.CASE_DESCRIPTOR_VALUE_XPATH, '{' + id + '}' + value.toString());
      }
   }

   /**
    * Creates a filter matching case descriptor using the like function with the given value.
    *
    * @param id descriptor id.
    * @param value the required descriptor value.
    * @return The configured data filter.
    */
   public static DataFilter likeCaseDescriptor(String id, Object value)
   {
      if (PredefinedConstants.CASE_NAME_ELEMENT.equals(id))
      {
         return like(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
               PredefinedConstants.CASE_NAME_ELEMENT, value.toString());
      }
      else if (PredefinedConstants.CASE_DESCRIPTION_ELEMENT.equals(id))
      {
         return like(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
               PredefinedConstants.CASE_DESCRIPTION_ELEMENT, value.toString());
      }
      else
      {
         return like(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
               PredefinedConstants.CASE_DESCRIPTOR_VALUE_XPATH, '{' + id + '}' + value.toString());
      }

   }

   @Override
   public int hashCode()
   {
      String dataID = getDataID();
      String attributeName = getAttributeName();
      final int prime = 31;
      int result = 1;
      result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
      result = prime * result + ((dataID == null) ? 0 : dataID.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      DataFilter other = (DataFilter) obj;
      String dataID = getDataID();
      String attributeName = getAttributeName();
      if (attributeName == null)
      {
         if (other.getAttributeName() != null)
         {
            return false;
         }
      }
      else if (!attributeName.equals(other.getAttributeName()))
      {
         return false;
      }
      if (dataID == null)
      {
         if (other.getDataID() != null)
         {
            return false;
         }
      }
      else if (!dataID.equals(other.getDataID()))
      {
         return false;
      }
      if (PredefinedConstants.QUALIFIED_CASE_DATA_ID.equals(dataID) &&
          PredefinedConstants.CASE_DESCRIPTOR_VALUE_XPATH.equals(attributeName))
      {
         Object value = getOperand();
         if (value == null)
         {
            if (other.getOperand() != null)
            {
               return false;
            }
         }
         else if (!value.equals(other.getOperand()))
         {
            return false;
         }
      }
      return true;
   }

}
