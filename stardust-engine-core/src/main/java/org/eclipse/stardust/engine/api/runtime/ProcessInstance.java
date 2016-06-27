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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkResult;


/**
 * The <code>ProcessInstance</code> represents a snapshot of the execution state of an
 * process instance.
 * <p>The corresponding runtime object is stored in the <code>process_instance</code>
 * table of the audit trail database.</p>

 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ProcessInstance extends RuntimeObject, IDescriptorProvider
{
   /**
    * This will be returned if the OID is not known.
    */
   static final long UNKNOWN_OID = -1;

   /**
    * Gets ID of this process instance's definition. Same as {@link #getModelElementID()}.
    *
    * @return The process ID.
    */
   String getProcessID();

   /**
    * Gets name of this process instance's definition.
    *
    * @return The process name.
    *
    * @see #getProcessName()
    */
   String getProcessName();

   /**
    * Gets the OID of the ultimate root process instance. Same as {@link #getOID()} for
    * top-level processes.
    *
    * @return The OID of the ultimate root process instance of this process instance.
    */
   long getRootProcessInstanceOID();

   /**
    * Gets the name of the ultimate root process instance. Same as {@link #getProcessName()} for
    * top-level processes.
    *
    * @return The name of the ultimate root process instance of this process instance.
    */
   String getRootProcessInstanceName();
   
   
   /**
    * Gets the OID of the scope process instance the data values of this
    * process instance are bound to.
    *
    * @return The OID of the data scope process instance of this process instance. Might be ProcessInstance.UNKNOWN_OID.
    */
   long getScopeProcessInstanceOID();

   /**
    * Gets the the scope process instance the data values of this
    * process instance are bound to.
    *
    * @return The scope process instance of this process instance. Might be NULL.
    */
   ProcessInstance getScopeProcessInstance();

   /**
    * Gets the priority of the process instance.
    *
    * @return The priority of the process instance or -1 if no priority set.
    */
   int getPriority();

   /**
    * Gets the time when this process instance was created.
    *
    * @return the creation time.
    */
   Date getStartTime();

   /**
    * Gets the time when this process instance was terminated.
    *
    * @return the time when the process was completed or aborted, or null if the process
    *         is still active.
    */
   Date getTerminationTime();

   /**
    * Gets the <code>User</code> object of the user that have started the process instance.
    *
    * @return the <code>User</code> object of the user.
    */
   User getStartingUser();

   /**
    * Gets the current state of the process instance.
    *
    * @return the state of the process instance.
    */
   ProcessInstanceState getState();

   /**
    * Retrieves the level of details for process instance.
    *
    * @return the process instance details level.
    */
   ProcessInstanceDetailsLevel getDetailsLevel();

   /**
    * Retrieves the options used for details creation for process instance.
    *
    * @return the process instance details level.
    */
   EnumSet<ProcessInstanceDetailsOptions> getDetailsOptions();

   /**
    * Retrieves extended attributes. Can return null when details level  is not appropriate.
    *
    * @return Attributes of the process instance
    */
   ProcessInstanceAttributes getAttributes();

   /**
    * @return custom runtime attributes not necessarily reflected in the database
    */
   Map<String, Object> getRuntimeAttributes();

   /**
    * Gets a list of requested additional data like notes, delegations, state changes and exceptions.
    * This list is sorted in ascending order (oldest first).
    * <br>
    * The list will be populated depending on {@link HistoricalEventPolicy} applied to
    * {@link ProcessInstanceQuery}. By default this list will be empty as
    * retrieval might degrade query performance.
    *
    * @return list of all historical events
    * @see org.eclipse.stardust.engine.api.runtime.HistoricalEvent
    * @see org.eclipse.stardust.engine.api.query.HistoricalEventPolicy
    */
   List<HistoricalEvent> getHistoricalEvents();

   /**
    * Returns the permission state of the given permission id for the current user.
    *
    * @param permissionId
    * @return Granted if the the permission was granted to the user, Denied if the permission
    *    was denied to the user or Unknown if the permission is invalid for this process instance.
    */
   PermissionState getPermission(String permissionId);

   /**
    * Gets the oid of the parent process instance. This will only be fetched if option
    * {@link ProcessInstanceDetailsOptions#WITH_HIERARCHY_INFO} has been set at
    * {@link ProcessInstanceDetailsPolicy}.
    *
    * @return oid of parent process instance, might be 0 if this process instance has
    * not been started as synchronous subprocess.<br>{@link #UNKNOWN_OID} will be
    * returned if an error occurred or details option has not been set.
    */
   long getParentProcessInstanceOid();

   /**
    * Returns process instance links which have this process instance as source or target.
    * This will only be fetched if option
    * {@link ProcessInstanceDetailsOptions#WITH_LINK_INFO} has been set at
    * {@link ProcessInstanceDetailsPolicy}.
    *
    * @return the process instances links. Returns an empty list if the details policy was
    *         not set on the query or no links exist.
    */
   List<ProcessInstanceLink> getLinkedProcessInstances();

   /**
    * Allows to check if the process instance is a case process instance which is used to
    * group other process instances.
    *
    * @return true - if the process instance is a case process instance.<br>
    *         false - if the process instance is not a case process instance.
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#createCase(String, String, long[])
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinCase(long, long[])
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#leaveCase(long, long[])
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#mergeCases(long, long[], String)
    */
   boolean isCaseProcessInstance();
   
   /**
    * 
    * @return the Benchmark Result
    */
   BenchmarkResult getBenchmarkResult();
   
   /**
    * 
    * @return OID of the Benchmark
    */
   long getBenchmark();
}
