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

import java.util.Map;

public interface Cache
{

   public interface Factory
   {
      public Cache createCache(Map env) throws CacheException;
   }

   Object remove(Object key);

   boolean containsKey(Object key);

   void clear();

   Object put(Object key, Object value);

   Object get(Object key);

   Object putIfAbsent(Object key, Object value);
   
   boolean isEmpty();

}
