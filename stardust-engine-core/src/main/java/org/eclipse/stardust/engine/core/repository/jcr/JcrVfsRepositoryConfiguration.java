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

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;

public class JcrVfsRepositoryConfiguration
      implements IRepositoryConfiguration
{
   private static final long serialVersionUID = 5386597909598794074L;
   
   private Map<String, Serializable> attributes;

   public static final String REPOSITORY_CONFIG_LOCATION = "repositoryConfigLocation";

   public static final String CONFIG_DISABLE_VERSIONING = "disableVersioning";
   
   public static final String CONFIG_JNDI_NAME = "jndiName";
   
   public static final String IS_IN_MEMORY_TEST_REPO = "inMemoryTestRepo";
   
   public static final String IS_DEFAULT_REPOSITORY = "isDefaultRepository";

   public JcrVfsRepositoryConfiguration(Map<String, Serializable> attributes)
   {
      this.attributes = attributes;
     
   }

   @Override
   public Map<String, Serializable> getAttributes()
   {
      return attributes;
   }

}
