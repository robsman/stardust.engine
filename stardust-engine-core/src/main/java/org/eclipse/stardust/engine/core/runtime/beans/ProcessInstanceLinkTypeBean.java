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

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;



/**
 * Stores a property information alternatively to the regular property
 * mechanism of Java.
 */
public class ProcessInstanceLinkTypeBean extends IdentifiablePersistentBean implements IProcessInstanceLinkType
{
   private static final long serialVersionUID = 1L;
   
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__ID = "id";
   public static final String FIELD__DESCRIPTION = "description";
   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(ProcessInstanceLinkTypeBean.class, FIELD__OID);
   public static final FieldRef FR__ID = new FieldRef(ProcessInstanceLinkTypeBean.class, FIELD__ID);
   public static final FieldRef FR__DESCRIPTION = new FieldRef(ProcessInstanceLinkTypeBean.class, FIELD__DESCRIPTION);
   public static final FieldRef FR__PARTITION = new FieldRef(ProcessInstanceLinkTypeBean.class, FIELD__PARTITION);

   public static final String TABLE_NAME = "link_type";
   public static final String DEFAULT_ALIAS = "lt";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "link_type_seq";
   public static final String[] link_type_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   
   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private static final int description_COLUMN_LENGTH = 255;
   private String description;
   private long partition;

   public static ResultIterator<ProcessInstanceLinkTypeBean> findAll()
   {
      short partition = SecurityProperties.getPartitionOid();
      QueryExtension extension = QueryExtension.where(Predicates.isEqual(FR__PARTITION, partition));
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      return session.getIterator(ProcessInstanceLinkTypeBean.class, extension);
   }
   
   private static final class Key
   {
      private String id;
      private short partition;
      
      private Key(String id, short partition)
      {
         this.id = id;
         this.partition = partition;
      }

      public int hashCode()
      {
         final int prime = 31;
         return prime * (prime + id.hashCode()) + partition;
      }

      public boolean equals(Object obj)
      {
         Key other = (Key) obj;
         return partition == other.partition && id.equals(other.id);
      }
   }

   // TODO: use global cache
   private static final Map<Key, IProcessInstanceLinkType> localCache = CollectionUtils.newMap();
   
   public static IProcessInstanceLinkType findById(PredefinedProcessInstanceLinkTypes linkType)
   {
      return findById(linkType.getId());
   }

   public static IProcessInstanceLinkType findById(String id)
   {
      assert id != null && id.trim().length() > 0 : "Id must not be empty.";
      
      short partition = SecurityProperties.getPartitionOid();
      Key key = new Key(id, partition);
      IProcessInstanceLinkType bean = localCache.get(key);
      if (bean == null)
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         QueryExtension extension = QueryExtension.where(
               Predicates.andTerm(
                     Predicates.isEqual(FR__ID, id),
                     Predicates.isEqual(FR__PARTITION, partition)));
         bean = session.findFirst(ProcessInstanceLinkTypeBean.class, extension);
         if (bean == null)
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.ATDB_UNKNOWN_LINK_TYPE_ID.raise(id, partition), id);
         }
         localCache.put(key, bean);
      }
      return bean;
   }

   public static ProcessInstanceLinkTypeBean findByOID(long oid)
      throws ObjectNotFoundException
   {
      assert oid > 0 : "Oid must be greater than 0";

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ProcessInstanceLinkTypeBean result = session.findByOID(ProcessInstanceLinkTypeBean.class, oid);
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_LINK_TYPE_OID.raise(0), 0);
      }
      return result;
   }

   /**
    * Default constructor for persistence management.
    */
   public ProcessInstanceLinkTypeBean()
   {
   }

   /**
    * Creates a new link type for a given partition.
    * 
    * @param id the identifier of the link type.
    * @param description the description of the link type.
    * @param partition the partition in which the link type should be created.
    * @throws NullPointerException if the partition is null
    */
   public ProcessInstanceLinkTypeBean(String id, String description)
   {
      assert id != null && id.trim().length() > 0 : "Id must not be empty.";

      partition = SecurityProperties.getPartitionOid();
      
      String trimmedId = StringUtils.cutString(id, id_COLUMN_LENGTH);
      
      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(ProcessInstanceLinkTypeBean.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__ID, trimmedId),
                  Predicates.isEqual(FR__PARTITION, partition)))))
      {
         throw new PublicException(
               BpmRuntimeError.ATDB_LINK_TYPE_ID_EXISTS.raise(id), id, null);
      }
      
      this.id = trimmedId;
      this.description = StringUtils.cutString(description, description_COLUMN_LENGTH);

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public String getId()
   {
      return id;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      if (!CompareHelper.areEqual(this.description, description))
      {
         markModified(FIELD__DESCRIPTION);
         this.description = description;
      }
   }
   
   public IAuditTrailPartition getPartition()
   {
      return LoginUtils.findPartition(Parameters.instance(), (short) partition);
   }

   public short getPartitionOid()
   {
      return (short) partition;
   }
   
   public String toString()
   {
      return "LinkType: " + id + " [partition: " + partition + "]";
   }
}
