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

   private final String firstName;

   private final String lastName;

   public UserInfoDetails(long oid)
   {
      this(oid, null, null);
   }

   public UserInfoDetails(long oid, String id, String name)
   {
      this(oid, id, name, null, null);
   }

   public UserInfoDetails(long oid, String id, String name, String firstName, String lastName)
   {
      super(oid, id, name);

      this.firstName = firstName;
      this.lastName = lastName;
   }
   
   public UserInfoDetails(UserInfo user)
   {
      this(user.getOID(), user.getId(), user.getName(), user.getFirstName(), user.getLastName());
   }
   
   public UserInfoDetails(IUser user)
   {
      this(user.getOID(), user.getId(), user.getName(), user.getFirstName(), user.getLastName());
   }

   @Override
   public String getAccount()
   {
      return getId();
   }

   @Override
   public String getFirstName()
   {
      return firstName;
   }

   @Override
   public String getLastName()
   {
      return lastName;
   }
}
