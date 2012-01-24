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

import java.util.List;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
//import org.mozilla.javascript.Script;


/**
 * @author sauer
 * @version $Revision$
 */
public class TransitionConditionEvaluator
{

   private static final String KEY_EVALUATION_ACTION = TransitionConditionEvaluator.class.getName()
         + ".EvaluationAction";

   public static boolean isEnabled(final ITransition transition, final SymbolTable symbolTable)
   {
      try
      {
         TransitionEvaluationAction evaluationAction = getEvaluationAction(transition);
         
         try
         {
            evaluationAction.bindSymbolTableToThread(symbolTable);
            
            // TODO consider caching of context
            ContextFactory jsContextFactory = ContextFactory.getGlobal();

            return ((Boolean) jsContextFactory.call(evaluationAction)).booleanValue();
         }
         finally
         {
            evaluationAction.unbindSymbolTableFromThread();
         }
      }
      catch (Exception e)
      {
         throw new PublicException(e);
      }
   }

   public static void checkConsistency(final ITransition transition, List inconsistencies)
   {
      ContextFactory jsContextFactory = null;
      try
      {
         jsContextFactory = ContextFactory.getGlobal();
         Context cx = jsContextFactory.enter();
         /*GlobalVariablesScope modelScope = */TransitionEvaluationAction.getModelScope(transition, cx);
         /*Script compiledCondition = */TransitionEvaluationAction.getScriptForCondition(transition, cx);
//         compiledCondition.exec(cx, modelScope);
      }
      catch (Exception e)
      {
         inconsistencies.add(new Inconsistency(
               getErrorType(e) + ": " + e.getMessage(),
               transition, Inconsistency.WARNING));
      }
   }

   private static String getErrorType(Exception e)
   {
      String name = e.getClass().getName();
      int ix = name.lastIndexOf('.');
      return ix < 0 ? name : name.substring(ix + 1);
   }

   private static TransitionEvaluationAction getEvaluationAction(
         final ITransition transition)
   {
      TransitionEvaluationAction evaluationAction = (TransitionEvaluationAction) transition.getRuntimeAttribute(KEY_EVALUATION_ACTION);

      if (null == evaluationAction)
      {
         evaluationAction = new TransitionEvaluationAction(transition);

         // reuse evaluation action per transition
         transition.setRuntimeAttribute(KEY_EVALUATION_ACTION, evaluationAction);
      }

      return evaluationAction;
   }

}
