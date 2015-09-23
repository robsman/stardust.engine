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

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQueryEvaluator;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectPolicy.BusinessObjectData;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IActivityInstanceQueryEvaluator;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkActivityStatisticsRetriever
      implements IActivityInstanceQueryEvaluator
{

   @Override
   public CustomActivityInstanceQueryResult evaluateQuery(
         CustomActivityInstanceQuery query)
   {
      if (!(query instanceof BenchmarkActivityStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + BenchmarkActivityStatisticsQuery.class.getName());
      }
      
      final BenchmarkActivityStatisticsQuery psq = (BenchmarkActivityStatisticsQuery) query;

      BusinessObjectPolicy boPolicy = (BusinessObjectPolicy)
            psq.getPolicy(BusinessObjectPolicy.class);

      return boPolicy == null ? evaluateActivbityStatisticsQuery(psq) :
         evaluateBusinessObjectStatisticsQuery(psq, boPolicy);
   }

   private CustomActivityInstanceQueryResult evaluateActivbityStatisticsQuery(BenchmarkActivityStatisticsQuery query)
   {
      final BenchmarkActivityStatisticsResult result = new BenchmarkActivityStatisticsResult(
            query);

      ResultIterator rawResult = null;
      try
      {
         rawResult = new ActivityInstanceQueryEvaluator(query,
               QueryServiceUtils.getDefaultEvaluationContext()).executeFetch();

         while (rawResult.hasNext())
         {
            ActivityInstanceBean activity = (ActivityInstanceBean) rawResult.next();
            String qualifiedActivityId = ModelUtils
                  .getQualifiedId(activity.getActivity());
            String qualifiedProcessId = ModelUtils.getQualifiedId(activity.getActivity()
                  .getProcessDefinition());

            if (ActivityInstanceState.Completed.equals(activity.getState()))
            {
               result.addCompletedInstance(qualifiedProcessId, qualifiedActivityId);
            }
            else if (ActivityInstanceState.Aborted.equals(activity.getState()))
            {
               result.addAbortedInstance(qualifiedProcessId, qualifiedActivityId);
            }
            else
            {
               // Count benchmark results only for Alive processes.
               int benchmarkValue = activity.getBenchmarkValue();

               result.registerActivityBenchmarkCategory(qualifiedProcessId,
                     qualifiedActivityId, benchmarkValue);
            }
         }
      }
      finally
      {
         if (rawResult != null)
         {
            rawResult.close();
         }
      }
      return result;
   }
   
   private CustomActivityInstanceQueryResult evaluateBusinessObjectStatisticsQuery(
         BenchmarkActivityStatisticsQuery query, BusinessObjectPolicy boPolicy)
   {
      final BenchmarkBusinessObjectActivityStatisticsResult result =
            new BenchmarkBusinessObjectActivityStatisticsResult(query);

      BusinessObjectData filter = boPolicy.getFilter();

      QName boName = new QName(filter.getModelId(), filter.getBusinessObjectId());
      BusinessObjectQuery boq = filter.getPrimaryKeyValues() == null
            ? BusinessObjectQuery.findForBusinessObject(boName.toString())
            : BusinessObjectQuery.findWithPrimaryKey(boName.toString(), filter.getPrimaryKeyValues());
            
      return result;
   }
}