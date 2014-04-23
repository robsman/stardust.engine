/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * Representing the context of a user.
 * <p>
 * The {@link IRepositoryService} can be cached in context of the current user when
 * registering it via
 * {@link #registerRepositoryService(IRepositoryInstance, IRepositoryService)}.<br>
 * For retrieval {@link #getRepositoryService(IRepositoryInstance)} has to be used.<br>
 * Within the same service call of the same user this will return the same registered
 * service instance.
 * <p>
 * At the end of a service call of a user the
 * {@link IRepositoryInstance#close(IRepositoryService)} is automatically called for the
 * registered {@link IRepositoryService} to allow cleanup and finalization.
 * <p>
 *
 * Example:
 * <pre>
 * IRepositoryService service = userContext.getRepositoryService(this);
 *
 * if (service == null)
 * {
 *    service = new SomeServiceImpl();
 *    userContext.registerRepositoryService(this, service);
 * }
 *
 * return service;
 * </pre>
 *
 *
 * @author Roland.Stamm
 *
 */
public class UserContext
{

   protected static final UserContext INSTANCE = new UserContext();

   /**
    * Retrieves the currently registered {@link IRepositoryService} for the current user.
    * <br>
    * This should be called to check if a service is already registered.
    *
    * @param instance
    *           The instance to retrieve the current registered service for.
    * @return Returns the service if it is currently registered for the user call. If no
    *         service is currently registered <code>null</code> is returned.
    */
   public IRepositoryService getRepositoryService(IRepositoryInstance instance)
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      IRepositoryService service = rtEnv.getRepositoryService(instance);
      return service;
   }

   /**
    * Allows registering the {@link IRepositoryService} for the
    * {@link IRepositoryInstance} effectively binding the services lifecyle to the users
    * service call.
    * <p>
    * At the end of a service call of a user the
    * {@link IRepositoryInstance#close(IRepositoryService)} is automatically called for
    * the registered {@link IRepositoryService} to allow cleanup and finalization.
    *
    * @param instance
    *           The repository instance to bind the service for.
    * @param service
    *           The to be registered service
    */
   public void registerRepositoryService(IRepositoryInstance instance,
         IRepositoryService service)
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      rtEnv.registerRepositoryService(instance, service);
   }

   /**
    * @return The current user.
    */
   public User getUser()
   {
      IUser user = SecurityProperties.getUser();
      return user == null || PredefinedConstants.SYSTEM.equals(user.getId())
            ? null
            : (User) DetailsFactory.create(user, IUser.class, UserDetails.class);
   }

   protected static UserContext getInstance()
   {
      return INSTANCE;
   }

}
