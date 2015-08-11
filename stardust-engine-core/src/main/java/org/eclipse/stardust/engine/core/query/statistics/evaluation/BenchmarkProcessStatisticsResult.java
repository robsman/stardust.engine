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

import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkCategoryCounts;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkProcessStatisticsResult extends BenchmarkProcessStatistics
{
   static final long serialVersionUID = -5825543169280211775L;

   public BenchmarkProcessStatisticsResult(BenchmarkProcessStatisticsQuery query)
   {
      super(query);
   }

   public void registerProcessBenchmarkCategory(String processId, int benchmarkValue)
   {
      BenchmarkCategoryCounts benchmarkCategoryCount = benchmarkCategoryCountsPerProcessId.get(processId);

      if (benchmarkCategoryCount == null)
      {
         benchmarkCategoryCount = new BenchmarkCategoryCounts();
         benchmarkCategoryCountsPerProcessId.put(processId, benchmarkCategoryCount);
      }

      benchmarkCategoryCount.registerBenchmarkValue(benchmarkValue);
   }

   public void addAbortedInstance(String qualifiedProcessId)
   {
      Long count = abortedPerProcessId.get(qualifiedProcessId);
      if (count == null)
      {
         count = 1L;
      }
      else
      {
         count++;
      }
      abortedPerProcessId.put(qualifiedProcessId, count);
   }

   public void addCompletedInstance(String qualifiedProcessId)
   {
      Long count = completedPerProcessId.get(qualifiedProcessId);
      if (count == null)
      {
         count = 1L;
      }
      else
      {
         count++;
      }
      completedPerProcessId.put(qualifiedProcessId, count);
   }
}
