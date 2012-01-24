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


/**
 * @author stephan.born
 * @version $Revision: $
 */
public class PartitionsCache extends AbstractCache <AuditTrailPartitionBean>
{
   private static PartitionsCache INSTANCE;
   
   public static PartitionsCache instance()
   {
      if (INSTANCE == null)
      {
         INSTANCE = new PartitionsCache();
      }
      return INSTANCE;
   }

   private PartitionsCache()
   {
      super("partitions");
   }
   

   @Override
   PrimaryKey getKeyForValue(AuditTrailPartitionBean partition)
   {
      return getKey(partition.getOID());
   }

   @Override
   PrimaryKey getKey(long oid)
   {
      return new PartitionOidKey(oid);
   }

   @Override
   AuditTrailPartitionBean retrieve(byte[] bytes) throws IOException
   {
      AuditTrailPartitionBean bean = new AuditTrailPartitionBean();
      bean.retrieve(bytes);
      return bean;
   }

   @Override
   List< ? extends CacheKey> getSecondaryKeys(AuditTrailPartitionBean value)
   {
      return Collections
            .<PartitionIdKey> singletonList(new PartitionIdKey(value.getId()));
   }
   
   /**
    * @author stephan.born
    * @version $Revision: $
    */
   public static final class PartitionOidKey extends PrimaryKey
   {
      private static final long serialVersionUID = 1L;
      
      public PartitionOidKey(long oid)
      {
         super(oid);
      }
   }
   
   /**
    * @author stephan.born
    * @version $Revision: $
    */
   public static final class PartitionIdKey implements CacheKey
   {
      private static final long serialVersionUID = 1L;
      
      private String id;

      public PartitionIdKey(String id)
      {
         this.id = id;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof PartitionIdKey)
         {
            PartitionIdKey other = (PartitionIdKey) obj;
            return (id == other.id || id != null && id.equals(other.id));
         }
         
         return false;
      }
   }

   public AuditTrailPartitionBean findById(String id)
   {
      PartitionIdKey idKey = new PartitionIdKey(id);
      PartitionOidKey oidKey = (PartitionOidKey) getPrimaryKey(idKey);
      if (oidKey == null)
      {
         return null;
      }
      AuditTrailPartitionBean partition = AuditTrailPartitionBean
            .findByOID((short) oidKey.getOid());
      if (partition == null)
      {
         // remove phantom key
         removeKey(idKey);
      }
      return partition;
   }}
