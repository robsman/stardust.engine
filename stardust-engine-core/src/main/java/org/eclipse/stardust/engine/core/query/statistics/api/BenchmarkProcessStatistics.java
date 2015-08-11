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
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQueryResult;


/**
 * @author roland.stamm
 * @version $Revision$
 */
public abstract class BenchmarkProcessStatistics extends CustomProcessInstanceQueryResult
{
   private static final long serialVersionUID = 1l;

   protected Map<String, BenchmarkCategoryCounts> benchmarkCategoryCountsPerProcessId;

   protected Map<String, Long> abortedPerProcessId;

   protected Map<String, Long> completedPerProcessId;

   protected BenchmarkProcessStatistics(BenchmarkProcessStatisticsQuery query)
   {
      super(query);

      this.benchmarkCategoryCountsPerProcessId = CollectionUtils.newMap();
      this.abortedPerProcessId  = CollectionUtils.newMap();
      this.completedPerProcessId = CollectionUtils.newMap();
   }

   public BenchmarkCategoryCounts getBenchmarkCategoryCountsForProcess(String qualifiedProcessId)
   {
      return benchmarkCategoryCountsPerProcessId.get(qualifiedProcessId);
   }

   public Map<String, BenchmarkCategoryCounts> getBenchmarkCategoryCounts()
   {
      return Collections.unmodifiableMap(benchmarkCategoryCountsPerProcessId);
   }

   public Map<String, Long> getAbortedPerProcessId()
   {
      return abortedPerProcessId;
   }

   public long getAbortedCountForProcess(String qualifiedProcessId)
   {
      Long count = abortedPerProcessId.get(qualifiedProcessId);
      return count == null ? 0 : count;
   }

   public Map<String, Long> getCompletedPerProcessId()
   {
      return completedPerProcessId;
   }

   public long getCompletedCountForProcess(String qualifiedProcessId)
   {
      Long count = completedPerProcessId.get(qualifiedProcessId);
      return count == null ? 0 : count;
   }
}
