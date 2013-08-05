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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.AccessControlEntry;
import org.eclipse.stardust.engine.api.runtime.Privilege;


import org.eclipse.stardust.vfs.IAccessControlEntry;
import org.eclipse.stardust.vfs.IPrivilege;

/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class IAccessControlEntryAdapter implements IAccessControlEntry, Serializable
{

   private static final long serialVersionUID = 1L;
   
   private final AccessControlEntry ace;

   public IAccessControlEntryAdapter(AccessControlEntry ace)
   {
      this.ace = ace;
   }

   public Principal getPrincipal()
   {
      return new PrincipalAdapter(ace.getPrincipal());
   }

   public Set<IPrivilege> getPrivileges()
   {
      Set<IPrivilege> result = CollectionUtils.newSet();
      for (Privilege privilege : this.ace.getPrivileges())
      {
         result.add(new IPrivilegeAdapter(privilege));
      }
      return result;
   }
   
   public EntryType getType()
   {
      return (ace.getType() == AccessControlEntry.EntryType.DENY)
            ? EntryType.DENY
            : EntryType.ALLOW;
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getPrincipal() == null) ? 0 : getPrincipal().hashCode());
      result = prime * result + ((getPrivileges() == null) ? 0 : getPrivileges().hashCode());
      result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if ( !(obj instanceof IAccessControlEntry))
         return false;
      IAccessControlEntry other = (IAccessControlEntry) obj;
      if (getPrincipal() == null)
      {
         if (other.getPrincipal() != null)
            return false;
      }
      else if ( !getPrincipal().equals(other.getPrincipal()))
         return false;
      if (getPrivileges() == null)
      {
         if (other.getPrivileges() != null)
            return false;
      }
      else if ( !getPrivileges().equals(other.getPrivileges()))
         return false;
      if ( !getType().equals(other.getType()))
         return false;  
      return true;
   }
}
