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

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;


/**
 * Evaluates the modeled quality assurance formula on an activity instance
 * See also {@link IActivity#getQualityAssuranceFormula()}
 * 
 * @author holger.prause
 * @version $Revision: $
 */
public class QualityAssuranceFormulaEvaluater
{
   private static final String KEY_EVALUATION_ACTION = QualityAssuranceFormulaEvaluater.class
         .getName() + ".EvaluationAction";

   public static boolean evaluate(final IActivityInstance activityInstance)
   {
      IActivity activity = activityInstance.getActivity();      
      if (!activity.isQualityAssuranceEnabled())
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("Illegal to attemtp to perform qa formula on non qa enabled activity. \n");
         errorMsg.append("activity instanceoid: "+activityInstance.getOID()+ "\n");
         errorMsg.append("activity id: "+activity.getId()+ "\n");
         
         throw new InternalException("Illegal to attemtp to perform qa formula on non qa enabled : "+activity.getId());
      }
      
      try
      {
         QualityAssuranceFormulaEvaluationAction evaluationAction = getEvaluationAction(activity);
         try
         {            
            evaluationAction.bindSymbolTableToThread(new SymbolTable()
            {
               public AccessPoint lookupSymbolType(String name)
               {
                  if (PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT.equals(name))
                  {
                     return activityInstance.getActivity().getAccessPoint(
                           PredefinedConstants.ENGINE_CONTEXT,
                           PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT);
                  }
                  return activityInstance.getProcessInstance().lookupSymbolType(name);
               }

               public Object lookupSymbol(String name)
               {
                  if (PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT.equals(name))
                  {
                     return activityInstance.getIntrinsicOutAccessPointValues().get(
                           PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT);
                  }
                  return activityInstance.getProcessInstance().lookupSymbol(name);
               }
            });
            

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

   public static void checkConsistency(final IActivity activity, List inconsistencies)
   {
      ContextFactory jsContextFactory = null;
      try
      {
         jsContextFactory = ContextFactory.getGlobal();
         Context cx = jsContextFactory.enter();
         /* GlobalVariablesScope modelScope = */QualityAssuranceFormulaEvaluationAction.getModelScope(
               activity, cx);
         /* Script compiledCondition = */QualityAssuranceFormulaEvaluationAction
               .getScriptForCondition(activity, cx);
         // compiledCondition.exec(cx, modelScope);
      }
      catch (Exception e)
      {
         inconsistencies.add(new Inconsistency(getErrorType(e) + ": " + e.getMessage(),
               activity, Inconsistency.WARNING));
      }
   }

   private static String getErrorType(Exception e)
   {
      String name = e.getClass().getName();
      int ix = name.lastIndexOf('.');
      return ix < 0 ? name : name.substring(ix + 1);
   }

   private static QualityAssuranceFormulaEvaluationAction getEvaluationAction(
         final IActivity activity)
   {
      QualityAssuranceFormulaEvaluationAction evaluationAction = (QualityAssuranceFormulaEvaluationAction) activity
            .getRuntimeAttribute(KEY_EVALUATION_ACTION);

      if (null == evaluationAction)
      {
         evaluationAction = new QualityAssuranceFormulaEvaluationAction(activity);

         // reuse evaluation action per activity
         activity.setRuntimeAttribute(KEY_EVALUATION_ACTION, evaluationAction);
      }

      return evaluationAction;
   }
}
