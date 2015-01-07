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

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;




/**
 * @author sauer
 * @version $Revision: $
 */
public class PreferenceStore extends AbstractPreferenceStore
{

   private final PreferenceScope scope;

   private Map preferences;

   public PreferenceStore(PreferenceScope scope, Map preferences)
   {
      this.scope = scope;
      this.preferences = preferences;
   }

   public Map getPreferences()
   {
      if (null == preferences)
      {
         this.preferences = CollectionUtils.newTreeMap();
      }
      return preferences;
   }

   protected String updatePreferencesValue(String name, String value)
   {
      return (String) getPreferences().put(name, value);
   }

   protected Boolean updatePreferencesValue(String name, boolean value)
   {
      return (Boolean) getPreferences().put(name, new Boolean(value));
   }

   protected Double updatePreferencesValue(String name, double value)
   {
      return (Double) getPreferences().put(name, new Double(value));
   }

   protected Float updatePreferencesValue(String name, float value)
   {
      return (Float) getPreferences().put(name, new Float(value));
   }

   protected Integer updatePreferencesValue(String name, int value)
   {
      return (Integer) getPreferences().put(name, new Integer(value));
   }

   protected Long updatePreferencesValue(String name, long value)
   {
      return (Long) getPreferences().put(name, new Long(value));
   }

   public PreferenceScope getScope()
   {
      return scope;
   }

}
