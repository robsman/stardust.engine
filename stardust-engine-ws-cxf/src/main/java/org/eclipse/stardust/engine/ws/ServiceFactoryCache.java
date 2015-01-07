/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2010 CARNOT AG
 */
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.web.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;


/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class ServiceFactoryCache
{
   private static final Logger trace = LogManager.getLogger(ServiceFactoryCache.class);

   private final HashMap<CacheKey, CacheEntry> sfCacheStore = newHashMap();

   private final LinkedList<CacheEntry> expiredEntries = newLinkedList();

   private final long expirationInterval = Parameters.instance().getInteger(
		   "Carnot.WebService.SessionCache.Timeout", 300 /* 5m */) * 1000L;

   private final long reAuthenticationInterval = Parameters.instance().getInteger(
         "Carnot.WebService.SessionCache.ReAuthenticationInterval", -1);

   private final boolean updateOnAccess = Parameters.instance().getBoolean(
		   "Carnot.WebService.SessionCache.RenewOnAccess", true);

   public synchronized ServiceFactory getServiceFactory(String userId, String password,
         Map<String, ? > properties)
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
      for (Iterator<CacheEntry> i = expiredEntries.iterator(); i.hasNext(); )
      {
         CacheEntry entry = i.next();

         if (entry.sf == sf)
         {
            // finally release service factory
            entry.release();
            if ( !entry.isInUse())
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

   public synchronized void invalidateForPartition(String partitionId)
   {
      for (Iterator<Entry<CacheKey, CacheEntry>> iter = sfCacheStore.entrySet().iterator(); iter.hasNext();)
      {
         Entry<CacheKey, CacheEntry> entry = iter.next();
         Map<String, ?> properties = entry.getKey().properties;
         if (properties == null)
         {
            continue;
         }

         if (partitionId.equals(properties.get(SecurityProperties.PARTITION)))
         {
            iter.remove();
         }
      }
   }

   private synchronized CacheEntry getNewServiceFactory(CacheKey sfKey)
   {
      CacheEntry entry = sfCacheStore.get(sfKey);
      if (null == entry)
      {
         entry = new CacheEntry(sfKey, ServiceFactoryLocator.get(sfKey.userId,
               sfKey.password, sfKey.properties));

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
         else if ( !password.equals(other.password))
            return false;
         if (properties == null)
         {
            if (other.properties != null)
               return false;
         }
         else if ( !properties.equals(other.properties))
            return false;
         if (userId == null)
         {
            if (other.userId != null)
               return false;
         }
         else if ( !userId.equals(other.userId))
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

      final AtomicLong lastAuthTime = new AtomicLong();

      public CacheEntry(CacheKey key, ServiceFactory sf)
      {
         this.key = key;
         this.sf = sf;

         lastAccessTime.set(System.currentTimeMillis());
         lastAuthTime.set(System.currentTimeMillis());
      }

      boolean isExpired()
      {
         return System.currentTimeMillis() > lastAccessTime.get() + expirationInterval;
      }

      void acquire()
      {
         refCount.incrementAndGet();
         long newAccessTime = System.currentTimeMillis();
         if (updateOnAccess)
         {
        	lastAccessTime.set(newAccessTime);
         }
         synchronized (lastAuthTime)
         {
            if (reAuthenticationInterval > -1 && (newAccessTime - lastAuthTime.get() > reAuthenticationInterval))
            {
               setReauthAttributes();
               lastAuthTime.set(newAccessTime);
            }
         }
      }

      private void setReauthAttributes()
      {
         InvokerPrincipal current = InvokerPrincipalUtils.getCurrent();
         if (current == null)
         {
            Map<Object, Object> props = CollectionUtils.newMap();
            props.put(AbstractLoginInterceptor.REAUTH_OUTER_PRINCIPAL, null);
            current = new InvokerPrincipal(key.userId, props);
         }

         @SuppressWarnings("unchecked")
         Map<String, String> properties = current.getProperties();

         properties.put(AbstractLoginInterceptor.REAUTH_USER_ID, key.userId);
         properties.put(AbstractLoginInterceptor.REAUTH_PASSWORD, key.password);

         InvokerPrincipalUtils.setCurrent(current);
      }

      private void removeReauthProperties()
      {
         InvokerPrincipal current = InvokerPrincipalUtils.getCurrent();
         if (current != null)
         {
            @SuppressWarnings("unchecked")
            Map<String, String> properties = current.getProperties();

            if (properties != null)
            {
               if (properties.containsKey(AbstractLoginInterceptor.REAUTH_USER_ID))
               {
                  properties.remove(AbstractLoginInterceptor.REAUTH_USER_ID);
                  properties.remove(AbstractLoginInterceptor.REAUTH_PASSWORD);
                  InvokerPrincipalUtils.setCurrent(current);
               }
               else if (properties.containsKey(AbstractLoginInterceptor.REAUTH_OUTER_PRINCIPAL))
               {
                  InvokerPrincipalUtils.removeCurrent();
               }
            }

         }
      }

      boolean isInUse()
      {
         return 0L < this.refCount.get();
      }

      void release()
      {
         this.refCount.decrementAndGet();
         removeReauthProperties();
      }

      void close()
      {
         if ( !isInUse())
         {
            // release expired service factory
            try
            {
               trace.debug("Closing expired service factory for user " + key.userId);

               sf.close();
            }
            catch (Exception e)
            {
               trace.debug(
                     "Failure while closing expired service factory. This will be ignored.",
                     e);
            }
         }
         else
         {
            trace.warn("Ignoring close of service factory still being in use.");
         }
      }
   }
}
