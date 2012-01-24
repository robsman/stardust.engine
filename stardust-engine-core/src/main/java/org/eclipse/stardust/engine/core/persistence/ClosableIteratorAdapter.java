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
package org.eclipse.stardust.engine.core.persistence;

import java.util.Iterator;

/**
 * @author sauer
 * @version $Revision$
 */
public class ClosableIteratorAdapter implements ClosableIterator
{
   private final Iterator delegate;

   public static ClosableIterator newIteratorAdapter(Iterator delegate)
   {
      return new ClosableIteratorAdapter(delegate);
   }
   
   public ClosableIteratorAdapter(Iterator delegate)
   {
      this.delegate = delegate;
   }

   public boolean hasNext()
   {
      return delegate.hasNext();
   }

   public Object next()
   {
      return delegate.next();
   }

   public void remove()
   {
      delegate.remove();
   }

   public void close()
   {
      // TODO ignore
   }
   
}
