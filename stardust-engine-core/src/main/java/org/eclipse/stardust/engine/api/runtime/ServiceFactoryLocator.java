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
package org.eclipse.stardust.engine.api.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.EmbeddedServiceFactory;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * Provides an abstraction to retrieve {@link ServiceFactory} instances in plain Java
 * or client container environments.
 *
 * @author ubirkemeyer
 * @author rsauer
 * @version $Revision$
 */
public class ServiceFactoryLocator
{
   /**
    * Retrieves a service factory using separately provided credentials.
    *
    * @param loginType
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(int loginType) throws PublicException,
         LoginFailedException
   {
      return get(loginType, Collections.EMPTY_MAP);
   }

   /**
    * Retrieves a service factory using separately provided credentials.
    *
    * @param loginType
    * @param properties Additional properties.
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(int loginType, Map properties)
         throws PublicException, LoginFailedException
   {
      try
      {
         if (CredentialProvider.CURRENT_TX == loginType)
         {
            return EmbeddedServiceFactory.CURRENT_TX();
         }
         else
         {
            Map credentials = retrieveCredentials(loginType, properties);

            return __get__(credentials, properties);
         }
      }
      catch (InternalException e)
      {
         throw new PublicException(
               BpmRuntimeError.EJB_INVALID_SERVICE_FACTORY_CONFIGURATION.raise(e
                     .getMessage()));
      }
   }

   /**
    * Retrieves a service factory using explicitly provided credentials.
    * <p />
    * Credential usage is depending on the configured credential provider.
    *
    * @param credentials The credentials to be used.
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(Map credentials) throws PublicException,
         LoginFailedException
   {
      return get(credentials, Collections.EMPTY_MAP);
   }

   /**
    * Retrieves a service factory using explicitly provided credentials.
    * <p />
    * Credential usage is depending on the configured credential provider.
    *
    * @param credentials The credentials to be used.
    * @param properties Additional properties.
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(Map credentials, Map properties)
         throws PublicException, LoginFailedException
   {
      try
      {
         Map result = retrieveCredentials(credentials, properties);

         return __get__(result, properties);
      }
      catch (InternalException e)
      {
         throw new PublicException(
               BpmRuntimeError.EJB_INVALID_SERVICE_FACTORY_CONFIGURATION.raise(e
                     .getMessage()));
      }
   }

   /**
    * Retrieves a service factory using the given name/password pair as credentials.
    * <p />
    * Credential usage is depending on the configured credential provider.
    *
    * @param user The user name to be used.
    * @param password The password to be used.
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(String user, String password) throws PublicException,
         LoginFailedException
   {
      return get(user, password, Collections.EMPTY_MAP);
   }

   /**
    * Retrieves a service factory using the given name/password pair as credentials.
    * <p />
    * Credential usage is depending on the configured credential provider.
    *
    * @param user The user name to be used.
    * @param password The password to be used.
    * @param properties Additional properties.
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(String user, String password, Map properties)
         throws PublicException, LoginFailedException
   {
      try
      {
         Map credentials = new HashMap();
         credentials.put("user", user);
         credentials.put("password", password);

         return __get__(retrieveCredentials(credentials, properties), properties);
      }
      catch (InternalException e)
      {
         throw new PublicException(
               BpmRuntimeError.EJB_INVALID_SERVICE_FACTORY_CONFIGURATION.raise(e
                     .getMessage()));
      }
   }

   /**
    * Indicates whether the currently configured CARNOT security configuration allows
    * CARNOT services with different principals contrary to a single JVM wide principal.
    *
    * @return true if different principals are allowed.
    */
   public static boolean hasMultipleIdentities() throws PublicException
   {
      return CredentialProvider.instance().hasMultipleIdenties();
   }

   private static ServiceFactory __get__(Map credentials, Map properties)
         throws PublicException
   {
      try
      {
         ServiceFactory sf = (ServiceFactory) Reflect.createInstance(Parameters
               .instance().getString(EngineProperties.CLIENT_SERVICE_FACTORY,
                     PredefinedConstants.POJO_SERVICEFACTORY_CLASS));
         sf.setCredentials(credentials);

         Map mergedProps = new HashMap(properties);
         LoginUtils.mergeCredentialProperties(mergedProps, credentials);
         LoginUtils.mergeDefaultCredentials(mergedProps);

         sf.setProperties(mergedProps);

         return sf;
      }
      catch (InternalException e)
      {
         throw new PublicException(
               BpmRuntimeError.EJB_INVALID_SERVICE_FACTORY_CONFIGURATION.raise(e
                     .getMessage()));
      }
   }

   private static Map retrieveCredentials(int loginType, Map properties)
         throws LoginFailedException
   {
      CredentialProvider provider = CredentialProvider.instance();

      Map result;
      if (provider instanceof PropertyAwareCredentialProvider)
      {
         result = ((PropertyAwareCredentialProvider) provider).getCredentials(loginType,
               properties);
      }
      else
      {
         result = provider.getCredentials(loginType);
      }

      return result;
   }

   private static Map retrieveCredentials(Map credentials, Map properties)
         throws LoginFailedException
   {
      CredentialProvider provider = CredentialProvider.instance();

      Map result;
      if (provider instanceof PropertyAwareCredentialProvider)
      {
         result = ((PropertyAwareCredentialProvider) provider).getCredentials(credentials,
               properties);
      }
      else
      {
         result = provider.getCredentials(credentials);
      }

      return result;
   }

   private ServiceFactoryLocator()
   {
      // utility class
   }
}
