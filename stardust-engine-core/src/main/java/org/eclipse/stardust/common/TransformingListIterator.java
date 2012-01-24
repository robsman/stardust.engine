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

import java.util.Iterator;
import java.util.List;

/**
 * An iterator wrapping another iterator and transforming it's contents according to
 * the provided transforming functor.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TransformingListIterator<S, T> implements Iterator<T>
{
   private final List<? extends S> source;
   
   private int i;
   
   private S nextElement;

   private final Functor<S, T> transformer;

   private final Predicate<S> filter;

   public TransformingListIterator(List<? extends S> source, Functor<S, T> transformer)
   {
      this(source, transformer, null);
   }
   
   public TransformingListIterator(List<? extends S> source, Functor<S, T> transformer, Predicate<S> filter)
   {
      this.source = source;
      this.i = 0;

      this.transformer = transformer;
      
      this.filter = filter;
      
      prepareNext();
   }

   public boolean hasNext()
   {
      return null != nextElement;
   }

   public T next()
   {
      T result = transformer.execute(nextElement);
      
      prepareNext();
      
      return result;
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }
   
   private void prepareNext()
   {
      this.nextElement = null;
      
      while (i < source.size())
      {
         S element = source.get(i++);
         
         if ((null == filter) || filter.accept(element))
         {
            this.nextElement = element;
            break;
         }
      }
   }
}
