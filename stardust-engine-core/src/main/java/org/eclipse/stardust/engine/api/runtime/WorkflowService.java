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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.error.*;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;


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
 * @version $Revision$
 */
public interface WorkflowService extends Service
{
   /**
    * retry n times until receiving an activity instance defined by the query
    * default is 5 times
    *
    * @see #activateNextActivityInstance(WorklistQuery query)
    */
   static final String ACTIVATE_NEXT_ACTIVITY_INSTANCE_RETRIES = "Infinity.Engine.Tuning.Query.ActivateNextActivityInstance.Retries";


   /**
    * If a synchronous interactive successor activity instance exists and can be activated
    * by the calling user, it will be immediately activated an returned.
    *
    * @see #activateNextActivityInstance(long)
    */
   int FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE = 0x00000001;

   /**
    * Activates the interactive activity instance identified by the
    * <code>activityInstanceOID</code>.
    *
    * <p>Activating means:
    * <li>Removing the activity instance from its original worklist.</li>
    * <li>Adding the activity instance to the logged-in user's worklist.</li>
    * <li>Setting the state of the activity instance to APPLICATION state.</li>
    *
    * @param activityInstanceOID the OID of the activity to be activated.
    *
    * @return the {@link ActivityInstance} that was activated.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws AccessForbiddenException if the current user is not valid or is not
    *         granted to execute the activity instance. Also thrown if the activity
    *         instance is already terminated.
    *
    * @see #activateAndComplete
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance activate(long activityInstanceOID)
         throws ConcurrencyException, ObjectNotFoundException, AccessForbiddenException;

   /**
    * Completes the interactive activity instance identified by the
    * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
    *
    * @param activityInstanceOID the OID of the activity to be completed.
    * @param context the ID of the context on which the data mapping will be performed.
    * @param outData a map with the values of the out access points.
    *
    * @return the {@link ActivityInstance} that was completed.
    *
    * @throws ConcurrencyException if the activity instance is exclusively locked by another thread.
    * @throws IllegalStateChangeException if that state change is not permitted,
    *         i.e. the activity is not active.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws InvalidValueException if one of the <code>outData</object> values to
    *         be written is invalid, most probably as of a type conflict in case of
    *         statically typed data.
    * @throws AccessForbiddenException if the current user is not allowed to complete the activity.
    *
    * @see #activateAndComplete(long, String, Map)
    * @see #complete(long, String, Map, int)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance complete(long activityInstanceOID, String context, Map<String, ?> outData)
         throws ConcurrencyException, IllegalStateChangeException,
         ObjectNotFoundException, InvalidValueException, AccessForbiddenException;

   /**
    * Completes the interactive activity instance identified by the
    * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
    *
    * @param activityInstanceOID the OID of the activity to be completed.
    * @param context the ID of the context on which the data mapping will be performed.
    * @param outData a map with the values of the out access points.
    * @param flags Optional adjustment to some details of operation. Supported values are
    *               {@link #FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE}.
    *
    * @return A log describing the result of the invocation. Depends on the flags parameter.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws IllegalStateChangeException if that state change is not permitted,
    *         i.e. the activity is not active.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws InvalidValueException if one of the <code>outData</object> values to
    *         be written is invalid, most probably as of a type conflict in case of
    *         statically typed data.
    * @throws AccessForbiddenException if the activity instance is
    *         already terminated.
    *
    * @see #complete(long, String, Map)
    * @see #activateAndComplete(long, String, Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityCompletionLog complete(long activityInstanceOID, String context, Map<String, ?> outData,
         int flags) throws ConcurrencyException, IllegalStateChangeException,
         ObjectNotFoundException, InvalidValueException, AccessForbiddenException;

   /**
    * Activates and completes the interactive activity instance identified by the
    * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
    *
    * If the activity is activated to be immediately completed, this method is more
    * efficient than invoking activate(...) and complete(...) separately.
    *
    * @param activityInstanceOID the OID of the activity to be completed.
    * @param context the ID of the context on which the data mapping will be performed.
    *        The value <code>null</code> will be interpreted as the default context.
    * @param outData a map with the values of the out access points.
    *
    * @return the {@link ActivityInstance} that was completed.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws IllegalStateChangeException if that state change is not permitted,
    *         i.e. the activity is not active.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws InvalidValueException if one of the <code>outData</code> values to
    *         be written is invalid, most probably as of a type conflict in case of
    *         statically typed data.
    * @throws AccessForbiddenException if the current user is not valid or is not
    *         granted to execute the activity instance. Also thrown if the activity
    *         instance is already terminated.
    *
    * @see #activate(long)
    * @see #complete(long, String, Map)
    * @see #activateAndComplete(long, String, Map, int)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance activateAndComplete(long activityInstanceOID, String context,
         Map<String, ? > outData) throws ConcurrencyException, ObjectNotFoundException,
         InvalidValueException, AccessForbiddenException;

   /**
    * Activates and completes the interactive activity instance identified by the
    * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
    *
    * If the activity is activated to be immediately completed, this method is more
    * efficient than invoking activate(...) and complete(...) separately.
    *
    * @param activityInstanceOID the OID of the activity to be completed.
    * @param context the ID of the context on which the data mapping will be performed.
    *        The value <code>null</code> will be interpreted as the default context.
    * @param outData a map with the values of the out access points.
    * @param flags Optional adjustment to some details of operation. Supported values are
    *               {@link #FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE}.
    *
    * @return A log describing the result of the invocation. Depends on the flags parameter.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws IllegalStateChangeException if that state change is not permitted,
    *         i.e. the activity is not active.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws InvalidValueException if one of the <code>outData</object> values to
    *         be written is invalid, most probably as of a type conflict in case of
    *         statically typed data.
    * @throws AccessForbiddenException if the current user is not valid or is not
    *         granted to execute the activity instance. Also thrown if the activity
    *         instance is already terminated.
    *
    * @see #activateAndComplete(long, String, Map)
    * @see #activate(long)
    * @see #complete(long, String, Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityCompletionLog activateAndComplete(long activityInstanceOID, String context,
         Map<String, ? > outData, int flags) throws ConcurrencyException, ObjectNotFoundException,
         InvalidValueException, AccessForbiddenException;

   /**
    * Retrieves all evaluated IN data mappings that match the provided application context
    * for the specified activity.
    *
    * @param activityInstanceOID the OID of the activity for which the data mappings are
    *        to be retrieved.
    * @param context the application context for which the mappings are retrieved.
    *        The value <code>null</code> will be interpreted as the default context.
    * @param id The ID of the data mapping to be retrieved.
    *
    * @return The retrieved value.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified
    *   OID or there is no mapping with the given ID under the given context.
    *
    * @see #getInDataValues(long, String, Set)
    *
    * @since 3.1.2
    */
   /*@ExecutionPermission(
         id=ExecutionPermission.Id.readDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})*/
   Serializable getInDataValue(long activityInstanceOID, String context, String id)
         throws ObjectNotFoundException;

