/*
 * Generated from Revision: 68817 
 */
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.Local;

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
 * @version 68817
 */
@Local
public interface AdministrationService extends org.eclipse.stardust.engine.api.ejb3.beans.Ejb3Service
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setPasswordRules(org.eclipse.stardust.engine.api.runtime.PasswordRules rules)
    */
    public void
         setPasswordRules(
         org.eclipse.stardust.engine.api.runtime.PasswordRules rules,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPasswordRules()
    */
    public org.eclipse.stardust.engine.api.runtime.PasswordRules
         getPasswordRules(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.lang.String model, int predecessorOID)
    */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deployModel(
         java.lang.String model, int predecessorOID,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(java.lang.String model, int modelOID)
    */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         java.lang.String model, int modelOID,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.lang.String model, java.lang.String configuration, int predecessorOID, java.util.Date validFrom, java.util.Date validTo, java.lang.String comment, boolean disabled, boolean ignoreWarnings)
    */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deployModel(
         java.lang.String model, java.lang.String configuration, int predecessorOID,
         java.util.Date validFrom, java.util.Date validTo, java.lang.String comment,
         boolean disabled, boolean ignoreWarnings,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(java.lang.String model, java.lang.String configuration, int modelOID, java.util.Date validFrom, java.util.Date validTo, java.lang.String comment, boolean disabled, boolean ignoreWarnings)
    */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         java.lang.String model, java.lang.String configuration, int modelOID,
         java.util.Date validFrom, java.util.Date validTo, java.lang.String comment,
         boolean disabled, boolean ignoreWarnings,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(org.eclipse.stardust.engine.api.runtime.DeploymentElement deploymentElement, int modelOID, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
    */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         org.eclipse.stardust.engine.api.runtime.DeploymentElement deploymentElement, int
         modelOID, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.util.List deploymentElements, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.DeploymentInfo>
         deployModel(
         java.util.List deploymentElements,
         org.eclipse.stardust.engine.api.runtime.DeploymentOptions options,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setPrimaryImplementation(long interfaceModelOid, java.lang.String processId, java.lang.String implementationModelId, org.eclipse.stardust.engine.api.runtime.LinkingOptions options)
    */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         setPrimaryImplementation(
         long interfaceModelOid, java.lang.String processId, java.lang.String
         implementationModelId, org.eclipse.stardust.engine.api.runtime.LinkingOptions
         options, org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteModel(long modelOID)
    */
    public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deleteModel(
         long modelOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteProcesses(java.util.List piOids)
    */
    public void deleteProcesses(
         java.util.List piOids, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#cleanupRuntime(boolean keepUsers)
    */
    public void cleanupRuntime(
         boolean keepUsers, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#cleanupRuntimeAndModels()
    */
    public void
         cleanupRuntimeAndModels(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProcessInstancePriority(long oid, int priority)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         setProcessInstancePriority(
         long oid, int priority, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProcessInstancePriority(long oid, int priority, boolean propagateToSubProcesses)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         setProcessInstancePriority(
         long oid, int priority, boolean propagateToSubProcesses,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#abortProcessInstance(long oid)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         abortProcessInstance(
         long oid, org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverProcessInstance(long oid)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         recoverProcessInstance(
         long oid, org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverProcessInstances(java.util.List oids)
    */
    public void recoverProcessInstances(
         java.util.List oids, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getDaemon(java.lang.String daemonType, boolean acknowledge)
    */
    public org.eclipse.stardust.engine.api.runtime.Daemon
         getDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#stopDaemon(java.lang.String daemonType, boolean acknowledge)
    */
    public org.eclipse.stardust.engine.api.runtime.Daemon
         stopDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#startDaemon(java.lang.String daemonType, boolean acknowledge)
    */
    public org.eclipse.stardust.engine.api.runtime.Daemon
         startDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAllDaemons(boolean acknowledge)
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Daemon>
         getAllDaemons(
         boolean acknowledge, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAuditTrailHealthReport()
    */
    public org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport
         getAuditTrailHealthReport(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAuditTrailHealthReport(boolean countOnly)
    */
    public org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport
         getAuditTrailHealthReport(
         boolean countOnly, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverRuntimeEnvironment()
    */
    public void
         recoverRuntimeEnvironment(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#startProcess(long modelOID, java.lang.String id, java.util.Map data, boolean synchronously)
    */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         startProcess(
         long modelOID, java.lang.String id, java.util.Map data, boolean synchronously,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#forceCompletion(long activityInstanceOID, java.util.Map accessPoints)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         forceCompletion(
         long activityInstanceOID, java.util.Map accessPoints,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#forceSuspendToDefaultPerformer(long activityInstanceOID)
    */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         forceSuspendToDefaultPerformer(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getUser()
    */
    public org.eclipse.stardust.engine.api.runtime.User
         getUser(org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#flushCaches()
    */
    public void flushCaches(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPermissions()
    */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getProfile(org.eclipse.stardust.engine.api.model.ProfileScope scope)
    */
    public java.util.Map<java.lang.String,?>
         getProfile(
         org.eclipse.stardust.engine.api.model.ProfileScope scope,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProfile(org.eclipse.stardust.engine.api.model.ProfileScope scope, java.util.Map profile)
    */
    public void setProfile(
         org.eclipse.stardust.engine.api.model.ProfileScope scope, java.util.Map profile,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#writeLogEntry(org.eclipse.stardust.engine.api.runtime.LogType logType, org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid, java.lang.String message, java.lang.Throwable throwable)
    */
    public void writeLogEntry(
         org.eclipse.stardust.engine.api.runtime.LogType logType,
         org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
         java.lang.String message, java.lang.Throwable throwable,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#createDepartment(java.lang.String id, java.lang.String name, java.lang.String description, org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
    */
    public org.eclipse.stardust.engine.api.runtime.Department
         createDepartment(
         java.lang.String id, java.lang.String name, java.lang.String description,
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
         org.eclipse.stardust.engine.api.model.OrganizationInfo organization,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getDepartment(long oid)
    */
    public org.eclipse.stardust.engine.api.runtime.Department getDepartment(
         long oid, org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#modifyDepartment(long oid, java.lang.String name, java.lang.String description)
    */
    public org.eclipse.stardust.engine.api.runtime.Department
         modifyDepartment(
         long oid, java.lang.String name, java.lang.String description,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#removeDepartment(long oid)
    */
    public void removeDepartment(
         long oid, org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPreferences(org.eclipse.stardust.engine.core.preferences.PreferenceScope scope, java.lang.String moduleId, java.lang.String preferencesId)
    */
    public org.eclipse.stardust.engine.core.preferences.Preferences
         getPreferences(
         org.eclipse.stardust.engine.core.preferences.PreferenceScope scope,
         java.lang.String moduleId, java.lang.String preferencesId,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#savePreferences(org.eclipse.stardust.engine.core.preferences.Preferences preferences)
    */
    public void
         savePreferences(
         org.eclipse.stardust.engine.core.preferences.Preferences preferences,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#savePreferences(java.util.List preferences)
    */
    public void savePreferences(
         java.util.List preferences, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.lang.String modelId)
    */
    public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         java.lang.String modelId, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.lang.String modelId, boolean all)
    */
    public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         java.lang.String modelId, boolean all,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.util.List modelIds)
    */
    public
         java.util.List<org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables>
         getConfigurationVariables(
         java.util.List modelIds, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(byte[] model)
    */
    public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         byte[] model, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#saveConfigurationVariables(org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables configurationVariables, boolean force)
    */
    public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo>
         saveConfigurationVariables(
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         configurationVariables, boolean force,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getGlobalPermissions()
    */
    public org.eclipse.stardust.engine.api.runtime.RuntimePermissions
         getGlobalPermissions(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         
   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setGlobalPermissions(org.eclipse.stardust.engine.api.runtime.RuntimePermissions permissions)
    */
    public void
         setGlobalPermissions(
         org.eclipse.stardust.engine.api.runtime.RuntimePermissions permissions,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb3.WorkflowException;
         }