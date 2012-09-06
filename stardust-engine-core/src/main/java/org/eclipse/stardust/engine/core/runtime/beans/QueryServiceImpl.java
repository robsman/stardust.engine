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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.*;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomQueryUtils;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.xsd.util.XSDResourceImpl;

import org.eclipse.stardust.vfs.IDocumentRepositoryService;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class QueryServiceImpl implements QueryService, Serializable
{
   private static final long serialVersionUID = 1L;

   public long getUsersCount(UserQuery query)
   {
      return GenericQueryEvaluator.count(query, UserBean.class,
            getDefaultEvaluationContext());
   }

   public long getUserGroupsCount(UserGroupQuery query)
   {
      return GenericQueryEvaluator.count(query, UserGroupBean.class,
            getDefaultEvaluationContext());
   }

   public ActivityInstances getAllActivityInstances(ActivityInstanceQuery query)
   {
      BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
      RtDetailsFactory detailsFactory = rte.getDetailsFactory();
      boolean usingCaches = detailsFactory.isUsingCaches();
      try
      {
         detailsFactory.setUsingCaches(true);

         if (query instanceof CustomActivityInstanceQuery)
         {
            // evaluating custom query
            return CustomQueryUtils.evaluateCustomQuery((CustomActivityInstanceQuery) query);
         }

         ResultIterator rawResult = new ActivityInstanceQueryEvaluator(query,
               getDefaultEvaluationContext()).executeFetch();

         try
         {
            return ProcessQueryPostprocessor.findMatchingActivityInstanceDetails(query,
                  rawResult);
         }
         finally
         {
            rawResult.close();
         }
      }
      finally
      {
         detailsFactory.setUsingCaches(usingCaches);
      }
   }

   public ActivityInstance findFirstActivityInstance(ActivityInstanceQuery query)
      throws ObjectNotFoundException
   {
      ResultIterator rawResult = new ActivityInstanceQueryEvaluator(query,
            getDefaultEvaluationContext()).executeFetch();
      try
      {
         return (ActivityInstance) ProcessQueryPostprocessor.findFirstMatchingActivityInstanceDetails(
               query, rawResult, ActivityInstanceDetails.class);
      }
      finally
      {
         rawResult.close();
      }
   }

   public ProcessInstance findFirstProcessInstance(ProcessInstanceQuery query)
         throws ObjectNotFoundException
   {
      ResultIterator rawResult = new ProcessInstanceQueryEvaluator(query,
            getDefaultEvaluationContext()).executeFetch();
      try
      {
         return (ProcessInstance) ProcessQueryPostprocessor.findFirstMatchingProcessInstanceDetails(
               query, rawResult, ProcessInstanceDetails.class);
      }
      finally
      {
         rawResult.close();
      }
   }

   private EvaluationContext getDefaultEvaluationContext()
   {
      return QueryServiceUtils.getDefaultEvaluationContext();
   }

   public LogEntries getAllLogEntries(LogEntryQuery query)
   {
      /*
       * Without <LogEntry, LogEntryDetails> ant would result in an incompatible types error.
       * The strange thing is that it is compiling in eclipse without any explicit declarations.
       */
      RawQueryResult<LogEntry> result = GenericQueryEvaluator.<LogEntry, LogEntryDetails>evaluate(
            query, LogEntryBean.class, ILogEntry.class, LogEntryDetails.class,
            getDefaultEvaluationContext());

      return QueryResultFactory.createLogEntryQueryResult(query, result);
   }

   public ProcessInstances getAllProcessInstances(ProcessInstanceQuery query)
   {
      BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
      RtDetailsFactory detailsFactory = rte.getDetailsFactory();
      boolean usingCaches = detailsFactory.isUsingCaches();
      try
      {
         detailsFactory.setUsingCaches(true);

         if (query instanceof CustomProcessInstanceQuery)
         {
            // evaluating custom query
            return CustomQueryUtils.evaluateCustomQuery((CustomProcessInstanceQuery) query);
         }
   
         ResultIterator rawResult = new ProcessInstanceQueryEvaluator(query,
               getDefaultEvaluationContext()).executeFetch();
         try
         {
            return ProcessQueryPostprocessor.findMatchingProcessInstancesDetails(query,
                  rawResult, ProcessInstanceDetails.class);
         }
         finally
         {
            rawResult.close();
         }
      }
      finally
      {
         detailsFactory.setUsingCaches(usingCaches);
      }
   }

   public Users getAllUsers(UserQuery query)
   {
      if (query instanceof CustomUserQuery)
      {
         // evaluating custom query
         return CustomQueryUtils.evaluateCustomQuery((CustomUserQuery) query);
      }
      else
      {
         return QueryServiceUtils.evaluateUserQuery(query);
      }
   }

   public UserGroups getAllUserGroups(UserGroupQuery query)
   {
      /*
       * Without <UserGroup,UserGroupDetails> antit would result in an incompatible types
       * error The strange thing is that it is compiling in eclipse without any explicit
       * declerations
       */
      RawQueryResult<UserGroup> result = GenericQueryEvaluator.<UserGroup, UserGroupDetails> evaluate(
            query, UserGroupBean.class, IUserGroup.class, UserGroupDetails.class,
            getDefaultEvaluationContext());

      return QueryResultFactory.createUserGroupQueryResult(query, result);
   }

   public long getProcessInstancesCount(ProcessInstanceQuery query)
   {
      return new ProcessInstanceQueryEvaluator(query, getDefaultEvaluationContext()).executeCount();
   }

   public long getActivityInstancesCount(ActivityInstanceQuery query)
   {
      return new ActivityInstanceQueryEvaluator(query, getDefaultEvaluationContext()).executeCount();
   }

   public LogEntry findFirstLogEntry(LogEntryQuery query) throws ObjectNotFoundException
   {
      /*
       * Without <LogEntry,LogEntryDetails> antit would result in an incompatible types
       * error The strange thing is that it is compiling in eclipse without any explicit
       * declerations
       */
      // @todo (france, ub): suboptimal, just to make the test cases happy
      RawQueryResult<LogEntry> result = GenericQueryEvaluator.<LogEntry, LogEntryDetails> evaluate(
            query, LogEntryBean.class, ILogEntry.class, LogEntryDetails.class,
            getDefaultEvaluationContext());
      if (result.size() == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_NO_MATCHING_LOG_ENTRY.raise());
      }
      else
      {
         return (LogEntry) result.get(0);
      }
   }

   public User findFirstUser(UserQuery query) throws ObjectNotFoundException
   {
      // @todo (france, ub): suboptimal, just to make the test cases happy
      /*
       * Without <User,UserDetails> antit would result in an incompatible types error The
       * strange thing is that it is compiling in eclipse without any explicit
       * declerations
       */
      RawQueryResult<User> result = GenericQueryEvaluator.<User, UserDetails> evaluate(
            query, UserBean.class, IUser.class, UserDetails.class,
            getDefaultEvaluationContext());
      if (result.size() == 0)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.ATDB_NO_MATCHING_USER.raise());
      }
      else
      {
         return (User) result.get(0);
      }
   }

   public UserGroup findFirstUserGroup(UserGroupQuery query)
         throws ObjectNotFoundException
   {
      /*
       * Without <UserGroup,UserGroupDetails> antit would result in an incompatible types
       * error The strange thing is that it is compiling in eclipse without any explicit
       * declerations
       */
      RawQueryResult<UserGroup> result = GenericQueryEvaluator.<UserGroup, UserGroupDetails> evaluate(
            query, UserGroupBean.class, IUserGroup.class, UserGroupDetails.class,
            getDefaultEvaluationContext());

      if (result.size() == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_NO_MATCHING_USER_GROUP.raise());
      }
      else
      {
         return (UserGroup) result.get(0);
      }
   }

   public long getLogEntriesCount(LogEntryQuery query)
   {
      return GenericQueryEvaluator.count(query, LogEntryBean.class,
            getDefaultEvaluationContext());
   }

   public List<ActivityInstance> getAuditTrail(long processInstanceOID)
         throws ObjectNotFoundException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processInstanceOID);
      Iterator raw = processInstance.getAllPerformedActivityInstances();
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate authorizationPredicate = runtimeEnvironment.getAuthorizationPredicate();
      /*
       * Without <Activityinstance,ActivityInstanceDetails> javac would result in an
       * incompatible types error. The strange thing is that it is compiling in eclipse
       * without any explicit declaration
       */
      return DetailsFactory.<ActivityInstance, ActivityInstanceDetails> createCollection(
            authorizationPredicate == null ? raw : new FilteringIterator(raw,
                  authorizationPredicate), IActivityInstance.class,
            ActivityInstanceDetails.class);
   }

   public String getModelAsXML(long modelOID) throws ObjectNotFoundException
   {
      String carnotXml = LargeStringHolder.getLargeString(modelOID, ModelPersistorBean.class);
      if (carnotXml == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_MODEL_OID.raise(modelOID), modelOID);
      }
      return ParametersFacade.instance().getBoolean(
            KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true)
            ? XpdlUtils.convertCarnot2Xpdl(carnotXml, false)
            : carnotXml;
   }

   public DeployedModelDescription getModelDescription(long modelOID)
         throws ObjectNotFoundException
   {
      IModel model = getIModel(modelOID, null);
      return (DeployedModelDescription) DetailsFactory.create(model, IModel.class,
            DeployedModelDescriptionDetails.class);
   }

   public DeployedModelDescription getActiveModelDescription()
         throws ObjectNotFoundException
   {
      return getModelDescription(PredefinedConstants.ACTIVE_MODEL);
   }

   public boolean wasRedeployed(long modelOid, int revision)
   {
      IModel model = getIModel(modelOid, null);

      int newRevision = revision = (null != model)
            ? model.getIntegerAttribute(PredefinedConstants.REVISION_ATT)
            : 0;
      return newRevision > revision;
   }

   public List<DeployedModelDescription> getAllAliveModelDescriptions()
   {
      /*
       * Without <DeployedModelDescription,DeployedModelDescriptionDetails> antit would
       * result in an incompatible types error. The strange thing is that it is compiling
       * in eclipse without any explicit declerations
       */
      return DetailsFactory.<DeployedModelDescription, DeployedModelDescriptionDetails> createCollection(
            ModelManagerFactory.getCurrent().getAllAliveModels(), IModel.class,
            DeployedModelDescriptionDetails.class);
   }

   public List<DeployedModelDescription> getAllModelDescriptions()
   {
      /*
       * Without <DeployedModelDescription,DeployedModelDescriptionDetails> antit would
       * result in an incompatible types error. The strange thing is that it is compiling
       * in eclipse without any explicit declerations
       */
      return DetailsFactory.<DeployedModelDescription, DeployedModelDescriptionDetails> createCollection(
            ModelManagerFactory.getCurrent().getAllModels(), IModel.class,
            DeployedModelDescriptionDetails.class);
   }

   public Models getModels(DeployedModelQuery query)
   {
      return new Models(query, DetailsFactory.<DeployedModelDescription, DeployedModelDescriptionDetails>createCollection(
            new FilteringIterator<IModel>(ModelManagerFactory.getCurrent().getAllModels(), new ModelQueryEvaluator(query)),
            IModel.class, DeployedModelDescriptionDetails.class));
   }

   public DeployedModel getActiveModel()
         throws ObjectNotFoundException
   {
      return getActiveModel(true);
   }

   static DeployedModel getActiveModel(boolean computeAliveness)
      throws ObjectNotFoundException
   {
      IModel model = getIModel(PredefinedConstants.ACTIVE_MODEL, null);
      if (model == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_NO_ACTIVE_MODEL.raise());
      }
      boolean alive = computeAliveness && ModelManagerFactory.getCurrent().isAlive(model);
      ModelDetails details = DetailsFactory.create(model, IModel.class, ModelDetails.class);
      return new ModelDetailsWithAliveness(details, computeAliveness ? alive : null);
   }

   public DeployedModel getModel(long modelOid)
         throws ObjectNotFoundException
   {
      return getModel(modelOid, true);
   }

   public DeployedModel getModel(long modelOid, boolean computeAliveness)
      throws ObjectNotFoundException
   {
      IModel model = getIModel(modelOid, null);
      ModelDetails modelDetails = DetailsFactory.create(model, IModel.class,
            ModelDetails.class);

      if (computeAliveness)
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();
         boolean alive = modelManager.isAlive(model);

         modelDetails = new ModelDetailsWithAliveness(modelDetails, alive);
      }
      return modelDetails;
   }

   public Participant getParticipant(String id) throws ObjectNotFoundException
   {
      return getParticipant(PredefinedConstants.ACTIVE_MODEL, id);
   }

   public Participant getParticipant(long modelOID, String id)
         throws ObjectNotFoundException
   {
      ProcessInstanceGroupUtils.assertNotCasePerformer(id);

      QName name = id == null ? null : QName.valueOf(id);
      IModel model = getIModel(modelOID, name.getNamespaceURI());

      String participantId = name.getLocalPart();
      IModelParticipant participant = model.findParticipant(participantId);

      if (participant instanceof IRole)
      {
         return (Participant) DetailsFactory.create(participant, IRole.class,
               RoleDetails.class);
      }

      if (participant instanceof IOrganization)
      {
         return (Participant) DetailsFactory.create(participant, IOrganization.class,
               OrganizationDetails.class);
      }

      throw new ObjectNotFoundException(
            BpmRuntimeError.MDL_UNKNOWN_PARTICIPANT_ID.raise(participantId), participantId);
   }

   public List<Participant> getAllParticipants() throws ObjectNotFoundException
   {
      return getAllParticipants(PredefinedConstants.ACTIVE_MODEL);
   }

   public List<Participant> getAllParticipants(long modelOID)
         throws ObjectNotFoundException
   {
      IModel model = getIModel(modelOID, null);
      List<Participant> result = CollectionUtils.newList();
      ModelElementList participants = model.getParticipants();
      for (int i = 0; i < participants.size(); i++ )
      {
         IModelParticipant participant = (IModelParticipant) participants.get(i);
         if (participant instanceof IRole)
         {
            result.add(DetailsFactory.create(participant, IRole.class, RoleDetails.class));
         }
         else if (participant instanceof IOrganization)
         {
            if ( !ProcessInstanceGroupUtils.isCasePerformer(participant.getQualifiedId()))
            {
               result.add(DetailsFactory.create(participant, IOrganization.class,
                     OrganizationDetails.class));
            }
         }
      }
      return result;
   }

   public ProcessDefinitions getProcessDefinitions(ProcessDefinitionQuery query)
   {
      ProcessDefinitionQueryEvaluator evaluator = new ProcessDefinitionQueryEvaluator(query);

      Long modelOID = evaluator.getModelOid();
      Iterator<IProcessDefinition> processes = Collections.<IProcessDefinition>emptyList().iterator();
      if (modelOID != null)
      {
         IModel model = ModelManagerFactory.getCurrent().findModel(modelOID);
         if (model != null)
         {
            processes = model.getProcessDefinitions().iterator();
         }
      }
      else
      {
         List<IModel> models = ModelManagerFactory.getCurrent().findActiveModels();
         if (!models.isEmpty())
         {
            if (models.size() == 1)
            {
               processes = models.get(0).getProcessDefinitions().iterator();
            }
            else
            {
               List<ModelElementList<IProcessDefinition>> allProcDefs = CollectionUtils.newList(models.size());
               for (IModel model : models)
               {
                  allProcDefs.add(model.getProcessDefinitions());
               }
               processes = new MultiIterator(allProcDefs);
            }
         }
      }

      return new ProcessDefinitions(query,
            DetailsFactory.<ProcessDefinition, ProcessDefinitionDetails> createCollection(
                  new FilteringIterator(processes, evaluator), IProcessDefinition.class,
                  ProcessDefinitionDetails.class));
   }

   public DataQueryResult getAllData(DataQuery query)
   {
      DataQueryEvaluator evaluator = new DataQueryEvaluator(query);

      Long modelOID = evaluator.getModelOid();
      IModel model;
      Iterator<IData> allData;
      if (modelOID != null)
      {
         model = ModelManagerFactory.getCurrent().findModel(modelOID);

         if (model == null)
         {
            // no model found; produce empty results
            allData = new Iterator<IData>()
            {
               public boolean hasNext()
               {
                  return false;
               }

               public IData next()
               {
                  return null;
               }

               public void remove()
               {
               }
            };
         }
         else
         {
            allData = model.getData().iterator();
         }
      }
      else
      {
         model = ModelManagerFactory.getCurrent().findActiveModel();
         allData = model.getData().iterator();
      }

      return new DataQueryResult(query,
            DetailsFactory.<Data, DataDetails> createCollection(
                  new FilteringIterator(allData, evaluator), IData.class,
                  DataDetails.class));
   }

   public ProcessDefinition getProcessDefinition(String id)
         throws ObjectNotFoundException
   {
      return getProcessDefinition(PredefinedConstants.ACTIVE_MODEL, id);
   }

   public ProcessDefinition getProcessDefinition(long modelOID, String id)
         throws ObjectNotFoundException
   {
      QName name = id == null ? null : QName.valueOf(id);

      IModel model = getIModel(modelOID, name.getNamespaceURI());

      String processId = name.getLocalPart();
      IProcessDefinition processDefinition = model.findProcessDefinition(processId);

      if (processDefinition == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_PROCESS_DEFINITION_ID.raise(processId), processId);
      }
      return (ProcessDefinition) DetailsFactory.create(processDefinition,
            IProcessDefinition.class, ProcessDefinitionDetails.class);
   }

   public List<ProcessDefinition> getAllProcessDefinitions()
         throws ObjectNotFoundException
   {
      return getAllProcessDefinitions(PredefinedConstants.ACTIVE_MODEL);
   }

   public List<ProcessDefinition> getAllProcessDefinitions(long modelOID)
         throws ObjectNotFoundException
   {
      IModel model = getIModel(modelOID, null);
      /* Without <ProcessDefinition,ProcessDefinitionDetails> antit
       * would result in an incompatible types error.
       * The strange thing is that it is compiling in eclipse without
       * any explicit declarations */
      return DetailsFactory.<ProcessDefinition, ProcessDefinitionDetails>
         createCollection(model.getProcessDefinitions(),
            IProcessDefinition.class, ProcessDefinitionDetails.class);
   }

   public List<Permission> getPermissions()
   {
      return Authorization2.getPermissions(QueryService.class);
   }

   public byte[] getSchemaDefinition(long modelOID, String id)
         throws ObjectNotFoundException
   {
      QName name = id == null ? null : QName.valueOf(id);

      IModel model = getIModel(modelOID, name.getNamespaceURI());

      String typeDeclarationId = name.getLocalPart();

      ITypeDeclaration typeDeclaration = model.findTypeDeclaration(typeDeclarationId);

      if (typeDeclaration == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_TYPE_DECLARATION_ID.raise(typeDeclarationId),
               typeDeclarationId);
      }

      // Serialize the schema
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      XSDResourceImpl.serialize(bout, StructuredTypeRtUtils.getXSDSchema(model,
            typeDeclaration).getElement());
      return bout.toByteArray();
   }

   private static IModel getIModel(long modelOID, String modelId)
         throws ObjectNotFoundException
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModel model = modelId == null || modelId.length() == 0
            ? modelManager.findModel(modelOID)
            : modelManager.findModel(modelOID, modelId);
      if (model == null)
      {
         if (modelOID == PredefinedConstants.ACTIVE_MODEL)
         {
            throw new ObjectNotFoundException(BpmRuntimeError.MDL_NO_ACTIVE_MODEL.raise());
         }
         else if (modelOID == PredefinedConstants.LAST_DEPLOYED_MODEL)
         {
            throw new ObjectNotFoundException(BpmRuntimeError.MDL_NO_MODEL.raise());
         }
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_MODEL_OID.raise(modelOID), modelOID);
      }
      return model;
   }

   public List<Department> findAllDepartments(DepartmentInfo parent,
         OrganizationInfo organization) throws ObjectNotFoundException
   {
      ProcessInstanceGroupUtils.assertNotCasePerformer(organization);

      Iterator<IDepartment> departments = null;
      IDepartment scope = null;
      if (parent != null)
      {
         scope = DepartmentBean.findByOID(parent.getOID());
      }
      if (organization == null)
      {
         departments = DepartmentBean.findAllForParent(scope);
      }
      else
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();
         IModelParticipant participant = null;
         long rtOid = organization.getRuntimeElementOID();
         if (rtOid != 0)
         {
            participant = modelManager.findModelParticipant(
                  PredefinedConstants.ANY_MODEL, rtOid);
         }
         if (participant == null)
         {
            if ( !StringUtils.isEmpty(organization.getQualifiedId()))
            {
               Iterator<IModelParticipant> participants = modelManager.getParticipantsForID(organization.getQualifiedId());
               if (participants.hasNext())
               {
                  participant = (IModelParticipant) participants.next();
               }
            }
            if (participant == null)
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.MDL_UNKNOWN_PARTICIPANT_ID.raise(organization.getQualifiedId()));
            }
         }
         participant = DepartmentUtils.getFirstScopedOrganization(participant);
         if (participant != null)
         {
            rtOid = modelManager.getRuntimeOid(participant);
            departments = DepartmentBean.findAllForOrganization(rtOid, scope);
         }
      }
      if (departments == null)
      {
         return Collections.emptyList();
      }

      // post processing of found departments
      final ModelManager modelManager = ModelManagerFactory.getCurrent();
      Iterator<IDepartment> filteredDepartments = new FilteringIterator<IDepartment>(
            departments, new Predicate<IDepartment>()
            {
               // Only accept departments which reference model participants that still
               // exist in any model.
               // This is necessary as on model overwrite participant can be removed from
               // model
               // but will not be deleted from database.
               public boolean accept(IDepartment o)
               {
                  final long rtOid = o.getRuntimeOrganizationOID();
                  IModelParticipant mp = modelManager.findModelParticipant(
                        PredefinedConstants.ANY_MODEL, rtOid);

                  return mp != null;
               }
            });

      return DetailsFactory.<Department, DepartmentDetails> createCollection(
            filteredDepartments, IDepartment.class, DepartmentDetails.class);
   }

   public Department findDepartment(DepartmentInfo parent, String id,
         OrganizationInfo info) throws ObjectNotFoundException
   {
      IOrganization org = null;
      if (info != null)
      {
         String participantId = info.getId();
         ModelManager manager = ModelManagerFactory.getCurrent();
         if (StringUtils.isEmpty(participantId))
         {
            long rtOid = info.getRuntimeElementOID();
            IModelParticipant participant = manager.findModelParticipant(info);

            ProcessInstanceGroupUtils.assertNotCasePerformer(participant == null
                  ? null
                  : participant.getQualifiedId());

            if ( !(participant instanceof IOrganization))
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.MDL_UNKNOWN_PARTICIPANT_RUNTIME_OID.raise(rtOid));
            }
            org = (IOrganization) participant;
         }
         else
         {
            org = (IOrganization) SynchronizationService.findModelParticipantFor(participantId, manager);
         }
      }

      IDepartment department = null;
      if ((SecurityProperties.isInternalAuthentication()
            || Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
            && (parent == null || parent.getOID() > 0))
      {
         IDepartment parentDepartment = parent == null ? null : DepartmentBean.findByOID(parent.getOID());
         department = DepartmentBean.findById(id, parentDepartment, org);
      }
      else
      {
         String participantId = org == null ? null : org.getQualifiedId();
         long participantModelOid = org == null ? PredefinedConstants.ANY_MODEL : org.getModel().getModelOID();
         final Pair<String, List<String>> departmentIdsPair =
         SynchronizationService.getDepartmentPairFor(id, participantId, parent);
         final Pair<IDepartment, Boolean> departmentPair =
            SynchronizationService.synchronizeDepartment(departmentIdsPair.getFirst(), participantModelOid, departmentIdsPair.getSecond());
         IDepartment foundDepartment = departmentPair.getFirst();
         if (foundDepartment != null && departmentPair.getSecond() != null
               && departmentPair.getSecond()
            && matchesParent(parent, foundDepartment.getParentDepartment()))
         {
            department = foundDepartment;
         }
      }

      if (department == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.ATDB_UNKNOWN_DEPARTMENT_ID2.raise(id,
                     parent != null ? parent.getId() : null));
      }
      return DetailsFactory.create(department, IDepartment.class, DepartmentDetails.class);
   }

   private boolean matchesParent(DepartmentInfo parentInfo, IDepartment parent)
   {
      if (parentInfo == null)
      {
         return parent == null;
      }
      if (parent == null)
      {
         return false;
      }
      long oid = parentInfo.getOID();
      if (oid > 0 && oid != parent.getOID())
      {
         return false;
      }
      String id = parentInfo.getId();
      if ( !StringUtils.isEmpty(id))
      {
         return id.equals(parent.getId());
      }
      return false;
   }

   public Document findFirstDocument(DocumentQuery query) throws ObjectNotFoundException
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      IDocumentRepositoryService vfs = rtEnv.getDocumentRepositoryService();

      if (vfs != null)
      {
         query.setPolicy(new SubsetPolicy(1));
         ResultIterator rawResult = new DocumentQueryEvaluator(query,
               getDefaultEvaluationContext(), vfs).executeFetch();
         try
         {
            return DocumentQueryPostProcessor.findFirstMatchingDocument(query, rawResult);
         }
         finally
         {
            rawResult.close();
         }
      }
      else
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.DMS_FILE_STORE_UNAVAILABLE.raise());
      }

   }

   public Documents getAllDocuments(DocumentQuery query)
   {

      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      IDocumentRepositoryService vfs = rtEnv.getDocumentRepositoryService();

      if (vfs != null)
      {
         ResultIterator rawResult = new DocumentQueryEvaluator(query,
               getDefaultEvaluationContext(), vfs).executeFetch();
         try
         {
            return DocumentQueryPostProcessor.findMatchingDocuments(query, rawResult);
         }
         finally
         {
            rawResult.close();
         }
      }
      else
      {
         return new Documents(query, new RawQueryResult<Document>(Collections.EMPTY_LIST,
               QueryUtils.getSubset(query), false));
      }

   }

   public Preferences getPreferences(PreferenceScope scope, String moduleId,
         String preferencesId)
   {
      return PreferenceStorageFactory.getCurrent().getPreferences(scope, moduleId, preferencesId);
   }

   public List<Preferences> getAllPreferences(PreferenceQuery preferenceQuery)
   {
      if(preferenceQuery == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("preferenceQuery"));
      }

      return PreferenceStorageFactory.getCurrent().getAllPreferences(preferenceQuery, true);
   }

   public RuntimeEnvironmentInfo getRuntimeEnvironmentInfo()
   {
      return new RuntimeEnvironmentInfoDetails();
   }
}