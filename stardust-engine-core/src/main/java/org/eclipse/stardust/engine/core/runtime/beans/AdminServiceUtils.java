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

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.persistence.ComparisonTerm;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Joins;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonLog;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;



/**
 * Utility class which contains methods for audit trail cleanup.
 *  
 * @author sborn
 * @version $Revision$
 */
public class AdminServiceUtils
{
   /**
    * Delete all runtime data which depends on model definitions, e.g. process definition, ...
    * 
    * @param modelOid The model OID.
    * @param session The session instance. 
    */
   public static void deleteModelRuntimePart(long modelOid, Session session)
   {
      AuditTrailModelAccessor accessor = new AuditTrailModelAccessor(session);
      
      for (Iterator i = accessor.getAllProcessDefinitions(modelOid); i.hasNext();)
      {
         AuditTrailProcessDefinitionBean pd = (AuditTrailProcessDefinitionBean) i.next();
         final long pdOid = pd.getOID();
         for (Iterator j = accessor.getAllActivities(pd); j.hasNext();)
         {
            AuditTrailActivityBean activity = (AuditTrailActivityBean) j.next();
            // activity_inst_log
            final long activityOid = activity.getOID();
            session.delete(ActivityInstanceLogBean.class, null,
                  new Join(ActivityInstanceBean.class)
                  .on(ActivityInstanceLogBean.FR__ACTIVITY_INSTANCE, ActivityInstanceBean.FIELD__OID)
                  .where(Predicates.andTerm(
                        Predicates.isEqual(ActivityInstanceBean.FR__MODEL, modelOid),
                        Predicates.isEqual(ActivityInstanceBean.FR__ACTIVITY, activityOid))),
                  false);
            // @todo (greece, ub): there could be act_inst_property data in string_data
            // act_inst_history
            session.delete(ActivityInstanceHistoryBean.class, null,
                  new Join(ActivityInstanceBean.class)
                  .on(ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE, ActivityInstanceBean.FIELD__OID)
                  .where(Predicates.andTerm(
                        Predicates.isEqual(ActivityInstanceBean.FR__MODEL, modelOid),
                        Predicates.isEqual(ActivityInstanceBean.FR__ACTIVITY, activityOid))),
                  false);
            // act_inst_property
            session.delete(ActivityInstanceProperty.class, null,
                  new Join(ActivityInstanceBean.class)
                  .on(ActivityInstanceProperty.FR__OBJECT_OID, ActivityInstanceBean.FIELD__OID)
                  .where(Predicates.andTerm(
                        Predicates.isEqual(ActivityInstanceBean.FR__MODEL, modelOid),
                        Predicates.isEqual(ActivityInstanceBean.FR__ACTIVITY, activityOid))),
                  false);
            // event binding
            session.delete(EventBindingBean.class, null,
                  new Join(ActivityInstanceBean.class)
                  .on(EventBindingBean.FR__OBJECT_OID, ActivityInstanceBean.FIELD__OID)
                  .where(Predicates.andTerm(
                        Predicates.isEqual(ActivityInstanceBean.FR__MODEL, modelOid),
                        Predicates.isEqual(ActivityInstanceBean.FR__ACTIVITY, activityOid))),
                  false);
            // log_entry
            session.delete(LogEntryBean.class, null,
                  new Join(ActivityInstanceBean.class)
                  .on(LogEntryBean.FR__ACTIVITY_INSTANCE, ActivityInstanceBean.FIELD__OID)
                  .where(Predicates.andTerm(
                        Predicates.isEqual(ActivityInstanceBean.FR__MODEL, modelOid),
                        Predicates.isEqual(ActivityInstanceBean.FR__ACTIVITY, activityOid))),
                  false);
            // workitem
            session.delete(WorkItemBean.class, null,
                  new Join(ActivityInstanceBean.class)
                  .on(WorkItemBean.FR__ACTIVITY_INSTANCE, ActivityInstanceBean.FIELD__OID)
                  .where(Predicates.andTerm(
                        Predicates.isEqual(ActivityInstanceBean.FR__MODEL, modelOid),
                        Predicates.isEqual(ActivityInstanceBean.FR__ACTIVITY, activityOid))),
                  false);
            // activity_instance
            session.delete(ActivityInstanceBean.class, Predicates.andTerm(
                  Predicates.isEqual(ActivityInstanceBean.FR__MODEL, modelOid),
                  Predicates.isEqual(ActivityInstanceBean.FR__ACTIVITY, activityOid)),
                  false);
         }
         for (Iterator j = accessor.getAllTransitions(pd); j.hasNext();)
         {
            AuditTrailTransitionBean transition = (AuditTrailTransitionBean) j.next();
            // trans_inst
            final long transitionOid = transition.getOID();
            session.delete(TransitionInstanceBean.class, Predicates.andTerm(
                  Predicates.isEqual(TransitionInstanceBean.FR__MODEL, modelOid),
                  Predicates.isEqual(TransitionInstanceBean.FR__TRANSITION, transitionOid)),
                  false);
            // trans_token
            session.delete(TransitionTokenBean.class, Predicates.andTerm(
                  Predicates.isEqual(TransitionTokenBean.FR__MODEL, modelOid),
                  Predicates.isEqual(TransitionTokenBean.FR__TRANSITION, transitionOid)),
                  false);
         }
         // starting trans_inst
         session.delete(TransitionInstanceBean.class, null, //
               new Join(ProcessInstanceBean.class) //
                     .on(TransitionInstanceBean.FR__PROCESS_INSTANCE,
                           ProcessInstanceBean.FIELD__OID)//
                     .where(Predicates.isEqual( //
                           ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid)), false);
         // starting trans_token
         session.delete(TransitionTokenBean.class, null, //
               new Join(ProcessInstanceBean.class) //
                     .on(TransitionTokenBean.FR__PROCESS_INSTANCE,
                           ProcessInstanceBean.FIELD__OID)//
                     .where(Predicates.isEqual( //
                           ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid)), false);
         
         // triggers
         for (Iterator j = accessor.getAllTriggers(pd); j.hasNext();)
         {
            AuditTrailTriggerBean trigger = (AuditTrailTriggerBean) j.next();
            // timer_log
            session.delete(TimerLog.class, Predicates.andTerm(
                  Predicates.isEqual(TimerLog.FR__MODEL, modelOid),
                  Predicates.isEqual(TimerLog.FR__TRIGGER_OID, trigger.getOID())),
                  false);
         }
         // event binding
         session.delete(EventBindingBean.class, null,
               new Join(ProcessInstanceBean.class)
               .on(EventBindingBean.FR__OBJECT_OID, ProcessInstanceBean.FIELD__OID)
               .where(Predicates.andTerm(
                     Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOid),
                     Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid))), false);
         // log_entry
         session.delete(LogEntryBean.class, null,
               new Join(ProcessInstanceBean.class)
               .on(LogEntryBean.FR__PROCESS_INSTANCE, ProcessInstanceBean.FIELD__OID)
               .where(Predicates.andTerm(
                     Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOid),
                     Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid))),
                     false);
         // @todo (paris, ub): there could be proc_inst_property data in string_data
         // proc_inst_property
         session.delete(ProcessInstanceProperty.class, null,
               new Join(ProcessInstanceBean.class)
               .on(ProcessInstanceProperty.FR__OBJECT_OID, ProcessInstanceBean.FIELD__OID)
               .where(Predicates.andTerm(
                     Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOid),
                     Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid))),
                     false);
         
         // from links
         session.delete(ProcessInstanceLinkBean.class, null, new Join(ProcessInstanceBean.class)
               .on(ProcessInstanceLinkBean.FR__PROCESS_INSTANCE, ProcessInstanceBean.FIELD__OID)
               .where(Predicates.andTerm(
                     Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOid),
                     Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid))),
                     false);
         
         // to links
         session.delete(ProcessInstanceLinkBean.class, null, new Join(ProcessInstanceBean.class)
               .on(ProcessInstanceLinkBean.FR__LINKED_PROCESS_INSTANCE, ProcessInstanceBean.FIELD__OID)
               .where(Predicates.andTerm(
                     Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOid),
                     Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid))),
                     false);

         // procinst_hierarchy
         QueryDescriptor piDescriptor = QueryDescriptor //
               .from(ProcessInstanceBean.class) //
               .select(ProcessInstanceBean.FR__OID) //
               .where(
                     Predicates.andTerm(
                           //
                           Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOid),
                           Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION,
                                 pdOid)));
         ComparisonTerm predicate = Predicates.inList( //
               ProcessInstanceHierarchyBean.FR__SUB_PROCESS_INSTANCE, piDescriptor);
         session.delete(ProcessInstanceHierarchyBean.class, predicate, false);
         
         predicate = Predicates.inList( //
               ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, piDescriptor);
         session.delete(ProcessInstanceHierarchyBean.class, predicate, false);
         
         // procinst_scope
         predicate = Predicates.inList( //
               ProcessInstanceScopeBean.FR__PROCESS_INSTANCE, piDescriptor);
         session.delete(ProcessInstanceScopeBean.class, predicate, false);
         
         // deleting rows from data clusters
         ResultIterator piIterator = session.getIterator(ProcessInstanceBean.class, QueryExtension.where(Predicates.andTerm(
               Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOid),
               Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid))));
         List piOids = new ArrayList();
         for (Iterator iterator = piIterator; iterator.hasNext();)
         {
            IProcessInstance pi = (IProcessInstance) iterator.next();
            piOids.add(new Long(pi.getOID()));
         }
         ProcessInstanceUtils.deleteDataClusterValues(piOids, session);
         
         // process_instance
         session.delete(ProcessInstanceBean.class, Predicates.andTerm(
               Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOid),
               Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, pdOid)),
               false);
      }
      IModel model = ModelManagerFactory.getCurrent().findModel(modelOid);
      for (Iterator i = accessor.getAllData(modelOid); i.hasNext();)
      {
         AuditTrailDataBean data = (AuditTrailDataBean) i.next();
         final long dataOid = data.getOID();
         
         IData iData = model.findData(data.getId());
         // TODO (ab) SPI for different data types 
         if (StructuredTypeRtUtils.isStructuredType(iData.getType().getId()) || 
               StructuredTypeRtUtils.isDmsType(iData.getType().getId())) 
         {
            // string_data
            Joins joins = new Joins();
            joins.add(new Join(StructuredDataValueBean.class).on(LargeStringHolder.FR__OBJECTID,
                  StructuredDataValueBean.FIELD__OID).where(
                  Predicates.isEqual(LargeStringHolder.FR__DATA_TYPE, StructuredDataValueBean.TABLE_NAME)));
            joins.add(new Join(StructuredDataBean.class).on(
                  StructuredDataValueBean.FR__XPATH, StructuredDataBean.FIELD__OID)
                  .where(Predicates.andTerm(Predicates.isEqual(StructuredDataBean.FR__MODEL, modelOid), 
                         Predicates.isEqual(StructuredDataBean.FR__DATA, dataOid))));
            session.delete(LargeStringHolder.class, null, joins, false);
            
            // structured_data_value
            session.delete(StructuredDataValueBean.class, null,
                  new Join(StructuredDataBean.class)
                  .on(StructuredDataValueBean.FR__XPATH, StructuredDataBean.FIELD__OID)
                  .where(Predicates.andTerm(
                        Predicates.isEqual(StructuredDataBean.FR__MODEL, modelOid),
                        Predicates.isEqual(StructuredDataBean.FR__DATA, dataOid))),
                        false);
            
            // huge_string_data
            session.delete(ClobDataBean.class, null,
                  new Join(DataValueBean.class)
                  .on(ClobDataBean.FR__OID, DataValueBean.FIELD__NUMBER_VALUE)
                  .where(Predicates.andTerm(
                        Predicates.isEqual(DataValueBean.FR__MODEL, modelOid),
                        Predicates.isEqual(DataValueBean.FR__DATA, dataOid))),
                        false);
         }
         
         // string_data
         session.delete(LargeStringHolder.class, null,
               new Join(DataValueBean.class)
               .on(LargeStringHolder.FR__OBJECTID, DataValueBean.FIELD__OID)
               .where(Predicates.andTerm(
                     Predicates.isEqual(DataValueBean.FR__MODEL, modelOid),
                     Predicates.isEqual(DataValueBean.FR__DATA, dataOid),
                     Predicates.isEqual(LargeStringHolder.FR__DATA_TYPE, DataValueBean.TABLE_NAME))),
                     false);
         
         // data_value
         session.delete(DataValueBean.class, Predicates.andTerm(
               Predicates.isEqual(DataValueBean.FR__MODEL, modelOid),
               Predicates.isEqual(DataValueBean.FR__DATA, dataOid)), false);
      }
      //model references
      ModelRefBean.deleteForModel(modelOid, session);
   }

   /**
    * Delete all model definition data, e.g. process definition, ...
    * 
    * @param modelOid The model OID.
    * @param session The session instance. 
    */
   public static void deleteModelModelingPart(long modelOid, Session session)
   {
      AuditTrailModelAccessor accessor = new AuditTrailModelAccessor(session);
      
      for (Iterator i = accessor.getAllProcessDefinitions(modelOid); i.hasNext();)
      {
         AuditTrailProcessDefinitionBean pd = (AuditTrailProcessDefinitionBean) i.next();
         
         for (Iterator j = accessor.getAllTransitions(pd); j.hasNext();)
         {
            AuditTrailTransitionBean transition = (AuditTrailTransitionBean) j.next();
            // transition
            session.delete(AuditTrailTransitionBean.class, Predicates.andTerm(
                  Predicates.isEqual(AuditTrailTransitionBean.FR__MODEL, modelOid),
                  Predicates.isEqual(AuditTrailTransitionBean.FR__OID, transition.getOID())), false);
         }
         // event binding
         final long pdOid = pd.getOID();
         // activity
         session.delete(AuditTrailActivityBean.class, Predicates.andTerm(
               Predicates.isEqual(AuditTrailActivityBean.FR__MODEL, modelOid),
               Predicates.isEqual(AuditTrailActivityBean.FR__PROCESS_DEFINITION, pdOid)),
               false);
         // event handlers
         session.delete(AuditTrailEventHandlerBean.class, Predicates.andTerm(
               Predicates.isEqual(AuditTrailEventHandlerBean.FR__MODEL, modelOid),
               Predicates.isEqual(AuditTrailEventHandlerBean.FR__PROCESS_DEFINITION, pdOid)),
               false);
         // event triggers
         session.delete(AuditTrailTriggerBean.class, Predicates.andTerm(
               Predicates.isEqual(AuditTrailTriggerBean.FR__MODEL, modelOid),
               Predicates.isEqual(AuditTrailTriggerBean.FR__PROCESS_DEFINITION, pdOid)),
               false);
         // process_definition
         session.delete(AuditTrailProcessDefinitionBean.class, Predicates.andTerm(
               Predicates.isEqual(AuditTrailProcessDefinitionBean.FR__MODEL, modelOid),
               Predicates.isEqual(AuditTrailProcessDefinitionBean.FR__OID, pdOid)),
               false);
      }

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
            session.delete(ClobDataBean.class, Predicates.andTerm(Predicates.isEqual(
                  ClobDataBean.FR__OWNER_ID, structuredDataBean.getOID()),
                  Predicates.isEqual(ClobDataBean.FR__OWNER_TYPE,
                        StructuredDataBean.TABLE_NAME)), false);
         }
      }

      for (Iterator i = accessor.getAllData(modelOid); i.hasNext();)
      {
         AuditTrailDataBean data = (AuditTrailDataBean) i.next();
         // data
         session.delete(AuditTrailDataBean.class, Predicates.andTerm(
               Predicates.isEqual(AuditTrailDataBean.FR__MODEL, modelOid),
               Predicates.isEqual(AuditTrailDataBean.FR__OID, data.getOID())), false);
         // xpath
         session.delete(StructuredDataBean.class, Predicates.andTerm(
               Predicates.isEqual(StructuredDataBean.FR__MODEL, modelOid),
               Predicates.isEqual(StructuredDataBean.FR__DATA, data.getOID())), false);
      }
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
            session.delete(UserParticipantLink.class, Predicates.andTerm(
                  Predicates.isEqual(participantJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), modelOid),
                  Predicates.isEqual(UserParticipantLink.FR__PARTICIPANT, participantOid)),
                  participantJoin,
                  false);
         }
         
         // participant
         session.delete(AuditTrailParticipantBean.class, Predicates.andTerm(
               Predicates.isEqual(AuditTrailParticipantBean.FR__MODEL, modelOid),
               Predicates.isEqual(AuditTrailParticipantBean.FR__OID, participantOid)),
               false);
      }
      
      // unused departments
      short partitionOid = ModelPersistorBean.findByModelOID(modelOid).getPartition()
            .getOID();
      List<Long> inValidDepartmentOids = new ArrayList<Long>();
      Map<Long, Set<Long>> participantModelMap = new HashMap<Long, Set<Long>>();
      // collect all used participants in the current partition and memorize the model oid
      Iterator pIter = AuditTrailParticipantBean.findAll(partitionOid);
      while(pIter.hasNext())
      {
         AuditTrailParticipantBean pBean = (AuditTrailParticipantBean) pIter.next();
         Set<Long> modelOids = participantModelMap.get(pBean.getOID());
         if(modelOids == null)
         {
            modelOids = new HashSet<Long>();
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
         session.delete(DepartmentBean.class, Predicates.andTerm(Predicates.inList(
               DepartmentBean.FR__OID, inValidDepartmentOids), Predicates.isEqual(
               DepartmentBean.FR__PARTITION, partitionOid)), false);
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

      public Iterator getAllProcessDefinitions(long modelOid)
      {
         return session.getIterator(AuditTrailProcessDefinitionBean.class, // 
               QueryExtension.where(Predicates.isEqual(
                     AuditTrailProcessDefinitionBean.FR__MODEL, modelOid)));
      }
      
      public Iterator getAllActivities(AuditTrailProcessDefinitionBean pd)
      {
         return session.getIterator(AuditTrailActivityBean.class, // 
               QueryExtension.where(Predicates.andTerm(//
                     Predicates.isEqual(AuditTrailActivityBean.FR__MODEL, pd.getModel()),
                     Predicates.isEqual(AuditTrailActivityBean.FR__PROCESS_DEFINITION, pd
                           .getOID()))));
      }
      
      public Iterator getAllTransitions(AuditTrailProcessDefinitionBean pd)
      {
         return session.getIterator(AuditTrailTransitionBean.class, // 
               QueryExtension.where(Predicates.andTerm(//
                     Predicates.isEqual(AuditTrailTransitionBean.FR__MODEL, pd.getModel()),
                     Predicates.isEqual(AuditTrailTransitionBean.FR__PROCESS_DEFINITION, pd
                           .getOID()))));
      }
      
      public Iterator getAllTriggers(AuditTrailProcessDefinitionBean pd)
      {
         return session.getIterator(AuditTrailTriggerBean.class, // 
               QueryExtension.where(Predicates.andTerm(//
                     Predicates.isEqual(AuditTrailTriggerBean.FR__MODEL, pd.getModel()),
                     Predicates.isEqual(AuditTrailTriggerBean.FR__PROCESS_DEFINITION, pd
                           .getOID()))));
      }
      
      public Iterator getAllData(long modelOid)
      {
         return session.getIterator(AuditTrailDataBean.class, // 
               QueryExtension.where(Predicates.isEqual( //
                     AuditTrailDataBean.FR__MODEL, modelOid)));
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
}
