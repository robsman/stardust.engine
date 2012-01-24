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

import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;


public class UserGroupInfoDetails extends DynamicParticipantInfoDetails
      implements UserGroupInfo
{
   private static final long serialVersionUID = 1L;

   public UserGroupInfoDetails(IUserGroup userGroup)
   {
      super(userGroup.getOID(), userGroup.getId(), userGroup.getName());
   }
   
   public UserGroupInfoDetails(long oid, String id, String name)
   {
      super(oid, id, name);
   }

   public UserGroupInfoDetails(UserGroupInfo user)
   {
      super(user.getOID(), user.getId(), user.getName());
   }
}
