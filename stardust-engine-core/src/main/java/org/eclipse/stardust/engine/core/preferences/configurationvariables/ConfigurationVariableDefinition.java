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

public class ConfigurationVariableDefinition implements IConfigurationVariableDefinition,
      Serializable
{
   private static final long serialVersionUID = 1L;
   
   private String name;
   private String defaultValue;
   private String description;
   private int modelOid;
   private ConfigurationVariableScope type;
   
   public ConfigurationVariableDefinition(IConfigurationVariableDefinition definition)
   {
      super();
      this.name = definition.getName();
      this.defaultValue = definition.getDefaultValue();
      this.description = definition.getDescription();
      this.modelOid = definition.getModelOid();
      this.type = definition.getType();
   }
   
   public ConfigurationVariableDefinition(String name, ConfigurationVariableScope type, String defaultValue,
         String description, int modelOid)
   {
      super();
      this.name = name;
      this.type = type;      
      this.defaultValue = defaultValue;
      this.description = description;
      this.modelOid = modelOid;
   }

   public String getName()
   {
      return name;
   }

   public String getDefaultValue()
   {
      return defaultValue;
   }

   public String getDescription()
   {
      return description;
   }
   
   public int getModelOid()
   {
      return modelOid;
   }
   
   @Override
   public String toString()
   {
      return "ConfVar: " + getName() + "(defaultValue:" + getDefaultValue() + ")";
   }

   @Override
   public ConfigurationVariableScope getType()
   {
      return type;
   }
}