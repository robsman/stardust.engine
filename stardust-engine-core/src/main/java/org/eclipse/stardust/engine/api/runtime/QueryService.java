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

import java.util.List;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;
import org.eclipse.stardust.engine.core.runtime.utils.TransientState;



/**
 * Provides information services for the CARNOT runtime environment.
 * <p>The functionality includes the following tasks:</p>
 * <ul>
 * <li>retrieve or counts elements in the audit trail (users, process instance, activity instances or log events)</li>
 * <li>retrieve deployed models from the audit trails</li>
 * <li>retrieve participants from the models</li>
 * </ul>
 * <p>A Query service always operates against an audit trail database.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
/**
 * to set {@link SessionProperties.DS_NAME_READ_ONLY}
 * used in {@link DataValueBean}
 */
@TransientState
public interface QueryService extends Service
{
   /**
    * Counts the number of users satisfying the criteria specified in the provided query.
    *
    * @param query the user query.
    *
    * @return the user count.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readUserData,
         defaults={ExecutionPermission.Default.ALL})
   long getUsersCount(UserQuery query);

   /**
    * Counts the number of user groups satisfying the criteria specified in the provided
    * query.
    *
    * @param query the user group query.
    *
    * @return the user group count.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readUserData,
         defaults={ExecutionPermission.Default.ALL})
   long getUserGroupsCount(UserGroupQuery query);

   /**
    * Counts the number of process instances satisfying the criteria specified in the
    * provided query.
    *
    * @param query the process instance query.
    *
    * @return the process instance count.
    *
    * @throws IllegalOperationException if no attributeName
    *    (XPath) is specified in a DataFilter for queries on a structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) is specified in a DataFilter for queries on a non-structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) specified in a DataFilter contains an invalid XPath
    * @throws IllegalOperationException if PerformingOnBehalfOfFilter is used
    *    but activity instance history is disabled.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})
   long getProcessInstancesCount(ProcessInstanceQuery query) throws IllegalOperationException;

   /**
    * Counts the number of activity instances satisfying the criteria specified in the
    * provided query.
    *
    * @param query the activity instance query.
    *
    * @return the activity instance count.
    *
    * @throws IllegalOperationException if no attributeName
    *    (XPath) is specified in a DataFilter for queries on a structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) is specified in a DataFilter for queries on a non-structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) specified in a DataFilter contains an invalid XPath
    * @throws IllegalOperationException if PerformingOnBehalfOfFilter is used
    *    but activity instance history is disabled.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readActivityInstanceData,
         scope=ExecutionPermission.Scope.activity,
         defer=true,
         defaults={ExecutionPermission.Default.ALL},
         fixed={ExecutionPermission.Default.OWNER})
   long getActivityInstancesCount(ActivityInstanceQuery query) throws IllegalOperationException;

   /**
    * Counts the number of log entries satisfying the criteria specified in the
    * provided query.
    *
    * @param query the log entry query.
    *
    * @return the log entry count.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.readAuditTrailStatistics)
   long getLogEntriesCount(LogEntryQuery query);

   /**
    * Retrieves all users satisfying the criteria specified in the provided query.
    *
    * @param query the user query.
    *
    * @return a List of User objects.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readUserData,
         defaults={ExecutionPermission.Default.ALL})
   Users getAllUsers(UserQuery query);

   /**
    * Retrieves all user groups satisfying the criteria specified in the provided query.
    *
    * @param query the user group query.
    *
    * @return A list of {@link UserGroup} objects.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readUserData,
         defaults={ExecutionPermission.Default.ALL})
   UserGroups getAllUserGroups(UserGroupQuery query);

   /**
    * Retrieves all process instances satisfying the criteria specified in the
    * provided query.
    *
    * @param query the process instance query.
    *
    * @return a List of ProcessInstance objects.
    *
    * @throws IllegalOperationException if no attributeName
    *     (XPath) is specified in a DataFilter for queries on a structured data
    * @throws IllegalOperationException if attributeName
    *     (XPath) is specified in a DataFilter for queries on a non-structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) specified in a DataFilter contains an invalid XPath
    * @throws IllegalOperationException if PerformingOnBehalfOfFilter is used
    *    but activity instance history is disabled.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstances getAllProcessInstances(ProcessInstanceQuery query) throws IllegalOperationException;

   /**
    * Retrieves all activity instances satisfying the criteria specified in the
    * provided query.
    *
    * @param query the activity instance query.
    *
    * @return a List of ActivityInstance objects.
    *
    * @throws IllegalOperationException if no attributeName
    *     (XPath) is specified in a DataFilter for queries on a structured data
    * @throws IllegalOperationException if attributeName
    *     (XPath) is specified in a DataFilter for queries on a non-structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) specified in a DataFilter contains an invalid XPath
    * @throws IllegalOperationException if PerformingOnBehalfOfFilter is used
    *    but activity instance history is disabled.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readActivityInstanceData,
         scope=ExecutionPermission.Scope.activity,
         defer=true,
         defaults={ExecutionPermission.Default.ALL},
         fixed={ExecutionPermission.Default.OWNER})
   ActivityInstances getAllActivityInstances(ActivityInstanceQuery query) throws IllegalOperationException;

   /**
    * Retrieves all log entries satisfying the criteria specified in the
    * provided query.
    *
    * @param query the log entry query.
    *
    * @return a List of LogEntry objects.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.readAuditTrailStatistics)
   LogEntries getAllLogEntries(LogEntryQuery query) throws AccessForbiddenException;

   /**
    * Retrieves the first User satisfying the criteria specified in the
    * provided query.
    *
    * @param query the user query.
    *
    * @return the first matching user.
    *
    * @throws ObjectNotFoundException if no matching user is found.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readUserData,
         defaults={ExecutionPermission.Default.ALL})
   User findFirstUser(UserQuery query)
         throws ObjectNotFoundException;

   /**
    * Retrieves the first UserGroup satisfying the criteria specified in the
    * provided query.
    *
    * @param query the user group query.
    *
    * @return the first matching user group.
    *
    * @throws ObjectNotFoundException if no matching user group is found.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readUserData,
         defaults={ExecutionPermission.Default.ALL})
   UserGroup findFirstUserGroup(UserGroupQuery query)
         throws ObjectNotFoundException;

   /**
    * Retrieves the first ProcessInstance satisfying the criteria specified in the
    * provided query.
    *
    * @param query the process instance query.
    *
    * @return the first matching process instance.
    *
    * @throws ObjectNotFoundException
    *    if no matching process instance is found.
    * @throws IllegalOperationException if no attributeName
    *    (XPath) is specified in a DataFilter for queries on a structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) is specified in a DataFilter for queries on a non-structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) specified in a DataFilter contains an invalid XPath
    * @throws IllegalOperationException if PerformingOnBehalfOfFilter is used
    *    but activity instance history is disabled.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readProcessInstanceData,
         scope=ExecutionPermission.Scope.processDefinition,
         defer=true,
         defaults={ExecutionPermission.Default.ALL})
   ProcessInstance findFirstProcessInstance(ProcessInstanceQuery query)
         throws ObjectNotFoundException, IllegalOperationException;

   /**
    * Retrieves the first ActivityInstance satisfying the criteria specified in the
    * provided query.
    *
    * @param query
    *           the activity instance query.
    *
    * @return the first matching activity instance.
    *
    * @throws ObjectNotFoundException
    *    if no matching activity instance is found.
    * @throws IllegalOperationException if no attributeName
    *    (XPath) is specified in a DataFilter for queries on a structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) is specified in a DataFilter for queries on a non-structured data
    * @throws IllegalOperationException if attributeName
    *    (XPath) specified in a DataFilter contains an invalid XPath
    * @throws IllegalOperationException if PerformingOnBehalfOfFilter is used
    *    but activity instance history is disabled.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readActivityInstanceData,
         scope=ExecutionPermission.Scope.activity,
         defer=true,
         defaults={ExecutionPermission.Default.ALL},
         fixed={ExecutionPermission.Default.OWNER})
   ActivityInstance findFirstActivityInstance(ActivityInstanceQuery query)
         throws ObjectNotFoundException, IllegalOperationException;

   /**
    * Retrieves the first LogEntry satisfying the criteria specified in the
    * provided query.
    *
    * @param query the log entry query.
    *
    * @return the first matching log entry.
    *
    * @throws ObjectNotFoundException if no matching log entry is found.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.readAuditTrailStatistics)
   LogEntry findFirstLogEntry(LogEntryQuery query)
         throws ObjectNotFoundException;

   /**
    * Returns all performed activity instances for the specified process instance.
    *
    * @param processInstanceOID the OID of the process instance from where we retrieve the audit trail.
    *
    * @return a List of {@link ActivityInstance} objects.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readActivityInstanceData,
         scope=ExecutionPermission.Scope.activity,
         defer=true,
         defaults={ExecutionPermission.Default.ALL},
         fixed={ExecutionPermission.Default.OWNER})
   List<ActivityInstance> getAuditTrail(long processInstanceOID)
         throws ObjectNotFoundException;

   /**
    * Retrieves the list of model descriptions for all deployed models.
    *
    * @return a List of {@link DeployedModelDescription} objects.
    * @deprecated Use {@link Models getModels(DeployedModelQuery.findAll())}.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   List<DeployedModelDescription> getAllModelDescriptions();

   /**
    * Retrieves the list of model descriptions for all alive models. Whereby alive models
    * are models with non-completed and non-aborted processes plus the
    * active model.
    *
    * @return a List of {@link DeployedModelDescription} objects.
    * @deprecated Use {@link Models getModels(DeployedModelQuery.findInState(DeployedModelQuery.ALIVE))}.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   List<DeployedModelDescription> getAllAliveModelDescriptions();

   /**
    * Retrieves the current active model description.
    *
    * @return the description of the active model.
    *
    * @throws ObjectNotFoundException if there is no active model.
    * @deprecated This method returns the description of the active model with the highest priority.
    *   Use {@link Models getModels(DeployedModelQuery.findActive())} to retrieve all active models.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   DeployedModelDescription getActiveModelDescription()
         throws ObjectNotFoundException;

   /**
    * Retrieves the model descriptions satisfying the criteria specified in the provided query.
    *
    * @param query the deployed model query.
    *
    * @return a List of DeployedModelDescription objects.
    */
   /**
    * Retrieves the model descriptions satisfying the criteria specified in the provided query.
    *
    * @param query the deployed model query.
    *
    * @return a List of DeployedModelDescription objects.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   Models getModels(DeployedModelQuery query);

   /*@ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   List<Deployment> getDeployments(Calendar from, Calendar upto);*/

