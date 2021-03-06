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

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
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

   private long benchmarkOid;

   public BenchmarkEvaluator(long benchmarkOid)
   {

      this.benchmarkOid = benchmarkOid;

   }

   @Override
   public int getBenchmarkForProcessInstance(long piOid)
   {
      ProcessInstanceBean piBean = ProcessInstanceBean.findByOID(piOid);

      int benchmarkValue = evaluateBenchmarkForPi(piBean);

      if (trace.isDebugEnabled())
      {
         trace.debug("Evaluating Benchmark with OID '" + this.benchmarkOid
               + "' for Process Instance OID '" + piOid + "'.");
      }

      return benchmarkValue;
   }

   @Override
   public int getBenchmarkForActivityInstance(long aiOid, String activityId)
   {

      ActivityInstanceBean aiBean = ActivityInstanceBean.findByOID(aiOid);

      int benchmarkValue = evaluateBenchmarkForAi(aiBean);

      if (trace.isDebugEnabled())
      {
         trace.debug("Evaluating Benchmark with OID '" + this.benchmarkOid
               + "' for Activity Instance OID '" + aiOid + "', activityId '" + activityId + "'.");
      }
      return benchmarkValue;
   }

   private String getQualifiedIdForProcess(IProcessDefinition pd)
   {

      return new QName(pd.getModel().getId(), pd.getId()).toString();
   }

   private int evaluateBenchmarkForAi(ActivityInstanceBean bean)
   {
      try
      {
         Pair<String, String> qualifiedActivityId = new Pair<String, String>(
               getQualifiedIdForProcess(bean.getActivity().getProcessDefinition()),
               bean.getActivity().getId());

         TreeMap<Integer, ConditionEvaluator> activityConditions = getBenchmarkDefinition()
               .getActivityConditions(qualifiedActivityId);

         if (activityConditions != null)
         {
            // evaluate activity scope.
            ArrayList<Integer> columns = new ArrayList<Integer>(
                  activityConditions.keySet());

            Collections.reverse(columns);

            for (Integer column : columns)
            {
               ConditionEvaluator evaluator = activityConditions.get(column);

               if (evaluator instanceof NoBenchmarkCondition || evaluator == null)
               {
                  // no condition set, continue evaluation with next column.
                  continue;
               }
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
         trace.warn(MessageFormat
               .format(
                     "Failed to evaluate benchmark for activity instance {0}, benchmark has been set to invalid (-1).",
                     bean.getOID())
               + " Error: " + e.getMessage());
         return BENCHMARKK_FAULT_VALUE;
      }
   }

   private BenchmarkDefinition getBenchmarkDefinition()
   {
      return BenchmarkUtils.getBenchmarkDefinition(benchmarkOid);
   }

   private int evaluateBenchmarkForPi(ProcessInstanceBean bean)
   {
      try
      {
         TreeMap<Integer, ConditionEvaluator> processConditions = getBenchmarkDefinition()
               .getProcessConditions(getQualifiedIdForProcess(bean.getProcessDefinition()));

         if (processConditions != null)
         {
            // evaluate process scope.
            ArrayList<Integer> columns = new ArrayList<Integer>(
                  processConditions.keySet());

            Collections.reverse(columns);

            for (Integer column : columns)
            {
               ConditionEvaluator evaluator = processConditions.get(column);

               if (evaluator instanceof NoBenchmarkCondition || evaluator == null)
               {
                  // no condition set, continue evaluation with next column.
                  continue;
               }
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
         trace.warn(MessageFormat
               .format(
                     "Failed to evaluate benchmark for process instance {0}, benchmark has been set to invalid (-1).",
                     bean.getOID())
               + " Error: " + e.getMessage());
         return BENCHMARKK_FAULT_VALUE;
      }
   }

}
