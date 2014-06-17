/*
 * Generated from
 */
package org.eclipse.stardust.engine.api.ejb2.tunneling;

/**
 * to set {@link SessionProperties.DS_NAME_READ_ONLY}
 * used in {@link DataValueBean}
 *
 */
public interface TunnelingLocalQueryService extends javax.ejb.EJBLocalObject, org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingLocalService
{

    /**
     * Counts the number of users satisfying the criteria specified in the provided query.
     *
     * @param query the user query.
     *
     * @return the user count.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getUsersCount(
     *     org.eclipse.stardust.engine.api.query.UserQuery query)
     */
    public long getUsersCount(
         org.eclipse.stardust.engine.api.query.UserQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Counts the number of user groups satisfying the criteria specified in the provided
     * query.
     *
     * @param query the user group query.
     *
     * @return the user group count.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getUserGroupsCount(
     *     org.eclipse.stardust.engine.api.query.UserGroupQuery query)
     */
    public long
         getUserGroupsCount(
         org.eclipse.stardust.engine.api.query.UserGroupQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Counts the number of process instances satisfying the criteria specified in the
     * provided query.
     *
     * @param query the process instance query.
     *
     * @return the process instance count.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if no attributeName
     *        (XPath) is specified in a DataFilter for queries on a structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) is specified in a DataFilter for queries on a non-structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) specified in a DataFilter contains an invalid XPath
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if
     *     PerformingOnBehalfOfFilter is used
     *        but activity instance history is disabled.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessInstancesCount(
     *     org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
     */
    public long
         getProcessInstancesCount(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Counts the number of activity instances satisfying the criteria specified in the
     * provided query.
     *
     * @param query the activity instance query.
     *
     * @return the activity instance count.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if no attributeName
     *        (XPath) is specified in a DataFilter for queries on a structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) is specified in a DataFilter for queries on a non-structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) specified in a DataFilter contains an invalid XPath
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if
     *     PerformingOnBehalfOfFilter is used
     *        but activity instance history is disabled.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActivityInstancesCount(
     *     org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
     */
    public long
         getActivityInstancesCount(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Counts the number of log entries satisfying the criteria specified in the
     * provided query.
     *
     * @param query the log entry query.
     *
     * @return the log entry count.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getLogEntriesCount(
     *     org.eclipse.stardust.engine.api.query.LogEntryQuery query)
     */
    public long
         getLogEntriesCount(
         org.eclipse.stardust.engine.api.query.LogEntryQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all users satisfying the criteria specified in the provided query.
     *
     * @param query the user query.
     *
     * @return a List of User objects.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllUsers(
     *     org.eclipse.stardust.engine.api.query.UserQuery query)
     */
    public org.eclipse.stardust.engine.api.query.Users
         getAllUsers(
         org.eclipse.stardust.engine.api.query.UserQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all user groups satisfying the criteria specified in the provided query.
     *
     * @param query the user group query.
     *
     * @return A list of {@link org.eclipse.stardust.engine.api.runtime.UserGroup} objects.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllUserGroups(
     *     org.eclipse.stardust.engine.api.query.UserGroupQuery query)
     */
    public org.eclipse.stardust.engine.api.query.UserGroups
         getAllUserGroups(
         org.eclipse.stardust.engine.api.query.UserGroupQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all process instances satisfying the criteria specified in the
     * provided query.
     *
     * @param query the process instance query.
     *
     * @return a List of ProcessInstance objects.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if no attributeName
     *         (XPath) is specified in a DataFilter for queries on a structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *         (XPath) is specified in a DataFilter for queries on a non-structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) specified in a DataFilter contains an invalid XPath
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if
     *     PerformingOnBehalfOfFilter is used
     *        but activity instance history is disabled.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessInstances(
     *     org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
     */
    public org.eclipse.stardust.engine.api.query.ProcessInstances
         getAllProcessInstances(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all activity instances satisfying the criteria specified in the
     * provided query.
     *
     * @param query the activity instance query.
     *
     * @return a List of ActivityInstance objects.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if no attributeName
     *         (XPath) is specified in a DataFilter for queries on a structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *         (XPath) is specified in a DataFilter for queries on a non-structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) specified in a DataFilter contains an invalid XPath
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if
     *     PerformingOnBehalfOfFilter is used
     *        but activity instance history is disabled.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllActivityInstances(
     *     org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
     */
    public org.eclipse.stardust.engine.api.query.ActivityInstances
         getAllActivityInstances(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all log entries satisfying the criteria specified in the
     * provided query.
     *
     * @param query the log entry query.
     *
     * @return a List of LogEntry objects.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllLogEntries(
     *     org.eclipse.stardust.engine.api.query.LogEntryQuery query)
     */
    public org.eclipse.stardust.engine.api.query.LogEntries
         getAllLogEntries(
         org.eclipse.stardust.engine.api.query.LogEntryQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the first User satisfying the criteria specified in the
     * provided query.
     *
     * @param query the user query.
     *
     * @return the first matching user.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if no matching user is
     *     found.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstUser(
     *     org.eclipse.stardust.engine.api.query.UserQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.User
         findFirstUser(
         org.eclipse.stardust.engine.api.query.UserQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the first UserGroup satisfying the criteria specified in the
     * provided query.
     *
     * @param query the user group query.
     *
     * @return the first matching user group.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if no matching user group is
     *     found.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstUserGroup(
     *     org.eclipse.stardust.engine.api.query.UserGroupQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.UserGroup
         findFirstUserGroup(
         org.eclipse.stardust.engine.api.query.UserGroupQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the first ProcessInstance satisfying the criteria specified in the
     * provided query.
     *
     * @param query the process instance query.
     *
     * @return the first matching process instance.
     *
     * @throws ObjectNotFoundException
     *        if no matching process instance is found.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if no attributeName
     *        (XPath) is specified in a DataFilter for queries on a structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) is specified in a DataFilter for queries on a non-structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) specified in a DataFilter contains an invalid XPath
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if
     *     PerformingOnBehalfOfFilter is used
     *        but activity instance history is disabled.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstProcessInstance(
     *     org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         findFirstProcessInstance(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the first ActivityInstance satisfying the criteria specified in the
     * provided query.
     *
     * @param query
     *               the activity instance query.
     *
     * @return the first matching activity instance.
     *
     * @throws ObjectNotFoundException
     *        if no matching activity instance is found.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if no attributeName
     *        (XPath) is specified in a DataFilter for queries on a structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) is specified in a DataFilter for queries on a non-structured data
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if attributeName
     *        (XPath) specified in a DataFilter contains an invalid XPath
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException if
     *     PerformingOnBehalfOfFilter is used
     *        but activity instance history is disabled.
     *     <em>Instances of {@link
     *     org.eclipse.stardust.engine.api.runtime.IllegalOperationException} will be wrapped
     *     inside {@link org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstActivityInstance(
     *     org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         findFirstActivityInstance(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the first LogEntry satisfying the criteria specified in the
     * provided query.
     *
     * @param query the log entry query.
     *
     * @return the first matching log entry.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if no matching log entry is
     *     found.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstLogEntry(
     *     org.eclipse.stardust.engine.api.query.LogEntryQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.LogEntry
         findFirstLogEntry(
         org.eclipse.stardust.engine.api.query.LogEntryQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Returns all performed activity instances for the specified process instance.
     *
     * @param processInstanceOID the OID of the process instance from where we retrieve the audit
     *     trail.
     *
     * @return a List of {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} objects.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process
     *     instance with the specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAuditTrail(
     *     long processInstanceOID)
     */
    public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ActivityInstance>
         getAuditTrail(
         long processInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the list of model descriptions for all deployed models.
     *
     * @return a List of {@link org.eclipse.stardust.engine.api.runtime.DeployedModelDescription}
     *     objects.
     *
     * @deprecated Use {@link Models getModels(DeployedModelQuery.findAll())}.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllModelDescriptions()
     */
    public
         java.util.List<org.eclipse.stardust.engine.api.runtime.DeployedModelDescription>
         getAllModelDescriptions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the list of model descriptions for all alive models. Whereby alive models
     * are models with non-completed and non-aborted processes plus the
     * active model.
     *
     * @return a List of {@link org.eclipse.stardust.engine.api.runtime.DeployedModelDescription}
     *     objects.
     *
     * @deprecated Use {@link Models
     *     getModels(DeployedModelQuery.findInState(DeployedModelQuery.ALIVE))}.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllAliveModelDescriptions()
     */
    public
         java.util.List<org.eclipse.stardust.engine.api.runtime.DeployedModelDescription>
         getAllAliveModelDescriptions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the current active model description.
     *
     * @return the description of the active model.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no active model.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     *
     * @deprecated This method returns the description of the active model with the highest priority.
     *       Use {@link Models getModels(DeployedModelQuery.findActive(
     *     ))} to retrieve all active models.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActiveModelDescription()
     */
    public org.eclipse.stardust.engine.api.runtime.DeployedModelDescription
         getActiveModelDescription(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the model descriptions satisfying the criteria specified in the provided
     * query.
     *
     * @param query the deployed model query.
     *
     * @return a List of DeployedModelDescription objects.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModels(
     *     org.eclipse.stardust.engine.api.query.DeployedModelQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.Models
         getModels(
         org.eclipse.stardust.engine.api.query.DeployedModelQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the description of the specified model.
     *
     * @param modelOID the oid of the model to retrieve.
     *
     * @return the description of the specified model.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no model with
     *     the specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModelDescription(
     *     long modelOID)
     */
    public org.eclipse.stardust.engine.api.runtime.DeployedModelDescription
         getModelDescription(
         long modelOID, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Determines if the model was redeployed, i.e. if a more recent revision than the
     * provided one is available.
     *
     * @param modelOid The OID of the model to be checked..
     * @param revision The currently retrieved revision of the model.
     *
     * @return <code>true</code> if a more recent revision of the model is available,
     *       <code>false</code> if not.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#wasRedeployed(
     *     long modelOid, int revision)
     */
    public boolean wasRedeployed(
         long modelOid, int revision,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the specified model.
     *
     * @param modelOID the oid of the model to retrieve.
     *
     * @return the specified model.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no model with
     *     the specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModel(long modelOID)
     */
    public org.eclipse.stardust.engine.api.runtime.DeployedModel getModel(
         long modelOID, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the specified model.
     *
     * @param modelOID the oid of the model to retrieve.
     * @param computeAliveness whether the aliveness of the model should be computed or not
     *
     * @return the specified model.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no model with
     *     the specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModel(
     *     long modelOID, boolean computeAliveness)
     */
    public org.eclipse.stardust.engine.api.runtime.DeployedModel getModel(
         long modelOID, boolean computeAliveness,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the current active model.
     *
     * @return the active model.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no active model.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     *
     * @deprecated This method returns the active model with the highest priority.
     *       Use {@link Models getModels(DeployedModelQuery.findActive(
     *     ))} to retrieve all active models.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActiveModel()
     */
    public org.eclipse.stardust.engine.api.runtime.DeployedModel
         getActiveModel(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the XML representation of the specified model.
     *
     * @param modelOID the oid of the model to retrieve.
     *
     * @return A string containing the XML representation of the model.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no model with
     *     the specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModelAsXML(long modelOID)
     */
    public java.lang.String getModelAsXML(
         long modelOID, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all the process definitions contained in the specified model.
     *
     * @param modelOID the oid of the model.
     *
     * @return a List of {@link org.eclipse.stardust.engine.api.model.ProcessDefinition} objects.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no model with
     *     the specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessDefinitions(
     *     long modelOID)
     */
    public
         java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getAllProcessDefinitions(
         long modelOID, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves a process definition from the specified model.
     *
     * @param modelOID   the oid of the model.
     * @param id         the id of the process definition.
     *
     * @return the process definition.
     *
     * @throws ObjectNotFoundException
     *           if there is no model with the specified OID or there is no process
     *           definition with the specified id in the model.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinition(
     *     long modelOID, java.lang.String id)
     */
    public org.eclipse.stardust.engine.api.model.ProcessDefinition
         getProcessDefinition(
         long modelOID, java.lang.String id,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all process definitions for the active model.
     *
     * @return a List of {@link org.eclipse.stardust.engine.api.model.ProcessDefinition} objects.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no active model.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessDefinitions()
     */
    public
         java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getAllProcessDefinitions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the specified process definition from the active model.
     *
     * @param id the id of the process definition.
     *
     * @return the process definition.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no active model
     *     or if the active model
     *             does not contain the requested process definition.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinition(
     *     java.lang.String id)
     */
    public org.eclipse.stardust.engine.api.model.ProcessDefinition
         getProcessDefinition(
         java.lang.String id,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinitions(
     *     org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessDefinitions
         getProcessDefinitions(
         org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all data satisfying the criteria specified in the
     * provided query.
     *
     * @param query The DataQuery.
     *
     * @return A list of Data objects.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllData(
     *     org.eclipse.stardust.engine.api.query.DataQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.DataQueryResult
         getAllData(
         org.eclipse.stardust.engine.api.query.DataQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all participants defined in the specified model.
     *
     * @param modelOID the oid of the model.
     *
     * @return a List of {@link org.eclipse.stardust.engine.api.model.Participant} objects.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no model with
     *     the specified oid.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllParticipants(long modelOID)
     */
    public java.util.List<org.eclipse.stardust.engine.api.model.Participant>
         getAllParticipants(
         long modelOID, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves a participant from a specified model.
     *
     * @param modelOID   the oid of the model.
     * @param id         the id of the participant.
     *
     * @return the participant.
     *
     * @throws ObjectNotFoundException
     *           if there is no model with the specified oid, or the model does not
     *           contains the requested participant.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getParticipant(
     *     long modelOID, java.lang.String id)
     */
    public org.eclipse.stardust.engine.api.model.Participant getParticipant(
         long modelOID, java.lang.String id,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all the participants from the active model.
     *
     * @return a List of {@link org.eclipse.stardust.engine.api.model.Participant} objects.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no active model.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllParticipants()
     */
    public java.util.List<org.eclipse.stardust.engine.api.model.Participant>
         getAllParticipants(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves a specific participant from the active model.
     *
     * @param id the id of the participant.
     *
     * @return the participant.
     *
     * @throws ObjectNotFoundException
     *           if there is no active model, or if the active model does not contain
     *           the requested participant.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getParticipant(
     *     java.lang.String id)
     */
    public org.eclipse.stardust.engine.api.model.Participant
         getParticipant(
         java.lang.String id,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all permissions the current user has on this service.
     *
     * @return a list of permission ids.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getPermissions()
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves XSD schema of the specified type declaration serialized
     * into a byte[].
     *
     * @param modelOID           the oid of the model.
     * @param typeDeclarationId  the id of the type declaration.
     *
     * @return XSD schema of this type declaration
     *
     * @throws ObjectNotFoundException
     *           if there is no active model, or if the active model does not contain the
     *           requested type declaration.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getSchemaDefinition(
     *     long modelOID, java.lang.String typeDeclarationId)
     */
    public byte[] getSchemaDefinition(
         long modelOID, java.lang.String typeDeclarationId,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all the departments satisfying the search criteria. The search is
     * performed as following:
     * <ul>
     * <li>if both parent and organization are null, then the result contains all top level
     * departments, regardless of the organization to which they are assigned.</li>
     * <li>if parent is not null but the organization is null, then the result contains all
     * direct children of the parent department, regardless of the organization to which
     * they are assigned.</li>
     * <li>if parent is null but the organization is not null, then the result contains all
     * departments assigned to the organization, regardless of their parent department.</li>
     * <li>if both parent and organization are not null, then the result contains all
     * departments assigned to the organization, that have as direct parent the specified
     * department.</li>
     * </ul>
     * On synchronization departments will be updated when existing in audit trail and
     * having any changes. If a department does not exist in audit trail but is present in
     * external repository the department will not be created in audit trail on
     * synchronization with external repository.
     *
     * @param parent
     *               the parent department.
     * @param organization
     *               the organization to which the retrieved departments are assigned.
     *
     * @return the list of departments. The list can be empty if no departments are
     *             matching the search criteria.
     *
     * @throws ObjectNotFoundException
     *                if either the parent or the organization could not be resolved.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#findAllDepartments(
     *     org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
     *     org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Department>
         findAllDepartments(
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
         org.eclipse.stardust.engine.api.model.OrganizationInfo organization,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Searches for a department having the specified id in the scope defined by the parent
     * department. On synchronization with external repository the specified department
     * will be created in audit trail if it is not already present there but exists in
     * external repository. If the department exists in audit trail it will be updated on
     * synchronization if there are any changes.
     *
     * @param parent
     *               the search scope. It can be null, in which case the search scope is the
     *               top level.
     * @param id
     *               the id of the department. Must not be null or empty.
     * @param organization
     *               the organization to which the retrieved departments are assigned.
     *
     * @return the department having the specified id.
     *
     * @throws ObjectNotFoundException
     *                if the parent could not be resolved or if the specified id is null or
     *                empty or if there is no department with the specified id in the parent
     *                scope.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#findDepartment(
     *     org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, java.lang.String id,
     *     org.eclipse.stardust.engine.api.model.OrganizationInfo info)
     */
    public org.eclipse.stardust.engine.api.runtime.Department
         findDepartment(
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, java.lang.String
         id, org.eclipse.stardust.engine.api.model.OrganizationInfo info,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the first document satisfying the criteria specified in the
     * provided query.
     *
     * @param query the document query.
     *
     * @return the first matching document.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if no matching document is
     *     found.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException}
     *     will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstDocument(
     *     org.eclipse.stardust.engine.api.query.DocumentQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.Document
         findFirstDocument(
         org.eclipse.stardust.engine.api.query.DocumentQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves all documents satisfying the criteria specified in the provided query.
     *
     * @param query the document query.
     *
     * @return a List of Document objects.
     *
     * @deprecated since 8.0 use {@link DocumentManagementService#findDocuments(DocumentQuery)}.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllDocuments(
     *     org.eclipse.stardust.engine.api.query.DocumentQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.Documents
         getAllDocuments(
         org.eclipse.stardust.engine.api.query.DocumentQuery query,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves preferences from the given scope.
     *
     * @param scope the scope from which the preferences are to be retrieved from.
     * @param moduleId the moduleId of the preferences.
     * @param preferencesId the id of the preferences.
     *
     * @return a preferences object.
     *
     * @throws PublicException if <tt>scope</tt> is null.
     *     <em>Instances of {@link PublicException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getPreferences(
     *     org.eclipse.stardust.engine.core.preferences.PreferenceScope scope, java.lang.String
     *     moduleId, java.lang.String preferencesId)
     */
    public org.eclipse.stardust.engine.core.preferences.Preferences
         getPreferences(
         org.eclipse.stardust.engine.core.preferences.PreferenceScope scope,
         java.lang.String moduleId, java.lang.String preferencesId,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves preferences satisfying the criteria specified in the provided query.
     *
     * @param preferenceQuery the preference query.
     *
     * @return a list of preferences.
     *
     * @throws PublicException if querying is not supported for the specified PreferenceScope.
     *     <em>Instances of {@link PublicException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws UnsupportedOperationException if the PreferenceQuery contains unsupported terms or
     *     operations.
     *     <em>Instances of {@link UnsupportedOperationException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws InvalidArgumentException if <tt>preferencesQuery</tt> is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.common.error.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllPreferences(
     *     org.eclipse.stardust.engine.api.query.PreferenceQuery preferenceQuery)
     */
    public
         java.util.List<org.eclipse.stardust.engine.core.preferences.Preferences>
         getAllPreferences(
         org.eclipse.stardust.engine.api.query.PreferenceQuery preferenceQuery,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves the information about the deployed runtime environment (
     * e.g. version information).
     *
     * @return the runtime environment information.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getRuntimeEnvironmentInfo()
     */
    public org.eclipse.stardust.engine.api.runtime.RuntimeEnvironmentInfo
         getRuntimeEnvironmentInfo(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;

    /**
     * Retrieves a resource bundle from a specified moduleId.
     *
     * @param moduleId The id of the engine resource bundle module.
     * @param bundleName The name of the bundle.
     * @param locale The to retrieve the resource bundle for.
     *
     * @return The ResourceBundle or null if no ResourceBundle was found.
     *
     * @throws org.eclipse.stardust.common.error.WorkflowException as a wrapper for
     *         org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.QueryService#getResourceBundle(
     *     java.lang.String moduleId, java.lang.String bundleName, java.util.Locale locale)
     */
    public org.eclipse.stardust.engine.api.runtime.ResourceBundle
         getResourceBundle(
         java.lang.String moduleId, java.lang.String bundleName, java.util.Locale locale,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.common.error.WorkflowException;
         }