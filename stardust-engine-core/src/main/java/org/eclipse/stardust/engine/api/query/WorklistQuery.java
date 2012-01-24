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

import org.eclipse.stardust.common.SplicingIterator;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;


/**
 * Query for retrieving (parts of) a user's worklist.
 * 
 * <p>
 * The retrieved worklist possibly includes contributions from:
 * <ul>
 * <li>The user's private worklist (see {@link #setUserContribution(boolean)} and
 * {@link #setUserContribution(SubsetPolicy)})</li>
 * <li>Several participant worklists (see
 * {@link #setParticipantContribution(PerformingParticipantFilter)} and
 * {@link #setParticipantContribution(PerformingParticipantFilter, SubsetPolicy)})</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Valid filter criteria, applying to items from all contributing worklists, are:
 * <ul>
 * <li>{@link FilterTerm} for building complex criteria.</li>
 * <li>{@link UnaryOperatorFilter}, {@link BinaryOperatorFilter} or
 * {@link TernaryOperatorFilter} for applying filters to activity instance attributes (see
 * {@link #START_TIME} or {@link #LAST_MODIFICATION_TIME}).</li>
 * <li>{@link ProcessDefinitionFilter} for finding instances of activities belonging to
 * specific process definitions.</li>
 * <li>{@link ProcessInstanceFilter} for finding activities belonging to specific process
 * instances.</li>
 * <li>{@link ActivityFilter} for finding instances of specific activities.</li>
 * <li>{@link ActivityStateFilter} for finding activity instances currently being in
 * specific states: APPLICATION or SUSPENDED</li>
 * <li>{@link DataFilter} for finding activity instances belonging to process instances
 * with same scope process instance containing specific workflow data.</li>
 * <li>{@link SubProcessDataFilter} for finding activity instances belonging to process
 * instances and its subprocess instances containing specific workflow data.</li>
 * <li>{@link HierarchyDataFilter} for finding activity instances belonging to the
 * complete hierarchy of process instances containing specific workflow data.</li>
 * </ul>
 * </p>
 * 
 * @author rsauer
 * @version $Revision$
 * 
 * @see ActivityInstanceQuery
 */
public class WorklistQuery extends Query
{
   private static final long serialVersionUID = -3280665639389822515L;

   private static final Logger trace = LogManager.getLogger(WorklistQuery.class);

   private static final String FIELD__PROCESS_INSTANCE_PRIORITY = "process_instance_"
         + ProcessInstanceBean.FIELD__PRIORITY;

   /**
    * The timestamp when the activity instance was created.
    */
   public static final Attribute START_TIME = new Attribute(
         WorkItemBean.FIELD__START_TIME);

   /**
    * The timestamp when the activity instance was last modified.
    */
   public static final Attribute LAST_MODIFICATION_TIME = new Attribute(
         WorkItemBean.FIELD__LAST_MODIFICATION_TIME);

   /**
    * The OID of the activity the worklist item is an instance of.
    */
   public static final Attribute ACTIVITY_OID = new Attribute(
         WorkItemBean.FIELD__ACTIVITY);

   /**
    * The OID of the activity instance the worklist item represents.
    */
   public static final Attribute ACTIVITY_INSTANCE_OID = new Attribute(
         WorkItemBean.FIELD__ACTIVITY_INSTANCE);

   /**
    * The OID of the process instance the worklist item belongs to.
    */
   public static final Attribute PROCESS_INSTANCE_OID = new Attribute(
         WorkItemBean.FIELD__PROCESS_INSTANCE);

   /**
    * The criticality of the activity instance work item.
    */
   public static final Attribute ACTIVITY_INSTANCE_CRITICALITY = new Attribute(
         WorkItemBean.FIELD__CRITICALITY);

   /**
    * The priority of the process instance the activity instance belongs to.
    * 
    * @see org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean#getPriority()
    */
   public static final FilterableAttribute PROCESS_INSTANCE_PRIORITY = new ReferenceAttribute(
         new Attribute(FIELD__PROCESS_INSTANCE_PRIORITY), ProcessInstanceBean.class,
         WorkItemBean.FIELD__PROCESS_INSTANCE, ProcessInstanceBean.FIELD__OID,
         ProcessInstanceBean.FIELD__PRIORITY);

   /**
    * Worklist items are either active (user worklist) or suspended (user/participant
    * worklist)
    */
   private static final ActivityStateFilter WORKLIST_ITEMS = new ActivityStateFilter(
         new ActivityInstanceState[] {
               ActivityInstanceState.Application, ActivityInstanceState.Suspended});

