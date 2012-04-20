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
package org.eclipse.stardust.test.impl.barrier;

/**
 * <p>
 * This class can be used to wait for a particular condition using the template pattern
 * so that the user of this class does not have to deal with the wait and retry mechanism
 * and can focus on the condition check.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public abstract class BarrierTemplate
{
   /**
    * <p>
    * the default retry count, i.e. the number of checks for the condition to meet
    * </p>
    */
   public static final int DEFAULT_RETRY_COUNT = 3;
   
   /**
    * <p>
    * the default wait period, i.e. the time between the checks for the condition to meet
    * </p>
    */
   public static final long DEFAULT_WAIT_PERIOD = 1000;
   
   private final int retryCount;
   private final long waitPeriod;
   
   /**
    * <p>
    * Initializes the object with default values for retry count as well as wait period 
    * (see {@link DEFAULT_RETRY_COUNT} and {@link DEFAULT_WAIT_PERIOD}).
    * </p>
    */
   public BarrierTemplate()
   {
      this(DEFAULT_RETRY_COUNT, DEFAULT_WAIT_PERIOD);
   }
   
   /**
    * <p>
    * Initializes the object with the given retry count and wait period.
    * </p>
    * 
    * @param retryCount the retry count to use, i.e. the number of checks for the condition to meet
    * @param waitPeriod the time between the checks for the condition to meet
    */
   public BarrierTemplate(final int retryCount, final long waitPeriod)
   {
      if (retryCount <= 0)
      {
         throw new IllegalArgumentException("Retry count must be greater than 0.");
      }
      if (waitPeriod <= 0)
      {
         throw new IllegalArgumentException("Wait period must be greater than 0.");
      }
      
      this.retryCount = retryCount;
      this.waitPeriod = waitPeriod;
   }
   
   /**
    * <p>
    * Waits for a condition until it's met or the retry count exceeded. The condition to wait for
    * can be defined in the method {@link #checkCondition()} by overriding it.
    * </p>
    * 
    * @throws IllegalStateException if the condition is still not met, but the retry count exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public final void await() throws IllegalStateException, InterruptedException
   {
      int count = retryCount;
      
      do
      {
         final ConditionStatus conditionStatus = checkCondition();
         if (conditionStatus == ConditionStatus.MET)
         {
            return;
         }
         
         Thread.sleep(waitPeriod);
      }
      while (--count > 0);
      
      throw new IllegalStateException("Condition is still not met: " + getConditionDescription());
   }
   
   /**
    * <p>
    * Checks the condition and returns whether it's met or not.
    * </p>
    * 
    * @return whether the condition is met or not
    */
   protected abstract ConditionStatus checkCondition();
   
   /**
    * @return a description of the condition an object of this class is waiting for
    */
   protected abstract String getConditionDescription();
   
   /**
    * <p>
    * Indicates whether the condition an object of <code>WaitForConditionTemplate</code>
    * is waiting for is met or not.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   protected static enum ConditionStatus { MET, NOT_MET }
}
