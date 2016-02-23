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
package org.eclipse.stardust.engine.core.model.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MultiHook extends Hook implements ModelElementList
{
   private final List elements = CollectionUtils.newList();

   private final List roView = Collections.unmodifiableList(elements);

   private Map idIndex = CollectionUtils.newMap();

   public MultiHook(ModelElement owner)
   {
      super(owner);
   }

   public void add(ModelElement element)
   {
      elements.add(element);

      if ((null != idIndex) && (element instanceof Identifiable))
      {
         Identifiable identifiable = (Identifiable) element;
         if ( !idIndex.containsKey(identifiable.getId()))
         {
            idIndex.put(identifiable.getId(), element);
         }
         else
         {
            // duplicate IDs, disable cache
            this.idIndex = null;
         }
      }

   }

   public void remove(ModelElement element)
   {
      if ((null != idIndex) && (element instanceof Identifiable))
      {
         Assert.condition(element == idIndex.get(((Identifiable) element).getId()));

         idIndex.remove(((Identifiable) element).getId());
      }

      elements.remove(element);
   }

   public void remove(String id)
   {
      ModelElement victim = findById(id);
      if (victim != null)
      {
         remove(victim);
      }
   }

   public Iterator iterator()
   {
      //return CollectionUtils.newList(elements).iterator();
      return roView.iterator();
   }

   public int size()
   {
      return elements.size();
   }

   public boolean isEmpty()
   {
      return elements.isEmpty();
   }

   public ModelElement get(int index)
   {
      return (ModelElement) elements.get(index);
   }

   public ModelElement findById(String id)
   {
      if (null != idIndex)
      {
         ModelElement element = (ModelElement) idIndex.get(id);
         if (null != element)
         {
            return element;
         }
      }

      for (int i = 0; i < elements.size(); ++i)
      {
         ModelElement modelElement = (ModelElement) elements.get(i);
         if (CompareHelper.areEqual(((Identifiable) modelElement).getId(), id))
         {
            return modelElement;
         }
      }
      return null;
   }

   public ModelElement findById(String id, Class filterClass)
   {
      ModelElement element = findById(id);
      if (filterClass.isInstance(element))
      {
         return element;
      }
      
      for (int i = 0; i < elements.size(); ++i)
      {
         element = (ModelElement) elements.get(i);
         if (((Identifiable)element).getId().equals(id)
               && filterClass.isInstance(element))
         {
            return element;
         }
      }
      return null;
   }

   public boolean contains(ModelElement element)
   {
      return elements.contains(element);
   }

   public int size(Class filterClass)
   {
      int result = 0;
      for (int i = 0; i < elements.size(); ++i)
      {
         ModelElement element = (ModelElement) elements.get(i);
         if (filterClass.isInstance(element))
         {
            result++;
         }
      }
      return result;
   }

   public Iterator iterator(final Class filterClass)
   {
      return new FilteringIterator(iterator(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return filterClass.isInstance(o);
         }
      });
   }

   // @todo (france, ub): this doesn't gracefully handle references
   public void clear()
   {
      elements.clear();

      if (null != idIndex)
      {
         idIndex.clear();
      }
      else
      {
         this.idIndex = CollectionUtils.newMap();
      }
   }

   public void addAll(Collection collection)
   {
      for (Iterator i = collection.iterator(); i.hasNext();)
      {
         add((ModelElement) i.next());
      }
   }

/*
   public void clearTransientElements()
   {
      for (Iterator i = iterator();i.hasNext();)
      {
         IdentifiableElement element = (IdentifiableElement) i.next();
         if (element.isTransient())
         {
            elements.remove(element);
         }
      }
   }
*/

}
