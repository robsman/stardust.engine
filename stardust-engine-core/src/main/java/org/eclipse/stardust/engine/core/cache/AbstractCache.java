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
package org.eclipse.stardust.engine.core.cache;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.spi.cache.Cache;



/**
 * @author Florin.Herinean
 */
public abstract class AbstractCache <V> 
{
   private static final Logger trace = LogManager.getLogger(AbstractCache.class);
   
   private Cache cache = null;
   
   public AbstractCache(String id)
   {
      cache = CacheHelper.createCache(id);
   }
   
   abstract PrimaryKey getKeyForValue(V value);
   
   abstract PrimaryKey getKey(long oid);
   
   abstract V retrieve(byte[] bytes) throws IOException;
   
   abstract List<? extends CacheKey> getSecondaryKeys(V value);
   
   V getValue(PrimaryKey key)
   {
      if (cache == null)
      {
         return null;
      }
      Object result = cache.get(key);
      if (result != null)
      {
         if (result instanceof byte[])
         {
            try
            {
               result = retrieve((byte[]) result);
            }
            catch (IOException ex)
            {
               trace.warn("Error retrieving object for key '" + key + "'.");
               return null;
            }
         }
         //trace.info("Retrieved '" + result + "' from cache.");
      }
      return (V) result;
   }
   
   PrimaryKey getPrimaryKey(CacheKey key)
   {
      if (cache == null)
      {
         return null;
      }
      return (PrimaryKey) cache.get(key);
   }
   
   void set(PrimaryKey key, V value)
   {
      if (cache != null)
      {
         if (value instanceof Cacheable)
         {
            try
            {
               byte[] bytes = ((Cacheable) value).store();
               cache.put(key, bytes);
            }
            catch (IOException ex)
            {
               trace.warn("Error storing '" + value + "'.");
               return;
            }
         }
         else
         {
            cache.put(key, value);
         }
         //trace.info("Updated '" + value + "' in the cache.");
      }
   }
   
   void removeKey(CacheKey key)
   {
      if (cache != null)
      {
         cache.remove(key);
      }
   }
   
   public V get(long oid)
   {
      return (V) getValue(getKey(oid));
   }

   public void set(V value, boolean force)
   {
      if (cache != null)
      {
         PrimaryKey key = getKeyForValue(value);
         
         try
         {
            set(key, value);
            List< ? extends CacheKey> secondaryKeys = getSecondaryKeys(value);
            for (CacheKey secondary : secondaryKeys)
            {
               if (force)
               {
                  cache.putIfAbsent(secondary, key);
               }
               else
               {
                  cache.put(secondary, key);
               }
            }
         }
         catch (ConcurrentModificationException e)
         {
            if (force)
            {
               throw e;
            }
            else
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Ignoring ConcurrentModificationException on 2nd level cache for: "
                        + value + " as requested.");
               }
            }
         }
      }
   }
   
   public void remove(V value)
   {
      if (cache != null)
      {
         PrimaryKey key = getKeyForValue(value);
         removeKey(key);
         List<? extends CacheKey> secondaryKeys = getSecondaryKeys(value);
         for (CacheKey secondary : secondaryKeys)
         {
            cache.remove(secondary);
         }
      }
   }
   
   public boolean isCached(V value)
   {
      if (cache != null)
      {
         return cache.containsKey(getKeyForValue(value));
      }
      return false;
   }

   public void clear()
   {
      if (cache != null)
      {
         cache.clear();
      }
   }
}
