/*
 * Generated from 
 */
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * to set {@link SessionProperties.DS_NAME_READ_ONLY}
 * used in {@link DataValueBean}
 *
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class QueryServiceImpl extends org.eclipse.stardust.engine.api.ejb3.beans.AbstractEjb3ServiceBean implements QueryService, RemoteQueryService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getUsersCount(org.eclipse.stardust.engine.api.query.UserQuery query)
    */
   public long getUsersCount(
         org.eclipse.stardust.engine.api.query.UserQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getUsersCount(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getUserGroupsCount(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
    */
   public long
         getUserGroupsCount(
         org.eclipse.stardust.engine.api.query.UserGroupQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getUserGroupsCount(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessInstancesCount(org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
    */
   public long
         getProcessInstancesCount(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getProcessInstancesCount(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActivityInstancesCount(org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
    */
   public long
         getActivityInstancesCount(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getActivityInstancesCount(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getLogEntriesCount(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
    */
   public long
         getLogEntriesCount(
         org.eclipse.stardust.engine.api.query.LogEntryQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getLogEntriesCount(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllUsers(org.eclipse.stardust.engine.api.query.UserQuery query)
    */
   public org.eclipse.stardust.engine.api.query.Users
         getAllUsers(
         org.eclipse.stardust.engine.api.query.UserQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllUsers(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllUserGroups(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
    */
   public org.eclipse.stardust.engine.api.query.UserGroups
         getAllUserGroups(
         org.eclipse.stardust.engine.api.query.UserGroupQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllUserGroups(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessInstances(org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
    */
   public org.eclipse.stardust.engine.api.query.ProcessInstances
         getAllProcessInstances(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllProcessInstances(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllActivityInstances(org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
    */
   public org.eclipse.stardust.engine.api.query.ActivityInstances
         getAllActivityInstances(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllActivityInstances(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllLogEntries(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
    */
   public org.eclipse.stardust.engine.api.query.LogEntries
         getAllLogEntries(
         org.eclipse.stardust.engine.api.query.LogEntryQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllLogEntries(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstUser(org.eclipse.stardust.engine.api.query.UserQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         findFirstUser(
         org.eclipse.stardust.engine.api.query.UserQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).findFirstUser(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstUserGroup(org.eclipse.stardust.engine.api.query.UserGroupQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         findFirstUserGroup(
         org.eclipse.stardust.engine.api.query.UserGroupQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).findFirstUserGroup(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstProcessInstance(org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         findFirstProcessInstance(
         org.eclipse.stardust.engine.api.query.ProcessInstanceQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).findFirstProcessInstance(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstActivityInstance(org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         findFirstActivityInstance(
         org.eclipse.stardust.engine.api.query.ActivityInstanceQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).findFirstActivityInstance(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstLogEntry(org.eclipse.stardust.engine.api.query.LogEntryQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.LogEntry
         findFirstLogEntry(
         org.eclipse.stardust.engine.api.query.LogEntryQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).findFirstLogEntry(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAuditTrail(long processInstanceOID)
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.ActivityInstance>
         getAuditTrail(
         long processInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAuditTrail(processInstanceOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllModelDescriptions()
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.DeployedModelDescription>
         getAllModelDescriptions(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllModelDescriptions();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllAliveModelDescriptions()
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.DeployedModelDescription>
         getAllAliveModelDescriptions(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllAliveModelDescriptions();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActiveModelDescription()
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModelDescription
         getActiveModelDescription(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getActiveModelDescription();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModels(org.eclipse.stardust.engine.api.query.DeployedModelQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.Models
         getModels(
         org.eclipse.stardust.engine.api.query.DeployedModelQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getModels(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModelDescription(long modelOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModelDescription
         getModelDescription(
         long modelOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getModelDescription(modelOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#wasRedeployed(long modelOid, int revision)
    */
   public boolean wasRedeployed(
         long modelOid, int revision, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).wasRedeployed(modelOid, revision);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModel(long modelOID)
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModel getModel(
         long modelOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getModel(modelOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModel(long modelOID, boolean computeAliveness)
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModel getModel(
         long modelOID, boolean computeAliveness,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getModel(modelOID, computeAliveness);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getActiveModel()
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModel
         getActiveModel(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getActiveModel();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getModelAsXML(long modelOID)
    */
   public java.lang.String getModelAsXML(
         long modelOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getModelAsXML(modelOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessDefinitions(long modelOID)
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getAllProcessDefinitions(
         long modelOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllProcessDefinitions(modelOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinition(long modelOID, java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.model.ProcessDefinition
         getProcessDefinition(
         long modelOID, java.lang.String id,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getProcessDefinition(modelOID, id);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessDefinitions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getAllProcessDefinitions(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllProcessDefinitions();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinition(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.model.ProcessDefinition
         getProcessDefinition(
         java.lang.String id, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getProcessDefinition(id);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getProcessDefinitions(org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessDefinitions
         getProcessDefinitions(
         org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getProcessDefinitions(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllData(org.eclipse.stardust.engine.api.query.DataQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.DataQueryResult
         getAllData(
         org.eclipse.stardust.engine.api.query.DataQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllData(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllParticipants(long modelOID)
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.Participant>
         getAllParticipants(
         long modelOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllParticipants(modelOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getParticipant(long modelOID, java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.model.Participant getParticipant(
         long modelOID, java.lang.String id,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getParticipant(modelOID, id);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllParticipants()
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.Participant>
         getAllParticipants(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllParticipants();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getParticipant(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.model.Participant
         getParticipant(
         java.lang.String id, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getParticipant(id);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getPermissions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getPermissions();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getSchemaDefinition(long modelOID, java.lang.String typeDeclarationId)
    */
   public byte[] getSchemaDefinition(
         long modelOID, java.lang.String typeDeclarationId,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getSchemaDefinition(modelOID, typeDeclarationId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findAllDepartments(org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, org.eclipse.stardust.engine.api.model.OrganizationInfo organization)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Department>
         findAllDepartments(
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent,
         org.eclipse.stardust.engine.api.model.OrganizationInfo organization,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).findAllDepartments(parent, organization);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findDepartment(org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, java.lang.String id, org.eclipse.stardust.engine.api.model.OrganizationInfo info)
    */
   public org.eclipse.stardust.engine.api.runtime.Department
         findDepartment(
         org.eclipse.stardust.engine.api.runtime.DepartmentInfo parent, java.lang.String
         id, org.eclipse.stardust.engine.api.model.OrganizationInfo info,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).findDepartment(parent, id, info);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#findFirstDocument(org.eclipse.stardust.engine.api.query.DocumentQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.Document
         findFirstDocument(
         org.eclipse.stardust.engine.api.query.DocumentQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).findFirstDocument(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllDocuments(org.eclipse.stardust.engine.api.query.DocumentQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.Documents
         getAllDocuments(
         org.eclipse.stardust.engine.api.query.DocumentQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllDocuments(query);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getPreferences(org.eclipse.stardust.engine.core.preferences.PreferenceScope scope, java.lang.String moduleId, java.lang.String preferencesId)
    */
   public org.eclipse.stardust.engine.core.preferences.Preferences
         getPreferences(
         org.eclipse.stardust.engine.core.preferences.PreferenceScope scope,
         java.lang.String moduleId, java.lang.String preferencesId,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getPreferences(scope, moduleId, preferencesId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getAllPreferences(org.eclipse.stardust.engine.api.query.PreferenceQuery preferenceQuery)
    */
   public
         java.util.List<org.eclipse.stardust.engine.core.preferences.Preferences>
         getAllPreferences(
         org.eclipse.stardust.engine.api.query.PreferenceQuery preferenceQuery,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getAllPreferences(preferenceQuery);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.QueryService#getRuntimeEnvironmentInfo()
    */
   public org.eclipse.stardust.engine.api.runtime.RuntimeEnvironmentInfo
         getRuntimeEnvironmentInfo(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.QueryService)
            service).getRuntimeEnvironmentInfo();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb3.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

	public QueryServiceImpl()
	{
      this.serviceType=org.eclipse.stardust.engine.api.runtime.QueryService.class;
      this.serviceTypeImpl=org.eclipse.stardust.engine.core.runtime.beans.QueryServiceImpl.class;
	}
}