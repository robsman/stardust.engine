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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;


public class PreferencePathParser
{

   private final static String REALMS_FOLDER = IPreferencesPersistenceManager.REALMS_FOLDER;

   private final static String USERS_FOLDER = IPreferencesPersistenceManager.USERS_FOLDER;

   private final static String PREFS_FOLDER = IPreferencesPersistenceManager.PREFS_FOLDER;

   private PreferenceScope scope = null;

   private String moduleId = null;

   private String realmId = null;

   private String userId = null;

   public PreferencePathParser(String path)
   {
      evaluate(path);
   }

   private void evaluate(String path)
   {
      StringTokenizer st = new StringTokenizer(path, "/");

      ArrayList<String> tokens = CollectionUtils.newArrayList();
      while (st.hasMoreTokens())
      {
         String token = st.nextToken();
         tokens.add(token);
      }

      boolean valid = false;

      try
      {
         if (PREFS_FOLDER.equals(tokens.get(0) + "/"))
         {
            // preferences
            this.scope = PreferenceScope.PARTITION;
            this.moduleId = tokens.get(1);
            valid = true;
         }
         else if (REALMS_FOLDER.equals(tokens.get(0) + "/"))
         {

            this.realmId = tokens.get(1);

            if (PREFS_FOLDER.equals(tokens.get(2) + "/"))
            {
               // realms/<realmId>/preferences
               this.scope = PreferenceScope.REALM;
               this.moduleId = tokens.get(3);
               valid = true;
            }
            else if (USERS_FOLDER.equals(tokens.get(2) + "/"))
            {
               // realms/<realmId>/users/<userId>/preferences
               this.scope = PreferenceScope.USER;
               this.userId = tokens.get(3);
               if (PREFS_FOLDER.equals(tokens.get(4) + "/"))
               {
                  this.moduleId = tokens.get(5);
                  valid = true;
               }
            }
         }
      }
      catch (IndexOutOfBoundsException ie)
      {
         valid = false;
      }

      if ( !valid)
      {
         throw new PublicException("Not a valid preferences path: " + path);
      }
   }

   public PreferenceScope getScope()
   {
      return scope;
   }

   public String getModuleId()
   {
      return moduleId;
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
