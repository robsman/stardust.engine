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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class POJOExceptionHandler implements MethodInterceptor
{
   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      try
      {
         return invocation.proceed();
      }
      catch (InvocationTargetException e)
      {
         Throwable targetException = e.getTargetException();
         LogUtils.traceException(targetException, false);
         throw targetException;
      }
      catch (Throwable e)
      {
         LogUtils.traceException(e, false);
         throw e;
      }
   }
}
