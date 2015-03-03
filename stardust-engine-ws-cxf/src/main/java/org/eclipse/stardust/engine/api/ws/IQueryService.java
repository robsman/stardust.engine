package org.eclipse.stardust.engine.api.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * Provides information services for the CARNOT runtime environment.
 * 	  The functionality includes the following tasks:
 * 	  retrieve or counts elements in the audit trail (users, process instance, activity instances or log events),
 * 	  retrieve deployed models from the audit trails,
 * 	  retrieve participants from the models.
 * 	  A Query service always operates against an audit trail database.
 * 	  
 *
 * This class was generated by Apache CXF 2.6.1
 * 2015-02-24T10:40:56.819+01:00
 * Generated source version: 2.6.1
 * 
 */
@WebService(targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "IQueryService")
@XmlSeeAlso({ObjectFactory.class})
public interface IQueryService {

    /**
     * documentation
     * 
     */
    @WebResult(name = "preferencesList", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findPreferences", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindPreferences")
    @WebMethod(action = "findPreferences")
    @ResponseWrapper(localName = "findPreferencesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindPreferencesResponse")
    public org.eclipse.stardust.engine.api.ws.PreferencesListXto findPreferences(
        @WebParam(name = "preferenceQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.PreferenceQueryXto preferenceQuery
    ) throws BpmFault;

    /**
     * Retrieves all process instances satisfying the criteria specified in the provided query.
     * 
     */
    @WebResult(name = "processDefinitions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findProcessDefinitions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindProcessDefinitions")
    @WebMethod(action = "findProcessDefinitions")
    @ResponseWrapper(localName = "findProcessDefinitionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindProcessDefinitionsResponse")
    public org.eclipse.stardust.engine.api.ws.ProcessDefinitionQueryResultXto findProcessDefinitions(
        @WebParam(name = "query", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.ProcessDefinitionQueryXto query
    ) throws BpmFault;

    /**
     * Retrieves the list of model descriptions for all alive models.
     * 
     */
    @WebResult(name = "modelDescriptions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getAllAliveModelDescriptions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllAliveModelDescriptions")
    @WebMethod(action = "getAllAliveModelDescriptions")
    @ResponseWrapper(localName = "getAllAliveModelDescriptionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllAliveModelDescriptionsResponse")
    public org.eclipse.stardust.engine.api.ws.ModelDescriptionsXto getAllAliveModelDescriptions() throws BpmFault;

    /**
     * Retrieves the XML representation of the specified model.
     * 
     */
    @WebResult(name = "xml", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getModelAsXML", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetModelAsXML")
    @WebMethod(action = "getModelAsXML")
    @ResponseWrapper(localName = "getModelAsXMLResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetModelAsXMLResponse")
    public org.eclipse.stardust.engine.api.ws.XmlValueXto getModelAsXML(
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long modelOid
    ) throws BpmFault;

    /**
     * Retrieves all variable definitions satisfying the criteria specified in the provided query.
     * 
     */
    @WebResult(name = "queryResult", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findVariableDefinitions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindVariableDefinitions")
    @WebMethod(action = "findVariableDefinitions")
    @ResponseWrapper(localName = "findVariableDefinitionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindVariableDefinitionsResponse")
    public org.eclipse.stardust.engine.api.ws.VariableDefinitionQueryResultXto findVariableDefinitions(
        @WebParam(name = "variableDefinitionQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.VariableDefinitionQueryXto variableDefinitionQuery
    ) throws BpmFault;

    /**
     * Retrieves all users satisfying the criteria specified in the provided query.
     * 
     */
    @WebResult(name = "users", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findUsers", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindUsers")
    @WebMethod(action = "findUsers")
    @ResponseWrapper(localName = "findUsersResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindUsersResponse")
    public org.eclipse.stardust.engine.api.ws.UserQueryResultXto findUsers(
        @WebParam(name = "userQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.UserQueryXto userQuery
    ) throws BpmFault;

    /**
     * Retrieves the list of model descriptions for all deployed models.
     * 
     */
    @WebResult(name = "modelDescriptions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getAllModelDescriptions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllModelDescriptions")
    @WebMethod(action = "getAllModelDescriptions")
    @ResponseWrapper(localName = "getAllModelDescriptionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllModelDescriptionsResponse")
    public org.eclipse.stardust.engine.api.ws.ModelDescriptionsXto getAllModelDescriptions() throws BpmFault;

    /**
     * Retrieves XSD schema of the specified type declaration.
     * 
     */
    @WebResult(name = "schema", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getSchemaDefinition", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetSchemaDefinition")
    @WebMethod(action = "getSchemaDefinition")
    @ResponseWrapper(localName = "getSchemaDefinitionResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetSchemaDefinitionResponse")
    public org.eclipse.stardust.engine.api.ws.XmlValueXto getSchemaDefinition(
        @WebParam(name = "type", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        javax.xml.namespace.QName type,
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long modelOid
    ) throws BpmFault;

    /**
     * documentation
     * 
     */
    @WebResult(name = "queryResult", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findModels", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindModels")
    @WebMethod(action = "findModels")
    @ResponseWrapper(localName = "findModelsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindModelsResponse")
    public org.eclipse.stardust.engine.api.ws.ModelsQueryResultXto findModels(
        @WebParam(name = "deployedModelQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.DeployedModelQueryXto deployedModelQuery
    ) throws BpmFault;

    /**
     * Retrieves all the departments satisfying the search criteria.
     * 
     */
    @WebResult(name = "departments", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findAllDepartments", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindAllDepartments")
    @WebMethod(action = "findAllDepartments")
    @ResponseWrapper(localName = "findAllDepartmentsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindAllDepartmentsResponse")
    public org.eclipse.stardust.engine.api.ws.DepartmentsXto findAllDepartments(
        @WebParam(name = "parent", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DepartmentInfoXto parent,
        @WebParam(name = "organization", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.OrganizationInfoXto organization
    ) throws BpmFault;

    /**
     * Retrieves all user groups satisfying the criteria specified in the provided query.
     * 
     */
    @WebResult(name = "userGroups", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findUserGroups", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindUserGroups")
    @WebMethod(action = "findUserGroups")
    @ResponseWrapper(localName = "findUserGroupsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindUserGroupsResponse")
    public org.eclipse.stardust.engine.api.ws.UserGroupQueryResultXto findUserGroups(
        @WebParam(name = "userGroupQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.UserGroupQueryXto userGroupQuery
    ) throws BpmFault;

    /**
     * Retrieves a participant from a active or specified model.
     * 
     */
    @WebResult(name = "participant", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getParticipant", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetParticipant")
    @WebMethod(action = "getParticipant")
    @ResponseWrapper(localName = "getParticipantResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetParticipantResponse")
    public org.eclipse.stardust.engine.api.ws.ParticipantXto getParticipant(
        @WebParam(name = "participantId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String participantId,
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long modelOid
    ) throws BpmFault;

    /**
     * Retrieves the specified model.
     * 
     */
    @WebResult(name = "model", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getModel", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetModel")
    @WebMethod(action = "getModel")
    @ResponseWrapper(localName = "getModelResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetModelResponse")
    public org.eclipse.stardust.engine.api.ws.ModelXto getModel(
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long modelOid,
        @WebParam(name = "computeAliveness", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean computeAliveness
    ) throws BpmFault;

    /**
     * Retrieves all permissions the current user has on this service.
     * 
     */
    @WebResult(name = "permissions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getPermissions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPermissions")
    @WebMethod(action = "getPermissions")
    @ResponseWrapper(localName = "getPermissionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPermissionsResponse")
    public org.eclipse.stardust.engine.api.ws.PermissionsXto getPermissions() throws BpmFault;

    /**
     * Retrieves all participants defined in the active or specified model.
     * 
     */
    @WebResult(name = "participants", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getAllParticipants", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllParticipants")
    @WebMethod(action = "getAllParticipants")
    @ResponseWrapper(localName = "getAllParticipantsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllParticipantsResponse")
    public org.eclipse.stardust.engine.api.ws.ParticipantsXto getAllParticipants(
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long modelOid
    ) throws BpmFault;

    /**
     * documentation
     * 
     */
    @WebResult(name = "queryResult", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getAllBusinessObjects", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllBusinessObjects")
    @WebMethod(action = "getAllBusinessObjects")
    @ResponseWrapper(localName = "getAllBusinessObjectsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllBusinessObjectsResponse")
    public org.eclipse.stardust.engine.api.ws.BusinessObjectsXto getAllBusinessObjects(
        @WebParam(name = "businessObjectQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.BusinessObjectQueryXto businessObjectQuery
    ) throws BpmFault;

    /**
     * documentation
     * 
     */
    @WebResult(name = "preferences", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getPreferences", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPreferences")
    @WebMethod(action = "getPreferences")
    @ResponseWrapper(localName = "getPreferencesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPreferencesResponse")
    public org.eclipse.stardust.engine.api.ws.PreferencesXto getPreferences(
        @WebParam(name = "scope", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.PreferenceScopeXto scope,
        @WebParam(name = "moduleId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String moduleId,
        @WebParam(name = "preferencesId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String preferencesId
    ) throws BpmFault;

    /**
     * Gets Documents based on the specified DocumentQuery.
     * 
     */
    @WebResult(name = "documents", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getAllDocuments", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllDocuments")
    @WebMethod(action = "getAllDocuments")
    @ResponseWrapper(localName = "getAllDocumentsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllDocumentsResponse")
    public org.eclipse.stardust.engine.api.ws.DocumentQueryResultXto getAllDocuments(
        @WebParam(name = "query", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.DocumentQueryXto query
    ) throws BpmFault;

    /**
     * Retrieves all log entries satisfying the criteria specified in the provided query.
     * 
     */
    @WebResult(name = "logEntries", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findLogEntries", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindLogEntries")
    @WebMethod(action = "findLogEntries")
    @ResponseWrapper(localName = "findLogEntriesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindLogEntriesResponse")
    public org.eclipse.stardust.engine.api.ws.LogEntryQueryResultXto findLogEntries(
        @WebParam(name = "logEntryQuery", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.LogEntryQueryXto logEntryQuery
    ) throws BpmFault;

    /**
     * Retrieves all activity instances satisfying the criteria specified in the provided query.
     * 
     */
    @WebResult(name = "activityInstances", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findActivities", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindActivities")
    @WebMethod(action = "findActivities")
    @ResponseWrapper(localName = "findActivitiesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindActivitiesResponse")
    public org.eclipse.stardust.engine.api.ws.ActivityQueryResultXto findActivities(
        @WebParam(name = "query", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.ActivityQueryXto query
    ) throws BpmFault;

    /**
     * Retrieves all process definitions for the active or specified model.
     * 
     */
    @WebResult(name = "processDefinitions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getAllProcessDefinitions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllProcessDefinitions")
    @WebMethod(action = "getAllProcessDefinitions")
    @ResponseWrapper(localName = "getAllProcessDefinitionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAllProcessDefinitionsResponse")
    public org.eclipse.stardust.engine.api.ws.ProcessDefinitionsXto getAllProcessDefinitions(
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long modelOid
    ) throws BpmFault;

    /**
     * Retrieves the description of the specified model.
     * 
     */
    @WebResult(name = "modelDescription", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getModelDescription", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetModelDescription")
    @WebMethod(action = "getModelDescription")
    @ResponseWrapper(localName = "getModelDescriptionResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetModelDescriptionResponse")
    public org.eclipse.stardust.engine.api.ws.ModelDescriptionXto getModelDescription(
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long modelOid
    ) throws BpmFault;

    /**
     * Retrieves all process instances satisfying the criteria specified in the provided query.
     * 
     */
    @WebResult(name = "processInstances", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findProcesses", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindProcesses")
    @WebMethod(action = "findProcesses")
    @ResponseWrapper(localName = "findProcessesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindProcessesResponse")
    public org.eclipse.stardust.engine.api.ws.ProcessInstanceQueryResultXto findProcesses(
        @WebParam(name = "query", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.query.ProcessQueryXto query
    ) throws BpmFault;

    /**
     * Searches for a department having the specified id in the scope defined by the parent department.
     * 
     */
    @WebResult(name = "department", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "findDepartment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindDepartment")
    @WebMethod(action = "findDepartment")
    @ResponseWrapper(localName = "findDepartmentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FindDepartmentResponse")
    public org.eclipse.stardust.engine.api.ws.DepartmentXto findDepartment(
        @WebParam(name = "parent", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DepartmentInfoXto parent,
        @WebParam(name = "organization", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.OrganizationInfoXto organization,
        @WebParam(name = "id", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String id
    ) throws BpmFault;

    /**
     * Retrieves a process definition from the active or specified model.
     * 
     */
    @WebResult(name = "processDefinition", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getProcessDefinition", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetProcessDefinition")
    @WebMethod(action = "getProcessDefinition")
    @ResponseWrapper(localName = "getProcessDefinitionResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetProcessDefinitionResponse")
    public org.eclipse.stardust.engine.api.ws.ProcessDefinitionXto getProcessDefinition(
        @WebParam(name = "processId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String processId,
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long modelOid
    ) throws BpmFault;
}
