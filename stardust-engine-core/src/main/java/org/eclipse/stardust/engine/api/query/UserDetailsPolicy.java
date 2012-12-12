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

   public UserDetailsLevel getLevel()
   {
      return level;
   }

   public String[] getPreferenceModules()
   {
      return moduleIds;
   }

   public void setPreferenceModules(String... moduleIds)
   {
      this.moduleIds = moduleIds;
   }
}
