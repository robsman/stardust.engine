/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;

/**
 * Container class for options that control how a process instance is started
 *
 * @author Thomas.Wolfram
 *
 */
public class StartOptions implements Serializable
{
   private static final long serialVersionUID = 2L;

   public static final String TRIGGER = "trigger";

   public static final String BUSINESS_CALENDAR = "businessCalendar";

   private Map<String, ?> data;

   private boolean synchronously;

   private String benchmarkId;

   private Map<String, Serializable> properties;

   public Map<String, ?> getData()
   {
      return data;
   }

   public boolean isSynchronously()
   {
      return synchronously;
   }

   public String getBenchmarkId()
   {
      return benchmarkId;
   }

   public Serializable getProperty(String name)
   {
      return properties == null ? null : properties.get(name);
   }

   public void setProperty(String name, Serializable value)
   {
      if (properties == null)
      {
         properties = CollectionUtils.newMap();
      }
      properties.put(name, value);
   }

   public StartOptions(Map<String, ? > data, boolean synchronously)
   {
      this(data, synchronously, null);
   }

   public StartOptions(Map<String, ? > data, boolean synchronously,
         String benchmarkId)
   {
      this.data = data;
      this.synchronously = synchronously;
      this.benchmarkId = benchmarkId;
   }
}
