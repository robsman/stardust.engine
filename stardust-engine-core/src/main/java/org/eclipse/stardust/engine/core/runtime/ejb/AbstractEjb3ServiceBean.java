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
package org.eclipse.stardust.engine.core.runtime.ejb;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.sql.DataSource;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;

/**
 * @author sauer
 * @version $Revision: $
 */
public abstract class AbstractEjb3ServiceBean implements Ejb3ManagedService
{
   @Resource
   protected SessionContext sessionContext;

   @Resource(mappedName = "jdbc/AuditTrail.DataSource")
   protected DataSource dataSource;

   @Resource(mappedName = "jcr/ContentRepository")
   protected Object repository;

   protected Object service;

   private InvocationManager invocationManager;

   protected Class<?> serviceType;

   protected Class<?> serviceTypeImpl;

   private String serviceTypeName;

   public AbstractEjb3ServiceBean()
   {
   }

   @PostConstruct
   public void init()
   {
      this.serviceTypeName = serviceType.getName();
      prepareInvocationManager(serviceTypeImpl);
      setupServiceProxy();
   }

   public LoggedInUser login(String username, String password, @SuppressWarnings("rawtypes") Map properties)
   {
      return ((ManagedService) service).login(username, password, properties);
   }

   protected Map<?, ?> initInvocationContext(TunneledContext tunneledContext)
   {
      if (null != tunneledContext)
      {
         if (null != tunneledContext.getInvokerPrincipal())
         {
            InvokerPrincipal principalBackup = InvokerPrincipalUtils
                  .setCurrent(tunneledContext.getInvokerPrincipal());
            if (null != principalBackup)
            {
               return Collections.singletonMap(InvokerPrincipal.class.getName(),
                     principalBackup);
            }
         }
      }
      return null;
   }

   protected void clearInvocationContext(TunneledContext tunneledContext, Map<?, ?> contextBackup)
   {
      if (null != tunneledContext)
      {
         if (null != tunneledContext.getInvokerPrincipal())
         {
            InvokerPrincipal backup = contextBackup == null ? null
                  : (InvokerPrincipal) contextBackup.get(InvokerPrincipal.class.getName());
            if (backup != null)
            {
               InvokerPrincipalUtils.setCurrent(backup);
            }
            else
            {
               InvokerPrincipalUtils.removeCurrent();
            }
         }
      }
   }

   private void prepareInvocationManager(Class<?> serviceImplType)
   {
      Object serviceInstance = null;
      try
      {
         serviceInstance = serviceImplType.newInstance();
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
      invocationManager = new SessionBeanInvocationManager(sessionContext, this,
            serviceInstance, serviceTypeName);
   }

   private void setupServiceProxy()
   {
      ClassLoader classLoader = getClass().getClassLoader();
      try
      {
         Class<?> serviceType = classLoader.loadClass(serviceTypeName);
         service = Proxy.newProxyInstance(classLoader, new Class[] {
               serviceType, ManagedService.class}, invocationManager);
      }
      catch (ClassNotFoundException e)
      {
         throw new PublicException("Failed loading service interface class.", e);
      }
   }

   public DataSource getDataSource()
   {
      return dataSource;
   }

   public ExecutorService getForkingService()
   {
      return null;
   }

   public Object getRepository()
   {
      return repository;
   }

   @Override
   public void remove()
   {
   }

   @Override
   public void logout()
   {
   }
}
