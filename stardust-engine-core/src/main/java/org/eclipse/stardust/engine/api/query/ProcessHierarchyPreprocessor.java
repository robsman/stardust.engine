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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ProcessHierarchyPreprocessor implements FilterEvaluationVisitor
{
   static final Logger trace =
         LogManager.getLogger(ProcessHierarchyPreprocessor.class);

   public Node preprocessQuery(Query query, EvaluationContext context)
   {
      // find state filters in top level filter term, using them to potentially massively
      // reduce the working set to be preprocessed
      Set processStateFilters = getFullProcessStateSet();
      for (Iterator i = query.getFilter().getParts().iterator(); i.hasNext();)
      {
         FilterCriterion filter = (FilterCriterion) i.next();
         if (filter instanceof ActivityStateFilter)
         {
            ActivityStateFilter activityStateFilter = (ActivityStateFilter) filter;

            Collection states = Arrays.asList(activityStateFilter.getStates());
            if (!activityStateFilter.isInclusive())
            {
               Set invertedStateSet = getFullActivityStateSet();
               invertedStateSet.removeAll(states);
               states = invertedStateSet;
            }

            // translate activity instance states to according process instance states
            Set allowedProcessStates = new TreeSet();

            for (Iterator j = states.iterator(); j.hasNext();)
            {
               ActivityInstanceState state = (ActivityInstanceState) j.next();

               switch (state.getValue())
               {
                  case ActivityInstanceState.CREATED:
                  case ActivityInstanceState.APPLICATION:
                  case ActivityInstanceState.INTERRUPTED:
                  case ActivityInstanceState.SUSPENDED:
                  case ActivityInstanceState.HIBERNATED:
                     allowedProcessStates.add(ProcessInstanceState.Active);
                     allowedProcessStates.add(ProcessInstanceState.Interrupted);
                     break;

                  default:
                     allowedProcessStates.addAll(getFullProcessStateSet());
                     break;
               }
            }

            processStateFilters = intersection(processStateFilters, allowedProcessStates);
         }
         else if (filter instanceof ProcessStateFilter)
         {
            ProcessStateFilter stateFilter = (ProcessStateFilter) filter;

            Set filterStates;
            if (stateFilter.isInclusive())
            {
               filterStates = new TreeSet(Arrays.asList(stateFilter.getStates()));
            }
            else
            {
               filterStates = getFullActivityStateSet();
               filterStates.removeAll(Arrays.asList(stateFilter.getStates()));
            }

            if (filterStates.contains(ProcessInstanceState.Completed)
                  || filterStates.contains(ProcessInstanceState.Aborted))
            {
               // terminated process may be childs of still active super processes
               filterStates.add(ProcessInstanceState.Active);
               filterStates.add(ProcessInstanceState.Interrupted);
            }

            processStateFilters = intersection(processStateFilters, filterStates);
         }
      }

      return (Node) query.getFilter().accept(this,
            new VisitationContext(query, context, processStateFilters));
   }

   public Object visit(FilterTerm filter, Object rawContext)
   {
      final VisitationContext context = (VisitationContext) rawContext;
      FilterTerm result = filter.createOfSameKind(context.getQuery().getFilter()
            .getVerifier());

      final Node localRestriction = performTermLevelPreprocessing(filter, context);

      Set rootProcessOIDs = localRestriction.getRootProcessOIDs();
      Set processOIDs = localRestriction.getProcessOIDs();

      for (Iterator itr = filter.getParts().iterator(); itr.hasNext();)
      {
         Node node = null;
         FilterCriterion filterCriterion = (FilterCriterion) itr.next();
         if(filterCriterion instanceof ProcessInstanceFilter
               && filter instanceof FilterOrTerm)
         {
            node = new Node(filterCriterion);
         }
         else
         {
            node = (Node) filterCriterion.accept(this, rawContext);
         }

         if (null != node.getFilter())
         {
            result.add(node.getFilter());
         }

         if (filter.getKind().equals(FilterTerm.AND))
         {
            rootProcessOIDs = intersection(rootProcessOIDs, node.getRootProcessOIDs());
            processOIDs = intersection(processOIDs, node.getProcessOIDs());
         }
         else if (filter.getKind().equals(FilterTerm.OR))
         {
            rootProcessOIDs = union(rootProcessOIDs, node.getRootProcessOIDs());
            processOIDs = union(processOIDs, node.getProcessOIDs());
         }
         else
         {
            Assert.lineNeverReached("Invalid filter term: " + filter.getKind());
         }
      }

      Node node;
      if (filter.getKind().equals(FilterTerm.AND))
      {
         node = new Node(result, null, intersection(QueryUtils.findProcessClosure(
               rootProcessOIDs, context.getEvaluationContext()), processOIDs));
      }
      else if (filter.getKind().equals(FilterTerm.OR))
      {
         node = new Node(result, null, union(QueryUtils.findProcessClosure(
               rootProcessOIDs, context.getEvaluationContext()), processOIDs));
      }
      else
      {
         Assert.lineNeverReached("Invalid filter term: " + filter.getKind());

         node = new Node(result);
      }

      return node;
   }

   /**
    * Template method allowing for term-level filter preprocessing. Returns by default
    * an empty node, meaning no term-level contributions.
    *
    * @param filter The term defining the scope for term-level evaluation
    * @param context The current visitation context
    * @return The {@link Node} containing the resulting term-level preprocessing result
    */
   protected Node performTermLevelPreprocessing(FilterTerm filter,
         VisitationContext context)
   {
      return new Node(null, null);
   }

   public Object visit(UnaryOperatorFilter filter, Object rawContext)
   {
      return new Node(filter);
   }

   public Object visit(BinaryOperatorFilter filter, Object rawContext)
   {
      return new Node(filter);
   }

   public Object visit(TernaryOperatorFilter filter, Object rawContext)
   {
      return new Node(filter);
   }

   public Object visit(ProcessDefinitionFilter filter, Object rawContext)
   {
      return new Node(filter);
   }

   public Object visit(ProcessStateFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(ProcessInstanceFilter filter, Object context)
   {
      // TODO leave filter in place if hierarchical, but OIDs designate root PIs
      if (filter.isIncludingSubprocesses())
      {
         return new Node(new HashSet(filter.getOids()), null);
      }
      else
      {
         return new Node(null, new HashSet(filter.getOids()));
      }
   }

   public Object visit(StartingUserFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(ActivityFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(ActivityInstanceFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(ActivityStateFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(PerformingUserFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(PerformingParticipantFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(PerformingOnBehalfOfFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(PerformedByUserFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(AbstractDataFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(ParticipantAssociationFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(CurrentPartitionFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(UserStateFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(ProcessInstanceLinkFilter filter, Object context)
   {
      return new Node(filter);
   }

   public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
   {
      return new Node(filter);
   }

   protected static Set intersection(Set lhs, Set rhs)
   {
      if (null == lhs)
      {
         return rhs;
      }
      else if (null == rhs)
      {
         return lhs;
      }
      else
      {
         if (lhs.size() < rhs.size())
         {
            lhs.retainAll(rhs);

            return lhs;
         }
         else
         {
            rhs.retainAll(lhs);

            return rhs;
         }
      }
   }

   protected static Set union(Set lhs, Set rhs)
   {
      if (null == lhs)
      {
         return rhs;
      }
      else if (null == rhs)
      {
         return lhs;
      }
      else
      {
         if (lhs.size() > rhs.size())
         {
            lhs.addAll(rhs);

            return lhs;
         }
         else
         {
            rhs.addAll(lhs);

            return rhs;
         }
      }
   }

   protected Set getFullProcessStateSet()
   {
      Set result = new TreeSet();

      result.add(ProcessInstanceState.Active);
      result.add(ProcessInstanceState.Aborted);
      result.add(ProcessInstanceState.Completed);
      result.add(ProcessInstanceState.Interrupted);

      return result;
   }

   protected Set getFullActivityStateSet()
   {
      Set result = new TreeSet();

      result.add(ActivityInstanceState.Created);
      result.add(ActivityInstanceState.Application);
      result.add(ActivityInstanceState.Completed);
      result.add(ActivityInstanceState.Interrupted);
      result.add(ActivityInstanceState.Suspended);
      result.add(ActivityInstanceState.Aborted);
      result.add(ActivityInstanceState.Hibernated);

      return result;
   }

   protected class Node
   {
      private final FilterCriterion filter;

      private Set rootProcessOIDs;
      private Set processOIDs;

      protected Node(FilterCriterion filter)
      {
         this.filter = filter;
      }

      protected Node(Set rootProcessOIDs, Set processOIDs)
      {
         this(null, rootProcessOIDs, processOIDs);
      }

      protected Node(FilterCriterion filter, Set rootProcessOIDs, Set processOIDs)
      {
         this.filter = filter;

         this.rootProcessOIDs = rootProcessOIDs;
         this.processOIDs = processOIDs;
      }

      public FilterCriterion getFilter()
      {
         return filter;
      }

      public Set getRootProcessOIDs()
      {
         return rootProcessOIDs;
      }

      public Set getProcessOIDs()
      {
         return processOIDs;
      }
   }

   protected static class VisitationContext
   {
      private final Query query;
      private final EvaluationContext evaluationContext;
      private final Set processStateRestriction;

      public VisitationContext(Query query, EvaluationContext evaluationContext,
            Set processStateRestriction)
      {
         this.query = query;
         this.evaluationContext = evaluationContext;
         this.processStateRestriction = processStateRestriction;
      }

      public Query getQuery()
      {
         return query;
      }

      public EvaluationContext getEvaluationContext()
      {
         return evaluationContext;
      }

      public Set getProcessStateRestriction()
      {
         return processStateRestriction;
      }
   }
}