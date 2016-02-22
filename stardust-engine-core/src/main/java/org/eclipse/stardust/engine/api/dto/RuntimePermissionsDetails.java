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
package org.eclipse.stardust.engine.api.dto;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.RuntimePermissions;
import org.eclipse.stardust.engine.core.preferences.permissions.PermissionUtils;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.ParticipantInfoUtil;

public class RuntimePermissionsDetails implements RuntimePermissions
{
   private static final long serialVersionUID = 2L;

   private Map<String, List<String>> permissions;
   private Map<String, List<String>> deniedPermissions;

   RuntimePermissionsDetails(Map<String, List<String>> cummulatedPermissions)
   {
      permissions = CollectionUtils.newMap();
      deniedPermissions = CollectionUtils.newMap();
      for (Map.Entry<String, List<String>> entry : cummulatedPermissions.entrySet())
      {
         String key = entry.getKey();
         if (key.startsWith("deny:"))
         {
            deniedPermissions.put(key.substring(5), entry.getValue());
         }
         else
         {
            permissions.put(key, entry.getValue());
         }
      }
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.engine.api.runtime.RuntimePermissions#getAllPermissionIds()
    */
   public Set<String> getAllPermissionIds()
   {
      Set<String> ids = CollectionUtils.newSet(permissions.keySet());
      ids.addAll(deniedPermissions.keySet());
      return Collections.unmodifiableSet(ids);
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.engine.api.dto.Permissions#getGrants(java.lang.String)
    */
   public Set<ModelParticipantInfo> getGrants(String permissionId)
   {
      return externalize(permissions.get(stripPrefix(permissionId)));
   }

   @Override
   public Set<ModelParticipantInfo> getDeniedGrants(String permissionId)
   {
      return externalize(deniedPermissions.get(stripPrefix(permissionId)));
   }

   private Set<ModelParticipantInfo> externalize(List<String> grants)
   {
      HashSet<ModelParticipantInfo> externalGrants = new HashSet<ModelParticipantInfo>();
      if (grants != null && !grants.contains(Authorization2.ALL))
      {
         for (String grant : grants)
         {
            QName qualifier = QName.valueOf(grant);
            externalGrants.add(ParticipantInfoUtil.newModelParticipantInfo(
                  qualifier.getNamespaceURI(), qualifier.getLocalPart()));
         }
      }
      return externalGrants;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.engine.api.runtime.RuntimePermissions#setGrants(java.lang.String,
    * java.util.Set)
    */
   public void setGrants(String permissionId, Set<ModelParticipantInfo> grants)
   {
      permissions.put(stripPrefix(permissionId), internalize(grants));
   }


   @Override
   public void setDeniedGrants(String permissionId, Set<ModelParticipantInfo> grants)
   {
      deniedPermissions.put(stripPrefix(permissionId), internalize(grants));
   }

   private List<String> internalize(Set<ModelParticipantInfo> grants)
   {
      if (grants != null && grants.size() > 0)
      {
         List<String> grantIds = new LinkedList<String>();

         for (ModelParticipantInfo modelParticipantInfo : grants)
         {
            if (modelParticipantInfo.getDepartment() != null)
            {
               throw new IllegalArgumentException(Department.class.getName());
            }
            if (modelParticipantInfo instanceof QualifiedModelParticipantInfo)
            {
               grantIds.add(((QualifiedModelParticipantInfo) modelParticipantInfo).getQualifiedId());
            }
            else
            {
               grantIds.add(modelParticipantInfo.getId());
            }
         }
         return grantIds;
      }
      return null;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.engine.api.dto.Permissions#setAllGrant(java.lang.String)
    */
   public void setAllGrant(String permissionId)
   {
      permissions.put(stripPrefix(permissionId),
            Collections.singletonList(Authorization2.ALL));
      deniedPermissions.put(stripPrefix(permissionId),
            Collections.<String>emptyList());
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.engine.api.dto.Permissions#hasAllGrant(java.lang.String)
    */
   public boolean hasAllGrant(String permissionId)
   {
      List<String> grants = permissions.get(stripPrefix(permissionId));

      if (grants != null)
      {
         if (grants.contains(Authorization2.ALL))
         {
            return true;
         }
      }
      return false;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.engine.api.runtime.RuntimePermissions#isDefaultGrant(java.lang.String)
    */
   public boolean isDefaultGrant(String permissionId)
   {
      String internalPermissionId = stripPrefix(permissionId);
      return PermissionUtils.isDefaultPermission(internalPermissionId,
            permissions.get(internalPermissionId));
   }

   public Map<String, List<String>> getPermissionMap()
   {
      return Collections.unmodifiableMap(permissions);
   }

   public Map<String, List<String>> getDeniedPermissionsMap()
   {
      return Collections.unmodifiableMap(deniedPermissions);
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
}
