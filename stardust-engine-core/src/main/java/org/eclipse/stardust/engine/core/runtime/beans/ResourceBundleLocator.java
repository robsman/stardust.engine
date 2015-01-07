/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ResourceBundle;
import org.eclipse.stardust.engine.core.spi.resources.IResourceBundleProvider;

/**
 * Locates and holds instances implementing the {@link IResourceBundleProvider} SPI.
 * 
 * @author Roland.Stamm
 *
 */
public class ResourceBundleLocator
{

   private static ResourceBundleLocator INSTANCE;

   private List<IResourceBundleProvider> providers = CollectionUtils.newList();

   public static ResourceBundleLocator getInstance()
   {
      if (INSTANCE == null)
      {
         INSTANCE = new ResourceBundleLocator();
      }
      return INSTANCE;
   }

   public ResourceBundleLocator()
   {
      ServiceLoader<IResourceBundleProvider.Factory> loader = ServiceLoader.load(IResourceBundleProvider.Factory.class);
      Iterator<IResourceBundleProvider.Factory> loaderIterator = loader.iterator();
      while (loaderIterator.hasNext())
      {
         IResourceBundleProvider.Factory providerFactory = (IResourceBundleProvider.Factory) loaderIterator.next();
         IResourceBundleProvider provider = providerFactory.getInstance();
         providers.add(provider);
      }
   }

   /**
    * Finds first the {@link ResourceBundle} matching the moduleId and bundleName.
    * 
    * @param moduleId The module identifier.
    * @param bundleName The name of the resource bundle.
    * @param locale The locale in which the resource bundle should be retrieved.
    * @return The {@link ResourceBundle} or null if none is found.
    */
   public ResourceBundle getResourceBundle(String moduleId, String bundleName,
         Locale locale)
   {
      for (IResourceBundleProvider provider : providers)
      {
         if (moduleId != null && moduleId.equals(provider.getModuleId()))
         {
            ResourceBundle resourceBundle = provider.getResourceBundle(bundleName, locale);
            if (resourceBundle != null)
            {
               return resourceBundle;
            }
         }
      }
      return null;
   }

}
