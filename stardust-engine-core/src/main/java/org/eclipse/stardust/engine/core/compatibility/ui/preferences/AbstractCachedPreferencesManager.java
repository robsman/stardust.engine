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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.compatibility.ui.preferences.spi.IStaticConfigurationProvider;
import org.eclipse.stardust.engine.core.preferences.*;
import org.eclipse.stardust.engine.core.preferences.manager.*;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;



/**
 * @author sauer
 * @version $Revision: $
 */
public abstract class AbstractCachedPreferencesManager extends AbstractPreferencesManager
     implements IPreferenceCache
{

   public static final String KEY_DEFAULTS_CACHE = AbstractCachedPreferencesManager.class.getName()
         + ".DefaultsCache";

   protected abstract User getUser();

   protected abstract ServiceFactory getServiceFactory();

   protected Map getDefaultPreferencesCache()
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
                                       new StaticPreferencesStore(prefs));
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
   
   protected abstract AgeCache getPartitionPreferencesCache(String partitionId);

   protected abstract AgeCache getRealmPreferencesCache(String partitionId, String realmId);

   protected abstract AgeCache getUserPreferencesCache(User user);

   public IPreferenceStore getPreferences(PreferenceScope scope, String moduleId,
         String preferencesId)
   {
      return getPreferences(scope, moduleId, preferencesId, false);
   }

   public IPreferenceStore getPreferences(String moduleId, String preferencesId)
   {
      return getPreferences(PreferenceScope.USER, moduleId, preferencesId);
   }

   public IPreferenceEditor getPreferencesEditor(PreferenceScope scope, String moduleId,
         String preferencesId)
   {
      if ((PreferenceScope.DEFAULT == scope))
      {
         // unmanaged scopes are read only
         return null;
      }

      AbstractPreferenceStore prefsStore = getPreferences(scope, moduleId, preferencesId,
            true);

      return Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false)
            ? new ReadOnlyUiPreferenceEditor(moduleId, preferencesId, prefsStore)
            : new UiPreferenceEditor(moduleId, preferencesId, prefsStore,
                  getServiceFactory(), this);
   }

   private AbstractPreferenceStore getPreferences(PreferenceScope scope, String moduleId,
         String preferencesId, boolean editable)
   {
      moduleId = assertValidModuleId(moduleId);
      preferencesId = assertValidPreferencesId(preferencesId);

      AbstractPreferenceStore result = null;

      AgeCache prefsCache = null;
      Pair cacheKey = null;

      // if ( !Parameters.instance().getBoolean(PRP_USE_DOCUMENT_REPOSITORY, false))
      // {
      // // without repository, only read only DEFAULT scope is available
      // return editable //
      // ? null
      // : getPreferences(PreferenceScope.DEFAULT, moduleId, preferencesId,
      // false);
      // }

      // try to obtain preferences store from cache
      prefsCache = getPreferencesCache(scope);

      if (null != prefsCache)
      {
         cacheKey = new Pair(moduleId, preferencesId);
         result = (AbstractPreferenceStore) prefsCache.getMap().get(cacheKey);
         if (editable && result instanceof EmptyPreferenceStore)
         {
            result = null;
            prefsCache.getMap().remove(cacheKey);
         }
      }

      Date resultLastModified=null;
      if (null == result)
      {
         // not in cache, load from repository

         final Preferences preferences = getServiceFactory().getAdministrationService()
               .getPreferences(scope, moduleId, preferencesId);
         if (preferences != null)
         {
            Map preferencesMap = preferences.getPreferences();

            resultLastModified = getLastModified(preferences);
            processPreferencesCacheHint(preferences);

            result = new PreferenceStore(scope, preferencesMap);
         }
      }

      if (result != null)
      {
         // connect parent scopes
         final AbstractPreferenceStore parent;
         PreferenceScope parentScope = getParentScope(scope);
         if ((null == result.getParent()) && (null != parentScope))
         {
            parent = getPreferences(parentScope, moduleId, preferencesId, false);

            if (result.isEmpty() && !editable)
            {
               if (parent instanceof EmptyPreferenceStore)
               {
                  result = getEmptyPrefs(scope);
               }
               else
               {
                  result.setParent(parent);
               }
            }
            else
            {
               result.setParent(parent);
            }
         }
      }

      // Fallback to portal default preferences spi
      if (PreferenceScope.DEFAULT == scope
            && (result == null || CollectionUtils.isEmpty(result.getPreferences())))
      {
         // provide static preferences, if available
         result = (AbstractPreferenceStore) getDefaultPreferencesCache().get(
               new Pair(moduleId, preferencesId));

         if (null == result)
         {
            result = EmptyPreferenceStore.EMPTY_DEFAULT_PREFS;
         }
      }

      if ((null != prefsCache) && (null != cacheKey) && !prefsCache.getMap().containsKey(cacheKey))
      {
         // TODO put into cache, but make sure cache is cleared on updates
         prefsCache.getMap().put(cacheKey, result);
         prefsCache.setLastModified(resultLastModified);
      }

      return result;
   }

   private Date getLastModified(Preferences preferences)
   {
      Date lastModified = null;
      if (preferences != null)
      {
         final PreferenceCacheHint preferenceCacheHint = preferences.getPreferenceCacheHint();
         if (preferenceCacheHint != null)
         {
            if (PreferenceScope.REALM.equals(preferences.getScope()))
            {
               lastModified = preferenceCacheHint.getRealmCacheLastModified();
            }
            else if (PreferenceScope.PARTITION.equals(preferences.getScope()))
            {
               lastModified = preferenceCacheHint.getPartitionCacheLastModified();
            }
         }
      }
      return lastModified;
   }

   private void processPreferencesCacheHint(Preferences preferences)
   {
      if (preferences != null && preferences.getPreferenceCacheHint() != null)
      {
         if (PreferenceScope.USER.equals(preferences.getScope()))
         {
            final Date partitionCacheLastModified = preferences.getPreferenceCacheHint()
                  .getPartitionCacheLastModified();
            final AgeCache partitionPreferencesCache = getPartitionPreferencesCache(preferences.getPartitionId());
            if (partitionPreferencesCache.getLastModified() != null
                  && partitionCacheLastModified != null
                  && partitionPreferencesCache.getLastModified().before(
                        partitionCacheLastModified))
            {
               partitionPreferencesCache.setLastModified(null);
               partitionPreferencesCache.getMap().clear();
            }
            
            
            final Date realmCacheLastModified = preferences.getPreferenceCacheHint()
                  .getRealmCacheLastModified();
            final AgeCache realmPreferencesCache = getRealmPreferencesCache(
                  preferences.getPartitionId(), preferences.getPartitionId());
            if (realmPreferencesCache.getLastModified() != null
                  && realmCacheLastModified != null
                  && realmPreferencesCache.getLastModified().before(
                        realmCacheLastModified))
            {
              realmPreferencesCache.setLastModified(null);
              realmPreferencesCache.getMap().clear();
            }
         }
         else if (PreferenceScope.REALM.equals(preferences.getScope()))
         {
            final Date partitionCacheLastModified = preferences.getPreferenceCacheHint()
                  .getPartitionCacheLastModified();
            final AgeCache partitionPreferencesCache = getPartitionPreferencesCache(preferences.getPartitionId());
            if (partitionPreferencesCache.getLastModified() != null
                  && partitionCacheLastModified != null
                  && partitionPreferencesCache.getLastModified().before(
                        partitionCacheLastModified))
            {
               partitionPreferencesCache.setLastModified(null);
               partitionPreferencesCache.getMap().clear();
            }
         }
      }
   }

   private AgeCache getPreferencesCache(PreferenceScope scope)
   {
      final User currentUser = getUser();
      if (PreferenceScope.USER == scope)
      {
         return getUserPreferencesCache(currentUser);
      }
      else if (PreferenceScope.REALM == scope)
      {
         return getRealmPreferencesCache(currentUser.getPartitionId(),
               currentUser.getRealmId());
      }
      else if (PreferenceScope.PARTITION == scope)
      {
         return getPartitionPreferencesCache(currentUser.getPartitionId());
      }
      else if (PreferenceScope.DEFAULT == scope)
      {
         // return getDefaultPreferencesCache();
         return null;
      }
      else
      {
         throw new IllegalArgumentException("Unsupported scope: " + scope);
      }
   }

   private String assertValidModuleId(String moduleId)
   {
      if (StringUtils.isEmpty(moduleId))
      {
         // TODO error code
         throw new IllegalArgumentException("Module-ID must not be empty.");
      }

      // TODO verify characters (i.e. no .. or .)
      return moduleId;
   }

   private String assertValidPreferencesId(String preferencesId)
   {
      // TODO verify characters (i.e. no .. or .)
      return StringUtils.isEmpty(preferencesId) ? "default" : preferencesId;
   }

   public void cleanCache(PreferenceScope updatedScope, String moduleId,
         String preferencesId)
   {
      Set parentScopes = getAllParentScopes(updatedScope);
      PreferenceScope currentScope = PreferenceScope.USER;
      while ( !currentScope.equals(updatedScope))
      {
         if ( !parentScopes.contains(currentScope))
         {
            AgeCache cache = getPreferencesCache(currentScope);
            Pair cacheKey = new Pair(moduleId, preferencesId);
            IPreferenceStore prefStore = (IPreferenceStore) (cache != null
                  ? cache.getMap().get(cacheKey)
                  : null);
            if (prefStore != null)
            {
               cache.setLastModified(null);
               cache.getMap().remove(cacheKey);
            }
         }
         currentScope = getParentScope(currentScope);
      }
   }

   public void flushCaches()
   {
      Set<PreferenceScope> scopes = getAllParentScopes(PreferenceScope.USER);
      for (PreferenceScope scope : scopes)
      {
         AgeCache preferencesCache = getPreferencesCache(scope);
         preferencesCache.setLastModified(null);
         preferencesCache.getMap().clear();
      }
   }
   
}
