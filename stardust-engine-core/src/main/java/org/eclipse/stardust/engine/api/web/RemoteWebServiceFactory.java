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
package org.eclipse.stardust.engine.api.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.ejb2.ClientInvocationHandler;
import org.eclipse.stardust.engine.api.ejb2.WorkflowException;
import org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext;
import org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingService;
import org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingUtils;
import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.core.runtime.beans.AbstractSessionAwareServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.extensions.ejb.utils.J2EEUtils;



/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class RemoteWebServiceFactory extends AbstractSessionAwareServiceFactory
{
   private String userName;
   private String password;
   private Principal principal;

   protected Service getNewServiceInstance(Class service)
   {
      Service result = null;
      try
      {
         String serviceName = service.getName();
         int dot = serviceName.lastIndexOf(".");
         String className = serviceName.substring(dot + 1);
         Class homeClass = Reflect.getClassFromClassName("org.eclipse.stardust.engine.api.ejb2.Remote"
               + className + "Home");

         Context context = new InitialContext();
         Context javacomp = (Context) context.lookup("java:comp/env");
         if (javacomp == null)
         {
            throw new InternalException("java:comp/env context is null.");
         }
         Object rawHome = javacomp.lookup("ejb/" + className);
         LogUtils.traceObject(rawHome, false);
         Object home;
         try
         {
            home = PortableRemoteObject.narrow(rawHome, homeClass);
         }
         catch (ClassCastException cce)
         {
            // try switching to tunneling mode
            Pair tunnelingHome = TunnelingUtils.castToTunnelingRemoteServiceHome(rawHome, homeClass);
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
         Method creationMethod = homeClass.getMethod("create", new Class[]{});
         Object inner = creationMethod.invoke(home, new Object[]{});
         LogUtils.traceObject(inner, false);

         TunneledContext tunneledContext = null;
         if (null == principal)
         {
            if (inner instanceof TunnelingService)
            {
               try
               {
                  tunneledContext = TunnelingUtils.performTunnelingLogin(
                        (TunnelingService) inner, userName, password, getProperties());
               }
               catch (WorkflowException wfe)
               {
                  if (wfe.getCause() instanceof PublicException)
                  {
                     throw (PublicException) wfe.getCause();
                  }
                  throw wfe;
               }
            }
            else
            {
               Method loginMethod = inner.getClass().getMethod("login",
                  new Class[]{String.class, String.class, Map.class});
               try
               {
                  loginMethod.invoke(inner, new Object[]{userName, password, getProperties()});
               }
               catch (InvocationTargetException e)
               {
                  Throwable t = e.getTargetException();
                  if(t instanceof WorkflowException &&
                        t.getCause() instanceof PublicException)
                  {
                     throw (PublicException)t.getCause();
                  }
                  throw e;
               }
            }
         }
         else
         {
            if (inner instanceof TunnelingService)
            {
               String principalName = J2EEUtils.getPrincipalName(principal);
               tunneledContext = new TunneledContext(new InvokerPrincipal(principalName, getProperties()));
            }
         }

         result = (Service) Proxy.newProxyInstance(service.getClassLoader(),
               new Class[]{service, ManagedService.class},
               new ClientInvocationHandler(inner, tunneledContext));
      }
      catch (Exception e)
      {
         LogUtils.traceException(e, true);
      }

      return result;
   }

   public void setCredentials(Map credentials)
   {
      HttpServletRequest request = (HttpServletRequest) credentials.get("request");
      if (request != null)
      {
         if (request.getUserPrincipal() != null)
         {
            principal = request.getUserPrincipal();
         }
         else
         {
            userName = request.getParameter("username");
            password = request.getParameter("password");
         }
      }
      else
      {
         userName = (String) credentials.get("user");
         password = (String) credentials.get("password");
      }
   }
}
