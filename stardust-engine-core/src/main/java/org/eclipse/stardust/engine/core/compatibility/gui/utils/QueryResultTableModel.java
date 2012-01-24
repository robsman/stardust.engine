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


import java.util.Iterator;

import org.eclipse.stardust.engine.core.compatibility.gui.SortableTableModel;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.FetchListener;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.QueryResultWrapper;

public abstract class QueryResultTableModel extends SortableTableModel
{
   private Iterator data;
   private FetchListener listener;

   public QueryResultTableModel(String[] headers)
   {
      this(null, headers);
   }

   public QueryResultTableModel(FetchListener listener, String[] headers)
   {
      super(headers);
      this.listener = listener;
   }

   public void setData(Iterator data)
   {
      cache.clear();
      this.data = data;
      fetch();
   }
   
   public void setFetchListener(FetchListener listener)
   {
      this.listener = listener;
   }

   private void fetch()
   {
      for (int i = 0; i < QueryResultWrapper.FETCH_SIZE && data.hasNext(); i++)
      {
         cache.add(data.next());
      }
      if (listener != null)
      {
         if (data.hasNext())
         {
            listener.partialFetchPerformed(super.getRowCount());
         }
         else
         {
            listener.fetchCompleted(super.getRowCount());
         }
      }
      fireTableDataChanged();
   }

   public int getRowCount()
   {
      int count = super.getRowCount();
      // 200 should be enough to ensure there is no flickering.
      if (data != null && data.hasNext())
      {
         count += 200;
      }
      return count;
   }

   public Object getObject(int row)
   {
      if (row >= cache.size())
      {
         fetch();
      }
      return super.getObject(row);
   }
}
