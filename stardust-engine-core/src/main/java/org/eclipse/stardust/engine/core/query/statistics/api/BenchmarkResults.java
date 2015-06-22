/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;

public class BenchmarkResults
{

   private Map<Pair<String, String>, BenchmarkCategoryCounts> benchmarkCategoryCounts;

   public BenchmarkResults()
   {
      super();
      this.benchmarkCategoryCounts = CollectionUtils.newMap();
   }

   public void registerProcess(String processId, int benchmarkValue)
   {
      registerActivity(processId, null, benchmarkValue);
   }

   public void registerActivity(String processId, String activityId, int benchmarkValue)
   {
      Pair<String,String> key = new Pair(processId, activityId);
      BenchmarkCategoryCounts benchmarkCategoryCount = benchmarkCategoryCounts.get(key);

      if (benchmarkCategoryCount == null)
      {
         benchmarkCategoryCount = new BenchmarkCategoryCounts();
         benchmarkCategoryCounts.put(key, benchmarkCategoryCount);
      }

      benchmarkCategoryCount.registerBenchmarkValue(benchmarkValue);
   }

   public BenchmarkCategoryCounts getBenchmarkCategoryCountsForProcess(String processId)
   {
      Pair<String,String> key = new Pair(processId, null);
      return benchmarkCategoryCounts.get(key);
   }

   public BenchmarkCategoryCounts getBenchmarkCategoryCountsForActivity(String processId, String activityId)
   {
      Pair<String,String> key = new Pair(processId, activityId);
      return benchmarkCategoryCounts.get(key);
   }

}
