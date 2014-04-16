/*
 * Generated from Revision: 60537 
 */
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * The WorkflowService provides all functionality for workflow operations in a
 * CARNOT runtime environment.
 * <p>
 * This includes:
 * <ul>
 * <li>starting and aborting process instances,</li>
 * <li>activating, completing, suspending and aborting activities,</li>
 * <li>binding and unbinding event handlers,</li>
 * <li>delegating activities, and </li>
 * <li>accessing workflow data.</li>
 * </ul>
 *
 * @author ubirkemeyer
 * @version 60537
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkflowServiceImpl extends org.eclipse.stardust.engine.api.ejb3.beans.AbstractEjb3ServiceBean implements WorkflowService, RemoteWorkflowService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activate(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance activate(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).activate(activityInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#complete(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance complete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).complete(activityInstanceOID, context, outData);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#complete(long activityInstanceOID, java.lang.String context, java.util.Map outData, int flags)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog
         complete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData, int
         flags, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).complete(activityInstanceOID, context, outData, flags);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateAndComplete(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateAndComplete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).activateAndComplete(activityInstanceOID, context, outData);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateAndComplete(long activityInstanceOID, java.lang.String context, java.util.Map outData, int flags)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog
         activateAndComplete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData, int
         flags, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).activateAndComplete(activityInstanceOID, context, outData, flags);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataValue(long activityInstanceOID, java.lang.String context, java.lang.String id)
    */
   public java.io.Serializable getInDataValue(
         long activityInstanceOID, java.lang.String context, java.lang.String id,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getInDataValue(activityInstanceOID, context, id);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataValues(long activityInstanceOID, java.lang.String context, java.util.Set ids)
    */
   public java.util.Map<java.lang.String,java.io.Serializable>
         getInDataValues(
         long activityInstanceOID, java.lang.String context, java.util.Set ids,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getInDataValues(activityInstanceOID, context, ids);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspend(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ContextData outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspend(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ContextData
         outData, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspend(activityInstanceOID, outData);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToDefaultPerformer(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToDefaultPerformer(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToDefaultPerformer(activityInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToDefaultPerformer(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToDefaultPerformer(
         long activityInstanceOID, java.lang.String context, java.util.Map outData,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToDefaultPerformer(activityInstanceOID, context, outData);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToUser(activityInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, java.lang.String context, java.util.Map outData,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToUser(activityInstanceOID, context, outData);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, long userOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, long userOID,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToUser(activityInstanceOID, userOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID, long userOID, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToUser(
         long activityInstanceOID, long userOID, java.lang.String context, java.util.Map
         outData, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToUser(activityInstanceOID, userOID, context, outData);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, java.lang.String participant)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(
         long activityInstanceOID, java.lang.String participant,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToParticipant(activityInstanceOID, participant);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, java.lang.String participant, java.lang.String context, java.util.Map outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(
         long activityInstanceOID, java.lang.String participant, java.lang.String context,
         java.util.Map outData, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToParticipant(
            activityInstanceOID, participant, context, outData);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant, org.eclipse.stardust.engine.api.model.ContextData outData)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToParticipant(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo
         participant, org.eclipse.stardust.engine.api.model.ContextData outData,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).suspendToParticipant(activityInstanceOID, participant, outData);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#hibernate(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance hibernate(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).hibernate(activityInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#startProcess(java.lang.String id, java.util.Map data, boolean synchronously)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         startProcess(
         java.lang.String id, java.util.Map data, boolean synchronously,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).startProcess(id, data, synchronously);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnSubprocessInstance(long parentProcessInstanceOid, java.lang.String spawnProcessID, boolean copyData, java.util.Map data)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnSubprocessInstance(
         long parentProcessInstanceOid, java.lang.String spawnProcessID, boolean copyData,
         java.util.Map data, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).spawnSubprocessInstance(
            parentProcessInstanceOid, spawnProcessID, copyData, data);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnSubprocessInstances(long parentProcessInstanceOid, java.util.List subprocessSpawnInfo)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.ProcessInstance>
         spawnSubprocessInstances(
         long parentProcessInstanceOid, java.util.List subprocessSpawnInfo,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).spawnSubprocessInstances(parentProcessInstanceOid, subprocessSpawnInfo);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnPeerProcessInstance(long processInstanceOid, java.lang.String spawnProcessID, boolean copyData, java.util.Map data, boolean abortProcessInstance, java.lang.String comment)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnPeerProcessInstance(
         long processInstanceOid, java.lang.String spawnProcessID, boolean copyData,
         java.util.Map data, boolean abortProcessInstance, java.lang.String comment,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).spawnPeerProcessInstance(
            processInstanceOid, spawnProcessID, copyData, data, abortProcessInstance,
            comment);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnPeerProcessInstance(long processInstanceOid, java.lang.String spawnProcessID, org.eclipse.stardust.engine.api.runtime.SpawnOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnPeerProcessInstance(
         long processInstanceOid, java.lang.String spawnProcessID,
         org.eclipse.stardust.engine.api.runtime.SpawnOptions options,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).spawnPeerProcessInstance(processInstanceOid, spawnProcessID, options);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#createCase(java.lang.String name, java.lang.String description, long[] memberOids)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         createCase(
         java.lang.String name, java.lang.String description, long[] memberOids,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).createCase(name, description, memberOids);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinCase(long caseOid, long[] memberOids)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance joinCase(
         long caseOid, long[] memberOids,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).joinCase(caseOid, memberOids);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#leaveCase(long caseOid, long[] memberOids)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance leaveCase(
         long caseOid, long[] memberOids,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).leaveCase(caseOid, memberOids);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#mergeCases(long targetCaseOid, long[] sourceCaseOids, java.lang.String comment)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance mergeCases(
         long targetCaseOid, long[] sourceCaseOids, java.lang.String comment,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).mergeCases(targetCaseOid, sourceCaseOids, comment);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateCase(long caseOid, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         delegateCase(
         long caseOid, org.eclipse.stardust.engine.api.model.ParticipantInfo participant,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).delegateCase(caseOid, participant);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinProcessInstance(long processInstanceOid, long targetProcessInstanceOid, java.lang.String comment)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         joinProcessInstance(
         long processInstanceOid, long targetProcessInstanceOid, java.lang.String comment,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).joinProcessInstance(
            processInstanceOid, targetProcessInstanceOid, comment);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         abortActivityInstance(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).abortActivityInstance(activityInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long activityInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         abortActivityInstance(
         long activityInstanceOid,
         org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).abortActivityInstance(activityInstanceOid, abortScope);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortProcessInstance(long processInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         abortProcessInstance(
         long processInstanceOid,
         org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).abortProcessInstance(processInstanceOid, abortScope);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getModel()
    */
   public org.eclipse.stardust.engine.api.runtime.DeployedModel
         getModel(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getModel();
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getWorklist(org.eclipse.stardust.engine.api.query.WorklistQuery query)
    */
   public org.eclipse.stardust.engine.api.query.Worklist
         getWorklist(
         org.eclipse.stardust.engine.api.query.WorklistQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getWorklist(query);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstance(org.eclipse.stardust.engine.api.query.WorklistQuery query)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstance(
         org.eclipse.stardust.engine.api.query.WorklistQuery query,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).activateNextActivityInstance(query);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstance(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstance(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).activateNextActivityInstance(activityInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstanceForProcessInstance(long processInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstanceForProcessInstance(
         long processInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).activateNextActivityInstanceForProcessInstance(processInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setOutDataPath(long processInstanceOID, java.lang.String id, java.lang.Object object)
    */
   public void setOutDataPath(
         long processInstanceOID, java.lang.String id, java.lang.Object object,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).setOutDataPath(processInstanceOID, id, object);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setOutDataPaths(long processInstanceOID, java.util.Map values)
    */
   public void setOutDataPaths(
         long processInstanceOID, java.util.Map values,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).setOutDataPaths(processInstanceOID, values);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataPath(long processInstanceOID, java.lang.String id)
    */
   public java.lang.Object getInDataPath(
         long processInstanceOID, java.lang.String id,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getInDataPath(processInstanceOID, id);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataPaths(long processInstanceOID, java.util.Set ids)
    */
   public java.util.Map<java.lang.String,java.io.Serializable>
         getInDataPaths(
         long processInstanceOID, java.util.Set ids,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getInDataPaths(processInstanceOID, ids);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToDefaultPerformer(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToDefaultPerformer(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).delegateToDefaultPerformer(activityInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToUser(long activityInstanceOID, long userOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToUser(
         long activityInstanceOID, long userOID,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).delegateToUser(activityInstanceOID, userOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToParticipant(long activityInstanceOID, java.lang.String performer)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToParticipant(
         long activityInstanceOID, java.lang.String performer,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).delegateToParticipant(activityInstanceOID, performer);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToParticipant(long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToParticipant(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo
         participant, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).delegateToParticipant(activityInstanceOID, participant);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getActivityInstance(long activityInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         getActivityInstance(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getActivityInstance(activityInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessInstance(long processInstanceOID)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         getProcessInstance(
         long processInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getProcessInstance(processInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessResults(long processInstanceOID)
    */
   public java.util.Map<java.lang.String,java.io.Serializable>
         getProcessResults(
         long processInstanceOID, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getProcessResults(processInstanceOID);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindActivityEventHandler(long activityInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         bindActivityEventHandler(
         long activityInstanceOID,
         org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).bindActivityEventHandler(activityInstanceOID, eventHandler);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindProcessEventHandler(long processInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         bindProcessEventHandler(
         long processInstanceOID,
         org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).bindProcessEventHandler(processInstanceOID, eventHandler);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindActivityEventHandler(long activityInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         bindActivityEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).bindActivityEventHandler(activityInstanceOID, handler);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindProcessEventHandler(long processInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         bindProcessEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).bindProcessEventHandler(processInstanceOID, handler);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#unbindActivityEventHandler(long activityInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         unbindActivityEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).unbindActivityEventHandler(activityInstanceOID, handler);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#unbindProcessEventHandler(long processInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         unbindProcessEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).unbindProcessEventHandler(processInstanceOID, handler);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getActivityInstanceEventHandler(long activityInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         getActivityInstanceEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getActivityInstanceEventHandler(activityInstanceOID, handler);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessInstanceEventHandler(long processInstanceOID, java.lang.String handler)
    */
   public org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         getProcessInstanceEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getProcessInstanceEventHandler(processInstanceOID, handler);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getAdHocTransitionTargets(long activityInstanceOid, org.eclipse.stardust.engine.api.runtime.TransitionOptions options, org.eclipse.stardust.engine.api.runtime.ScanDirection direction)
    */
   public
         java.util.List<org.eclipse.stardust.engine.api.runtime.TransitionTarget>
         getAdHocTransitionTargets(
         long activityInstanceOid,
         org.eclipse.stardust.engine.api.runtime.TransitionOptions options,
         org.eclipse.stardust.engine.api.runtime.ScanDirection direction,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getAdHocTransitionTargets(activityInstanceOid, options, direction);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#performAdHocTransition(long activityInstanceOid, org.eclipse.stardust.engine.api.runtime.TransitionTarget target, boolean complete)
    */
   public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         performAdHocTransition(
         long activityInstanceOid,
         org.eclipse.stardust.engine.api.runtime.TransitionTarget target, boolean
         complete, org.eclipse.stardust.engine.api.ejb3.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).performAdHocTransition(activityInstanceOid, target, complete);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getStartableProcessDefinitions()
    */
   public java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getStartableProcessDefinitions(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getStartableProcessDefinitions();
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getUser()
    */
   public org.eclipse.stardust.engine.api.runtime.User
         getUser(
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).getUser();
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getPermissions()
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
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setProcessInstanceAttributes(org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes attributes)
    */
   public void
         setProcessInstanceAttributes(
         org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes attributes,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).setProcessInstanceAttributes(attributes);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setActivityInstanceAttributes(org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes attributes)
    */
   public void
         setActivityInstanceAttributes(
         org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes attributes,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).setActivityInstanceAttributes(attributes);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#writeLogEntry(org.eclipse.stardust.engine.api.runtime.LogType logType, org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid, java.lang.String message, java.lang.Throwable throwable)
    */
   public void writeLogEntry(
         org.eclipse.stardust.engine.api.runtime.LogType logType,
         org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
         java.lang.String message, java.lang.Throwable throwable,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).writeLogEntry(logType, contextType, contextOid, message, throwable);
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
    * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#execute(org.eclipse.stardust.engine.core.runtime.command.ServiceCommand serviceCmd)
    */
   public java.io.Serializable
         execute(
         org.eclipse.stardust.engine.core.runtime.command.ServiceCommand serviceCmd,
         org.eclipse.stardust.engine.api.ejb3.TunneledContext __tunneledContext)throws
         org.eclipse.stardust.engine.api.ejb3.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService)
            service).execute(serviceCmd);
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

	public WorkflowServiceImpl()
	{
      this.serviceType=org.eclipse.stardust.engine.api.runtime.WorkflowService.class;
      this.serviceTypeImpl=org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl.class;
	}
}