/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.common.config;

import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;
import static org.eclipse.stardust.common.CollectionUtils.newSet;
import static org.eclipse.stardust.common.CollectionUtils.newSortedMap;
import static org.eclipse.stardust.common.config.ValueProviderUtils.precalculatedValueProvider;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters.IDisposable;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;


public class GlobalParameters
{
   private static final Logger trace = LogManager.getLogger(GlobalParameters.class);

   private static final ReentrantReadWriteLock acquisitionLock = new ReentrantReadWriteLock();

   private static final ReadLock globalsReadLock = acquisitionLock.readLock();

   private static final WriteLock globalsWriteLock = acquisitionLock.writeLock();

   private static final AtomicReference<GlobalParameters> singleton = new AtomicReference<GlobalParameters>();

   private final AtomicInteger usageCount = new AtomicInteger(0);

   private final AtomicInteger closing = new AtomicInteger(0);

   private static boolean constructing = false;

   private ConcurrentMap<String, Object> localProperties = CollectionUtils.newConcurrentHashMap();

   private final Map<String, Object> systemProperties;

   private Set<String> userBundleNames;

   /**
    * @return a singleton object of this class
    */
   public static GlobalParameters globals()
   {
      ParametersFacade params = (ParametersFacade) Parameters.instance();

      GlobalParameters instance = params.getGlobalsFromFacade();

      if (null == instance)
      {
         instance = getSingleton();
      }

      return instance;
   }

   private static GlobalParameters getSingleton()
   {
      GlobalParameters instance = singleton.get();
      if (null == instance)
      {
         synchronized (singleton)
         {
            instance = singleton.get();
            if (null == instance)
            {
               instance = new GlobalParameters();
               if ( !singleton.compareAndSet(null, instance))
               {
                  throw new InternalException(
                        "Failed bootstrapping internal configuration: concurrency failure.");
               }
            }
         }
      }
      return instance;
   }

   private GlobalParameters()
   {
      LogUtils.traceObject(this, true);

      // copy system properties to avoid improve concurrency (Properties is heavily
      // synchronized)
      this.systemProperties = CollectionUtils.newMap();
      try
      {
         Properties sysProps = System.getProperties();

         for (Map.Entry<Object, Object> property : sysProps.entrySet())
         {
            systemProperties.put((String) property.getKey(), property.getValue());
         }
      }
      catch (Exception e)
      {
         systemProperties.clear();
      }
      this.localProperties = CollectionUtils.newConcurrentHashMap();
      this.userBundleNames = CollectionUtils.newSet();

      // initSharedResources must only be called if it was not already called before and
      // is still initializing, otherwise it can run into an endless loop in some cases
      // (e.g. on Weblogic when WeblogicLogger is used)
      if ( !constructing)
      {
         constructing = true;
         initSharedResources();
         constructing = false;
      }
   }

   public static boolean isConstructing()
   {
      return constructing;
   }

   synchronized void flush()
   {
      globalsWriteLock.lock();
      try
      {
         GlobalParameters currentSingleton = singleton.get();
         if (this == currentSingleton)
         {
            Assert.condition(singleton.compareAndSet(this, null),
                  "Race condition while flushing current configuration singleton.");
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Configuration singleton " + this
                     + " was already superceeded by " + currentSingleton
                     + ", skipping initialization of new singleton.");
            }
         }

