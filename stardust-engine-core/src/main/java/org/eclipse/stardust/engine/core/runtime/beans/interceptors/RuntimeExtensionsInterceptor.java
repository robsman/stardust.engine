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

import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.internal.utils.RuntimeExtensionUtils;



/**
 * @author sauer
 * @version $Revision: $
 */
public class RuntimeExtensionsInterceptor implements MethodInterceptor
{

   private static final long serialVersionUID = 1L;

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      try
      {
         RuntimeExtensionUtils.configureExtensions(invocation);
         
         return invocation.proceed();
      }
      finally
      {
         RuntimeExtensionUtils.cleanupExtensions(invocation);
      }
   }

}
