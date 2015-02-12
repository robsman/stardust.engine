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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
//import org.mozilla.javascript.Script;





import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.el.*;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * @author sauer
 * @version $Revision$
 */
public class ConditionEvaluator
{

   private static final String KEY_EVALUATION_ACTION = ConditionEvaluator.class.getName()
         + ".EvaluationAction";
   
   private static final String PARSED_EL_EXPRESSION = "ParsedElExpression";
   
   public static final String ECMASCRIPT_TYPE = "text/ecmascript"; 
   public static final String JAVASCRIPT_TYPE = "text/ecmascript";

   public static boolean isEnabled(final ModelElement modelElement, final SymbolTable symbolTable,
         String condition)
   {
      final IModel model = (IModel) modelElement.getModel();
      String type = model.getScripting().getType();
      if (ECMASCRIPT_TYPE.equals(type) || JAVASCRIPT_TYPE.equals(type))
      {
         if (condition.startsWith(PredefinedConstants.CARNOT_EL_PREFIX))
         {
            // fallback to carnotEL evaluation.
            condition = condition.substring(PredefinedConstants.CARNOT_EL_PREFIX.length());
         }
         else if (XMLConstants.CONDITION_OTHERWISE_VALUE.equals(condition))
         {
            return false;
         }
         else
         {
            // pure ecmascript
            return ConditionEvaluator.isEnabledECMA(modelElement, symbolTable, condition);
         }
      }
      // unsupported scripting languages will throw an exception here.
      try
      {
         if ("true".equals(condition))
         {
            // immediately return "true" (for conditions containing "true")
            return true;
         }
         BooleanExpression expression = (BooleanExpression) modelElement.getRuntimeAttribute(PARSED_EL_EXPRESSION);
         if (null == expression)
         {
            // for compatibility, correct the new default "true" to the old default "TRUE"
            if ("true".equals(condition))
            {
               condition = "TRUE";
            }
            expression = Interpreter.parse(condition);
            modelElement.setRuntimeAttribute(PARSED_EL_EXPRESSION, expression);
         }
         return Result.TRUE.equals(Interpreter.evaluate(expression, symbolTable));
      }
      catch (SyntaxError x)
      {
         throw new InternalException(x);
      }
      catch (EvaluationError x)
      {
         throw new InternalException(x);
      }
   }
   
   private static boolean isEnabledECMA(final ModelElement modelElement, final SymbolTable symbolTable,
         String condition)
   {
      try
      {
         ConditionEvaluationAction evaluationAction = getEvaluationAction(modelElement, condition);
         
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
   
   public static boolean isOtherwiseEnabled(ModelElement modelElement, SymbolTable symbolTable,
         String condition)
   {
      String elType = ((IModel) modelElement.getModel()).getScripting().getType();
      if (ECMASCRIPT_TYPE.equals(elType) || JAVASCRIPT_TYPE.equals(elType))
      {
         return XMLConstants.CONDITION_OTHERWISE_VALUE.equals(condition);
      }
      else
      {
         try
         {
            BooleanExpression expression = (BooleanExpression) modelElement.getRuntimeAttribute(PARSED_EL_EXPRESSION);
            if (null == expression)
            {
               expression = Interpreter.parse(condition);
               modelElement.setRuntimeAttribute(PARSED_EL_EXPRESSION, expression);
            }
            return Result.OTHERWISE.equals(Interpreter.evaluate(expression, symbolTable));
         }
         catch (SyntaxError x)
         {
            //throw new InternalException(x);
            return false;
         }
         catch (EvaluationError x)
         {
            throw new InternalException(x);
         }
      }
   }

   public static void checkConsistency(final ModelElement modelElement, List inconsistencies,
         String condition)
   {
      ContextFactory jsContextFactory = null;
      try
      {
         jsContextFactory = ContextFactory.getGlobal();
         Context cx = jsContextFactory.enterContext();
         /*GlobalVariablesScope modelScope = */ConditionEvaluationAction.getModelScope(modelElement, cx);
         /*Script compiledCondition = */ConditionEvaluationAction.getScriptForCondition(modelElement, cx, condition);
//         compiledCondition.exec(cx, modelScope);
      }
      catch (Exception e)
      {
         inconsistencies.add(new Inconsistency(
               getErrorType(e) + ": " + e.getMessage(),
               modelElement, Inconsistency.WARNING));
      }
   }

   private static String getErrorType(Exception e)
   {
      String name = e.getClass().getName();
      int ix = name.lastIndexOf('.');
      return ix < 0 ? name : name.substring(ix + 1);
   }

   private static ConditionEvaluationAction getEvaluationAction(
         final ModelElement model, String condition)
   {
      ConditionEvaluationAction evaluationAction = (ConditionEvaluationAction) model.getRuntimeAttribute(KEY_EVALUATION_ACTION);

      if (null == evaluationAction)
      {
         evaluationAction = new ConditionEvaluationAction(model, condition);

         // reuse evaluation action per transition
         model.setRuntimeAttribute(KEY_EVALUATION_ACTION, evaluationAction);
      }

      return evaluationAction;
   }

}
