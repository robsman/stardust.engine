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

import org.eclipse.stardust.common.IdentityHashMap;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryPool;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ThreadLocalServiceFactoryPool implements ServiceFactoryPool
{
   // Using double indexing, to make sure both containers stay synchronized.
   private ThreadLocal services = new ThreadLocal()
   {
      protected Object initialValue()
      {
         return new HashMap();
      }
   };

   // It is very important to use identity to find stored services, as due to
   // various Proxy decorations the equals() contract is broken.
   private Map pool = new IdentityHashMap();

   public Object get(Class service)
   {
      return ((Map) services.get()).get(service);
   }

   public void put(Class type, Object service)
   {
      pool.put(service, type);
     ((Map)services.get()).put(type, service);
   }

   public void remove(Object service)
   {
      Class serviceType = (Class) pool.get(service);
      // @todo (france, ub): warn if object is not in the pool;
      ((Map) services.get()).remove(serviceType);
      pool.remove(service);
   }

   public Iterator iterator()
   {
      return pool.keySet().iterator();
   }

   public void clear()
   {
      pool.clear();
      ((Map) services.get()).clear();
   }
}
