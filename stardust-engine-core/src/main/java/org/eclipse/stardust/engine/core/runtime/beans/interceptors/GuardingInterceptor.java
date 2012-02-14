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
package org.eclipse.stardust.engine.core.runtime.beans.interceptors;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.security.utils.SecurityUtils;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class GuardingInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = -2252311619932405039L;
   
   private static final String PREFIX = "org.eclipse.stardust.engine.api.runtime.";
   private static final String LEGACY_PREFIX = "ag.carnot.workflow.runtime.";
   
   private final String guardedParamName;
   private final String legacyGuardedParamName;

   public GuardingInterceptor(String serviceName)
   {
      this.guardedParamName = serviceName.substring(PREFIX.length()) + ".Guarded";
      this.legacyGuardedParamName = serviceName.substring(LEGACY_PREFIX.length()) + ".Guarded";
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      if (SecurityProperties.getUser() == null)
      {
         throw new AccessForbiddenException(BpmRuntimeError.AUTHx_NOT_LOGGED_IN.raise());
      }
      try
      {
         if (!invocation.getMethod().getDeclaringClass().getName().equals("java.lang.Object"))
         {
            // check if password must be changed
            SecurityUtils.checkPasswordExpired(SecurityProperties.getUser(), invocation);         
            
            if (invocation.getParameters().getBoolean(guardedParamName, true)
                  || invocation.getParameters().getBoolean(legacyGuardedParamName, true))
            {
               Authorization2.checkPermission(invocation.getMethod(), invocation.getArguments());
            }
         }
         return invocation.proceed();
      }
      finally
      {
         BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
         runtimeEnvironment.setAuthorizationPredicate(null);
      }
   }
}