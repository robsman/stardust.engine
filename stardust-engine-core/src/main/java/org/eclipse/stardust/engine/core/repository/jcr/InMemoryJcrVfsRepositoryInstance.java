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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.vfs.impl.utils.RepositoryHelper;

public class InMemoryJcrVfsRepositoryInstance extends JcrVfsRepositoryInstance
{

   private static final long serialVersionUID = -2431625190427787667L;

   private static Logger trace = LogManager.getLogger(InMemoryJcrVfsRepositoryInstance.class);

   private String repositoryConfigLocation;

   public InMemoryJcrVfsRepositoryInstance(IRepositoryConfiguration configuration,
         String partitionId)
   {
      super(configuration, partitionId);
   }

   @Override
   protected void initRepositoryInfo(IRepositoryConfiguration configuration)
   {
      this.repositoryConfigLocation = (String) configuration.getAttributes().get(
            JcrVfsRepositoryConfiguration.REPOSITORY_CONFIG_LOCATION);

      Repository repository = initEmbeddedJNDIRepo();

      this.repository = repository;

      this.repositoryInfo = new JcrVfsRepositoryInstanceInfo(repositoryId, repository,
            configuration);
   }

   private Repository initEmbeddedJNDIRepo()
   {
      Repository repository;
      try
      {
         repository = connect(repositoryId, repositoryConfigLocation);
      }
      catch (IOException e)
      {
         throw new PublicException(
               BpmRuntimeError.DMS_IN_MEMORY_REPOSITORY_FAILED.raise(), e);
      }
      catch (URISyntaxException e)
      {
         throw new PublicException(
               BpmRuntimeError.DMS_IN_MEMORY_REPOSITORY_FAILED.raise(), e);
      }

      return repository;
   }

   private Repository connect(String repositoryId, String repositoryConfigLocation)
         throws IOException, URISyntaxException
   {
      jndiName = "jndi-in-mem-jcr-" + repositoryId;
      Repository repository;
      InitialContext context = InMemoryInitialContextHolder.getContext();
      try
      {
         repository = (Repository) context.lookup(jndiName);
      }
      catch (NamingException e1)
      {
         repository = null;
      }

      if (repository == null)
      {
         URL resource = Thread.currentThread()
               .getContextClassLoader()
               .getResource(repositoryConfigLocation);
         File configFile = null;
         if (resource != null)
         {
            try
            {
               configFile = new File(resource.toURI());
            }
            catch (Exception e)
            {
               configFile = null;
            }
         }
         if (configFile == null)
         {
            configFile = new File(new URI(repositoryConfigLocation));
         }

         String actualWorkspace = getTmpFolder().getCanonicalPath();

         try
         {
            String configFilePath = configFile.getCanonicalPath();
            trace.info("Bootstraping Embedded Repository at jndiName: " + jndiName
                  + " configFile: " + configFilePath);
            RepositoryHelper.registerRepository(context, jndiName, configFilePath,
                  actualWorkspace, true);

            repository = (Repository) context.lookup(jndiName);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
         catch (RepositoryException e)
         {
            throw new RuntimeException(e);
         }
         trace.info("Connected to Embedded Repository at jndiName: " + jndiName);
      }

      return repository;
   }

   public void shutdown()
   {
      try
      {
         RepositoryHelper.unregisterRepository(InMemoryInitialContextHolder.getContext(),
               jndiName);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static File getTmpFolder() throws IOException
   {
      final File tmpFile = File.createTempFile("jcr-vfs-test-", null);
      tmpFile.delete();
      tmpFile.mkdir();
      tmpFile.deleteOnExit();
      return tmpFile;
   }

}
