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
import java.util.NoSuchElementException;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class OneElementIterator<E> implements Iterator<E>
{
   private E element;
   private boolean consumed;

   public OneElementIterator(E element)
   {
      this.element = element;
   }

   public boolean hasNext()
   {
      return !consumed;
   }

   public E next()
   {
      if (consumed)
      {
         throw new NoSuchElementException();
      }
      consumed = true;
      return element;
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }
}
