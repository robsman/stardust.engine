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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.Participation;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.PostponedActivities;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.PostponedActivityDetails;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.query.statistics.utils.PkRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.AbstractAuthorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.AuthorizationContext;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IUserQueryEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class PostponedActivitiesStatisticsRetriever implements IUserQueryEvaluator
{

   private static final String EVENT_HANDLER_RESUBMISSION = "Resubmission";
   public static final int MILLISECONDS_PER_MINUTE = 60 * 1000;

   public CustomUserQueryResult evaluateQuery(CustomUserQuery query)
   {
      if ( !(query instanceof PostponedActivitiesStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + PostponedActivitiesStatisticsQuery.class.getName());
      }

      final PostponedActivitiesStatisticsQuery pasq = (PostponedActivitiesStatisticsQuery) query;

      final Users users = QueryServiceUtils.evaluateUserQuery(pasq);

      // retrieve login times

      final Date now = new Date();

      // TODO find all wiedervorlage event RT OIDs
      
      QueryDescriptor sqlQuery = QueryDescriptor.from(ActivityInstanceBean.class)
            .select(new Column[] {
                  ActivityInstanceBean.FR__MODEL,
                  EventBindingBean.FR__HANDLER_OID,
                  ActivityInstanceBean.FR__CURRENT_USER_PERFORMER,
                  ActivityInstanceBean.FR__OID,
                  ProcessInstanceBean.FR__OID,
                  ActivityInstanceBean.FR__ACTIVITY,
                  ProcessInstanceBean.FR__PROCESS_DEFINITION,
                  ActivityInstanceBean.FR__START_TIME,
                  ProcessInstanceBean.FR__START_TIME,
                  ProcessInstanceBean.FR__PRIORITY,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT,
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE
            })
            .where(Predicates.andTerm(
                  Predicates.isEqual(ActivityInstanceBean.FR__STATE, ActivityInstanceState.HIBERNATED),
                  Predicates.isEqual(EventBindingBean.FR__TYPE, Event.ACTIVITY_INSTANCE)))
            .orderBy( //
                  ActivityInstanceBean.FR__CURRENT_USER_PERFORMER,
                  ActivityInstanceBean.FR__START_TIME);

      sqlQuery.innerJoin(ActivityInstanceHistoryBean.class)
            .on(ActivityInstanceBean.FR__OID,
                  ActivityInstanceHistoryBean.FIELD__ACTIVITY_INSTANCE)
            .andOn(ActivityInstanceBean.FR__LAST_MODIFICATION_TIME,
                  ActivityInstanceHistoryBean.FIELD__FROM);
      
      sqlQuery.innerJoin(EventBindingBean.class)
            .on(ActivityInstanceBean.FR__OID, EventBindingBean.FIELD__OBJECT_OID);
      
      boolean singlePartition = Parameters.instance().getBoolean(
            KernelTweakingProperties.SINGLE_PARTITION, false);
      if (!singlePartition)
      {
         // restrict PIs to current partition
         sqlQuery.innerJoin(ModelPersistorBean.class) //
               .on(ActivityInstanceBean.FR__MODEL, ModelPersistorBean.FIELD__OID)
               .andWhere(Predicates.isEqual( //
                     ModelPersistorBean.FR__PARTITION, //
                     SecurityProperties.getPartitionOid()));
      }

      // retrieve root/scope process for aggregation
      Join piJoin = StatisticsQueryUtils.joinCumulationPi(pasq, sqlQuery,
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
               ActivityInstanceBean.FR__CURRENT_USER_PERFORMER, userOids));
      }
      else
      {
         ((AndTerm) sqlQuery.getPredicateTerm()).add(Predicates.notEqual(
               ActivityInstanceBean.FR__CURRENT_USER_PERFORMER, 0));
      }

      // TODO implement
      final CriticalExecutionTimePolicy criticalityPolicy = StatisticsQueryUtils.getCriticalExecutionTimePolicy(pasq);

      final AuthorizationContext ctx = AuthorizationContext.create(WorkflowService.class,
            "getActivityInstance", long.class);
      final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
            && !ctx.isAdminOverride();
      final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(ctx) {};
      
      authPredicate.addRawPrefetch(sqlQuery, piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE));

      final Map<Long, PostponedActivities> performanceStatistics = CollectionUtils.newMap();
      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         private final ModelManager modelManager = ModelManagerFactory.getCurrent();

         private final PkRegistry visitedAis = new PkRegistry();
