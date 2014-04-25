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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.query.BinaryOperatorFilter;
import org.eclipse.stardust.engine.api.query.ScopedFilter;
import org.eclipse.stardust.engine.api.query.TernaryOperatorFilter;

/**
 * Predicates is an abstract class which provides several factories for conveniently
 * creating <code>PredicateTerm</code>s.
 *
 * @author sborn
 * @version $Revision$
 */
public abstract class Predicates
{
   // TODO replace with something immutable
   public static final PredicateTerm TRUE = new AndTerm();

   public static AndTerm andTerm(PredicateTerm lhs, PredicateTerm rhs)
   {
      return new AndTerm(new PredicateTerm[] { lhs, rhs });
   }

   public static AndTerm andTerm(PredicateTerm pred1, PredicateTerm pred2,
         PredicateTerm pred3)
   {
      return new AndTerm(new PredicateTerm[] { pred1, pred2, pred3 });
   }

   public static AndTerm andTerm(PredicateTerm pred1, PredicateTerm pred2,
         PredicateTerm pred3, PredicateTerm pred4)
   {
      return new AndTerm(new PredicateTerm[] { pred1, pred2, pred3, pred4 });
   }

   public static AndTerm andTerm(PredicateTerm pred1, PredicateTerm pred2,
         PredicateTerm pred3, PredicateTerm pred4, PredicateTerm pred5)
   {
      return new AndTerm(new PredicateTerm[] { pred1, pred2, pred3, pred4, pred5 });
   }

   public static OrTerm orTerm(PredicateTerm lhs, PredicateTerm rhs)
   {
      return new OrTerm(new PredicateTerm[] { lhs, rhs });
   }

   /**
    * Creates a <code>IS NULL</code> comparison predicate for a alphanumeric attribute.
    *
    * @param field
    *           The attribute name.
    * @return The comparison predicate.
    */
   public static ComparisonTerm isNull(FieldRef field)
   {
      return new ComparisonTerm(field, Operator.IS_NULL);
   }

