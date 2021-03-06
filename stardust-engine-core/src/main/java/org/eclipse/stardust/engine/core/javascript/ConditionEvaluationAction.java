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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Undefined;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * @author sauer
 * @version $Revision$
 */
public final class ConditionEvaluationAction implements ContextAction
{

   private static final String KEY_MODEL_SCOPE = ConditionEvaluationAction.class.getName()
         + ".ModelScope";

   private static final String KEY_COMPILED_CONDITION = ConditionEvaluationAction.class.getName()
         + ".CompiledCondition";

   private final ModelElement modelElement;

   private final ThreadLocal<SymbolTable> threadLocalSymbolTable = new ThreadLocal<SymbolTable>();

   private String condition;

   public ConditionEvaluationAction(ModelElement modelElement, String condition)
   {
      this.modelElement = modelElement;
      this.condition = condition;
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
      GlobalVariablesScope modelScope = getModelScope(modelElement, cx);

      try
      {
         modelScope.bindThreadLocalSymbolTable(getSymbolTableForThread());

         Script compiledCondition = getScriptForCondition(modelElement, cx, condition);
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

   public static GlobalVariablesScope getModelScope(ModelElement modelElement, Context cx)
   {
      IModel model = (IModel) modelElement.getModel();

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

   public static Script getScriptForCondition(ModelElement modelElement, Context cx, String condition)
   {
      Script compiledCondition = (Script) modelElement.getRuntimeAttribute(KEY_COMPILED_CONDITION);

      if (null == compiledCondition)
      {
         cx.setOptimizationLevel( -1);

         compiledCondition = cx.compileString(condition, modelElement.toString(), 1, null);

         modelElement.setRuntimeAttribute(KEY_COMPILED_CONDITION, compiledCondition);
      }

      return compiledCondition;
   }
}