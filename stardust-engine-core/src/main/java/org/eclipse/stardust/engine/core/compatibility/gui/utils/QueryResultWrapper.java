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
package org.eclipse.stardust.engine.core.compatibility.gui.utils;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;


public abstract class QueryResultWrapper extends AbstractCollection
{
   private static final Logger trace = LogManager.getLogger(QueryResultWrapper.class);

   private final int fetchSize;

   private QueryResult result;

   // This Size has been arbitrarily selected so high that hasNext() on inner
   // collection would always return true except when collection returned is empty
   // Though size() could be overridden by the subclasses to return specific size.
   // If we know the exact size of the total concrete collection size, We could eliminate
   //the check for returning false if inncerCollectionsize is less than the result req.
   private static int SIZE = 100000;

   public static final int FETCH_SIZE = 30;

   protected QueryResultWrapper(int fetchSize)
   {
      this.fetchSize = fetchSize;
   }

   /**
    * Retrieves an iterator that traverses the inner Collection.
    */
   public synchronized Iterator iterator()
   {
      if (null == result)
      {
         result = fetchItems();
      }

      return new QueryResultIterator(result);
   }

   /**
    *
    */
   public boolean isEmpty()
   {
      return 0 == result.size();
   }

   /**
    *  The following abstract method is implemented by each type of CollectionWrapper.
    *  Actually gets more Collection for the iterator
    */
   private QueryResult fetchItems()
   {
      return fetchMoreItems(new SubsetPolicy(fetchSize + 1));
   }

   protected abstract QueryResult fetchMoreItems(SubsetPolicy currentSubset);

   /**
    *
    */
   public int size()
   {
      return SIZE;
   }

   /**
    *
    */
   private class QueryResultIterator implements Iterator
   {
      private QueryResult result;
      private Iterator items;

      //Index of element to be returned by subsequent call to next.

      public QueryResultIterator(QueryResult result)
      {
         this.result = result;
         this.items = result.iterator();
      }

      public boolean hasNext()
      {
         return items.hasNext();
      }

      public Object next()
      {
         try
         {
            Object next = items.next();

            if (!items.hasNext() && result.hasMore())
            {
               result = fetchMoreItems(SubsetPolicy.nextChunk(result.getSubsetPolicy()));
               items = result.iterator();
            }

            return next;
         }
         catch (Exception e)
         {
            trace.warn("", e);
            throw new NoSuchElementException("Failed retrieving next element.");
         }
      }

      public void remove()
      {
         // not implemented
      }
   }
}