   /**
    * Creates a <code>IS NOT NULL</code> comparison predicate for a alphanumeric
    * attribute.
    *
    * @param field
    *           The attribute name.
    * @return The comparison predicate.
    */
   public static ComparisonTerm isNotNull(FieldRef field)
   {
      return new ComparisonTerm(field, Operator.IS_NOT_NULL);
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValue
    *           The numeric value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm isEqual(FieldRef field, long nValue)
   {
      return new ComparisonTerm(field, Operator.IS_EQUAL, new Long(nValue));
   }

   /**
    * Creates a comparison predicate for an alphanumeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param sValue
    *           The alphanumeric value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm isEqual(FieldRef field, String sValue)
   {
      return new ComparisonTerm(field, Operator.IS_EQUAL, sValue);
   }

   /**
    * Creates a comparison predicate for comparison with subqueries
    *
    * @param field
    *           The attribute name
    * @param sQuery
    *           The subquery
    * @return The comparison predicate
    */
   public static ComparisonTerm isEqual(FieldRef field, QueryDescriptor subQuery)
   {
      return new ComparisonTerm(field, Operator.IS_EQUAL, subQuery);
   }

   /**
    * Creates a comparison predicate for comparison with other field.
    *
    * @param lhsField
    *           The left hand side attribute name
    * @param rhsField
    *           The right hand side attribute name
    * @return The comparison predicate
    */
   public static ComparisonTerm isEqual(FieldRef lhsField, FieldRef rhsField)
   {
      return new ComparisonTerm(lhsField, Operator.IS_EQUAL, rhsField);
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValue
    *           The numeric value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm notEqual(FieldRef field, long nValue)
   {
      return new ComparisonTerm(field, Operator.NOT_EQUAL, new Long(nValue));
   }

   /**
    * Creates a comparison predicate for a alphanumeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param sValue
    *           The alphanumeric value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm notEqual(FieldRef field, String sValue)
   {
      return new ComparisonTerm(field, Operator.NOT_EQUAL, sValue);
   }

   /**
    * Creates a comparison predicate for comparison with other field.
    *
    * @param lhsField
    *           The left hand side attribute name
    * @param rhsField
    *           The right hand side attribute name
    * @return The comparison predicate
    */
   public static ComparisonTerm notEqual(FieldRef lhsField, FieldRef rhsField)
   {
      return new ComparisonTerm(lhsField, Operator.NOT_EQUAL, rhsField);
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValue
    *           The numeric value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm lessThan(FieldRef field, long nValue)
   {
      return new ComparisonTerm(field, Operator.LESS_THAN, new Long(nValue));
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValue
    *           The numeric value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm lessOrEqual(FieldRef field, long nValue)
   {
      return new ComparisonTerm(field, Operator.LESS_OR_EQUAL, new Long(nValue));
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValue
    *           The numeric value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm greaterOrEqual(FieldRef field, long nValue)
   {
      return new ComparisonTerm(field, Operator.GREATER_OR_EQUAL, new Long(nValue));
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValue
    *           The numeric value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm greaterThan(FieldRef field, long nValue)
   {
      return new ComparisonTerm(field, Operator.GREATER_THAN, new Long(nValue));
   }

   /**
    * Creates a comparison predicate for a alphanumeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param pattern
    *           The alphanumeric pattern.
    * @return The comparison predicate.
    */
   public static ComparisonTerm isLike(FieldRef field, String pattern)
   {
      return new ComparisonTerm(field, Operator.LIKE, pattern);
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValues
    *           The list of numeric values.
    * @return The comparison predicate.
    */
   public static ComparisonTerm inList(FieldRef field, int[] nValues)
   {
      List values = new ArrayList(nValues.length);
      for (int i = 0; i < nValues.length; i++)
      {
         values.add(new Long(nValues[i]));
      }


      return new ComparisonTerm(field, Operator.IN, values);
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValues
    *           The list of numeric values.
    * @return The comparison predicate.
    */
   public static ComparisonTerm inList(FieldRef field, long[] nValues)
   {
      List values = new ArrayList(nValues.length);
      for (int i = 0; i < nValues.length; i++)
      {
         values.add(new Long(nValues[i]));
      }

      return new ComparisonTerm(field, Operator.IN, values);
   }

   /**
    * Creates a comparison predicate for a alphanumeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param sValues
    *           The list of alphanumeric values.
    * @return The comparison predicate.
    */
   public static ComparisonTerm inList(FieldRef field, String[] sValues)
   {
      List values = new ArrayList(sValues.length);
      for (int i = 0; i < sValues.length; i++)
      {
         values.add(sValues[i]);
      }

      return new ComparisonTerm(field, Operator.IN, values);
   }

   /**
    * Creates a comparison predicate for a numeric or alphanumeric attributes attribute.
    *
    * @param field
    *           The attribute name.
    * @param values
    *           The list of values.
    * @return The comparison predicate.
    */
   public static ComparisonTerm inList(FieldRef field, List values)
   {
      return new ComparisonTerm(field, Operator.IN, new ArrayList(values));
   }

   /**
    * Creates a comparison predicate for a numeric or alphanumeric attributes attribute.
    *
    * @param field
    *           The attribute name.
    * @param valuesIter
    *           Iterator which is used to build the actual values list.
    * @return The comparison predicate.
    */
   public static ComparisonTerm inList(FieldRef field, Iterator valuesIter)
   {
      List list = new ArrayList();
      while (valuesIter.hasNext())
      {
         list.add(valuesIter.next());
      }

      return inList(field, list);
   }

   /**
    * Creates a comparison predicate for a numeric attribute.
    *
    * @param field
    *           The attribute name.
    * @param nValues
    *           The list of numeric values.
    * @return The comparison predicate.
    */
   public static ComparisonTerm notInList(FieldRef field, int[] nValues)
   {
      List values = new ArrayList(nValues.length);
      for (int i = 0; i < nValues.length; i++)
      {
         values.add(new Long(nValues[i]));
      }

      return new ComparisonTerm(field, Operator.NOT_IN, values);
   }

   /**
    * Creates a comparison predicate for a numeric or alphanumeric attributes attribute.
    *
    * @param field
    *           The attribute name.
    * @param values
    *           The list of values.
    * @return The comparison predicate.
    */
   public static ComparisonTerm notInList(FieldRef field, List values)
   {
      return new ComparisonTerm(field, Operator.NOT_IN, new ArrayList(values));
   }

   /**
    * Creates a comparison predicate for a numeric or alphanumeric attributes attribute.
    *
    * @param field
    *           The attribute name.
    * @param valuesIter
    *           Iterator which is used to build the actual values list.
    * @return The comparison predicate.
    */
   public static ComparisonTerm notInList(FieldRef field, Iterator valuesIter)
   {
      List list = new ArrayList();
      while (valuesIter.hasNext())
      {
         list.add(valuesIter.next());
      }

      return notInList(field, list);
   }

   /**
    * Creates a comparison predicate for a numeric or alphanumeric attributes attribute.
    *
    * @param field
    *           The attribute name.
    * @param values
    *           The list of values.
    * @return The comparison predicate.
    */
   public static ComparisonTerm inList(FieldRef field, QueryDescriptor subQuery)
   {
      return new ComparisonTerm(field, Operator.IN, subQuery);
   }

   /**
   /**
    * Creates a comparison predicate for a numeric or alphanumeric attributes attribute.
    *
    * @param attribute
    *           The attribute name.
    * @param values
    *           The list of values.
    * @return The comparison predicate.
    */
   public static ComparisonTerm notInList(FieldRef attribute, QueryDescriptor subQuery)
   {
      return new ComparisonTerm(attribute, Operator.NOT_IN, subQuery);
   }

   /**
    * Creates a comparison predicate for a numeric attributes attribute.
    *
    * @param field
    *           The attribute name.
    * @param nLowerBound
    *           The lower bound value.
    * @param nUpperBound
    *           The upper bound value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm between(FieldRef field, long nLowerBound, long nUpperBound)
   {
      return new ComparisonTerm(field, Operator.BETWEEN, new Pair(new Long(nLowerBound),
            new Long(nUpperBound)));
   }

   /**
    * Creates a comparison predicate for a numeric or alphanumeric attributes attribute.
    *
    * @param field
    *           The attribute name.
    * @param sLowerBound
    *           The lower bound value.
    * @param sUpperBound
    *           The upper bound value.
    * @return The comparison predicate.
    */
   public static ComparisonTerm between(FieldRef field, String sLowerBound, String sUpperBound)
   {
      return new ComparisonTerm(field, Operator.BETWEEN, new Pair(sLowerBound,
            sUpperBound));
   }

   /**
    * Creates a comparsion predicate for a alphanumeric attributes attribute based on a
    * generic compare operator
    *
    * @param field
    *          The attribute name of type {@link FieldRef}
    * @param filter
    *          The compare filter of type {@link ScopedFilter}
    * @return
    *          The comparsion predicate of type {@link ComparisonTerm}.
    *          Will be <code>null</code> if filter type is invalid
    */
   public static ComparisonTerm genericComparison(FieldRef field, ScopedFilter filter)
   {
      if (filter instanceof BinaryOperatorFilter)
      {
         ComparisonTerm activityCriticalityTerm = new ComparisonTerm(
               field,
               ((BinaryOperatorFilter) filter).getOperator(),
               ((BinaryOperatorFilter) filter).getValue());
         return activityCriticalityTerm;
      }
      else if (filter instanceof TernaryOperatorFilter)
      {
         ComparisonTerm activityCriticalityTerm = new ComparisonTerm(
               field,
               ((TernaryOperatorFilter) filter).getOperator(),
               ((TernaryOperatorFilter) filter).getValue());
         return activityCriticalityTerm;

      }
      return null;
   }

   private Predicates()
   {
   // utility class
   }
}
