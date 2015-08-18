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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/** Renderer for Buttons in a table */
public class ButtonRenderer extends JButton implements TableCellRenderer,
      TableCellEditor
{
   // if actionCounter < -1 => no ActionListener added => no ActionEvent
   private int actionCounter;
   // actual triggered row
   private int actionRow;

   String leadText = "";
   // store default colors
   Color defaultBackgroundColor = getBackground();
   Color defaultForegroundColor = getForeground();
   // event var
   protected EventListenerList listenerList = new EventListenerList();

   /** Default constructor */
   public ButtonRenderer()
   {
      super();
      setMargin(new Insets(0, 0, 0, 0));

      actionCounter = -2; // <=> no actionListener added
   }

   /** constructor accepting an ActionListener */
   public ButtonRenderer(ActionListener actionListener)
   {
      super();
      setMargin(new Insets(0, 0, 0, 0));

      addActionListener(actionListener);
   }

   /** Can add an ActionListener */
   public void addActionListener(ActionListener actionListener)
   {
      actionCounter = 0;
      actionRow = Integer.MIN_VALUE;

      super.addActionListener(actionListener);
   }

   /** Removes an ActionListener */
   public void removeActionListener(ActionListener actionListener)
   {
      actionCounter = 0;
      actionRow = Integer.MIN_VALUE;

      super.removeActionListener(actionListener);
   }

   /** set the leading text in front of button text
    * @param leadText The text to be used
    */
   public void setLeadText(String leadText)
   {
      this.leadText = leadText;
   }

   /** Returns the leading text */
   public String getLeadText()
   {
      return leadText;
   }

   /** implements TableCellRenderer */
   public java.awt.Component getTableCellRendererComponent(JTable table,
         Object value,
         boolean isSelected,
         boolean hasFocus,
         int row, int column)
   {

      if (isSelected)
      {
         setBackground(table.getSelectionBackground());
         setForeground(table.getSelectionForeground());
      }
      else
      {
         setBackground(defaultBackgroundColor);
         setForeground(defaultForegroundColor);
      }

      if (value != null)
      {
         setText(leadText + value.toString());
      }

      setMaximumSize(new Dimension(52, getMaximumSize().height));

      return this;
   }

   /** implements TableCellEditor // needed for doublclick ... in case Table needs
    *  only one click for edit-mode
    */
   public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected,
         int row, int column)
   {
      if (row == actionRow)
      {
         actionCounter++;
      }
      else // row != actionRow
      {
         actionCounter = 0;
         actionRow = row;
      }

      if (isSelected && actionCounter >= 1)
      {
         fireActionPerformed(new ActionEvent(this, 0, "" + row));
         actionCounter = -1;
      }

      return this;
   }

   /** listener handling */
   public void addCellEditorListener(CellEditorListener l)
   {
      listenerList.add(CellEditorListener.class, l);
   }

   /** listener handling */
   public void removeCellEditorListener(CellEditorListener l)
   {
      listenerList.remove(CellEditorListener.class, l);
   }

   /** cell editing handling */
   public void cancelCellEditing()
   {
      //fireEditingCanceled();
   }

   /** cell editing handling */
   public Object getCellEditorValue()
   {
      return this;
   }

   /** cell editing handling */
   public boolean isCellEditable(EventObject anEvent)
   {
      return false;
   }

   /** cell editing handling */
   public boolean shouldSelectCell(EventObject anEvent)
   {
      return true;
   }

   /** cell editing handling */
   public boolean stopCellEditing()
   {
      return true;
   }
}// ButtonRenderer
