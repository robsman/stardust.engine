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
package org.eclipse.stardust.engine.api.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.model.IScopedModelParticipant;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.model.beans.ScopedModelParticipant;
import org.eclipse.stardust.engine.core.persistence.AndTerm;
import org.eclipse.stardust.engine.core.persistence.ComparisonTerm;
import org.eclipse.stardust.engine.core.persistence.FetchPredicate;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserGroupBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserParticipantLink;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.AbstractAuthorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public class QueryUtils
{
   private static final Logger trace = LogManager.getLogger(QueryUtils.class);

   public static void addModelVersionPredicate(AndTerm andTerm, FieldRef modelFieldRef,
         Query query, ModelManager modelManager)
   {
      if (isRestrictedToActiveModel(query))
      {
         IModel activeModel = modelManager.findActiveModel();
         if (null != activeModel)
         {
            andTerm.add(Predicates.isEqual(modelFieldRef, activeModel.getModelOID()));
         }
         else
         {
            trace.warn("ModelVersionPolicy[restricted to active model] is used, but "
                  + "no model is active.");
            andTerm.add(Predicates.isNull(modelFieldRef));
         }
      }
   }

   public static Set findProcessClosure(Set processInstanceOIDs,
         EvaluationContext context)
   {
      if ((null == processInstanceOIDs) || processInstanceOIDs.isEmpty())
      {
         return null;
      }

      final boolean USE_PREPARED_STATEMENTS = Parameters.instance().getBoolean(
            "workflow.query.preparedStatements", false);
      final int QUERY_BATCH_SIZE = USE_PREPARED_STATEMENTS
            ? 1
            : Parameters.instance().getInteger("workflow.query.batch.size", 400);

      QueryDescriptor query = QueryDescriptor
            .from(ProcessInstanceBean.class)
            .select(ProcessInstanceBean.FIELD__OID);
      ComparisonTerm piOidTerm = Predicates
            .inList(query.fieldRef(ProcessInstanceBean.FIELD__ROOT_PROCESS_INSTANCE),
                  new ArrayList());
      query.setPredicateTerm(piOidTerm);
      List piOidList = (List) piOidTerm.getValueExpr();

      final Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      final Set closure = new HashSet();
      try
      {
         piOidList.clear();

         for (Iterator parentItr = processInstanceOIDs.iterator(); parentItr.hasNext();)
         {
            Long parentOID = (Long) parentItr.next();

            piOidList.add(parentOID);

            if (USE_PREPARED_STATEMENTS || (QUERY_BATCH_SIZE <= piOidList.size())
                  || !parentItr.hasNext())
            {
               ResultSet resultSet = null;

               try
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Getting process closure: " + query.getQueryExtension().toString());
                  }

                  resultSet = session.executeQuery(query, Session.NO_TIMEOUT);

                  final int INSTANCE_OID_COLUMN_INDEX = 1;

                  while ((null != resultSet) && resultSet.next())
                  {
                     closure.add(new Long(resultSet.getLong(INSTANCE_OID_COLUMN_INDEX)));
                  }
               }
               catch (SQLException e)
               {
                  throw new PublicException(
                        BpmRuntimeError.QUERY_FAILED_EVALUATING_PROCESS_INSTANCE_CLOSURE_RESULTSET
                              .raise(), e);

               }
               finally
               {
                  org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet(resultSet);
               }

               // reinit for next batch

               if (parentItr.hasNext())
               {
                  piOidList.clear();
               }
            }
         }
      }
      catch (PublicException e)
      {
         trace.warn("Failed evaluating process instance closure.", e);
         throw new PublicException(
               BpmRuntimeError.QUERY_FAILED_EVALUATING_PROCESS_INSTANCE_CLOSURE.raise());
      }

      // parent OIDs not being resolved via the rootProcessInstance column are possibly
      // sub-processes itself, thus the need to resolve those the old-fashioned way

      Set unresolvedOIDs = new HashSet(processInstanceOIDs);
      unresolvedOIDs.removeAll(closure);

      if (!unresolvedOIDs.isEmpty())
      {
         closure.addAll(findSubProcessClosure(unresolvedOIDs, context));
      }

      return closure;
   }

   public static Set findSubProcessClosure(Set processInstanceOIDs,
         EvaluationContext context)
   {
      if ((null == processInstanceOIDs) || processInstanceOIDs.isEmpty())
      {
         return null;
      }

      final boolean USE_PREPARED_STATEMENTS = Parameters.instance().getBoolean(
            "workflow.query.preparedStatements", false);
      final int QUERY_BATCH_SIZE = USE_PREPARED_STATEMENTS
            ? 1
            : Parameters.instance().getInteger("workflow.query.batch.size", 400);

      QueryDescriptor query = QueryDescriptor
            .from(ProcessInstanceBean.class)
            .select(ProcessInstanceBean.FIELD__OID,
                  ProcessInstanceBean.FIELD__PROCESS_DEFINITION,
                  ProcessInstanceBean.FIELD__MODEL);
      query.getQueryExtension().setDistinct(true);
      Join aiJoin = query.innerJoin(ActivityInstanceBean.class, "ai")
            .on(query.fieldRef(ProcessInstanceBean.FIELD__STARTING_ACTIVITY_INSTANCE),
                  ActivityInstanceBean.FIELD__OID);

      ComparisonTerm piOidTerm = Predicates
            .inList(aiJoin.fieldRef(ActivityInstanceBean.FIELD__PROCESS_INSTANCE),
                  new ArrayList());
      query.setPredicateTerm(piOidTerm);
      List piOidList = (List) piOidTerm.getValueExpr();

      final Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      final Map hierarchyFlagMap = new HashMap();

      final Set closure = new HashSet();

      Set parents = processInstanceOIDs;

      try
      {
         while(!parents.isEmpty())
         {
            piOidList.clear();

            Set childs = new HashSet();

            for (Iterator parentItr = parents.iterator(); parentItr.hasNext();)
            {
               Long parentOID = (Long) parentItr.next();
               closure.add(parentOID);

               piOidList.add(parentOID);

               if (USE_PREPARED_STATEMENTS || (QUERY_BATCH_SIZE <= piOidList.size())
                     || !parentItr.hasNext())
               {
                  ResultSet resultSet = null;

                  try
                  {
                     if (trace.isDebugEnabled())
                     {
                        trace.debug("Getting process closure: "
                              + query.getQueryExtension().toString());
                     }

                     resultSet = session.executeQuery(query, Session.NO_TIMEOUT);

                     fetchChildProcesses(resultSet, closure, childs, hierarchyFlagMap,
                           context);
                  }
                  catch (SQLException e)
                  {
                     throw new PublicException(
                           BpmRuntimeError.QUERY_FAILED_EVALUATING_PROCESS_INSTANCE_CLOSURE_RESULTSET
                                 .raise(), e);
                  }
                  finally
                  {
                     org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet(resultSet);
                  }

                  // reinit for next batch

                  if (parentItr.hasNext())
                  {
                     piOidList.clear();
                  }
               }
            }

            parents = childs;
         }
      }
      catch (PublicException e)
      {
         trace.warn("Failed evaluating process instance closure.", e);
         throw new PublicException(
               BpmRuntimeError.QUERY_FAILED_EVALUATING_PROCESS_INSTANCE_CLOSURE.raise());
      }

      return closure;
   }

   private static void fetchChildProcesses(ResultSet resultSet, final Set closure,
         Set childs, final Map hierarchyFlagMap, EvaluationContext context)
         throws SQLException
   {
      final int INSTANCE_OID_COLUMN_INDEX = 1;
      final int DEFINITION_OID_COLUMN_INDEX = 2;
      final int MODEL_OID_COLUMN_INDEX = 3;

      while ((null != resultSet) && resultSet.next())
      {
         Long modelOid = new Long(resultSet.getLong(MODEL_OID_COLUMN_INDEX));

         Map modelEntry = (Map) hierarchyFlagMap.get(modelOid);
         if (null == modelEntry)
         {
            modelEntry = new HashMap();
            hierarchyFlagMap.put(modelOid, modelEntry);
         }

         Long processRtOid = new Long(resultSet.getLong(DEFINITION_OID_COLUMN_INDEX));
         Boolean isHierarchyFlag = (Boolean) modelEntry.get(processRtOid);

         if (null == isHierarchyFlag)
         {
            IProcessDefinition process = context.getModelManager().findProcessDefinition(
                  modelOid.longValue(), processRtOid.longValue());
            isHierarchyFlag = isHierarchicalProcess(process);

            modelEntry.put(processRtOid, isHierarchyFlag);
         }

         if (isHierarchyFlag.booleanValue())
         {
            childs.add(new Long(resultSet.getLong(INSTANCE_OID_COLUMN_INDEX)));
         }
         else
         {
            closure.add(new Long(resultSet.getLong(INSTANCE_OID_COLUMN_INDEX)));
         }
      }
   }

   public static Set<IParticipant> findContributingParticipants(
         PerformingParticipantFilter filter, EvaluationContext context)
   {
      Set<IParticipant> participants = new HashSet();

      // TODO: change together with deputy worklist changes?
      if (PerformingParticipantFilter.FILTER_KIND_ANY_FOR_USER.equals(filter.getFilterKind())
            && (null != context.getUser()))
      {
         for (Iterator<UserParticipantLink> i = context.getUser()
               .getAllParticipantLinks(); i.hasNext();)
         {
            UserParticipantLink participantLink = i.next();

            if(isPredefinedParticipant(participantLink.getParticipant().getId()))
            {
               IParticipant administrator = getPredefinedParticipant(participants,
                     participantLink.getParticipant().getId());
               if(administrator == null)
               {
                  IScopedModelParticipant participant = new ScopedModelParticipant(participantLink
                        .getParticipant(), participantLink.getDepartment());
                  addScopedParticipant(participants, participant, filter.isRecursively());
               }
            }
            else
            {
               IScopedModelParticipant scopedParticipant = new ScopedModelParticipant(
                     participantLink.getParticipant(), participantLink.getDepartment());
               addScopedParticipant(participants, scopedParticipant, filter.isRecursively());
            }
         }

         for (Iterator i = UserGroupBean.findForUser(context.getUser().getOID(), true); i
               .hasNext();)
         {
            participants.add((IParticipant) i.next());
         }
      }
      else if (PerformingParticipantFilter.FILTER_KIND_MODEL_PARTICIPANT.equals(filter
            .getFilterKind()))
      {
         if (null != filter.getParticipant())
         {
            addParticipant(participants, filter.getParticipant(), filter.isRecursively(),
                  context);
         }
      }
      else if (PerformingParticipantFilter.FILTER_KIND_USER_GROUP.equals(filter
            .getFilterKind()))
      {
         if (null != filter.getParticipant())
         {
            try
            {
               UserGroupBean group = UserGroupBean.findById(filter.getParticipant()
                     .getId(), SecurityProperties.getPartitionOid());

               if (null != group && group.isValid())
               {
                  participants.add(group);
               }
            }
            catch (ObjectNotFoundException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Ignoring contribution from unknown user group '"
                        + filter.getParticipantID() + "'.", e);
               }
            }
         }
      }

      return participants;
   }

   public static Set<IParticipant> findContributingParticipants(
         PerformingOnBehalfOfFilter filter, EvaluationContext context)
   {
      Set<IParticipant> participants = new HashSet();

      if (PerformingOnBehalfOfFilter.FILTER_KIND_MODEL_PARTICIPANT.equals(filter
            .getFilterKind()))
      {
         if (null != filter.getParticipant())
         {
            addParticipant(participants, filter.getParticipant(), filter.isRecursively(),
                  context);
         }
         else if(null != filter.getContributors())
         {
            Iterator<ParticipantInfo> iter = filter.getContributors().iterator();
            while (iter.hasNext())
            {
               ParticipantInfo participant = iter.next();
               addParticipant(participants, participant,
                     filter.isRecursively(), context);
            }
         }
      }
      else if (PerformingOnBehalfOfFilter.FILTER_KIND_USER_GROUP.equals(filter
            .getFilterKind()))
      {
         if (null != filter.getParticipant())
         {
            try
            {
               UserGroupBean group = UserGroupBean.findById(filter.getParticipant()
                     .getId(), SecurityProperties.getPartitionOid());

               if (null != group && group.isValid())
               {
                  participants.add(group);
               }
            }
            catch (ObjectNotFoundException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Ignoring contribution from unknown user group '"
                        + filter.getParticipantID() + "'.", e);
               }
            }
         }
         else if (null != filter.getContributors())
         {
            Iterator<ParticipantInfo> iter = filter.getContributors().iterator();
            while (iter.hasNext())
            {
               ParticipantInfo groupInfo = iter.next();
               try
               {
                  UserGroupBean group = UserGroupBean.findById(groupInfo.getId(),
                        SecurityProperties.getPartitionOid());

                  if (null != group && group.isValid())
                  {
                     participants.add(group);
                  }
               }
               catch (ObjectNotFoundException e)
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Ignoring contribution from unknown user group '"
                           + groupInfo + "'.", e);
                  }
               }

            }
         }
      }

      return participants;
   }

   public static Set<IParticipant> findParticipants(ParticipantAssociationFilter filter,
         EvaluationContext context)
   {
      Set<IParticipant> participants = new HashSet();

      if (null != filter.getParticipant())
      {
         addParticipant(participants, filter.getParticipant(), filter.isRecursively(),
               context);
      }

      return participants;
   }

   /**
    * Returns a set of organizations which are selected by team leader role.
    * Furthermore it includes all manager-of and works-for roles related
    * to the organizations and their sub-orgs
    * @param filter filter of type <code>FILTER_KIND_TEAM_LEADER</code>
    * @param context
    * @return
    */
   public static Set<IModelParticipant> findOrganizationsAndRolesByTeamLeaderRole(
         ParticipantAssociationFilter filter, EvaluationContext context)
   {
      Set<IModelParticipant> organizations = new HashSet<IModelParticipant>();

      final ParticipantInfo participant = filter.getParticipant();
      if (null != participant)
      {
         final ModelManager modelManager = context.getModelManager();

         // get roles referred by participant
         Set<IRole> allRoles = new HashSet();
         Iterator<IModelParticipant> participantIter = modelManager.getParticipantsForID(
               participant.getQualifiedId());
         while (participantIter.hasNext())
         {
            IModelParticipant rawParticipant = participantIter.next();
            if (rawParticipant instanceof IRole)
            {
               allRoles.add((IRole) rawParticipant);
            }
         }

         IDepartment department = DepartmentUtils.getDepartment(participant);

         // get all teams (i.e. organizations) for these roles and bind the department
         Set<IScopedModelParticipant> allTeams = new HashSet();
         for (Iterator<IRole> rolesIter = allRoles.iterator(); rolesIter.hasNext();)
         {
            IRole role = rolesIter.next();
            for (Iterator<IOrganization> teamsIter = role.getAllTeams(); teamsIter
                  .hasNext();)
            {
               final IOrganization team = teamsIter.next();

               // TODO: only add this ScopedParticipant if department is a valid binding for the participant?
               // Currently we trust the filter. And if it is not valid than this ScopedParticipant
               // should not find entries in the audit trail.
               IScopedModelParticipant scopedTeam = new ScopedModelParticipant(team, department);
               allTeams.add(scopedTeam);
            }
         }

         // add all teams (i.e. organizations) to result set.
         for (Iterator<IScopedModelParticipant> teamsIter = allTeams.iterator(); teamsIter
               .hasNext();)
         {
            IScopedModelParticipant scopedTeam = teamsIter.next();
            addOrganizationAndRoles(organizations, scopedTeam, filter.isRecursively());
         }
      }

      return organizations;
   }

   public static Set<IParticipant> findScopedParticipantClosure(IUser user, boolean merged)
   {
      Set<IParticipant> participants = CollectionUtils.newHashSet();
      for (Iterator<UserParticipantLink> iterator = user.getAllParticipantLinks(); iterator.hasNext();)
      {
         UserParticipantLink upLink = iterator.next();

         // if merged == true then add all links, otherwise only own links
         if (merged || upLink.getOnBehalfOf() == 0)
         {
            if (isPredefinedParticipant(upLink.getParticipant().getId()))
            {
               IParticipant administrator = getPredefinedParticipant(participants, upLink
                     .getParticipant().getId());
               if (administrator == null)
               {
                  IScopedModelParticipant participant = new ScopedModelParticipant(
                        upLink.getParticipant(), upLink.getDepartment());
                  addScopedParticipant(participants, participant, true);
               }
            }
            else
            {
               IScopedModelParticipant participant = new ScopedModelParticipant(
                     upLink.getParticipant(), upLink.getDepartment());
               addScopedParticipant(participants, participant, true);
            }
         }
      }
      for (Iterator i = user.getAllUserGroups(true); i.hasNext();)
      {
         participants.add((IUserGroup) i.next());
      }
      return participants;
   }

   public static boolean participantClosureContainsParticipant(Set<IParticipant> participantClosure, ParticipantInfo filterParticipant)
   {
      for (IParticipant contributor : participantClosure)
      {
         // do contributors match the the filter and supported types?
         IParticipant rawParticipant = contributor;
         IDepartment department = null;
         DepartmentInfo filterDepartment = null;
         if(rawParticipant instanceof IScopedModelParticipant)
         {
            IScopedModelParticipant scopedModelParticipant = (IScopedModelParticipant) rawParticipant;
            rawParticipant = scopedModelParticipant.getModelParticipant();
            department = scopedModelParticipant.getDepartment();
         }
         if(filterParticipant instanceof ModelParticipantInfo)
         {
            filterDepartment = ((ModelParticipantInfo) filterParticipant).getDepartment();
         }

         if (rawParticipant instanceof IModelParticipant)
         {
            IModelParticipant participant = (IModelParticipant) rawParticipant;
            if (CompareHelper.areEqual((participant).getQualifiedId(), filterParticipant.getQualifiedId())
                  && DepartmentUtils.areEqual(department, filterDepartment))
            {
               return true;
            }
         }
         else if (rawParticipant instanceof IUserGroup)
         {
            IUserGroup userGroup = (IUserGroup) rawParticipant;
            if (CompareHelper.areEqual((userGroup).getId(), filterParticipant.getId()))
            {
               return true;
            }
         }
      }
      return false;
   }

   public static boolean isPredefinedParticipant(String id)
   {
      if(PredefinedConstants.ADMINISTRATOR_ROLE.equals(id))
      {
         return true;
      }
      return false;
   }

   private static IParticipant getPredefinedParticipant(Set<IParticipant> participants, String id)
   {
      for (IParticipant participant : participants)
      {
         if(id.equals(participant.getId()))
         {
            return participant;
         }
      }
      return null;
   }

   public static Set findProcessHierarchyClosure(IProcessDefinition rootProcess)
   {
      Set hierarchy = new HashSet();
      addProcess(hierarchy, rootProcess, true);
      return hierarchy;
   }

   public static Boolean isHierarchicalProcess(IProcessDefinition process)
   {
      Boolean hierarchyFlag = Boolean.FALSE;

      for (Iterator i = process.getAllActivities(); i.hasNext();)
      {
         IActivity a = (IActivity) i.next();
         if (a.getImplementationType().equals(ImplementationType.SubProcess))
         {
            hierarchyFlag = Boolean.TRUE;
            break;
         }
      }

      return hierarchyFlag;
   }

   private QueryUtils()
   {
      // utility class
   }

   private static final void addParticipant(Set<IParticipant> participants,
         ParticipantInfo participant, boolean recursively, EvaluationContext context)
   {
      QualifiedModelParticipantInfo qualifiedModelParticipant = (QualifiedModelParticipantInfo) participant;
      IDepartment department = DepartmentUtils.getDepartment(participant);
      for (Iterator<IModelParticipant> i = context.getModelManager().getParticipantsForID(
            qualifiedModelParticipant.getQualifiedId()); i.hasNext();)
      {
         IModelParticipant modelParticipant = i.next();

         if(isPredefinedParticipant(modelParticipant.getId()))
         {
            IParticipant administrator = getPredefinedParticipant(participants, modelParticipant.getId());
            if(administrator == null)
            {
               IScopedModelParticipant participant_ = new ScopedModelParticipant(modelParticipant, null);
               addScopedParticipant(participants, participant_, recursively);
            }
         }
         else
         {
            addScopedParticipant(participants, new ScopedModelParticipant(modelParticipant,
                  department), recursively);
         }
      }
   }

   private static final void addScopedParticipant(Set<IParticipant> participants,
         IScopedModelParticipant scopedParticipant, boolean recursively)
   {
      if ( !participants.contains(scopedParticipant))
      {
         participants.add(scopedParticipant);

         if (recursively)
         {
            IModelParticipant modelParticipant = scopedParticipant.getModelParticipant();
            IDepartment department = scopedParticipant.getDepartment();

            IDepartment nextDepartment = null;
            if (department != null)
            {
               ModelManager modelManager = ModelManagerFactory.getCurrent();
               long modelParticipantRtOid = modelManager.getRuntimeOid(modelParticipant);

               // if the current participant is the one which defines the
               // current department then next participant has to be scoped by parent department
               if (modelParticipantRtOid == department.getRuntimeOrganizationOID())
               {
                  nextDepartment = department.getParentDepartment();
               }
               else
               {
                  nextDepartment = department;
               }
            }

            for (Iterator itr = modelParticipant.getAllOrganizations(); itr.hasNext();)
            {
               IModelParticipant nextParticipant = (IModelParticipant) itr.next();

               IScopedModelParticipant scopedNextParticipant = new ScopedModelParticipant(
                     nextParticipant, nextDepartment);

               addScopedParticipant(participants, scopedNextParticipant, recursively);
            }

         }
      }
   }

   /**
    * Adds organization to set of organizations if it is not already contained.
    * If recursively is true then all sub organizations are added as well.
    *
    * @param participants Target set of organizations.
    * @param scopedOrg Scoped organization to be added.
    * @param recursively If set to true the all sub organizations will be added as well.
    */
   private static final void addOrganizationAndRoles(Set<IModelParticipant> participants,
         IScopedModelParticipant scopedOrg, boolean recursively)
   {
      IModelParticipant rawParticipant = scopedOrg.getModelParticipant();
      if (rawParticipant instanceof IOrganization)
      {
         if ( !participants.contains(scopedOrg))
         {
            participants.add(scopedOrg);

            IOrganization organization = (IOrganization) rawParticipant;
            IRole teamLead = organization.getTeamLead();
            if(teamLead != null)
            {
               // include manager-of relations
               if (participants.contains(new ScopedModelParticipant(
                     organization.getTeamLead(), scopedOrg.getDepartment())))
               {
                  trace.warn("Duplicated team leader " + teamLead.getId() + " detected in hierarchy.");
               }
               participants.add(new ScopedModelParticipant(teamLead, scopedOrg.getDepartment()));
            }
            Iterator pIter = organization.getAllParticipants();
            while(pIter.hasNext())
            {
               IModelParticipant p = (IModelParticipant) pIter.next();
               if(p instanceof IRole && !p.equals(teamLead))
               {
                  // include all works-for relations
                  if(participants.contains(new ScopedModelParticipant(p, scopedOrg.getDepartment())))
                  {
                     trace.warn("Role " + p.getId() + " is used in more than one orgainzation.");
                  }
                  participants.add(new ScopedModelParticipant(p, scopedOrg.getDepartment()));
               }
            }

            if (recursively)
            {
               ModelManager modelManager = ModelManagerFactory.getCurrent();
               IDepartment department = scopedOrg.getDepartment();

               if (department != null)
               {
                  // current organization child department handling
                  long orgRtOid = modelManager.getRuntimeOid(organization);
                  Iterator<IDepartment> orgDepartmentsIter = DepartmentUtils.getChildDepartmentsIterator(department);
                  while (orgDepartmentsIter.hasNext())
                  {
                     IDepartment nextChildDepartment = orgDepartmentsIter.next();
                     // Only traverse if dep is bound to org.
                     if (orgRtOid == nextChildDepartment.getRuntimeOrganizationOID())
                     {
                        ScopedModelParticipant nextScopedOrg = new ScopedModelParticipant(
                              organization, nextChildDepartment);
                        addOrganizationAndRoles(participants, nextScopedOrg, recursively);
                     }
                  }

               }

               // sub organization handling
               for (Iterator itr = organization.getSubOrganizations(); itr.hasNext();)
               {
                  final IOrganization nextOrg = (IOrganization) itr.next();

                  IScopedModelParticipant nextScopedOrg = new ScopedModelParticipant(nextOrg, department);
                  addOrganizationAndRoles(participants, nextScopedOrg, recursively);
               }
            }
         }
      }
   }

   private static final void addProcess(Set hierarchy, IProcessDefinition process,
         boolean recursively)
   {
      if (!hierarchy.contains(process))
      {
         hierarchy.add(process);

         if (recursively)
         {
            for (Iterator itr = process.getAllActivities(); itr.hasNext();)
            {
               IActivity activity = (IActivity) itr.next();
               if (ImplementationType.SubProcess.equals(activity.getImplementationType()))
               {
                  addProcess(hierarchy, activity.getImplementationProcessDefinition(),
                        recursively);
               }
            }
         }
      }
   }

   public static SubsetPolicy getSubset(Query query)
   {
      SubsetPolicy subset = (SubsetPolicy) query.getPolicy(SubsetPolicy.class);
      if (null == subset)
      {
         subset = SubsetPolicy.UNRESTRICTED;
      }
      return subset;
   }

   public static int getTimeOut(Query query)
   {
      TimeoutPolicy timeoutPolicy = (TimeoutPolicy) query.getPolicy(TimeoutPolicy.class);
      int timeout = 0;
      if (timeoutPolicy != null)
      {
         timeout = timeoutPolicy.getTimeout();
      }
      return timeout;
   }

   public static boolean isRestrictedToActiveModel(Query query)
   {
      ModelVersionPolicy modelVersionPolicy = (ModelVersionPolicy) query
            .getPolicy(ModelVersionPolicy.class);
      boolean timeout = false;
      if (modelVersionPolicy != null)
      {
         timeout = modelVersionPolicy.isRestrictedToActiveModel();
      }
      return timeout;
   }

   /**
    * Checks whether an instance of {@link CurrentPartitionFilter} already exists as
    * toplevel predicate of given query. Otherwise an instance will be added.
    *
    * @param query
    *           The current query.
    * @param type
    *           A concrete subtype of {@link PersistentBean} the query will return
    *           instances of. This type has to correspond with the type of query.
    */
   public static void addCurrentPartitionFilter(Query query, Class type)
   {
      if (null == query)
      {
         throw new IllegalArgumentException("Parameter 'query' can never be null");
      }

      if (null == type)
      {
         throw new IllegalArgumentException("Parameter 'type' can never be null");
      }

      boolean singlePartition = Parameters.instance().getBoolean(
            KernelTweakingProperties.SINGLE_PARTITION, false);

      if ( !singlePartition)
      {
         FilterAndTerm filter = query.getFilter();

         boolean currentPartitionFilterExists = false;
         for (Iterator iterator = filter.getParts().iterator(); iterator.hasNext();)
         {
            if (iterator.next() instanceof CurrentPartitionFilter)
            {
               currentPartitionFilterExists = true;
               break;
            }
         }

         if ( !currentPartitionFilterExists)
         {
            filter.add(new CurrentPartitionFilter(type));
         }
      }
   }

   public static long getTotalCountThreshold(FetchPredicate fetchPredicate)
   {
      long totalCountThreshold = Long.MAX_VALUE;
      if (Parameters.instance().getBoolean(
            KernelTweakingProperties.ENGINE_EXCLUDE_USER_EVALUATION, false)
            && hasDataPrefetchHintFilter(fetchPredicate))
      {
         totalCountThreshold = Parameters.instance().getLong(
               KernelTweakingProperties.EXCLUDE_USER_MAX_WORKLIST_COUNT, 100);
      }
      return totalCountThreshold;
   }

   private static boolean hasDataPrefetchHintFilter(FetchPredicate fetchPredicate)
   {
      boolean hasDataPrefetchHints = false;
      if ((fetchPredicate instanceof AbstractAuthorization2Predicate)
            && ((AbstractAuthorization2Predicate) fetchPredicate)
                  .hasDataPrefetchHintFilter())
      {
         hasDataPrefetchHints = true;
      }
      return hasDataPrefetchHints;
   }
}