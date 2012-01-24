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

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.CollectionUtils;


/**
 * Shared log management functional for client and server usage of CARNOT logging.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LogManager
{
   private static final String LOGGER_TYPE = "carnot.log.type";
   private static final String DEBUG_FILTER = "carnot.log.debug.filter";

   private static enum LogType
   {
      NOOP, STDOUT, LOG4J, CUSTOM
   }

   private static LogType logType = null;
   private static String custom = null;

   private static boolean bootstrapped = false;
   private static boolean debug;
   private static List filters;
   
   private LogManager()
   {
   }

   /**
    * Retrieves an instance of the <code>Category</code> class with the specified
    * class name as channel name.
    */
   public static Logger getLogger(Class clazz)
   {
      return getLogger(clazz.getName());
   }

   /**
    * Retrieves an instance of the <code>Category</code> class with the specified
    * String as channel name.
    */
   public static Logger getLogger(String name)
   {
      bootstrap();
      
      switch (logType)
      {
      case NOOP: return new NoopLogger();
      case LOG4J: return getLog4JCategory(name);
      case STDOUT: return new DefaultLogger(name);
      }
      return getCustomCategory(name, custom);
   }

   private static synchronized void bootstrap()
   {
      if (!bootstrapped)
      {
         try
         {
            custom = System.getProperty(LOGGER_TYPE);
            logType = LogType.valueOf(custom);
         }
         catch (Exception e)
         {
         }
   
         if (logType == null)
         {
            boolean besMatch = false;
            try
            {
               besMatch = "com.inprise.vbroker.rmi.CORBA.StubImpl".equals(
                     System.getProperty("javax.rmi.CORBA.StubClass"));
            }
            catch (Exception e)
            {
            }
            logType = besMatch ? LogType.STDOUT : LogType.LOG4J;
         }
         
         if (logType == LogType.LOG4J)
         {
            try
            {
               Class.forName("org.apache.log4j.Category");
            }
            catch (ClassNotFoundException e)
            {
               logType = LogType.STDOUT;
            }
         }
   
         bootstrapped = true;
      }
   }

   private static Logger getCustomCategory(String name, String className)
   {
      try
      {
         Class clazz = Class.forName(className);
         Constructor ctor = clazz.getConstructor(new Class[]{String.class});
         return (Logger) ctor.newInstance(new Object[]{name});
      }
      catch (Exception e)
      {
         return new DefaultLogger(name);
      }
   }

   private static Logger getLog4JCategory(String name)
   {
      return new Log4j12Logger(name);
   }

   public static boolean isDebugEnabled(String category)
   {
      if (filters == null)
      {
         initDebugFilters();
      }
      if (debug)
      {
         for (int i = 0; i < filters.size(); i++)
         {
            String prefix = (String)filters.get(i);
            if (category.startsWith(prefix))
            {
               return true;
            }
         }
      }
      return false;
   }

   private static void initDebugFilters()
   {
      filters = CollectionUtils.newList();
      String filterList = null;
      try
      {
         filterList = System.getProperty(DEBUG_FILTER);
      }
      catch (Exception e)
      {
      }
      if (filterList != null)
      {
         StringTokenizer st = new StringTokenizer(filterList, ",");
         while (st.hasMoreTokens())
         {
            filters.add(st.nextToken());
         }
      }
      debug = filters.size() > 0;
   }
}
