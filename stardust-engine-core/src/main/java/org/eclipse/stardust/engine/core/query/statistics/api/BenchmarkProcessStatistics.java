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

import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQueryResult;


/**
 * @author roland.stamm
 * @version $Revision$
 */
public abstract class BenchmarkProcessStatistics extends CustomProcessInstanceQueryResult
{
   private static final long serialVersionUID = 1l;
   
   private final BenchmarkStatistics<String, Long> benchmarkStatistcs;

   protected BenchmarkProcessStatistics(BenchmarkProcessStatisticsQuery query)
   {
      super(query);
      
      benchmarkStatistcs = new BenchmarkStatistics<String, Long>() {

         @Override
         public long getCompletedCount(String key)
         {
            Long count = getCompletedPerItem().get(key);
            return count == null ? 0 : count;
         }

         @Override
         public long getAbortedCount(String key)
         {
            Long count = getAbortedPerItem().get(key);
            return count == null ? 0 : count;
         }
         
      };
   }

   public BenchmarkCategoryCounts getBenchmarkCategoryCountsForProcess(String qualifiedProcessId)
   {
      Map <String, BenchmarkCategoryCounts> benchmarkCategoryCountsPerItem = 
            benchmarkStatistcs.getBenchmarkCategoryCountsPerItem();
      return benchmarkCategoryCountsPerItem.get(qualifiedProcessId);
   }
   
   public Map<String, Long> getAbortedPerProcessId()
   {
      return Collections.unmodifiableMap(benchmarkStatistcs.getAbortedPerItem());
   }

   public long getAbortedCountForProcess(String qualifiedProcessId)
   {
      return benchmarkStatistcs.getAbortedCount(qualifiedProcessId);
   }

   public Map<String, Long> getCompletedPerProcessId()
   {
      return Collections.unmodifiableMap(benchmarkStatistcs.getCompletedPerItem());
   }

   public long getCompletedCountForProcess(String qualifiedProcessId)
   {
      return benchmarkStatistcs.getCompletedCount(qualifiedProcessId);
   }
   
   public Map<String, BenchmarkCategoryCounts> getBenchmarkCategoryCounts()
   {
      return Collections.unmodifiableMap(benchmarkStatistcs.getBenchmarkCategoryCountsPerItem());
   }
}
