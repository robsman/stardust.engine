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
package org.eclipse.stardust.engine.core.model.convert.income;

import java.util.HashMap;

public class DefaultIncomeApplication
{
   private HashMap dataMap;
   
   public DefaultIncomeApplication()
   {
      this.dataMap = new HashMap();
   }
   
   public Object getObject(String key)
   {
      if (this.dataMap.containsKey(key))
      {
         return this.dataMap.get(key);
      }
      return null;
   }
   
   public void setObject(String key, Object object)
   {
      if (this.dataMap.containsKey(key))
      {
         this.dataMap.remove(key);
      }
      this.dataMap.put(key, object);
   }
   
   public void complete()
   {
      
   }
}
