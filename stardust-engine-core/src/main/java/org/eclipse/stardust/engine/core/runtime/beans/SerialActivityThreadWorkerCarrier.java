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

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusterSafeObjectProviderHolder;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceSupport;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class SerialActivityThreadWorkerCarrier extends ActionCarrier<Void>
{
   private static final long serialVersionUID = 1308240032670965545L;

   public static final String SERIAL_ACTIVITY_THREAD_MAP_ID = "SerialActivityThreadCarrierMap";
   
   private static final String ROOT_PROCESS_INSTANCE_OID_NAME = "RootProcessInstanceOID";
   
   private Long rootPiOID;
   
   public SerialActivityThreadWorkerCarrier()
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
   }
   
   public void setRootProcessInstanceOid(final long rootPiOID)
   {
      this.rootPiOID = rootPiOID;
   }
   
   @Override
   public Action<Void> doCreateAction()
   {
      return new SerialActivityThreadRunner(this);
   }

   @Override
   protected void doFillMessage(final Message message) throws JMSException
   {
      ensureIsMapMessage(message);
      ensureMandatoryFieldIsInitialized();
      
      final MapMessage mapMsg = (MapMessage) message;
      mapMsg.setLong(ROOT_PROCESS_INSTANCE_OID_NAME, rootPiOID);
   }

   @Override
   protected void doExtract(final Message message) throws JMSException
   {
      ensureIsMapMessage(message);
      
      final MapMessage mapMsg = (MapMessage) message;
      this.rootPiOID = mapMsg.getLong(ROOT_PROCESS_INSTANCE_OID_NAME);
      
      ensureMandatoryFieldIsInitialized();
   }
   
   @Override
   public String toString()
   {
      return "Serial Activity Thread Carrier: root pi OID = " + rootPiOID;
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

   
   private static final class SerialActivityThreadRunner extends SecurityContextAwareAction<Void>
   {
      private final long rootPiOID;

      private Map<Long, Queue> activityThreadMap;
      
      public SerialActivityThreadRunner(final SerialActivityThreadWorkerCarrier carrier)
      {
         super(carrier);
         
         this.rootPiOID = carrier.rootPiOID.longValue();
      }
      
      @Override
      public Void execute()
      {
         try
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.beforeAccess();
            
            doExecute();
         }
         catch (final Exception e)
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.exception(e);
            scheduleCancellationOfTransientProcessing();
            throw new InternalException(e);
         }
         finally
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.afterAccess();
         }
         
         return null;
      }
      
      @Override
      public String toString()
      {
         return "Serial Activity Thread: root pi OID = " + rootPiOID;
      }
      
      private void doExecute()
      {
         activityThreadMap = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(SERIAL_ACTIVITY_THREAD_MAP_ID);
         final Queue<SerialActivityThreadData> beforeExecutionQueue = retrieveQueueConsideringCancellationOfTransientExecution();
         loadProcessInstanceGraphIfExistent();
         final ActivityThread activityThread = initActivityThread(beforeExecutionQueue);
         
         final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI(activityThread.processInstance());
         if (rootPi.getAuditTrailPersistence() == AuditTrailPersistence.IMMEDIATE)
         {
            scheduleSystemQueueActivityThreads(activityThread, beforeExecutionQueue);
            activityThreadMap.remove(rootPiOID);
         }
         else
         {
            activityThread.run();
      
            final Queue<SerialActivityThreadData> afterExecutionQueue = activityThreadMap.get(rootPiOID);   
            if (afterExecutionQueue.peek() != null)
            {
               scheduleNextSerialActivityThreadWorker();
            }
            else
            {
               activityThreadMap.remove(rootPiOID);
            }
         }
      }
      
      private <T> Queue<T> retrieveQueueConsideringCancellationOfTransientExecution()
      {
         if (activityThreadMap.containsKey(-rootPiOID))
         {
            /* transient process execution has been cancelled */
            throw new IllegalStateException("Transient process instance execution has already been cancelled.");
         }
         
         final Queue<T> result = activityThreadMap.get(rootPiOID);
         if (result == null)
         {
            /* transient process execution has been cancelled */
            throw new IllegalStateException("Transient process instance execution has already been cancelled.");
         }
         
         return result;
      }
      
      private void loadProcessInstanceGraphIfExistent()
      {
         final Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         TransientProcessInstanceSupport.loadProcessInstanceGraphIfExistent(rootPiOID, session);
      }
      
      private ActivityThread initActivityThread(final Queue<SerialActivityThreadData> queue)
      {
         final SerialActivityThreadData data = queue.poll();
         if (data == null)
         {
            throw new IllegalStateException("Activity Thread Queue must not be empty.");
         }
         
         /* explicitly override modified queue in cluster safe map */
         /* in order to notify cluster safe map provider           */
         activityThreadMap.put(rootPiOID, queue);
         
         final ActivityThread result = createActivityThreadFor(data.piOID(), data.activityOID());
         return result;
      }
      
      private ActivityThread createActivityThreadFor(final long piOID, final long activityOID)
      {
         final IProcessInstance pi = ProcessInstanceBean.findByOID(piOID);
         @SuppressWarnings("deprecation")
         final IActivity activity = (IActivity) ModelManagerFactory.getCurrent().lookupObjectByOID(activityOID);
         
         return new ActivityThread(pi, activity, null, null, Collections.emptyMap(), false);
      }
      
      private void scheduleSystemQueueActivityThreads(final ActivityThread activityThread, final Queue<SerialActivityThreadData> queue)
      {
         final Set<ActivityThreadCarrier> activityThreadCarrierSet = newHashSet();
         activityThreadCarrierSet.add(toActivityThreadCarrier(activityThread));
         SerialActivityThreadData data;
         while ((data = queue.poll()) != null)
         {
            activityThreadCarrierSet.add(data.toActivityThreadCarrier());
         }
         
         doWithForkingService(new ForkingServiceAction()
         {
            @Override
            public void doWithForkingService(final ForkingService forkingService)
            {
               for (final ActivityThreadCarrier atc : activityThreadCarrierSet)
               {
                  forkingService.fork(atc, true);
               }
            }
         });
      }
      
      private ActivityThreadCarrier toActivityThreadCarrier(final ActivityThread activityThread)
      {
         final ActivityThreadCarrier carrier = new ActivityThreadCarrier();
         carrier.setProcessInstanceOID(activityThread.processInstance().getOID());
         carrier.setActivityOID(activityThread.activity().getOID());
         return carrier;
      }
      
      private void scheduleNextSerialActivityThreadWorker()
      {
         final SerialActivityThreadWorkerCarrier carrier = new SerialActivityThreadWorkerCarrier();
         carrier.setRootProcessInstanceOid(rootPiOID);
         
         doWithForkingService(new ForkingServiceAction()
         {
            @Override
            public void doWithForkingService(final ForkingService forkingService)
            {
               forkingService.fork(carrier, true);
            }
         });
      }
      
      private void scheduleCancellationOfTransientProcessing()
      {
         if (hasTransientExecutionBeenCancelled())
         {
            /* nothing to do: already cancelled */
            return;
         }
         
         doWithForkingService(new ForkingServiceAction()
         {
            @Override
            public void doWithForkingService(final ForkingService forkingService)
            {
               final IsolatedCancellationAction action = new IsolatedCancellationAction(SerialActivityThreadRunner.this);
               forkingService.isolate(action);
            }
         });
      }
      
      private boolean hasTransientExecutionBeenCancelled()
      {
         final boolean markedForCancellation = activityThreadMap.containsKey(-rootPiOID);
         final boolean cancelled = activityThreadMap.get(rootPiOID) == null;
         
         return markedForCancellation || cancelled;
      }
      
      private void doWithForkingService(final ForkingServiceAction action)
      {
         final ForkingServiceFactory fsFactory = (ForkingServiceFactory) Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
         ForkingService forkingService = null;
         try
         {
            forkingService = fsFactory.get();
            action.doWithForkingService(forkingService);
         }
         finally
         {
            fsFactory.release(forkingService);
         }         
      }
      
      private static interface ForkingServiceAction
      {
         void doWithForkingService(final ForkingService forkingService);
      }
      
      private static final class IsolatedCancellationAction implements Action<Void>
      {
         final SerialActivityThreadRunner obj;
         
         public IsolatedCancellationAction(final SerialActivityThreadRunner obj)
         {
            this.obj = obj;
         }
         
         @Override
         public Void execute()
         {
            /* first mark the root process instance to be cancelled ... */
            obj.activityThreadMap.put(-obj.rootPiOID, new LinkedList<SerialActivityThreadData>());
            
            /* ... then schedule a new thread swapping the transient process instance to the audit trail db */
            obj.doWithForkingService(new ForkingServiceAction()
            {
               @Override
               public void doWithForkingService(final ForkingService forkingService)
               {
                  final CancelTransientExecutionActionCarrier carrier = new CancelTransientExecutionActionCarrier();
                  carrier.setRootProcessInstanceOid(obj.rootPiOID);
                  forkingService.fork(carrier, true);
               }
            });
            
            return null;
         }
      }
   }  
}
