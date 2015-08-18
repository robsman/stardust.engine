/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rsauer
 * @version $Revision$
 */
public final class RawQueryResult<T> extends AbstractQueryResult<T>
{
   private final SubsetPolicy subset;
   
   private boolean hasTotalCount;

   public RawQueryResult(List<T> itemList, SubsetPolicy subset, boolean hasMore)
   {
      this(itemList, subset, hasMore, null, Long.MAX_VALUE);
   }
   
   public RawQueryResult(List<T> itemList, SubsetPolicy subset, boolean hasMore,
         Long totalCount)
   {
      this(itemList, subset, hasMore, totalCount, Long.MAX_VALUE);
   }

   public RawQueryResult(List<T> itemList, SubsetPolicy subset, boolean hasMore,
         Long totalCount, long totalCountThreshold)
   {
      super(null, new ArrayList<T>(itemList), hasMore, totalCount, totalCountThreshold);

      this.subset = subset;
      this.hasTotalCount = (null != totalCount);
   }

   public List<T> getItemList()
   {
      return items;
   }

   public SubsetPolicy getSubsetPolicy()
   {
      return subset;
   }

   public boolean hasTotalCount()
   {
      return hasTotalCount;
   }
}
