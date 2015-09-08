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


/**
 * @author roland.stamm
 * @version $Revision$
 */
public abstract class BenchmarkProcessStatistics extends BenchmarkStatistics<String, Long>
{
   private static final long serialVersionUID = 1l;

   protected BenchmarkProcessStatistics(BenchmarkProcessStatisticsQuery query)
   {
      super(query);
   }

   public BenchmarkCategoryCounts getBenchmarkCategoryCountsForProcess(String qualifiedProcessId)
   {
      Map <String, BenchmarkCategoryCounts> benchmarkCategoryCountsPerItem = getBenchmarkCategoryCountsPerItem();
      return benchmarkCategoryCountsPerItem.get(qualifiedProcessId);
   }
   
   @Override
   protected long getCompletedCount(String key)
   {
      Long count = getCompletedPerItem().get(key);
      return count == null ? 0 : count;
   }
   
   @Override
   protected long getAbortedCount(String key)
   {
      Long count = getAbortedPerItem().get(key);
      return count == null ? 0 : count;
   }

   public Map<String, Long> getAbortedPerProcessId()
   {
      return getAbortedPerItem();
   }

   public long getAbortedCountForProcess(String qualifiedProcessId)
   {
      return getAbortedCount(qualifiedProcessId);
   }

   public Map<String, Long> getCompletedPerProcessId()
   {
      return getCompletedPerItem();
   }

   public long getCompletedCountForProcess(String qualifiedProcessId)
   {
      return getCompletedCount(qualifiedProcessId);
   }
   
   public Map<String, BenchmarkCategoryCounts> getBenchmarkCategoryCounts()
   {
      return Collections.unmodifiableMap(getBenchmarkCategoryCountsPerItem());
   }
}
