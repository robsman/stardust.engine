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

import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.utils.ejb.EJBUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext;

/**
 * @author ubirkemeyer
 * @version $Revision: 60059 $
 */
public class InternallyAuthentifiedSecureSessionFactory implements
      SecureSessionFactory, TunnelingAwareSecureSessionFactory
{
   public Object get(String jndiName, Class<?> remoteClass,
         Class<?>[] creationArgTypes, Object[] creationArgs,
         Map<?, ?> credentials, Map<?, ?> properties)
   {
      // invoke new contract, to determine if deployment uses tunneling or not
      SecureSession result = getSecureSession(jndiName, remoteClass, creationArgTypes,
            creationArgs, credentials, properties);
      if (result.tunneledContext != null) {
         // no way to propagate tunneled context via old contract
         throw new ServiceNotAvailableException(
               "Service is only available for tunneling invocations, but service factory does not seem to be aware.");
      }

      return result.endpoint;
   }

   public SecureSession getSecureSession(String jndiName, Class<?> remoteClass,
         Class<?>[] creationArgTypes, Object[] creationArgs,
         Map<?, ?> credentials, Map<?, ?> properties)
         throws ServiceNotAvailableException
   {
      Object service = null;

      // first obtain the service.
      try
      {
         Context context = EJBUtils.getInitialContext(false, false);

         Object rawObject = context.lookup(jndiName);
         LogUtils.traceObject(rawObject, false);

         service = PortableRemoteObject.narrow(rawObject, remoteClass);
         LogUtils.traceObject(service, false);
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
      try {

         TunneledContext tunneledContext = TunnelingUtils.performTunnelingLogin(
               (ManagedService) service, (String) credentials.get(SecurityProperties.CRED_USER),
               (String) credentials.get(SecurityProperties.CRED_PASSWORD), properties);
         return new SecureSession(service, tunneledContext);
      }
      catch (Exception e)
      {
         throw ClientInvocationHandler.unwrapException(e);
      }
   }
}
