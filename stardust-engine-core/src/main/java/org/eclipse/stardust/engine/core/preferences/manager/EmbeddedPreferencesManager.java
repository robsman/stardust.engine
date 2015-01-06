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
package org.eclipse.stardust.engine.core.preferences.manager;

import org.eclipse.stardust.engine.core.preferences.*;

public class EmbeddedPreferencesManager extends AbstractPreferencesManager implements IPreferencesManager.Factory
{

   public IPreferenceStore getPreferences(String moduleId, String preferencesId)
   {
      return getPreferences(PreferenceScope.USER, moduleId, preferencesId);
   }

   public IPreferenceStore getPreferences(PreferenceScope scope, String moduleId,
         String preferencesId)
   {
      return getPreferences(scope, moduleId, preferencesId, false);
   }

   public IPreferenceEditor getPreferencesEditor(PreferenceScope scope, String moduleId,
         String preferencesId)
   {
      if ((PreferenceScope.DEFAULT == scope))
      {
         // unmanaged scopes are read only
         return null;
      }

      AbstractPreferenceStore prefsStore = getPreferences(scope, moduleId, preferencesId,
            true);

      return new EmbeddedPreferenceEditor(moduleId, preferencesId, prefsStore);
   }

   private AbstractPreferenceStore getPreferences(PreferenceScope scope, String moduleId,
         String preferencesId, boolean editable)
   {
      IPreferenceStorageManager preferenceStorage = PreferenceStorageFactory.getCurrent();
      
      Preferences prefs = preferenceStorage.getPreferences(scope, moduleId, preferencesId);

      AbstractPreferenceStore result = new PreferenceStore(scope, prefs.getPreferences());

      // connect parent scopes
      final AbstractPreferenceStore parent;
      PreferenceScope parentScope = getParentScope(scope);
      if ((null == result.getParent()) && (null != parentScope))
      {
         parent = getPreferences(parentScope, moduleId, preferencesId, false);

         if (result.isEmpty() && !editable)
         {
            if (parent instanceof EmptyPreferenceStore)
            {
               result = getEmptyPrefs(scope);
            }
            else
            {
               result.setParent(parent);
            }
         }
         else
         {
            result.setParent(parent);
         }
      }

      if (null == result)
      {
         result = EmptyPreferenceStore.EMPTY_DEFAULT_PREFS;
      }

      return result;
   }

   public IPreferencesManager getPreferencesManager()
   {
     return new EmbeddedPreferencesManager();
   }

}
