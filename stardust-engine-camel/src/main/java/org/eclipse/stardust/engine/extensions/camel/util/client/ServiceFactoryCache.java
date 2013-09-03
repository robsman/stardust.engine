/*
 * $Id: $
 * (C) 2000 - 2010 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.util.client;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.runtime.internal.SessionManager;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class ServiceFactoryCache
{
   private static final Logger trace = LogManager.getLogger(ServiceFactoryCache.class);

   private final HashMap<CacheKey, CacheEntry> sfCacheStore = newHashMap();

   private final LinkedList<CacheEntry> expiredEntries = newLinkedList();

   // The default setting in IPP's SessionManager is currently 60 min.
   // To be on the safe side, it is set to 30 min. default for cached ServiceFactories.
   private final long expirationInterval = Parameters.instance().getLong(
         SessionManager.PRP_SESSION_EXPIRATION_INTERVAL, 30L * 60L * 1000L);

   private INewServiceFactoryProvider sfProvider;

   public ServiceFactoryCache(INewServiceFactoryProvider sfProvider)
   {
      this.sfProvider = sfProvider;
   }

   public ServiceFactoryCache()
   {

   }

/**
 * @param userId
 * @param password
 * @param properties
 * @return a service factory
 */
public synchronized ServiceFactory getServiceFactory(String userId, String password, Map<String, ? > properties)
   {
      CacheKey sfKey = new CacheKey(userId, password, properties);

      CacheEntry entry = null;

      while (null == entry)
      {
         entry = sfCacheStore.get(sfKey);

         if (null != entry)
         {
            if (entry.isExpired())
            {
               // release expired service factory
               sfCacheStore.remove(sfKey);

               if (!entry.isInUse())
               {
                  entry.close();
               }
               else
               {
                  expiredEntries.add(entry);
               }

               entry = null;
            }
         }

         if (null != entry)
         {
            trace.debug("Reusing cached service factory for user " + userId);
         }
         else
         {
            entry = getNewServiceFactory(sfKey);
         }
      }

      if (null != entry)
      {
         entry.acquire();
      }

      return (null != entry) ? entry.sf : null;
   }

   public synchronized void release(ServiceFactory sf)
   {
      for (CacheEntry entry : sfCacheStore.values())
      {
         if (entry.sf == sf)
         {
            // release service factory, but keep in cache
            entry.release();
            return;
         }
      }

      // remove expired session factories when ref count reaches zero
      for (Iterator<CacheEntry> i = expiredEntries.iterator(); i.hasNext();)
      {
         CacheEntry entry = i.next();

         if (entry.sf == sf)
         {
            // finally release service factory
            entry.release();
            if (!entry.isInUse())
            {
               entry.close();
               i.remove();
            }
            return;
         }
      }

      // no cached service factory
      sf.close();
   }

/**
 * returns an entry. If there is a preferred way to get the ServiceFactory, use it
 * otherwise the Web ServiceFactoryLocator is used by default
 * 
 * @param sfKey
 * @return entry
 */
private synchronized CacheEntry getNewServiceFactory(CacheKey sfKey)
   {
      CacheEntry entry = sfCacheStore.get(sfKey);
      if (null == entry)
      {
         // if there is a preferred way to get the ServiceFactory, use it
         if (null != sfProvider)
         {
            entry = new CacheEntry(sfKey, sfProvider.getServiceFactory(sfKey.userId, sfKey.password, sfKey.properties));
         }
         // otherwise the Web ServiceFactoryLocator is used by default
         else
         {
            entry = new CacheEntry(sfKey, ServiceFactoryLocator.get(sfKey.userId, sfKey.password, sfKey.properties));
         }
         sfCacheStore.put(sfKey, entry);

         trace.debug("Using new service factory for user " + sfKey.userId);
      }
      else
      {
         trace.debug("Reusing cached service factory for user " + sfKey.userId);
      }

      return entry;
   }

   private static class CacheKey
   {
      final String userId;

      final String password;

      final Map<String, ? > properties;

      public CacheKey(String userId, String password, Map<String, ? > properties)
      {
         this.userId = userId;
         this.password = password;
         this.properties = properties;
      }

      /**
       * Returns a hash code value for the object. 
       *
       * @return  a hash code value for this object.
       */
    @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((password == null) ? 0 : password.hashCode());
         result = prime * result + ((properties == null) ? 0 : properties.hashCode());
         result = prime * result + ((userId == null) ? 0 : userId.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         CacheKey other = (CacheKey) obj;
         if (password == null)
         {
            if (other.password != null)
               return false;
         }
         else if (!password.equals(other.password))
            return false;
         if (properties == null)
         {
            if (other.properties != null)
               return false;
         }
         else if (!properties.equals(other.properties))
            return false;
         if (userId == null)
         {
            if (other.userId != null)
               return false;
         }
         else if (!userId.equals(other.userId))
            return false;
         return true;
      }
   }

   private class CacheEntry
   {
      final CacheKey key;

      final ServiceFactory sf;

      final AtomicLong refCount = new AtomicLong();

      final AtomicLong lastAccessTime = new AtomicLong();

      public CacheEntry(CacheKey key, ServiceFactory sf)
      {
         this.key = key;
         this.sf = sf;

         lastAccessTime.set(System.currentTimeMillis());
      }

      boolean isExpired()
      {
         return System.currentTimeMillis() > lastAccessTime.get() + expirationInterval;
      }

      void acquire()
      {
         refCount.incrementAndGet();
         lastAccessTime.set(System.currentTimeMillis());
      }

      boolean isInUse()
      {
         return 0L < this.refCount.get();
      }

      void release()
      {
         this.refCount.decrementAndGet();
      }

      void close()
      {
         if (!isInUse())
         {
            // release expired service factory
            try
            {
               trace.debug("Closing expired service factory for user " + key.userId);

               sf.close();
            }
            catch (Exception e)
            {
               trace.debug("Failure while closing expired service factory. This will be ignored.", e);
            }
         }
         else
         {
            trace.warn("Ignoring close of service factory still being in use.");
         }
      }
   }

   public void setNewServiceFactoryProvider(INewServiceFactoryProvider sfProvider)
   {
      this.sfProvider = sfProvider;
   }
}