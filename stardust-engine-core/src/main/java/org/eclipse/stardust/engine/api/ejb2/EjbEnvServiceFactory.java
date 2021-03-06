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
package org.eclipse.stardust.engine.api.ejb2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingService;
import org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingUtils;
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
 * @version $Revision$
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
    * @param service
    *           The interface type defining the service to be retrieved.
    * @return An instance of the requested service, either freshly created or retrieved
    *         from the service factorie's pool.
    */
   protected <T extends Service> T getNewServiceInstance(Class<T> type)
   {
      try
      {
         IServiceProvider<T> provider = ServiceProviderFactory.findServiceProvider(type);

         Context context = new InitialContext();
         Context javacomp = (Context) context.lookup("java:comp/env");
         if (javacomp == null)
         {
            throw new InternalException("java:comp/env context is null.");
         }

         Pair homePair;
         try
         {
            homePair = getLocalHome(javacomp, provider);
            trace.info("Using local interface for service " + provider.getName());
         }
         catch (NameNotFoundException x)
         {
            homePair = getRemoteHome(javacomp, provider);
            trace.info("Using remote interface for service " + provider.getName());
         }

         Class homeClass = (Class) homePair.getFirst();
         Object home = homePair.getSecond();

         Method creationMethod = homeClass.getMethod("create", new Class[]{});
         Object inner = creationMethod.invoke(home, new Object[]{});
         LogUtils.traceObject(inner, false);

         TunneledContext tunneledContext = null;
         if (!SecurityProperties.isPrincipalBasedLogin())
         {
            if (inner instanceof TunnelingService)
            {
               try
               {
                  tunneledContext = TunnelingUtils.performTunnelingLogin(
                        (TunnelingService) inner, userName, password, getProperties());
               }
               catch (Exception e)
               {
                  throw ClientInvocationHandler.unwrapException(e);
               }
            }
            else
            {
               Method loginMethod = inner.getClass().getMethod("login",
                     new Class[] {String.class, String.class, Map.class});
               try
               {
                  loginMethod.invoke(inner, new Object[] {userName, password, getProperties()});
               }
               catch (InvocationTargetException e)
               {
                  Throwable t = e.getTargetException();
                  if (t instanceof WorkflowException
                        && t.getCause() instanceof PublicException)
                  {
                     throw (PublicException) t.getCause();
                  }
                  throw e;
               }
            }
         }

         return (T) Proxy.newProxyInstance(type.getClassLoader(),
               new Class[] {type, ManagedService.class},
               new ClientInvocationHandler(inner, tunneledContext));
      }
      catch (Exception e)
      {
         LogUtils.traceException(e, true);
      }

      // (fh) this line is never reached, but won't compile without it.
      return null;
   }

   private Pair getRemoteHome(Context javacomp, IServiceProvider<?> provider) throws NamingException
   {
      Object rawHome = javacomp.lookup("ejb/" + provider.getName());
      LogUtils.traceObject(rawHome, false);

      Class homeClass = provider.getEJBHomeClass();
      Object home;
      try
      {
         home = PortableRemoteObject.narrow(rawHome, homeClass);
      }
      catch (ClassCastException cce)
      {
         // try to fall back to tunneling mode
         Pair tunnelingHome = TunnelingUtils.castToTunnelingRemoteServiceHome(rawHome,
               homeClass);

         if (null != tunnelingHome)
         {
            homeClass = (Class) tunnelingHome.getFirst();
            home = tunnelingHome.getSecond();
         }
         else
         {
            // tunneling could not be enabled, reverting to previous error
            throw cce;
         }
      }
      LogUtils.traceObject(home, false);

      return new Pair(homeClass, home);
   }

   private Pair getLocalHome(Context javacomp, IServiceProvider<?> provider) throws NamingException
   {
      Object rawHome = javacomp.lookup("ejb/" + provider.getLocalName());
      LogUtils.traceObject(rawHome, false);
      Class homeClass = provider.getLocalHomeClass();
      if (!homeClass.isInstance(rawHome))
      {
         Pair tunnelingHome = TunnelingUtils.castToTunnelingLocalServiceHome(rawHome, homeClass);
         if (null != tunnelingHome)
         {
            return tunnelingHome;
         }
      }

      return new Pair(homeClass, rawHome);
   }

   /**
    * Retrieves <code>username</code> and <code>password</code> credentials, if available,
    * for later use.
    *
    * @param credentials The credentials available in the current configuration.
    */
   public void setCredentials(Map credentials)
   {
      this.userName = (String) credentials.get("user");
      this.password = (String) credentials.get("password");
   }
}
