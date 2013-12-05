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
package org.eclipse.stardust.engine.core.runtime.beans.removethis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;


/**
 * @author sauer
 * @version $Revision$
 */
public class ItemLocatorUtils
{
   static final String PREFIX_LOCATOR = ItemLocator.class.getName() + ". Locator.";

   // descriptions are cached locally to prevent them from being flushed with parameters
   static final ConcurrentHashMap descriptions = new ConcurrentHashMap();

   static final AtomicReference mmDescription = new AtomicReference();

   // caches the global cache key per type to avoid heavy string concatenation
   private static final ConcurrentHashMap itemTypeCacheKeys = new ConcurrentHashMap();

   private static final String MM_CACHE_KEY = ItemLocatorUtils.class.getName()
         + ".cacheKey." + ModelManagerFactory.ITEM_NAME;

   public static synchronized void registerDescription(String itemType, ItemDescription description)
   {
      if (ModelManagerFactory.ITEM_NAME.equals(itemType))
      {
         mmDescription.set(description);
      }
      else
      {
         descriptions.put(itemType, description);
      }
   }

   public static ItemDescription getDescription(String itemType)
   {
      return (ModelManagerFactory.ITEM_NAME == itemType)
            ? (ItemDescription) mmDescription.get()
            : (ItemDescription) descriptions.get(itemType);
   }
   
   public static synchronized void unregisterDescription(String itemType)
   {
      if (ModelManagerFactory.ITEM_NAME.equals(itemType))
      {
         mmDescription.set(null);
      }
      else
      {
         descriptions.remove(itemType);
      }
   }

   public static ItemLocator getLocator(String itemType)
   {
      final GlobalParameters globals = GlobalParameters.globals();

      ItemLocator locator = (ItemLocator) globals.get(getLocatorCacheKey(itemType));
      
      if (null == locator)
      {
         final ItemDescription description = getDescription(itemType);
         if (null == description)
         {
            throw new InternalException("Unknown service type: '" + itemType + "'.");
         }
         
         locator = (ItemLocator) globals.initializeIfAbsent(getLocatorCacheKey(itemType),
               new ValueProvider()
               {
                  public Object getValue()
                  {
                     return new ItemLocator(description);
                  }
               });
      }
      
      return locator;
   }
   
   private static String getLocatorCacheKey(String itemType)
   {
      if (ModelManagerFactory.ITEM_NAME == itemType)
      {
         return MM_CACHE_KEY;
      }
      
      String cacheKey = (String) itemTypeCacheKeys.get(itemType);
      if (null == cacheKey)
      {
         // initialization is cheap, so avoid extra locking
         cacheKey = ItemLocatorUtils.class.getName() + ".cacheKey." + itemType;

         itemTypeCacheKeys.putIfAbsent(itemType, cacheKey);
      }

      return cacheKey;
   }

}
