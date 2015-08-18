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

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/** Custom Header Renderer for sorted tables. */
public class SortedTableHeaderRenderer extends JLabel
      implements TableCellRenderer
{
   // vars needed for rendering image icons
   ImageIcon imageArrowEmpty, imageArrowDown, imageArrowUp;

   private int sortingColumn = -1;
   private boolean ascending;

   private Border border;

   /** */
   public SortedTableHeaderRenderer()
   {
      imageArrowEmpty = new ImageIcon(getClass().getResource("images/ArrowEmpty.gif"));
      imageArrowDown = new ImageIcon(getClass().getResource("images/ArrowDownRight.gif"));
      imageArrowUp = new ImageIcon(getClass().getResource("images/ArrowUpRight.gif"));

      // Set the border

      border = new javax.swing.plaf.basic.BasicBorders.FieldBorder(Color.white,
            Color.white,
            Color.gray,
            Color.gray);
   }

   /** Set the column that did the current sorting. */
   public void setSortingColumn(int sortingColumn, boolean ascending)
   {
      this.sortingColumn = sortingColumn;
      this.ascending = ascending;
   }

   /** implement the TableCellRenderer */
   public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
   {
      setBorder(border);

      String text = (String) value;
      setText(text);

      if (sortingColumn > 0 && table.convertColumnIndexToModel(column) == sortingColumn)
      {
         if (!ascending)
         {
            setIcon(imageArrowDown);
         }
         else
         {
            setIcon(imageArrowUp);
         }
      }
      else
      {
         setIcon(imageArrowEmpty);
      }

      return this;
   }
}
