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

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.inList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessCumulationPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.ModelPersistorBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.*;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IProcessInstanceQueryEvaluator;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkProcessStatisticsRetriever implements IProcessInstanceQueryEvaluator
{
   public CustomProcessInstanceQueryResult evaluateQuery(CustomProcessInstanceQuery query)
   {
      if ( !(query instanceof BenchmarkProcessStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + BenchmarkProcessStatisticsQuery.class.getName());
      }

      final BenchmarkProcessStatisticsQuery psq = (BenchmarkProcessStatisticsQuery) query;

      Column[] columns = {
            ProcessInstanceBean.FR__OID,
            ProcessInstanceBean.FR__MODEL,
            ProcessInstanceBean.FR__PROCESS_DEFINITION,
            ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
            ProcessInstanceBean.FR__BENCHMARK_OID,
            ProcessInstanceBean.FR__BENCHMARK_VALUE,
         };

      QueryDescriptor sqlQuery = QueryDescriptor
            .from(ProcessInstanceBean.class)
            .select(columns);

      boolean singlePartition = Parameters.instance().getBoolean(
            KernelTweakingProperties.SINGLE_PARTITION, false);
      if (!singlePartition)
      {
         // restrict PIs to current partition
         sqlQuery.innerJoin(ModelPersistorBean.class).on(ProcessInstanceBean.FR__MODEL,
               ModelPersistorBean.FIELD__OID).andWhere(
               Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                     SecurityProperties.getPartitionOid()));
      }

      // count only non-terminated PIs
      sqlQuery.where(Predicates.notInList(ProcessInstanceBean.FR__STATE, new int[] {
            ProcessInstanceState.COMPLETED, ProcessInstanceState.ABORTED}));

      // count only PIs having benchmarks
      if (psq.getSelectedBenchmarks() != null)
      {
         sqlQuery.where(Predicates.inList(ProcessInstanceBean.FR__BENCHMARK_OID, psq.getSelectedBenchmarks()));
      }
      else
      {
         sqlQuery.where(Predicates.greaterThan(ProcessInstanceBean.FR__BENCHMARK_OID, 0));
      }

      final Set<Long> processRtOidFilter = StatisticsQueryUtils.extractProcessFilter(psq.getFilter());

      if ( !isEmpty(processRtOidFilter))
      {
         sqlQuery.where(andTerm(sqlQuery.getPredicateTerm(),
               inList(ProcessInstanceBean.FR__PROCESS_DEFINITION, newArrayList(processRtOidFilter))));
      }

      final AuthorizationContext ctx = AuthorizationContext.create(ClientPermission.READ_PROCESS_INSTANCE_DATA);
      final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
            && !ctx.isAdminOverride();
      final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(ctx) {};

      authPredicate.addRawPrefetch(sqlQuery, ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE);

      final BenchmarkProcessStatisticsResult result = new BenchmarkProcessStatisticsResult(psq);

      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         public void handleRow(ResultSet rs) throws SQLException
         {
            authPredicate.accept(rs);

            long piOid = rs.getLong(1);
            long modelOid = rs.getLong(2);
            long processRtOid = rs.getLong(3);
            long scopePiOid = rs.getLong(4);
            long benchmarkOid = rs.getLong(5);
            int benchmarkValue = rs.getInt(6);

            if ((null == processRtOidFilter) || processRtOidFilter.contains(processRtOid))
            {
               ctx.setProcessData(scopePiOid, processRtOid, modelOid);
               if (!guarded || Authorization2.hasPermission(ctx))
               {
                  IProcessDefinition process = (IProcessDefinition) ctx.getModelElement();

                  String elementId = ModelUtils.getQualifiedId(process);
                  result.addBenchmarkedInstances(elementId, benchmarkOid, benchmarkValue);
               }
            }
         }
      });

      return result;
   }
}