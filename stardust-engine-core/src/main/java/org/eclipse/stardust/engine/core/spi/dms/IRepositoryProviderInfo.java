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
 * Provides information about a {@link IRepositoryProvider}.
 * 
 * @author Roland.Stamm
 */
public interface IRepositoryProviderInfo extends IRepositoryCapabilities
{
   /**
    * @return The Id that identifies a {@link IRepositoryProvider}.
    */
   public String getProviderId();

   /**
    * @return A human readable name for the provider.
    */
   public String getProviderName();

   /**
    * To simplify configuration using a UI the repository configuration returned here
    * should contain keys that are needed by the provider. <br>
    * Also values can be provided as templates to make configuration more intuitive.
    * <p>
    * Example:
    * 
    * <pre>
    *    key   : jndiName
    *    value : java:/jcr/repository
    *    
    *    key   : serverURL
    *    value : https://host:port/context/repository
    * </pre>
    * 
    * @return A configuration template.
    */
   public IRepositoryConfiguration getConfigurationTemplate();

}
