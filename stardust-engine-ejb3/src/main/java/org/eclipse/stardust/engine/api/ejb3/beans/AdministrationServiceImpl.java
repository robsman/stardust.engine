/*
 * Generated from Revision
 */
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

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
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AdministrationServiceImpl extends org.eclipse.stardust.engine.api.ejb3.beans.AbstractServiceImpl implements AdministrationService, RemoteAdministrationService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setPasswordRules(org.eclipse.stardust.engine.api.runtime.PasswordRules rules)
    */
   public void
         setPasswordRules(
         org.eclipse.stardust.engine.api.runtime.PasswordRules rules,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).setPasswordRules(rules);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPasswordRules()
    */
   public org.eclipse.stardust.engine.api.runtime.PasswordRules
         getPasswordRules(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getPasswordRules();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.lang.String model, int predecessorOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deployModel(
         java.lang.String model, int predecessorOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).deployModel(model, predecessorOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(java.lang.String model, int modelOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         java.lang.String model, int modelOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).overwriteModel(model, modelOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.lang.String model, java.lang.String configuration, int predecessorOID, java.util.Date validFrom, java.util.Date validTo, java.lang.String comment, boolean disabled, boolean ignoreWarnings)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         deployModel(
         java.lang.String model, java.lang.String configuration, int predecessorOID,
         java.util.Date validFrom, java.util.Date validTo, java.lang.String comment,
         boolean disabled, boolean ignoreWarnings,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).deployModel(
            model, configuration, predecessorOID, validFrom, validTo, comment, disabled,
            ignoreWarnings);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(java.lang.String model, java.lang.String configuration, int modelOID, java.util.Date validFrom, java.util.Date validTo, java.lang.String comment, boolean disabled, boolean ignoreWarnings)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         java.lang.String model, java.lang.String configuration, int modelOID,
         java.util.Date validFrom, java.util.Date validTo, java.lang.String comment,
         boolean disabled, boolean ignoreWarnings,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).overwriteModel(
            model, configuration, modelOID, validFrom, validTo, comment, disabled,
            ignoreWarnings);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteModel(org.eclipse.stardust.engine.api.runtime.DeploymentElement deploymentElement, int modelOID, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         overwriteModel(
         org.eclipse.stardust.engine.api.runtime.DeploymentElement deploymentElement, int
         modelOID, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).overwriteModel(deploymentElement, modelOID, options);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployModel(java.util.List deploymentElements, org.eclipse.stardust.engine.api.runtime.DeploymentOptions options)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.DeploymentInfo>
         deployModel(
         java.util.List<org.eclipse.stardust.engine.api.runtime.DeploymentElement>
         deploymentElements, org.eclipse.stardust.engine.api.runtime.DeploymentOptions
         options, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).deployModel(deploymentElements, options);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setPrimaryImplementation(long interfaceModelOid, java.lang.String processId, java.lang.String implementationModelId, org.eclipse.stardust.engine.api.runtime.LinkingOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo
         setPrimaryImplementation(
         long interfaceModelOid, java.lang.String processId, java.lang.String
         implementationModelId, org.eclipse.stardust.engine.api.runtime.LinkingOptions
         options, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).setPrimaryImplementation(
            interfaceModelOid, processId, implementationModelId, options);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteModel(long modelOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeploymentInfo deleteModel(
         long modelOID, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).deleteModel(modelOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteProcesses(java.util.List piOids)
    */
   public void deleteProcesses(
         java.util.List<java.lang.Long> piOids,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).deleteProcesses(piOids);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#cleanupRuntime(boolean keepUsers)
    */
   public void cleanupRuntime(
         boolean keepUsers, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).cleanupRuntime(keepUsers);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#cleanupRuntimeAndModels()
    */
   public void
         cleanupRuntimeAndModels(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).cleanupRuntimeAndModels();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProcessInstancePriority(long oid, int priority)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         setProcessInstancePriority(
         long oid, int priority,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).setProcessInstancePriority(oid, priority);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProcessInstancePriority(long oid, int priority, boolean propagateToSubProcesses)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         setProcessInstancePriority(
         long oid, int priority, boolean propagateToSubProcesses,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).setProcessInstancePriority(oid, priority, propagateToSubProcesses);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#abortProcessInstance(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         abortProcessInstance(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).abortProcessInstance(oid);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverProcessInstance(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         recoverProcessInstance(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).recoverProcessInstance(oid);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverProcessInstances(java.util.List oids)
    */
   public void recoverProcessInstances(
         java.util.List<java.lang.Long> oids,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).recoverProcessInstances(oids);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getDaemon(java.lang.String daemonType, boolean acknowledge)
    */
   public org.eclipse.stardust.engine.api.runtime.Daemon
         getDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getDaemon(daemonType, acknowledge);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#stopDaemon(java.lang.String daemonType, boolean acknowledge)
    */
   public org.eclipse.stardust.engine.api.runtime.Daemon
         stopDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).stopDaemon(daemonType, acknowledge);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#startDaemon(java.lang.String daemonType, boolean acknowledge)
    */
   public org.eclipse.stardust.engine.api.runtime.Daemon
         startDaemon(
         java.lang.String daemonType, boolean acknowledge,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).startDaemon(daemonType, acknowledge);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAllDaemons(boolean acknowledge)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Daemon>
         getAllDaemons(
         boolean acknowledge, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getAllDaemons(acknowledge);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAuditTrailHealthReport()
    */
   public org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport
         getAuditTrailHealthReport(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getAuditTrailHealthReport();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getAuditTrailHealthReport(boolean countOnly)
    */
   public org.eclipse.stardust.engine.api.runtime.AuditTrailHealthReport
         getAuditTrailHealthReport(
         boolean countOnly, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getAuditTrailHealthReport(countOnly);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#recoverRuntimeEnvironment()
    */
   public void
         recoverRuntimeEnvironment(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).recoverRuntimeEnvironment();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#startProcess(long modelOID, java.lang.String id, java.util.Map data, boolean synchronously)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         startProcess(
         long modelOID, java.lang.String id, java.util.Map<java.lang.String,?> data,
         boolean synchronously,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).startProcess(modelOID, id, data, synchronously);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#forceCompletion(long activityInstanceOID, java.util.Map accessPoints)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         forceCompletion(
         long activityInstanceOID, java.util.Map<java.lang.String,?> accessPoints,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).forceCompletion(activityInstanceOID, accessPoints);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#forceSuspendToDefaultPerformer(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         forceSuspendToDefaultPerformer(
         long activityInstanceOID,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).forceSuspendToDefaultPerformer(activityInstanceOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getUser()
    */
   public org.eclipse.stardust.engine.api.runtime.User
         getUser(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getUser();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#flushCaches()
    */
   public void
         flushCaches(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).flushCaches();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPermissions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getPermissions();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getProfile(org.eclipse.stardust.engine.api.model.ProfileScope scope)
    */
   public java.util.Map<java.lang.String,?>
         getProfile(
         org.eclipse.stardust.engine.api.model.ProfileScope scope,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getProfile(scope);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setProfile(org.eclipse.stardust.engine.api.model.ProfileScope scope, java.util.Map profile)
    */
   public void setProfile(
         org.eclipse.stardust.engine.api.model.ProfileScope scope,
         java.util.Map<java.lang.String,?> profile,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).setProfile(scope, profile);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#writeLogEntry(org.eclipse.stardust.engine.api.runtime.LogType logType, org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid, java.lang.String message, java.lang.Throwable throwable)
    */
   public void writeLogEntry(
         org.eclipse.stardust.engine.api.runtime.LogType logType,
         org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
         java.lang.String message, java.lang.Throwable throwable,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).writeLogEntry(logType, contextType, contextOid, message, throwable);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#createDepartment(java.lang.String id, java.lang.String name, java.lang.String description, org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
    */
   public org.eclipse.stardust.engine.api.runtime.Department
         createDepartment(
         java.lang.String id, java.lang.String name, java.lang.String description,
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
         org.eclipse.stardust.engine.api.model.OrganizationInfo organization,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).createDepartment(id, name, description, parent, organization);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getDepartment(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.Department getDepartment(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getDepartment(oid);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#modifyDepartment(long oid, java.lang.String name, java.lang.String description)
    */
   public org.eclipse.stardust.engine.api.runtime.Department
         modifyDepartment(
         long oid, java.lang.String name, java.lang.String description,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).modifyDepartment(oid, name, description);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#removeDepartment(long oid)
    */
   public void removeDepartment(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).removeDepartment(oid);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getPreferences(org.eclipse.stardust.engine.core.preferences.PreferenceScope scope, java.lang.String moduleId, java.lang.String preferencesId)
    */
   public org.eclipse.stardust.engine.core.preferences.Preferences
         getPreferences(
         org.eclipse.stardust.engine.core.preferences.PreferenceScope scope,
         java.lang.String moduleId, java.lang.String preferencesId,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getPreferences(scope, moduleId, preferencesId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#savePreferences(org.eclipse.stardust.engine.core.preferences.Preferences preferences)
    */
   public void
         savePreferences(
         org.eclipse.stardust.engine.core.preferences.Preferences preferences,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).savePreferences(preferences);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#savePreferences(java.util.List preferences)
    */
   public void
         savePreferences(
         java.util.List<org.eclipse.stardust.engine.core.preferences.Preferences>
         preferences, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).savePreferences(preferences);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.lang.String modelId)
    */
   public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         java.lang.String modelId,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getConfigurationVariables(modelId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.lang.String modelId, boolean all)
    */
   public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         java.lang.String modelId, boolean all,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getConfigurationVariables(modelId, all);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(java.util.List modelIds)
    */
   public
         java.util.List<org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables>
         getConfigurationVariables(
         java.util.List<java.lang.String> modelIds,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getConfigurationVariables(modelIds);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getConfigurationVariables(byte[] model)
    */
   public
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         getConfigurationVariables(
         byte[] model, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getConfigurationVariables(model);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#saveConfigurationVariables(org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables configurationVariables, boolean force)
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo>
         saveConfigurationVariables(
         org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables
         configurationVariables, boolean force,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).saveConfigurationVariables(configurationVariables, force);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getGlobalPermissions()
    */
   public org.eclipse.stardust.engine.api.runtime.RuntimePermissions
         getGlobalPermissions(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getGlobalPermissions();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#setGlobalPermissions(org.eclipse.stardust.engine.api.runtime.RuntimePermissions permissions)
    */
   public void
         setGlobalPermissions(
         org.eclipse.stardust.engine.api.runtime.RuntimePermissions permissions,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).setGlobalPermissions(permissions);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deployRuntimeArtifact(org.eclipse.stardust.engine.api.runtime.RuntimeArtifact runtimeArtifact)
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact
         deployRuntimeArtifact(
         org.eclipse.stardust.engine.api.runtime.RuntimeArtifact runtimeArtifact,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).deployRuntimeArtifact(runtimeArtifact);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#overwriteRuntimeArtifact(long oid, org.eclipse.stardust.engine.api.runtime.RuntimeArtifact runtimeArtifact)
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact
         overwriteRuntimeArtifact(
         long oid, org.eclipse.stardust.engine.api.runtime.RuntimeArtifact
         runtimeArtifact, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).overwriteRuntimeArtifact(oid, runtimeArtifact);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#deleteRuntimeArtifact(long oid)
    */
   public void deleteRuntimeArtifact(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).deleteRuntimeArtifact(oid);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getRuntimeArtifact(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.RuntimeArtifact
         getRuntimeArtifact(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getRuntimeArtifact(oid);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#getSupportedRuntimeArtifactTypes()
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.ArtifactType>
         getSupportedRuntimeArtifactTypes(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).getSupportedRuntimeArtifactTypes();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#createProcessInstanceLinkType(java.lang.String id, java.lang.String description)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstanceLinkType
         createProcessInstanceLinkType(
         java.lang.String id, java.lang.String description,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.AdministrationService)
            service).createProcessInstanceLinkType(id, description);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

	public AdministrationServiceImpl()
	{
      this.serviceType=org.eclipse.stardust.engine.api.runtime.AdministrationService.class;
      this.serviceTypeImpl=org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl.class;
	}
}