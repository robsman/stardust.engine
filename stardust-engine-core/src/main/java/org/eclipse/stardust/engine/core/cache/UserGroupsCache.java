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

import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserGroupBean;


/**
 * @author Florin.Herinean
 */
public class UserGroupsCache extends AbstractCache <UserGroupBean>
{
   private static UserGroupsCache INSTANCE;
   
   public static UserGroupsCache instance()
   {
      if (INSTANCE == null)
      {
         INSTANCE = new UserGroupsCache();
      }
      return INSTANCE;
   }

   private UserGroupsCache()
   {
      super("groups");
   }

   @Override
   UserGroupOidKey getKeyForValue(UserGroupBean realm)
   {
      return new UserGroupOidKey(realm.getOID());
   }

   @Override
   UserGroupBean retrieve(byte[] bytes) throws IOException
   {
      UserGroupBean realm = new UserGroupBean();
      realm.retrieve(bytes);
      return realm;
   }

   @Override
   UserGroupOidKey getKey(long oid)
   {
      return new UserGroupOidKey(oid);
   }

   @Override
   List<? extends CacheKey> getSecondaryKeys(UserGroupBean group)
   {
      AuditTrailPartitionBean partition = group.getPartition();
      return Collections.<UserGroupIdKey>singletonList(new UserGroupIdKey(
            group.getId(), partition == null ? 0 : partition.getOID()));
   }
   
   public UserGroupBean findById(String id, short partitionOid)
   {
      UserGroupIdKey idKey = new UserGroupIdKey(id, partitionOid);
      UserGroupOidKey oidKey = (UserGroupOidKey) getPrimaryKey(idKey);
      if (oidKey == null)
      {
         return null;
      }
      UserGroupBean group = UserGroupBean.findByOid(oidKey.getOid());
      if (group == null)
      {
         // remove phantom key
         removeKey(idKey);
      }
      return group;
   }

   public static final class UserGroupIdKey implements CacheKey
   {
      private static final long serialVersionUID = 1L;
      
      private String id;
      private short partitionOid;

      public UserGroupIdKey(String id, short partitionOid)
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
         if (obj instanceof UserGroupIdKey)
         {
            UserGroupIdKey other = (UserGroupIdKey) obj;
            return partitionOid == other.partitionOid &&
                  (id == other.id || id != null && id.equals(other.id));
         }
         return false;
      }
   }
   
   public static final class UserGroupOidKey extends PrimaryKey
   {
      private static final long serialVersionUID = 1L;
      
      public UserGroupOidKey(long oid)
      {
         super(oid);
      }
   }
}
