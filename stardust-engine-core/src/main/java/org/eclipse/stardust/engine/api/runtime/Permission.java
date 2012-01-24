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
import java.util.List;

public class Permission implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String permissionId;
   private List<Scope> scopes;
   
   public Permission(String permissionId, List<Scope> scopes)
   {
      this.permissionId = permissionId;
      this.scopes = scopes;
   }
   
   /**
    * Gets the id of this permission.
    * 
    * @return The permission id.
    */
   public String getPermissionId()
   {
      return permissionId;
   }

   /**
    * Get all scopes for this permission.
    * 
    * @return A list of all scopes for this permission.
    */
   public List<Scope> getScopes()
   {
      return scopes;
   }
   
   /**
    * Returns a string representation of the object.
    *
    * @return A string representation of the object.
    */
   public String toString()
   {
      return permissionId + ": " + scopes;
   }
}