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
package org.eclipse.stardust.engine.core.spi.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public abstract class CacheAdapterBase<C extends ConcurrentMap<Object, Object>>
      implements Cache
{
   protected C delegate;

   // //
   // // IPP
   // //

   protected void beforeCacheAccess(boolean read)
   {
   }

   protected void afterCacheAccess(boolean read)
   {
   }

   // //
   // // jcache
   // //

   public Object peek(Object key)
   {
      try
      {
         beforeCacheAccess(true);

         return delegate.get(key);
      }
      finally
      {
         afterCacheAccess(true);
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public Map getAll(Collection keys) throws CacheException
   {
      try
      {
         beforeCacheAccess(true);

         Map result = new HashMap();
         for (Object key : keys)
         {
            result.put(key, delegate.get(key));
         }

         return result;
      }
      finally
      {
         afterCacheAccess(true);
      }
   }

   // //
   // // java.util.Map
   // //

   public boolean isEmpty()
   {
      try
      {
         beforeCacheAccess(true);

         return delegate.isEmpty();
      }
      finally
      {
         afterCacheAccess(true);
      }
   }

   public boolean containsKey(Object key)
   {
      try
      {
         beforeCacheAccess(true);

         return delegate.containsKey(key);
      }
      finally
      {
         afterCacheAccess(true);
      }
   }

   public Object get(Object key)
   {
      try
      {
         beforeCacheAccess(true);

         return delegate.get(key);
      }
      finally
      {
         afterCacheAccess(true);
      }
   }

   public Object put(Object key, Object value)
   {
      try
      {
         beforeCacheAccess(false);

         return delegate.put(key, value);
      }
      finally
      {
         afterCacheAccess(false);
      }
   }

   public Object remove(Object key)
   {
      try
      {
         beforeCacheAccess(false);

         return delegate.remove(key);
      }
      finally
      {
         afterCacheAccess(false);
      }
   }

   public Object putIfAbsent(Object key, Object value)
   {
      try
      {
         beforeCacheAccess(false);

         return delegate.putIfAbsent(key, value);
      }
      finally
      {
         afterCacheAccess(false);
      }
   }

   public void clear()
   {
      try
      {
         beforeCacheAccess(false);

         delegate.clear();
      }
      finally
      {
         afterCacheAccess(false);
      }
   }

}