   /**
    * Retrieves all evaluated IN data mappings that match the provided application context
    * for the specified activity.
    *
    * @param activityInstanceOID the OID of the activity for which the data mappings are
    *        to be retrieved.
    * @param context the application context for which the mappings are retrieved.
    *        The value <code>null</code> will be interpreted as the default context.
    * @param ids the set of data mapping IDs designating the values to be retrieved. If
    *        <code>null</code> is passed, all IN data mappings for the context are
    *        retrieved.
    *
    * @return A Map with corresponding (data mapping ID, data value)-pairs. Data values
    *         are {@link java.io.Serializable}.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified
    *   OID or not all mapping IDs can be resolved in the given context.
    *
    * @see #getInDataValue(long, String, String)
    *
    * @since 3.1.2
    */
   /*@ExecutionPermission(
         id=ExecutionPermission.Id.readDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})*/
   Map<String, Serializable> getInDataValues(long activityInstanceOID, String context, Set<String> ids)
         throws ObjectNotFoundException;

   /**
    * Suspends the specified activity instance. It will be added to the same worklist
    * in which it was prior to activation, and the specified activity instance will be
    * set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    * @param outData the context data containing values of out access points to be stored.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws AccessForbiddenException if the activity instance is already terminated or is
    *         currently processed by another user or the current user does not have the
    *         required permission.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws InvalidArgumentException if QA AI with non empty out data map
    *
    * @see #suspendToDefaultPerformer(long, String, Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspend(long activityInstanceOID, ContextData outData)
         throws ObjectNotFoundException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * default performer declared for the corresponding activity, and the specified
    * activity instance will be set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the activity instance is already terminated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    *
    * @see #suspendToDefaultPerformer(long, String, Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToDefaultPerformer(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * default performer declared for the corresponding activity, and the specified
    * activity instance will be set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    * @param context the ID of the context on which the data mapping will be performed.
    * @param outData a map with values of out access points to be stored.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the activity instance is already terminated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    *
    * @see #suspendToDefaultPerformer(long)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToDefaultPerformer(long activityInstanceOID, String context,
         Map<String, ? > outData) throws ObjectNotFoundException, ConcurrencyException,
         AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * current user, and the specified activity instance will be set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    *
    * @see #suspendToUser(long, String, Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToUser(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * current user, and the specified activity instance will be set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    * @param context the ID of the context on which the data mapping will be performed.
    * @param outData a map with values of out access points to be stored.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    *
    * @see #suspendToUser(long)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToUser(long activityInstanceOID, String context, Map<String, ?> outData)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * provided user, and the specified activity instance will be set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    * @param userOID the OID of the user.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    *
    * @see #suspendToUser(long, long, String, Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToUser(long activityInstanceOID, long userOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * provided user, and the specified activity instance will be set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    * @param userOID the OID of the user.
    * @param context the ID of the context on which the data mapping will be performed.
    * @param outData a map with values of out access points to be stored.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    *
    * @see #suspendToUser(long, long)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToUser(long activityInstanceOID, long userOID, String context,
         Map<String, ? > outData) throws ObjectNotFoundException, ConcurrencyException,
         AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * provided performer, and the specified activity instance will be set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    * @param participant the ID of the performer.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    *
    * @see #suspendToParticipant(long, String, String, Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToParticipant(long activityInstanceOID, String participant)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * provided performer, and the specified activity instance will be set to SUSPENDED state.
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    * @param participant the ID of the performer.
    * @param context the ID of the context on which the data mapping will be performed.
    * @param outData a map with values of out access points to be stored.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    *
    * @see #suspendToParticipant(long, String)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToParticipant(long activityInstanceOID, String participant,
         String context, Map<String, ? > outData) throws ObjectNotFoundException,
         ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the activity instance and, if the participant is not null, delegates it to the specified participant.
    *
    * @param activityInstanceOID the OID of the activity instance.
    * @param participant the participant (model participant, user or user group) to which the activity instance will be delegated.
    * @param outData the context data containing values of out access points to be stored.
    * @return the {@link ActivityInstance} that was suspended.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID or if the participant is not null
    *         and could not be resolved to an actual user user group or model participant.
    * @throws AccessForbiddenException if the activity instance is already terminated or is
    *         currently processed by another user or the current user does not have the
    *         required permission or if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToParticipant(long activityInstanceOID, ParticipantInfo participant,
         ContextData outData) throws ObjectNotFoundException, AccessForbiddenException;

   /**
    * Change the state of the specified activity instance to HIBERNATED.
    *
    * @param activityInstanceOID the OID of the activity to be hibernated.
    *
    * @return the {@link ActivityInstance} that was hibernated.
    *
    * @throws IllegalStateChangeException if that state change is not permitted,
    *         i.e. the activity is already completed or aborted.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL})
   ActivityInstance hibernate(long activityInstanceOID)
         throws IllegalStateChangeException, ObjectNotFoundException;

   /**
    * Starts the process specified by the given <code>ID</code> using the provided data
    * and returns the OID of the newly created process instance.
    *
    * @param id The ID of the process to be started. If multiple models with different IDs
    *        are deployed then the process definition id needs to be qualified with model id,
    *        e.g. "{modelId}processDefinitionId"
    * @param data Contains data IDs as keyset and corresponding data values to be set
    *        as values.
    * @param synchronously Determines whether the process will be started synchronously
    *        or asynchronously.
    *
    * @return the {@link ProcessInstance} that was started.
    *
    * @throws ObjectNotFoundException if there is no process with the specified ID in the
    *         active model or an invalid data id was specified.
    */
   // TODO discuss...
   ProcessInstance startProcess(String id, Map<String, ?> data, boolean synchronously)
         throws ObjectNotFoundException;