   /**
    * Retrieves the description of the specified model.
    *
    * @param modelOID the oid of the model to retrieve.
    *
    * @return the description of the specified model.
    *
    * @throws ObjectNotFoundException if there is no model with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   DeployedModelDescription getModelDescription(long modelOID)
         throws ObjectNotFoundException;

   /**
    * Determines if the model was redeployed, i.e. if a more recent revision than the
    * provided one is available.
    *
    * @param modelOid The OID of the model to be checked..
    * @param revision The currently retrieved revision of the model.
    * @return <code>true</code> if a more recent revision of the model is available,
    *   <code>false</code> if not.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   boolean wasRedeployed(long modelOid, int revision);

   /**
    * Retrieves the specified model.
    *
    * @param modelOID the oid of the model to retrieve.
    *
    * @return the specified model.
    *
    * @throws ObjectNotFoundException if there is no model with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   DeployedModel getModel(long modelOID)
         throws ObjectNotFoundException;

   /**
    * Retrieves the specified model.
    *
    * @param modelOID the oid of the model to retrieve.
    * @param computeAliveness whether the aliveness of the model should be computed or not
    *
    * @return the specified model.
    *
    * @throws ObjectNotFoundException if there is no model with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   DeployedModel getModel(long modelOID, boolean computeAliveness)
         throws ObjectNotFoundException;

   /**
    * Retrieves the current active model.
    *
    * @return the active model.
    *
    * @throws ObjectNotFoundException if there is no active model.
    * @deprecated This method returns the active model with the highest priority.
    *   Use {@link Models getModels(DeployedModelQuery.findActive())} to retrieve all active models.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   DeployedModel getActiveModel()
         throws ObjectNotFoundException;

   /**
    * Retrieves the XML representation of the specified model.
    *
    * @param modelOID the oid of the model to retrieve.
    *
    * @return A string containing the XML representation of the model.
    *
    * @throws ObjectNotFoundException if there is no model with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   String getModelAsXML(long modelOID)
         throws ObjectNotFoundException;

   /**
    * Retrieves all the process definitions contained in the specified model.
    *
    * @param modelOID the oid of the model.
    *
    * @return a List of {@link ProcessDefinition} objects.
    *
    * @throws ObjectNotFoundException if there is no model with the specified OID.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   List<ProcessDefinition> getAllProcessDefinitions(long modelOID)
         throws ObjectNotFoundException;

   /**
    * Retrieves a process definition from the specified model.
    *
    * @param modelOID   the oid of the model.
    * @param id         the id of the process definition.
    *
    * @return the process definition.
    *
    * @throws ObjectNotFoundException
    *       if there is no model with the specified OID or there is no process
    *       definition with the specified id in the model.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   ProcessDefinition getProcessDefinition(long modelOID, String id)
         throws ObjectNotFoundException;

   /**
    * Retrieves all process definitions for the active model.
    *
    * @return a List of {@link ProcessDefinition} objects.
    *
    * @throws ObjectNotFoundException if there is no active model.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   List<ProcessDefinition> getAllProcessDefinitions()
         throws ObjectNotFoundException;

   /**
    * Retrieves the specified process definition from the active model.
    *
    * @param id the id of the process definition.
    *
    * @return the process definition.
    *
    * @throws ObjectNotFoundException if there is no active model or if the active model
    *         does not contain the requested process definition.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   ProcessDefinition getProcessDefinition(String id)
         throws ObjectNotFoundException;

   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   ProcessDefinitions getProcessDefinitions(ProcessDefinitionQuery query);

   /**
    * Retrieves all data satisfying the criteria specified in the
    * provided query.
    *
    * @param query The DataQuery.
    * @return A list of Data objects.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   DataQueryResult getAllData(DataQuery query);

   /**
    * Retrieves all participants defined in the specified model.
    *
    * @param modelOID the oid of the model.
    *
    * @return a List of {@link Participant} objects.
    *
    * @throws ObjectNotFoundException if there is no model with the specified oid.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   List<Participant> getAllParticipants(long modelOID)
         throws ObjectNotFoundException;

   /**
    * Retrieves a participant from a specified model.
    *
    * @param modelOID   the oid of the model.
    * @param id         the id of the participant.
    *
    * @return the participant.
    *
    * @throws ObjectNotFoundException
    *       if there is no model with the specified oid, or the model does not
    *       contains the requested participant.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   Participant getParticipant(long modelOID, String id)
         throws ObjectNotFoundException;

   /**
    * Retrieves all the participants from the active model.
    *
    * @return a List of {@link Participant} objects.
    *
    * @throws ObjectNotFoundException if there is no active model.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   List<Participant> getAllParticipants()
         throws ObjectNotFoundException;

   /**
    * Retrieves a specific participant from the active model.
    *
    * @param id the id of the participant.
    *
    * @return the participant.
    *
    * @throws ObjectNotFoundException
    *       if there is no active model, or if the active model does not contain
    *       the requested participant.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   Participant getParticipant(String id)
         throws ObjectNotFoundException;

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
    * Retrieves XSD schema of the specified type declaration serialized
    * into a byte[].
    *
    * @param modelOID           the oid of the model.
    * @param typeDeclarationId  the id of the type declaration.
    * @return XSD schema of this type declaration
    * @throws ObjectNotFoundException
    *       if there is no active model, or if the active model does not contain the
    *       requested type declaration.
    */
   @ExecutionPermission(
         id=ExecutionPermission.Id.readModelData,
         defaults={ExecutionPermission.Default.ALL})
   byte[] getSchemaDefinition(long modelOID, String typeDeclarationId)
         throws ObjectNotFoundException;

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
    *           the parent department.
    * @param organization
    *           the organization to which the retrieved departments are assigned.
    * @return the list of departments. The list can be empty if no departments are
    *         matching the search criteria.
    * @throws ObjectNotFoundException
    *            if either the parent or the organization could not be resolved.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.readDepartments,
         defaults={ExecutionPermission.Default.ALL})
   List<Department> findAllDepartments(DepartmentInfo parent, OrganizationInfo organization)
         throws ObjectNotFoundException;

   /**
    * Searches for a department having the specified id in the scope defined by the
    * parent department for the given organization.
    *
    * @param parent the search scope. It can be null, in which case the search scope
    *       is the top level.
    * @param id the id of the department. Must not be null or empty.
    * @return the department having the specified id.
    * @throws ObjectNotFoundException
    *       if the parent could not be resolved or if the specified id is null or empty
    *       or if there is no department with the specified id in the parent scope.
    */
