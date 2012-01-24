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


import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.table.DefaultTableCellRenderer;

import org.eclipse.stardust.engine.core.compatibility.gui.GUI;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.awt.Color;

public class SortableTable extends JTable
{
   private boolean ascending;
   private int sortingColumn = -1;

   private Icon imageArrowEmpty = GUI.getIcon("images/ArrowEmpty.gif");
   private Icon imageArrowDown = GUI.getIcon("images/ArrowDownRight.gif");
   private Icon imageArrowUp = GUI.getIcon("images/ArrowUpRight.gif");
   private MouseAdapter popupMouseListener;

   public SortableTable(SortableTableModel dm)
   {
      super(dm);

      // (fh) extend DefaultTableCellRenderer because it has rendering optimizations
      // compared with JLabel
      DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
      {
         public Component getTableCellRendererComponent(JTable table, Object value,
               boolean isSelected, boolean hasFocus, int row, int column)
         {
            if (sortingColumn == column)
            {
               setIcon(ascending ? imageArrowUp : imageArrowDown);
            }
            else
            {
               setIcon(imageArrowEmpty);
            }
            setText(value == null ? "" : value.toString());
            return this;
         }
      };
      renderer.setBorder(new BasicBorders.FieldBorder(Color.white,
            Color.white, Color.gray, Color.gray));
//    renderer.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      getTableHeader().setDefaultRenderer(renderer);

      getTableHeader().addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            if (!GUI.isPopupTrigger(e))
            {
               int column = getTableHeader().columnAtPoint(e.getPoint());
               if (column != sortingColumn)
               {
                  sortingColumn = column;
               }
               else
               {
                  ascending = !ascending;
               }
               getTableHeader().resizeAndRepaint();
               SortableTableModel model = (SortableTableModel) getModel();
               model.sort(sortingColumn, ascending);
            }
         }
      });
   }

   public void cancelSorting()
   {
      sortingColumn = -1;
      ascending = false;
      getTableHeader().resizeAndRepaint();
      SortableTableModel model = (SortableTableModel) getModel();
      model.sort(null);
   }

   public void setPopupMenu(final JPopupMenu popupMenu)
   {
      if (popupMouseListener != null)
      {
         removeMouseListener(popupMouseListener);
      }
      if (popupMenu != null)
      {
         addMouseListener(popupMouseListener = new MouseAdapter()
         {
            public void mousePressed(MouseEvent e)
            {
               if (GUI.isPopupTrigger(e))
               {
                  int row = rowAtPoint(e.getPoint());
                  if (row >= 0)
                  {
                     getSelectionModel().setSelectionInterval(row, row);
                     GUI.showPopup(popupMenu, SortableTable.this, e.getX(), e.getY());
                  }
               }
            }
         });
      }
   }
}
