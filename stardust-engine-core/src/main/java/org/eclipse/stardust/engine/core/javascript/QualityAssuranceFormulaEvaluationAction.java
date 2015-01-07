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

import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Undefined;


/**
 * This class will actually do the javascript evaluation on the qa formula
 * and is used by the {@link QualityAssuranceFormulaEvaluater}
 * @author holger.prause
 * @version $Revision: $
 */
public class QualityAssuranceFormulaEvaluationAction implements ContextAction
{

   private static final String KEY_MODEL_SCOPE = QualityAssuranceFormulaEvaluater.class.getName()
         + ".ModelScope";

   private static final String KEY_COMPILED_CONDITION = QualityAssuranceFormulaEvaluater.class.getName()
         + ".CompiledCondition";

   private final ThreadLocal<SymbolTable> threadLocalSymbolTable = new ThreadLocal<SymbolTable>();

   private final IActivity activity;

   public QualityAssuranceFormulaEvaluationAction(IActivity activity)
   {
      this.activity = activity;
   }

   public SymbolTable getSymbolTableForThread()
   {
      return threadLocalSymbolTable.get();
   }

   public void bindSymbolTableToThread(SymbolTable symbolTable)
   {
      threadLocalSymbolTable.set(symbolTable);
   }

   public void unbindSymbolTableFromThread()
   {
      threadLocalSymbolTable.remove();
   }

   public Object run(Context cx)
   {
      GlobalVariablesScope modelScope = getModelScope(activity, cx);

      try
      {
         modelScope.bindThreadLocalSymbolTable(getSymbolTableForThread());

         Script compiledCondition = getScriptForCondition(activity, cx);
         final Object result = compiledCondition.exec(cx, modelScope);

         if (Boolean.TRUE.equals(result) || result instanceof Undefined)
         {
            // return TRUE also for undefined results (e.g. conditions without
            // statements, consisting of comments only)
            return Boolean.TRUE;
         }
         else
         {
            return Boolean.FALSE;
         }
      }
      finally
      {
         modelScope.unbindThreadLocalSymbolTable();
      }
   }

   public static GlobalVariablesScope getModelScope(IActivity activity, Context cx)
   {
      IModel m = (IModel) activity.getModel();
      GlobalVariablesScope modelScope = (GlobalVariablesScope) m.getRuntimeAttribute(KEY_MODEL_SCOPE);
      if (null == modelScope)
      {
         // initialize if needed
         modelScope = new GlobalVariablesScope(m, cx);
         cx.initStandardObjects(modelScope, false);
         cx.setWrapFactory(new JavaScriptWrapFactory());
         m.setRuntimeAttribute(KEY_MODEL_SCOPE, modelScope);
      }

      return modelScope;
   }

   public static Script getScriptForCondition(IActivity activity, Context cx)
   {
      Script compiledCondition = (Script) activity.getRuntimeAttribute(KEY_COMPILED_CONDITION);

      if (null == compiledCondition)
      {
         cx.setOptimizationLevel( -1);

         compiledCondition = cx.compileString(activity.getQualityAssuranceFormula(),
               activity.toString(), 1, null);

         activity.setRuntimeAttribute(KEY_COMPILED_CONDITION, compiledCondition);
      }

      return compiledCondition;
   }
}
