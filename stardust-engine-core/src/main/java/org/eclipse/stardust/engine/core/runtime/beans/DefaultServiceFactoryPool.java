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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ServiceFactoryPool;


public class DefaultServiceFactoryPool implements ServiceFactoryPool
{
   private Map services = new HashMap();

   public void clear()
   {
      services.clear();
   }

   public Object get(Class type)
   {
      return services.get(type);
   }

   public Iterator iterator()
   {
      return services.values().iterator();
   }

   public void put(Class type, Object service)
   {
      services.put(type, service);
   }

   public void remove(Object service)
   {
      Class typeToRemove = null;
      for (Iterator iter = services.keySet().iterator(); iter.hasNext();)
      {
         Class type = (Class) iter.next();
         if (service == services.get(type))
         {
            typeToRemove = type;
            break;
         }
      }
      services.remove(typeToRemove);
   }

}
