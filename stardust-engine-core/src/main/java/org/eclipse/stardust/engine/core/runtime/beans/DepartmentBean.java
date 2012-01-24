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
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.core.cache.CacheHelper;
import org.eclipse.stardust.engine.core.cache.CacheInputStream;
import org.eclipse.stardust.engine.core.cache.CacheOutputStream;
import org.eclipse.stardust.engine.core.cache.Cacheable;
import org.eclipse.stardust.engine.core.cache.DepartmentsCache;
import org.eclipse.stardust.engine.core.persistence.ClosableIterator;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.Loader;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;



/**
 * @author Florin.Herinean
 */
public class DepartmentBean extends IdentifiablePersistentBean implements IDepartment, Cacheable
{
   private static final long serialVersionUID = 1L;

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__ID = "id";
   public static final String FIELD__NAME = "name";
   public static final String FIELD__PARTITION = "partition";
   public static final String FIELD__PARENTDEPARTMENT = "parentDepartment";
   public static final String FIELD__DESCRIPTION = "description";
   public static final String FIELD__ORGANIZATION = "organization";

   public static final FieldRef FR__OID = new FieldRef(DepartmentBean.class, FIELD__OID);
   public static final FieldRef FR__ID = new FieldRef(DepartmentBean.class, FIELD__ID);
   public static final FieldRef FR__NAME = new FieldRef(DepartmentBean.class, FIELD__NAME);
   public static final FieldRef FR__PARTITION = new FieldRef(DepartmentBean.class, FIELD__PARTITION);
   public static final FieldRef FR__PARENTDEPARTMENT = new FieldRef(DepartmentBean.class, FIELD__PARENTDEPARTMENT);
   public static final FieldRef FR__DESCRIPTION = new FieldRef(DepartmentBean.class, FIELD__DESCRIPTION);
   public static final FieldRef FR__ORGANIZATION = new FieldRef(DepartmentBean.class, FIELD__ORGANIZATION);

   public static final String TABLE_NAME = "department";
   public static final String DEFAULT_ALIAS = "dptm";
   public static final String PK_FIELD = FIELD__OID;
   protected static final String PK_SEQUENCE = "department_seq";
   public static final String[] department_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] department_idx2_UNIQUE_INDEX = new String[] {FIELD__ID, FIELD__ORGANIZATION, FIELD__PARENTDEPARTMENT};
   protected static final Class<? extends Loader> LOADER = DepartmentLoader.class;
   
   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private static final int name_COLUMN_LENGTH = 150;
   private String name;
   private static final int description_COLUMN_LENGTH = 4000;
   private String description;
   private long organization;
   
   private AuditTrailPartitionBean partition;
   private long parentDepartment;

   public static DepartmentBean findByOID(long oid)
         throws ObjectNotFoundException
   {
      if (oid == Department.DEFAULT.getOID())
      {
         return null;
      }
      
      DepartmentBean result = (DepartmentBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findByOID(DepartmentBean.class, oid);
      
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_DEPARTMENT_OID.raise(oid), oid);
      }
      
      return result;
   }

   public static DepartmentBean findById(String id, IDepartment parentDepartment, IOrganization org)
         throws ObjectNotFoundException
   {
      long parentDepartmentOid = parentDepartment == null ? 0 : parentDepartment.getOID();
      
      org = DepartmentUtils.getFirstScopedOrganization(org);
      if (id != null && org != null)
      {
         short partitionOid = SecurityProperties.getPartitionOid();

         long runtimeOrganizationOid = ModelManagerFactory.getCurrent().getRuntimeOid(org);
         
         if (CacheHelper.isCacheable(DepartmentBean.class))
         {
            DepartmentBean result = DepartmentsCache.instance().findById(id, parentDepartmentOid, runtimeOrganizationOid, partitionOid);
            if (result != null)
            {
               return result;
            }
         }
         
         PredicateTerm partitionPredicate = Predicates.isEqual(FR__PARTITION, partitionOid);
   
         String trimmedId = StringUtils.cutString(id, id_COLUMN_LENGTH);
         PredicateTerm idPredicate = Predicates.isEqual(FR__ID, trimmedId);
   
         PredicateTerm parentDepartmentPredicate = Predicates.isEqual(FR__PARENTDEPARTMENT, parentDepartmentOid);
   
         PredicateTerm orgPredicate = Predicates.isEqual(FR__ORGANIZATION, runtimeOrganizationOid);
   
         QueryExtension queryExtension = QueryExtension.where(Predicates.andTerm(
               idPredicate, parentDepartmentPredicate, orgPredicate, partitionPredicate));
         
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         DepartmentBean result = (DepartmentBean) session.findFirst(DepartmentBean.class, queryExtension);
         if (result != null)
         {
            return result;
         }
      }
      
      throw new ObjectNotFoundException(
            BpmRuntimeError.ATDB_UNKNOWN_DEPARTMENT_ID.raise(id, parentDepartmentOid), id);
   }
   
   public static Iterator<IDepartment> findAllForParent(IDepartment parentDepartment)
   {
      PredicateTerm partitionPredicate = Predicates.isEqual(FR__PARTITION, SecurityProperties.getPartitionOid());

      PredicateTerm parentDepartmentPredicate = Predicates.isEqual(FR__PARENTDEPARTMENT,
            parentDepartment == null ? 0 : parentDepartment.getOID());

      QueryExtension queryExtension = QueryExtension.where(
            Predicates.andTerm(parentDepartmentPredicate, partitionPredicate));

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      return session.<IDepartment,DepartmentBean>getIterator(DepartmentBean.class, queryExtension);
   }

   public static Iterator<IDepartment> findAllForOrganization(long organizationRtOid,
         IDepartment department)
   {
      PredicateTerm partitionPredicate = Predicates.isEqual(FR__PARTITION, SecurityProperties.getPartitionOid());

      PredicateTerm organizationPredicate = Predicates.isEqual(FR__ORGANIZATION, organizationRtOid);
      if (department != null)
      {
         PredicateTerm parentDepartmentPredicate = Predicates.isEqual(FR__PARENTDEPARTMENT, department.getOID());
         organizationPredicate = Predicates.andTerm(organizationPredicate, parentDepartmentPredicate);
      }

      QueryExtension queryExtension = QueryExtension.where(
            Predicates.andTerm(organizationPredicate, partitionPredicate));

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      return session.<IDepartment,DepartmentBean>getIterator(DepartmentBean.class, queryExtension);
   }

   /**
    * Constructor for persistence framework.
    */
   public DepartmentBean()
   {
   }
   
   public DepartmentBean(String id, String name, AuditTrailPartitionBean partition,
         IDepartment parentDepartment, String description, OrganizationInfo organization)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModelParticipant participant = modelManager.findModelParticipant(organization);
      long rtOid = modelManager.getRuntimeOid(participant);
      if (!(participant instanceof IOrganization))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_TYPE.raise("organization", IOrganization.class.getName()));
      }
      if (!((IOrganization) participant).getBooleanAttribute(PredefinedConstants.BINDING_ATT))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_SPEC.raise("organization",
               PredefinedConstants.BINDING_ATT + "=true"));
      }
