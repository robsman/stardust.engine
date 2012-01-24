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
import java.rmi.RemoteException;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MultipleTryInterceptor implements MethodInterceptor
{
   private static final Logger trace = LogManager.getLogger(MultipleTryInterceptor.class);

   private final int maxTries;
   private final int pause;

   public MultipleTryInterceptor(int maxTries, int pause)
   {
      this.maxTries = maxTries;
      this.pause = pause;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      for (int triesLeft = maxTries; true;)
      {
         try
         {
            return invocation.proceed();
         }
         catch (InvocationTargetException e)
         {
            triesLeft = handleException(e, e.getTargetException(), triesLeft);
         }
         catch (RemoteException e)
         {
            triesLeft = handleException(e, e.detail, triesLeft);
         }
         catch (Throwable e)
         {
            triesLeft = handleException(e, e, triesLeft);
         }
      }
   }
   
   private int handleException(Throwable caughtException, Throwable cause, int triesLeft)
         throws Throwable
   {
      if (0 >= --triesLeft)
      {
         throw caughtException;
      }
      else
      {
         if (cause instanceof PublicException)
         {
            trace.warn("Expected exception : " + cause.getMessage() + ".");
         }
         else
         {
            trace.warn("Unexpected exception : " + cause.getMessage() + ".");
         }
         trace.warn("Retrying " + triesLeft + ((1 < triesLeft) ? " times." : " time."));

         try
         {
            Thread.sleep(pause);
         }
         catch (InterruptedException e1)
         {
         }
         
         return triesLeft;
      }
   }
}