   /**
    * Verifier for the complete - internally built - worklist filter.
    */
   private static final FilterVerifier WORKLIST_FILTER_VERIFYER = new WhitelistFilterVerifyer(
         new Class[] {
               FilterTerm.class, UnaryOperatorFilter.class, BinaryOperatorFilter.class,
               TernaryOperatorFilter.class, ProcessDefinitionFilter.class,
               ProcessInstanceFilter.class, ActivityFilter.class,
               ActivityStateFilter.class, PerformingUserFilter.class,
               PerformingParticipantFilter.class, PerformingOnBehalfOfFilter.class,
               DataFilter.class, SubProcessDataFilter.class, HierarchyDataFilter.class,
               DataPrefetchHint.class, CurrentPartitionFilter.class});

   /**
    * Verifyer for preventing users from applying unsupported filter criteria.
    */
   private static final FilterVerifier USER_FILTER_VERIFYER = new FilterScopeVerifier(
         new BlacklistFilterVerifyer(WORKLIST_FILTER_VERIFYER, new Class[] {
               PerformingUserFilter.class, PerformingParticipantFilter.class}),
         WorklistQuery.class);

   private UserContribution userContribution;

   private final Map<Object, ParticipantContribution> modelParticipantContributions;

   private final Map<ParticipantInfo, ParticipantContribution> userGroupContributions;

   /**
    * Prepares a query for retrieving all items from the user's private worklist.
    * 
    * @return The readily configured query.
    * 
    * @see #findPrivateWorklist(int)
    * @see #findCompleteWorklist
    */
   public static WorklistQuery findPrivateWorklist()
   {
      return new WorklistQuery();
   }

   /**
    * Creates a query for retrieving up to <code>maxSize</code> items from the user's
    * private worklist.
    * 
    * @param maxSize
    *           The maximum number of items to retrieve.
    * @return The readily configured query.
    * 
    * @see #findPrivateWorklist()
    * @see #findCompleteWorklist()
    */
   public static WorklistQuery findPrivateWorklist(int maxSize)
   {
      WorklistQuery query = findPrivateWorklist();
      query.setUserContribution(new SubsetPolicy(maxSize));

      return query;
   }

   /**
    * Creates a query for retrieving all items from the user's private worklist plus all
    * items from role and organization worklists the user belongs to directly or
    * indirectly via the participant hierarchy.
    * 
    * @return The readily configured worklist.
    * 
    * @see #findPrivateWorklist()
    * @see PerformingParticipantFilter#ANY_FOR_USER
    */
   public static WorklistQuery findCompleteWorklist()
   {
      WorklistQuery query = new WorklistQuery();
      query.setParticipantContribution(PerformingParticipantFilter.ANY_FOR_USER);

      return query;
   }

   /**
    * Initializes a new {@link WorklistQuery} to retrieve the user's private worklist.
    */
   public WorklistQuery()
   {
      super(USER_FILTER_VERIFYER);
      setPolicy(new ModelVersionPolicy(false));

      setUserContribution(true);
      this.modelParticipantContributions = new TreeMap(new ObjectComparator());
      this.userGroupContributions = new TreeMap(new ParticipantInfoComparator());
   }

   private WorklistQuery(WorklistQuery rhs)
   {
      super(rhs, new FilterCopier(USER_FILTER_VERIFYER));

      this.userContribution = rhs.userContribution;
      this.modelParticipantContributions = new TreeMap(new ObjectComparator());
      this.userGroupContributions = new TreeMap(new ParticipantInfoComparator());

      this.modelParticipantContributions.putAll(rhs.modelParticipantContributions);
      this.userGroupContributions.putAll(rhs.userGroupContributions);
   }

   /**
    * Gets the currently configured contribution of the user's private worklist to the
    * retrieved result.
    * 
    * @return The current settings for the user's private worklist contribution.
    * 
    * @see #setUserContribution(boolean)
    */
   public UserContribution getUserContribution()
   {
      return userContribution;
   }

   /**
    * Configures the user's private worklist to be either included or ommitted from the
    * result.
    * 
    * <p>
    * The size of the private worklist contribution will be restricted by the
    * {@link SubsetPolicy} set via {@link Query#setPolicy(EvaluationPolicy)}, if existing.
    * </p>
    * 
    * @param included
    *           Flag indicating if the user's private worklist will be included in the
    *           result.
    * 
    * @see #setUserContribution(SubsetPolicy)
    * @see #setParticipantContribution(PerformingParticipantFilter)
    */
   public void setUserContribution(boolean included)
   {
      this.userContribution = new UserContribution(included);
   }

