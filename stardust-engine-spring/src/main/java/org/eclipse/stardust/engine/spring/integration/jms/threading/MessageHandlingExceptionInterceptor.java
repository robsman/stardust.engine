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
package org.eclipse.stardust.engine.spring.integration.jms.threading;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.MDBExceptionHandler;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author sauer
 * @version $Revision: $
 */
public class MessageHandlingExceptionInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = 1L;

   public static final Logger trace = LogManager.getLogger(MDBExceptionHandler.class);

   private final boolean rollbackOnError;

   public MessageHandlingExceptionInterceptor(boolean rollbackOnError)
   {
      this.rollbackOnError = rollbackOnError;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      try
      {
         return invocation.proceed();
      }
      catch (InvocationTargetException e)
      {
         processException(e.getTargetException(), invocation.getParameters());

         return null;
      }
      catch (Throwable e)
      {
         processException(e, invocation.getParameters());
         return null;
      }
   }

   private void processException(Throwable e, Parameters params)
   {
      trace.warn("", e);
      if (rollbackOnError)
      {
         try
         {
            ITransactionStatus txStatus = TransactionUtils.getCurrentTxStatus(params);
            if (null != txStatus)
            {
               trace.info("Failed handling message, message will be rolled back.");

               txStatus.setRollbackOnly();
            }
            else
            {
               throw new IllegalStateException("There is no TX active.");
            }
         }
         catch (IllegalStateException ise)
         {
            trace.warn("Failed rolling back JMS message, message will be lost. Recovery "
                  + "run may be required.", ise);
         }
      }
   }
}
