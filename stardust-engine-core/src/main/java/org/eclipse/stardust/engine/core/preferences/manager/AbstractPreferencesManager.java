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

import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;




public abstract class AbstractPreferencesManager implements IPreferencesManager
{

   public abstract IPreferenceStore getPreferences(String moduleId, String preferencesId);

   public abstract IPreferenceStore getPreferences(PreferenceScope scope, String moduleId,
         String preferencesId);

   public abstract IPreferenceEditor getPreferencesEditor(PreferenceScope scope, String moduleId,
         String preferencesId);


   protected Set getAllParentScopes(PreferenceScope scope)
   {
      Set parentScopes = CollectionUtils.newSet();
      do
      {
         PreferenceScope parentScope = getParentScope(scope);
         if (parentScope != null)
         {
            parentScopes.add(parentScope);
         }
         scope = parentScope;
      }
      while (scope != null);
      return parentScopes;
   }

   protected PreferenceScope getParentScope(PreferenceScope scope)
   {
      if (PreferenceScope.USER == scope)
      {
         return PreferenceScope.REALM;
      }
      else if (PreferenceScope.REALM == scope)
      {
         return PreferenceScope.PARTITION;
      }
      else if (PreferenceScope.PARTITION == scope)
      {
         return PreferenceScope.DEFAULT;
      }
      else if (PreferenceScope.DEFAULT == scope)
      {
         return null;
      }
      else
      {
         throw new IllegalArgumentException("Unsupported scope: " + scope);
      }
   }
   
   protected EmptyPreferenceStore getEmptyPrefs(PreferenceScope scope)
   {
      if (PreferenceScope.USER == scope)
      {
         return EmptyPreferenceStore.EMPTY_USER_PREFS;
      }
      else if (PreferenceScope.REALM == scope)
      {
         return EmptyPreferenceStore.EMPTY_REALM_PREFS;
      }
      else if (PreferenceScope.PARTITION == scope)
      {
         return EmptyPreferenceStore.EMPTY_PARTITION_PREFS;
      }
      else if (PreferenceScope.DEFAULT == scope)
      {
         return EmptyPreferenceStore.EMPTY_DEFAULT_PREFS;
      }
      else
      {
         throw new IllegalArgumentException("Unsupported scope: " + scope);
      }
   }

}
