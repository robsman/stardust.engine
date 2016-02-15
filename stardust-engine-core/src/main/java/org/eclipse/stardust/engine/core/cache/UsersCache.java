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
package org.eclipse.stardust.engine.core.cache;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;


/**
 * @author Florin.Herinean
 */
public class UsersCache extends AbstractCache <UserBean>
{
   private static UsersCache INSTANCE;

   public static UsersCache instance()
   {
      if (INSTANCE == null)
      {
         INSTANCE = new UsersCache();
      }
      return INSTANCE;
   }

   private UsersCache()
   {
      super("users");
   }

   @Override
   UserOidKey getKeyForValue(UserBean user)
   {
      return new UserOidKey(user.getOID());
   }

   @Override
   UserBean retrieve(byte[] bytes) throws IOException
   {
      UserBean user = new UserBean();
      user.retrieve(bytes);
      return user;
   }

   @Override
   UserOidKey getKey(long oid)
   {
      return new UserOidKey(oid);
   }

   @Override
   List<? extends CacheKey> getSecondaryKeys(UserBean user)
   {
      IUserRealm realm = user.getRealm();
      return Collections.<UserIdKey>singletonList(new UserIdKey(
            user.getId(), realm == null ? 0 : realm.getOID()));
   }

   public UserBean findById(String id, long realmOid)
   {
      UserIdKey idKey = new UserIdKey(id, realmOid);
      UserOidKey oidKey = (UserOidKey) getPrimaryKey(idKey);
      if (oidKey == null)
      {
         return null;
      }
      UserBean user = UserBean.findByOid(oidKey.getOid());
      if (user == null)
      {
         // remove phantom key
         removeKey(idKey);
      }
      return user;
   }

   public static final class UserIdKey implements CacheKey
   {
      private static final long serialVersionUID = 1L;

      private String id;
      private long userRealmOid;

      public UserIdKey(String id, long userRealmOid)
      {
         this.id = id;
         this.userRealmOid = userRealmOid;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         result = prime * result + (int) (userRealmOid ^ (userRealmOid >>> 32));
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof UserIdKey)
         {
            UserIdKey other = (UserIdKey) obj;
            return userRealmOid == other.userRealmOid &&
                  (id == other.id || id != null && id.equals(other.id));
         }
         return false;
      }

      @Override
      public String toString()
      {
         return "User [id=" + id + ", realmOid=" + userRealmOid + "]";
      }
   }

   public static final class UserOidKey extends PrimaryKey
   {
      private static final long serialVersionUID = 1L;

      public UserOidKey(long oid)
      {
         super(oid);
      }

      @Override
      public String toString()
      {
         return "User [oid=" + super.toString() + "]";
      }
   }
}
