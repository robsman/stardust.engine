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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.query.PreferenceQueryEvaluator;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ReconfigurationInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.dms.data.TransientUser;



public class PreferenceStorageManager implements IPreferenceStorageManager
{
   public static final String PREFERENCES_STORE_DMS = "DMS_READ_ONLY";

   public static final String PREFERENCES_STORE_AUDIT_TRAIL = "AuditTrail";

   public static final String PRP_PREFERENCES_STORE = "Infinity.Preferences.Store";

   private IPreferencesReader preferenceReader;

   private IPreferencesWriter preferenceWriter;

   private IPreferencesPersistenceManager persistenceManager;

   private Map<String, PreferenceChangeHandler> preferenceChangeHandlers;

   public PreferenceStorageManager()
   {
      this(Collections.EMPTY_LIST);
   }

   public PreferenceStorageManager(List<PreferenceChangeHandler> preferenceChangeHandlers)
   {
      preferenceReader = new XmlPreferenceReader();
      preferenceWriter = new XmlPreferenceWriter();

      if (preferenceChangeHandlers == null || preferenceChangeHandlers.isEmpty())
      {
         this.preferenceChangeHandlers = Collections.EMPTY_MAP;
      }
      else
      {
         this.preferenceChangeHandlers = CollectionUtils.newHashMap();
         for (PreferenceChangeHandler preferenceChangeHandler : preferenceChangeHandlers)
         {
            this.preferenceChangeHandlers.put(preferenceChangeHandler.getModuleId(),
                  preferenceChangeHandler);
         }

      }
      IPreferencesPersistenceManager subPersistenceManager;
      String storeLocation = Parameters.instance().getString(PRP_PREFERENCES_STORE,
            PREFERENCES_STORE_AUDIT_TRAIL);

      if (storeLocation.equals(PREFERENCES_STORE_AUDIT_TRAIL))
      {
         subPersistenceManager = new AuditTrailPersistenceManager();
      }
      else if (storeLocation.equals(PREFERENCES_STORE_DMS))
      {
         subPersistenceManager = new DmsPersistenceManager();
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.PREF_UNKNOWN_VALUE_FOR_PROPERTY_INFINITY_PREFERENCE_STORE
                     .raise());
      }
      this.persistenceManager = new GlobalsCachedPersistenceManager(subPersistenceManager);
   }

   private IPreferencesPersistenceManager getPersistenceManager()
   {
      return persistenceManager;
   }

   public Preferences getPreferences(PreferenceScope scope, String moduleId,
         String preferenceId)
   {
      Preferences preferences = getPersistenceManager().loadPreferences(scope, moduleId,
            preferenceId, preferenceReader);

      return preferences;
   }

   @Override
   public Preferences getPreferences(IUser user, PreferenceScope scope, String moduleId,
         String preferenceId)
   {
      Preferences preferences = getPersistenceManager().loadPreferences(user, scope, moduleId,
            preferenceId, preferenceReader);

      return preferences;
   }

   public List<ReconfigurationInfo> savePreferences(Preferences preferences, boolean force)
   {
      checkUpdatePermissions(preferences);

      List<ReconfigurationInfo> infos = CollectionUtils.newArrayList();
      if ( !preferenceChangeHandlers.isEmpty())
      {
         PreferenceChangeHandler handler = preferenceChangeHandlers.get(preferences
               .getModuleId());
         if (handler != null)
         {
            try
            {
               infos.addAll(handler
                     .fireEvent(new PreferenceChangeEvent(preferences, force)));
            }
            catch (PreferenceChangeException t)
            {
               if ( !force)
               {
                  throw new PublicException(t);
               }
            }
         }
      }

      for (ReconfigurationInfo reconfigurationInfo : infos)
      {
         if ( !reconfigurationInfo.success())
         {
            return infos;
         }
      }

      getPersistenceManager().updatePreferences(preferences, preferenceWriter);
      return infos;
   }

   public List<Preferences> getAllPreferences(PreferenceQuery preferenceQuery, boolean checkPermissions)
   {
      ParsedPreferenceQuery parsedQuery = new PreferenceQueryEvaluator(preferenceQuery).getParsedQuery();

      if (checkPermissions)
      {
         ParsedPreferenceQuery secureParsedQuery = checkGetAllPermissions(parsedQuery);

         return getPersistenceManager().getAllPreferences(secureParsedQuery,
               preferenceReader);
      }
      else
      {
         return getPersistenceManager().getAllPreferences(parsedQuery,
               preferenceReader);
      }
   }

   public void flushCaches()
   {
      if (persistenceManager instanceof IPreferenceCache)
      {
         ((IPreferenceCache) persistenceManager).flushCaches();
      }
   }

   private ParsedPreferenceQuery checkGetAllPermissions(ParsedPreferenceQuery pq)
   {
      final IUser user = SecurityProperties.getUser();

      String realmId = null;
      String userId = null;
      // TransientUser is always Admin, follow up Issue will prevent creating details objects
      if (user != null && !(user instanceof TransientUser))
      {
         User userDetails = (User) DetailsFactory.create(SecurityProperties.getUser(),
               IUser.class, UserDetails.class);

         realmId = user.getRealm().getId();
         userId = user.getId();

         if ( !userDetails.isAdministrator())
         {
            // Restrict query to current user's realmId, userId
            return new ParsedPreferenceQuery(pq.getScope(), pq.getModuleId(),
                  pq.getPreferencesId(), realmId, userId);
         }
      }
      else if(!(user instanceof TransientUser))
      {
         throw new AccessForbiddenException(BpmRuntimeError.AUTHx_NOT_LOGGED_IN.raise());
      }
      return pq;
   }

   private void checkUpdatePermissions(Preferences preferences)
   {
      final IUser user = SecurityProperties.getUser();

      String realmId = null;
      String userId = null;
      if (user != null)
      {
         User userDetails = (User) DetailsFactory.create(SecurityProperties.getUser(),
               IUser.class, UserDetails.class);

         realmId = user.getRealm().getId();
         userId = user.getId();

         if ( !userDetails.isAdministrator())
         {
            final PreferenceScope scope = preferences.getScope();
            if (PreferenceScope.USER.equals(scope) || PreferenceScope.REALM.equals(scope))
            {
               // Only null or own realmId, userId allowed;
               if ((preferences.getRealmId() != null && !preferences.getRealmId().equals(
                     realmId))
                     || ((preferences.getUserId() != null && !preferences.getUserId()
                           .equals(userId))))
               {
                  throw new AccessForbiddenException(
                        BpmRuntimeError.AUTHx_AUTH_SAVING_OWN_PREFERENCES_FAILED.raise());
               }

               // ALLOWED: Non admin users are only allowed to save preferences to own
               // realm/user scope.
            }
         }
      }
      else
      {
         // throw new PublicException(
         // "Security check on Preferences failed: No current user found.");
      }
   }
}
