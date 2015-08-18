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
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.*;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.PerformanceInInterval;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.PerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.*;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IUserQueryEvaluator;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public class UserPerformanceStatisticsRetriever implements IUserQueryEvaluator
{

   private PredicateTerm createDateRangeIntervalQuery(Set<DateRange> dateRanges)
   {
      PredicateTerm lhs = null;

      Iterator<DateRange> iterator = dateRanges.iterator();
      int i = 0;
      while (iterator.hasNext())
      {
         DateRange dateRange = iterator.next();

         PredicateTerm rhs = Predicates.andTerm(
               Predicates.isEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.COMPLETED),
               Predicates.greaterOrEqual(ActivityInstanceBean.FR__START_TIME, dateRange.getIntervalBegin().getTime()),
               Predicates.lessOrEqual(ActivityInstanceBean.FR__LAST_MODIFICATION_TIME, dateRange.getIntervalEnd().getTime()),
               // hint for database so that a range scan is used
               Predicates.greaterOrEqual(ActivityInstanceHistoryBean.FR__UNTIL, dateRange.getIntervalBegin().getTime()));

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


   public CustomUserQueryResult evaluateQuery(CustomUserQuery query)
   {
      if ( !(query instanceof UserPerformanceStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + UserPerformanceStatisticsQuery.class.getName());
      }

      final UserPerformanceStatisticsQuery wpq = (UserPerformanceStatisticsQuery) query;

      final StatisticsDateRangePolicy policy = (StatisticsDateRangePolicy) wpq.getPolicy(StatisticsDateRangePolicy.class);
      final StatisticsDateRangePolicy dateRangePolicy = policy != null
            ? policy
            : new StatisticsDateRangePolicy(Collections.singletonList(DateRange.TODAY));

      final Users users = QueryServiceUtils.evaluateUserQuery(wpq);

      // retrieve login times

      QueryDescriptor sqlQuery = QueryDescriptor.from(ActivityInstanceBean.class)
            .select(new Column[] {
                  ActivityInstanceBean.FR__PERFORMED_BY,
                  ActivityInstanceBean.FR__OID,
                  ProcessInstanceBean.FR__OID,
                  ActivityInstanceBean.FR__MODEL,
                  ActivityInstanceBean.FR__ACTIVITY,
                  ProcessInstanceBean.FR__PROCESS_DEFINITION,
                  ActivityInstanceBean.FR__START_TIME,
                  ActivityInstanceBean.FR__LAST_MODIFICATION_TIME,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT,
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE
            })
            .where(Predicates.andTerm(createDateRangeIntervalQuery(dateRangePolicy.getDateRanges()), Predicates.TRUE))
            .orderBy(
                  ActivityInstanceBean.FR__PERFORMED_BY,
                  ActivityInstanceBean.FR__START_TIME,
                  ActivityInstanceBean.FR__LAST_MODIFICATION_TIME);

      sqlQuery.innerJoin(ActivityInstanceHistoryBean.class)
            .on(ActivityInstanceBean.FR__OID,
                  ActivityInstanceHistoryBean.FIELD__ACTIVITY_INSTANCE)
            .andOn(ActivityInstanceBean.FR__LAST_MODIFICATION_TIME,
                  ActivityInstanceHistoryBean.FIELD__UNTIL);

      // retrieve root/scope process for aggregation
      Join piJoin = StatisticsQueryUtils.joinCumulationPi(wpq, sqlQuery,
            ActivityInstanceBean.FR__PROCESS_INSTANCE);

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
               ActivityInstanceBean.FR__PERFORMED_BY, userOids));
      }
      else
      {
         ((AndTerm) sqlQuery.getPredicateTerm()).add(Predicates.notEqual(
               ActivityInstanceBean.FR__PERFORMED_BY, 0));
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

      final AuthorizationContext ctx = AuthorizationContext.create(ClientPermission.READ_ACTIVITY_INSTANCE_DATA);
      final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
            && !ctx.isAdminOverride();
      final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(ctx) {};

      authPredicate.addRawPrefetch(sqlQuery, piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE));

      final Map<Long, Map<String, PerformanceStatistics>> statistics = CollectionUtils.newMap();
      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         private final ModelManager modelManager = ModelManagerFactory.getCurrent();

         private final Map<PerformanceInInterval, Set<Long>> visitedAis = CollectionUtils.newMap();
         private final Map<PerformanceInInterval, Set<Long>> visitedPis = CollectionUtils.newMap();

         private final Date tsCompleted = TimestampProviderUtils.getTimeStamp();

         public void handleRow(ResultSet rs) throws SQLException
         {
            authPredicate.accept(rs);

            long userOid = rs.getLong(1);
            long aiOid = rs.getLong(2);
            long piOid = rs.getLong(3);
            long modelOid = rs.getLong(4);
            long activityRtOid = rs.getLong(5);
            long targetProcessRtOid = rs.getLong(6);
            long aiCompleteTime = rs.getLong(8);
            int performerKind = rs.getInt(9);
            long performerOid = rs.getLong(10);
            long department = rs.getLong(11);
            long scopePiOid = rs.getLong(12);

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
               // TODO retrieve performance per process ID?
               IProcessDefinition targetProcess = modelManager.findProcessDefinition(modelOid, targetProcessRtOid);
               String qualifiedId = ModelUtils.getQualifiedId(targetProcess);

               tsCompleted.setTime(aiCompleteTime);

               PerformerType onBehalfOfKind = PerformerType.get(performerKind);

               IActivity activity = modelManager.findActivity(modelOid, activityRtOid);

               Map<String, PerformanceStatistics> userStatistics = statistics.get(userOid);
               if (null == userStatistics)
               {
                  userStatistics = CollectionUtils.newMap();
                  statistics.put(userOid, userStatistics);
               }

               PerformanceStatistics userPerformance = userStatistics.get(qualifiedId);
               if (null == userPerformance)
               {
                  userPerformance = new PerformanceStatistics(userOid);
                  userStatistics.put(qualifiedId, userPerformance);
               }

               ParticipantInfo onBehalfOf = DepartmentUtils.getParticipantInfo(
                     onBehalfOfKind, performerOid, department, modelOid);
               int index = userPerformance.getContributionIndex(onBehalfOf);
               Contribution contrib = null;
               if(index == -1)
               {
                  contrib = new Contribution(onBehalfOf);
                  userPerformance.contributions.add(contrib);
               }
               else
               {
                  contrib = userPerformance.contributions.get(index);
               }

               for (DateRange dateRange : dateRangePolicy.getDateRanges())
               {
                  addContributionForPeriod(dateRange, activity,
                        contrib, piOid, aiOid);
               }
            }
         }

         private void addContributionForPeriod(DateRange dateRange,
               IActivity activity, Contribution contribution, Long piOid, Long aiOid)
         {
            PerformanceInInterval performanceInInterval = contribution.getOrCreatePerformanceInInterval(dateRange);

            Date periodBegin = dateRange.getIntervalBegin();
            Date periodEnd = dateRange.getIntervalEnd();
            if (tsCompleted.after(periodBegin) && tsCompleted.before(periodEnd))
            {
               Set<Long> ais = visitedAis.get(contribution);
               if (null == ais)
               {
                  ais = CollectionUtils.newSet();
                  visitedAis.put(performanceInInterval, ais);
               }
               if ( !ais.contains(aiOid))
               {
                  performanceInInterval.addnAisCompleted(1);

                  ais.add(aiOid);
               }

               Set<Long> pis = visitedPis.get(performanceInInterval);
               if (null == pis)
               {
                  pis = CollectionUtils.newSet();
                  visitedPis.put(performanceInInterval, pis);
               }

               if ( !pis.contains(piOid))
               {
                  performanceInInterval.addnPisAffected(1);

                  pis.add(piOid);
               }
            }
         }

      });

      return new UserPerformanceStatisticsResult(wpq, users, statistics);
   }
}
