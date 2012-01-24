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

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Undefined;


/**
 * @author sauer
 * @version $Revision$
 */
public final class TransitionEvaluationAction implements ContextAction
{

   private static final String KEY_MODEL_SCOPE = TransitionConditionEvaluator.class.getName()
         + ".ModelScope";

   private static final String KEY_COMPILED_CONDITION = TransitionConditionEvaluator.class.getName()
         + ".CompiledCondition";

   private final ITransition transition;

   private final ThreadLocal threadLocalSymbolTable = new ThreadLocal();
   
   public TransitionEvaluationAction(ITransition transition)
   {
      this.transition = transition;
   }

   public SymbolTable getSymbolTableForThread()
   {
      return (SymbolTable) threadLocalSymbolTable.get();
   }
   
   public void bindSymbolTableToThread(SymbolTable symbolTable)
   {
      threadLocalSymbolTable.set(symbolTable);
   }
   
   public void unbindSymbolTableFromThread()
   {
      threadLocalSymbolTable.set(null);
   }
   
   public Object run(Context cx)
   {
      GlobalVariablesScope modelScope = getModelScope(transition, cx);

      try
      {
         modelScope.bindThreadLocalSymbolTable(getSymbolTableForThread());
         
         Script compiledCondition = getScriptForCondition(transition, cx);
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

   public static GlobalVariablesScope getModelScope(ITransition transition, Context cx)
   {
      IModel model = (IModel) transition.getModel();

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

   public static Script getScriptForCondition(ITransition transition, Context cx)
   {
      Script compiledCondition = (Script) transition.getRuntimeAttribute(KEY_COMPILED_CONDITION);

      if (null == compiledCondition)
      {
         cx.setOptimizationLevel( -1);

         compiledCondition = cx.compileString(transition.getCondition(),
               transition.toString(), 1, null);

         transition.setRuntimeAttribute(KEY_COMPILED_CONDITION, compiledCondition);
      }

      return compiledCondition;
   }
}