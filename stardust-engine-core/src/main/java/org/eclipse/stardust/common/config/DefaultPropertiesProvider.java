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

/**
 * @author rsauer
 * @version $Revision$
 */
public class DefaultPropertiesProvider extends AbstractPropertiesBundleProvider
      implements PropertyProvider
{

   public DefaultPropertiesProvider()
   {
      super(Parameters.getDefaultProperties());
   }

   public static class Factory implements GlobalParametersProviderFactory
   {
      public int getPriority()
      {
         // usually load first
         return 1;
      }

      public PropertyProvider getPropertyProvider()
      {
         return new DefaultPropertiesProvider();
      }
   }

}