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

import org.eclipse.stardust.engine.core.preferences.PreferenceScope;


/**
 * @author sauer
 * @version $Revision: $
 */
public abstract class AbstractPreferenceEditor
      implements IPreferenceEditor
{
   
   protected final AbstractPreferenceStore prefsStore;
   
   protected final String moduleId;

   protected final String preferencesId;
   
   protected boolean isModified;
   
   public AbstractPreferenceEditor(String moduleId, String preferencesId,
         AbstractPreferenceStore prefsStore)
   {
      this.moduleId = moduleId;
      this.preferencesId = preferencesId;

      this.prefsStore = prefsStore;
      
      this.isModified = false;
   }

   public abstract void save();

   public void resetValue(String name)
   {
      if ( !prefsStore.isEmpty())
      {
         this.isModified = true;
         prefsStore.getPreferences().remove(name);
      }
   }

   public void setValue(String name, boolean value)
   {
      this.isModified = true;
      
      prefsStore.updatePreferencesValue(name, value);
   }

   public void setValue(String name, double value)
   {
      this.isModified = true;
      
      prefsStore.updatePreferencesValue(name, value);
   }

   public void setValue(String name, float value)
   {
      this.isModified = true;
      
      prefsStore.updatePreferencesValue(name, value);
   }

   public void setValue(String name, int value)
   {
      this.isModified = true;
      
      prefsStore.updatePreferencesValue(name, value);
   }

   public void setValue(String name, long value)
   {
      this.isModified = true;
      
      prefsStore.updatePreferencesValue(name, value);
   }

   public void setValue(String name, String value)
   {
      this.isModified = true;
      
      prefsStore.updatePreferencesValue(name, value);
   }
   
   ////
   //  IPreferenceStore
   ////

   public PreferenceScope getScope()
   {
      return prefsStore.getScope();
   }
   
   public boolean contains(String name)
   {
      return prefsStore.contains(name);
   }

   public boolean getBoolean(String name)
   {
      return prefsStore.getBoolean(name);
   }

   public double getDouble(String name)
   {
      return prefsStore.getDouble(name);
   }

   public float getFloat(String name)
   {
      return prefsStore.getFloat(name);
   }

   public int getInt(String name)
   {
      return prefsStore.getInt(name);
   }

   public long getLong(String name)
   {
      return prefsStore.getLong(name);
   }

   public PreferenceScope getScope(String name)
   {
      return prefsStore.getScope(name);
   }

   public String getString(String name)
   {
      return prefsStore.getString(name);
   }

   public boolean isEmpty()
   {
      return prefsStore.isEmpty();
   }



}
