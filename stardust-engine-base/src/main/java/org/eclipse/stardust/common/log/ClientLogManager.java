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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.utils.xml.BaseXmlUtils;
import org.eclipse.stardust.common.utils.xml.SecureEntityResolver;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The ClientLogManager is a helper to explicitely bootstrap log4j in a safe manner.
 * The class is implemented as a singleton.
 *
 * @author Sebastian Woelk
 * @version $Revision$
 */
public class ClientLogManager
{
   /**
    * A static reference to the one and only logger instance.
    *
    * @see #instance()
    */
   private static ClientLogManager instance;

   /**
    * Default value for the log file format for the standard configuration
    */
   private final String DEFAULT_LOG_FORMAT =
         "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%t] (%x) %c{2} - %m%n";

   /**
    * Default priority for the root category
    */
   private final Level DEFAULT_LEVEL = Level.INFO;

   /**
    * Default file size for a log size before it is rolled over
    * @see org.eclipse.stardust.common.log.ArchivingFileAppender
    */
   private final String DEFAULT_MAX_FILE_SIZE = "4MB";

   /**
    * The name of the application
    */
   private String applicationName = null;

   private static boolean isCanonicallyBootstrapped = false;

   /**
    * Bootstraps the logger with the name of the application, that means if
    * there isn't already a logger instance a new instance is created and the
    * name of the application is passed. If there is already an instance of the
    * logger the method just returns.
    *
    * @param applicationName  the name of the application that bootstraps the
    * logger
    */
   public synchronized static void bootstrap(String applicationName)
   {
      if (!isCanonicallyBootstrapped)
      {
         instance = new ClientLogManager(applicationName);
         isCanonicallyBootstrapped = true;
      }
   }

   /**
    * Bootstraps the logger with a configuration file.
    * If there isn't already a logger instance a new instance is created.
    * If there is already an instance of the
    * logger the method just returns.
    */
   public synchronized static void bootstrap(File configFile)
   {
      if (!isCanonicallyBootstrapped)
      {
         instance = new ClientLogManager(configFile);
         isCanonicallyBootstrapped = true;
      }
   }

   /**
    * Return the one and only instance of the logger object. If no one exists
    * the boostrap method is called with a default application name, and then
    * the new created instance is returned.
    *
    * @return        an instance of the logger object
    * @see #bootstrap
    * @deprecated
    */
   public synchronized static ClientLogManager instance()
   {
      if (instance == null)
      {
         throw new InternalException("ClientLogManager not bootstrapped");
      }
      return instance;
   }

   public synchronized static boolean isBootstrapped()
   {
      return instance != null;
   }

   /**
    * <p>This Constructor initializes the logger after bootstrap in a real client
    * context. First it tries to load the value of a property with name
    * "Logging.Configuration.APPLICATIONNAME". If such a property is found the
    * value is treated as the name of a XML configuration file and this file is
    * used to configure the log4j package.
    *
    * <p>If the property isn't found then the default configuration is applied.
    */
   private ClientLogManager(String applicationName)
   {
      this.applicationName = applicationName;
      String configFile = Parameters.instance().getString(
            "Logging.Configuration." + applicationName);

      if (configFile == null)
      {
         try
         {
            configureWithDefaults();
         }
         catch (Exception e)
         {
            addEmergencyAppender(org.apache.log4j.Logger.getRootLogger());
         }
      }
      else
      {
         try
         {
            configureFromFile(new File(configFile));
         }
         catch (Exception e)
         {
            try
            {
               configureWithDefaults();
            }
            catch (Exception e1)
            {
               addEmergencyAppender(org.apache.log4j.Logger.getRootLogger());
            }
            org.apache.log4j.Logger.getRootLogger().error(
                  "Exception while configuring log4j from configuratio file", e);
         }
      }
   }

   /**
    * This constructor is for explicitely configuring the logger from a configuration
    * file.
    */
   private ClientLogManager(File configFile)
   {
      try
      {
         configureFromFile(configFile);
      }
      catch (Exception e)
      {
         addEmergencyAppender(org.apache.log4j.Logger.getRootLogger());
      }
   }

   private static void configureFromFile(File configFile) throws IOException, SAXException
   {
      if (configFile.getName().endsWith(".xml"))
      {
         // log4j.dtd is placed in org.log4j/xml/log4j.dtd. The
         // DOMConfigurator class is placed in the same directory and can
         // find it.
         URL dtdURL = DOMConfigurator.class.getResource("log4j.dtd");
         if (dtdURL == null)
         {
            throw new InternalException("Unable to find log4j.dtd");
         }

         InputSource inputSource = new InputSource(new FileInputStream(configFile));
         inputSource.setSystemId(dtdURL.toString());

         DocumentBuilder domBuilder = BaseXmlUtils.newDomBuilder(true);
         domBuilder.setErrorHandler(new LoggerXMLErrorHandler());
         domBuilder.setEntityResolver(SecureEntityResolver.INSTANCE);

         Document doc = domBuilder.parse(inputSource);
         DOMConfigurator.configure(doc.getDocumentElement());
      }
      else
      {
         PropertyConfigurator.configure(configFile.getPath());
      }
   }

   /**
    * If xml configuration of log4j failed, this method is called, which
    * initializes a log4j emergency configuration.
    */
   private void configureWithDefaults()
   {
      String logDir = Parameters.instance().getString(LogProperties.DIRECTORY_PROPERTY, ".");
      String logFile = logDir + File.separator + applicationName + ".log";

      Collection<String> filters = Parameters.instance().getStrings(LogProperties.FILTERS_PROPERTY);

      if (filters.size() == 0)
      {
         createRootCategory(logFile);
      }
      else
      {
         Iterator<String> itr = filters.iterator();
         while (itr.hasNext())
         {
            createFilterCategory(logFile, itr.next());
         }
      }
   }

