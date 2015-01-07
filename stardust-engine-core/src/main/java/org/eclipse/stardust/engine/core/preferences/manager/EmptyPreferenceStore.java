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

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;




/**
 * @author sauer
 * @version $Revision: $
 */
public class EmptyPreferenceStore extends AbstractPreferenceStore
{
   
   public static final EmptyPreferenceStore EMPTY_DEFAULT_PREFS = new EmptyPreferenceStore(
         PreferenceScope.DEFAULT, null);

   public static final EmptyPreferenceStore EMPTY_PARTITION_PREFS = new EmptyPreferenceStore(
         PreferenceScope.PARTITION, EMPTY_DEFAULT_PREFS);

   public static final EmptyPreferenceStore EMPTY_REALM_PREFS = new EmptyPreferenceStore(
         PreferenceScope.REALM, EMPTY_PARTITION_PREFS);

   public static final EmptyPreferenceStore EMPTY_USER_PREFS = new EmptyPreferenceStore(
         PreferenceScope.USER, EMPTY_REALM_PREFS);

   private final PreferenceScope scope;
   
   public EmptyPreferenceStore(PreferenceScope scope, EmptyPreferenceStore parent)
   {
      this.scope = scope;
      
      super.setParent(parent);
   }

   public PreferenceScope getScope()
   {
      return scope;
   }

   public Map getPreferences()
   {
      return Collections.EMPTY_MAP;
   }

   public void setParent(IPreferenceStore parent)
   {
      throw new IllegalOperationException(
            BpmRuntimeError.PREF_EMPTY_PREF_STORE_READONLY.raise());
   }

}
