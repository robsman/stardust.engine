/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.security;

import org.eclipse.stardust.engine.api.runtime.PermissionEvaluator;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;

public class PreferencesPermissionEvaluator extends PermissionEvaluator
{
   private static final String ASSUMPTION_MODULE_ID_KEY = "moduleId";

   public PreferencesPermissionEvaluator(String[] assumptions)
   {
      super(assumptions);
   }

   public boolean isAllowed(Object object)
   {
      
      MethodInvocation invocation = (MethodInvocation) object;

      Object[] arguments = invocation.getArguments();           
      
      String moduleId = findAssumptionValue(ASSUMPTION_MODULE_ID_KEY);

      // Arguments [1] = ModuleID
      // Arguments [2] = PreferencesID
      
      if (arguments[1].equals(moduleId))
      {
         return true;
      }

      return false;
   }

   private String findAssumptionValue(String assumption)
   {
      for (int i = 0; i < this.assumptions.length; i++ )
      {
         String permissionAssumption = this.assumptions[i].trim();

         if (permissionAssumption.startsWith(assumption.trim() + "="))
         {
            return permissionAssumption.substring(permissionAssumption.indexOf("=") + 1);
         }
      }

      return null;
   }

}
