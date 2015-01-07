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
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.BinaryOperatorFilter;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Operator.Binary;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalCostPerExecutionPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalProcessingTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.ParticipantDepartmentPair;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessCumulationPolicy;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceScopeBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class StatisticsQueryUtils
{

   public static void executeQuery(QueryDescriptor query, IResultSetTemplate template)
   {
      ResultSet rs = null;
      try
      {
         Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

         rs = session.executeQuery(query);
         while (rs.next())
         {
            template.handleRow(rs);
         }
      }
      catch (SQLException  sqle)
      {
         throw new PublicException(BpmRuntimeError.QUERY_FAILED_EXECUTING_QUERY.raise(),
               sqle);
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }
   }

   public static final Set<Long> extractProcessFilter(FilterAndTerm filter)
   {
      Set<Long> processRtOids = null;

      if ( !filter.getParts().isEmpty())
      {
         // TODO change to more robust visitor based approach

         processRtOids = CollectionUtils.newSet();
         Set<String> processIds = CollectionUtils.newSet();

         FilterTerm term = (FilterTerm) filter.getParts().get(0);

         if (term instanceof FilterOrTerm)
         {
            for (Iterator i = term.getParts().iterator(); i.hasNext();)
            {
               FilterCriterion criterion = (FilterCriterion) i.next();
               if (criterion instanceof ProcessDefinitionFilter)
               {
                  processIds.add(((ProcessDefinitionFilter) criterion).getProcessID());
               }
               else if (criterion instanceof BinaryOperatorFilter)
               {
                  BinaryOperatorFilter comparison = (BinaryOperatorFilter) criterion;
                  if (ProcessInstanceQuery.class.isAssignableFrom(comparison.getScope())
                        && ProcessInstanceBean.FIELD__PROCESS_DEFINITION.equals(comparison.getAttribute()))
                  {
                     if (Binary.IS_EQUAL.equals(comparison.getOperator())
                           && (comparison.getValue() instanceof Number))
                     {
                        processRtOids.add(Long.valueOf(
                              ((Number) comparison.getValue()).longValue()));
                     }
                  }
               }
            }
         }

         if ( !processIds.isEmpty())
         {
            processRtOids.addAll(StatisticsModelUtils.rtOidsForProcessIds(processIds));
         }

         if (processRtOids.isEmpty())
         {
            processRtOids = null;
         }
      }

      return processRtOids;
   }

   public static final Set<ParticipantDepartmentOidPair> extractModelParticipantFilter(FilterAndTerm filterTerm)
   {
      Set<ParticipantDepartmentOidPair> mpRtOids = CollectionUtils.newSet();

      if ( !filterTerm.getParts().isEmpty())
      {
         // TODO change to more robust visitor based approach

         Set<ParticipantDepartmentPair> mpIds = CollectionUtils.newSet();

         FilterCriterion filterPart = (FilterCriterion) filterTerm.getParts().get(0);

         if (filterPart instanceof ParticipantAssociationFilter)
         {
            ParticipantAssociationFilter filter = ((ParticipantAssociationFilter) filterPart);

            if (ParticipantAssociationFilter.FILTER_KIND_MODEL_PARTICIPANT.equals(filter.getFilterKind()))
            {
               mpIds.add(ParticipantDepartmentPair.getParticipantDepartmentPair(
                     (ModelParticipantInfo) filter.getParticipant()));
            }
            else
            {
               // TODO
            }
         }
         else if (filterPart instanceof FilterOrTerm)
         {
            for (Iterator i = ((FilterOrTerm) filterPart).getParts().iterator(); i.hasNext();)
            {
               FilterCriterion criterion = (FilterCriterion) i.next();
               if (criterion instanceof ParticipantAssociationFilter)
               {
                  ParticipantAssociationFilter filter = ((ParticipantAssociationFilter) criterion);

                  if (ParticipantAssociationFilter.FILTER_KIND_MODEL_PARTICIPANT.equals(filter.getFilterKind()))
                  {
                     mpIds.add(ParticipantDepartmentPair.getParticipantDepartmentPair(
                           (ModelParticipantInfo) filter.getParticipant()));
                  }
                  else
                  {
                     // TODO
                  }

               }
            }
         }

         if ( !mpIds.isEmpty())
         {
            mpRtOids.addAll(StatisticsModelUtils.rtOidsForModelParticipantIds(mpIds));
         }
      }

      return mpRtOids;
   }

   public static final Join joinCumulationPi(Query query, QueryDescriptor sqlQuery,
         FieldRef piField)
   {
      ProcessCumulationPolicy pcp = (ProcessCumulationPolicy) query.getPolicy(ProcessCumulationPolicy.class);

      return joinCumulationPi(pcp, sqlQuery, piField);
   }

   public static final Join joinCumulationPi(ProcessCumulationPolicy pcp,
         QueryDescriptor sqlQuery, FieldRef piField)
   {
      if (null == pcp)
      {
         pcp = ProcessCumulationPolicy.WITH_SCOPE_PI;
      }

      // retrieve root/scope process for aggregation
      if (pcp.cumulateWithScopeProcess() || pcp.cumulateWithRootProcess())
      {
         Join join = sqlQuery.innerJoin(ProcessInstanceScopeBean.class) //
               .on(piField, ProcessInstanceScopeBean.FIELD__PROCESS_INSTANCE);

         if (pcp.cumulateWithScopeProcess())
         {
            sqlQuery.innerJoin(ProcessInstanceBean.class) //
                  .on(ProcessInstanceScopeBean.FR__SCOPE_PROCESS_INSTANCE,
                        ProcessInstanceBean.FIELD__OID);
         }
         else if (pcp.cumulateWithRootProcess())
         {
            sqlQuery.innerJoin(ProcessInstanceBean.class) //
                  .on(ProcessInstanceScopeBean.FR__ROOT_PROCESS_INSTANCE,
                        ProcessInstanceBean.FIELD__OID);
         }

         return join;
      }
      else
      {
         return sqlQuery.innerJoin(ProcessInstanceBean.class) //
               .on(piField, ProcessInstanceBean.FIELD__OID);
      }
   }

   public static CriticalExecutionTimePolicy getCriticalExecutionTimePolicy(Query query)
   {
      CriticalExecutionTimePolicy policy = (CriticalExecutionTimePolicy) query.getPolicy(CriticalExecutionTimePolicy.class);

      return (null != policy) ? policy : CriticalExecutionTimePolicy.EXCEEDING_TARGET_EXECUTION_TIME;
   }

   public static CriticalProcessingTimePolicy getCriticalProcessingTimePolicy(Query query)
   {
      CriticalProcessingTimePolicy policy = (CriticalProcessingTimePolicy) query.getPolicy(CriticalProcessingTimePolicy.class);

      return (null != policy) ? policy : CriticalProcessingTimePolicy.EXCEEDING_TARGET_PROCESSING_TIME;
   }

   public static CriticalCostPerExecutionPolicy getCriticalCostPerExecutionPolicy(Query query)
   {
      CriticalCostPerExecutionPolicy policy = (CriticalCostPerExecutionPolicy) query.getPolicy(CriticalCostPerExecutionPolicy.class);

      return (null != policy) ? policy : CriticalCostPerExecutionPolicy.EXCEEDING_TARGET_COST_PER_EXECUTION;
   }

   public static ProcessCumulationPolicy getProcessCumulationPolicy(Query query)
   {
      ProcessCumulationPolicy policy = (ProcessCumulationPolicy) query.getPolicy(ProcessCumulationPolicy.class);

      return (null != policy) ? policy : ProcessCumulationPolicy.WITH_SCOPE_PI;
   }

}
