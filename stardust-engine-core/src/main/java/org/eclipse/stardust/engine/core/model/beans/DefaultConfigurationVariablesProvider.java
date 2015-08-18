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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.IConfigurationVariableDefinition;



public class DefaultConfigurationVariablesProvider implements
      IConfigurationVariablesProvider
{
   private ConfigurationVariables confVariables;
   private Set<String> conVarCandidateNames = CollectionUtils.newHashSet();

   public DefaultConfigurationVariablesProvider()
   {
      init("");
   }

   public ConfigurationVariables getConfigurationVariables()
   {
      return confVariables;
   }

   public void register(IConfigurationVariableDefinition definition)
   {
      final List<ConfigurationVariable> configurationVariables = confVariables
            .getConfigurationVariables();

      // Do nothing if a value already exists for variable definition, e.g. in preference store.
      for (ConfigurationVariable configurationVariable : configurationVariables)
      {
         if (configurationVariable.getName().equals(definition.getName()))
         {
            return;
         }
      }

      // no value, therefore default value will be added.
      configurationVariables.add(new ConfigurationVariable(definition, definition
            .getDefaultValue()));
   }
   
   public void registerCandidate(String name) 
   {
      conVarCandidateNames.add(name);
   }
   
   public void resetModelId(String modelId)
   {
      if (confVariables == null
            || !CompareHelper.areEqual(confVariables.getModelId(), modelId))
      {
         init(modelId);
         conVarCandidateNames = CollectionUtils.newHashSet();
      }
   }

   public String getModelId()
   {
      return confVariables.getModelId();
   }
   
   public Set<String> getConVarCandidateNames() 
   {
      return Collections.unmodifiableSet(conVarCandidateNames);
   }
   
   protected void setConfVariables(ConfigurationVariables confVariables)
   {
      this.confVariables = confVariables;
   }
   
   protected void init(String modelId)
   {
      setConfVariables(new ConfigurationVariables(modelId));
      confVariables.setConfigurationVariables(CollectionUtils
            .<ConfigurationVariable> newArrayList());
   }
}
