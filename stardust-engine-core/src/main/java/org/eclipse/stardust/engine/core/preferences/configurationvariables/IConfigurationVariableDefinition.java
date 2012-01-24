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

/**
 * Contains the basic unchangeable attributes of a configuration-variable.
 */
public interface IConfigurationVariableDefinition
{
   /**
    * @return the name as defined in the model.
    */
   String getName();

   /**
    * @return the defaultValue if one is defined in the model.
    */
   String getDefaultValue();

   /**
    * @return the description as defined in the model.
    */
   String getDescription();

   /**
    * If a configuration-variable is present in multiple model versions having the same
    * modelId, the configuration-variable is taken from most recent model.<br>
    * 
    * @return the modelOid from which the configuration variable originates from.
    */
   int getModelOid();
}
