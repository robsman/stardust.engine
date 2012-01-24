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

/**
 * Stores the changeable value of configuration-variable and also exposes non-changeable
 * information like name, defaultValue, description
 * 
 * @author roland.stamm
 * 
 */
public class ConfigurationVariable extends ConfigurationVariableDefinition implements
      IConfigurationVariableDefinition, Serializable
{
   private static final long serialVersionUID = 1L;

   private String value;

   public ConfigurationVariable(IConfigurationVariableDefinition definition,
         String value)
   {
      super(definition);
      this.value = value;
   }

   /**
    * @return The currently set value of the configuration-variable.
    */
   public String getValue()
   {
      return value;
   }

   /**
    * Changes the value of the configuration-variable. <br>
    * After modifying this value a
    * <code>saveConfigurationVariables(ConfigurationVariables)</code> API call has to be
    * made for the changes to take effect.<br>
    * <p>
    * Saving a value that equals the defaultValue has the same effect as saving a value of
    * null. It will remove the currently set value so the defaultValue takes priority.
    * DefaultValues are defined per model so this behavior prevents having default values
    * persistent over different model versions because a set value would take priority
    * over a future model versions defaultValue.
    * 
    * @param value a string defining the configuration variables runtime value
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   @Override
   public String toString()
   {
      return "(" + getName() + "," + value + "," + getDefaultValue() + ","
            + getDescription() + "," + getModelOid() +")";
   }
}
