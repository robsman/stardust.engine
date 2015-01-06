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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.core.persistence.Operator;


/**
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public abstract class AbstractQueryPredicate<T> implements Predicate<T>
{
   private FilterTerm term;

   public AbstractQueryPredicate(Query query)
   {
      term = query.getFilter();
   }

   public boolean accept(T model)
   {
      return (Boolean) term.accept(evaluator, model);
   }

   public abstract Object getValue(T model, String attribute, Object expected);

   private final FilterEvaluationVisitor evaluator = createFilterEvaluationVisitor();

   protected FilterEvaluationVisitor createFilterEvaluationVisitor()
   {
      return new AbstractEvaluationVisitor() {};
   }

   public abstract class AbstractEvaluationVisitor implements FilterEvaluationVisitor
   {
      public Object visit(FilterTerm filter, Object context)
      {
         boolean and = isAndTerm(filter.getKind());

         Boolean result = null;
         List<FilterCriterion> parts = filter.getParts();
         for (FilterCriterion criterion : parts)
         {
            Boolean value = (Boolean) criterion.accept(this, context);
            if (result == null)
            {
               result = value;
            }
            else if (and)
            {
               result &= value;
               if (!result)
               {
                  break;
               }
            }
            else
            {
               result |= value;
               if (result)
               {
                  break;
               }
            }
         }
         // (fh) no parts means no restrictions
         return result == null ? Boolean.TRUE : result;
      }

      public Object visit(UnaryOperatorFilter filter, Object context)
      {
         Object value = getValue((T) context, filter.getAttribute(), null);
         return evaluate(filter.getOperator(), value);
      }

      public Object visit(BinaryOperatorFilter filter, Object context)
      {
         Object value = getValue((T) context, filter.getAttribute(), filter.getValue());
         return evaluate(filter.getOperator(), value, filter.getValue());
      }

      public Object visit(TernaryOperatorFilter filter, Object context)
      {
         Pair pair = filter.getValue();
         Object value = getValue((T) context, filter.getAttribute(), pair);
         return evaluate(filter.getOperator(), value, pair.getFirst(), pair.getSecond());
      }

      private Object evaluate(Operator operator, Object left)
      {
         if (Operator.IS_NULL == operator)
         {
            return left == null;
         }
         if (Operator.IS_NOT_NULL == operator)
         {
            return left != null;
         }
         throw new IllegalArgumentException("Unsupported operator: " + operator);
      }

      private Object evaluate(Operator operator, Object left, Object right)
      {
         if (Operator.IS_EQUAL == operator)
         {
            return CompareHelper.areEqual(left, right);
         }
         if (Operator.NOT_EQUAL == operator)
         {
            return !CompareHelper.areEqual(left, right);
         }
         if (Operator.LESS_THAN == operator)
         {
            return CompareHelper.compare(left, right) < 0;
         }
         if (Operator.LESS_OR_EQUAL == operator)
         {
            return CompareHelper.compare(left, right) <= 0;
         }
         if (Operator.GREATER_THAN == operator)
         {
            return CompareHelper.compare(left, right) > 0;
         }
         if (Operator.GREATER_OR_EQUAL == operator)
         {
            return CompareHelper.compare(left, right) >= 0;
         }
         if (Operator.LIKE == operator)
         {
            if (left == null || right == null)
            {
               return false;
            }
            return left.toString().toLowerCase().matches(convert(right.toString().toLowerCase()));
         }
         if (Operator.IN == operator)
         {
            return ((Collection) right).contains(left);
         }
         if (Operator.NOT_IN == operator)
         {
            return !((Collection) right).contains(left);
         }
         if (Operator.NOT_ANY_OF == operator)
         {
            Assert.lineNeverReached("TODO: Still to be implemented");
         }

         throw new IllegalArgumentException("Unsupported operator: " + operator);
      }

      /**
       * Converts an sql like style pattern to a java regex pattern.
       *
       * <ul>
       * <li>'%s' converts to '.*'</li>
       * <li>'_' converts to '.'</li>
       * <li>'[charlist]' remains '[charlist]'</li>
       * <li>'[^charlist]' remains '[^charlist]'</li>
       * <li>'[!charlist]' converts to '[^charlist]'</li>
       * <li>all regex control characters will be quoted depending on the context.</li>
       * </ul>
       *
       * @param sqlPattern the sql pattern
       * @return the regex pattern
       */
      private String convert(String sqlPattern)
      {
         StringBuilder builder = new StringBuilder();
         boolean inList = false;
         for (int i = 0, len = sqlPattern.length(); i < len; i++)
         {
            char c = sqlPattern.charAt(i);
            if (inList)
            {
               if ("\\^![-&|()".indexOf(c) >= 0)
               {
                  builder.append('\\');
               }
               builder.append(c);
               if (c == ']')
               {
                  inList = false;
               }
            }
            else
            {
               switch (c)
               {
               case '%':
                  builder.append(".*");
               case '_':
                  builder.append('.');
                  break;
               case '[':
                  inList = true;
                  if (i + 1 < len && (sqlPattern.charAt(i + 1) == '^' || sqlPattern.charAt(i + 1) == '!'))
                  {
                     builder.append("[^");
                     i++;
                  }
                  else
                  {
                     builder.append(c);
                  }
                  break;
               default:
                  if ("\\.*?+|()^$".indexOf(c) >= 0)
                  {
                     builder.append('\\');
                  }
                  builder.append(c);
               }
            }
         }
         return builder.toString();
      }

      private Object evaluate(Operator operator, Object left, Object right1, Object right2)
      {
         if (Operator.BETWEEN == operator)
         {
            return CompareHelper.compare(left, right1) >= 0 && CompareHelper.compare(right2, left) >= 0;
         }
         throw new IllegalArgumentException("Unsupported operator: " + operator);
      }

      private boolean isAndTerm(FilterTerm.Kind kind)
      {
         boolean and = FilterTerm.AND == kind;
         if (!and && !(FilterTerm.OR == kind))
         {
            throw new IllegalArgumentException("Unsupported aggregator: " + kind);
         }
         return and;
      }

      public Object visit(ProcessDefinitionFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(ProcessStateFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(ProcessInstanceFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(StartingUserFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(ActivityFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(ActivityInstanceFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(ActivityStateFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(PerformingUserFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(PerformingParticipantFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(PerformingOnBehalfOfFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(PerformedByUserFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(AbstractDataFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(ParticipantAssociationFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(CurrentPartitionFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(UserStateFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(ProcessInstanceLinkFilter filter,
            Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }

      public Object visit(DocumentFilter filter, Object context)
      {
         throw new IllegalArgumentException("Unsupported filter: " + filter.getClass());
      }
   };
}
