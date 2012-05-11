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

import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.io.Serializable;

import org.eclipse.stardust.common.error.InternalException;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ScopedList<K> implements Serializable
{
   private static final long serialVersionUID = -8077429044994430782L;

   private boolean sloppy;

   private HashMap<String,List<K>> scopes = CollectionUtils.newHashMap();

   public ScopedList(boolean sloppy)
   {
      this.sloppy = sloppy;
   }

   public void clear()
   {
      scopes.clear();
   }

   public void set(String scope, Collection<? extends K> elements)
   {
      List<K> scopeImpl = getScope(scope);
      scopeImpl.clear();
      if (elements != null)
      {
         scopeImpl.addAll(elements);
      }
   }

   public void addScope(String scope)
   {
      List<K> scopeImpl = scopes.get(scope);
      if (scopeImpl == null)
      {
         scopes.put(scope, new LinkedList<K>());
      }
   }

   public Iterator<K> getIterator(String scope)
   {
      return getScope(scope).iterator();
   }

   private List<K> getScope(String scope)
   {
      List<K> scopeImpl = scopes.get(scope);
      if (scopeImpl == null)
      {
         if (sloppy)
         {
            scopeImpl = new LinkedList<K>();
            scopes.put(scope, scopeImpl);
         }
         else
         {
            throw new InternalException("Scope '" + scope + "' not found.");
         }
      }
      return scopeImpl;
   }

   public void add(String scope, K object)
   {
      getScope(scope).add(object);
   }
}
