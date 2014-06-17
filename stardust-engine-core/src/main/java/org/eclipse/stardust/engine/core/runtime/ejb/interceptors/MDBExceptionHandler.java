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
package org.eclipse.stardust.engine.core.runtime.ejb.interceptors;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.MessageDrivenContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MDBExceptionHandler implements MethodInterceptor
{
   private static final long serialVersionUID = 1L;

   public static final Logger trace = LogManager.getLogger(MDBExceptionHandler.class);

   private final MessageDrivenContext context;

   private final boolean rollback;

   public MDBExceptionHandler(MessageDrivenContext context, boolean rollback)
   {
      this.context = context;
      this.rollback = rollback;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      try
      {
         return invocation.proceed();
      }
      catch (InvocationTargetException e)
      {
         processException(e.getTargetException());
      }
      catch (Throwable e)
      {
         processException(e);
      }
      return null;
   }

   private void processException(Throwable e)
   {
      trace.warn("", e);
      if (rollback)
      {
         try
         {
            trace.info("Failed handling message, message will be " + "rolled back.");

            context.setRollbackOnly();
         }
         catch (IllegalStateException e1)
         {
            trace.warn("Failed rolling back system message, message will "
                  + "be lost. Recovery run may be required.", e1);
         }
      }
   }
}