   /**
    * Configures the user's private worklist to be partially included in the result.
    * 
    * @param subset
    *           The specification of the subset of the user's private worklist to be
    *           included in the result.
    * 
    * @see #setUserContribution(boolean)
    * @see #setParticipantContribution(PerformingParticipantFilter, SubsetPolicy)
    */
   public void setUserContribution(SubsetPolicy subset)
   {
      this.userContribution = new UserContribution(subset);
   }

   public Collection< ? extends ParticipantContribution> getUserGroupContributions()
   {
      return Collections.unmodifiableCollection(userGroupContributions.values());
   }

   public Collection< ? extends ParticipantContribution> getModelParticipantContributions()
   {
      return Collections.unmodifiableCollection(modelParticipantContributions.values());
   }

   /**
    * Configures the worklist(s) resulting from the given participant filter to be
    * included in the result.
    * 
    * <p>
    * The size of all these worklist contributions will be restricted by the
    * {@link SubsetPolicy} set via {@link Query#setPolicy(EvaluationPolicy)}, if existing.
    * </p>
    * 
    * @param filter
    *           The filter used to specify the participants contributing their worklists
    *           to the result.
    */
   public void setParticipantContribution(PerformingParticipantFilter filter)
   {
      setParticipantContribution(filter, null);
   }

   /**
    * Configures the worklist(s) resulting from the given participant filter to be
    * included in the result, restricting the size of these worklist according to the
    * given
    * <p>
    * subset
    * </p>
    * .
    * 
    * @param filter
    *           The filter used to determine the participants contributing their worklists
    *           to the result.
    * @param subset
    *           The specification of the subset of each participant's worklist to be
    *           included in the result.
    */
   public void setParticipantContribution(PerformingParticipantFilter filter,
         SubsetPolicy subset)
   {
      if (PerformingParticipantFilter.FILTER_KIND_MODEL_PARTICIPANT.equals(filter.getFilterKind()))
      {
         modelParticipantContributions.put(filter.getParticipant(),
               new ParticipantContribution(filter, subset));
      }
      else if (PerformingParticipantFilter.FILTER_KIND_USER_GROUP.equals(filter.getFilterKind()))
      {
         userGroupContributions.put(filter.getParticipant(), new ParticipantContribution(
               filter, subset));
      }
      else
      {
         modelParticipantContributions.put(filter.getFilterKind(),
               new ParticipantContribution(filter, subset));
      }
   }

   public Object evaluateFilter(FilterEvaluationVisitor visitor, Object context)
   {
      FilterTerm performerFilter = new FilterOrTerm(WORKLIST_FILTER_VERIFYER);
      if (getUserContribution().isIncluded())
      {
         performerFilter.add(PerformingUserFilter.CURRENT_USER);
      }

      for (Iterator itr = modelParticipantContributions.values().iterator(); itr.hasNext();)
      {
         ParticipantContribution contribution = (ParticipantContribution) itr.next();
         performerFilter.add(contribution.getFilter());
      }
      for (Iterator itr = userGroupContributions.values().iterator(); itr.hasNext();)
      {
         ParticipantContribution contribution = (ParticipantContribution) itr.next();
         performerFilter.add(contribution.getFilter());
      }

      FilterTerm worklistFilter = new FilterAndTerm(WORKLIST_FILTER_VERIFYER);
      worklistFilter.add(getFilter());
      worklistFilter.add(performerFilter);
      worklistFilter.add(WORKLIST_ITEMS);

      return worklistFilter.accept(visitor, context);
   }

   /**
    * Evaluates a semantically equivalent query including any implicit worklist
    * contributions in an explicit form. Such an explicit clone is specifically useful for
    * returning as source of an evaluated query.
    * 
    * @param context
    *           The context to use for query evaluation.
    * @return The evaluated query clone.
    */
   protected WorklistQuery expandedQuery(EvaluationContext context)
   {
      WorklistQuery query = new WorklistQuery(this);
      ModelManager modelManager = context.getModelManager();

      query.modelParticipantContributions.clear();
      query.userGroupContributions.clear();

      for (Iterator contributionItr = new SplicingIterator(
            getModelParticipantContributions().iterator(),
            getUserGroupContributions().iterator()); contributionItr.hasNext();)
      {
         ParticipantContribution implicitContribution = (ParticipantContribution) contributionItr.next();

         SubsetPolicy subset = implicitContribution.getSubset();
         if (null == subset)
         {
            subset = (SubsetPolicy) getPolicy(SubsetPolicy.class);
         }

         Set<IParticipant> explicitContributors = QueryUtils.findContributingParticipants(
               implicitContribution.getFilter(), context);

         for (IParticipant contributor : explicitContributors)
         {
            if (contributor instanceof IModelParticipant)
            {
               ParticipantInfo info = DepartmentUtils.getParticipantInfo(contributor,
                     modelManager);
               query.setParticipantContribution(
                     PerformingParticipantFilter.forParticipant(info, false), subset);
            }
            else if (contributor instanceof IUserGroup)
            {
               ParticipantInfo info = DepartmentUtils.getParticipantInfo(contributor,
                     null);
               query.setParticipantContribution(
                     PerformingParticipantFilter.forParticipant(info, false), subset);
            }
            else
            {
               trace.warn("Ignoring unknown worklist contributor: " + contributor);
            }
         }
      }

      return query;
   }

