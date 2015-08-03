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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


public abstract class AbstractPropertyWithUser extends AbstractProperty
{
   public static final String FIELD__WORKFLOWUSER = "workflowUser";

   public static final FieldRef FR__WORKFLOWUSER = new FieldRef(
         ProcessInstanceProperty.class, FIELD__WORKFLOWUSER);

   private long workflowUser;

   protected AbstractPropertyWithUser()
   {
      super();
      this.workflowUser = SecurityProperties.getUserOID();
   }

   protected AbstractPropertyWithUser(long objectOID, String name, Object value)
   {
      super(objectOID, name, value);
      this.workflowUser = SecurityProperties.getUserOID();
   }

   /**
    * @return the user who last modified the property, or null if the user is unknown.
    */
   public IUser getUser()
   {
      fetch();
      if (Unknown.LONG == workflowUser)
      {
         return null;
      }
      else if (0 == workflowUser)
      {
         return null;
      }      
      return UserBean.findByOid(workflowUser);
   }

   protected void callOnChange()
   {
      super.callOnChange();
      setUser(SecurityProperties.getUserOID());
   }

   private void setUser(long userOid)
   {
      fetch();
      if (this.workflowUser != userOid)
      {
         markModified(FIELD__WORKFLOWUSER);
         this.workflowUser = userOid;
      }
   }

   public <T extends AbstractPropertyWithUser> T clone(long objectId, T property)
   {
     property.workflowUser = this.workflowUser;
     return super.clone(objectId, property);
   }
}
