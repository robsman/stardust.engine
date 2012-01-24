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


public class EmptyResultSetIterator implements ResultIterator
{
   public static final EmptyResultSetIterator INSTANCE = new EmptyResultSetIterator();

   public boolean hasNext()
   {
      return false;
   }

   public Object next()
   {
      return null;
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }

   public void close()
   {
   }

   public boolean hasMore()
   {
      return false;
   }

   public int getStartIndex()
   {
      return 0;
   }

   public int getMaxSize()
   {
      return 0;
   }

   public boolean hasTotalCount()
   {
      return false;
   }

   public long getTotalCount() throws UnsupportedOperationException
   {
      throw new UnsupportedOperationException("Total item count not available.");
   }
}