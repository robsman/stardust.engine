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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.preferences.IStaticConfigurationProvider;



public class GlobalsCachedPersistenceManager
      implements IPreferencesPersistenceManager, IPreferenceCache
{
   private static final String KEY_DEFAULTS_CACHE = GlobalsCachedPersistenceManager.class.getName()
         + ".DefaultsCache";

   private static final String KEY_PARTITION_CACHE = GlobalsCachedPersistenceManager.class.getName()
         + ".PartitionCache";

   private static final String KEY_REALM_CACHE = GlobalsCachedPersistenceManager.class.getName()
         + ".RealmCache";

   private static final String KEY_USER_CACHE = GlobalsCachedPersistenceManager.class.getName()
         + ".UserCache";

   private final IPreferencesPersistenceManager subPersistenceManager;

   public GlobalsCachedPersistenceManager(
         IPreferencesPersistenceManager persistenceManager)
   {
      this.subPersistenceManager = persistenceManager;

   }

   protected Map<Pair, Map> getDefaultPreferencesCache()
   {
      final GlobalParameters globals = GlobalParameters.globals();

      Map defaultsCache = (Map) globals.get(KEY_DEFAULTS_CACHE);
      if (null == defaultsCache)
      {
         defaultsCache = (Map) globals.initializeIfAbsent(KEY_DEFAULTS_CACHE,
               new ValueProvider()
               {
                  public Object getValue()
                  {
                     Map cache = CollectionUtils.newMap();

                     // inspect extension services
                     List factories = ExtensionProviderUtils.getExtensionProviders(IStaticConfigurationProvider.Factory.class);
                     for (int i = 0; i < factories.size(); ++i)
                     {
                        IStaticConfigurationProvider.Factory factory = (IStaticConfigurationProvider.Factory) factories.get(i);

                        IStaticConfigurationProvider configProvider = factory.getProvider();
                        if (null != configProvider)
                        {
                           // any implementation may provide multiple categories
                           List prefsIds = configProvider.getPreferenceIds();
                           for (int j = 0; j < prefsIds.size(); ++j)
                           {
                              String prefsId = (String) prefsIds.get(j);

                              Map prefs = configProvider.getPreferenceDefaults(prefsId);
                              if ((null != prefs) && !prefs.isEmpty())
                              {
                                 cache.put(
                                       new Pair(configProvider.getModuleId(), prefsId),
                                       prefs);
                              }
                           }
                        }
                     }

                     // seal the cache
                     return Collections.unmodifiableMap(cache);
                  }
               });
      }

      return defaultsCache;
   }

   protected AgeCache getPartitionPreferencesCache(String partitionId)
   {
      final GlobalParameters globals = GlobalParameters.globals();

      ConcurrentHashMap<String, AgeCache> partitionsPrefsCache = (ConcurrentHashMap<String, AgeCache>) globals.get(KEY_PARTITION_CACHE);
      if (null == partitionsPrefsCache)
      {
         globals.initializeIfAbsent(KEY_PARTITION_CACHE, new ValueProvider()
         {

            public Object getValue()
            {
               return new ConcurrentHashMap<String, AgeCache>();
            }

         });
         partitionsPrefsCache = (ConcurrentHashMap<String, AgeCache>) globals.get(KEY_PARTITION_CACHE);
      }

      String partitionCacheKey = partitionId;
      AgeCache partitionPrefsCache = partitionsPrefsCache.get(partitionCacheKey);
      if (null == partitionPrefsCache)
      {
         partitionsPrefsCache.put(partitionCacheKey, new AgeCache(null,
               new ConcurrentHashMap<String, Map>()));
         partitionPrefsCache = (AgeCache) partitionsPrefsCache.get(partitionCacheKey);
      }

      return partitionPrefsCache;
   }

   protected AgeCache getRealmPreferencesCache(String partitionId, String realmId)
   {
      final GlobalParameters globals = GlobalParameters.globals();

      ConcurrentHashMap<String, AgeCache> realmsPrefsCache = (ConcurrentHashMap<String, AgeCache>) globals.get(KEY_REALM_CACHE);
      if (null == realmsPrefsCache)
      {
         globals.initializeIfAbsent(KEY_REALM_CACHE, new ValueProvider()
         {

            public Object getValue()
            {
               return new ConcurrentHashMap<String, AgeCache>();
            }

         });
         realmsPrefsCache = (ConcurrentHashMap<String, AgeCache>) globals.get(KEY_REALM_CACHE);
      }

      String realmCacheKey = partitionId + "." + realmId;
      AgeCache realmPrefsCache = realmsPrefsCache.get(realmCacheKey);
      if (null == realmPrefsCache)
      {
         realmsPrefsCache.put(realmCacheKey, new AgeCache(null,
               new ConcurrentHashMap<String, Map>()));
         realmPrefsCache = (AgeCache) realmsPrefsCache.get(realmCacheKey);
      }

      return realmPrefsCache;
   }

   protected Map<Pair, Map> getUserPreferencesCache(String partitionId, String realmId,
         String userId)
   {
      final GlobalParameters globals = GlobalParameters.globals();

      ConcurrentHashMap<String, Map> usersPrefsCache = (ConcurrentHashMap<String, Map>) globals.get(KEY_USER_CACHE);
      if (null == usersPrefsCache)
      {
         globals.initializeIfAbsent(KEY_USER_CACHE, new ValueProvider()
         {

            public Object getValue()
            {
               return new ConcurrentHashMap<String, Map>();
            }

         });
         usersPrefsCache = (ConcurrentHashMap<String, Map>) globals.get(KEY_USER_CACHE);
      }

      String userCacheKey = partitionId + "." + realmId + "." + userId;
      Map userPrefsCache = usersPrefsCache.get(userCacheKey);
      if (null == userPrefsCache)
      {
         usersPrefsCache.put(userCacheKey, new ConcurrentHashMap<String, Map>());
         userPrefsCache = (Map<String, Map>) usersPrefsCache.get(userCacheKey);
      }

      return userPrefsCache;
   }

   public List<Preferences> getAllPreferences(ParsedPreferenceQuery evaluatedQuery,
         IPreferencesReader xmlPreferenceReader)
   {
      if (PreferenceScope.DEFAULT.equals(evaluatedQuery.getScope()))
      {
         throw new PublicException("Querying is not supported for scope: "
               + evaluatedQuery.getScope());
      }

      List<Preferences> allPreferences = subPersistenceManager.getAllPreferences(
            evaluatedQuery, xmlPreferenceReader);

      for (Preferences preferences : allPreferences)
      {
         putToCache(null, preferences);
         // addCacheHint(preferences);
      }
      
      return filterEmptyPreferences(allPreferences);
   }

   private List<Preferences> filterEmptyPreferences(List<Preferences> allPreferences)
   {
      List<Preferences> nonEmptyPreferences = new ArrayList<Preferences>(allPreferences.size());
      
      for (Preferences preferences : allPreferences)
      {
         if ( !CollectionUtils.isEmpty(preferences.getPreferences()))
         {
            nonEmptyPreferences.add(preferences);
         }
      } 
      return nonEmptyPreferences;
   }

   @Override
   public Preferences loadPreferences(IUser user, PreferenceScope scope, String moduleId,
         String preferencesId, IPreferencesReader xmlPreferenceReader)
   {
      String realmId = null;
      String userId = null;
      if (PreferenceScope.USER.equals(scope) || PreferenceScope.REALM.equals(scope))
      {
         if (user == null)
         {
            throw new PublicException(
                  "No user specified. PreferenceScope USER and REALM not available.");
         }

         if (user != null)
         {
            realmId = user.getRealm().getId();
            userId = user.getId();
         }
      }
      String partitionId = SecurityProperties.getPartition().getId();

      Preferences preferences = null;
      Map<Pair, Map> cache = getCache(scope, moduleId, preferencesId, partitionId, realmId, userId);      
      if (cache != null)
      {
         Pair<String, String> cacheKey = new Pair(moduleId, preferencesId);
         //default scope allows no reading / writing from the database
         if(!cache.containsKey(cacheKey) && !PreferenceScope.DEFAULT.equals(scope))
         {
            preferences = subPersistenceManager.loadPreferences(user, scope, moduleId, preferencesId,
                  xmlPreferenceReader);
            putToCache(user, preferences);
         }
         else
         {
            preferences = getFromCache(scope, moduleId, preferencesId, partitionId, realmId, userId);
         }
      }
      
      return preferences;
   }
   
   public Preferences loadPreferences(PreferenceScope scope, String moduleId,
         String preferencesId, IPreferencesReader xmlPreferenceReader)
   {
      final IUser currentUser = SecurityProperties.getUser();
      return loadPreferences(currentUser, scope, moduleId, preferencesId, xmlPreferenceReader);
   }

   public void updatePreferences(Preferences preferences,
         IPreferencesWriter preferenceWriter)
   {
      //persist new values
      subPersistenceManager.updatePreferences(preferences, preferenceWriter);
      //update cache
      cleanCache(preferences);
   }

   
   private Map<Pair, Map> getCache(PreferenceScope scope, String moduleId,
         String preferencesId, String partitionId, String realmId, String userId)
   {
      Map<Pair, Map> cache = null;
      if (PreferenceScope.USER.equals(scope))
      {
         cache = getUserPreferencesCache(partitionId, realmId, userId);
      }
      else if (PreferenceScope.REALM.equals(scope))
      {
         AgeCache realmPreferencesCache = getRealmPreferencesCache(partitionId, realmId);
         cache = (Map) realmPreferencesCache.getMap();
      }
      else if (PreferenceScope.PARTITION.equals(scope))
      {
         AgeCache partitionPreferencesCache = getPartitionPreferencesCache(partitionId);
         cache = (Map) partitionPreferencesCache.getMap();
      }
      else if (PreferenceScope.DEFAULT.equals(scope))
      {
         cache = getDefaultPreferencesCache();
      }
      else
      {
         throw new PublicException("PreferenceScope not supported: " + scope);
      }
      
      return cache;
   }
   
   private Preferences getFromCache(PreferenceScope scope, String moduleId,
         String preferencesId, String partitionId, String realmId, String userId)
   {
      Preferences preferences = null;
      if (PreferenceScope.USER.equals(scope))
      {
         Map<Pair, Map> userPreferencesCache = getUserPreferencesCache(partitionId,
               realmId, userId);
         Map preferencesMap = userPreferencesCache.get(new Pair(moduleId, preferencesId));

         if (preferencesMap != null)
         {
            preferences = new Preferences(scope, moduleId, preferencesId, preferencesMap);
            preferences.setPartitionId(partitionId);
            preferences.setRealmId(realmId);
            preferences.setUserId(userId);

            preferences.setPreferenceCacheHint(new PreferenceCacheHint(
                  fetchRealmCacheHint(partitionId, realmId),
                  fetchPartitionCacheHint(partitionId)));
         }
      }
      else if (PreferenceScope.REALM.equals(scope))
      {
         AgeCache realmPreferencesCache = getRealmPreferencesCache(partitionId, realmId);
         Map preferencesMap = (Map) realmPreferencesCache.getMap().get(
               new Pair(moduleId, preferencesId));

         if (preferencesMap != null)
         {
            preferences = new Preferences(scope, moduleId, preferencesId, preferencesMap);
            preferences.setPartitionId(partitionId);
            preferences.setRealmId(realmId);

            preferences.setPreferenceCacheHint(new PreferenceCacheHint(
                  realmPreferencesCache.getLastModified(),
                  fetchPartitionCacheHint(partitionId)));
         }
      }
      else if (PreferenceScope.PARTITION.equals(scope))
      {
         AgeCache partitionPreferencesCache = getPartitionPreferencesCache(partitionId);
         Map preferencesMap = (Map) partitionPreferencesCache.getMap().get(
               new Pair(moduleId, preferencesId));

         if (preferencesMap != null)
         {
            preferences = new Preferences(scope, moduleId, preferencesId, preferencesMap);
            preferences.setPartitionId(partitionId);

            preferences.setPreferenceCacheHint(new PreferenceCacheHint(null,
                  partitionPreferencesCache.getLastModified()));
         }
      }
      else if (PreferenceScope.DEFAULT.equals(scope))
      {
         final Map<Pair, Map> defaultPreferencesCache = getDefaultPreferencesCache();
         if (defaultPreferencesCache != null)
         {
            final Map preferencesMap = defaultPreferencesCache.get(new Pair(moduleId,
                  preferencesId));
            if (preferencesMap != null)
            {
               preferences = new Preferences(scope, moduleId, preferencesId,
                     preferencesMap);
            }
         }
      }
      else
      {
         throw new PublicException("PreferenceScope not supported: " + scope);
      }
      return preferences;
   }

   private void putToCache(IUser user, Preferences preferences)
   {
      if(preferences != null)
      {
         Map<String, Serializable> preferenceValues = new HashMap<String, Serializable>();
         if(preferences.getPreferences() != null)
         {
            preferenceValues = preferences.getPreferences();
         }
         
         PreferenceScope scope = preferences.getScope();
         String moduleId = preferences.getModuleId();
         String preferencesId = preferences.getPreferencesId();
         String partitionId = SecurityProperties.getPartition().getId();
         
         final String realmId;
         final String userId;
         if(user != null)
         {
            userId = user.getId();
            realmId = user.getRealm().getId();
         }
         else
         {
            userId = preferences.getUserId();
            realmId = preferences.getRealmId();
         }
         
         if (PreferenceScope.USER.equals(scope))
         {
            Map<Pair, Map> userPreferencesCache = getUserPreferencesCache(partitionId,
                  realmId, userId);
            userPreferencesCache.put(new Pair(moduleId, preferencesId),
                  preferenceValues);
            
         }
         else if (PreferenceScope.REALM.equals(scope))
         {
            AgeCache realmPreferencesCache = getRealmPreferencesCache(partitionId, realmId);

            realmPreferencesCache.setLastModified(new Date(System.currentTimeMillis()));
            realmPreferencesCache.getMap().put(new Pair(moduleId, preferencesId),
                  preferenceValues);
            
         }
         else if (PreferenceScope.PARTITION.equals(scope))
         {
            AgeCache partitionPreferencesCache = getPartitionPreferencesCache(partitionId);

            partitionPreferencesCache.setLastModified(new Date(System.currentTimeMillis()));
            partitionPreferencesCache.getMap().put(new Pair(moduleId, preferencesId),
                  preferenceValues);
            
         }
         else if (PreferenceScope.DEFAULT.equals(scope))
         {
            throw new PublicException("PreferenceScope.DEFAULT is read only");
         }
         else
         {
            throw new PublicException("PreferenceScope not supported: " + scope);
         }
      }      
   }

   private Date fetchRealmCacheHint(String partitionId, String realmId)
   {
      AgeCache realmPreferencesCache = getRealmPreferencesCache(partitionId, realmId);
      if (realmPreferencesCache != null)
      {
         return realmPreferencesCache.getLastModified();
      }
      return null;
   }

   private Date fetchPartitionCacheHint(String partitionId)
   {
      AgeCache partitionPreferencesCache = getPartitionPreferencesCache(partitionId);

      if (partitionPreferencesCache != null)
      {
         return partitionPreferencesCache.getLastModified();
      }

      return null;
   }

   private void cleanCache(Preferences preferences)
   {          
      PreferenceScope scope = preferences.getScope();
      String moduleId = preferences.getModuleId();
      String preferencesId = preferences.getPreferencesId();
      
      cleanCache(scope, moduleId, preferencesId);
   }

   public void cleanCache(PreferenceScope scope, String moduleId, String preferencesId)
   {
      String partitionId = SecurityProperties.getPartition().getId();
      String userId = SecurityProperties.getUser().getId();
      String realmId = SecurityProperties.getUser().getRealm().getId();     

      Map<Pair, Map> cache 
         = getCache(scope, moduleId, preferencesId, partitionId, realmId, userId);
      Pair<String, String> cacheKey = new Pair(moduleId, preferencesId);
      if(cache != null)
      {
         cache.remove(cacheKey);
      }
   }

   public synchronized void flushCaches()
   {
      final GlobalParameters globals = GlobalParameters.globals();

      globals.set(KEY_DEFAULTS_CACHE, null);
      globals.set(KEY_PARTITION_CACHE, null);
      globals.set(KEY_REALM_CACHE, null);
      globals.set(KEY_USER_CACHE, null);
   }
}
