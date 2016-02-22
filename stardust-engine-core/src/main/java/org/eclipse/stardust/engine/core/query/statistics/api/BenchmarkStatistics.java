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
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;

public abstract class BenchmarkStatistics<K, V>
{
   private Map<K, BenchmarkCategoryCounts> benchmarkCategoryCountsPerItem;
   private Map<K, V> abortedPerItem;
   private Map<K, V> completedPerItem;
   
   public BenchmarkStatistics()
   {
      this.benchmarkCategoryCountsPerItem = CollectionUtils.newMap();
      this.abortedPerItem  = CollectionUtils.newMap();
      this.completedPerItem = CollectionUtils.newMap();
   }
   
   abstract long getCompletedCount(K key);
   
   abstract long getAbortedCount(K key);
   
   protected Map<K, V> getAbortedPerItem()
   {
      return abortedPerItem;
   }
   
   protected Map<K, V> getCompletedPerItem()
   {
      return completedPerItem;
   }
   
   protected Map<K, BenchmarkCategoryCounts> getBenchmarkCategoryCountsPerItem()
   {
      return benchmarkCategoryCountsPerItem;
   }
}
