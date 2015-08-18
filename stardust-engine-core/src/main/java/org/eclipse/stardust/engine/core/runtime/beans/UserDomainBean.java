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

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.cache.CacheHelper;
import org.eclipse.stardust.engine.core.cache.CacheInputStream;
import org.eclipse.stardust.engine.core.cache.CacheOutputStream;
import org.eclipse.stardust.engine.core.cache.Cacheable;
import org.eclipse.stardust.engine.core.cache.UserDomainsCache;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;



/**
 *
 */
public class UserDomainBean extends IdentifiablePersistentBean implements IUserDomain,
      Cacheable, Serializable
{
   private static final Logger trace = LogManager.getLogger(UserDomainBean.class);

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__ID = "id";
   public static final String FIELD__PARTITION = "partition";
   public static final String FIELD__SUPERDOMAIN = "superDomain";
   public static final String FIELD__DESCRIPTION = "description";

   public static final FieldRef FR__OID = new FieldRef(UserDomainBean.class, FIELD__OID);
   public static final FieldRef FR__ID = new FieldRef(UserDomainBean.class, FIELD__ID);
   public static final FieldRef FR__PARTITION = new FieldRef(UserDomainBean.class, FIELD__PARTITION);
   public static final FieldRef FR__SUPERDOMAIN = new FieldRef(UserDomainBean.class, FIELD__SUPERDOMAIN);
   public static final FieldRef FR__DESCRIPTION = new FieldRef(UserDomainBean.class, FIELD__DESCRIPTION);

   public static final String TABLE_NAME = "domain";
   public static final String DEFAULT_ALIAS = "dm";
   public static final String PK_FIELD = FIELD__OID;
   protected static final String PK_SEQUENCE = "domain_seq";
   public static final String[] domain_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] domain_idx2_UNIQUE_INDEX = new String[] {FIELD__ID, FIELD__PARTITION};

   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private static final int description_COLUMN_LENGTH = 4000;
   private String description;

   private AuditTrailPartitionBean partition;
   private UserDomainBean superDomain;

   public static UserDomainBean findByOID(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      if (oid == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_DOMAIN_OID.raise(0), 0);
      }

      UserDomainBean result = (UserDomainBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findByOID(UserDomainBean.class, oid);

      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_DOMAIN_OID.raise(oid), oid);
      }

      return result;
   }

   public static UserDomainBean findById(String id, long partitionOid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      UserDomainBean result = null;
      if (CacheHelper.isCacheable(UserDomainBean.class))
      {
         result = UserDomainsCache.instance().findById(id, partitionOid);
         if (result != null)
         {
            return result;
         }
      }

      result = (UserDomainBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).
            findFirst(UserDomainBean.class,
                  QueryExtension.where(Predicates.andTerm(
                        Predicates.isEqual(FR__ID, id),
                        Predicates.isEqual(FR__PARTITION, partitionOid))));

      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_DOMAIN_ID.raise(id), id);
      }

      return result;
   }

   public UserDomainBean()
   {
   }

   public UserDomainBean(String id, AuditTrailPartitionBean partition,
         UserDomainBean superDomain)
   {
      String trimmedId = StringUtils.cutString(id, id_COLUMN_LENGTH);

      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(//
            UserDomainBean.class, QueryExtension.where(Predicates.andTerm(//
                  Predicates.isEqual(FR__ID, trimmedId),//
                  Predicates.isEqual(FR__PARTITION, partition.getOID())))))
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_DOMAIN_WITH_ID_ALREADY_EXISTS.raise(trimmedId));
      }

      this.id = trimmedId;
      this.partition = partition;
      this.superDomain = superDomain;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);

      new UserDomainHierarchyBean(this, this);
      UserDomainBean iteratingSuperDomain = superDomain;
      while (null != iteratingSuperDomain)
      {
         new UserDomainHierarchyBean(iteratingSuperDomain, this);
         iteratingSuperDomain = (UserDomainBean) iteratingSuperDomain.getSuperDomain();
      }
   }

   public String toString()
   {
      return "Domain: " + getId();
   }

   public String getId()
   {
      fetch();

      return id;
   }

   public void setId(String id)
   {
      fetch();

      if ( !CompareHelper.areEqual(this.id, id))
      {
         if (null == this.superDomain)
         {
            throw new PublicException(
                  BpmRuntimeError.BPMRT_ID_FOR_A_PARTITIONS_DEFAULT_DOMAIN_IS_NOT_ALLOWED_TO_BE_CHANGED
                        .raise());

         }

         markModified(FIELD__ID);
         this.id = StringUtils.cutString(id, id_COLUMN_LENGTH);
      }
   }

   public IAuditTrailPartition getPartition()
   {
      fetchLink(FIELD__PARTITION);
      return partition;
   }

   public IUserDomain getSuperDomain()
   {
      fetchLink(FIELD__SUPERDOMAIN);
      return superDomain;
   }

   /**
    *
    */
   public String getDescription()
   {
      fetch();
      return description;
   }

   /**
    *
    */
   public void setDescription(String description)
   {
      if ( !CompareHelper.areEqual(this.description, description))
      {
         markModified(FIELD__DESCRIPTION);
         this.description = description;
      }
   }

   public void delete(boolean writeThrough)
   {
      long oid = getOID();

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).delete(
            UserDomainHierarchyBean.class, Predicates.orTerm(//
                  Predicates.isEqual(UserDomainHierarchyBean.FR__SUPERDOMAIN, oid),//
                  Predicates.isEqual(UserDomainHierarchyBean.FR__SUBDOMAIN, oid)),
            writeThrough);

      super.delete(writeThrough);
   }

   public byte[] store() throws IOException
   {
      CacheOutputStream cos = new CacheOutputStream();
      byte[] bytes;

      try
      {
         fetch();
         cos.writeLong(oid);
         cos.writeString(id);

         fetchLink(FIELD__PARTITION);
         cos.writeShort(partition.getOID());

         // encode null superDomain with 0
         fetchLink(FIELD__SUPERDOMAIN);
         cos.writeLong(superDomain == null ? 0 : superDomain.getOID());

         cos.writeString(description);

         cos.flush();
         bytes = cos.getBytes();
      }
      finally
      {
         cos.close();
      }

      return bytes;
   }

   public void retrieve(byte[] bytes) throws IOException
   {
      CacheInputStream cis = new CacheInputStream(bytes);

      try
      {
         oid = cis.readLong();
         id = cis.readString();

         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         short partitionOid = cis.readShort();
         if (partitionOid >= 0)
         {
            this.partition = (AuditTrailPartitionBean) session.findByOID(
                  AuditTrailPartitionBean.class, partitionOid);
         }

         long superDomainOid = cis.readLong();
         // superDomain can be null which is encoded as 0
         if (superDomainOid > 0)
         {
            this.superDomain = (UserDomainBean) session.findByOID(UserDomainBean.class,
                  partitionOid);
         }

         description = cis.readString();
      }
      finally
      {
         cis.close();
      }
   }
}

