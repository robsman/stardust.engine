/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.utils.AbstractAuthorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.AuthorizationContext;
import org.eclipse.stardust.engine.core.runtime.utils.ClientPermission;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IActivityInstanceQueryEvaluator;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkActivityStatisticsRetriever implements IActivityInstanceQueryEvaluator
{
   private static Column[] columns = {
      ActivityInstanceBean.FR__OID,
      ActivityInstanceBean.FR__MODEL,
      ActivityInstanceBean.FR__ACTIVITY,
      ActivityInstanceBean.FR__BENCHMARK_VALUE,
      ProcessInstanceBean.FR__BENCHMARK_OID,
      ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
      ActivityInstanceBean.FR__CURRENT_PERFORMER,
      ActivityInstanceBean.FR__CURRENT_USER_PERFORMER,
      ActivityInstanceBean.FR__CURRENT_DEPARTMENT
   };

   public CustomActivityInstanceQueryResult evaluateQuery(
         CustomActivityInstanceQuery query)
   {
      if (!(query instanceof BenchmarkActivityStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + BenchmarkActivityStatisticsQuery.class.getName());
      }

      final BenchmarkActivityStatisticsQuery asq = (BenchmarkActivityStatisticsQuery) query;
      final BenchmarkActivityStatisticsResult result = new BenchmarkActivityStatisticsResult(asq);

      final Set<Long> processRtOidFilter = StatisticsQueryUtils.extractProcessFilter(asq
            .getFilter());
      for (Iterator<List<Long>> iterator = getSubLists(processRtOidFilter, 100)
            .iterator(); iterator.hasNext();)
      {
         List<Long> processRtOidSubList = iterator.next();

         QueryDescriptor sqlQuery = QueryDescriptor.from(ActivityInstanceBean.class) //
               .select(columns);

         // join PI
         Join piJoin = sqlQuery.innerJoin(ProcessInstanceBean.class) //
               .on(ActivityInstanceBean.FR__PROCESS_INSTANCE,
                     ProcessInstanceBean.FIELD__OID).where(
                     Predicates.inList(ProcessInstanceBean.FR__PROCESS_DEFINITION,
                           processRtOidSubList));

         // restrict PIs to current partition
         /*
          * sqlQuery.innerJoin(ModelPersistorBean.class) //
          * .on(ActivityInstanceBean.FR__MODEL, ModelPersistorBean.FIELD__OID)
          * .andWhere(Predicates.isEqual( // ModelPersistorBean.FR__PARTITION, //
          * SecurityProperties.getPartitionOid()));
          */

         MultiPartPredicateTerm predicate = new AndTerm();
         boolean singlePartition = Parameters.instance().getBoolean(
               KernelTweakingProperties.SINGLE_PARTITION, false);
         if (!singlePartition)
         {
            List<Integer> modelRtOids = new ArrayList<Integer>();
            for (Iterator iter = ModelManagerFactory.getCurrent().getAllModels(); iter
                  .hasNext();)
            {
               IModel model = (IModel) iter.next();
               modelRtOids.add(model.getModelOID());
            }
            PredicateTerm modelRtOidsPredicate = Predicates.inList(
                  ActivityInstanceBean.FR__MODEL, modelRtOids);
            predicate.add(modelRtOidsPredicate);
         }

         // count only non-terminated PIs
         ComparisonTerm activityStateTerm = Predicates.notInList(
               ActivityInstanceBean.FR__STATE, new int[] {
                     ActivityInstanceState.COMPLETED, ActivityInstanceState.ABORTED});
         predicate.add(activityStateTerm);
         sqlQuery.where(predicate);

         // count only PIs having benchmarks
         if (asq.getSelectedBenchmarks() != null)
         {
            sqlQuery.where(Predicates.inList(ProcessInstanceBean.FR__BENCHMARK_OID, asq.getSelectedBenchmarks()));
         }
         else
         {
            sqlQuery.where(Predicates.greaterThan(ProcessInstanceBean.FR__BENCHMARK_OID, 0));
         }

         final AuthorizationContext ctx = AuthorizationContext.create(ClientPermission.READ_ACTIVITY_INSTANCE_DATA);
         final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
               && !ctx.isAdminOverride();
         final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(ctx) {};

         authPredicate.addRawPrefetch(sqlQuery, piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE));

         StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
         {
            public void handleRow(ResultSet rs) throws SQLException
            {
               authPredicate.accept(rs);

               long aiOid = rs.getLong(1);
               long modelOid = rs.getLong(2);
               long activityRtOid = rs.getLong(3);
               int benchmarkValue = rs.getInt(4);
               long benchmarkOid = rs.getLong(5);
               long scopePiOid = rs.getLong(6);
               long currentPerformer = rs.getLong(7);
               long currentUserPerformer = rs.getLong(8);
               long department = rs.getLong(9);

               ctx.setActivityDataWithScopePi(scopePiOid, activityRtOid, modelOid, currentPerformer, currentUserPerformer, department);
               if (!guarded || Authorization2.hasPermission(ctx))
               {
                  IActivity activity = (IActivity) ctx.getModelElement();

                  String pdId = ModelUtils.getQualifiedId(activity.getProcessDefinition());
                  String aId = ModelUtils.getQualifiedId(activity);
                  result.addBenchmarkedInstances(pdId, aId, aiOid, benchmarkOid, benchmarkValue);
               }
            }
         });
      }

      return result;
   }

   private List<List<Long>> getSubLists(Set<Long> processRtOidFilter, int maxProcessOids)
   {
      List<Long> processRtOids = (processRtOidFilter != null) ? new ArrayList<Long>(processRtOidFilter) : new ArrayList<Long>();
      List<List<Long>> processRtOidSubLists = new ArrayList<List<Long>>();
      for (int i = 0; i < processRtOids.size(); i += maxProcessOids)
      {
         int toIndex = processRtOids.size() - i <= maxProcessOids
               ? processRtOids.size()
               : i + maxProcessOids;
         List<Long> subList = processRtOids.subList(i, toIndex);
         processRtOidSubLists.add(subList);
      }
      return processRtOidSubLists;
   }
}