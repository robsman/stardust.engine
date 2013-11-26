/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common.log;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RootLogger;

public class Log4j12Logger implements Logger
{
   public static final String LOGGER_CONFIG = "carnot.log.config";

   private org.apache.log4j.Logger l4jLogger;

   Log4j12Logger(String name)
   {
      URL log4jProperties = getResource("log4j.properties");
      if (!ClientLogManager.isBootstrapped()
            && System.getProperty("log4j.configuration")== null
            && log4jProperties == null)
      {
         String configFile = System.getProperty(LOGGER_CONFIG);
         if (configFile == null)
         {
//            org.apache.log4j.BasicConfigurator.configure();
         }
         else
         {
            ClientLogManager.bootstrap(new File(configFile));
         }
      }
      l4jLogger = LogManager.getLogger(name);
      // Workaround: JBoss 7.1.1 logging framework ignores deployment's log4j.properties.
      if (l4jLogger != null
            && "org.jboss.logmanager.log4j.BridgeLogger".equals(l4jLogger
                  .getClass().getName()) && log4jProperties != null)
      {
         // Initialize log4j Logger with found log4j.properties.
         Hierarchy h = new Hierarchy(new RootLogger((Level) Level.DEBUG));
         DefaultRepositorySelector defaultRepositorySelector = new DefaultRepositorySelector(
               h);
         LoggerRepository loggerRepository = defaultRepositorySelector.getLoggerRepository();

         OptionConverter.selectAndConfigure(log4jProperties, null,
               loggerRepository);

         l4jLogger = loggerRepository.getLogger(name);
      }
   }

   public void debug(Object o)
   {
      l4jLogger.debug(o);
   }

   public void debug(Object o, Throwable throwable)
   {
      l4jLogger.debug(o, throwable);
   }

   public void error(Object o)
   {
      l4jLogger.error(o);
   }

   public void error(Object o, Throwable throwable)
   {
      l4jLogger.error(o, throwable);
   }

   public void fatal(Object o)
   {
      l4jLogger.fatal(o);
   }

   public void fatal(Object o, Throwable throwable)
   {
      l4jLogger.fatal(o, throwable);
   }

   public void info(Object o)
   {
      l4jLogger.info(o);
   }

   public void info(Object o, Throwable throwable)
   {
      l4jLogger.info(o, throwable);
   }

   public void warn(Object o)
   {
      l4jLogger.warn(o);
   }

   public void warn(Object o, Throwable throwable)
   {
      l4jLogger.warn(o, throwable);
   }

   public boolean isInfoEnabled()
   {
      return l4jLogger.isInfoEnabled();
   }

   public boolean isDebugEnabled()
   {
      return l4jLogger.isDebugEnabled();
   }

   private URL getResource(String resource)
   {
      URL url = null;

      try
      {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null)
            {
               url = classLoader.getResource(resource);
               if (url != null)
               {
                  return url;
               }
            }

         classLoader = Loader.class.getClassLoader();
         if (classLoader != null)
         {

            url = classLoader.getResource(resource);
            if (url != null)
            {
               return url;
            }
         }
      }
      catch (Throwable t)
      {
      }
      return ClassLoader.getSystemResource(resource);
   }

}
