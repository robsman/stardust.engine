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
package org.eclipse.stardust.engine.core.spi.dms;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.stardust.engine.core.runtime.beans.AbstractResourceBundle;

/**
 * A resource bundle containing properties required by repositories.
 * 
 * @author Roland.Stamm
 *
 */
public class RepositoryResourceBundle extends AbstractResourceBundle

{
   private static final long serialVersionUID = -569294969361872702L;

   /**
    * The moduleId represents all repository provider resource bundles.<br>
    * All repository related resource bundles should be handled by this module.
    */
   public final static String MODULE_ID = "repository-provider";

   /**
    * The property for keys in {@link IRepositoryConfiguration#getAttributes()}.
    */
   public final static String REPOSITORY_CONFIGURATION_NAME = "RepositoryConfiguration.name.";

   /**
    * The property for values in {@link IRepositoryConfiguration#getAttributes()}.
    */
   public final static String REPOSITORY_CONFIGURATION_VALUE = "RepositoryConfiguration.value.";

   public RepositoryResourceBundle(ResourceBundle bundle)
   {
      super(bundle);
   }

   public RepositoryResourceBundle(Map<String, Serializable> resources, Locale locale)
   {
      super(resources, locale);
   }

}
