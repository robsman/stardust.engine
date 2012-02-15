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
import java.util.Map;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.security.authentication.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;

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

   //@Deprecated
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

   public Object getService(Class service) throws ServiceNotAvailableException,
         LoginFailedException
   {
      String serviceName = service.getName();
      int dot = serviceName.lastIndexOf(".");
      String packageName = serviceName.substring(0, dot).replace(".api.", ".core.");
      String className = serviceName.substring(dot + 1);

      Object inner = Reflect.createInstance(packageName + ".beans." + className + "Impl");

      InvocationManager manager = new EmbeddedInvocationManager(inner,
            serviceName, withPropertyLayer, withLogin, autoFlush);

      Service result = (Service) Proxy.newProxyInstance(service.getClassLoader(),
            new Class[] {service, ManagedService.class}, manager);

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

   public void setCredentials(Map credentials)
   {
      username = (String) credentials.get(SecurityProperties.CRED_USER);
      password = (String) credentials.get(SecurityProperties.CRED_PASSWORD);
   }

   public static class EmbeddedInvocationManager extends InvocationManager
   {
      private static final long serialVersionUID = 1L;

      public EmbeddedInvocationManager(Object service, String serviceName,
            boolean withPropertyLayer, boolean withLogin, boolean autoFlush)
      {
         super(service, setupInterceptors(serviceName, withPropertyLayer, withLogin, autoFlush));
      }

      private static List setupInterceptors(String serviceName,
            boolean withPropertyLayer, boolean withLogin, boolean autoFlush)
      {
         List interceptors = new ArrayList();

         if (withPropertyLayer)
         {
            interceptors.add(new PropertyLayerProviderInterceptor());
         }
         if (autoFlush)
         {
            interceptors.add(new FlushInterceptor());
         }
         if (withLogin)
         {
            interceptors.add(new LoginInterceptor());
         }
         else
         {
            interceptors.add(new CurrentUserInterceptor());
         }
         interceptors.add(new GuardingInterceptor(serviceName));
         interceptors.add(new RuntimeExtensionsInterceptor());
         interceptors.add(new CallingInterceptor());
         return interceptors;
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
}
