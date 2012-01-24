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
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocationImpl;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class CallingInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = 1L;

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      try
      {
         Object result = invocation.getMethod().invoke(invocation.getTarget(), invocation.getArguments());
         
         // store result into invocation for later inspection
         if (invocation instanceof MethodInvocationImpl)
         {
            ((MethodInvocationImpl) invocation).setResult(result);
         }

         return result;
      }
      catch (Throwable t)
      {
         // store exception into invocation for later inspection
         if (invocation instanceof MethodInvocationImpl)
         {
            ((MethodInvocationImpl) invocation).setException(t);
         }

         throw t;
      }
   }
}
