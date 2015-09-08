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

import java.util.Map;

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
      Map<String, BenchmarkCategoryCounts> benchmarkCategoryCountsPerItem = getBenchmarkCategoryCountsPerItem();
      BenchmarkCategoryCounts benchmarkCategoryCount = benchmarkCategoryCountsPerItem.get(processId);

      if (benchmarkCategoryCount == null)
      {
         benchmarkCategoryCount = new BenchmarkCategoryCounts();
         benchmarkCategoryCountsPerItem.put(processId, benchmarkCategoryCount);
      }

      benchmarkCategoryCount.registerBenchmarkValue(benchmarkValue);
   }

   public void addAbortedInstance(String qualifiedProcessId)
   {
      Map<String, Long> abortedPerItem = getAbortedPerItem();
      Long count = abortedPerItem.get(qualifiedProcessId);
      if (count == null)
      {
         count = 1L;
      }
      else
      {
         count++;
      }
      abortedPerItem.put(qualifiedProcessId, count);
   }

   public void addCompletedInstance(String qualifiedProcessId)
   {
      Map<String, Long> completedPerItem = getCompletedPerItem();
      Long count = completedPerItem.get(qualifiedProcessId);
      if (count == null)
      {
         count = 1L;
      }
      else
      {
         count++;
      }
      completedPerItem.put(qualifiedProcessId, count);
   }
}
