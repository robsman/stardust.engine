/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.spi.runtime.IServiceProvider;

public final class ServiceProviderFactory implements IServiceProvider.Factory
{
   private static final ServiceLoader<IServiceProvider.Factory> factoryLoader = ServiceLoader.load(IServiceProvider.Factory.class);

   public static <T extends Service> IServiceProvider<T> findServiceProvider(Class<T> clazz)
   {
      for (IServiceProvider.Factory factory : factoryLoader)
      {
         IServiceProvider provider = factory.get(clazz);
         if (provider != null)
         {
            return provider;
         }
      }
      throw new ServiceNotAvailableException(String.valueOf(clazz));
   }

   private static Map<Class, IServiceProvider> providers;

   static
   {
      providers = CollectionUtils.newMap();
      providers.put(AdministrationService.class, new Provider("AdministrationService"));
      providers.put(WorkflowService.class, new Provider("WorkflowService"));
      providers.put(QueryService.class, new Provider("QueryService"));
      providers.put(UserService.class, new Provider("UserService"));
      providers.put(DocumentManagementService.class, new Provider("DocumentManagementService"));
   }

   @Override
   public <T extends Service> IServiceProvider get(Class<T> clazz)
   {
      return providers.get(clazz);
   }

   public static class Provider<T extends Service> implements IServiceProvider
   {
      private String name;
      private String localName;
      private String serviceName;
      private String instanceClassName;
      private String springBeanName;
      private String jndiPropertyName;
      private String ejbHomeClassName;
      private String ejbRemoteClassName;
      private String localHomeClassName;
      private String EJB3ModuleName;
      private String localEJB3ClassName;
      private String remoteEJB3ClassName;

      private transient T instance;
      private transient Class<T> instanceClass;
      private transient Class<?> ejbHomeClass;
      private transient Class<?> ejbRemoteClass;
      private transient Class<?> localHomeClass;

      private Provider(String name)
      {
         this(name,
              "Local" + name,
              "org.eclipse.stardust.engine.api.runtime." + name,
              "org.eclipse.stardust.engine.core.runtime.beans." + name + "Impl",
              "carnot" + name,
              name + ".JndiName",
              "org.eclipse.stardust.engine.api.ejb2.Remote" + name + "Home",
              "org.eclipse.stardust.engine.api.ejb2.Remote" + name,
              "org.eclipse.stardust.engine.api.ejb2.Local" + name + "Home",
              "carnot-ejb3",
              "org.eclipse.stardust.engine.api.ejb3.beans." + name,
              "org.eclipse.stardust.engine.api.ejb3.beans.Remote" + name);
      }

      public Provider(String name, String localName,
            String serviceName, String instanceClassName,
            String springBeanName, String jndiPropertyName,
            String ejbHomeClassName, String ejbRemoteClassName,
            String localHomeClassName, String EJB3ModuleName,
            String localEJB3ClassName, String remoteEJB3ClassName)
      {
         this.name = name;
         this.localName = localName;
         this.serviceName = serviceName;
         this.instanceClassName = instanceClassName;
         this.springBeanName = springBeanName;
         this.jndiPropertyName = jndiPropertyName;
         this.ejbHomeClassName = ejbHomeClassName;
         this.ejbRemoteClassName = ejbRemoteClassName;
         this.localHomeClassName = localHomeClassName;
         this.EJB3ModuleName = EJB3ModuleName;
         this.localEJB3ClassName = localEJB3ClassName;
         this.remoteEJB3ClassName = remoteEJB3ClassName;
      }

      @Override
      public T getInstance()
      {
         if (instance == null)
         {
            try
            {
               if (instanceClass == null)
               {
                  instanceClass = (Class<T>) Reflect.getClassFromClassName(instanceClassName, true);
               }
               instance = instanceClass.newInstance();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
         return instance;
      }

      @Override
      public String getName()
      {
         return name;
      }

      @Override
      public String getLocalName()
      {
         return localName;
      }

      @Override
      public String getServiceName()
      {
         return serviceName;
      }

      @Override
      public String getSpringBeanName()
      {
         return springBeanName;
      }

      @Override
      public String getJndiPropertyName()
      {
         return jndiPropertyName;
      }

      @Override
      public Class<?> getEJBRemoteClass()
      {
         if (ejbRemoteClass == null)
         {
            ejbRemoteClass = Reflect.getClassFromClassName(ejbRemoteClassName, true);
         }
         return ejbRemoteClass;
      }

      @Override
      public Class<?> getEJBHomeClass()
      {
         if (ejbHomeClass == null)
         {
            ejbHomeClass = Reflect.getClassFromClassName(ejbHomeClassName, true);
         }
         return ejbHomeClass;
      }

      @Override
      public Class<?> getLocalHomeClass()
      {
         if (localHomeClass == null)
         {
            localHomeClass = Reflect.getClassFromClassName(localHomeClassName, true);
         }
         return localHomeClass;
      }

      public String getEJB3ModuleName()
      {
         return EJB3ModuleName;
      }

      @Override
      public String getLocalEJB3ClassName()
      {
         return localEJB3ClassName;
      }

      @Override
      public String getRemoteEJB3ClassName()
      {
         return remoteEJB3ClassName;
      }
   }
}