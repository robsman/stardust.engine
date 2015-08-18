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

import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.core.runtime.beans.AbstractSessionAwareServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.runtime.beans.ServiceProviderFactory;
import org.eclipse.stardust.engine.core.spi.runtime.IServiceProvider;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class PlainWebServiceFactory extends AbstractSessionAwareServiceFactory
{
   private Principal principal;
   private String userName;
   private String password;

   protected <T extends Service> T getNewServiceInstance(Class<T> type)
   {
      IServiceProvider<T> provider = ServiceProviderFactory.findServiceProvider(type);
      InvocationManager invocationHandler = new PlainWebInvocationManager(provider.getInstance(),
            provider.getServiceName(), principal);

      T result = (T) Proxy.newProxyInstance(type.getClassLoader(),
            new Class[]{type, ManagedService.class}, invocationHandler);

      if (principal == null)
      {
         ((ManagedService) result).login(userName, password, getProperties());
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
