/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.HistoricalState;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceInfo;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkResult;


/**
 * The <code>ActivityInstance</code> represents a snapshot of the execution state of an
 * activity instance.
 * <p>The corresponding runtime object is stored in the <code>activity_instance</code>
 * table of the audit trail database.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ActivityInstance extends RuntimeObject, IDescriptorProvider
{
   /**
    * Gets information about the performed quality assurance workflow
    * @return information about the performed quality assurance workflow
    *         or null if this is a not quality assurance enabled instance
    */
   QualityAssuranceInfo getQualityAssuranceInfo();

   /**
    * Gets the state of this activity instance regarding quality assurance
    * @return the {@link QualityAssuranceState} this instance is in
    */
   QualityAssuranceState getQualityAssuranceState();

   /**
    * Get the {@link ActivityInstanceAttributes}
    * @return the {@link ActivityInstanceAttributes} for this activity instance
    */
   ActivityInstanceAttributes getAttributes();

   /**
    * Gets the current state of this activity instance.
    *
    * @return the state of this activity instance.
    */
   ActivityInstanceState getState();

   /**
    * Gets the time of the creation of this activity instance.
    *
    * @return the time when the activity instance has been instantiated.
    */
   Date getStartTime();

   /**
    * Gets the time of the most recent modification of this activity instance.
    * <p>An activity instance is considered modified if it's state has changed.</p>
    *
    * @return The last modification time of this activity instance.
    *
    * @see #getState
    */
   Date getLastModificationTime();

   /**
    * Gets the workflow activity corresponding to this activity instance.
    *
    * @return the workflow activity.
    */
   Activity getActivity();

   /**
    * Gets the id of the process definition containing the workflow activity associated
    * with this activity instance.
    *
    * @see org.eclipse.stardust.engine.api.model.ProcessDefinition
    */
   String getProcessDefinitionId();

   /**
    * Gets the OID of the process instance containing this activity instance.
    *
    * @return the OID of the parent process instance
    */
   long getProcessInstanceOID();

   /**
    * Gets the process instance containing this activity instance. If the user does not
    * have the permission to read the process instance (declarative security) then this
    * method returns <code>null</code>.
    *
    * @return the process instance initialized with <code>ProcessInstanceDetailsLevel.Core</code> of the activity instance
    */
   ProcessInstance getProcessInstance();

   /**
    * Gets the OID of the current user performer of this activity instance.
    *
    * @return the OID of the user or 0 if not assigned to a user.
    *
    * @see #isAssignedToUser
    */
   long getUserPerformerOID();

   /**
    * Gets the name of the current user performer of this activity instance.
    *
    * @return the name of the user or null if not assigned to a user.
    *
    * @see #isAssignedToUser
    */
   String getUserPerformerName();

   /**
    * Gets the current <code>User</code> Object of this activity instance.
    *
    * @return the <code>User</code> object or null if not assigned to a user.
    *
    * @see #isAssignedToUser
    */
   User getUserPerformer();

   /**
    * Gets the ID of the current participant performer of this activity instance.
    *
    * @return the ID of the performer or null if assigned to a user.
    *
    * @see #isAssignedToUser
    */
   String getParticipantPerformerID();

   /**
    * Gets the name of the current participant performer of this activity instance.
    *
    * @return the name of the performer or null if assigned to a user.
    *
    * @see #isAssignedToUser
    */
   String getParticipantPerformerName();

   /**
    * Gets whether the current performer of this activity instance is a user.
    *
    * @return true, if the current performer is a user.
    *
    * @see #isAssignedToModelParticipant()
    * @see #isAssignedToUserGroup()
    */
   boolean isAssignedToUser();

   /**
    * Gets whether the current performer of this activity instance is a model participant.
    *
    * @return true, if the current performer is a model participant.
    *
    * @see #isAssignedToUser()
    * @see #isAssignedToUserGroup()
    */
   boolean isAssignedToModelParticipant();

   /**
    * Gets whether the current performer of this activity instance is a user group.
    *
    * @return true, if the current performer is a user group.
    *
    * @see #isAssignedToUser()
    * @see #isAssignedToModelParticipant()
    */
   boolean isAssignedToUserGroup();

   /**
    * Gets the current Performer of this Activity Instance (or null).
    * Can be a user group, a user or model participant.
    *
    * @return The current performer.
    */
   ParticipantInfo getCurrentPerformer();

   /**
    * Gets the OID of the user who completed this activity instance.
    *
    * @return the OID of the user, or 0 if this activity instance is not yet completed.
    */
   long getPerformedByOID();

   /**
    * Gets the name of the user who completed this activity instance.
    *
    * @return the name of the user, or null if this activity instance is not yet completed.
    */
   String getPerformedByName();

   /**
    * Returns the user who performed this activity instance.
    *
    * @return The user who performed this activity instance or null if the activity instance is not completed.
    */
   UserInfo getPerformedBy();

   /**
    * Returns the user on behalf of this activity instance was performed.
    *
    * @return The user on behalf of this activity instance was performed or null if the
    *         activity instance is not completed or if it was not performed on behalf of
    *         another user.
    */
   UserInfo getPerformedOnBehalfOf();

   /**
    * Gets whether a note for the scope process instance of this activity instance is available.
    *
    * @return true, if note is available for scope process instance.
    */
   boolean isScopeProcessInstanceNoteAvailable();

   /**
    * Gets a list of historical states for the activity instance. This list is sorted in
    * descending order (latest state first).
    *
    * @return the list of historical states.
    */
   List<HistoricalState> getHistoricalStates();

   /**
    * Gets a list of requested additional data like notes, delegations, state changes and exceptions.
    * This list is sorted in ascending order (oldest first).
    * <br>
    * The list will be populated depending on {@link HistoricalEventPolicy} applied to
    * {@link ActivityInstanceQuery} and {@link WorklistQuery}. By default this list will be empty as
    * retrieval might degrade query performance.
    *
    * @return list of all historical events
    * @see org.eclipse.stardust.engine.api.runtime.HistoricalEvent
    * @see org.eclipse.stardust.engine.api.query.HistoricalEventPolicy
    */
   List<HistoricalEvent> getHistoricalEvents();

   /**
    * Gets the {@link PermissionState} for the given permission id for the current user.<br />
    * This information can be used to decide if the user has the permission to perform a certain operation on this
    * activity instance.<br />
    *
    * @param permissionId - the id of the permission, valid values are: <br />
    * <ul>
    *    <li>delegateToUser</li> - if the user is allowed to delegate this activity instance to another user
    *    <li>abortActivityInstance</li> - if the user is allowed to abort this activity instance
    * </ul>
    * @return 
    * {@link PermissionState#Granted} - if the user has the specified permission <br />
    * {@link PermissionState#Denied} - if the user has <strong>not</strong> the specified permission <br />
    * {@link PermissionState#Unknown} if the permission is unknown. <br />
    *  
    */
   PermissionState getPermission(String permissionId);

   /**
    * Returns the current criticality of an activity instance
    *
    * @return the value if the criticality
    */
   double getCriticality();
   
   /**
    * Returns the current benchmark value of the activity instance
    * 
    * @return the result of the benchmark
    */
   BenchmarkResult getBenchmarkResult();
}