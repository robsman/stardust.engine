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

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQueryEvaluator;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IProcessInstanceQueryEvaluator;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkProcessStatisticsRetriever
      implements IProcessInstanceQueryEvaluator
{
   public CustomProcessInstanceQueryResult evaluateQuery(CustomProcessInstanceQuery query)
   {
      if (!(query instanceof BenchmarkProcessStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + BenchmarkProcessStatisticsQuery.class.getName());
      }

      final BenchmarkProcessStatisticsQuery psq = (BenchmarkProcessStatisticsQuery) query;

      final BenchmarkProcessStatisticsResult result = new BenchmarkProcessStatisticsResult(
            psq);
      ResultIterator rawResult = null;
      try
      {
         rawResult = new ProcessInstanceQueryEvaluator(query,
               QueryServiceUtils.getDefaultEvaluationContext()).executeFetch();

         while (rawResult.hasNext())
         {
            ProcessInstanceBean process = (ProcessInstanceBean) rawResult.next();
            String qualifiedProcessId = ModelUtils.getQualifiedId(process
                  .getProcessDefinition());

            if (ProcessInstanceState.Completed.equals(process.getState()))
            {
               result.addCompletedInstance(qualifiedProcessId);
            }
            else if (ProcessInstanceState.Aborted.equals(process.getState()))
            {
               result.addAbortedInstance(qualifiedProcessId);
            }
            else
            {
               // Count benchmark results only for Alive processes.
               int benchmarkValue = process.getBenchmarkValue();

               result.registerProcessBenchmarkCategory(qualifiedProcessId, benchmarkValue);
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
}