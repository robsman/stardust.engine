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

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;


/**
 * @author sauer
 * @version $Revision$
 */
public class CustomParameters extends Parameters
{

   private final Parameters parent;
   
   private final Map<String, Object> customParams = CollectionUtils.newMap();
   
   public CustomParameters()
   {
      this(null);
   }
   
   public CustomParameters(Parameters parent)
   {
      this.parent = (null != parent) ? parent : Parameters.instance();
   }
   
   public Object get(String name)
   {
      Object value = customParams.get(name);
      
      if (null == value)
      {
         value = parent.get(name);
      }
      
      return value;
   }
   
   public void set(String name, Object value)
   {
      customParams.put(name, value);
   }
   
   public void flush()
   {
      customParams.clear();
      
      parent.flush();
   }

   public void addProperties(String fileName)
   {
      parent.addProperties(fileName);
   }
   
}
