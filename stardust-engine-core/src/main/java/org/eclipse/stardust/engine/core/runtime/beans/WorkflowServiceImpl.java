/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils.isSerialExecutionScenario;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.*;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.*;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions.SpawnMode;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkUtils;
import org.eclipse.stardust.engine.core.model.beans.ScopedModelParticipant;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.preferences.*;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.command.Configurable;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.runtime.utils.*;
import org.eclipse.stardust.engine.core.spi.artifact.ArtifactManagerFactory;
import org.eclipse.stardust.engine.core.spi.artifact.impl.BenchmarkDefinitionArtifactType;

/**
 * @author mgille
 * @version $Revision$
 */
public class WorkflowServiceImpl implements Serializable, WorkflowService
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(WorkflowServiceImpl.class);

   public ProcessInstance startProcess(String id, Map<String, ? > inputData,
         boolean synchronously)
   {
      StartOptions options = new StartOptions(inputData, synchronously, null);

      IProcessDefinition processDefinition = ModelUtils.getProcessDefinition(id);
      return startProcess(processDefinition, options);
   }

   public ProcessInstance startProcess(IProcessDefinition processDefinition,
         Map<String, ? > inputData, boolean synchronously)
   {
      StartOptions options = new StartOptions(inputData, synchronously, null);

      return startProcess(processDefinition, options);
   }

   public ProcessInstance startProcess(String id, StartOptions options)
   {
      IProcessDefinition processDefinition = ModelUtils.getProcessDefinition(id);
      return startProcess(processDefinition, options);
   }

   public ProcessInstance startProcess(IProcessDefinition processDefinition,
         StartOptions options)
   {
      Map<String, ?> inputData = options.getData();
      boolean synchronously = options.isSynchronously();

      Map<String, Object> values = (Map<String, Object>) inputData;

      if (processDefinition.getDeclaresInterface())
      {
         processDefinition = ModelRefBean.getPrimaryImplementation(processDefinition,
               null, null);
         if (inputData != null)
         {
            values = CollectionUtils.newMap();
            for (Map.Entry<String, ? > entry : inputData.entrySet())
            {
               String parameterId = entry.getKey();
               String dataId = processDefinition.getMappedDataId(parameterId);
               values.put(dataId == null ? parameterId : dataId, entry.getValue());
            }
         }
      }

      ModelUtils.validateData((IModel) processDefinition.getModel(), values);

      IProcessInstance processInstance = ProcessInstanceBean.createInstance(
            processDefinition, SecurityProperties.getUser(), values);
      Serializable calendar = options.getProperty(StartOptions.BUSINESS_CALENDAR);
      if (calendar != null)
      {
         processInstance.setPropertyValue(StartOptions.BUSINESS_CALENDAR, calendar);
      }

      if ( !AuditTrailPersistence.isTransientExecution(processInstance.getAuditTrailPersistence()))
      {
         if (options.getBenchmarkId() != null)
         {
            DeployedRuntimeArtifact artifact = ArtifactManagerFactory.getCurrent()
                  .getActiveDeployedArtifact(BenchmarkDefinitionArtifactType.TYPE_ID,
                        options.getBenchmarkId());
            if (artifact != null)
            {
               processInstance.setBenchmark(artifact.getOid());
            }
            else
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.BPMRT_BENCHMARK_NOT_FOUND.raise(options.getBenchmarkId()));
            }
         }
         else
         {
            // Lookup default benchmark
            IPreferenceStorageManager prefManager = PreferenceStorageFactory.getCurrent();

            Preferences defaultBenchmarkPrefs = prefManager.getPreferences(
                  PreferenceScope.PARTITION,
                  PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
                  PreferencesConstants.PREFERENCE_ID_DEFAULT_BENCHMARKS);

            Map<String, Serializable> defaults = defaultBenchmarkPrefs.getPreferences();

            String modelId = processDefinition.getModel()
                  .getId();
            String qualifedProcessDefinitionId = new QName(modelId, processDefinition.getId()).toString();

            String benchmarkArtifactId = null;
            if (defaults.containsKey(qualifedProcessDefinitionId))
            {
               Serializable value = defaults.get(qualifedProcessDefinitionId);

               if (value != null)
               {
                  if ("0".equals(value.toString()))
                  {
                     // use model default benchmark
                     benchmarkArtifactId = (String) defaults.get(modelId);
                  }
                  else
                  {
                     // process definition benchmark
                     benchmarkArtifactId = value.toString();
                  }
               }
            }

            if (benchmarkArtifactId != null)
            {
               DeployedRuntimeArtifact artifact = ArtifactManagerFactory.getCurrent()
                     .getActiveDeployedArtifact(BenchmarkDefinitionArtifactType.TYPE_ID,
                           benchmarkArtifactId);

               if (artifact != null)
               {
                  processInstance.setBenchmark(artifact.getOid());
               }
               else
               {
                  throw new ObjectNotFoundException(
                        BpmRuntimeError.BPMRT_BENCHMARK_NOT_FOUND.raise(options
                              .getBenchmarkId()));
               }
            }
         }
      }

      if (trace.isInfoEnabled())
      {
         trace.info("Starting process '" + processDefinition.getId() + "', oid = "
               + processInstance.getOID() + ", synchronous = " + synchronously);
      }
      IActivity startActivity = getStartActivity(processDefinition, options.getProperty(StartOptions.TRIGGER));

      ActivityThread.schedule(processInstance, startActivity, null, synchronously, null,
            Collections.EMPTY_MAP, false);

      final ProcessInstance pi = DetailsFactory.create(processInstance);

      if (isSerialExecutionScenario(processInstance))
      {
         ProcessInstanceUtils.scheduleSerialActivityThreadWorkerIfNecessary(processInstance);
      }

      return pi;
   }

   public IActivity getStartActivity(IProcessDefinition processDefinition, Serializable triggerId)
   {
      /*if (triggerId != null)
      {
         ITrigger trigger = processDefinition.findTrigger(triggerId.toString());
         Iterator<Diagram> diagrams = processDefinition.getAllDiagrams();
         while (diagrams.hasNext())
         {
            Diagram diagram = diagrams.next();
            Symbol symbol = diagram.findSymbolForUserObject(trigger);
         }
      }*/
      return processDefinition.getRootActivity();
   }

   public ProcessInstance spawnSubprocessInstance(long rootProcessInstanceOid,
         String spawnProcessID, boolean copyData, Map<String, ? > data)
         throws IllegalOperationException, ObjectNotFoundException
   {
      IProcessDefinition processDefinition = ModelUtils.getProcessDefinition(spawnProcessID);

      IProcessInstance parentProcessInstance = ProcessInstanceBean.findByOID(rootProcessInstanceOid);
      assertNotCaseProcessInstance(parentProcessInstance);
      assertNotTransientProcessInstance(parentProcessInstance);
      assertActiveProcessInstance(parentProcessInstance);

      IProcessInstance processInstance = ProcessInstanceBean.createInstance(
            processDefinition, parentProcessInstance, SecurityProperties.getUser(), data);

      if (BenchmarkUtils.isBenchmarkedPI(parentProcessInstance))
      {
         processInstance.setBenchmark(parentProcessInstance.getBenchmark());
      }

      if (copyData)
      {
         DataCopyUtils.copyDataUsingDocumentCopyHeuristics(parentProcessInstance,
               processInstance, data == null ? Collections.EMPTY_SET : data.keySet());
         DataCopyUtils.copyNotes(parentProcessInstance, processInstance);
      }
      runProcessInstance(processInstance, null);

      return DetailsFactory.create(processInstance);
   }

   public void assertNotCaseProcessInstance(IProcessInstance processInstance)
   {
      if (processInstance.isCaseProcessInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_IS_CASE.raise(
               processInstance.getOID(), processInstance.getProcessDefinition().getId()));
      }
   }

   private void assertNotTransientProcessInstance(IProcessInstance pi)
   {
      if (ProcessInstanceUtils.isTransientExecutionScenario(pi))
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_IS_TRANSIENT
               .raise(pi.getOID(), pi.getProcessDefinition().getId()));
      }
   }

   private void runProcessInstance(IProcessInstance processInstance,
         List<String> startActivitiesIds)
   {
      IProcessDefinition processDefinition = processInstance.getProcessDefinition();

      if (trace.isInfoEnabled())
      {
         trace.info("Spawning subprocess '" + processDefinition.getId() + "', oid = "
               + processInstance.getOID());
      }

      // schedule async unless it's an upgrade.
      boolean sync = false;

      IActivity startActivity = null;
      if (startActivitiesIds == null || startActivitiesIds.isEmpty())
      {
         startActivity = processDefinition.getRootActivity();
      }
      else
      {
         String firstActivityId = startActivitiesIds.get(0);
         startActivity = processDefinition.findActivity(firstActivityId);
         if (startActivity == null)
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_ACTIVITY_DEFINITION.raise(firstActivityId),
                  firstActivityId);
         }
         TransitionTarget transitionTarget = computeTransitionTarget(startActivity, startActivitiesIds);
         ExecutionPlan plan = new ExecutionPlan(transitionTarget);
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         rtEnv.setExecutionPlan(plan);
         sync = true;
      }

      ActivityThread.schedule(processInstance, startActivity, null, sync, null,
            Collections.EMPTY_MAP, sync);
   }

   private TransitionTarget computeTransitionTarget(IActivity startActivity, List<String> activitiesIds)
   {
      if (activitiesIds.size() > 1)
      {
         Stack<TransitionStep> steps = new Stack();
         IProcessDefinition processDefinition = startActivity.getProcessDefinition();
         for (String activityId : activitiesIds)
         {
            steps.add(TransitionTargetFactory.createTransitionStep(startActivity));
            startActivity = processDefinition.findActivity(activityId);
            if (startActivity == null)
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.MDL_UNKNOWN_ACTIVITY_DEFINITION.raise(activityId),
                     activityId);
            }
            if (ImplementationType.SubProcess.equals(startActivity.getImplementationType()))
            {
               processDefinition = startActivity.getImplementationProcessDefinition();
            }
         }
         return TransitionTargetFactory.createTransitionTarget(startActivity, steps, true);
      }
      else
      {
         return TransitionTargetFactory.createTransitionTarget(startActivity);
      }
   }

   public List<ProcessInstance> spawnSubprocessInstances(long rootProcessInstanceOid,
         List<SubprocessSpawnInfo> subprocessSpawnInfo) throws IllegalOperationException,
         ObjectNotFoundException
   {
      List<ProcessInstance> spawnedProcessInstances = new ArrayList<ProcessInstance>();
      if (subprocessSpawnInfo != null)
      {
         for (SubprocessSpawnInfo spawnInfo : subprocessSpawnInfo)
         {
            spawnedProcessInstances.add(spawnSubprocessInstance(rootProcessInstanceOid,
                  spawnInfo.getProcessId(), spawnInfo.isCopyData(), spawnInfo.getData()));
         }
      }
      return spawnedProcessInstances;
   }

   public synchronized ProcessInstance createCase(String name, String description,
         long[] memberOids)
   {
      if (memberOids == null || memberOids.length <= 0)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise(
               "memberOids", Arrays.toString(memberOids)));
      }

      IModel model = ModelManagerFactory.getCurrent().findActiveModel(
            PredefinedConstants.PREDEFINED_MODEL_ID);
      if (model == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_NO_MATCHING_MODEL_WITH_ID.raise(PredefinedConstants.PREDEFINED_MODEL_ID),
               PredefinedConstants.PREDEFINED_MODEL_ID);
      }
      IProcessDefinition process = model.findProcessDefinition(PredefinedConstants.CASE_PROCESS_ID);
      if (process == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_PROCESS_DEFINITION_ID.raise(PredefinedConstants.CASE_PROCESS_ID),
               PredefinedConstants.CASE_PROCESS_ID);
      }

      IUser user = SecurityProperties.getUser();

      ProcessInstanceBean group = ProcessInstanceBean.createInstance(process, user, null);

      IActivity rootActivity = getStartActivity(process, null);

      ActivityThread.schedule(group, rootActivity, null, true, null,
            Collections.EMPTY_MAP, false);

      delegateCase(group, user);

      addGroupMembers(group, memberOids);

      Map<String, String> caseInfoMap = CollectionUtils.newMap();
      caseInfoMap.put(PredefinedConstants.CASE_NAME_ELEMENT, name);
      caseInfoMap.put(PredefinedConstants.CASE_DESCRIPTION_ELEMENT, description);
      IData caseData = model.findData(PredefinedConstants.CASE_DATA_ID);
      group.setOutDataValue(caseData, null, caseInfoMap);

      if (trace.isInfoEnabled())
      {
         trace.info("Created case '" + name + "', oid = " + group.getOID());
      }
      return DetailsFactory.create(group);
   }

   public ProcessInstance joinCase(long groupOid, long[] memberOids)
   {
      if (memberOids == null || memberOids.length <= 0)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise(
               "memberOids", Arrays.toString(memberOids)));
      }

      ProcessInstanceBean group = ProcessInstanceBean.findByOID(groupOid);
      if ( !group.isCaseProcessInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_NOT_CASE
               .raise(groupOid, group.getProcessDefinition().getId()));
      }
      assertActiveProcessInstance(group);

      group.lock();

      addGroupMembers(group, memberOids);

      return DetailsFactory.create(group);
   }

   public ProcessInstance leaveCase(long groupOid, long[] memberOids)
   {
      if (memberOids == null || memberOids.length <= 0)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise(
               "memberOids", Arrays.toString(memberOids)));
      }

      ProcessInstanceBean group = ProcessInstanceBean.findByOID(groupOid);
      if ( !group.isCaseProcessInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_NOT_CASE
               .raise(groupOid, group.getProcessDefinition().getId()));
      }
      assertActiveProcessInstance(group);

      group.lock();

      for (long oid : memberOids)
      {
         ProcessInstanceBean member = ProcessInstanceBean.findByOID(oid);
         if (member.isTerminated())
         {
            // TODO: should we raise an exception here ?
         }
         if (isDirectChild(group, member))
         {
            // locking all transitions
            new ProcessInstanceLocking().lockAllTransitions(member);

            deleteRootHierarchy(group, member);
         }
         else
         {
            throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_NOT_MEMBER.raise(
                  oid, member.getProcessDefinition().getId(), groupOid,
                  group.getProcessDefinition().getId()));
         }
      }

      ProcessInstanceUtils.checkGroupTermination(group, StopMode.ABORT);

      return DetailsFactory.create(group);
   }

   private boolean isDirectChild(ProcessInstanceBean group, ProcessInstanceBean member)
   {
      IProcessInstance parentProcessInstance = null;
      IActivityInstance ai = member.getStartingActivityInstance();
      if (ai != null)
      {
         parentProcessInstance = ai.getProcessInstance();
      }
      else
      {
         parentProcessInstance = ProcessInstanceHierarchyBean.findParentForSubProcessInstanceOid(member.getOID());
      }

      if (parentProcessInstance != null && parentProcessInstance == group)
      {
         return true;
      }

      return false;
   }

   public ProcessInstance mergeCases(long targetCaseOid, long[] sourceCaseOids,
         String comment)
   {
      if (sourceCaseOids == null || sourceCaseOids.length <= 0)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise(
               "sourceCaseOids", Arrays.toString(sourceCaseOids)));
      }

      ProcessInstanceBean targetCase = ProcessInstanceBean.findByOID(targetCaseOid);
      if ( !targetCase.isCaseProcessInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_NOT_CASE
               .raise(targetCaseOid, targetCase.getProcessDefinition().getId()));
      }
      assertActiveProcessInstance(targetCase);

      IProcessInstanceLinkType linkType = ProcessInstanceLinkTypeBean.findById(PredefinedProcessInstanceLinkTypes.JOIN);

      for (long oid : sourceCaseOids)
      {
         ProcessInstanceBean sourceCase = ProcessInstanceBean.findByOID(oid);

         if (targetCaseOid == oid)
         {
            throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_IS_MEMBER
                  .raise(oid, sourceCase.getProcessDefinition().getId()));
         }
         if ( !sourceCase.isCaseProcessInstance())
         {
            throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_NOT_CASE
                  .raise(oid, sourceCase.getProcessDefinition().getId()));
         }
         if (sourceCase.isTerminated())
         {
            throw new IllegalOperationException(
                  BpmRuntimeError.BPMRT_PI_NOT_ACTIVE.raise(oid, sourceCase.getProcessDefinition().getId()));
         }

         sourceCase.lock();
         targetCase.lock();

         // DataCopy: In this case effectively only merges process attachments to target.
         DataCopyUtils.copyDataUsingNoOverrideHeuristics(sourceCase, targetCase, null);

         List<IProcessInstance> members = ProcessInstanceHierarchyBean.findChildren(sourceCase);
         for (IProcessInstance member : members)
         {
            // locking all transitions
            new ProcessInstanceLocking().lockAllTransitions(member);

            // delete complete hierarchy
            deleteRootHierarchy(sourceCase, member);
            // create complete hierarchy
            createRootHierarchy(targetCase, member);
         }
         new ProcessInstanceLinkBean(sourceCase, targetCase, linkType, comment);
         ProcessInstanceUtils.abortProcessInstance(sourceCase);
      }

      return DetailsFactory.create(targetCase);
   }

   public ProcessInstance delegateCase(long caseOid, ParticipantInfo participant)
         throws ObjectNotFoundException, AccessForbiddenException
   {
      ProcessInstanceGroupUtils.assertNotCasePerformer(participant);

      ProcessInstanceBean group = ProcessInstanceBean.findByOID(caseOid);
      if ( !group.isCaseProcessInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_NOT_CASE
               .raise(caseOid, group.getProcessDefinition().getId()));
      }
      assertActiveProcessInstance(group);

      IActivityInstance ai = ActivityInstanceBean.getDefaultGroupActivityInstance(group);
      internalDelegateToParticipant(ai, participant);

      return DetailsFactory.create(group);
   }

   private void delegateCase(IProcessInstance group, IUser user)
   {
      BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
      IActivityInstance ai = rte.getCurrentActivityInstance();
      if (ai == null)
      {
         ai = ActivityInstanceBean.getDefaultGroupActivityInstance(group);
      }
      ai.delegateToUser(user);
   }

   private void addGroupMembers(ProcessInstanceBean group, long[] memberOids)
   {
      for (long oid : memberOids)
      {
         ProcessInstanceBean member = ProcessInstanceBean.findByOID(oid);
         assertNotCaseProcessInstance(member);
         assertNotTransientProcessInstance(member);
         assertActiveProcessInstance(member);

         IProcessInstance root = member.getRootProcessInstance();
         if (root != null && root != member)
         {
            throw new IllegalOperationException(
                  BpmRuntimeError.BPMRT_PI_NOT_ROOT.raise(oid));
         }
         if (root instanceof ProcessInstanceBean
               && ((ProcessInstanceBean) root).isCaseProcessInstance())
         {
            throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_IS_MEMBER
                  .raise(oid, member.getProcessDefinition().getId()));
         }

         // locking all transitions
         new ProcessInstanceLocking().lockAllTransitions(member);

         createRootHierarchy(group, member);
      }
   }

   private void createRootHierarchy(IProcessInstance group, IProcessInstance member)
   {
      new ProcessInstanceScopeBean(member, member.getScopeProcessInstance(), group);
      ProcessInstanceScopeBean.delete(member);

      new ProcessInstanceHierarchyBean(group, member);
      ((ProcessInstanceBean) member).setRootProcessInstance((ProcessInstanceBean) group);

      List<IProcessInstance> children = ProcessInstanceHierarchyBean.findChildren(member);
      for (IProcessInstance child : children)
      {
         child.lock();

         // change ProcessInstanceScope
         new ProcessInstanceScopeBean(child, child.getScopeProcessInstance(), group);
         ProcessInstanceScopeBean.delete(child);

         // create ProcessInstanceHierarchy entry
         new ProcessInstanceHierarchyBean(group, child);
         // change ProcessInstance root
         ((ProcessInstanceBean) child).setRootProcessInstance((ProcessInstanceBean) group);
      }

      // change workitems
      List<WorkItemBean> workitems = WorkItemBean.findByProcessInstanceRootOid(member.getOID());
      for (WorkItemBean workItemBean : workitems)
      {
         workItemBean.setRootProcessInstance(group.getOID());
      }
   }

   private void deleteRootHierarchy(IProcessInstance group, IProcessInstance member)
   {
      new ProcessInstanceScopeBean(member, member.getScopeProcessInstance(), member);
      ProcessInstanceScopeBean.delete(member);

      ProcessInstanceHierarchyBean.delete(group, member);
      ((ProcessInstanceBean) member).setRootProcessInstance((ProcessInstanceBean) member);

      List<IProcessInstance> children = ProcessInstanceHierarchyBean.findChildren(member);
      for (IProcessInstance child : children)
      {
         child.lock();

         // change ProcessInstanceScope
         new ProcessInstanceScopeBean(child, child.getScopeProcessInstance(), member);
         ProcessInstanceScopeBean.delete(child);

         // create ProcessInstanceHierarchy entry
         ProcessInstanceHierarchyBean.delete(group, child);
         // change ProcessInstance root
         ((ProcessInstanceBean) child).setRootProcessInstance((ProcessInstanceBean) member);
      }

      // change workitems
      List<WorkItemBean> workitems = WorkItemBean.findByProcessInstanceRootOid(group.getOID());
      for (WorkItemBean workItemBean : workitems)
      {
         workItemBean.setRootProcessInstance(member.getOID());
      }

   }

   public void assertActiveProcessInstance(IProcessInstance processInstance)
   {
      assertNotHalted(processInstance);

      if (ProcessInstanceState.ACTIVE != processInstance.getState().getValue())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_NOT_ACTIVE.raise(
               processInstance.getOID(), processInstance.getProcessDefinition().getId()));
      }
   }

   private void assertNotHalted(IProcessInstance pi)
   {
      if (pi.isHalted() || pi.isHalting())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_PI_IS_HALTED.raise(pi.getOID()));
      }
   }

   public ProcessInstance spawnPeerProcessInstance(long processInstanceOid,
         String spawnProcessID, boolean copyData,
         Map<String, ? extends Serializable> data, boolean abortProcessInstance,
         String comment) throws IllegalOperationException, ObjectNotFoundException,
         InvalidArgumentException
   {
      DataCopyOptions dataCopyOptions = new DataCopyOptions(copyData, null, data, true);
      SpawnOptions options = new SpawnOptions(null, abortProcessInstance
            ? SpawnMode.ABORT
            : SpawnMode.KEEP, comment, dataCopyOptions);
      return spawnPeerProcessInstance(processInstanceOid, spawnProcessID, options);
   }

   public ProcessInstance spawnPeerProcessInstance(long processInstanceOid,
         String spawnProcessID, SpawnOptions options) throws IllegalOperationException,
         ObjectNotFoundException, InvalidArgumentException
   {
      // check if the target process is specified.
      if (spawnProcessID == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_PROCESS_DEFINITION_ID.raise(spawnProcessID),
               spawnProcessID);
      }
      QName qname = QName.valueOf(spawnProcessID);

      // check if the source process instance exists.
      IProcessInstance originatingProcessInstance = ProcessInstanceBean.findByOID(processInstanceOid);

      // check if the source process instance is a case.
      assertNotCaseProcessInstance(originatingProcessInstance);

      // check if the source process instance is a transient process instance.
      assertNotTransientProcessInstance(originatingProcessInstance);

      // check if the source process instance is a root process instance.
      IProcessInstance rootPI = originatingProcessInstance.getRootProcessInstance();
      if (rootPI != null && rootPI != originatingProcessInstance)
      {
         if ( !rootPI.isCaseProcessInstance()
               || originatingProcessInstance.getStartingActivityInstance() != null)
         {
            throw new IllegalOperationException(
                  BpmRuntimeError.BPMRT_PI_NOT_ROOT.raise(processInstanceOid));
         }
      }

      // check if the source process instance is terminated.
      if (originatingProcessInstance.isTerminated())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_PI_IS_ALREADY_TERMINATED.raise(processInstanceOid));
      }

      // retrieve model of originating process instance.
      IProcessDefinition originatingProcessDefinition = originatingProcessInstance.getProcessDefinition();
      IModel model = (IModel) originatingProcessDefinition.getModel();

      String modelId = qname.getNamespaceURI();
      if (!modelId.equals(XMLConstants.NULL_NS_URI))
      {
         ModelManager mm = ModelManagerFactory.getCurrent();
         model = mm.findActiveModel(modelId);
         if (model == null)
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_NO_ACTIVE_MODEL_WITH_ID.raise(modelId), modelId);
         }
      }

      // retrieve targeted process definition.
      String processId = qname.getLocalPart();
      IProcessDefinition processDefinition = model.findProcessDefinition(processId);
      if (processDefinition == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_PROCESS_DEFINITION_ID.raise(processId),
               processId);
      }

      if (options == null)
      {
         options = SpawnOptions.DEFAULT;
      }

      // Determine Link Type
      PredefinedProcessInstanceLinkTypes linkType = null;
      if (options.isAbortProcessInstance())
      {
         if (processId.equals(originatingProcessDefinition.getId()))
         {
            linkType = PredefinedProcessInstanceLinkTypes.UPGRADE;
         }
         else
         {
            linkType = PredefinedProcessInstanceLinkTypes.SWITCH;
         }
      }
      else if (options.isHaltProcessInstance())
      {
         linkType = PredefinedProcessInstanceLinkTypes.INSERT;
      }
      else
      {
         linkType = PredefinedProcessInstanceLinkTypes.SPAWN;
      }

      // Handle aborting or halting of originating process.
      if (options.isAbortProcessInstance() || options.isHaltProcessInstance())
      {
         // check authorization
         BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
         Authorization2Predicate authorizationPredicate = runtimeEnvironment.getAuthorizationPredicate();
         if (authorizationPredicate != null)
         {
            authorizationPredicate.check(originatingProcessInstance);
         }

         StopMode stopMode = options.isAbortProcessInstance() ? StopMode.ABORT : StopMode.HALT;
         internalStopProcessInstance(processInstanceOid, AbortScope.RootHierarchy, stopMode);
      }

      // Handle data copy
      DataCopyOptions dco = options.getDataCopyOptions();
      if (dco == null)
      {
         dco = DataCopyOptions.DEFAULT;
      }
      DataCopyResult data = DataCopyUtils.copyData(originatingProcessInstance, model, dco);
      IProcessInstance processInstance = ProcessInstanceBean.createInstance(
            processDefinition, (IProcessInstance) null, SecurityProperties.getUser(),
            data.result);
      assertNotTransientProcessInstance(processInstance);
      processInstance.setPriority(originatingProcessInstance.getPriority());

      if (dco.copyAllData())
      {
         if (dco.useHeuristics())
         {
            DataCopyUtils.copyDataUsingDocumentCopyHeuristics(originatingProcessInstance,
                  processInstance, data == null
                        ? Collections.EMPTY_SET
                        : data.ignoreDataIds);
         }
         DataCopyUtils.copyNotes(originatingProcessInstance, processInstance);
      }

      // create process instance link
      IProcessInstanceLinkType link = ProcessInstanceLinkTypeBean.findById(linkType);
      new ProcessInstanceLinkBean(originatingProcessInstance, processInstance, link,
            options.getComment());

      // run the target process instance
      ProcessStateSpec spec = options.getProcessStateSpec();
      Iterator<List<String>> itr = spec.iterator();
      if (!itr.hasNext())
      {
         runProcessInstance(processInstance, null);
      }
      else
      {
         while (true)
         {
            List<String> targets = itr.next();
            runProcessInstance(processInstance, targets);
            if (itr.hasNext())
            {
               TransitionTokenBean.createStartToken(processInstance);
            }
            else
            {
               break;
            }
         }
      }

      return DetailsFactory.create(processInstance);
   }

   public ProcessInstance joinProcessInstance(long processInstanceOid,
         long targetProcessInstanceOid, String comment)
   {
      IProcessInstance originatingProcessInstance = ProcessInstanceBean.findByOID(processInstanceOid);
      IProcessInstance targetProcessInstance = ProcessInstanceBean.findByOID(targetProcessInstanceOid);

      // illegal to join to self
      if (processInstanceOid == targetProcessInstanceOid)
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_PI_JOIN_TO_SAME_PROCESS_INSTANCE.raise(processInstanceOid));
      }

      assertNotHalted(originatingProcessInstance);
      assertNotHalted(targetProcessInstance);

      // illegal to join from an interrupted process instance
      if (ProcessInstanceState.ACTIVE != originatingProcessInstance.getState().getValue())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_PI_NOT_ACTIVE.raise(processInstanceOid,
                     originatingProcessInstance.getProcessDefinition().getId()));
      }

      // illegal to join an interrupted process instance
      if (ProcessInstanceState.ACTIVE != targetProcessInstance.getState().getValue())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_PI_NOT_ACTIVE.raise(targetProcessInstanceOid,
                     targetProcessInstance.getProcessDefinition().getId()));
      }

      // illegal to join own subprocess
      if (ProcessInstanceHierarchyBean.isSubprocess(originatingProcessInstance,
            targetProcessInstance))
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_PI_JOIN_TO_CHILD_INSTANCE.raise(processInstanceOid));
      }

      // illegal to join from case process instance
      assertNotCaseProcessInstance(originatingProcessInstance);

      // illegal from join from case process instance
      assertNotCaseProcessInstance(targetProcessInstance);

      // illegal to join from a transient process instance
      assertNotTransientProcessInstance(originatingProcessInstance);

      // illegal to join to a transient process instance
      assertNotTransientProcessInstance(targetProcessInstance);

      // check authorization
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate authorizationPredicate = runtimeEnvironment.getAuthorizationPredicate();
      if (authorizationPredicate != null)
      {
         authorizationPredicate.check(originatingProcessInstance);
      }

      IProcessInstanceLinkType linkType = null;
      try
      {
         linkType = ProcessInstanceLinkTypeBean.findById(PredefinedProcessInstanceLinkTypes.JOIN);
      }
      catch (ObjectNotFoundException e)
      {
         // Only fallback. Actually done in SchemaHelper.createSchema() to avoid
         // concurrency issues.
         linkType = new ProcessInstanceLinkTypeBean(
               PredefinedProcessInstanceLinkTypes.JOIN.getId(),
               PredefinedProcessInstanceLinkTypes.JOIN.getDescription());
      }

      abortProcessInstance(processInstanceOid, AbortScope.RootHierarchy);
      DataCopyUtils.copyDataUsingNoOverrideHeuristics(originatingProcessInstance,
            targetProcessInstance, null);
      IProcessInstance linkSource = originatingProcessInstance.getRootProcessInstance()
            .isCaseProcessInstance()
            ? originatingProcessInstance
            : originatingProcessInstance.getRootProcessInstance();
      new ProcessInstanceLinkBean(linkSource, targetProcessInstance, linkType, comment);

      return DetailsFactory.create(targetProcessInstance);
   }

   public ActivityInstance activate(long oid) throws ObjectNotFoundException
   {
      IActivityInstance activityInstance = ActivityInstanceUtils.lock(oid);
      ActivityInstanceUtils.assertNotHalted(activityInstance);

      activate(activityInstance);

      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ActivityInstance complete(long activityInstanceOID, String context,
         Map<String, ? > outData) throws ObjectNotFoundException, InvalidValueException
   {
      // TODO merge code with next method for release 3.5 (not immediately done for safety
      // reasons)

      IActivityInstance activityInstance = ActivityInstanceUtils.lock(activityInstanceOID);

      complete(activityInstance, context, outData, true);

      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ActivityCompletionLog complete(long activityInstanceOID, String context,
         Map<String, ? > outData, int flags) throws ObjectNotFoundException,
         InvalidValueException
   {
      ActivityThreadContext threadContext = ActivityThread.getCurrentActivityThreadContext();

      PerformedActivitiesListener listener = new PerformedActivitiesListener();
      try
      {
         if (null != threadContext)
         {
            threadContext.addWorkflowEventListener(listener);
         }

         IActivityInstance activityInstance = ActivityInstanceUtils.lock(activityInstanceOID);

         complete(activityInstance, context, outData, true);

         return createCompletionLog(flags, activityInstance, listener.getActivities());
      }
      finally
      {
         if ((null != threadContext) && (null != listener))
         {
            threadContext.removeWorkflowEventListener(listener);
         }
      }
   }

   public ActivityInstance activateAndComplete(long activityInstanceOID, String context,
         Map<String, ? > outData) throws ObjectNotFoundException
   {
      // TODO merge code with next method for release 3.5 (not immediately done for safety
      // reasons)

      return (ActivityInstance) DetailsFactory.create(
            activateAndComplete(activityInstanceOID, context, outData, true),
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ActivityCompletionLog activateAndComplete(long activityInstanceOID,
         String context, Map<String, ? > outData, int flags)
         throws ObjectNotFoundException
   {
      ActivityThreadContext threadContext = ActivityThread.getCurrentActivityThreadContext();

      PerformedActivitiesListener listener = new PerformedActivitiesListener();
      try
      {
         if (null != threadContext)
         {
            threadContext.addWorkflowEventListener(listener);
         }

         IActivityInstance activityInstance = activateAndComplete(activityInstanceOID,
               context, outData, true);

         return createCompletionLog(flags, activityInstance, listener.getActivities());
      }
      finally
      {
         if ((null != threadContext) && (null != listener))
         {
            threadContext.removeWorkflowEventListener(listener);
         }
      }
   }

   public IActivityInstance activateAndComplete(long activityInstanceOID, String context,
         Map<String, ? > outData, boolean synchronously) throws ObjectNotFoundException,
         InvalidValueException
   {
      IActivityInstance activityInstance = ActivityInstanceUtils.lock(activityInstanceOID);

      activate(activityInstance);
      complete(activityInstance, context, outData, synchronously);

      return activityInstance;
   }

   private void activate(IActivityInstance activityInstance)
   {
      ActivityInstanceUtils.assertNotHalted(activityInstance);
      ActivityInstanceUtils.assertNotTerminated(activityInstance);
      ActivityInstanceUtils.assertNotInAbortingProcess(activityInstance);
      ActivityInstanceUtils.assertNotActivatedByOther(activityInstance);
      ActivityInstanceUtils.assertNoSubprocess(activityInstance);
      ActivityInstanceUtils.assertNotOnOtherUserWorklist(activityInstance, false);
      ActivityInstanceUtils.assertNotDefaultCaseInstance(activityInstance);

      IUser currentUser = SecurityProperties.getUser();
      if (activityInstance.getActivity().isInteractive() && currentUser != null
            && currentUser.getOID() != 0
            && activityInstance.getCurrentUserPerformerOID() != currentUser.getOID())
      {
         if (activityInstance instanceof ActivityInstanceBean)
         {
            ((ActivityInstanceBean) activityInstance).internalDelegateToUser(currentUser,
                  false);
         }
         else
         {
            activityInstance.delegateToUser(currentUser);
         }
      }
      if (activityInstance.getState() != ActivityInstanceState.Application)
      {
         activityInstance.activate();
      }
   }

   private void complete(IActivityInstance activityInstance, String context,
         Map<String, ? > outData, boolean synchronously) throws ObjectNotFoundException,
         InvalidValueException, AccessForbiddenException
   {
      ActivityInstanceUtils.assertNotTerminated(activityInstance);
      ActivityInstanceUtils.assertNotInAbortingProcess(activityInstance);
      ActivityInstanceUtils.assertNotDefaultCaseInstance(activityInstance);
      ActivityInstanceUtils.assertPerformAllowedIfHalted(activityInstance);

      // TODO rsauer fix for special scenario from CSS, involving automatic completion
      // of activities after a certain period of time, while the activity sticky to the
      // predecessor activitie's user worklist.
      // Consider adding a well known identity to all daemons to avoid problems.
      IUser user = SecurityProperties.getUser();
      if (null != user && 0 != user.getOID())
      {
         ActivityInstanceUtils.assertNotActivatedByOther(activityInstance, true);
      }

      if (activityInstance.getState() == ActivityInstanceState.Application
            || activityInstance.isHalted())
      {
         ActivityInstanceUtils.complete(activityInstance, context, outData, synchronously);
      }
      else
      {
         throw new IllegalStateChangeException(activityInstance.toString(),
               ActivityInstanceState.Completed, activityInstance.getState());
      }
   }

   public ActivityInstance suspendToParticipant(long activityInstanceOID,
         String participantID) throws ObjectNotFoundException, ConcurrencyException,
         AccessForbiddenException
   {
      return suspendToParticipant(activityInstanceOID,
            new RoleInfoDetails(participantID), null);
   }

   public ActivityInstance suspendToParticipant(long activityInstanceOID,
         String participantID, String context, Map<String, ? > outData)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException
   {
      return suspendToParticipant(activityInstanceOID,
            new RoleInfoDetails(participantID), new ContextData(context, outData));
   }

   public ActivityInstance suspend(long activityInstanceOID, ContextData data)
         throws ObjectNotFoundException, AccessForbiddenException
   {
      IActivityInstance activityInstance = ActivityInstanceUtils.lock(activityInstanceOID);

      ActivityInstanceUtils.assertNotTerminated(activityInstance);
      ActivityInstanceUtils.assertNotInAbortingProcess(activityInstance);
      ActivityInstanceUtils.assertNotActivatedByOther(activityInstance);
      ActivityInstanceUtils.assertNotDefaultCaseInstance(activityInstance);
      ActivityInstanceUtils.assertPerformAllowedIfHalted(activityInstance);

      if (data != null)
      {
         ActivityInstanceUtils.setOutDataValues(data.getContext(), data.getData(),
               activityInstance, true);
      }

      IParticipant participant = null;
      IDepartment department = null;
      long currentUser = activityInstance.getCurrentUserPerformerOID();

      Iterator<ActivityInstanceHistoryBean> history = ActivityInstanceHistoryBean.getAllForActivityInstance(
            activityInstance, false);
      while (history.hasNext())
      {
         ActivityInstanceHistoryBean aih = history.next();
         if (ActivityInstanceState.Application == aih.getState())
         {
            IParticipant performer = aih.getPerformer();
            if (performer instanceof IUser && ((IUser) performer).getOID() != currentUser)
            {
               participant = performer;
            }
            else
            {
               continue;
            }
         }
         else if (ActivityInstanceState.Suspended == aih.getState())
         {
            participant = aih.getPerformer();
            department = aih.getDepartment();
            if (department == null)
            {
               department = IDepartment.NULL;
            }
         }
         break;
      }

      boolean halted = activityInstance.isHalted();
      activityInstance.suspend();

      if (participant == null)
      {
         activityInstance.delegateToDefaultPerformer();
      }
      else if (participant instanceof IUser)
      {
         if (activityInstance instanceof ActivityInstanceBean)
         {
            ((ActivityInstanceBean) activityInstance).internalDelegateToUser(
                  (IUser) participant, false);
         }
         else
         {
            activityInstance.delegateToUser((IUser) participant);
         }
      }
      else if (participant instanceof IUserGroup)
      {
         activityInstance.delegateToUserGroup((IUserGroup) participant);
      }
      else if (participant instanceof IModelParticipant)
      {
         activityInstance.delegateToParticipant((IModelParticipant) participant,
               department, department);
      }
      else
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_USER_GROUP_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
                     ((UserGroupInfo) participant).getId(),
                     ((UserGroupInfo) participant).getOID(), activityInstance.getOID(),
                     activityInstance.getActivity().getId()));
      }

      if (halted)
      {
         activityInstance.halt();
      }
      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ActivityInstance suspendToParticipant(long activityInstanceOID,
         ParticipantInfo participant, ContextData data) throws ObjectNotFoundException,
         AccessForbiddenException
   {
      IActivityInstance activityInstance = ActivityInstanceUtils.lock(activityInstanceOID);

      ActivityInstanceUtils.assertNotTerminated(activityInstance);
      ActivityInstanceUtils.assertNotInAbortingProcess(activityInstance);
      ActivityInstanceUtils.assertNotActivatedByOther(activityInstance);
      ActivityInstanceUtils.assertNotDefaultCaseInstance(activityInstance);
      ProcessInstanceGroupUtils.assertNotCasePerformer(participant);
      ActivityInstanceUtils.assertPerformAllowedIfHalted(activityInstance);

      if (data != null)
      {
         ActivityInstanceUtils.setOutDataValues(data.getContext(), data.getData(),
               activityInstance, true);
      }
      boolean halted = activityInstance.isHalted();
      activityInstance.suspend();
      delegateToParticipant(activityInstance.getOID(), participant);
      if (halted)
      {
         activityInstance.halt();
      }
      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ActivityInstance suspendToUser(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException
   {
      return suspendToParticipant(activityInstanceOID, new UserInfoDetails(
            SecurityProperties.getUserOID()), null);
   }

   public ActivityInstance suspendToUser(long activityInstanceOID, String context,
         Map<String, ? > outData) throws ObjectNotFoundException, ConcurrencyException,
         AccessForbiddenException
   {
      return suspendToParticipant(activityInstanceOID, new UserInfoDetails(
            SecurityProperties.getUserOID()), new ContextData(context, outData));
   }

   public ActivityInstance suspendToDefaultPerformer(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException
   {
      return suspendToParticipant(activityInstanceOID, null, null);
   }

   public ActivityInstance suspendToDefaultPerformer(long activityInstanceOID,
         String context, Map<String, ? > outData) throws ObjectNotFoundException,
         ConcurrencyException, AccessForbiddenException
   {
      return suspendToParticipant(activityInstanceOID, null, new ContextData(context,
            outData));
   }

   public ActivityInstance suspendToUser(long activityInstanceOID, long userOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException
   {
      return suspendToParticipant(activityInstanceOID, new UserInfoDetails(userOID), null);
   }

   public ActivityInstance suspendToUser(long activityInstanceOID, long userOID,
         String context, Map<String, ? > outData) throws ObjectNotFoundException,
         ConcurrencyException, AccessForbiddenException
   {
      return suspendToParticipant(activityInstanceOID, new UserInfoDetails(userOID),
            new ContextData(context, outData));
   }

   public ActivityInstance delegateToParticipant(long oid, final String participantID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException
   {
      return delegateToParticipant(oid, new RoleInfoDetails(participantID));
   }

   public ActivityInstance delegateToParticipant(long oid, ParticipantInfo participant)
         throws ObjectNotFoundException, AccessForbiddenException
   {
      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(oid);
      ActivityInstanceUtils.assertNotDefaultCaseInstance(activityInstance);

      internalDelegateToParticipant(activityInstance, participant);

      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   private void internalDelegateToParticipant(IActivityInstance activityInstance,
         ParticipantInfo participant)
   {
      ActivityInstanceUtils.assertNotTerminated(activityInstance);
      ActivityInstanceUtils.assertNotInAbortingProcess(activityInstance);
      ActivityInstanceUtils.assertNotActivatedByOther(activityInstance);
      ActivityInstanceUtils.assertNotActivated(activityInstance);
      ActivityInstanceUtils.assertNotInUserWorklist(activityInstance, participant);
      ProcessInstanceGroupUtils.assertNotCasePerformer(participant);

      if (participant == null)
      {
         activityInstance.delegateToDefaultPerformer();
      }
      else if (participant instanceof UserInfo)
      {
         IUser user = UserBean.findByOid(((UserInfo) participant).getOID());
         // if qa instance - check if delegation is allow
         QualityAssuranceUtils.assertDelegationIsAllowed(activityInstance, user);
         activityInstance.delegateToUser(user);
      }
      else if (participant instanceof ModelParticipantInfo)
      {

         long modelOid = 0;

         if ( !activityInstance.isDefaultCaseActivityInstance())
         {
            // if no default Case Activity Instance use the modelOid to define the allowed
            // range of delegation
            modelOid = activityInstance.getActivity().getModel().getModelOID();
         }

         ScopedModelParticipant scopedParticipant = (ScopedModelParticipant) DepartmentUtils.getScopedParticipant(
               participant, ModelManagerFactory.getCurrent(), modelOid);
         IModelParticipant modelParticipant = scopedParticipant.getModelParticipant();

         IDepartment department = scopedParticipant.getDepartment();
         activityInstance.delegateToParticipant(modelParticipant, department, null);
      }
      else
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_USER_GROUP_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
                     ((UserGroupInfo) participant).getId(),
                     ((UserGroupInfo) participant).getOID(), activityInstance.getOID(),
                     activityInstance.getActivity().getId()));
      }
   }

   public ActivityInstance delegateToUser(long oid, final long userOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException
   {
      return delegateToParticipant(oid, new UserInfoDetails(userOID));
   }

   public ActivityInstance delegateToDefaultPerformer(long oid)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException
   {
      return delegateToParticipant(oid, (ParticipantInfo) null);
   }

   public Worklist getWorklist(final WorklistQuery query)
   {
      ForkingServiceFactory factory = (ForkingServiceFactory)
            Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
      ForkingService forkingService = factory.get();

      return (Worklist) forkingService.isolate(new Action()
            {
               public Object execute()
               {
                  return new WorklistQueryEvaluator(query, new EvaluationContext(
                        ModelManagerFactory.getCurrent(), SecurityProperties.getUser())).buildWorklist();
               }
            });
   }

   public ActivityInstance activateNextActivityInstance(WorklistQuery query)
   {
      int retry = Parameters.instance().getInteger(
            ACTIVATE_NEXT_ACTIVITY_INSTANCE_RETRIES, 5);
      boolean loop = false;

      ActivityInstance result = null;

      EmbeddedServiceFactory sf = new EmbeddedServiceFactory();
      WorkflowService embeddedWorkflowService = sf.getService(WorkflowService.class);

      Worklist wl = embeddedWorkflowService.getWorklist(query);
      int cumulatedSize = wl.getCumulatedSize();

      if (cumulatedSize == 0)
      {
         retry = 0;
      }

      for (int r = 0; r < retry; r++ )
      {
         if (loop)
         {
            wl = embeddedWorkflowService.getWorklist(query);
            cumulatedSize = wl.getCumulatedSize();
            if (cumulatedSize == 0)
            {
               break;
            }
         }

         for (Iterator i = wl.getCumulatedItems().iterator(); i.hasNext()
               && result == null;)
         {
            ActivityInstance ai = (ActivityInstance) i.next();
            try
            {
               long lastModTimeFromQuery = ai.getLastModificationTime().getTime();

               ActivityInstanceBean activityInstance = ActivityInstanceBean.findByOID(ai.getOID());

               // compare if AI was changed in the meantime - no lock
               if (isAiModified(lastModTimeFromQuery, activityInstance))
               {
                  continue;
               }

               // AI still unchanged - now lock and check again
               activityInstance.lock();
               if (isAiModified(lastModTimeFromQuery, activityInstance))
               {
                  continue;
               }

               activate(activityInstance);

               result = (ActivityInstance) DetailsFactory.create(activityInstance,
                     IActivityInstance.class, ActivityInstanceDetails.class);

               break;
            }
            catch (AccessForbiddenException ae)
            {
               continue;
            }
            catch (ConcurrencyException ce)
            {
               continue;
            }
            catch (PhantomException e)
            {
               continue;
            }
         }

         if (result != null)
         {
            break;
         }
         loop = true;
      }

      return result;
   }

   /**
    * This reloads the attribute "lastModificationTime" and compares it with given
    * timestamp.
    *
    * @param lastModTimeFromQuery
    *           the modification timestamp to compare with
    * @param activityInstance
    *           the activity instance to be compared on modification based on timestamps
    * @return <code>true</code> if modified, otherwise <code>false</code>
    * @throws PhantomException
    *            thrown if activity instance is no longer available
    */
   private boolean isAiModified(long lastModTimeFromQuery,
         ActivityInstanceBean activityInstance) throws PhantomException
   {
      activityInstance.reloadAttribute(ActivityInstanceBean.FIELD__LAST_MODIFICATION_TIME);
      return lastModTimeFromQuery != activityInstance.getLastModificationTime().getTime();
   }

   public Serializable getInDataValue(long activityInstanceOID, String context, String id)
         throws ObjectNotFoundException
   {
      Map<String, Serializable> values = getInDataValues(activityInstanceOID, context,
            Collections.singleton(id));
      return (Serializable) values.get(id);
   }

   public Map<String, Serializable> getInDataValues(long activityInstanceOID,
         String context, Set<String> ids) throws ObjectNotFoundException
   {
      IActivityInstance ai = ActivityInstanceBean.findByOID(activityInstanceOID);

      if (null == context)
      {
         context = PredefinedConstants.DEFAULT_CONTEXT;
      }

      IActivity activity = ai.getActivity();

      if (null == activity.getContext(context))
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_APP_CONTEXT.raise(context));
      }
      else if ((null != ids) && !ids.isEmpty())
      {
         SortedSet<String> unresolvedIds = new TreeSet<String>(ids);

         ModelElementList inDataMappings = activity.getInDataMappings();
         for (int i = 0; i < inDataMappings.size(); ++i)
         {
            IDataMapping dataMapping = (IDataMapping) inDataMappings.get(i);
            if (CompareHelper.areEqual(context, dataMapping.getContext()))
            {
               unresolvedIds.remove(dataMapping.getId());
            }
         }

         if ( !unresolvedIds.isEmpty())
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_IN_DATA_MAPPING.raise(
                        unresolvedIds.first(), context, activity.getId(), activityInstanceOID));
         }
      }

      Map<String, Serializable> dataValues = CollectionUtils.newHashMap();

      ModelElementList inDataMappings = activity.getInDataMappings();
      for (int i = 0; i < inDataMappings.size(); ++i)
      {
         IDataMapping dataMapping = (IDataMapping) inDataMappings.get(i);

         if (CompareHelper.areEqual(context, dataMapping.getContext()))
         {
            if ((null == ids) || ids.contains(dataMapping.getId()))
            {
               Serializable inDataValue = null;
               if(ActivityInstanceUtils.isMandatoryDatamapping(dataMapping, activityInstanceOID))
               {
                  if(dataMapping.getData() != null)
                  {
                     ProcessInstanceBean processInstance = ProcessInstanceBean.findByOID(ai.getProcessInstanceOID());
                     IDataValue cachedDataValue = processInstance.getCachedDataValue(dataMapping.getData().getId());
                     if(cachedDataValue == null)
                     {
                        cachedDataValue = processInstance.findDataValue(dataMapping.getData());
                     }

                     if(cachedDataValue != null)
                     {
                        inDataValue = cachedDataValue.getSerializedValue();
                     }
                  }
               }
               else
               {
                  inDataValue = getInDataValue(dataMapping, ai.getProcessInstanceOID());
               }
               dataValues.put(dataMapping.getId(), inDataValue);
            }
         }
      }

      return dataValues;
   }

   private Serializable getInDataValue(IDataMapping dataMapping, long processInstanceOID)
         throws ObjectNotFoundException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processInstanceOID);

      Object value = processInstance.getInDataValue(dataMapping.getData(),
            dataMapping.getDataPath());

      return (Serializable) value;
   }

   public Object getInDataPath(long processOID, String id) throws ObjectNotFoundException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processOID);
      IProcessDefinition processDefinition = processInstance.getProcessDefinition();

      if (processInstance.isCaseProcessInstance())
      {
         IDataPath path = processDefinition.findDataPath(id, Direction.IN);

         if (path != null)
         {
            return getInDataPath(processInstance, path);
         }
         else
         {
            return ProcessInstanceGroupUtils.getPrimitiveDescriptor(processInstance, id);
         }
      }
      else
      {
         // @todo (france, ub): find data path by id *and* direction
         IDataPath path = processDefinition.findDataPath(id, Direction.IN);
         if (path == null)
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_DATA_PATH.raise(id, processDefinition.getId(), processOID));
         }
         return getInDataPath(processInstance, path);
      }
   }

   public Map<String, Serializable> getInDataPaths(long processOID, Set<String> ids)
         throws ObjectNotFoundException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processOID);
      IProcessDefinition processDefinition = processInstance.getProcessDefinition();

      if (processInstance.isCaseProcessInstance())
      {
         HashMap primitiveDescriptors = new HashMap(
               ProcessInstanceGroupUtils.getPrimitiveDescriptors(processInstance, ids));

         ModelElementList allDataPaths = processDefinition.getDataPaths();
         for (int i = 0; i < allDataPaths.size(); i++ )
         {
            final IDataPath path = (IDataPath) allDataPaths.get(i);
            if ((Direction.IN.equals(path.getDirection()) || Direction.IN_OUT.equals(path.getDirection()))
                  && ((null == ids) || ids.contains(path.getId())))
            {
               primitiveDescriptors.put(path.getId(),
                     getInDataPath(processInstance, path));
            }
         }
         return primitiveDescriptors;
      }
      else
      {

         Set<String> requestedIds = new HashSet<String>();
         if (null != ids)
         {
            requestedIds.addAll(ids);
         }

         List<IDataPath> requestedDataPaths = new ArrayList<IDataPath>();
         ModelElementList allDataPaths = processDefinition.getDataPaths();
         for (int i = 0; i < allDataPaths.size(); i++ )
         {
            final IDataPath path = (IDataPath) allDataPaths.get(i);
            if ((Direction.IN.equals(path.getDirection()) || Direction.IN_OUT.equals(path.getDirection()))
                  && ((null == ids) || ids.contains(path.getId())))
            {
               requestedIds.remove(path.getId());
               requestedDataPaths.add(path);
            }
         }

         if ( !requestedIds.isEmpty())
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_IN_DATA_PATH.raise(requestedIds.iterator()
                        .next(), processDefinition.getName(), processOID));
         }

         // prefetch data values in batch to improve performance
         List<IData> dataItems = new ArrayList<IData>(requestedDataPaths.size());
         for (IDataPath path : requestedDataPaths)
         {
            if (path.getData() != null)
            {
               dataItems.add(path.getData());
            }
         }
         processInstance.preloadDataValues(dataItems);

         Map<String, Serializable> values = new HashMap<String, Serializable>(
               requestedDataPaths.size());
         for (IDataPath path : requestedDataPaths)
         {
            values.put(path.getId(), getInDataPath(processInstance, path));
         }

         return values;
      }
   }

   private Serializable getInDataPath(IProcessInstance processInstance, IDataPath path)
   {
      return new CompositeDataPathEvaluator(processInstance).getDataPathValue(path);
   }

   public void setOutDataPath(long processOID, String id, Object value)
         throws ObjectNotFoundException, InvalidValueException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processOID);
      IProcessDefinition processDefinition = processInstance.getProcessDefinition();

      if (processInstance.isCaseProcessInstance())
      {
         IDataPath path = processDefinition.findDataPath(id, Direction.OUT);
         if (path != null)
         {
            setOutDataPath(value, processInstance, path);
         }
         else
         {
            ProcessInstanceGroupUtils.setPrimitiveDescriptor(processInstance, id, value);
         }
      }
      else
      {

         // @todo (france, ub): find data path by id *and* direction
         IDataPath path = processDefinition.findDataPath(id, Direction.OUT);
         if (path == null)
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_OUT_DATA_PATH.raise(id, processDefinition.getId(), processOID));
         }

         setOutDataPath(value, processInstance, path);
      }
   }

   public void setOutDataPaths(long processOID, Map<String, ? > values)
         throws ObjectNotFoundException, InvalidValueException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processOID);
      IProcessDefinition processDefinition = processInstance.getProcessDefinition();

      if (processInstance.isCaseProcessInstance())
      {
         Map<String, Object> primitiveValues = CollectionUtils.newMap();

         for (Map.Entry<String, ? > entry : values.entrySet())
         {
            IDataPath path = processDefinition.findDataPath(entry.getKey(), Direction.OUT);
            if (path != null)
            {
               setOutDataPath(entry.getValue(), processInstance, path);
            }
            else
            {
               primitiveValues.put(entry.getKey(), (Object) entry.getValue());
            }
         }
         if ( !primitiveValues.isEmpty())
         {
            ProcessInstanceGroupUtils.setPrimitiveDescriptors(processInstance,
                  primitiveValues);
         }
      }
      else
      {
         // @todo (france, ub): find data path by id *and* direction
         for (Iterator< ? > i = values.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry<String, ? > entry = (Map.Entry<String, ? >) i.next();
            IDataPath path = processDefinition.findDataPath(entry.getKey(), Direction.OUT);
            if (path == null)
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.MDL_UNKNOWN_OUT_DATA_PATH.raise(entry.getKey(),
                           processDefinition.getId(), processOID));
            }

            setOutDataPath(entry.getValue(), processInstance, path);
         }
      }
   }

   private void setOutDataPath(Object value, IProcessInstance processInstance,
         IDataPath path) throws ObjectNotFoundException, InvalidValueException
   {
      IData data = path.getData();
      if (data == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_DANGLING_OUT_DATA_PATH.raise(path));
      }

      processInstance.setOutDataValue(data, path.getAccessPath(), value);

      AuditTrailLogger.getInstance(LogCode.DATA, processInstance)
            .info(MessageFormat.format(
                  "Data ''{0}'' of Process Instance ''{1}'' has been changed through datapath ''{2}''",
                  new Object[] {data.getId(), processInstance.getOID(), path.getId()}));
   }

   public DeployedModel getModel()
   {
      return QueryServiceImpl.getActiveModel(true);
   }

   public ActivityInstance abortActivityInstance(long activityInstanceOid)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException
   {
      return abortActivityInstance(activityInstanceOid, AbortScope.RootHierarchy);
   }

   public ActivityInstance abortActivityInstance(long activityInstanceOID,
         AbortScope abortScope) throws ObjectNotFoundException, ConcurrencyException,
         AccessForbiddenException
   {
      IActivityInstance activityInstance = ActivityInstanceUtils.lock(activityInstanceOID);

      if (activityInstance.isDefaultCaseActivityInstance())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_AI_CAN_NOT_BE_ABORTED_BY_USER
                     .raise(activityInstanceOID, activityInstance.getActivity().getId()));
      }

      ActivityInstanceUtils.abortActivityInstance(activityInstance, abortScope);

      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ProcessInstance abortProcessInstance(long processInstanceOID,
         AbortScope abortScope) throws ObjectNotFoundException, AccessForbiddenException
   {
      IProcessInstance processInstance = internalStopProcessInstance(processInstanceOID,
            abortScope, StopMode.ABORT);
      // return the details.
      return DetailsFactory.create(processInstance);
   }

   private IProcessInstance internalStopProcessInstance(long processInstanceOID, AbortScope abortScope, StopMode stopMode)
   {
      // fetch the process.
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processInstanceOID);

      if (processInstance.isCaseProcessInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_IS_CASE.raise(
               processInstanceOID, processInstance.getProcessDefinition().getId()));
      }

      // TODO check if change needed for halt
      // allow this operation only on spawned processes.
      if (processInstance.getStartingActivityInstance() != null)
      {
         IActivityInstance activityInstance = processInstance
               .getStartingActivityInstance();
         if (!activityInstance.isTerminated())
         {
            if (activityInstance instanceof ActivityInstanceBean)
            {
               ((ActivityInstanceBean) activityInstance).lockAndCheck();
            }
            else
            {
               activityInstance.lock();
            }
         }
         // TODO: (fh) this is not correct if stopMode is not Abort
         ActivityInstanceUtils.abortActivityInstance(activityInstance, abortScope);
      }
      else
      {
         // find the process to stop, depending on the scope either this process or the root process.
         IProcessInstance pi = processInstance;
         IProcessInstance rootProcessInstance = processInstance.getRootProcessInstance();
         if (rootProcessInstance != processInstance
               && (abortScope == null || abortScope == AbortScope.RootHierarchy))
         {
            pi = ProcessInstanceUtils.getActualRootPI(pi);
            trace.info(stopMode.getTransitionState() + " subprocess, starting from root process instance " + pi
                  + ".");
         }
         else
         {
            trace.info(stopMode.getTransitionState() + " process instance " + pi + ".");
         }

         // stop the process.
         if (!pi.isTerminated() && !pi.isAborting() && !pi.isHalting()
               && !(pi.isHalted() && StopMode.HALT.equals(stopMode)))
         {
            // stopping is only allowed if pi is alive or halted to be aborted.
            ProcessInstanceUtils.stopProcessInstance(pi, stopMode);
         }
         else if (pi.isHalting() && StopMode.ABORT.equals(stopMode))
         {
            throw new IllegalOperationException(
                  BpmRuntimeError.BPMRT_PI_IS_STILL_HALTING.raise(pi.getOID()));
         }
         else
         {
            String currentState = null;
            if (pi.isTerminated())
            {
               currentState = "terminated";
            }
            else if (pi.isAborting())
            {
               currentState = StopMode.ABORT.getTransitionState();
            }
            else if (pi.isHalting())
            {
               currentState = StopMode.HALT.getTransitionState();
            }
            else if (pi.isHalted())
            {
               currentState = StopMode.HALT.getFinalState();
            }
            trace.info("Skipping " + stopMode.getModeName() + " of already "
                  + currentState + " process instance " + pi + ".");
         }
      }
      return processInstance;
   }

   public ActivityInstance getActivityInstance(long oid) throws ObjectNotFoundException
   {
      return (ActivityInstance) DetailsFactory.create(
            ActivityInstanceBean.findByOID(oid), IActivityInstance.class,
            ActivityInstanceDetails.class);
   }

   public ProcessInstance getProcessInstance(long oid) throws ObjectNotFoundException
   {
      return DetailsFactory.create(ProcessInstanceBean.findByOID(oid));
   }

   public Map<String, Serializable> getProcessResults(long processInstanceOID)
         throws ObjectNotFoundException, AccessForbiddenException
   {
      ProcessInstanceBean pi = ProcessInstanceBean.findByOID(processInstanceOID);
      if ( !pi.isTerminated())
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.ATDB_PROCESS_INSTANCE_NOT_TERMINATED.raise(processInstanceOID));
      }
      else if (pi.getScopeProcessInstanceOID() != pi.getOID())
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_PROCESS_INSTANCE_REFERENCED_BY_OID_HAS_TO_BE_A_SCOPE_PROCESS_INSTANCES.raise(processInstanceOID));
      }
      IProcessDefinition pd = pi.getProcessDefinition();
      IProcessDefinition intf = getProcessInterface(pd);
      if (intf == null)
      {
         // return pi.getExistingDataValues(false);
         return Collections.emptyMap();
      }
      List<IData> mappedData = CollectionUtils.newList();
      Map<String, IData> mappedParams = CollectionUtils.newMap();
      for (IFormalParameter formalParameter : intf.getFormalParameters())
      {
         if (Direction.OUT.isCompatibleWith(formalParameter.getDirection()))
         {
            String parameterId = formalParameter.getId();
            IData data = pd.getMappedData(parameterId);
            mappedData.add(data);
            mappedParams.put(parameterId, data);
         }
      }
      if ( !mappedData.isEmpty())
      {
         pi.preloadDataValues(mappedData);
      }
      Map<String, Serializable> result = CollectionUtils.newMap();
      for (Map.Entry<String, IData> entry : mappedParams.entrySet())
      {
         IData data = entry.getValue();
         result.put(entry.getKey(), (Serializable) pi.getInDataValue(data, null));
      }
      return result;
   }

   private IProcessDefinition getProcessInterface(IProcessDefinition pd)
   {
      IProcessDefinition intf = null;
      if (pd.getDeclaresInterface())
      {
         intf = pd;
      }
      else
      {
         IReference ref = pd.getExternalReference();
         if (ref != null)
         {
            IExternalPackage pkg = ref.getExternalPackage();
            if (pkg != null)
            {
               IModel refModel = pkg.getReferencedModel();
               if (refModel != null)
               {
                  String uuid = pd.getStringAttribute("carnot:connection:uuid");
                  intf = refModel.findProcessDefinition(StringUtils.isEmpty(uuid)
                        ? ref.getId()
                        : ref.getId() + "?uuid=" + uuid);
               }
            }
         }
      }
      return intf;
   }

   public ActivityInstance bindActivityEventHandler(long activityInstanceOID,
         EventHandlerBinding eventHandler) throws ObjectNotFoundException,
         BindingException, InvalidArgumentException
   {
      if (null == eventHandler)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("eventHandler"));
      }

      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);

      checkInvalidState(activityInstanceOID, activityInstance);

      if (eventHandler.getHandler().getModelOID() != ModelUtils.nullSafeGetModelOID(activityInstance.getActivity()))
      {
         throw new BindingException(
               BpmRuntimeError.BPMRT_AI_AND_HANDLER_BINDING_FRM_DIFF_MODELS
                     .raise(activityInstanceOID, activityInstance.getActivity().getId()));
      }

      IEventHandler handler = getIEventHandler(activityInstance,
            eventHandler.getHandler().getId());

      activityInstance.bind(handler, eventHandler);

      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ProcessInstance bindProcessEventHandler(long processInstanceOID,
         EventHandlerBinding eventHandler) throws ObjectNotFoundException,
         BindingException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processInstanceOID);

      checkInvalidState(processInstanceOID, processInstance);

      if (eventHandler.getHandler().getModelOID() != ModelUtils
            .nullSafeGetModelOID(processInstance.getProcessDefinition()))
      {
         throw new BindingException(
               BpmRuntimeError.BPMRT_PI_AND_HANDLER_BINDING_FRM_DIFF_MODELS.raise(
                     processInstanceOID, processInstance.getProcessDefinition().getId()));
      }

      IEventHandler handler = getIEventHandler(processInstance, eventHandler.getHandler()
            .getId());

      processInstance.bind(handler, eventHandler);

      return DetailsFactory.create(processInstance);
   }

   public ActivityInstance bindActivityEventHandler(long activityInstanceOID,
         String handler) throws ObjectNotFoundException, BindingException
   {
      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);

      checkInvalidState(activityInstanceOID, activityInstance);

      activityInstance.bind(getIEventHandler(activityInstance, handler), null);

      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ProcessInstance bindProcessEventHandler(long processInstanceOID, String handler)
         throws ObjectNotFoundException, BindingException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processInstanceOID);

      checkInvalidState(processInstanceOID, processInstance);

      processInstance.bind(getIEventHandler(processInstance, handler), null);

      return DetailsFactory.create(processInstance);
   }

   public ActivityInstance unbindActivityEventHandler(long activityInstanceOID,
         String eventHandler) throws ObjectNotFoundException, BindingException
   {
      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);

      checkInvalidState(activityInstanceOID, activityInstance);

      activityInstance.unbind(getIEventHandler(activityInstance, eventHandler), null);

      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ProcessInstance unbindProcessEventHandler(long processInstanceOID,
         String eventHandler) throws ObjectNotFoundException, BindingException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processInstanceOID);

      checkInvalidState(processInstanceOID, processInstance);

      processInstance.unbind(getIEventHandler(processInstance, eventHandler), null);

      return DetailsFactory.create(processInstance);
   }

   private void checkInvalidState(long activityInstanceOID,
         IActivityInstance activityInstance)
   {
      if (activityInstance.isTerminated())
      {
         throw new BindingException(BpmRuntimeError.BPMRT_AI_IS_ALREADY_TERMINATED
               .raise(activityInstanceOID, activityInstance.getActivity().getId()));
      }
      if (activityInstance.isAborting())
      {
         throw new BindingException(BpmRuntimeError.BPMRT_AI_IS_IN_ABORTING_PROCESS
               .raise(activityInstanceOID, activityInstance.getActivity().getId()));
      }
   }

   private void checkInvalidState(long processInstanceOID,
         IProcessInstance processInstance)
   {
      if (processInstance.isTerminated())
      {
         throw new BindingException(
               BpmRuntimeError.ATDB_PROCESS_INSTANCE_TERMINATED.raise(processInstanceOID));
      }
      if (processInstance.isAborting())
      {
         throw new BindingException(
               BpmRuntimeError.ATDB_PROCESS_INSTANCE_ABORTING.raise(processInstanceOID));
      }
   }

   public EventHandlerBinding getActivityInstanceEventHandler(long activityInstanceOID,
         String handler) throws ObjectNotFoundException
   {
      return new EventHandlerBindingDetails(
            ActivityInstanceBean.findByOID(activityInstanceOID), getIEventHandler(
                  ActivityInstanceBean.findByOID(activityInstanceOID), handler));
   }

   public EventHandlerBinding getProcessInstanceEventHandler(long processInstanceOID,
         String handler) throws ObjectNotFoundException
   {
      return new EventHandlerBindingDetails(
            ProcessInstanceBean.findByOID(processInstanceOID), getIEventHandler(
                  ProcessInstanceBean.findByOID(processInstanceOID), handler));
   }

   public List<ProcessDefinition> getStartableProcessDefinitions()
   {

      List<ProcessDefinition> result = new ArrayList<ProcessDefinition>();
      IUser user = SecurityProperties.getUser();

      List<IModel> models = getActiveIModels();
      for (IModel model : models)
      {
         ModelElementList processes = model.getProcessDefinitions();
         for (int i = 0, len = processes.size(); i < len; i++ )
         {
            IProcessDefinition process = (IProcessDefinition) processes.get(i);
            if (user.isAuthorizedForStarting(process))
            {
               result.add(DetailsFactory.<ProcessDefinition> create(process,
                     IProcessDefinition.class, ProcessDefinitionDetails.class));
            }
         }
      }

      return result;
   }

   public User getUser()
   {
      return (User) DetailsFactory.create(SecurityProperties.getUser(), IUser.class,
            UserDetails.class);
   }

   public ActivityInstance hibernate(long activityInstanceOID)
         throws ObjectNotFoundException
   {
      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);
      ActivityInstanceUtils.assertNotDefaultCaseInstance(activityInstance);
      ActivityInstanceUtils.assertNotHalted(activityInstance);

      activityInstance.hibernate();

      return (ActivityInstance) DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public ActivityInstance activateNextActivityInstance(long activityInstanceOID)
         throws ObjectNotFoundException
   {
      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);
      IProcessInstance processInstance = activityInstance.getProcessInstance();
      return activateNextActivityInstance(activityInstance, processInstance);
   }

   public ActivityInstance activateNextActivityInstanceForProcessInstance(
         long processInstanceOID) throws ObjectNotFoundException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(processInstanceOID);
      return activateNextActivityInstance(null, processInstance);
   }

   public ActivityInstance activateNextActivityInstance(
         IActivityInstance activityInstance, IProcessInstance processInstance)
         throws ObjectNotFoundException
   {
      assertNotCaseProcessInstance(processInstance);
      IActivityInstance result = findNextActivityInstance(processInstance,
            activityInstance, false);
      if (result == null)
      {
         // look in the root process
         IProcessInstance root = processInstance.getRootProcessInstance();
         result = findNextActivityInstance(root, activityInstance, true);
      }
      if (result != null)
      {
         result.lock();
         // TODO: (fh) recheck state ?
         result.activate();
      }
      return DetailsFactory.create(result, IActivityInstance.class,
            ActivityInstanceDetails.class);
   }

   private IActivityInstance findNextActivityInstance(IProcessInstance processInstance,
         IActivityInstance activityInstance, boolean withHierarchy)
   {
      IUser currentUser = SecurityProperties.getUser();
      if (currentUser == null || currentUser.getOID() == 0)
      {
         return null;
      }
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(ActivityInstanceState.Suspended);
      query.where(new ProcessInstanceFilter(processInstance.getOID(), withHierarchy));
      FilterOrTerm performerTerm = query.getFilter().addOrTerm();
      performerTerm.or(PerformingUserFilter.CURRENT_USER).or(
            PerformingParticipantFilter.ANY_FOR_USER);
      if (activityInstance != null)
      {
         // Due to patching of lastModificationTimestamp by 1 millisecond in
         // AIB.recordHistoricState() the
         // original assumption that startTime of successor AI is always greater or equal
         // lastModTime of predecessor AI
         // is no longer true. Therefore an epsilon of a few milliseconds is applied.
         long epsilon = Parameters.instance().getLong(
               KernelTweakingProperties.LAST_MODIFIED_TIMESTAMP_EPSILON, 3);
         long threshold = activityInstance.getLastModificationTime().getTime() - epsilon;
         query.where(ActivityInstanceQuery.START_TIME.greaterOrEqual(threshold));
      }
      query.orderBy(ActivityInstanceQuery.START_TIME);

      ResultIterator rawResult = new ActivityInstanceQueryEvaluator(query,
            new EvaluationContext(ModelManagerFactory.getCurrent(),
                  SecurityProperties.getUser())).executeFetch();
      IActivityInstance result = null;
      try
      {
         while (rawResult.hasNext())
         {
            try
            {
               // we filter here since no filter was installed for the query
               IActivityInstance nextAI = (IActivityInstance) rawResult.next();
               nextAI.delegateToUser(currentUser);
               result = nextAI;
               break;
            }
            catch (AccessForbiddenException e)
            {
               // ignored
            }
            catch (ConcurrencyException e)
            {
               // ignored
            }
         }
      }
      finally
      {
         rawResult.close();
      }

      return result;
   }

   private IEventHandler getIEventHandler(IActivityInstance activityInstance, String id)
         throws ObjectNotFoundException
   {
      IEventHandler result = activityInstance.getActivity().findHandlerById(id);
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_EVENT_HANDLER_ID.raise(id), id);
      }
      return result;
   }

   private IEventHandler getIEventHandler(IProcessInstance processInstance, String id)
         throws ObjectNotFoundException
   {
      IEventHandler result = processInstance.getProcessDefinition().findHandlerById(id);
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_EVENT_HANDLER_ID.raise(id), id);
      }
      return result;
   }

   public IModel getIModel() throws ObjectNotFoundException
   {
      return ModelUtils.getModel(PredefinedConstants.ACTIVE_MODEL);
   }

   private List<IModel> getActiveIModels() throws ObjectNotFoundException
   {
      List<IModel> result = ModelManagerFactory.getCurrent().findActiveModels();
      if (result == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_NO_ACTIVE_MODEL.raise());
      }
      return result;
   }

   private ActivityCompletionLog createCompletionLog(int flags,
         IActivityInstance completedAi, List<IActivityInstance> performedAis)
   {
      IActivityInstance firstPerformedAi = performedAis.get(0);
      if (completedAi.getOID() != firstPerformedAi.getOID())
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("First performed activity (" + firstPerformedAi
                  + ") instance is not the one expected (" + completedAi + ").");
         }
      }

      IUser currentUser = SecurityProperties.getUser();
      ActivityInstance nextForUser = null;
      if (null != currentUser && 0 != currentUser.getOID()
            && 0 != (flags & WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE))
      {
         AuthorizationContext context = AuthorizationContext.create(ClientPermission.PERFORM_ACTIVITY);
         // (fh) ai at #0 is excluded.
         for (int t = performedAis.size() - 1; t > 0; t-- )
         {
            IActivityInstance ai = performedAis.get(t);
            context.setActivityInstance(ai);
            if (ai.getActivity().isInteractive()
                  && (ActivityInstanceState.Suspended.equals(ai.getState()) || ActivityInstanceState.Application.equals(ai.getState()))
                  && !ai.isDefaultCaseActivityInstance()
                  && Authorization2.hasPermission(context)
                  && QualityAssuranceUtils.isActivationAllowed(ai))
            {
               try
               {
                  activate(ai);
                  nextForUser = (ActivityInstance) DetailsFactory.create(ai,
                        IActivityInstance.class, ActivityInstanceDetails.class);
               }
               catch (AccessForbiddenException e)
               {
                  String errorId = e.getError().getId();
                  if ( !errorId.equals("BPMRT03112"))
                  {
                     throw e;
                  }
               }
            }
         }
      }

      return new ActivityCompletionLogDetails((ActivityInstance) DetailsFactory.create(
            completedAi, IActivityInstance.class, ActivityInstanceDetails.class),
            nextForUser);
   }

   public List<Permission> getPermissions()
   {
      return Authorization2.getPermissions(WorkflowService.class);
   }

   public void setProcessInstanceAttributes(ProcessInstanceAttributes attributes)
         throws ObjectNotFoundException
   {
      if (attributes == null)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("attributes"));
      }
      ProcessInstanceAttributesDetails details = (ProcessInstanceAttributesDetails) attributes;

      long processInstanceOid = attributes.getProcessInstanceOid();
      ProcessInstanceBean pi = ProcessInstanceBean.findByOID(processInstanceOid);

      writeNotes(pi, details.getAddedNotes());
   }

   private void writeNotes(IProcessInstance pi, List<Note> notes)
   {
      // At the moment only scope process instances are supported because notes are only
      // allowed there.
      if (pi != pi.getScopeProcessInstance())
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_PROCESS_INSTANCE_REFERENCED_BY_OID_HAS_TO_BE_A_SCOPE_PROCESS_INSTANCES.raise(pi.getOID()));
      }

      for (Note note : notes)
      {
         pi.addNote(note.getText(), note.getContextKind(), note.getContextOid());
      }
   }

   public void writeLogEntry(LogType logType, ContextKind contextType, long contextOid,
         String message, Throwable throwable) throws ObjectNotFoundException
   {
      Object context = null;
      if (ContextKind.ActivityInstance.equals(contextType))
      {
         context = ActivityInstanceBean.findByOID(contextOid);
      }
      else if (ContextKind.ProcessInstance.equals(contextType))
      {
         context = ProcessInstanceBean.findByOID(contextOid);
      }

      AuditTrailLogger logger = AuditTrailLogger.getInstance(LogCode.EXTERNAL, context);

      if (logType == null)
      {
         logType = LogType.Unknwon;
      }

      switch (logType.getValue())
      {
      case LogType.DEBUG:
         logger.debug(message, throwable);
         break;
      case LogType.ERROR:
         logger.error(message, throwable);
         break;
      case LogType.FATAL:
         logger.fatal(message, throwable);
         break;
      case LogType.INFO:
         logger.info(message, throwable);
         break;
      default:
         /* WARN and UNKNOWN are logged as warnings */
         logger.warn(message, throwable);
         break;
      }
   }

   public Serializable execute(ServiceCommand serviceCmd) throws ServiceCommandException
   {
      try
      {
         boolean autoFlush = getBooleanOption(serviceCmd, "autoFlush", true);
         EmbeddedServiceFactory factory = autoFlush
               ? EmbeddedServiceFactory.CURRENT_TX_WITH_AUTO_FLUSH()
               : EmbeddedServiceFactory.CURRENT_TX();
         return serviceCmd.execute(factory);
      }
      catch (final ServiceCommandException e)
      {
         // re-throw wrapped exception;
         throw e;
      }
      catch (final Exception e)
      {
         ErrorCase ec = null;
         if (e instanceof ApplicationException)
         {
            ec = ((ApplicationException) e).getError();
         }
         if (ec == null)
         {
            throw new ServiceCommandException(
                  "Unexpected exception while executing command.", e);
         }
         else
         {
            throw new ServiceCommandException(ec, e);
         }
      }
   }

   private static boolean getBooleanOption(ServiceCommand serviceCmd, String name,
         boolean defaultValue)
   {
      if (serviceCmd instanceof Configurable)
      {
         Map<String, Object> options = ((Configurable) serviceCmd).getOptions();
         if (options != null)
         {
            Object option = options.get(name);
            if (option instanceof Boolean)
            {
               return ((Boolean) option).booleanValue();
            }
         }
      }
      return defaultValue;
   }

   public void setActivityInstanceAttributes(ActivityInstanceAttributes attributes)
         throws ObjectNotFoundException
   {
      QualityAssuranceUtils.assertAttributesNotNull(attributes);
      long activityInstanceOID = attributes.getActivityInstanceOid();
      ActivityInstanceBean activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);

      List<Note> addedNotes = attributes.getAddedNotes();
      if (addedNotes != null && !addedNotes.isEmpty())
      {
         writeNotes(activityInstance.getProcessInstance().getScopeProcessInstance(),
               addedNotes);
      }

      ActivityInstanceAttributes preparedAttributes = QualityAssuranceUtils.prepareForSave(attributes);
      QualityAssuranceUtils.setActivityInstanceAttributes(preparedAttributes,
            activityInstance);
   }

   public List<TransitionTarget> getAdHocTransitionTargets(long activityInstanceOid,
         TransitionOptions options, ScanDirection direction)
         throws ObjectNotFoundException
   {
      return RelocationUtils.getRelocateTargets(activityInstanceOid, options, direction);
   }

   public ActivityInstance performAdHocTransition(long activityInstanceOid,
         TransitionTarget transitionTarget, boolean complete)
         throws IllegalOperationException, ObjectNotFoundException,
         AccessForbiddenException
   {
      if (transitionTarget.getActivityInstanceOid() != activityInstanceOid)
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_AI_NOT_ADHOC_TRANSITION_SOURCE.raise(activityInstanceOid));
      }
      return performAdHocTransition(transitionTarget, complete).getSourceActivityInstance();
   }

   public TransitionReport performAdHocTransition(TransitionTarget transitionTarget,
         boolean complete) throws IllegalOperationException, ObjectNotFoundException,
         AccessForbiddenException
   {
      IActivityInstance activityInstance = ActivityInstanceUtils.lock(transitionTarget.getActivityInstanceOid());
      IActivityInstance targetActivityInstance = null;

      ActivityInstanceUtils.assertNotHalted(activityInstance);
      ActivityInstanceUtils.assertNotTerminated(activityInstance);
      ActivityInstanceUtils.assertNotInAbortingProcess(activityInstance);
      ActivityInstanceUtils.assertNotDefaultCaseInstance(activityInstance);

      // TODO rsauer fix for special scenario from CSS, involving automatic completion
      // of activities after a certain period of time, while the activity sticky to the
      // predecessor activitie's user worklist.
      // Consider adding a well known identity to all daemons to avoid problems.
      IUser user = SecurityProperties.getUser();
      if (null != user && 0 != user.getOID())
      {
         ActivityInstanceUtils.assertNotActivatedByOther(activityInstance, complete);
      }

      boolean interactive = activityInstance.getActivity().isInteractive();
      ActivityInstanceState state = activityInstance.getState();
      if (state == ActivityInstanceState.Hibernated || interactive
            && state == ActivityInstanceState.Application || !interactive
            && state == ActivityInstanceState.Interrupted)
      {
         if (state == ActivityInstanceState.Hibernated && complete
               || state == ActivityInstanceState.Interrupted)
         {
            activityInstance.activate();
            if (state == ActivityInstanceState.Interrupted)
            {
               ProcessInstanceBean processInstance = (ProcessInstanceBean) activityInstance.getProcessInstance();
               ProcessInstanceState piState = processInstance.getState();
               if (piState == ProcessInstanceState.Interrupted)
               {
                  processInstance.setState(ProcessInstanceState.ACTIVE);
                  EventUtils.recoverEvent(processInstance);
               }
               EventUtils.recoverEvent(activityInstance);
            }
         }
         targetActivityInstance = RelocationUtils.performTransition(activityInstance,
               transitionTarget, complete);
      }
      else
      {
         throw new IllegalStateChangeException(activityInstance.toString(), complete
               ? ActivityInstanceState.Completed
               : ActivityInstanceState.Aborted, state);
      }
      return new TransitionReportDetails(DetailsFactory.create(activityInstance,
            IActivityInstance.class, ActivityInstanceDetails.class),
            DetailsFactory.create(targetActivityInstance, IActivityInstance.class,
                  ActivityInstanceDetails.class));
   }

   @Override
   public BusinessObject createBusinessObjectInstance(String businessObjectId,
         Object initialValue)
   {
      return BusinessObjectUtils.createInstance(businessObjectId, initialValue);
   }

   @Override
   public BusinessObject updateBusinessObjectInstance(String businessObjectId,
         Object newValue)
   {
      return BusinessObjectUtils.updateInstance(businessObjectId, newValue);
   }

   @Override
   public void deleteBusinessObjectInstance(String businessObjectId, Object primaryKey)
   {
      BusinessObjectUtils.deleteInstance(businessObjectId, primaryKey);
   }
}