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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.core.preferences.permissions.GlobalPermissionConstants;



/**
 * RuntimePermissions present permissions that are changeable at runtime. While other
 * permissions are bound to model elements in the process model RuntimePermissions can be
 * set via the public API.
 *
 * @author roland.stamm
 *
 */
public interface RuntimePermissions extends Serializable
{

   /**
    * Retrieves a set of all permissionIds which can be used to set and retrieve grants
    * for.
    *
    * @return all permissionIds
    * @see GlobalPermissionConstants
    */
   public Set<String> getAllPermissionIds();

   /**
    * Retrieves the currently set grants for the Permission.
    * If the all-grant is set this list is empty.
    *
    * @param permissionId the id of the permission from <code>GlobalPermissionConstants</code>
    * @return the currently set grants.
    * @see GlobalPermissionConstants
    * @see RuntimePermissions#hasAllGrant(String)
    */
   public Set<ModelParticipantInfo> getGrants(String permissionId);

   /**
    * Allows setting a set of <code>ModelParticipantInfo</code>. This can be used to
    * grant the specified Permission for certain Roles or Organizations.
    * The <code>ModelParticipantInfo</code> must not be scoped with a department.
    *
    * @param permissionId the id of the permission from <code>GlobalPermissionConstants</code>
    * @param grants a set of grants which will replace the existing ones.
    * @see GlobalPermissionConstants
    * @throws IllegalArgumentException If the <code>ModelParticipantInfo</code> is department scoped.
    */
   public void setGrants(String permissionId, Set<ModelParticipantInfo> grants);

   /**
    * Sets the all-grant to the specified Permission.
    * By doing this all other grants will be removed.
    *
    * @param permissionId the id of the permission from <code>GlobalPermissionConstants</code>
    * @see GlobalPermissionConstants
    */
   public void setAllGrant(String permissionId);

   /**
    * Allows to check if the all-grant is set for the
    * specified permissionId.
    *
    * @param permissionId the id of the permission from <code>GlobalPermissionConstants</code>
    * @return <code>true</code> if the all-grant is set for the specified permissionId.
    * @see GlobalPermissionConstants
    */
   public boolean hasAllGrant(String permissionId);

   /**
    * Allows to check if the currently set grants are the default grants for the specified.
    * permissionId
    *
    * @param permissionId the id of the permission from <code>GlobalPermissionConstants</code>
    * @return <code>true</code> if the specified permissionId currently has its default grant assigned.
    * @see GlobalPermissionConstants
    */
   public boolean isDefaultGrant(String permissionId);

}