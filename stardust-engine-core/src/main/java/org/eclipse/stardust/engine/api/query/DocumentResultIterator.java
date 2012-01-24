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
package org.eclipse.stardust.engine.api.query;

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;


public class DocumentResultIterator implements ResultIterator
{
   private List<Document> result;

   private int startIndex;

   private int maxSize;

   private boolean hasMore;

   private Long totalCount;

   private Iterator<Document> iterator;

   public DocumentResultIterator(List<Document> result, int startIndex, int maxSize, long totalCount)
   {
      this.result = result;
      this.maxSize = maxSize < 0 ? Integer.MAX_VALUE : maxSize;
      this.startIndex = startIndex;
      this.totalCount = totalCount < 0 ? null : totalCount;

      this.hasMore = (totalCount < 0 || result.size() < totalCount) ? true : false;

      this.iterator = result.iterator();
   }

   public boolean hasNext()
   {
      return iterator.hasNext();
   }

   public Object next()
   {
      return iterator.next();
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
      // result.iterator().remove();
   }

   /**
    * Releases the result set explicitely.
    */
   public void close()
   {
   }

   public int getStartIndex()
   {
      return startIndex;
   }

   public int getMaxSize()
   {
      return maxSize;
   }

   public boolean hasMore()
   {
      return hasMore;
   }

   public boolean hasTotalCount()
   {
      return null != totalCount;
   }

   public long getTotalCount() throws UnsupportedOperationException
   {
      if (null == totalCount)
      {
         throw new UnsupportedOperationException("Total item count not available.");
      }
      return totalCount.longValue();
   }

}
