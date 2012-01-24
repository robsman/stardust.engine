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

import java.util.*;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class POJOForkingInterceptor implements MethodInterceptor
{
   private static final Logger trace = LogManager.getLogger(POJOForkingInterceptor.class);

   public POJOForkingInterceptor()
   {
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      Object result = null;

      List forkList = new ArrayList(5);

      Map locals = new HashMap();
      locals.put(EngineProperties.FORK_LIST, forkList);
      ParametersFacade.pushLayer(invocation.getParameters(), locals);

      try
      {
         result = invocation.proceed();
      }
      finally
      {
         ParametersFacade.popLayer(invocation.getParameters());
      }

      if ( !forkList.isEmpty())
      {
         for (Iterator i = forkList.iterator(); i.hasNext();)
         {
            ActionCarrier order = (ActionCarrier) i.next();

            ForkingServiceFactory factory = (ForkingServiceFactory) invocation
                  .getParameters().get(EngineProperties.FORKING_SERVICE_HOME);
            ForkingService service = factory.get();
            Runnable runnable = new ManagingRunner(order.createAction(), service);
            new Thread(runnable).start();
         }
      }
      return result;
   }

   public class ManagingRunner implements Runnable
   {
      private final Action managedRunnable;

      private ForkingService service;

      /**
       * @param managedRunnable
       */
      public ManagingRunner(Action managedRunnable, ForkingService service)
      {
         this.managedRunnable = managedRunnable;
         this.service = service;
      }

      public void run()
      {
         try
         {
            service.isolate(managedRunnable);
         }
         catch (Throwable e)
         {
            // No way to handle it besides logging

            trace.warn("Exceptionally terminating managed runnable.", e);
         }
         finally
         {
            // @todo (france, ub): ForkingServiceLocator.release(service);
         }
      }
   }
}
