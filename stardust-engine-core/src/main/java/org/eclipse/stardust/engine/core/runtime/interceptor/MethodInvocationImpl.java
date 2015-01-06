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
package org.eclipse.stardust.engine.core.runtime.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MethodInvocationImpl implements MethodInvocation, Serializable
{
   private static final long serialVersionUID = 1L;

   private final Object target;

   private final Method method;
   private final Object[] arguments;

   private Object result;
   
   private Throwable exception;

   private final MethodInterceptor[] interceptors;
   
   private Parameters parameters;

   private int currentInterceptor = -1;

   public MethodInvocationImpl(Object target, Method method, Object[] args,
         List interceptors)
   {
      this.target = target;
      
      this.method = method;
      this.arguments = args;
      
      this.interceptors = (MethodInterceptor[]) interceptors
            .toArray(new MethodInterceptor[interceptors.size()]);
   }

   public Object execute() throws Throwable
   {
      try
      {
         this.parameters = Parameters.instance();

         Assert.condition(-1 == this.currentInterceptor, "Invalid method invocation "
               + "state, previous interceptor counter not reset.");

         this.currentInterceptor = -1;
   
         return proceed();
      }
      finally
      {
         Assert.condition(-1 == this.currentInterceptor, "Invalid method invocation "
               + "state, interceptor counter not reset.");

         this.parameters = null;
         this.result = null;
         this.exception = null;
      }
   }

   public Object getTarget()
   {
      return target;
   }

   public Method getMethod()
   {
      return method;
   }

   public Object[] getArguments()
   {
      return arguments;
   }
   
   public Object getResult()
   {
      return result;
   }
   
   public void setResult(Object result)
   {
      this.result = result;
   }

   public Throwable getException()
   {
      return exception;
   }

   public void setException(Throwable exception)
   {
      this.exception = exception;
   }

   public Parameters getParameters()
   {
      return parameters;
   }

   public Object proceed() throws Throwable
   {
      ++currentInterceptor;
      try
      {
         if (currentInterceptor >= this.interceptors.length)
         {
            throw new InternalException("All interceptors have already been invoked");
         }
         return this.interceptors[currentInterceptor].invoke(this);
      }
      finally
      {
         --currentInterceptor;
      }
   }
}
