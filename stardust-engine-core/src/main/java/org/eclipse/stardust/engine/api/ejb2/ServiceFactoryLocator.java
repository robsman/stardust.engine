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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBContext;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.CredentialProvider;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.api.runtime.PropertyAwareCredentialProvider;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.extensions.ejb.utils.J2EEUtils;


// @todo (france, ub): review
/**
 * Provides an abstraction to retrieve {@link ServiceFactory} instances in an EJB
 * container environment.
 *
 * @author ubirkemeyer
 * @author rsauer
 * @version $Revision$
 */
public class ServiceFactoryLocator
{
   /**
    * Retrieves a {@link ServiceFactory} using the credentials included in the given
    * EJB context.
    *
    * @param context The EJB context object providing credentials.
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(EJBContext context) throws PublicException
   {
      return get(context, Collections.EMPTY_MAP);
   }

   /**
    * Retrieves a {@link ServiceFactory} using the credentials included in the given
    * EJB context.
    *
    * @param context The EJB context object providing credentials.
    * @param properties Additional properties.
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(EJBContext context, Map properties)
         throws PublicException
   {
      Map credentials = new HashMap();
      String principal = ((null == context) || (null == context.getCallerPrincipal()))
            ? null
            : J2EEUtils.getPrincipalName(context.getCallerPrincipal());
      credentials.put("user", principal);

      return __get__(credentials, properties);
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
   public static ServiceFactory get(Map credentials, Map properties) throws PublicException,
         LoginFailedException
   {
      try
      {
         CredentialProvider credentialProvider = CredentialProvider.instance();

         Map result;
         if (credentialProvider instanceof PropertyAwareCredentialProvider)
         {
            result = ((PropertyAwareCredentialProvider) credentialProvider).getCredentials(
                  credentials, properties);
         }
         else
         {
            result = credentialProvider.getCredentials(credentials);
         }

         return __get__(result, properties);
      }
      catch (InternalException e)
      {
			throw new PublicException(
					BpmRuntimeError.EJB_INVALID_SERVICE_FACTORY_CONFIGURATION
							.raise(e.getMessage()));
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
   public static ServiceFactory get(String user, String password) throws PublicException
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
         throws PublicException
   {
      Map credentials = new HashMap();
      credentials.put(SecurityProperties.CRED_USER, user);
      credentials.put(SecurityProperties.CRED_PASSWORD, password);

      return __get__(credentials, properties);
   }

   public static ServiceFactory get() throws PublicException
   {
      return __get__(Collections.EMPTY_MAP, Collections.EMPTY_MAP);
   }

   private static ServiceFactory __get__(Map credentials, Map properties)
         throws PublicException
   {
      try
      {
         ServiceFactory result = (ServiceFactory) Reflect.createInstance(Parameters
               .instance().getString(EngineProperties.CLIENT_SERVICE_FACTORY,
                     PredefinedConstants.POJO_SERVICEFACTORY_CLASS));
         result.setCredentials(credentials);

         Map mergedProps = new HashMap(properties);
         LoginUtils.mergeCredentialProperties(mergedProps, credentials);
         LoginUtils.mergeDefaultCredentials(mergedProps);

         result.setProperties(mergedProps);

         return result;
      }
      catch (InternalException e)
      {
			throw new PublicException(
					BpmRuntimeError.EJB_INVALID_SERVICE_FACTORY_CONFIGURATION
							.raise(e.getMessage()));
      }
   }

   private ServiceFactoryLocator()
   {
      // utility class
   }
}
