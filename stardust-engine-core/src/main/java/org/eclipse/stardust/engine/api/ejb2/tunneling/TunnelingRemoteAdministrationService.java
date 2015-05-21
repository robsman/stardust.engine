/*
 * Generated from  Revision
 */
package org.eclipse.stardust.engine.api.ejb2.tunneling;

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
 * @version $Revision
 */
public interface TunnelingRemoteAdministrationService extends javax.ejb.EJBObject, org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingRemoteService
{

    /**
     * Set password rule.
     *
     * @param The rules or null.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setPasswordRules(
     *     org.eclipse.stardust.engine.api.runtime.PasswordRules rules)
     */
    public void
         setPasswordRules(
         org.eclipse.stardust.engine.api.runtime.PasswordRules rules,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Returns the password rules.
     *
     * @return The password rules or null.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPasswordRules()
     */
    public org.eclipse.stardust.engine.api.runtime.PasswordRules
         getPasswordRules(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Deploys a new model.
     *
     * @param model            the XML representation of the model to deploy.
     * @param predecessorOID   the predecessor of the model in the priority list. A value
     *                             of <code>0</code> indicates an absent predecessor.
     *
     * @return deployment information, including possible errors or warnings.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DeploymentException
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.DeploymentException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     *
     * @deprecated since 6.0, predecessorOID is ignored.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(
     *     java.lang.String model, int predecessorOID)
     */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deployModel(
         java.lang.String model, int predecessorOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Overwrites the specified model.
     *
     * @param model      the XML representation of the model to deploy.
     * @param modelOID   the model to be overwritten.
     *
     * @return deployment information, including possible errors or warnings.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DeploymentException
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.DeploymentException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     *
     * @deprecated since 6.0
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(
     *     java.lang.String model, int modelOID)
     */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         java.lang.String model, int modelOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Deploys a new model.
     *
     * @param model            the XML representation of the model to deploy.
     * @param configuration    reserved for internal use (can be null).
     * @param predecessorOID   the predecessor of the model in the priority list. A value
     *                             of <code>0</code> indicates an absent predecessor.
     * @param validFrom        validity start time for the model or null if unlimited.
     * @param validTo          validity end time for the model or null if unlimited.
     * @param comment          deployment comment.
     * @param disabled         specifies if the model should disabled after deployment.
     * @param ignoreWarnings   specifies that the deployment should continue if only warnings were
     *     issued.
     *
     * @return deployment information, including possible errors or warnings.
     *
     * @deprecated since 6.0, configuration, validTo and disabled are ignored.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(
     *     java.lang.String model, java.lang.String configuration, int predecessorOID,
     *     java.util.Date validFrom, java.util.Date validTo, java.lang.String comment, boolean
     *     disabled, boolean ignoreWarnings)
     */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deployModel(
         java.lang.String model, java.lang.String configuration, int predecessorOID,
         java.util.Date validFrom, java.util.Date validTo, java.lang.String comment,
         boolean disabled, boolean ignoreWarnings,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
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
     * @param ignoreWarnings   specifies that the deployment should continue if only warnings were
     *     issued.
     *
     * @return deployment information, including possible errors or warnings.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DeploymentException
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.DeploymentException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     *
     * @deprecated since 6.0, configuration, validFrom, validTo and disabled are ignored.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(
     *     java.lang.String model, java.lang.String configuration, int modelOID, java.util.Date
     *     validFrom, java.util.Date validTo, java.lang.String comment, boolean disabled, boolean
     *     ignoreWarnings)
     */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         java.lang.String model, java.lang.String configuration, int modelOID,
         java.util.Date validFrom, java.util.Date validTo, java.lang.String comment,
         boolean disabled, boolean ignoreWarnings,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Overwrites the specified model.
     *
     * @param deploymentElement   The model to be overwritten.
     * @param modelOID         The modelOID of the model to be overwritten.
     * @param options          The deployment options. Can be null, in which case default deployment
     *     options will be used.
     *
     * @return depoymentInfo   Deployment information information, including possible errors or
     *     warning
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DeploymentException   Exception if the
     *     overwrite operation could not be performed.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.DeploymentException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(
     *     org.eclipse.stardust.engine.api.runtime.DeploymentElement deploymentElement, int
     *     modelOID, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
     */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         org.eclipse.stardust.engine.api.runtime.DeploymentElement deploymentElement, int
         modelOID, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Deploys a group of models.
     * 
     * The deployment operation is transactional, that means either all models in the group
     * are deployed or none of them.
     * Model references will be resolved first within the group, and only if there is no
     * corresponding model in the group
     * the already deployed models will be considered.
     * Note: It is possible to deploy an empty set of models. This will not necessarily mean
     * that audit trail is not being changed.
     * If the PredefinedModel is not already present in audit trail this means it will be
     * deployed in any case.
     *
     * @param deploymentElements The models to be deployed. Each model in the set must have a unique
     *     ID.
     * @param options            The deployment options. Can be null, in which case default
     *     deployment options will be used.
     *
     * @return Deployment information, including possible errors or warnings, one DeploymentInfo per
     *     DeploymentElement.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DeploymentException if the deployment
     *     operation could not be performed.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.DeploymentException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if the deploymentElements argument is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the multiple transactions
     *     trying to deploy models at the same time.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will
     *     be wrapped inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(
     *     java.util.List deploymentElements,
     *     org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.DeploymentInfo>
         deployModel(
         java.util.List deploymentElements,
         org.eclipse.stardust.engine.api.runtime.DeploymentOptions options,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Specifies which implementation alternative (
     * identified by <i>implementationModelId</i>) will be considered
     * the primary implementation of the process interface declared by a specific process
     * definition
     * (identified by <i>interfaceModelOid</i> and <i>processId</i>).
     * 
     * <p>Precondition:
     * <ul><li>There needs to be at least one model having the ID <i>implementingModelId</i>
     * that contains a process
     * definition which implements the specified process interface.</li></ul></p>
     * 
     * <p>If <i>implementationModelId</i> is <code>null</code> the default implementation
     * will be reset
     * to the process definition declaring the process interface (
     * the default implementation).</p>
     *
     * @param interfaceModelOid The OID of the model defining the process interface.
     * @param processId The ID of the process definition declaring the process interface.
     * @param implementationModelId The ID of the model providing the implementation.
     * @param options The linking comments.
     *
     * @return Deployment information, including possible errors or warnings.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DeploymentException if the linking operation
     *     could not be performed.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.DeploymentException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setPrimaryImplementation(
     *     long interfaceModelOid, java.lang.String processId, java.lang.String
     *     implementationModelId, org.eclipse.stardust.engine.api.runtime.LinkingOptions options)
     */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         setPrimaryImplementation(
         long interfaceModelOid, java.lang.String processId, java.lang.String
         implementationModelId, org.eclipse.stardust.engine.api.runtime.LinkingOptions
         options, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Deletes the specified model.
     *
     * @param modelOID the model to be deleted.
     *
     * @return deployment information, including possible errors or warnings.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.DeploymentException
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.DeploymentException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteModel(
     *     long modelOID)
     */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deleteModel(
         long modelOID, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Deletes process instances from the audit trail.
     * <p />
     * Only terminated root process instance can be deleted. All subprocess instances
     * started by one of the root process instances to be deleted will be deleted
     * transitively, too.
     *
     * @param piOids A list with OIDs of the root process instance to be deleted.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException Raised if non-root
     *     or non-terminated process
     *        instances are to be deleted.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteProcesses(
     *     java.util.List piOids)
     */
    public void deleteProcesses(
         java.util.List piOids,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Removes all records from the runtime environment making up the audit trail
     * database. The tables will still remain in the database.
     *
     * @param keepUsers a flag to specify if the users should be deleted or not.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#cleanupRuntime(
     *     boolean keepUsers)
     */
    public void cleanupRuntime(
         boolean keepUsers, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Removes all records from the runtime environment making up the audit trail
     * database. Additionally empties the model table. The tables will still remain in the
     * database.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#cleanupRuntimeAndModels(
     *     )
     */
    public void
         cleanupRuntimeAndModels(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Changes the process instance priority.
     * Equivalent with setProcessInstancePriority(oid, priority, false).
     *
     * @param oid the OID of the process instance the priority should be changed of.
     * @param priority the new priority of the process instance.
     *
     * @return the process instance that was changed.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process
     *     instance with the specified oid.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProcessInstancePriority(
     *     long oid, int priority)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         setProcessInstancePriority(
         long oid, int priority,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Changes the process instance priority.
     *
     * @param oid the OID of the process instance the priority should be changed of.
     * @param priority the new priority of the process instance.
     * @param propagateToSubProcesses if true, the priority will be propagated to all subprocesses.
     *
     * @return the process instance that was changed.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process
     *     instance with the specified oid.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProcessInstancePriority(
     *     long oid, int priority, boolean propagateToSubProcesses)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         setProcessInstancePriority(
         long oid, int priority, boolean propagateToSubProcesses,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Aborts a process instance disregarding any activities which were or are
     * performed by the process instance.
     * Regularly the process instance to be aborted will be set to the state ABORTING
     * synchronously.
     * The returned ProcessInstance object will already be in that state.
     * Before returning the ProcessInstance object, a asynchronous abortion task is scheduled
     * for it.
     * If the process instance is not yet inserted in the database but needs to be aborted
     * for some reason ( e.g. by abort process event)
     * then the abort operation is optimized to happen completely synchronously.
     * In that case the returned ProcessInstance will already be in state ABORTED.
     * 
     * <em>This method also aborts all super process instances.</em>
     * 
     * <p>State changes:
     * <ul><li>Process state before: active, interrupted</li>
     * <li>State after: The state of root process, all sub-processes and activities that are
     * not yet completed changes to aborted.</li></ul>
     * </p>
     *
     * @param oid the OID of the process instance to be aborted.
     *
     * @return the process instance that was aborted.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process
     *     instance with the specified oid.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if the oid
     *     references a case process instance.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long)
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#abortProcessInstance(
     *     long oid)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         abortProcessInstance(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Recovers the process instance identified by the given OID and all of its subprocess
     * instances, executed in a separate transaction. By default the execution is
     * synchronous.
     * An asynchronous successor attempt to recover the process instance is scheduled only
     * if there is a non fatal error (e.g. locking conflicts) during the first attempt.
     * <p>This includes crashes during non-interactive application execution and crashes
     * of the CARNOT runtime engine itself.</p>
     *
     * @param oid the OID of the process instance to be recovered.
     *
     * @return the process instance that was recovered.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process
     *     instance with the specified oid.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverProcessInstance(
     *     long oid)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         recoverProcessInstance(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Recovers the process instances identified by the given list of OIDs and all
     * associated subprocess instances.Executed in a separate transaction.
     * By default the execution is synchronous. Asynchronous successor attempts to recover
     * the affected
     * process instances are only scheduled if there are non fatal errors (
     * e.g. locking conflicts)
     * during the first attempts.
     * <p>This includes crashes during non-interactive application execution and crashes
     * of the CARNOT runtime engine itself.</p>
     *
     * @param oids the list of OID of the process instance to be recovered.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process
     *     instance for one of the specified oids.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverProcessInstances(
     *     java.util.List oids)
     */
    public void recoverProcessInstances(
         java.util.List oids, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
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
     *
     * @return daemon information.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no daemon with
     *     the specified type.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getDaemon(
     *     java.lang.String daemonType, boolean acknowledge)
     */
    public org.eclipse.stardust.engine.api.runtime.Daemon
         getDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Stops the specified daemon. See {@link #getDaemon(String, boolean)} for a list
     * of daemon types.The stop daemon operation is inherently asynchronous, regardless of
     * the acknowledge flag.
     * The stop operation only marks the daemon for stopping. The daemon will stop
     * asynchronously at the first opportunity.
     * If the acknowledge parameter is set to true then the method will wait for a
     * predetermined (configurable)
     * amount of time for the asynchronous daemon to confirm the execution of the operation.
     * If acknowledge is false then the method will return immediately,
     * without status updates to the returned object.
     * Otherwise it will wait for the executor thread to confirm the action.
     * The acknowledge flag allows to wait for the executor to confirm the actual stop of the
     * daemon.
     * The status object is not influenced by this flag.
     *
     * @param daemonType the type of the daemon to be stopped.
     * @param acknowledge whether to acknowledge the stop operation.
     *
     * @return daemon information.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no daemon with
     *     the specified type.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#stopDaemon(
     *     java.lang.String daemonType, boolean acknowledge)
     */
    public org.eclipse.stardust.engine.api.runtime.Daemon
         stopDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Starts the specified daemon. See {@link #getDaemon(String, boolean)} for a list
     * of daemon types.
     * The start daemon operation is inherently asynchronous, regardless of the acknowledge
     * flag.
     * The start operation schedules a timer for the daemon execution which will be performed
     * asynchronously.
     * If the acknowledge parameter is set to true then the method will wait for a
     * predetermined (configurable)
     * amount of time for the asynchronous daemon to confirm the execution of the operation.
     * If acknowledge is false then the method will return immediately, without status
     * updates to the returned object.
     * Otherwise it will wait for the executor thread to confirm the action.
     * The acknowledge flag allows to wait for the executor to confirm the actual start of
     * the daemon.
     * The status object is not influenced by this flag.
     *
     * @param daemonType the type of the daemon to be started.
     * @param acknowledge whether to acknowledge the start operation
     *
     * @return daemon information.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no daemon with
     *     the specified type.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#startDaemon(
     *     java.lang.String daemonType, boolean acknowledge)
     */
    public org.eclipse.stardust.engine.api.runtime.Daemon
         startDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves a list of all the available daemons.
     *
     * @param acknowledge whether to acknowledge the daemon information
     *
     * @return a list containing Daemon objects.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAllDaemons(
     *     boolean acknowledge)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Daemon>
         getAllDaemons(
         boolean acknowledge, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Determines key indicators of audit trail health.
     *
     * @return A status report indicating some important indicators of audit trail health.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAuditTrailHealthReport(
     *     )
     */
    public org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport
         getAuditTrailHealthReport(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Determines key indicators of audit trail health.
     *
     * @param countOnly
     *               Determines if report should include the process instances oids or just the
     *               total count of oids. If countOnly is set to true the total count of
     *               process instances will be included in report. If countOnly is set to false
     *               a list containing the process instances oids will be included in report.
     *
     * @return A status report indicating some important indicators of audit trail health.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAuditTrailHealthReport(
     *     boolean countOnly)
     */
    public org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport
         getAuditTrailHealthReport(
         boolean countOnly, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Recovers the complete CARNOT runtime environment.Executed in a separate transaction.
     * By default the execution is synchronous. Only if there are non fatal errors
     * (e.g. locking conflicts), for the affected process instances, a successor asynchronous
     * attempt
     * to recover is scheduled.
     * <p>This reanimates dead activity threads from an application server crash.
     * Additionally previously interrupted processes are reanimated.</p>
     * <p>It is equivalent with recovering all the process instances which are in the
     * states active or interrupted.</p>
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverRuntimeEnvironment(
     *     )
     */
    public void
         recoverRuntimeEnvironment(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Starts a process from a specified model.The startProcess method is executed
     * asynchronously
     * if the synchronously parameter is set to false.However, even if the synchronously
     * parameter is true,
     * the execution of activities is performed in the calling thread only up to the first
     * transition marked
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
     *                          be set as values.
     * @param synchronously determines whether the process will be started synchronously
     *                          or asynchronously.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance} that was started.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process with
     *     the specified ID in the
     *             specified model or if the model does not exist.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#startProcess(
     *     long modelOID, java.lang.String id, java.util.Map data, boolean synchronously)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         startProcess(
         long modelOID, java.lang.String id, java.util.Map data, boolean synchronously,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Forces the completion of a non-interactive activity instance. A map of access points
     * maybe provided.
     * This way this method can mimic precisely the behavior of a normal completion of the
     * activity.
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
     *                           can be null
     *
     * @return the completed {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity with
     *     the specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the activity instance is
     *     exclusively locked by another thread.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will
     *     be wrapped inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException if the activity is
     *     already completed or aborted.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidValueException if one of the
     *     <code>outData</object> values to
     *             be written is invalid, most probably as of a type conflict in case of
     *             statically typed data.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidValueException} will
     *     be wrapped inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user is not
     *     an administrator.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if the activity
     *     instance is interactive.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see #forceSuspendToDefaultPerformer(long)
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#forceCompletion(
     *     long activityInstanceOID, java.util.Map accessPoints)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         forceCompletion(
         long activityInstanceOID, java.util.Map accessPoints,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
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
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was
     *     suspended.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity
     *     instance with the specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the activity instance is
     *     exclusively locked by another thread.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will
     *     be wrapped inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException if the activity is
     *     already completed or aborted.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user does
     *     not have the required privilege.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see #forceCompletion(long, Map)
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#forceSuspendToDefaultPerformer(
     *     long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         forceSuspendToDefaultPerformer(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves information on the current user.
     *
     * @return the current user.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getUser()
     */
    public org.eclipse.stardust.engine.api.runtime.User
         getUser(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Flushes all internal caches, effectively returning the engine to a state just like
     * after it has started.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#flushCaches()
     */
    public void
         flushCaches(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves all permissions the current user has on this service plus the global
     * permissions.
     *
     * @return a list of permission ids.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPermissions()
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves the profile for the specified scope.
     *
     * @param scope
     *
     * @return the profile.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getProfile(
     *     org.eclipse.stardust.engine.api.model.ProfileScope scope)
     */
    public java.util.Map<java.lang.String,?>
         getProfile(
         org.eclipse.stardust.engine.api.model.ProfileScope scope,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Sets the profile for the specified scope.
     *
     * @param scope the scope.
     * @param profile the profile.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProfile(
     *     org.eclipse.stardust.engine.api.model.ProfileScope scope, java.util.Map profile)
     */
    public void setProfile(
         org.eclipse.stardust.engine.api.model.ProfileScope scope, java.util.Map profile,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Logs an audit trail event of type <code>LogCode.ADMINISTRATION</code>.
     *
     * @param logType Set the type of log (
     *     info, warn, error etc.). Whereas the <code>Unknown</code> type is mapped to a warning.
     * @param contextType Set the context scope of the event
     * @param contextOid Oid of the runtime object (
     *     only used if context type is set to ProcessInstance or ActivityInstance)
     * @param message any message that should be logged
     * @param throwable any exception (or null) that should be appended to the message
     *
     * @exception ObjectNotFoundException if there is no runtime object with the specified OID
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#writeLogEntry(
     *     org.eclipse.stardust.engine.api.runtime.LogType logType,
     *     org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
     *     java.lang.String message, java.lang.Throwable throwable)
     */
    public void writeLogEntry(
         org.eclipse.stardust.engine.api.runtime.LogType logType,
         org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
         java.lang.String message, java.lang.Throwable throwable,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Creates a new department.
     *
     * @param id the id of the department. Must not be null or empty and it must be unique in the
     *     parent scope.
     * @param name the name of the department. Must not be null or empty.
     * @param description the description of the department.
     * @param parent the parent scope. Can be null if the department will be a top level department.
     * @param organization the organization to which this department is assigned. Must not be null.
     *
     * @return the created department.
     *
     * @throws DepartmentExistsException
     *           if a department with the same id already exists in the parent scope.
     *     <em>Instances of {@link DepartmentExistsException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws ObjectNotFoundException
     *           if either the parent or the organization could not be resolved.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidArgumentException <br>
     *           - if either the id or the name is null or an empty string or<br>
     *           - if the organization is null or<br>
     *           - if the organization does not resolve to an actual organization in the model
     *           (i.e. resolves to a role or conditional performer) or<br>
     *           - if the organization is not directly part of the organization to which
     *             the parent department is assigned (invalid hierarchy).
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidArgumentException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException - if the user was
     *     external authentified
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#createDepartment(
     *     java.lang.String id, java.lang.String name, java.lang.String description,
     *     org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
     *     org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
     */
    public org.eclipse.stardust.engine.api.runtime.Department
         createDepartment(
         java.lang.String id, java.lang.String name, java.lang.String description,
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
         org.eclipse.stardust.engine.api.model.OrganizationInfo organization,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves the department with the given oid.
     *
     * @param oid the unique identifier of the department.
     *
     * @return the modified department.
     *
     * @throws ObjectNotFoundException
     *           if there is no department with the specified oid.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getDepartment(long oid)
     */
    public org.eclipse.stardust.engine.api.runtime.Department getDepartment(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Change the description of a department.
     *
     * @param oid the unique identifier of the department.
     * @param name the new name of the department.
     * @param description the new description.
     *
     * @return the modified department.
     *
     * @throws ObjectNotFoundException
     *           if there is no department with the specified oid.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException
     *           if the name is null or an empty string
     *     <em>Instances of {@link InvalidArgumentException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException - if the user was
     *     external authentified
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#modifyDepartment(
     *     long oid, java.lang.String name, java.lang.String description)
     */
    public org.eclipse.stardust.engine.api.runtime.Department
         modifyDepartment(
         long oid, java.lang.String name, java.lang.String description,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Removes the department having the specified oid, all his children and all user grants
     * associated with the department.
     *
     * @param oid the unique identifier of the department.
     *
     * @throws ObjectNotFoundException
     *           if there is no department with the specified oid.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException
     *     if there are work items currently associated with the department or any child of the
     *     f the department.
     *     <em>Instances of {@link InvalidArgumentException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException - if the user was
     *     external authentified
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#removeDepartment(
     *     long oid)
     */
    public void removeDepartment(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves preferences from the given scope.
     *
     * @param scope the scope from which the preferences are to be retrieved from
     * @param moduleId the moduleId of the preferences
     * @param preferencesId the id of the preferences
     *
     * @return a preferences object.
     *
     * @throws PublicException if <tt>scope</tt> is null.
     *     <em>Instances of {@link PublicException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPreferences(
     *     org.eclipse.stardust.engine.core.preferences.PreferenceScope scope, java.lang.String
     *     moduleId, java.lang.String preferencesId)
     */
    public org.eclipse.stardust.engine.core.preferences.Preferences
         getPreferences(
         org.eclipse.stardust.engine.core.preferences.PreferenceScope scope,
         java.lang.String moduleId, java.lang.String preferencesId,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Saves the changed preferences to the preference store.
     *
     * @param preferences an preferences object to be saved.
     *
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user does
     *     not have the required privilege.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if <tt>preferences</tt> is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if preferences property <tt>preferences</tt> is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if preferences property <tt>moduleId</tt> is null or empty.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if preferences property <tt>preferencesId</tt> is null or
     *     empty.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#savePreferences(
     *     org.eclipse.stardust.engine.core.preferences.Preferences preferences)
     */
    public void
         savePreferences(
         org.eclipse.stardust.engine.core.preferences.Preferences preferences,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Saves a complete list of preferences to the preference store.
     *
     * @param preferences a list of preferences to be saved.
     *
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user does
     *     not have the required privilege.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if <tt>preferences</tt> is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if preferences property <tt>moduleId</tt> is null or empty.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if preferences property <tt>preferencesId</tt> is null or
     *     empty.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#savePreferences(
     *     java.util.List preferences)
     */
    public void savePreferences(
         java.util.List preferences,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves merged configuration variables from all models matching the specified
     * modelId (without Password type).
     * The contained descriptions and default values are taken from the newest model version
     * the configuration variable exists in.
     *
     * @param modelId The modelId of the model(s) to retrieve the configuration variables from.
     *
     * @return A ConfigurationVariables object containing the merged configuration variables from all
     *     model versions.
     *
     * @throws InvalidArgumentException if <tt>modelId</tt> is null or empty.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(
     *     java.lang.String modelId)
     */
    public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         java.lang.String modelId,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves merged configuration variables from all models matching the specified
     * modelId.
     * The contained descriptions and default values are taken from the newest model version
     * the configuration variable exists in.
     *
     * @param modelId The modelId of the model(s) to retrieve the configuration variables from.
     * @param all Indicates if to fetch all configuration variables, including Password type.
     *
     * @return A ConfigurationVariables object containing the merged configuration variables from all
     *     model versions.
     *
     * @throws InvalidArgumentException if <tt>modelId</tt> is null or empty.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(
     *     java.lang.String modelId, boolean all)
     */
    public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         java.lang.String modelId, boolean all,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves merged configuration variables from all models matching the specified
     * modelIds (without Password type).
     * The contained descriptions and default values are taken from the newest model version
     * the configuration variable exists in.
     *
     * @param modelIds The modelId of the model(s) to retrieve the configuration variables from.
     *
     * @return A List of ConfigurationVariables objects containing the merged configuration variables
     *     from all model versions.
     *
     * @throws InvalidArgumentException if <tt>modelIds</tt> is null or contains an modelId which is
     *     null or empty.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(
     *     java.util.List modelIds)
     */
    public
         java.util.List<org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables>
         getConfigurationVariables(
         java.util.List modelIds,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves configuration variables from the given model (without Password type).
     *
     * @param model The model xml representation in byte array form.
     *
     * @return A ConfigurationVariables object containing only the configuration variables from the
     *     given model.
     *
     * @throws InvalidArgumentException if <tt>model</tt> is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(
     *     byte[] model)
     */
    public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         byte[] model, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Saves changes to configuration variables values.
     *
     * @param configurationVariables The configuration variables containing changed values.
     * @param force Option to ignore validation warnings.
     *
     * @return model reconfiguration information, including possible errors or warnings.
     *
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user does
     *     not have the required privilege.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if <tt>configurationVariables</tt> is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#saveConfigurationVariables(
     *     org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
     *     configurationVariables, boolean force)
     */
    public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo>
         saveConfigurationVariables(
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         configurationVariables, boolean force,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves permissions that are globally set. For example permissions concerning
     * model deployment, preference saving, modifying AuditTrail, managing deamons ect.
     *
     * @return the currently set Permissions
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getGlobalPermissions()
     */
    public org.eclipse.stardust.engine.api.runtime.RuntimePermissions
         getGlobalPermissions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Saves the changed Permissions.
     * Use <code>getGlobalPermissions</code> to retrieve currently valid global permissions
     * first.
     * Permissions with null or empty lists set as grants will be reset to their internal
     * default.
     * Changed grants are validated against the currently active model for existing model
     * participants.
     *
     * @param permissions the modified permissions
     *
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user does
     *     not have the required privilege.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if <tt>permissions</tt> is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws ValidationException if changed grants are not valid in the active model.
     *     <em>Instances of {@link ValidationException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setGlobalPermissions(
     *     org.eclipse.stardust.engine.api.runtime.RuntimePermissions permissions)
     */
    public void
         setGlobalPermissions(
         org.eclipse.stardust.engine.api.runtime.RuntimePermissions permissions,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Deploys a new artifact with a new oid.
     * <p>
     * If an artifact with the same validFrom date already exists,
     * the newly deployed artifact takes priority when querying for active artifacts.
     *
     * @param runtimeArtifact The new artifact.
     *
     * @return The deployed artifact including an assigned oid.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployRuntimeArtifact(
     *     org.eclipse.stardust.engine.api.runtime.RuntimeArtifact runtimeArtifact)
     */
    public org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact
         deployRuntimeArtifact(
         org.eclipse.stardust.engine.api.runtime.RuntimeArtifact runtimeArtifact,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Overwrites only content of a specified already deployed artifact.
     * Other fields cannot be changed.
     *
     * @param oid The oid of the artifact.
     * @param runtimeArtifact The new artifact.
     *
     * @return The updated artifact.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no runtime
     *     artifact with the specified oid.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteRuntimeArtifact(
     *     long oid, org.eclipse.stardust.engine.api.runtime.RuntimeArtifact runtimeArtifact)
     */
    public org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact
         overwriteRuntimeArtifact(
         long oid, org.eclipse.stardust.engine.api.runtime.RuntimeArtifact
         runtimeArtifact, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Deleted a deployed artifact by oid.
     *
     * @param oid The oid of the artifact
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no runtime
     *     artifact with the specified oid.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteRuntimeArtifact(
     *     long oid)
     */
    public void deleteRuntimeArtifact(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Retrieves the artifact by the unique oid.
     *
     * @param oid The oid of the artifact.
     *
     * @return The artifact or <code>null<code> if it does not exist.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getRuntimeArtifact(
     *     long oid)
     */
    public org.eclipse.stardust.engine.api.runtime.RuntimeArtifact
         getRuntimeArtifact(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         
    /**
     * Returns a list of supported artifact types.
     * <p>
     * 
     * The {@link ArtifactType#getId()} is used to identify the {@link ArtifactType} for a
     * {@link RuntimeArtifact}.
     *
     * @return The supported artifact types.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getSupportedRuntimeArtifactTypes(
     *     )
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.ArtifactType>
         getSupportedRuntimeArtifactTypes(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException,
         java.rmi.RemoteException;
         }