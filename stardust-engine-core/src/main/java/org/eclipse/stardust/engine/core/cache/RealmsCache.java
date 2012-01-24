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

import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;


/**
 * @author Florin.Herinean
 */
public class RealmsCache extends AbstractCache <UserRealmBean>
{
   private static RealmsCache INSTANCE;
   
   public static RealmsCache instance()
   {
      if (INSTANCE == null)
      {
         INSTANCE = new RealmsCache();
      }
      return INSTANCE;
   }

   private RealmsCache()
   {
      super("realms");
   }

   @Override
   UserRealmOidKey getKeyForValue(UserRealmBean realm)
   {
      return new UserRealmOidKey(realm.getOID());
   }

   @Override
   UserRealmBean retrieve(byte[] bytes) throws IOException
   {
      UserRealmBean realm = new UserRealmBean();
      realm.retrieve(bytes);
      return realm;
   }

   @Override
   UserRealmOidKey getKey(long oid)
   {
      return new UserRealmOidKey(oid);
   }

   @Override
   List<? extends CacheKey> getSecondaryKeys(UserRealmBean realm)
   {
      return Collections.<UserRealmIdKey>singletonList(new UserRealmIdKey(
            realm.getId(), realm.getPartitionOid()));
   }
   
   public UserRealmBean findById(String id, short partitionOid)
   {
      UserRealmIdKey idKey = new UserRealmIdKey(id, partitionOid);
      UserRealmOidKey oidKey = (UserRealmOidKey) getPrimaryKey(idKey);
      if (oidKey == null)
      {
         return null;
      }
      UserRealmBean realm = UserRealmBean.findByOID(oidKey.getOid());
      if (realm == null)
      {
         // remove phantom key
         removeKey(idKey);
      }
      return realm;
   }

   public static final class UserRealmIdKey implements CacheKey
   {
      private static final long serialVersionUID = 1L;
      
      private String id;
      private short partitionOid;

      public UserRealmIdKey(String id, short partitionOid)
      {
         this.id = id;
         this.partitionOid = partitionOid;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         result = prime * result + partitionOid;
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof UserRealmIdKey)
         {
            UserRealmIdKey other = (UserRealmIdKey) obj;
            return partitionOid == other.partitionOid &&
                  (id == other.id || id != null && id.equals(other.id));
         }
         return false;
      }
   }
   
   public static final class UserRealmOidKey extends PrimaryKey
   {
      private static final long serialVersionUID = 1L;
      
      public UserRealmOidKey(long oid)
      {
         super(oid);
      }
   }
}
