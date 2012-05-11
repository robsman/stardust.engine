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

import static org.eclipse.stardust.common.CollectionUtils.newMap;

import java.util.*;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractPropertiesBundleProvider implements PropertyProvider
{
   private final Map<String, Object> properties;

   private final String bundleName;

   public AbstractPropertiesBundleProvider(String bundleName)
   {
      this(bundleName, Thread.currentThread().getContextClassLoader());
   }

   public AbstractPropertiesBundleProvider(String bundleName, ClassLoader classLoader)
   {
      this.bundleName = bundleName;

      this.properties = Collections.unmodifiableMap(readProperties(bundleName,
            classLoader));
   }

   public Map<String, Object> getProperties()
   {
      return properties;
   }

   public String getBundleName()
   {
      return bundleName;
   }

   public Map<String, Object> readProperties(String bundleName, ClassLoader classLoader)
   {
      if (null == classLoader)
      {
         classLoader = Thread.currentThread().getContextClassLoader();
      }
      if (null == classLoader)
      {
         classLoader = AbstractPropertiesBundleProvider.class.getClassLoader();
      }

      final Logger providerTrace = LogManager.getLogger(getClass());
      
      Map<String, Object> properties = Collections.emptyMap();
      try
      {
         ResourceBundle bundle = ResourceBundle.getBundle(bundleName,
               Locale.getDefault(), classLoader);

         if (null != providerTrace)
         {
            providerTrace.info("Property bundle " + bundleName
                  + ".properties bootstrapped from classloader " + classLoader + ":");
         }

         properties = readProperties(bundle);
      }
      catch (MissingResourceException e)
      {
         if (null != providerTrace)
         {
            providerTrace.warn("Property bundle '" + bundleName
                  + ".properties' not found. Current classloader = " + classLoader);
         }
      }
      catch (Exception e)
      {
         if (null != providerTrace)
         {
            providerTrace.warn("Could not lookup property bundle " + bundleName
                  + ".properties. Current classloader = " + classLoader, e);
         }
      }

      return properties;
   }

   public static Map<String, Object> readProperties(ResourceBundle bundle)
   {
      Map<String, Object> properties = newMap();

      Enumeration<String> keys = bundle.getKeys();
      while (keys.hasMoreElements())
      {
         String key = keys.nextElement();
         Object value = bundle.getObject(key);
         properties.put(key, value);
      }

      return properties;
   }

}