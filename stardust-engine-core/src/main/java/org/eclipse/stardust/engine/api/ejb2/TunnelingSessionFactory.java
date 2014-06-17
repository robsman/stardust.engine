/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
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
import java.rmi.RemoteException;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.utils.ejb.EJBUtils;
import org.eclipse.stardust.engine.api.ejb2.SecureSessionFactory;
import org.eclipse.stardust.engine.api.ejb2.TunnelingAwareSecureSessionFactory;
import org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingService;
import org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext;

/**
 * @author Florin.Herinean
 */
public class TunnelingSessionFactory
      implements SecureSessionFactory, TunnelingAwareSecureSessionFactory
{

   public Object get(String jndiName, @SuppressWarnings("rawtypes") Class homeClass,
         @SuppressWarnings("rawtypes") Class remoteClass,
         @SuppressWarnings("rawtypes") Class[] creationArgTypes,
         final Object[] creationArgs, @SuppressWarnings("rawtypes") Map credentials,
         @SuppressWarnings("rawtypes") Map properties)
   {
      SecureSession result = getSecureSession(jndiName, homeClass, remoteClass,
            creationArgTypes, creationArgs, credentials, properties);

      if (null != result.tunneledContext)
      {
         // no way to propagate tunneled context via old contract
         throw new ServiceNotAvailableException(
               "Service is only available for tunneling invocations, but service factory does not seem to be aware.");
      }

      return result.endpoint;
   }

   public SecureSession getSecureSession(String jndiName,
         @SuppressWarnings("rawtypes") Class homeClass,
         @SuppressWarnings("rawtypes") Class remoteClass,
         @SuppressWarnings("rawtypes") Class[] creationArgTypes, Object[] creationArgs,
         @SuppressWarnings("rawtypes") Map credentials,
         @SuppressWarnings("rawtypes") Map properties)
         throws ServiceNotAvailableException
   {
      Object endpoint = null;

      TunneledContext tunneledContext = null;

      // first obtain the service.
      try
      {
         Context context = EJBUtils.getInitialContext(false, false);
         Object rawHome = context.lookup(jndiName);
         LogUtils.traceObject(rawHome, false);

         @SuppressWarnings("unchecked")
         Pair<Class< ? >, Object> tunnelingHome = TunnelingUtils
               .castToTunnelingRemoteServiceHome(rawHome, homeClass);
         if (tunnelingHome != null)
         {
            homeClass = tunnelingHome.getFirst();
            Object home = tunnelingHome.getSecond();
            LogUtils.traceObject(homeClass, false);
            @SuppressWarnings("unchecked")
            Method creationMethod = homeClass.getMethod("create", creationArgTypes);
            endpoint = creationMethod.invoke(home, creationArgs);
            LogUtils.traceObject(endpoint, false);
         }
         else
         {
            // tunneling could not be enabled
            throw new ClassCastException("Could not cast " + rawHome.getClass() + " to "
                  + homeClass);
         }
      }
      catch (InvocationTargetException e)
      {
         ApplicationException ex = getRootException(e.getTargetException());
         throw new ServiceNotAvailableException(ex.getMessage(), ex);
      }
      catch (NamingException e)
      {
         throw new ServiceNotAvailableException(e.getMessage(), e);
      }
      catch (Exception e)
      {
         throw new InternalException("Failed to create session bean.", e);
      }

      // now attempt to login
      try
      {
         if (endpoint instanceof TunnelingService)
         {
            tunneledContext = TunnelingUtils.performTunnelingLogin(
                  (TunnelingService) endpoint,
                  (String) credentials.get(SecurityProperties.CRED_USER),
                  (String) credentials.get(SecurityProperties.CRED_PASSWORD), properties);
         }
         else
         {
            Method loginMethod = endpoint.getClass().getMethod("login",
                  new Class[] {String.class, String.class, Map.class});
            loginMethod.invoke(
                  endpoint,
                  new Object[] {
                        credentials.get(SecurityProperties.CRED_USER),
                        credentials.get(SecurityProperties.CRED_PASSWORD), properties});
         }
      }
      catch (Throwable e)
      {
         throw getRootException(e);
      }

      return new SecureSession(endpoint, tunneledContext);
   }

   private ApplicationException getRootException(Throwable source)
   {
      if (source instanceof InvocationTargetException)
      {
         source = ((InvocationTargetException) source).getTargetException();
      }
      while (source instanceof RemoteException)
      {
         if (((RemoteException) source).detail == null)
         {
            return new InternalException(source);
         }
         source = ((RemoteException) source).detail;
      }
      if (source instanceof WorkflowException)
      {
         source = ((WorkflowException) source).getCause();
         if (source instanceof PublicException)
         {
            throw (PublicException) source;
         }
      }
      if (source instanceof ApplicationException)
      {
         return (ApplicationException) source;
      }
      throw new InternalException(source.getMessage(), source);
   }
}