   /**
    * Spawns a process as subprocess of the specified process instance. The spawned
    * process executes asynchronously but has to be completed before the parent process is
    * able to complete.
    *
    * @param parentProcessInstanceOid
    *           The oid of the process to spawn from.
    * @param spawnProcessID
    *           The id of the process definition to spawn as a subprocess.
    * @param copyData
    *           Defines if data of the parent process definition should be copied to the
    *           spawned process.
    * @param data
    *           Contains data IDs as keyset and corresponding data values to be set as
    *           values.
    *
    * @return the {@link ProcessInstance} that was spawned.
    * @throws IllegalOperationException
    *            if the process instance is not a case process instance, is not active or
    *            if the process definition is from a different model.
    * @throws ObjectNotFoundException
    *            if there is no process instance with the specified oid or if there is no
    *            process definition with the specified id.
    * @throws ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained.
    *            This can happen while the process hierarchy is currently
    *            locked because of case operations or subprocess creation.
    */
   ProcessInstance spawnSubprocessInstance(long parentProcessInstanceOid,
         String spawnProcessID, boolean copyData, Map<String, ? > data)
         throws IllegalOperationException, ObjectNotFoundException, ConcurrencyException;

   /**
    * Spawns multiple processes as subprocesses of the specified process instance. The
    * spawned processes execute asynchronously but have to be completed before the parent
    * process is able to complete.
    *
    * @param parentProcessInstanceOid
    *           The oid of the process to spawn from.
    * @param subprocessSpawnInfo
    *           A List of {@link SubprocessSpawnInfo} holding information about the
    *           subprocesses to be spawned.
    * @return A list of {@link ProcessInstance} that were spawned.
    * @throws IllegalOperationException
    *            if the process instance is not a case process instance, is not active or
    *            if the process definition is from a different model.
    * @throws ObjectNotFoundException
    *            if there is no process instance with the specified oid or if there is no
    *            process definition with the specified id.
    * @throws ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained.
    *            This can happen while the process hierarchy is currently
    *            locked because of case operations or subprocess creation.
    */
   List<ProcessInstance> spawnSubprocessInstances(long parentProcessInstanceOid,
         List<SubprocessSpawnInfo> subprocessSpawnInfo) throws IllegalOperationException,
         ObjectNotFoundException, ConcurrencyException;

