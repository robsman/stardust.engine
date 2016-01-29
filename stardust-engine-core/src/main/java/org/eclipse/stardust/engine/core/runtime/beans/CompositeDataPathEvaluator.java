/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataPath;
import org.eclipse.stardust.engine.core.model.beans.DefaultConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.IConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.*;

public class CompositeDataPathEvaluator
{
   private IProcessInstance processInstance;
   private Map<IDataPath, Serializable> cache;
   private IConfigurationVariablesProvider variablesProvider;

   public CompositeDataPathEvaluator(IProcessInstance processInstance)
   {
      this.processInstance = processInstance;
      cache = new HashMap<IDataPath, Serializable>();
   }

   public Serializable getDataPathValue(IDataPath path)
   {
      Serializable result = cache.get(path);
      if (result == null)
      {
         IData data = path.getData();
         result = data == null
               ? evaluate(path)
               : (Serializable) processInstance.getInDataValue(data, path.getAccessPath());
         cache.put(path, result);
      }
      return result;
   }

   private String evaluate(IDataPath path)
   {
      String expression = path.getAccessPath();
      if (expression == null)
      {
         return null;
      }
      cache.put(path, "");
      return ConfigurationVariableUtils.evaluate(getVariablesProvider(), '%', expression);
   }

   private IConfigurationVariablesProvider getVariablesProvider()
   {
      if (variablesProvider == null)
      {
         variablesProvider = new DefaultConfigurationVariablesProvider();
         List<ConfigurationVariable> variables = new ArrayList<ConfigurationVariable>();
         variablesProvider.getConfigurationVariables().setConfigurationVariables(variables);
         for (IDataPath path : processInstance.getProcessDefinition().getDataPaths())
         {
            if (Direction.IN.isCompatibleWith(path.getDirection()))
            {
               variables.add(new DataPathVariable(this, path));
            }
         }
      }
      return variablesProvider;
   }

   private static class DataPathVariable extends ConfigurationVariable
   {
      private static final long serialVersionUID = 1L;

      private CompositeDataPathEvaluator evaluator;
      private IDataPath path;

      public DataPathVariable(CompositeDataPathEvaluator evaluator, IDataPath path)
      {
         super(new ConfigurationVariableDefinition(path.getId(), ConfigurationVariableScope.String, null, null, 0), null);

         this.evaluator = evaluator;
         this.path = path;
      }

      @Override
      public String getValue()
      {
         Serializable value = evaluator.getDataPathValue(path);
         return value == null ? null : value.toString();
      }
   }
}
