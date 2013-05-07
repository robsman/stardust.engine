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
package org.eclipse.stardust.engine.api.dto;

import java.io.Serializable;
import java.util.Date;

import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;


/**
 * The <code>HistoricalState</code> represents a snapshot of the historic states of an 
 * activity instance.
 * <p>The corresponding runtime object is stored in the <code>act_inst_history</code>
 * table of the audit trail database.</p>
 *
 * @author born
 * @version $Revision$
 */
public interface HistoricalState extends Serializable
{
   /**
    * Gets the id of the process definition containing the workflow activity associated
    * with this activity instance historic state.
    *
    * @see org.eclipse.stardust.engine.api.model.ProcessDefinition
    */
   String getProcessDefinitionId();

   /**
    * Gets the OID of the process instance containing this activity instance historic state.
    *
    * @return the OID of the parent process instance
    */
   long getProcessInstanceOID();
   
   /**
    * Gets the ID workflow activity corresponding to this activity instance historic state.
    *
    * @return the ID of the workflow activity.
    */
   String getActivityId();

   /**
    * Gets the OID of the workflow activity corresponding to this activity instance historic state.
    *
    * @return the OID of the parent process instance
    */
   long getActivityInstanceOID();
   
   /**
    * Gets the historic state for the corresponding activity instance.
    *
    * @return the state of the corresponding activity instance.
    */
   ActivityInstanceState getState();
   
   /**
    * Gets the start time for the period the state was set.
    *
    * @return the start time of the period
    *
    * @see #getState
    */
   Date getFrom();

   /**
    * Gets the end time for the period the state was set.
    *
    * @return the end time of the period 
    *
    * @see #getState
    */
   Date getUntil();
   
   /**
    * Gets the participant on whose worklist the activity instance was in the period.
    * 
    * @return the participant of the worklist.
    * @deprecated Superseded by {@link #getParticipant()}  
    */
   @Deprecated
   Participant getPerfomer();
   
   /**
    * Gets the participant on whose worklist the activity instance was in the period.
    * 
    * @return the participant of the worklist.
    */
   ParticipantInfo getParticipant();
   
   /**
    * Gets the participant on whose worklist the activity instance has been when it was
    * activated.
    * 
    * @return the participant of the worklist before the activity instance was activated.
    * @deprecated Superseded by {@link #getOnBehalfOfParticipant()}  
    */
   @Deprecated
   Participant getOnBehalfOf();
   
   /**
    * Gets the participant on whose worklist the activity instance has been when it was
    * activated.
    * 
    * @return the participant of the worklist before the activity instance was activated.
    */
   ParticipantInfo getOnBehalfOfParticipant();
   
   /**
    * Gets the user on behalf of which the activity state changed.
    * 
    * @return the user on behalf of which the activity state changed.
    */
   UserInfo getOnBehalfOfUser();            
   
   /**
    * Gets the workflow user who changed the state or worklist of the activity instance.
    * 
    * @return the workflow user who changed the state.
    */
   User getUser();
}
