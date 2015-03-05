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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.Mockito;

public class TimeMeasureTest
{
   private final int MAX_WAIT_TIME = 10 /*ms */;
   
   @Test
   public void testDurationWithPredefinedTimes()
   {
      TimeMeasure timer = Mockito.mock(TimeMeasure.class, Mockito.CALLS_REAL_METHODS);
      when(timer.getStartTimeInMillis()).thenReturn(0L);
      when(timer.getStopTimeInMillis()).thenReturn(120L);
      assertTrue(timer.getDurationInMillis() == 120L);
   }
   
   @Test
   public void testNonInitializedStopTime() throws InterruptedException
   {
      TimeMeasure spyTimer = spy(new TimeMeasure());
      Thread.sleep(MAX_WAIT_TIME);
      // TimeMeasure.stopTime is not initialized at this point
      long duration = spyTimer.getDurationInMillis();
      verify(spyTimer, times(1)).stop();
      assertTrue("duration must be at least " + MAX_WAIT_TIME + "ms",
            duration >= MAX_WAIT_TIME);
   }
   
   @Test
   public void testInitializedStopTime() throws InterruptedException
   {
      TimeMeasure timer = new TimeMeasure();
      Thread.sleep(MAX_WAIT_TIME);
      timer.stop();
      TimeMeasure spyTimer = spy(timer);
      long duration = spyTimer.getDurationInMillis();
      verify(spyTimer, never()).stop();
      assertTrue("duration must be at least " + MAX_WAIT_TIME + "ms", 
            duration >= MAX_WAIT_TIME);
   }
   
   @Test
   public void testStartTime() throws InterruptedException
   {
      TimeMeasure timer = new TimeMeasure();
      long objectCreationTime = timer.getStartTimeInMillis();
      Thread.sleep(MAX_WAIT_TIME);
      assertTrue("object creation time must be equal", 
            objectCreationTime == timer.getStartTimeInMillis());
      timer.start();
      assertTrue("start time must be later", 
            objectCreationTime < timer.getStartTimeInMillis());
   }
   
   @Test
   public void testStopTime() throws InterruptedException
   {
      TimeMeasure timer = new TimeMeasure();
      long objectCreationTime = timer.getStartTimeInMillis(); 
      assertTrue("stop time must not be initialized", 
            timer.getStopTimeInMillis() == TimeMeasure.NOT_INITIALIZED);
      Thread.sleep(MAX_WAIT_TIME);
      timer.stop();
      assertTrue("stop time must be initialized", 
            timer.getStopTimeInMillis() != TimeMeasure.NOT_INITIALIZED);
      assertTrue("stop time must be greater as the object creation time", 
            objectCreationTime < timer.getStopTimeInMillis());
      
      // start() method should reset the stop time
      timer.start();
      assertTrue("stop time must not be initialized", 
            timer.getStopTimeInMillis() == TimeMeasure.NOT_INITIALIZED);
   }
   
   @Test
   public void testResetTimer() throws InterruptedException
   {
      TimeMeasure timer = new TimeMeasure();
      long objectCreationTime = timer.getStartTimeInMillis();
      assertTrue(timer.getStopTimeInMillis() == TimeMeasure.NOT_INITIALIZED);
      Thread.sleep(MAX_WAIT_TIME);
      // make sure that all time fields are initialized
      timer.start().stop();
      long lastStartTime = timer.getStartTimeInMillis();
      assertTrue(lastStartTime > objectCreationTime);
      assertTrue(timer.getStopTimeInMillis() != TimeMeasure.NOT_INITIALIZED);
      
      // reset the timer by invoking the start method again
      Thread.sleep(MAX_WAIT_TIME);
      timer.start();
      long resetTime = timer.getStartTimeInMillis();
      assertTrue(resetTime > lastStartTime);
      assertTrue((timer.getStopTimeInMillis() == TimeMeasure.NOT_INITIALIZED));
   }

}
