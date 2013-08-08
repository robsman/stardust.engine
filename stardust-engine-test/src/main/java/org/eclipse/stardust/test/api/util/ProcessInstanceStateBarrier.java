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

import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor;
import org.springframework.transaction.support.TransactionSynchronization;
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
   private static WaitTimeout timeout = new WaitTimeout(10, TimeUnit.SECONDS);
   
   private static ProcessInstanceStateBarrier instance;
   
   private final Map<Long, ProcessInstanceState> piStates;
   
   private volatile ProcessInstanceStateCondition condition;
   
   /**
    * <p>
    * Returns the one and only instance of this class.
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
    * @param timeout the timeout to set
    */
   public synchronized static void setTimeout(final WaitTimeout timeout)
   {
      if (timeout == null)
      {
         throw new NullPointerException("Timeout must not be null.");
      }
      
      ProcessInstanceStateBarrier.timeout = timeout;
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
      if (piState.equals(ProcessInstanceState.Created) || piState.equals(ProcessInstanceState.Aborting))
      {
         throw new UnsupportedOperationException("Waiting for process instance state '" + piState + "' is not supported.");
      }
      
      if (isProcessInstanceStateConditionMet(piOid, piState))
      {
         return;
      }
      
      initCondition(piOid, piState);
      
      try
      {
         final boolean success = condition.latch().await(timeout.time(), timeout.unit());
         if ( !success)
         {
            throw new TimeoutException("Process instance is still not in the state '" + piState + "'.");
         }
      }
      finally
      {
         condition = null;
      }
   }

   /**
    * needs to be synchronized since it reads the field {@link ProcessInstanceStateBarrier#piStates},
    * which is accessed concurrently
    */
   private synchronized boolean isProcessInstanceStateConditionMet(final long piOid, final ProcessInstanceState piState)
   {
      if (piState.equals(piStates.get(piOid)))
      {
         return true;
      }
      
      return false;
   }
   
   private synchronized void initCondition(final long piOid, final ProcessInstanceState piState)
   {
      if (condition != null)
      {
         throw new IllegalStateException("It's not allowed to wait for more than one condition at a time.");
      }
      
      condition = new ProcessInstanceStateCondition(piOid, piState);
   }
   
   /**
    * needs to be synchronized since it modifies the field {@link ProcessInstanceStateBarrier#piStates},
    * which is accessed concurrently
    */
   private synchronized void stateChanged(final long piOid, final ProcessInstanceState piState)
   {
      piStates.put(piOid, piState);
      
      if (condition != null)
      {
         if (condition.piOid() == piOid && condition.piState().equals(piState))
         {
            condition.latch().countDown();
         }
      }      
   }
   
   /**
    * private constructor for singleton implementation
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
      
      /*
       * (non-Javadoc)
       * @see org.springframework.transaction.support.TransactionSynchronization#afterCompletion(int)
       */
      @Override
      public void afterCompletion(final int status)
      {
         if (status == TransactionSynchronization.STATUS_COMMITTED)
         {
            instance().stateChanged(piOid, piState);
         }
         else if (status == TransactionSynchronization.STATUS_ROLLED_BACK)
         {
            /* just ignore state change */
         }
         else if (status == TransactionSynchronization.STATUS_UNKNOWN)
         {
            throw new UnsupportedOperationException("Unknown tx status.");
         }
         else
         {
            throw new UnsupportedOperationException("Unsupported tx status '" + status + "'.");
         }
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
      
      public long piOid()
      {
         return piOid;
      }
      
      public ProcessInstanceState piState()
      {
         return piState;
      }
      
      public CountDownLatch latch()
      {
         return latch;
      }
   }
}
