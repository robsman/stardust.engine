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
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.EvaluationOption;
import org.eclipse.stardust.engine.core.persistence.IEvaluationOptionProvider;
import org.eclipse.stardust.engine.core.persistence.Operator;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractDataFilter
      implements FilterCriterion, IEvaluationOptionProvider
{
   public static final int MODE_ALL_FROM_SCOPE = 1;
   public static final int MODE_SUBPROCESSES = 2;
   public static final int MODE_ALL_FROM_HIERARCHY = 3;

   private final Operator operator;
   private final String dataID;
   private final String attributeName;
   private final Serializable operand;

   private final int filterMode;

   private Map options;

   protected AbstractDataFilter(String dataID, String attributeName, Operator.Binary operator, Serializable value,
         int filterMode)
   {
      this.operator = operator;
      this.dataID = dataID;
      this.attributeName = attributeName;
      this.operand = value;
      this.filterMode = filterMode;
   }

   protected AbstractDataFilter(String dataID, String attributeName, Operator.Ternary operator,
         Serializable value1, Serializable value2, int filterMode)
   {
      this.operator = operator;
      this.dataID = dataID;
      this.attributeName = attributeName;
      this.operand = new Pair(value1, value2);
      this.filterMode = filterMode;
   }

   public Operator getOperator()
   {
      return operator;
   }

   /**
    * Returns the single- or list-valued operand for unary operator filters, or a
    * {@link org.eclipse.stardust.common.Pair Pair} of operands for a binary operator filter.
    *
    * @return The operand.
    * @see #getOperator
    */
   public Serializable getOperand()
   {
      return operand;
   }

   /**
    * Returns the ID of the workflow data this filter is applying to.
    * @return
    */
   public String getDataID()
   {
      return dataID;
   }

   /**
    * Returns the name of the data attribute this filter is applying to.
    * @return
    */
   public String getAttributeName()
   {
      return attributeName;
   }

   /**
    * Returns the name of the data attribute or an empty string if the name is null.
    * @return
    */
   public String getNormalizedAttributeName()
   {
      return attributeName == null ? "" : attributeName;
   }

   public int getFilterMode()
   {
      return filterMode;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
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

   /**
    * Returns a string representation of the filter definition.
    */
   public String toString()
   {
      String attributeString = this.getAttributeName() == null ? "" : "("+this.getAttributeName()+")";

      if (getOperator().isUnary())
      {
         return "data['" + getDataID() + "'"+attributeString+"] " + getOperator();
      }
      else if (getOperator().isTernary())
      {
         Operator.Ternary ternaryOperator = (Operator.Ternary) getOperator();
         Pair pair = (Pair) operand;

         return "data['" + getDataID() + "'"+attributeString+"] " + ternaryOperator + " " + pair.getFirst()
               + " " + ternaryOperator.getSecondOperator() + " " + pair.getSecond();
      }
      else
      {
         return "data['" + getDataID() + "'"+attributeString+"] " + getOperator() + " " + operand;
      }
   }

   @Override
   public int hashCode()
   {
      int h = 7 * getOperator().getId().hashCode() + 11 * getDataID().hashCode() + 13 * filterMode;
      if(getAttributeName() != null)
      {
         h = h + 17 * getAttributeName().hashCode();
      }
      if(options != null && options.size() > 0)
      {
         h = h + 19 * options.size() + 23 * options.entrySet().iterator().next().hashCode();
      }
      return h;
   }
}
