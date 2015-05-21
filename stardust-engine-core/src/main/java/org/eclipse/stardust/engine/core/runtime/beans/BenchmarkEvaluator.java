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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.javascript.BenchmarkEvaluationAction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class BenchmarkEvaluator implements IBenchmarkEvaluator
{

   private static final Logger trace = LogManager.getLogger(BenchmarkEvaluator.class);

   private static final int BENCHMARKK_FAULT_VALUE = 0;

   private BenchmarkDefinition benchmark;

   public BenchmarkEvaluator(long benchmarkOid)
   {

      // Retrieving benchmark from cache (currently mocked)
      benchmark = new BenchmarkDefinition(benchmarkOid);

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

   private static int evaluateBenchmarkForAi(ActivityInstanceBean bean)
   {

      try
      {
         ContextFactory jsContextFactory = ContextFactory.getGlobal();
         Object result = jsContextFactory.call(new BenchmarkEvaluationAction(bean));

         Double resultInt = Context.toNumber(result);
         if ( !resultInt.isNaN())
         {
            return (int) resultInt.doubleValue();
         }
         else
         {
            trace.warn("Benchmark of activity instance <" + bean.getOID()
                  + "> is not a number and will be set to fault value <"
                  + BENCHMARKK_FAULT_VALUE + ">");
         }
         return BENCHMARKK_FAULT_VALUE;
      }
      catch (Exception e)
      {
         trace.warn("Benchmark evaluation caused an excpetion. Activity benchmark will be set to fault value <"
               + BENCHMARKK_FAULT_VALUE + ">", e);
      }
      return 0;
   }

   
   private static int evaluateBenchmarkForPi(ProcessInstanceBean bean)
   {

      try
      {
         ContextFactory jsContextFactory = ContextFactory.getGlobal();
         Object result = jsContextFactory.call(new BenchmarkEvaluationAction(bean));

         Double resultInt = Context.toNumber(result);
         if ( !resultInt.isNaN())
         {
            return (int) resultInt.doubleValue();
         }
         else
         {
            trace.warn("Benchmark of process instance <" + bean.getOID()
                  + "> is not a number and will be set to fault value <"
                  + BENCHMARKK_FAULT_VALUE + ">");
         }
         return BENCHMARKK_FAULT_VALUE;
      }
      catch (Exception e)
      {
         trace.warn("Benchmark evaluation caused an excpetion. Process benchmark will be set to fault value <"
               + BENCHMARKK_FAULT_VALUE + ">", e);
      }
      return 0;
   }

}
