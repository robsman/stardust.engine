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
package org.eclipse.stardust.test.api.util;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IActivityInstanceMonitor;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <p>
 * Allows to wait for an activity instance state change.
 * </p>
 * 
 * <p>
 * The default timeout when waiting for a state change is
 * 5 seconds.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ActivityInstanceStateBarrier
{
   private static WaitTimeout timeout = new WaitTimeout(5, TimeUnit.SECONDS);
   
   private static ActivityInstanceStateBarrier instance;

   private final Map<Long, ActivityInstanceInfo> aiStates;
   
   private volatile ActivityInstanceStateCondition aiStateCondition;
   private volatile ActivityInstanceAliveCondition aiAliveCondition;
   
   /**
    * <p>
    * Returns the one and only instance of this class.
    * </p>
    * 
    * @return the one and only instance of this class
    */
   public synchronized static ActivityInstanceStateBarrier instance()
   {
      return (instance != null) 
         ? instance 
         : (instance = new ActivityInstanceStateBarrier());
   }
   
   /**
    * <p>
    * Allows for changing the timeout when waiting for a state change.
    * </p>
    * 
    * @param timeout the timeout to set
    */
   public synchronized static void setTimeout(final WaitTimeout timeout)
   {
      if (timeout == null)
      {
         throw new NullPointerException("Timeout must not be null.");
      }
      
      ActivityInstanceStateBarrier.timeout = timeout;
   }
   
   /**
    * <p>
    * Waits until the given activity instance is in the given state or the timeout has exceeded. It's only
    * allowed to wait for one condition at a time.
    * </p>
    * 
    * @param aiOid the OID of the activity instance to wait for
    * @param aiState the state to wait for
    * 
    * @throws IllegalStateException if one tries to wait for more than one condition at a time
    * @throws TimeoutException if the condition is still not met, but the timeout has exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public void await(final long aiOid, final ActivityInstanceState aiState) throws IllegalStateException, TimeoutException, InterruptedException
   {
      if (aiState == null)
      {
         throw new NullPointerException("Activity instance state must not be null.");
      }
      
      if (isActivityInstanceStateConditionMet(aiOid, aiState))
      {
         return;
      }
      
      initAiStateCondition(aiOid, aiState);
      
      try
      {
         final boolean success = aiStateCondition.latch().await(timeout.time(), timeout.unit());
         if ( !success)
         {
            throw new TimeoutException("Activity instance is still not in the state '" + aiState + "'.");
         }
      }
      finally
      {
         aiStateCondition = null;
      }
   }
   
   /**
    * <p>
    * Waits until the there's an alive activity instance for the given process instance or the timeout has exceeded. It's only
    * allowed to wait for one condition at a time.
    * </p>
    * 
    * @param piOid the OID of the process instance enclosing the activity instance to wait for
    * 
    * @throws IllegalStateException if one tries to wait for more than one condition at a time
    * @throws TimeoutException if the condition is still not met, but the timeout has exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public void awaitAliveActivityInstance(final long piOid) throws IllegalStateException, TimeoutException, InterruptedException
   {
      if (isActivityInstanceAliveConditionMet(piOid))
      {
         return;
      }
      
      initAiAliveCondition(piOid);
      
      try
      {
         final boolean success = aiAliveCondition.latch().await(timeout.time(), timeout.unit());
         if ( !success)
         {
            throw new TimeoutException("Activity instance is still not alive.");
         }
      }
      finally
      {
         aiAliveCondition = null;
      }
   }

   /**
    * needs to be synchronized since it reads the field {@link ActivityInstanceStateBarrier#aiStates},
    * which is accessed concurrently
    */
   private synchronized boolean isActivityInstanceStateConditionMet(final long aiOid, final ActivityInstanceState aiState)
   {
      if (aiState.equals(aiStates.get(aiOid)))
      {
         return true;
      }
      
      return false;
   }

   private synchronized void initAiStateCondition(final long aiOid, final ActivityInstanceState aiState)
   {
      if (aiStateCondition != null)
      {
         throw new IllegalStateException("It's not allowed to wait for more than one condition at a time.");
      }
      
      aiStateCondition = new ActivityInstanceStateCondition(aiOid, aiState);
   }
   
   /**
    * needs to be synchronized since it reads the field {@link ActivityInstanceStateBarrier#aiStates},
    * which is accessed concurrently
    */
   private synchronized boolean isActivityInstanceAliveConditionMet(final long piOid)
   {
      for (final ActivityInstanceInfo aiInfo : aiStates.values())
      {
         if (aiInfo.piOid() == piOid && activityInstanceIsAlive(aiInfo.aiState()))
         {
            return true;
         }
      }
      
      return false;
   }

   private synchronized void initAiAliveCondition(final long piOid)
   {
      if (aiAliveCondition != null)
      {
         throw new IllegalStateException("It's not allowed to wait for more than one condition at a time.");
      }
      
      aiAliveCondition = new ActivityInstanceAliveCondition(piOid);
   }
   
   /**
    * needs to be synchronized since it modifies the field {@link ActivityInstanceStateBarrier#aiStates},
    * which is accessed concurrently
    */
   private synchronized void stateChanged(final long aiOid, final long piOid, final ActivityInstanceState aiState)
   {
      aiStates.put(aiOid, new ActivityInstanceInfo(piOid, aiState));
      
      if (aiStateCondition != null)
      {
         if (aiStateCondition.aiOid() == aiOid && aiStateCondition.aiState().equals(aiState));
         {
            aiStateCondition.latch().countDown();
         }
      }
      if (aiAliveCondition != null)
      {
         if (aiAliveCondition.piOid() == piOid && activityInstanceIsAlive(aiState))
         {
            aiAliveCondition.latch().countDown();
         }
      }
   }
   
   private boolean activityInstanceIsAlive(final ActivityInstanceState aiState)
   {
      if ( !aiState.equals(ActivityInstanceState.Completed) && !aiState.equals(ActivityInstanceState.Aborted)) 
      {
         return true;
      }
      
      return false;
   }
   
   /**
    * private constructor for singleton implementation
    */
   private ActivityInstanceStateBarrier()
   {
      aiStates = newHashMap();
   }
   
   /**
    * <p>
    * Monitors the activity instance state changes.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class ActivityInstanceStateChangeMonitor implements IActivityInstanceMonitor
   {
      /*
       * (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.spi.monitoring.IActivityInstanceMonitor#activityInstanceStateChanged(org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance, int)
       */
      @Override
      public void activityInstanceStateChanged(final IActivityInstance ai, final int aiState)
      {
         final TransactionAwareMonitor monitor = new TransactionAwareMonitor(ai.getOID(), ai.getProcessInstanceOID(), ActivityInstanceState.getState(aiState));
         TransactionSynchronizationManager.registerSynchronization(monitor);
      }
   }
   
   private static final class TransactionAwareMonitor extends TransactionSynchronizationAdapter
   {
      private final long aiOid;
      private final long piOid;
      private final ActivityInstanceState aiState;
      
      public TransactionAwareMonitor(final long aiOid, final long piOid, final ActivityInstanceState aiState)
      {
         this.aiOid = aiOid;
         this.piOid = piOid;
         this.aiState = aiState;
      }
      
      /*
       * (non-Javadoc)
       * @see org.springframework.transaction.support.TransactionSynchronization#afterCompletion(int)
       */
      @Override
      public void afterCompletion(final int status)
      {
         instance().stateChanged(aiOid, piOid, aiState);
      }
   }
   
   private static final class ActivityInstanceStateCondition
   {
      private final long aiOid;
      private final ActivityInstanceState aiState;
      private final CountDownLatch latch;
      
      public ActivityInstanceStateCondition(final long aiOid, final ActivityInstanceState aiState)
      {
         this.aiOid = aiOid;
         this.aiState = aiState;
         this.latch = new CountDownLatch(1);
      }
      
      public long aiOid()
      {
         return aiOid;
      }
      
      public ActivityInstanceState aiState()
      {
         return aiState;
      }
      
      public CountDownLatch latch()
      {
         return latch;
      }
   }

   private static final class ActivityInstanceAliveCondition
   {
      private final long piOid;
      private final CountDownLatch latch;
      
      public ActivityInstanceAliveCondition(final long piOid)
      {
         this.piOid = piOid;
         this.latch = new CountDownLatch(1);
      }
      
      public long piOid()
      {
         return piOid;
      }
      
      public CountDownLatch latch()
      {
         return latch;
      }
   }
   
   private static final class ActivityInstanceInfo
   {
      private final long piOid;
      private final ActivityInstanceState aiState;
      
      public ActivityInstanceInfo(final long piOid, final ActivityInstanceState aiState)
      {
         this.piOid = piOid;
         this.aiState = aiState;
      }
      
      public long piOid()
      {
         return piOid;
      }
      
      public ActivityInstanceState aiState()
      {
         return aiState;
      }
   }
}
