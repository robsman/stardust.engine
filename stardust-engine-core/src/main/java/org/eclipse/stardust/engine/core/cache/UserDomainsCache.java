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

import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.UserDomainBean;


/**
 * @author stephan.born
 * @version $Revision: $
 */
public class UserDomainsCache extends AbstractCache<UserDomainBean>
{
   private static UserDomainsCache INSTANCE;

   public static UserDomainsCache instance()
   {
      if (INSTANCE == null)
      {
         INSTANCE = new UserDomainsCache();
      }
      return INSTANCE;
   }

   private UserDomainsCache()
   {
      super("domains");
   }

   @Override
   PrimaryKey getKeyForValue(UserDomainBean domain)
   {
      return getKey(domain.getOID());
   }

   @Override
   PrimaryKey getKey(long oid)
   {
      return new DomainOidKey(oid);
   }

   @Override
   UserDomainBean retrieve(byte[] bytes) throws IOException
   {
      UserDomainBean bean = new UserDomainBean();
      bean.retrieve(bytes);
      return bean;
   }

   @Override
   List< ? extends CacheKey> getSecondaryKeys(UserDomainBean value)
   {
      IAuditTrailPartition partition = value.getPartition();
      return Collections.<DomainIdKey> singletonList(new DomainIdKey(value.getId(),
            partition.getOID()));
   }

   /**
    * @author stephan.born
    * @version $Revision: $
    */
   public static final class DomainOidKey extends PrimaryKey
   {
      private static final long serialVersionUID = 1L;

      public DomainOidKey(long oid)
      {
         super(oid);
      }
   }

   /**
    * @author stephan.born
    * @version $Revision: $
    */
   public static final class DomainIdKey implements CacheKey
   {
      private static final long serialVersionUID = 1L;

      private String id;
      private short partitionOid;

      public DomainIdKey(String id, short partitionOid)
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
         if (obj instanceof DomainIdKey)
         {
            DomainIdKey other = (DomainIdKey) obj;
            return partitionOid == other.partitionOid
                  && (id == other.id || id != null && id.equals(other.id));
         }

         return false;
      }
   }

   public UserDomainBean findById(String id, long partitionOid)
   {
      DomainIdKey idKey = new DomainIdKey(id, (short) partitionOid);
      DomainOidKey oidKey = (DomainOidKey) getPrimaryKey(idKey);
      if (oidKey == null)
      {
         return null;
      }

      UserDomainBean userDomain = UserDomainBean
            .findByOID(oidKey.getOid());
      if (userDomain == null)
      {
         // remove phantom key
         removeKey(idKey);
      }

      return userDomain;
   }
}
