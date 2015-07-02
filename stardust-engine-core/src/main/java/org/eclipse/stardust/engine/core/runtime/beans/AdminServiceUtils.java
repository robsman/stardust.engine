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

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.inList;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.persistence.ComparisonTerm;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Joins;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonLog;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;

/**
 * Utility class which contains methods for audit trail cleanup.
 *
 * @author sborn
 * @version $Revision$
 */
public class AdminServiceUtils
{
   /**
    * Delete all model definition data, e.g. process definition, ...
    *
    * @param modelOid The model OID.
    * @param session The session instance.
    */
   public static void deleteModelModelingPart(long modelOid, Session session)
   {
      AuditTrailModelAccessor accessor = new AuditTrailModelAccessor(session);

      // transition
      session.delete(AuditTrailTransitionBean.class,
            isEqual(AuditTrailTransitionBean.FR__MODEL, modelOid), false);
      // event binding
      // activity
      session.delete(AuditTrailActivityBean.class,
            isEqual(AuditTrailActivityBean.FR__MODEL, modelOid),
            false);
      // event handlers
      session.delete(AuditTrailEventHandlerBean.class,
            isEqual(AuditTrailEventHandlerBean.FR__MODEL, modelOid),
            false);
      // event triggers
      session.delete(AuditTrailTriggerBean.class,
            isEqual(AuditTrailTriggerBean.FR__MODEL, modelOid),
            false);
      // process_definition
      session.delete(AuditTrailProcessDefinitionBean.class,
            isEqual(AuditTrailProcessDefinitionBean.FR__MODEL, modelOid),
            false);

      // delete xpath overflows from clob_data, but only if no other models reference
      // this overflow, since the overflow of equal xpaths can be referenced from
      // different models
      for (Iterator s = accessor.getAllStructuredData(modelOid); s.hasNext();)
      {
         StructuredDataBean structuredDataBean = (StructuredDataBean) s.next();
         if (structuredDataBean.hasOverflow()
               && !isOverflowReferencedByOtherModels(structuredDataBean.getOID(),
                     modelOid, session, accessor))
         {
            // TODO perform delete in batches
            session.delete(ClobDataBean.class, andTerm(
                  isEqual(ClobDataBean.FR__OWNER_ID, structuredDataBean.getOID()),
                  isEqual(ClobDataBean.FR__OWNER_TYPE, StructuredDataBean.TABLE_NAME)),
                  false);
         }
      }

      // struct data xpaths
      session.delete(StructuredDataBean.class,
            isEqual(StructuredDataBean.FR__MODEL, modelOid),
            false);
      // data
      session.delete(AuditTrailDataBean.class,
            isEqual(AuditTrailDataBean.FR__MODEL, modelOid),
            false);

      // participant
      for (Iterator i = accessor.getAllParticipants(modelOid); i.hasNext();)
      {
         AuditTrailParticipantBean participant = (AuditTrailParticipantBean) i.next();

         // user_participant
         final long participantOid = participant.getOID();
         long grantsCount = UserParticipantLink.countAllFor(participantOid, modelOid);
         if (grantsCount > 0 && UserParticipantLink.countAllFor(participantOid, 0) == grantsCount)
         {
            Join participantJoin = new Join(AuditTrailParticipantBean.class, "p")
               .on(UserParticipantLink.FR__PARTICIPANT, AuditTrailParticipantBean.FIELD__OID);
            session.delete(UserParticipantLink.class, andTerm(
                  isEqual(participantJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), modelOid),
                  isEqual(UserParticipantLink.FR__PARTICIPANT, participantOid)),
                  participantJoin,
                  false);
         }
      }
      session.delete(AuditTrailParticipantBean.class,
            isEqual(AuditTrailParticipantBean.FR__MODEL, modelOid),
            false);

