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
package org.eclipse.stardust.engine.core.model.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;

/**
 * IMPORTANT: this class overrides used methods.
 * If you use modifying methods of the ArrayList that are not overridden here,
 * please make sure that id2ValueMap consistency is maintained.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class SearchableList<T extends IdentifiableElement> extends ArrayList<T> implements ModelElementList<T>
{
   private static final long serialVersionUID = 1L;

   private Map<String, T> id2ValueMap = CollectionUtils.newMap();

   @Override
   public boolean add(T o)
   {
      id2ValueMap.put(o.getId(), o);
      return super.add(o);
   }

   public T find(String id)
   {
      return id2ValueMap.get(id);
   }

   @Override
   public boolean remove(Object o)
   {
      id2ValueMap.remove(((T) o).getId());
      return super.remove(o);
   }

   public <I extends T> I find(String id, Class<I> clazz)
   {
      I o = (I) find(id);
      return clazz.isInstance(o) ? o : null;
   }

   public <I extends T> int size(final Class<I> clazz)
   {
      int size = 0;
      for (T o : this)
      {
         if (clazz.isInstance(o))
         {
            size++;
         }
      }
      return size;
   }
   
   public <I extends T> Iterator<I> iterator(final Class<I> clazz)
   {
      Predicate<T> predicate = new Predicate<T>()
      {
         public boolean accept(T o)
         {
            return clazz.isInstance(o);
         }
      };
      return new FilteringIterator(iterator(), predicate);
   }
}
