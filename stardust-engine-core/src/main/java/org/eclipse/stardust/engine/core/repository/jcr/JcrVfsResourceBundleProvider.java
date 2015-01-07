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
package org.eclipse.stardust.engine.core.repository.jcr;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.stardust.engine.core.spi.dms.RepositoryResourceBundle;
import org.eclipse.stardust.engine.core.spi.resources.IResourceBundleProvider;

/**
 * @author Roland.Stamm
 *
 */
public class JcrVfsResourceBundleProvider
      implements IResourceBundleProvider, IResourceBundleProvider.Factory
{
   @Override
   public RepositoryResourceBundle getResourceBundle(String bundleName, Locale locale)
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      if (null == classLoader)
      {
         classLoader = JcrVfsRepositoryProvider.class.getClassLoader();
      }

      String composedBundleName = RepositoryResourceBundle.MODULE_ID +"-"+ bundleName;
      return new RepositoryResourceBundle(ResourceBundle.getBundle(composedBundleName,
            locale, classLoader));
   }

   @Override
   public IResourceBundleProvider getInstance()
   {
      return new JcrVfsResourceBundleProvider();
   }

   @Override
   public String getModuleId()
   {
      return RepositoryResourceBundle.MODULE_ID;
   }

}