      // unused departments
      short partitionOid = ModelPersistorBean.findByModelOID(modelOid).getPartition()
            .getOID();
      List<Long> inValidDepartmentOids = newArrayList();
      Map<Long, Set<Long>> participantModelMap = newHashMap();
      // collect all used participants in the current partition and memorize the model oid
      Iterator pIter = AuditTrailParticipantBean.findAll(partitionOid);
      while(pIter.hasNext())
      {
         AuditTrailParticipantBean pBean = (AuditTrailParticipantBean) pIter.next();
         Set<Long> modelOids = participantModelMap.get(pBean.getOID());
         if(modelOids == null)
         {
            modelOids = newHashSet();
            participantModelMap.put(pBean.getOID(), modelOids);
         }
         if(pBean.getModel() != modelOid) // only add model if it is not the current one
         {
            modelOids.add(pBean.getModel());
         }
      }
      // iterate over all departments which belongs to the partition
      for (Iterator<IDepartment> iterator = accessor.getAllDepartments(partitionOid); iterator
            .hasNext();)
      {
         IDepartment department = (IDepartment) iterator.next();
         Set<Long> modelOids = participantModelMap.get(department.getRuntimeOrganizationOID());
         if (modelOids == null || modelOids.isEmpty()) // no other model is using this department
         {
            inValidDepartmentOids.add(department.getOID());
         }
      }

