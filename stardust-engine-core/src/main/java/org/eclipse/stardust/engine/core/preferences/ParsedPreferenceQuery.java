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

public class ParsedPreferenceQuery
{
   private PreferenceScope scope;

   private String moduleId;

   private String preferencesId;

   private String realmId;

   private String userId;

   public ParsedPreferenceQuery(PreferenceScope scope, String moduleId,
         String preferencesId, String realmId, String userId)
   {
      this.scope = scope;
      this.moduleId = moduleId;
      this.preferencesId = preferencesId;
      this.realmId = realmId;
      this.userId = userId;
   }

   public PreferenceScope getScope()
   {
      return scope;
   }

   public String getModuleId()
   {
      return moduleId;
   }

   public String getPreferencesId()
   {
      return preferencesId;
   }

   public String getRealmId()
   {
      return realmId;
   }

   public String getUserId()
   {
      return userId;
   }
   
   

}