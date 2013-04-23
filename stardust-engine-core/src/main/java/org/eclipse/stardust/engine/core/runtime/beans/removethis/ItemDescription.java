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

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.runtime.beans.NullWatcher;
import org.eclipse.stardust.engine.core.spi.cluster.Watcher;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ItemDescription
{

   private final ItemLoader loader;

   private final String watcher;

   public ItemDescription(ItemLoader loader)
   {
      this(loader, NullWatcher.class.getName());
   }

   public ItemDescription(ItemLoader loader, String watcher)
   {
      this.loader = loader;
      this.watcher = watcher;
   }

   public ItemLoader getLoader()
   {
      return loader;
   }
   
   public Watcher getWatcher()
   {
      try
      {
         return (Watcher) Reflect.getClassFromClassName(watcher).newInstance();
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

}

