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
package org.eclipse.stardust.common;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import org.eclipse.stardust.common.error.InternalException;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ScopedMap<K,V> implements Serializable
{
   private static final long serialVersionUID = -2355594882525862290L;

   HashMap<String,Map<K,V>> scopes = CollectionUtils.newHashMap();
   boolean sloppy;

   public ScopedMap(boolean sloppy)
   {
      this.sloppy = sloppy;
   }

   public void set(String scope, K name, V value)
   {
      getScope(scope).put(name, value);
   }

   private Map<K,V> getScope(String scope)
   {
      Map<K,V> scopeImpl = scopes.get(scope);
      if (scopeImpl == null)
      {
         if (sloppy)
         {
            scopeImpl = new HashMap<K,V>();
            scopes.put(scope, scopeImpl);
         }
         else
         {
            throw new InternalException("Scope '" + scope + "' not found.");
         }
      }
      return scopeImpl;
   }

   public Map<K,V> getMap(String scope)
   {
      Map<K,V> result = scopes.get(scope);
      if (result == null)
      {
         return new HashMap<K,V>();
      }
      return result;
   }

   public V get(String scope, String key)
   {
      Map<K,V> scopeImpl = scopes.get(scope);
      if (scopeImpl == null)
      {
         return null;
      }
      return scopeImpl.get(key);
   }

   public void set(String scope, Map<? extends K,? extends V> attributes)
   {
      Map<K,V> scopeImpl = getScope(scope);
      scopeImpl.clear();

      if (attributes != null)
      {
         scopeImpl.putAll(attributes);
      }
   }

   public void clear()
   {
      scopes.clear();
   }

   public void addScope(String scope)
   {
      Map<K,V> scopeImpl = scopes.get(scope);
      if (scopeImpl == null)
      {
         scopes.put(scope, new HashMap<K,V>());
      }
   }
}
