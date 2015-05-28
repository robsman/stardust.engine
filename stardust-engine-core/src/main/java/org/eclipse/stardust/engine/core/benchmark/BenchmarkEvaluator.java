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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.IBenchmarkEvaluator;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 *
 * @author Thomas.Wolfram
 *
 */
public class BenchmarkEvaluator implements IBenchmarkEvaluator
{

   private static final Logger trace = LogManager.getLogger(BenchmarkEvaluator.class);

   private static final int BENCHMARKK_FAULT_VALUE = -1;

   private static final String KEY_BENCHMARK_CACHE = BenchmarkEvaluator.class.getName()
         + ".BenchmarkCache";

   private BenchmarkDefinition benchmark;

   private Map<Long, BenchmarkDefinition> benchmarkCache;

   public BenchmarkEvaluator(long benchmarkOid)
   {

      benchmarkCache = getBenchmarkCache(SecurityProperties.getPartition().getId());

      benchmark = benchmarkCache.get(benchmarkOid);

      if (benchmark == null)
      {
         benchmark = new BenchmarkDefinition(benchmarkOid);
         benchmarkCache.put(benchmarkOid, benchmark);
      }

   }

   @Override
   public int getBenchmarkForProcessInstance(long piOid)
   {
      ProcessInstanceBean piBean = ProcessInstanceBean.findByOID(piOid);

      int benchmarkValue = evaluateBenchmarkForPi(piBean);

      trace.info("Evaluating Benchmark with OID <" + this.benchmark.getOid()
            + "> for Process Instance <" + piOid + ">");

      return benchmarkValue;
   }

   @Override
   public int getBenchmarkForActivityInstance(long aiOid, String activityId)
   {

      ActivityInstanceBean aiBean = ActivityInstanceBean.findByOID(aiOid);

      int benchmarkValue = evaluateBenchmarkForAi(aiBean);

      trace.info("Evaluating Benchmark with OID <" + this.benchmark.getOid()
            + "> for Activity Instance <" + aiOid + ">/<" + activityId + ">");

      return benchmarkValue;
   }

   private int evaluateBenchmarkForAi(ActivityInstanceBean bean)
   {
      try
      {
         TreeMap<Integer, ConditionEvaluator> activityConditions = benchmark
               .getActivityConditions(bean.getActivity().getId());
         ArrayList<Integer> columns = new ArrayList<Integer>(activityConditions.keySet());

         Collections.reverse(columns);

         for (Integer column : columns)
         {
            ConditionEvaluator eval = activityConditions.get(column);

            Boolean result = eval.evaluate(bean);

            if (result == null)
            {
               ConditionEvaluator globalEvaluator = benchmark
                     .getGlobalActivityConditions().get(column);
               if (globalEvaluator == null)
               {
                  // no global condition set, continue evaluation with next column.
                  result = false;
               }
               else
               {
                  result = globalEvaluator.evaluate(bean);
                  if (result)
                  {
                     return column;
                  }
               }
            }
            else if (result)
            {
               return column;
            }
         }
         return 0;
      }
      catch (Exception e)
      {
         AuditTrailLogger.getInstance(LogCode.ENGINE)
         .warn(MessageFormat.format(
               "Failed to evaluate benchmark for activity instance {0}, benchmark value has been set to invalid (-1).",
               bean.getOID(), e));
         return BENCHMARKK_FAULT_VALUE;
      }
   }

   private int evaluateBenchmarkForPi(ProcessInstanceBean bean)
   {
      try
      {
         TreeMap<Integer, ConditionEvaluator> processConditions = benchmark
               .getActivityConditions(bean.getProcessDefinition().getId());
         ArrayList<Integer> columns = new ArrayList<Integer>(processConditions.keySet());

         Collections.reverse(columns);

         for (Integer column : columns)
         {
            ConditionEvaluator eval = processConditions.get(column);

            Boolean result = eval.evaluate(bean);

            if (result == null)
            {
               ConditionEvaluator globalEvaluator = benchmark
                     .getGlobalProcessConditions().get(column);

               if (globalEvaluator == null)
               {
                  // no global condition set, continue evaluation with next column.
                  result = false;
               }
               else
               {
                  result = globalEvaluator.evaluate(bean);
                  if (result)
                  {
                     return column;
                  }
               }
            }
            else if (result)
            {
               return column;
            }
         }
         return 0;
      }
      catch (Exception e)
      {
         AuditTrailLogger.getInstance(LogCode.ENGINE)
         .warn(MessageFormat.format(
               "Failed to evaluate benchmark for process instance {0}, benchmark has been set to invalid (-1).",
               bean.getOID(), e));
         return BENCHMARKK_FAULT_VALUE;
      }
   }

   protected Map<Long, BenchmarkDefinition> getBenchmarkCache(String partitionId)
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
