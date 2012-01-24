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
package org.eclipse.stardust.engine.core.compatibility.ui.preferences;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.*;
import org.eclipse.stardust.engine.core.preferences.manager.AbstractPreferenceEditor;
import org.eclipse.stardust.engine.core.preferences.manager.AbstractPreferenceStore;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceEditor;


public class UiPreferenceEditor extends AbstractPreferenceEditor implements IPreferenceEditor
{

   private IPreferenceCache cacheManager;

   private ServiceFactory sf;
   
   public UiPreferenceEditor(String moduleId, String preferencesId,
         AbstractPreferenceStore prefsStore, ServiceFactory sf,
         IPreferenceCache cacheManager)
   {
      super(moduleId, preferencesId, prefsStore);
      
      this.sf = sf;
      
      this.cacheManager = cacheManager;
   }
   
   public void save()
   {
      if (isModified)
      {
         final PreferenceScope scope = getScope();
         sf.getAdministrationService().savePreferences(
               new Preferences(scope, moduleId, preferencesId,
                     prefsStore.getPreferences()));
         cacheManager.cleanCache(scope, moduleId, preferencesId);
         isModified = false;
      }
   }

}
