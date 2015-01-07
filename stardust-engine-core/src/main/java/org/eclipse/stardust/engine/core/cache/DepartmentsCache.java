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

import org.eclipse.stardust.engine.core.runtime.beans.DepartmentBean;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;


/**
 * @author Florin.Herinean
 */
public class DepartmentsCache extends AbstractCache <DepartmentBean>
{
   private static DepartmentsCache INSTANCE;
   
   public static DepartmentsCache instance()
   {
      if (INSTANCE == null)
      {
         INSTANCE = new DepartmentsCache();
      }
      return INSTANCE;
   }

   private DepartmentsCache()
   {
      super("departments");
   }

   @Override
   DepartmentOidKey getKeyForValue(DepartmentBean department)
   {
      return getKey(department.getOID());
   }

   @Override
   DepartmentBean retrieve(byte[] bytes) throws IOException
   {
      DepartmentBean bean = new DepartmentBean();
      bean.retrieve(bytes);
      return bean;
   }

   @Override
   DepartmentOidKey getKey(long oid)
   {
      return new DepartmentOidKey(oid);
   }

   @Override
   List<? extends CacheKey> getSecondaryKeys(DepartmentBean value)
   {
      IAuditTrailPartition partition = value.getPartition();
      return Collections.<DepartmentIdKey>singletonList(new DepartmentIdKey(
            value.getId(), value.getParentDepartmentOID(), value.getRuntimeOrganizationOID(),
            partition == null ? 0 : partition.getOID()));
   }

   public DepartmentBean findById(String id, long parentDepartmentOid,
         long runtimeOrganizationOid, short partitionOid)
   {
      DepartmentIdKey idKey = new DepartmentIdKey(id, parentDepartmentOid, runtimeOrganizationOid, partitionOid);
      DepartmentOidKey oidKey = (DepartmentOidKey) getPrimaryKey(idKey);
      if (oidKey == null)
      {
         return null;
      }
      DepartmentBean department = DepartmentBean.findByOID(oidKey.getOid());
      if (department == null)
      {
         // remove phantom key
         removeKey(idKey);
      }
      return department;
   }
   
   public static final class DepartmentIdKey implements CacheKey
   {
      private static final long serialVersionUID = 1L;
      
      private String id;
      private long parentDepartmentOid;
      private long runtimeOrganizationOid;
      private short partitionOid;

      public DepartmentIdKey(String id, long parentDepartmentOid, long runtimeOrganizationOid, short partitionOid)
      {
         this.id = id;
         this.parentDepartmentOid = parentDepartmentOid;
         this.runtimeOrganizationOid = runtimeOrganizationOid;
         this.partitionOid = partitionOid;
      }

      @Override
      public String toString()
      {
         return id;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         result = prime * result + (int) (parentDepartmentOid ^ (parentDepartmentOid >>> 32));
         result = prime * result + (int) (runtimeOrganizationOid ^ (runtimeOrganizationOid >>> 32));
         result = prime * result + partitionOid;
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof DepartmentIdKey)
         {
            DepartmentIdKey other = (DepartmentIdKey) obj;
            return parentDepartmentOid == other.parentDepartmentOid &&
                   runtimeOrganizationOid == other.runtimeOrganizationOid &&
                   partitionOid == other.partitionOid &&
                  (id == other.id || id != null && id.equals(other.id));
         }
         return false;
      }
   }
   
   public static final class DepartmentOidKey extends PrimaryKey
   {
      private static final long serialVersionUID = 1L;
      
      public DepartmentOidKey(long oid)
      {
         super(oid);
      }
   }
}
