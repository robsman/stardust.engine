/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.compatibility.ui.preferences;

import org.eclipse.stardust.engine.core.preferences.manager.AbstractPreferenceEditor;
import org.eclipse.stardust.engine.core.preferences.manager.AbstractPreferenceStore;

public class ReadOnlyUiPreferenceEditor extends AbstractPreferenceEditor
{

   public ReadOnlyUiPreferenceEditor(String moduleId, String preferencesId,
         AbstractPreferenceStore prefsStore)
   {
      super(moduleId, preferencesId, prefsStore);
   }
   
   @Override
   public void save()
   {
   }

   @Override
   public void setValue(String name, boolean value)
   {}

   @Override
   public void setValue(String name, double value)
   {}

   @Override
   public void setValue(String name, float value)
   {}

   @Override
   public void setValue(String name, int value)
   {}

   @Override
   public void setValue(String name, long value)
   {}

   @Override
   public void setValue(String name, String value)
   {}

}
