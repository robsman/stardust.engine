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
package org.eclipse.stardust.engine.core.cache.hazelcast;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.cache.CacheAdapterBase;
import org.eclipse.stardust.engine.runtime.utils.HazelcastUtils;

import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEntry;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Expression;
import com.hazelcast.query.Predicate;

public class HazelcastCacheAdapter extends CacheAdapterBase<IMap<Object, Object>>
{
   public static class SafeMap implements IMap<Object, Object>
   {
      private IMap<Object, Object> delegate;

      public SafeMap(String cacheName)
      {
         delegate = HazelcastUtils.getHazelcastInstance().getMap(cacheName);
      }

      private void handleException(Exception ex)
      {
         HazelcastInstance instance = HazelcastUtils.getHazelcastInstance();
         if (!instance.getLifecycleService().isRunning())
         {
            trace.warn("Hazelcast instance is not active !");
         }
      }

      @Override
      public int size()
      {
         try
         {
            return delegate.size();
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return 0;
      }

      @Override
      public boolean isEmpty()
      {
         try
         {
            return delegate.isEmpty();
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return true;
      }

      @Override
      public void putAll(Map<? extends Object, ? extends Object> m)
      {
         try
         {
            delegate.putAll(m);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
      }

      @Override
      public void clear()
      {
         try
         {
            delegate.clear();
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
      }

      @Override
      public InstanceType getInstanceType()
      {
         throw new UnsupportedOperationException("SafeMap.getInstanceType");
      }

      @Override
      public void destroy()
      {
         throw new UnsupportedOperationException("SafeMap.destroy");
      }

      @Override
      public Object getId()
      {
         throw new UnsupportedOperationException("SafeMap.getId");
      }

      @Override
      public boolean containsKey(Object key)
      {
         try
         {
            return delegate.containsKey(key);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return false;
      }

      @Override
      public boolean containsValue(Object value)
      {
         try
         {
            return delegate.containsValue(value);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return false;
      }

      @Override
      public Object get(Object key)
      {
         try
         {
            return delegate.get(key);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return null;
      }

      @Override
      public Object put(Object key, Object value)
      {
         try
         {
            return delegate.put(key, value);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return null;
      }

      @Override
      public Object remove(Object key)
      {
         try
         {
            return delegate.remove(key);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return null;
      }

      @Override
      public boolean remove(Object key, Object value)
      {
         try
         {
            return delegate.remove(key, value);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return false;
      }

      @Override
      public void flush()
      {
         throw new UnsupportedOperationException("SafeMap.flush");
      }

      @Override
      public String getName()
      {
         throw new UnsupportedOperationException("SafeMap.getName");
      }

      @Override
      public Map<Object, Object> getAll(Set<Object> keys)
      {
         throw new UnsupportedOperationException("SafeMap.getAll");
      }

      @Override
      public Future<Object> getAsync(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.getAsync");
      }

      @Override
      public Future<Object> putAsync(Object key, Object value)
      {
         throw new UnsupportedOperationException("SafeMap.putAsync");
      }

      @Override
      public Future<Object> removeAsync(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.removeAsync");
      }

      @Override
      public Object tryRemove(Object key, long timeout, TimeUnit timeunit)
            throws TimeoutException
      {
         throw new UnsupportedOperationException("SafeMap.tryRemove");
      }

      @Override
      public boolean tryPut(Object key, Object value, long timeout, TimeUnit timeunit)
      {
         throw new UnsupportedOperationException("SafeMap.tryPut");
      }

      @Override
      public Object put(Object key, Object value, long ttl, TimeUnit timeunit)
      {
         throw new UnsupportedOperationException("SafeMap.tryPut");
      }

      @Override
      public void putTransient(Object key, Object value, long ttl, TimeUnit timeunit)
      {
         throw new UnsupportedOperationException("SafeMap.tryPut");
      }

      @Override
      public Object putIfAbsent(Object key, Object value)
      {
         try
         {
            return delegate.putIfAbsent(key, value);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return null;
      }

      @Override
      public Object putIfAbsent(Object key, Object value, long ttl, TimeUnit timeunit)
      {
         throw new UnsupportedOperationException("SafeMap.putIfAbsent");
      }

      @Override
      public boolean replace(Object key, Object oldValue, Object newValue)
      {
         try
         {
            return delegate.replace(key, oldValue, newValue);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return false;
      }

      @Override
      public Object replace(Object key, Object value)
      {
         try
         {
            return delegate.replace(key, value);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return null;
      }

      @Override
      public void set(Object key, Object value, long ttl, TimeUnit timeunit)
      {
         throw new UnsupportedOperationException("SafeMap.set");
      }

      @Override
      public Object tryLockAndGet(Object key, long time, TimeUnit timeunit)
            throws TimeoutException
      {
         throw new UnsupportedOperationException("SafeMap.tryLockAndGet");
      }

      @Override
      public void putAndUnlock(Object key, Object value)
      {
         throw new UnsupportedOperationException("SafeMap.putAndUnlock");
      }

      @Override
      public void lock(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.lock");
      }

      @Override
      public boolean isLocked(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.isLocked");
      }

      @Override
      public boolean tryLock(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.tryLock");
      }

      @Override
      public boolean tryLock(Object key, long time, TimeUnit timeunit)
      {
         throw new UnsupportedOperationException("SafeMap.tryLock");
      }

      @Override
      public void unlock(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.unlock");
      }

      @Override
      public void forceUnlock(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.forceUnlock");
      }

      @Override
      public boolean lockMap(long time, TimeUnit timeunit)
      {
         throw new UnsupportedOperationException("SafeMap.lockMap");
      }

      @Override
      public void unlockMap()
      {
         throw new UnsupportedOperationException("SafeMap.unlockMap");
      }

      @Override
      public void addLocalEntryListener(EntryListener<Object, Object> listener)
      {
         throw new UnsupportedOperationException("SafeMap.addLocalEntryListener");
      }

      @Override
      public void addEntryListener(EntryListener<Object, Object> listener,
            boolean includeValue)
      {
         throw new UnsupportedOperationException("SafeMap.addEntryListener");
      }

      @Override
      public void removeEntryListener(EntryListener<Object, Object> listener)
      {
         throw new UnsupportedOperationException("SafeMap.removeEntryListener");
      }

      @Override
      public void addEntryListener(EntryListener<Object, Object> listener, Object key,
            boolean includeValue)
      {
         throw new UnsupportedOperationException("SafeMap.addEntryListener");
      }

      @Override
      public void removeEntryListener(EntryListener<Object, Object> listener, Object key)
      {
         throw new UnsupportedOperationException("SafeMap.removeEntryListener");
      }

      @Override
      public MapEntry<Object, Object> getMapEntry(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.getMapEntry");
      }

      @Override
      public boolean evict(Object key)
      {
         throw new UnsupportedOperationException("SafeMap.evict");
      }

      @Override
      public Set<Object> keySet()
      {
         try
         {
            return delegate.keySet();
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return null;
      }

      @Override
      public Collection<Object> values()
      {
         try
         {
            return delegate.values();
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return null;
      }

      @Override
      public Set<Entry<Object, Object>> entrySet()
      {
         try
         {
            return delegate.entrySet();
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         return null;
      }

      @Override
      public Set<Object> keySet(Predicate predicate)
      {
         throw new UnsupportedOperationException("SafeMap.keySet");
      }

      @Override
      public Set<Entry<Object, Object>> entrySet(Predicate predicate)
      {
         throw new UnsupportedOperationException("SafeMap.entrySet");
      }

      @Override
      public Collection<Object> values(Predicate predicate)
      {
         throw new UnsupportedOperationException("SafeMap.values");
      }

      @Override
      public Set<Object> localKeySet()
      {
         throw new UnsupportedOperationException("SafeMap.localKeySet");
      }

      @Override
      public Set<Object> localKeySet(Predicate predicate)
      {
         throw new UnsupportedOperationException("SafeMap.localKeySet");
      }

      @Override
      public void addIndex(String attribute, boolean ordered)
      {
         throw new UnsupportedOperationException("SafeMap.addIndex");
      }

      @Override
      public void addIndex(Expression< ? > expression, boolean ordered)
      {
         throw new UnsupportedOperationException("SafeMap.addIndex");
      }

      @Override
      public LocalMapStats getLocalMapStats()
      {
         throw new UnsupportedOperationException("SafeMap.getLocalMapStats");
      }
   }

   private static final Logger trace = LogManager.getLogger(HazelcastCacheAdapter.class);

   public static final String KEY_CURRENT_CONNECTION = HazelcastCacheAdapter.class.getName() + ".CurrentConnection";

   public static final String PRP_HAZELCAST_GLOBAL_CACHE_NAME = "Infinity.Engine.Caching.Hazelcast.GlobalCacheName";

   private final String txMode;

   private final ConnectionFactory hzCf;

   @SuppressWarnings("rawtypes")
   public HazelcastCacheAdapter(String txMode, ConnectionFactory hzCf, Map config)
   {
      this.txMode = txMode;
      this.hzCf = hzCf;

      Parameters params = Parameters.instance();
      String cacheName = params.getString(PRP_HAZELCAST_GLOBAL_CACHE_NAME, "ipp-2nd-level-cache");
      if (StringUtils.isEmpty(cacheName))
      {
         cacheName = (String) config.get("name");
      }

      delegate = createSafeMap(cacheName);

      if (trace.isDebugEnabled())
      {
         if (trace.isInfoEnabled())
         {
            trace.info("Successfully bound Hazelcast cache \"" + delegate.getName()
                  + "\" for \"" + config.get("name") + "\".");
         }
      }
   }

   protected IMap<Object, Object> createSafeMap(String cacheName)
   {
      return new SafeMap(cacheName);
   }

   @Override
   protected void beforeCacheAccess(boolean read)
   {
      if ("rw".equals(txMode) || ("w".equals(txMode) && !read))
      {
         final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         if (rtEnv != null)
         {
            try
            {
               rtEnv.retrieveJcaConnection(hzCf);
            }
            catch (final ResourceException e)
            {
               throw new PublicException(
                     BpmRuntimeError.HZLC_FAILES_ENLISTING_HAZLECAST_CACHE_IN_CURRENT_TRANSACTION
                           .raise(), e);
            }
         }
      }
   }
}
