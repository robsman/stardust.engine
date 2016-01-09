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

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.inList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
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
 * @author rsauer
 * @version $Revision$
 */
public class ProcessStatisticsRetriever implements IProcessInstanceQueryEvaluator
{
   public CustomProcessInstanceQueryResult evaluateQuery(CustomProcessInstanceQuery query)
   {
      if ( !(query instanceof ProcessStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + ProcessStatisticsQuery.class.getName());
      }

      final ProcessStatisticsQuery psq = (ProcessStatisticsQuery) query;

      ProcessCumulationPolicy cumulationPolicy = StatisticsQueryUtils.getProcessCumulationPolicy(psq);
      FieldRef frCumulationPi;
      if ((null == cumulationPolicy) || cumulationPolicy.cumulateWithScopeProcess())
      {
         frCumulationPi = ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE;
      }
      else if (cumulationPolicy.cumulateWithRootProcess())
      {
         frCumulationPi = ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE;
      }
      else
      {
         frCumulationPi = ProcessInstanceBean.FR__OID;
      }

      Column[] columns = {
            ProcessInstanceBean.FR__OID,
            frCumulationPi,
            ProcessInstanceBean.FR__MODEL,
            ProcessInstanceBean.FR__PROCESS_DEFINITION,
            ProcessInstanceBean.FR__PRIORITY,
            ProcessInstanceBean.FR__START_TIME,
            ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
            ProcessInstanceBean.FR__STATE
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

      final Set<Long> processRtOidFilter = StatisticsQueryUtils.extractProcessFilter(psq.getFilter());

      if ( !isEmpty(processRtOidFilter))
      {
         sqlQuery.where(andTerm(sqlQuery.getPredicateTerm(),
               inList(ProcessInstanceBean.FR__PROCESS_DEFINITION, newArrayList(processRtOidFilter))));
      }

      final AuthorizationContext ctx = AuthorizationContext.create(ClientPermission.READ_PROCESS_INSTANCE_DATA);

      final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(ctx) {};

      authPredicate.addRawPrefetch(sqlQuery, ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE);

      ProcessStatisticsResultsetEvaluator evaluator = 
            new ProcessStatisticsResultsetEvaluator(psq, ctx, authPredicate, processRtOidFilter);
      
      StatisticsQueryUtils.executeQuery(sqlQuery, evaluator);

      return evaluator.getProcessStatisticsResult();
   }
   
   protected static class ProcessStatisticsResultsetEvaluator implements IResultSetTemplate
   {
      private final CriticalExecutionTimePolicy criticalityPolicy;

      private final Date tsPiStart = TimestampProviderUtils.getTimeStamp();
      private final Date now = TimestampProviderUtils.getTimeStamp();
      
      private final AuthorizationContext authCtx;
      private final boolean guarded;
      private final Predicate authPredicate;
      private final ProcessStatisticsResult result;
      private final Set<Long> processRtOidFilter;
      
      ProcessStatisticsResultsetEvaluator(ProcessStatisticsQuery psq,
            AuthorizationContext ctx, Predicate authPredicate, Set<Long> processRtOidFilter)
      {
         this.authCtx = ctx;
         guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
               && !ctx.isAdminOverride();
         this.authPredicate = authPredicate;
         criticalityPolicy = StatisticsQueryUtils.getCriticalExecutionTimePolicy(psq);
         result = new ProcessStatisticsResult(psq);
         this.processRtOidFilter = processRtOidFilter;
      }

      public void handleRow(ResultSet rs) throws SQLException
      {
         authPredicate.accept(rs);

         long processRtOid = rs.getLong(4);

         if ((null == processRtOidFilter) || processRtOidFilter.contains(processRtOid))
         {
            long scopePiOid = rs.getLong(7);
            long modelOid = rs.getLong(3);

            authCtx.setProcessData(scopePiOid, processRtOid, modelOid);
            if (!guarded || Authorization2.hasPermission(authCtx))
            {
               long piOid = rs.getLong(1);
               long cumulationPiOid = rs.getLong(2);
               int priority = rs.getInt(5);
               int state = rs.getInt(8);

               IProcessDefinition process = (IProcessDefinition) authCtx.getModelElement();

               boolean isCritical = false;
               boolean isInterrupted = false;

               if (piOid == cumulationPiOid)
               {
                  long piStartTime = rs.getLong(6);
                  tsPiStart.setTime(piStartTime);

                  isCritical = criticalityPolicy.isCriticalDuration(priority, tsPiStart,
                        now, process);
               }

               if (state == ProcessInstanceState.INTERRUPTED)
               {
                  isInterrupted = true;
               }

               String elementId = process == null ? null : process.getQualifiedId();
               result.addPriorizedInstances(elementId, priority, piOid, isCritical, isInterrupted);
            }
         }
      }
      
      public ProcessStatisticsResult getProcessStatisticsResult()
      {
         return result;
      }
   }
   
}