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

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.core.query.statistics.api.*;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkActivityStatisticsResult extends BenchmarkActivityStatistics
{
   static final long serialVersionUID = -6036388240512274629L;

   public BenchmarkActivityStatisticsResult(BenchmarkActivityStatisticsQuery asq)
   {
      super(asq);
   }

   public void registerActivityBenchmarkCategory(String processId, String activityId, int benchmarkValue)
   {
      Pair<String,String> key = new Pair(processId, activityId);
      BenchmarkCategoryCounts benchmarkCategoryCount = benchmarkCategoryCountsPerActivityId.get(key);

      if (benchmarkCategoryCount == null)
      {
         benchmarkCategoryCount = new BenchmarkCategoryCounts();
         benchmarkCategoryCountsPerActivityId.put(key, benchmarkCategoryCount);
      }

      benchmarkCategoryCount.registerBenchmarkValue(benchmarkValue);
   }

   public void addAbortedInstance(String qualifiedProcessId, String qualifiedActivityId)
   {
      Pair<String,String> key = new Pair<String, String>(qualifiedProcessId, qualifiedActivityId);
      Long count = abortedPerActivityId.get(key);
      if (count == null)
      {
         count = 1L;
      }
      else
      {
         count++;
      }
      abortedPerActivityId.put(key, count);
   }

   public void addCompletedInstance(String qualifiedProcessId, String qualifiedActivityId)
   {
      Pair<String,String> key = new Pair<String, String>(qualifiedProcessId, qualifiedActivityId);
      Long count = completedPerActivityId.get(key);
      if (count == null)
      {
         count = 1L;
      }
      else
      {
         count++;
      }
      completedPerActivityId.put(key, count);
   }
}
