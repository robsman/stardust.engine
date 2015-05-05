/*
 * Generated from Revision
 */
package org.eclipse.stardust.engine.api.spring;

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
public class AdministrationServiceBean extends org.eclipse.stardust.engine.api.spring.AbstractSpringServiceBean implements IAdministrationService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setPasswordRules(org.eclipse.stardust.engine.api.runtime.PasswordRules rules)
    */
   public void
         setPasswordRules(org.eclipse.stardust.engine.api.runtime.PasswordRules rules)
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).setPasswordRules(rules);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPasswordRules()
    */
   public org.eclipse.stardust.engine.api.runtime.PasswordRules
         getPasswordRules()
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getPasswordRules();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.lang.String model, int predecessorOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deployModel(java.lang.String model, int predecessorOID)
         throws org.eclipse.stardust.engine.api.runtime.DeploymentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).deployModel(model, predecessorOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(java.lang.String model, int modelOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(java.lang.String model, int modelOID)
         throws org.eclipse.stardust.engine.api.runtime.DeploymentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).overwriteModel(model, modelOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.lang.String model, java.lang.String configuration, int predecessorOID, java.util.Date validFrom, java.util.Date validTo, java.lang.String comment, boolean disabled, boolean ignoreWarnings)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deployModel(
         java.lang.String model, java.lang.String configuration, int predecessorOID,
         java.util.Date validFrom, java.util.Date validTo, java.lang.String comment,
         boolean disabled, boolean ignoreWarnings)
         throws org.eclipse.stardust.engine.api.runtime.DeploymentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).deployModel(
            model, configuration, predecessorOID, validFrom, validTo, comment, disabled,
            ignoreWarnings);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(java.lang.String model, java.lang.String configuration, int modelOID, java.util.Date validFrom, java.util.Date validTo, java.lang.String comment, boolean disabled, boolean ignoreWarnings)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         java.lang.String model, java.lang.String configuration, int modelOID,
         java.util.Date validFrom, java.util.Date validTo, java.lang.String comment,
         boolean disabled, boolean ignoreWarnings)
         throws org.eclipse.stardust.engine.api.runtime.DeploymentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).overwriteModel(
            model, configuration, modelOID, validFrom, validTo, comment, disabled,
            ignoreWarnings);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(org.eclipse.stardust.engine.api.runtime.DeploymentElement deploymentElement, int modelOID, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         org.eclipse.stardust.engine.api.runtime.DeploymentElement deploymentElement, int
         modelOID, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
         throws org.eclipse.stardust.engine.api.runtime.DeploymentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).overwriteModel(deploymentElement, modelOID, options);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.util.List deploymentElements, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.DeploymentInfo>
         deployModel(
         java.util.List deploymentElements,
         org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
         throws org.eclipse.stardust.engine.api.runtime.DeploymentException,
         org.eclipse.stardust.common.error.ConcurrencyException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).deployModel(deploymentElements, options);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setPrimaryImplementation(long interfaceModelOid, java.lang.String processId, java.lang.String implementationModelId, org.eclipse.stardust.engine.api.runtime.LinkingOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         setPrimaryImplementation(
         long interfaceModelOid, java.lang.String processId, java.lang.String
         implementationModelId, org.eclipse.stardust.engine.api.runtime.LinkingOptions
         options)
         throws org.eclipse.stardust.engine.api.runtime.DeploymentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).setPrimaryImplementation(
            interfaceModelOid, processId, implementationModelId, options);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteModel(long modelOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo deleteModel(
         long modelOID)
         throws org.eclipse.stardust.engine.api.runtime.DeploymentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).deleteModel(modelOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteProcesses(java.util.List piOids)
    */
   public void deleteProcesses(java.util.List piOids)
         throws org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).deleteProcesses(piOids);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#cleanupRuntime(boolean keepUsers)
    */
   public void cleanupRuntime(boolean keepUsers)
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).cleanupRuntime(keepUsers);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#cleanupRuntimeAndModels()
    */
   public void cleanupRuntimeAndModels()
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).cleanupRuntimeAndModels();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProcessInstancePriority(long oid, int priority)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         setProcessInstancePriority(long oid, int priority)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).setProcessInstancePriority(oid, priority);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProcessInstancePriority(long oid, int priority, boolean propagateToSubProcesses)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         setProcessInstancePriority(
         long oid, int priority, boolean propagateToSubProcesses)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).setProcessInstancePriority(
            oid, priority, propagateToSubProcesses);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#abortProcessInstance(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         abortProcessInstance(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).abortProcessInstance(oid);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverProcessInstance(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         recoverProcessInstance(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).recoverProcessInstance(oid);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverProcessInstances(java.util.List oids)
    */
   public void recoverProcessInstances(java.util.List oids)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).recoverProcessInstances(oids);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getDaemon(java.lang.String daemonType, boolean acknowledge)
    */
   public org.eclipse.stardust.engine.api.runtime.Daemon
         getDaemon(java.lang.String daemonType, boolean acknowledge)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getDaemon(daemonType, acknowledge);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#stopDaemon(java.lang.String daemonType, boolean acknowledge)
    */
   public org.eclipse.stardust.engine.api.runtime.Daemon
         stopDaemon(java.lang.String daemonType, boolean acknowledge)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).stopDaemon(daemonType, acknowledge);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#startDaemon(java.lang.String daemonType, boolean acknowledge)
    */
   public org.eclipse.stardust.engine.api.runtime.Daemon
         startDaemon(java.lang.String daemonType, boolean acknowledge)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).startDaemon(daemonType, acknowledge);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAllDaemons(boolean acknowledge)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Daemon>
         getAllDaemons(boolean acknowledge)
         throws org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getAllDaemons(acknowledge);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAuditTrailHealthReport()
    */
   public org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport
         getAuditTrailHealthReport()
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getAuditTrailHealthReport();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAuditTrailHealthReport(boolean countOnly)
    */
   public org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport
         getAuditTrailHealthReport(boolean countOnly)
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getAuditTrailHealthReport(countOnly);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverRuntimeEnvironment()
    */
   public void recoverRuntimeEnvironment()
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).recoverRuntimeEnvironment();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#startProcess(long modelOID, java.lang.String id, java.util.Map data, boolean synchronously)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         startProcess(
         long modelOID, java.lang.String id, java.util.Map data, boolean synchronously)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).startProcess(modelOID, id, data, synchronously);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#forceCompletion(long activityInstanceOID, java.util.Map accessPoints)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         forceCompletion(long activityInstanceOID, java.util.Map accessPoints)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException,
         org.eclipse.stardust.common.error.InvalidValueException,
         org.eclipse.stardust.common.error.AccessForbiddenException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).forceCompletion(activityInstanceOID, accessPoints);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#forceSuspendToDefaultPerformer(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         forceSuspendToDefaultPerformer(long activityInstanceOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).forceSuspendToDefaultPerformer(activityInstanceOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getUser()
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser()
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getUser();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#flushCaches()
    */
   public void flushCaches()
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).flushCaches();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPermissions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions()
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getPermissions();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getProfile(org.eclipse.stardust.engine.api.model.ProfileScope scope)
    */
   public java.util.Map<java.lang.String,?>
         getProfile(org.eclipse.stardust.engine.api.model.ProfileScope scope)
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getProfile(scope);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProfile(org.eclipse.stardust.engine.api.model.ProfileScope scope, java.util.Map profile)
    */
   public void setProfile(
         org.eclipse.stardust.engine.api.model.ProfileScope scope, java.util.Map profile)
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).setProfile(scope, profile);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#writeLogEntry(org.eclipse.stardust.engine.api.runtime.LogType logType, org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid, java.lang.String message, java.lang.Throwable throwable)
    */
   public void writeLogEntry(
         org.eclipse.stardust.engine.api.runtime.LogType logType,
         org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
         java.lang.String message, java.lang.Throwable throwable)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).writeLogEntry(
            logType, contextType, contextOid, message, throwable);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#createDepartment(java.lang.String id, java.lang.String name, java.lang.String description, org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
    */
   public org.eclipse.stardust.engine.api.runtime.Department
         createDepartment(
         java.lang.String id, java.lang.String name, java.lang.String description,
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
         org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
         throws org.eclipse.stardust.engine.api.runtime.DepartmentExistsException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidArgumentException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).createDepartment(id, name, description, parent, organization);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getDepartment(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.Department getDepartment(
         long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getDepartment(oid);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#modifyDepartment(long oid, java.lang.String name, java.lang.String description)
    */
   public org.eclipse.stardust.engine.api.runtime.Department
         modifyDepartment(long oid, java.lang.String name, java.lang.String description)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidArgumentException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).modifyDepartment(oid, name, description);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#removeDepartment(long oid)
    */
   public void removeDepartment(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.common.error.InvalidArgumentException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).removeDepartment(oid);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPreferences(org.eclipse.stardust.engine.core.preferences.PreferenceScope scope, java.lang.String moduleId, java.lang.String preferencesId)
    */
   public org.eclipse.stardust.engine.core.preferences.Preferences
         getPreferences(
         org.eclipse.stardust.engine.core.preferences.PreferenceScope scope,
         java.lang.String moduleId, java.lang.String preferencesId)
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getPreferences(scope, moduleId, preferencesId);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#savePreferences(org.eclipse.stardust.engine.core.preferences.Preferences preferences)
    */
   public void
         savePreferences(
         org.eclipse.stardust.engine.core.preferences.Preferences preferences)
         throws org.eclipse.stardust.common.error.AccessForbiddenException
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).savePreferences(preferences);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#savePreferences(java.util.List preferences)
    */
   public void savePreferences(java.util.List preferences)
         throws org.eclipse.stardust.common.error.AccessForbiddenException
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).savePreferences(preferences);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.lang.String modelId)
    */
   public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(java.lang.String modelId)
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getConfigurationVariables(modelId);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.lang.String modelId, boolean all)
    */
   public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(java.lang.String modelId, boolean all)
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getConfigurationVariables(modelId, all);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.util.List modelIds)
    */
   public
         java.util.List<org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables>
         getConfigurationVariables(java.util.List modelIds)
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getConfigurationVariables(modelIds);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(byte[] model)
    */
   public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(byte[] model)
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getConfigurationVariables(model);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#saveConfigurationVariables(org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables configurationVariables, boolean force)
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo>
         saveConfigurationVariables(
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         configurationVariables, boolean force)
         throws org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).saveConfigurationVariables(configurationVariables, force);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getGlobalPermissions()
    */
   public org.eclipse.stardust.engine.api.runtime.RuntimePermissions
         getGlobalPermissions()
   {
      return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).getGlobalPermissions();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setGlobalPermissions(org.eclipse.stardust.engine.api.runtime.RuntimePermissions permissions)
    */
   public void
         setGlobalPermissions(
         org.eclipse.stardust.engine.api.runtime.RuntimePermissions permissions)
         throws org.eclipse.stardust.common.error.AccessForbiddenException
   {
      ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            serviceProxy).setGlobalPermissions(permissions);
   }

	public AdministrationServiceBean()
	{
      super(org.eclipse.stardust.engine.api.runtime.AdministrationService.class,
            org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl.class);
	}
}