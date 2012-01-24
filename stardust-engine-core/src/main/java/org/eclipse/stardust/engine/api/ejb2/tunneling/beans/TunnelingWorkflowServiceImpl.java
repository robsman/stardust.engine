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
/*
 * Generated from  Revision: 52371 
 */
package org.eclipse.stardust.engine.api.ejb2.tunneling.beans;

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
 * @version 52371
 */
public class TunnelingWorkflowServiceImpl extends org.eclipse.stardust.engine.api.ejb2.tunneling.beans.AbstractTunnelingServiceImpl
{

    /**
     * Activates the interactive activity instance identified by the
     * <code>activityInstanceOID</code>.
     * 
     * <p>Activating means:
     * <li>Removing the activity instance from its original worklist.</li>
     * <li>Adding the activity instance to the logged-in user's worklist.</li>
     * <li>Setting the state of the activity instance to APPLICATION state.</li>
     *
     * @param activityInstanceOID the OID of the activity to be activated.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was activated.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user is not valid or is not
     *             granted to execute the activity instance. Also thrown if the activity
     *             instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #activateAndComplete
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activate(long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance activate(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Completes the interactive activity instance identified by the
     * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
     *
     * @param activityInstanceOID the OID of the activity to be completed.
     * @param context the ID of the context on which the data mapping will be performed.
     * @param outData a map with the values of the out access points.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was completed.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the activity instance is exclusively locked by
     *     another thread.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException if that state change is not
     *     permitted,
     *             i.e. the activity is not active.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException} will
     *     be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidValueException if one of the <code>outData</object> values to
     *             be written is invalid, most probably as of a type conflict in case of
     *             statically typed data.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidValueException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user is not allowed to
     *     complete the activity.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #activateAndComplete(long, String, Map)
     * @see #complete(long, String, Map, int)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#complete(
     *     long activityInstanceOID, java.lang.String context, java.util.Map outData)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance complete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Completes the interactive activity instance identified by the
     * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
     *
     * @param activityInstanceOID the OID of the activity to be completed.
     * @param context the ID of the context on which the data mapping will be performed.
     * @param outData a map with the values of the out access points.
     * @param flags Optional adjustment to some details of operation. Supported values are
     *                   {@link #FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE}.
     *
     * @return A log describing the result of the invocation. Depends on the flags parameter.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException if that state change is not
     *     permitted,
     *             i.e. the activity is not active.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException} will
     *     be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidValueException if one of the <code>outData</object> values to
     *             be written is invalid, most probably as of a type conflict in case of
     *             statically typed data.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidValueException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the activity instance is
     *             already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #complete(long, String, Map)
     * @see #activateAndComplete(long, String, Map)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#complete(
     *     long activityInstanceOID, java.lang.String context, java.util.Map outData, int flags)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog complete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData, int
         flags, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Activates and completes the interactive activity instance identified by the
     * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
     * 
     * If the activity is activated to be immediately completed, this method is more
     * efficient than invoking activate(...) and complete(...) separately.
     *
     * @param activityInstanceOID the OID of the activity to be completed.
     * @param context the ID of the context on which the data mapping will be performed.
     *            The value <code>null</code> will be interpreted as the default context.
     * @param outData a map with the values of the out access points.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was completed.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException if that state change is not
     *     permitted,
     *             i.e. the activity is not active.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException} will
     *     be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidValueException if one of the <code>outData</code> values to
     *             be written is invalid, most probably as of a type conflict in case of
     *             statically typed data.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidValueException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user is not valid or is not
     *             granted to execute the activity instance. Also thrown if the activity
     *             instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #activate(long)
     * @see #complete(long, String, Map)
     * @see #activateAndComplete(long, String, Map, int)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateAndComplete(
     *     long activityInstanceOID, java.lang.String context, java.util.Map outData)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance activateAndComplete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Activates and completes the interactive activity instance identified by the
     * <code>activityInstanceOID</code> on the behalf of the currently logged-in user.
     * 
     * If the activity is activated to be immediately completed, this method is more
     * efficient than invoking activate(...) and complete(...) separately.
     *
     * @param activityInstanceOID the OID of the activity to be completed.
     * @param context the ID of the context on which the data mapping will be performed.
     *            The value <code>null</code> will be interpreted as the default context.
     * @param outData a map with the values of the out access points.
     * @param flags Optional adjustment to some details of operation. Supported values are
     *                   {@link #FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE}.
     *
     * @return A log describing the result of the invocation. Depends on the flags parameter.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException if that state change is not
     *     permitted,
     *             i.e. the activity is not active.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException} will
     *     be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidValueException if one of the <code>outData</object> values to
     *             be written is invalid, most probably as of a type conflict in case of
     *             statically typed data.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidValueException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user is not valid or is not
     *             granted to execute the activity instance. Also thrown if the activity
     *             instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #activateAndComplete(long, String, Map)
     * @see #activate(long)
     * @see #complete(long, String, Map)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateAndComplete(
     *     long activityInstanceOID, java.lang.String context, java.util.Map outData, int flags)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog
         activateAndComplete(
         long activityInstanceOID, java.lang.String context, java.util.Map outData, int
         flags, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves all evaluated IN data mappings that match the provided application context
     * for the specified activity.
     *
     * @param activityInstanceOID the OID of the activity for which the data mappings are
     *            to be retrieved.
     * @param context the application context for which the mappings are retrieved.
     *            The value <code>null</code> will be interpreted as the default context.
     * @param id The ID of the data mapping to be retrieved.
     *
     * @return The retrieved value.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified
     *       OID or there is no mapping with the given ID under the given context.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #getInDataValues(long, String, Set)
     *
     * @since 3.1.2
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataValue(
     *     long activityInstanceOID, java.lang.String context, java.lang.String id)
     */
    public java.io.Serializable getInDataValue(
         long activityInstanceOID, java.lang.String context, java.lang.String id,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves all evaluated IN data mappings that match the provided application context
     * for the specified activity.
     *
     * @param activityInstanceOID the OID of the activity for which the data mappings are
     *            to be retrieved.
     * @param context the application context for which the mappings are retrieved.
     *            The value <code>null</code> will be interpreted as the default context.
     * @param ids the set of data mapping IDs designating the values to be retrieved. If
     *            <code>null</code> is passed, all IN data mappings for the context are
     *            retrieved.
     *
     * @return A Map with corresponding (data mapping ID, data value)-pairs. Data values
     *             are {@link java.io.Serializable}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified
     *       OID or not all mapping IDs can be resolved in the given context.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #getInDataValue(long, String, String)
     *
     * @since 3.1.2
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataValues(
     *     long activityInstanceOID, java.lang.String context, java.util.Set ids)
     */
    public java.util.Map<java.lang.String,java.io.Serializable>
         getInDataValues(
         long activityInstanceOID, java.lang.String context, java.util.Set ids,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the same worklist
     * in which it was prior to activation, and the specified activity instance will be
     * set to SUSPENDED state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     * @param outData the context data containing values of out access points to be stored.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the activity instance is already
     *     terminated or is
     *             currently processed by another user or the current user does not have the
     *             required permission.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidArgumentException if QA AI with non empty out data map
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidArgumentException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToDefaultPerformer(long, String, Map)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspend(
     *     long activityInstanceOID, org.eclipse.stardust.engine.api.model.ContextData outData)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspend(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ContextData outData,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the worklist of the
     * default performer declared for the corresponding activity, and the specified
     * activity instance will be set to SUSPENDED state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the activity instance is already
     *     terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToDefaultPerformer(long, String, Map)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToDefaultPerformer(
     *     long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToDefaultPerformer(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the worklist of the
     * default performer declared for the corresponding activity, and the specified
     * activity instance will be set to SUSPENDED state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     * @param context the ID of the context on which the data mapping will be performed.
     * @param outData a map with values of out access points to be stored.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the activity instance is already
     *     terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToDefaultPerformer(long)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToDefaultPerformer(
     *     long activityInstanceOID, java.lang.String context, java.util.Map outData)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         suspendToDefaultPerformer(
         long activityInstanceOID, java.lang.String context, java.util.Map outData,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the worklist of the
     * current user, and the specified activity instance will be set to SUSPENDED state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the delegation target is not granted to
     *     execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToUser(long, String, Map)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspendToUser(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the worklist of the
     * current user, and the specified activity instance will be set to SUSPENDED state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     * @param context the ID of the context on which the data mapping will be performed.
     * @param outData a map with values of out access points to be stored.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the delegation target is not granted to
     *     execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToUser(long)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(
     *     long activityInstanceOID, java.lang.String context, java.util.Map outData)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspendToUser(
         long activityInstanceOID, java.lang.String context, java.util.Map outData,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the worklist of the
     * provided user, and the specified activity instance will be set to SUSPENDED state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     * @param userOID the OID of the user.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the delegation target is not granted to
     *     execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToUser(long, long, String, Map)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(
     *     long activityInstanceOID, long userOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspendToUser(
         long activityInstanceOID, long userOID,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the worklist of the
     * provided user, and the specified activity instance will be set to SUSPENDED state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     * @param userOID the OID of the user.
     * @param context the ID of the context on which the data mapping will be performed.
     * @param outData a map with values of out access points to be stored.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the delegation target is not granted to
     *     execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToUser(long, long)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToUser(
     *     long activityInstanceOID, long userOID, java.lang.String context, java.util.Map
     *     outData)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspendToUser(
         long activityInstanceOID, long userOID, java.lang.String context, java.util.Map
         outData, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the worklist of the
     * provided performer, and the specified activity instance will be set to SUSPENDED
     * state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     * @param participant the ID of the performer.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the delegation target is not granted to
     *     execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToParticipant(long, String, String, Map)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(
     *     long activityInstanceOID, java.lang.String participant)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspendToParticipant(
         long activityInstanceOID, java.lang.String participant,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the specified activity instance. It will be added to the worklist of the
     * provided performer, and the specified activity instance will be set to SUSPENDED
     * state.
     *
     * @param activityInstanceOID the OID of the activity to be suspended.
     * @param participant the ID of the performer.
     * @param context the ID of the context on which the data mapping will be performed.
     * @param outData a map with values of out access points to be stored.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the delegation target is not granted to
     *     execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #suspendToParticipant(long, String)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(
     *     long activityInstanceOID, java.lang.String participant, java.lang.String context,
     *     java.util.Map outData)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspendToParticipant(
         long activityInstanceOID, java.lang.String participant, java.lang.String context,
         java.util.Map outData, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Suspends the activity instance and, if the participant is not null, delegates it to
     * the specified participant.
     *
     * @param activityInstanceOID the OID of the activity instance.
     * @param participant the participant (
     *     model participant, user or user group) to which the activity instance will be
     *     delegated.
     * @param outData the context data containing values of out access points to be stored.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was suspended.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID or if the participant is not null
     *             and could not be resolved to an actual user user group or model participant.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the activity instance is already
     *     terminated or is
     *             currently processed by another user or the current user does not have the
     *             required permission or if the delegation target is not granted to execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#suspendToParticipant(
     *     long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant,
     *     org.eclipse.stardust.engine.api.model.ContextData outData)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance suspendToParticipant(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant,
         org.eclipse.stardust.engine.api.model.ContextData outData,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Change the state of the specified activity instance to HIBERNATED.
     *
     * @param activityInstanceOID the OID of the activity to be hibernated.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was hibernated.
     *
     * @throws org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException if that state change is not
     *     permitted,
     *             i.e. the activity is already completed or aborted.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException} will
     *     be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#hibernate(long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance hibernate(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Starts the process specified by the given <code>ID</code> using the provided data
     * and returns the OID of the newly created process instance.
     *
     * @param id The ID of the process to be started. If multiple models with different IDs
     *     are deployed then the process definition id needs to be qualified with model
     *      model id,
     *            e.g. "{modelId}processDefinitionId"
     * @param data Contains data IDs as keyset and corresponding data values to be set
     *            as values.
     * @param synchronously Determines whether the process will be started synchronously
     *            or asynchronously.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance} that was started.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process with the specified ID
     *     in the
     *             active model or an invalid data id was specified.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#startProcess(
     *     java.lang.String id, java.util.Map data, boolean synchronously)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         startProcess(
         java.lang.String id, java.util.Map data, boolean synchronously,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Spawns a process as subprocess of the specified process instance. The spawned
     * process executes asynchronously but has to be completed before the parent process is
     * able to complete.
     *
     * @param parentProcessInstanceOid
     *               The oid of the process to spawn from.
     * @param spawnProcessID
     *               The id of the process definition to spawn as a subprocess.
     * @param copyData
     *               Defines if data of the parent process definition should be copied to the
     *               spawned process.
     * @param data
     *               Contains data IDs as keyset and corresponding data values to be set as
     *               values.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance} that was spawned.
     *
     * @throws IllegalOperationException
     *                if the process instance is not a case process instance, is not active or
     *                if the process definition is from a different model.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws ObjectNotFoundException
     *                if there is no process instance with the specified oid or if there is no
     *                process definition with the specified id.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnSubprocessInstance(
     *     long parentProcessInstanceOid, java.lang.String spawnProcessID, boolean copyData,
     *     java.util.Map data)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnSubprocessInstance(
         long parentProcessInstanceOid, java.lang.String spawnProcessID, boolean copyData,
         java.util.Map data, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Spawns multiple processes as subprocesses of the specified process instance. The
     * spawned processes execute asynchronously but have to be completed before the parent
     * process is able to complete.
     *
     * @param parentProcessInstanceOid
     *               The oid of the process to spawn from.
     * @param subprocessSpawnInfo
     *               A List of {@link SubprocessSpawnInfo} holding information about the
     *               subprocesses to be spawned.
     *
     * @return A list of {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance} that were spawned.
     *
     * @throws IllegalOperationException
     *                if the process instance is not a case process instance, is not active or
     *                if the process definition is from a different model.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws ObjectNotFoundException
     *                if there is no process instance with the specified oid or if there is no
     *                process definition with the specified id.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnSubprocessInstances(
     *     long parentProcessInstanceOid, java.util.List subprocessSpawnInfo)
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.ProcessInstance>
         spawnSubprocessInstances(
         long parentProcessInstanceOid, java.util.List subprocessSpawnInfo,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Spawns a new root process and creates a link of type
     * {@link ProcessInstanceLinkType#SWITCH} to the specified process instance.<br>
     * Optionally existing data from the specified process instance can be copied to the
     * newly spawned process.
     * <p>
     * Please note that currently the specified process instance has to be aborted by
     * setting <code>abortProcessInstance</code> to <code>true</code>.
     *
     * @param processInstanceOid
     *               The oid of the process to spawn from.
     * @param spawnProcessID
     *               The id of the process definition to spawn as a new root process.
     * @param copyData
     *               Defines if data of the parent process definition should be copied to the
     *               spawned process.
     * @param data
     *               Contains data IDs as keyset and corresponding data values to be set as
     *               values.
     * @param abortProcessInstance
     *               whether the originating process instance should be aborted. <b>Currently
     *               has to be true</b>.
     * @param comment
     *               Allows to specify a comment for the link that is created.
     *
     * @return The {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance} that was spawned.
     *
     * @throws IllegalOperationException
     *                if the process instance is terminated or not a root process instance.
     *     if the process instance and the process definition are from different
     *      different models.
     *     if the process instances process definition is the same as the specified process
     *     ed process definition.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws ObjectNotFoundException
     *                if the process instance for the specified oid or the process definition
     *                for the specified process id is not found.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws InvalidArgumentException
     *                if <code>abortProcessInstance</code> is false (currently not
     *                implemented).
     *     <em>Instances of {@link InvalidArgumentException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#spawnPeerProcessInstance(
     *     long processInstanceOid, java.lang.String spawnProcessID, boolean copyData,
     *     java.util.Map data, boolean abortProcessInstance, java.lang.String comment)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         spawnPeerProcessInstance(
         long processInstanceOid, java.lang.String spawnProcessID, boolean copyData,
         java.util.Map data, boolean abortProcessInstance, java.lang.String comment,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Creates a case process instance which groups the specified members as subprocesses.
     *
     * @param name
     *               The name of the case.
     * @param description
     *               A description for the case.
     * @param memberOids
     *               The oids of the process instances which should become members of the case.
     *
     * @return The case process instance.
     *
     * @See {@link ProcessInstance#isCaseProcessInstance()}
     *
     * @throws ObjectNotFoundException
     *                if one of the process instances referenced by <code>memberOids</code> is
     *                not found.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws IllegalOperationException
     *                if <code>memberOids</code> contains a process instance which is not a
     *                root process.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws InvalidArgumentException
     *                if <code>memberOids</code> is empty or null.
     *     <em>Instances of {@link InvalidArgumentException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#createCase(
     *     java.lang.String name, java.lang.String description, long[] memberOids)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance createCase(
         java.lang.String name, java.lang.String description, long[] memberOids,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Adds the process instances referenced by the specified memberOids to the specified
     * case process instance.
     *
     * @param caseOid
     *               The oid of the case process instance.
     * @param memberOids
     *               The oids of the process instances which should become members of the case.
     *
     * @return The case process instance.
     *
     * @throws ObjectNotFoundException
     *                if one of the process instances referenced by <code>memberOids</code> is
     *                not found.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws IllegalOperationException
     *                if <code>memberOids</code> contains a process instance which is not a
     *                root process or is already a member of the case.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the user is not the owner of the case.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinCase(long caseOid, long[] memberOids)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance joinCase(
         long caseOid, long[] memberOids, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Removes the process instances referenced by the specified memberOids from the
     * specified case process instance.
     *
     * @param caseOid
     *               The oid of the case process instance.
     * @param memberOids
     *               The oids of the process instances which should be removed from the case.
     *
     * @return The case process instance.
     *
     * @throws ObjectNotFoundException
     *                if one of the process instances referenced by <code>memberOids</code> is
     *                not found.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws IllegalOperationException
     *                if <code>memberOids</code> contains a process instance which is not a
     *                root process or is not a member of the case.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws AccessForbiddenException
     *                if the user is not the owner of the case.
     *     <em>Instances of {@link AccessForbiddenException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#leaveCase(long caseOid, long[] memberOids)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance leaveCase(
         long caseOid, long[] memberOids, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Merges the specified source case process instances into the target case process
     * instance
     * by adding all case members of the source case process instances as members of the
     * target case process instance.
     *
     * @param targetCaseOid The target case process instance
     * @param sourceCaseOids The source case process instances.
     * @param comment Allows to specify a comment
     *
     * @return The case process instance.
     *
     * @throws ObjectNotFoundException
     *     if one of the process instances referenced by <code>sourceCaseOids</code>
     *     ids</code> is
     *                not found.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws IllegalOperationException
     *                if <code>sourceCaseOids</code> contains a process instance which is not a
     *                case process instance.
     *     if <code>sourceCaseOids</code> contains a process instance which is not
     *     ich is not active.
     *     if <code>sourceCaseOids</code> contains a process instance which equals the
     *     equals the <code>targetCaseOid</code>.
     *                if <code>targetCaseOid</code> is not a case process instance.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws AccessForbiddenException
     *                if the user is not the owner of the case.
     *     <em>Instances of {@link AccessForbiddenException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#mergeCases(
     *     long targetCaseOid, long[] sourceCaseOids, java.lang.String comment)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance mergeCases(
         long targetCaseOid, long[] sourceCaseOids, java.lang.String comment,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Delegates the case process instance to the specified participant.
     *
     * @param caseOid
     *               The case process instance to delegate.
     * @param participant
     *               The targetParticipant.
     *
     * @return The case process instance.
     *
     * @throws ObjectNotFoundException
     *                if one of the process instances referenced by <code>caseOid</code> is not
     *                found.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws IllegalOperationException
     *                if <code>caseOid</code> is not a case process instance.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws AccessForbiddenException
     *                if the user is not the owner of the case.
     *     <em>Instances of {@link AccessForbiddenException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateCase(
     *     long caseOid, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance delegateCase(
         long caseOid, org.eclipse.stardust.engine.api.model.ParticipantInfo participant,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Aborts the specified process instance and joins the data into the specified target
     * process instance. Existing data values of the target process instance are not
     * overwritten. Process attachments are merged.
     *
     * @param processInstanceOid
     *               The oid of the process instance which should be aborted and joined into
     *               the target process instance.
     * @param targetProcessInstanceOid
     *               The oid of the process instance that should be the target of the join.
     * @param comment
     *               Allows specifying a comment.
     *
     * @return The target process instance.
     *
     * @throws ObjectNotFoundException
     *                if the process instance referenced by <code>processInstanceOid</code> or
     *                <code>targetProcessInstanceOid</code> do not exist.
     *     <em>Instances of {@link ObjectNotFoundException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws IllegalOperationException
     *                if the source and target are identical.<br>
     *                if the source or target are not active.<br>
     *                if the join target is a subprocess of the source process instance.<br>
     *                if the source or target is a case process instance.
     *     <em>Instances of {@link IllegalOperationException
     *     } will be wrapped inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#joinProcessInstance(
     *     long processInstanceOid, long targetProcessInstanceOid, java.lang.String comment)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance joinProcessInstance(
         long processInstanceOid, long targetProcessInstanceOid, java.lang.String comment,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Aborts the specified activity instance, effectively aborting the whole process
     * instance hierarchy this activity instance belongs to.
     * <p/>
     * Aborting an activity instance is only allowed if the activity was modeled to be
     * abortable (@see Activity#isAbortable()}). Additionally it is required that the
     * aborting user is a valid performing participant for this activity.
     * <p/>
     * Behavior is equivalent to
     * {@link WorkflowService#abortActivityInstance(long, AbortScope)}
     * using <code>AbortScope.RootHierarchy</code>.
     * <p/>
     * Note: Abort is performed asynchronously.
     *
     * @param activityInstanceOID The OID of the activity instance to be aborted.
     *
     * @return The {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was aborted.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified
     *             OID in the audit trail.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user is not valid or is not
     *     granted
     *             access to the activity instance. Also thrown if the activity instance is
     *             already terminated or if the activity is not allowed to be aborted.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#abortProcessInstance(long)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long, AbortScope)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(
     *     long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance abortActivityInstance(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Aborts the specified activity instance, effectively aborting the whole process
     * instance hierarchy this activity instance belongs to.
     * <p/>
     * Aborting an activity instance is only allowed if the activity was modeled to be
     * abortable (@see Activity#isAbortable()}). Additionally it is required that the
     * aborting user is a valid performing participant for this activity.
     * <p/>
     * Note: Abort is performed asynchronously.
     *
     * @param activityInstanceOID The OID of the activity instance to be aborted.
     * @param abortScope The scope of abortion. You can either choose the current activity
     *           or the entire process hierarchy.
     *           <br/>If you have chosen <code>AbortScope.SubHierarchy</code> then the specified
     *           activity instance is set to state <code>ActivityInstanceState.Aborting</code>.
     *     The abort itself is performed asynchronously. If activity instance is a
     *      is a subprocess
     *           then the complete subprocess hierarchy will be aborted.
     *           <br/>If you have chosen <code>AbortScope.RootHierarchy</code> abortion is done
     *           starting at the root process instance for specified activity instance. The
     *           specified activity instance will be returned unchanged. The state of the
     *           root process instance will be set to <code>ProcessInstanceState.Aborting</code>.
     *           Abort itself will be performed asynchronously.
     *
     * @return The {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was aborted.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified
     *             OID in the audit trail.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user is not valid or is not
     *     granted
     *             access to the activity instance. Also thrown if the activity instance is
     *             already terminated or if the activity is not allowed to be aborted.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.AdministrationService#abortProcessInstance(long)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(long)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortActivityInstance(
     *     long activityInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance abortActivityInstance(
         long activityInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Aborts the specified process instance. Depending on the scope, it will abort either
     * this process instance only (including eventual subprocesses) or the whole process
     * hierarchy starting with the root process.
     *
     * @param processInstanceOid The OID of the process instance to be aborted.
     * @param abortScope The scope of abortion. You can abort either the spawned process instance
     *            or the entire process hierarchy.
     *
     * @return The {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance} that was aborted.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified
     *             OID in the audit trail.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the current user is not valid or is not
     *     granted
     *             access to abort the process instance.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#abortProcessInstance(
     *     long processInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance abortProcessInstance(
         long processInstanceOid, org.eclipse.stardust.engine.core.runtime.beans.AbortScope abortScope,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * 
     *
     * @deprecated Retrieves the active model.
     *
     * @return the active model.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no active model.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getModel()
     */
    public org.eclipse.stardust.engine.api.runtime.DeployedModel
         getModel(org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves (parts of) the worklist of the currently logged-in user.
     *
     * @param query An instance of class {@link org.eclipse.stardust.engine.api.query.WorklistQuery} describing
     *     the requested
     *            view on the worklist.
     *
     * @return An instance of {@link org.eclipse.stardust.engine.api.query.Worklist} making up the requested view
     *     on the
     *             current user's worklist.
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getWorklist(
     *     org.eclipse.stardust.engine.api.query.WorklistQuery query)
     */
    public org.eclipse.stardust.engine.api.query.Worklist
         getWorklist(
         org.eclipse.stardust.engine.api.query.WorklistQuery query,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Activates the next activity instance from the given worklist query if any.
     *
     * @param worklist query.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was activated.
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstance(
     *     org.eclipse.stardust.engine.api.query.WorklistQuery query)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstance(
         org.eclipse.stardust.engine.api.query.WorklistQuery query,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Activates the next activity instance after the specified one in the same process
     * instance.
     *
     * @param activityInstanceOID the OID of the last completed activity instance.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was activated.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstance(
     *     long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstance(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Activates the next activity instance for the specified process instance.
     *
     * @param processInstanceOID the OID of the process instance.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was activated.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#activateNextActivityInstanceForProcessInstance(
     *     long processInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         activateNextActivityInstanceForProcessInstance(
         long processInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Sets an OUT data path on a process instance as specified in the corresponding
     * process definition.
     *
     * @param processInstanceOID the OID of the process instance.
     * @param id the ID of the data path as defined in the model.
     * @param object the value to set on the data path.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *             specified OID, if there is no data path with the specified id or if the
     *             underlying data is not found.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidValueException if the <code>object</object> to be written
     *     represents
     *             an invalid value, most probably as of a type conflict in case of statically
     *             typed data.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidValueException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #setOutDataPaths(long, Map)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setOutDataPath(
     *     long processInstanceOID, java.lang.String id, java.lang.Object object)
     */
    public void setOutDataPath(
         long processInstanceOID, java.lang.String id, java.lang.Object object,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Sets multiple OUT data paths on a process instance as specified in the corresponding
     * process definition.
     *
     * @param processInstanceOID the OID of the process instance.
     * @param values A map of (id, value) pairs to be set, where every ID has to designate
     *           a valid data path as defined in the model.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *             specified OID, if there is no data path with the specified id or if the
     *             underlying data is not found.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidValueException if one of the <code>values</object> to be
     *     written
     *             represents is invalid, most probably as of a type conflict in case of
     *             statically typed data.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidValueException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #setOutDataPath(long, String, Object)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setOutDataPaths(
     *     long processInstanceOID, java.util.Map values)
     */
    public void setOutDataPaths(
         long processInstanceOID, java.util.Map values,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves an IN data path on a process instance as specified in the corresponding
     * process definition.
     *
     * @param processInstanceOID the OID of the process instance.
     * @param id the ID of the data path as defined in the model.
     *
     * @return the value of the data path applied to the process instance.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *             specified OID, if there is no data path with the specified id or if the
     *             underlying data is not found.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #getInDataPaths(long, Set)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataPath(
     *     long processInstanceOID, java.lang.String id)
     */
    public java.lang.Object getInDataPath(
         long processInstanceOID, java.lang.String id,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves multiple IN data paths from a process instance as specified in the
     * corresponding process definition.
     *
     * @param processInstanceOID the OID of the process instance.
     * @param ids the set of data path IDs designating the values to be retrieved. If
     *            <code>null</code> is passed, all IN data paths for the process instance are
     *            retrieved.
     *
     * @return the values of the data paths applied to the process instance.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *             specified OID, if there is no data path with the specified id or if the
     *             underlying data is not found.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #getInDataPath(long, String)
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getInDataPaths(
     *     long processInstanceOID, java.util.Set ids)
     */
    public java.util.Map<java.lang.String,java.io.Serializable>
         getInDataPaths(
         long processInstanceOID, java.util.Set ids,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Delegates the specified activitiy instance to the default worklist of the
     * corresponding activity.
     *
     * @param activityInstanceOID the OID of the activity instance.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was delegated.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the activity instance is already
     *     terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToDefaultPerformer(
     *     long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         delegateToDefaultPerformer(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Delegates the specified activity instance to a specific performer.
     *
     * @param activityInstanceOID the OID of the activity instance.
     * @param userOID the OID of the user to which the activity instance will be delegated.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was delegated.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance or user with
     *     the specified OIDs.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the delegation target is not granted to
     *     execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToUser(
     *     long activityInstanceOID, long userOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance delegateToUser(
         long activityInstanceOID, long userOID,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Delegates the specified activity instance to a specific performer.
     *
     * @param activityInstanceOID the OID of the activity instance.
     * @param performer the ID of the performer to which the activity instance will be delegated.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was delegated.
     *
     * @throws org.eclipse.stardust.common.error.ConcurrencyException if the same activity instance is being processed
     *     by another user.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ConcurrencyException} will be wrapped inside
     *     {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the delegation target is not granted to
     *     execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToParticipant(
     *     long activityInstanceOID, java.lang.String performer)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance delegateToParticipant(
         long activityInstanceOID, java.lang.String performer,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Delegates the activity instance to the specified participant as follows:
     * <ul>
     * <li>if the participant is null, then delegates the activity to the default
     * performer.</li>
     * <li>if the participant is an instance of a UserInfo, then delegates the activity to
     * the specified user.</li>
     * <li>if the participant is an instance of a UserGroupInfo, then delegates the activity
     * to the specified user group.</li>
     * <li>if the participant is an instance of a ModelParticipantInfo, then delegates the
     * activity to the specified model participant.</li>
     * </ul>
     *
     * @param activityInstanceOID the OID of the activity instance.
     * @param participant the participant (
     *     model participant, user or user group) to which the activity instance will be
     *     delegated.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} that was delegated.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID or if the participant is not null
     *             and could not be resolved to an actual user user group or model participant.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the activity instance is already
     *     terminated or is
     *             currently processed by another user or the current user does not have the
     *             required permission or if the delegation target is not granted to execute
     *             the activity instance or if the activity instance is already terminated.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#delegateToParticipant(
     *     long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance delegateToParticipant(
         long activityInstanceOID, org.eclipse.stardust.engine.api.model.ParticipantInfo participant,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves the specified ActivityInstance.
     *
     * @param activityInstanceOID the OID of the activity instance.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getActivityInstance(
     *     long activityInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance getActivityInstance(
         long activityInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves the specified process instance.
     *
     * @param processInstanceOID the OID of the process instance.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessInstance(long processInstanceOID)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance getProcessInstance(
         long processInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * TODO
     *
     * @param processInstanceOID the OID of the process instance.
     *
     * @return TODO
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.AccessForbiddenException if the process instance is not completed or
     *           the user does not have the permission to access this process.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.AccessForbiddenException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessResults(long processInstanceOID)
     */
    public java.util.Map<java.lang.String,java.io.Serializable>
         getProcessResults(
         long processInstanceOID, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext
         __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Binds an event handler to the specified activity instance.
     *
     * @param activityInstanceOID the OID of the activity instance.
     * @param eventHandler the specialized form of the event handler to bind.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.BindingException in case of semantic binding errors.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.BindingException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.common.error.InvalidArgumentException in case eventHandler is null.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.InvalidArgumentException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #getActivityInstanceEventHandler
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindActivityEventHandler(
     *     long activityInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         bindActivityEventHandler(
         long activityInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         eventHandler, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Binds an event handler to the specified process instance.
     *
     * @param processInstanceOID the OID of the process instance.
     * @param eventHandler the specialized form of the event handler to bind.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.BindingException in case of semantic binding errors.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.BindingException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see #getProcessInstanceEventHandler
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindProcessEventHandler(
     *     long processInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding eventHandler)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         bindProcessEventHandler(
         long processInstanceOID, org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         eventHandler, org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Binds an event handler to the specified activity instance.
     *
     * @param activityInstanceOID the OID of the activity instance.
     * @param handler the ID of the event handler to bind.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.BindingException in case of semantic binding errors.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.BindingException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindActivityEventHandler(
     *     long activityInstanceOID, java.lang.String handler)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         bindActivityEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Binds an event handler to the specified process instance.
     *
     * @param processInstanceOID the OID of the process instance.
     * @param handler the ID of the event handler to bind.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.BindingException in case of semantic binding errors.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.BindingException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#bindProcessEventHandler(
     *     long processInstanceOID, java.lang.String handler)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         bindProcessEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Unbinds an event handler from the specified activity instance.
     *
     * @param activityInstanceOID the OID of the activity instance.
     * @param handler the ID of the event handler to unbind.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.BindingException in case of semantic binding errors.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.BindingException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#unbindActivityEventHandler(
     *     long activityInstanceOID, java.lang.String handler)
     */
    public org.eclipse.stardust.engine.api.runtime.ActivityInstance
         unbindActivityEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Unbinds an event handler from the specified process instance.
     *
     * @param processInstanceOID the OID of the process instance.
     * @param handler the ID of the event handler to unbind.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.runtime.BindingException in case of semantic binding errors.
     *     <em>Instances of {@link org.eclipse.stardust.engine.api.runtime.BindingException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#unbindProcessEventHandler(
     *     long processInstanceOID, java.lang.String handler)
     */
    public org.eclipse.stardust.engine.api.runtime.ProcessInstance
         unbindProcessEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Gets the binding state of an event handler for the specified activity instance.
     *
     * @param activityInstanceOID the OID of the activity instance.
     * @param handler the ID of the event handler.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.EventHandlerBinding}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no activity instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getActivityInstanceEventHandler(
     *     long activityInstanceOID, java.lang.String handler)
     */
    public org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         getActivityInstanceEventHandler(
         long activityInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Gets the binding state of an event handler for the specified process instance.
     *
     * @param processInstanceOID the OID of the process instance.
     * @param handler the ID of the event handler.
     *
     * @return the {@link org.eclipse.stardust.engine.api.runtime.EventHandlerBinding}.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getProcessInstanceEventHandler(
     *     long processInstanceOID, java.lang.String handler)
     */
    public org.eclipse.stardust.engine.api.runtime.EventHandlerBinding
         getProcessInstanceEventHandler(
         long processInstanceOID, java.lang.String handler,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves the list of process definitions that can be started by the current user.
     *
     * @return a List with {@link org.eclipse.stardust.engine.api.model.ProcessDefinition} objects.
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getStartableProcessDefinitions()
     */
    public java.util.List<org.eclipse.stardust.engine.api.model.ProcessDefinition>
         getStartableProcessDefinitions(
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves information on the current user.
     *
     * @return the current user.
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getUser()
     */
    public org.eclipse.stardust.engine.api.runtime.User
         getUser(org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
    {
      java.util.Map __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.WorkflowService) service).getUser();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Retrieves all permissions the current user has on this service.
     *
     * @return a list of permission ids.
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#getPermissions()
     */
    public java.util.List<org.eclipse.stardust.engine.api.runtime.Permission>
         getPermissions(
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Sets specific attributes of a process instance.
     * At the moment attributes has to be bound to a scope process instance.
     * <br/>
     * <br/>
     * Note: After a {@link ProcessInstanceAttributes} instance is applied to this method
     * it is discouraged to use this same instance again. Any new note which has been added
     * by the first use will be added again. In order to add new notes to a certain
     * process instance a fresh {@link ProcessInstance} has to be retrieved (e.g. by
     * {@link WorkflowService#getProcessInstance(long)}). Get a current copy of
     * {@link ProcessInstanceAttributes} by {@link ProcessInstance#getAttributes()}.
     *
     * @param attributes the container of attributes.
     *
     * @throws org.eclipse.stardust.common.error.ObjectNotFoundException if there is no process instance with the
     *     specified OID.
     *     <em>Instances of {@link org.eclipse.stardust.common.error.ObjectNotFoundException} will be wrapped
     *     inside {@link org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws PublicException if the process instance is no scope process instance.
     *     <em>Instances of {@link PublicException} will be wrapped inside {@link
     *     org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws InvalidArgumentException if attributes is null.
     *     <em>Instances of {@link InvalidArgumentException} will be wrapped inside {@link
     *     org.eclipse.stardust.engine.api.ejb2.WorkflowException}.</em>
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setProcessInstanceAttributes(
     *     org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes attributes)
     */
    public void
         setProcessInstanceAttributes(
         org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes attributes,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#setActivityInstanceAttributes(
     *     org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes attributes)
     */
    public void
         setActivityInstanceAttributes(
         org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes attributes,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * Logs an audit trail event of type <code>LogCode.EXTERNAL</code>.
     *
     * @param logType Set the type of log (
     *     info, warn, error etc.). Whereas the <code>Unknown</code> type is mapped to a warning.
     * @param contextType Set the context scope of the event
     * @param contextOid Oid of the runtime object (
     *     only used if context type is set to ProcessInstance or ActivityInstance)
     * @param message any message that should be logged
     * @param throwable any exception (or null) that should be appended to the message
     *
     * @exception ObjectNotFoundException if there is no runtime object with the specified OID
     *
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#writeLogEntry(
     *     org.eclipse.stardust.engine.api.runtime.LogType logType,
     *     org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
     *     java.lang.String message, java.lang.Throwable throwable)
     */
    public void writeLogEntry(
         org.eclipse.stardust.engine.api.runtime.LogType logType,
         org.eclipse.stardust.engine.api.dto.ContextKind contextType, long contextOid,
         java.lang.String message, java.lang.Throwable throwable,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    /**
     * @throws org.eclipse.stardust.engine.api.ejb2.WorkflowException as a wrapper for
     *         org.eclipse.stardust.engine.api.ejb2.PublicExceptions and org.eclipse.stardust.engine.api.ejb2.ResourceExceptions
     *
     * @see org.eclipse.stardust.engine.api.runtime.WorkflowService#execute(
     *     org.eclipse.stardust.engine.core.runtime.command.ServiceCommand serviceCmd)
     */
    public java.io.Serializable
         execute(
         org.eclipse.stardust.engine.core.runtime.command.ServiceCommand serviceCmd,
         org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext __tunneledContext)
         throws org.eclipse.stardust.engine.api.ejb2.WorkflowException
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
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.engine.api.ejb2.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

    public void ejbCreate() throws javax.ejb.CreateException
    {
      super.init(org.eclipse.stardust.engine.api.runtime.WorkflowService.class,
            org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl.class);
    }
}