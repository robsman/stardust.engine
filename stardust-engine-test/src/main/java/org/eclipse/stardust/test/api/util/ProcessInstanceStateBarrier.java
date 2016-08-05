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
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <p>
 * Allows to wait for a process instance state change.
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
public class ProcessInstanceStateBarrier
{
   /**
    * the default timeout: 20 seconds
    */
   private static final WaitTimeout DEFAULT_TIMEOUT = new WaitTimeout(20, TimeUnit.SECONDS);

   private static WaitTimeout timeout = DEFAULT_TIMEOUT;

   private static ProcessInstanceStateBarrier instance;

   private final Map<Long, Set<ProcessInstanceState>> piStates;

   private volatile ProcessInstanceStateCondition condition;

   /**
    * <p>
    * Returns the one and only instance of this class.
    * </p>
    *
    * <p>
    * Needs to be synchronized since it modifies the field {@link #instance}
    * which is accessed concurrently.
    * </p>
    *
    * @return the one and only instance of this class
    */
   public synchronized static ProcessInstanceStateBarrier instance()
   {
      return (instance != null)
         ? instance
         : (instance = new ProcessInstanceStateBarrier());
   }

   /**
    * <p>
    * Allows for changing the timeout when waiting for a state change.
    * </p>
    *
    * <p>
    * Needs to be synchronized since it modifies the field {@link #timeout}
    * which is accessed concurrently.
    * </p>
    *
    * @param timeout the timeout to set, if <code>null</code> is passed, it's reset to {@link #DEFAULT_TIMEOUT}
    */
   public synchronized void setTimeout(final WaitTimeout timeout)
   {
      ProcessInstanceStateBarrier.timeout = (timeout != null) ? timeout : DEFAULT_TIMEOUT;
   }

   /**
    * <p>
    * Waits until the given process instance is in the given state or the timeout has exceeded. It's only
    * allowed to wait for one condition at a time.
    * </p>
    *
    * @throws IllegalStateException if one tries to wait for more than one condition at a time
    * @throws TimeoutException if the condition is still not met, but the timeout has exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public void await(final long piOid, final ProcessInstanceState piState) throws TimeoutException, InterruptedException
   {
      if (piState == null)
      {
         throw new NullPointerException("Process instance state must not be null.");
      }
      if (piState.equals(ProcessInstanceState.Created) || piState.equals(ProcessInstanceState.Aborting) || piState.equals(ProcessInstanceState.Halting))
      {
         throw new UnsupportedOperationException("Waiting for process instance state '" + piState + "' is not supported.");
      }


      try
      {
         if (isProcessInstanceStateConditionMet(piOid, piState))
         {
            return;
         }

         final boolean success = condition.latch().await(timeout.time(), timeout.unit());
         if ( !success)
         {
            throw new TimeoutException("Process instance " + piOid + " is still not in the state '" + piState + "'.");
         }
      }
      finally
      {
         condition = null;
      }
   }

   /**
    * <p>
    * Cleans up all the state gathered so far.
    * </p>
    *
    * <p>
    * Needs to be synchronized since it modifies the fields {@link #piStates}
    * and {@link #condition} which are accessed concurrently.
    * </p>
    */
   public synchronized void cleanUp()
   {
      piStates.clear();

      condition = null;
   }

   /**
    * Needs to be synchronized since it modifies the fields {@link #piStates}
    * and {@link #condition} which are accessed concurrently.
    */
   private synchronized boolean isProcessInstanceStateConditionMet(final long piOid, final ProcessInstanceState piState)
   {
      if (condition != null)
      {
         throw new IllegalStateException("It's not allowed to wait for more than one condition at a time.");
      }

      condition = new ProcessInstanceStateCondition(piOid, piState);

      Set<ProcessInstanceState> states = piStates.get(piOid);
      if (states != null)
      {
         for (final ProcessInstanceState pis : states)
         {
            if (condition.matches(piOid, pis))
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * needs to be synchronized since it modifies the fields {@link #piStates}
    * and {@link #condition} which are accessed concurrently
    */
   private synchronized void stateChanged(final long piOid, final ProcessInstanceState piState)
   {
      Set<ProcessInstanceState> pis = piStates.get(piOid);
      if (pis == null)
      {
         pis = newHashSet();
         piStates.put(piOid, pis);
      }
      pis.add(piState);

      if (condition != null)
      {
         if (condition.matches(piOid, piState))
         {
            condition.latch().countDown();
         }
      }
   }

   /**
    * private constructor for singleton implementation, does <b>not</b> need to be synchronized
    * since it's only called from a synchronized method
    */
   private ProcessInstanceStateBarrier()
   {
      piStates = newHashMap();
   }

   /**
    * <p>
    * Monitors the process instance state changes.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static class ProcessInstanceStateChangeMonitor implements IProcessExecutionMonitor
   {
      /*
       * (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor#processAborted(org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance)
       */
      @Override
      public void processStarted(final IProcessInstance pi)
      {
         registerTransactionAwareMonitor(pi.getOID(), ProcessInstanceState.Active);
      }

      /*
       * (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor#processInterrupted(org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance)
       */
      @Override
      public void processInterrupted(final IProcessInstance pi)
      {
         registerTransactionAwareMonitor(pi.getOID(), ProcessInstanceState.Interrupted);
      }

      /*
       * (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor#processAborted(org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance)
       */
      @Override
      public void processAborted(final IProcessInstance pi)
      {
         registerTransactionAwareMonitor(pi.getOID(), ProcessInstanceState.Aborted);
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor#processHalted(org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance)
       */
      @Override
      public void processHalted(final IProcessInstance pi)
      {
         registerTransactionAwareMonitor(pi.getOID(), ProcessInstanceState.Halted);
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor#processResumed(org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance)
       */
      @Override
      public void processResumed(final IProcessInstance pi)
      {
         registerTransactionAwareMonitor(pi.getOID(), ProcessInstanceState.Active);
      }

      /*
       * (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor#processAborted(org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance)
       */
      @Override
      public void processCompleted(final IProcessInstance pi)
      {
         registerTransactionAwareMonitor(pi.getOID(), ProcessInstanceState.Completed);
      }

      private void registerTransactionAwareMonitor(final long piOid, final ProcessInstanceState piState)
      {
         final TransactionAwareMonitor monitor = new TransactionAwareMonitor(piOid, piState);
         TransactionSynchronizationManager.registerSynchronization(monitor);
      }
   }

   private static final class TransactionAwareMonitor extends TransactionSynchronizationAdapter
   {
      private final long piOid;
      private final ProcessInstanceState piState;

      public TransactionAwareMonitor(final long piOid, final ProcessInstanceState piState)
      {
         this.piOid = piOid;
         this.piState = piState;
      }

      /* (non-Javadoc)
       * @see org.springframework.transaction.support.TransactionSynchronizationAdapter#afterCommit()
       */
      @Override
      public void afterCommit()
      {
         instance().stateChanged(piOid, piState);
      }
   }

   private static final class ProcessInstanceStateCondition
   {
      private final long piOid;
      private final ProcessInstanceState piState;
      private final CountDownLatch latch;

      public ProcessInstanceStateCondition(final long piOid, final ProcessInstanceState piState)
      {
         this.piOid = piOid;
         this.piState = piState;
         this.latch = new CountDownLatch(1);
      }

      public CountDownLatch latch()
      {
         return latch;
      }

      public boolean matches(final long piOid, final ProcessInstanceState piState)
      {
         if (this.piOid == piOid && this.piState == piState)
         {
            return true;
         }
         return false;
      }
   }
}