      if (!inValidDepartmentOids.isEmpty())
      {
         session.delete(DepartmentBean.class, andTerm(
               inList(DepartmentBean.FR__OID, inValidDepartmentOids),
               isEqual(DepartmentBean.FR__PARTITION, partitionOid)),
               false);
      }

   }

   private static boolean isOverflowReferencedByOtherModels(long structuredDataOid, long thisModelOid, Session session, AuditTrailModelAccessor accessor)
   {
      // iterate over all not deleted other models and look if they reference this xpath
      List referencingModelOids = accessor.getAllReferencingModelOidsForOwnerId(structuredDataOid);
      for (int i=0; i<referencingModelOids.size(); i++)
      {
         Long referencingModelOid = (Long)referencingModelOids.get(i);
         if (referencingModelOid.longValue() != thisModelOid)
         {
            // there is an other model referencing, xpath should be kept
            return true;
         }
      }
      return false;
   }

   /**
    * Delete all runtime data which does not depend on a model but partition, e.g. properties, users ...
    *
    * @param keepUsers If true then deletion of users and dependent data is prevented.
    * @param keepLoginUser If true then deletion of user, given by loginUserOid, and dependent data is prevented.
    * @param session The session instance.
    * @param loginUserOid The Oid of the user currently logged in. Will be ignored when keepLoginUser is set to false.
    * @param partitionOid The Oid of the partition.
    */
   public static void deleteModelIndependentRuntimeData(boolean keepUsers,
         boolean keepLoginUser, Session session, long loginUserOid, long partitionOid)
   {
      AuditTrailModelAccessor accessor = new AuditTrailModelAccessor(session);
      // TODO (kafka): deletion of UserDomainUserBean has to be implemented.
      if ( !keepUsers)
      {
         PredicateTerm isNotLoginUser = null;
         if (keepLoginUser)
         {
            isNotLoginUser = Predicates.notEqual(UserBean.FR__OID, loginUserOid);
         }

         // PreferencesBean User Preferences

         Join prfJoinUser = new Join(UserBean.class).on(PreferencesBean.FR__OWNER_ID,
               UserBean.FIELD__OID).where(//
               Predicates.isEqual(PreferencesBean.FR__OWNER_TYPE,
                     PreferenceScope.USER.name()));

         Join urJoin = new Join(UserRealmBean.class) //
         .andOn(UserBean.FR__REALM, UserRealmBean.FIELD__OID)
               //
               .andWhere(Predicates.isEqual(UserRealmBean.FR__PARTITION, partitionOid));

         session.delete(PreferencesBean.class, null, new Joins().add(prfJoinUser).add(urJoin)//
               , false);

         // PreferencesBean Realm Preferences

         Joins joinsRealm = new Joins();
         Join prfJoinRealm = new Join(UserRealmBean.class).on(PreferencesBean.FR__OWNER_ID,
               UserRealmBean.FIELD__OID).where(//
               Predicates.andTerm(Predicates.isEqual(PreferencesBean.FR__OWNER_TYPE,
                     PreferenceScope.REALM.name()),//
                     Predicates.isEqual(UserRealmBean.FR__PARTITION, partitionOid)//
               ));
         joinsRealm.add(prfJoinRealm);

         session.delete(PreferencesBean.class, null, joinsRealm//
               , false);

         // UserParticipantLink
         Join uplJoin = new Join(UserBean.class) //
               .andOn(UserParticipantLink.FR__USER, UserBean.FIELD__OID);

         if (null != isNotLoginUser)
         {
            uplJoin.andWhere(isNotLoginUser);
         }

         Join uJoin = new Join(UserRealmBean.class) //
               .andOn(UserBean.FR__REALM, UserRealmBean.FIELD__OID)//
               .andWhere(Predicates.isEqual(UserRealmBean.FR__PARTITION, partitionOid));

         session.delete(UserParticipantLink.class, null, //
               new Joins().add(uplJoin).add(uJoin), false);

         // UserSession
         Join usJoin = new Join(UserBean.class) //
               .andOn(UserSessionBean.FR__USER, UserBean.FIELD__OID);

         if (null != isNotLoginUser)
         {
            usJoin.andWhere(isNotLoginUser);
         }

         session.delete(UserSessionBean.class, null, //
               new Joins().add(usJoin).add(uJoin), false);

         // UserBean
         Join join = new Join(UserRealmBean.class) //
               .andOn(UserBean.FR__REALM, UserRealmBean.FIELD__OID)//
               .andWhere(Predicates.isEqual(UserRealmBean.FR__PARTITION, partitionOid));

         session.delete(UserBean.class, isNotLoginUser, join, false);

         // UserRealmBean
         PredicateTerm predicate = Predicates.isEqual(UserRealmBean.FR__PARTITION,
               partitionOid);
         if (keepLoginUser)
         {
            IUserRealm userRealm = SecurityProperties.getUserRealm();
            predicate = Predicates.andTerm(//
                  Predicates.notEqual(UserRealmBean.FR__OID, userRealm.getOID()),//
                  predicate);
         }

         session.delete(UserRealmBean.class, predicate, false);

         Set<Long> departmentOidsToKeepSet = new HashSet<Long>();
         Iterator<UserParticipantLink> userParticipants = accessor
               .getAllUserParticipants(loginUserOid);
         while (userParticipants.hasNext())
         {
            UserParticipantLink userParticipant = userParticipants.next();
            IDepartment department = userParticipant.getDepartment();
            if (department != null && department.getOID() != 0)
            {
               getAllParentDepartmentOids(department, departmentOidsToKeepSet);
            }
         }
         List<Long> departmentOidsToKeep = new ArrayList<Long>(departmentOidsToKeepSet);

         PredicateTerm depAndTerm = departmentOidsToKeep.isEmpty() ? Predicates.isEqual(
               DepartmentBean.FR__PARTITION, partitionOid) : Predicates.andTerm(
               Predicates.notInList(DepartmentBean.FR__OID, departmentOidsToKeep),
               Predicates.isEqual(DepartmentBean.FR__PARTITION, partitionOid));
         // Department Hierarchy
         Join dJoin = new Join(DepartmentBean.class) //
               .andOn(DepartmentHierarchyBean.FR__SUBDEPARTMENT,
                     DepartmentBean.FIELD__OID).andWhere(
                     depAndTerm);
         session.delete(DepartmentHierarchyBean.class, null, dJoin, false);

         // Department
         session.delete(DepartmentBean.class, depAndTerm, false);

      }

      // UserUserGroupLink
      Join uugJoin = new Join(UserGroupBean.class) //
            .andOn(UserUserGroupLink.FR__USER_GROUP, UserGroupBean.FIELD__OID)//
            .andWhere(Predicates.isEqual(UserGroupBean.FR__PARTITION, partitionOid));

      session.delete(UserUserGroupLink.class, null, uugJoin, false);

      // UserGroupBean
      session.delete(UserGroupBean.class, Predicates.isEqual(UserGroupBean.FR__PARTITION,
            partitionOid), false);

      // UserDomainBean
      PredicateTerm partitionTerm = Predicates.andTerm( //
            Predicates.isEqual(UserDomainBean.FR__PARTITION, partitionOid),//
            Predicates.isNotNull(UserDomainBean.FR__SUPERDOMAIN));

      Join join = new Join(UserDomainBean.class)//
            .andOn(UserDomainHierarchyBean.FR__SUPERDOMAIN, UserDomainBean.FIELD__OID)//
            .andWhere(partitionTerm);
      session.delete(UserDomainHierarchyBean.class, null, join, false);

      join = new Join(UserDomainBean.class)//
            .andOn(UserDomainHierarchyBean.FR__SUBDOMAIN, UserDomainBean.FIELD__OID)//
            .andWhere(partitionTerm);
      session.delete(UserDomainHierarchyBean.class, null, join, false);

      session.delete(UserDomainBean.class, partitionTerm, false);

      // DaemonLog
      session.delete(DaemonLog.class, Predicates.isEqual(DaemonLog.FR__PARTITION,
            partitionOid), false);

      // LogEntry
      session.delete(LogEntryBean.class, Predicates.isEqual(LogEntryBean.FR__PARTITION,
            partitionOid), false);

      // SignalMessage (incl. lookup table)
      session.delete(SignalMessageBean.class, Predicates.isEqual(SignalMessageBean.FR__PARTITION_OID,
            partitionOid), false);
      session.delete(SignalMessageLookupBean.class, Predicates.isEqual(SignalMessageLookupBean.FR__PARTITION_OID,
            partitionOid), false);

      // PropertyPersistor
      // save the properties of interest
      // TODO (kafka): Do property deletion and preservation by partition
      List propOids = new ArrayList();
      PropertyPersistor prop = PropertyPersistor.findByName(Constants.CARNOT_VERSION);
      propOids.add(new Long(prop.getOID()));

      prop = PropertyPersistor.findByName(Constants.SYSOP_PASSWORD);
      propOids.add(new Long(prop.getOID()));

      prop = PropertyPersistor
            .findByName(RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION);
      if (null != prop)
      {
         propOids.add(new Long(prop.getOID()));
      }

      if (keepUsers || keepLoginUser)
      {
         prop = PropertyPersistor.findByName(SecurityUtils.PASSWORD_RULES, partitionOid);
         if (null != prop)
         {
            propOids.add(new Long(prop.getOID()));
         }
      }

      final long[] partitions = new long[] { -1, partitionOid };
      final ComparisonTerm partitionPredicate = Predicates.inList(
            PropertyPersistor.FR__PARTITION, partitions);

      // delete all CLOBs which are bound to properties that will be deleted in next step
      session.delete(ClobDataBean.class, //
            Predicates.andTerm( //
                  Predicates.notInList(ClobDataBean.FR__OWNER_ID, propOids), //
                  Predicates.isEqual(ClobDataBean.FR__OWNER_TYPE,
                        PropertyPersistor.TABLE_NAME)), //
            new Join(PropertyPersistor.class) //
                  .on(ClobDataBean.FR__OWNER_ID, PropertyPersistor.FIELD__OID) //
                  .where(partitionPredicate), false);

      // only remove global properties(is that really intended??) and
      // properties for selected partition
      session.delete(PropertyPersistor.class, Predicates.andTerm( //
            Predicates.notInList(PropertyPersistor.FR__OID, propOids), //
            partitionPredicate), false);
      
      AdminServiceUtils.deletePartitionRuntimeArtifacts((short) partitionOid, session);      
   }

   private static void getAllParentDepartmentOids(IDepartment department,
         Set<Long> departmentOidsToKeep)
   {
      departmentOidsToKeep.add(department.getOID());
      IDepartment parentDepartment = department.getParentDepartment();
      if (parentDepartment != null)
      {
         departmentOidsToKeep.add(parentDepartment.getOID());
         if (parentDepartment.getParentDepartment() != null)
         {
            getAllParentDepartmentOids(parentDepartment.getParentDepartment(),
                  departmentOidsToKeep);
         }
      }
   }

   private AdminServiceUtils()
   {
      // utility class
   }

   private static class AuditTrailModelAccessor
   {
      private org.eclipse.stardust.engine.core.persistence.Session session;

      public AuditTrailModelAccessor(org.eclipse.stardust.engine.core.persistence.Session session)
      {
         this.session = session;
      }

      public Iterator getAllStructuredData(long modelOid)
      {
         return session.getIterator(StructuredDataBean.class,
               QueryExtension.where(Predicates.isEqual(StructuredDataBean.FR__MODEL,
                     modelOid)));
      }

      public List getAllReferencingModelOidsForOwnerId(long structuredDataOid)
      {
         QueryExtension queryExtension = new QueryExtension();
         queryExtension.setWhere(Predicates.isEqual(StructuredDataBean.FR__OID,
               structuredDataOid));
         Join join = new Join(ClobDataBean.class).on(StructuredDataBean.FR__OID,
               ClobDataBean.FIELD__OWNER_ID).where(
               Predicates.isEqual(ClobDataBean.FR__OWNER_TYPE,
                     StructuredDataBean.TABLE_NAME));
         queryExtension.addJoin(join);

         List modelOids = CollectionUtils.newList();
         for (Iterator i = session.getIterator(StructuredDataBean.class, queryExtension); i.hasNext();)
         {
            StructuredDataBean structuredDataBean = (StructuredDataBean) i.next();
            // check if the model is not deleted in the current session
            if (null != session.findByOID(ModelPersistorBean.class,
                  structuredDataBean.getModel()))
            {
               modelOids.add(new Long(structuredDataBean.getModel()));
            }
         }
         return modelOids;
      }

      public Iterator getAllParticipants(long modelOid)
      {
         return session.getIterator(AuditTrailParticipantBean.class, //
               QueryExtension.where(Predicates.isEqual( //
                     AuditTrailParticipantBean.FR__MODEL, modelOid)));
      }

      public Iterator<IDepartment> getAllDepartments(short partitionOid)
      {
         return session.<IDepartment,DepartmentBean>getIterator(DepartmentBean.class,
               QueryExtension.where(Predicates.isEqual(DepartmentBean.FR__PARTITION, partitionOid)));
      }

      public Iterator<UserParticipantLink> getAllUserParticipants(long userOid)
      {
         return session.getIterator(UserParticipantLink.class, QueryExtension
               .where(Predicates.isEqual(UserParticipantLink.FR__USER, userOid)));
      }
   }

   public static void deletePartitionPreferences(short partitionOid, Session session)
   {
      // PreferencesBean Partition Preferences
      Join prfJoinPartition = new Join(AuditTrailPartitionBean.class).on(
            PreferencesBean.FR__OWNER_ID, AuditTrailPartitionBean.FIELD__OID).where(//
            Predicates.andTerm(Predicates.isEqual(PreferencesBean.FR__OWNER_TYPE,
                  PreferenceScope.PARTITION.name()),//
                  Predicates.isEqual(AuditTrailPartitionBean.FR__OID, partitionOid)//
            ));

      session.delete(PreferencesBean.class, null, prfJoinPartition//
            , false);
   }

   public static void deletePartitionRuntimeArtifacts(short partitionOid, Session session)
   {
      // PreferencesBean Partition Preferences
      PredicateTerm partitionArtifacts = Predicates.isEqual(RuntimeArtifactBean.FR__PARTITION, partitionOid);

      session.delete(RuntimeArtifactBean.class, partitionArtifacts, false);
   }
}
