/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common;

public class TimeMeasure
{
   private long startTime;
   private long objectCreatedTime;
   private long stopTime;
   
   public final static long NOT_INITIALIZED = -1L;
   
   public TimeMeasure()
   {
      init();
   }
   
   private void init()
   {
      objectCreatedTime = System.currentTimeMillis();
      startTime = stopTime = NOT_INITIALIZED;
   }
   
   /**
    * Starts the timer explicitly. If this method is not invoked then the object creation
    * time is used or the time as the {@link TimeMeasure.reset()} method was called.
    */
   public TimeMeasure start()
   {
      startTime = System.currentTimeMillis();
      stopTime = NOT_INITIALIZED;
      return this;
   }
   
   /**
    * Stops the timer.
    */
   public TimeMeasure stop()
   {
      stopTime = System.currentTimeMillis();
      return this;
   }
   
   /**
    * Resets the timer to the same state as the constructor would be invoked.
    */
   public TimeMeasure reset()
   {
      init();
      return this;
   }
   
   /**
    * Returns the start time in millis.
    * @return Either the time as the {@link TimeMeasure.start()} method was invoked or 
    *    {@link TimeMeasure.NOT_INITIALIZED} if it was never called.
    */
   public long getStartTimeInMillis()
   {
      return startTime;
   }
   
   /**
    * Returns the start time in millis.
    * @return Either the time as the {@link TimeMeasure.start()} method was invoked or 
    *    if it was never called the object creation time.
    */
   public long getInitializedStartTimeInMillis()
   {
      return startTime == NOT_INITIALIZED ? objectCreatedTime : startTime;
   }
   
   /**
   * Returns the stop time in millis which will be used to calculate the duration from a 
   * given start time.
   * @return The time as the {@link TimeMeasure.stop()} method was invoked or if it was never called
   *    {@link TimeMeasure.NOT_INITIALIZED}.
   */
   public long getStopTimeInMillis()
   {
      return stopTime;
   }
   
   /**
    * Returns the duration of the time as the timer was created or in case that you have 
    * invoked the {@link TimeMeasure.start()} method the time of the 
    * {@link TimeMeasure.start()} method invocation. If the {@link TimeMeasure.stop()}
    * method was not invoked then this will be done in this method to ensure repeatable
    * invocations with the same result.
    * @return duration in millis
    */
   public long getDurationInMillis()
   {
      if(stopTime == NOT_INITIALIZED)
      {
         this.stop();
      }
      return getStopTimeInMillis() - getInitializedStartTimeInMillis();
   }
}
