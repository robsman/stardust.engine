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

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


public class CurrentUserInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = 1L;

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      if (SecurityProperties.getUser() == null)
      {
         throw new InternalException("User not initialized.");
      }

      return invocation.proceed();
   }

}
