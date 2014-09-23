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
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;

/**
 * Policy for specifying the level of detail for user details. The following level exist
 * {@link UserDetailsLevel#MINIMAL}, {@link UserDetailsLevel#CORE},
 * {@link UserDetailsLevel#WITH_PROPERTIES} and {@link UserDetailsLevel#FULL}.
 */
public class UserDetailsPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 1L;
   
   private UserDetailsLevel level;
   private String[] moduleIds;

   public UserDetailsPolicy(UserDetailsLevel level)
   {
      super();
      this.level = level;
   }

   /**
    * @return The level of details for UserDetailsPolicy.
    */
   public UserDetailsLevel getLevel()
   {
      return level;
   }

   /**
    * @return The module ids of the included preferences.
    */
   public String[] getPreferenceModules()
   {
      return moduleIds;
   }

   /**
    * Instruct the engine to include preferences from the specified modules.
    * 
    * @param moduleIds an array of module ids.
    */
   public void setPreferenceModules(String... moduleIds)
   {
      this.moduleIds = moduleIds;
   }
}
