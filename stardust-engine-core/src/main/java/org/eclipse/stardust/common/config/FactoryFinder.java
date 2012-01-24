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
package org.eclipse.stardust.common.config;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * This code is designed to implement the pluggability feature and is designed to both
 * compile and run on JDK version 1.1 and later. The code also runs both as part of an
 * unbundled jar file and when bundled as part of the JDK.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class FactoryFinder
{
   private static final Logger trace = LogManager.getLogger(FactoryFinder.class);

   /**
    * Finds the implementation Class object in the specified order. Main entry point.
    * 
    * @return Class object of factory, never null
    * 
    * @param factoryId
    *           Interface of the factory to find, name of interface is same as a property
    *           name
    * @param fallbackClass
    *           Implementation class, if nothing else is found. Use null to mean no
    *           fallback.
    * 
    * @exception FactoryFinder.ConfigurationError
    */
   public static <T> T findFactory(Class<T> factoryId, Class<? extends T> fallbackClass,
         String propertyFile) throws ConfigurationError
   {
      Object factory = findFactory(factoryId.getName(), (null != fallbackClass)
            ? fallbackClass.getName()
            : null, propertyFile);

      return factoryId.cast(factory);
   }

   /**
    * Finds the implementation Class object in the specified order. Main entry point.
    * 
    * @return Class object of factory, never null
    * 
    * @param factoryId
    *           Name of the factory to find, same as a property name
    * @param fallbackClassName
    *           Implementation class name, if nothing else is found. Use null to mean no
    *           fallback.
    * 
    * @exception FactoryFinder.ConfigurationError
    */
   public static Object findFactory(String factoryId, String fallbackClassName,
         String propertyFile) throws ConfigurationError
   {
      List<Object> factories = findFactories(factoryId, fallbackClassName, propertyFile);

      Object factory = factories.get(0);

      if (1 < factories.size())
      {
         trace.debug("Ignoring all but first factory: " + factoryId + " = "
               + factory.getClass().getName());
      }

      return factory;
   }

   /**
    * Finds multiple implementation Class objects in the specified order. Main entry
    * point.
    * 
    * @return List of Class objects of factory, never empty
    * 
    * @param factoryId
    *           Interface of the factory to find, name of interface is same as a property
    *           name
    * @param fallbackClass
    *           Implementation class, if nothing else is found. Use null to mean no
    *           fallback.
    * 
    * @exception FactoryFinder.ConfigurationError
    */
   @SuppressWarnings("unchecked")
   public static <T> List<T> findFactories(Class<T> factoryId, Class<? extends T> fallbackClass,
         String propertyFile) throws ConfigurationError
   {
      // the implementation guarantees that all elements are assignment compatible with T
      return (List<T>) findFactories(factoryId.getName(), (null != fallbackClass)
            ? fallbackClass.getName()
            : null, propertyFile);
   }

   @SuppressWarnings("unchecked")
   public static <T> List<T> findFactories(Class<T> factoryId, Class<? extends T> fallbackClass,
         String propertyFile, List<String> factories,
         Set<String> blacklistedFactories)
         throws ConfigurationError
   {
      // the implementation guarantees that all elements are assignment compatible with T
      return (List<T>) findFactories(factoryId.getName(), (null != fallbackClass)
            ? fallbackClass.getName()
            : null, propertyFile, factories, blacklistedFactories);
   }

   /**
    * Finds multiple implementation Class objects in the specified order. Main entry
    * point.
    * 
    * @return List of Class objects of factory, never empty
    * 
    * @param factoryId
    *           Name of the factory to find, same as a property name
    * @param fallbackClassName
    *           Implementation class name, if nothing else is found. Use null to mean no
    *           fallback.
    * 
    * @exception FactoryFinder.ConfigurationError
    */
   public static List<Object> findFactories(String factoryId, String fallbackClassName,
         String propertyFile) throws ConfigurationError
   {
      return findFactories(factoryId, fallbackClassName, propertyFile,
            null, null);
   }

   public static List<Object> findFactories(String factoryId, String fallbackClassName,
         String propertyFile, List<String> factories,
         Set<String> blacklistedFactories)
         throws ConfigurationError
   {
      if (null == factories)
      {
         factories = Collections.emptyList();
      }
      if (null == blacklistedFactories)
      {
         blacklistedFactories = Collections.emptySet();
      }
      
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      Class<?> factoryInterface;
      try
      {
         factoryInterface = classLoader.loadClass(factoryId);
      }
      catch (ClassNotFoundException e)
      {
         trace.debug(
               "Factory ID can not be resolved to an interface/class: " + factoryId, e);

         factoryInterface = null;
      }

      final List<Object> result = CollectionUtils.newList();

      // system property has highest priority to allow for override
      addFactory(result, findFromSystemProperty(factoryId, classLoader),
            factoryInterface, blacklistedFactories);
      
      // 2nd highest: factory configured in $JAVA_HOME/lib/propertyFile
      addFactory(result, findFromPropertyFile(factoryId, propertyFile, classLoader),
            factoryInterface, blacklistedFactories);

      // 3rd highest: factories explicitly passed in
      for (int i = 0; i < factories.size(); ++i)
      {
         String factoryName = (String) factories.get(i);
         
         addFactory(result, findFromClassName(factoryName, classLoader),
               factoryInterface, blacklistedFactories);
      }
      
      // 4th highest: factories declared in META-INF/services/factoryId files
      List factoriesFromServices = findFromServices(factoryId, classLoader,
            blacklistedFactories);
      for (int i = 0; i < factoriesFromServices.size(); ++i)
      {
         addFactory(result, factoriesFromServices.get(i), factoryInterface,
               blacklistedFactories);
      }

      // if no declared factory was found, optionally fall back to default
      if (result.isEmpty() && (null != fallbackClassName))
      {
         trace.debug("Loaded from fallback value: " + factoryId + " = "
               + fallbackClassName);

         addFactory(result, newInstance(fallbackClassName, classLoader),
               factoryInterface, blacklistedFactories);
      }

      if (result.isEmpty())
      {
         throw new ConfigurationError("Provider for " + factoryId + " cannot be found",
               null);
      }

      return result;
   }

   public static Object findFromSystemProperty(String factoryId, ClassLoader classLoader)
         throws ConfigurationError
   {
      Object factory = null;

      try
      {
         String systemProp = System.getProperty(factoryId);
         if (null != systemProp)
         {
            trace.debug("Found system property: " + factoryId + " = " + systemProp);

            factory = newInstance(systemProp, classLoader);
         }
      }
      catch (SecurityException se)
      {
      }

      return factory;
   }

   public static Object findFromPropertyFile(String factoryId, String propertyFile,
         ClassLoader classLoader) throws ConfigurationError
   {
      Object factory = null;

      if ((null != propertyFile) && (0 < propertyFile.length()))
      {
         // try to read from $java.home/lib/propertyFile
         try
         {
            File javaHome = new File(System.getProperty("java.home"));
            File configFile = new File(new File(javaHome, "lib"), propertyFile);
            if (configFile.exists())
            {
               Properties props = new Properties();

               FileInputStream iStream = new FileInputStream(configFile);
               try
               {
                  props.load(iStream);
               }
               finally
               {
                  iStream.close();
               }

               String factoryClassName = props.getProperty(factoryId);
               trace.debug("Found java.home property: " + factoryId + " = "
                     + factoryClassName);

               factory = newInstance(factoryClassName, classLoader);
            }
         }
         catch (Exception ex)
         {
            trace.debug("Failed retrieving factory from property file.", ex);
         }
      }

      return factory;
   }

   public static Object findFromClassName(String factoryClassName, ClassLoader classLoader)
         throws ConfigurationError
   {
      Object factory = null;

      if ( !StringUtils.isEmpty(factoryClassName))
      {
         try
         {
            factory = newInstance(factoryClassName, classLoader);
         }
         catch (Exception ex)
         {
            trace.debug("Failed resolving factory by class name.", ex);
         }
      }

      return factory;
   }

   public static List findFromServices(String factoryId, ClassLoader classLoader)
      throws ConfigurationError
   {
      return findFromServices(factoryId, classLoader, Collections.EMPTY_SET);
   }

   public static List findFromServices(String factoryId, ClassLoader classLoader,
         Set<String> blacklistedFactories) throws ConfigurationError
   {
      List factories = Collections.EMPTY_LIST;

      String serviceId = "META-INF/services/" + factoryId;
      // try to find services in CLASSPATH
      try
      {
         Enumeration<URL> serviceSpecs;
         if (classLoader == null)
         {
            serviceSpecs = ClassLoader.getSystemResources(serviceId);
         }
         else
         {
            serviceSpecs = classLoader.getResources(serviceId);
         }

         if (serviceSpecs.hasMoreElements())
         {
            trace.debug("Found " + serviceId);

            factories = new ArrayList<Object>();
            while (serviceSpecs.hasMoreElements())
            {
               URL serviceSpec = serviceSpecs.nextElement();

               try
               {
                  String factoryClassName;

                  InputStream is = serviceSpec.openStream();
                  try
                  {
                     BufferedReader serviceIdReader = null;
                     try
                     {
                        try
                        {
                           // spec mandates UTF-8 encoding
                           serviceIdReader = new BufferedReader(new InputStreamReader(is,
                                 "UTF-8"));
                        }
                        catch (java.io.UnsupportedEncodingException e)
                        {
                           // well, just in case UTF-8 is not available (sic!)
                           serviceIdReader = new BufferedReader(new InputStreamReader(is));
                        }

                        factoryClassName = serviceIdReader.readLine();
                     }
                     finally
                     {
                        if (null != serviceIdReader)
                        {
                           serviceIdReader.close();
                        }
                     }
                  }
                  finally
                  {
                     is.close();
                  }

                  if ( !isEmpty(factoryClassName)
                        && !blacklistedFactories.contains(factoryClassName))
                  {
                     trace.debug("Loaded from services: " + factoryId + " = "
                           + factoryClassName + " (" + serviceSpec + ")");

                     factories.add(newInstance(factoryClassName, classLoader));
                  }
               }
               catch (Exception ex)
               {
                  trace.debug("Failed retrieving factory from service spec: "
                        + serviceSpec.toString(), ex);
               }
            }
         }

      }
      catch (Exception ex)
      {
         trace.debug("Failed retrieving factory from property file.", ex);
      }

      return factories;
   }

   /**
    * Create an instance of a class using the specified <code>ClassLoader</code>, or if
    * that fails from the <code>ClassLoader</code> that loaded this class.
    * 
    * @param className
    *           the name of the class to instantiate
    * @param classLoader
    *           a <code>ClassLoader</code> to load the class from
    * 
    * @return a new <code>Object</code> that is an instance of the class of the given
    *         name from the given class loader
    * @throws ConfigurationError
    *            if the class could not be found or instantiated
    */
   private static Object newInstance(String className, ClassLoader classLoader)
         throws ConfigurationError
   {
      try
      {
         if (classLoader != null)
         {
            try
            {
               return classLoader.loadClass(className).newInstance();
            }
            catch (ClassNotFoundException x)
            {
               // try again
            }
         }
         return Class.forName(className).newInstance();
      }
      catch (ClassNotFoundException x)
      {
         throw new ConfigurationError("Provider " + className + " not found", x);
      }
      catch (Exception x)
      {
         throw new ConfigurationError("Provider " + className
               + " could not be instantiated: " + x, x);
      }
   }

   private static <T> void addFactory(List<Object> factories, Object factory, Class<T> factoryInterface,
         Set<String> blacklistedFactories)
   {
      if (null != factory)
      {
         if ( !blacklistedFactories.contains(factory.getClass().getName()))
         {
            if ((null != factoryInterface)
                  && !factoryInterface.isAssignableFrom(factory.getClass()))
            {
               trace.warn("Ignoring provider " + factory.getClass().getName()
                     + " as it is not assignment compatible with factory interface "
                     + factoryInterface.getName() + ".");
               
               factory = null;
            }
            else if(null != factoryInterface)
            {
               factories.add(factoryInterface.cast(factory));
            }
         }
         else
         {
            trace.info("Ignoring blacklisted provider " + factory.getClass().getName()
                  + " for factory interface " + factoryInterface.getName() + ".");
         }
      }
   }

   public static class ConfigurationError extends Error
   {
      private static final long serialVersionUID = 1L;

      // fixme: should this be refactored to use the jdk1.4 exception
      // wrapping?

      private Exception exception;

      /**
       * Construct a new instance with the specified detail string and exception.
       * 
       * @param msg
       *           the Message for this error
       * @param x
       *           an Exception that caused this failure, or null
       */
      ConfigurationError(String msg, Exception x)
      {
         super(msg);
         this.exception = x;
      }

      Exception getException()
      {
         return exception;
      }
   }

}
