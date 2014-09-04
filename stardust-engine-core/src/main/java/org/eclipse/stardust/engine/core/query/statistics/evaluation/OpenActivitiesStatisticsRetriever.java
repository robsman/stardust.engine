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
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics.OpenActivities;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics.OpenActivitiesDetails;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ModelPersistorBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.AbstractAuthorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.AuthorizationContext;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IActivityInstanceQueryEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class OpenActivitiesStatisticsRetriever implements IActivityInstanceQueryEvaluator
{

   public CustomActivityInstanceQueryResult evaluateQuery(CustomActivityInstanceQuery query)
   {
      if ( !(query instanceof OpenActivitiesStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + OpenActivitiesStatisticsQuery.class.getName());
      }

      final OpenActivitiesStatisticsQuery oasq = (OpenActivitiesStatisticsQuery) query;

      final Date now = new Date();
      final long nowInMilli = now.getTime();
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(now);
      setTimeToDayEnd(cal);
      
      // history starts one month back
      int daysOfHistory = calculateHistoryDays(cal);
      final List<Long> historyTimestamps = CollectionUtils.newList();
      for(int i=0; i<daysOfHistory; i++)
      {
         cal.add(Calendar.DAY_OF_MONTH, -1);
         historyTimestamps.add(new Long(cal.getTimeInMillis()));
      }
      
      
      Date intervalStart = cal.getTime();

      QueryDescriptor sqlQuery = QueryDescriptor.from(ActivityInstanceHistoryBean.class) //
            .select(new Column[] {
                  ProcessInstanceBean.FR__OID,
                  ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE,
                  ActivityInstanceHistoryBean.FR__FROM,
                  ActivityInstanceHistoryBean.FR__UNTIL,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF,
                  ProcessInstanceBean.FR__MODEL,
                  ProcessInstanceBean.FR__PROCESS_DEFINITION,
                  ProcessInstanceBean.FR__START_TIME,
                  ProcessInstanceBean.FR__PRIORITY,
                  ActivityInstanceBean.FR__ACTIVITY,
                  ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT,
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
                  ActivityInstanceBean.FR__STATE
                  })
            .where(Predicates.andTerm(
                  Predicates.notEqual(ActivityInstanceHistoryBean.FR__PERFORMER, 0l),
                  Predicates.lessOrEqual(ActivityInstanceHistoryBean.FR__FROM, nowInMilli),
                  Predicates.orTerm(
                        Predicates.isEqual(ActivityInstanceHistoryBean.FR__UNTIL, 0l),
                        Predicates.greaterOrEqual(ActivityInstanceHistoryBean.FR__UNTIL, intervalStart.getTime()))
                  ))
            .orderBy(ActivityInstanceHistoryBean.FR__FROM, ActivityInstanceHistoryBean.FR__UNTIL);

      sqlQuery.innerJoin(ActivityInstanceBean.class)
            .on(ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE,
                  ActivityInstanceBean.FIELD__OID);

      // join PI
      Join piJoin = StatisticsQueryUtils.joinCumulationPi(oasq, sqlQuery,
            ActivityInstanceHistoryBean.FR__PROCESS_INSTANCE);

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

      final Set<Long> processRtOidFilter = StatisticsQueryUtils.extractProcessFilter(oasq.getFilter());
      if (null != processRtOidFilter)
      {
         ((AndTerm) sqlQuery.getPredicateTerm()).add(Predicates.inList(
               ProcessInstanceBean.FR__PROCESS_DEFINITION, processRtOidFilter.iterator()));
      }

      final OpenActivitiesStatisticsResult result = new OpenActivitiesStatisticsResult(oasq,
            historyTimestamps.size());

      final CriticalExecutionTimePolicy criticalityPolicy = StatisticsQueryUtils.getCriticalExecutionTimePolicy(oasq);

      final AuthorizationContext ctx = AuthorizationContext.create(WorkflowService.class,
            "getActivityInstance", long.class);
      final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
            && !ctx.isAdminOverride();
      final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(ctx) {};
      
      authPredicate.addRawPrefetch(sqlQuery, piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE));
            
      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         private final ModelManager modelManager = ModelManagerFactory.getCurrent();

         private final Map<OpenActivitiesDetails, PiRegistry> pendingPisPool = CollectionUtils.newMap();
         private final Map<OpenActivitiesDetails, PiRegistry> criticalPisPool = CollectionUtils.newMap();

         private long tsFrom;
         private long tsUntil;

         private final Date tsPiStart = new Date();

         private final Long[] history = (Long[]) historyTimestamps.toArray(new Long[0]);

         public void handleRow(ResultSet rs) throws SQLException
         {
            authPredicate.accept(rs);
            
            Long piOid = rs.getLong(1);
            long fromTime = rs.getLong(3);
            long untilTime = rs.getLong(4);
            int performerKind = rs.getInt(5);
            long performerOid = rs.getLong(6);
            long modelOid = rs.getLong(7);
            long cumulationProcessRtOid = rs.getLong(8);
            long piStartTime = rs.getLong(9);
            int priority = rs.getInt(10);
            long activityRtOid = rs.getLong(11);
            long department = rs.getLong(12);
            long scopePiOid = rs.getLong(13);
            int state = rs.getInt(14);
            
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

               tsFrom = fromTime;
               tsUntil = untilTime;

               IProcessDefinition cumulationProcess = modelManager.findProcessDefinition(modelOid, cumulationProcessRtOid);
               String qualifiedId = ModelUtils.getQualifiedId(cumulationProcess);                  

               tsPiStart.setTime(piStartTime);

               ParticipantInfo performer = DepartmentUtils.getParticipantInfo(
                     PerformerType.get(performerKind), performerOid, department, modelOid);
               
               OpenActivities oa = result.findOpenActivities(qualifiedId, performer);
               OpenActivitiesDetails priorityRecord = oa.getDetailsForPriority(priority);

               boolean isHibernated = false;
               if (state == ActivityInstanceState.HIBERNATED)
               {
                  isHibernated = true;
               }
               
               updateStatistics(priorityRecord, piOid, cumulationProcess, isHibernated);
            }
         }

         private void updateStatistics(OpenActivitiesDetails oad, Long cumulationPiOid,
               IProcessDefinition cumulationProcess, boolean isHibernated)
         {
            for (int i = history.length - 1; i >= 0; i--)
            {
               long dayEnd = history[i].longValue();
               if (tsFrom < dayEnd && (tsUntil == 0 || tsUntil > dayEnd))
               {
                  // AI was pending at this point in time
                  oad.pendingAisHistory[i]++;
                  if (!registerNewPi(dayEnd, cumulationPiOid, oad, pendingPisPool))
                  {
                     oad.pendingPisHistory[i]++;
                  }

                  if (criticalityPolicy.isCriticalDuration(oad.priority, tsPiStart,
                        new Date(dayEnd), cumulationProcess))
                  {
                     oad.pendingCriticalAisHistory[i]++;

                     if (!registerNewPi(dayEnd, cumulationPiOid, oad, criticalPisPool))
                     {
                        oad.pendingCriticalPisHistory[i]++;
                     }
                  }
               }
               
            }

            if (tsFrom < nowInMilli && ((0 == tsUntil) || !(tsUntil < nowInMilli)))
            {
               oad.pendingAis++;

               if (isHibernated)
               {
                  oad.hibernatedAis++;
               }
               if (registerNewPi(nowInMilli, cumulationPiOid, oad, pendingPisPool))
               {
                  oad.pendingPis++;
               }

               if (criticalityPolicy.isCriticalDuration(oad.priority, tsPiStart, now,
                     cumulationProcess))
               {
                  oad.pendingCriticalAis++;
                  if (registerNewPi(nowInMilli, cumulationPiOid, oad, criticalPisPool))
                  {
                     oad.pendingPis++;
                  }
               }
            }                      
         }
      });

      return result;
   }

   private static boolean registerNewPi(long ts, Long piOid, OpenActivitiesDetails oad,
         Map<OpenActivitiesDetails, PiRegistry> registryPool)
   {
      PiRegistry registry = registryPool.get(oad);
      if (null == registry)
      {
         registry = new PiRegistry();
         registryPool.put(oad, registry);
      }

      return registry.registerNewPi(ts, piOid);
   }

   private static class PiRegistry
   {
      private Map<Long, Set<Long>> visitedPis = CollectionUtils.newMap();

      public boolean registerNewPi(long timestamp, Long piOid)
      {
         Long ts = new Long(timestamp);
         Set<Long> visitedPis = this.visitedPis.get(ts);
         if (null == visitedPis)
         {
            visitedPis = CollectionUtils.newSet();
            this.visitedPis.put(ts, visitedPis);
         }

         boolean foundPi = visitedPis.contains(piOid);
         if ( !foundPi)
         {
            visitedPis.add(piOid);
            return true;
         }
         return false;
      }
   }
   
   private int calculateHistoryDays(Calendar now)
   {
      Calendar start = (Calendar) now.clone();
      start.add(Calendar.MONTH, -1);
      
      int daysBetween = 0;
      while (start.before(now))
      {
         start.add(Calendar.DAY_OF_MONTH, 1);
         daysBetween++;
      }
      return daysBetween;
      
   }
   
   private void setTimeToDayEnd(Calendar cal)
   {
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH);
      int day = cal.get(Calendar.DAY_OF_MONTH);
      cal.set(year, month, day, 23, 59, 59);
      cal.set(Calendar.MILLISECOND, 999);
   }
}
