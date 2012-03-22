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

import java.util.Iterator;

import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;


/**
 * Scans a filter criteria tree for DataFilter instances involving BIG data.
 *
 * @author rsauer
 * @version $Revision$
 */
public class BigDataFilterFinder implements FilterEvaluationVisitor
{
   public Object visit(FilterTerm filter, Object context)
   {
      boolean foundBigData = false;

      for (Iterator i = filter.getParts().iterator(); i.hasNext();)
      {
         FilterCriterion part = (FilterCriterion) i.next();
         foundBigData |= !Boolean.FALSE.equals(part.accept(this, context));
      }

      return foundBigData ? Boolean.TRUE : Boolean.FALSE;
   }

   public Object visit(UnaryOperatorFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(BinaryOperatorFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(TernaryOperatorFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(ProcessDefinitionFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(ProcessStateFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(ProcessInstanceFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(StartingUserFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(ActivityFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(ActivityInstanceFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(ActivityStateFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(PerformingUserFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(PerformingParticipantFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(PerformingOnBehalfOfFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(PerformedByUserFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(AbstractDataFilter filter, Object context)
   {
      return DataValueBean.isLargeValue(filter.getOperand())
            ? Boolean.TRUE
            : Boolean.FALSE;
   }

   public Object visit(ParticipantAssociationFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(CurrentPartitionFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(UserStateFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(ProcessInstanceLinkFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
   {
      return Boolean.FALSE;
   }

   public Object visit(DocumentFilter filter, Object context)
   {
      return Boolean.FALSE;
   }
}
