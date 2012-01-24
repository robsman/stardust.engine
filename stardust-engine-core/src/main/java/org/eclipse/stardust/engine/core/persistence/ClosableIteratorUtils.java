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

import java.util.List;

import org.eclipse.stardust.common.Assert;


/**
 * This class implements little helper method for working with 
 * ClosableIterators.
 *
 * @author Robert Sauer
 * @version $Revision$
 */
public class ClosableIteratorUtils
{
   /**
    * Performs a close if the given iterator is not <code>null</code>.
    * 
    * @param iterator the iterator that will be tested and closed if possible
    */
   public static final void closeSafely(ClosableIterator iterator)
   {
      if (null != iterator)
      {
         iterator.close();
      }
   }

   /**
    * Copies a ClosableIterator into a vector and close the iterator to release
    * underlying resultset.
    *
    * @param list the list in which the result is awaited
    * @param iterator the closable iterator to copy
    * @return a List containing the objects represented by the iterator
    */
   public static List copyResult(List list, ClosableIterator iterator)
   {
      Assert.isNotNull(list, "list argument is expected to be not null");

      while(iterator.hasNext())
      {
         list.add(iterator.next());
      }

      closeSafely(iterator);

      return list;
   }
}
