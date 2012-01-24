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

import java.util.*;

public class MultiIterator<E> implements Iterator<E>
{
   private Iterator<Iterable<E>> main;
   private Iterator<E> sub;

   public MultiIterator(Collection<Iterable<E>> iterables)
   {
      this(iterables.iterator());
   }

   public MultiIterator(Iterable<E>... iterables)
   {
      this(Arrays.asList(iterables).iterator());
   }

   public MultiIterator(Iterator<Iterable<E>> iterables)
   {
      main = iterables;
   }

   public boolean hasNext()
   {
      while (sub == null || !sub.hasNext())
      {
         if (!main.hasNext())
         {
            return false;
         }
         sub = main.next().iterator();
      }
      return sub.hasNext();
   }

   public E next()
   {
      while (sub == null || !sub.hasNext())
      {
         sub = main.next().iterator();
      }
      return sub.next();
   }

   public void remove()
   {
      throw new UnsupportedOperationException("remove");
   }
}
