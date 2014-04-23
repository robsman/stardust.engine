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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class RepositoryProviderUtils
{
   public static final String MODULE_ID_REPOSITORY_CONFIGURATIONS = "RepositoryConfigurations";
   
   public static final String DMS_ADMIN_SESSION = RepositoryProviderUtils.class.getName() + ".AdminSessionFlag";
   
   public RepositoryProviderUtils()
   {
      // utility class
   }
   
   public static boolean isAdminSessionFlagEnabled()
   {
      return Parameters.instance().getBoolean(DMS_ADMIN_SESSION, false);
   }
   
   public static void setAdminSessionFlag(boolean enabled, PropertyLayer layer)
   {
      layer.setProperty(DMS_ADMIN_SESSION, enabled);
   }
   
   public static User getCurrentUser()
   {
      IUser user = SecurityProperties.getUser();
      return user == null || PredefinedConstants.SYSTEM.equals(user.getId())
            ? null
            : (User) DetailsFactory.create(user, IUser.class, UserDetails.class);
   }
   
   public static void saveConfiguration(IRepositoryConfiguration configuration)
   {
      IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();
      
      String repositoryId = (String) configuration.getAttributes().get(IRepositoryConfiguration.REPOSITORY_ID);

      preferenceStore.savePreferences(
            new Preferences(PreferenceScope.PARTITION,
                  MODULE_ID_REPOSITORY_CONFIGURATIONS, repositoryId,
                  configuration.getAttributes()), false);
   }

   public static List<IRepositoryConfiguration> getAllConfigurations()
   {
      IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();

      PreferenceQuery preferenceQuery = PreferenceQuery.findPreferences(
            PreferenceScope.PARTITION, MODULE_ID_REPOSITORY_CONFIGURATIONS, "*");
      
      List<IRepositoryConfiguration> configurations = CollectionUtils.newArrayList();
      List<Preferences> allPreferences = preferenceStore.getAllPreferences(preferenceQuery, true);
      for (final Preferences preferences : allPreferences)
      {
         if (preferences.getPreferences() != null && !preferences.getPreferences().isEmpty())
         {
            configurations.add(new IRepositoryConfiguration()
            {
               private static final long serialVersionUID = 1L;

               @Override
               public Map<String, Serializable> getAttributes()
               {
                  return preferences.getPreferences();
               }
            });
         }
      }
      return configurations;
   }

   public static void removeConfiguration(String repositoryId)
   {
      IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();

      preferenceStore.savePreferences(new Preferences(PreferenceScope.PARTITION,
            MODULE_ID_REPOSITORY_CONFIGURATIONS, repositoryId, Collections.EMPTY_MAP),
            false);
   }


}