/*      if (parentDepartment != null && (rtOid == parentDepartment.getRuntimeOrganizationOID()
            || !isParent(modelManager, participant, parentDepartment.getRuntimeOrganizationOID(), new HashSet<IModelParticipant>())))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ORGANIZATION_HIERARCHY.raise());
      }*/
      List<IOrganization> restricted = Authorization2.findRestricted(participant);
      restricted.remove(participant);
      if (parentDepartment == null)
      {
         if (!restricted.isEmpty())
         {
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ORGANIZATION_HIERARCHY.raise());
         }
      }
      else
      {
         if (restricted.isEmpty())
         {
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ORGANIZATION_HIERARCHY.raise());
         }
         IOrganization parentOrg = restricted.get(restricted.size() - 1);
         long parentRtOid = parentDepartment.getRuntimeOrganizationOID();
         if (parentOrg != modelManager.findModelParticipant(participant.getModel().getModelOID(), parentRtOid))
         {
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ORGANIZATION_HIERARCHY.raise());
         }
      }
      
      this.organization = rtOid;

      String trimmedId = StringUtils.cutString(id, id_COLUMN_LENGTH);
      PredicateTerm idPredicate = Predicates.isEqual(FR__ID, trimmedId);
      
      long parentOid = parentDepartment == null ? 0 : parentDepartment.getOID();
      PredicateTerm parentDepartmentPredicate = Predicates.isEqual(
            FR__PARENTDEPARTMENT, parentOid);
      
      PredicateTerm partitionPredicate = Predicates.isEqual(FR__PARTITION, partition.getOID());
      
      PredicateTerm orgPredicate = Predicates.isEqual(FR__ORGANIZATION, rtOid);
      
      QueryExtension whereClause = QueryExtension.where(Predicates.andTerm(
            idPredicate, parentDepartmentPredicate, orgPredicate, partitionPredicate));
      
      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(
            DepartmentBean.class, whereClause))
      {
         throw new InvalidArgumentException(BpmRuntimeError.ATDB_DEPARTMENT_EXISTS.raise(trimmedId));
      }
      
      this.id = trimmedId;
      this.name = StringUtils.cutString(name, name_COLUMN_LENGTH);
      this.partition = partition;
      this.parentDepartment = parentOid;
      this.description = StringUtils.cutString(description, description_COLUMN_LENGTH);
      
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
      
      // add department hierarchy.
      long oid = getOID();
      new DepartmentHierarchyBean(oid, oid);
      if (parentDepartment != null)
      {
         if (CacheHelper.isCacheable(getClass()))
         {
            while (parentDepartment != null)
            {
               new DepartmentHierarchyBean(parentDepartment.getOID(), oid);
               parentDepartment = parentDepartment.getParentDepartment();
            }
         }
         else
         {
            List<Long> superOIDs = DepartmentHierarchyBean.findAllSuperDepartments(parentDepartment.getOID());
            for (int i = 0; i < superOIDs.size(); i++)
            {
               new DepartmentHierarchyBean(superOIDs.get(i), oid);
            }
         }
      }
   }

   /*private boolean isParent(ModelManager modelManager, IModelParticipant participant, long runtimeOrganizationOID,
         HashSet<IModelParticipant> visited)
   {
      long rtOid = modelManager.getRuntimeOid(participant);
      if (rtOid == runtimeOrganizationOID)
      {
         return true;
      }
      if (!visited.contains(participant))
      {
         visited.add(participant);
         Iterator<IOrganization> organizations = participant.getAllOrganizations();
         while (organizations.hasNext())
         {
            if (isParent(modelManager, organizations.next(), runtimeOrganizationOID, visited))
            {
               return true;
            }
         }
      }
      return false;
   }*/

   public String toString()
   {
      return "Department: " + getName();
   }

   public String getId()
   {
      fetch();
      return id;
   }

   public void setId(String id)
   {
      fetch();
      id = StringUtils.cutString(id, id_COLUMN_LENGTH);
      if (!CompareHelper.areEqual(this.id, id))
      {
         // TODO: (fh) check hierarchy for id uniqueness
         markModified(FIELD__ID);
         this.id = id;
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
      name = StringUtils.cutString(name, name_COLUMN_LENGTH);
      if (!CompareHelper.areEqual(this.name, name))
      {
         markModified(FIELD__NAME);
         this.name = name;
      }
   }

   public long getRuntimeOrganizationOID()
   {
      fetch();
      return organization;
   }

   public void setRuntimeOrganizationOID(long organizationOID)
   {
      fetch();
      if (organization != organizationOID)
      {
         markModified(FIELD__ORGANIZATION);
         this.organization = organizationOID;
      }
   }

   public IAuditTrailPartition getPartition()
   {
      fetchLink(FIELD__PARTITION);
      return partition;
   }

   public IDepartment getParentDepartment()
   {
      fetch();
      if (parentDepartment > 0)
      {
         return DepartmentBean.findByOID(parentDepartment);
      }
      return null;
   }
   
   public long getParentDepartmentOID()
   {
      fetch();
      return parentDepartment;
   }
   
   public String getDescription()
   {
      fetch();
      return description;
   }

   public void setDescription(String description)
   {
      fetch();
      description = StringUtils.cutString(description, description_COLUMN_LENGTH);
      if (!CompareHelper.areEqual(this.description, description))
      {
         markModified(FIELD__DESCRIPTION);
         this.description = description;
      }
   }
   
   @Override
   public void delete(boolean writeThrough)
   {
      long oid = getOID();
      
      if (ActivityInstanceBean.existsForDepartment(oid) || ActivityInstanceHistoryBean.existsForDepartment(oid))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_DEPARTMENT_HAS_ACTIVE_ACTIVITY_INSTANCES.raise(oid));
      }
      
      Iterator<IDepartment> children = findAllForParent(this);
      while (children.hasNext())
      {
         children.next().delete(writeThrough);
      }
      if (children instanceof ClosableIterator)
      {
         ((ClosableIterator) children).close();
      }
      UserParticipantLink.deleteAllForDepartment(this);
      
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).delete(
            DepartmentHierarchyBean.class, Predicates.orTerm(//
                  Predicates.isEqual(DepartmentHierarchyBean.FR__SUPERDEPARTMENT, oid),//
                  Predicates.isEqual(DepartmentHierarchyBean.FR__SUBDEPARTMENT, oid)),
            writeThrough);
      
      super.delete(writeThrough);
   }

   public void retrieve(byte[] bytes) throws IOException
   {
      CacheInputStream cis = new CacheInputStream(bytes);
      oid = cis.readLong();
      id = cis.readString();
      name = cis.readString();
      description = cis.readString();
      short partition = cis.readShort();
      if (partition >= 0)
      {
         this.partition = (AuditTrailPartitionBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).
            findByOID(AuditTrailPartitionBean.class, partition);
      }
      parentDepartment = cis.readLong();
      organization = cis.readLong();
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
      
      fetchLink(FIELD__PARTITION);
      cos.writeShort(partition == null ? -1 : partition.getOID());
      cos.writeLong(parentDepartment);
      cos.writeLong(organization);
      cos.flush();
      byte[] bytes = cos.getBytes();
      cos.close();
      return bytes;
   }
}
