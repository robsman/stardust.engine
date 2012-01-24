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

import org.eclipse.stardust.engine.api.runtime.AccessControlEntry;
import org.eclipse.stardust.engine.api.runtime.Privilege;



/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class DmsAccessControlEntry implements AccessControlEntry, Serializable 
{

   private static final long serialVersionUID = 1L;
   
   private final Principal principal;
   private final Set<Privilege> privileges;
   
   public DmsAccessControlEntry(Principal principal, Set<Privilege> privileges)
   {
      super();
      this.principal = principal;
      this.privileges = privileges;
   }

   public Principal getPrincipal()
   {
      return principal;
   }

   public Set<Privilege> getPrivileges()
   {
      return privileges;
   }
 
   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(this.principal);
      sb.append(": ");
      sb.append(this.privileges);
      
      return sb.toString();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((principal == null) ? 0 : principal.hashCode());
      result = prime * result + ((privileges == null) ? 0 : privileges.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      DmsAccessControlEntry other = (DmsAccessControlEntry) obj;
      if (principal == null)
      {
         if (other.principal != null)
            return false;
      }
      else if ( !principal.equals(other.principal))
         return false;
      if (privileges == null)
      {
         if (other.privileges != null)
            return false;
      }
      else if ( !privileges.equals(other.privileges))
         return false;
      return true;
   }
}