   /**
    * Spawns a new root process and creates a link of type
    * {@link ProcessInstanceLinkType#SWITCH} to the specified process instance.<br>
    * Optionally existing data from the specified process instance can be copied to the
    * newly spawned process.
    * <p>
    * Please note that currently the specified process instance has to be aborted by
    * setting <code>abortProcessInstance</code> to <code>true</code>.
    *
    * @param processInstanceOid
    *           The oid of the process to spawn from.
    * @param spawnProcessID
    *           The id of the process definition to spawn as a new root process.
    * @param copyData
    *           Defines if data of the parent process definition should be copied to the
    *           spawned process.
    * @param data
    *           Contains data IDs as keyset and corresponding data values to be set as
    *           values.
    * @param abortProcessInstance
    *           whether the originating process instance should be aborted. <b>Currently
    *           has to be true</b>.
    * @param comment
    *           Allows to specify a comment for the link that is created.
    * @return The {@link ProcessInstance} that was spawned.
    *
    * @throws IllegalOperationException
    *            if the process instance is terminated or not a root process instance.
    *            if the process instance and the process definition are from different models.
    *            if the process instances process definition is the same as the specified process definition.
    * @throws ObjectNotFoundException
    *            if the process instance for the specified oid or the process definition
    *            for the specified process id is not found.
    * @throws InvalidArgumentException
    *            if <code>abortProcessInstance</code> is false (currently not
    *            implemented).
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.abortProcessInstances,
         scope=ExecutionPermission.Scope.processDefinition,
         defer=true)
   public ProcessInstance spawnPeerProcessInstance(long processInstanceOid,
         String spawnProcessID, boolean copyData, Map<String, ? > data,
         boolean abortProcessInstance, String comment) throws IllegalOperationException,
         ObjectNotFoundException, InvalidArgumentException;

   /**
    * Creates a case process instance which groups the specified members as subprocesses.
    *
    * @param name
    *           The name of the case.
    * @param description
    *           A description for the case.
    * @param memberOids
    *           The oids of the process instances which should become members of the case.
    * @return The case process instance.
    * @See {@link ProcessInstance#isCaseProcessInstance()}
    * @throws ObjectNotFoundException
    *            if one of the process instances referenced by <code>memberOids</code> is
    *            not found.
    * @throws IllegalOperationException
    *            if <code>memberOids</code> contains a process instance which is not a
    *            root process.
    * @throws InvalidArgumentException
    *            if <code>memberOids</code> is empty or null.
    * @throws ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained.
    *            This can happen while the process hierarchy is currently
    *            locked because of case operations or subprocess creation.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.createCase,
         scope=ExecutionPermission.Scope.model,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance createCase(String name, String description, long[] memberOids)
         throws ObjectNotFoundException, IllegalOperationException, InvalidArgumentException, ConcurrencyException;

   /**
    * Adds the process instances referenced by the specified memberOids to the specified
    * case process instance.
    *
    * @param caseOid
    *           The oid of the case process instance.
    * @param memberOids
    *           The oids of the process instances which should become members of the case.
    * @return The case process instance.
    * @throws ObjectNotFoundException
    *            if one of the process instances referenced by <code>memberOids</code> is
    *            not found.
    * @throws IllegalOperationException
    *            if <code>memberOids</code> contains a process instance which is not a
    *            root process or is already a member of the case.
    * @throws AccessForbiddenException if the user is not the owner of the case.
    * @throws ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained.
    *            This can happen while the process hierarchy is currently
    *            locked because of case operations or subprocess creation.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.modifyCase,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false)
   ProcessInstance joinCase(long caseOid, long[] memberOids)
         throws ObjectNotFoundException, IllegalOperationException, AccessForbiddenException, ConcurrencyException;

   /**
    * Removes the process instances referenced by the specified memberOids from the
    * specified case process instance.
    *
    * @param caseOid
    *           The oid of the case process instance.
    * @param memberOids
    *           The oids of the process instances which should be removed from the case.
    * @return The case process instance.
    * @throws ObjectNotFoundException
    *            if one of the process instances referenced by <code>memberOids</code> is
    *            not found.
    * @throws IllegalOperationException
    *            if <code>memberOids</code> contains a process instance which is not a
    *            root process or is not a member of the case.
    * @throws AccessForbiddenException
    *            if the user is not the owner of the case.
    * @throws ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained.
    *            This can happen while the process hierarchy is currently
    *            locked because of case operations or subprocess creation.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.modifyCase,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false)
   ProcessInstance leaveCase(long caseOid, long[] memberOids)
         throws ObjectNotFoundException, IllegalOperationException, AccessForbiddenException, ConcurrencyException;

   /**
    * Merges the specified source case process instances into the target case process instance
    * by adding all case members of the source case process instances as members of the target case process instance.
    *
    * @param targetCaseOid The target case process instance
    * @param sourceCaseOids The source case process instances.
    * @param comment Allows to specify a comment
    * @return The case process instance.
    * @throws ObjectNotFoundException
    *            if one of the process instances referenced by <code>sourceCaseOids</code> is
    *            not found.
    * @throws IllegalOperationException
    *            if <code>sourceCaseOids</code> contains a process instance which is not a
    *            case process instance.
    *            if <code>sourceCaseOids</code> contains a process instance which is not active.
    *            if <code>sourceCaseOids</code> contains a process instance which equals the <code>targetCaseOid</code>.
    *            if <code>targetCaseOid</code> is not a case process instance.
    * @throws AccessForbiddenException
    *            if the user is not the owner of the case.
    * @throws ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained.
    *            This can happen while the process hierarchy is currently
    *            locked because of case operations or subprocess creation.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.modifyCase,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false)
   ProcessInstance mergeCases(long targetCaseOid, long[] sourceCaseOids, String comment)
         throws ObjectNotFoundException, IllegalOperationException, AccessForbiddenException, ConcurrencyException;

   /**
    * Delegates the case process instance to the specified participant.
    *
    * @param caseOid
    *           The case process instance to delegate.
    * @param participant
    *           The targetParticipant.
    * @return The case process instance.
    * @throws ObjectNotFoundException
    *            if one of the process instances referenced by <code>caseOid</code> is not
    *            found.
    * @throws IllegalOperationException
    *            if <code>caseOid</code> is not a case process instance.
    * @throws AccessForbiddenException
    *            if the user is not the owner of the case.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         implied={ExecutionPermission.Id.delegateToDepartment})
   ProcessInstance delegateCase(long caseOid, ParticipantInfo participant)
         throws ObjectNotFoundException, IllegalOperationException, AccessForbiddenException;

   /**
    * Aborts the specified process instance and joins the data into the specified target
    * process instance. Existing data values of the target process instance are not
    * overwritten. Process attachments are merged.
    *
    * @param processInstanceOid
    *           The oid of the process instance which should be aborted and joined into
    *           the target process instance.
    * @param targetProcessInstanceOid
    *           The oid of the process instance that should be the target of the join.
    * @param comment
    *           Allows specifying a comment.
    * @return The target process instance.
    * @throws ObjectNotFoundException
    *            if the process instance referenced by <code>processInstanceOid</code> or
    *            <code>targetProcessInstanceOid</code> do not exist.
    * @throws IllegalOperationException
    *            if the source and target are identical.<br>
    *            if the source or target are not active.<br>
    *            if the join target is a subprocess of the source process instance.<br>
    *            if the source or target is a case process instance.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.abortProcessInstances,
         scope=ExecutionPermission.Scope.processDefinition,
         defer=true)
   public ProcessInstance joinProcessInstance(long processInstanceOid,
         long targetProcessInstanceOid, String comment) throws ObjectNotFoundException, IllegalOperationException;

   /**
    * Aborts the specified activity instance, effectively aborting the whole process
    * instance hierarchy this activity instance belongs to.
    * <p/>
    * Aborting an activity instance is only allowed if the activity was modeled to be
    * abortable (@see Activity#isAbortable()}). Additionally it is required that the
    * aborting user is a valid performing participant for this activity.
    * <p/>
    * Behavior is equivalent to
    * {@link WorkflowService#abortActivityInstance(long, AbortScope)}
    * using <code>AbortScope.RootHierarchy</code>.
    * <p/>
    * Note: Abort is performed asynchronously.
    *
    * @param activityInstanceOID The OID of the activity instance to be aborted.
    *
    * @return The {@link ActivityInstance} that was aborted.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified
    *         OID in the audit trail.
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the current user is not valid or is not granted
    *         access to the activity instance. Also thrown if the activity instance is
    *         already terminated or if the activity is not allowed to be aborted.
    *
    * @see AdministrationService#abortProcessInstance(long)
    * @see WorkflowService#abortActivityInstance(long, AbortScope)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.abortActivityInstances,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER})
   ActivityInstance abortActivityInstance(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Aborts the specified activity instance, effectively aborting the whole process
    * instance hierarchy this activity instance belongs to.
    * <p/>
    * Aborting an activity instance is only allowed if the activity was modeled to be
    * abortable (@see Activity#isAbortable()}). Additionally it is required that the
    * aborting user is a valid performing participant for this activity.
    * <p/>
    * Note: Abort is performed asynchronously.
    *
    * @param activityInstanceOID The OID of the activity instance to be aborted.
    * @param abortScope The scope of abortion. You can either choose the current activity
    *       or the entire process hierarchy.
    *       <br/>If you have chosen <code>AbortScope.SubHierarchy</code> then the specified
    *       activity instance is set to state <code>ActivityInstanceState.Aborting</code>.
    *       The abort itself is performed asynchronously. If activity instance is a subprocess
    *       then the complete subprocess hierarchy will be aborted.
    *       <br/>If you have chosen <code>AbortScope.RootHierarchy</code> abortion is done
    *       starting at the root process instance for specified activity instance. The
    *       specified activity instance will be returned unchanged. The state of the
    *       root process instance will be set to <code>ProcessInstanceState.Aborting</code>.
    *       Abort itself will be performed asynchronously.
    *
    * @return The {@link ActivityInstance} that was aborted.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified
    *         OID in the audit trail.
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws AccessForbiddenException if the current user is not valid or is not granted
    *         access to the activity instance. Also thrown if the activity instance is
    *         already terminated or if the activity is not allowed to be aborted.
    *
    * @see AdministrationService#abortProcessInstance(long)
    * @see WorkflowService#abortActivityInstance(long)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.abortActivityInstances,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER})
   ActivityInstance abortActivityInstance(long activityInstanceOid,
         AbortScope abortScope) throws ObjectNotFoundException, ConcurrencyException,
         AccessForbiddenException;

   /**
    * Aborts the specified process instance. Depending on the scope, it will abort either
    * this process instance only (including eventual subprocesses) or the whole process
    * hierarchy starting with the root process.
    *
    * @param processInstanceOid The OID of the process instance to be aborted.
    * @param abortScope The scope of abortion. You can abort either the spawned process instance
    *        or the entire process hierarchy.
    *
    * @return The {@link ProcessInstance} that was aborted.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified
    *         OID in the audit trail.
    * @throws AccessForbiddenException if the current user is not valid or is not granted
    *         access to abort the process instance.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.abortProcessInstances,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.OWNER})
   ProcessInstance abortProcessInstance(long processInstanceOid,
         AbortScope abortScope) throws ObjectNotFoundException,
         AccessForbiddenException;

   /**
    * @deprecated
    *
    * Retrieves the active model.
    *
    * @return the active model.
    *
    * @throws ObjectNotFoundException if there is no active model.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   DeployedModel getModel()
         throws ObjectNotFoundException;

   /**
    * Retrieves (parts of) the worklist of the currently logged-in user.
    *
    * @param query An instance of class {@link WorklistQuery} describing the requested
    *        view on the worklist.
    *
    * @return An instance of {@link Worklist} making up the requested view on the
    *         current user's worklist.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readActivityInstanceData,
         scope=ExecutionPermission.Scope.workitem,
         defer=true,
         defaults={ExecutionPermission.Default.ALL},
         fixed={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   Worklist getWorklist(WorklistQuery query);

   /**
    * Activates the next activity instance from the given worklist query if any.
    *
    * @param worklist query.
    *
    * @return the {@link ActivityInstance} that was activated.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.workitem,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance activateNextActivityInstance(WorklistQuery query);

   /**
    * Activates the next activity instance after the specified one in the same process instance.
    *
    * @param activityInstanceOID the OID of the last completed activity instance.
    *
    * @return the {@link ActivityInstance} that was activated.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defer=true,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance activateNextActivityInstance(long activityInstanceOID)
         throws ObjectNotFoundException;

   /**
    * Activates the next activity instance for the specified process instance.
    *
    * @param processInstanceOID the OID of the process instance.
    *
    * @return the {@link ActivityInstance} that was activated.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defer=true,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance activateNextActivityInstanceForProcessInstance(long processInstanceOID)
         throws ObjectNotFoundException;

   /**
    * Sets an OUT data path on a process instance as specified in the corresponding
    * process definition.
    *
    * @param processInstanceOID the OID of the process instance.
    * @param id the ID of the data path as defined in the model.
    * @param object the value to set on the data path.
    *
    * @throws ObjectNotFoundException if there is no process instance with the
    *         specified OID, if there is no data path with the specified id or if the
    *         underlying data is not found.
    * @throws InvalidValueException if the <code>object</object> to be written represents
    *         an invalid value, most probably as of a type conflict in case of statically
    *         typed data.
    *
    * @see #setOutDataPaths(long, Map)
    */
   /*@ExecutionPermission(
         id=ExecutionPermission.Id.modifyDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})*/
   void setOutDataPath(long processInstanceOID, String id, Object object)
         throws ObjectNotFoundException, InvalidValueException;

