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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.core.runtime.beans.AbstractSessionAwareServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.runtime.beans.ServiceProviderFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.runtime.IServiceProvider;

/**
 * @author ubirkemeyer
 * @version $Revision: 52592 $
 */
public class RemoteServiceFactory extends AbstractSessionAwareServiceFactory
{
   private Map<?, ?> credentials = new HashMap<Object, Object>();

   private TunnelingAwareSecureSessionFactory secureSessionFactory;

   public RemoteServiceFactory()
   {
      this.secureSessionFactory = (TunnelingAwareSecureSessionFactory) Reflect.createInstance(
            Parameters.instance().getString(SecurityProperties.SECURE_SESSION_FACTORY,
                  "org.eclipse.stardust.engine.api.ejb3.InternallyAuthentifiedSecureSessionFactory"));
   }

   @SuppressWarnings("unchecked")
   protected <T extends Service> T getNewServiceInstance(Class<T> service)
   {
      IServiceProvider<T> provider = ServiceProviderFactory.findServiceProvider(service);

      String remoteEJB3ClassName = provider.getRemoteEJB3ClassName();
      Class<?> remote = Reflect.getClassFromClassName(remoteEJB3ClassName);

      TunnelingAwareSecureSessionFactory.SecureSession session = secureSessionFactory.getSecureSession(
               Parameters.instance().getString(provider.getJndiPropertyName(),
                     "ejb:carnot/" + provider.getEJB3ModuleName() + "/" + provider.getName() + "Impl!" + remoteEJB3ClassName),
               remote, new Class[] {}, new Object[] {}, credentials, getProperties());

      return (T) Proxy.newProxyInstance(service.getClassLoader(),
            new Class[] {service, ManagedService.class},
            new ClientInvocationHandler(session.endpoint, session.tunneledContext));
   }

   public void setCredentials(@SuppressWarnings("rawtypes") Map credentials)
   {
      this.credentials = credentials;
   }
}
