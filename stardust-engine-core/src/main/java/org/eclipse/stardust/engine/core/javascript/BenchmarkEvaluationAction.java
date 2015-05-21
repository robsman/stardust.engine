/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.javascript;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable.SymbolTableFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.BenchmarkEvaluator;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Script;

/**
 *
 * @author thomas.wolfram
 *
 */
public class BenchmarkEvaluationAction implements ContextAction
{
   private static final Logger trace = LogManager.getLogger(BenchmarkEvaluationAction.class);

   // ********** Global default benchmark formula **********
   private static final String DEFAULT_GLOBAL_BENCHMARK_FORMULA = "var benchmark=processInstance.getOID(); benchmark;";
   private static final String DEFAULT_GLOBAL_AI_BENCHMARK_FORMULA = "var benchmar=activityInstance.getOID(); benchmark;";

   private static final String KEY_MODEL_SCOPE = BenchmarkEvaluator.class.getName()
         + ".ModelScope";

   // ********** **********

   private ActivityInstanceBean aiBean;

   private ProcessInstanceBean piBean;

   public BenchmarkEvaluationAction(ActivityInstanceBean aiBean)
   {
      this.aiBean = aiBean;
   }

   public BenchmarkEvaluationAction(ProcessInstanceBean piBean)
   {
      this.piBean = piBean;
   }

   public Object run(Context cx)
   {
      GlobalVariablesScope modelScope = null;
      Script compiledFormula;
      try
      {

         if (this.aiBean != null)
         {
            modelScope = getModelScopeForActivityInstance(aiBean, cx);
            modelScope.bindThreadLocalSymbolTable(SymbolTableFactory.create(aiBean));
            compiledFormula = cx.compileString(DEFAULT_GLOBAL_AI_BENCHMARK_FORMULA,
                  "Benchmark", 1, null);            
         }
         else if (this.piBean != null)
         {
            modelScope = getModelScopeForProcessInstance(piBean, cx);
            modelScope.bindThreadLocalSymbolTable(SymbolTableFactory.create(piBean, null));
            cx.setOptimizationLevel( -1);
            compiledFormula = cx.compileString(DEFAULT_GLOBAL_BENCHMARK_FORMULA,
                  "Benchmark", 1, null);
         }
         else
         {
            throw new InternalException("Could not create variable scope.");
         }

         // Temporary Fix for js.jar dependency issue: Run always in interpretive mode
         // (-1)

         Object result = compiledFormula.exec(cx, modelScope);
         return result;
      }
      finally
      {
         modelScope.unbindThreadLocalSymbolTable();
      }
   }

   public static GlobalVariablesScope getModelScopeForActivityInstance(
         ActivityInstanceBean aiBean, Context cx)
   {
      IModel model = (IModel) aiBean.getActivity().getModel();

      GlobalVariablesScope modelScope = (GlobalVariablesScope) model.getRuntimeAttribute(KEY_MODEL_SCOPE);
      if (null == modelScope)
      {
         // initialize if needed

         modelScope = new GlobalVariablesScope(model, cx);
         cx.initStandardObjects(modelScope, false);
         cx.setWrapFactory(new JavaScriptWrapFactory());
         model.setRuntimeAttribute(KEY_MODEL_SCOPE, modelScope);
      }

      return modelScope;
   }

   public static GlobalVariablesScope getModelScopeForProcessInstance(
         ProcessInstanceBean piBean, Context cx)
   {
      IModel model = (IModel) piBean.getProcessDefinition().getModel();

      GlobalVariablesScope modelScope = (GlobalVariablesScope) model.getRuntimeAttribute(KEY_MODEL_SCOPE);
      if (null == modelScope)
      {
         // initialize if needed

         modelScope = new GlobalVariablesScope(model, cx);
         cx.initStandardObjects(modelScope, false);
         cx.setWrapFactory(new JavaScriptWrapFactory());
         model.setRuntimeAttribute(KEY_MODEL_SCOPE, modelScope);
      }

      return modelScope;
   }

}
