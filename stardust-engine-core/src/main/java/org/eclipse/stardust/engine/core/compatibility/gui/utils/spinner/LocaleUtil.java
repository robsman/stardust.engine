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
package org.eclipse.stardust.engine.core.compatibility.gui.utils.spinner;

import java.awt.FontMetrics;
import java.text.FieldPosition;
import java.text.Format;

/**
 * @author rsauer
 * @version $Revision$
 */
public class LocaleUtil
{
   public static void sortFieldOrder(Format formatter, Object obj, int[] fieldIDs)
   {
      int size = fieldIDs.length;
      int[] order = new int[size];
      for (int i = 0; i < size; i++)
      {
         order[i] = getFieldPosition(formatter, obj, fieldIDs[i]).getBeginIndex();
      }
      sort(fieldIDs, order);
   }

   public static int findMouseInField(FontMetrics metrics, int x,
         Format formatter, Object obj, int[] fieldIDs)
   {
      String text = formatter.format(obj);
      int size = fieldIDs.length;
      FieldPosition pos;
      for (int i = 0; i < size; i++)
      {
         pos = getFieldPosition(formatter, obj, fieldIDs[i]);
         int left = metrics.stringWidth(text.substring(0, pos.getBeginIndex()));
         int right = metrics.stringWidth(text.substring(0, pos.getEndIndex()));
         if (x >= left && x <= right)
         {
            return fieldIDs[i];
         }
      }
      return fieldIDs[0];
   }

   public static FieldPosition getFieldPosition(Format formatter, Object obj, int field)
   {
      FieldPosition pos = new FieldPosition(field);
      StringBuffer buffer = new StringBuffer();
      formatter.format(obj, buffer, pos);
      return pos;
   }

   private static void sort(int[] fieldIDs, int[] order)
   {
      sort(fieldIDs, order, 0, fieldIDs.length - 1);
   }

   private static void sort(int[] fieldIDs, int[] order, int first, int last)
   {
      if (first >= last) return;
      int lo = first, hi = last;
      int mid = order[(first + last) / 2];
      int tmp, temp;
      do
      {
         while (mid > order[lo]) lo++;
         while (mid < order[hi]) hi--;
         if (lo <= hi)
         {
            tmp = order[lo];
            temp = fieldIDs[lo];
            order[lo] = order[hi];
            fieldIDs[lo] = fieldIDs[hi];
            lo++;
            order[hi] = tmp;
            fieldIDs[hi] = temp;
            hi--;
         }
      }
      while (lo <= hi);
      sort(fieldIDs, order, first, hi);
      sort(fieldIDs, order, lo, last);
   }

   public static void main(String[] args)
   {
      int[] order = {2, 8, 12, 6, 10, 4};
      int[] fieldIDs = {1, 4, 6, 3, 5, 2};
      sort(fieldIDs, order);
      for (int i = 0; i < fieldIDs.length; i++)
      {
         System.out.print(fieldIDs[i] + "; ");
      }
   }

   private LocaleUtil()
   {
      // utility class
   }
}