   /**
    * Configures the root category with a ArchivingFileAppender whose
    * configuration parameters are read from special carnot properties.
    *
    * @param logFile     the path of the directory where the log files
    * will be stored
    * @see org.apache.log4j.Category
    * @see ArchivingFileAppender
    */
   private void createRootCategory(String logFile)
   {

      ArchivingFileAppender fileAppender = null;

      org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();

      try
      {
         fileAppender = createArchivingFileAppender(logFile);
      }
      catch (IOException e)
      {
         addEmergencyAppender(rootLogger);
         return;
      }
      rootLogger.addAppender(fileAppender);

      String loglevel = Parameters.instance().getString(LogProperties.LEVEL_PROPERTY);
      rootLogger.setLevel(Level.toLevel(loglevel, DEFAULT_LEVEL));
   }

   /**
    * Configures a filter category with an ArchivingFileAppender whose
    * configuration parameters are read from special carnot properties.
    *
    * @see org.apache.log4j.Category
    * @see ArchivingFileAppender
    */
   private void createFilterCategory(String logFile, String name)
   {
      ArchivingFileAppender appender = null;
      org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
      try
      {
         appender = createArchivingFileAppender(logFile);
      }
      catch (IOException e)
      {
         addEmergencyAppender(logger);
         return;
      }
      logger.addAppender(appender);
      logger.setAdditivity(false);
      String loglevel = Parameters.instance().getString(LogProperties.LEVEL_PROPERTY);
      logger.setLevel(Level.toLevel(loglevel, DEFAULT_LEVEL));
   }

   /**
    * Creates an ArchivingFileAppender and returns the instance
    *
    * @param fileName     the name of the log file
    * @return             the instantiated file appender
    * @see  ArchivingFileAppender
    */
   private ArchivingFileAppender createArchivingFileAppender(String fileName)
         throws IOException
   {
      String logFormat = Parameters.instance().getString(LogProperties.FORMAT_PROPERTY,
            DEFAULT_LOG_FORMAT);

      ArchivingFileAppender fileAppender = new ArchivingFileAppender(
            new PatternLayout(logFormat), fileName, true);

      fileAppender.setMaxFileSize(Parameters.instance().getString(
            LogProperties.MAXFILESIZE_PROPERTY, DEFAULT_MAX_FILE_SIZE));

      fileAppender.activateOptions();

      return fileAppender;
   }

   private void addEmergencyAppender(org.apache.log4j.Logger logger)
   {
      logger.addAppender(new ConsoleAppender(new PatternLayout(DEFAULT_LOG_FORMAT)));
      logger.warn("Could not bootstrap category '" + logger.getName() +
            "' correctly, using console appender");
   }

   private static class Parameters
   {
      private static Parameters singleton;

      private Properties systemProperties;
      private Map<String, Object> properties = new HashMap<String, Object>();

      public static synchronized Parameters instance()
      {
         if (singleton == null)
         {
            singleton = new Parameters();
         }

         return singleton;
      }

      private Parameters()
      {
         try
         {
            systemProperties = System.getProperties();
         }
         catch (Exception e)
         {
            systemProperties = new Properties();
         }
         try
         {
            ResourceBundle defaultBundle = ResourceBundle.getBundle("carnot", Locale.getDefault());

            Enumeration<String> keys = defaultBundle.getKeys();
            while (keys.hasMoreElements())
            {
               String key = keys.nextElement();
               Object value = defaultBundle.getObject(key);
               properties.put(key, value);
            }
         }
         catch (Exception e)
         {
            System.out.println("Could not lookup carnot.properties.");
         }
      }

      public String getString(String name)
      {
         String value;

         if ((value = (String) properties.get(name)) != null)
         {
            return value.trim();
         }

         if ((value = (String) systemProperties.get(name)) != null)
         {
            return value.trim();
         }

         return null;
      }

      public String getString(String name, String defaultValue)
      {
         String result = getString(name);

         if (result != null)
         {
            return result;
         }

         return defaultValue;
      }

      /*public boolean getBoolean(String name, boolean defaultValue)
      {
         final String[] trueWords = {"true", "enabled", "on"};
         final String[] falseWords = {"false", "disabled", "off"};

         String value = getString(name);

         if (value == null)
         {
            return defaultValue;
         }

         value = value.toLowerCase().trim();

         for (int i = 0; i < trueWords.length; ++i)
         {
            if (trueWords[i].equals(value))
            {
               return true;
            }
         }

         for (int i = 0; i < falseWords.length; ++i)
         {
            if (falseWords[i].equals(value))
            {
               return false;
            }
         }

         throw new PublicException(
               BaseErrorCase.BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_TRUE_OR_FALSE
                     .raise(value, name));
      }*/

      public Collection<String> getStrings(String name)
      {
         return getStrings(name, ",");
      }

      public Collection<String> getStrings(String name, String separators)
      {
         LinkedList<String> result = new LinkedList<String>();
         String str = getString(name);

         if (str == null)
         {
            return result;
         }
         else
         {
            StringTokenizer tkr = new StringTokenizer(str, separators);
            while (tkr.hasMoreTokens())
            {
               result.add(tkr.nextToken().trim());
            }
            return result;
         }
      }
   }
}
