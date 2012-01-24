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
import java.util.Iterator;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.cache.CacheHelper;
import org.eclipse.stardust.engine.core.cache.CacheInputStream;
import org.eclipse.stardust.engine.core.cache.CacheOutputStream;
import org.eclipse.stardust.engine.core.cache.Cacheable;
import org.eclipse.stardust.engine.core.cache.PartitionsCache;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.PersistentVector;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;



/**
 *
 */
public class AuditTrailPartitionBean extends PersistentBean implements
      IAuditTrailPartition, Cacheable, Serializable
{
   private static final Logger trace = LogManager.getLogger(AuditTrailPartitionBean.class);

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__ID = "id";
   public static final String FIELD__DESCRIPTION = "description";

   public static final FieldRef FR__OID = new FieldRef(AuditTrailPartitionBean.class, FIELD__OID);
   public static final FieldRef FR__ID = new FieldRef(AuditTrailPartitionBean.class, FIELD__ID);
   public static final FieldRef FR__DESCRIPTION = new FieldRef(AuditTrailPartitionBean.class, FIELD__DESCRIPTION);
   
   public static final String LINK__USER_DOMAIN_LINKS = "userDomainLinks";

   public static final String TABLE_NAME = "partition";
   public static final String DEFAULT_ALIAS = "prt";
   public static final String PK_FIELD = FIELD__OID;
   protected static final String PK_SEQUENCE = "partition_seq";
   public static final String[] partition_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] partition_idx2_UNIQUE_INDEX = new String[] {FIELD__ID};
   
   public PersistentVector userDomains;
   private static final String userDomains_TABLE_NAME = UserDomainBean.TABLE_NAME;
   private static final String userDomains_CLASS = UserDomainBean.class.getName();
   private static final String userDomains_OTHER_ROLE = UserDomainBean.FIELD__PARTITION;
   private static final String userDomains_OWNED = "true";

   private long oid;
   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private static final int description_COLUMN_LENGTH = 4000;
   private String description;

   public static Iterator findAll()
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            AuditTrailPartitionBean.class);
   }

   public static AuditTrailPartitionBean findByOID(short oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      AuditTrailPartitionBean result = (AuditTrailPartitionBean) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL).findByOID(
                  AuditTrailPartitionBean.class, oid);
      
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_PARTITION_OID.raise(oid), oid);
      }
      
      return result;
   }

   public static AuditTrailPartitionBean findById(String id)
      throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      AuditTrailPartitionBean result = null;
      if (CacheHelper.isCacheable(AuditTrailPartitionBean.class))
      {
         result = PartitionsCache.instance().findById(id);
         if (result != null)
         {
            return result;
         }
      }
      
      result = (AuditTrailPartitionBean) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL).findFirst(
                  AuditTrailPartitionBean.class,
                  QueryExtension.where(Predicates.isEqual(FR__ID, id)));

      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_PARTITION_ID.raise(id), id);
      }

      return result;
   }

   public AuditTrailPartitionBean()
   {
   }

   public AuditTrailPartitionBean(String id)
   {
      Assert.isNotEmpty(id, "Id must not be empty.");
      String trimmedId = StringUtils.cutString(id, id_COLUMN_LENGTH);
      
      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(
            AuditTrailPartitionBean.class,
            QueryExtension.where(Predicates.isEqual(FR__ID, trimmedId))))
      {
         throw new PublicException("Partition with id '" + trimmedId
               + "' already exists.");
      }
      
      this.id = trimmedId;
      
      this.userDomains = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .createPersistentVector();

      
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public String toString()
   {
      return "Partition: " + getId();
   }

   public void lock() throws ConcurrencyException
   {
      if (isPersistent())
      {
         getPersistenceController().getSession().lock(getClass(), getOID());
      }
   }
   
   public short getOID()
   {
      fetch();
      return (short) oid;
   }

   public void setOID(short oid)
   {
      fetch();
      if (this.oid != oid)
      {
         markModified(FIELD__OID);
         this.oid = oid;
      }
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
         markModified(FIELD__ID);
         this.id = StringUtils.cutString(id, id_COLUMN_LENGTH);
      }
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
      fetch();
      if ( !CompareHelper.areEqual(this.description, description))
      {
         markModified(FIELD__DESCRIPTION);
         this.description = description;
      }
   }
   
   public Iterator getAllUserDomains()
   {
      fetchVector(LINK__USER_DOMAIN_LINKS);
      return userDomains.scan();
   }

   public IUserDomain getDefaultUserDomain()
   {
      UserDomainBean result = (UserDomainBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(UserDomainBean.class,//
            QueryExtension.where(Predicates.andTerm(//
                  Predicates.isEqual(UserDomainBean.FR__PARTITION, this.getOID()),//
                  Predicates.isNull(UserDomainBean.FR__SUPERDOMAIN))));

      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_DEFAULT_DOMAIN_FOR_PARTITION_ID.raise(id));
      }
      
      return result;
   }
   
   public AbstractProperty createProperty(String name, Serializable value)
   {
      return new UserProperty(getOID(), name, value);
   }

   public Class getPropertyImplementationClass()
   {
      return UserProperty.class;
   }

   public byte[] store() throws IOException
   {
      fetch();
      
      CacheOutputStream cos = new CacheOutputStream();
      byte[] bytes;

      try
      {
         cos.writeLong(oid);
         cos.writeString(id);
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
         description = cis.readString();
      }
      finally
      {
         cis.close();
      }
   }
}

