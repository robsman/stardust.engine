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
package org.eclipse.stardust.engine.api.spring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.core.runtime.beans.AbstractSessionAwareServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.runtime.beans.ServiceProviderFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.extensions.ejb.utils.J2EEUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;
import org.eclipse.stardust.engine.core.spi.runtime.IServiceProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SpringServiceFactory extends AbstractSessionAwareServiceFactory
      implements ApplicationContextAware
{
   private ApplicationContext appContext;

   private String username;

   private String password;

   private Principal userPrincipal;

   private Map properties = Collections.EMPTY_MAP;

   public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException
   {
      this.appContext = applicationContext;
   }

   protected ApplicationContext getApplicationContext()
   {
      if(this.appContext != null)
      {
         return this.appContext;
      }
      else
      {
         return SpringUtils.getWebApplicationContext();
      }
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public void setProperties(Map properties)
   {
      this.properties = properties;
   }

   protected <T extends Service> T getNewServiceInstance(Class<T> type)
   {
      IServiceProvider<T> provider = ServiceProviderFactory.findServiceProvider(type);

      ApplicationContext appContext = getApplicationContext();
      if (null == appContext)
      {
         appContext = SpringUtils.getApplicationContext();
      }

      T serviceBean = appContext.getBean(provider.getSpringBeanName(), type);
      if (SecurityProperties.isPrincipalBasedLogin() && userPrincipal == null
            || serviceBean instanceof AbstractSpringServiceBean
               && ((AbstractSpringServiceBean) serviceBean).getPrincipalProvider() != null)
      {
         return serviceBean;
      }

      LoggedInUser loggedInUser;
      if (null != userPrincipal)
      {
         loggedInUser = new LoggedInUser(J2EEUtils.getPrincipalName(userPrincipal),
               Collections.EMPTY_MAP);
      }
      else
      {
         loggedInUser = ((ManagedService) serviceBean).login(username, password, properties);
      }

      return (T) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[] {type, ManagedService.class},
            new SpringServiceInvocationHandler(serviceBean, loggedInUser));
   }

   public void setCredentials(Map credentials)
   {
      HttpServletRequest request = (HttpServletRequest) credentials.get("request");
      if (request != null)
      {
         if (null != request.getUserPrincipal())
         {
            this.userPrincipal = request.getUserPrincipal();
         }
         else
         {
            this.username = request.getParameter("username");
            this.password = request.getParameter("password");
         }
      }
      else
      {
         this.username = (String) credentials.get("user");
         this.password = (String) credentials.get("password");
      }
   }

   private static class SpringServiceInvocationHandler implements InvocationHandler
   {
      private Object bean;

      private LoggedInUser user;

      public SpringServiceInvocationHandler(Object bean, LoggedInUser user)
      {
         this.bean = bean;
         this.user = user;
      }

      public Object invoke(Object proxy, final Method method, final Object[] args)
            throws Throwable
      {
         InvokerPrincipal outerPrincipal = InvokerPrincipalUtils.getCurrent();
         try
         {
            if (null != user)
            {
               Object signedPrincipal = user.getProperties().get(InvokerPrincipal.PRP_SIGNED_PRINCIPAL);
               if (signedPrincipal instanceof InvokerPrincipal)
               {
                  // pass principal created during previous login
                  InvokerPrincipalUtils.setCurrent((InvokerPrincipal) signedPrincipal);
               }
               else
               {
                  InvokerPrincipalUtils.setCurrent(new InvokerPrincipal(user.getUserId(), user.getProperties()));
               }
            }
            else
            {
               InvokerPrincipalUtils.removeCurrent();
            }

            try
            {
               return method.invoke(bean, args);
            }
            catch (InvocationTargetException e)
            {
               Throwable target = e.getTargetException();
               if (target instanceof Exception)
               {
                  throw (Exception) target;
               }
               else
               {
                  throw new InternalException(e);
               }
            }
            catch (IllegalAccessException e)
            {
               throw new InternalException(e);
            }
         }
         finally
         {
            if (null != outerPrincipal)
            {
               InvokerPrincipalUtils.setCurrent(outerPrincipal);
            }
            else
            {
               InvokerPrincipalUtils.removeCurrent();
            }
         }
      }
   }

}
