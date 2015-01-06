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

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;



import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ResourceBundle;

/**
 * @author Roland.Stamm
 *
 */
public abstract class AbstractResourceBundle implements ResourceBundle
{
   private static final long serialVersionUID = 8757077247042223366L;

   protected Map<String, Serializable> resources = CollectionUtils.newHashMap();

   protected Locale locale;

   public AbstractResourceBundle(Map<String, Serializable> resources, Locale locale)
   {
      this.resources = resources;
      this.locale = locale;
   }

   public AbstractResourceBundle(java.util.ResourceBundle bundle)
   {
      this.locale = bundle.getLocale();
      Enumeration<String> keys = bundle.getKeys();

      while (keys.hasMoreElements())
      {
         String key = keys.nextElement();
         String value = bundle.getString(key);

         resources.put(key, value);
      }
   }

   @Override
   public Map<String, Serializable> getResources()
   {
      return Collections.unmodifiableMap(resources);
   }

   @Override
   public Locale getLocale()
   {
      return locale;
   }

}