   /**
    * Sets multiple OUT data paths on a process instance as specified in the corresponding
    * process definition.
    *
    * @param processInstanceOID the OID of the process instance.
    * @param values A map of (id, value) pairs to be set, where every ID has to designate
    *       a valid data path as defined in the model.
    *
    * @throws ObjectNotFoundException if there is no process instance with the
    *         specified OID, if there is no data path with the specified id or if the
    *         underlying data is not found.
    * @throws InvalidValueException if one of the <code>values</object> to be written
    *         represents is invalid, most probably as of a type conflict in case of
    *         statically typed data.
    *
    * @see #setOutDataPath(long, String, Object)
    */
   /*@ExecutionPermission(
         id=ExecutionPermission.Id.modifyDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})*/
   void setOutDataPaths(long processInstanceOID, Map<String, ?> values)
         throws ObjectNotFoundException, InvalidValueException;

   /**
    * Retrieves an IN data path on a process instance as specified in the corresponding
    * process definition.
    *
    * @param processInstanceOID the OID of the process instance.
    * @param id the ID of the data path as defined in the model.
    *
    * @return the value of the data path applied to the process instance.
    *
    * @throws ObjectNotFoundException if there is no process instance with the
    *         specified OID, if there is no data path with the specified id or if the
    *         underlying data is not found.
    *
    * @see #getInDataPaths(long, Set)
    */
   /*@ExecutionPermission(
         id=ExecutionPermission.Id.readDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})*/
   Object getInDataPath(long processInstanceOID, String id)
         throws ObjectNotFoundException;

