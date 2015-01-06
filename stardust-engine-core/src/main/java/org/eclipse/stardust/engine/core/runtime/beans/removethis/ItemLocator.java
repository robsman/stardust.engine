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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.spi.cluster.Watcher;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ItemLocator
{
   private static final Logger trace = LogManager.getLogger(ItemLocator.class);
   
   private static final Object INITIALIZATION_LOCK = new Object();
   
   private final Watcher watcher;
   private final ItemLoader loader;
   
   private final AtomicReference initializationLock = new AtomicReference();

   private final AtomicReference item = new AtomicReference();

   public ItemLocator(ItemDescription description)
   {
      this.watcher = description.getWatcher();
      this.loader = description.getLoader();
   }

   public Object get()
   {
      if ((null == item.get()) || watcher.isDirty())
      {
         synchronized (initializationLock)
         {
            if ((null == item.get()) || watcher.isDirty())
            {
               if (initializationLock.compareAndSet(null, INITIALIZATION_LOCK))
               {
                  final Object state = watcher.getGlobalState();

                  item.set(loader.load());
                  watcher.updateState(state);

                  if (!initializationLock.compareAndSet(INITIALIZATION_LOCK, null))
                  {
                     trace.warn("Race condition while initializing item from loader '"
                           + loader + "'.");
                  }
               }
            }
         }
      }

      return item.get();
   }

   public void markDirty()
   {
      if (watcher != null)
      {
         watcher.setDirty();
      }
   }
   
}

