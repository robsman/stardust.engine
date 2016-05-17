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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.api.runtime.UserPK;
import org.eclipse.stardust.engine.core.cache.CacheHelper;
import org.eclipse.stardust.engine.core.cache.CacheInputStream;
import org.eclipse.stardust.engine.core.cache.CacheOutputStream;
import org.eclipse.stardust.engine.core.cache.Cacheable;
import org.eclipse.stardust.engine.core.cache.RealmsCache;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;

public class UserRealmBean extends AttributedIdentifiablePersistentBean implements IUserRealm, Serializable, Cacheable
{
   private static final long serialVersionUID = 2L;

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__ID = "id";
   public static final String FIELD__NAME = "name";
   public static final String FIELD__DESCRIPTION = "description";
   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(UserRealmBean.class, FIELD__OID);
   public static final FieldRef FR__ID = new FieldRef(UserRealmBean.class, FIELD__ID);
   public static final FieldRef FR__NAME = new FieldRef(UserRealmBean.class, FIELD__NAME);
   public static final FieldRef FR__DESCRIPTION = new FieldRef(UserRealmBean.class, FIELD__DESCRIPTION);
   public static final FieldRef FR__PARTITION = new FieldRef(UserRealmBean.class, FIELD__PARTITION);

   public static final String LINK__USER_LINKS = "userLinks";

   public static final String TABLE_NAME = "wfuser_realm";
   public static final String DEFAULT_ALIAS = "ur";
   public static final String PK_FIELD = FIELD__OID;
   protected static final String PK_SEQUENCE = "wfuser_realm_seq";
   public static final String[] wfuser_realm_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] wfuser_realm_idx2_UNIQUE_INDEX = new String[] {FIELD__ID, FIELD__PARTITION};
   //protected static final Class LOADER = UserLoader.class;

   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private static final int name_COLUMN_LENGTH = 100;
   private String name;
   private static final int description_COLUMN_LENGTH = 4000;
   private String description;
   private long partition;

   private static ConcurrentMap<Short, IUserRealm> SYSTEM_REALMS = new ConcurrentHashMap<Short, IUserRealm>();

   public static UserRealmBean findByOID(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      UserRealmBean result = null;

      if (0 != oid)
      {
         result = (UserRealmBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
               .findByOID(UserRealmBean.class, oid);
      }

      if (null == result)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_REALM_OID.raise(new Long(oid)), oid);
      }

      return result;
   }

   public static UserRealmBean findById(String id, short partitionOid)
      throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      UserRealmBean result = null;
      if (CacheHelper.isCacheable(UserRealmBean.class))
      {
         result = RealmsCache.instance().findById(id, partitionOid);
         if (result != null)
         {
            return result;
         }
      }

      result = (UserRealmBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(UserRealmBean.class,//
                  QueryExtension.where(Predicates.andTerm(//
                        Predicates.isEqual(FR__ID, id),//
                        Predicates.isEqual(FR__PARTITION, partitionOid))));

      if (result == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.ATDB_UNKNOWN_USER_REALM_ID.raise(
               id, new Long(partitionOid)));
      }
      return result;
   }

   public static IUserRealm getSystemRealm(IAuditTrailPartition partition)
   {
      Short partitionOid = partition.getOID();
      IUserRealm systemRealm = SYSTEM_REALMS.get(partitionOid);
      if (systemRealm == null)
      {
         systemRealm = new UserRealmBean();
         ((UserRealmBean) systemRealm).id = PredefinedConstants.SYSTEM_REALM;
         ((UserRealmBean) systemRealm).name = PredefinedConstants.SYSTEM_REALM;
         ((UserRealmBean) systemRealm).partition = (null != partition) ? partition.getOID() : 0;
         IUserRealm existing = SYSTEM_REALMS.putIfAbsent(partitionOid, systemRealm);
         if (existing != null)
         {
            systemRealm = existing;
         }
      }
      return systemRealm;
   }

   /*public static UserRealmBean createTransientRealm(String id, String name,
         IAuditTrailPartition partition)
   {
      UserRealmBean realm = new UserRealmBean();

      realm.setId(id);
      realm.setName(name);
      realm.setPartition(partition);

      return realm;
   }*/

   public UserRealmBean()
   {
   }

   public UserRealmBean(String id, String name, AuditTrailPartitionBean partition)
   {
      Assert.isNotEmpty(id, "ID for user realm must not be empty.");
      Assert.isNotNull(partition, "Partition for user realm must not be null.");

      String trimmedId = StringUtils.cutString(id, id_COLUMN_LENGTH);

      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(UserRealmBean.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__ID, trimmedId),
                  Predicates.isEqual(FR__PARTITION, partition.getOID())))))
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_USER_REALM_WITH_ID_ALREADY_EXISTS.raise(trimmedId));
      }

      this.id = trimmedId;
      this.name = StringUtils.cutString(name, name_COLUMN_LENGTH);
      this.partition = (null != partition) ? partition.getOID() : 0;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public UserPK getPK()
   {
      return new UserPK(getOID());
   }

   public String toString()
   {
      return "User Realm: " + getId() + " (" + name + ")";
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

   public String getName()
   {
      fetch();
      return name;
   }

   public void setName(String name)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.name, name))
      {
         markModified(FIELD__NAME);
         this.name = StringUtils.cutString(name, name_COLUMN_LENGTH);
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
      if ( !CompareHelper.areEqual(this.description, description))
      {
         markModified(FIELD__DESCRIPTION);
         this.description = description;
      }
   }

   public IAuditTrailPartition getPartition()
   {
      fetch();
      return LoginUtils.findPartition(Parameters.instance(), (short) partition);
   }

   public short getPartitionOid()
   {
      fetch();
      return (short) partition;
   }

   public AbstractProperty createProperty(String name, Serializable value)
   {
      return new UserProperty(getOID(), name, value);
   }

   public Class getPropertyImplementationClass()
   {
      return UserProperty.class;
   }

   private void setPartition(IAuditTrailPartition partition)
   {
      short partitionOid = (null != partition) ? partition.getOID() : 0;
      if (this.partition != partitionOid)
      {
         markModified(FIELD__PARTITION);
         this.partition = partitionOid;
      }
   }

   public void retrieve(byte[] bytes) throws IOException
   {
      CacheInputStream cis = new CacheInputStream(bytes);
      oid = cis.readLong();
      id = cis.readString();
      name = cis.readString();
      description = cis.readString();
      partition = cis.readLong();
      cis.close();
   }

   public byte[] store() throws IOException
   {
      fetch();
      CacheOutputStream cos = new CacheOutputStream();
      cos.writeLong(oid);
      cos.writeString(id);
      cos.writeString(name);
      cos.writeString(description);
      cos.writeLong(partition);
      cos.flush();
      byte[] bytes = cos.getBytes();
      cos.close();
      return bytes;
   }
}

