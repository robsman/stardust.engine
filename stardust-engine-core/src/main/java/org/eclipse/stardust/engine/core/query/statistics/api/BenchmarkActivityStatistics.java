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
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public abstract class BenchmarkActivityStatistics
      extends CustomActivityInstanceQueryResult
{
   private static final long serialVersionUID = 1l;

   protected Map<Pair<String, String>, BenchmarkCategoryCounts> benchmarkCategoryCountsPerActivityId;

   protected Map<Pair<String, String>, Long> abortedPerActivityId;

   protected Map<Pair<String, String>, Long> completedPerActivityId;

   protected BenchmarkActivityStatistics(BenchmarkActivityStatisticsQuery query)
   {
      super(query);

      this.benchmarkCategoryCountsPerActivityId = CollectionUtils.newMap();
      this.abortedPerActivityId = CollectionUtils.newMap();
      this.completedPerActivityId = CollectionUtils.newMap();
   }

   public BenchmarkCategoryCounts getBenchmarkCategoryCountsForActivity(String processId,
         String activityId)
   {
      Pair<String, String> key = new Pair<String, String>(processId, activityId);
      return benchmarkCategoryCountsPerActivityId.get(key);
   }

   public Map<Pair<String, String>, BenchmarkCategoryCounts> getBenchmarkCategoryCounts()
   {
      return Collections.unmodifiableMap(benchmarkCategoryCountsPerActivityId);
   }

   public Map<Pair<String, String>, Long> getAbortedPerActivityId()
   {
      return Collections.unmodifiableMap(abortedPerActivityId);
   }

   public long getAbortedCountForActivity(String processId, String activityId)
   {
      Pair<String, String> key = new Pair<String, String>(processId, activityId);
      Long count = abortedPerActivityId.get(key);
      return count == null ? 0 : count;
   }

   public Map<Pair<String, String>, Long> getCompletedPerActivityId()
   {
      return Collections.unmodifiableMap(completedPerActivityId);
   }

   public long getCompletedCountForActivity(String processId, String activityId)
   {
      Pair<String, String> key = new Pair<String, String>(processId, activityId);
      Long count = completedPerActivityId.get(key);
      return count == null ? 0 : count;
   }
}
