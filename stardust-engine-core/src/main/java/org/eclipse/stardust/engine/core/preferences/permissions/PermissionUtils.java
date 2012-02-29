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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

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
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;



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

   private final static Map<String, String> defaultPermissions;

   static
   {
      defaultPermissions = new HashMap<String, String>();

      defaultPermissions.put(ExecutionPermission.Id.controlProcessEngine.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.deployProcessModel.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.forceSuspend.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.manageDaemons.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.modifyAuditTrail.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.modifyDepartments.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.modifyUserData.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.readAuditTrailStatistics.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);

      defaultPermissions.put(ExecutionPermission.Id.createCase.name(),
            Authorization2.ALL);

      defaultPermissions.put(ExecutionPermission.Id.readDepartments.name(),
            Authorization2.ALL);
      defaultPermissions.put(ExecutionPermission.Id.readModelData.name(),
            Authorization2.ALL);
      defaultPermissions.put(ExecutionPermission.Id.readUserData.name(),
            Authorization2.ALL);
      defaultPermissions.put(ExecutionPermission.Id.resetUserPassword.name(),
            Authorization2.ALL);

      defaultPermissions.put(ExecutionPermission.Id.runRecovery.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.manageAuthorization.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);

      defaultPermissions.put(
            ExecutionPermission.Id.saveOwnPartitionScopePreferences.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.saveOwnRealmScopePreferences.name(),
            PredefinedConstants.ADMINISTRATOR_ROLE);
      defaultPermissions.put(ExecutionPermission.Id.saveOwnUserScopePreferences.name(),
            Authorization2.ALL);

      defaultPermissions.put(ExecutionPermission.Id.spawnSubProcessInstance.name(),
            Authorization2.ALL);
      defaultPermissions.put(ExecutionPermission.Id.spawnPeerProcessInstance.name(),
            Authorization2.ALL);
      defaultPermissions.put(ExecutionPermission.Id.joinProcessInstance.name(),
            Authorization2.ALL);
   }

   public static boolean isDefaultPermission(String permissionId, List<String> grants)
   {
      if ( !CollectionUtils.isEmpty(grants))
      {
         String permission = defaultPermissions.get(stripPrefix(permissionId));
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
      final Map<String, Serializable> permissions = getPreferences(preferenceStore);

      Map<String, List<String>> filteredPermissions = filterPermissions(permissions,
            includeDefaultPermissions);

      return filteredPermissions;
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
            String defaultPermission = getModelDefaultPermissions().get(
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

   public static void setGlobalPermissions(IPreferenceStorageManager preferenceStore,
         Map<String, List<String>> permissions) throws ValidationException
   {
      Map<String, Serializable> preferencesMap = getPreferences(preferenceStore);

      Map<String, List<String>> filteredPermissions = removeDefaultPermissions(permissions);

      mergePermissions(preferencesMap, filteredPermissions);

      savePreferences(preferenceStore, preferencesMap);
   }

   public static void setGlobalPermissionValues(
         IPreferenceStorageManager preferenceStore, String permissionId,
         List<String> values)
   {
      String internalPermissionId = stripPrefix(permissionId);
      Map<String, Serializable> preferencesMap = getPreferences(preferenceStore);

      String permission = getModelDefaultPermissions().get(internalPermissionId);
      if (permission == null || values == null || values.size() != 1
            || !values.get(0).equals(permission))
      {
         preferencesMap.put(internalPermissionId, (Serializable) values);
         savePreferences(preferenceStore, preferencesMap);
      }
   }

   private static String stripPrefix(String permissionId)
   {
      if ( !StringUtils.isEmpty(permissionId))
      {
         int idx = permissionId.lastIndexOf('.');
         if (idx > -1)
         {
            return permissionId.substring(idx + 1);
         }
      }
      return permissionId;
   }

   private static void addDefaultPermissions(Map<String, List<String>> permissions)
   {
      for (Entry<String, String> entry : getModelDefaultPermissions().entrySet())
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

   private static Map<String, List<String>> removeDefaultPermissions(
         Map<String, List<String>> permissions)
   {
      Map<String, List<String>> filteredPermissions = new HashMap<String, List<String>>(
            permissions);

      for (Entry<String, String> entry : getModelDefaultPermissions().entrySet())
      {
         List<String> values = filteredPermissions.get(entry.getKey());
         // only remove if equals exactly one default permission
         if (values != null && values.size() == 1
               && values.get(0).equals(entry.getValue()))
         {
            filteredPermissions.remove(entry.getKey());
         }
      }
      return filteredPermissions;
   }

   private static Map<String, String> getModelDefaultPermissions()
   {
      return defaultPermissions;
   }

   private static Map<String, Serializable> getPreferences(
         IPreferenceStorageManager preferenceStore)
   {
      String preferenceId = GLOBAL_SCOPE;

      Preferences preferences = preferenceStore.getPreferences(PreferenceScope.PARTITION,
            PERMISSIONS, preferenceId);

      return preferences != null ? preferences.getPreferences() : null;
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
         Map<String, List<String>> permissions) throws ValidationException
   {
      Map<String, Serializable> toAdd = new HashMap<String, Serializable>();
      for (java.util.Map.Entry<String, List<String>> entry : permissions.entrySet())
      {
         if (entry.getValue() != null && !entry.getValue().isEmpty())
         {
            List<String> srcList = (List<String>) preferencesMap.get(entry.getKey());
            List<String> targetList = entry.getValue();
            TreeSet<String> srcSet = null;
            if (srcList!= null){

               srcSet = new TreeSet<String>(srcList);
            }
            TreeSet<String> targetSet = new TreeSet<String>(targetList);

            // checkValidParticipants throws ValidationException if a grant is not
            // valid for the active model.
            if (srcSet != null && srcSet.equals(targetSet)
                  || checkValidParticipants(targetList))
            {
               toAdd.put(entry.getKey(), (Serializable) entry.getValue());
            }

         }
      }
      preferencesMap.clear();
      preferencesMap.putAll(toAdd);
   }

   private static boolean checkValidParticipants(List<String> grants)
         throws ValidationException
   {
      for (String qualifiedModelParticipantId : grants)
      {
         if ( !PredefinedConstants.ADMINISTRATOR_ROLE.equals(qualifiedModelParticipantId)
               && !Authorization2.ALL.equals(qualifiedModelParticipantId))
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
            IOrganization firstScopedOrganization = DepartmentUtils.getFirstScopedOrganization(participant);
            if (firstScopedOrganization != null)
            {
               throw new ValidationException(
                     "Setting permissions failed. Setting grants to scoped model participants is not allowed: "
                           + qualifiedModelParticipantId, false);
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
