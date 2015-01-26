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

import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;

public class EmbeddedPreferenceEditor extends AbstractPreferenceEditor
{

   public EmbeddedPreferenceEditor(String moduleId, String preferencesId,
         AbstractPreferenceStore prefsStore)
   {
      super(moduleId, preferencesId, prefsStore);
     
   }

   public void save()
   {
      Preferences preferences = new Preferences(getScope(), moduleId, preferencesId, prefsStore.getPreferences());
      PreferenceStorageFactory.getCurrent().savePreferences(preferences, true);
   }
}
