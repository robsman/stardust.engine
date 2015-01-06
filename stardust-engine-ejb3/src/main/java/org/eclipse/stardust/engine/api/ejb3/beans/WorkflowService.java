/*
 * Generated from Revision
 */
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.Local;

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
 * @version $Revision
 */
@Local
public interface WorkflowService extends org.eclipse.stardust.engine.core.runtime.ejb.Ejb3ManagedService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activate(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance activate(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#complete(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance complete(
         long activityInstanceOID, java.lang.String context,
         java.util.Map<java.lang.String,?> outData,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#complete(long activityInstanceOID, java.lang.String context, java.util.Map outData, int flags)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog
         complete(
         long activityInstanceOID, java.lang.String context,
         java.util.Map<java.lang.String,?> outData, int flags,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateAndComplete(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateAndComplete(
         long activityInstanceOID, java.lang.String context,
         java.util.Map<java.lang.String,?> outData,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateAndComplete(long activityInstanceOID, java.lang.String context, java.util.Map outData, int flags)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog
         activateAndComplete(
         long activityInstanceOID, java.lang.String context,
         java.util.Map<java.lang.String,?> outData, int flags,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataValue(long activityInstanceOID, java.lang.String context, java.lang.String id)
    */
    public java.io.Serializable getInDataValue(
         long activityInstanceOID, java.lang.String context, java.lang.String id,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataValues(long activityInstanceOID, java.lang.String context, java.util.Set ids)
    */
    public java.util.Map<java.lang.String,java.io.Serializable>
         getInDataValues(
         long activityInstanceOID, java.lang.String context,
         java.util.Set<java.lang.String> ids,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspend(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ContextData outData)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspend(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ContextData
         outData, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToDefaultPerformer(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToDefaultPerformer(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToDefaultPerformer(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToDefaultPerformer(
         long activityInstanceOID, java.lang.String context,
         java.util.Map<java.lang.String,?> outData,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, java.lang.String context,
         java.util.Map<java.lang.String,?> outData,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, long userOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, long userOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, long userOID, java.lang.String context, java.util.Map outData)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, long userOID, java.lang.String context,
         java.util.Map<java.lang.String,?> outData,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, java.lang.String participant)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(
         long activityInstanceOID, java.lang.String participant,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, java.lang.String participant, java.lang.String context, java.util.Map outData)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(
         long activityInstanceOID, java.lang.String participant, java.lang.String context,
         java.util.Map<java.lang.String,?> outData,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant, org.eclipse.stardust.engine.api.model.ContextData outData)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo
         participant, org.eclipse.stardust.engine.api.model.ContextData outData,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#hibernate(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         hibernate(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#startProcess(java.lang.String id, java.util.Map data, boolean synchronously)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         startProcess(
         java.lang.String id, java.util.Map<java.lang.String,?> data, boolean
         synchronously, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnSubprocessInstance(long parentProcessInstanceOid, java.lang.String spawnProcessID, boolean copyData, java.util.Map data)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnSubprocessInstance(
         long parentProcessInstanceOid, java.lang.String spawnProcessID, boolean copyData,
         java.util.Map<java.lang.String,?> data,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnSubprocessInstances(long parentProcessInstanceOid, java.util.List subprocessSpawnInfo)
    */
    public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ProcessInstance>
         spawnSubprocessInstances(
         long parentProcessInstanceOid,
         java.util.List<org.eclipse.stardust.engine.api.runtime.SubprocessSpawnInfo>
         subprocessSpawnInfo, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnPeerProcessInstance(long processInstanceOid, java.lang.String spawnProcessID, boolean copyData, java.util.Map data, boolean abortProcessInstance, java.lang.String comment)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnPeerProcessInstance(
         long processInstanceOid, java.lang.String spawnProcessID, boolean copyData,
         java.util.Map<java.lang.String,? extends java.io.Serializable> data, boolean
         abortProcessInstance, java.lang.String comment,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnPeerProcessInstance(long processInstanceOid, java.lang.String spawnProcessID, org.eclipse.stardust.engine.api.runtime.SpawnOptions options)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnPeerProcessInstance(
         long processInstanceOid, java.lang.String spawnProcessID,
         org.eclipse.stardust.engine.api.runtime.SpawnOptions options,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#createCase(java.lang.String name, java.lang.String description, long[] memberOids)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         createCase(
         java.lang.String name, java.lang.String description, long[] memberOids,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinCase(long caseOid, long[] memberOids)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance joinCase(
         long caseOid, long[] memberOids,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#leaveCase(long caseOid, long[] memberOids)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance leaveCase(
         long caseOid, long[] memberOids,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#mergeCases(long targetCaseOid, long[] sourceCaseOids, java.lang.String comment)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         mergeCases(
         long targetCaseOid, long[] sourceCaseOids, java.lang.String comment,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateCase(long caseOid, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         delegateCase(
         long caseOid, org.eclipse.stardust.engine.api.model.ParticipantInfo participant,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinProcessInstance(long processInstanceOid, long targetProcessInstanceOid, java.lang.String comment)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         joinProcessInstance(
         long processInstanceOid, long targetProcessInstanceOid, java.lang.String comment,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         abortActivityInstance(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long activityInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         abortActivityInstance(
         long activityInstanceOid,
         org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortProcessInstance(long processInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         abortProcessInstance(
         long processInstanceOid,
         org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getModel()
    */
    public org.eclipse.stardust.engine.api.runtime.DeployedModel
         getModel(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getWorklist(org.eclipse.stardust.engine.api.query.WorklistQuery query)
    */
    public org.eclipse.stardust.engine.api.query.Worklist
         getWorklist(
         org.eclipse.stardust.engine.api.query.WorklistQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstance(org.eclipse.stardust.engine.api.query.WorklistQuery query)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstance(
         org.eclipse.stardust.engine.api.query.WorklistQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstance(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstance(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstanceForProcessInstance(long processInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstanceForProcessInstance(
         long processInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setOutDataPath(long processInstanceOID, java.lang.String id, java.lang.Object object)
    */
    public void setOutDataPath(
         long processInstanceOID, java.lang.String id, java.lang.Object object,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setOutDataPaths(long processInstanceOID, java.util.Map values)
    */
    public void setOutDataPaths(
         long processInstanceOID, java.util.Map<java.lang.String,?> values,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataPath(long processInstanceOID, java.lang.String id)
    */
    public java.lang.Object getInDataPath(
         long processInstanceOID, java.lang.String id,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataPaths(long processInstanceOID, java.util.Set ids)
    */
    public java.util.Map<java.lang.String,java.io.Serializable>
         getInDataPaths(
         long processInstanceOID, java.util.Set<java.lang.String> ids,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToDefaultPerformer(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToDefaultPerformer(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToUser(long activityInstanceOID, long userOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToUser(
         long activityInstanceOID, long userOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToParticipant(long activityInstanceOID, java.lang.String performer)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToParticipant(
         long activityInstanceOID, java.lang.String performer,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToParticipant(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToParticipant(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo
         participant, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getActivityInstance(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         getActivityInstance(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessInstance(long processInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         getProcessInstance(
         long processInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessResults(long processInstanceOID)
    */
    public java.util.Map<java.lang.String,java.io.Serializable>
         getProcessResults(
         long processInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindActivityEventHandler(long activityInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         bindActivityEventHandler(
         long activityInstanceOID,
         org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindProcessEventHandler(long processInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         bindProcessEventHandler(
         long processInstanceOID,
         org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindActivityEventHandler(long activityInstanceOID, java.lang.String handler)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         bindActivityEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindProcessEventHandler(long processInstanceOID, java.lang.String handler)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         bindProcessEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#unbindActivityEventHandler(long activityInstanceOID, java.lang.String handler)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         unbindActivityEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#unbindProcessEventHandler(long processInstanceOID, java.lang.String handler)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         unbindProcessEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getActivityInstanceEventHandler(long activityInstanceOID, java.lang.String handler)
    */
    public org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         getActivityInstanceEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessInstanceEventHandler(long processInstanceOID, java.lang.String handler)
    */
    public org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         getProcessInstanceEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getAdHocTransitionTargets(long activityInstanceOid, org.eclipse.stardust.engine.api.runtime.TransitionOptions options, org.eclipse.stardust.engine.api.runtime.ScanDirection direction)
    */
    public
         java.util.List<org.eclipse.stardust.engine.api.runtime.TransitionTarget>
         getAdHocTransitionTargets(
         long activityInstanceOid,
         org.eclipse.stardust.engine.api.runtime.TransitionOptions options,
         org.eclipse.stardust.engine.api.runtime.ScanDirection direction,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#performAdHocTransition(long activityInstanceOid, org.eclipse.stardust.engine.api.runtime.TransitionTarget target, boolean complete)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         performAdHocTransition(
         long activityInstanceOid,
         org.eclipse.stardust.engine.api.runtime.TransitionTarget target, boolean
         complete, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getStartableProcessDefinitions()
    */
    public
         java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getStartableProcessDefinitions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getUser()
    */
    public org.eclipse.stardust.engine.api.runtime.User
         getUser(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getPermissions()
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setProcessInstanceAttributes(org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes attributes)
    */
    public void
         setProcessInstanceAttributes(
         org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes attributes,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setActivityInstanceAttributes(org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes attributes)
    */
    public void
         setActivityInstanceAttributes(
         org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes attributes,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#writeLogEntry(org.eclipse.stardust.engine.api.runtime.LogType logType, org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid, java.lang.String message, java.lang.Throwable throwable)
    */
    public void writeLogEntry(
         org.eclipse.stardust.engine.api.runtime.LogType logType,
         org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
         java.lang.String message, java.lang.Throwable throwable,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#execute(org.eclipse.stardust.engine.core.runtime.command.ServiceCommand serviceCmd)
    */
    public java.io.Serializable
         execute(
         org.eclipse.stardust.engine.core.runtime.command.ServiceCommand serviceCmd,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#createBusinessObjectInstance(java.lang.String qualifiedBusinessObjectId, java.lang.Object initialValue)
    */
    public org.eclipse.stardust.engine.api.runtime.BusinessObject
         createBusinessObjectInstance(
         java.lang.String qualifiedBusinessObjectId, java.lang.Object initialValue,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#updateBusinessObjectInstance(java.lang.String qualifiedBusinessObjectId, java.lang.Object newValue)
    */
    public org.eclipse.stardust.engine.api.runtime.BusinessObject
         updateBusinessObjectInstance(
         java.lang.String qualifiedBusinessObjectId, java.lang.Object newValue,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#deleteBusinessObjectInstance(java.lang.String qualifiedBusinessObjectId, java.lang.Object primaryKey)
    */
    public void deleteBusinessObjectInstance(
         java.lang.String qualifiedBusinessObjectId, java.lang.Object primaryKey,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         }