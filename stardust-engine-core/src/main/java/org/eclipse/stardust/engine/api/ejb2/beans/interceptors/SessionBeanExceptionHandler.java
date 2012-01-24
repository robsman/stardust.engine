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
package org.eclipse.stardust.engine.api.ejb2.beans.interceptors;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJBContext;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.ejb2.beans.EjbTxPolicy;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SessionBeanExceptionHandler implements MethodInterceptor
{
   public static final Logger trace = LogManager.getLogger(SessionBeanExceptionHandler.class);
   
   private final EjbTxPolicy ejbTxPolicy;

   private final EJBContext context;

   public SessionBeanExceptionHandler(EJBContext context)
   {
      this(context, null);
   }

   public SessionBeanExceptionHandler(EJBContext context, EjbTxPolicy ejbTxPolicy)
   {
      this.ejbTxPolicy = ejbTxPolicy;
      this.context = context;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      try
      {
         return invocation.proceed();
      }
      catch (InvocationTargetException e)
      {
         if ((null == ejbTxPolicy)
               || ejbTxPolicy.mustRollback(invocation, e.getTargetException()))
         {
            context.setRollbackOnly();
         }
         LogUtils.traceException(e.getTargetException(), false);
         if (e.getTargetException() instanceof ApplicationException)
         {
            throw e.getTargetException();
         }
         else
         {
            throw new InternalException(e.getTargetException().getMessage());
         }
      }
      catch (Throwable e)
      {
         if ((null == ejbTxPolicy) || ejbTxPolicy.mustRollback(invocation, e))
         {
            context.setRollbackOnly();
         }
         LogUtils.traceException(e, false);
         throw new InternalException(e.getMessage());
      }
   }
}
