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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.EjbDocumentRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.vfs.IDocumentRepositoryService;

public class JcrVfsRepositoryService extends AbstractJcrVfsRepositoryService
{
   private static final long serialVersionUID = -5256677063628195043L;

   protected String repositoryId;
   
   protected String providerId;

   protected String partitionId;

   protected IDocumentRepositoryService vfs;

   protected String jndiName;

   protected JcrVfsRepositoryInstanceInfo repositoryInfo;

   public JcrVfsRepositoryService(IRepositoryConfiguration configuration, String partitionId)
   {
      super();
      this.partitionId = partitionId;
      this.repositoryId = (String) configuration.getAttributes().get("repositoryId");
      this.providerId = (String) configuration.getAttributes().get("providerId");
      this.jndiName = (String) configuration.getAttributes().get("jndiName");
      
      initRepositoryInfo(configuration);
   }
   
   protected void initRepositoryInfo(IRepositoryConfiguration configuration)
   {
      this.repositoryInfo = new JcrVfsRepositoryInstanceInfo(repositoryId);
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
   public void setVfs(IDocumentRepositoryService vfs)
   {
      this.vfs = vfs;
   }

   @Override
   public IDocumentRepositoryService getVfs()
   {
      return vfs;
   }
   
   @Override
   public void initialize(Parameters parameters, User user)
   {
      // repository will be retrieved from bean local JNDI location
      // TODO init via JNDI config
   }
   
   @Override
   public void close(User user)
   {      
      if (vfs != null && vfs instanceof EjbDocumentRepositoryService)
      {
         ((EjbDocumentRepositoryService) vfs).closeJcrSessions();
      }
   }

   @Override
   public void cleanup(User user)
   {
   }

}
