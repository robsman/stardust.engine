/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserGroupInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserInfoDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.Functions.BoundFunction;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class WorklistQueryEvaluator
{
   private static final int PERF_KIND_IDX = 1;
   private static final int PERF_IDX = 2;
   private static final int DEPARTMENT_IDX = 3;
   //private static final int MODEL_IDX = 4;
   //private static final int ACTIVITY_IDX = 5;
   //private static final int SCOPE_PI_IDX = 6;
   private static final int COUNT_IDX = 7;
   // Extension columns have always to be added as last columns after the count
   private static final int EXTENSION_IDX = COUNT_IDX + 1;

   private static final Logger trace = LogManager.getLogger(WorklistQueryEvaluator.class);

   private final EvaluationContext context;

   private final WorklistQuery query;

   public WorklistQueryEvaluator(WorklistQuery query, EvaluationContext context)
   {
      this.context = context;
      this.query = query.expandedQuery(context);
   }

   public long getWorklistSize()
   {
      long size = 0;

      final WorklistQuery.UserContribution userContribution = query.getUserContribution();
      if (userContribution.isIncluded())
      {
         ActivityInstanceQuery userWorklistQuery = new ActivityInstanceQuery(query,
               PerformingUserFilter.CURRENT_USER);
         userWorklistQuery.setPolicy(getContributionSubset(userContribution));

         size = new WorkItemQueryEvaluator(userWorklistQuery, context).executeCount();
      }

      for (Iterator i = new SplicingIterator(query.getModelParticipantContributions()
            .iterator(), query.getUserGroupContributions().iterator()); i.hasNext();)
      {
         final WorklistQuery.ParticipantContribution contrib = (WorklistQuery.ParticipantContribution) i
               .next();

         ActivityInstanceQuery worklistQuery = new ActivityInstanceQuery(query, contrib
               .getFilter());
         worklistQuery.setPolicy(getContributionSubset(contrib));

         size += new WorkItemQueryEvaluator(worklistQuery, context).executeCount();
      }

      return size;
   }

   public Worklist buildWorklist()
   {
      SubsetPolicy subset = (SubsetPolicy) query.getPolicy(SubsetPolicy.class);

      if (Parameters.instance().getBoolean(
            KernelTweakingProperties.OPTIMIZE_COUNT_ONLY_SUBSET_POLICY, true)
            && isCountOnly())
      {
         return buildCountOnlyWorklist(subset);
      }
      else
      {
         return buildStandardWorklist();
      }
   }

   private Worklist buildCountOnlyWorklist(SubsetPolicy subsetPolicy)
   {
      final Map<ScopedParticipantInfo, WorklistCollector> modelParticipantInfos = CollectionUtils
            .newHashMap();
      final Map<Long, WorklistCollector> userGroupRtOids = CollectionUtils.newHashMap();
      final List<WorklistCollector> allCollectors = CollectionUtils.newArrayList();

      final WorklistQuery.UserContribution userContribution = query.getUserContribution();
      if ( !userContribution.isIncluded()
            && query.getModelParticipantContributions().isEmpty()
            && query.getUserGroupContributions().isEmpty())
      {
         // no contributions, return empty worklist
         return createUserWorklist(subsetPolicy, Collections.EMPTY_LIST, null, Long.MAX_VALUE);
      }

      collectParticipantInfos(modelParticipantInfos, userGroupRtOids, allCollectors);

      OrTerm contributionsPredicate = new OrTerm();

      final long userOid = context.getUser().getOID();
      if (userContribution.isIncluded())
      {
         // add user contribution
         contributionsPredicate.add(Predicates.andTerm( //
               Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND, PerformerType.USER), // 
               Predicates.isEqual(WorkItemBean.FR__PERFORMER, userOid)));
      }

      if ( !modelParticipantInfos.isEmpty())
      {
         // add model participant contribution
         OrTerm spOrTerm = new OrTerm();
         for (ScopedParticipantInfo spInfo : modelParticipantInfos.keySet())
         {
            Long participantOid = spInfo.getParticipantOid();
            Long depOid = spInfo.getDepartmentOid();
            spOrTerm.add(Predicates.andTerm( //
                  Predicates.isEqual(WorkItemBean.FR__PERFORMER, participantOid), // 
                  Predicates.isEqual(WorkItemBean.FR__DEPARTMENT, depOid)));
         }
         PredicateTerm participantPredicate = Predicates.andTerm(Predicates.isEqual(
               WorkItemBean.FR__PERFORMER_KIND, PerformerType.MODEL_PARTICIPANT),
               spOrTerm);
         contributionsPredicate.add(participantPredicate);
      }

      if ( !userGroupRtOids.isEmpty())
      {
         // add user group contribution
         final ArrayList<Long> values = new ArrayList(userGroupRtOids.keySet());

         PredicateTerm participantPredicate = Predicates.andTerm(Predicates.isEqual(
               WorkItemBean.FR__PERFORMER_KIND, PerformerType.USER_GROUP), Predicates
               .inList(WorkItemBean.FR__PERFORMER, values));
         contributionsPredicate.add(participantPredicate);
      }
      
      ActivityInstanceQuery userWorklistQuery = new ActivityInstanceQuery(query);
      
      final BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      boolean legacyEvaluation = false;
      Authorization2Predicate authorizationPredicate = runtimeEnvironment.getAuthorizationPredicate();
      if (authorizationPredicate != null)
      {
         legacyEvaluation = authorizationPredicate.addPrefetchDataHints(userWorklistQuery);
      }

      ParsedQuery parsedQuery = new WorkItemQueryEvaluator(userWorklistQuery, context)
            .parseQuery();

      BoundFunction countFunction = parsedQuery.useDistinct() 
            ? Functions.countDistinct(WorkItemBean.FR__ACTIVITY_INSTANCE) 
            : Functions.rowCount();
            
      QueryDescriptor countWorkItems = QueryDescriptor
            .from(WorkItemBean.class)
            .select(assembleColumns(new Column[] {
                  WorkItemBean.FR__PERFORMER_KIND,
                  WorkItemBean.FR__PERFORMER,
                  WorkItemBean.FR__DEPARTMENT,
                  WorkItemBean.FR__MODEL,
                  WorkItemBean.FR__ACTIVITY,
                  !legacyEvaluation
                        ? WorkItemBean.FR__SCOPE_PROCESS_INSTANCE
                        : Functions.constantExpression("0 AS " + WorkItemBean.FIELD__SCOPE_PROCESS_INSTANCE),
                  countFunction},
                  parsedQuery.getSelectExtension()))
            .where(Predicates.andTerm(
                  parsedQuery.getPredicateTerm(),
                  contributionsPredicate))
            .groupBy(assembleColumns(new FieldRef[] {
                  WorkItemBean.FR__PERFORMER_KIND,
                  WorkItemBean.FR__PERFORMER,
                  WorkItemBean.FR__DEPARTMENT,
                  WorkItemBean.FR__MODEL,
                  WorkItemBean.FR__ACTIVITY,
                  WorkItemBean.FR__SCOPE_PROCESS_INSTANCE},
                  parsedQuery.getSelectExtension()))
            .orderBy(
                  WorkItemBean.FR__PERFORMER_KIND,
                  WorkItemBean.FR__PERFORMER,
                  WorkItemBean.FR__DEPARTMENT);
      
      countWorkItems.getQueryExtension().addJoins(Joins.newJoins(parsedQuery.getPredicateJoins()));

      Session jdbcSession = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ResultSet countedWorkitems = jdbcSession.executeQuery(countWorkItems, QueryUtils
            .getTimeOut(query));
      
      if(authorizationPredicate != null)
      {
         authorizationPredicate.setSelectionExtension(EXTENSION_IDX, parsedQuery.getSelectExtension());
      }

      // Read values for each worklist and ignore workitems whose activities are 
      // restricted for the current user.

      long totalCountThreshold = QueryUtils.getTotalCountThreshold(authorizationPredicate);
      long totalCount = 0;
      try
      {
         while (countedWorkitems.next())
         {
            if (authorizationPredicate == null
                  || authorizationPredicate.accept(countedWorkitems))
            {
               int performerKind = countedWorkitems.getInt(PERF_KIND_IDX);
               long count = countedWorkitems.getLong(COUNT_IDX);
               
               if (PerformerType.USER == performerKind)
               {
                  totalCount += count;
               }
               else if (PerformerType.USER_GROUP == performerKind
                     || PerformerType.MODEL_PARTICIPANT == performerKind)
               {
                  long performer = countedWorkitems.getLong(PERF_IDX);
                  long department = countedWorkitems.getLong(DEPARTMENT_IDX);

                  WorklistCollector collector = (WorklistCollector) 
                     (PerformerType.MODEL_PARTICIPANT == performerKind
                        ? modelParticipantInfos.get(new ScopedParticipantInfo(performer, department))
                        : userGroupRtOids.get(new Long(performer)));
                  if (collector != null)
                  {
                     collector.addToTotalCount(count);
                  }
                  else
                  {
                     trace.warn("Unable to find worklist collector for " +
                        (PerformerType.MODEL_PARTICIPANT == performerKind
                           ? "model participant with runtime oid #" + performer + " and department with oid #" + department
                           : "usergroup with oid #" + (-performer)));
                  }
               }
            }
            if (totalCount > totalCountThreshold)
            {
               totalCount = Long.MAX_VALUE;
               break;
            }
         }
      }
      catch (SQLException e)
      {
         trace.error("Exception occured on fetching count only worklist retrieval.", e);
         throw new PublicException(e);
      }
      finally
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet(countedWorkitems);
      }

      // Build sub worklists
      final List subWorklists = new ArrayList();
      for (Iterator iterator = allCollectors.iterator(); iterator.hasNext();)
      {
         WorklistCollector collector = (WorklistCollector) iterator.next();
         Worklist participantWorklist = convertParticipantWorklist(collector);
         subWorklists.add(participantWorklist);
      }

      return createUserWorklist(subsetPolicy, subWorklists, new Long(totalCount), totalCountThreshold);
   }
   
   private UserWorklist createUserWorklist(SubsetPolicy subsetPolicy, List subWorklists,
         Long totalCount, long totalCountThreshold)
   {
      final UserInfo owner = DetailsFactory.create(context.getUser(), IUser.class,
            UserInfoDetails.class);
      return new UserWorklist(owner, query, subsetPolicy, Collections.EMPTY_LIST, false,
            subWorklists, totalCount, totalCountThreshold);

   }

   private Worklist buildStandardWorklist()
   {
      final WorklistCollector userWorklist = collectUserWorklist();

      final List participantWorklists = collectParticipantWorklists();

      final List subWorklists = new ArrayList();

      for (Iterator itr = participantWorklists.iterator(); itr.hasNext();)
      {
         WorklistCollector participantWorklist = (WorklistCollector) itr.next();

         subWorklists.add(convertParticipantWorklist(participantWorklist));
      }

      final UserInfo owner = DetailsFactory.create(context.getUser(), IUser.class,
            UserInfoDetails.class);
      return new UserWorklist(owner, query, userWorklist.subset,
            userWorklist.retrievedItems, userWorklist.hasMore, subWorklists,
            userWorklist.hasTotalCount() ? new Long(userWorklist.getTotalCount()) : null,
            userWorklist.getTotalCountThreshold());
   }

   /**
    * @param subset
    * @param modelParticipants
    * @param userGroups
    */
   private void collectParticipantInfos(
         final Map<ScopedParticipantInfo, WorklistCollector> modelParticipants,
         final Map<Long, WorklistCollector> userGroups,
         final List<WorklistCollector> allCollectors)
   {
      final ModelManager modelManager = context.getModelManager();
      final Set<IParticipant> participantClosure = QueryUtils
            .findScopedParticipantClosure(context.getUser());
      
      for (Iterator i = new SplicingIterator(query.getModelParticipantContributions()
            .iterator(), query.getUserGroupContributions().iterator()); i.hasNext();)
      {
         WorklistQuery.ParticipantContribution contrib = (WorklistQuery.ParticipantContribution) i
               .next();
         PerformingParticipantFilter filter = contrib.getFilter();
         
         WorklistCollector collector = new WorklistCollector(filter,
               getContributionSubset(contrib));
         collector.setTotalCount(0);
         allCollectors.add(collector);
         
         for (IParticipant contributor : participantClosure)
         {
            IParticipant rawParticipant = contributor;
            IDepartment department = null;
            
            if(rawParticipant instanceof IScopedModelParticipant)
            {
               IScopedModelParticipant scopedModelParticipant = (IScopedModelParticipant) rawParticipant;
               rawParticipant = scopedModelParticipant.getModelParticipant();
               department = scopedModelParticipant.getDepartment();
            }
            
            if ((rawParticipant instanceof IModelParticipant)
                  && PerformingParticipantFilter.FILTER_KIND_MODEL_PARTICIPANT
                        .equals(filter.getFilterKind()))
            {
               final IModelParticipant participant = (IModelParticipant) rawParticipant;

               final ModelParticipantInfo filterParticipant = (ModelParticipantInfo) filter
                     .getParticipant();
               if (CompareHelper.areEqual(participant.getId(), filterParticipant.getId())
                     && DepartmentUtils.areEqual(department, filterParticipant
                           .getDepartment()))
               {
                  try
                  {
                     IModelParticipant modelParticipant = modelManager
                           .findModelParticipant((ModelParticipantInfo) filterParticipant);
                     
                     final long runtimeOid = modelManager.getRuntimeOid(modelParticipant);
                     final long departmentOid = department == null ? 0 : department.getOID();
                     
                     modelParticipants.put(new ScopedParticipantInfo(runtimeOid,
                           departmentOid), collector);
                  }
                  catch (ObjectNotFoundException x)
                  {
                     // if participant cannot be found then it is simply not included into the query
                  }
               }
            }
            else if ((rawParticipant instanceof IUserGroup)
                  && PerformingParticipantFilter.FILTER_KIND_USER_GROUP.equals(filter
                        .getFilterKind()))
            {
               final IUserGroup userGroup = (IUserGroup) rawParticipant;
               if (CompareHelper.areEqual(userGroup.getId(), filter.getParticipantID()))
               {
                  userGroups.put(new Long(userGroup.getOID()), collector);
               }
            }
         }
      }
   }

   private WorklistCollector collectUserWorklist()
   {
      final WorklistQuery.UserContribution userContribution = query.getUserContribution();
      final SubsetPolicy subset = getContributionSubset(userContribution);

      UserInfo userInfo = new UserInfoDetails(context.getUser());
      WorklistCollector userWorklist = new WorklistCollector(userInfo, subset);
      if (userContribution.isIncluded())
      {
         ActivityInstanceQuery userWorklistQuery = new ActivityInstanceQuery(query,
               PerformingUserFilter.CURRENT_USER);
         userWorklistQuery.setPolicy(subset);

         collectWorklistItems(userWorklistQuery, userWorklist);
      }

      return userWorklist;
   }

   private List collectParticipantWorklists()
   {
      final List participantWorklists = new ArrayList();
      final Set<IParticipant> participantClosure = QueryUtils
            .findScopedParticipantClosure(context.getUser());

      for (Iterator i = new SplicingIterator(query.getModelParticipantContributions()
            .iterator(), query.getUserGroupContributions().iterator()); i.hasNext();)
      {
         WorklistQuery.ParticipantContribution contrib = (WorklistQuery.ParticipantContribution) i
               .next();

         SubsetPolicy subset = getContributionSubset(contrib);

         PerformingParticipantFilter filter = contrib.getFilter();
         Set<IParticipant> contributors = new HashSet();
         
         for (IParticipant contributor : participantClosure)
         {
            // do contributors match the the filter and supported types?  
            IParticipant rawParticipant = contributor;
            IDepartment department = null;
            if(rawParticipant instanceof IScopedModelParticipant)
            {
               IScopedModelParticipant scopedModelParticipant = (IScopedModelParticipant) rawParticipant;
               rawParticipant = scopedModelParticipant.getModelParticipant();
               department = scopedModelParticipant.getDepartment();
            }
            
            if ((rawParticipant instanceof IModelParticipant)
                  && PerformingParticipantFilter.FILTER_KIND_MODEL_PARTICIPANT
                        .equals(filter.getFilterKind()))
            {
               final IModelParticipant participant = (IModelParticipant) rawParticipant;
               final ModelParticipantInfo filterParticipant = (ModelParticipantInfo) filter.getParticipant();
               
               if (CompareHelper.areEqual((participant).getQualifiedId(), filterParticipant.getQualifiedId())
                     && DepartmentUtils.areEqual(department, filterParticipant.getDepartment()))
               {
                  contributors.add(contributor);
               }
            }
            else if ((rawParticipant instanceof IUserGroup)
                  && PerformingParticipantFilter.FILTER_KIND_USER_GROUP.equals(filter
                        .getFilterKind()))
            {
               IUserGroup userGroup = (IUserGroup) rawParticipant;
               if (CompareHelper.areEqual((userGroup).getId(), filter.getParticipantID()))
               {
                  contributors.add(contributor);
               }
            }
         }

         ActivityInstanceQuery worklistQuery = new ActivityInstanceQuery(query,
               new PerformingParticipantFilter(contributors));
         worklistQuery.setPolicy(subset);

         WorklistCollector participantWorklist = new WorklistCollector(filter, subset);

         collectWorklistItems(worklistQuery, participantWorklist);

         participantWorklists.add(participantWorklist);
      }

      return participantWorklists;
   }

   private SubsetPolicy getContributionSubset(WorklistQuery.Contribution contribution)
   {
      SubsetPolicy subset = null;

      if (null != contribution)
      {
         subset = contribution.getSubset();
      }

      if (null == subset)
      {
         subset = (SubsetPolicy) query.getPolicy(SubsetPolicy.class);
      }

      return subset;
   }

   private void collectWorklistItems(ActivityInstanceQuery wlQuery,
         WorklistCollector worklist)
   {
      ResultIterator rawResult = new WorkItemQueryEvaluator(wlQuery, context)
            .executeFetch();

      try
      {
         ActivityInstances worklistItems = ProcessQueryPostprocessor
               .findMatchingActivityInstanceDetails(wlQuery, rawResult);
         worklist.addAll(worklistItems);

         worklist.hasMore = worklistItems.hasMore();

         if (worklistItems.hasTotalCount())
         {
            worklist.setTotalCount(worklistItems.getTotalCount());
            worklist.setTotalCountThreshold(worklistItems.getTotalCountThreshold());
         }
      }
      finally
      {
         rawResult.close();
      }
   }

   private Worklist convertParticipantWorklist(WorklistCollector collector)
   {
      IParticipant participant;
      final ParticipantInfo participantInfo = collector.getParticipant();
      if (PerformingParticipantFilter.FILTER_KIND_MODEL_PARTICIPANT.equals(collector
            .getParticipantKind()))
      {
         String qualifiedId = participantInfo.getQualifiedId();
         QName qname = QName.valueOf(qualifiedId);
         final String modelId = qname.getNamespaceURI();
         IModel activeModel = null;
         if (StringUtils.isEmpty(modelId))
         {
            activeModel = context.getModelManager().findActiveModel();            
         }
         else
         {
            activeModel = context.getModelManager().findActiveModel(modelId);                        
         }

         // first try to resolve participant against active model
         participant = (null != activeModel) ? activeModel
               .findParticipant(participantInfo.getId()) : null;

         if (null == participant)
         {
            try
            {
               // if participant does not exist in active model, fall back to the first one
               // matching from any model
               participant = context.getModelManager().findModelParticipant(
                     (ModelParticipantInfo) participantInfo);
            }
            catch (ObjectNotFoundException x)
            {
               // if it cannot be found then participant shall stay null  
            }
         }
      }
      else if (PerformingParticipantFilter.FILTER_KIND_USER_GROUP.equals(collector
            .getParticipantKind()))
      {
         participant = UserGroupBean.findById(participantInfo.getId(), SecurityProperties
               .getPartitionOid());
      }
      else
      {
         trace.warn("Ignoring unknown worklist contributor: " + participantInfo.getId());
         participant = null;
      }

      final ParticipantInfo owner;
      if (participant instanceof IModelParticipant)
      {
         IDepartment department = DepartmentUtils.getDepartment(participantInfo);
         owner = DetailsFactory.createModelDetails((IModelParticipant) participant,
               department);
      }
      else if (participant instanceof IUserGroup)
      {
         owner = DetailsFactory.create(participant, IUserGroup.class,
               UserGroupInfoDetails.class);
      }
      else
      {
         owner = null;
      }

      if (null != owner)
      {
         return new ParticipantWorklist(owner, query, collector.subset,
               collector.retrievedItems, collector.hasMore, collector.hasTotalCount()
                     ? new Long(collector.getTotalCount())
                     : null, collector.getTotalCountThreshold());
      }
      else
      {
         return new ParticipantWorklist(participant.getId(), query, collector.subset,
               collector.retrievedItems, collector.hasMore, collector.hasTotalCount()
                     ? new Long(collector.getTotalCount())
                     : null, collector.getTotalCountThreshold());
      }
   }

   private boolean isCountOnly()
   {
      Iterator iter = new SplicingIterator(query.getModelParticipantContributions()
            .iterator(), query.getUserGroupContributions().iterator());
      iter = new SplicingIterator(iter, new OneElementIterator(query
            .getUserContribution()));

      boolean result = true;
      while (iter.hasNext())
      {
         final WorklistQuery.Contribution contrib = (WorklistQuery.Contribution) iter
               .next();
         if ( !isCountOnly(getContributionSubset(contrib)))
         {
            result = false;
            break;
         }
      }
      return result;
   }

   private static boolean isCountOnly(SubsetPolicy subset)
   {
      return null != subset && subset.getMaxSize() == 0
            && subset.isEvaluatingTotalCount();
   }

   private static <T> T[] assembleColumns(T[] baseCols, List< ? extends T> additionalCols)
   {
      ArrayList<T> result = CollectionUtils.newArrayList(baseCols.length
            + additionalCols.size());
      for (int i = 0; i < baseCols.length; i++)
      {
         result.add(baseCols[i]);
      }
   
      result.addAll(additionalCols);
      T[] target = (T[]) java.lang.reflect.Array.newInstance(baseCols.getClass()
            .getComponentType(), result.size());
      return result.toArray(target);
   }

   private static final class WorklistCollector
   {
      private final PerformingParticipantFilter.Kind participantKind;

      private final ParticipantInfo participant;

      private final SubsetPolicy subset;

      private List retrievedItems;

      private boolean hasMore;

      private Long totalCount;

      private long totalCountThreshold;

      public String toString()
      {
         return "Worklist: " + participant.getId() + " [" + subset + "]";
      }

      WorklistCollector(ParticipantInfo participant, SubsetPolicy subset)
      {
         this.participantKind = null;
         this.participant = participant;
         this.subset = subset;

         if ((null == subset)
               || SubsetPolicy.UNRESTRICTED.getMaxSize() == subset.getMaxSize())
         {
            retrievedItems = new ArrayList();
         }
         else
         {
            retrievedItems = new ArrayList(subset.getMaxSize());
         }
         hasMore = false;
      }

      WorklistCollector(PerformingParticipantFilter ownerFilter, SubsetPolicy subset)
      {
         this.participantKind = ownerFilter.getFilterKind();
         this.participant = ownerFilter.getParticipant();
         this.subset = subset;

         if ((null == subset)
               || SubsetPolicy.UNRESTRICTED.getMaxSize() == subset.getMaxSize())
         {
            retrievedItems = new ArrayList();
         }
         else
         {
            retrievedItems = new ArrayList(subset.getMaxSize());
         }
         hasMore = false;
      }

      public PerformingParticipantFilter.Kind getParticipantKind()
      {
         return participantKind;
      }

      public ParticipantInfo getParticipant()
      {
         return participant;
      }

      private void addAll(List source)
      {
         retrievedItems.addAll(source);
      }

      public void setTotalCount(long totalCount)
      {
         this.totalCount = new Long(totalCount);
      }

      public boolean hasTotalCount()
      {
         return null != totalCount;
      }

      public long getTotalCount()
      {
         if (null == totalCount)
         {
            throw new UnsupportedOperationException("Total item count not available");
         }
         return totalCount.longValue();
      }

      public void addToTotalCount(long count)
      {
         if ( !hasTotalCount())
         {
            totalCount = new Long(count);
         }
         else
         {
            totalCount = new Long(totalCount.longValue() + count);
         }
      }
      
      public void setTotalCountThreshold(long totalCountThreshold)
      {
         this.totalCountThreshold = totalCountThreshold;
      }
      
      public long getTotalCountThreshold()
      {
         return totalCountThreshold;
      }
   }

   private static final class ScopedParticipantInfo extends Pair<Long, Long> implements Comparable<ScopedParticipantInfo>
   {
      private static final long serialVersionUID = 1L;

      public ScopedParticipantInfo(Long participantOid, Long departmentOid)
      {
         super(participantOid, departmentOid);
      }

      public Long getParticipantOid()
      {
         return getFirst();
      }

      public Long getDepartmentOid()
      {
         return getSecond();
      }

      public int compareTo(ScopedParticipantInfo o)
      {
         int result = getParticipantOid().compareTo(o.getParticipantOid());

         if (result != 0)
         {
            return result;
         }
         else
         {
            return CompareHelper.compare(getDepartmentOid(), o.getDepartmentOid());
         }
      }
   }
}