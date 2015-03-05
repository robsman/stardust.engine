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

/**
 * <p>
 * TimeMeasure class can be used to compute the duration. The time measure will start
 * as soon as the object is created or if you have explicitly called the 
 * {@link TimeMeasure.start()} method. To get the elapsed time you can simply invoke
 * {@link TimeMeasure.getDurationInMillis()}. This will automatically stop the timer if
 * it is not already done to ensure repeatable and remaining durations if you don't invoke
 * the {@link TimeMeasure.start()} method between the {@link TimeMeasure.getDurationInMillis()}
 * invocations.
 * </p>
 * <b>Example:</b>
 * <pre>
 * TimeMeasure overallTimer = new TimeMeasure();
 * TimeMeasure method2Timer = new TimeMeasure();
 * 
 * TimeMeasure method1Timer = new TimeMeasure();
 * method1();
 * long elapsedTimeOfMethod1 = method1Timer.getDurationInMillis(); // get elapsed time of method1()
 * 
 * method2Timer.start(); // starts method2Timer explicitly because we don't want to include the elapsed time of method1
 * method2();
 * method2Timer.stop();
 * 
 * method3();
 * 
 * long elapsedTimeOfMethod2 = method2Timer.getDurationInMillis(); // get elapsed time of method2()
 * 
 * overallTimer.stop(); // stop() method can be invoked explicitly without calling the start() method at first
 * long overallElapsedTime = overallTimer.getDurationInMillis(); // get elapsed time of this example code
 * 
 * </pre>
 * 
 * @author Sven.Rottstock
 */
public class TimeMeasure
{
   private long startTime;
   private long stopTime;
   
   public final static long NOT_INITIALIZED = -1L;
   
   public TimeMeasure()
   {
      init();
   }
   
   private void init()
   {
      startTime = System.currentTimeMillis();
      stopTime = NOT_INITIALIZED;
   }
   
   /**
    * Starts the timer explicitly. If this method is not invoked then the object creation
    * time is used for the computation of the duration. If the method is invoked for a 
    * second time then it acts like a reset method.
    * 
    * @return The TimeMeasure itself
    */
   public TimeMeasure start()
   {
      init();
      return this;
   }
   
   /**
    * Stops the timer.
    * 
    * @return The TimeMeasure itself
    */
   public TimeMeasure stop()
   {
      stopTime = System.currentTimeMillis();
      return this;
   }
   
   /**
    * Returns the start time in millis.
    * @return Either the time as the {@link TimeMeasure.start()} method was invoked or 
    *    if it was never called the object creation resp. reset time.
    */
   public long getStartTimeInMillis()
   {
      return startTime;
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
      return getStopTimeInMillis() - getStartTimeInMillis();
   }
}
