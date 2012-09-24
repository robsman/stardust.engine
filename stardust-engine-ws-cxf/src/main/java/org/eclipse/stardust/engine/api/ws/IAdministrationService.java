/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * Provides administration services for the CARNOT runtime environment.
 * The functionality includes the following tasks:
 * manage the workflow models (deploy, modify or delete)
 * recover the runtime environment or single workflow objects
 * terminate running process instances
 * manage the life cycle management of CARNOT daemons
 * 
 * An administration service always operates against an audit trail database.
 * The administration service requires that the user performing tasks has been
 * assigned to the predefined role Administrator.
 * 
 *
 * This class was generated by Apache CXF 2.6.1
 * 2012-09-24T09:40:00.914+02:00
 * Generated source version: 2.6.1
 * 
 */
@WebService(targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "IAdministrationService")
@XmlSeeAlso({ObjectFactory.class})
public interface IAdministrationService {

    /**
     * Retrieves information on the current user.
     * 
     */
    @WebResult(name = "user", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getSessionUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetSessionUser")
    @WebMethod(action = "getSessionUser")
    @ResponseWrapper(localName = "getSessionUserResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetSessionUserResponse")
    public org.eclipse.stardust.engine.api.ws.UserXto getSessionUser() throws BpmFault;

    /**
     * Returns the password rules.
     * 
     */
    @WebResult(name = "passwordRules", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getPasswordRules", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPasswordRules")
    @WebMethod(action = "getPasswordRules")
    @ResponseWrapper(localName = "getPasswordRulesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetPasswordRulesResponse")
    public org.eclipse.stardust.engine.api.ws.PasswordRulesXto getPasswordRules() throws BpmFault;

    /**
     * Retrieves merged configuration variables from all models matching the specified modelIds.
     * 
     */
    @WebResult(name = "ConfigurationVariablesList", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getConfigurationVariables", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetConfigurationVariables")
    @WebMethod(action = "getConfigurationVariables")
    @ResponseWrapper(localName = "getConfigurationVariablesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetConfigurationVariablesResponse")
    public org.eclipse.stardust.engine.api.ws.ConfigurationVariablesListXto getConfigurationVariables(
        @WebParam(name = "modelIds", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.StringListXto modelIds,
        @WebParam(name = "modelXml", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.XmlValueXto modelXml
    ) throws BpmFault;

    /**
     * Forces an activity instance to be suspended.
     * 
     */
    @WebResult(name = "activityInstance", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "forceSuspend", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ForceSuspend")
    @WebMethod(action = "forceSuspend")
    @ResponseWrapper(localName = "forceSuspendResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ForceSuspendResponse")
    public org.eclipse.stardust.engine.api.ws.ActivityInstanceXto forceSuspend(
        @WebParam(name = "activityOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long activityOid
    ) throws BpmFault;

    /**
     * Deletes the specified model.
     * 
     */
    @WebResult(name = "deploymentInfo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "deleteModel", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.DeleteModel")
    @WebMethod(action = "deleteModel")
    @ResponseWrapper(localName = "deleteModelResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.DeleteModelResponse")
    public org.eclipse.stardust.engine.api.ws.DeploymentInfoXto deleteModel(
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        int modelOid
    ) throws BpmFault;

    /**
     * Deletes process instances from the audit trail.
     * 
     */
    @RequestWrapper(localName = "deleteProcesses", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.DeleteProcesses")
    @WebMethod(action = "deleteProcesses")
    @ResponseWrapper(localName = "deleteProcessesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.DeleteProcessesResponse")
    public void deleteProcesses(
        @WebParam(name = "oids", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DeleteProcesses.OidsXto oids
    ) throws BpmFault;

    /**
     * Aborts a process instance disregarding any activities which were or are performed by the process instance.
     * Regularly the process instance to be aborted will be set to the state ABORTING synchronously.
     * The returned ProcessInstance object will already be in that state.
     * Before returning the ProcessInstance object, a asynchronous abortion task is scheduled for it.
     * If the process instance is not yet inserted in the database but needs to be aborted for some reason ( e.g. by abort process event) then the abort operation is optimized to happen completely synchronously.
     * In that case the returned ProcessInstance will already be in state ABORTED.
     * This method also aborts all super process instances disregarding the parameter AbortScope.
     * 
     */
    @WebResult(name = "processInstance", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "abortProcessInstance", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.AbortProcessInstance")
    @WebMethod(action = "abortProcessInstance")
    @ResponseWrapper(localName = "abortProcessInstanceResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.AbortProcessInstanceResponse")
    public org.eclipse.stardust.engine.api.ws.ProcessInstanceXto abortProcessInstance(
        @WebParam(name = "oid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long oid,
        @WebParam(name = "abortScope", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.AbortScopeXto abortScope
    ) throws BpmFault;

    /**
     * Removes all records from the runtime environment making up the audit trail database.
     * 
     */
    @RequestWrapper(localName = "cleanupRuntime", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CleanupRuntime")
    @WebMethod(action = "cleanupRuntime")
    @ResponseWrapper(localName = "cleanupRuntimeResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CleanupRuntimeResponse")
    public void cleanupRuntime(
        @WebParam(name = "keepUsers", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        boolean keepUsers
    ) throws BpmFault;

    /**
     * Starts the specified daemon.
     * 
     */
    @WebResult(name = "deamons", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "startDaemon", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.StartDaemon")
    @WebMethod(action = "startDaemon")
    @ResponseWrapper(localName = "startDaemonResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.StartDaemonResponse")
    public org.eclipse.stardust.engine.api.ws.DaemonsXto startDaemon(
        @WebParam(name = "daemonParameters", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DaemonParametersXto daemonParameters
    ) throws BpmFault;

    /**
     * Recovers the process instances identified by the given list of OIDs and all associated subprocess instances.
     * 
     */
    @RequestWrapper(localName = "recoverProcessInstances", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RecoverProcessInstances")
    @WebMethod(action = "recoverProcessInstances")
    @ResponseWrapper(localName = "recoverProcessInstancesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RecoverProcessInstancesResponse")
    public void recoverProcessInstances(
        @WebParam(name = "oids", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.RecoverProcessInstances.OidsXto oids
    ) throws BpmFault;

    /**
     * Overwrites the specified model.
     * 
     */
    @WebResult(name = "deploymentInfo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "overwriteModel", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.OverwriteModel")
    @WebMethod(action = "overwriteModel")
    @ResponseWrapper(localName = "overwriteModelResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.OverwriteModelResponse")
    public org.eclipse.stardust.engine.api.ws.DeploymentInfoXto overwriteModel(
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        int modelOid,
        @WebParam(name = "validFrom", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.util.Date validFrom,
        @WebParam(name = "validTo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.util.Date validTo,
        @WebParam(name = "comment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String comment,
        @WebParam(name = "disabled", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean disabled,
        @WebParam(name = "ignoreWarnings", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean ignoreWarnings,
        @WebParam(name = "configuration", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String configuration,
        @WebParam(name = "xml", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.XmlValueXto xml
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
     * Retrieves preferences from the given scope.
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
     * Creates a new department.
     * 
     */
    @WebResult(name = "department", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "createDepartment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateDepartment")
    @WebMethod(action = "createDepartment")
    @ResponseWrapper(localName = "createDepartmentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateDepartmentResponse")
    public org.eclipse.stardust.engine.api.ws.DepartmentXto createDepartment(
        @WebParam(name = "id", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String id,
        @WebParam(name = "name", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String name,
        @WebParam(name = "description", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String description,
        @WebParam(name = "parent", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DepartmentInfoXto parent,
        @WebParam(name = "organization", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.OrganizationInfoXto organization
    ) throws BpmFault;

    /**
     * Change the description of a department.
     * 
     */
    @WebResult(name = "department", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "modifyDepartment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ModifyDepartment")
    @WebMethod(action = "modifyDepartment")
    @ResponseWrapper(localName = "modifyDepartmentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ModifyDepartmentResponse")
    public org.eclipse.stardust.engine.api.ws.DepartmentXto modifyDepartment(
        @WebParam(name = "oid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long oid,
        @WebParam(name = "name", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String name,
        @WebParam(name = "description", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String description
    ) throws BpmFault;

    /**
     * Removes all records from the runtime environment making up the audit trail database.
     * 
     */
    @RequestWrapper(localName = "cleanupRuntimeAndModels", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CleanupRuntimeAndModels")
    @WebMethod(action = "cleanupRuntimeAndModels")
    @ResponseWrapper(localName = "cleanupRuntimeAndModelsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CleanupRuntimeAndModelsResponse")
    public void cleanupRuntimeAndModels() throws BpmFault;

    /**
     * Stops the specified daemon.
     * 
     */
    @WebResult(name = "deamons", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "stopDaemon", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.StopDaemon")
    @WebMethod(action = "stopDaemon")
    @ResponseWrapper(localName = "stopDaemonResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.StopDaemonResponse")
    public org.eclipse.stardust.engine.api.ws.DaemonsXto stopDaemon(
        @WebParam(name = "daemonParameters", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DaemonParametersXto daemonParameters
    ) throws BpmFault;

    /**
     * Removes the department having the specified oid, all his children and all user grants associated with the department.
     * 
     */
    @RequestWrapper(localName = "removeDepartment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RemoveDepartment")
    @WebMethod(action = "removeDepartment")
    @ResponseWrapper(localName = "removeDepartmentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RemoveDepartmentResponse")
    public void removeDepartment(
        @WebParam(name = "oid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long oid
    ) throws BpmFault;

    /**
     * Set password rule.
     * 
     */
    @RequestWrapper(localName = "setPasswordRules", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetPasswordRules")
    @WebMethod(action = "setPasswordRules")
    @ResponseWrapper(localName = "setPasswordRulesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetPasswordRulesResponse")
    public void setPasswordRules(
        @WebParam(name = "passwordRules", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.PasswordRulesXto passwordRules
    ) throws BpmFault;

    /**
     * Retrieves permissions that are globally set. For example permissions concerning model deployment, preference saving,
     * 		modifying AuditTrail, managing deamons ect.
     *        
     */
    @WebResult(name = "runtimePermissions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getGlobalPermissions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetGlobalPermissions")
    @WebMethod(action = "getGlobalPermissions")
    @ResponseWrapper(localName = "getGlobalPermissionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetGlobalPermissionsResponse")
    public org.eclipse.stardust.engine.api.ws.RuntimePermissionsXto getGlobalPermissions() throws BpmFault;

    /**
     * Retrieves the department with the given oid.
     * 
     */
    @WebResult(name = "department", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDepartment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDepartment")
    @WebMethod(action = "getDepartment")
    @ResponseWrapper(localName = "getDepartmentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDepartmentResponse")
    public org.eclipse.stardust.engine.api.ws.DepartmentXto getDepartment(
        @WebParam(name = "oid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long oid
    ) throws BpmFault;

    /**
     * Deploys a new model.
     * 
     */
    @WebResult(name = "deploymentInfo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "deployModel", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.DeployModel")
    @WebMethod(action = "deployModel")
    @ResponseWrapper(localName = "deployModelResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.DeployModelResponse")
    public org.eclipse.stardust.engine.api.ws.DeploymentInfoXto deployModel(
        @WebParam(name = "predecessorOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Integer predecessorOid,
        @WebParam(name = "validFrom", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.util.Date validFrom,
        @WebParam(name = "validTo", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.util.Date validTo,
        @WebParam(name = "comment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String comment,
        @WebParam(name = "disabled", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean disabled,
        @WebParam(name = "ignoreWarnings", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean ignoreWarnings,
        @WebParam(name = "configuration", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String configuration,
        @WebParam(name = "xml", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.XmlValueXto xml
    ) throws BpmFault;

    /**
     * Determines key indicators of audit trail health.
     * 
     */
    @WebResult(name = "auditTrailHealthReport", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getAuditTrailHealthReport", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAuditTrailHealthReport")
    @WebMethod(action = "getAuditTrailHealthReport")
    @ResponseWrapper(localName = "getAuditTrailHealthReportResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetAuditTrailHealthReportResponse")
    public org.eclipse.stardust.engine.api.ws.AuditTrailHealthReportXto getAuditTrailHealthReport() throws BpmFault;

    /**
     * Flushes all internal caches, effectively returning the engine to a state just like after it has started.
     * 
     */
    @RequestWrapper(localName = "flushCaches", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FlushCaches")
    @WebMethod(action = "flushCaches")
    @ResponseWrapper(localName = "flushCachesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.FlushCachesResponse")
    public void flushCaches() throws BpmFault;

    /**
     * Recovers the complete CARNOT runtime environment.
     * 
     */
    @RequestWrapper(localName = "recoverRuntimeEnvironment", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RecoverRuntimeEnvironment")
    @WebMethod(action = "recoverRuntimeEnvironment")
    @ResponseWrapper(localName = "recoverRuntimeEnvironmentResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.RecoverRuntimeEnvironmentResponse")
    public void recoverRuntimeEnvironment() throws BpmFault;

    /**
     * Logs an audit trail event of type LogCode.ADMINISTRATION.
     * 
     */
    @RequestWrapper(localName = "writeLogEntry", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.WriteLogEntry")
    @WebMethod(action = "writeLogEntry")
    @ResponseWrapper(localName = "writeLogEntryResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.WriteLogEntryResponse")
    public void writeLogEntry(
        @WebParam(name = "logType", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.LogTypeXto logType,
        @WebParam(name = "activityOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long activityOid,
        @WebParam(name = "processOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long processOid,
        @WebParam(name = "message", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String message
    ) throws BpmFault;

    /**
     * Starts a process from a specified model.
     * 
     */
    @WebResult(name = "processInstance", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "startProcessForModel", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.StartProcessForModel")
    @WebMethod(action = "startProcessForModel")
    @ResponseWrapper(localName = "startProcessForModelResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.StartProcessForModelResponse")
    public org.eclipse.stardust.engine.api.ws.ProcessInstanceXto startProcessForModel(
        @WebParam(name = "modelOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long modelOid,
        @WebParam(name = "processId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String processId,
        @WebParam(name = "parameters", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.ParametersXto parameters,
        @WebParam(name = "startSynchronously", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean startSynchronously,
        @WebParam(name = "attachments", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.InputDocumentsXto attachments
    ) throws BpmFault;

    /**
     * Retrieves the specified daemon status.
     * 
     */
    @WebResult(name = "deamons", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getDaemonStatus", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDaemonStatus")
    @WebMethod(action = "getDaemonStatus")
    @ResponseWrapper(localName = "getDaemonStatusResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetDaemonStatusResponse")
    public org.eclipse.stardust.engine.api.ws.DaemonsXto getDaemonStatus(
        @WebParam(name = "daemonParameters", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.DaemonParametersXto daemonParameters
    ) throws BpmFault;

    /**
     * Saves changes to configuration variables values.
     * 
     */
    @WebResult(name = "modelReconfigurationInfoList", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "saveConfigurationVariables", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SaveConfigurationVariables")
    @WebMethod(action = "saveConfigurationVariables")
    @ResponseWrapper(localName = "saveConfigurationVariablesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SaveConfigurationVariablesResponse")
    public org.eclipse.stardust.engine.api.ws.ModelReconfigurationInfoListXto saveConfigurationVariables(
        @WebParam(name = "configurationVariables", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.ConfigurationVariablesXto configurationVariables,
        @WebParam(name = "force", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        boolean force
    ) throws BpmFault;

    /**
     * Saves the changed Permissions. Use getGlobalPermissions to retrieve currently valid global permissions first.
     * Permissions with null or empty lists set as grants will be reset to their internal default.
     * Changed grants are validated against the currently active model for existing model participants.
     * 
     */
    @RequestWrapper(localName = "setGlobalPermissions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetGlobalPermissions")
    @WebMethod(action = "setGlobalPermissions")
    @ResponseWrapper(localName = "setGlobalPermissionsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetGlobalPermissionsResponse")
    public void setGlobalPermissions(
        @WebParam(name = "runtimePermissions", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.RuntimePermissionsXto runtimePermissions
    ) throws BpmFault;

    /**
     * Forces the completion of a non-interactive activity instance. Optionally out data mappings may be provided.
     */
    @WebResult(name = "activityInstance", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "forceCompletion", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ForceCompletion")
    @WebMethod(action = "forceCompletion")
    @ResponseWrapper(localName = "forceCompletionResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ForceCompletionResponse")
    public org.eclipse.stardust.engine.api.ws.ActivityInstanceXto forceCompletion(
        @WebParam(name = "activityOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long activityOid,
        @WebParam(name = "outDataValues", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.ParametersXto outDataValues
    ) throws BpmFault;

    /**
     * Saves a complete list of preferences to the preference store.
     * 
     */
    @RequestWrapper(localName = "savePreferences", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SavePreferences")
    @WebMethod(action = "savePreferences")
    @ResponseWrapper(localName = "savePreferencesResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SavePreferencesResponse")
    public void savePreferences(
        @WebParam(name = "preferenceList", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.PreferencesListXto preferenceList
    ) throws BpmFault;

    /**
     * Changes the process instance priority.
     * 
     */
    @WebResult(name = "processInstance", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "setProcessInstancePriority", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetProcessInstancePriority")
    @WebMethod(action = "setProcessInstancePriority")
    @ResponseWrapper(localName = "setProcessInstancePriorityResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.SetProcessInstancePriorityResponse")
    public org.eclipse.stardust.engine.api.ws.ProcessInstanceXto setProcessInstancePriority(
        @WebParam(name = "oid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long oid,
        @WebParam(name = "priority", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        int priority,
        @WebParam(name = "propagateToSubProcesses", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean propagateToSubProcesses
    ) throws BpmFault;
}
