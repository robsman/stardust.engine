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
package org.eclipse.stardust.common.config.extensions;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.config.extensions.PojoExtensionsUtils.instantiatePojoExtension;

import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.stardust.common.config.ConfigLog;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.log.Logger;


public class PojoExtensionsCache<T> extends AbstractList<T>
{
   public static Logger extLog = ConfigLog.EXTENSIONS_LOG;

   private final List<ValueProvider<T>> extensions;

   public PojoExtensionsCache()
   {
      this.extensions = newArrayList();
   }

   public PojoExtensionsCache(int initialCapacity)
   {
      this.extensions = newArrayList(initialCapacity);
   }

   public <E extends T> void addExtensionProvider(Class<E> extensionType,
         boolean stateless)
   {
      if (stateless)
      {
         extensions.add(new StatelessExtensionProvider<T, E>(extensionType));
      }
      else
      {
         extensions.add(new StatefulExtensionProvider<T, E>(extensionType));
      }
   }

   @Override
   public int size()
   {
      return extensions.size();
   }

   @Override
   public T get(int index)
   {
      return extensions.get(index).getValue();
   }

   static class StatelessExtensionProvider<T, E extends T> implements ValueProvider<T>
   {
      private final Class<E> instanceType;

      private final AtomicReference<T> sharedInstance;

      public StatelessExtensionProvider(Class<E> instanceType)
      {
         this.instanceType = instanceType;
         this.sharedInstance = new AtomicReference<T>();
      }

      public T getValue()
      {
         if (null == sharedInstance.get())
         {
            if (extLog.isDebugEnabled())
            {
               extLog.debug("About to initialize shared instance for stateless SPI provider "
                     + instanceType);
            }

            sharedInstance.compareAndSet(null, instantiatePojoExtension(instanceType));
         }

         return sharedInstance.get();
      }
   }

   static class StatefulExtensionProvider<T, E extends T> implements ValueProvider<T>
   {
      private final Class<E> instanceType;

      public StatefulExtensionProvider(Class<E> instanceType)
      {
         this.instanceType = instanceType;
      }

      public T getValue()
      {
         if (extLog.isDebugEnabled())
         {
            extLog.debug("About to initialize one-time instance for stateful SPI provider "
                  + instanceType);
         }

         T instance = instantiatePojoExtension(instanceType);

         return instance;
      }
   }
}
