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
package org.eclipse.stardust.engine.core.model.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableDefinition;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;



public class ValidationConfigurationVariablesProvider extends
      DefaultConfigurationVariablesProvider
{
   private Map<String, Serializable> preferences;

   public ValidationConfigurationVariablesProvider(Map<String, Serializable> preferences)
   {
      super();
      this.preferences = CollectionUtils.copyMap(preferences);
   }

   @Override
   protected void init(String modelId)
   {
      if (StringUtils.isEmpty(modelId))
      {
         super.init(modelId);
      }
      else
      {
         final ConfigurationVariables configurationVariables = ConfigurationVariableUtils
               .getConfigurationVariables(PreferenceStorageFactory.getCurrent(), modelId,
                     true, true);
         Map<String, Serializable> workingPrefs = CollectionUtils.copyMap(this.preferences);
         List<ConfigurationVariable> newList = CollectionUtils.newArrayList();

         // add existing variables with new values
         for (ConfigurationVariable variable : configurationVariables
               .getConfigurationVariables())
         {
            final String varName = variable.getName();
            if (workingPrefs.containsKey(varName))
            {
               String value;
               Serializable serializable = workingPrefs.get(varName);
               if (serializable == null)
               {
                  value = variable.getDefaultValue();
               }
               else
               {
                  value = serializable.toString();
               }

               newList.add(new ConfigurationVariable(variable, value));
               workingPrefs.remove(varName);
            }
            else
            {
               // in case it does not exists in new preferences then keep old one
               newList.add(variable);
            }
         }

         // add variables which did not exist before
         for (Entry<String, Serializable> prefName : workingPrefs.entrySet())
         {
            Serializable serializable = prefName.getValue();
            String value = serializable == null ? "" : serializable.toString();
            ConfigurationVariableDefinition definition = new ConfigurationVariableDefinition(
                  ConfigurationVariableUtils.getName(prefName.getKey()),
                  ConfigurationVariableUtils.getType(prefName.getKey()),
                  value, "", 0);
            newList.add(new ConfigurationVariable(definition, value));
         }

         configurationVariables.setConfigurationVariables(newList);

         setConfVariables(configurationVariables);
      }
   }
}