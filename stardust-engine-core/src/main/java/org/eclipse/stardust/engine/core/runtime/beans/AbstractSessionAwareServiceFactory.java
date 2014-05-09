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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.Service;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.api.runtime.UserService;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class AbstractSessionAwareServiceFactory extends DefaultServiceFactory
{
   private static final Logger trace = LogManager.getLogger(AbstractSessionAwareServiceFactory.class);

   private String clientId;

   private String sessionId;

   protected AbstractSessionAwareServiceFactory()
   {
      this("");
   }

   protected AbstractSessionAwareServiceFactory(String clientId)
   {
      this.clientId = clientId;
   }

   protected abstract <T extends Service> T getNewServiceInstance(Class<T> type)
         throws ServiceNotAvailableException, LoginFailedException;

   public <T extends Service> T getService(Class<T> type) throws ServiceNotAvailableException,
         LoginFailedException
   {
      if (StringUtils.isEmpty(sessionId))
      {
         // any service requires a session to be started first on the user service
         this.sessionId = startSession(clientId);
      }

      // try to reuse service according to the rules of the pool in use
      return getOrCreateService(type);
   }

   private <T extends Service> T getOrCreateService(Class<T> type)
   {
      T result = getServiceFromPool(type);
      if (null == result)
      {
         result = getNewServiceInstance(type);
         putServiceToPool(type, result);
      }
      return result;
   }

   public void close()
   {
      // get hold on user service before cleaning pool to be able to close session
      // afterwards
      UserService userService = null;
      try
      {
         if (!StringUtils.isEmpty(sessionId))
         {
            try
            {
               userService = getUserService();
               removeServiceFromPool(userService);
            }
            catch (Exception ex)
            {
               trace.warn("Unable to close the session.", ex);
            }
         }
         else if (getServicesFromPool().hasNext())
         {
            trace.warn("Service factory without an active session must not have service instances.");
         }

         super.close();
      }
      finally
      {
         if (userService != null)
         {
            try
            {
               userService.closeSession(sessionId);
            }
            catch (Exception ex)
            {
               trace.warn("Unable to close the session.", ex);
            }
            this.sessionId = null;
            release(userService);
         }
      }
   }

   protected String startSession(String clientId) throws ServiceNotAvailableException,
         LoginFailedException
   {
      // must not invoke the regular getService(...) method as this will lead to infinite
      // recursion wrt. session start
      UserService userService = (UserService) getOrCreateService(UserService.class);

      // TODO client ID is not yet propagated as activity tracking on the server is not
      // session aware
      return userService.startSession("");
   }

   @Override
   public String getSessionId()
   {
      return sessionId;
   }
}
