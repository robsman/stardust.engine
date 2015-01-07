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
package org.eclipse.stardust.common;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.LRUMap;

/**
 * A simple cache based on a fixed size map where the least-recently-used
 * objects are removed if the maximum cache size is exceeded. Objects are
 * removed as well when they are being accessed and the maximum caching 
 * time (timeout) has been reached 
 */
public class LRUCache
{
   public static final long NO_TIMEOUT = -1;
   
   private Map <Object,LRUCacheEntry> entryMap;
   private long ttlMs;
   
   /**
    * @param ttlMs maximum caching time, milliseconds or NO_TIMEOUT to 
    * avoid time-based entry expiration 
    * @param maxSize maximum number of objects in the cache
    * @param synchronize true if the map access should be synchronized 
    * using Collections.synchronizedMap()
    */
   public LRUCache(long ttlMs, int maxSize, boolean synchronize)
   {
      if (synchronize)
      {
         this.entryMap = Collections.synchronizedMap(new LRUMap(maxSize));
      }
      else
      {
         this.entryMap = new LRUMap(maxSize);
      }
      this.ttlMs = ttlMs;
   }
   
   public void put(Object id, Object value)
   {
      this.entryMap.put(id, new LRUCacheEntry(value, System.currentTimeMillis()));
   }
   
   public Object get(Object id)
   {
      LRUCacheEntry entry = (LRUCacheEntry) this.entryMap.get(id);
      if (entry == null)
      {
         return null;
      }
      else
      {
         if (entry.getCreateTime() + this.ttlMs > System.currentTimeMillis()
               || this.ttlMs == NO_TIMEOUT)
         {
            return entry.getValue();
         }
         else
         {
            return null;
         }
      }
   }

   public void clear()
   {
      this.entryMap.clear();
   }
   
   public Collection values()
   {
      List<Object> result = CollectionUtils.newList();
      for (LRUCacheEntry entry : this.entryMap.values())
      {
         if (entry.getCreateTime() + this.ttlMs > System.currentTimeMillis()
               || this.ttlMs == NO_TIMEOUT)
         {
            result.add(entry.getValue());
         }
      }
      return result;
   }
   
   private class LRUCacheEntry 
   {
      private Object value;
      private long createTime;
      
      public LRUCacheEntry(Object value, long createTime)
      {
         this.value = value;
         this.createTime = createTime;
      }

      public Object getValue()
      {
         return value;
      }

      public long getCreateTime()
      {
         return createTime;
      }
   }
   
}
