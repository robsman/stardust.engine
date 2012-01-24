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
package org.eclipse.stardust.common.reflect;

import java.awt.BorderLayout;
import java.lang.reflect.Array;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/** Programmed bei Dingfelder needs review!!

 Implements a table which is created and populated based on the type of
 objects whose data are to be displayed in this table. */
public class Table extends JPanel
{
   protected JTable table;
   protected AbstractTableModel dataModel;
   protected Vector columnsVector;
   protected Object array;

   public Table(Vector newColumnsVector)
   {
      // Constructor of the superclass

      super();

      this.columnsVector = newColumnsVector;

      // Layout initialization

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      // Create the table data model

      dataModel = new AbstractTableModel()
      {
         public int getColumnCount()
         {
            return columnsVector.size();
         }

         public int getRowCount()
         {
            if (array != null)
            {
               return Array.getLength(array);
            }

            return 0;
         }

         public String getColumnName(int column)
         {
            if (column < 0)
               return "";

            return ((Column) columnsVector.elementAt(column)).name;
         }

         public Class getColumnClass(int column)
         {
            if (column < 0)
               return String.class;

            if (((Column) columnsVector.elementAt(column)).columnType == Boolean.TYPE)
            {
               return Boolean.class;
            }

            return String.class;
            //		return ((Column)columnsVector.elementAt(column)).columnType;
         }

         public boolean isCellEditable(int row, int column)
         {
            if (column < 1)
               return false;
            else
               return true;
         }

         public Object getValueAt(int rowIndex,
               int columnIndex)
         {
            if (columnIndex == 0)
            {
               String S = "" + rowIndex;

               return S;
            }

            DereferencePath path = ((Column) columnsVector.elementAt(columnIndex)).path;

            if (path == null)
            {
               return Array.get(array,
                     rowIndex);
            }
            else
            {
               return path.getValue(Array.get(array,
                     rowIndex));
            }
         }

         public void setValueAt(Object newValue,
               int rowIndex,
               int columnIndex)
         {
            DereferencePath path = ((Column) columnsVector.elementAt(columnIndex)).path;

            Object newCastedValue = null;

            Class columnType = ((Column) columnsVector.elementAt(columnIndex)).columnType;

            try
            {
               if (columnType == String.class
                     || columnType == Boolean.TYPE
                     || columnType == Boolean.class)
               {
                  newCastedValue = newValue;
               }
               else if (columnType == Byte.TYPE
                     || columnType == Byte.class)
               {
                  newCastedValue = Byte.valueOf((String) newValue);
               }
               else if (columnType == Integer.TYPE
                     || columnType == Integer.class)
               {
                  newCastedValue = Integer.valueOf((String) newValue);
               }
               else if (columnType == Short.TYPE
                     || columnType == Short.class)
               {
                  newCastedValue = Short.valueOf((String) newValue);
               }
               else if (columnType == Long.TYPE
                     || columnType == Long.class)
               {
                  newCastedValue = Long.valueOf((String) newValue);
               }
               else if (columnType == Float.TYPE
                     || columnType == Float.class)
               {
                  newCastedValue = Float.valueOf((String) newValue);
               }
               else if (columnType == Double.TYPE
                     || columnType == Double.class)
               {
                  newCastedValue = Double.valueOf((String) newValue);
               }
            }
            catch (ClassCastException exception)
            {
               newCastedValue = newValue;
            }
            catch (java.lang.NumberFormatException exception)
            {
               newCastedValue = newValue;
            }

            if (path == null)
            {
               try
               {
                  Array.set(array,
                        rowIndex,
                        newCastedValue);
               }
               catch (IllegalArgumentException e)
               {
               }
            }
            else
            {
               try
               {
                  path.setValue(Array.get(array,
                        rowIndex),
                        newCastedValue);
               }
               catch (IllegalArgumentException e)
               {
               }

            }
         }
      }; // AbstractTableModel

      // Create the table

      table = new JTable(dataModel);
      /*
      {
         public void validate()
         {
            System.out.println("validate");
            resizeAndRepaint();
            super.validate();
         }
      };
      */
      table.setAutoCreateColumnsFromModel(true);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

      // Put the table and header into a scrollable pane

      JScrollPane scrollpane = new JScrollPane(table);

      // Speed up resizing repaints by turning off live cell updates

      table.getTableHeader().setUpdateTableInRealTime(false);

      // Add scrollpane

      add(scrollpane,
            BorderLayout.CENTER);
   }

   /** */
   public void setValue(Object newArray)
   {
      array = newArray;
      table.validate();
   }

   /** */
   public Object getValue()
   {
      return array;
   }
}