         // clean up current instance
         closing.incrementAndGet();
         if (0 == usageCount.get())
         {
            // clean up immediately if nobody else is referencing
            cleanup();
         }
      }
      finally
      {
         globalsWriteLock.unlock();
      }
   }

   protected static GlobalParameters acquire()
   {
      globalsReadLock.lock();
      try
      {
         GlobalParameters globals = getSingleton();

         Assert.condition(0 == globals.closing.get(),
               "Attempting to access global parameters after flushing.");

         globals.usageCount.incrementAndGet();

         return globals;
      }
      finally
      {
         globalsReadLock.unlock();
      }
   }

   protected static void release(GlobalParameters globals)
   {
      if (0 == globals.usageCount.decrementAndGet())
      {
         globals.cleanup();
      }
   }

   private void cleanup()
   {
      if ((0 != closing.get()) && (0 >= usageCount.get()))
      {
         Map<String, Object> properties = this.localProperties;
         for (Map.Entry<String, Object> prop : properties.entrySet())
         {
            if (prop.getValue() instanceof IDisposable)
            {
               ((IDisposable) prop.getValue()).dispose();
            }
         }

         this.userBundleNames.clear();
         this.localProperties.clear();
         this.systemProperties.clear();
         this.userBundleNames = null;
      }
   }

   private void initSharedResources()
   {
      List<GlobalParametersProviderFactory> providerFactories = FactoryFinder.findFactories(
            GlobalParametersProviderFactory.class,
            DefaultPropertiesProvider.Factory.class, null);

      Set<Class<?>> factoryNames = newSet();
      SortedMap<Integer, GlobalParametersProviderFactory> factoriesByPriority = newSortedMap();
      for (int i = 0; i < providerFactories.size(); ++i)
      {
         GlobalParametersProviderFactory providerFactory = providerFactories.get(i);

         if (trace.isDebugEnabled())
         {
            trace.debug(MessageFormat.format(
                  "Found factory class {0} (classloader: {1}).", new Object[] {
                        providerFactory.getClass().getName(),
                        providerFactory.getClass().getClassLoader()}));
         }

         if (factoryNames.contains(providerFactory.getClass()))
         {
            trace.debug("Ignoring multiple occurance of provider factory "
                  + providerFactory.getClass().getName());

            continue;
         }
         else
         {
            factoryNames.add(providerFactory.getClass());
            factoriesByPriority.put(new Integer(providerFactory.getPriority()),
                  providerFactory);
         }
      }

      this.localProperties = CollectionUtils.newConcurrentHashMap();

      for (GlobalParametersProviderFactory providerFactory : factoriesByPriority.values())
      {
         trace.info("Loading properties via provider factory "
               + providerFactory.getClass().getName() + " (priority "
               + providerFactory.getPriority() + ").");

         try
         {
            PropertyProvider provider = providerFactory.getPropertyProvider();

            if (trace.isDebugEnabled())
            {
               trace.debug("About to add properties from provider "
                     + provider.getClass().getName() + ".");
            }
            final Logger providerTrace = LogManager.getLogger(provider.getClass());
            for (Map.Entry<String, Object> entry : provider.getProperties().entrySet())
            {
               final String key = entry.getKey();
               final Object value = entry.getValue();

               if ( !key.equals("AuditTrail.Password") && !key.equals("Security.Principal.Secret"))
               {
                  final Object oldValue = this.localProperties.get(key);

                  StringBuffer logMsg = new StringBuffer();
                  logMsg.append("  ").append(key).append(" = ").append(value);
                  if (null != oldValue)
                  {
                     logMsg.append(" (overriding previous value: ").append(oldValue)
                           .append(")");
                  }
                  providerTrace.info(logMsg);
               }

               if (value == null)
               {
                  trace.warn("Removing property with key '" + key + "' as value is null.");
               }
               set(key, value);
            }

            // for bundle based providers: register bundle as loaded
            if (provider instanceof AbstractPropertiesBundleProvider)
            {
               userBundleNames.add(((AbstractPropertiesBundleProvider) provider).getBundleName());
            }
         }
         catch (Exception ex)
         {
            // ignore faulty provider
            trace.warn("Skipping global parameters provider factory: " + providerFactory,
                  ex);
         }
      }

      List<String> dependent = getStrings("Dependent.Properties");
      for (String dep : dependent)
      {
         try
         {
            addProperties(dep, getClass().getClassLoader());
         }
         catch (Exception e)
         {
            trace.warn("Could not lookup " + dep + ".properties. Current classloader = "
                  + getClass().getClassLoader(), e);
         }
      }
   }

   private final List<String> getStrings(String name)
   {
      List<String> result = newLinkedList();
      Object str = get(name);

      if (str == null)
      {
         return result;
      }
      else
      {
         StringTokenizer tkr = new StringTokenizer(str.toString(), ",");
         while (tkr.hasMoreTokens())
         {
            result.add(tkr.nextToken().trim());
         }
         return result;
      }
   }

   /**
    * There must be a a file "<fileName>.properties" in one of the directories provided
    * with the classpath. This file will be used as the properties file.
    *
    * @param fileName
    *           the name of the file storing your properties (e.g. "carnot" will render
    *           "carnot.properties")
    */
   public synchronized void addProperties(String fileName)
   {
      addProperties(fileName, getClass().getClassLoader());
   }

   private void addProperties(String fileName, ClassLoader classLoader)
   {
      if (userBundleNames.contains(fileName))
      {
         trace.warn("Properties '" + fileName + "' will not be added as they already "
               + "exist. Classloader = " + classLoader);
         return;
      }

      try
      {
         trace.info("Adding properties '" + fileName + "'. Classloader = " + classLoader);
         ResourceBundle bundle = ResourceBundle.getBundle(fileName, Locale.getDefault(),
               classLoader);

         for (Enumeration<String> i = bundle.getKeys(); i.hasMoreElements();)
         {
            String key = i.nextElement();
            Object value = bundle.getObject(key);

            if (value == null)
            {
               trace.warn("Removing property with key '" + key + "' as value is null.");
            }
            set(key, value);
            trace.info("  " + key + " = " + value);
         }
         userBundleNames.add(fileName);
      }
      catch (MissingResourceException e)
      {
         throw new InternalException("Cannot load '" + fileName
               + ".properties' for locale '" + Locale.getDefault().toString()
               + "'. Current classloader = " + classLoader, e);
      }
   }

   public Object get(String name)
   {
      Object value = localProperties.get(name);

      if (value == null)
      {
         if (name != null)
         {
            value = systemProperties.get(name);
         }
      }

      return value;
   }

   public void set(String name, Object value)
   {
      if (name == null)
      {
         trace.warn("Undefinded Behavior for properties with name equal to null.");
      }

      if (null != value)
      {
         localProperties.put(name, value);
      }
      else
      {
         localProperties.remove(name);
      }
   }

   /**
    * @deprecated Superseded by {@link #getOrInitialize(String, ValueProvider)}.
    */
   @Deprecated
   public synchronized Object initializeIfAbsent(String name, ValueProvider initializationCallback)
   {
      return getOrInitialize(name, initializationCallback);
   }

   /**
    * Retrieve an entry from the configuration repository. If no entry exists, initialize
    * it based on the given value provider.
    * <p>
    * As the value provider will only be invoked if no current entry exists, this method
    * is recommended for values imposing nontrivial initialization cost.
    *
    * @param name
    *           the name of the entry to be retrieved
    * @param initializationCallback
    *           the provider of an initial value for the entry, if no entry exists so far
    * @return the value associated with the given name
    *
    * @see #getOrInitialize(String, Object)
    */
   public synchronized Object getOrInitialize(String name, ValueProvider initializationCallback)
   {
      if (null == initializationCallback)
      {
         throw new NullPointerException("Initial value provider must not be null.");
      }

      if ( !localProperties.containsKey(name))
      {
         // perform initialization

         Object initialValue = initializationCallback.getValue();
         if (null != initialValue)
         {
            if (null != localProperties.putIfAbsent(name, initialValue))
            {
               trace.warn("Race condition while initializing property '" + name + "'.");
            }
         }
      }

      return get(name);
   }

   /**
    * Retrieve an entry from the configuration repository. If no entry exists, initialize
    * it based on the given value provider.
    *
    * @param name
    *           the name of the entry to be retrieved
    * @param initialValue
    *           the initial value for the entry, if no entry exists so far
    * @return the value associated with the given name
    *
    * @see #getOrInitialize(String, ValueProvider)
    */
   public synchronized Object getOrInitialize(String name, final Object initialValue)
   {
      if (null == initialValue)
      {
         throw new NullPointerException("Initial value must not be null.");
      }

      return getOrInitialize(name, precalculatedValueProvider(initialValue));
   }

}
