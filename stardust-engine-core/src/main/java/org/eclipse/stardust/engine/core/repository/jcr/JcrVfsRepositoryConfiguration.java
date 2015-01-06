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

   // Public use
   public static final String JNDI_NAME = "jndiName";

   public static final String USER_LEVEL_AUTHORIZATION = "userLevelAuthorization";

   public static final String DISABLE_CAPABILITY_VERSIONING = "disableVersioning";

   public static final String DISABLE_CAPABILITY_WRITE = "disableWrite";

   // Internal use
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

   public static boolean getBoolean(IRepositoryConfiguration configuration, String key,
         boolean defaultValue)
   {
      boolean ret = defaultValue;
      if (configuration != null)
      {
         Serializable serializable = configuration.getAttributes().get(key);
         if (serializable == null)
         {
            // use default value
         }
         else if (serializable instanceof Boolean)
         {
            ret = (Boolean) serializable;
         }
         else if (serializable instanceof String)
         {
            ret = Boolean.valueOf((String) serializable);
         }
      }
      return ret;
   }

}
