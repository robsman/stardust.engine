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
package org.eclipse.stardust.engine.api.ejb3;

import java.lang.reflect.Proxy;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.core.runtime.beans.AbstractSessionAwareServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.runtime.beans.ServiceProviderFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;
import org.eclipse.stardust.engine.core.spi.runtime.IServiceProvider;

/**
 * Retrieves IPP service homes from the EJB environment's JNDI context. Supports both
 * login- and principal-based authentication scenarios.
 *
 * @author rsauer
 * @version $Revision: 56255 $
 *
 * @see org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator
 */
public class EjbEnvServiceFactory extends AbstractSessionAwareServiceFactory
{
   private static final Logger trace = LogManager.getLogger(EjbEnvServiceFactory.class);

   private String userName;
   private String password;

   public <T extends Service> T getService(Class<T> type) throws ServiceNotAvailableException,
         LoginFailedException
   {
      InvokerPrincipal current = InvokerPrincipalUtils.removeCurrent();
      try
      {
         return super.getService(type);
      }
      finally
      {
         InvokerPrincipalUtils.setCurrent(current);
      }
   }

   /**
    * Retrieves an IPP session bean home from the EJB environment. If no implicit caller
    * principal propagation is to be used, an explicit login will be performed on any
    * freshly created session bean instance.
    * First it tries to lookup local home object from "ejb/Local<ServiceName>".
    * If this fails it falls back to lookup remote home object from "ejb/<ServiceName>".
    *
    * @param type
    *           The interface type defining the service to be retrieved.
    * @return An instance of the requested service, either freshly created or retrieved
    *         from the service factorie's pool.
    */
   @SuppressWarnings("unchecked")
   protected <T extends Service> T getNewServiceInstance(Class<T> type)
   {
      try
      {
         IServiceProvider<T> provider = ServiceProviderFactory.findServiceProvider(type);

         Context context = new InitialContext();
         Object service;
         try
         {
            service = getLocalService(context, provider);
            trace.info("Using local interface for service " + provider.getName());
         }
         catch (NameNotFoundException x)
         {
            service = getRemoteService(context, provider);
            trace.info("Using remote interface for service " + provider.getName());
         }

         TunneledContext tunneledContext = null;
         if (!SecurityProperties.isPrincipalBasedLogin())
         {
            try
            {
               tunneledContext = TunnelingUtils.performTunnelingLogin(
                     (ManagedService) service, userName, password, getProperties());
            }
            catch (Exception e)
            {
               throw ClientInvocationHandler.unwrapException(e);
            }
         }

         return (T) Proxy.newProxyInstance(type.getClassLoader(),
               new Class[]{type, ManagedService.class},
               new ClientInvocationHandler(service, tunneledContext));
      }
      catch (Exception e)
      {
         LogUtils.traceException(e, true);
      }

      // (fh) this line is never reached, but won't compile without it.
      return null;
   }

   private <T extends Service> Object getRemoteService(Context context, IServiceProvider<T> provider)
         throws NamingException
   {
      String remoteEJB3ClassName = provider.getRemoteEJB3ClassName();
      String remoteJndiName = "java:app/" + provider.getEJB3ModuleName() + "/" + provider.getName() + "Impl!" + remoteEJB3ClassName;

      Object rawObject = context.lookup(remoteJndiName);
      LogUtils.traceObject(rawObject, false);

      Class<?> remoteClass = Reflect.getClassFromClassName(remoteEJB3ClassName);
      try
      {
         Object service = PortableRemoteObject.narrow(rawObject, remoteClass);
         LogUtils.traceObject(service, false);
         return service;
      }
      catch (ClassCastException cce)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Failed resolving remote service with class " + remoteClass, cce);
         }
      }
      return rawObject;
   }

   private <T extends Service> Object getLocalService(Context context, IServiceProvider<T> provider) throws NamingException
   {
      String localEJB3ClassName = provider.getLocalEJB3ClassName();
      String localJndiName = "java:app/" + provider.getEJB3ModuleName() + "/" + provider.getName() + "Impl!" + localEJB3ClassName;

      Object rawObject = context.lookup(localJndiName);
      LogUtils.traceObject(rawObject, false);

      Class<?> localClass = Reflect.getClassFromClassName(localEJB3ClassName);
      if (localClass.isInstance(rawObject))
      {
         return rawObject;
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Failed resolving local service with class " + localClass);
         }
      }
      return rawObject;
   }

   /**
    * Retrieves <code>username</code> and <code>password</code> credentials, if available,
    * for later use.
    *
    * @param credentials The credentials available in the current configuration.
    */
   public void setCredentials(@SuppressWarnings("rawtypes") Map credentials)
   {
      this.userName = (String) credentials.get("user");
      this.password = (String) credentials.get("password");
   }
}
