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

import java.util.List;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * This SPI allows to implement {@link IRepositoryProvider} which provides an access layer
 * for arbitrary repository technologies.
 * <p>
 * <h3>General Design</h3>
 * <ul>
 * <li>{@link IRepositoryProvider} Represents a provider for a specific repository
 * technology.</li>
 * <li>{@link IRepositoryInstance} Represents one instance of a specific repository.</li>
 * <li>{@link IRepositoryService} Represents a service on the specific repository
 * instance.</li>
 * </ul>
 * <h3>Lifecycle</h3><br>
 * The {@link IRepositoryProvider} is loaded as part of the SPI contract.
 * <p>
 * A {@link IRepositoryInstance} is usually located via a distinct URL or jndiName and is
 * created and destroyed at runtime using the following methods:
 * <ul>
 * <li>{@link #createInstance(IRepositoryConfiguration, String)}</li>
 * <li>{@link #destroyInstance(IRepositoryInstance)}</li>
 * </ul>
 * <p>
 * A {@link IRepositoryService} usually represents a session on the
 * {@link IRepositoryInstance} and contains all methods needed for repository operations.
 * <p>
 * <h3>Default Instances</h3><br>
 * If a configuration is supplied via {@link #getDefaultConfigurations()} the repository
 * instances are created after the provider is loaded.
 * 
 * @author Roland.Stamm
 */
@SPI(status = Status.Experimental, useRestriction = UseRestriction.Public)
public interface IRepositoryProvider
{

   /**
    * Factory for {@link IRepositoryProvider}.
    */
   public interface Factory
   {
      IRepositoryProvider getInstance();
   }

   /**
    * @return The id of the provider. Each provider must have a different Id.
    */
   public String getProviderId();

   /**
    * Optionally configurations that should be used to create repository instances after
    * the provider has loaded can be specified.
    * 
    * @return repository configurations that should be used to implicitly create
    *         repository instances.
    */
   public List<IRepositoryConfiguration> getDefaultConfigurations();

   /**
    * This is used to create a {@link IRepositoryInstance}.
    * 
    * @param configuration
    *           The configuration for the instance.
    * @param partitionId
    *           The partition on which the instance is used.
    * @return An initialized instance.
    */
   public IRepositoryInstance createInstance(IRepositoryConfiguration configuration,
         String partitionId);

   /**
    * This is called when a {@link IRepositoryInstance} is destroyed and should cleanup
    * the used resources.
    * 
    * @param instance
    *           The instance to be destroyed.
    */
   public void destroyInstance(IRepositoryInstance instance);

   /**
    * @return Information about the provider.
    */
   public IRepositoryProviderInfo getProviderInfo();

}
