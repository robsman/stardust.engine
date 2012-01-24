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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.Subject;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.utils.ejb.EJBUtils;
import org.eclipse.stardust.engine.api.runtime.DefaultJAASCredentialProvider;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DefaultJAASSecureSessionFactory implements SecureSessionFactory
{
   public Object get(String jndiName, Class homeClass, Class remoteClass, Class[] creationArgTypes,
         final Object[] creationArgs, Map credentials, Map properties)
   {
      // TODO (kafka): use properties
      try
      {
         final Subject subject = (Subject) credentials.get(DefaultJAASCredentialProvider.SUBJECT);

         Context context = EJBUtils.getInitialContext(false, false);

         Object rawHome = context.lookup(jndiName);
         final Object home = PortableRemoteObject.narrow(rawHome, homeClass);
         final Method creationMethod = homeClass.getMethod("create", creationArgTypes);

         boolean isImplicitlyAuthenticated = Parameters.instance().getBoolean(
               SecurityProperties.AUTHENTICATION_IMPLICIT_CLIENT_IDENTITY_PROPERTY, false);

         if (isImplicitlyAuthenticated)
         {
            // client invokation stack magically set caller principal

            return creationMethod.invoke(home, creationArgs);
         }
         else
         {
            // explicitly set JAAS caller principal

            final Object bean = Subject.doAs(subject, new PrivilegedAction()
            {
               public Object run()
               {
                  try
                  {
                     return creationMethod.invoke(home, creationArgs);
                  }
                  catch (InvocationTargetException e)
                  {
                     throw new InternalException("Couldn't lookup session bean.", e
                           .getTargetException());
                  }
                  catch (Exception e)
                  {
                     throw new InternalException("Couldn't lookup session bean.", e);
                  }
               }

            });

            InvocationHandler handler = new InvocationHandler()
            {
               public Object invoke(Object proxy, final Method method,
                     final Object[] args) throws Throwable
               {
                  try
                  {
                     return Subject.doAs(subject, new PrivilegedExceptionAction()
                     {
                        public Object run() throws Exception
                        {
                           try
                           {
                              return method.invoke(bean, args);
                           }
                           catch (InvocationTargetException e)
                           {
                              Throwable target = e.getTargetException();
                              if (target instanceof Exception)
                              {
                                 throw (Exception) target;
                              }
                              else
                              {
                                 throw new InternalException(e);
                              }
                           }
                           catch (IllegalAccessException e)
                           {
                              throw new InternalException(e);
                           }
                        }
                     });
                  }
                  catch (PrivilegedActionException e)
                  {
                     throw e.getException();
                  }
               }
            };

            return Proxy.newProxyInstance(getClass().getClassLoader(),
                  new Class[]{remoteClass}, handler);
         }
      }
      catch (InvocationTargetException e)
      {
         if (e.getTargetException() instanceof RemoteException)
         {
            throw new ServiceNotAvailableException(e.getTargetException().getMessage(),
                  ((RemoteException)e.getTargetException()).detail);
         }
         throw new InternalException(e.getTargetException());
      }
      catch (NamingException e)
      {
         throw new ServiceNotAvailableException(e.getMessage(), e);
      }
      catch (Exception e)
      {
         throw new InternalException("Failed to create session bean.", e);
      }
   }
}
