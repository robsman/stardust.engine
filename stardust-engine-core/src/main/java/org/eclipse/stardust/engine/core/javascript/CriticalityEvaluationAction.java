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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable.SymbolTableFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.CriticalityEvaluator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Script;

/**
 *
 * @author thomas.wolfram
 *
 */
public class CriticalityEvaluationAction implements ContextAction
{
   private static final Logger trace = LogManager.getLogger(CriticalityEvaluator.class);

   // ********** Global default criticality formula **********
   private static final String DEFAULT_GLOBAL_CRITICALITY_FORMULA = "if(PROCESS_PRIORITY == -1) "
         + "{ "
         + "if(activityInstance.getActivity().getTargetExecutionTime() == 0) "
         + "var T = 86400; "
         + "else "
         + "var T = activityInstance.getActivity().getTargetExecutionTime(); "
         + "var Cp = 0; "
         + "var Mp = 10; "
         + "var t = activityInstance.getAge() / 1000; "
         + "Cp + (1 - Cp) * t/(Mp * T); "
         + "} "
         + "if(PROCESS_PRIORITY == 0) "
         + "{ "
         + "if(activityInstance.getActivity().getTargetExecutionTime() == 0) "
         + "var T = 86400; "
         + "else "
         + "var T = activityInstance.getActivity().getTargetExecutionTime(); "
         + "var Cp = 0.33; "
         + "var Mp = 10; "
         + "var t = activityInstance.getAge() / 1000; "
         + "Cp + (1 - Cp) * t/(Mp * T); "
         + "} "
         + "if(PROCESS_PRIORITY == 1) "
         + "{ "
         + "if(activityInstance.getActivity().getTargetExecutionTime() == 0) "
         + "var T = 86400; "
         + "else "
         + "var T = activityInstance.getActivity().getTargetExecutionTime(); "
         + "var Cp = 0.66; "
         + "var Mp = 10; "
         + "var t = activityInstance.getAge() / 1000; "
         + "Cp + (1 - Cp) * t/(Mp * T); "
         + "}";

   // ********** **********

   // TODO move to PredefinedConstants;
   private static final String MODEL_CRITICALITY_FORMULA = "ipp:criticalityFormula";

   public static final String DEFAULT_PREF_CRITICALITY_FORMULA = "Criticality.Formula.Default";

   private ActivityInstanceBean aiBean;

   private static final String KEY_MODEL_SCOPE = CriticalityEvaluator.class.getName()
         + ".ModelScope";

   public CriticalityEvaluationAction(ActivityInstanceBean aiBean)
   {
      this.aiBean = aiBean;
   }

   public Object run(Context cx)
   {
      GlobalVariablesScope modelScope = getModelScope(aiBean, cx);

      try
      {
         modelScope.bindThreadLocalSymbolTable(SymbolTableFactory.create(aiBean));

         // Temporary Fix for js.jar dependency issue: Run always in interpretive mode
         // (-1)
         cx.setOptimizationLevel( -1);
         Script compiledFormula = cx.compileString(getScriptForActivityInstance(aiBean),
               aiBean.getActivity().getModel().getName(), 1, null);

         Object result = compiledFormula.exec(cx, modelScope);
         return result;
      }
      finally
      {
         modelScope.unbindThreadLocalSymbolTable();
      }
   }

   public static GlobalVariablesScope getModelScope(ActivityInstanceBean aiBean,
         Context cx)
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

   private String getScriptForActivityInstance(ActivityInstanceBean aiBean)
   {
      if (aiBean.getActivity().getModel().getAttribute(MODEL_CRITICALITY_FORMULA) != null
            && aiBean.getActivity()
                  .getModel()
                  .getAttribute(MODEL_CRITICALITY_FORMULA)
                  .toString()
                  .trim()
                  .length() > 0)
      {
         return aiBean.getActivity()
               .getModel()
               .getAttribute(MODEL_CRITICALITY_FORMULA)
               .toString();
      }
      else
      {
         if (trace.isDebugEnabled())
         {
         trace.debug("The model with OID <"
               + aiBean.getActivity().getModel().getOID()
               + "> does not provide a criticality formula. Falling back to preference store default formula");
         }
         return getDefaultFormulaFromPreferences();
      }
   }

   private String getDefaultFormulaFromPreferences()
   {
      Preferences criticalityPreferences = PreferenceStorageFactory.getCurrent()
            .getPreferences(PreferenceScope.PARTITION,
                  PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
                  PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);

      if (criticalityPreferences.getPreferences().containsKey(
            DEFAULT_PREF_CRITICALITY_FORMULA)
            && criticalityPreferences.getPreferences()
                  .get(DEFAULT_PREF_CRITICALITY_FORMULA)
                  .toString()
                  .trim()
                  .length() > 0)
      {
         return criticalityPreferences.getPreferences()
               .get(DEFAULT_PREF_CRITICALITY_FORMULA)
               .toString();
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("No criticality formula provided in the Preference Store. Falling back to global default formula");
         }
         return DEFAULT_GLOBAL_CRITICALITY_FORMULA;
      }
   }
}
