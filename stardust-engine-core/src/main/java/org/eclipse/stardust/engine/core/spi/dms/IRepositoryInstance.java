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



/**
 * Represents a repository instance which was initialized by a {@link IRepositoryProvider}.
 * A repository instance is uniquely identified by the repositoryId.
 * <p>
 * Via {@link #getService(UserContext)} a repository service is retrieved which handles all operations on the repository.
 *
 * @author Roland.Stamm
 */
public interface IRepositoryInstance
{

   /**
    * @return the unique identified of this repository instance.
    */
   public String getRepositoryId();

   /**
    * @return the id of the provider this instance was created from.
    */
   public String getProviderId();

   /**
    * @return the partitionId which this repository instance belongs to.
    */
   public String getPartitionId();

   /**
    * @return information about the instance including the {@link IRepositoryCapabilities}.
    */
   public IRepositoryInstanceInfo getRepositoryInstanceInfo();

   /**
    * Retrieves a {@link IRepositoryService} which contains the methods for repository
    * operations.
    *
    * @param userContext
    *           Allows to retrieve the user which requests a repository service instance and contains methods
    *           to bind the service to the user's lifecycle.
    * @return an instance of the {@link IRepositoryService}.
    */
   public IRepositoryService getService(UserContext userContext);

   /**
    * The close method is invoked at the end of a service call and should be used to free resources reserved by {@link IRepositoryService}.
    *
    * @param repositoryService
    */
   public void close(IRepositoryService repositoryService);

}
