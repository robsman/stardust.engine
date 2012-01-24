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

import org.eclipse.stardust.engine.api.runtime.Privilege;

import com.sungard.infinity.bpm.vfs.IPrivilege;


/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class IPrivilegeAdapter implements IPrivilege
{
   private final String iPrivilegeName; 
   
   public IPrivilegeAdapter(Privilege privilege)
   {
      if (privilege == null)
      {
         throw new NullPointerException("Privilege must not be null.");
      }
      
      iPrivilegeName = mapPrivilege2IPrivilege(privilege);
   }

   public String getName()
   {
      return iPrivilegeName;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if ( !(obj instanceof IPrivilege))
         return false;
      IPrivilege other = (IPrivilege) obj;
      if (getName() == null)
      {
         if (other.getName() != null)
            return false;
      }
      else if ( !getName().equals(other.getName()))
         return false;
      return true;
   }
   
   private String mapPrivilege2IPrivilege(final Privilege privilege)
   {
      if (DmsPrivilege.ALL_PRIVILEGES.equals(privilege))
      {
         return IPrivilege.ALL_PRIVILEGE;
      }
      else if (DmsPrivilege.CREATE_PRIVILEGE.equals(privilege))
      {
         return IPrivilege.CREATE_PRIVILEGE;
      }
      else if (DmsPrivilege.DELETE_CHILDREN_PRIVILEGE.equals(privilege))
      {
         return IPrivilege.DELETE_CHILDREN_PRIVILEGE;
      }
      else if (DmsPrivilege.DELETE_PRIVILEGE.equals(privilege))
      {
         return IPrivilege.DELETE_PRIVILEGE;
      }
      else if (DmsPrivilege.MODIFY_ACL_PRIVILEGE.equals(privilege))
      {
         return IPrivilege.MODIFY_ACL_PRIVILEGE;
      }
      else if (DmsPrivilege.MODIFY_PRIVILEGE.equals(privilege))
      {
         return IPrivilege.MODIFY_PRIVILEGE;
      }
      else if (DmsPrivilege.READ_ACL_PRIVILEGE.equals(privilege))
      {
         return IPrivilege.READ_ACL_PRIVILEGE;
      }
      else if (DmsPrivilege.READ_PRIVILEGE.equals(privilege))
      {
         return IPrivilege.READ_PRIVILEGE;
      }
      else
      {
         throw new IllegalArgumentException("Unknown DMS Privilege: " + privilege);
      }
   }
}
