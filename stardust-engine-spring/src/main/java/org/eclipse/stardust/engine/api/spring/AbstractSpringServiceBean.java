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

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.spi.jms.IJmsResourceProvider;
import org.eclipse.stardust.engine.core.spi.security.PrincipalProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.PlatformTransactionManager;

import org.eclipse.stardust.vfs.IDocumentRepositoryService;


public abstract class AbstractSpringServiceBean
      implements ManagedService, ApplicationContextAware, BeanFactoryAware, InitializingBean, ISpringServiceBean
{
   private final Class serviceInterfaceType;

   private final Class serviceImplType;

   private ApplicationContext applicationContext;

   private BeanFactory beanFactory;

   private PlatformTransactionManager txManager;

   private DataSource dataSource;
   
   private IDocumentRepositoryService dmsProvider;
   
   private IJmsResourceProvider jmsResourceProvider;

   private ForkingServiceFactory forkingServiceFactory;
   
   private PrincipalProvider principalProvider;
   
   private Properties properties;

   protected Object serviceProxy;

   protected AbstractSpringServiceBean(Class serviceInterfaceType, Class serviceImplType)
   {
      this.serviceInterfaceType = serviceInterfaceType;
      this.serviceImplType = serviceImplType;
   }

   public ApplicationContext getApplicationContext()
   {
      return applicationContext;
   }

   public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException
   {
      this.applicationContext = applicationContext;
   }

   public BeanFactory getBeanFactory()
   {
      return beanFactory;
   }

   public void setBeanFactory(BeanFactory beanFactory)
   {
      this.beanFactory = beanFactory;
   }

   public DataSource getDataSource()
   {
      return dataSource;
   }

   public void setDataSource(DataSource dataSource)
   {
      this.dataSource = dataSource;
   }

   public IDocumentRepositoryService getDmsProvider()
   {
      return dmsProvider;
   }

   public void setDmsProvider(IDocumentRepositoryService dmsProvider)
   {
      this.dmsProvider = dmsProvider;
   }

   public IJmsResourceProvider getJmsResourceProvider()
   {
      return jmsResourceProvider;
   }

   public void setJmsResourceProvider(IJmsResourceProvider jmsResourceProvider)
   {
      this.jmsResourceProvider = jmsResourceProvider;
   }

   public PlatformTransactionManager getTransactionManager()
   {
      return txManager;
   }

   public void setTransactionManager(PlatformTransactionManager txManager)
   {
      this.txManager = txManager;
   }

   public ForkingServiceFactory getForkingServiceFactory()
   {
      return forkingServiceFactory;
   }

   public void setForkingServiceFactory(ForkingServiceFactory forkingServiceFactory)
   {
      this.forkingServiceFactory = forkingServiceFactory;
   }

   public PrincipalProvider getPrincipalProvider()
   {
      return principalProvider;
   }

   public void setPrincipalProvider(PrincipalProvider principalProvider)
   {
      this.principalProvider = principalProvider;
   }

   public Properties getCarnotProperties()
   {
      return properties;
   }

   public void setCarnotProperties(Properties properties)
   {
      this.properties = properties;
   }

   public void afterPropertiesSet() throws Exception
   {
      if (null == this.txManager)
      {
         throw new IllegalArgumentException("transactionManager is required");
      }
      if (null == this.dataSource)
      {
         throw new IllegalArgumentException("auditTrailDataSource is required");
      }

      Object serviceInstance = null;
      try
      {
         serviceInstance = serviceImplType.newInstance();
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }

      InvocationManager invocationManager = new SpringInvocationManager(this,
            serviceInstance, serviceInterfaceType.getName());

      ClassLoader classLoader = getClass().getClassLoader();
      this.serviceProxy = Proxy.newProxyInstance(classLoader, new Class[]
      {
            serviceInterfaceType, ManagedService.class
      }, invocationManager);
   }

   public void login(String username, String password)
   {
      login(username, password, Collections.EMPTY_MAP);
   }

   public LoggedInUser login(String username, String password, Map properties)
   {
      InvokerPrincipal outerPrincipal = InvokerPrincipalUtils.getCurrent();
      try
      {
         InvokerPrincipalUtils.removeCurrent();
         
         return ((ManagedService) serviceProxy).login(username, password, properties);
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

   public void logout()
   {
      // TODO review
      ((ManagedService) serviceProxy).logout();
   }

   public void remove()
   {
      // nothing to be done
   }
}
