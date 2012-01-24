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
package org.eclipse.stardust.common.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.error.InternalException;


/**
 * @author fherinean
 * @version $Revision$
 */
abstract class AbstractPropertyCache
{
   private final AbstractPropertyCache predecessor;
   private AbstractPropertyCache successor;

   private final Map<String, Object> cache;

   AbstractPropertyCache(AbstractPropertyCache predecessor)
   {
      this(predecessor, false);
   }

   AbstractPropertyCache(AbstractPropertyCache predecessor, boolean cacheValues)
   {
      this.predecessor = predecessor;
      
      if (null != predecessor)
      {
         if (null != predecessor.successor)
         {
            throw new InternalException("Overwriting non null successor.");
         }
         
         predecessor.successor = this;
      }
      
      this.cache = cacheValues ? new ConcurrentHashMap<String, Object>() : null;
   }

   public Object get(String name)
   {
      Object value = (null != cache) ? cache.get(name) : null;
      
      if (null == value)
      {
         value = resolveProperty(name);

         if ((null == value) && (null != predecessor))
         {
            value = predecessor.get(name);
         }

         if (null != cache)
         {
            cache.put(name, (null == value) ? Parameters.NULL_VALUE : value);
         }
      }

      return (Parameters.NULL_VALUE == value) ? null : value;
   }

   protected abstract Object resolveProperty(String name);

   protected void uncacheProperty(String name)
   {
      if (null != cache)
      {
         cache.remove(name);
      }

      if (null != successor)
      {
         successor.uncacheProperty(name);
      }
   }

   AbstractPropertyCache getPredecessor()
   {
      return predecessor;
   }

   void resetSuccessor()
   {
      this.successor = null;
   }
}