//         private final PkRegistry visitedPis = new PkRegistry();

         private final Date tsAiStarted = new Date();
         private final Date tsPiStarted = new Date();

         public void handleRow(ResultSet rs) throws SQLException
         {
            authPredicate.accept(rs);
            
            long modelOid = rs.getLong(1);
            long eventHandlerRtOid = rs.getLong(2);
            long userOid = rs.getLong(3);
            long aiOid = rs.getLong(4);
            long piOid = rs.getLong(5);
            long activityRtOid = rs.getLong(6);
            long targetProcessRtOid = rs.getLong(7);
            long aiStartTime = rs.getLong(8);
            long piStartTime = rs.getLong(9);
            int targetProcessPriority = rs.getInt(10);
            int performerKind = rs.getInt(11);
            long performerOid = rs.getLong(12);
            long department = rs.getLong(13);
            long scopePiOid = rs.getLong(14);

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

            IEventHandler activeHandler = modelManager.findEventHandler(modelOid, eventHandlerRtOid);

            if (PredefinedConstants.TIMER_CONDITION.equals(activeHandler.getType().getId())
                  && EVENT_HANDLER_RESUBMISSION.equals(activeHandler.getId()))
            {
               ctx.setActivityDataWithScopePi(scopePiOid, activityRtOid, modelOid,
                     currentPerformer, currentUserPerformer, department);
               if (!guarded || Authorization2.hasPermission(ctx))
               {

                  PostponedActivities userPerformance = (PostponedActivities) performanceStatistics.get(userOid);
                  if (null == userPerformance)
                  {
                     userPerformance = new PostponedActivities(userOid);
                     performanceStatistics.put(userOid, userPerformance);
                  }

                  tsAiStarted.setTime(aiStartTime);
                  tsPiStarted.setTime(piStartTime);
   
                  IProcessDefinition targetProcess = modelManager.findProcessDefinition(modelOid, targetProcessRtOid);
                  String qualifiedId = ModelUtils.getQualifiedId(targetProcess);                  

                  PerformerType onBehalfOfKind = PerformerType.get(performerKind);
                  ParticipantInfo onBehalfOf = DepartmentUtils.getParticipantInfo(
                        onBehalfOfKind, performerOid, department, modelOid);
                  Participation contrib = null;
                  int index = userPerformance.getParticipationIndex(qualifiedId, onBehalfOf);
                  List<Participation> participations = userPerformance.participationsPerProcess.get(qualifiedId);
                  if(index == -1)
                  {
                     contrib = new Participation(qualifiedId, onBehalfOf, onBehalfOfKind, performerOid);
                     participations.add(contrib);
                  }
                  else
                  {
                     contrib = participations.get(index);
                  }
   
                  List<PostponedActivityDetails> postponedAis = contrib.getDetailsForPriority(targetProcessPriority);
   
                  // any AI may have multiple event bindings
                  if (visitedAis.registerPk(this, aiOid))
                  {
                     PostponedActivityDetails newEntry = new PostponedActivityDetails(
                           targetProcessPriority, aiOid, piOid, tsAiStarted, tsPiStarted);
   
                     postponedAis.add(newEntry);
                     if (criticalityPolicy.isCriticalDuration(targetProcessPriority,
                           tsPiStarted, now, targetProcess))
                     {
                        List<PostponedActivityDetails> postponedCriticalAis = 
                           contrib.getCriticalDetailsForPriority(targetProcessPriority);
                        postponedCriticalAis.add(newEntry);
                     }
                  }
               }
            }
         }
      });

      return new PostponedActivitiesStatisticsResult(pasq, users, performanceStatistics);
   }
}
