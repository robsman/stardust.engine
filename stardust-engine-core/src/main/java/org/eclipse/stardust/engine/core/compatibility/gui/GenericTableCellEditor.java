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

import java.awt.event.*;
import java.awt.Component;
import java.util.EventObject;

import javax.swing.table.TableCellEditor;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.CellEditorListener;

/**
 *
 */
class GenericTableCellEditor implements TableCellEditor, ActionListener,
      FocusListener,
      KeyListener
{
   private JTable table;
   private EventListenerList listenerList;
   private JComponent component;
   private JComponent wrappedComponent;
   private ChangeEvent changeEvent;
   private int clickCountToStart;
   private Object value;

   /**
    *  The wrapped component controls the apperance and the behavior
    * (focus handling etc.); the component itself controls the data flow.
    */
   public GenericTableCellEditor(Class columnType)
   {
      component = GuiMapper.getComponentForClass(columnType, true);

      if (component instanceof Entry)
      {
         wrappedComponent = ((Entry) component).getWrappedComponent();
      }

      wrappedComponent.setBorder(null);

      if (wrappedComponent instanceof JTextField)
      {
         ((JTextField) wrappedComponent).addActionListener(this);
      }
      //		wrappedComponent.addKeyListener(this); **/

      listenerList = new EventListenerList();
      ChangeEvent changeEvent = null;
      clickCountToStart = 1;
   }

   /**
    * Add a listener to the list that's notified when the editor starts, stops,
    * or cancels editing.
    */
   public void addCellEditorListener(CellEditorListener l)
   {
      listenerList.add(CellEditorListener.class, l);
   }

   /**
    * Remove a listener from the list that's notified.
    */
   public void removeCellEditorListener(CellEditorListener l)
   {
      listenerList.remove(CellEditorListener.class, l);
   }

   /**
    *
    */
   private void fireEditingStopped()
   {
      Object[] listeners = listenerList.getListenerList();

      for (int n = listeners.length - 2; n >= 0; n -= 2)
      {
         if (listeners[n] == CellEditorListener.class)
         {
            if (changeEvent == null)
            {
               changeEvent = new ChangeEvent(this);
            }

            ((CellEditorListener) listeners[n + 1]).editingStopped(changeEvent);
         }
      }
   }

   /**
    *
    */
   private void fireEditingCanceled()
   {
      Object[] listeners = listenerList.getListenerList();

      for (int n = listeners.length - 2; n >= 0; n -= 2)
      {
         if (listeners[n] == CellEditorListener.class)
         {
            if (changeEvent == null)
            {
               changeEvent = new ChangeEvent(this);
            }

            ((CellEditorListener) listeners[n + 1]).editingCanceled(changeEvent);
         }
      }
   }

   /**
    * Returns the value contained in the editor.
    */
   public Object getCellEditorValue()
   {
      return value;
   }

   /**
    * Sets the value contained in the editor.
    */
   public void setCellEditorValue(Object value)
   {
      this.value = value;
   }

   /**
    * Ask the editor if it can start editing using anEvent.
    */
   public boolean isCellEditable(EventObject anEvent)
   {
      if (anEvent == null)
      {
         return true;
      }

      if (anEvent instanceof MouseEvent)
      {
         if (((MouseEvent) anEvent).getClickCount() < clickCountToStart)
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Tell the editor to cancel editing and not accept any partially edited value.
    */
   public void cancelCellEditing()
   {
      fireEditingCanceled();
   }

   /**
    * Tell the editor to start editing using anEvent.
    */
   public boolean shouldSelectCell(EventObject anEvent)
   {
      return true;
   }

   /**
    * Tell the editor to stop editing and accept any partially edited value
    * as the value of the editor.
    */
   public boolean stopCellEditing()
   {
      if (component instanceof Entry)
      {
         setCellEditorValue(((Entry) component).getObjectValue());
      }

      fireEditingStopped();

      return true;
   }

   /**
    * Sets an initial value for the editor.
    */
   public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected, int row,
         int column)
   {
      if (component instanceof Entry)
      {
         ((Entry) component).setObjectValue(value);
      }

      this.table = table;

      wrappedComponent.requestFocus();

      return wrappedComponent;
   }

   /**
    * Invoked when a component loses the keyboard focus.
    */
   public void actionPerformed(ActionEvent e)
   {
      fireEditingStopped();
   }

   /**
    * Invoked when a component gains the keyboard focus.
    */
   public void focusGained(FocusEvent e)
   {
   }

   /**
    * Invoked when a component loses the keyboard focus.
    */
   public void focusLost(FocusEvent e)
   {
      table.editCellAt(table.getEditingRow(), table.getEditingColumn() + 1);
   }

   /**
    * Invoked when a key has been pressed.
    */
   public void keyPressed(KeyEvent e)
   {
   }

   /**
    * Invoked when a key has been released.
    */
   public void keyReleased(KeyEvent e)
   {
      switch (e.getKeyCode())
      {
         case KeyEvent.VK_ENTER:
            {
               table.editCellAt(table.getEditingRow() + 1, table.getEditingColumn());
            }
      }
   }

   /**
    * Invoked when a key has been typed.
    */
   public void keyTyped(KeyEvent e)
   {
   }
}
