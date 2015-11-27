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
package org.eclipse.stardust.engine.core.preferences.permissions;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.ClientPermission;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;

public class PermissionUtils
{
   /**
    * The moduleId of preferences which are used to store permissions.
    */
   public static final String PERMISSIONS = "permissions";

   /**
    * The preferencesId of preferences which are scoped as global permissions.
    */
   public static final String GLOBAL_SCOPE = "global";

   /**
    * The static defaults for the permissions.
    * Please Note: Changing a default here does not automatically update saved preferences.
    */
   private final static Map<String, String> defaultGlobalPermissions = ClientPermission.getGlobalDefaults();

   public static boolean isDefaultPermission(String permissionId, List<String> grants)
   {
      if (!CollectionUtils.isEmpty(grants))
      {
         String permission = defaultGlobalPermissions.get(stripPrefix(permissionId));
         if (permission != null && grants.size() == 1 && grants.get(0).equals(permission))
         {
            return true;
         }
      }
      return false;
   }

   public static Map<String, List<String>> getGlobalPermissions(
         IPreferenceStorageManager preferenceStore, boolean includeDefaultPermissions)
   {
      Map<String, Serializable> permissions = getPreferences(preferenceStore);
      return filterPermissions(permissions, includeDefaultPermissions);
   }

   public static List<String> getGlobalPermissionValues(
         IPreferenceStorageManager preferenceStore, String permissionId,
         boolean includeDefaultPermissions)
   {
      String internalPermissionId = stripPrefix(permissionId);
      final Map<String, Serializable> permissions = getPreferences(preferenceStore);

      List<String> values = (List<String>) permissions.get(internalPermissionId);

      if (includeDefaultPermissions)
      {
         if (values == null || values.isEmpty())
         {
            String defaultPermission = defaultGlobalPermissions.get(
                  internalPermissionId);
            if ( !StringUtils.isEmpty(defaultPermission))
            {
               values = new LinkedList<String>();
               values.add(defaultPermission);
            }
         }
      }
      if (values == null)
      {
         values = Collections.EMPTY_LIST;
      }

      return values;
   }

   public static List<String> getScopedGlobalPermissionValues(
         IPreferenceStorageManager preferenceStore, String internalPermissionId,
         boolean includeDefaultPermissions)
   {
      final Map<String, Serializable> permissions = getPreferences(preferenceStore);
      List<String> values = (List<String>) permissions.get(internalPermissionId);

      if (values == null || values.isEmpty())
      {
         String defaultPermission = defaultGlobalPermissions.get(internalPermissionId);
         if (!StringUtils.isEmpty(defaultPermission))
         {
            values = new LinkedList<String>();
            values.add(defaultPermission);
         }
      }
      if (values == null)
      {
         values = Collections.EMPTY_LIST;
      }

      return values;
   }

   public static void setGlobalPermissions(IPreferenceStorageManager preferenceStore,
         Map<String, List<String>> permissions, Map<String, List<String>> deniedPermissionsMap) throws ValidationException
   {
      Map<String, Serializable> preferencesMap = getPreferences(preferenceStore);

      mergePermissions(preferencesMap, permissions, deniedPermissionsMap);

      savePreferences(preferenceStore, preferencesMap);
   }

   public static void setGlobalPermissionValues(
         IPreferenceStorageManager preferenceStore, String permissionId,
         List<String> values)
   {
      String internalPermissionId = stripPrefix(permissionId);
      Map<String, Serializable> preferencesMap = getPreferences(preferenceStore);

      preferencesMap.put(internalPermissionId, (Serializable) values);
      savePreferences(preferenceStore, preferencesMap);
   }

   private static String stripPrefix(String permissionId)
   {
      if (!StringUtils.isEmpty(permissionId))
      {
         int idx = permissionId.lastIndexOf(':');
         if (idx > -1)
         {
            permissionId = permissionId.substring(idx + 1);
         }
         idx = permissionId.lastIndexOf('.');
         if (idx > -1 && "model".equals(permissionId.substring(0, idx)))
         {
            permissionId = permissionId.substring(idx + 1);
         }
      }
      return permissionId;
   }

   private static void addDefaultPermissions(Map<String, List<String>> permissions)
   {
      for (Entry<String, String> entry : defaultGlobalPermissions.entrySet())
      {
         List value = permissions.get(entry.getKey());
         if (value == null)
         {
            List values = new LinkedList<String>();
            values.add(entry.getValue());
            permissions.put(entry.getKey(), values);
         }
      }
   }

   private static Map<String, Serializable> getPreferences(
         IPreferenceStorageManager preferenceStore)
   {
      Preferences defaultPreferences = preferenceStore.getPreferences(PreferenceScope.DEFAULT,
            PERMISSIONS, GLOBAL_SCOPE);

      Preferences partitionPreferences = preferenceStore.getPreferences(PreferenceScope.PARTITION,
            PERMISSIONS, GLOBAL_SCOPE);

      return mergePreferencesMap(defaultPreferences, partitionPreferences);
   }

   private static Map<String, Serializable> mergePreferencesMap(
         Preferences defaultPreferences, Preferences partitionPreferences)
   {
      Map<String, Serializable> mergedPreferencesMap = CollectionUtils.newHashMap();

      if (defaultPreferences != null)
      {
         mergedPreferencesMap.putAll(defaultPreferences.getPreferences());
      }

      if (partitionPreferences != null)
      {
         mergedPreferencesMap.putAll(partitionPreferences.getPreferences());
      }

      return mergedPreferencesMap;
   }

