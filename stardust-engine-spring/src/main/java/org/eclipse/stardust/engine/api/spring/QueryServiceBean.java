/*
 * Generated from 
 */
package org.eclipse.stardust.engine.api.spring;

/**
 * to set {@link
 * org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties#DS_NAME_READ_ONLY
 * SessionProperties.DS_NAME_READ_ONLY}
 * used in {@link org.eclipse.stardust.engine.core.runtime.beans.DataValueBean
 * DataValueBean}
 *
 */
public class QueryServiceBean extends org.eclipse.stardust.engine.api.spring.AbstractSpringServiceBean implements IQueryService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getUsersCount(org.eclipse.stardust.engine.api.query.UserQuery query)
    */
   public long getUsersCount(
         org.eclipse.stardust.engine.api.query.UserQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getUsersCount(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getUserGroupsCount(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
    */
   public long
         getUserGroupsCount(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getUserGroupsCount(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessInstancesCount(org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
    */
   public long
         getProcessInstancesCount(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getProcessInstancesCount(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActivityInstancesCount(org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
    */
   public long
         getActivityInstancesCount(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getActivityInstancesCount(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getLogEntriesCount(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
    */
   public long
         getLogEntriesCount(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getLogEntriesCount(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllUsers(org.eclipse.stardust.engine.api.query.UserQuery query)
    */
   public org.eclipse.stardust.engine.api.query.Users
         getAllUsers(org.eclipse.stardust.engine.api.query.UserQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllUsers(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllUserGroups(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
    */
   public org.eclipse.stardust.engine.api.query.UserGroups
         getAllUserGroups(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllUserGroups(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessInstances(org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
    */
   public org.eclipse.stardust.engine.api.query.ProcessInstances
         getAllProcessInstances(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllProcessInstances(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllActivityInstances(org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
    */
   public org.eclipse.stardust.engine.api.query.ActivityInstances
         getAllActivityInstances(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllActivityInstances(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllLogEntries(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
    */
   public org.eclipse.stardust.engine.api.query.LogEntries
         getAllLogEntries(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
         throws org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllLogEntries(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstUser(org.eclipse.stardust.engine.api.query.UserQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         findFirstUser(org.eclipse.stardust.engine.api.query.UserQuery query)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).findFirstUser(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstUserGroup(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         findFirstUserGroup(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).findFirstUserGroup(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstProcessInstance(org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         findFirstProcessInstance(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).findFirstProcessInstance(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstActivityInstance(org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         findFirstActivityInstance(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).findFirstActivityInstance(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstLogEntry(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.LogEntry
         findFirstLogEntry(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).findFirstLogEntry(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAuditTrail(long processInstanceOID)
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ActivityInstance>
         getAuditTrail(long processInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAuditTrail(processInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllBusinessObjects(org.eclipse.stardust.engine.api.query.BusinessObjectQuery query)
    */
   public org.eclipse.stardust.engine.api.query.BusinessObjects
         getAllBusinessObjects(
         org.eclipse.stardust.engine.api.query.BusinessObjectQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllBusinessObjects(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllModelDescriptions()
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.DeployedModelDescription>
         getAllModelDescriptions()
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllModelDescriptions();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllAliveModelDescriptions()
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.DeployedModelDescription>
         getAllAliveModelDescriptions()
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllAliveModelDescriptions();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActiveModelDescription()
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModelDescription
         getActiveModelDescription()
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getActiveModelDescription();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModels(org.eclipse.stardust.engine.api.query.DeployedModelQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.Models
         getModels(org.eclipse.stardust.engine.api.query.DeployedModelQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getModels(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModelDescription(long modelOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModelDescription
         getModelDescription(long modelOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getModelDescription(modelOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#wasRedeployed(long modelOid, int revision)
    */
   public boolean wasRedeployed(long modelOid, int revision)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).wasRedeployed(modelOid, revision);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModel(long modelOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModel getModel(
         long modelOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getModel(modelOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModel(long modelOID, boolean computeAliveness)
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModel getModel(
         long modelOID, boolean computeAliveness)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getModel(modelOID, computeAliveness);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActiveModel()
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModel getActiveModel()
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getActiveModel();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModelAsXML(long modelOID)
    */
   public java.lang.String getModelAsXML(long modelOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getModelAsXML(modelOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessDefinitions(long modelOID)
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getAllProcessDefinitions(long modelOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllProcessDefinitions(modelOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinition(long modelOID, java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.model.ProcessDefinition
         getProcessDefinition(long modelOID, java.lang.String id)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getProcessDefinition(modelOID, id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessDefinitions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getAllProcessDefinitions()
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllProcessDefinitions();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinition(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.model.ProcessDefinition
         getProcessDefinition(java.lang.String id)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getProcessDefinition(id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinitions(org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessDefinitions
         getProcessDefinitions(
         org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getProcessDefinitions(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllData(org.eclipse.stardust.engine.api.query.DataQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.DataQueryResult
         getAllData(org.eclipse.stardust.engine.api.query.DataQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllData(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllParticipants(long modelOID)
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.Participant>
         getAllParticipants(long modelOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllParticipants(modelOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getParticipant(long modelOID, java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.model.Participant getParticipant(
         long modelOID, java.lang.String id)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getParticipant(modelOID, id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllParticipants()
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.Participant>
         getAllParticipants()
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllParticipants();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getParticipant(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.model.Participant
         getParticipant(java.lang.String id)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getParticipant(id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getPermissions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions()
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getPermissions();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getSchemaDefinition(long modelOID, java.lang.String typeDeclarationId)
    */
   public byte[] getSchemaDefinition(
         long modelOID, java.lang.String typeDeclarationId)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getSchemaDefinition(modelOID, typeDeclarationId);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findAllDepartments(org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Department>
         findAllDepartments(
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
         org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).findAllDepartments(parent, organization);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findDepartment(org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, java.lang.String id, org.eclipse.stardust.engine.api.model.OrganizationInfo info)
    */
   public org.eclipse.stardust.engine.api.runtime.Department
         findDepartment(
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, java.lang.String
         id, org.eclipse.stardust.engine.api.model.OrganizationInfo info)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).findDepartment(parent, id, info);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstDocument(org.eclipse.stardust.engine.api.query.DocumentQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         findFirstDocument(org.eclipse.stardust.engine.api.query.DocumentQuery query)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).findFirstDocument(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllDocuments(org.eclipse.stardust.engine.api.query.DocumentQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.Documents
         getAllDocuments(org.eclipse.stardust.engine.api.query.DocumentQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllDocuments(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getPreferences(org.eclipse.stardust.engine.core.preferences.PreferenceScope scope, java.lang.String moduleId, java.lang.String preferencesId)
    */
   public org.eclipse.stardust.engine.core.preferences.Preferences
         getPreferences(
         org.eclipse.stardust.engine.core.preferences.PreferenceScope scope,
         java.lang.String moduleId, java.lang.String preferencesId)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getPreferences(scope, moduleId, preferencesId);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllPreferences(org.eclipse.stardust.engine.api.query.PreferenceQuery preferenceQuery)
    */
   public
         java.util.List<org.eclipse.stardust.engine.core.preferences.Preferences>
         getAllPreferences(
         org.eclipse.stardust.engine.api.query.PreferenceQuery preferenceQuery)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllPreferences(preferenceQuery);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getRuntimeEnvironmentInfo()
    */
   public org.eclipse.stardust.engine.api.runtime.RuntimeEnvironmentInfo
         getRuntimeEnvironmentInfo()
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getRuntimeEnvironmentInfo();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getResourceBundle(java.lang.String moduleId, java.lang.String bundleName, java.util.Locale locale)
    */
   public org.eclipse.stardust.engine.api.runtime.ResourceBundle
         getResourceBundle(
         java.lang.String moduleId, java.lang.String bundleName, java.util.Locale locale)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getResourceBundle(moduleId, bundleName, locale);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getRuntimeArtifact(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.RuntimeArtifact
         getRuntimeArtifact(long oid)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getRuntimeArtifact(oid);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getRuntimeArtifacts(org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery query)
    */
   public org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts
         getRuntimeArtifacts(
         org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery query)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getRuntimeArtifacts(query);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessInstanceLinkType(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstanceLinkType
         getProcessInstanceLinkType(java.lang.String id)
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getProcessInstanceLinkType(id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessInstanceLinkTypes()
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ProcessInstanceLinkType>
         getAllProcessInstanceLinkTypes()
   {
      return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            serviceProxy).getAllProcessInstanceLinkTypes();
   }

	public QueryServiceBean()
	{
      super(org.eclipse.stardust.engine.api.runtime.QueryService.class,
            org.eclipse.stardust.engine.core.runtime.beans.QueryServiceImpl.class);
	}
}