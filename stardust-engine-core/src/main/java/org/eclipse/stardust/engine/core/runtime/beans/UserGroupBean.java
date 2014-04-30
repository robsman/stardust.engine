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
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.cache.CacheHelper;
import org.eclipse.stardust.engine.core.cache.CacheInputStream;
import org.eclipse.stardust.engine.core.cache.CacheOutputStream;
import org.eclipse.stardust.engine.core.cache.Cacheable;
import org.eclipse.stardust.engine.core.cache.UserGroupsCache;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;



/**
 *
 */
public class UserGroupBean extends AttributedIdentifiablePersistentBean implements
      IUserGroup, Serializable, Cacheable
{
   private static final Logger trace = LogManager.getLogger(UserGroupBean.class);
   
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__ID = "id";
   public static final String FIELD__NAME = "name";
   public static final String FIELD__VALID_FROM = "validFrom";
   public static final String FIELD__VALID_TO = "validTo";
   public static final String FIELD__DESCRIPTION = "description";
   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(UserGroupBean.class, FIELD__OID);
   public static final FieldRef FR__ID = new FieldRef(UserGroupBean.class, FIELD__ID);
   public static final FieldRef FR__NAME = new FieldRef(UserGroupBean.class, FIELD__NAME);
   public static final FieldRef FR__VALID_FROM = new FieldRef(UserGroupBean.class, FIELD__VALID_FROM);
   public static final FieldRef FR__VALID_TO = new FieldRef(UserGroupBean.class, FIELD__VALID_TO);
   public static final FieldRef FR__DESCRIPTION = new FieldRef(UserGroupBean.class, FIELD__DESCRIPTION);
   public static final FieldRef FR__PARTITION = new FieldRef(UserGroupBean.class, FIELD__PARTITION);

   public static final String TABLE_NAME = "usergroup";
   public static final String DEFAULT_ALIAS = "ug";
   public static final String PK_FIELD = FIELD__OID;
   private static final String PK_SEQUENCE = "usergroup_seq";
   public static final String[] usergroup_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] usergroup_idx2_UNIQUE_INDEX = new String[] {FIELD__ID, FIELD__PARTITION};
   private static final Class LOADER = UserGroupLoader.class;

   private static final int id_COLUMN_LENGTH = 50;
   private String id;

   private static final int name_COLUMN_LENGTH = 150;
   private String name;

   private Date validFrom;
   private Date validTo;

   private String description;
   private AuditTrailPartitionBean partition;

   public static UserGroupBean findByOid(long oid) throws ObjectNotFoundException
   {
      if (oid == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_GROUP_OID.raise(0), 0);
      }
      UserGroupBean result = (UserGroupBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findByOID(UserGroupBean.class, oid);
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_GROUP_OID.raise(oid), oid);
      }

      return result;
   }

   public static UserGroupBean findById(String id, short partitionOid) throws ObjectNotFoundException
   {
      UserGroupBean result = null;
      if (CacheHelper.isCacheable(UserGroupBean.class))
      {
         result = UserGroupsCache.instance().findById(id, partitionOid);
         if (result != null)
         {
            return result;
         }
      }
      
      result = (UserGroupBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(UserGroupBean.class,
                  QueryExtension.where(Predicates.andTerm(
                        Predicates.isEqual(FR__ID, id),
                        Predicates.isEqual(FR__PARTITION, partitionOid))));
      
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_GROUP_ID.raise(id), id);
      }

      return result;
   }

   /**
    * Returns user groups for a given user. It is possible to decide whether all user
    * groups or valid only should be returned.
    * 
    * @param userOid Oid of an user
    * @param validOnly
    * @return Iterator of UserGroupBeans
    */
   public static Iterator findForUser(long userOid, boolean validOnly)
   {
      QueryExtension queryExtension = new QueryExtension();

      Join uugJoin = new Join(UserUserGroupLink.class)
            .on(UserGroupBean.FR__OID, UserUserGroupLink.FIELD__USER_GROUP);
      
      queryExtension.addJoin(uugJoin);
      
      PredicateTerm predicate = Predicates.isEqual(UserUserGroupLink.FR__USER,
            userOid);

      if (true == validOnly)
      {
         Date now = new Date();
         
         Join uJoin = new Join(UserBean.class)
               .on(UserUserGroupLink.FR__USER, UserBean.FIELD__OID);
         queryExtension.addJoin(uJoin);

         predicate = Predicates.andTerm(
               predicate,
               Predicates.lessOrEqual(UserGroupBean.FR__VALID_FROM, now.getTime()),
               Predicates.orTerm(
                     Predicates.greaterOrEqual(UserGroupBean.FR__VALID_TO, now.getTime()),
                     Predicates.isEqual(UserGroupBean.FR__VALID_TO, 0)));
      }
      queryExtension.setWhere(predicate);

      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            UserGroupBean.class, queryExtension);
   }

   public static int getMaxIdLength()
   {
      return id_COLUMN_LENGTH;
   }
   
   public UserGroupBean()
   {
   }

   public UserGroupBean(String id, String name, AuditTrailPartitionBean partition)
   {
      this();
      String trimmedId = StringUtils.cutString(id, id_COLUMN_LENGTH);

      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(
            UserGroupBean.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__ID, trimmedId),
                  Predicates.isEqual(FR__PARTITION, partition.getOID())))))
      {
         throw new PublicException(MessageFormat.format(
               "User group with id ''{0}'' already exists for {1}.", new Object[] {
                     trimmedId, partition }));
      }

      this.id = trimmedId;
      this.name = StringUtils.cutString(name, name_COLUMN_LENGTH);
      this.partition = partition;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public void delete()
   {
      // TODO (sb): invalidate user group instead?
   }

   public String toString()
   {
      return "UserGroup: " + getId() + " (" + name + ")";
   }

   public String getId()
   {
      fetch();
      return id;
   }

   public void setId(String id)
   {
      if ( !CompareHelper.areEqual(getId(), id))
      {
         markModified(FIELD__ID);
         this.id = id;
      }
   }

   /**
    * 
    */
   public String getName()
   {
      fetch();
      return name;
   }

   /**
    * 
    */
   public void setName(String name)
   {
      if ( !CompareHelper.areEqual(getName(), name))
      {
         markModified(FIELD__NAME);
         this.name = name;
      }
   }

   /**
    * 
    */
   public Date getValidFrom()
   {
      fetch();
      return validFrom;
   }

   public void setValidFrom(Date validFrom)
   {
      if ( !CompareHelper.areEqual(getValidFrom(), validFrom))
      {
         markModified(FIELD__VALID_FROM);
         this.validFrom = validFrom;
      }
   }

   public Date getValidTo()
   {
      fetch();
      return validTo;
   }

   public void setValidTo(Date validTo)
   {
      if ( !CompareHelper.areEqual(getValidTo(), validTo))
      {
         markModified(FIELD__VALID_TO);
         this.validTo = validTo;
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
      if ( !CompareHelper.areEqual(getDescription(), description))
      {
         markModified(FIELD__DESCRIPTION);
         this.description = description;
      }
   }

   public AuditTrailPartitionBean getPartition()
   {
      fetchLink(FIELD__PARTITION);
      return partition;
   }

   public void setPartition(AuditTrailPartitionBean partition)
   {
      fetchLink(FIELD__PARTITION);

      if (this.partition != partition)
      {
         this.partition = partition;
         markModified(FIELD__PARTITION);
      }
   }
   
   // @todo (france, ub): introduce an additional boolean field for temporary disabling
   public boolean isValid()
   {
      fetch();
      Date now = new Date();

      return (validFrom == null || validFrom.getTime() <= now.getTime())
            && (validTo == null || validTo.getTime() > now.getTime());
   }

   /**
    * TODO (sb): improve user loading efficiency
    */
   public Iterator findAllUsers()
   {
      return new TransformingIterator(UserUserGroupLink.findAllFor(getOID()),
            new Functor()
            {
               public Object execute(Object source)
               {
                  IUser result = ((UserUserGroupLink) source).getUser();
                  if (result == null)
                  {
                     trace.warn("Dangling link with OID "
                           + ((UserUserGroupLink) source).getOID() + "  for user group'"
                           + getOID());
                  }
                  return result;
               }
            });
   }

   public void addUser(IUser user)
   {
      fetch();

      if (null != UserUserGroupLink.find(getOID(), user.getOID()))
      {
         return;
      }

      if (false == isValid())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.AUTHx_USER_CANNOT_JOIN_INVALID_USER_GROUP.raise(user.getOID(), getId()));
      }

      UserUserGroupLink link = new UserUserGroupLink(user, this);

      StringBuffer buffer = new StringBuffer();
      buffer.append("Granting membership for ")//
            .append(this)
            .append(" to ")
            .append(user)
            .append(".");
      AuditTrailLogger.getInstance(LogCode.SECURITY).info(buffer.toString());

      if (CacheHelper.isCacheable(UserBean.class))
      {
         // mark modified to trigger 2nd level cache update
         ((UserBean) user).markModified();
      }
      ((UserBean) user).userGroupLinks.add(link);

      try
      {
         SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(link);
      }
      catch (PublicException e)
      {
         // "local rollback" in case e.g. of a audit trail exception
         ((UserBean) user).userGroupLinks.remove(link);
         throw e;
      }
   }

   public void removeUser(IUser user)
   {
      fetch();

      UserUserGroupLink link = UserUserGroupLink.find(getOID(), user.getOID());

      if (link != null)
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("Removing membership for ")//
               .append(this)
               .append(" from ")
               .append(user)
               .append(".");
         AuditTrailLogger.getInstance(LogCode.SECURITY).info(buffer.toString());

         if (CacheHelper.isCacheable(UserBean.class))
         {
            // mark modified to trigger 2nd level cache update
            ((UserBean) user).markModified();
         }
         link.delete();
      }
   }

   public AbstractProperty createProperty(String name, Serializable value)
   {
      return new UserGroupProperty(getOID(), name, value);
   }

   public Class getPropertyImplementationClass()
   {
      return UserGroupProperty.class;
   }

   public void retrieve(byte[] bytes) throws IOException
   {
      CacheInputStream cis = new CacheInputStream(bytes);
      oid = cis.readLong();
      id = cis.readString();
      name = cis.readString();
      validFrom = cis.readDate();
      validTo = cis.readDate();
      description = cis.readString();
      short partition = cis.readShort();
      if (partition >= 0)
      {
         this.partition = (AuditTrailPartitionBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).
            findByOID(AuditTrailPartitionBean.class, partition);
      }
      cis.close();
   }

   public byte[] store() throws IOException
   {
      fetch();
      CacheOutputStream cos = new CacheOutputStream();
      cos.writeLong(oid);
      cos.writeString(id);
      cos.writeString(name);
      cos.writeDate(validFrom);
      cos.writeDate(validTo);
      cos.writeString(description);
      
      fetchLink(FIELD__PARTITION);
      cos.writeShort(partition == null ? -1 : partition.getOID());
      
      cos.flush();
      byte[] bytes = cos.getBytes();
      cos.close();
      return bytes;
   }
}
