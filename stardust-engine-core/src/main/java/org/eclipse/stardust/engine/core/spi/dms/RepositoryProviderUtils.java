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
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;

public class RepositoryProviderUtils
{
   public static final String MODULE_ID_REPOSITORY_CONFIGURATIONS = "RepositoryConfigurations";

   public static final String MODULE_ID_REPOSITORY_MANAGER = "RepositoryManager";

   public static final String PREFERENCES_ID_SETTINGS = "Settings";

   public static final String DEFAULT_REPOSITORY_ID = "defaultRepositoryId";

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

   public static UserContext getUserContext()
   {
      return UserContext.getInstance();
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

   public static void saveDefaultRepositoryId(String defaultRepositoryId)
   {
      IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();

      preferenceStore.savePreferences(
            new Preferences(PreferenceScope.PARTITION, MODULE_ID_REPOSITORY_MANAGER,
                  PREFERENCES_ID_SETTINGS, Collections.singletonMap(
                        DEFAULT_REPOSITORY_ID, (Serializable) defaultRepositoryId)),
            false);
   }

   public static String loadDefaultRepositoryId()
   {
      IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();

      Preferences preferences = preferenceStore.getPreferences(PreferenceScope.PARTITION,
            MODULE_ID_REPOSITORY_MANAGER, PREFERENCES_ID_SETTINGS);

      return (String) preferences.getPreferences().get(DEFAULT_REPOSITORY_ID);
   }

   /**
    * Throws an exception if {@link Constants#CARNOT_ARCHIVE_AUDITTRAIL} is set.
    * This method should only be called for write operations.
    */
   public static void checkWriteInArchiveMode()
   {
      // prevent write in archive mode
      if (Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         throw new DocumentManagementServiceException(
               BpmRuntimeError.DMS_SECURITY_ERROR_WRITE_IN_ARCHIVE_MODE.raise());
      }
   }


}
