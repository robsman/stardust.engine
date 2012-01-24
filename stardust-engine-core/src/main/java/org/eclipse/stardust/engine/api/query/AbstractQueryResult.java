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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractQueryResult<T> extends AbstractList<T>
      implements QueryResult<T>, Serializable
{
   private static final long serialVersionUID = 7571257843718415055L;

   protected final Query query;
   protected final List<T> items;
   protected final boolean hasMore;
   
   private final Long totalCount;

   protected AbstractQueryResult(Query query, List<T> items, boolean hasMore, Long totalCount)
   {
      this.query = query;
      this.items = items;
      this.hasMore = hasMore;
      
      this.totalCount = totalCount;
   }

   public T get(int index)
   {
      return items.get(index);
   }

   public int size()
   {
      return items.size();
   }

   public long getSize()
   {
      return items.size();
   }
   
   protected boolean hasTotalCount()
   {
      return null != totalCount;
   }

   public long getTotalCount() throws UnsupportedOperationException
   {
      if (null == totalCount)
      {
         throw new UnsupportedOperationException("Total item count is not available.");
      }
      return totalCount.longValue();
   }

   public SubsetPolicy getSubsetPolicy()
   {
      return (SubsetPolicy) query.getPolicy(SubsetPolicy.class);
   }

   public boolean hasMore()
   {
      return hasMore;
   }
}