/*   @ExecutionPermission(id=ExecutionPermission.Id.readDepartments,
         defaults={ExecutionPermission.Default.ALL})
   Department findDepartment(DepartmentInfo parent, String id)
         throws ObjectNotFoundException;*/

   /**
    * Searches for a department having the specified id in the scope defined by the parent
    * department. On synchronization with external repository the specified department
    * will be created in audit trail if it is not already present there but exists in
    * external repository. If the department exists in audit trail it will be updated on
    * synchronization if there are any changes.
    *
    * @param parent
    *           the search scope. It can be null, in which case the search scope is the
    *           top level.
    * @param id
    *           the id of the department. Must not be null or empty.
    * @param organization
    *           the organization to which the retrieved departments are assigned.
    * @return the department having the specified id.
    * @throws ObjectNotFoundException
    *            if the parent could not be resolved or if the specified id is null or
    *            empty or if there is no department with the specified id in the parent
    *            scope.
    */
   @ExecutionPermission(id=ExecutionPermission.Id.readDepartments,
         defaults={ExecutionPermission.Default.ALL})
   Department findDepartment(DepartmentInfo parent, String id, OrganizationInfo info)
         throws ObjectNotFoundException;

   /**
    * Retrieves the first document satisfying the criteria specified in the
    * provided query.
    *
    * @param query the document query.
    *
    * @return the first matching document.
    *
    * @throws ObjectNotFoundException if no matching document is found.
    */
   Document findFirstDocument(DocumentQuery query) throws ObjectNotFoundException;

   /**
    * Retrieves all documents satisfying the criteria specified in the provided query.
    *
    * @param query the document query.
    *
    * @return a List of Document objects.
    * @deprecated since 8.0 use {@link DocumentManagementService#findDocuments(DocumentQuery)}.
    */
   @Deprecated
   Documents getAllDocuments(DocumentQuery query);

   /**
    * Retrieves preferences from the given scope.
    *
    * @param scope the scope from which the preferences are to be retrieved from.
    * @param moduleId the moduleId of the preferences.
    * @param preferencesId the id of the preferences.
    * @return a preferences object.
    *
    * @throws PublicException if <tt>scope</tt> is null.
    */
   Preferences getPreferences(PreferenceScope scope, String moduleId, String preferencesId);

   /**
    * Retrieves preferences satisfying the criteria specified in the provided query.
    *
    *
    * @param preferenceQuery the preference query.
    * @return a list of preferences.
    *
    * @throws PublicException if querying is not supported for the specified PreferenceScope.
    * @throws UnsupportedOperationException if the PreferenceQuery contains unsupported terms or operations.
    * @throws InvalidArgumentException if <tt>preferencesQuery</tt> is null.
    */
   List<Preferences> getAllPreferences(PreferenceQuery preferenceQuery);

   /**
    * Retrieves the information about the deployed runtime environment (e.g. version information).
    *
    * @return the runtime environment information.
    */
   /* No execution permission needed */
   RuntimeEnvironmentInfo getRuntimeEnvironmentInfo();
}
