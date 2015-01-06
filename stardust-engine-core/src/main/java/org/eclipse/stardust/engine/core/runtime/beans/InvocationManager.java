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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocationImpl;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class InvocationManager implements InvocationHandler, Serializable
{
   private final Object service;
   
   private final List interceptors;
   
   public InvocationManager(Object serviceInstance, List interceptors)
   {
      this.service = serviceInstance;
      this.interceptors = interceptors;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // bind parameters to current thread for whole duration of call
      
      // bind current global properties, fix snapshot
      
      ParametersFacade.pushGlobals();

      EngineService.init();

      try
      {
         MethodInvocation invocation = new MethodInvocationImpl(service, method, args,
               interceptors);
         return invocation.execute();
      }
      finally
      {
         ParametersFacade.popGlobals();
      }
   }
}
