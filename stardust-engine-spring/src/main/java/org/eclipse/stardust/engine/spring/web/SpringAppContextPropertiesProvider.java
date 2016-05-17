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
package org.eclipse.stardust.engine.spring.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.PropertyProvider;
import org.eclipse.stardust.engine.api.spring.SpringConstants;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;


public class SpringAppContextPropertiesProvider implements PropertyProvider
{
   public Map getProperties()
   {
      WebApplicationContext webApplicationContext = ContextLoader
            .getCurrentWebApplicationContext();

      if (webApplicationContext == null)
      {
         return Collections.emptyMap();
      }

      Map properties = new HashMap();
      properties.put(SpringConstants.PRP_CACHED_APPLICATION_CONTEXT,
            webApplicationContext);
      return properties;
   }

   @Override
   public String getPropertyDisplayValue(String key)
   {
      return getProperties().get(key).toString();
   }
}
