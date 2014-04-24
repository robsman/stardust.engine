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
package org.eclipse.stardust.engine.core.repository.jcr;

import javax.jcr.Repository;
import javax.naming.NamingException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstance;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.UserContext;
import org.eclipse.stardust.engine.core.spi.jca.IJcaResourceProvider;
import org.eclipse.stardust.vfs.impl.jcr.jackrabbit.JackrabbitRepositoryContext;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;
import org.eclipse.stardust.vfs.jcr.spring.JcrSpringSessionFactory;

public class JcrVfsRepositoryInstance implements IRepositoryInstance
{
   protected String repositoryId;

   protected String providerId;

   protected String partitionId;

   protected String jndiName;

   protected JcrVfsRepositoryInstanceInfo repositoryInfo;

   protected Repository repository;

   protected boolean userLevelAuthorization;

   private ISessionFactory externalSessionFactory;

   public JcrVfsRepositoryInstance(IRepositoryConfiguration configuration, String partitionId)
   {
      super();
      this.partitionId = partitionId;
      this.repositoryId = (String) configuration.getAttributes().get(IRepositoryConfiguration.REPOSITORY_ID);
      this.providerId = (String) configuration.getAttributes().get(IRepositoryConfiguration.PROVIDER_ID);
      this.jndiName = (String) configuration.getAttributes().get(JcrVfsRepositoryConfiguration.JNDI_NAME);
      this.userLevelAuthorization = JcrVfsRepositoryConfiguration.getBoolean(configuration, JcrVfsRepositoryConfiguration.USER_LEVEL_AUTHORIZATION, false);

      this.externalSessionFactory = retrieveExternalJcrSessionFactory(jndiName);

      if (configuration.getAttributes().containsKey(
            JcrVfsRepositoryConfiguration.IS_DEFAULT_REPOSITORY))
      {
         // lookup default repository in parameters
         Object contentRepositoryRes = Parameters.instance().get(jndiName);
         if (contentRepositoryRes instanceof Repository)
         {
            this.repository = (Repository) contentRepositoryRes;
         }
      }

      if (jndiName != null && repository == null)
      {
         // lookup in local context
         this.repository = JackrabbitRepositoryContext.getRepository(jndiName);

         if (repository == null)
         {
            this.repository = extractFromSessionFactory();
         }

         if (repository == null)
         {
            // lookup repository via jndi
            try
            {
               this.repository = JackrabbitRepositoryContext.lookup(jndiName);
            }
            catch (NamingException e)
            {
               throw new PublicException(
                     BpmRuntimeError.DMS_REPOSITORY_NOT_FOUND_FOR_JNDI_NAME
                           .raise(jndiName),
                     e);
            }
         }
      }

      initRepositoryInfo(configuration);
   }

   private Repository extractFromSessionFactory()
   {
      Repository repository = null;
      if (this.externalSessionFactory != null && this.externalSessionFactory instanceof JcrSpringSessionFactory)
      {
         JcrSpringSessionFactory factory = (JcrSpringSessionFactory) this.externalSessionFactory;
         repository = factory.getRepository();
      }
      return repository;
   }

   private ISessionFactory retrieveExternalJcrSessionFactory(String name)
   {
      ISessionFactory sessionFactory = null;
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      IJcaResourceProvider jcaResourceProvider = rtEnv.getJcaResourceProvider();
      if (jcaResourceProvider != null)
      {
         Object jcaResource = jcaResourceProvider.resolveJcaResource(name);
         if (jcaResource instanceof ISessionFactory)
         {
            sessionFactory = (ISessionFactory) jcaResource;
         }
      }
      return sessionFactory;
   }

   protected void initRepositoryInfo(IRepositoryConfiguration configuration)
   {
      this.repositoryInfo = new JcrVfsRepositoryInstanceInfo(repositoryId, repository, configuration);
   }

   @Override
   public String getRepositoryId()
   {
      return repositoryId;
   }

   @Override
   public String getProviderId()
   {
      return providerId;
   }

   public String getPartitionId()
   {
      return partitionId;
   }

   @Override
   public JcrVfsRepositoryInstanceInfo getRepositoryInstanceInfo()
   {
      return repositoryInfo;
   }

   @Override
   public IRepositoryService getService(UserContext userContext)
   {
      synchronized (repository)
      {
         IRepositoryService service = userContext.getRepositoryService(this);

         if (service == null)
         {
            ISessionFactory sessionFactory = null;
            if (externalSessionFactory != null)
            {
               sessionFactory = externalSessionFactory;
            }
            else
            {
               sessionFactory = new LocalSessionFactory(repository,
                     userLevelAuthorization);
            }
            service = new JcrVfsRepositoryService(sessionFactory, repository);

            userContext.registerRepositoryService(this, service);
         }
         return service;
      }
   }


   @Override
   public void close(IRepositoryService service)
   {
      if (service != null)
      {
        if (service instanceof JcrVfsRepositoryService)
        {
           ISessionFactory sessionFactory = ((JcrVfsRepositoryService) service).getSessionFactory();
           if (sessionFactory instanceof LocalSessionFactory)
           {
              ((LocalSessionFactory) sessionFactory).closeJcrSessions();
           }
        }
      }
   }

}
