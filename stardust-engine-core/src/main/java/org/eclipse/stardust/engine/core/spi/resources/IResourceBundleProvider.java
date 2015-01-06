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
package org.eclipse.stardust.engine.core.spi.resources;

import java.util.Locale;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.api.runtime.ResourceBundle;

/**
 * @author Roland.Stamm
 *
 */
@SPI(status = Status.Stable, useRestriction = UseRestriction.Public)
public interface IResourceBundleProvider
{

   /**
    * Factory for {@link IResourceBundleProvider}.
    */
   public interface Factory
   {
      IResourceBundleProvider getInstance();
   }

   /**
    * Provides localized resources.
    * <p>
    * The main use is to provide localized labels for a UI.
    *
    * @param bundleName
    *           The name of the bundle to retrieve.
    *
    * @param locale
    *           The locale to request the resources for.
    * @return The ResourceBundle contains all resources for the selected locale.
    */
   public ResourceBundle getResourceBundle(String bundleName, Locale locale);

   /**
    * @return An id that identifies the resource bundle provider.
    */
   public String getModuleId();

}
