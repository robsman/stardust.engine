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
import java.util.Map;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class POJOServiceFactory extends AbstractSessionAwareServiceFactory
{
   private String password;
   private String username;

   protected Service getNewServiceInstance(Class service)
   {
      String serviceName = service.getName();
      int dot = serviceName.lastIndexOf(".");
      String packageName = serviceName.substring(0, dot).replace(".api.", ".core.");
      String className = serviceName.substring(dot + 1);

      Object inner = Reflect.createInstance(packageName + ".beans." + className + "Impl");

      InvocationManager invocationHandler = new POJOInvocationManager(inner, serviceName);

      Service result = (Service) Proxy.newProxyInstance(
            service.getClassLoader(), new Class[] {service, ManagedService.class}, invocationHandler);

      ((ManagedService)result).login(username, password, getProperties());

      return result;
   }

   public void setCredentials(Map credentials)
   {
      username = (String) credentials.get(SecurityProperties.CRED_USER);
      password = (String) credentials.get(SecurityProperties.CRED_PASSWORD);
   }
}
