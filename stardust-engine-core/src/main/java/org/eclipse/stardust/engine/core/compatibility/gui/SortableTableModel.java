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
package org.eclipse.stardust.engine.core.compatibility.gui;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public abstract class SortableTableModel extends AbstractTableModel
{
   private String[] headers;
   private Comparator comparator;

   protected Vector cache = new Vector();

   public SortableTableModel(String[] headers)
   {
      this.headers = headers;
   }

   public void setHeaders(String[] headers)
   {
      this.headers = headers;
      fireTableStructureChanged();
   }

   public void setData(Collection data)
   {
      this.cache.clear();
      if (data != null)
      {
         this.cache.addAll(data);
      }
      if (comparator != null)
      {
         sort();
      }
      else
      {
         // we want to avoid firing table data changed twice !
         fireTableDataChanged();
      }
   }

   public void sort(Comparator comparator)
   {
      this.comparator = comparator;
      sort();
   }

   protected void sort()
   {
      if (comparator != null)
      {
         Collections.sort(cache, comparator);
         fireTableDataChanged();
      }
   }

   public int getRowCount()
   {
      return cache.size();
   }

   public int getColumnCount()
   {
      return headers.length;
   }

   public String getColumnName(int col)
   {
      return headers[col];
   }

   public Object getValueAt(int row, int col)
   {
      Object item = getObject(row);
      return item == null ? null : getCellValue(item, col);
   }

   public Object getObject(int row)
   {
      return row < 0 || row >= cache.size() ? null : cache.get(row);
   }

   public Comparator getComparator(final int column, final boolean ascending)
   {
      return new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            Object v1 = getCellValue(o1, column);
            Object v2 = getCellValue(o2, column);
            Comparable c1 = v1 instanceof Comparable ?
                  (Comparable) v1 : v1 == null ? "" : v1.toString();
            Comparable c2 = v2 instanceof Comparable ?
                  (Comparable) v2 : v2 == null ? "" : v2.toString();
            return ascending ? c1.compareTo(c2) : c2.compareTo(c1);
         }
      };
   }

   public void sort(int sortingColumn, boolean ascending)
   {
      sort(getComparator(sortingColumn, ascending));
   }

   public abstract Object getCellValue(Object item, int col);
}
