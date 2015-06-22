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

public class BenchmarkCategoryCounts
{

   private Map<Integer, Long> benchmarkCategoryCount = CollectionUtils.newMap();

   public void registerBenchmarkValue(int benchmarkValue)
   {
      Long count = benchmarkCategoryCount.get(benchmarkValue);
      if (count == null)
      {
         count = 1L;
      }
      else
      {
         count++;
      }
      benchmarkCategoryCount.put(benchmarkValue, count);
   }

   public Map<Integer, Long> getBenchmarkCategoryCount()
   {
      return benchmarkCategoryCount;
   }

}
