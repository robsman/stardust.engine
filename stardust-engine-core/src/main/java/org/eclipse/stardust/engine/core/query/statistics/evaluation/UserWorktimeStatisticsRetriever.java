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
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.AbstractStoplightPolicy.Status;
import org.eclipse.stardust.engine.core.query.statistics.api.*;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.ContributionInInterval;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.WorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.*;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IUserQueryEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class UserWorktimeStatisticsRetriever implements IUserQueryEvaluator
{
//   private static final Logger trace = LogManager.getLogger(UserWorktimeStatisticsRetriever.class);

   private static final String ATT_INCLUDE_TIME = "carnot:pwh:includeTime";
   public static final int MILLISECONDS_PER_MINUTE = 60 * 1000;

   public CustomUserQueryResult evaluateQuery(CustomUserQuery query)
   {
      if ( !(query instanceof UserWorktimeStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + UserWorktimeStatisticsQuery.class.getName());
      }

      final UserWorktimeStatisticsQuery wsq = (UserWorktimeStatisticsQuery) query;
      final StatisticsDateRangePolicy policy = (StatisticsDateRangePolicy) wsq.getPolicy(StatisticsDateRangePolicy.class);
      final StatisticsDateRangePolicy dateRangePolicy = policy != null
            ? policy
            : new StatisticsDateRangePolicy(Collections.singletonList(DateRange.TODAY));

      final Users users = QueryServiceUtils.evaluateUserQuery(wsq);


      // retrieve login times

            AndTerm inApplicationStateForUser = Predicates.andTerm(
               Predicates.isEqual(ActivityInstanceHistoryBean.FR__STATE, ActivityInstanceState.APPLICATION),
               Predicates.isEqual(ActivityInstanceHistoryBean.FR__PERFORMER_KIND, PerformerType.USER)
      );
            QueryDescriptor sqlQuery = QueryDescriptor.from(ActivityInstanceHistoryBean.class)
            .select(new Column[] {
                  ActivityInstanceHistoryBean.FR__USER,
                  ActivityInstanceBean.FR__OID,
                  ProcessInstanceBean.FR__OID,
                  ActivityInstanceBean.FR__MODEL,
                  ActivityInstanceBean.FR__ACTIVITY,
                  ProcessInstanceBean.FR__PROCESS_DEFINITION,
                  ActivityInstanceHistoryBean.FR__FROM,
                  ActivityInstanceHistoryBean.FR__UNTIL,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT,
                  ActivityInstanceHistoryBean.FR__STATE,
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE
            })
            .where(
                  Predicates.andTerm(
                        wsq.isCalculateWaitTime() ?
                        // include SUSPENDED state activity instance history entries
                        Predicates.orTerm(
                               inApplicationStateForUser,
                               Predicates.isEqual(ActivityInstanceHistoryBean.FR__STATE, ActivityInstanceState.SUSPENDED)
                               )
                        // (else) do not include SUSPENDED state activity instance history entries
                               : inApplicationStateForUser
                        ,
                        createDateRangeIntervalQuery(dateRangePolicy.getDateRanges())
                  )
            )

            .orderBy(
                  ActivityInstanceHistoryBean.FR__USER,
                  ActivityInstanceHistoryBean.FR__FROM,
                  ActivityInstanceHistoryBean.FR__UNTIL);

      sqlQuery.innerJoin(ActivityInstanceBean.class)
            .on(ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE,
                  ActivityInstanceBean.FIELD__OID);

      // retrieve root/scope process for aggregation
      Join piJoin = StatisticsQueryUtils.joinCumulationPi(wsq, sqlQuery,
            ActivityInstanceHistoryBean.FR__PROCESS_INSTANCE);

      // TODO configure this?
      if (users.size() <= 100)
      {
         final List<Long> userOids = CollectionUtils.newList();
         for (Iterator i1 = users.iterator(); i1.hasNext();)
         {
            User user = (User) i1.next();
            userOids.add(new Long(user.getOID()));
         }
         ((AndTerm) sqlQuery.getPredicateTerm()).add(Predicates.inList(
               ActivityInstanceHistoryBean.FR__USER, userOids));
      }

      boolean singlePartition = Parameters.instance().getBoolean(
            KernelTweakingProperties.SINGLE_PARTITION, false);
      if (!singlePartition)
      {
         sqlQuery.innerJoin(ModelPersistorBean.class) //
            .on(ActivityInstanceBean.FR__MODEL, ModelPersistorBean.FIELD__OID)
            .andWhere(Predicates.isEqual( //
                  ModelPersistorBean.FR__PARTITION, //
                  SecurityProperties.getPartitionOid()));
      }

      final Date tsFrom = new Date();
      final Date tsUntil = new Date();

      final Map<Long, WorktimeStatistics> worktimeStatistics = CollectionUtils.newMap();

      final Map<ContributionInInterval, Set<Long>> aisPerContribution = CollectionUtils.newMap();
      final Map<ContributionInInterval, Set<Long>> pisPerContribution = CollectionUtils.newMap();

      final Map<ContributionInInterval, Set<Long>> aisPerContributionWaiting = CollectionUtils.newMap();
      final Map<ContributionInInterval, Set<Long>> pisPerContributionWaiting = CollectionUtils.newMap();

      final Map<Long, EffortPerExecution> effortPerAi = CollectionUtils.newMap();
      final Map<Long, EffortPerExecution> effortPerPi = CollectionUtils.newMap();

      final AuthorizationContext ctx = AuthorizationContext.create(ClientPermission.READ_ACTIVITY_INSTANCE_DATA);
      final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
            && !ctx.isAdminOverride();
      final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(ctx) {};

      authPredicate.addRawPrefetch(sqlQuery, piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE));

      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         private final ModelManager modelManager = ModelManagerFactory.getCurrent();

         public void handleRow(ResultSet rs) throws SQLException
         {
            authPredicate.accept(rs);

            long userOid = rs.getLong(1);
            long aiOid = rs.getLong(2);
            long piOid = rs.getLong(3);
            long modelOid = rs.getLong(4);
            long activityRtOid = rs.getLong(5);
            long targetProcessRtOid = rs.getLong(6);
            long aiFromTime = rs.getLong(7);
            long aiUntilTime = rs.getLong(8);
            int performerKind = rs.getInt(9);
            long performerOid = rs.getLong(10);
            long department = rs.getLong(11);
            long aiState = rs.getLong(12);
            long scopePiOid = rs.getLong(13);

            long currentPerformer = 0;
            long currentUserPerformer = 0;
            switch (performerKind)
            {
            case PerformerType.USER:
               currentPerformer = 0;
               currentUserPerformer = performerOid;
               break;
            case PerformerType.USER_GROUP:
               currentPerformer = -performerOid;
               currentUserPerformer = 0;
               break;
            case PerformerType.MODEL_PARTICIPANT:
               currentPerformer = performerOid;
               currentUserPerformer = 0;
               break;
            }

            ctx.setActivityDataWithScopePi(scopePiOid, activityRtOid, modelOid,
                  currentPerformer, currentUserPerformer, department);
            if (!guarded || Authorization2.hasPermission(ctx))
            {
               WorktimeStatistics workTimes = worktimeStatistics.get(userOid);
               if (null == workTimes)
               {
                  workTimes = new WorktimeStatistics(userOid);
                  worktimeStatistics.put(userOid, workTimes);
               }

               tsFrom.setTime(aiFromTime);
               tsUntil.setTime(aiUntilTime);

               PerformerType onBehalfOfKind = PerformerType.get(performerKind);

               IActivity activity = (IActivity) ctx.getModelElement();

               Boolean includeTime = (Boolean) activity.getAttribute(ATT_INCLUDE_TIME);
               if (includeTime == null || includeTime.booleanValue())
               {
                  IProcessDefinition cumulationProcess = modelManager
                        .findProcessDefinition(modelOid, targetProcessRtOid);

                  IParticipant onBehalfOf = PerformerUtils.decodePerformer(onBehalfOfKind,
                        performerOid, ctx.getModels().get(0));

                  final float costPerMinute = StatisticsModelUtils
                        .getCostPerMinute(onBehalfOf);

                  ParticipantInfo onBehalfOfParticipant = DepartmentUtils.getParticipantInfo(
                        onBehalfOfKind, performerOid, department, modelOid);

                  String elementId = ModelUtils.getQualifiedId(cumulationProcess);
                  int index = workTimes.getContributionIndex(elementId, onBehalfOfParticipant);
                  Contribution contrib = null;
                  if(index == -1)
                  {
                     contrib = new Contribution(elementId, onBehalfOfParticipant);
                     workTimes.contributions.add(contrib);
                  }
                  else
                  {
                     contrib = workTimes.contributions.get(index);
                  }

                  if (ActivityInstanceState.APPLICATION == aiState)
                  {
                     // The ai state APPLICATION is used for most calculations.
                     registerEffort(aiOid, activity, effortPerAi, tsFrom, tsUntil,
                           costPerMinute);
                     registerEffort(piOid, cumulationProcess, effortPerPi, tsFrom,
                           tsUntil, costPerMinute);

                     for (DateRange dateRange : dateRangePolicy.getDateRanges())
                     {
                        addContributionForPeriod(piOid, aiOid, aiState, activity,
                              dateRange, costPerMinute, contrib);
                     }
                  }
                  else if (ActivityInstanceState.SUSPENDED == aiState)
                  {
                     // The ai state SUSPENDED is only used for wait time
                     for (DateRange dateRange : dateRangePolicy.getDateRanges())
                     {
                        addWaitTimeContribution(piOid, aiOid, aiState, activity, dateRange,
                              costPerMinute, contrib);
                     }
                  }
               }
            }
         }

         private void addWaitTimeContribution(long piOid, long aiOid, long aiState,
               IActivity activity, DateRange dateRange, float costPerMinute,
               Contribution contribution)
         {
            ContributionInInterval contributionInInterval = contribution.getOrCreateContributionInInterval(dateRange);

            Date periodBegin = dateRange.getIntervalBegin();
            Date periodEnd = dateRange.getIntervalEnd();

            // Interpret non set until timestamp (with 0 milis) as Now for activities that are currently suspended.
            Date tsUntilWait = tsUntil.equals(new Date(0l))? new Date():tsUntil;

            if (tsUntilWait.after(periodBegin) && tsFrom.before(periodEnd))
            {
               Set<Long> ais = aisPerContributionWaiting.get(contributionInInterval);
               if (null == ais)
               {
                  ais = CollectionUtils.newSet();
                  aisPerContributionWaiting.put(contributionInInterval, ais);
               }
               if ( !ais.contains(aiOid))
               {
                  ais.add(aiOid);
                  contributionInInterval.addnAisWaiting(1);
               }

               Set<Long> pis = pisPerContributionWaiting.get(contributionInInterval);
               if (null == pis)
               {
                  pis = CollectionUtils.newSet();
                  pisPerContributionWaiting.put(contributionInInterval, pis);
               }
               if ( !pis.contains(piOid))
               {
                  pis.add(piOid);
                  contributionInInterval.addnPisWaiting(1);
               }


               Date from = tsFrom.before(periodBegin) ? periodBegin : tsFrom;
               Date until = tsUntilWait.after(periodEnd) ? periodEnd : tsUntilWait;

               final long duration = until.getTime() - from.getTime();

               contributionInInterval.addTimeWaiting(duration);
            }
         }

         private void addContributionForPeriod(Long cumulationPiOid, Long aiOid,
               long aiState, IActivity activity, DateRange dateRange, float costPerMinute, Contribution contribution)
         {
            ContributionInInterval contributionInInterval = contribution.getOrCreateContributionInInterval(dateRange);

            Date periodBegin = dateRange.getIntervalBegin();
            Date periodEnd = dateRange.getIntervalEnd();
            if (tsUntil.after(periodBegin) && tsFrom.before(periodEnd))
            {
               Set<Long> ais = aisPerContribution.get(contributionInInterval);
               if (null == ais)
               {
                  ais = CollectionUtils.newSet();
                  aisPerContribution.put(contributionInInterval, ais);
               }
               if ( !ais.contains(aiOid))
               {
                  ais.add(aiOid);
                  contributionInInterval.addnAis(1);
               }

               Set<Long> pis = pisPerContribution.get(contributionInInterval);
               if (null == pis)
               {
                  pis = CollectionUtils.newSet();
                  pisPerContribution.put(contributionInInterval, pis);
               }
               if ( !pis.contains(cumulationPiOid))
               {
                  pis.add(cumulationPiOid);
                  contributionInInterval.addnPis(1);
               }

               Date from = tsFrom.before(periodBegin) ? periodBegin : tsFrom;
               Date until = tsUntil.after(periodEnd) ? periodEnd : tsUntil;

               final long duration = until.getTime() - from.getTime();

               contributionInInterval.addTimeSpent(duration);

               // TODO add cost
               if (0.0f < costPerMinute)
               {
                  double durationInMinutes = (double) duration / MILLISECONDS_PER_MINUTE;

                  contributionInInterval.addCost(durationInMinutes * costPerMinute);
               }
            }
         }
      });

      // TODO rate AIs/PIs per processing time/cost

