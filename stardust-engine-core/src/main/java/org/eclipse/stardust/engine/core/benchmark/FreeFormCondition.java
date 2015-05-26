/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.benchmark;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.javascript.BenchmarkEvaluationAction;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class FreeFormCondition implements ConditionEvaluator
{
   protected String javaScript;

   private static final Logger trace = LogManager.getLogger(FreeFormCondition.class);

   public FreeFormCondition(String javaScript)
   {
      this.javaScript = javaScript;
   }

   @Override
   public Boolean evaluate(ActivityInstanceBean ai)
   {
      try
      {
         ContextFactory jsContextFactory = ContextFactory.getGlobal();
         Object result = jsContextFactory.call(new BenchmarkEvaluationAction(ai, javaScript));

         boolean resultBool = Context.toBoolean(result);

         return resultBool;
      }
      catch (Exception e)
      {
         trace.warn(
               "Benchmark evaluation caused an excpetion. Benchmark for AI "
                     + ai.getOID() + " will not be evaluated", e);
      }
      return false;
   }

   @Override
   public Boolean evaluate(ProcessInstanceBean pi)
   {
      try
      {
         ContextFactory jsContextFactory = ContextFactory.getGlobal();
         Object result = jsContextFactory.call(new BenchmarkEvaluationAction(pi, javaScript));

         boolean resultBool = Context.toBoolean(result);

         return resultBool;
      }
      catch (Exception e)
      {
         trace.warn(
               "Benchmark evaluation caused an excpetion. Benchmark for PI "
                     + pi.getOID() + " will not be evaluated", e);
      }
      return false;
   }
}
