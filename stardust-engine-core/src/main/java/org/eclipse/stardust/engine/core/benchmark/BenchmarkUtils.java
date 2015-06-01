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
package org.eclipse.stardust.engine.core.benchmark;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class BenchmarkUtils
{

   private static final String KEY_BENCHMARK_CACHE = BenchmarkEvaluator.class.getName()
         + ".BenchmarkCache";

   
   public static boolean isBenchmarkedPI(IProcessInstance pi)
   {
      if (pi.getBenchmark() > 0)
      {
         return true;
      }
      return false;
   }
   
   
   public static synchronized BenchmarkDefinition getBenchmarkDefinition(long definitionOid)
   {
      Map<Long, BenchmarkDefinition> benchmarkCache = BenchmarkUtils.getBenchmarkCache(SecurityProperties.getPartition()
            .getId());

      BenchmarkDefinition benchmarkDefinition = benchmarkCache.get(definitionOid);

      if (benchmarkDefinition == null)
      {
         benchmarkDefinition = new BenchmarkDefinition(definitionOid);
         benchmarkCache.put(definitionOid, benchmarkDefinition);
      }
      return benchmarkDefinition;
   }
   
   public static void removeAllBenchmarksFromCache()
   {
      getBenchmarkCache(SecurityProperties.getPartition().getId()).clear();
   }
   
   public static void removeBenchmarkFromCache(long definitionOid)
   {
      getBenchmarkCache(SecurityProperties.getPartition().getId()).remove(definitionOid);
   }
   
   private static Map<Long, BenchmarkDefinition> getBenchmarkCache(String partitionId)
   {
      final GlobalParameters globals = GlobalParameters.globals();

      ConcurrentHashMap<String, Map> benchmarkPartitionCache = (ConcurrentHashMap<String, Map>) globals
            .get(KEY_BENCHMARK_CACHE);
      if (null == benchmarkPartitionCache)
      {
         globals.getOrInitialize(KEY_BENCHMARK_CACHE, new ValueProvider()
         {

            public Object getValue()
            {
               return new ConcurrentHashMap<String, Map>();
            }

         });
         benchmarkPartitionCache = (ConcurrentHashMap<String, Map>) globals
               .get(KEY_BENCHMARK_CACHE);
      }

      Map benchmarkCache = benchmarkPartitionCache.get(partitionId);
      if (null == benchmarkCache)
      {
         benchmarkPartitionCache.put(partitionId,
               new ConcurrentHashMap<Long, BenchmarkDefinition>());
         benchmarkCache = (Map<Long, BenchmarkDefinition>) benchmarkPartitionCache
               .get(partitionId);
      }

      return benchmarkCache;
   }   
   
}
