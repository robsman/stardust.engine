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
package org.eclipse.stardust.engine.core.preferences.configurationvariables;

import java.io.Serializable;
import java.util.List;

/**
 * Holds a List of ConfigurationVariables belonging to a specific modelId
 * 
 * @author roland.stamm
 *
 */
public class ConfigurationVariables implements Serializable
{
   private static final long serialVersionUID = 1L;

   private List<ConfigurationVariable> configurationVariables;

   private final String modelId;
   
   public ConfigurationVariables(String modelId)
   {
      this.modelId = modelId;
   }
   
   /**
    * Retrieves the modelId of the Model the <code>ConfigurationVariables</code> belong to.
    * 
    * @return {@link String} modelId
    */
   public String getModelId()
   {
      return modelId;
   }

   /**
    * Sets a List of <code>ConfigurationVariable</code>. Either a list formerly retrieved by
    * <code>getConfigurationVariables()</code> or completely new one.
    * 
    * @param configurationVariables
    */
   public void setConfigurationVariables(
         List<ConfigurationVariable> configurationVariables)
   {
      this.configurationVariables = configurationVariables;
   }
   
   /**
    * Retrieves a List of <code>ConfigurationVariable</code> belonging to the modelId <code>getModelId()</code>.
    * 
    * @return
    */
   public List<ConfigurationVariable> getConfigurationVariables()
   {
      return configurationVariables;
   }
   
   /**
    * 
    * Returns true if the List of <code>ConfigurationVariables</code> is empty 
    * 
    * @return {@link Boolean}
    */
   public boolean isEmpty()
   {
      return configurationVariables == null ? true : configurationVariables.isEmpty();
   }


   
}
