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
import org.eclipse.stardust.engine.api.dto.*;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;
import org.eclipse.stardust.engine.core.runtime.utils.TransientState;

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
    * <p>
    * Activating means:
    * <ul>
    * <li>Removing the activity instance from its original worklist.</li>
    * <li>Adding the activity instance to the logged-in user's worklist.</li>
    * <li>Setting the state of the activity instance to APPLICATION state.</li>
    * </ul>
    * </p>
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: suspended, hibernated or application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: application, activity with application that provides
    * asynchronous receive functionality: hibernated</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be activated.
    *
    * @return the {@link ActivityInstance} that was activated.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not valid or is not granted to execute the
    *            activity instance. Also thrown if the activity instance is already
    *            terminated.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the current user is the
    *            one who worked on the previous workflow instance.
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
    * <p>
    * State Changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: completed</li>
    * <li>Process state after: Completed if all activities are completed. Otherwise state
    * does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be completed.
    * @param context
    *           the ID of the context on which the data mapping will be performed.
    * @param outData
    *           a map with the values of the out access points.
    *
    * @return the {@link ActivityInstance} that was completed.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the activity instance is exclusively locked by another thread.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException
    *            if that state change is not permitted, i.e. the activity is not active.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.InvalidValueException
    *            if one of the <code>outData</code> values to be written is invalid,
    *            most probably as of a type conflict in case of statically typed data.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not allowed to complete the activity.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance and no
    *            {@link org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes
    *            ActivityInstanceAttributes} has been set before.
    * @see #activateAndComplete(long, String, java.util.Map)
    * @see #complete(long, String, java.util.Map, int)
    * @see #setActivityInstanceAttributes(org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance complete(long activityInstanceOID, String context, Map<String, ?> outData)
         throws ConcurrencyException, IllegalStateChangeException,
         ObjectNotFoundException, InvalidValueException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Completes the interactive activity instance identified by the
    * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
    *
    * <p>
    * State Changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: completed</li>
    * <li>Process state after: Completed if all activities are completed. Otherwise state
    * does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be completed.
    * @param context
    *           the ID of the context on which the data mapping will be performed.
    * @param outData
    *           a map with the values of the out access points.
    * @param flags
    *           Optional adjustment to some details of operation. Supported values are
    *           {@link
    *           org.eclipse.stardust.engine.api.runtime.WorkflowService#FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE
    *           WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE}.
    *
    * @return A log describing the result of the invocation. Depends on the flags
    *         parameter.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException
    *            if that state change is not permitted, i.e. the activity is not active.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.InvalidValueException
    *            if one of the <code>outData</code> values to be written is invalid,
    *            most probably as of a type conflict in case of statically typed data.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the activity instance is already terminated.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance and no
    *            {@link org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes
    *            ActivityInstanceAttributes} has been set before.
    *
    * @see #complete(long, String, java.util.Map)
    * @see #activateAndComplete(long, String, java.util.Map)
    * @see #setActivityInstanceAttributes(org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityCompletionLog complete(long activityInstanceOID, String context, Map<String, ?> outData,
         int flags) throws ConcurrencyException, IllegalStateChangeException,
         ObjectNotFoundException, InvalidValueException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Activates and completes the interactive activity instance identified by the
    * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
    *
    * If the activity is activated to be immediately completed, this method is more
    * efficient than invoking activate(...) and complete(...) separately.
    *
    * @param activityInstanceOID
    *           the OID of the activity to be completed.
    * @param context
    *           the ID of the context on which the data mapping will be performed. The
    *           value <code>null</code> will be interpreted as the default context.
    * @param outData
    *           a map with the values of the out access points.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was completed.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.InvalidValueException
    *            if one of the <code>outData</code> values to be written is invalid, most
    *            probably as of a type conflict in case of statically typed data.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not valid or is not granted to execute the
    *            activity instance. Also thrown if the activity instance is already
    *            terminated.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the current user
    *            is the one who worked on the previous workflow instance
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance and no
    *            {@link org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes
    *            ActivityInstanceAttributes} has been set before.
    *
    * @see #activate(long)
    * @see #complete(long, String, java.util.Map)
    * @see #activateAndComplete(long, String, java.util.Map, int)
    * @see #setActivityInstanceAttributes(org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes)
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
    * @param activityInstanceOID
    *           the OID of the activity to be completed.
    * @param context
    *           the ID of the context on which the data mapping will be performed. The
    *           value <code>null</code> will be interpreted as the default context.
    * @param outData
    *           a map with the values of the out access points.
    * @param flags
    *           Optional adjustment to some details of operation. Supported values are
    *           {@link
    *           org.eclipse.stardust.engine.api.runtime.WorkflowService#FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE
    *           WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE}.
    *
    * @return A log describing the result of the invocation. Depends on the flags
    *         parameter.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.InvalidValueException
    *            if one of the <code>outData</code> values to be written is invalid,
    *            most probably as of a type conflict in case of statically typed data.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the current user
    *            is the one who worked on the previous workflow instance
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not valid or is not granted to execute the
    *            activity instance. Also thrown if the activity instance is already
    *            terminated.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance and no
    *            {@link org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes
    *            ActivityInstanceAttributes} has been set before.
    * @see #activateAndComplete(long, String, java.util.Map)
    * @see #activate(long)
    * @see #complete(long, String, java.util.Map)
    * @see #setActivityInstanceAttributes(org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes)
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
    * @param activityInstanceOID
    *           the OID of the activity for which the data mappings are to be retrieved.
    * @param context
    *           the application context for which the mappings are retrieved. The value
    *           <code>null</code> will be interpreted as the default context.
    * @param id
    *           The ID of the data mapping to be retrieved.
    *
    * @return The retrieved value.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID or there is no
    *            mapping with the given ID under the given context.
    *
    * @see #getInDataValues(long, String, java.util.Set)
    *
    * @since 3.1.2
    */
   // This method also checks data.readDataValues permission for the accessed data
   @ExecutionPermission(
         id=ExecutionPermission.Id.readActivityInstanceData,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         fixed={ExecutionPermission.Default.OWNER})
   Serializable getInDataValue(long activityInstanceOID, String context, String id)
         throws ObjectNotFoundException;

   /**
    * Retrieves all evaluated IN data mappings that match the provided application context
    * for the specified activity.
    *
    * @param activityInstanceOID
    *           the OID of the activity for which the data mappings are to be retrieved.
    * @param context
    *           the application context for which the mappings are retrieved. The value
    *           <code>null</code> will be interpreted as the default context.
    * @param ids
    *           the set of data mapping IDs designating the values to be retrieved. If
    *           <code>null</code> is passed, all IN data mappings for the context are
    *           retrieved.
    *
    * @return A Map with corresponding (data mapping ID, data value)-pairs. Data values
    *         are {@link java.io.Serializable}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID or not all
    *            mapping IDs can be resolved in the given context.
    *
    * @see #getInDataValue(long, String, String)
    *
    * @since 3.1.2
    */
   // This method also checks data.readDataValues permission for each accessed data
   @ExecutionPermission(
         id=ExecutionPermission.Id.readActivityInstanceData,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         fixed={ExecutionPermission.Default.OWNER})
   Map<String, Serializable> getInDataValues(long activityInstanceOID, String context, Set<String> ids)
         throws ObjectNotFoundException;

   /**
    * Suspends the specified activity instance. It will be added to the same worklist in
    * which it was prior to activation, and the specified activity instance will be set to
    * SUSPENDED state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    * @param outData
    *           the context data containing values of out access points to be stored.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the activity instance is already terminated or is currently processed
    *            by another user or the current user does not have the required
    *            permission.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    *
    * @see #suspendToDefaultPerformer(long, String, java.util.Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance suspend(long activityInstanceOID, ContextData outData)
         throws ObjectNotFoundException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * default performer declared for the corresponding activity, and the specified
    * activity instance will be set to SUSPENDED state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    *
    * @see #suspendToDefaultPerformer(long, String, java.util.Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance suspendToDefaultPerformer(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * default performer declared for the corresponding activity, and the specified
    * activity instance will be set to SUSPENDED state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    * @param context
    *           the ID of the context on which the data mapping will be performed.
    * @param outData
    *           a map with values of out access points to be stored.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    *
    * @see #suspendToDefaultPerformer(long)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance suspendToDefaultPerformer(long activityInstanceOID, String context,
         Map<String, ? > outData) throws ObjectNotFoundException, ConcurrencyException,
         AccessForbiddenException, InvalidArgumentException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * current user, and the specified activity instance will be set to SUSPENDED state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    *
    * @see #suspendToUser(long, String, java.util.Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance suspendToUser(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * current user, and the specified activity instance will be set to SUSPENDED state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    * @param context
    *           the ID of the context on which the data mapping will be performed.
    * @param outData
    *           a map with values of out access points to be stored.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @see #suspendToUser(long)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance suspendToUser(long activityInstanceOID, String context, Map<String, ?> outData)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * provided user, and the specified activity instance will be set to SUSPENDED state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    * @param userOID
    *           the OID of the user.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the specified user
    *            is the one who worked on the previous workflow instance
    *
    * @see #suspendToUser(long, long, String, java.util.Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false,
         implied={ExecutionPermission.Id.delegateToOther, ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToUser(long activityInstanceOID, long userOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * provided user, and the specified activity instance will be set to SUSPENDED state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    * @param userOID
    *           the OID of the user.
    * @param context
    *           the ID of the context on which the data mapping will be performed.
    * @param outData
    *           a map with values of out access points to be stored.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the specified user
    *            is the one who worked on the previous workflow instance
    * @see #suspendToUser(long, long)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false,
         implied={ExecutionPermission.Id.delegateToOther, ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToUser(long activityInstanceOID, long userOID, String context,
         Map<String, ? > outData) throws ObjectNotFoundException, ConcurrencyException,
         AccessForbiddenException, InvalidArgumentException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * provided performer, and the specified activity instance will be set to SUSPENDED
    * state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    * @param participant
    *           the ID of the performer.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    *
    * @see #suspendToParticipant(long, String, String, java.util.Map)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false,
         implied={ExecutionPermission.Id.delegateToOther, ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToParticipant(long activityInstanceOID, String participant)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException;

   /**
    * Suspends the specified activity instance. It will be added to the worklist of the
    * provided performer, and the specified activity instance will be set to SUSPENDED
    * state.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity to be suspended.
    * @param participant
    *           the ID of the performer.
    * @param context
    *           the ID of the context on which the data mapping will be performed.
    * @param outData
    *           a map with values of out access points to be stored.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @see #suspendToParticipant(long, String)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false,
         implied={ExecutionPermission.Id.delegateToOther, ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToParticipant(long activityInstanceOID, String participant,
         String context, Map<String, ? > outData) throws ObjectNotFoundException,
         ConcurrencyException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Suspends the activity instance and, if the participant is not null, delegates it to
    * the specified participant.
    *
    * <p>
    * State changes:
    * <ul>
    * <li>Activity state before: application</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           the OID of the activity instance.
    * @param participant
    *           the participant (model participant, user or user group) to which the
    *           activity instance will be delegated.
    * @param outData
    *           the context data containing values of out access points to be stored.
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was suspended.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID or if the
    *            participant is not null and could not be resolved to an actual user user
    *            group or model participant.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the activity instance is already terminated or is currently processed
    *            by another user or the current user does not have the required permission
    *            or if the delegation target is not granted to execute the activity
    *            instance or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the passed
    *            participant is a user who worked on the previous workflow instance
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false,
         implied={ExecutionPermission.Id.delegateToOther, ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance suspendToParticipant(long activityInstanceOID, ParticipantInfo participant,
         ContextData outData) throws ObjectNotFoundException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Change the state of the specified activity instance to HIBERNATED.
    *
    * @param activityInstanceOID
    *           the OID of the activity to be hibernated.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was hibernated.
    *
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException
    *            if that state change is not permitted, i.e. the activity is already
    *            completed or aborted.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
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
    * <p>
    * State changes:
    * <ul>
    * <li>Process state after: active</li>
    * </ul>
    * </p>
    *
    * @param id
    *           The ID of the process to be started. If multiple models with different IDs
    *           are deployed then the process definition id needs to be qualified with
    *           model id, e.g. "{modelId}processDefinitionId"
    * @param data
    *           Contains data IDs as keyset and corresponding data values to be set as
    *           values.
    * @param synchronously
    *           Determines whether the process will be started synchronously or
    *           asynchronously.
    *
    * @return the {@link ProcessInstance} that was started.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process with the specified ID in the active model or an
    *            invalid data id was specified.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.startProcesses,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance startProcess(String id, Map<String, ?> data, boolean synchronously)
         throws ObjectNotFoundException;


   @ExecutionPermission(id=ExecutionPermission.Id.startProcesses,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance startProcess(String id, StartOptions options);

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
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the process instance is not a case process instance, is not active or
    *            if the process definition is from a different model.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified oid or if there is no
    *            process definition with the specified id.
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained. This
    *            can happen while the process hierarchy is currently locked because of
    *            case operations or subprocess creation.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.spawnSubProcessInstance,
         defaults={ExecutionPermission.Default.ALL})
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
    *           A List of {@link
    *           org.eclipse.stardust.engine.api.runtime.SubprocessSpawnInfo
    *           SubprocessSpawnInfo} holding information about the subprocesses to be
    *           spawned.
    * @return A list of {@link ProcessInstance} that were spawned.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the process instance is not a case process instance, is not active or
    *            if the process definition is from a different model.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified oid or if there is no
    *            process definition with the specified id.
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained. This
    *            can happen while the process hierarchy is currently locked because of
    *            case operations or subprocess creation.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.spawnSubProcessInstance,
         defaults={ExecutionPermission.Default.ALL})
   List<ProcessInstance> spawnSubprocessInstances(long parentProcessInstanceOid,
         List<SubprocessSpawnInfo> subprocessSpawnInfo) throws IllegalOperationException,
         ObjectNotFoundException, ConcurrencyException;

   /**
    * Spawns a new root process and creates a link of type
    * {@link org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes#SWITCH
    * PredefinedProcessInstanceLinkTypes.SWITCH} to the specified process instance.<br>
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
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the process instance is terminated or not a root process instance. if
    *            the process instance and the process definition are from different
    *            models. if the process instances process definition is the same as the
    *            specified process definition.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if the process instance for the specified oid or the process definition
    *            for the specified process id is not found.
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if <code>abortProcessInstance</code> is false (currently not
    *            implemented).
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if a lock on process instances cannot be obtained.
    * @deprecated use {@link
    *          #spawnPeerProcessInstance(long, String, org.eclipse.stardust.engine.api.runtime.SpawnOptions)
    *          spawnPeerProcessInstance(long, String, SpawnOptions)}
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.spawnPeerProcessInstance,
         defaults={ExecutionPermission.Default.ALL})
   @Deprecated
   public ProcessInstance spawnPeerProcessInstance(long processInstanceOid,
         String spawnProcessID, boolean copyData, Map<String, ? extends Serializable> data,
         boolean abortProcessInstance, String comment) throws IllegalOperationException,
         ObjectNotFoundException, InvalidArgumentException, ConcurrencyException;

   /**
    * Spawns a new root process and creates a link of type
    * {@link org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes#SWITCH
    * PredefinedProcessInstanceLinkTypes.SWITCH} to the specified process instance.<br>
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
    * @param options
    *           Options that controls how the spawning operation has to be performed.
    * @return The {@link ProcessInstance} that was spawned.
    *
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the process instance is terminated or not a root process instance. if
    *            the process instance and the process definition are from different
    *            models. if the process instances process definition is the same as the
    *            specified process definition.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if the process instance for the specified oid or the process definition
    *            for the specified process id is not found.
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if <code>abortProcessInstance</code> is false (currently not
    *            implemented).
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if a lock on process instances cannot be obtained.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.spawnPeerProcessInstance,
         defaults={ExecutionPermission.Default.ALL})
   public ProcessInstance spawnPeerProcessInstance(long processInstanceOid,
         String spawnProcessID, SpawnOptions options) throws IllegalOperationException,
         ObjectNotFoundException, InvalidArgumentException, ConcurrencyException;

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
    * @see org.eclipse.stardust.engine.api.runtime.ProcessInstance#isCaseProcessInstance()
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if one of the process instances referenced by <code>memberOids</code> is
    *            not found.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if <code>memberOids</code> contains a process instance which is not a
    *            root process.
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if <code>memberOids</code> is empty or null.
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained. This
    *            can happen while the process hierarchy is currently locked because of
    *            case operations or subprocess creation.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.createCase,
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
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if one of the process instances referenced by <code>memberOids</code> is
    *            not found.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if <code>memberOids</code> contains a process instance which is not a
    *            root process or is already a member of the case.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the user is not the owner of the case.
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained. This
    *            can happen while the process hierarchy is currently locked because of
    *            case operations or subprocess creation.
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
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if one of the process instances referenced by <code>memberOids</code> is
    *            not found.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if <code>memberOids</code> contains a process instance which is not a
    *            root process or is not a member of the case.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the user is not the owner of the case.
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained. This
    *            can happen while the process hierarchy is currently locked because of
    *            case operations or subprocess creation.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.modifyCase,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false)
   ProcessInstance leaveCase(long caseOid, long[] memberOids)
         throws ObjectNotFoundException, IllegalOperationException, AccessForbiddenException, ConcurrencyException;

   /**
    * Merges the specified source case process instances into the target case process
    * instance by adding all case members of the source case process instances as members
    * of the target case process instance.
    *
    * @param targetCaseOid
    *           The target case process instance
    * @param sourceCaseOids
    *           The source case process instances.
    * @param comment
    *           Allows to specify a comment
    * @return The case process instance.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if one of the process instances referenced by <code>sourceCaseOids</code>
    *            is not found.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if <code>sourceCaseOids</code> contains a process instance which is not a
    *            case process instance. if <code>sourceCaseOids</code> contains a process
    *            instance which is not active. if <code>sourceCaseOids</code> contains a
    *            process instance which equals the <code>targetCaseOid</code>. if
    *            <code>targetCaseOid</code> is not a case process instance.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the user is not the owner of the case.
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if a lock on transitions or process instances cannot be obtained. This
    *            can happen while the process hierarchy is currently locked because of
    *            case operations or subprocess creation.
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
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if one of the process instances referenced by <code>caseOid</code> is not
    *            found.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if <code>caseOid</code> is not a case process instance.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the user is not the owner of the case.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the specified user
    *            is the one who worked on the previous workflow instance
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
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if the process instance referenced by <code>processInstanceOid</code> or
    *            <code>targetProcessInstanceOid</code> do not exist.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the source and target are identical.<br>
    *            if the source or target are not active.<br>
    *            if the join target is a subprocess of the source process instance.<br>
    *            if the source or target is a case process instance.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.joinProcessInstance,
         defaults={ExecutionPermission.Default.ALL})
   public ProcessInstance joinProcessInstance(long processInstanceOid,
         long targetProcessInstanceOid, String comment) throws ObjectNotFoundException, IllegalOperationException;

   /**
    * Aborts the specified activity instance, effectively aborting the whole process
    * instance hierarchy this activity instance belongs to.
    * <p/>
    * Aborting an activity instance is only allowed if the activity was modeled to be
    * abortable (see @link org.eclipse.stardust.engine.api.model.Activity#isAbortable()
    * Activity.isAbortable()}). Additionally it is required that the
    * aborting user is a valid performing participant for this activity.
    * <p/>
    * Behavior is equivalent to
    * {@link #abortActivityInstance(long, org.eclipse.stardust.engine.core.runtime.beans.AbortScope)
    * abortActivityInstance(long, AbortScope)} using <code>AbortScope.RootHierarchy</code>
    * .
    * <p/>
    * Note: Abort is performed asynchronously.
    *
    * <p>
    * State changes
    * <ul>
    * <li>Activity state before: suspended, application, interrupted, hibernated</li>
    * <li>Process state before: active, interrupted</li>
    * <li>State after: <br>
    * <i>If abort scope is root hierarchy:</i> The state of the specified activity, its
    * root process, all contained sub-processes and activities that are not yet completed
    * changes to aborted. <br>
    * <i>If abort scope is sub hierarchy:</i> The state of the specified activity changes
    * to aborted. If activity instance is a subprocess then the state of contained
    * subprocesses and activities also changes to aborted. <br>
    * If the last activity of the process is aborted and is not a subprocess then the
    * process state will be set to completed.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID
    *           The OID of the activity instance to be aborted.
    *
    * @return The {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was aborted.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID in the audit
    *            trail.
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not valid or is not granted access to the activity
    *            instance. Also thrown if the activity instance is already terminated or
    *            if the activity is not allowed to be aborted.
    *
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#abortProcessInstance(long)
    * @see #abortActivityInstance(long,
    *      org.eclipse.stardust.engine.core.runtime.beans.AbortScope)
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
    * abortable (see {@link org.eclipse.stardust.engine.api.model.Activity#isAbortable()
    * Activity.isAbortable()}). Additionally it is required that the
    * aborting user is a valid performing participant for this activity.
    * <p/>
    * Note: Abort is performed asynchronously.
    *
    * <p>
    * State changes
    * <ul>
    * <li>Activity state before: suspended, application, interrupted, hibernated</li>
    * <li>Process state before: active, interrupted</li>
    * <li>State after: <br>
    * <i>If abort scope is root hierarchy:</i> The state of the specified activity, its
    * root process, all contained sub-processes and activities that are not yet completed
    * changes to aborted. <br>
    * <i>If abort scope is sub hierarchy:</i> The state of the specified activity changes
    * to aborted. If activity instance is a subprocess then the state of contained
    * subprocesses and activities also changes to aborted. <br>
    * If the last activity of the process is aborted and is not a subprocess then the
    * process state will be set to completed.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOid
    *           The OID of the activity instance to be aborted.
    * @param abortScope
    *           The scope of abortion. You can either choose the current activity or the
    *           entire process hierarchy. <br/>
    *           If you have chosen <code>AbortScope.SubHierarchy</code> then the specified
    *           activity instance is set to state
    *           <code>ActivityInstanceState.Aborting</code>. The abort itself is performed
    *           asynchronously. If activity instance is a subprocess then the complete
    *           subprocess hierarchy will be aborted. <br/>
    *           If you have chosen <code>AbortScope.RootHierarchy</code> abortion is done
    *           starting at the root process instance for specified activity instance. The
    *           specified activity instance will be returned unchanged. The state of the
    *           root process instance will be set to
    *           <code>ProcessInstanceState.Aborting</code>. Abort itself will be performed
    *           asynchronously.
    *
    * @return The {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was aborted.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID in the audit
    *            trail.
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not valid or is not granted access to the activity
    *            instance. Also thrown if the activity instance is already terminated or
    *            if the activity is not allowed to be aborted.
    *
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#abortProcessInstance(long)
    * @see #abortActivityInstance(long)
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
    * <p>
    * State changes:
    * <ul>
    * <li>Process state before: active, interrupted</li>
    * <li>State after: <br>
    * <i>If abort scope is root hierarchy:</i> The state of root process, all
    * sub-processes and activities that are not yet completed changes to aborted.</li>
    * <br>
    * <i>If abort scope is sub hierarchy:</i> The state of the sub-process, all its
    * subprocesses and activities that are not yet completed changes to aborted.</li>
    * </ul>
    * </p>
    *
    * @param processInstanceOid
    *           The OID of the process instance to be aborted.
    * @param abortScope
    *           The scope of abortion. You can abort either the spawned process instance
    *           or the entire process hierarchy.
    *
    * @return The {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance
    *         ProcessInstance} that was aborted.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID in the audit
    *            trail.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not valid or is not granted access to abort the
    *            process instance.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.abortProcessInstances,
         scope=ExecutionPermission.Scope.processDefinition)
   ProcessInstance abortProcessInstance(long processInstanceOid,
         AbortScope abortScope) throws ObjectNotFoundException,
         AccessForbiddenException;

   /**
    * @deprecated
    *
    *             Retrieves the active model.
    *
    * @return the active model.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no active model.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   DeployedModel getModel()
         throws ObjectNotFoundException;

   /**
    * Retrieves (parts of) the worklist of the currently logged-in user.
    *
    * @param query
    *           An instance of class
    *           {@link org.eclipse.stardust.engine.api.query.WorklistQuery WorklistQuery}
    *           describing the requested view on the worklist.
    *
    * @return An instance of {@link org.eclipse.stardust.engine.api.query.Worklist
    *         Worklist} making up the requested view on the current user's worklist.
    */
   @TransientState
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
    * @param query
    *           worklist query.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the current user
    *            is the one who worked on the previous workflow instance
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was activated.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.workitem,
         defaults={ExecutionPermission.Default.OWNER},
         changeable=false,
         administratorOverride=false)
   ActivityInstance activateNextActivityInstance(WorklistQuery query);

   /**
    * Activates the next activity instance after the specified one in the same process
    * instance. The activation is based on a given time frame between the completion of
    * the current and the instantiation of the next activity. There might occur scenarios
    * where this method will not be able to retrieve the next activity due to the runtime
    * situation
    *
    * @param activityInstanceOID
    *           the OID of the last completed activity instance.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was activated.
    *
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the current user
    *            is the one who worked on the previous workflow instance
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
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
    * Activates the next activity instance for the specified process instance. The
    * activation is based on a given time frame between the completion of the current and
    * the instantiation of the next activity. There might occur scenarios where this
    * method will not be able to retrieve the next activity due to the runtime situation
    *
    * @param processInstanceOID
    *           the OID of the process instance.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was activated.
    *
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the current user
    *            is the one who worked on the previous workflow instance
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID.
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
    * @param processInstanceOID
    *           the OID of the process instance.
    * @param id
    *           the ID of the data path as defined in the model.
    * @param object
    *           the value to set on the data path.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID, if there is no
    *            data path with the specified id or if the underlying data is not found.
    * @throws org.eclipse.stardust.common.error.InvalidValueException
    *            if the <code>object</code> to be written represents an invalid value,
    *            most probably as of a type conflict in case of statically typed data.
    *
    * @see #setOutDataPaths(long, java.util.Map)
    */
   // This method also checks data.modifyDataValues permission for the accessed data
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   void setOutDataPath(long processInstanceOID, String id, Object object)
         throws ObjectNotFoundException, InvalidValueException;

   /**
    * Sets multiple OUT data paths on a process instance as specified in the corresponding
    * process definition.
    *
    * @param processInstanceOID
    *           the OID of the process instance.
    * @param values
    *           A map of (id, value) pairs to be set, where every ID has to designate a
    *           valid data path as defined in the model.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID, if there is no
    *            data path with the specified id or if the underlying data is not found.
    * @throws org.eclipse.stardust.common.error.InvalidValueException
    *            if one of the <code>values</code> to be written represents is invalid,
    *            most probably as of a type conflict in case of statically typed data.
    *
    * @see #setOutDataPath(long, String, Object)
    */
   // This method also checks data.modifyDataValues permission for each accessed data
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   void setOutDataPaths(long processInstanceOID, Map<String, ?> values)
         throws ObjectNotFoundException, InvalidValueException;

   /**
    * Retrieves an IN data path on a process instance as specified in the corresponding
    * process definition.
    *
    * @param processInstanceOID
    *           the OID of the process instance.
    * @param id
    *           the ID of the data path as defined in the model.
    *
    * @return the value of the data path applied to the process instance.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID, if there is no
    *            data path with the specified id or if the underlying data is not found.
    *
    * @see #getInDataPaths(long, java.util.Set)
    */
   // This method also checks data.readDataValues permission for the accessed data
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   Object getInDataPath(long processInstanceOID, String id)
         throws ObjectNotFoundException;

   /**
    * Retrieves multiple IN data paths from a process instance as specified in the
    * corresponding process definition.
    *
    * @param processInstanceOID
    *           the OID of the process instance.
    * @param ids
    *           the set of data path IDs designating the values to be retrieved. If
    *           <code>null</code> is passed, all IN data paths for the process instance
    *           are retrieved.
    *
    * @return the values of the data paths applied to the process instance.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID, if there is no
    *            data path with the specified id or if the underlying data is not found.
    *
    * @see #getInDataPath(long, String)
    */
   // This method also checks data.readDataValues permission for each accessed data
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   Map<String, Serializable> getInDataPaths(long processInstanceOID, Set<String> ids)
         throws ObjectNotFoundException;

   /**
    * Delegates the specified activitiy instance to the default worklist of the
    * corresponding activity.
    *
    * @param activityInstanceOID
    *           the OID of the activity instance.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was delegated.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the activity instance is already terminated.
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
    * @param activityInstanceOID
    *           the OID of the activity instance.
    * @param userOID
    *           the OID of the user to which the activity instance will be delegated.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was delegated.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance or user with the specified OIDs.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the specified user
    *            is the one who worked on the previous workflow instance
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance delegateToUser(long activityInstanceOID, long userOID)
         throws ObjectNotFoundException, ConcurrencyException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Delegates the specified activity instance to a specific performer.
    *
    * @param activityInstanceOID
    *           the OID of the activity instance.
    * @param performer
    *           the ID of the performer to which the activity instance will be delegated.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was delegated.
    *
    * @throws org.eclipse.stardust.common.error.ConcurrencyException
    *            if the same activity instance is being processed by another user.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the delegation target is not granted to execute the activity instance
    *            or if the activity instance is already terminated.
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
    * <li>if the participant is null, then delegates the activity to the default
    * performer.</li>
    * <li>if the participant is an instance of a UserInfo, then delegates the activity to
    * the specified user.</li>
    * <li>if the participant is an instance of a UserGroupInfo, then delegates the
    * activity to the specified user group.</li>
    * <li>if the participant is an instance of a ModelParticipantInfo, then delegates the
    * activity to the specified model participant.</li>
    * </ul>
    * 
    * @param activityInstanceOID
    *           the OID of the activity instance.
    * @param participant
    *           the participant (model participant, user or user group) to which the
    *           activity instance will be delegated.
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance} that was delegated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID or if the
    *            participant is not null and could not be resolved to an actual user user
    *            group or model participant.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the activity instance is already terminated or is currently processed
    *            by another user or the current user does not have the required permission
    *            or if the delegation target is not granted to execute the activity
    *            instance or if the activity instance is already terminated.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the specified activity instance is a quality assurance instance
    *            {@link
    *            org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState#IS_QUALITY_ASSURANCE
    *            QualityAssuranceState.IS_QUALITY_ASSURANCE} and the specified user
    *            is the one who worked on the previous workflow instance
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.delegateToOther,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL},
         implied={ExecutionPermission.Id.delegateToDepartment})
   ActivityInstance delegateToParticipant(long activityInstanceOID, ParticipantInfo participant)
         throws ObjectNotFoundException, AccessForbiddenException, InvalidArgumentException;

   /**
    * Retrieves the specified ActivityInstance.
    *
    * @param activityInstanceOID
    *           the OID of the activity instance.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
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
    * @param processInstanceOID
    *           the OID of the process instance.
    *
    * @return the {@link ProcessInstance}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance getProcessInstance(long processInstanceOID)
         throws ObjectNotFoundException;

   /**
    * Process instances can declare or implement process interfaces. These process
    * interfaces have "input" and "output". "input" is represented by IN and INOUT
    * parameters, "output" by OUT and INOUT parameters.
    *
    * This method allows to retrieve all "output" values for a specific process instance.
    * This process instance needs to be a terminated scope process instance which
    * implements a process interface.
    *
    * @param processInstanceOID
    *           the OID of the process instance.
    *
    * @return map containing all "output" parameters defined in process interface of
    *         process instance. If the process instance does not implement any process
    *         interface the map will be empty.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the process instance is not completed, is no scope process instance or
    *            the user does not have the permission to access this process.
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
    * @param activityInstanceOID
    *           the OID of the activity instance.
    * @param eventHandler
    *           the specialized form of the event handler to bind.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.engine.api.runtime.BindingException
    *            in case of semantic binding errors.
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            in case eventHandler is null.
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
    * @param processInstanceOID
    *           the OID of the process instance.
    * @param eventHandler
    *           the specialized form of the event handler to bind.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance
    *         ProcessInstance}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID.
    * @throws org.eclipse.stardust.engine.api.runtime.BindingException
    *            in case of semantic binding errors.
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
    * @param activityInstanceOID
    *           the OID of the activity instance.
    * @param handler
    *           the ID of the event handler to bind.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.engine.api.runtime.BindingException
    *            in case of semantic binding errors.
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
    * @param processInstanceOID
    *           the OID of the process instance.
    * @param handler
    *           the ID of the event handler to bind.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance
    *         ProcessInstance}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID.
    * @throws org.eclipse.stardust.engine.api.runtime.BindingException
    *            in case of semantic binding errors.
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
    * @param activityInstanceOID
    *           the OID of the activity instance.
    * @param handler
    *           the ID of the event handler to unbind.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance
    *         ActivityInstance}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
    * @throws org.eclipse.stardust.engine.api.runtime.BindingException
    *            in case of semantic binding errors.
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
    * @param processInstanceOID
    *           the OID of the process instance.
    * @param handler
    *           the ID of the event handler to unbind.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance
    *         ProcessInstance}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID.
    * @throws org.eclipse.stardust.engine.api.runtime.BindingException
    *            in case of semantic binding errors.
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
    * @param activityInstanceOID
    *           the OID of the activity instance.
    * @param handler
    *           the ID of the event handler.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
    *         EventHandlerBinding}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified OID.
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
    * @param processInstanceOID
    *           the OID of the process instance.
    * @param handler
    *           the ID of the event handler.
    *
    * @return the {@link org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
    *         EventHandlerBinding}.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.manageEventHandlers,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   EventHandlerBinding getProcessInstanceEventHandler(long processInstanceOID, String handler)
         throws ObjectNotFoundException;

   /**
    * Retrieves the possible targets for forward transitions starting from the specified
    * activity instance.
    *
    * @param activityInstanceOid
    *           the oid of the activity instance from where the transition will be
    *           performed.
    * @param options
    *           search options, if null then TransitionOptions.DEFAULT will be used.
    * @param direction
    *           TODO
    * @return A list of possible transition targets.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified oid.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         scope=ExecutionPermission.Scope.activity,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})
   List<TransitionTarget> getAdHocTransitionTargets(long activityInstanceOid, TransitionOptions options, ScanDirection direction)
         throws ObjectNotFoundException;

   /**
    * Performs the transition from the specified activity instance to the specified
    * target.
    *
    * @param activityInstanceOid
    *           the oid of the activity instance from where the transition will be
    *           performed.
    * @param target
    *           the transition target.
    * @param complete
    *           true if the activity instance specified should be completed, false if the
    *           activity should be aborted.
    * @return the activity instance from which the transition was performed.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the transition could not be performed because the specified
    *            TransitionTarget did not originate from the specified activity instance,
    *            or the process instance containing the activity instance has more than
    *            one active activity instance.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not allowed to perform the ad-hoc transition, or
    *            the activity instance was already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified oid.
    * @deprecated replaced with {@link
    *          #performAdHocTransition(org.eclipse.stardust.engine.api.runtime.TransitionTarget, boolean)
    *          performAdHocTransition(TransitionTarget, boolean)}
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER})
   ActivityInstance performAdHocTransition(long activityInstanceOid, TransitionTarget target, boolean complete)
         throws IllegalOperationException, ObjectNotFoundException, AccessForbiddenException;

   /**
    * Performs the transition from the specified activity instance to the specified
    * target.
    *
    * @param target
    *           the transition target.
    * @param complete
    *           true if the activity instance specified should be completed, false if the
    *           activity should be aborted.
    * @return a pair of activity instances, where the first is the activity instance from
    *         which the transition was performed and the second is the activity instance
    *         that was created for the target activity.
    * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
    *            if the transition could not be performed because the specified
    *            TransitionTarget did not originate from the specified activity instance,
    *            or the process instance containing the activity instance has more than
    *            one active activity instance.
    * @throws org.eclipse.stardust.common.error.AccessForbiddenException
    *            if the current user is not allowed to perform the ad-hoc transition, or
    *            the activity instance was already terminated.
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no activity instance with the specified oid.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.performActivity,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.OWNER})
   TransitionReport performAdHocTransition(TransitionTarget target, boolean complete)
         throws IllegalOperationException, ObjectNotFoundException, AccessForbiddenException;

   /**
    * Retrieves the list of process definitions that can be started by the current user.
    *
    * @return a List with {@link org.eclipse.stardust.engine.api.model.ProcessDefinition
    *         ProcessDefinition} objects.
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
   @ExecutionPermission
   User getUser();

   /**
    * Retrieves all permissions the current user has on this service.
    *
    * @return a list of permission ids.
    */
   @ExecutionPermission
   List<Permission> getPermissions();

   /**
    * Sets specific attributes of a process instance. At the moment attributes has to be
    * bound to a scope process instance. <br/>
    * <br/>
    * Note: After a {@link org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes
    * ProcessInstanceAttributes} instance is applied to this method it is discouraged to
    * use this same instance again. Any new note which has been added by the first use
    * will be added again. In order to add new notes to a certain process instance a fresh
    * {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance} has to be retrieved
    * (e.g. by {@link #getProcessInstance(long)}). Get a current copy of
    * {@link org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes
    * ProcessInstanceAttributes} by
    * {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance#getAttributes()
    * ProcessInstance.getAttributes()}.
    *
    * @param attributes
    *           the container of attributes.
    *
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            if there is no process instance with the specified OID.
    * @throws org.eclipse.stardust.common.error.PublicException
    *            if the process instance is no scope process instance.
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if attributes is null.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyAttributes,
         scope=ExecutionPermission.Scope.processDefinition,
         defaults={ExecutionPermission.Default.ALL})
   void setProcessInstanceAttributes(ProcessInstanceAttributes attributes)
         throws ObjectNotFoundException;

   /**
    * Sets attributes for an activity instance
    *
    * @param attributes
    *           - the attributes to set
    * @throws org.eclipse.stardust.common.error.ObjectNotFoundException
    *            - if the activity instance specified by {@link
    *            org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes#getActivityInstanceOid()
    *            ActivityInstanceAttributes.getActivityInstanceOid()} could no be found.
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            - when a result is set ({@link
    *            org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes#getQualityAssuranceResult()
    *            ActivityInstanceAttributes.getQualityAssuranceResult()} and the codes
    *            list({@link
    *            org.eclipse.stardust.engine.api.dto.QualityAssuranceResult#getQualityAssuranceCodes()
    *            QualityAssuranceResult.getQualityAssuranceCodes()} contains a
    *            null element
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            - when the specified quality assurance {@link
    *            org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes#getActivityInstanceOid()
    *            ActivityInstanceAttributes.getActivityInstanceOid()} instance is marked
    *            as {@link
    *            org.eclipse.stardust.engine.api.dto.QualityAssuranceResult.ResultState#PASS_WITH_CORRECTION
    *            ResultState.PASS_WITH_CORRECTION} or {@link
    *            org.eclipse.stardust.engine.api.dto.QualityAssuranceResult.ResultState#FAILED
    *            ResultState.FAILED}, the corresponding activity for this activity
    *            instance supplies error codes {@link
    *            org.eclipse.stardust.engine.api.model.IActivity#getQualityAssuranceCodes()
    *            IActivity.getQualityAssuranceCodes()} and no error code was supplied
    *
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyAttributes,
         scope=ExecutionPermission.Scope.activity,
         defaults={ExecutionPermission.Default.ALL})
   void setActivityInstanceAttributes(ActivityInstanceAttributes attributes)
         throws ObjectNotFoundException, InvalidArgumentException;

   /**
    * Logs an audit trail event of type <code>LogCode.EXTERNAL</code>.
    * 
    * @param logType
    *           Set the type of log (info, warn, error etc.). Whereas the
    *           <code>Unknown</code> type is mapped to a warning.
    * @param contextType
    *           Set the context scope of the event
    * @param contextOid
    *           Oid of the runtime object (only used if context type is set to
    *           ProcessInstance or ActivityInstance)
    * @param message
    *           any message that should be logged
    * @param throwable
    *           any exception (or null) that should be appended to the message
    *
    * @exception org.eclipse.stardust.common.error.ObjectNotFoundException
    *               if there is no runtime object with the specified OID
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrailStatistics)
   void writeLogEntry(LogType logType, ContextKind contextType, long contextOid,
         String message, Throwable throwable) throws ObjectNotFoundException;

   /**
    * Executes a {@link org.eclipse.stardust.engine.core.runtime.command.ServiceCommand
    * ServiceCommand} in a single engine transaction.
    *
    * If the service command implements
    * <code>{@link org.eclipse.stardust.engine.core.runtime.command.Configurable Configurable}</code>
    * , the following option may be provided:
    * <ul>
    * <li>"<b>autoFlush</b>" - automatically flushes the audit trail changes after every
    * service call. The value must be a <code>{@link java.lang.Boolean Boolean}</code>
    * object. The default value is
    * <code>{@link java.lang.Boolean#FALSE Boolean.FALSE}</code>.
    * </ul>
    *
    * @param serviceCmd
    *           the {@link org.eclipse.stardust.engine.core.runtime.command.ServiceCommand
    *           ServiceCommand} to be executed.
    * @return the result of the execution. May be <code>null</code> if the command has no
    *         result.
    * @throws org.eclipse.stardust.common.error.ServiceCommandException
    *            that encapsulates any exception thrown during the execution of the
    *            command.
    */
   @ExecutionPermission
   Serializable execute(ServiceCommand serviceCmd) throws ServiceCommandException;

   /**
    * Creates a new business object instance if it does not exist.
    *
    * @param qualifiedBusinessObjectId
    *           the qualified id of the business object.
    * @param initialValue
    *           the initial value of the business instance (can be null).
    * @return the newly created business object instance.
    * @throws org.eclipse.stardust.common.error.ObjectExistsException
    *            if BO already exists.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.modifyDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})
   BusinessObject createBusinessObjectInstance(String qualifiedBusinessObjectId, Object initialValue);

   /**
    * Updates the value of a business object instance.
    *
    * @param qualifiedBusinessObjectId
    *           the qualified id of the business object.
    * @param newValue
    *           the new value of the business instance (can be null).
    * @return the updated business object instance.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.modifyDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})
   BusinessObject updateBusinessObjectInstance(String qualifiedBusinessObjectId, Object newValue);

   /**
    * Deletes a business object instance.
    *
    * @param qualifiedBusinessObjectId
    *           the qualified id of the business object.
    * @param primaryKey
    *           the primary key identifying the instance to be deleted.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.modifyDataValues,
         scope=ExecutionPermission.Scope.data,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})
   void deleteBusinessObjectInstance(String qualifiedBusinessObjectId, Object primaryKey);
}