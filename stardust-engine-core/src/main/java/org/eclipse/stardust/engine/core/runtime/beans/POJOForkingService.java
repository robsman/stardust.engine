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

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.api.ejb2.beans.interceptors.ContainerConfigurationInterceptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class POJOForkingService implements ForkingService
{
   private final J2eeContainerType type;

   private final MyInvocationManager manager;
   private final ActionRunner isolator;

   public POJOForkingService(J2eeContainerType type)
   {
      this.type = type;
      ActionRunner serviceInstance = new MyActionRunner();
      manager = new MyInvocationManager(type, serviceInstance);
      this.isolator = (ActionRunner) Proxy.newProxyInstance(
            ActionRunner.class.getClassLoader(), new Class[] {ActionRunner.class},
            manager);
   }

   public Object isolate(Action action) throws PublicException
   {
      return isolator.execute(action);
   }

   public void fork(ActionCarrier order, boolean transacted)
   {
      if (transacted)
      {
         List successors = (List) Parameters.instance().get(EngineProperties.FORK_LIST);
         successors.add(order);
      }
      else
      {
         Runnable runnable = new MyManagedRunnable(order.createAction());
         new Thread(runnable).start();

      }
   }

   private static class MyInvocationManager extends InvocationManager
   {
      private static final long serialVersionUID = 1L;

      private static final String FORKING_SERVICE = "ForkingService";
      private static final String POJO_FORKING = "POJO.Forking";

      public MyInvocationManager(J2eeContainerType type, ActionRunner actionRunner)
      {
         super(actionRunner, setupInterceptors(type));
      }

      private static List setupInterceptors(J2eeContainerType type)
      {
         List interceptors = new ArrayList();

         interceptors.add(new ForkingDebugInterceptor());
         interceptors.add(new PropertyLayerProviderInterceptor());
         final Parameters parameters = Parameters.instance();
         interceptors.add(new MultipleTryInterceptor(parameters.getInteger(POJO_FORKING
               + JmsProperties.PROP_SUFFIX_MDB_RETRY_COUNT, 10), parameters.getInteger(
               POJO_FORKING + JmsProperties.PROP_SUFFIX_MDB_RETRY_PAUSE, 500)));
         interceptors.add(new ContainerConfigurationInterceptor(FORKING_SERVICE, type));
         interceptors.add(new POJOForkingInterceptor());
         interceptors.add(new POJOSessionInterceptor(SessionFactory.AUDIT_TRAIL));
         interceptors.add(new NonInteractiveSecurityContextInterceptor());
         interceptors.add(new RuntimeExtensionsInterceptor());
         interceptors.add(new POJOExceptionHandler());
         interceptors.add(new CallingInterceptor());

         return interceptors;
      }
   }

   private static class MyActionRunner implements ActionRunner
   {
      public Object execute(Action action)
      {
         return action.execute();
      }
   }

   private class MyManagedRunnable implements Runnable
   {
      private final Action action;

      public MyManagedRunnable(Action action)
      {
         this.action = action;
      }

      public void run()
      {
         new POJOForkingService(type).isolate(action);
      }
   }
}
