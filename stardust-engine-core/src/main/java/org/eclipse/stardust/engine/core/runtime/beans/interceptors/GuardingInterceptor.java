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
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class GuardingInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = -2252311619932405039L;

   private final String paramName;

   public GuardingInterceptor(String serviceName)
   {
      paramName = serviceName.substring(serviceName.lastIndexOf('.') + 1) + ".Guarded";
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

            if (invocation.getParameters().getBoolean(paramName, true))
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
         runtimeEnvironment.setSecureContext(false);
      }
   }
}