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
 * 10 seconds.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ActivityInstanceStateBarrier
{
   private static WaitTimeout timeout = new WaitTimeout(10, TimeUnit.SECONDS);
   
   private static ActivityInstanceStateBarrier instance;

   private final Map<Long, ActivityInstanceInfo> ais;
   
   private volatile ActivityInstanceStateCondition aiStateCondition;
   private volatile ActivityInstanceAliveCondition aiAliveCondition;
   private volatile ActivityInstanceForIdCondition aiForIdCondition;
   
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

      try
      {
         if (isActivityInstanceStateConditionAlreadyMet(aiOid, aiState))
         {
            return;
         }
         
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
    * Waits until there's an alive activity instance for the given process instance or the timeout has exceeded. It's only
    * allowed to wait for one condition at a time.
    * </p>
    * 
    * @param piOid the OID of the process instance enclosing the activity instance to wait for
    * 
    * @throws IllegalStateException if one tries to wait for more than one condition at a time
    * @throws TimeoutException if the condition is still not met, but the timeout has exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public void awaitAlive(final long piOid) throws IllegalStateException, TimeoutException, InterruptedException
   {      
      try
      {
         if (isActivityInstanceAliveConditionAlreadyMet(piOid))
         {
            return;
         }
         
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
    * <p>
    * Waits until there's an activity instance with the given ID for the given process instance or the timeout has exceeded.
    * It's only allowed to wait for one condition at a time.
    * </p>
    * 
    * @param piOid the OID of the process instance enclosing the activity instance to wait for
    * @param activityID the ID of the activity to wait for
    * 
    * @throws IllegalStateException if one tries to wait for more than one condition at a time
    * @throws TimeoutException if the condition is still not met, but the timeout has exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public void awaitForId(final long piOid, final String activityId) throws IllegalStateException, TimeoutException, InterruptedException
   {
      try
      {
         if (isActivityInstanceForIdConditionAlreadyMet(piOid, activityId))
         {
            return;
         }
         
         final boolean success = aiForIdCondition.latch().await(timeout.time(), timeout.unit());
         if ( !success)
         {
            throw new TimeoutException("Activity instance is still not alive.");
         }
      }
      finally
      {
         aiForIdCondition = null;
      }
   }

   /**
    * <p>
    * Cleans up all the state gathered so far.
    * </p>
    */
   public synchronized void cleanUp()
   {
      ais.clear();
      
      aiStateCondition = null;
      aiAliveCondition = null;
      aiForIdCondition = null;
   }
   
   /**
    * needs to be synchronized since it reads the field {@link ActivityInstanceStateBarrier#aiStates},
    * which is accessed concurrently
    */
   private synchronized boolean isActivityInstanceStateConditionAlreadyMet(final long aiOid, final ActivityInstanceState aiState)
   {
      if (aiStateCondition != null)
      {
         throw new IllegalStateException("It's not allowed to wait for more than one condition at a time.");
      }
      
      aiStateCondition = new ActivityInstanceStateCondition(aiOid, aiState);
      
      if (aiStateCondition.matches(aiOid, ais.get(aiOid).state()))
      {
         return true;
      }
      
      return false;
   }
   
   /**
    * needs to be synchronized since it reads the field {@link ActivityInstanceStateBarrier#aiStates},
    * which is accessed concurrently
    */
   private synchronized boolean isActivityInstanceAliveConditionAlreadyMet(final long piOid)
   {
      if (aiAliveCondition != null)
      {
         throw new IllegalStateException("It's not allowed to wait for more than one condition at a time.");
      }
      
      aiAliveCondition = new ActivityInstanceAliveCondition(piOid);
      
      for (final ActivityInstanceInfo ai : ais.values())
      {
         if (aiAliveCondition.matches(ai))
         {
            return true;
         }
      }
      
      return false;
   }
   
   /**
    * needs to be synchronized since it reads the field {@link ActivityInstanceStateBarrier#aiStates},
    * which is accessed concurrently
    */
   private synchronized boolean isActivityInstanceForIdConditionAlreadyMet(final long piOid, final String activityId)
   {
      if (aiForIdCondition != null)
      {
         throw new IllegalStateException("It's not allowed to wait for more than one condition at a time.");
      }
      
      aiForIdCondition = new ActivityInstanceForIdCondition(piOid, activityId);
      
      for (final ActivityInstanceInfo ai : ais.values())
      {
         if (aiForIdCondition.matches(ai.piOid(), ai.activityId()))
         {
            return true;
         }
      }
      
      return false;
   }
   
   /**
    * needs to be synchronized since it modifies the field {@link ActivityInstanceStateBarrier#aiStates},
    * which is accessed concurrently
    */
   private synchronized void stateChanged(final ActivityInstanceInfo ai)
   {
      ais.put(ai.oid(), ai);
      
      if (aiStateCondition != null)
      {
         if (aiStateCondition.matches(ai.oid(), ai.state()))
         {
            aiStateCondition.latch().countDown();
         }
      }
      if (aiAliveCondition != null)
      {
         if (aiAliveCondition.matches(ai))
         {
            aiAliveCondition.latch().countDown();
         }
      }
      if (aiForIdCondition != null)
      {
         if (aiForIdCondition.matches(ai.piOid(), ai.activityId()))
         {
            aiForIdCondition.latch().countDown();
         }
      }
   }
   
   /**
    * private constructor for singleton implementation
    */
   private ActivityInstanceStateBarrier()
   {
      ais = newHashMap();
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
         final ActivityInstanceState state = ActivityInstanceState.getState(aiState);
         final ActivityInstanceInfo aiInfo = new ActivityInstanceInfo(ai.getOID(), ai.getActivity().getId(), ai.getProcessInstanceOID(), state);
         final TransactionAwareMonitor monitor = new TransactionAwareMonitor(aiInfo);
         TransactionSynchronizationManager.registerSynchronization(monitor);
      }
   }
   
   private static final class TransactionAwareMonitor extends TransactionSynchronizationAdapter
   {
      private final ActivityInstanceInfo ai;
      
      public TransactionAwareMonitor(final ActivityInstanceInfo ai)
      {
         this.ai = ai;
      }
      
      /* (non-Javadoc)
       * @see org.springframework.transaction.support.TransactionSynchronizationAdapter#afterCommit()
       */
      @Override
      public void afterCommit()
      {
         instance().stateChanged(ai);
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
      
      public CountDownLatch latch()
      {
         return latch;
      }
      
      public boolean matches(final long aiOid, final ActivityInstanceState aiState)
      {
         if (this.aiOid == aiOid && this.aiState == aiState)
         {
            return true;
         }
         return false;
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
      
      public CountDownLatch latch()
      {
         return latch;
      }
      
      public boolean matches(final ActivityInstanceInfo ai)
      {
         if (this.piOid == ai.piOid() && !ai.isTerminated())
         {
            return true;
         }
         return false;
      }
   }
   
   private static final class ActivityInstanceForIdCondition
   {
      private final long piOid;
      private final String activityId;
      private final CountDownLatch latch;
      
      public ActivityInstanceForIdCondition(final long piOid, final String activityId)
      {
         this.piOid = piOid;
         this.activityId = activityId;
         this.latch = new CountDownLatch(1);
      }
      
      public CountDownLatch latch()
      {
         return latch;
      }
      
      public boolean matches(final long piOid, final String activityId)
      {
         if (this.piOid == piOid && this.activityId.equals(activityId))
         {
            return true;
         }
         return false;
      }
   }
   
   private static final class ActivityInstanceInfo
   {
      private final long oid;
      private final String activityId;
      private final long piOid;
      private ActivityInstanceState state;
      
      public ActivityInstanceInfo(final long oid, final String activityId, final long piOid, final ActivityInstanceState state)
      {
         this.oid = oid;
         this.activityId = activityId;
         this.piOid = piOid;
         this.state = state;
      }
      
      public long oid()
      {
         return oid;
      }
      
      public String activityId()
      {
         return activityId;
      }
      
      public long piOid()
      {
         return piOid;
      }
      
      public ActivityInstanceState state()
      {
         return state;
      }
      
      public boolean isTerminated()
      {
         return state == ActivityInstanceState.Completed || state == ActivityInstanceState.Aborted;
      }
   }
}
