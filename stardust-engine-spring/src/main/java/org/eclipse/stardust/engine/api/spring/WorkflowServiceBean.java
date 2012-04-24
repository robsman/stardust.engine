/*
 * Generated from Revision: 55779 
 */
package org.eclipse.stardust.engine.api.spring;

/**
 * The WorkflowService provides all functionality for workflow operations in a
 * CARNOT runtime environment.
 * <p>
 * This includes:
 * <ul>
 * <li>starting and aborting process instances,</li>
 * <li>activating, completing, suspending and aborting activities,</li>
 * <li>binding and unbinding event handlers,</li>
 * <li>delegating activities, and </li>
 * <li>accessing workflow data.</li>
 * </ul>
 *
 * @author ubirkemeyer
 * @version 55779
 */
public class WorkflowServiceBean extends org.eclipse.stardust.engine.api.spring.AbstractSpringServiceBean implements IWorkflowService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activate(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance activate(
         long activityInstanceOID)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).activate(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#complete(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance complete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidValueException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).complete(activityInstanceOID, context, outData);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#complete(long activityInstanceOID, java.lang.String context, java.util.Map outData, int flags)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog
         complete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData, int
         flags)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidValueException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).complete(activityInstanceOID, context, outData, flags);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateAndComplete(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateAndComplete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidValueException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).activateAndComplete(activityInstanceOID, context, outData);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateAndComplete(long activityInstanceOID, java.lang.String context, java.util.Map outData, int flags)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog
         activateAndComplete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData, int
         flags)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidValueException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).activateAndComplete(
            activityInstanceOID, context, outData, flags);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataValue(long activityInstanceOID, java.lang.String context, java.lang.String id)
    */
   public java.io.Serializable getInDataValue(
         long activityInstanceOID, java.lang.String context, java.lang.String id)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getInDataValue(activityInstanceOID, context, id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataValues(long activityInstanceOID, java.lang.String context, java.util.Set ids)
    */
   public java.util.Map<java.lang.String,java.io.Serializable>
         getInDataValues(
         long activityInstanceOID, java.lang.String context, java.util.Set ids)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getInDataValues(activityInstanceOID, context, ids);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspend(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ContextData outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspend(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ContextData
         outData)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspend(activityInstanceOID, outData);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToDefaultPerformer(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToDefaultPerformer(long activityInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToDefaultPerformer(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToDefaultPerformer(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToDefaultPerformer(
         long activityInstanceOID, java.lang.String context, java.util.Map outData)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToDefaultPerformer(
            activityInstanceOID, context, outData);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(long activityInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToUser(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, java.lang.String context, java.util.Map outData)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToUser(activityInstanceOID, context, outData);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, long userOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(long activityInstanceOID, long userOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToUser(activityInstanceOID, userOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, long userOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, long userOID, java.lang.String context, java.util.Map
         outData)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToUser(activityInstanceOID, userOID, context, outData);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, java.lang.String participant)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(long activityInstanceOID, java.lang.String participant)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToParticipant(activityInstanceOID, participant);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, java.lang.String participant, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(
         long activityInstanceOID, java.lang.String participant, java.lang.String context,
         java.util.Map outData)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToParticipant(
            activityInstanceOID, participant, context, outData);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant, org.eclipse.stardust.engine.api.model.ContextData outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo
         participant, org.eclipse.stardust.engine.api.model.ContextData outData)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).suspendToParticipant(activityInstanceOID, participant, outData);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#hibernate(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance hibernate(
         long activityInstanceOID)
         throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException,
         org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).hibernate(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#startProcess(java.lang.String id, java.util.Map data, boolean synchronously)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         startProcess(java.lang.String id, java.util.Map data, boolean synchronously)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).startProcess(id, data, synchronously);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnSubprocessInstance(long parentProcessInstanceOid, java.lang.String spawnProcessID, boolean copyData, java.util.Map data)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnSubprocessInstance(
         long parentProcessInstanceOid, java.lang.String spawnProcessID, boolean copyData,
         java.util.Map data)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).spawnSubprocessInstance(
            parentProcessInstanceOid, spawnProcessID, copyData, data);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnSubprocessInstances(long parentProcessInstanceOid, java.util.List subprocessSpawnInfo)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.ProcessInstance>
         spawnSubprocessInstances(
         long parentProcessInstanceOid, java.util.List subprocessSpawnInfo)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).spawnSubprocessInstances(
            parentProcessInstanceOid, subprocessSpawnInfo);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnPeerProcessInstance(long processInstanceOid, java.lang.String spawnProcessID, boolean copyData, java.util.Map data, boolean abortProcessInstance, java.lang.String comment)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnPeerProcessInstance(
         long processInstanceOid, java.lang.String spawnProcessID, boolean copyData,
         java.util.Map data, boolean abortProcessInstance, java.lang.String comment)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).spawnPeerProcessInstance(
            processInstanceOid, spawnProcessID, copyData, data, abortProcessInstance,
            comment);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnPeerProcessInstance(long processInstanceOid, java.lang.String spawnProcessID, org.eclipse.stardust.engine.api.runtime.SpawnOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnPeerProcessInstance(
         long processInstanceOid, java.lang.String spawnProcessID,
         org.eclipse.stardust.engine.api.runtime.SpawnOptions options)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).spawnPeerProcessInstance(
            processInstanceOid, spawnProcessID, options);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#createCase(java.lang.String name, java.lang.String description, long[] memberOids)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         createCase(
         java.lang.String name, java.lang.String description, long[] memberOids)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.InvalidArgumentException,
         org.eclipse.stardust.common.error.ConcurrencyException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).createCase(name, description, memberOids);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinCase(long caseOid, long[] memberOids)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance joinCase(
         long caseOid, long[] memberOids)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.ConcurrencyException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).joinCase(caseOid, memberOids);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#leaveCase(long caseOid, long[] memberOids)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance leaveCase(
         long caseOid, long[] memberOids)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.ConcurrencyException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).leaveCase(caseOid, memberOids);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#mergeCases(long targetCaseOid, long[] sourceCaseOids, java.lang.String comment)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance mergeCases(
         long targetCaseOid, long[] sourceCaseOids, java.lang.String comment)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.ConcurrencyException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).mergeCases(targetCaseOid, sourceCaseOids, comment);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateCase(long caseOid, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         delegateCase(
         long caseOid, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).delegateCase(caseOid, participant);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinProcessInstance(long processInstanceOid, long targetProcessInstanceOid, java.lang.String comment)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         joinProcessInstance(
         long processInstanceOid, long targetProcessInstanceOid, java.lang.String comment)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).joinProcessInstance(
            processInstanceOid, targetProcessInstanceOid, comment);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         abortActivityInstance(long activityInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).abortActivityInstance(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long activityInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         abortActivityInstance(
         long activityInstanceOid,
         org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).abortActivityInstance(activityInstanceOid, abortScope);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortProcessInstance(long processInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         abortProcessInstance(
         long processInstanceOid,
         org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).abortProcessInstance(processInstanceOid, abortScope);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getModel()
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModel getModel()
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getModel();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getWorklist(org.eclipse.stardust.engine.api.query.WorklistQuery query)
    */
   public org.eclipse.stardust.engine.api.query.Worklist
         getWorklist(org.eclipse.stardust.engine.api.query.WorklistQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getWorklist(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstance(org.eclipse.stardust.engine.api.query.WorklistQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstance(
         org.eclipse.stardust.engine.api.query.WorklistQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).activateNextActivityInstance(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstance(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstance(long activityInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).activateNextActivityInstance(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstanceForProcessInstance(long processInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstanceForProcessInstance(long processInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).activateNextActivityInstanceForProcessInstance(
            processInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setOutDataPath(long processInstanceOID, java.lang.String id, java.lang.Object object)
    */
   public void setOutDataPath(
         long processInstanceOID, java.lang.String id, java.lang.Object object)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidValueException
   {
      ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).setOutDataPath(processInstanceOID, id, object);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setOutDataPaths(long processInstanceOID, java.util.Map values)
    */
   public void setOutDataPaths(long processInstanceOID, java.util.Map values)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidValueException
   {
      ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).setOutDataPaths(processInstanceOID, values);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataPath(long processInstanceOID, java.lang.String id)
    */
   public java.lang.Object getInDataPath(
         long processInstanceOID, java.lang.String id)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getInDataPath(processInstanceOID, id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataPaths(long processInstanceOID, java.util.Set ids)
    */
   public java.util.Map<java.lang.String,java.io.Serializable>
         getInDataPaths(long processInstanceOID, java.util.Set ids)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getInDataPaths(processInstanceOID, ids);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToDefaultPerformer(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToDefaultPerformer(long activityInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).delegateToDefaultPerformer(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToUser(long activityInstanceOID, long userOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToUser(long activityInstanceOID, long userOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).delegateToUser(activityInstanceOID, userOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToParticipant(long activityInstanceOID, java.lang.String performer)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToParticipant(long activityInstanceOID, java.lang.String performer)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).delegateToParticipant(activityInstanceOID, performer);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToParticipant(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToParticipant(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo
         participant)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).delegateToParticipant(activityInstanceOID, participant);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getActivityInstance(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         getActivityInstance(long activityInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getActivityInstance(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessInstance(long processInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         getProcessInstance(long processInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getProcessInstance(processInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessResults(long processInstanceOID)
    */
   public java.util.Map<java.lang.String,java.io.Serializable>
         getProcessResults(long processInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getProcessResults(processInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindActivityEventHandler(long activityInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         bindActivityEventHandler(
         long activityInstanceOID,
         org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.BindingException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).bindActivityEventHandler(activityInstanceOID, eventHandler);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindProcessEventHandler(long processInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         bindProcessEventHandler(
         long processInstanceOID,
         org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.BindingException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).bindProcessEventHandler(processInstanceOID, eventHandler);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindActivityEventHandler(long activityInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         bindActivityEventHandler(long activityInstanceOID, java.lang.String handler)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.BindingException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).bindActivityEventHandler(activityInstanceOID, handler);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindProcessEventHandler(long processInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         bindProcessEventHandler(long processInstanceOID, java.lang.String handler)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.BindingException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).bindProcessEventHandler(processInstanceOID, handler);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#unbindActivityEventHandler(long activityInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         unbindActivityEventHandler(long activityInstanceOID, java.lang.String handler)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.BindingException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).unbindActivityEventHandler(activityInstanceOID, handler);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#unbindProcessEventHandler(long processInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         unbindProcessEventHandler(long processInstanceOID, java.lang.String handler)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.BindingException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).unbindProcessEventHandler(processInstanceOID, handler);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getActivityInstanceEventHandler(long activityInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         getActivityInstanceEventHandler(
         long activityInstanceOID, java.lang.String handler)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getActivityInstanceEventHandler(activityInstanceOID, handler);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessInstanceEventHandler(long processInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         getProcessInstanceEventHandler(long processInstanceOID, java.lang.String handler)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getProcessInstanceEventHandler(processInstanceOID, handler);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getAdHocTransitionTargets(long activityInstanceOid, org.eclipse.stardust.engine.api.runtime.TransitionOptions options, org.eclipse.stardust.engine.api.runtime.ScanDirection direction)
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.TransitionTarget>
         getAdHocTransitionTargets(
         long activityInstanceOid,
         org.eclipse.stardust.engine.api.runtime.TransitionOptions options,
         org.eclipse.stardust.engine.api.runtime.ScanDirection direction)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getAdHocTransitionTargets(
            activityInstanceOid, options, direction);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#performAdHocTransition(long activityInstanceOid, org.eclipse.stardust.engine.api.runtime.TransitionTarget target, boolean complete)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         performAdHocTransition(
         long activityInstanceOid,
         org.eclipse.stardust.engine.api.runtime.TransitionTarget target, boolean
         complete)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).performAdHocTransition(activityInstanceOid, target, complete);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getStartableProcessDefinitions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getStartableProcessDefinitions()
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getStartableProcessDefinitions();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getUser()
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser()
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getUser();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getPermissions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions()
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).getPermissions();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setProcessInstanceAttributes(org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes attributes)
    */
   public void
         setProcessInstanceAttributes(
         org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes attributes)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).setProcessInstanceAttributes(attributes);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setActivityInstanceAttributes(org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes attributes)
    */
   public void
         setActivityInstanceAttributes(
         org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes attributes)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).setActivityInstanceAttributes(attributes);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#writeLogEntry(org.eclipse.stardust.engine.api.runtime.LogType logType, org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid, java.lang.String message, java.lang.Throwable throwable)
    */
   public void writeLogEntry(
         org.eclipse.stardust.engine.api.runtime.LogType logType,
         org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
         java.lang.String message, java.lang.Throwable throwable)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).writeLogEntry(
            logType, contextType, contextOid, message, throwable);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#execute(org.eclipse.stardust.engine.core.runtime.command.ServiceCommand serviceCmd)
    */
   public java.io.Serializable
         execute(
         org.eclipse.stardust.engine.core.runtime.command.ServiceCommand serviceCmd)
         throws org.eclipse.stardust.common.error.ServiceCommandException
   {
      return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            serviceProxy).execute(serviceCmd);
   }

	public WorkflowServiceBean()
	{
      super(org.eclipse.stardust.engine.api.runtime.WorkflowService.class,
            org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl.class);
	}
}