   /**
    * Retrieves multiple IN data paths from a process instance as specified in the
    * corresponding process definition.
    *
    * @param processInstanceOID the OID of the process instance.
    * @param ids the set of data path IDs designating the values to be retrieved. If
    *        <code>null</code> is passed, all IN data paths for the process instance are
    *        retrieved.
    *
    * @return the values of the data paths applied to the process instance.
    *
    * @throws ObjectNotFoundException if there is no process instance with the
    *         specified OID, if there is no data path with the specified id or if the
    *         underlying data is not found.
    *
    * @see #getInDataPath(long, String)
    */
   /*@ExecutionPermission(
         id=ExecutionPermission.Id.readDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})*/
   Map<String, Serializable> getInDataPaths(long processInstanceOID, Set<String> ids)
         throws ObjectNotFoundException;

   /**
    * Delegates the specified activitiy instance to the default worklist of the
    * corresponding activity.
    *
    * @param activityInstanceOID the OID of the activity instance.
    *
    * @return the {@link ActivityInstance} that was delegated.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws AccessForbiddenException if the activity instance is already terminated.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance delegateToDefaultPerformer(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Delegates the specified activity instance to a specific performer.
    *
    * @param activityInstanceOID the OID of the activity instance.
    * @param userOID the OID of the user to which the activity instance will be delegated.
    *
    * @return the {@link ActivityInstance} that was delegated.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws ObjectNotFoundException if there is no activity instance or user with the specified OIDs.
    * @throws AccessForbiddenException if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance delegateToUser(long activityInstanceOID, long userOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Delegates the specified activity instance to a specific performer.
    *
    * @param activityInstanceOID the OID of the activity instance.
    * @param performer the ID of the performer to which the activity instance will be delegated.
    *
    * @return the {@link ActivityInstance} that was delegated.
    *
    * @throws ConcurrencyException if the same activity instance is being processed by another user.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws AccessForbiddenException if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance delegateToParticipant(long activityInstanceOID, String performer)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Delegates the activity instance to the specified participant as follows:
    * <ul>
    * <li>if the participant is null, then delegates the activity to the default performer.</li>
    * <li>if the participant is an instance of a UserInfo, then delegates the activity to the specified user.</li>
    * <li>if the participant is an instance of a UserGroupInfo, then delegates the activity to the specified user group.</li>
    * <li>if the participant is an instance of a ModelParticipantInfo, then delegates the activity to the specified model participant.</li>
    * </ul>
    * @param activityInstanceOID the OID of the activity instance.
    * @param participant the participant (model participant, user or user group) to which the activity instance will be delegated.
    * @return the {@link ActivityInstance} that was delegated.
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID or if the participant is not null
    *         and could not be resolved to an actual user user group or model participant.
    * @throws AccessForbiddenException if the activity instance is already terminated or is
    *         currently processed by another user or the current user does not have the
    *         required permission or if the delegation target is not granted to execute
    *         the activity instance or if the activity instance is already terminated.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance delegateToParticipant(long activityInstanceOID, ParticipantInfo participant)
         throws ObjectNotFoundException, AccessForbiddenException;

   /**
    * Retrieves the specified ActivityInstance.
    *
    * @param activityInstanceOID the OID of the activity instance.
    *
    * @return the {@link ActivityInstance}.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readActivityInstanceData,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         fixed={ExecutionPermission.Default.OWNER})
   ActivityInstance getActivityInstance(long activityInstanceOID)
         throws ObjectNotFoundException;

   /**
    * Retrieves the specified process instance.
    *
    * @param processInstanceOID the OID of the process instance.
    *
    * @return the {@link ProcessInstance}.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance getProcessInstance(long processInstanceOID)
         throws ObjectNotFoundException;

   /**
    * TODO
    *
    * @param processInstanceOID the OID of the process instance.
    *
    * @return TODO
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    * @throws AccessForbiddenException if the process instance is not completed or
    *       the user does not have the permission to access this process.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   Map<String, Serializable> getProcessResults(long processInstanceOID)
         throws ObjectNotFoundException, AccessForbiddenException;

   /**
    * Binds an event handler to the specified activity instance.
    *
    * @param activityInstanceOID the OID of the activity instance.
    * @param eventHandler the specialized form of the event handler to bind.
    *
    * @return the {@link ActivityInstance}.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws BindingException in case of semantic binding errors.
    * @throws InvalidArgumentException in case eventHandler is null.
    *
    * @see #getActivityInstanceEventHandler
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL}) // TODO: check if it should not be by default OWNER
   ActivityInstance bindActivityEventHandler(long activityInstanceOID, EventHandlerBinding eventHandler)
         throws ObjectNotFoundException, BindingException, InvalidArgumentException;

   /**
    * Binds an event handler to the specified process instance.
    *
    * @param processInstanceOID the OID of the process instance.
    * @param eventHandler the specialized form of the event handler to bind.
    *
    * @return the {@link ProcessInstance}.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    * @throws BindingException in case of semantic binding errors.
    *
    * @see #getProcessInstanceEventHandler
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance bindProcessEventHandler(long processInstanceOID, EventHandlerBinding eventHandler)
         throws ObjectNotFoundException, BindingException;

   /**
    * Binds an event handler to the specified activity instance.
    *
    * @param activityInstanceOID the OID of the activity instance.
    * @param handler the ID of the event handler to bind.
    *
    * @return the {@link ActivityInstance}.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws BindingException in case of semantic binding errors.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL}) // TODO: check if it should not be by default OWNER
   ActivityInstance bindActivityEventHandler(long activityInstanceOID, String handler)
         throws ObjectNotFoundException, BindingException;

   /**
    * Binds an event handler to the specified process instance.
    *
    * @param processInstanceOID the OID of the process instance.
    * @param handler the ID of the event handler to bind.
    *
    * @return the {@link ProcessInstance}.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    * @throws BindingException in case of semantic binding errors.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance bindProcessEventHandler(long processInstanceOID, String handler)
         throws ObjectNotFoundException, BindingException;

   /**
    * Unbinds an event handler from the specified activity instance.
    *
    * @param activityInstanceOID the OID of the activity instance.
    * @param handler the ID of the event handler to unbind.
    *
    * @return the {@link ActivityInstance}.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws BindingException in case of semantic binding errors.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL}) // TODO: check if it should not be by default OWNER
   ActivityInstance unbindActivityEventHandler(long activityInstanceOID, String handler)
         throws ObjectNotFoundException, BindingException;

   /**
    * Unbinds an event handler from the specified process instance.
    *
    * @param processInstanceOID the OID of the process instance.
    * @param handler the ID of the event handler to unbind.
    *
    * @return the {@link ProcessInstance}.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    * @throws BindingException in case of semantic binding errors.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance unbindProcessEventHandler(long processInstanceOID, String handler)
      throws ObjectNotFoundException, BindingException;


   /**
    * Gets the binding state of an event handler for the specified activity instance.
    *
    * @param activityInstanceOID the OID of the activity instance.
    * @param handler the ID of the event handler.
    *
    * @return the {@link EventHandlerBinding}.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL}) // TODO: check if it should not be by default OWNER
   EventHandlerBinding getActivityInstanceEventHandler(long activityInstanceOID, String handler)
         throws ObjectNotFoundException;

   /**
    * Gets the binding state of an event handler for the specified process instance.
    *
    * @param processInstanceOID the OID of the process instance.
    * @param handler the ID of the event handler.
    *
    * @return the {@link EventHandlerBinding}.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   EventHandlerBinding getProcessInstanceEventHandler(long processInstanceOID, String handler)
         throws ObjectNotFoundException;

   /**
    * Retrieves the list of process definitions that can be started by the current user.
    *
    * @return a List with {@link org.eclipse.stardust.engine.api.model.ProcessDefinition} objects.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   List<ProcessDefinition> getStartableProcessDefinitions();

   /**
    * Retrieves information on the current user.
    *
    * @return the current user.
    */
   // no permission check here
   User getUser();

