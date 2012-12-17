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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.stardust.engine.api.runtime.TimeoutException;

/**
 * <p>
 * Allows for waiting for a particular log message logged via the <i>log4j</i> system.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class Log4jLogMessageBarrier extends WriterAppender
{
   private final List<String> logMessages = new CopyOnWriteArrayList<String>();
   
   private volatile boolean registered = false;
   
   private volatile boolean active = false;
   private volatile Pattern regex;
   private volatile CountDownLatch latch;
   
   /**
    * <p>
    * Initializes the barrier such that it only considers log messages
    * greater or equal to the given log level. 
    * </p>
    * 
    * @param level the log level to be considered
    */
   public Log4jLogMessageBarrier(final Level level)
   {
      if (level == null)
      {
         throw new NullPointerException("Level must not be null.");
      }

      setThreshold(level);
   }
   
   /* (non-Javadoc)
    * @see org.apache.log4j.WriterAppender#append(org.apache.log4j.spi.LoggingEvent)
    */
   @Override
   public void append(final LoggingEvent event)
   {
      final Object msg = event.getMessage();
      if ( !(msg instanceof String))
      {
         return;
      }
      
      final String stringMsg = (String) msg;
      logMessages.add(stringMsg);
      
      if (active)
      {
         final Matcher matcher = regex.matcher(stringMsg);
         if (matcher.matches())
         {
            latch.countDown();
         }
      }
   }
   
   /**
    * <p>
    * Registers this object with the <i>log4j</i> system, i.e. it starts receiving log messages.
    * </p>
    */
   public synchronized void registerWithLog4j()
   {
      if (registered)
      {
         throw new IllegalStateException("Already registered with the log4j system.");
      }
      
      Logger.getRootLogger().addAppender(this);
      registered = true;
   }
   
   /**
    * <p>
    * Unregisters this object from the <i>log4j</i> system, i.e. it stops receiving log messages.
    * </p>
    */
   public synchronized void unregisterFromLog4j()
   {
      if ( !registered)
      {
         throw new IllegalStateException("Not registered with the log4j system.");
      }
      
      Logger.getRootLogger().removeAppender(this);
      registered = false;
   }
   
   /**
    * <p>
    * Wait for a log message satisfying the given regular expression.
    * </p>
    * 
    * @param regex the regular expression pattern of the log message we're looking for
    * @param timeout the time to wait for the log message to be logged before aborting
    */
   public void waitForLogMessage(final String regex, final WaitTimeout timeout) throws InterruptedException
   {
      if ( !registered)
      {
         throw new IllegalStateException("Not yet registered with the log4j system.");
      }
          
      init(regex);
      
      /* has the pattern we're looking for alreaday been logged ... */
      for (final String s : logMessages)
      {
         final Matcher matcher = this.regex.matcher(s);
         if (matcher.matches())
         {
            deactivate();
            return;
         }
      }
      
      /* ... or do we have to wait for it? */
      boolean found = this.latch.await(timeout.time(), timeout.unit());
      deactivate();
      if ( !found)
      {
         throw new TimeoutException("The requested log message could not be found.");
      }
   }
   
   /**
    * @return whether this object is registered with the <i>log4j</i> system
    */
   public boolean isRegisteredWithLog4j()
   {
      return registered;
   }
   
   private synchronized void init(final String regex)
   {
      if (active)
      {
         throw new IllegalStateException("It's not allowed to wait for more than one log message at a time.");
      }
      
      this.active = true;
      this.regex = Pattern.compile(regex);
      this.latch = new CountDownLatch(1);
   }
   
   private synchronized void deactivate()
   {
      this.active = false;
   }
}
