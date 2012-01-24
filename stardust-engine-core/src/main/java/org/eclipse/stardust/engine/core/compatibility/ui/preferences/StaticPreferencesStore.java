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

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.manager.AbstractPreferenceStore;



/**
 * @author sauer
 * @version $Revision: $
 */
public class StaticPreferencesStore extends AbstractPreferenceStore
{
   
   private final Map prefs;
   
   public StaticPreferencesStore(Map prefs)
   {
      this.prefs = Collections.unmodifiableMap(CollectionUtils.copyMap(prefs));
   }

   public PreferenceScope getScope()
   {
      return PreferenceScope.DEFAULT;
   }
   
   public Map getPreferences()
   {
      return prefs;
   }

}
