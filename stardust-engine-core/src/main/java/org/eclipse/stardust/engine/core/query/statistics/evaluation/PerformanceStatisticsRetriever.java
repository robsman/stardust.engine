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
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.model.beans.ScopedModelParticipant;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.*;
import org.eclipse.stardust.engine.core.query.statistics.api.PerformanceStatistics.ModelParticipantPerformance;
import org.eclipse.stardust.engine.core.query.statistics.api.PerformanceStatistics.PerformanceDetails;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.query.statistics.utils.PkRegistry;
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
public class PerformanceStatisticsRetriever implements IUserQueryEvaluator
{

   public CustomUserQueryResult evaluateQuery(CustomUserQuery query)
   {
      if ( !(query instanceof PerformanceStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + PerformanceStatisticsQuery.class.getName());
      }

      final PerformanceStatisticsQuery woq = (PerformanceStatisticsQuery) query;

      final Users users = QueryServiceUtils.evaluateUserQuery(woq);

      final List<Long> userOids = CollectionUtils.newList();
      for (Iterator<User> i1 = users.iterator(); i1.hasNext();)
      {
         User user = i1.next();

         userOids.add(user.getOID());
      }
      final Map<ParticipantDepartmentPair, ModelParticipantPerformance> mpPerformance = CollectionUtils.newMap();

      final Date now = new Date();

      final Set<ParticipantDepartmentOidPair> mpRtOids = StatisticsQueryUtils
         .extractModelParticipantFilter(woq.getFilter());

      QueryDescriptor openAisQuery = QueryDescriptor
            .from(WorkItemBean.class)
            .select(new Column[] {
                  WorkItemBean.FR__ACTIVITY_INSTANCE,
                  ProcessInstanceBean.FR__OID,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF,
                  WorkItemBean.FR__MODEL,
                  ProcessInstanceBean.FR__PROCESS_DEFINITION,
                  WorkItemBean.FR__ACTIVITY,
                  ProcessInstanceBean.FR__START_TIME,
                  ProcessInstanceBean.FR__PRIORITY,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT,
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE
            });

      if ((null != mpRtOids) && !mpRtOids.isEmpty())
      {
         OrTerm orTerm = new OrTerm();
         for(ParticipantDepartmentOidPair pdPair : mpRtOids)
         {
            orTerm.add(Predicates.andTerm(
                  Predicates.isEqual(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND, PerformerType.MODEL_PARTICIPANT),
                  Predicates.isEqual(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF, pdPair.getParticipantOid()),
                  Predicates.isEqual(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT, pdPair.getDepartmentOid())));
         }
         openAisQuery.where(orTerm);
      }

      // join current AI history record to get access to "on behalf of" fields
      openAisQuery
            .innerJoin(ActivityInstanceHistoryBean.class)
            .on(WorkItemBean.FR__ACTIVITY_INSTANCE, ActivityInstanceHistoryBean.FIELD__ACTIVITY_INSTANCE)
            .andOn(WorkItemBean.FR__LAST_MODIFICATION_TIME, ActivityInstanceHistoryBean.FIELD__FROM);

      // join cumulation PI
      ProcessCumulationPolicy pcp = StatisticsQueryUtils.getProcessCumulationPolicy(woq);
      final FieldRef frCumulationPi = pcp.cumulateWithScopeProcess()
            ? WorkItemBean.FR__SCOPE_PROCESS_INSTANCE
            : pcp.cumulateWithRootProcess()
                  ? WorkItemBean.FR__ROOT_PROCESS_INSTANCE
                  : WorkItemBean.FR__PROCESS_INSTANCE;
      openAisQuery
            .innerJoin(ProcessInstanceBean.class)
            .on(frCumulationPi, ProcessInstanceBean.FIELD__OID);

      boolean singlePartition = Parameters.instance().getBoolean(
            KernelTweakingProperties.SINGLE_PARTITION, false);
      if (!singlePartition)
      {
         // restrict PIs to current partition
         openAisQuery.innerJoin(ModelPersistorBean.class) //
               .on(WorkItemBean.FR__MODEL, ModelPersistorBean.FIELD__OID)
               .andWhere(Predicates.isEqual( //
                     ModelPersistorBean.FR__PARTITION, //
                     SecurityProperties.getPartitionOid()));
      }

      final AuthorizationContext ctx = AuthorizationContext.create(ClientPermission.READ_ACTIVITY_INSTANCE_DATA);
      final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
            && !ctx.isAdminOverride();
      final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(ctx) {};

      authPredicate.addRawPrefetch(openAisQuery, WorkItemBean.FR__SCOPE_PROCESS_INSTANCE);

      StatisticsQueryUtils.executeQuery(openAisQuery, new IResultSetTemplate()
      {
         private final ModelManager modelManager = ModelManagerFactory.getCurrent();

         private final Calendar durationCalendar = Calendar.getInstance();

         private final PkRegistry openPis = new PkRegistry();
         private final PkRegistry openCriticalPis = new PkRegistry();

         private final CriticalExecutionTimePolicy criticalityPolicy = StatisticsQueryUtils.getCriticalExecutionTimePolicy(woq);

         private final Date tsPiStart = new Date();

         public void handleRow(ResultSet rs) throws SQLException
         {
            authPredicate.accept(rs);

            long aiOid = rs.getLong(1);
            long piOid = rs.getLong(2);
            int performerKind = rs.getInt(3);
            long performerOid = rs.getLong(4);
            long modelOid = rs.getLong(5);
            long processRtOid = rs.getLong(6);
            long activityRtOid = rs.getLong(7);
            long piStartTime = rs.getLong(8);
            int priority = rs.getInt(9);
            long department = rs.getLong(10);
            long scopePiOid = rs.getLong(11);

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
               final PerformerType onBehalfOfKind = PerformerType.get(performerKind);
               tsPiStart.setTime(piStartTime);

               IActivity activity = modelManager.findActivity(modelOid, activityRtOid);
               IProcessDefinition scopeProcess = modelManager.findProcessDefinition(modelOid, processRtOid);

               if (PerformerType.ModelParticipant.equals(onBehalfOfKind))
               {
                  IModelParticipant onBehalfOf = modelManager.findModelParticipant(modelOid, performerOid);
                  String qualifiedMPId = ModelUtils.getQualifiedId(onBehalfOf);

                  ParticipantDepartmentPair onBehalfOfPair =
                     new ParticipantDepartmentPair(qualifiedMPId, department);
                  ModelParticipantPerformance wo = mpPerformance.get(onBehalfOfPair);
                  if (wo == null)
                  {
                     IDepartment theDepartment = department != 0 ?
                           DepartmentBean.findByOID(department) : null;
                     ModelParticipantInfo mpi = (ModelParticipantInfo) DepartmentUtils
                           .getParticipantInfo(new ScopedModelParticipant(onBehalfOf,
                                 theDepartment), modelManager);
                     wo = new ModelParticipantPerformance(mpi);
                     mpPerformance.put(onBehalfOfPair, wo);
                  }

                  if (wo !=null)
                  {
                     updateWorklistOutline(wo, priority, aiOid, piOid, activity, scopeProcess);
                  }
               }
            }
         }

         private void updateWorklistOutline(PerformanceDetails wo, int priority, long aiOid,
               long piOid, IActivity activity, IProcessDefinition scopeProcess)
         {
            long targetExecutionTime = toMilliseconds(StatisticsModelUtils.getTargetExecutionTime(activity));

            wo.openActivities.registerInstance(priority, targetExecutionTime);

            if (openPis.registerPk(wo.openProcesses, piOid))
            {
               wo.openProcesses.registerInstance(priority);
            }

            if (criticalityPolicy.isCriticalDuration(priority, tsPiStart, now, scopeProcess))
            {
               wo.openCriticalActivities.registerInstance(priority, targetExecutionTime);

               if (openCriticalPis.registerPk(wo.openCriticalProcesses, piOid))
               {
                  wo.openCriticalProcesses.registerInstance(priority);
               }
            }
         }

         private long toMilliseconds(Period period)
         {
            return (null != period) //
                  ? StatisticsDateUtils.periodToDate(period, durationCalendar).getTime()
                  : 0l;
         }

      });