//      CriticalProcessingTimePolicy cptp = StatisticsQueryUtils.getCriticalProcessingTimePolicy(wsq);
//      CriticalCostPerExecutionPolicy ccpep = StatisticsQueryUtils.getCriticalCostPerExecutionPolicy(wsq);

      InstancesStoplightHistogram criticalAisByProcessingTime = new InstancesStoplightHistogram();
      InstancesStoplightHistogram criticalAisByExecutionCost = new InstancesStoplightHistogram();

      findCriticalEfforts(effortPerAi, wsq, criticalAisByProcessingTime,
            criticalAisByExecutionCost);

      InstancesStoplightHistogram criticalPisByProcessingTime = new InstancesStoplightHistogram();
      InstancesStoplightHistogram criticalPisByExecutionCost = new InstancesStoplightHistogram();

      findCriticalEfforts(effortPerPi, wsq, criticalPisByProcessingTime,
            criticalPisByExecutionCost);

      registerCriticalInstances(aisPerContribution, criticalAisByProcessingTime,
            criticalAisByExecutionCost);

      registerCriticalInstances(pisPerContribution, criticalPisByProcessingTime,
            criticalPisByExecutionCost);

      return new UserWorktimeStatisticsResult(wsq, users, worktimeStatistics);
   }

   private PredicateTerm createDateRangeIntervalQuery(Set<DateRange> dateRanges)
   {
      PredicateTerm lhs = null;
      Iterator<DateRange> iterator = dateRanges.iterator();
      int i = 0;
      while (iterator.hasNext())
      {
         DateRange dateRange = iterator.next();

         PredicateTerm rhs = Predicates.andTerm(
               Predicates.lessOrEqual(ActivityInstanceHistoryBean.FR__FROM,
                     dateRange.getIntervalEnd().getTime()), Predicates.orTerm(
                     Predicates.isEqual(ActivityInstanceHistoryBean.FR__UNTIL, 0),
                     Predicates.greaterOrEqual(ActivityInstanceHistoryBean.FR__UNTIL,
                           dateRange.getIntervalBegin().getTime())));

         if (i == 0)
         {
            lhs = rhs;
         }
         else
         {
            lhs = Predicates.orTerm(lhs, rhs);
         }

         i++;
      }
      return lhs;

   }

   private static void registerEffort(Long instanceOid, ModelElement definition,
         Map<Long, EffortPerExecution> effortRegistry, Date tsFrom, Date tsUntil, float costPerMinute)
   {
      EffortPerExecution effort = (EffortPerExecution) effortRegistry.get(instanceOid);
      if (null == effort)
      {
         effort = new EffortPerExecution(instanceOid.longValue(), definition);
         effortRegistry.put(instanceOid, effort);
      }

      final long duration = tsUntil.getTime() - tsFrom.getTime();

      effort.timeSpent += duration;

      if (0.0f < costPerMinute)
      {
         double durationInMinutes = (double) duration / MILLISECONDS_PER_MINUTE;
         effort.cost += (durationInMinutes * costPerMinute);
      }
   }

   private static void findCriticalEfforts(Map<Long, EffortPerExecution> effortRegistry,
         UserWorktimeStatisticsQuery query,
         InstancesStoplightHistogram criticalByProcessingTime,
         InstancesStoplightHistogram criticalByExecutionCost)
   {
      CriticalProcessingTimePolicy cptp = StatisticsQueryUtils.getCriticalProcessingTimePolicy(query);
      CriticalCostPerExecutionPolicy ccpep = StatisticsQueryUtils.getCriticalCostPerExecutionPolicy(query);

      for (Iterator<EffortPerExecution> i = effortRegistry.values().iterator(); i.hasNext();)
      {
         EffortPerExecution effort = i.next();

         Status cptpStatus = cptp.rateDuration(effort.timeSpent, effort.definition);
         criticalByProcessingTime.registerInstance(cptpStatus, effort.oid);

         Status ccpepStatus = ccpep.rateCost(effort.cost, effort.definition);
         criticalByExecutionCost.registerInstance(ccpepStatus, effort.oid);
      }
   }

   private static void registerCriticalInstances(
         Map<ContributionInInterval, Set<Long>> instancesPerContribution,
         InstancesStoplightHistogram criticalByProcessingTime,
         InstancesStoplightHistogram criticalByExecutionCost)
   {
      for(Map.Entry<ContributionInInterval, Set<Long>> mapEntry : instancesPerContribution.entrySet())
      {
         ContributionInInterval contribution = mapEntry.getKey();
         Set<Long> instances = mapEntry.getValue();

         Set<Long> yellowByTime = CollectionUtils.copySet(instances);
         yellowByTime.retainAll(criticalByProcessingTime.getYellowInstances());
         contribution.getCriticalByProcessingTime().registerYellowInstances(yellowByTime);

         Set<Long> redByTime = CollectionUtils.copySet(instances);
         redByTime.retainAll(criticalByProcessingTime.getRedInstances());
         contribution.getCriticalByProcessingTime().registerRedInstances(redByTime);

         Set<Long> yellowByCost = CollectionUtils.copySet(instances);
         yellowByCost.retainAll(criticalByExecutionCost.getYellowInstances());
         contribution.getCriticalByExecutionCost().registerYellowInstances(yellowByCost);

         Set<Long> redByCost = CollectionUtils.copySet(instances);
         redByCost.retainAll(criticalByExecutionCost.getRedInstances());
         contribution.getCriticalByExecutionCost().registerRedInstances(redByCost);
      }
   }

   private static class EffortPerExecution
   {
      public final long oid;

      public final ModelElement definition;

      private long timeSpent = 0l;

      private float cost = 0.0f;

      public EffortPerExecution(long oid, ModelElement definition)
      {
         this.oid = oid;
         this.definition = definition;
      }
   }
}
