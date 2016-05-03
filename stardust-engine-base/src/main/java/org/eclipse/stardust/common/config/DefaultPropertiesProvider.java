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

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;

/**
 * @author rsauer
 * @version $Revision$
 */
public class DefaultPropertiesProvider extends AbstractPropertiesBundleProvider
      implements PropertyProvider
{
   private static final String HIDDEN_VALUE_REPLACER = "***";
   
   private List<String> propertyKeyDisplayBlackList;
   
   public DefaultPropertiesProvider()
   {
      super(Parameters.getDefaultProperties());
      
      this.propertyKeyDisplayBlackList = CollectionUtils.newList();
      
      this.propertyKeyDisplayBlackList.add("AuditTrail.Password");
      this.propertyKeyDisplayBlackList.add("Security.Principal.Secret");
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

   @Override
   public String getPropertyDisplayValue(String key)
   {
      if (this.propertyKeyDisplayBlackList.contains(key))
      {
         return HIDDEN_VALUE_REPLACER;
      }
      return getProperties().get(key).toString();
   }

}