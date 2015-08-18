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
   private static final long serialVersionUID = -6314888067527212016L;

   private Map<String, List<String>> permissions;

   RuntimePermissionsDetails(Map<String, List<String>> permissions)
   {
      this.permissions = permissions;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.engine.api.runtime.RuntimePermissions#getAllPermissionIds()
    */
   public Set<String> getAllPermissionIds()
   {
      return Collections.unmodifiableSet(permissions.keySet());
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.engine.api.dto.Permissions#getGrants(java.lang.String)
    */
   public Set<ModelParticipantInfo> getGrants(String permissionId)
   {
      List<String> grants = permissions.get(stripPrefix(permissionId));

      return externalize(grants);
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
}
