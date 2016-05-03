/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.utils;

import java.lang.reflect.Constructor;

import org.junit.rules.ExternalResource;
import org.mockito.Mockito;

import org.eclipse.stardust.common.log.DefaultLogger;
import org.eclipse.stardust.common.log.Log4j12Logger;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.log.NoopLogger;

public class SpyLogger extends ExternalResource
{
   private String orgCarnotLogType;
   private static Logger delegate;
   
   @Override
   protected void before() throws Throwable
   {
      super.before();
      orgCarnotLogType = System.setProperty("carnot.log.type", "custom,"+LogDelegator.class.getName());
   }

   @Override
   protected void after()
   {
      if(orgCarnotLogType == null)
      {
         System.getProperties().remove("carnot.log.type");
      }
      else
      {
         System.setProperty("carnot.log.type", orgCarnotLogType);
      }
      delegate = null;
      super.after();
   }
   
   public Logger getSpy()
   {
      return delegate;
   }

   private static final String loggerToUse;
   
   static
   {
      String underlyingLogger;
      try
      {
         Class.forName("org.apache.log4j.Category");
         underlyingLogger = Log4j12Logger.class.getName();
      }
      catch (ClassNotFoundException e)
      {
         underlyingLogger = DefaultLogger.class.getName();
      }
      loggerToUse = underlyingLogger;
   }

   public static class LogDelegator implements Logger
   {
      public LogDelegator(String category)
      {
         Logger loggerCandidate = null;
         try
         {
            Class< ? > clazz = Class.forName(loggerToUse);
            Constructor< ? > ctor = clazz.getConstructor(new Class[] {String.class});
            loggerCandidate = (Logger) ctor.newInstance(new Object[]{category});
         }
         catch(Exception e)
         {
            loggerCandidate = new NoopLogger();
         }
         delegate = Mockito.spy(loggerCandidate);
      }
      
      public void debug(Object o)
      {
         delegate.debug(o);
      }

      public void debug(Object o, Throwable throwable)
      {
         delegate.debug(o, throwable);
      }

      public void error(Object o)
      {
         delegate.error(o);
      }

      public void error(Object o, Throwable throwable)
      {
         delegate.error(o, throwable);
      }

      public void fatal(Object o)
      {
         delegate.fatal(o);
      }

      public void fatal(Object o, Throwable throwable)
      {
         delegate.fatal(o, throwable);
      }

      public void info(Object o)
      {
         delegate.info(o);
      }

      public void info(Object o, Throwable throwable)
      {
         delegate.info(o, throwable);
      }

      public void warn(Object o)
      {
         delegate.warn(o);
      }

      public void warn(Object o, Throwable throwable)
      {
         delegate.warn(o, throwable);
      }

      public boolean isInfoEnabled()
      {
         return delegate.isInfoEnabled();
      }

      public boolean isDebugEnabled()
      {
         return delegate.isDebugEnabled();
      }

   }
}
