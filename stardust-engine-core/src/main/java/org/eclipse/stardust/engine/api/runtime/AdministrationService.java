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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.ProfileScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;



/**
 * Provides administration services for the CARNOT runtime environment.
 * <p>The functionality includes the following tasks:</p>
 * <ul>
 * <li>manage the workflow models (deploy, modify or delete)</li>
 * <li>recover the runtime environment or single workflow objects</li>
 * <li>terminate running process instances</li>
 * <li>manage the life cycle management of CARNOT daemons</li>
 * </ul>
 * <p>An administration service always operates against an audit trail database.</p>
 * <p>The administration service requires that the user performing tasks has been
 * assigned to the predefined role <tt>Administrator</tt>.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface AdministrationService extends Service
{
   /**
    * The predefined event daemon type.
    */
   String EVENT_DAEMON = "event.daemon";

   /**
    * The predefined notification daemon type.
    */
   String SYSTEM_DAEMON = "system.daemon";

   /**
    * The predefined criticality daemon type.
    */
   String CRITICALITY_DAEMON = "criticality.daemon";
   
   /**
    * The predefined benchmark daemon type-
    */
   String BENCHMARK_DAEMON = "benchmark.daemon";

   /**
    * Set password rule.
    *
    * @param The rules or null.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrail)
   void setPasswordRules(PasswordRules rules);

   /**
    * Returns the password rules.
    *
    * @return The password rules or null.
    */
   // No permission check
   PasswordRules getPasswordRules();

   /**
    * Deploys a new model.
    *
    * @param model            the XML representation of the model to deploy.
    * @param predecessorOID   the predecessor of the model in the priority list. A value
    *                         of <code>0</code> indicates an absent predecessor.
    *
    * @return deployment information, including possible errors or warnings.
    * @throws DeploymentException
    * @deprecated since 6.0, predecessorOID is ignored.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
   DeploymentInfo deployModel(String model, int predecessorOID)
         throws DeploymentException;

   /**
    * Overwrites the specified model.
    *
    * @param model      the XML representation of the model to deploy.
    * @param modelOID   the model to be overwritten.
    *
    * @return deployment information, including possible errors or warnings.
    * @throws DeploymentException
    * @deprecated since 6.0
    */
   @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
   DeploymentInfo overwriteModel(String model, int modelOID)
         throws DeploymentException;

   /**
    * Deploys a new model.
    *
    * @param model            the XML representation of the model to deploy.
    * @param configuration    reserved for internal use (can be null).
    * @param predecessorOID   the predecessor of the model in the priority list. A value
    *                         of <code>0</code> indicates an absent predecessor.
    * @param validFrom        validity start time for the model or null if unlimited.
    * @param validTo          validity end time for the model or null if unlimited.
    * @param comment          deployment comment.
    * @param disabled         specifies if the model should disabled after deployment.
    * @param ignoreWarnings   specifies that the deployment should continue if only warnings were issued.
    *
    * @return deployment information, including possible errors or warnings.
    * @deprecated since 6.0, configuration, validTo and disabled are ignored.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
   DeploymentInfo deployModel(String model,
         String configuration, int predecessorOID, Date validFrom, Date validTo,
         String comment, boolean disabled, boolean ignoreWarnings)
         throws DeploymentException;

   /**
    * Overwrites the specified model.
    *
    * @param model            the XML representation of the model to deploy.
    * @param configuration    reserved for internal use (can be null).
    * @param modelOID         the model to be overwritten.
    * @param validFrom        validity start time for the model or null if unlimited.
    * @param validTo          validity end time for the model or null if unlimited.
    * @param comment          deployment comment.
    * @param disabled         specifies if the model should disabled after deployment.
    * @param ignoreWarnings   specifies that the deployment should continue if only warnings were issued.
    *
    * @return deployment information, including possible errors or warnings.
    * @throws DeploymentException
    * @deprecated since 6.0, configuration, validFrom, validTo and disabled are ignored.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
   DeploymentInfo overwriteModel(String model,
         String configuration, int modelOID, Date validFrom, Date validTo,
         String comment, boolean disabled, boolean ignoreWarnings)
         throws DeploymentException;

   /**
    * Overwrites the specified model.
    *
    * @param deploymentElement   The model to be overwritten.
    * @param modelOID         The modelOID of the model to be overwritten.
    * @param options          The deployment options. Can be null, in which case default deployment options will be used.
    * @return depoymentInfo   Deployment information information, including possible errors or warning
    * @throws DeploymentException   Exception if the overwrite operation could not be performed.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
   DeploymentInfo overwriteModel(DeploymentElement deploymentElement, int modelOID, DeploymentOptions options) throws DeploymentException;

   /**
    * Deploys a group of models.
    *
    * The deployment operation is transactional, that means either all models in the group are deployed or none of them.
    * Model references will be resolved first within the group, and only if there is no corresponding model in the group
    * the already deployed models will be considered.
    * Note: It is possible to deploy an empty set of models. This will not necessarily mean that audit trail is not being changed.
    * If the PredefinedModel is not already present in audit trail this means it will be deployed in any case.
    *
    * @param deploymentElements The models to be deployed. Each model in the set must have a unique ID.
    * @param options            The deployment options. Can be null, in which case default deployment options will be used.
    * @return Deployment information, including possible errors or warnings, one DeploymentInfo per DeploymentElement.
    * @throws DeploymentException if the deployment operation could not be performed.
    * @throws InvalidArgumentException if the deploymentElements argument is null.
    * @throws ConcurrencyException if the multiple transactions trying to deploy models at the same time.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
   List<DeploymentInfo> deployModel(List<DeploymentElement> deploymentElements, DeploymentOptions options)
         throws DeploymentException, ConcurrencyException;

   /**
    * Specifies which implementation alternative (identified by <i>implementationModelId</i>) will be considered
    * the primary implementation of the process interface declared by a specific process definition
    * (identified by <i>interfaceModelOid</i> and <i>processId</i>).
    *
    * <p>Precondition:
    * <ul><li>There needs to be at least one model having the ID <i>implementingModelId</i> that contains a process
    * definition which implements the specified process interface.</li></ul></p>
    *
    * <p>If <i>implementationModelId</i> is <code>null</code> the default implementation will be reset
    * to the process definition declaring the process interface (the default implementation).</p>
    *
    * @param interfaceModelOid The OID of the model defining the process interface.
    * @param processId The ID of the process definition declaring the process interface.
    * @param implementationModelId The ID of the model providing the implementation.
    * @param options The linking comments.
    * @return Deployment information, including possible errors or warnings.
    * @throws DeploymentException if the linking operation could not be performed.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
   DeploymentInfo setPrimaryImplementation(long interfaceModelOid, String processId, String implementationModelId,
         LinkingOptions options) throws DeploymentException;

   /**
    * Deletes the specified model.
    *
    * @param modelOID the model to be deleted.
    *
    * @return deployment information, including possible errors or warnings.
    * @throws DeploymentException
    */
   @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
   DeploymentInfo deleteModel(long modelOID)
      throws DeploymentException;

   /**
    * Deletes process instances from the audit trail.
    * <p />
    * Only terminated root process instance can be deleted. All subprocess instances
    * started by one of the root process instances to be deleted will be deleted
    * transitively, too.
    *
    * @param piOids A list with OIDs of the root process instance to be deleted.
    *
    * @throws IllegalOperationException Raised if non-root or non-terminated process
    *    instances are to be deleted.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrail,
         changeable=false)
   void deleteProcesses(List<Long> piOids) throws IllegalOperationException;

   /**
    * Removes all records from the runtime environment making up the audit trail
    * database. The tables will still remain in the database.
    *
    * @param keepUsers a flag to specify if the users should be deleted or not.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrail)
   void cleanupRuntime(boolean keepUsers);

   /**
    * Removes all records from the runtime environment making up the audit trail
    * database. Additionally empties the model table. The tables will still remain in the
    * database.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrail)
   void cleanupRuntimeAndModels();

   /**
    * Changes the process instance priority.
    * Equivalent with setProcessInstancePriority(oid, priority, false).
    *
    * @param oid the OID of the process instance the priority should be changed of.
    * @param priority the new priority of the process instance.
    *
    * @return the process instance that was changed.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified oid.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyProcessInstances,
         scope=ExecutionPermission.Scope.processDefinition)
   ProcessInstance setProcessInstancePriority(long oid, int priority)
      throws ObjectNotFoundException;

   /**
    * Changes the process instance priority.
    *
    * @param oid the OID of the process instance the priority should be changed of.
    * @param priority the new priority of the process instance.
    * @param propagateToSubProcesses if true, the priority will be propagated to all subprocesses.
    *
    * @return the process instance that was changed.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified oid.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyProcessInstances,
         scope=ExecutionPermission.Scope.processDefinition)
   ProcessInstance setProcessInstancePriority(long oid, int priority, boolean propagateToSubProcesses)
      throws ObjectNotFoundException;

   /**
    * Aborts a process instance disregarding any activities which were or are
    * performed by the process instance.
    * Regularly the process instance to be aborted will be set to the state ABORTING synchronously.
    * The returned ProcessInstance object will already be in that state.
    * Before returning the ProcessInstance object, a asynchronous abortion task is scheduled for it.
    * If the process instance is not yet inserted in the database but needs to be aborted for some reason ( e.g. by abort process event)
    * then the abort operation is optimized to happen completely synchronously.
    * In that case the returned ProcessInstance will already be in state ABORTED.
    *
    * <em>This method also aborts all super process instances.</em>
    *
    * <p>State changes:
    * <ul><li>Process state before: active, interrupted</li>
    * <li>State after: The state of root process, all sub-processes and activities that are not yet completed changes to aborted.</li></ul>
    * </p>
    *
    * @param oid the OID of the process instance to be aborted.
    *
    * @return the process instance that was aborted.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified oid.
    * @throws IllegalOperationException if the oid references a case process instance.
    *
    * @see WorkflowService#abortActivityInstance(long)
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.abortProcessInstances,
         scope=ExecutionPermission.Scope.processDefinition)
   ProcessInstance abortProcessInstance(long oid)
      throws ObjectNotFoundException, IllegalOperationException;

   /**
    * Recovers the process instance identified by the given OID and all of its subprocess
    * instances, executed in a separate transaction. By default the execution is synchronous.
    * An asynchronous successor attempt to recover the process instance is scheduled only
    * if there is a non fatal error (e.g. locking conflicts) during the first attempt.
    * <p>This includes crashes during non-interactive application execution and crashes
    * of the CARNOT runtime engine itself.</p>
    *
    * @param oid the OID of the process instance to be recovered.
    *
    * @return the process instance that was recovered.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified oid.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.runRecovery)
   ProcessInstance recoverProcessInstance(long oid)
         throws ObjectNotFoundException;

   /**
    * Recovers the process instances identified by the given list of OIDs and all
    * associated subprocess instances.Executed in a separate transaction.
    * By default the execution is synchronous. Asynchronous successor attempts to recover the affected
    * process instances are only scheduled if there are non fatal errors (e.g. locking conflicts)
    * during the first attempts.
    * <p>This includes crashes during non-interactive application execution and crashes
    * of the CARNOT runtime engine itself.</p>
    *
    * @param oids the list of OID of the process instance to be recovered.
    *
    * @throws ObjectNotFoundException if there is no process instance for one of the specified oids.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.runRecovery)
   void recoverProcessInstances(List<Long> oids)
         throws ObjectNotFoundException;

   /**
    * Retrieves the specified daemon.
    * The following daemon types exist:
    * <ul>
    * <li><code>event.daemon</code> for the event daemon</li>
    * <li><code>mail.trigger</code> for the mail trigger daemon</li>
    * <li><code>timer.trigger</code> for the timer trigger daemon</li>
    * <li><code>system.daemon</code> for the notification daemon.</li>
    * </ul>
    *
    * @param daemonType the type of the daemon.
    * @param acknowledge whether to acknowledge the daemon information
    * @return daemon information.
    *
    * @throws ObjectNotFoundException if there is no daemon with the specified type.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.manageDaemons)
   Daemon getDaemon(String daemonType, boolean acknowledge)
         throws ObjectNotFoundException;

   /**
    * Stops the specified daemon. See {@link #getDaemon(String, boolean)} for a list
    * of daemon types.The stop daemon operation is inherently asynchronous, regardless of the acknowledge flag.
    * The stop operation only marks the daemon for stopping. The daemon will stop asynchronously at the first opportunity.
    * If the acknowledge parameter is set to true then the method will wait for a predetermined (configurable)
    * amount of time for the asynchronous daemon to confirm the execution of the operation.
    * If acknowledge is false then the method will return immediately,
    * without status updates to the returned object.
    * Otherwise it will wait for the executor thread to confirm the action.
    * The acknowledge flag allows to wait for the executor to confirm the actual stop of the daemon.
    * The status object is not influenced by this flag.
    * @param daemonType the type of the daemon to be stopped.
    * @param acknowledge whether to acknowledge the stop operation.
    * @return daemon information.
    *
    * @throws ObjectNotFoundException if there is no daemon with the specified type.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.manageDaemons)
   Daemon stopDaemon(String daemonType, boolean acknowledge)
         throws ObjectNotFoundException;

   /**
    * Starts the specified daemon. See {@link #getDaemon(String, boolean)} for a list
    * of daemon types.
    * The start daemon operation is inherently asynchronous, regardless of the acknowledge flag.
    * The start operation schedules a timer for the daemon execution which will be performed asynchronously.
    * If the acknowledge parameter is set to true then the method will wait for a predetermined (configurable)
    * amount of time for the asynchronous daemon to confirm the execution of the operation.
    * If acknowledge is false then the method will return immediately, without status updates to the returned object.
    * Otherwise it will wait for the executor thread to confirm the action.
    * The acknowledge flag allows to wait for the executor to confirm the actual start of the daemon.
    * The status object is not influenced by this flag.
    * @param daemonType the type of the daemon to be started.
    * @param acknowledge whether to acknowledge the start operation
    * @return daemon information.
    *
    * @throws ObjectNotFoundException if there is no daemon with the specified type.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.manageDaemons)
   Daemon startDaemon(String daemonType, boolean acknowledge)
         throws ObjectNotFoundException;

   /**
    * Retrieves a list of all the available daemons.
    *
    * @param acknowledge whether to acknowledge the daemon information
    * @return a list containing Daemon objects.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.manageDaemons)
   List<Daemon> getAllDaemons(boolean acknowledge) throws AccessForbiddenException;

   /**
    * Determines key indicators of audit trail health.
    *
    * @return A status report indicating some important indicators of audit trail health.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.readAuditTrailStatistics)
   public AuditTrailHealthReport getAuditTrailHealthReport();

   /**
    * Determines key indicators of audit trail health.
    *
    * @param countOnly
    *           Determines if report should include the process instances oids or just the
    *           total count of oids. If countOnly is set to true the total count of
    *           process instances will be included in report. If countOnly is set to false
    *           a list containing the process instances oids will be included in report.
    * @return A status report indicating some important indicators of audit trail health.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.readAuditTrailStatistics)
   public AuditTrailHealthReport getAuditTrailHealthReport(boolean countOnly);

   /**
    * Recovers the complete CARNOT runtime environment.Executed in a separate transaction.
    * By default the execution is synchronous. Only if there are non fatal errors
    * (e.g. locking conflicts), for the affected process instances, a successor asynchronous attempt
    * to recover is scheduled.
    * <p>This reanimates dead activity threads from an application server crash.
    * Additionally previously interrupted processes are reanimated.</p>
    * <p>It is equivalent with recovering all the process instances which are in the
    * states active or interrupted.</p>
    */
   @ExecutionPermission(id=ExecutionPermission.Id.runRecovery)
   void recoverRuntimeEnvironment();

   /**
    * Starts a process from a specified model.The startProcess method is executed asynchronously
    * if the synchronously parameter is set to false.However, even if the synchronously parameter is true,
    * the execution of activities is performed in the calling thread only up to the first transition marked
    * with "Fork on Traversal", from that point on execution is asynchronous.
    *
    * <p>State changes:
    * <ul>
    * <li>Process state after: active</li>
    * </ul>
    * </p>
    *
    * @param modelOID      the model where the process is defined.
    * @param id            the ID of the process to start.
    * @param data          contains data IDs as keyset and corresponding data values to
    *                      be set as values.
    * @param synchronously determines whether the process will be started synchronously
    *                      or asynchronously.
    *
    * @return the {@link ProcessInstance} that was started.
    *
    * @throws ObjectNotFoundException if there is no process with the specified ID in the
    *         specified model or if the model does not exist.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrail,
         changeable=false)
   ProcessInstance startProcess(long modelOID, String id, Map<String, ?> data, boolean synchronously)
      throws ObjectNotFoundException;

   /**
    * Forces the completion of a non-interactive activity instance. A map of access points maybe provided.
    * This way this method can mimic precisely the behavior of a normal completion of the activity.
    *
    * <p>State changes:
    * <ul><li>Activity state before: application, suspended, hibernated</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: completed</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID - the OID of the non-interactive activity to be completed.
    * @param accessPoints - an optional map with access points to perform data mappings,
    *                       can be null
    *
    * @return the completed {@link ActivityInstance}.
    *
    * @throws ObjectNotFoundException if there is no activity with the specified OID.
    * @throws ConcurrencyException if the activity instance is exclusively locked by another thread.
    * @throws IllegalStateChangeException if the activity is already completed or aborted.
    * @throws InvalidValueException if one of the <code>outData</object> values to
    *         be written is invalid, most probably as of a type conflict in case of
    *         statically typed data.
    * @throws AccessForbiddenException if the current user is not an administrator.
    * @throws IllegalOperationException if the activity instance is interactive.
    *
    * @see #forceSuspendToDefaultPerformer(long)
    */
   @ExecutionPermission(id=ExecutionPermission.Id.performActivity,
         changeable=false)
   ActivityInstance forceCompletion(long activityInstanceOID, Map<String, ?> accessPoints)
      throws ConcurrencyException, ObjectNotFoundException, IllegalStateChangeException,
             InvalidValueException, AccessForbiddenException, IllegalOperationException;

   /**
    * Forces an activity instance to be suspended. It will be added to the worklist of
    * the default performer declared for the corresponding activity, and the specified
    * activity instance will be set to SUSPENDED state.
    *
    * <p>State changes:
    * <ul><li>Activity state before: application, suspended, hibernated</li>
    * <li>Process state before: active, interrupted</li>
    * <li>Activity state after: suspended</li>
    * <li>Process state after: State does not change.</li>
    * </ul>
    * </p>
    *
    * @param activityInstanceOID the OID of the activity to be suspended.
    *
    * @return the {@link ActivityInstance} that was suspended.
    *
    * @throws ObjectNotFoundException if there is no activity instance with the specified OID.
    * @throws ConcurrencyException if the activity instance is exclusively locked by another thread.
    * @throws IllegalStateChangeException if the activity is already completed or aborted.
    * @throws AccessForbiddenException if the current user does not have the required privilege.
    *
    * @see #forceCompletion(long, Map)
    */
   @ExecutionPermission(id=ExecutionPermission.Id.forceSuspend)
   ActivityInstance forceSuspendToDefaultPerformer(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException,
         IllegalStateChangeException, AccessForbiddenException;

   /**
    * Retrieves information on the current user.
    *
    * @return the current user.
    */
   // No permission check
   User getUser();

   /**
    * Flushes all internal caches, effectively returning the engine to a state just like
    * after it has started.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.controlProcessEngine)
   void flushCaches();

   /**
    * Retrieves all permissions the current user has on this service plus the global permissions.
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
    * Retrieves the profile for the specified scope.
    *
    * @param scope
    * @return the profile.
    */
   // TODO: deprecate
   Map<String, ?> getProfile(ProfileScope scope);

   /**
    * Sets the profile for the specified scope.
    *
    * @param scope the scope.
    * @param profile the profile.
    */
   // TODO: deprecate
   void setProfile(ProfileScope scope, Map<String, ?> profile);

   /**
    * Logs an audit trail event of type <code>LogCode.ADMINISTRATION</code>.
    * @param logType Set the type of log (info, warn, error etc.). Whereas the <code>Unknown</code> type is mapped to a warning.
    * @param contextType Set the context scope of the event
    * @param contextOid Oid of the runtime object (only used if context type is set to ProcessInstance or ActivityInstance)
    * @param message any message that should be logged
    * @param throwable any exception (or null) that should be appended to the message
    *
    * @exception ObjectNotFoundException if there is no runtime object with the specified OID
    */
   // TODO: discuss, introduce permission
   void writeLogEntry(LogType logType, ContextKind contextType, long contextOid,
         String message, Throwable throwable) throws ObjectNotFoundException;

   /**
    * Creates a new department.
    *
    * @param id the id of the department. Must not be null or empty and it must be unique in the parent scope.
    * @param name the name of the department. Must not be null or empty.
    * @param description the description of the department.
    * @param parent the parent scope. Can be null if the department will be a top level department.
    * @param organization the organization to which this department is assigned. Must not be null.
    * @return the created department.
    * @throws DepartmentExistsException
    *       if a department with the same id already exists in the parent scope.
    * @throws ObjectNotFoundException
    *       if either the parent or the organization could not be resolved.
    * @throws InvalidArgumentException <br>
    *       - if either the id or the name is null or an empty string or<br>
    *       - if the organization is null or<br>
    *       - if the organization does not resolve to an actual organization in the model
    *       (i.e. resolves to a role or conditional performer) or<br>
    *       - if the organization is not directly part of the organization to which
    *         the parent department is assigned (invalid hierarchy).
    * @throws IllegalOperationException - if the user was external authentified
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyDepartments)
   Department createDepartment(String id, String name, String description, DepartmentInfo parent, OrganizationInfo organization)
         throws DepartmentExistsException, ObjectNotFoundException, InvalidArgumentException, IllegalOperationException;

   /**
    * Retrieves the department with the given oid.
    *
    * @param oid the unique identifier of the department.
    * @return the modified department.
    * @throws ObjectNotFoundException
    *       if there is no department with the specified oid.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.readDepartments,
         defaults={ExecutionPermission.Default.ALL})
   Department getDepartment(long oid) throws ObjectNotFoundException;

   /**
    * Change the description of a department.
    *
    * @param oid the unique identifier of the department.
    * @param name the new name of the department.
    * @param description the new description.
    * @return the modified department.
    * @throws ObjectNotFoundException
    *       if there is no department with the specified oid.
    * @throws InvalidArgumentException
    *       if the name is null or an empty string
    * @throws IllegalOperationException - if the user was external authentified
    */
   @ExecutionPermission(id=ExecutionPermission.Id.modifyDepartments)
   Department modifyDepartment(long oid, String name, String description) throws ObjectNotFoundException, InvalidArgumentException, IllegalOperationException;

   /**
    * Removes the department having the specified oid, all his children and all user grants associated with the department.
    *
    * @param oid the unique identifier of the department.
    * @throws ObjectNotFoundException
    *       if there is no department with the specified oid.
    * @throws InvalidArgumentException
    *       if there are work items currently associated with the department or any child of the department.
    * @throws IllegalOperationException - if the user was external authentified
    */
   @ExecutionPermission(id = ExecutionPermission.Id.modifyDepartments)
   void removeDepartment(long oid) throws ObjectNotFoundException,
         InvalidArgumentException, IllegalOperationException;

   /**
    * Retrieves preferences from the given scope.
    *
    * @param scope the scope from which the preferences are to be retrieved from
    * @param moduleId the moduleId of the preferences
    * @param preferencesId the id of the preferences
    * @return a preferences object.
    *
    * @throws PublicException if <tt>scope</tt> is null.
    */
   Preferences getPreferences(PreferenceScope scope, String moduleId, String preferencesId);

   /**
    * Saves the changed preferences to the preference store.
    *
    * @param preferences an preferences object to be saved.
    *
    * @throws AccessForbiddenException if the current user does not have the required privilege.
    * @throws InvalidArgumentException if <tt>preferences</tt> is null.
    * @throws InvalidArgumentException if preferences property <tt>preferences</tt> is null.
    * @throws InvalidArgumentException if preferences property <tt>moduleId</tt> is null or empty.
    * @throws InvalidArgumentException if preferences property <tt>preferencesId</tt> is null or empty.
    */
   @ExecutionPermission(id = ExecutionPermission.Id.saveOwnUserScopePreferences,
         defaults = {ExecutionPermission.Default.ALL})
   void savePreferences(Preferences preferences) throws AccessForbiddenException;

   /**
    * Saves a complete list of preferences to the preference store.
    *
    * @param preferences a list of preferences to be saved.
    *
    * @throws AccessForbiddenException if the current user does not have the required privilege.
    * @throws InvalidArgumentException if <tt>preferences</tt> is null.
    * @throws InvalidArgumentException if preferences property <tt>moduleId</tt> is null or empty.
    * @throws InvalidArgumentException if preferences property <tt>preferencesId</tt> is null or empty.
    */
   @ExecutionPermission(id = ExecutionPermission.Id.saveOwnUserScopePreferences,
         defaults = {ExecutionPermission.Default.ALL})
   void savePreferences(List<Preferences> preferences) throws AccessForbiddenException;


   /**
    * Retrieves merged configuration variables from all models matching the specified modelId (without Password type).
    * The contained descriptions and default values are taken from the newest model version the configuration variable exists in.
    *
    * @param modelId The modelId of the model(s) to retrieve the configuration variables from.
    * @return A ConfigurationVariables object containing the merged configuration variables from all model versions.
    *
    * @throws InvalidArgumentException if <tt>modelId</tt> is null or empty.
    */
   ConfigurationVariables getConfigurationVariables(String modelId);

   /**
    * Retrieves merged configuration variables from all models matching the specified modelId.
    * The contained descriptions and default values are taken from the newest model version the configuration variable exists in.
    *
    * @param modelId The modelId of the model(s) to retrieve the configuration variables from.
    * @param all Indicates if to fetch all configuration variables, including Password type.
    * @return A ConfigurationVariables object containing the merged configuration variables from all model versions.
    *
    * @throws InvalidArgumentException if <tt>modelId</tt> is null or empty.
    */
   ConfigurationVariables getConfigurationVariables(String modelId, boolean all);

   /**
    * Retrieves merged configuration variables from all models matching the specified modelIds (without Password type).
    * The contained descriptions and default values are taken from the newest model version the configuration variable exists in.
    *
    * @param modelIds The modelId of the model(s) to retrieve the configuration variables from.
    * @return A List of ConfigurationVariables objects containing the merged configuration variables from all model versions.
    *
    * @throws InvalidArgumentException if <tt>modelIds</tt> is null or contains an modelId which is null or empty.
    */
   List<ConfigurationVariables> getConfigurationVariables(List<String> modelIds);

   /**
    * Retrieves configuration variables from the given model (without Password type).
    *
    * @param model The model xml representation in byte array form.
    * @return A ConfigurationVariables object containing only the configuration variables from the given model.
    *
    * @throws InvalidArgumentException if <tt>model</tt> is null.
    */
   ConfigurationVariables getConfigurationVariables(byte[] model);

   /**
    * Saves changes to configuration variables values.
    *
    * @param configurationVariables The configuration variables containing changed values.
    * @param force Option to ignore validation warnings.
    *
    * @return model reconfiguration information, including possible errors or warnings.
    *
    * @throws AccessForbiddenException if the current user does not have the required privilege.
    * @throws InvalidArgumentException if <tt>configurationVariables</tt> is null.
    */
   @ExecutionPermission(id = ExecutionPermission.Id.saveOwnPartitionScopePreferences)
   List<ModelReconfigurationInfo> saveConfigurationVariables(ConfigurationVariables configurationVariables, boolean force) throws AccessForbiddenException;

   /**
    * Retrieves permissions that are globally set. For example permissions concerning
    * model deployment, preference saving, modifying AuditTrail, managing deamons ect.
    *
    * @return the currently set Permissions
    */
   public RuntimePermissions getGlobalPermissions();

   /**
    * Saves the changed Permissions.
    * Use <code>getGlobalPermissions</code> to retrieve currently valid global permissions first.
    * Permissions with null or empty lists set as grants will be reset to their internal default.
    * Changed grants are validated against the currently active model for existing model participants.
    *
    * @param permissions the modified permissions
    *
    * @throws AccessForbiddenException if the current user does not have the required privilege.
    * @throws InvalidArgumentException if <tt>permissions</tt> is null.
    * @throws ValidationException if changed grants are not valid in the active model.
    */
   @ExecutionPermission(id = ExecutionPermission.Id.saveOwnPartitionScopePreferences)
   public void setGlobalPermissions(RuntimePermissions permissions) throws AccessForbiddenException;

   @ExecutionPermission(id = ExecutionPermission.Id.deployRuntimeArtifact)
   public DeployedRuntimeArtifact deployRuntimeArtifact(RuntimeArtifact runtimeArtifact);

   @ExecutionPermission(id = ExecutionPermission.Id.deployRuntimeArtifact)
   public DeployedRuntimeArtifact overwriteRuntimeArtifact(long oid, RuntimeArtifact runtimeArtifact);

   @ExecutionPermission(id = ExecutionPermission.Id.deployRuntimeArtifact)
   public void deleteRuntimeArtifact(long oid);

   @ExecutionPermission(id = ExecutionPermission.Id.readRuntimeArtifact)
   public RuntimeArtifact getRuntimeArtifact(long oid);

   @ExecutionPermission(id = ExecutionPermission.Id.readRuntimeArtifact)
   public List<ArtifactType> getSupportedRuntimeArtifactTypes();

}
