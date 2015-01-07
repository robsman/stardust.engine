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

/**
 * Visitor allowing to transfer copies of filter criteria between queries.
 *
 * @author rsauer
 * @version $Revision$
 */
public class FilterCopier implements FilterEvaluationVisitor
{
   private final FilterVerifier targetVerifier;

   /**
    * Creates copies of filter terms.
    *
    * @see #FilterCopier(FilterVerifier)
    * @see #visit(FilterTerm, Object)
    */
   public FilterCopier()
   {
      this.targetVerifier = null;
   }

   /**
    * Creates copies of filter terms, but using the <code>targetVerifier</code> for the
    * copy made.
    *
    * @param targetVerifier The filter targetVerifier to be used by the copy made.
    *
    * @see #FilterCopier()
    * @see #visit(FilterTerm, Object)
    */
   public FilterCopier(FilterVerifier targetVerifier)
   {
      this.targetVerifier = targetVerifier;
   }

   /**
    * Creates a copy of the given filter term, but optionally using a different
    * {@link FilterVerifier} for the copy made.
    *
    * @param filter The filter term to copy
    * @param context Not considered, can be <code>null</code> for plain copying.
    *
    * @return The copied filter term.
    */
   public Object visit(FilterTerm filter, Object context)
   {
      FilterTerm result = filter.createOfSameKind(targetVerifier);

      for (Iterator itr = filter.getParts().iterator(); itr.hasNext();)
      {
         FilterCriterion criterion = (FilterCriterion) itr.next();
         result.add((FilterCriterion) criterion.accept(this, context));
      }

      return result;
   }

   public Object visit(UnaryOperatorFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(BinaryOperatorFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(TernaryOperatorFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ProcessDefinitionFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ProcessStateFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ProcessInstanceFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(StartingUserFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ActivityFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ActivityInstanceFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ActivityStateFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(PerformingUserFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(PerformingParticipantFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(PerformingOnBehalfOfFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(PerformedByUserFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(AbstractDataFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ParticipantAssociationFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(CurrentPartitionFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(UserStateFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ProcessInstanceLinkFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
   {
      return filter;
   }

   public Object visit(DocumentFilter filter, Object context)
   {
      return filter;
   }
}
