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
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusterSafeObjectProviderHolder;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class SerialActivityThreadCarrier extends ActionCarrier<Void>
{
   private static final long serialVersionUID = 1308240032670965545L;

   public static final String SERIAL_ACTIVITY_THREAD_CARRIER_MAP_ID = "SerialActivityThreadCarrierMap";
   
   private static final String ROOT_PROCESS_INSTANCE_OID_NAME = "RootProcessInstanceOID";
   
   private Long rootPiOID;
   
   public SerialActivityThreadCarrier()
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
      return "Serial Activity Thread Carrier: root pi = " + rootPiOID;
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
      
      public SerialActivityThreadRunner(final SerialActivityThreadCarrier carrier)
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
         finally
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.afterAccess();
         }
         
         return null;
      }
      
      @Override
      public String toString()
      {
         return "Serial Activity Thread: root pi = " + rootPiOID;
      }
      
      private void doExecute()
      {
         activityThreadMap = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(SERIAL_ACTIVITY_THREAD_CARRIER_MAP_ID);
         final Queue<SerialActivityThreadData> beforeExecutionQueue = retrieveQueue();
         final ActivityThread activityThread = initActivityThread(beforeExecutionQueue);
         
         if ( !activityThread.processInstance().isTransient())
         {
            scheduleSystemQueueActivityThreads(activityThread, beforeExecutionQueue);
            activityThreadMap.remove(rootPiOID);
         }
         else
         {
            activityThread.run();
      
            final Queue<SerialActivityThreadData> afterExecutionQueue = retrieveQueue();   
            if (afterExecutionQueue.peek() != null)
            {
               scheduleNextSerialActivityThread();
            }
            else
            {
               activityThreadMap.remove(rootPiOID);
            }
         }
      }
      
      private <T> Queue<T> retrieveQueue()
      {
         return activityThreadMap.get(rootPiOID);
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
         // TODO (nw) remove deprecated API usage
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
         
         fork(new ForkAction()
         {
            @Override
            public void fork(final ForkingService forkingService)
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
      
      private void scheduleNextSerialActivityThread()
      {
         final SerialActivityThreadCarrier carrier = new SerialActivityThreadCarrier();
         carrier.setRootProcessInstanceOid(rootPiOID);
         
         fork(new ForkAction()
         {
            @Override
            public void fork(final ForkingService forkingService)
            {
               forkingService.fork(carrier, true);
            }
         });
      }
      
      private void fork(final ForkAction action)
      {
         final ForkingServiceFactory fsFactory = (ForkingServiceFactory) Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
         ForkingService forkingService = null;
         try
         {
            forkingService = fsFactory.get();
            action.fork(forkingService);
         }
         finally
         {
            fsFactory.release(forkingService);
         }         
      }
      
      private static interface ForkAction
      {
         void fork(final ForkingService forkingService);
      }
   }  
}
