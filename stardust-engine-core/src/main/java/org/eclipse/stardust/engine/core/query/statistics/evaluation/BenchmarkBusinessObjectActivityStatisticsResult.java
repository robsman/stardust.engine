/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.util.Set;

import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkBusinessObjectStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectBenchmarkStatistics;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;

public class BenchmarkBusinessObjectActivityStatisticsResult
      extends CustomActivityInstanceQueryResult
      implements BenchmarkBusinessObjectStatistics
{
   private static final long serialVersionUID = 1L;

   private final BusinessObjectBenchmarkStatistics benchmarkStatistics;

   public BenchmarkBusinessObjectActivityStatisticsResult(
         BenchmarkActivityStatisticsQuery query)
   {
      super(query);

      benchmarkStatistics = new BusinessObjectBenchmarkStatistics();
   }

   protected BusinessObjectBenchmarkStatistics getBenchmarkStatistics()
   {
      return benchmarkStatistics;
   }

   @Override
   public Set<String> getGroupByValues()
   {
      return benchmarkStatistics.getGroupByValues();
   }

   @Override
   public Set<String> getFilterValues(String groupByValue)
   {
      return benchmarkStatistics.getFilterValues(groupByValue);
   }

   @Override
   public Set<Integer> getRegisterdBenchmarkValues()
   {
      return benchmarkStatistics.getRegisterdBenchmarkValues();
   }

   @Override
   public long getAbortedCount(String groupByValue, String filterValue)
   {
      return benchmarkStatistics.getAbortedCount(groupByValue, filterValue);
   }

   @Override
   public long getCompletedCount(String groupByValue, String filterValue)
   {
      return benchmarkStatistics.getCompletedCount(groupByValue, filterValue);
   }

   @Override
   public long getBenchmarkCategoryCount(String groupByValue, String filterValue,
         int benchmarkValue)
   {
      return benchmarkStatistics.getBenchmarkCategoryCount(
            groupByValue, filterValue, benchmarkValue);
   }

   @Override
   public Set<Long> getInstanceOIDsForBenchmarkCategory(String groupByValue,
         String filterValue, int benchmarkValue)
   {
      return benchmarkStatistics.getInstanceOIDsForBenchmarkCategory(
            groupByValue, filterValue, benchmarkValue);
   }

   @Override
   public Set<Long> getAbortedInstanceOIDs(String groupByValue, String filterValue)
   {
      return benchmarkStatistics.getAbortedInstanceOIDs(groupByValue, filterValue);
   }

   @Override
   public Set<Long> getCompletedInstanceOIDs(String groupByValue, String filterValue)
   {
      return benchmarkStatistics.getCompletedInstanceOIDs(groupByValue, filterValue);
   }

   public void setTotalCount(long totalCount)
   {
      this.totalCount = totalCount;
   }
}
