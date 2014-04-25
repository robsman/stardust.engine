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

import java.util.*;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class DefaultServiceFactory implements ServiceFactory
{
   private static final Logger trace = LogManager.getLogger(DefaultServiceFactory.class);

   private Map properties = Collections.EMPTY_MAP;

   private final ServiceFactoryPool pool;

   protected DefaultServiceFactory()
   {
      this.pool = (ServiceFactoryPool) Reflect.createInstance(Parameters.instance().getString(
            EngineProperties.SERVICEFACTORY_POOL,
            PredefinedConstants.DEFAULT_SERVICEFACTORY_POOL_CLASS));
   }

   public WorkflowService getWorkflowService()
   {
      return (WorkflowService) getService(WorkflowService.class);
   }

   public UserService getUserService()
   {
      return (UserService) getService(UserService.class);
   }

   public AdministrationService getAdministrationService()
   {
      return (AdministrationService) getService(AdministrationService.class);
   }

   public QueryService getQueryService()
   {
      return (QueryService) getService(QueryService.class);
   }

   public DocumentManagementService getDocumentManagementService()
         throws ServiceNotAvailableException, LoginFailedException
   {
      return (DocumentManagementService) getService(DocumentManagementService.class);
   }

   public void release(Service service)
   {
      if (null != service)
      {
         pool.remove(service);
         try
         {
            ((ManagedService) service).remove();
         }
         catch (Exception e)
         {
            trace.warn("Error during service removal. Probably no error:", e);
         }
      }
   }

   public void close()
   {
      for (Iterator i = pool.iterator(); i.hasNext();)
      {
         Service service = (Service) i.next();

         release(service);

         // refreshing with new iterator as release(..) has modified the pool concurrently
         i = pool.iterator();
      }
   }

   protected <T extends Service> T getServiceFromPool(Class<T> serviceType)
   {
      return (T) pool.get(serviceType);
   }

   protected <T extends Service> void putServiceToPool(Class<T> serviceType, T service)
   {
      pool.put(serviceType, service);
   }

   protected void removeServiceFromPool(Service service)
   {
      pool.remove(service);
   }

   /**
    * TODO review contract, {@link ServiceFactoryPool} should be a {@link Map} or
    * {@link Collection}.
    */
   protected Iterator<Service> getServicesFromPool()
   {
      return pool.iterator();
   }

   protected Map getProperties()
   {
      return properties;
   }

   public void setProperties(Map properties)
   {
      this.properties = ((null == properties) || properties.isEmpty())
            ? Collections.EMPTY_MAP
            : new HashMap(properties);
   }

   public String getSessionId()
   {
      return null;
   }
}
