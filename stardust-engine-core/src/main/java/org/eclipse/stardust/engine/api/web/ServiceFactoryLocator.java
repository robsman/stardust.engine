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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;


/**
 * Provides an abstraction to retrieve {@link ServiceFactory} instances in a Web container
 * environment.
 *
 * @author ubirkemeyer
 * @author rsauer
 * @version $Revision$
 */
public class ServiceFactoryLocator
{
   /**
    * Retrieves a {@link ServiceFactory} using the credentials included in the given
    * request.
    *
    * @param request The request object providing credentials.
    *
    * @return A readily usable service factory.
    */
   public static ServiceFactory get(HttpServletRequest request) throws PublicException
   {
      return get(request, Collections.EMPTY_MAP);
   }

   public static ServiceFactory get(HttpServletRequest request, Map properties)
         throws PublicException
   {
      Map credentials = new HashMap();
      credentials.put("request", request);

      return __get__(credentials, properties);
   }

   public static ServiceFactory get(String user, String password)
   {
      return get(user, password, Collections.EMPTY_MAP);
   }

   public static ServiceFactory get(String user, String password, Map properties)
   {
      Map credentials = new HashMap();
      credentials.put("user", user);
      credentials.put("password", password);

      return __get__(credentials, properties);
   }

   private static ServiceFactory __get__(Map credentials, Map properties)
         throws PublicException
   {
      try
      {
         ServiceFactory result = (ServiceFactory) Reflect.createInstance(Parameters
               .instance().getString(WebProperties.WEB_SERVICE_FACTORY,
                     PredefinedConstants.PLAIN_WEB_SERVICEFACTORY_CLASS),
               ServiceFactoryLocator.class.getClassLoader());
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
               BpmRuntimeError.EJB_INVALID_SERVICE_FACTORY_CONFIGURATION.raise(e
                     .getMessage()));
      }
   }

   private ServiceFactoryLocator()
   {
      // utility class
   }
}
