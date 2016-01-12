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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.interceptor.TransactionPolicyAdvisor;
import org.eclipse.stardust.engine.core.spi.runtime.IServiceProvider;

public class EmbeddedServiceFactory extends DefaultServiceFactory
{
   private String password;

   private String username;

   private boolean withPropertyLayer;

   private boolean withLogin;

   private boolean autoFlush;

   /**
    * ServiceFactory that works within the same transaction
    * without creating a new Property Layer.
    *
    * <code>SecurityProperties.getUser()</code> must be set.
    *
    * @return The configured ServiceFactory.
    */
   public static EmbeddedServiceFactory CURRENT_TX()
   {
      return new EmbeddedServiceFactory(false, false, false);
   }

   /**
    * ServiceFactory that works within the same transaction
    * without creating a new Property Layer.
    *
    * <code>SecurityProperties.getUser()</code> must be set.
    *
    * @return The configured ServiceFactory.
    */
   public static EmbeddedServiceFactory CURRENT_TX_WITH_AUTO_FLUSH()
   {
      return new EmbeddedServiceFactory(false, false, true);
   }

   /**
    * ServiceFactory that works within the same transaction
    * with an added Property Layer.
    *
    * <code>SecurityProperties.getUser()</code> must be set.
    *
    * @return The configured ServiceFactory.
    */
   public static EmbeddedServiceFactory CURRENT_TX_WITH_PROPERTY_LAYER()
   {
      return new EmbeddedServiceFactory(false, true, false);
   }

   public EmbeddedServiceFactory()
   {
      this(true, true, false);
   }

   private EmbeddedServiceFactory(boolean withLogin, boolean withPropertyLayer, boolean autoFlush)
   {
     this.withLogin = withLogin;
     this.withPropertyLayer = withPropertyLayer;
     this.autoFlush = autoFlush;
   }

   public <T extends Service> T getService(Class<T> service)
         throws ServiceNotAvailableException, LoginFailedException
   {
      IServiceProvider<T> provider = ServiceProviderFactory.findServiceProvider(service);
      InvocationManager invocationHandler = createInvocationManager(provider, withPropertyLayer, withLogin, autoFlush);

      T result = (T) Proxy.newProxyInstance(service.getClassLoader(),
            new Class[] {service, ManagedService.class}, invocationHandler);

      if (withLogin)
      {
         if (username != null)
         {
            ((ManagedService) result).login(username, password, getProperties());
         }
      }
      else
      {
         if (SecurityProperties.getUser() == null)
         {
            throw new InternalException("User not initialized.");
         }
      }

      return result;
   }

   protected <T extends Service> EmbeddedInvocationManager createInvocationManager(
         IServiceProvider<T> provider, boolean withPropertyLayer, boolean withLogin, boolean autoFlush)
   {
      return new EmbeddedInvocationManager(provider.getInstance(),
            setupInterceptors(provider.getServiceName(), withPropertyLayer, withLogin, autoFlush));
   }

   protected List<MethodInterceptor> setupInterceptors(String serviceName,
         boolean withPropertyLayer, boolean withLogin, boolean autoFlush)
   {
      List<MethodInterceptor> interceptors = new ArrayList<MethodInterceptor>();

      if (withPropertyLayer)
      {
         interceptors.add(new PropertyLayerProviderInterceptor());
      }
      if (autoFlush)
      {
         interceptors.add(new FlushInterceptor());
      }
      interceptors.add(getLoginInterceptor(withLogin));
      // ExceptionHandlingInterceptor should be in front of GuardingInterceptor just for the case
      // that an exception is thrown during the authentication. With it the tx status
      // can be set accordantly.
      interceptors.add(new ExceptionHandlingInterceptor());
      interceptors.add(new GuardingInterceptor(serviceName));
      interceptors.add(new RuntimeExtensionsInterceptor());
      interceptors.add(new CallingInterceptor());
      return interceptors;
   }

   protected MethodInterceptor getLoginInterceptor(boolean withLogin)
   {
      if (withLogin)
      {
         return new LoginInterceptor();
      }
      else
      {
         return new CurrentUserInterceptor();
      }
   }

   public void setCredentials(Map credentials)
   {
      username = (String) credentials.get(SecurityProperties.CRED_USER);
      password = (String) credentials.get(SecurityProperties.CRED_PASSWORD);
   }

   public static class EmbeddedInvocationManager extends InvocationManager
   {
      private static final long serialVersionUID = 1L;

      public EmbeddedInvocationManager(Object service, List<MethodInterceptor> interceptors)
      {
         super(service, interceptors);
      }
   }

   private static class FlushInterceptor implements MethodInterceptor
   {
      private static final long serialVersionUID = 1L;

      public Object invoke(MethodInvocation invocation) throws Throwable
      {
         try
         {
            return invocation.proceed();
         }
         finally
         {
            ((Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)).flush();
         }
      }
   }

   private static class ExceptionHandlingInterceptor implements MethodInterceptor
   {
      private static final long serialVersionUID = 1L;

      public Object invoke(MethodInvocation invocation) throws Throwable
      {
         try
         {
            return invocation.proceed();
         }
         catch (InvocationTargetException e)
         {
            maybeTriggerTxRollback(invocation, e);

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
            maybeTriggerTxRollback(invocation, e);

            LogUtils.traceException(e, false);

            if (e instanceof ApplicationException)
            {
               throw e;
            }
            else
            {
               throw new InternalException(e.getMessage());
            }
         }
      }

      private static void maybeTriggerTxRollback(MethodInvocation invocation, Throwable e)
      {
         ITransactionStatus txStatus = TransactionUtils.getCurrentTxStatus();

         // rollback by default (if there is a TX) ...
         boolean mustRollback = (null != txStatus);
         if (txStatus instanceof TransactionPolicyAdvisor)
         {
            // ... but allow to be advised differently
            TransactionPolicyAdvisor txPolicyAdvisor = (TransactionPolicyAdvisor) txStatus;
            mustRollback = txPolicyAdvisor.mustRollback(invocation, e);
         }

         if (mustRollback)
         {
            txStatus.setRollbackOnly();
         }
      }
   }
}
