/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.preferences;


public class PreferencePathBuilder
{

   public static String getPreferencesModuleIdPath(Preferences preferences)
   {
      return getPreferencesFolderPath(preferences.getScope(),
           preferences.getRealmId(),
            preferences.getUserId())
            + preferences.getModuleId() + "/";

   }

   public static String getPreferencesFolderPath(PreferenceScope scope,
         String realmId, String userId)
   {
      if (PreferenceScope.USER == scope)
      {

         return "/" //
               + IPreferencesPersistenceManager.REALMS_FOLDER + realmId + "/" //
               + IPreferencesPersistenceManager.USERS_FOLDER + userId + "/" //
               + IPreferencesPersistenceManager.PREFS_FOLDER;
      }
      else if (PreferenceScope.REALM == scope)
      {
         return "/" //
               + IPreferencesPersistenceManager.REALMS_FOLDER + realmId + "/" //
               + IPreferencesPersistenceManager.PREFS_FOLDER;
      }
      else if (PreferenceScope.PARTITION == scope)
      {
         return "/"//
               + IPreferencesPersistenceManager.PREFS_FOLDER;
      }
      else
      {
         throw new UnsupportedOperationException(
               "DmsPersistenceManager does not support preference scope: " + scope);
      }

   }

}
