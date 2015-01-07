/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.runtime.beans.SerialActivityThreadWorkerCarrier.SERIAL_ACTIVITY_THREAD_MAP_ID;

import java.util.Map;
import java.util.Queue;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusterSafeObjectProviderHolder;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceSupport;

/**
 * <p>
 * This {@link ActionCarrier} holds the {@link Action} cancelling a transient process instance execution
 * (see {@link CancelTransientExecutionAction}).
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class CancelTransientExecutionActionCarrier extends ActionCarrier<Void>
{
   private static final long serialVersionUID = -5673833660755363283L;
   
   private static final String ROOT_PROCESS_INSTANCE_OID_NAME = "RootProcessInstanceOID";
   
   private Long rootPiOID;
   
   /**
    * The constructor initializing an object of this class.
    */
   public CancelTransientExecutionActionCarrier()
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
   }
   
   /**
    * <p>
    * Sets the given root process instance OID of the process instance this carrier is working on.
    * </p>
    * 
    * @param rootPiOID the root process instance OID of the process instance this carrier is working on
    */
   public void setRootProcessInstanceOid(final long rootPiOID)
   {
      this.rootPiOID = rootPiOID;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier#doCreateAction()
    */
   @Override
   public Action<Void> doCreateAction()
   {
      return new CancelTransientExecutionAction(this);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier#doFillMessage(javax.jms.Message)
    */
   @Override
   protected void doFillMessage(final Message message) throws JMSException
   {
      ensureIsMapMessage(message);
      ensureMandatoryFieldIsInitialized();
      
      final MapMessage mapMsg = (MapMessage) message;
      mapMsg.setLong(ROOT_PROCESS_INSTANCE_OID_NAME, rootPiOID);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier#doExtract(javax.jms.Message)
    */
   @Override
   protected void doExtract(final Message message) throws JMSException
   {
      ensureIsMapMessage(message);
      
      final MapMessage mapMsg = (MapMessage) message;
      this.rootPiOID = mapMsg.getLong(ROOT_PROCESS_INSTANCE_OID_NAME);
      
      ensureMandatoryFieldIsInitialized();
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "Cancel Transient Execution Action Carrier: root pi OID = " + rootPiOID;
   }
   
   private void ensureIsMapMessage(final Message message)
   {
      if ( !(message instanceof MapMessage))
      {
         throw new IllegalArgumentException("Map message expected.");
      }
   }
   
   private void ensureMandatoryFieldIsInitialized()
   {
      if (rootPiOID == null)
      {
         throw new IllegalStateException("Root Process Instance OID must be initialized.");
      }
   }
   
   /**
    * <p>
    * The {@link Action} cancelling a transient process instance execution involving the following steps:
    * <ul>
    *   <li>Marking the process instance as {@link AuditTrailPersistence#IMMEDIATE}.</li>
    *   <li>Cleaning up data structures used for transient process instance execution.</li>
    *   <li>Writing the process instance into the Audit Trail database.</li>
    * </ul>
    * </p>
    */
   private static final class CancelTransientExecutionAction extends SecurityContextAwareAction<Void>
   {
      private final long rootPiOID;
      
      private Map<Long, Queue> activityThreadMap;
      
      /**
       * <p>
       * The constructor initializing an object of this class with the {@link ActionCarrier}
       * encapsulating the {@link Action} to execute.
       * </p>
       * 
       * @param carrier the carrier encapsulating the {@link Action} to execute
       */
      public CancelTransientExecutionAction(final CancelTransientExecutionActionCarrier carrier)
      {
         super(carrier);
         
         this.rootPiOID = carrier.rootPiOID.longValue();
      }
      
      /* (non-Javadoc)
       * @see org.eclipse.stardust.common.Action#execute()
       */
      @Override
      public Void execute()
      {
         try
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.beforeAccess();
            
            activityThreadMap  = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(SERIAL_ACTIVITY_THREAD_MAP_ID);
            removeCancellationMarker();
            removeScheduledTransientActivityThreads();
            forceWriteIntoAuditTrail();
         }
         catch (final Exception e)
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.exception(e);
            throw new InternalException(e);
         }
         finally
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.afterAccess();
         }
         
         return null;
      }
      
      /* (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return "Cancel Transient Execution Action: root pi OID = " + rootPiOID;
      }
      
      private void removeCancellationMarker()
      {
         activityThreadMap.remove(-rootPiOID);
      }
      
      private void removeScheduledTransientActivityThreads()
      {
         activityThreadMap.remove(rootPiOID);
      }
      
      private void forceWriteIntoAuditTrail()
      {
         /* load the whole PI blob from the in-memory storage ... */
         final Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         TransientProcessInstanceSupport.loadProcessInstanceGraphIfExistent(rootPiOID, session);
         
         /* ... and set the process instance to be interrupted so that during Session#flush() */
         /* the PI blob will be purged from the in-memory storage and written into the db     */
         final ProcessInstanceBean rootPi = ProcessInstanceBean.findByOID(rootPiOID);
         rootPi.interrupt();
      }
   }
}
