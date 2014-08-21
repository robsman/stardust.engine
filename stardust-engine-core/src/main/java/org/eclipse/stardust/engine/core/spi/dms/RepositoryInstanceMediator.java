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


public class RepositoryInstanceMediator implements IRepositoryInstance
{

   private IRepositoryInstance instance;
   private RepositoryManager repositoryManager;

   public RepositoryInstanceMediator(IRepositoryInstance instance, RepositoryManager repositoryManager)
   {
      this.instance = instance;
      this.repositoryManager = repositoryManager;
   }

   @Override
   public String getRepositoryId()
   {
      return instance.getRepositoryId();
   }

   @Override
   public String getProviderId()
   {
      return instance.getProviderId();
   }

   @Override
   public String getPartitionId()
   {
      return instance.getPartitionId();
   }

   @Override
   public IRepositoryInstanceInfo getRepositoryInstanceInfo()
   {
      return instance.getRepositoryInstanceInfo();
   }

   @Override
   public IRepositoryService getService(UserContext userContext)
   {
      return new MetaDataMediator(instance.getService(userContext), repositoryManager);
   }

   @Override
   public void close(IRepositoryService repositoryService)
   {
      instance.close(repositoryService);
   }

}
