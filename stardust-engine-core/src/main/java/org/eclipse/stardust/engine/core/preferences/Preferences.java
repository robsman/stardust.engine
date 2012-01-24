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

import java.io.Serializable;
import java.util.Map;


/**
 * A runtime object holding a map of preferences in form of a Map<String, Serializable>.
 * This map of preferences is identified by a <code>moduleId, preferencesId</code> and a <code>PreferencesScope</code>
 * where depending on the <code>PreferencesScope partitionId, realmId</code> and <code>userId</code> are also used.
 *
 */
public class Preferences implements Serializable
{
   private static final long serialVersionUID = 1L;

   private Map<String, Serializable> preferences;

   private String moduleId;

   private String preferencesId;

   private PreferenceScope scope;

   private String partitionId;

   private String realmId;

   private String userId;

   private PreferenceCacheHint preferenceCacheHint;

   public Preferences(PreferenceScope scope, String moduleId, String preferencesId,
         Map<String, Serializable> preferences)
   {
      super();
      this.scope = scope;
      this.moduleId = moduleId;
      this.preferencesId = preferencesId;
      this.preferences = preferences;
   }

   /**
    * Retrieves the map of preferences.
    *
    * @return The map of preferences.
    */
   public Map<String, Serializable> getPreferences()
   {
      return preferences;
   }

   /**
    * Sets the map of preferences.
    *
    * @param preferences The map of preferences.
    */
   public void setPreferences(Map<String, Serializable> preferences)
   {
      this.preferences = preferences;
   }

   /**
    * The moduleId used as a first identifier for the preferences.
    *
    * @return The moduleId.
    */
   public String getModuleId()
   {
      return moduleId;
   }

   /**
    * The moduleId used as a first identifier for the preferences.
    * May not be <code>null</code>.
    *
    * @param moduleId The moduleId.
    */
   public void setModuleId(String moduleId)
   {
      this.moduleId = moduleId;
   }

   /**
    * The preferencesId is used as second identifier for the preferences.
    *
    * @return The preferencesId.
    */
   public String getPreferencesId()
   {
      return preferencesId;
   }

   /**
    * The preferencesId is used as second identifier for the preferences.
    * May not be <code>null</code>.
    *
    * @param preferencesId The preferencesId.
    */
   public void setPreferencesId(String preferencesId)
   {
      this.preferencesId = preferencesId;
   }

   /**
    * The scope the preferences are stored in.
    *
    * @return The PreferencesScope.
    */
   public PreferenceScope getScope()
   {
      return scope;
   }

   /**
    * The scope the preferences are stored in.
    *
    * @param scope The PreferencesScope.
    */
   public void setScope(PreferenceScope scope)
   {
      this.scope = scope;
   }

   /**
    * @return The partitionId the preferences are stored in.
    */
   public String getPartitionId()
   {
      return partitionId;
   }

   /**
    * This value cannot be changed from client-side, it is only set on server-side.
    * The partitionId is internally determined by the logged in user's partition.
    *
    * @param partitionId The partitionId.
    */
   public void setPartitionId(String partitionId)
   {
      this.partitionId = partitionId;
   }

   /**
    * Returns the realmId if valid for the PreferencesScope.
    *
    * @return The realmId of the preferences.
    */
   public String getRealmId()
   {
      return realmId;
   }

   /**
    * Sets the realmId. Only valid in USER, REALM PreferencesScopes.
    * Changing this value is restricted to administrators.
    *
    * @param realmId The realmId of the preferences.
    */
   public void setRealmId(String realmId)
   {
      this.realmId = realmId;
   }

   /**
    * Returns the userId if valid for the PreferencesScope.
    *
    * @return
    */
   public String getUserId()
   {
      return userId;
   }

   /**
    * Sets the userId. Only valid in USER PreferencesScope.
    * Changing this value is restricted to administrators.
    *
    * @param userId
    */
   public void setUserId(String userId)
   {
      this.userId = userId;
   }

   /**
    * Holds information for cache synchronization.
    *
    * @return
    */
   public PreferenceCacheHint getPreferenceCacheHint()
   {
      return preferenceCacheHint;
   }

   /**
    * Internally used.
    *
    * @param preferenceCacheHint
    */
   public void setPreferenceCacheHint(PreferenceCacheHint preferenceCacheHint)
   {
      this.preferenceCacheHint = preferenceCacheHint;
   }

}