   protected static class Contribution implements Serializable
   {
      private static final long serialVersionUID = 4714660948900395306L;

      private final SubsetPolicy subset;

      private Contribution(SubsetPolicy subset)
      {
         this.subset = subset;
      }

      /**
       * Gets the specification of the subset contributed to the result.
       * 
       * @return The subset specification, or <code>null</code> if no such exists.
       */
      public SubsetPolicy getSubset()
      {
         return subset;
      }
   }

   /**
    * Class describing the user's private worklist contribution.
    * 
    * @see WorklistQuery#getUserContribution
    * @see WorklistQuery#setUserContribution(boolean)
    * @see WorklistQuery#setUserContribution(SubsetPolicy)
    */
   public static final class UserContribution extends Contribution
   {
      private static final long serialVersionUID = 7204518377769124081L;

      private final boolean included;

      private UserContribution(boolean included)
      {
         super(null);

         this.included = included;
      }

      private UserContribution(SubsetPolicy subset)
      {
         super(subset);

         this.included = true;
      }

      /**
       * Gets the flag if the user's private worklist contributes to the result.
       * 
       * @return Flag indicating if the user's private worklist contributes.
       */
      public boolean isIncluded()
      {
         return included;
      }
   }

   /**
    * Class describing participant worklist contributions.
    * 
    * @see WorklistQuery#setParticipantContribution(PerformingParticipantFilter)
    * @see WorklistQuery#setParticipantContribution(PerformingParticipantFilter,
    *      SubsetPolicy)
    */
   public static final class ParticipantContribution extends Contribution
   {
      private static final long serialVersionUID = 6061860803437728816L;

      private final PerformingParticipantFilter filter;

      private ParticipantContribution(PerformingParticipantFilter participant,
            SubsetPolicy subset)
      {
         super(subset);

         this.filter = participant;
      }

      /**
       * Gets the filter used to determine the participants contributing their worklists
       * to the result.
       * 
       * @return The participant filter.
       */
      public PerformingParticipantFilter getFilter()
      {
         return filter;
      }
   }

   /**
    * Worklist item attribute supporting filter operations.
    * <p />
    * Not for direct use.
    * 
    */
   public static final class Attribute extends FilterableAttributeImpl
   {
      private Attribute(String name)
      {
         super(WorklistQuery.class, name);
      }
   }

   private static final class ObjectComparator
         implements Comparator<Object>, Serializable
   {
      private static final long serialVersionUID = 1L;

      private final ParticipantInfoComparator partComparator = new ParticipantInfoComparator();

      public int compare(Object o1, Object o2)
      {
         if (o1 instanceof ParticipantInfo && o2 instanceof ParticipantInfo)
         {
            return partComparator.compare((ParticipantInfo) o1, (ParticipantInfo) o2);
         }

         return o1.toString().compareTo(o2.toString());
      }
   }

   private static final class ParticipantInfoComparator
         implements Comparator<ParticipantInfo>, Serializable
   {
      private static final long serialVersionUID = 1L;

      public int compare(ParticipantInfo o1, ParticipantInfo o2)
      {
         if (o1 == null && o2 == null)
         {
            return 0;
         }
         else if (o1 == null && o2 != null)
         {
            return -1;
         }
         else if (o1 != null && o2 == null)
         {
            return 1;
         }
         else if (o1 instanceof ModelParticipantInfo
               && o2 instanceof ModelParticipantInfo)
         {
            final int idResult = o1.getQualifiedId().compareTo(o2.getQualifiedId());
            if (idResult == 0)
            {
               DepartmentInfo dep1 = ((ModelParticipantInfo) o1).getDepartment();
               DepartmentInfo dep2 = ((ModelParticipantInfo) o2).getDepartment();
               Long dep1Oid = dep1 == null ? 0 : dep1.getOID();
               Long dep2Oid = dep2 == null ? 0 : dep2.getOID();

               return dep1Oid.compareTo(dep2Oid);
            }
            else
            {
               return idResult;
            }
         }
         else
         {
            return o1.getQualifiedId().compareTo(o2.getQualifiedId());
         }
      }
   }
}