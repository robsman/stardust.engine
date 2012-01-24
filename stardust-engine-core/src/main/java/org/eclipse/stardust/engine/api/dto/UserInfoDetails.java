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

import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;


public class UserInfoDetails extends DynamicParticipantInfoDetails implements UserInfo
{
   private static final long serialVersionUID = 1L;

   public UserInfoDetails(long oid)
   {
      this(oid, null, null);
   }

   public UserInfoDetails(long oid, String id, String name)
   {
      super(oid, id, name);
   }
   
   public UserInfoDetails(UserInfo user)
   {
      this(user.getOID(), user.getId(), user.getName());
   }
   
   public UserInfoDetails(IUser user)
   {
      this(user.getOID(), user.getId(), user.getName());
   }
}
