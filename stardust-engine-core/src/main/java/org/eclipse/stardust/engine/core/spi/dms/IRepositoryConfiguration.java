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

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a repository configuration.<br>
 * The {@link IRepositoryConfiguration#PROVIDER_ID} and
 * {@link IRepositoryConfiguration#REPOSITORY_ID} are keys that are always required for a
 * valid configuration. <br>
 * Each specific repository configuration may require additional keys for e.g. connection URL, jndiName, etc.
 * 
 * @author Roland.Stamm
 */
public interface IRepositoryConfiguration extends Serializable
{

   /**
    * The id of the {@link IRepositoryProvider} which should be configured.
    */
   public static final String PROVIDER_ID = "providerId";

   /**
    * A choosable id for the {@link IRepositoryInstance}.
    * This Id must be unique as it identifies one specific repository instance.
    */
   public static final String REPOSITORY_ID = "repositoryId";

   /**
    * The attributes map that contains the actual configuration data.
    * 
    * @return the attributes map.
    */
   public Map<String, Serializable> getAttributes();

}
