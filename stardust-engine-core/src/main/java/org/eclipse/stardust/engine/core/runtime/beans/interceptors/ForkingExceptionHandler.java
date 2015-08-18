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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ForkingExceptionHandler implements MethodInterceptor
{
   private static final Logger trace = LogManager.getLogger(ForkingExceptionHandler.class);

   public ForkingExceptionHandler()
   {
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      try
      {
         return invocation.proceed();
      }
      catch (InvocationTargetException e)
      {
         trace.warn("", e.getTargetException());
         return null;
      }
   }
}
