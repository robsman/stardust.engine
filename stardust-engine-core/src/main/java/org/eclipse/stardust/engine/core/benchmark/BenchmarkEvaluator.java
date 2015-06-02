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
import java.util.TreeMap;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.IBenchmarkEvaluator;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

/**
 *
 * @author Thomas.Wolfram
 *
 */
public class BenchmarkEvaluator implements IBenchmarkEvaluator
{

   private static final Logger trace = LogManager.getLogger(BenchmarkEvaluator.class);

   private static final int BENCHMARKK_FAULT_VALUE = -1;

   private BenchmarkDefinition benchmark;

   public BenchmarkEvaluator(long benchmarkOid)
   {

      benchmark = BenchmarkUtils.getBenchmarkDefinition(benchmarkOid);

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

         if (activityConditions == null)
         {
            // evaluate only global scope;

            TreeMap<Integer, ConditionEvaluator> globalActivityConditions = benchmark.getGlobalActivityConditions();
            ArrayList<Integer> globalColumns = new ArrayList<Integer>(globalActivityConditions.keySet());

            Collections.reverse(globalColumns);

            for (Integer column: globalColumns)
            {
            // if no evaluator is present, default to global scope.
               ConditionEvaluator globalEvaluator = benchmark.getGlobalActivityConditions().get(column);
               if (globalEvaluator instanceof NoBenchmarkCondition
                     || globalEvaluator == null)
               {
                  // no global condition set, continue evaluation with next column.
                  continue;
               }
               else
               {
                  Boolean result = globalEvaluator.evaluate(bean);
                  if (result != null && result)
                  {
                     return column;
                  }
               }
            }
         }

         ArrayList<Integer> columns = new ArrayList<Integer>(activityConditions.keySet());

         Collections.reverse(columns);

         for (Integer column : columns)
         {
            ConditionEvaluator evaluator = activityConditions.get(column);

            if (evaluator instanceof NoBenchmarkCondition)
            {
               // no condition set, continue evaluation with next column.
               continue;
            }
            else if (evaluator instanceof DefaultCondition || evaluator == null)
            {
               // if no evaluator is present, default to global scope.
               ConditionEvaluator globalEvaluator = benchmark.getGlobalActivityConditions().get(column);

               if (globalEvaluator instanceof NoBenchmarkCondition
                     || globalEvaluator == null)
               {
                  // no global condition set, continue evaluation with next column.
                  continue;
               }
               else
               {
                  Boolean result = globalEvaluator.evaluate(bean);
                  if (result != null && result)
                  {
                     return column;
                  }
               }
            }
            else
            {
               Boolean result = evaluator.evaluate(bean);
               if (result != null && result)
               {
                  return column;
               }
            }
         }
         // no benchmark result everything was false.
         return 0;
      }
      catch (Exception e)
      {
         AuditTrailLogger
               .getInstance(LogCode.ENGINE)
               .warn(MessageFormat
                     .format(
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

         if (processConditions == null)
         {
            // evaluate only global scope;

            TreeMap<Integer, ConditionEvaluator> globalProcessConditions = benchmark.getGlobalProcessConditions();
            ArrayList<Integer> globalColumns = new ArrayList<Integer>(globalProcessConditions.keySet());

            for (Integer column: globalColumns)
            {
            // if no evaluator is present, default to global scope.
               ConditionEvaluator globalEvaluator = benchmark.getGlobalProcessConditions().get(column);
               if (globalEvaluator instanceof NoBenchmarkCondition
                     || globalEvaluator == null)
               {
                  // no global condition set, continue evaluation with next column.
                  continue;
               }
               else
               {
                  Boolean result = globalEvaluator.evaluate(bean);
                  if (result != null && result)
                  {
                     return column;
                  }
               }
            }
         }

         ArrayList<Integer> columns = new ArrayList<Integer>(processConditions.keySet());

         Collections.reverse(columns);

         for (Integer column : columns)
         {
            ConditionEvaluator evaluator = processConditions.get(column);

            if (evaluator instanceof NoBenchmarkCondition)
            {
               // no condition set, continue evaluation with next column.
               continue;
            }
            else if (evaluator instanceof DefaultCondition || evaluator == null)
            {
               // if no evaluator is present, default to global scope.
               ConditionEvaluator globalEvaluator = benchmark.getGlobalProcessConditions().get(column);

               if (globalEvaluator instanceof NoBenchmarkCondition
                     || globalEvaluator == null)
               {
                  // no global condition set, continue evaluation with next column.
                  continue;
               }
               else
               {
                  Boolean result = globalEvaluator.evaluate(bean);
                  if (result != null && result)
                  {
                     return column;
                  }
               }
            }
            else
            {
               Boolean result = evaluator.evaluate(bean);
               if (result != null && result)
               {
                  return column;
               }
            }
         }
         // no benchmark result everything was false.
         return 0;
      }
      catch (Exception e)
      {
         AuditTrailLogger
               .getInstance(LogCode.ENGINE)
               .warn(MessageFormat
                     .format(
                           "Failed to evaluate benchmark for process instance {0}, benchmark has been set to invalid (-1).",
                           bean.getOID(), e));
         return BENCHMARKK_FAULT_VALUE;
      }
   }

}