      final Date beginOfDay = StatisticsDateUtils.getBeginOfDay(now);

      QueryDescriptor completedAisQuery = QueryDescriptor
            .from(ActivityInstanceBean.class)
            .select(new Column[] {
                  ActivityInstanceBean.FR__OID,
                  ProcessInstanceBean.FR__OID,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF,
                  ActivityInstanceBean.FR__MODEL,
                  ActivityInstanceBean.FR__LAST_MODIFICATION_TIME,
                  ProcessInstanceBean.FR__PRIORITY,
                  ActivityInstanceBean.FR__ACTIVITY,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT,
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE
            })
            .where(
                  Predicates.andTerm(
                        Predicates.isEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.COMPLETED),
                        Predicates.greaterOrEqual(ActivityInstanceBean.FR__LAST_MODIFICATION_TIME, beginOfDay.getTime())));

      if ((null != mpRtOids) && !mpRtOids.isEmpty())
      {
         OrTerm orTerm = new OrTerm();
         for(ParticipantDepartmentOidPair pdPair : mpRtOids)
         {
            orTerm.add(Predicates.andTerm(
                  Predicates.isEqual(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND, PerformerType.MODEL_PARTICIPANT),
                  Predicates.isEqual(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF, pdPair.getParticipantOid()),
                  Predicates.isEqual(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT, pdPair.getDepartmentOid())));
         }
         ((AndTerm) completedAisQuery.getPredicateTerm()).add(orTerm);
      }

      // join current AI history record to get access to "on behalf of" fields
      completedAisQuery.innerJoin(ActivityInstanceHistoryBean.class) //
            .on(ActivityInstanceBean.FR__OID,
                  ActivityInstanceHistoryBean.FIELD__ACTIVITY_INSTANCE)
            .andOn(ActivityInstanceBean.FR__LAST_MODIFICATION_TIME,
                  ActivityInstanceHistoryBean.FIELD__UNTIL);

      // join cumulation PI
      Join piJoin = StatisticsQueryUtils.joinCumulationPi(woq, completedAisQuery,
            ActivityInstanceBean.FR__PROCESS_INSTANCE);

      if (!singlePartition)
      {
         // restrict PIs to current partition
         completedAisQuery.innerJoin(ModelPersistorBean.class) //
               .on(ActivityInstanceBean.FR__MODEL, ModelPersistorBean.FIELD__OID)
               .andWhere(Predicates.isEqual( //
                     ModelPersistorBean.FR__PARTITION, //
                     SecurityProperties.getPartitionOid()));
      }

      authPredicate.addRawPrefetch(completedAisQuery, piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE));

      StatisticsQueryUtils.executeQuery(completedAisQuery, new IResultSetTemplate()
      {
         private final ModelManager modelManager = ModelManagerFactory.getCurrent();

         private final PkRegistry completedPis = new PkRegistry();

         private final Date tsAiCompletion = new Date();

         public void handleRow(ResultSet rs) throws SQLException
         {
            authPredicate.accept(rs);

            long aiOid = rs.getLong(1);
            long piOid = rs.getLong(2);
            int performerKind = rs.getInt(3);
            long performerOid = rs.getLong(4);
            long modelOid = rs.getLong(5);
            long aiCompletionTime = rs.getLong(6);
            int priority = rs.getInt(7);
            long activityRtOid = rs.getLong(8);
            long department = rs.getLong(9);
            long scopePiOid = rs.getLong(10);

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
               final PerformerType onBehalfOfKind = PerformerType.get(performerKind);

               tsAiCompletion.setTime(aiCompletionTime);

               if (PerformerType.ModelParticipant.equals(onBehalfOfKind))
               {
                  IModelParticipant onBehalfOf = modelManager.findModelParticipant(modelOid,
                        performerOid);
                  String qualifiedMPId = ModelUtils.getQualifiedId(onBehalfOf);

                  ParticipantDepartmentPair onBehalfOfPair =
                     new ParticipantDepartmentPair(qualifiedMPId, department);
                  ModelParticipantPerformance wo = mpPerformance.get(onBehalfOfPair);
                  if(wo == null)
                  {
                     IDepartment theDepartment = department != 0 ?
                           DepartmentBean.findByOID(department) : null;
                     ModelParticipantInfo mpi = (ModelParticipantInfo) DepartmentUtils
                           .getParticipantInfo(new ScopedModelParticipant(onBehalfOf,
                                 theDepartment), modelManager);
                     wo = new ModelParticipantPerformance(mpi);
                     mpPerformance.put(onBehalfOfPair, wo);
                  }

                  updateWorklistOutline(wo, priority, aiOid, piOid);
               }
            }
         }

         private void updateWorklistOutline(PerformanceDetails wo, int priority,
               long aiOid, long piOid)
         {
            wo.completedActivities.registerInstance(priority);

            if (completedPis.registerPk(wo.completedProcesses, piOid))
            {
               wo.completedProcesses.registerInstance(priority);
            }
         }

      });

      // link users and roles

      // retrieve grants directly per SQL
      QueryDescriptor userStatusQuery = QueryDescriptor //
            .from(UserParticipantLink.class) //
            .select( //
                  UserParticipantLink.FR__USER, //
                  UserParticipantLink.FR__PARTICIPANT, //
                  UserSessionBean.FR__OID,
                  UserParticipantLink.FR__DEPARTMENT);

      if ((null != mpRtOids) && !mpRtOids.isEmpty())
      {
         OrTerm orTerm = new OrTerm();
         for(ParticipantDepartmentOidPair pdPair : mpRtOids)
         {
            orTerm.add(Predicates.andTerm(
                  Predicates.isEqual(UserParticipantLink.FR__PARTICIPANT, pdPair.getParticipantOid()),
                  Predicates.isEqual(UserParticipantLink.FR__DEPARTMENT, pdPair.getDepartmentOid())));
         }
         userStatusQuery.where(orTerm);
      }

      userStatusQuery
            .leftOuterJoin(UserSessionBean.class)
            .on(UserParticipantLink.FR__USER, UserSessionBean.FIELD__USER)
            .where(Predicates.andTerm(
                  Predicates.lessOrEqual(UserSessionBean.FR__START_TIME, now.getTime()),
                  Predicates.greaterOrEqual(UserSessionBean.FR__EXPIRATION_TIME, now.getTime())));

      if (!singlePartition)
      {
         // restrict PIs to current partition
         Join participantJoin = userStatusQuery.innerJoin(AuditTrailParticipantBean.class)
            .on(UserParticipantLink.FR__PARTICIPANT, AuditTrailParticipantBean.FIELD__OID);
         userStatusQuery.innerJoin(ModelPersistorBean.class)
            .on(participantJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), ModelPersistorBean.FIELD__OID)
            .andWhere(Predicates.isEqual(
                  ModelPersistorBean.FR__PARTITION,
                  SecurityProperties.getPartitionOid()));
      }

      // TODO: apply prefetching here as well?

      StatisticsQueryUtils.executeQuery(userStatusQuery, new IResultSetTemplate()
      {
         private final ModelManager modelManager = ModelManagerFactory.getCurrent();

         private final PkRegistry userRegistry = new PkRegistry();

         public void handleRow(ResultSet rs) throws SQLException
         {
            final long userOid = rs.getLong(1);
            final long participantRtOid = rs.getLong(2);
            final long departmentOid = rs.getLong(4);

            rs.getLong(3);
            final boolean loggedOn = !rs.wasNull();

            final IModelParticipant participant = modelManager.findModelParticipant(
                  PredefinedConstants.ANY_MODEL, participantRtOid);
            String qualifiedMPId = ModelUtils.getQualifiedId(participant);

            ParticipantDepartmentPair onBehalfOfPair =
               new ParticipantDepartmentPair(qualifiedMPId, departmentOid);
            ModelParticipantPerformance mpwo = mpPerformance.get(onBehalfOfPair);
            if(mpwo == null)
            {
               IDepartment department = departmentOid != 0 ?
                     DepartmentBean.findByOID(departmentOid) : null;
               ModelParticipantInfo mpi = (ModelParticipantInfo) DepartmentUtils
                     .getParticipantInfo(new ScopedModelParticipant(participant,
                           department), modelManager);
               mpwo = new ModelParticipantPerformance(mpi);
               mpPerformance.put(onBehalfOfPair, mpwo);
            }

            if (userRegistry.registerPk(mpwo, userOid))
            {
               mpwo.nUsers++;
               if (loggedOn)
               {
                  mpwo.nLoggedInUsers++;
               }
            }
         }
      });

      return new PerformanceStatisticsResult(woq, mpPerformance);
   }
}