   /**
    * Retrieves all permissions the current user has on this service.
    *
    * @return a list of permission ids.
    */
   /*@ExecutionPermission(
         id="readModelData",
         scope=ExecutionPermission.Scope.model,
         changeable=false,
         defaults={ExecutionPermission.Default.ALL})*/
   // no permission check here
   List<Permission> getPermissions();

   /**
    * Sets specific attributes of a process instance.
    * At the moment attributes has to be bound to a scope process instance.
    * <br/>
    * <br/>
    * Note: After a {@link ProcessInstanceAttributes} instance is applied to this method
    * it is discouraged to use this same instance again. Any new note which has been added
    * by the first use will be added again. In order to add new notes to a certain
    * process instance a fresh {@link ProcessInstance} has to be retrieved (e.g. by
    * {@link WorkflowService#getProcessInstance(long)}). Get a current copy of
    * {@link ProcessInstanceAttributes} by {@link ProcessInstance#getAttributes()}.
    *
    * @param attributes the container of attributes.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    * @throws PublicException if the process instance is no scope process instance.
    * @throws InvalidArgumentException if attributes is null.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   void setProcessInstanceAttributes(ProcessInstanceAttributes attributes)
         throws ObjectNotFoundException;


   void setActivityInstanceAttributes(ActivityInstanceAttributes attributes)
         throws ObjectNotFoundException;

   /**
    * Logs an audit trail event of type <code>LogCode.EXTERNAL</code>.
    * @param logType Set the type of log (info, warn, error etc.). Whereas the <code>Unknown</code> type is mapped to a warning.
    * @param contextType Set the context scope of the event
    * @param contextOid Oid of the runtime object (only used if context type is set to ProcessInstance or ActivityInstance)
    * @param message any message that should be logged
    * @param throwable any exception (or null) that should be appended to the message
    *
    * @exception ObjectNotFoundException if there is no runtime object with the specified OID
    */
   // TODO discuss...
   void writeLogEntry(LogType logType, ContextKind contextType, long contextOid,
         String message, Throwable throwable) throws ObjectNotFoundException;

   Serializable execute(ServiceCommand serviceCmd) throws ServiceCommandException;
}