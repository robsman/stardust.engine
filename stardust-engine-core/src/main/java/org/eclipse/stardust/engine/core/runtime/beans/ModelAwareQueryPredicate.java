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

import java.util.List;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.core.persistence.Operator.Binary;


public class ModelAwareQueryPredicate<T> extends AbstractQueryPredicate<T>
{
   public static final String INTERNAL_MODEL_OID_ATTRIBUTE = "modelOid";

   private Long modelOid;

   public ModelAwareQueryPredicate(Query query)
   {
      super(query);

      modelOid = (Long) query.getFilter().accept(new ModelOidVisitor(), null);
   }

   public Object getValue(T t, String attribute, Object expected)
   {
      if (INTERNAL_MODEL_OID_ATTRIBUTE.equals(attribute))
      {
         // Not filtered by modelOid, returning the expected value does not exclude it
         // from the results.
         return expected;
      }
      throw new IllegalArgumentException("Unsupported attribute: "
            + attribute);
   }

   public Long getModelOid()
   {
      return modelOid;
   }

   public IModel getModel()
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      return modelManager.findModel(modelOid);
   }

   private class ModelOidVisitor implements FilterEvaluationVisitor
   {

      public Object visit(FilterTerm filter, Object context)
      {
         Long result = null;
         List<FilterCriterion> parts = filter.getParts();
         for (FilterCriterion criterion : parts)
         {
            Long value = (Long) criterion.accept(this, context);
            if (value != null)
            {
               result = value;
            }
         }
         // no parts means no restrictions
         return result;
      }

      public Object visit(UnaryOperatorFilter filter, Object context)
      {
         return null;
      }

      public Object visit(BinaryOperatorFilter filter, Object context)
      {
         if (Binary.IS_EQUAL.equals(filter.getOperator())
               && INTERNAL_MODEL_OID_ATTRIBUTE.equals(filter.getAttribute()))
         {
            return filter.getValue();
         }
         return null;
      }

      public Object visit(TernaryOperatorFilter filter, Object context)
      {
         return null;
      }

      public Object visit(RootProcessInstanceFilter filter, Object context)
      {
         return null;
      }
      
      public Object visit(ProcessDefinitionFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceFilter filter, Object context)
      {
         return null;
      }

      public Object visit(StartingUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityInstanceFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingParticipantFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingOnBehalfOfFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformedByUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(AbstractDataFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ParticipantAssociationFilter filter, Object context)
      {
         return null;
      }

      public Object visit(CurrentPartitionFilter filter, Object context)
      {
         return null;
      }

      public Object visit(UserStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceLinkFilter filter,
            Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
      {
         return null;
      }

      public Object visit(DocumentFilter filter, Object context)
      {
         return null;
      }
      
      public Object visit(DescriptorFilter filter, Object context)
      {
         return null;
      }

   }
}