   private static void savePreferences(IPreferenceStorageManager preferenceStore,
         Map<String, Serializable> preferencesMap)
   {
      String preferenceId = GLOBAL_SCOPE;

      Preferences preferences = new Preferences(PreferenceScope.PARTITION, PERMISSIONS,
            preferenceId, preferencesMap);

      preferenceStore.savePreferences(preferences, true);
   }

   private static void mergePermissions(Map<String, Serializable> preferencesMap,
         Map<String, List<String>> permissions, Map<String, List<String>> deniedPermissionsMap) throws ValidationException
   {
      Map<String, Serializable> toAdd = new HashMap<String, Serializable>();
      for (Map.Entry<String, List<String>> entry : permissions.entrySet())
      {
         if (entry.getValue() != null && !entry.getValue().isEmpty())
         {
            String key = entry.getKey();
            List<String> srcList = (List<String>) preferencesMap.get(key);
            List<String> targetList = entry.getValue();
            TreeSet<String> srcSet = null;
            if (srcList != null)
            {
               srcSet = new TreeSet<String>(srcList);
            }
            TreeSet<String> targetSet = new TreeSet<String>(targetList);

            // checkValidParticipants throws ValidationException if a grant is not
            // valid for the active model.
            if (srcSet != null && srcSet.equals(targetSet)
                  || checkValidParticipants(targetList, isGlobalPermission(key)))
            {
               toAdd.put(entry.getKey(), (Serializable) entry.getValue());
            }
         }
      }
      for (Map.Entry<String, List<String>> entry : deniedPermissionsMap.entrySet())
      {
         List<String> value = entry.getValue();
         if (value != null && !value.isEmpty())
         {
            String key = entry.getKey();
            List<String> srcList = (List<String>) preferencesMap.get(key);
            List<String> targetList = value;
            TreeSet<String> srcSet = null;
            if (srcList != null)
            {
               srcSet = new TreeSet<String>(srcList);
            }
            TreeSet<String> targetSet = new TreeSet<String>(targetList);

            // checkValidParticipants throws ValidationException if a grant is not
            // valid for the active model.
            if (srcSet != null && srcSet.equals(targetSet)
                  || checkValidParticipants(targetList, isGlobalPermission(key)))
            {
               toAdd.put("deny:" + key, (Serializable) value);
            }
         }
      }
      preferencesMap.clear();
      preferencesMap.putAll(toAdd);
   }

   private static boolean isGlobalPermission(String key)
   {
      int ix = key.lastIndexOf('.');
      if (ix >= 0)
      {
         key = key.substring(0, ix);
         ix = key.lastIndexOf(':');
         if (ix >= 0)
         {
            key = key.substring(ix + 1);
         }
         return "model".equals(key);
      }
      return true;
   }

   private static boolean checkValidParticipants(List<String> grants, boolean isGlobal)
         throws ValidationException
   {
      for (String qualifiedModelParticipantId : grants)
      {
         if (!PredefinedConstants.ADMINISTRATOR_ROLE.equals(qualifiedModelParticipantId)
               && !Authorization2.ALL.equals(qualifiedModelParticipantId)
               && !Authorization2.OWNER.equals(qualifiedModelParticipantId)
               && !PredefinedConstants.AUDITOR_ROLE.equals(qualifiedModelParticipantId))
         {
            QName qualifier = QName.valueOf(qualifiedModelParticipantId);

            IModel model = null;
            if ( !StringUtils.isEmpty(qualifier.getNamespaceURI()))
            {
               model = ModelManagerFactory.getCurrent().findActiveModel(
                     qualifier.getNamespaceURI());
            }
            else
            {
               // if only one active model is deployed, default to it.
               List<IModel> allActiveModels = ModelManagerFactory.getCurrent().findActiveModels();
               if (allActiveModels != null && allActiveModels.size() == 1)
               {
                  model = allActiveModels.get(0);
               }
            }

            if (model == null)
            {
               throw new ValidationException(
                     "Setting permissions failed. No active model found for participant: "
                           + qualifiedModelParticipantId, false);
            }
            IModelParticipant participant = model.findParticipant(qualifier.getLocalPart());
            if (participant == null)
            {
               throw new ValidationException(
                     "Setting permissions failed. Participant does not exist in active model: "
                           + qualifiedModelParticipantId, false);
            }
            if (isGlobal)
            {
               IOrganization firstScopedOrganization = DepartmentUtils.getFirstScopedOrganization(participant);
               if (firstScopedOrganization != null)
               {
                  throw new ValidationException(
                        "Setting permissions failed. Setting grants to scoped model participants is not allowed: "
                              + qualifiedModelParticipantId, false);
               }
            }
         }
      }

      return true;
   }

   private static Map<String, List<String>> filterPermissions(
         Map<String, Serializable> preferencesMap, boolean includeDefaultPermissions)
   {
      Map<String, List<String>> permissions = CollectionUtils.newHashMap();

      for (java.util.Map.Entry<String, Serializable> entry : preferencesMap.entrySet())
      {
         if (entry.getValue() != null && entry.getValue() instanceof List)
         {
            permissions.put(entry.getKey(), (List<String>) entry.getValue());
         }
      }

      if (includeDefaultPermissions)
      {
         addDefaultPermissions(permissions);
      }
      return permissions;
   }
}
